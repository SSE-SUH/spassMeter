package de.uni_hildesheim.sse.monitoring.runtime.wrap;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import de.uni_hildesheim.sse.monitoring.runtime.boot.RecorderFrontend;
import de.uni_hildesheim.sse.monitoring.runtime.boot.StreamType;

/**
 * Instances of this class support both reading and writing to a 
 * random access file. A random access file behaves like a large 
 * array of bytes stored in the file system. There is a kind of cursor, 
 * or index into the implied array, called the <em>file pointer</em>; 
 * input operations read bytes starting at the file pointer and advance 
 * the file pointer past the bytes read. If the random access file is 
 * created in read/write mode, then output operations are also available; 
 * output operations write bytes starting at the file pointer and advance 
 * the file pointer past the bytes written. Output operations that write 
 * past the current end of the implied array cause the array to be 
 * extended. The file pointer can be read by the 
 * <code>getFilePointer</code> method and set by the <code>seek</code> 
 * method. 
 * <p>
 * We need this delegating class, because due to public native methods 
 * instrumentation does not work. Read still works with Java 9, write does not
 * work and needs additional instrumentation.
 *
 * @author  Holger Eichelberger
 * @since   1.0
 */
public class DelegatingRandomAccessFile extends RandomAccessFile {

    /**
     * Stores the (optional) recording id.
     */
    private String recId = null;
    
    /**
     * Creates a random access file stream to read from, and optionally 
     * to write to, a file with the specified name. A new 
     * {@link java.io.FileDescriptor} object is created to represent the 
     * connection to the file.
     *
     * @param      name   the system-dependent filename
     * @param      mode   the access mode
     * @exception  IllegalArgumentException  if the mode argument is not equal
     *               to one of <tt>"r"</tt>, <tt>"rw"</tt>, <tt>"rws"</tt>, or
     *               <tt>"rwd"</tt>
     * @exception FileNotFoundException
     *            if the mode is <tt>"r"</tt> but the given string does not
     *            denote an existing regular file, or if the mode begins with
     *            <tt>"rw"</tt> but the given string does not denote an
     *            existing, writable regular file and a new regular file of
     *            that name cannot be created, or if some other error occurs
     *            while opening or creating the file
     */
    public DelegatingRandomAccessFile(String name, String mode)
        throws FileNotFoundException {
        super(name, mode);
    }

    /**
     * Creates a random access file stream to read from, and optionally to
     * write to, the file specified by the {@link java.io.File} argument.  A new
     * {@link java.io.FileDescriptor} object is created to represent this file 
     * connection.
     *
     * @param      file   the file object
     * @param      mode   the access mode
     * @exception  IllegalArgumentException  if the mode argument is not equal
     *               to one of <tt>"r"</tt>, <tt>"rw"</tt>, <tt>"rws"</tt>, or
     *               <tt>"rwd"</tt>
     * @exception FileNotFoundException
     *            if the mode is <tt>"r"</tt> but the given file object does
     *            not denote an existing regular file, or if the mode begins
     *            with <tt>"rw"</tt> but the given file object does not denote
     *            an existing, writable regular file and a new regular file of
     *            that name cannot be created, or if some other error occurs
     *            while opening or creating the file
     */
    public DelegatingRandomAccessFile(File file, String mode) 
        throws FileNotFoundException {
        super(file, mode);
    }

    /**
     * Creates a random access file stream to read from, and optionally 
     * to write to, a file with the specified name. A new 
     * {@link java.io.FileDescriptor} object is created to represent the 
     * connection to the file.
     *
     * @param      name   the system-dependent filename
     * @param      mode   the access mode
     * @param      recId  an optional recorder id
     * @exception  IllegalArgumentException  if the mode argument is not equal
     *               to one of <tt>"r"</tt>, <tt>"rw"</tt>, <tt>"rws"</tt>, or
     *               <tt>"rwd"</tt>
     * @exception FileNotFoundException
     *            if the mode is <tt>"r"</tt> but the given string does not
     *            denote an existing regular file, or if the mode begins with
     *            <tt>"rw"</tt> but the given string does not denote an
     *            existing, writable regular file and a new regular file of
     *            that name cannot be created, or if some other error occurs
     *            while opening or creating the file
     */
    public DelegatingRandomAccessFile(String name, String mode, String recId)
        throws FileNotFoundException {
        super(name, mode);
        this.recId = recId;
    }

    /**
     * Creates a random access file stream to read from, and optionally to
     * write to, the file specified by the {@link File} argument.  A new {@link
     * java.io.FileDescriptor} object is created to represent this file 
     * connection.
     *
     * @param      file   the file object
     * @param      mode   the access mode
     * @param      recId  an optional recorder id
     * @exception  IllegalArgumentException  if the mode argument is not equal
     *               to one of <tt>"r"</tt>, <tt>"rw"</tt>, <tt>"rws"</tt>, or
     *               <tt>"rwd"</tt>
     * @exception FileNotFoundException
     *            if the mode is <tt>"r"</tt> but the given file object does
     *            not denote an existing regular file, or if the mode begins
     *            with <tt>"rw"</tt> but the given file object does not denote
     *            an existing, writable regular file and a new regular file of
     *            that name cannot be created, or if some other error occurs
     *            while opening or creating the file
     */
    public DelegatingRandomAccessFile(File file, String mode, String recId) 
        throws FileNotFoundException {
        super(file, mode);
        this.recId = recId;
    }
    
    /**
     * Reads a byte of data from this file. The byte is returned as an 
     * integer in the range 0 to 255 (<code>0x00-0x0ff</code>). This 
     * method blocks if no input is yet available. 
     * 
     * @return     the next byte of data, or <code>-1</code> if the end of the
     *             file has been reached.
     * @exception  IOException  if an I/O error occurs. Not thrown if  
     *                          end-of-file has been reached.
     */
    public int read() throws IOException {
        int result = super.read();
        if (null != RecorderFrontend.instance) {
            RecorderFrontend.instance.readIo(recId, null, 1, StreamType.FILE);
        }
        return result;
    }

    /**
     * Reads up to <code>len</code> bytes of data from this file into an 
     * array of bytes. This method blocks until at least one byte of input 
     * is available. 
     *
     * @param      buffer     the buffer into which the data is read.
     * @param      off   the start offset in array <code>buffer</code>
     *                   at which the data is written.
     * @param      len   the maximum number of bytes read.
     * @return     the total number of bytes read into the buffer, or
     *             <code>-1</code> if there is no more data because the end of
     *             the file has been reached.
     * @exception  IOException If the first byte cannot be read for any reason
     * other than end of file, or if the random access file has been closed, or 
     * if some other I/O error occurs.
     * @exception  NullPointerException If <code>buffer</code> is 
     *     <b>null</b>.
     * @exception  IndexOutOfBoundsException If <code>off</code> is negative, 
     * <code>len</code> is negative, or <code>len</code> is greater than 
     * <code>buffer.length - off</code>
     */
    public int read(byte[] buffer, int off, int len) throws IOException {
        int result = super.read(buffer, off, len);
        if (null != RecorderFrontend.instance) {
            RecorderFrontend.instance.readIo(
                recId, null, buffer.length, StreamType.FILE);
        }
        return result;
    }

    /**
     * Reads up to <code>buffer.length</code> bytes of data from this file 
     * into an array of bytes. This method blocks until at least one byte 
     * of input is available. 
     *
     * @param      buffer   the buffer into which the data is read.
     * @return     the total number of bytes read into the buffer, or
     *             <code>-1</code> if there is no more data because the end of
     *             this file has been reached.
     * @exception  IOException If the first byte cannot be read for any reason
     *             other than end of file, or if the random access file has been
     *             closed, or if some other I/O error occurs.
     * @exception  NullPointerException If <code>buffer</code> is 
     *             <b>null</b>.
     */
    public int read(byte[] buffer) throws IOException {
        int result = super.read(buffer);
        if (null != RecorderFrontend.instance) {
            RecorderFrontend.instance.readIo(
                recId, null, buffer.length, StreamType.FILE);
        }
        return result;
    }


    /**
     * Writes the specified byte to this file. The write starts at 
     * the current file pointer.
     *
     * @param      value   the <code>byte</code> to be written.
     * @exception  IOException  if an I/O error occurs.
     */
    public void write(int value) throws IOException {
        super.write(value);
        if (null != RecorderFrontend.instance) {
            RecorderFrontend.instance.writeIo(recId, null, 1, StreamType.FILE);
        }
    }

    /**
     * Writes <code>buffer.length</code> bytes from the specified byte array 
     * to this file, starting at the current file pointer. 
     *
     * @param      buffer   the data.
     * @exception  IOException  if an I/O error occurs.
     */
    public void write(byte[] buffer) throws IOException {
        super.write(buffer);
        if (null != RecorderFrontend.instance) {
            RecorderFrontend.instance.writeIo(
                recId, null, buffer.length, StreamType.FILE);
        }
    }

    /**
     * Writes <code>len</code> bytes from the specified byte array 
     * starting at offset <code>off</code> to this file. 
     *
     * @param      buffer the data.
     * @param      off   the start offset in the data.
     * @param      len   the number of bytes to write.
     * @exception  IOException  if an I/O error occurs.
     */
    public void write(byte[] buffer, int off, int len) throws IOException {
        super.write(buffer, off, len);
        if (null != RecorderFrontend.instance) {
            RecorderFrontend.instance.writeIo(
                recId, null, buffer.length, StreamType.FILE);
        }
    }

}
