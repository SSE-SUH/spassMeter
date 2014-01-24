package de.uni_hildesheim.sse.monitoring.runtime.recordingStrategiesElements;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import de.uni_hildesheim.sse.codeEraser.annotations.Operation;
import de.uni_hildesheim.sse.codeEraser.annotations.Variability;
import de.uni_hildesheim.sse.monitoring.runtime.AnnotationConstants;
import de.uni_hildesheim.sse.monitoring.runtime.plugins.ValueType;
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
@Variability(id = {AnnotationConstants.STRATEGY_TCP, 
        AnnotationConstants.MONITOR_VALUES }, op = Operation.AND)
public class NotifyValueChangeElement extends RecordingStrategiesElement {
        
    /**
     * Stores the attribute id.
     */
    private String id;

    /**
     * Stores the type of the value.
     */
    private ValueType type;
    
    /**
     * Stores the timer state.
     */
    private Object value;
    
    /**
     * Creates a new empty instance.
     * 
     * @since 1.00
     */
    public NotifyValueChangeElement() {
    }
    
    /**
     * Creates a new instance with the given parameters.
     * 
     * @param id the timer identification (may overlap with recorder ids)
     * @param type the type of the value
     * @param value the new value after the change
     * 
     * @since 1.00
     */
    public NotifyValueChangeElement(String id, ValueType type, Object value) {
        // do not call this from outside, use the ObjectPools
        this.id = id;
        this.type = type;
        this.value = value;
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
     * Getter for the value.
     * 
     * @return the value
     * 
     * @since 1.00
     */
    public Object getValue() {
        return value;
    }

    /**
     * Setter for the value.
     * 
     * @param value the value to set
     * 
     * @since 1.00
     */
    public void setValue(Object value) {
        this.value = value;
    }

    /**
     * Getter for the type.
     * 
     * @return the type
     * 
     * @since 1.00
     */
    public ValueType getType() {
        return type;
    }

    /**
     * Setter for the type.
     * 
     * @param type the type to set
     * 
     * @since 1.00
     */
    public void setType(ValueType type) {
        this.type = type;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void send(DataOutputStream out) throws IOException {
        StreamUtilities.writeString(out, id);
        out.writeInt(type.ordinal());
        type.write(out, value);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void read(DataInputStream in) throws IOException {
        id = StreamUtilities.readString(in);
        int t = in.readInt();
        type = ValueType.values()[t];
        value = type.read(in);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getIdentification() {
        return Constants.NOTIFYVALUE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void process(RecorderStrategy strategy) {
        strategy.notifyValueChange(id, type, value);
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