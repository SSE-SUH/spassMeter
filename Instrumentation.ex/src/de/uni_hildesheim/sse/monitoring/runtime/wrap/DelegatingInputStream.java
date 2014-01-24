package de.uni_hildesheim.sse.monitoring.runtime.wrap;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

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
public class DelegatingInputStream extends InputStream {

    /**
     * Stores the stream to delegate to.
     */
    private InputStream stream;
    
    /**
     * Stores if we are in a read method with 3 parameters (avoid double 
     * recording).
     */
    private boolean inRead3 = false;
    
    /**
     * Stores if we are in a read method with 1 parameter (avoid double 
     * recording).
     */
    private boolean inRead1 = false;

    /**
     * Stores the stream type.
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
    public DelegatingInputStream(InputStream stream, StreamType type) {
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
    public DelegatingInputStream(InputStream stream, StreamType type, 
        String recId) {
        this.stream = stream;
        this.type = type;
        this.recId = recId;
    }
    
    /**
     * Creates a delegating input stream (if possible) from the given
     * parameters.
     * 
     * @param is the input stream to create the delegating input stream for
     * @param url an URL to take the stream type from
     * @param recId the recorder id to aggregate the notifications for
     * @return the delegating input stream or <code>is</code>
     * 
     * @since 1.00
     */
    public static final InputStream createFrom(InputStream is, URL url, 
        String recId) {
        StreamType type = StreamType.getForURL(url.getProtocol());
        if (null != type) {
            return new DelegatingInputStream(is, type, recId);
        } else {
            return is;
        }
    }
    
    /**
     * Returns an estimate of the number of bytes that can be read (or 
     * skipped over) from this input stream without blocking by the next
     * invocation of a method for this input stream. The next invocation
     * might be the same thread or another thread.  A single read or skip 
     * of this many bytes will not block, but may read or skip fewer bytes.
     *
     * @return     an estimate of the number of bytes that can be read 
     *   (or skipped over) from this input stream without blocking 
     *   or {@code 0} when it reaches the end of the input stream.
     * @throws  IOException if an I/O error occurs.
     */
    public int available() throws IOException {
        return stream.available();
    }

    /**
     * Marks the current position in this input stream. A subsequent call to
     * the <code>reset</code> method repositions this stream at the last marked
     * position so that subsequent reads re-read the same bytes.
     *
     * @param   readlimit   the maximum limit of bytes that can be read before
     *                      the mark position becomes invalid.
     */
    public synchronized void mark(int readlimit) {
        stream.mark(readlimit);
    }
    
    /**
     * Tests if this input stream supports the <code>mark</code> and
     * <code>reset</code> methods. Whether or not <code>mark</code> and
     * <code>reset</code> are supported is an invariant property of a
     * particular input stream instance. The <code>markSupported</code> method
     * of <code>InputStream</code> returns <code>false</code>.
     *
     * @return  <code>true</code> if this stream instance supports the mark
     *          and reset methods; <code>false</code> otherwise.
     * @see     java.io.InputStream#mark(int)
     * @see     java.io.InputStream#reset()
     */
    public boolean markSupported() {
        return stream.markSupported();
    }
    
    /**
     * Repositions this stream to the position at the time the
     * <code>mark</code> method was last called on this input stream.
     *
     * @throws  IOException  if this stream has not been marked or if the
     *               mark has been invalidated.
     */
    public synchronized void reset() throws IOException {
        stream.reset();
    }
    
    /**
     * Skips over and discards <code>n</code> bytes of data from this input
     * stream. The <code>skip</code> method may, for a variety of reasons, end
     * up skipping over some smaller number of bytes, possibly <code>0</code>.
     * This may result from any of a number of conditions; reaching end of file
     * before <code>n</code> bytes have been skipped is only one possibility.
     * The actual number of bytes skipped is returned.  If <code>n</code> is
     * negative, no bytes are skipped.
     *
     * <p> The <code>skip</code> method of this class creates a
     * byte array and then repeatedly reads into it until <code>n</code> bytes
     * have been read or the end of the stream has been reached. Subclasses are
     * encouraged to provide a more efficient implementation of this method.
     * For instance, the implementation may depend on the ability to seek.
     *
     * @param      number   the number of bytes to be skipped.
     * @return     the actual number of bytes skipped.
     * @exception  IOException  if the stream does not support seek,
     *              or if some other I/O error occurs.
     */
    public long skip(long number) throws IOException {
        return stream.skip(number);
    }

    /**
     * Reads a byte from a stream.
     * 
     * @return the byte or -1 at the end of the stream
     * @throws IOException in case of any I/O error or problem
     */
    @Override
    public int read() throws IOException {
        int result = stream.read();
        if (!(inRead3 || inRead1)) {
            /*if (!Helper.NEW_IO_HANDLING) {
                IoNotifier.notifyRead(type, recId, 1);
            } else {*/
            if (null != RecorderFrontend.instance) {
                RecorderFrontend.instance.readIo(recId, 
                    null, 1, type);
            }
            //}
        }
        return result;
    }

    /**
     * Reads bytes into <code>buffer</code>, at maximum up to the length of the 
     * buffer.
     * 
     * @param buffer the byte buffer to be modified
     *   
     * @return the number of bytes read, negative at stream end
     * @throws IOException in case of I/O reading errors
     */
    public int read(byte[] buffer) throws IOException {
        inRead1 = true;
        int result = 0;
        try {
            result = stream.read(buffer);
            if (!inRead3) {
                /*if (!Helper.NEW_IO_HANDLING) {
                    IoNotifier.notifyRead(type, recId, result);
                } else {*/
                if (null != RecorderFrontend.instance) {
                    RecorderFrontend.instance.readIo(recId, 
                        null, result, type);
                }
                //}
            }
            inRead1 = false;
        } catch (IOException e) {
            inRead1 = false;
            throw e;
        }
        return result;
    }
    
    /**
     * Reads at maximum <code>len</code> bytes into <code>buffer</code> starting
     * at <code>off</code>.
     * 
     * @param buffer the byte buffer to be modified
     * @param off the offset where to start storing bytes
     * @param len the maximum number of bytes to be read (less than length of 
     *   <code>b</code>)
     *   
     * @return the number of bytes read, negative at stream end
     * @throws IOException in case of I/O reading errors
     */
    public int read(byte[] buffer, int off, int len) throws IOException {
        inRead3 = true;
        int result = 0;
        try {
            result = stream.read(buffer, off, len);
            if (!inRead1) {
                /*if (!Helper.NEW_IO_HANDLING) {
                    IoNotifier.notifyRead(type, recId, result);
                } else {*/
                if (null != RecorderFrontend.instance) {
                    RecorderFrontend.instance.readIo(recId, 
                        null, result, type);
                }
                //}
            }
            inRead3 = false;
        } catch (IOException e) {
            inRead3 = false;
            throw e;
        }
        return result;
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
