package de.uni_hildesheim.sse.monitoring.runtime.wrap;

import java.io.IOException;
import java.io.Reader;

//import de.uni_hildesheim.sse.monitoring.runtime.annotations.Helper;
//import de.uni_hildesheim.sse.monitoring.runtime.boot.IoNotifier;
import de.uni_hildesheim.sse.monitoring.runtime.boot.RecorderFrontend;
import de.uni_hildesheim.sse.monitoring.runtime.boot.StreamType;

/**
 * A reader delegating its operation to an arbitrary reader
 * while notifying the recorder about performed operations.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public class DelegatingReader extends Reader {

    /**
     * Stores the reader to delegate to.
     */
    private Reader reader;
    
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
     * Creates a new delegating reader.
     * 
     * @param reader the reader to delegate the operations to
     * @param type the type of the stream
     * 
     * @since 1.00
     */
    public DelegatingReader(Reader reader, StreamType type) {
        this(reader, type, null);
    }
    
    /**
     * Creates a new delegating reader.
     * 
     * @param reader the reader to delegate the operations to
     * @param type the type of the stream
     * @param recId the recorder id to aggregate the notifications for
     * 
     * @since 1.00
     */
    public DelegatingReader(Reader reader, StreamType type, String recId) {
        this.reader = reader;
        this.type = type;
        this.recId = recId;
    }
    
    /**
     * Reads a char from a stream.
     * 
     * @return the char or -1 at the end of the stream
     * @throws IOException in case of any I/O error or problem
     */
    public int read() throws IOException {
        int result = reader.read();
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
     * Reads chars into <code>buffer</code>, at maximum up to the length of the 
     * buffer.
     * 
     * @param buffer the char buffer to be modified
     *   
     * @return the number of bytes read, negative at stream end
     * @throws IOException in case of I/O reading errors
     */
    public int read(char[] buffer) throws IOException {
        inRead1 = true;
        int result = 0;
        try {
            result = reader.read(buffer);
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
     * Reads at maximum <code>len</code> chars into <code>buffer</code> starting
     * at <code>off</code>.
     * 
     * @param buffer the char buffer to be modified
     * @param off the offset where to start storing bytes
     * @param len the maximum number of bytes to be read (less than length of 
     *   <code>b</code>)
     *   
     * @return the number of bytes read, negative at stream end
     * @throws IOException in case of I/O reading errors
     */
    public int read(char[] buffer, int off, int len) throws IOException {
        inRead3 = true;
        int result = 0;
        try {
            result = reader.read(buffer, off, len);
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
     * Closes the reader.
     * 
     * @throws IOException in case of I/O errors
     */
    public void close() throws IOException {
        reader.close();
    }

    /**
     * Skips characters.  This method will block until some characters are
     * available, an I/O error occurs, or the end of the stream is reached.
     *
     * @param  number  The number of characters to skip
     *
     * @return    The number of characters actually skipped
     *
     * @throws  IllegalArgumentException  If <code>n</code> is negative.
     * @throws  IOException  If an I/O error occurs
     */
    public long skip(long number) throws IOException {
        return reader.skip(number);
    }
    
    /**
     * Tells whether this stream is ready to be read.
     *
     * @return True if the next read() is guaranteed not to block for input,
     * false otherwise.  Note that returning false does not guarantee that the
     * next read will block.
     *
     * @throws  IOException  If an I/O error occurs
     */
    public boolean ready() throws IOException {
        return reader.ready();
    }

    /**
     * Tells whether this stream supports the mark() operation. The default
     * implementation always returns false. Subclasses should override this
     * method.
     *
     * @return true if and only if this stream supports the mark operation.
     */
    public boolean markSupported() {
        return reader.markSupported();
    }

    /**
     * Marks the present position in the stream.  Subsequent calls to reset()
     * will attempt to reposition the stream to this point.  Not all
     * character-input streams support the mark() operation.
     *
     * @param  readAheadLimit  Limit on the number of characters that may be
     *                         read while still preserving the mark.  After
     *                         reading this many characters, attempting to
     *                         reset the stream may fail.
     *
     * @throws  IOException  If the stream does not support mark(),
     *                          or if some other I/O error occurs
     */
    public void mark(int readAheadLimit) throws IOException {
        reader.mark(readAheadLimit);
    }

    /**
     * Resets the stream.  If the stream has been marked, then attempt to
     * reposition it at the mark.  If the stream has not been marked, then
     * attempt to reset it in some way appropriate to the particular stream,
     * for example by repositioning it to its starting point.  Not all
     * character-input streams support the reset() operation, and some support
     * reset() without supporting mark().
     *
     * @exception  IOException  If the stream has not been marked,
     *                          or if the mark has been invalidated,
     *                          or if the stream does not support reset(),
     *                          or if some other I/O error occurs
     */
    public void reset() throws IOException {
        reader.reset();
    }

}
