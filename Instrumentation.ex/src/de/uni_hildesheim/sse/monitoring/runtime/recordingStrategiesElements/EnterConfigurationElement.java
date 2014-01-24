package de.uni_hildesheim.sse.monitoring.runtime.recordingStrategiesElements;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import de.uni_hildesheim.sse.codeEraser.annotations.Variability;
import de.uni_hildesheim.sse.monitoring.runtime.AnnotationConstants;
import de.uni_hildesheim.sse.monitoring.runtime.recordingStrategies.
    RecorderStrategy;
import de.uni_hildesheim.sse.monitoring.runtime.utils.StreamUtilities;

/**
 * Defines a EnterConfigurationElement.
 * 
 * @author Stephan Dederichs
 * @since 1.00
 * @version 1.00
 */
@Variability(id = AnnotationConstants.STRATEGY_TCP)
public class EnterConfigurationElement extends RecordingStrategiesElement {

    /**
     * Stores the id.
     */
    private String id;
    
    /**
     * Creates a new empty instance.
     * 
     * @since 1.00
     */
    public EnterConfigurationElement() { 
    }
    
    /**
     * Creates a new instance with the given parameter.
     * 
     * @param id the id
     * 
     * @since 1.00
     */
    public EnterConfigurationElement(String id) {
        this.id = id;
    }

    /**
     * Getter for the id.
     * 
     * @return the id
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
     * {@inheritDoc}
     */
    @Override
    public void send(DataOutputStream out) throws IOException {
        StreamUtilities.writeString(out, id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void read(DataInputStream in) throws IOException {
        id = StreamUtilities.readString(in);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int getIdentification() {
        return Constants.ENTERCONFIGURATION;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void process(RecorderStrategy strategy) {
        strategy.enterConfiguration(id);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void clear() {
    }   
}