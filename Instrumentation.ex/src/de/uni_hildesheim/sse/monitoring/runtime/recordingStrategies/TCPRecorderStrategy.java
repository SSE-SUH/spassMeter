package de.uni_hildesheim.sse.monitoring.runtime.recordingStrategies;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import de.uni_hildesheim.sse.codeEraser.annotations.Variability;
import de.uni_hildesheim.sse.monitoring.runtime.AnnotationConstants;
import de.uni_hildesheim.sse.monitoring.runtime.annotations.Helper;
import de.uni_hildesheim.sse.monitoring.runtime.boot.RecorderFrontend;
import de.uni_hildesheim.sse.monitoring.runtime.boot.StreamType;
import de.uni_hildesheim.sse.monitoring.runtime.configuration.Configuration;
import de.uni_hildesheim.sse.monitoring.runtime.recordingStrategiesElements.*;
import de.uni_hildesheim.sse.monitoring.runtime.wrap.DelegatingOutputStream;

/**
 * Implements the TCP recorder strategy for recording. This class sends all
 * recorded events via tcp to a specified server. To specify server and port vm
 * arguments must be added: tcp=server:port (i.e. tcp=localhost:6002). The
 * server must listen on the same specified port.
 * 
 * @author Stephan Dederichs
 * @since 1.00
 * @version 1.00
 */
@Variability(id = AnnotationConstants.STRATEGY_TCP)
public class TCPRecorderStrategy extends AbstractEventRecorderStrategy {

    /**
     * Stores the {@link Socket} for the client.
     */
    private Socket socket;

    /**
     * Stores the {@link DataOutputStream} to send messages to the server.
     */
    private DataOutputStream out;

    /**
     * Constructor. Creates the recorder strategy.
     * 
     * @since 1.00
     */
    public TCPRecorderStrategy() {
        super(new StrategyStorage());
        connect2Server();
    }

    /**
     * Creates the connection to the server. This method also brings the
     * {@link Configuration} to send their informations to the server and starts
     * a thread which sends the gathered information to the
     * server in parallel.
     * 
     * @since 1.00
     */
    private void connect2Server() {
        try {
            // Get Configuration instance
            Configuration conf = Configuration.INSTANCE;
            // Create a connection to server
            socket = new Socket(conf.getTCPHostname(), conf.getTCPPort());
            // close socket after 1 minute
            socket.setSoTimeout(60000);
            // Create input and output streams to socket
            OutputStream os = socket.getOutputStream();
            if (null != RecorderFrontend.instance) {
                if (conf.instrumentJavaLib()) {
                    // socket streams are modified in this mode
                    RecorderFrontend.instance.registerAsOverheadStream(os);    
                } else {
                    if (conf.recordOverhead()) {
                        os = new DelegatingOutputStream(
                            os, StreamType.NET, Helper.RECORDER_ID);
                    }
                }
            }
            out = new DataOutputStream(os);
            // send configuration
            conf.write(out);
            // Start SendThread
            start();
        } catch (SocketException e) {
            System.out.println("Socket error : " + e);
        } catch (UnknownHostException e) {
            System.out.println("Invalid host!");
        } catch (SocketTimeoutException ste) {
            try {
                socket.close();
            } catch (IOException e) {
                System.out.println("I/O error while closing the socket: " + e);
            }
        } catch (IOException e) {
            System.out.println("I/O error : " + e);
        }
    }

    /**
     * Handles an event.
     * 
     * @param event the event to be handled
     * @throws HandleException any kind of throwable exception which should 
     *    cause aborting the event loop
     * 
     * @since 1.00
     */
    protected void handleEvent(RecordingStrategiesElement event) 
        throws HandleException {
        try {
            if (Constants.CLEARTEMPORARYDATA != event.getIdentification()) {
                out.writeInt(event.getIdentification());
                // Sends the element to the server
                event.send(out);
            }
        } catch (IOException e) {
            throw new HandleException(e);
        }
    }
}