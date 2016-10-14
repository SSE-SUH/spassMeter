package de.uni_hildesheim.sse.monitoring.runtime.recordingServer;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import de.uni_hildesheim.sse.monitoring.runtime.configuration.Configuration;
import de.uni_hildesheim.sse.monitoring.runtime.recordingStrategies.*;
import de.uni_hildesheim.sse.monitoring.runtime.recordingStrategiesElements.*;

/**
 * Implements a server for receiving measurement events. Therefore the baseDir
 * for the measurement results and the port on which the server listen must be
 * specified in the program arguments (i.e. baseDir=..\\measurement, port=6002).
 * 
 * @author Stephan Dederichs
 * @version 1.00
 * @since 1.00
 */
public class TCPRecordingServer {

    /**
     * Defines the baseDir argument.
     */
    private static final String PARAM_BASEDIR = "baseDir=";
    
    /**
     * Defines the port argument.
     */
    private static final String PARAM_PORT = "port=";
    
    /**
     * Stores elements for all received data.
     */
    private List<RecordingStrategiesElement> elements;
    
    /**
     * Stores the {@link RecorderStrategy}.
     */
    private RecorderStrategy strategy;
    
    /**
     * Stores the count of enterConfiguration events.
     */
    private int enterConfiguration = 0;
    
    /**
     * Stores the count of enter events.
     */
    private int enter = 0;
    
    /**
     * Stores the count of exit events.
     */
    private int exit = 0;
    
    /**
     * Stores the count of finishRecording events.
     */
    private int finishRecording = 0;
    
    /**
     * Stores the count of memoryAllocated events.
     */
    private int memoryAllocated = 0;
    
    /**
     * Stores the count of memoryFreedById events.
     */
    private int memoryFreedById = 0;
    
    /**
     * Stores the count of memoryFreed events.
     */
    private int memoryFreed = 0;
    
    /**
     * Stores the count of printStatistics events.
     */
    private int printStatistics = 0;
    
    /**
     * Stores the count of io events.
     */
    private int io = 0;
    
    /**
     * Stores the count of startRecording events.
     */
    private int startRecording = 0;
    
    /**
     * Stores the count of stopTimeRecording events.
     */
    private int stopTimeRecording = 0;
    
    /**
     * Stores the count of enterconfiguration events.
     */
    private int endsystem = 0;

    /**
     * Receives the data send from the client. 
     * 
     * @param baseDir the baseDir
     * @param port the port
     * 
     * @since 1.00
     */
    private TCPRecordingServer(String baseDir, int port) {
        elements = Collections
                .synchronizedList(new LinkedList<RecordingStrategiesElement>());
        // Instancing the RecorderStrategy
        strategy = new DefaultRecorderStrategy(new TabFormatter());
        // Creating the WorkThread
        Thread workThread = new WorkThread();
        workThread.start();
        // Stores the frequency of events
        long count = 0;
        // true if the server should receive events
        boolean receive = true;
        try {
            // Instancing the ServerSocket on the given port
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("TCPRecordingServer startet...");
            System.out.println(" queue size: '.' < 20, ',' < 50, " 
                + "'*' < 100, o >= 100");
            while (receive) {
                Socket s = serverSocket.accept();
                DataInputStream in = new DataInputStream(s.getInputStream());
                // Reading and setting the configuration from the stream 
                Configuration.INSTANCE.read(in);
                Configuration.INSTANCE.setBaseDir(baseDir);
                
                // Getting the data
                int identification = in.readInt();
                System.out.println(identification);
                while (identification >= 0 && receive) {
                    RecordingStrategiesElement e = null;
                    switch (identification) {
                    case Constants.ENTERCONFIGURATION:
                        e = new EnterConfigurationElement();
                        enterConfiguration++;
                        break;
                    case Constants.ENTER:
                        e = EnterElement.POOL.getFromPool();
                        enter++;
                        break;
                    case Constants.EXIT:
                        e = ExitElement.POOL.getFromPool();
                        exit++;
                        break;
                    case Constants.FINISHRECORDING:
                        e = new FinishRecordingElement();
                        finishRecording++;
                        break;
                    case Constants.MEMORYALLOCATED:
                        e = MemoryAllocatedElement.POOL.getFromPool();
                        memoryAllocated++;
                        break;
                    case Constants.MEMORYFREEDBYRECID:
                        e = MemoryFreedByRecIdElement.POOL.getFromPool();
                        memoryFreedById++;
                        break;
                    case Constants.PRINT_STATISTICS:
                        e = new PrintStatisticsElement();
                        printStatistics++;
                        break;
                    case Constants.IO:
                        e = IoElement.POOL.getFromPool();
                        io++;
                        break;
                    case Constants.STARTRECORDING:
                        e = new StartRecordingElement();
                        startRecording++;
                        break;
                    case Constants.STOP_TIME_RECORDING:
                        e = new StopTimeRecordingElement();
                        stopTimeRecording++;
                        break;
                    case Constants.ENDSYSTEM:
                        e = new EndSystemElement();
                        endsystem++;
                        receive = false;
                        break;
                    case Constants.REGISTER:
                        e = new RegisterElement();
                        break;
                    case Constants.ASSIGNTOALL:
                        e  = new AssignToAllElement();
                        break;
                    case Constants.PRINTCURRENTSTATE:
                        e = new PrintCurrentState();
                        break;
                    case Constants.NOTIFYTIMER:
                        e = new NotifyTimerElement();
                        break;
                    case Constants.NOTIFYVALUE:
                        e = new NotifyValueChangeElement();
                        break;
                    case Constants.PROGRAMRECORDCREATION:
                        e = new NotifyProgramRecordCreationElement();
                        break;
                    default:
                        System.err.println("problem: " + identification);
                        System.exit(0);
                        break;
                    }
                    e.read(in);
                    elements.add(e);
                    count++;
                    // display queue size every 500 times with different symbols
                    // according to the queue size
                    if (count % 500 == 0) {
                        int queueSize = elements.size();
                        if (queueSize < 20) {
                            System.out.print('.');
                        } else if (queueSize < 50) {
                            System.out.print(',');
                        } else if (queueSize < 100) {
                            System.out.print('*');
                        } else {
                            System.out.print('o');
                        }
                        if (count % (500 * 79) == 0) {
                            System.out.println();
                        }
                    }
                    if (receive) {
                        identification = in.readInt();
                    } else {
                        System.out.println();
                    }
                }
            }
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Prints the frequency of the events on the console.
     * 
     * @since 1.00
     */
    private void printElementStatistics() {
        System.out.println("enterconfiguration: " + enterConfiguration);
        System.out.println("enter: " + enter);
        System.out.println("exit: " + exit);
        System.out.println("finishrecording: " + finishRecording);
        System.out.println("memoryallocated: " + memoryAllocated);
        System.out.println("memoryfreedbyid: " + memoryFreedById);
        System.out.println("memoryfreed: " + memoryFreed);
        System.out.println("print_statistics: " + printStatistics);
        System.out.println("io: " + io);
        System.out.println("startrecording: " + startRecording);
        System.out.println("stop_time_recording: " + stopTimeRecording);
        System.out.println("endsystem: " + endsystem);
    }

    /**
     * Implements a thread which processes the received data.
     * 
     * @author Stephan Dederichs
     * @version 1.00
     * @since 1.00
     */
    private class WorkThread extends Thread {

        /**
         * Responsible for processing the received information.
         * 
         * @since 1.00
         */
        @Override
        public void run() {
            boolean end = false;
            while (!end) {
                if (elements.size() > 0) {
                    RecordingStrategiesElement element = elements.remove(0);
                    element.process(strategy);
                    int id = element.getIdentification();
//                    element.clear();
                    element.release();
                    end = (Constants.ENDSYSTEM == id);
                } else {
                    Thread.yield();
                }
            }
            printElementStatistics();
        }
    }

    /**
     * Main method for starting the server. "baseDir" and "port" must be
     * specified in the program arguments. In the baseDir the measurement
     * results will be written and the port specifies the position the server is
     * listening
     * 
     * @param args the program arguments.
     * 
     * @since 1.00
     */
    public static void main(String[] args) {
        String baseDir = null;
        int port = -1;
        // fetching baseDir and port from program arguments
        for (int i = 0; i < args.length; i++) {
            if (args[i].startsWith(PARAM_BASEDIR)) {
                baseDir = args[i].substring(PARAM_BASEDIR.length());
            }
            if (args[i].startsWith(PARAM_PORT)) {
                port = Integer.parseInt(args[i].substring(PARAM_PORT.length()));
            }
        }
        if (null == baseDir) {
            System.err.println("No baseDir given. Exiting.");
        }
        if (-1 == port) {
            System.err.println("No port given. Exiting.");
        }
        new TCPRecordingServer(baseDir, port);
    }
}