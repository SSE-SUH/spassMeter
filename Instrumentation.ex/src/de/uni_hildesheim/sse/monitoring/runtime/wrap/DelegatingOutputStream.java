package de.uni_hildesheim.sse.monitoring.runtime.wrap;

import java.io.IOException;
import java.io.OutputStream;

//import de.uni_hildesheim.sse.monitoring.runtime.annotations.Helper;
//import de.uni_hildesheim.sse.monitoring.runtime.boot.IoNotifier;
import de.uni_hildesheim.sse.monitoring.runtime.boot.RecorderFrontend;
import de.uni_hildesheim.sse.monitoring.runtime.boot.StreamType;

/**
 * A stream class delegating its operation to an arbitrary input stream
 * while notifying the recorder about performed operations.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public class DelegatingOutputStream extends OutputStream {

    /**
     * Stores the output stream to delegate to.
     */
    private OutputStream stream;
    
    /**
     * Stores if we are in a write method with 3 parameters (avoid double 
     * recording).
     */
    private boolean inWrite3 = false;
    
    /**
     * Stores if we are in a write method with 1 parameter (avoid double 
     * recording).
     */
    private boolean inWrite1 = false;
    
    /**
     * Stores the type of stream to delegate to.
     */
    private StreamType type;
    
    /**
     * Stores the recorder id to aggregate the notifications for.
     */
    private String recId;

    /**
     * Creates a new delegating stream.
     * 
     * @param stream the stream to delegate the operations to
     * @param type the type of the stream
     * 
     * @since 1.00
     */
    public DelegatingOutputStream(OutputStream stream, StreamType type) {
        this(stream, type, null);
    }

    /**
     * Creates a new delegating stream.
     * 
     * @param stream the stream to delegate the operations to
     * @param type the type of the stream
     * @param recId the recorder id to aggregate the notifications for
     * 
     * @since 1.00
     */
    public DelegatingOutputStream(OutputStream stream, StreamType type, 
        String recId) {
        this.stream = stream;
        this.type = type;
        this.recId = recId;
    }
    
    /**
     * Writes a byte to the stream.
     *
     * @param  value the data byte
     *
     * @exception  IOException  If an I/O error occurs
     */
    @Override
    public void write(int value) throws IOException {
        stream.write(value);
        if (!(inWrite1 || inWrite3)) {
            /*if (!Helper.NEW_IO_HANDLING) {
                IoNotifier.notifyWrite(type, recId, 1);
            } else {*/
            if (null != RecorderFrontend.instance) {
                RecorderFrontend.instance.writeIo(recId, 
                    null, 1, type);
            }
            //}
        }
    }

    /**
     * Writes a data array.
     *
     * @param  buffer the data array
     *
     * @exception  IOException  If an I/O error occurs
     */
    @Override
    public void write(byte[] buffer) throws IOException {
        inWrite1 = true;
        try {
            stream.write(buffer);
            if (!inWrite3) {
                /*if (!Helper.NEW_IO_HANDLING) {
                    IoNotifier.notifyWrite(type, recId, buffer.length);
                } else {*/
                if (null != RecorderFrontend.instance) {
                    RecorderFrontend.instance.writeIo(recId, 
                        null, buffer.length, type);
                }
                //}
            }
            inWrite1 = false;
        } catch (IOException e) {
            inWrite1 = false;
            throw e;
        }
    }

    /**
     * Writes a portion of a data array.
     *
     * @param  buffer the data array
     * @param  off  Offset from which to start writing characters
     * @param  len  Number of characters to write
     *
     * @exception  IOException  If an I/O error occurs
     */
    @Override
    public void write(byte[] buffer, int off, int len) throws IOException {
        inWrite3 = true;
        try {
            stream.write(buffer, off, len);
            if (!inWrite1) {
                /*if (!Helper.NEW_IO_HANDLING) {
                    IoNotifier.notifyWrite(type, recId, len);
                } else {*/
                if (null != RecorderFrontend.instance) {
                    RecorderFrontend.instance.writeIo(recId, 
                        null, len, type);
                }
                //}
            }
            inWrite3 = false;
        } catch (IOException e) {
            inWrite3 = false;
            throw e;
        }
    }
    
    /**
     * Flushes the contents.
     * 
     * @throws IOException in case of I/O errors
     */
    @Override
    public void flush() throws IOException {
        stream.flush();
    }
    
    /**
     * Closes the stream.
     * 
     * @throws IOException in case of I/O errors
     */
    public void close() throws IOException {
        stream.close();
    }

}
