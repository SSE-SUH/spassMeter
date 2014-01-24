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
 * Defines a MemoryAllocatedElement.
 * 
 * @author Stephan Dederichs
 * @since 1.00
 * @version 1.00
 */
@Variability(id = { AnnotationConstants.STRATEGY_TCP, 
        AnnotationConstants.MONITOR_MEMORY_ALLOCATED }, op = Operation.AND)
public class MemoryAllocatedElement extends RecordingStrategiesElement
        implements Poolable<MemoryAllocatedElement> {

    /**
     * Defines an object pool for this class.
     */
    public static final ObjectPool<MemoryAllocatedElement> POOL 
        = new ObjectPool<MemoryAllocatedElement>(
            new MemoryAllocatedElement(), 500);
    
    /**
     * Stores the recId.
     */
    private String recId;
    
    /**
     * Stores the id.
     */
    private long id;
    
    /**
     * Stores the size.
     */
    private long size;
    
    /**
     * Stores the identification of the calling thread.
     */
    private long threadId;

    /**
     * Creates a new empty instance.
     * 
     * @since 1.00
     */
    MemoryAllocatedElement() {
        // do not call this from outside, use the ObjectPools
    }

    /**
     * Creates a new instance with the given parameters.
     * 
     * @param recId the recId
     * @param threadId the identification of the calling thread
     * @param id the internal object-specific id
     * @param size the size
     * 
     * @since 1.00
     */
    public MemoryAllocatedElement(String recId, long threadId, long id, 
        long size) {
        // do not call this from outside, use the ObjectPools
        this.recId = recId;
        this.threadId = threadId;
        this.id = id;
        this.size = size;
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
     * @param recId the recId
     * @param threadId the identification of the calling thread
     * @param id the internal object-specific id
     * @param size the size
     * 
     * @since 1.00
     */
    public void setData(String recId, long threadId, long id, long size) {
        this.recId = recId;
        this.threadId = threadId;
        this.id = id;
        this.size = size;
    }

    /**
     * Getter for the size.
     * 
     * @return the size
     * 
     * @since 1.00
     */
    public long getSize() {
        return size;
    }

    /**
     * Setter for the size.
     * 
     * @param size the size to set
     * 
     * @since 1.00
     */
    public void setSize(long size) {
        this.size = size;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void send(DataOutputStream out) throws IOException {
        StreamUtilities.writeString(out, recId);
        out.writeLong(id);
        out.writeLong(size);
        out.writeLong(threadId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void read(DataInputStream in) throws IOException {
        recId = StreamUtilities.readString(in);
        id = in.readLong();
        size = in.readLong();
        threadId = in.readLong();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getIdentification() {
        return Constants.MEMORYALLOCATED;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void process(RecorderStrategy strategy) {
        strategy.memoryAllocated(recId, threadId, id, size);
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
    public MemoryAllocatedElement create() {
        return new MemoryAllocatedElement();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void release() {
        POOL.release(this);
    }
    
}