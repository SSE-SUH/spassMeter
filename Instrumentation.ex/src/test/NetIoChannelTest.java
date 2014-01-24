package test;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.nio.channels.Channels;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import de.uni_hildesheim.sse.codeEraser.annotations.Variability;
import de.uni_hildesheim.sse.monitoring.runtime.annotations.EndSystem;
import de.uni_hildesheim.sse.monitoring.runtime.annotations.Monitor;
import de.uni_hildesheim.sse.monitoring.runtime.annotations.StartSystem;

/**
 * Perform simple network IO testing of the monitoring infrastructure. This
 * class realizes a simple TCP client as well as a simple server over NIO 
 * channels.<br/>
 * Monitoring must be enabled via the agent JVM parameter!
 * 
 * @author Robin Gawenda, Yilmaz Eldogan
 * @since 1.00
 * @version 1.00
 */
@Variability(id = AnnotationId.VAR_TESTING)
@Monitor(id = "netIoTest")
public class NetIoChannelTest {

    /**
     * Defines the port used for communication operations.
     */
    private static final int PORT = 10101;
    
    /**
     * The size of an int over the data in/out streams.
     */
    private static final int NET_SIZE_INT = 4;
    
    /**
     * The overhead of a string over the data in/out streams (size).
     */
    private static final int NET_OVERHEAD_STRING = 2;

    /**
     * Prevents this class from being instantiated from outside.
     * 
     * @since 1.00
     */
    private NetIoChannelTest() {
    }
    
    /**
     * Implements a simple thread for accepting network connections.
     * 
     * @author Holger Eichelberger
     * @since 1.00
     * @version 1.00
     */
    private static class ServerMainThread extends Thread {

        /**
         * Stores the underlying server socket.
         */
        private ServerSocketChannel ssc;
        
        /**
         * Stores if this thread is ready for accepting connections.
         */
        private boolean ready = true;
        
        /**
         * Creates a new server thread on the given socket.
         * 
         * @since 1.00
         */
        public ServerMainThread() {
            try {
                ssc = ServerSocketChannel.open();
                ssc.socket().bind(new InetSocketAddress(PORT));
            } catch (IOException e) {
                e.printStackTrace();
            }
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
                    SocketChannel sc = ssc.accept();
                    Thread t = new ServerWorkThread(sc);
                    t.start();
                } catch (SocketTimeoutException e) {
                    ready = false;
                } catch (IOException e) {
                    System.err.println("error while accepting connection");
                    e.printStackTrace();
                } 
            }
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
    private static class ServerWorkThread extends Thread {
        
        /**
         * Stores the concrete channel for communication with client.
         */
        private SocketChannel channel;
        
        /**
         * Creates the working thread for the given socket.
         * 
         * @param channel the channel to communicate on
         * 
         * @since 1.00
         */
        public ServerWorkThread(SocketChannel channel) {
            this.channel = channel;
        }
        
        /**
         * Performs the communication work, i.e. receives data according to
         * a simple protocol.
         */
        public void run() {
            try {
                DataInputStream in 
                    = new DataInputStream(Channels.newInputStream(channel));
                DataOutputStream out 
                    = new DataOutputStream(Channels.newOutputStream(channel));
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
     * Starts the server part, executes the client part and prints out the 
     * results. Currently to be compared manually with the output of the 
     * monitoring infrastructure.
     * 
     * @param args command line arguments (ignored)
     * @throws IOException any kind of network I/O problem in the client
     * 
     * @since 1.00
     */
    @StartSystem
    @EndSystem
    public static void main(String[] args) throws IOException {
        String testString = NetIoChannelTest.class.getName(); 
        System.out.println(testString);

        System.out.println("Starting server on port " + PORT);

        InetSocketAddress addr = new InetSocketAddress(PORT);
        
        ServerMainThread serverThread = new ServerMainThread();
        serverThread.start();

        // client starts here
        
        SocketChannel sc = SocketChannel.open();
        sc.connect(addr);
        System.out.println("Client connected on address " + addr);
        
        DataInputStream in = new DataInputStream(
            Channels.newInputStream(sc));
        DataOutputStream out = new DataOutputStream(
            Channels.newOutputStream(sc));

        int count = 10;
        out.writeInt(count);
        int write = NET_SIZE_INT;
        for (int i = 1; i <= count; i++) {
            out.writeUTF(testString);
            write += TestHelper.getUtfLen(testString) + NET_OVERHEAD_STRING;
        }
        int read = 0;
        //Bei folgender Zeile hängt er
        int serverRead = in.readInt(); //FEHLER
        read += NET_SIZE_INT;
        int serverWrite = in.readInt();
        read += NET_SIZE_INT;

        System.out.println("write " + write + " server " + serverWrite 
            + " total " + (write + serverWrite));
        System.out.println("read " + read + " server " + serverRead 
            + " total " + (read + serverRead));
        System.out.println("------------------ done: NetIo");
    }

}
