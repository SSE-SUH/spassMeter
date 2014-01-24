package de.uni_hildesheim.sse.monitoring.runtime.recordingStrategiesElements;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import de.uni_hildesheim.sse.codeEraser.annotations.Variability;
import de.uni_hildesheim.sse.monitoring.runtime.AnnotationConstants;
import de.uni_hildesheim.sse.monitoring.runtime.boot.Poolable;
import de.uni_hildesheim.sse.monitoring.runtime.recordingStrategies.
    RecorderStrategy;
import de.uni_hildesheim.sse.monitoring.runtime.recordingStrategies.ThreadsInfo;
import de.uni_hildesheim.sse.monitoring.runtime.utils.StreamUtilities;

/**
 * Defines an AssignToAll element.
 * 
 * @author Stephan Dederichs
 * @since 1.00
 * @version 1.00
 */
@Variability(id = AnnotationConstants.STRATEGY_TCP)
public class AssignToAllElement extends RecordingStrategiesElement 
    implements Poolable<AssignToAllElement> {
    
    /**
     * Stores the recId.
     */
    private String recId;
    
    /**
     * Stores the enter state.
     */
    private boolean enter;
    
    /**
     * Stores the now.
     */
    private long now;
    
    /**
     * Stores the {@link ThreadsInfo}.
     */
    private ThreadsInfo threadsInfo;
    
    /**
     * Creates a new empty instance.
     * 
     * @since 1.00
     */
    public AssignToAllElement() {
    }
    
    /**
     * Creates a new instance with the given parameters.
     * 
     * @param recId the recording id (may be also the variability prefix)
     * @param enter <code>true</code> if this is a method enter event, 
     *   <code>false</code> else
     * @param now the current system time
     * @param threadsInfo information on the current thread
     * 
     * @since 1.00
     */
    public AssignToAllElement(String recId, boolean enter, long now, 
        ThreadsInfo threadsInfo) {
        // do not call this from outside, use the ObjectPools
        this.recId = recId;
        this.enter = enter;
        this.now = now;
        this.threadsInfo = threadsInfo;
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
     * Getter for enter.
     * 
     * @return the enter state
     * 
     * @since 1.00
     */
    public boolean getEnter() {
        return enter;
    }

    /**
     * Setter for the enter state.
     * 
     * @param enter the new enter state
     * 
     * @since 1.00
     */
    public void setEnter(boolean enter) {
        this.enter = enter;
    }

    /**
     * Getter for the now.
     * 
     * @return the now
     * 
     * @since 1.00
     */
    public long getNow() {
        return now;
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
     * Getter for the {@link ThreadsInfo}.
     * 
     * @return the threadsInfo
     * 
     * @since 1.00
     */
    public ThreadsInfo getThreadsInfo() {
        return threadsInfo;
    }

    /**
     * Setter for the {@link ThreadsInfo}.
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
        out.writeBoolean(enter);
        out.writeLong(now);
        threadsInfo.write(out);
    }
    
    /**
     * {@inheritDoc}
     */    
    @Override
    public void read(DataInputStream in) throws IOException {
        recId = StreamUtilities.readString(in);
        enter = in.readBoolean();
        now = in.readLong();
        threadsInfo = ThreadsInfo.readFromPool(in);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getIdentification() {
        return Constants.ASSIGNTOALL;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void process(RecorderStrategy strategy) {
        strategy.assignAllTo(recId, enter, now, threadsInfo);
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
    public AssignToAllElement create() {
        return new AssignToAllElement();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void release() {
    }
    
}