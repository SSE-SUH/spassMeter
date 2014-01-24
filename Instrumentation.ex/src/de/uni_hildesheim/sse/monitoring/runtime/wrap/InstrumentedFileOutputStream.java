package de.uni_hildesheim.sse.monitoring.runtime.wrap;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

//import de.uni_hildesheim.sse.monitoring.runtime.annotations.Helper;
//import de.uni_hildesheim.sse.monitoring.runtime.boot.IoNotifier;
import de.uni_hildesheim.sse.monitoring.runtime.boot.RecorderFrontend;
import de.uni_hildesheim.sse.monitoring.runtime.boot.StreamType;

/**
 * Extends the file output stream class defined by java in order to obtain
 * information while writing to a file.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public class InstrumentedFileOutputStream extends FileOutputStream {

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
     * Stores the recorder id to aggregate the monitored data for.
     */
    private String recId;

    /**
     * Creates an output file stream to write to the file with the 
     * specified name. A new <code>FileDescriptor</code> object is 
     * created to represent this file connection.
     * <p>
     * If the file exists but is a directory rather than a regular file, does
     * not exist but cannot be created, or cannot be opened for any other
     * reason then a <code>FileNotFoundException</code> is thrown.
     *
     * @param      name   the system-dependent filename
     * @exception  FileNotFoundException  if the file exists but is a directory
     *                   rather than a regular file, does not exist but cannot
     *                   be created, or cannot be opened for any other reason
     */
    public InstrumentedFileOutputStream(String name) 
        throws FileNotFoundException {
        super(name);
    }

    /**
     * Creates an output file stream to write to the file with the specified
     * <code>name</code>.  If the second argument is <code>true</code>, then
     * bytes will be written to the end of the file rather than the beginning.
     * A new <code>FileDescriptor</code> object is created to represent this
     * file connection.
     * <p>
     * If the file exists but is a directory rather than a regular file, does
     * not exist but cannot be created, or cannot be opened for any other
     * reason then a <code>FileNotFoundException</code> is thrown.
     * 
     * @param     name        the system-dependent file name
     * @param     append      if <code>true</code>, then bytes will be written
     *                   to the end of the file rather than the beginning
     * @exception  FileNotFoundException  if the file exists but is a directory
     *                   rather than a regular file, does not exist but cannot
     *                   be created, or cannot be opened for any other reason.
     */
    public InstrumentedFileOutputStream(String name, boolean append)
        throws FileNotFoundException {
        super(name, append);
    }

    /**
     * Creates a file output stream to write to the file represented by 
     * the specified <code>File</code> object. A new 
     * <code>FileDescriptor</code> object is created to represent this 
     * file connection.
     * <p>
     * If the file exists but is a directory rather than a regular file, does
     * not exist but cannot be created, or cannot be opened for any other
     * reason then a <code>FileNotFoundException</code> is thrown.
     *
     * @param      file               the file to be opened for writing.
     * @throws FileNotFoundException  if the file exists but is a directory
     *                   rather than a regular file, does not exist but cannot
     *                   be created, or cannot be opened for any other reason
     */
    public InstrumentedFileOutputStream(File file) 
        throws FileNotFoundException {
        super(file);
    }

    /**
     * Creates a file output stream to write to the file represented by 
     * the specified <code>File</code> object. If the second argument is
     * <code>true</code>, then bytes will be written to the end of the file
     * rather than the beginning. A new <code>FileDescriptor</code> object is
     * created to represent this file connection.
     * <p>
     * If the file exists but is a directory rather than a regular file, does
     * not exist but cannot be created, or cannot be opened for any other
     * reason then a <code>FileNotFoundException</code> is thrown.
     *
     * @param      file               the file to be opened for writing.
     * @param     append      if <code>true</code>, then bytes will be written
     *                   to the end of the file rather than the beginning
     * @throws FileNotFoundException  if the file exists but is a directory
     *                   rather than a regular file, does not exist but cannot
     *                   be created, or cannot be opened for any other reason
     */
    public InstrumentedFileOutputStream(File file, boolean append)
        throws FileNotFoundException {
        super(file, append);
    }

    /**
     * Creates an output file stream to write to the specified file 
     * descriptor, which represents an existing connection to an actual 
     * file in the file system.
     *
     * @param      fdObj   the file descriptor to be opened for writing
     */
    public InstrumentedFileOutputStream(FileDescriptor fdObj) {
        super(fdObj);
    }

    /**
     * Creates an output file stream to write to the file with the 
     * specified name. A new <code>FileDescriptor</code> object is 
     * created to represent this file connection.
     * <p>
     * If the file exists but is a directory rather than a regular file, does
     * not exist but cannot be created, or cannot be opened for any other
     * reason then a <code>FileNotFoundException</code> is thrown.
     *
     * @param      name   the system-dependent filename
     * @param   recId the recorder id to aggregate the monitored data for
     * @exception  FileNotFoundException  if the file exists but is a directory
     *                   rather than a regular file, does not exist but cannot
     *                   be created, or cannot be opened for any other reason
     */    
    public InstrumentedFileOutputStream(String name, String recId) 
        throws FileNotFoundException {
        super(name);
        this.recId = recId;
    }

    /**
     * Creates an output file stream to write to the file with the specified
     * <code>name</code>.  If the second argument is <code>true</code>, then
     * bytes will be written to the end of the file rather than the beginning.
     * A new <code>FileDescriptor</code> object is created to represent this
     * file connection.
     * <p>
     * If the file exists but is a directory rather than a regular file, does
     * not exist but cannot be created, or cannot be opened for any other
     * reason then a <code>FileNotFoundException</code> is thrown.
     * 
     * @param     name        the system-dependent file name
     * @param     append      if <code>true</code>, then bytes will be written
     *                   to the end of the file rather than the beginning
     * @param   recId the recorder id to aggregate the monitored data for
     * @exception  FileNotFoundException  if the file exists but is a directory
     *                   rather than a regular file, does not exist but cannot
     *                   be created, or cannot be opened for any other reason.
     */
    public InstrumentedFileOutputStream(String name, boolean append, 
        String recId) throws FileNotFoundException {
        super(name, append);
        this.recId = recId;
    }

    /**
     * Creates a file output stream to write to the file represented by 
     * the specified <code>File</code> object. A new 
     * <code>FileDescriptor</code> object is created to represent this 
     * file connection.
     * <p>
     * If the file exists but is a directory rather than a regular file, does
     * not exist but cannot be created, or cannot be opened for any other
     * reason then a <code>FileNotFoundException</code> is thrown.
     *
     * @param      file               the file to be opened for writing.
     * @param   recId the recorder id to aggregate the monitored data for
     * @throws FileNotFoundException  if the file exists but is a directory
     *                   rather than a regular file, does not exist but cannot
     *                   be created, or cannot be opened for any other reason
     */
    public InstrumentedFileOutputStream(File file, String recId) 
        throws FileNotFoundException {
        super(file);
        this.recId = recId;
    }

    /**
     * Creates a file output stream to write to the file represented by 
     * the specified <code>File</code> object. If the second argument is
     * <code>true</code>, then bytes will be written to the end of the file
     * rather than the beginning. A new <code>FileDescriptor</code> object is
     * created to represent this file connection.
     * <p>
     * If the file exists but is a directory rather than a regular file, does
     * not exist but cannot be created, or cannot be opened for any other
     * reason then a <code>FileNotFoundException</code> is thrown.
     *
     * @param      file               the file to be opened for writing.
     * @param     append      if <code>true</code>, then bytes will be written
     *                   to the end of the file rather than the beginning
     * @param   recId the recorder id to aggregate the monitored data for
     * @throws FileNotFoundException  if the file exists but is a directory
     *                   rather than a regular file, does not exist but cannot
     *                   be created, or cannot be opened for any other reason
     */
    public InstrumentedFileOutputStream(File file, boolean append, 
        String recId) throws FileNotFoundException {
        super(file, append);
        this.recId = recId;
    }

    /**
     * Creates an output file stream to write to the specified file 
     * descriptor, which represents an existing connection to an actual 
     * file in the file system.
     *
     * @param      fdObj   the file descriptor to be opened for writing
     * @param   recId the recorder id to aggregate the monitored data for
     */
    public InstrumentedFileOutputStream(FileDescriptor fdObj, String recId) {
        super(fdObj);
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
        super.write(value);
        if (!(inWrite1 || inWrite3)) {
            /*if (!Helper.NEW_IO_HANDLING && null != IoNotifier.fileNotifier) {
                IoNotifier.fileNotifier.notifyWrite(recId, 1);
            } else {*/
            if (null != RecorderFrontend.instance) {
                RecorderFrontend.instance.writeIo(recId, 
                    null, 1, StreamType.FILE);
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
            super.write(buffer);
            if (!inWrite3) {
                /*if (!Helper.NEW_IO_HANDLING 
                    && null != IoNotifier.fileNotifier) {
                    IoNotifier.fileNotifier.notifyWrite(recId, buffer.length);
                } else {*/
                if (null != RecorderFrontend.instance) {
                    RecorderFrontend.instance.writeIo(recId, 
                        null, buffer.length, StreamType.FILE);
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
            super.write(buffer, off, len);
            if (!inWrite1) {
                /*if (!Helper.NEW_IO_HANDLING 
                    && null != IoNotifier.fileNotifier) {
                    IoNotifier.fileNotifier.notifyWrite(recId, len);
                } else {*/
                if (null != RecorderFrontend.instance) {
                    RecorderFrontend.instance.writeIo(recId, 
                        null, len, StreamType.FILE);
                }
                //}
            }
            inWrite3 = false;
        } catch (IOException e) {
            inWrite3 = false;
            throw e;
        }
    }

}
