package de.uni_hildesheim.sse.wildcat.plugins;

import de.uni_hildesheim.sse.wildcat.launcher.GearsBridgeContextConstants;

/**
 * Stores the attribte of the {@link Context}.
 * 
 * @author Stephan Dederichs
 * 
 * @version 1.00
 * @since 1.00
 */
public class InstrumentationContextConstants {

    /**
     * Stores the domain for the instrumentation data in the {@link Context}.
     * 
     * @since 1.00
     */
    public static final String SPASS_MONITOR_DOMAIN = 
            GearsBridgeContextConstants.CONTEXT_ROOT + "SPASSMeter";
    
    /**
     * Stores the domain for the {@link IMonitoringGroup}s in the 
     * {@link Context}.
     * 
     * @since 1.00
     */
    public static final String SPASS_MONITOR_DOMAIN_MONITORING_GROUP = 
            SPASS_MONITOR_DOMAIN + "/monitoringgroups";
    
    /**
     * Stores the domain for the timers in the 
     * {@link Context}.
     * 
     * @since 1.00
     */
    public static final String SPASS_MONITOR_DOMAIN_TIMER = 
            SPASS_MONITOR_DOMAIN + "/timer";
    
    /**
     * Stores the domain for the values in the 
     * {@link Context}.
     * 
     * @since 1.00
     */
    public static final String SPASS_MONITOR_DOMAIN_VALUE = 
            SPASS_MONITOR_DOMAIN + "/values";
    
    /**
     * Stores the domain for the {@link IMeasurements} in the 
     * {@link Context}.
     * 
     * @since 1.00
     */
    public static final String SPASS_MONITOR_DOMAIN_MEASUREMENTS = 
            SPASS_MONITOR_DOMAIN + "/system";
    
    /**
     * Prevents this class from being created from outside.
     * 
     * @since 1.00
     */
    private InstrumentationContextConstants() {
    }
    
}
