package de.uni_hildesheim.sse.monitoring.runtime.wrap;

import java.io.IOException;
import java.io.Writer;

//import de.uni_hildesheim.sse.monitoring.runtime.annotations.Helper;
//import de.uni_hildesheim.sse.monitoring.runtime.boot.IoNotifier;
import de.uni_hildesheim.sse.monitoring.runtime.boot.RecorderFrontend;
import de.uni_hildesheim.sse.monitoring.runtime.boot.StreamType;

/**
 * A writer delegating its operation to an arbitrary writer
 * while notifying the recorder about performed operations.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public class DelegatingWriter extends Writer {

    /**
     * Stores the writer to delegate to.
     */
    private Writer writer;
    
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
     * Creates a delegating writer.
     * 
     * @param writer the writer to delegate to.
     * @param type the type of stream
     * 
     * @since 1.00
     */
    public DelegatingWriter(Writer writer, StreamType type) {
        this(writer, type, null);
    }

    /**
     * Creates a delegating writer.
     * 
     * @param writer the writer to delegate to.
     * @param type the type of stream
     * @param recId the recorder id to aggregate the notifications for
     * 
     * @since 1.00
     */
    public DelegatingWriter(Writer writer, StreamType type, String recId) {
        this.writer = writer;
        this.type = type;
        this.recId = recId;
    }

    
    /**
     * Writes a char to the writer.
     *
     * @param  value the char
     *
     * @exception  IOException  If an I/O error occurs
     */
    @Override
    public void write(int value) throws IOException {
        writer.write(value);
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
    public void write(char[] buffer) throws IOException {
        inWrite1 = true;
        try {
            writer.write(buffer);
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
    public void write(char[] buffer, int off, int len) throws IOException {
        inWrite3 = true;
        try {
            writer.write(buffer, off, len);
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
        writer.flush();
    }

    /**
     * Closes the stream.
     * 
     * @throws IOException in case of I/O errors
     */
    @Override
    public void close() throws IOException {
        writer.close();
    }
    
    /**
     * Appends the specified character to this writer.
     *
     * @param  ch
     *         The 16-bit character to append
     *
     * @return  This writer
     *
     * @throws  IOException
     *          If an I/O error occurs
     */
    public Writer append(char ch) throws IOException {
        writer.append(ch);
        return this;
    }

    /**
     * Appends the specified character sequence to this writer.
     *
     * @param  csq
     *         The character sequence to append.  If <tt>csq</tt> is
     *         <tt>null</tt>, then the four characters <tt>"null"</tt> are
     *         appended to this writer.
     *
     * @return  This writer
     *
     * @throws  IOException
     *          If an I/O error occurs
     */
    public Writer append(CharSequence csq) throws IOException {
        writer.append(csq);
        return this;
    }
    
    /**
     * Appends a subsequence of the specified character sequence to this writer.
     * <tt>Appendable</tt>.
     *
     * @param  csq
     *         The character sequence from which a subsequence will be
     *         appended.  If <tt>csq</tt> is <tt>null</tt>, then characters
     *         will be appended as if <tt>csq</tt> contained the four
     *         characters <tt>"null"</tt>.
     *
     * @param  start
     *         The index of the first character in the subsequence
     *
     * @param  end
     *         The index of the character following the last character in the
     *         subsequence
     *
     * @return  This writer
     *
     * @throws  IndexOutOfBoundsException
     *          If <tt>start</tt> or <tt>end</tt> are negative, <tt>start</tt>
     *          is greater than <tt>end</tt>, or <tt>end</tt> is greater than
     *          <tt>csq.length()</tt>
     *
     * @throws  IOException
     *          If an I/O error occurs
     */
    public Writer append(CharSequence csq, int start, int end) 
        throws IOException { 
        writer.append(csq, start, end);
        return this;
    }

}
