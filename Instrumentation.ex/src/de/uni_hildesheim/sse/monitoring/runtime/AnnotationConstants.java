package de.uni_hildesheim.sse.monitoring.runtime;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import de.uni_hildesheim.sse.monitoring.runtime.boot.ArrayList;

/**
 * Defines constants to be used in annotations, particularly for 
 * denoting variabilities to be enabled / disabled at / directly after compile 
 * time.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public class AnnotationConstants {
    
    /**
     * Denotes the TCP recording strategy.
     */
    public static final String STRATEGY_TCP = "strategy:tcp";

    /**
     * Denotes the default recording strategy.
     */
    public static final String STRATEGY_DEFAULT = "strategy:default";

    /**
     * Denotes experimental stuff.
     */
    public static final String EXPERIMENTAL = "experimental";
    
    /**
     * Denotes the default recording strategy.
     */
    public static final String DEBUG = "includingDebug";
    
    /**
     * Denotes calibration specific classes and operations.
     */
    public static final String CALIBRATION = "calibration";
    
    /**
     * Denotes whether file I/O monitoring should be performed.
     */    
    public static final String MONITOR_OVERHEAD = "monitor:overhead";

    /**
     * Denotes whether monitoring of individual values should be performed.
     */    
    public static final String MONITOR_VALUES = "monitor:values";

    /**
     * Denotes whether file I/O monitoring should be performed.
     */    
    public static final String MONITOR_FILE_IO = "monitor:fileIO";

    /**
     * Denotes whether file I/O monitoring should be performed.
     */    
    public static final String MONITOR_NET_IO = "monitor:netIO";

    /**
     * Denotes whether memory allocation should be monitored.
     */    
    public static final String MONITOR_MEMORY_ALLOCATED = "monitor:memAlloc";

    /**
     * Denotes whether (summarized) memory usage (unallocation) should be 
     * monitored.
     */    
    public static final String MONITOR_MEMORY_USAGE = "monitor:memUsage";

    /**
     * Denotes whether (summarized) memory usage (native unallocation) should 
     * be monitored.
     */    
    public static final String MONITOR_MEMORY_USAGE_NATIVE 
        = "monitor:memUsage:native";
    
    /**
     * Denotes whether allocation of system time should be monitored. Either 
     * {@link #MONITOR_TIME_SYSTEM} or {@link #MONITOR_TIME_CPU} must be active!
     */    
    public static final String MONITOR_TIME_SYSTEM = "monitor:sysTime";

    /**
     * Denotes whether allocation of CPU time should be monitored. Either 
     * {@link #MONITOR_TIME_SYSTEM} or {@link #MONITOR_TIME_CPU} must be active!
     */
    public static final String MONITOR_TIME_CPU = "monitor:cpuTime";

    /**
     * Denotes whether timing events should be monitored / generated.
     */
    public static final String MONITOR_TIMERS = "monitor:timer";

    /**
     * Denotes that XML configuration handling should be included.
     */
    public static final String CONFIG_XML = "config:XML";
    
    /**
     * Denotes that OSGi specific functions should be included.
     */
    public static final String INSTRUMENT_OSGI = "instrument:OSGi";
    
    /**
     * Prevents this class from being instantiated from outside.
     * 
     * @since 1.00
     */
    private AnnotationConstants() {
    }
    
    /**
     * Returns the list of all constant values defined in this class.
     * 
     * @return the list of all constant values
     * 
     * @since 1.00
     */
    public static final String[] allValues() {
        ArrayList<String> result = new ArrayList<String>();
        for (Field field : AnnotationConstants.class.getFields()) {
            int modifiers = field.getModifiers();
            if (Modifier.isFinal(modifiers) && Modifier.isStatic(modifiers) 
                && Modifier.isPublic(modifiers)) {
                try {
                    Object val = field.get(null);
                    if (null != val) {
                        result.add(val.toString());
                    }
                } catch (IllegalArgumentException e) {
                } catch (IllegalAccessException e) {
                }
            }
        }
        String[] array = new String[result.size()];
        return result.toArray(array);
    }
    
}
