package de.uni_hildesheim.sse.monitoring.runtime.recordingStrategiesElements;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import de.uni_hildesheim.sse.codeEraser.annotations.Variability;
import de.uni_hildesheim.sse.monitoring.runtime.AnnotationConstants;
import de.uni_hildesheim.sse.monitoring.runtime.recordingStrategies.
    RecorderStrategy;

/**
 * Defines an EndSystemElement.
 * 
 * @author Stephan Dederichs
 * @since 1.00
 * @version 1.00
 */
@Variability(id = AnnotationConstants.STRATEGY_TCP)
public class EndSystemElement extends RecordingStrategiesElement {

    /**
     * Creates a new empty instance.
     * 
     * @since 1.00
     */
    public EndSystemElement() {
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
        return Constants.ENDSYSTEM;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void process(RecorderStrategy strategy) {
        strategy.endSystem();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clear() {
    }
}