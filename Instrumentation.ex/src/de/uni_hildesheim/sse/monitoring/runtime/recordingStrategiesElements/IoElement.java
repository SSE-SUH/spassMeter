package de.uni_hildesheim.sse.monitoring.runtime.recordingStrategiesElements;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import de.uni_hildesheim.sse.codeEraser.annotations.Operation;
import de.uni_hildesheim.sse.codeEraser.annotations.Variability;
import de.uni_hildesheim.sse.monitoring.runtime.AnnotationConstants;
import de.uni_hildesheim.sse.monitoring.runtime.boot.StreamType;
import de.uni_hildesheim.sse.monitoring.runtime.recordingStrategies.
    RecorderStrategy;
import de.uni_hildesheim.sse.monitoring.runtime.utils.StreamUtilities;

/**
 * Defines a ReadIoElement.
 * 
 * @author Stephan Dederichs
 * @since 1.00
 * @version 1.00
 */
@Variability(id = { AnnotationConstants.STRATEGY_TCP, 
        AnnotationConstants.MONITOR_NET_IO, 
        AnnotationConstants.MONITOR_FILE_IO }, op = Operation.AND)
public class IoElement extends RecordingStrategiesElement {
    
    /**
     * Stores the recId.
     */
    private String recId;
    
    /**
     * Stores the bytes.
     */
    private int bytes; 
    
    /**
     * Stores the type.
     */
    private StreamType type;
    
    /**
     * Stores the identification of the calling thread.
     */
    private long threadId;
    
    /**
     * Stores the caller.
     */
    private String caller;
    
    /**
     * Write (true) or read (false).
     */
    private boolean write;
    
    /**
     * Creates a new empty instance.
     * 
     * @since 1.00
     */
    public IoElement() { 
    }
    
    /**
     * Creates a new instance with the given parameters.
     * 
     * @param recId the recId
     * @param caller optional class name of the calling class
     * @param threadId the identification of the calling thread
     * @param bytes the bytes
     * @param type the type
     * @param write whether it is a write or read access
     * 
     * @since 1.00
     */
    public IoElement(String recId, String caller, long threadId, int bytes, 
        StreamType type, boolean write) {
        this.recId = recId;
        this.caller = caller;
        this.threadId = threadId;
        this.bytes = bytes;
        this.type = type;
        this.write = write;
    }

    /**
     * Getter for the recId.
     * 
     * @return the recId
     * 
     * @since 1.00
     */
    public String getRecId() {
        return recId;
    }

    /**
     * Setter for the recId.
     * 
     * @param recId the recId to set
     * 
     * @since 1.00
     */
    public void setRecId(String recId) {
        this.recId = recId;
    }

    /**
     * Getter for the bytes.
     * 
     * @return the bytes
     * 
     * @since 1.00
     */
    public int getBytes() {
        return bytes;
    }

    /**
     * Setter for the bytes.
     * 
     * @param bytes the bytes to set
     * 
     * @since 1.00
     */
    public void setBytes(int bytes) {
        this.bytes = bytes;
    }

    /**
     * Getter for the type.
     * 
     * @return the type
     * 
     * @since 1.00
     */
    public StreamType getType() {
        return type;
    }

    /**
     * Setter for the type.
     * 
     * @param type the type to set
     * 
     * @since 1.00
     */
    public void setType(StreamType type) {
        this.type = type;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void send(DataOutputStream out) throws IOException {
        StreamUtilities.writeString(out, recId);
        StreamUtilities.writeString(out, caller);
        out.writeInt(bytes);
        out.writeUTF(type.toString());
        out.writeLong(threadId);
        out.writeBoolean(write);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void read(DataInputStream in) throws IOException {
        recId = StreamUtilities.readString(in);
        caller = StreamUtilities.readString(in);
        bytes = in.readInt();
        type = StreamType.valueOf(in.readUTF());
        threadId = in.readLong();
        write = in.readBoolean();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int getIdentification() {
        return Constants.IO;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void process(RecorderStrategy strategy) {
        if (write) {
            strategy.writeIo(recId, caller, threadId, bytes, type);
        } else {
            strategy.readIo(recId, caller, threadId, bytes, type);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void clear() {
    }
}