package de.uni_hildesheim.sse.monitoring.runtime.recordingStrategiesElements;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import de.uni_hildesheim.sse.monitoring.runtime.recordingStrategies.
    RecorderStrategy;

/**
 * Defines a message element which transports recording of threads e.g. over
 * network.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public class RegisterElement extends RecordingStrategiesElement {

    /**
     * Stores the thread identification.
     */
    private long threadId;
    
    /**
     * Stores the id of the new thread.
     */
    private long newId;
    
    /**
     * The number of nano seconds in {@link #threadId}.
     */
    private long threadTicks;
    
    /**
     * The current time in nano seconds.
     */
    private long now;
    
    /**
     * Creates a new message.
     * 
     * @param threadId the identification of the thread to be registered
     * @param newId the new thread
     * @param threadTicks the ticks of <code>threadId</code>
     * @param now the current time in nano seconds
     * 
     * @since 1.00
     */
    public RegisterElement(long threadId, long newId, long threadTicks, 
        long now) {
        this.threadId = threadId;
        this.newId = newId;
        this.threadTicks = threadTicks;
        this.now = now;
    }
    
    /**
     * Creates a new message without explicit information.
     * 
     * @since 1.00
     */
    public RegisterElement() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void send(DataOutputStream out) throws IOException {
        out.writeLong(threadId);
        out.writeLong(newId);
        out.writeLong(threadTicks);
        out.writeLong(now);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void read(DataInputStream in) throws IOException {
        threadId = in.readLong();
        newId = in.readLong();
        threadTicks = in.readLong();
        now = in.readLong();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getIdentification() {
        return Constants.REGISTER;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void process(RecorderStrategy strategy) {
        strategy.register(threadId, newId, threadTicks, now);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clear() {
    }

}
