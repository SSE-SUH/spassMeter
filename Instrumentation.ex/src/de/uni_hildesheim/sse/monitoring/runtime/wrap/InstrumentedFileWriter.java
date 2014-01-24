package de.uni_hildesheim.sse.monitoring.runtime.wrap;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileWriter;
import java.io.IOException;

//import de.uni_hildesheim.sse.monitoring.runtime.annotations.Helper;
//import de.uni_hildesheim.sse.monitoring.runtime.boot.IoNotifier;
import de.uni_hildesheim.sse.monitoring.runtime.boot.RecorderFrontend;
import de.uni_hildesheim.sse.monitoring.runtime.boot.StreamType;

/**
 * Extends the file writer class defined by java in order to obtain
 * information while writing to a file.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public class InstrumentedFileWriter extends FileWriter {

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
    private String recId = null;
    
    /**
     * Creates a file writer to write to the file with the 
     * specified name. A new <code>FileDescriptor</code> object is 
     * created to represent this file connection.
     * <p>
     * If the file exists but is a directory rather than a regular file, does
     * not exist but cannot be created, or cannot be opened for any other
     * reason then a <code>FileNotFoundException</code> is thrown.
     *
     * @param      name   the system-dependent filename
     * @exception  IOException  if the file exists but is a directory
     *                   rather than a regular file, does not exist but cannot
     *                   be created, or cannot be opened for any other reason
     */
    public InstrumentedFileWriter(String name) throws IOException {
        super(name);
    }

    /**
     * Constructs a file writer object given a file name with a boolean
     * indicating whether or not to append the data written.
     *
     * @param name  String The system-dependent filename.
     * @param append    boolean if <code>true</code>, then data will be written
     *                  to the end of the file rather than the beginning.
     * @throws IOException  if the named file exists but is a directory rather
     *                  than a regular file, does not exist but cannot be
     *                  created, or cannot be opened for any other reason
     */
    public InstrumentedFileWriter(String name, boolean append) 
        throws IOException {
        super(name, append);
    }

    /**
     * Constructs a file writer object given a File object.
     *
     * @param file  a File object to write to.
     * @throws IOException  if the file exists but is a directory rather than
     *                  a regular file, does not exist but cannot be created,
     *                  or cannot be opened for any other reason
     */
    public InstrumentedFileWriter(File file) throws IOException {
        super(file);
    }

    /**
     * Constructs a file writer object given a File object. If the second
     * argument is <code>true</code>, then bytes will be written to the end
     * of the file rather than the beginning.
     *
     * @param file  a File object to write to
     * @param     append    if <code>true</code>, then bytes will be written
     *                      to the end of the file rather than the beginning
     * @throws IOException  if the file exists but is a directory rather than
     *                  a regular file, does not exist but cannot be created,
     *                  or cannot be opened for any other reason
     */
    public InstrumentedFileWriter(File file, boolean append) 
        throws IOException {
        super(file, append);
    }

    /**
     * Constructs a FileWriter object associated with a file descriptor.
     *
     * @param fdObj  FileDescriptor object to write to.
     */
    public InstrumentedFileWriter(FileDescriptor fdObj) {
        super(fdObj);
    }

    /**
     * Creates a file writer to write to the file with the 
     * specified name. A new <code>FileDescriptor</code> object is 
     * created to represent this file connection.
     * <p>
     * If the file exists but is a directory rather than a regular file, does
     * not exist but cannot be created, or cannot be opened for any other
     * reason then a <code>FileNotFoundException</code> is thrown.
     *
     * @param      name   the system-dependent filename
     * @param   recId the recorder id to aggregate the monitored data for
     * @exception  IOException  if the file exists but is a directory
     *                   rather than a regular file, does not exist but cannot
     *                   be created, or cannot be opened for any other reason
     */
    public InstrumentedFileWriter(String name, String recId) 
        throws IOException {
        super(name);
        this.recId = recId;
    }

    /**
     * Constructs a file writer object given a file name with a boolean
     * indicating whether or not to append the data written.
     *
     * @param name  String The system-dependent filename.
     * @param append    boolean if <code>true</code>, then data will be written
     *                  to the end of the file rather than the beginning.
     * @param   recId the recorder id to aggregate the monitored data for
     * @throws IOException  if the named file exists but is a directory rather
     *                  than a regular file, does not exist but cannot be
     *                  created, or cannot be opened for any other reason
     */
    public InstrumentedFileWriter(String name, boolean append, String recId) 
        throws IOException {
        super(name, append);
        this.recId = recId;
    }

    /**
     * Constructs a file writer object given a File object.
     *
     * @param file  a File object to write to.
     * @param   recId the recorder id to aggregate the monitored data for
     * @throws IOException  if the file exists but is a directory rather than
     *                  a regular file, does not exist but cannot be created,
     *                  or cannot be opened for any other reason
     */
    public InstrumentedFileWriter(File file, String recId) throws IOException {
        super(file);
        this.recId = recId;
    }

    /**
     * Constructs a file writer object given a File object. If the second
     * argument is <code>true</code>, then bytes will be written to the end
     * of the file rather than the beginning.
     *
     * @param file  a File object to write to
     * @param     append    if <code>true</code>, then bytes will be written
     *                      to the end of the file rather than the beginning
     * @param   recId the recorder id to aggregate the monitored data for
     * @throws IOException  if the file exists but is a directory rather than
     *                  a regular file, does not exist but cannot be created,
     *                  or cannot be opened for any other reason
     */
    public InstrumentedFileWriter(File file, boolean append, String recId) 
        throws IOException {
        super(file, append);
        this.recId = recId;
    }

    /**
     * Constructs a FileWriter object associated with a file descriptor.
     *
     * @param fdObj  FileDescriptor object to write to.
     * @param   recId the recorder id to aggregate the monitored data for
     */
    public InstrumentedFileWriter(FileDescriptor fdObj, String recId) {
        super(fdObj);
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
    public void write(char[] buffer) throws IOException {
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
    public void write(char[] buffer, int off, int len) throws IOException {
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

    /**
     * Writes a portion of a string.
     *
     * @param  str  A String
     * @param  off  Offset from which to start writing characters
     * @param  len  Number of characters to write
     *
     * @exception  IOException  If an I/O error occurs
     */
    @Override
    public void write(String str, int off, int len) throws IOException {
        inWrite3 = true;
        try {
            super.write(str, off, len);
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
