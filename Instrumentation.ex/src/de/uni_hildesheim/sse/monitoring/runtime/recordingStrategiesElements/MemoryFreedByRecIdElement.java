package de.uni_hildesheim.sse.monitoring.runtime.recordingStrategiesElements;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import de.uni_hildesheim.sse.codeEraser.annotations.Operation;
import de.uni_hildesheim.sse.codeEraser.annotations.Variability;
import de.uni_hildesheim.sse.monitoring.runtime.AnnotationConstants;
import de.uni_hildesheim.sse.monitoring.runtime.boot.ObjectPool;
import de.uni_hildesheim.sse.monitoring.runtime.boot.Poolable;
import de.uni_hildesheim.sse.monitoring.runtime.recordingStrategies.
    RecorderStrategy;
import de.uni_hildesheim.sse.monitoring.runtime.utils.StreamUtilities;

/**
 * Defines a MemoryFreedByRecIdElement.
 * 
 * @author Stephan Dederichs
 * @since 1.00
 * @version 1.00
 */
@Variability(id = { AnnotationConstants.STRATEGY_TCP, 
        AnnotationConstants.MONITOR_MEMORY_USAGE }, op = Operation.AND)
public class MemoryFreedByRecIdElement extends RecordingStrategiesElement 
    implements Poolable<MemoryFreedByRecIdElement> {

    /**
     * Defines an object pool for this class.
     */
    public static final ObjectPool<MemoryFreedByRecIdElement> POOL 
        = new ObjectPool<MemoryFreedByRecIdElement>(
            new MemoryFreedByRecIdElement(), 500);
    
    /**
     * Stores the recId.
     */
    private String recId;
    
    /**
     * Stores the size.
     */
    private long size;
    
    /**
     * Creates a new empty instance.
     * 
     * @since 1.00
     */
    private MemoryFreedByRecIdElement() {
    }
    
    /**
     * Creates a new instance with the given parameters.
     * 
     * @param recId the recId
     * @param size the size
     * 
     * @since 1.00
     */
    private MemoryFreedByRecIdElement(String recId, long size) {
        this.recId = recId;
        this.size = size;
    }

    /**
     * Sets the data in one step.
     * 
     * @param recId the recorder identification
     * @param size the size of the allocation
     * 
     * @since 1.00
     */
    public void setData(String recId, long size) {
        this.recId = recId;
        this.size = size;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void send(DataOutputStream out) throws IOException {
        StreamUtilities.writeString(out, recId);
        out.writeLong(size);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void read(DataInputStream in) throws IOException {
        recId = StreamUtilities.readString(in);
        size = in.readLong();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int getIdentification() {
        return Constants.MEMORYFREEDBYRECID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void process(RecorderStrategy strategy) {
        strategy.memoryFreedByRecId(recId, size);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void clear() {
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public MemoryFreedByRecIdElement create() {
        return new MemoryFreedByRecIdElement();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void release() {
        POOL.release(this);
    }

}