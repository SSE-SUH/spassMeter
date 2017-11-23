package test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import de.uni_hildesheim.sse.codeEraser.annotations.Variability;
import de.uni_hildesheim.sse.monitoring.runtime.annotations.EndSystem;
import de.uni_hildesheim.sse.monitoring.runtime.annotations.Monitor;
import de.uni_hildesheim.sse.monitoring.runtime.annotations.StartSystem;

/**
 * Perform simple UDP IO testing of the monitoring infrastructure. <p>
 * Monitoring must be enabled via the agent JVM parameter!
 * 
 * @author Holger Eichelberger, Stephan Dederichs
 * @since 1.00
 * @version 1.00
 */
@Variability(id = AnnotationId.VAR_TESTING)
@Monitor(id = "udpIoTest")
public class UDPIoChannelTest {

    /**
     * Defines the port used for communication operations.
     */
    private static final int PORT = 10101;

    /**
     * Prevents this class from being instantiated from outside.
     * 
     * @since 1.00
     */
    private UDPIoChannelTest() {
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
        private DatagramChannel channel;

        /**
         * Stores if this thread is ready for accepting connections.
         */
        private boolean ready = true;

        /**
         * Array for data.
         */
        private ByteBuffer buf = ByteBuffer.allocate(65507);

        /**
         * Creates a new server thread on the given socket.
         * 
         * @since 1.00
         */

        public ServerMainThread() {
            try {
                channel = DatagramChannel.open();
                channel.socket().bind(new InetSocketAddress(PORT));
            } catch (IOException e) {

                e.printStackTrace();
            }
        }

        /**
         * Executes the thread, i.e. waits for incoming connections. We assume
         * that an appropriate SO timeout is set so that this thread my stop
         * after certain seconds of no new connection (for graceful end of test
         * program).
         */
        public void run() {
            while (ready) {
                try {

                    buf.clear();
                    SocketAddress client = channel.receive(buf);
                    buf.flip();
                    int totalRead = 0;
                    int totalWrite = 0;
                    int count = buf.getInt();
                    totalRead += buf.position();
                    buf.clear();

                    for (int i = 1; i <= count; i++) {
                        channel.receive(buf);
                        totalRead += buf.position();
                        buf.clear();
                    }

                    buf.putInt(totalRead);
                    int intSize = buf.position();
                    totalWrite += intSize;
                    buf.flip();
                    channel.send(buf, client);
                    buf.clear();

                    totalWrite += intSize;
                    buf.putInt(totalWrite);
                    buf.flip();
                    channel.send(buf, client);

                    channel.close();
                    ready = false;
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
     * @param args
     *            command line arguments (ignored)
     * @throws IOException
     *             any kind of network I/O problem in the client
     * 
     * @since 1.00
     */
    @StartSystem
    @EndSystem
    public static void main(String[] args) throws IOException {
        String testString = UDPIoChannelTest.class.getName();
        ServerMainThread serverThread = new ServerMainThread();
        serverThread.start();
        DatagramChannel client = DatagramChannel.open();
        client.configureBlocking(false);
        client.connect(new InetSocketAddress("localhost", PORT));
        Selector selector = Selector.open();
        client.register(selector, SelectionKey.OP_WRITE);
        int write = 0;
        int count = 10;
        selector.select(10000);
        ByteBuffer buf = ByteBuffer.allocate(500);
        buf.putInt(count);
        write += buf.position();
        buf.flip();
        client.write(buf);

        for (int i = 1; i <= count; i++) {
            buf.clear();
            buf.put(testString.getBytes());
            write += buf.position();
            buf.flip();
            client.write(buf);

        }

        buf.clear();
        Selector empfangen = Selector.open();
        client.register(empfangen, SelectionKey.OP_READ);
        empfangen.select(10000);
        int read = 0;
        System.out.println("CLIENT WILL EMPFANGEN!");
        client.read(buf);
        int serverRead = 0;
        buf.flip();
        serverRead += buf.getInt();
        read += buf.position();
        buf.clear();
        client.read(buf);
        int serverWrite = 0;
        buf.flip();
        serverWrite += buf.getInt();
        read += buf.position();
        client.close();

        System.out.println("write " + write + " server " + serverWrite
                + " total " + (write + serverWrite));
        System.out.println("read " + read + " server " + serverRead + " total "
                + (read + serverRead));
        System.out.println("------------------ done: UDPIoChannel");
    }
}
