package test;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

import de.uni_hildesheim.sse.codeEraser.annotations.Variability;

import test.testing.TestEnvironment;

/**
 * Defines reusable functionalities for the plain network tests. The rationale 
 * behind this class is to annotate the inner server classes differently, once
 * without an annotation (accounted to the program but not the outer class), 
 * once with outer annotation (accounted to program and outer class), once with
 * own annotation (accounted to program and own group).
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
@Variability(id = AnnotationId.VAR_TESTING)
public abstract class AbstractNetIoTest {

    /**
     * Defines the port used for communication operations.
     */
    private static int port = 10101;
    
    /**
     * The size of an int over the data in/out streams.
     */
    private static final int NET_SIZE_INT = 4;
    
    /**
     * The overhead of a string over the data in/out streams (size).
     */
    private static final int NET_OVERHEAD_STRING = 2;

    /**
     * Stores the number of bytes read by the server part.
     */
    private static int serverRead = 0;

    /**
     * Stores the number of bytes written by the server part.
     */
    private static int serverWrite = 0;

    /**
     * Stores the number of bytes read by the client part.
     */
    private static int clientRead = 0;

    /**
     * Stores the number of bytes written by the client part.
     */
    private static int clientWrite = 0;

    /**
     * Prevents this class from being instantiated from outside.
     * 
     * @since 1.00
     */
    protected AbstractNetIoTest() {
    }
    
    /**
     * Implements a simple thread for accepting network connections.
     * 
     * @author Holger Eichelberger
     * @since 1.00
     * @version 1.00
     */
    public static class ServerMainThread extends Thread {

        /**
         * Stores the underlying server socket.
         */
        private ServerSocket serverSocket;
        
        /**
         * Stores if this thread is ready for accepting connections.
         */
        private boolean ready = true;
        
        /**
         * Assigns the server socket.
         * 
         * @param serverSocket the socket used for accepting connections
         * 
         * @since 1.00
         */
        public void setSocket(ServerSocket serverSocket) {
            this.serverSocket = serverSocket;
        }
        
        /**
         * Executes the thread, i.e. waits for incoming connections. We assume
         * that an appropriate SO timeout is set so that this thread my stop
         * after certain seconds of no new connection (for graceful end of 
         * test program).
         */
        public void run() {
            while (ready) {
                try {
                    Thread t = createThread(serverSocket.accept());
                    t.start();
                } catch (SocketTimeoutException e) {
                    ready = false;
                } catch (IOException e) {
                    System.err.println("error while accepting connection");
                    e.printStackTrace();
                } 
            }
        }
        
        /**
         * Factory method for creating a worker thread.
         * 
         * @param serverSocket the socket to work on
         * @return the created thread
         * 
         * @since 1.00
         */
        protected ServerWorkThread createThread(Socket serverSocket) {
            return new ServerWorkThread(serverSocket);
        }
        
    }
    
    /**
     * Implements a thread which is started on an accepted connection. This
     * thread does the communication work for the server.
     * 
     * @author Holger Eichelberger
     * @since 1.00
     * @version 1.00
     */
    public static class ServerWorkThread extends Thread {
        
        /**
         * Stores the socket.
         */
        private Socket socket;
        
        /**
         * Creates the working thread for the given socket.
         * 
         * @param socket the socket to communicate on
         * 
         * @since 1.00
         */
        public ServerWorkThread(Socket socket) {
            this.socket = socket;
        }
        
        /**
         * Performs the communication work, i.e. receives data according to
         * a simple protocol.
         */
        public void run() {
            try {
                DataInputStream in 
                    = new DataInputStream(socket.getInputStream());
                DataOutputStream out 
                    = new DataOutputStream(socket.getOutputStream());
                int totalRead = 0;
                int totalWrite = 0;
                int count = in.readInt();
                totalRead += NET_SIZE_INT;
                for (int i = 1; i <= count; i++) {
                    String string = in.readUTF();
                    totalRead += TestHelper.getUtfLen(string) 
                        + NET_OVERHEAD_STRING;
                }
                out.writeInt(totalRead);
                totalWrite += 2 * NET_SIZE_INT;
                out.writeInt(totalWrite);
            } catch (IOException ioe) {
                System.err.println("Error in server thread");
                ioe.printStackTrace();
            }
        }
    }

    /**
     * Starts the server part.
     *
     * @throws IOException any kind of network I/O problem in the client
     * 
     * @since 1.00
     */
    protected void executeServer() 
        throws IOException {
        TestEnvironment.notice("Starting server on port " + port);
        ServerSocket serverSocket = new ServerSocket(port); 
        serverSocket.setSoTimeout(3000);
        ServerMainThread serverThread = createServerThread();
        serverThread.setSocket(serverSocket);
        serverThread.start();
    }
    
    /**
     * Starts the client part and prints out the results.
     *
     * @throws IOException any kind of network I/O problem in the client
     * 
     * @since 1.00
     */
    protected void executeClient() 
        throws IOException {
        String testString = AbstractNetIoTest.class.getName(); 
        Socket socket = new Socket("localhost", port);
        socket.setSoTimeout(3000); 

        DataInputStream in = new DataInputStream(socket.getInputStream());
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());
        
        int count = 10;
        out.writeInt(count);
        clientWrite = NET_SIZE_INT;
        for (int i = 1; i <= count; i++) {
            out.writeUTF(testString);
            clientWrite += TestHelper.getUtfLen(testString) 
                + NET_OVERHEAD_STRING;
        }
        clientRead = 0;
        serverRead = in.readInt();
        clientRead += NET_SIZE_INT;
        serverWrite = in.readInt();
        clientRead += NET_SIZE_INT;

        TestEnvironment.notice("write " + clientWrite + " server " + serverWrite
            + " total " + (clientWrite + serverWrite));
        TestEnvironment.notice("read " + clientRead + " server " + serverRead 
            + " total " + (clientRead + serverRead));
        
        port++;
    }

    /**
     * Returns the total number of bytes read by the server part.
     * 
     * @return the total number of bytes
     */
    protected static int getServerRead() {
        return serverRead;
    }

    /**
     * Returns the total number of bytes written by the server part.
     * 
     * @return the total number of bytes
     */
    protected static int getServerWrite() {
        return serverWrite;
    }
    
    /**
     * Returns the total number of bytes read by the client part.
     * 
     * @return the total number of bytes
     */
    protected static int getClientRead() {
        return clientRead;
    }

    /**
     * Returns the total number of bytes written by the client part.
     * 
     * @return the total number of bytes
     */
    protected static int getClientWrite() {
        return clientWrite;
    }

    /**
     * Returns the total number of bytes read by client and server.
     * 
     * @return the total number of bytes
     * 
     * @since 1.00
     */
    protected static int getAllRead() {
        return serverRead + clientRead;
    }

    /**
     * Returns the total number of bytes written by client and server.
     * 
     * @return the total number of bytes
     * 
     * @since 1.00
     */
    protected static int getAllWrite() {
        return serverWrite + clientWrite;
    }
    
    /**
     * Factory method for the server main thread.
     * 
     * @return the main thread
     * 
     * @since 1.00
     */
    protected ServerMainThread createServerThread() {
        return new ServerMainThread();
    }
    
    /**
     * The main method to be called from the main method of the test with the
     * appropriate server thread.
     * 
     * @param args the command line arguments
     * @param functions an instance of this class to be executed
     * @throws IOException thrown in case of read/write problems
     * 
     * @since 1.00
     */
    public static void main(String[] args, AbstractNetIoTest functions) 
        throws IOException {
        if (args.length > 0) {
            if (args[0].equals("continue")) {
                for (int i = 0; i < 20; i++) {
                    functions.executeServer();
                    functions.executeClient();
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                System.out.println("invalid parameter: " + args[0]);
            }
        } else {
            functions.executeServer();
            functions.executeClient();
        }
        TestEnvironment.notice("------------------ done: NetIo");
    }
    
}
