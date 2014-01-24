package de.uni_hildesheim.sse.serviceConstants;

/**
 * Stores the attribute names of the {@link IBatteryDataGatherer}.
 * 
 * @author Stephan Dederichs
 * 
 * @version 1.00
 * @since 1.00
 */
public class BatteryDataConstants {

    /**
     * HasSystemBattery attribute.
     * 
     * @since 1.00
     */
    public static final String HAS_SYSTEM_BATTERY = "HasSystemBattery";
    
    /**
     * BatteryLifePercent attribute.
     * 
     * @since 1.00
     */
    public static final String BATTERY_LIFE_PERCENT = "BatteryLifePercent";
    
    /**
     * BatteryLifeTime attribute.
     * 
     * @since 1.00
     */
    public static final String BATTERY_LIFE_TIME = "BatteryLifeTime";
    
    /**
     * PowerPlugStatus attribute.
     * 
     * @since 1.00
     */
    public static final String POWER_PLUG_STATUS = "PowerPlugStatus";
    
    /**
     * Prevents this class from being created from outside.
     * 
     * @since 1.00
     */
    private BatteryDataConstants() {
        
    }
    
}
