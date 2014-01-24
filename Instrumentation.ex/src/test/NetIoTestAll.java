package test;

import java.io.IOException;
import java.net.Socket;

import test.testing.TestEnvironment;
import test.testing.MonitoringGroupValue;

import de.uni_hildesheim.sse.codeEraser.annotations.Variability;
import de.uni_hildesheim.sse.monitoring.runtime.annotations.EndSystem;
import de.uni_hildesheim.sse.monitoring.runtime.annotations.Monitor;
import de.uni_hildesheim.sse.monitoring.runtime.annotations.StartSystem;

/**
 * Perform simple network IO testing of the monitoring infrastructure. This
 * class realizes a simple TCP client as well as a simple server.<br/>
 * Monitoring must be enabled via the agent JVM parameter!<br/>
 * Note that this class reuses {@link AbstractNetIoTest} so that the inner
 * classes defined there are accounted by {@link AnnotationId#ID_NET_IO} and 
 * by the entire program.<br/>
 * This class may cause fileIO as it reads internal Java security policies.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
@Variability(id = AnnotationId.VAR_TESTING)
@Monitor(id = AnnotationId.ID_NET_IO)
public class NetIoTestAll extends AbstractNetIoTest {

    /**
     * Prevents this class from being instantiated from outside.
     * 
     * @since 1.00
     */
    protected NetIoTestAll() {
    }
    
    /**
     * Implements a simple thread for accepting network connections.
     * 
     * @author Holger Eichelberger
     * @since 1.00
     * @version 1.00
     */
    @Monitor(id = AnnotationId.ID_NET_IO)
    private static class ServerMainThread extends 
        AbstractNetIoTest.ServerMainThread {
        
        /**
         * Executes the thread, i.e. waits for incoming connections. We assume
         * that an appropriate SO timeout is set so that this thread my stop
         * after certain seconds of no new connection (for graceful end of 
         * test program).
         */
        @Override
        public void run() {
            // force accounting
            super.run();
        }

        /**
         * Factory method for creating a worker thread.
         * 
         * @param serverSocket the socket to work on
         * @return the created thread
         * 
         * @since 1.00
         */
        @Override
        protected AbstractNetIoTest.ServerWorkThread createThread(
            Socket serverSocket) {
            // the one from this class!
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
    @Monitor(id = AnnotationId.ID_NET_IO)
    private static class ServerWorkThread extends 
        AbstractNetIoTest.ServerWorkThread {
     
        /**
         * Creates the working thread for the given socket.
         * 
         * @param socket the socket to communicate on
         * 
         * @since 1.00
         */
        public ServerWorkThread(Socket socket) {
            super(socket);
        }
        
        /**
         * Performs the communication work, i.e. receives data according to
         * a simple protocol.
         */
        public void run() {
            // force accounting
            super.run();
        }
        
    }

    /**
     * Factory method for the server main thread.
     * 
     * @return the main thread
     * 
     * @since 1.00
     */
    @Override
    protected AbstractNetIoTest.ServerMainThread createServerThread() {
        return new ServerMainThread();
    }
    
    /**
     * Starts the server part, executes the client part and prints out the 
     * results. Currently to be compared manually with the output of the 
     * monitoring infrastructure.
     *
     * @throws IOException any kind of network I/O problem in the client
     * 
     * @since 1.00
     */
    protected void executeClient() throws IOException {
        // force accounting
        super.executeClient();
    }
    
    /**
     * Starts the test.
     * 
     * @param args command line arguments (ignored)
     * @throws IOException any kind of network I/O problem in the client
     * 
     * @since 1.00
     */
    @StartSystem
    @EndSystem(invoke = "asserts")
    public static void main(String[] args) throws IOException {
        AbstractNetIoTest.main(args, new NetIoTestAll());
    }

    /**
     * The automated tests using assert statements. This method is called
     * by SPASS-meter at end of monitoring upon the <code>EndSystem</code> 
     * annotation.
     * 
     * @since 1.00
     */
    public static void asserts() {
        // client and server resources are accounted to ID_NET_IO
        TestEnvironment.assertEquals(AnnotationId.ID_NET_IO, 
            MonitoringGroupValue.NET_WRITE, AbstractNetIoTest.getAllWrite(), 1);
        TestEnvironment.assertEquals(AnnotationId.ID_NET_IO, 
            MonitoringGroupValue.NET_READ, AbstractNetIoTest.getAllRead());

     // client and server resources on program
        TestEnvironment.assertEquals(TestEnvironment.getProgramId(), 
            MonitoringGroupValue.NET_WRITE, AbstractNetIoTest.getAllWrite(), 1);
        TestEnvironment.assertEquals(TestEnvironment.getProgramId(), 
            MonitoringGroupValue.NET_READ, AbstractNetIoTest.getAllRead());

        TestEnvironment.assertEquals(TestEnvironment.getProgramId(), 
            MonitoringGroupValue.FILE_WRITE, 0);
        // reads internal security policy
        TestEnvironment.assertGreater(TestEnvironment.getProgramId(), 
            MonitoringGroupValue.FILE_READ, 0);
        
        TestEnvironment.success(AnnotationId.ID_NET_IO);
    }

}
