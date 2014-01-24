package de.uni_hildesheim.sse.monitoring.runtime.wrap;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

//import de.uni_hildesheim.sse.monitoring.runtime.annotations.Helper;
//import de.uni_hildesheim.sse.monitoring.runtime.boot.IoNotifier;
import de.uni_hildesheim.sse.monitoring.runtime.boot.RecorderFrontend;
import de.uni_hildesheim.sse.monitoring.runtime.boot.StreamType;

/**
 * Extends the file input stream class defined by java in order to obtain
 * information while reading from a file.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public class InstrumentedFileInputStream extends FileInputStream {

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
     * Stores the recorder id to aggregate the notifications for.
     */
    private String recId = null;

    /**
     * Creates a file input stream by
     * opening a connection to an actual file,
     * the file named by the path name <code>name</code>
     * in the file system.<p>
     * If the named file does not exist, is a directory rather than a regular
     * file, or for some other reason cannot be opened for reading then a
     * <code>FileNotFoundException</code> is thrown.
     *
     * @param      name   the system-dependent file name.
     * @throws  FileNotFoundException  if the file does not exist,
     *                   is a directory rather than a regular file,
     *                   or for some other reason cannot be opened for
     *                   reading.
     */
    public InstrumentedFileInputStream(String name) 
        throws FileNotFoundException {
        super(name);
    }
    
    /**
     * Creates a file input stream by
     * opening a connection to an actual file,
     * the file named by the <code>File</code>
     * object <code>file</code> in the file system.
     * <p>
     * If the named file does not exist, is a directory rather than a regular
     * file, or for some other reason cannot be opened for reading then a
     * <code>FileNotFoundException</code> is thrown.
     *
     * @param      file   the file to be opened for reading.
     * @exception  FileNotFoundException  if the file does not exist,
     *                   is a directory rather than a regular file,
     *                   or for some other reason cannot be opened for
     *                   reading.
     */
    public InstrumentedFileInputStream(File file) throws FileNotFoundException {
        super(file);
    }
    
    /**
     * Creates a file input stream by using the file descriptor
     * <code>fdObj</code>, which represents an existing connection to an
     * actual file in the file system.
     * <p>
     * If <code>fdObj</code> is null then a <code>NullPointerException</code>
     * is thrown.
     *
     * @param      fdObj   the file descriptor to be opened for reading.
     */
    public InstrumentedFileInputStream(FileDescriptor fdObj) {
        super(fdObj);
    }

    /**
     * Creates a delegating file input stream by
     * opening a connection to an actual file,
     * the file named by the path name <code>name</code>
     * in the file system.<p>
     * If the named file does not exist, is a directory rather than a regular
     * file, or for some other reason cannot be opened for reading then a
     * <code>FileNotFoundException</code> is thrown.
     *
     * @param   name   the system-dependent file name.
     * @param   recId the recorder id to aggregate the monitored data for
     * @throws  FileNotFoundException  if the file does not exist,
     *                   is a directory rather than a regular file,
     *                   or for some other reason cannot be opened for
     *                   reading.
     */
    public InstrumentedFileInputStream(String name, String recId) 
        throws FileNotFoundException {
        super(name);
        this.recId = recId;
    }

    /**
     * Creates a file input stream by
     * opening a connection to an actual file,
     * the file named by the <code>File</code>
     * object <code>file</code> in the file system.
     * <p>
     * If the named file does not exist, is a directory rather than a regular
     * file, or for some other reason cannot be opened for reading then a
     * <code>FileNotFoundException</code> is thrown.
     *
     * @param      file   the file to be opened for reading.
     * @param   recId the recorder id to aggregate the monitored data for
     * @exception  FileNotFoundException  if the file does not exist,
     *                   is a directory rather than a regular file,
     *                   or for some other reason cannot be opened for
     *                   reading.
     */
    public InstrumentedFileInputStream(File file, String recId) 
        throws FileNotFoundException {
        super(file);
        this.recId = recId;
    }

    /**
     * Creates a file input stream by using the file descriptor
     * <code>fdObj</code>, which represents an existing connection to an
     * actual file in the file system.
     * <p>
     * If <code>fdObj</code> is null then a <code>NullPointerException</code>
     * is thrown.
     *
     * @param      fdObj   the file descriptor to be opened for reading.
     * @param   recId the recorder id to aggregate the monitored data for
     */
    public InstrumentedFileInputStream(FileDescriptor fdObj, String recId) {
        super(fdObj);
        this.recId = recId;
    }
    
    /**
     * Reads a byte from a stream.
     * 
     * @return the byte or -1 at the end of the stream
     * @throws IOException in case of any I/O error or problem
     */
    @Override
    public int read() throws IOException {
        int result = super.read();
        if (!(inRead3 || inRead1)) {
            /*if (!Helper.NEW_IO_HANDLING && null != IoNotifier.fileNotifier) {
                IoNotifier.fileNotifier.notifyRead(recId, 1);
            } else {*/
            if (null != RecorderFrontend.instance) {
                RecorderFrontend.instance.readIo(recId, 
                    null, 1, StreamType.FILE);
            }
            //}
        }
        return result;
    }

    /**
     * Reads byte into <code>buffer</code>, at maximum up to the length of the 
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
            result = super.read(buffer);
            if (!inRead3) {
                /*if (!Helper.NEW_IO_HANDLING 
                    && null != IoNotifier.fileNotifier) {
                    IoNotifier.fileNotifier.notifyRead(recId, result);
                } else {*/
                if (null != RecorderFrontend.instance) {
                    RecorderFrontend.instance.readIo(recId, 
                        null, result, StreamType.FILE);
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
            result = super.read(buffer, off, len);
            if (!inRead1) {
                /*if (!Helper.NEW_IO_HANDLING 
                    && null != IoNotifier.fileNotifier) {
                    IoNotifier.fileNotifier.notifyRead(recId, result);
                } else {*/
                if (null != RecorderFrontend.instance) {
                    RecorderFrontend.instance.readIo(recId, 
                        null, result, StreamType.FILE);
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

}
