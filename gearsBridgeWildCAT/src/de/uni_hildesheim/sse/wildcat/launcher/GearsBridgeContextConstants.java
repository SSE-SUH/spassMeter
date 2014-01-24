package de.uni_hildesheim.sse.wildcat.launcher;

/**
 * Stores the attributes of the {@link Context}.
 * 
 * @author Stephan Dederichs
 * 
 * @version 1.00
 * @since 1.00
 */
public class GearsBridgeContextConstants {

    /**
     * Stores the name of the {@link Context}.
     * 
     * @since 1.00
     */
    public static final String CONTEXT_NAME = "Measurement";
    
    /**
     * Stores the root name of the {@link Context}.
     * 
     * @since 1.00
     */
    public static final String CONTEXT_ROOT = "self://";
    
    /**
     * Stores the domain for the gearsBridge system data in the {@link Context}.
     * 
     * @since 1.00
     */
    public static final String GEARSBRIDGE_DATA_DOMAIN = 
            CONTEXT_ROOT + "GearsBridge/system";
    
    /**
     * Stores the domain for the WildCAT build in sensor data in the 
     * {@link Context}.
     * 
     * @since 1.00
     */
    public static final String WILDCAT_DATA_DOMAIN = 
            CONTEXT_ROOT + "WildCAT";
    
    /**
     * Prevents this class from being created from outside.
     * 
     * @since 1.00
     */
    private GearsBridgeContextConstants() {
        
    }
    
}
