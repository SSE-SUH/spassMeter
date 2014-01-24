package de.uni_hildesheim.sse.wildcat.services;

import javax.management.AttributeNotFoundException;

import de.uni_hildesheim.sse.monitoring.runtime.plugins.ValueType;
import de.uni_hildesheim.sse.serviceConstants.ValueConstants;

/**
 * WildCAT service for events from the {@link ValueChangeListener}.
 * 
 * @author Stephan Dederichs
 * 
 * @version 1.00
 * @since 1.00
 */

public class WValue extends AbstractWServiceData {

    /**
     * Stores the recId.
     * 
     * @since 1.00
     */
    private String recId;
    
    /**
     * Stores the type.
     * 
     * @since 1.00
     */
    private ValueType type;
    
    /**
     * Stores the newValue.
     * 
     * @since 1.00
     */
    private Object newValue;
    
    /**
     * Creates an instance of {@link WValue}.
     * 
     * @param recId The recId.
     * @param type The type.
     * @param newValue The newValue.
     * 
     * @since 1.00
     */
    public WValue(String recId, ValueType type, Object newValue) {
        this.recId = recId;
        this.type = type;
        this.newValue = newValue;
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
     * Returns the type.
     * 
     * @return The type
     * 
     * @since 1.00
     */
    public String getType() {
        String result = type.toString();
        setSensorValue(ValueConstants.TYPE, result);
        return result;
        
    }
    
    /**
     * Returns the newValue.
     * 
     * @return The newValue.
     * 
     * @since 1.00
     */
    public Object getNewValue() {
        Object result =  newValue;
        setSensorValue(ValueConstants.NEW_VALUE, result);        
        return result;
    }
    
    /**
     * Sets the newValue.
     * 
     * @param newValue The newValue.
     * 
     * @since 1.00
     */
    public void setNewValue(Object newValue) {
        setSensorValue(ValueConstants.NEW_VALUE, newValue);
        this.newValue = newValue;
    }
    
    // ------------------------------------------------------------------------
    
    @Override
    public void burstValueUpdate() {
        getRecId();
        getType();
        getNewValue();
    }

    @Override
    public Object getDataSpecificAttribute(String attributeName)
        throws AttributeNotFoundException {
        if (attributeName.equals(ValueConstants.NEW_VALUE)) {
            return getNewValue();
        } else if (attributeName.equals(ValueConstants.TYPE)) {
            return getType();
        } else {
            throw new AttributeNotFoundException("Invalid attribute: "
                    + attributeName);
        }
    }
    
}
