package test;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;

import test.testing.MonitoringGroupValue;
import test.testing.TestEnvironment;

import de.uni_hildesheim.sse.codeEraser.annotations.Variability;
import de.uni_hildesheim.sse.monitoring.runtime.annotations.EndSystem;
import de.uni_hildesheim.sse.monitoring.runtime.annotations.Monitor;
import de.uni_hildesheim.sse.monitoring.runtime.annotations.StartSystem;

/**
 * Perform simple UDP IO testing of the monitoring infrastructure. 
 * <p> Monitoring must be enabled via the agent JVM parameter!
 * 
 * @author Holger Eichelberger, Stephan Dederichs
 * @since 1.00
 * @version 1.00
 */
@Variability(id = AnnotationId.VAR_TESTING)
@Monitor(id = AnnotationId.ID_UDP_IO)
public class UDPIoTest {

    /**
     * Defines the port used for communication operations.
     */
    private static int port = 10101;

    /**
     * Stores the bytes read by the client.
     */
    private static int clientRead = 0;

    /**
     * Stores the bytes written by the client.
     */
    private static int clientWrite = 0;
    
    /**
     * Stores the bytes read by the server.
     */
    private static int serverRead = 0;

    /**
     * Stores the bytes written by the server.
     */
    private static int serverWrite = 0;

    
    /**
     * Prevents this class from being instantiated from outside.
     * 
     * @since 1.00
     */
    private UDPIoTest() {
    }
    
    /**
     * Implements a simple thread for accepting network connections.
     * 
     * @author Holger Eichelberger, Stephan Dederichs
     * @since 1.00
     * @version 1.00
     */
    @Monitor(id = AnnotationId.ID_UDP_IO_SERVER)
    private static class ServerMainThread extends Thread {

        /**
         * Stores the underlying server socket.
         */
        private DatagramSocket serverSocket;
        
        /**
         * Stores if this thread is ready for accepting connections.
         */
        private boolean ready = true;

        /**
         * Array for sending data.
         */
        private byte[] sendData = new byte[1024];

        /**
         * Array for receiving data.
         */
        private byte[] receiveData = new byte[1024];
        
        /**
         * Creates a new server thread on the given socket.
         * 
         * @param serverSocket the socket used for accepting connections
         * 
         * @since 1.00
         */
        public ServerMainThread(DatagramSocket serverSocket) {
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
                    DatagramPacket receivePacket = new DatagramPacket(
                        receiveData, receiveData.length);
                    serverSocket.receive(receivePacket);

                    int totalRead = 0;
                    int totalWrite = 0;
                    TestEnvironment.notice("DATA: " 
                        + new String(receivePacket.getData(), 0, 
                            receivePacket.getLength()));
                    int count = Integer.parseInt(
                        new String(receivePacket.getData(), 0, 
                            receivePacket.getLength()));
                    totalRead += receivePacket.getLength();

                    for (int i = 1; i <= count; i++) {
                        serverSocket.receive(receivePacket);
                        totalRead += receivePacket.getLength();
                    }
                    
                    InetAddress ipAddress = receivePacket.getAddress();
                    int port = receivePacket.getPort();
                    sendData = String.valueOf(totalRead).getBytes();
                    DatagramPacket sendPacket = new DatagramPacket(sendData, 
                        sendData.length, ipAddress, port);
                    serverSocket.send(sendPacket);
                    totalWrite += sendPacket.getLength();

                    sendData = String.valueOf(totalWrite).getBytes();
                    sendPacket = new DatagramPacket(sendData, 
                        sendData.length, ipAddress, port);
                    totalWrite += sendPacket.getLength();
                    // not more than 10, no further increase expected
                    sendData = String.valueOf(totalWrite).getBytes();
                    sendPacket.setData(sendData);
                    sendPacket.setLength(sendData.length);
                    serverSocket.send(sendPacket);
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
     * Starts the server part, executes the client part and prints out the 
     * results. Currently to be compared manually with the output of the 
     * monitoring infrastructure.
     * 
     * @throws IOException any kind of network I/O problem in the client
     * 
     * @since 1.00
     */
    private static void execute() throws IOException {
        String testString = UDPIoTest.class.getName();

        TestEnvironment.notice("Starting server on port " + port);
        DatagramSocket serverSocket = new DatagramSocket(port);
        serverSocket.setSoTimeout(3000);
        ServerMainThread serverThread = new ServerMainThread(serverSocket);
        serverThread.start();
        
        DatagramSocket clientSocket = new DatagramSocket();
        clientSocket.setSoTimeout(3000);
        InetAddress ipAddress = InetAddress.getByName("localhost");
        byte[] sendData = new byte[1024];
        byte[] receiveData = new byte[1024];
        
        int count = 10;
        sendData = String.valueOf(count).getBytes();
        DatagramPacket sendPacket = new DatagramPacket(sendData, 
            sendData.length, ipAddress, port);

        clientSocket.send(sendPacket);
        clientWrite += sendPacket.getLength();
        for (int i = 1; i <= count; i++) {
            sendData = testString.getBytes();
            sendPacket = new DatagramPacket(sendData, sendData.length, 
                ipAddress, port);
            clientSocket.send(sendPacket);
            clientWrite += sendPacket.getLength();
        }
        
        DatagramPacket receivePacket = new DatagramPacket(receiveData, 
            receiveData.length);
        clientSocket.receive(receivePacket);
        serverRead += Integer.parseInt(new String(receivePacket.getData(),
                0, receivePacket.getLength()));
        clientRead += receivePacket.getLength();
        clientSocket.receive(receivePacket);
        serverWrite += Integer.parseInt(new String(receivePacket.getData(),
                0, receivePacket.getLength()));
        clientRead += receivePacket.getLength();
        clientSocket.close();

        TestEnvironment.notice("write " + clientWrite + " server " + serverWrite
            + " total " + (clientWrite + serverWrite));
        TestEnvironment.notice("read " + clientRead + " server " + serverRead 
            + " total " + (clientRead + serverRead));
        
        port++;
    }
    
    /**
     * Starts the test.
     * 
     * @param args command line arguments (ignored)
     * 
     * @throws IOException any kind of network I/O problem in the client
     * 
     * @since 1.00
     */
    @StartSystem
    @EndSystem(invoke = "asserts")
    public static void main(String[] args) throws IOException {
        TestEnvironment.notice(UDPIoTest.class.getName());
        if (args.length > 0) {
            if (args[0].equals("continue")) {
                for (int i = 0; i < 15; i++) {
                    execute();
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
            execute();
        }
        TestEnvironment.notice("------------------ done: UDPIoTest");
    }

    /**
     * Prints a given packet (for debugging).
     * 
     * @param prefix a textual prefix
     * @param packet the package to print
     * 
     * @since 1.00
     */
    @SuppressWarnings("unused")
    private static final void printDatagramPacket(String prefix, 
        DatagramPacket packet) {
        System.out.println(prefix + " " 
            + new String(packet.getData(), 0, packet.getLength()));
    }

    /**
     * The automated tests using assert statements. This method is called
     * by SPASS-meter at end of monitoring upon the <code>EndSystem</code> 
     * annotation.
     * 
     * @since 1.00
     */
    public static void asserts() {
        if (TestEnvironment.isIndirectTest()) {
            // online + serverWrite, offline without...
            TestEnvironment.assertEquals(AnnotationId.ID_UDP_IO, 
                MonitoringGroupValue.NET_WRITE, clientWrite, serverWrite);
            TestEnvironment.assertEquals(AnnotationId.ID_UDP_IO, 
                MonitoringGroupValue.NET_READ, clientRead, serverRead);
        } else {
            TestEnvironment.assertEquals(AnnotationId.ID_UDP_IO, 
                MonitoringGroupValue.NET_WRITE, clientWrite);
            TestEnvironment.assertEquals(AnnotationId.ID_UDP_IO, 
                MonitoringGroupValue.NET_READ, clientRead);
        }

        TestEnvironment.assertEquals(AnnotationId.ID_UDP_IO_SERVER, 
            MonitoringGroupValue.NET_WRITE, serverWrite);
        TestEnvironment.assertEquals(AnnotationId.ID_UDP_IO_SERVER, 
            MonitoringGroupValue.NET_READ, serverRead);

        TestEnvironment.assertEquals(TestEnvironment.getProgramId(), 
            MonitoringGroupValue.NET_WRITE, clientWrite + serverWrite);
        TestEnvironment.assertEquals(TestEnvironment.getProgramId(), 
            MonitoringGroupValue.NET_READ, clientRead + serverRead);

        TestEnvironment.assertEquals(TestEnvironment.getProgramId(), 
            MonitoringGroupValue.FILE_WRITE, 0);
        // reads internal security policy
        TestEnvironment.assertGreater(TestEnvironment.getProgramId(), 
            MonitoringGroupValue.FILE_READ, 0);
        
        TestEnvironment.success(AnnotationId.ID_UDP_IO);
    }

}
