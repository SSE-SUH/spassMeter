package de.uni_hildesheim.sse.wildcat.services;

import java.util.LinkedList;
import java.util.List;

import javax.management.AttributeNotFoundException;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.RuntimeOperationsException;

import de.uni_hildesheim.sse.functions.IFunction;
import de.uni_hildesheim.sse.functions.IServiceData;
import de.uni_hildesheim.sse.wildcat.sensors.Sensor;

/**
 * Defines an abstract superclass for the specific data classes.
 * 
 * @author Stephan Dederichs
 * 
 * @version 1.00
 * @since 1.00
 */
public abstract class AbstractWServiceData implements IServiceData, 
    NotificationListener {
    
    /**
     * Instance of a battery data WildCAT sensor.
     * 
     * @since 1.00
     */
    private Sensor sensor;
    
    /**
     * List which contains all functions that are used for this MBean.
     * 
     * @since 1.00
     */
    private List<IFunction> functions = new LinkedList<IFunction>();
    
    /**
     * Updates all values in the sensor.
     * 
     * @since 1.00
     */
    public abstract void burstValueUpdate();
    
    /**
     * Updates a specific value in the sensor.
     * 
     * @param attributeName the name of value to update.
     * 
     * @return The changed value.
     * 
     * @throws AttributeNotFoundException If there is no value with the 
     *            given name.
     * 
     * @since 1.00
     */
    public abstract Object getDataSpecificAttribute(String attributeName)
        throws AttributeNotFoundException;
    
    /**
     * Sets the sensor.
     * 
     * @param sensor The sensor.
     * 
     * @since 1.00
     */
    public void setSensor(Sensor sensor) {
        this.sensor = sensor;
        // init sensor values
        burstValueUpdate();
    }
    
    /**
     * Sets the value for the given key.
     * 
     * @param key The key for the value.
     * @param value The value.
     * 
     * @since 1.00
     */
    public void setSensorValue(String key, Object value) {
        sensor.setValue(key, value);
    }

    /**
     * Adds a function of the type {@link IFunction} to the service class.
     * 
     * @param function The function to add.
     * 
     * @since 1.00
     */
    public void addFunction(IFunction function) {
        functions.add(function);
    }
    
    @Override
    public Object getSpecificAttribute(String attributeName) {
        Object result = null;
        try {
            result = getAttribute(attributeName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
    
    /**
     * Updates a value in the sensor.
     * 
     * @param attributeName the name of value to update.
     * 
     * @throws AttributeNotFoundException If there is no value with the 
     *            given name.
     *            
     * @return The changed value.
     * 
     * @since 1.00
     */
    public Object getAttribute(String attributeName) throws 
        AttributeNotFoundException {
        // checking if valueName is null
        if (null == attributeName) {
            IllegalArgumentException iae = new IllegalArgumentException(
                    "Value name cannot be null");
            throw new RuntimeOperationsException(iae, "null value name");
        }
        
        // Check the names from the functions (min, max, avg, ...)
        if (!functions.isEmpty()) {
            for (IFunction function : functions) {
                if (attributeName.equals(function.getIdentity().
                    getAttributeName() + "." + function.getFunctionType())) {
                    return function.getValue();
                }
            }
        }
        
        // Check the individual attributenames
        return getDataSpecificAttribute(attributeName);
    }

    @Override
    public void handleNotification(Notification notification, Object handback) {
        burstValueUpdate();
        updateFunctionValues();
    }
    
    /**
     * Updates all function values.
     * 
     * @since 1.00
     */
    public void updateFunctionValues() {
        for (IFunction function : functions) {
            function.calculate();
            String key = function.getIdentity().getAttributeName() + "." 
                    + function.getFunctionType();
            sensor.setValue(key, function.getValue());
        }
    }

}
