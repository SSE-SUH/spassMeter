package de.uni_hildesheim.sse.wildcat.sensors;

import java.util.LinkedHashMap;
import java.util.Map;

import org.ow2.wildcat.hierarchy.attribute.POJOAttribute;

/**
 * WildCAT Sensor for the gearsBridge.
 * 
 * @author Stephan Dederichs
 * 
 * @version 1.00
 * @since 1.00
 */
public class Sensor extends POJOAttribute {

    /**
     * Stores all battery data in key value pairs.
     * 
     * @since 1.00
     */
    private Map<String, Object> values = new LinkedHashMap<String, Object>();

    /**
     * Stores the name of the sensor.
     * 
     * @since 1.00
     */
    private String sensorName;
    
    /**
     * Constructor.
     * 
     * @param sensorName The name of the sensor.
     * 
     * @since 1.00
     */
    public Sensor(String sensorName) {
        super(null);
        this.sensorName = sensorName;
    }
    
    @Override
    public Object getValue() {
        return values;
    }
    
    /**
     * Sets the value for the given key.
     * 
     * @param key The key for the value.
     * @param value The value.
     * 
     * @since 1.00
     */
    public void setValue(String key, Object value) {
        values.put(key, value);
    }
    
    /**
     * Returns the name of the sensor.
     * 
     * @return The name of the sensor.
     * 
     * @since 1.00
     */
    public String getSensorName() {
        return sensorName;
    }
    
}
