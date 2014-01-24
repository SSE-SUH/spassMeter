package de.uni_hildesheim.sse.monitoring.runtime.recordingStrategiesElements;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import de.uni_hildesheim.sse.codeEraser.annotations.Variability;
import de.uni_hildesheim.sse.monitoring.runtime.AnnotationConstants;
import de.uni_hildesheim.sse.monitoring.runtime.recordingStrategies.
    RecorderStrategy;

/**
 * Defines a StartRecordingElement.
 * 
 * @author Stephan Dederichs
 * @since 1.00
 * @version 1.00
 */
@Variability(id = AnnotationConstants.STRATEGY_TCP)
public class StartRecordingElement extends RecordingStrategiesElement {

    /**
     * Stores the now.
     */
    private long now;
    
    /**
     * Stores the threadId.
     */
    private long threadId;
    
    /**
     * Stores the current CPU ticks.
     */
    private long threadTicks;
    
    /**
     * Creates a new empty instance.
     * 
     * @since 1.00
     */
    public StartRecordingElement() { 
    }
    
    /**
     * Creates a new instance with the given parameters.
     * 
     * @since 1.00
     * 
     * @param now the now
     * @param threadId the threadId
     * @param threadTicks the current CPU ticks
     */
    public StartRecordingElement(long now, long threadId, 
        long threadTicks) {
        this.now = now;
        this.threadId = threadId;
        this.threadTicks = threadTicks;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void send(DataOutputStream out) throws IOException {
        out.writeLong(now);
        out.writeLong(threadId);
        out.writeLong(threadTicks);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void read(DataInputStream in) throws IOException {
        now = in.readLong();
        threadId = in.readLong();
        threadTicks = in.readLong();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int getIdentification() {
        return Constants.STARTRECORDING;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void process(RecorderStrategy strategy) {
        strategy.startRecording(now, threadId, threadTicks);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void clear() {
    }
}