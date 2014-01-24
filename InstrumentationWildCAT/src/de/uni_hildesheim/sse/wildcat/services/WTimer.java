package de.uni_hildesheim.sse.wildcat.services;

import javax.management.AttributeNotFoundException;

import de.uni_hildesheim.sse.serviceConstants.TimerConstants;

/**
 * WildCAT service for events from the {@link TimerChangeListener}.
 * 
 * @author Stephan Dederichs
 * 
 * @version 1.00
 * @since 1.00
 */
public class WTimer extends AbstractWServiceData {
    
    /**
     * Stores the recId.
     * 
     * @since 1.00
     */
    private String recId;
    
    /**
     * Stores a value.
     * 
     * @since 1.00
     */
    private long value;

    /**
     * Creates an instance of {@link WTimer}.
     * 
     * @param recId The recId.
     * @param value The value to store.
     * 
     * @since 1.00
     */
    public WTimer(String recId, long value) {
        this.recId = recId;
        this.value = value;
    }
    
    /**
     * Returns the recId.
     * 
     * @return The recId.
     * 
     * @since 1.00
     */
    public String getRecId() {
        String result = recId;
        return result;
    }
    
    /**
     * Returns the value.
     * 
     * @return The value.
     * 
     * @since 1.00
     */
    public long getValue() {
        long result = value;
        setSensorValue(TimerConstants.VALUE, result);
        return result;
    }
    
    /**
     * Sets the value.
     * 
     * @param value The value to set.
     * 
     * @since 1.00
     */
    public void setValue(long value) {
        setSensorValue(TimerConstants.VALUE, value);
        this.value = value;
    }
    
    // ------------------------------------------------------------------------
    
    @Override
    public void burstValueUpdate() {
        getRecId();
        getValue();
    }
    
    @Override
    public Object getDataSpecificAttribute(String attributeName) 
        throws AttributeNotFoundException {
        // Check for individual attributes
        if (attributeName.equals(TimerConstants.VALUE)) {
            return getValue();
        } else {
            throw new AttributeNotFoundException("Invalid attribute: "
                + attributeName);
        }
    }
    
}
