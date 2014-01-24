package de.uni_hildesheim.sse.monitoring.runtime.recordingStrategiesElements;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import de.uni_hildesheim.sse.codeEraser.annotations.Variability;
import de.uni_hildesheim.sse.monitoring.runtime.AnnotationConstants;
import de.uni_hildesheim.sse.monitoring.runtime.boot.ObjectPool;
import de.uni_hildesheim.sse.monitoring.runtime.boot.Poolable;
import de.uni_hildesheim.sse.monitoring.runtime.recordingStrategies.
    RecorderStrategy;
import de.uni_hildesheim.sse.monitoring.runtime.recordingStrategies.ThreadsInfo;
import de.uni_hildesheim.sse.monitoring.runtime.utils.StreamUtilities;

/** 
 * Defines an ExitElement.
 * 
 * @author Stephan Dederichs
 * @since 1.00
 * @version 1.00
 */
@Variability(id = AnnotationConstants.STRATEGY_TCP)
public class ExitElement extends RecordingStrategiesElement 
    implements Poolable<ExitElement> {
    
    /**
     * Defines an object pool for this class.
     */
    public static final ObjectPool<ExitElement> POOL 
        = new ObjectPool<ExitElement>(new ExitElement(), 500);
    
    /**
     * Stores the recId.
     */
    private String recId;
    
    /**
     * Stores the now.
     */
    private long now;
    
    /**
     * Stores the {@link ThreadsInfo}.
     */
    private ThreadsInfo threadsInfo;
    
    /**
     * Is this an exclusion from monitoring.
     */
    private boolean exclude;
    
    /**
     * Creates a new empty instance.
     * 
     * @since 1.00
     */
    ExitElement() {
        // do not call this from outside, use the ObjectPools
    }
    
    /**
     * Creates a new instance with the given parameters.
     * 
     * @param recId the recId
     * @param now the now
     * @param threadsInfo the threadsInfo
     * @param exclude is this an exclusion from monitoring
     * 
     * @since 1.00
     */
    public ExitElement(String recId, long now, ThreadsInfo threadsInfo, 
        boolean exclude) {
        // do not call this from outside, use the ObjectPools
        this.recId = recId;
        this.now = now;
        this.threadsInfo = threadsInfo;
        this.exclude = exclude;
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
     * Setter for the now.
     * 
     * @param now the now to set
     * 
     * @since 1.00
     */
    public void setNow(long now) {
        this.now = now;
    }
    
    /**
     * Sets whether this is an exclusion from monitoring.
     * 
     * @param exclude is this an exclusion from monitoring
     * 
     * @since 1.00
     */
    public void setExclude(boolean exclude) {
        this.exclude = exclude;
    }

    /**
     * Setter for the threadsInfo.
     * 
     * @param threadsInfo the threadsInfo to set
     * 
     * @since 1.00
     */
    public void setThreadsInfo(ThreadsInfo threadsInfo) {
        this.threadsInfo = threadsInfo;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void send(DataOutputStream out) throws IOException {
        StreamUtilities.writeString(out, recId);
        out.writeLong(now);
        threadsInfo.write(out);
        out.writeBoolean(exclude);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void read(DataInputStream in) throws IOException {
        recId = StreamUtilities.readString(in);
        now = in.readLong();
        threadsInfo = ThreadsInfo.readFromPool(in);
        exclude = in.readBoolean();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getIdentification() {
        return Constants.EXIT;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void process(RecorderStrategy strategy) {
        strategy.exit(recId, now, threadsInfo, exclude);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clear() {
        ThreadsInfo.POOL.release(threadsInfo);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ExitElement create() {
        return new ExitElement();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void release() {
        POOL.release(this);
    }
}