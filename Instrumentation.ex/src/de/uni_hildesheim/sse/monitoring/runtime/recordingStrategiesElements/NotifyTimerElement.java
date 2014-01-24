package de.uni_hildesheim.sse.monitoring.runtime.recordingStrategiesElements;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import de.uni_hildesheim.sse.codeEraser.annotations.Variability;
import de.uni_hildesheim.sse.monitoring.runtime.AnnotationConstants;
import de.uni_hildesheim.sse.monitoring.runtime.annotations.TimerState;
import de.uni_hildesheim.sse.monitoring.runtime.recordingStrategies.
    RecorderStrategy;
import de.uni_hildesheim.sse.monitoring.runtime.utils.StreamUtilities;

/** 
 * Defines an ExitElement.
 * 
 * @author Stephan Dederichs
 * @since 1.00
 * @version 1.00
 */
@Variability(id = AnnotationConstants.STRATEGY_TCP)
public class NotifyTimerElement extends RecordingStrategiesElement {
    
    /**
     * Stores the timer id.
     */
    private String id;
    
    /**
     * Stores the timer state.
     */
    private TimerState state;
    
    /**
     * Stores the current time in milli seconds.
     */
    private long now;
    
    /**
     * Stores the thread id, may be negative if irrelevant.
     */
    private long threadId;
    
    /**
     * Creates a new empty instance.
     * 
     * @since 1.00
     */
    public NotifyTimerElement() {
    }
    
    /**
     * Creates a new instance with the given parameters.
     * 
     * @param id the timer identification (may overlap with recorder ids)
     * @param state the new timer state
     * @param now the current system time in milliseconds
     * @param threadId the id of the currently executing thread, may be 
     *     negative if it should be ignored
     * 
     * @since 1.00
     */
    public NotifyTimerElement(String id, TimerState state, long now, 
        long threadId) {
        // do not call this from outside, use the ObjectPools
        this.id = id;
        this.state = state;
        this.now = now;
        this.threadId = threadId;
    }

    /**
     * Getter for the recId.
     * 
     * @return the recId
     * 
     * @since 1.00
     */
    public String getId() {
        return id;
    }

    /**
     * Setter for the id.
     * 
     * @param id the id to set
     * 
     * @since 1.00
     */
    public void setId(String id) {
        this.id = id;
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
     * Getter for the threadId.
     * 
     * @return the threadId, may be negative if irrelevant
     * 
     * @since 1.00
     */
    public long getThreadId() {
        return threadId;
    }

    /**
     * Setter for the threadId.
     * 
     * @param threadId the id to set, may be negative if irrelevant
     * 
     * @since 1.00
     */
    public void setThreadId(long threadId) {
        this.threadId = threadId;
    }

    /**
     * Getter for the state.
     * 
     * @return the state
     * 
     * @since 1.00
     */
    public TimerState getState() {
        return state;
    }

    /**
     * Setter for the state.
     * 
     * @param state the new state to set
     * 
     * @since 1.00
     */
    public void setState(TimerState state) {
        this.state = state;
    }

    
    /**
     * {@inheritDoc}
     */
    @Override
    public void send(DataOutputStream out) throws IOException {
        StreamUtilities.writeString(out, id);
        out.writeLong(now);
        out.writeLong(threadId);
        out.writeInt(state.ordinal());
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void read(DataInputStream in) throws IOException {
        id = StreamUtilities.readString(in);
        now = in.readLong();
        threadId = in.readLong();
        state = TimerState.values()[in.readInt()];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getIdentification() {
        return Constants.NOTIFYTIMER;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void process(RecorderStrategy strategy) {
        strategy.notifyTimer(id, state, now, threadId);
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
    public void release() {
    }
    
}