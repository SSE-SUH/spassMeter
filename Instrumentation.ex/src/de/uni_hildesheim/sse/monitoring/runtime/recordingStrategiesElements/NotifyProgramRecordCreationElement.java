package de.uni_hildesheim.sse.monitoring.runtime.recordingStrategiesElements;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import de.uni_hildesheim.sse.monitoring.runtime.recordingStrategies.
    RecorderStrategy;

/** 
 * Defines an ExitElement.
 * 
 * @author Stephan Dederichs
 * @since 1.00
 * @version 1.00
 */
public class NotifyProgramRecordCreationElement 
    extends RecordingStrategiesElement {
        
    /**
     * Creates a new empty instance.
     * 
     * @since 1.00
     */
    public NotifyProgramRecordCreationElement() {
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void send(DataOutputStream out) throws IOException {
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void read(DataInputStream in) throws IOException {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getIdentification() {
        return Constants.PROGRAMRECORDCREATION;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void process(RecorderStrategy strategy) {
        strategy.notifyProgramRecordCreation();
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