package de.uni_hildesheim.sse.monitoring.runtime.recordingStrategiesElements;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import de.uni_hildesheim.sse.codeEraser.annotations.Variability;
import de.uni_hildesheim.sse.monitoring.runtime.AnnotationConstants;
import de.uni_hildesheim.sse.monitoring.runtime.recordingStrategies.
    RecorderStrategy;
import de.uni_hildesheim.sse.monitoring.runtime.recordingStrategies.ThreadsInfo;

/**
 * Defines a StopTimeRecordingElement.
 * 
 * @author Stephan Dederichs
 * @since 1.00
 * @version 1.00
 */
@Variability(id = AnnotationConstants.STRATEGY_TCP)
public class StopTimeRecordingElement extends RecordingStrategiesElement {

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
    public StopTimeRecordingElement() {    
    }
    
    /**
     * Creates a new instance with the given parameters.
     * 
     * @param now the now
     * @param threadInfo the threadsInfo
     * 
     * @since 1.00
     */
    public StopTimeRecordingElement(long now, ThreadsInfo threadInfo) {
        this.now = now;
        this.threadsInfo = threadInfo;
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
     * Getter for the threadsInfo.
     * 
     * @return the threadInfo
     * 
     * @since 1.00
     */
    public ThreadsInfo getThreadsInfo() {
        return threadsInfo;
    }

    /**
     * Setter for the treadsInfo.
     * 
     * @param threadsInfo the threadInfo to set
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
        out.writeLong(now);
        threadsInfo.write(out);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void read(DataInputStream in) throws IOException {
        now = in.readLong();
        threadsInfo = ThreadsInfo.readFromPool(in);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getIdentification() {
        return Constants.STOP_TIME_RECORDING;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void process(RecorderStrategy strategy) {
        strategy.stopTimeRecording(now, threadsInfo);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void clear() {
        ThreadsInfo.POOL.release(threadsInfo);
    }
}
