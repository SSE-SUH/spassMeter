package de.uni_hildesheim.sse.wildcat.test;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;

import de.uni_hildesheim.sse.codeEraser.annotations.Variability;
import de.uni_hildesheim.sse.monitoring.runtime.annotations.EndSystem;
import de.uni_hildesheim.sse.monitoring.runtime.annotations.Monitor;
import de.uni_hildesheim.sse.monitoring.runtime.annotations.StartSystem;

/**
 * Perform simple UDP IO testing of the monitoring infrastructure. 
 * <br/> Monitoring must be enabled via the agent JVM parameter!
 * 
 * @author Holger Eichelberger, Stephan Dederichs
 * @since 1.00
 * @version 1.00
 */
@Variability(id = "testing")
@Monitor(id = "UdpIoTest")
public class UdpIoTest {
    /**
     * Defines the port used for communication operations.
     * 
     * @since 1.00
     */
    private static int port = 10101;

//    /**
//     * For memory allocation...
//     * 
//     * @since 1.00
//     */
//    private static Object[] obj;

    /**
     * Prevents this class from being instantiated from outside.
     * 
     * @since 1.00
     */
    private UdpIoTest() {
    }
    
    /**
     * Implements a simple thread for accepting network connections.
     * 
     * @author Holger Eichelberger, Stephan Dederichs
     * @since 1.00
     * @version 1.00
     */
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
                    System.out.println("DATA: " 
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
                    serverSocket.send(sendPacket);
                    totalWrite += sendPacket.getLength();
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
        
//        obj = new Object[5000];
//        for (int i = 0; i < obj.length; i++) {
//            ArrayList<Object> list = new ArrayList<Object>(10000);
//            obj[i] = list;
//            for (int j = 0; j < list.size(); j++) {
//                list.add(new Object());
//            }
//        }
//        obj = null;
        
        String testString = UdpIoTest.class.getName();

        System.out.println("Starting server on port " + port);
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
        int write = sendPacket.getLength();
        for (int i = 1; i <= count; i++) {
            sendData = testString.getBytes();
            sendPacket = new DatagramPacket(sendData, sendData.length, 
                ipAddress, port);
            clientSocket.send(sendPacket);
            write += sendPacket.getLength();
        }
        
        int read = 0;
        DatagramPacket receivePacket = new DatagramPacket(receiveData, 
            receiveData.length);
        clientSocket.receive(receivePacket);
        int serverRead = Integer.parseInt(new String(receivePacket.getData(),
                0, receivePacket.getLength()));
        read += receivePacket.getLength();
        clientSocket.receive(receivePacket);
        int serverWrite = Integer.parseInt(new String(receivePacket.getData(),
                0, receivePacket.getLength()));
        read += receivePacket.getLength();
        clientSocket.close();

        System.out.println("write " + write + " server " + serverWrite 
            + " total " + (write + serverWrite));
        System.out.println("read " + read + " server " + serverRead 
            + " total " + (read + serverRead));
        
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
    @SuppressWarnings("static-access")
    @StartSystem
    @EndSystem
    public static void main(String[] args) throws IOException {
        System.out.println(UdpIoTest.class.getName());
        while (true) {
            execute();
            try {
                Thread.currentThread().sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
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
}
