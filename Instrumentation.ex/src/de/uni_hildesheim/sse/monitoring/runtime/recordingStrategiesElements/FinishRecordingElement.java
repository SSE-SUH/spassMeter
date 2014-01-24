package de.uni_hildesheim.sse.monitoring.runtime.recordingStrategiesElements;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import de.uni_hildesheim.sse.codeEraser.annotations.Variability;
import de.uni_hildesheim.sse.monitoring.runtime.AnnotationConstants;
import de.uni_hildesheim.sse.monitoring.runtime.recordingStrategies.
    RecorderStrategy;
import de.uni_hildesheim.sse.monitoring.runtime.utils.LongLongHashMap;

/**
 * Defines a FinishRecordingElement.
 * 
 * @author Stephan Dederichs
 * @since 1.00
 * @version 1.00
 */
@Variability(id = AnnotationConstants.STRATEGY_TCP)
public class FinishRecordingElement extends RecordingStrategiesElement {
    
    /**
     * Stores the now.
     */
    private long now;
    
    /**
     * The id of the current thread.
     */
    private long threadId;

    /**
     * The id-cpuTime mapping.
     */
    private LongLongHashMap curCpuTime;
    
    /**
     * Creates a new empty instance.
     * 
     * @since 1.00
     */
    public FinishRecordingElement() {
        curCpuTime = new LongLongHashMap();
    }
    
    /**
     * Creates a new instance with the given parameters.
     * 
     * @param now the now
     * @param threadId the id of the current thread
     * @param curCpuTime the id-CPUtime mapping of all threads
     * 
     * @since 1.00
     */
    public FinishRecordingElement(long now, long threadId, 
        LongLongHashMap curCpuTime) {
        this.now = now;
        this.threadId = threadId;
        this.curCpuTime = curCpuTime;
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
     * {@inheritDoc}
     */
    @Override
    public void send(DataOutputStream out) throws IOException {
        out.writeLong(now);
        out.writeLong(threadId);
        curCpuTime.write(out);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void read(DataInputStream in) throws IOException {
        now = in.readLong();
        threadId = in.readLong();
        curCpuTime.read(in);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int getIdentification() {
        return Constants.FINISHRECORDING;
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public void process(RecorderStrategy strategy) {
        strategy.finishRecording(now, threadId, curCpuTime);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void clear() {
    }
}