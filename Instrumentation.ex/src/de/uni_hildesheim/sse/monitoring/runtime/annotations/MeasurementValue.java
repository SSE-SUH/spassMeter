package de.uni_hildesheim.sse.monitoring.runtime.annotations;

import de.uni_hildesheim.sse.codeEraser.annotations.Variability;
import de.uni_hildesheim.sse.monitoring.runtime.AnnotationConstants;

/**
 * Defines monitoring values to be influenced by {@link NotifyValue}.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public enum MeasurementValue {
    
    /**
     * Assign all values to the specified record.
     */
    ALL,
    
    /**
     * Treats the probed value as a value change.
     */
    @Variability(id = AnnotationConstants.MONITOR_VALUES)
    VALUE,
    
    /**
     * The value to be notified should be considered as memory allocation.
     */
    MEM_ALLOCATED,

    /**
     * The value to be notified should be considered as memory unallocation.
     */
    MEM_UNALLOCATED,

    /**
     * The value to be notified should be considered as bytes read from network.
     */
    NET_IN,

    /**
     * The value to be notified should be considered as bytes written to 
     * network.
     */
    NET_OUT,

    /**
     * The value to be notified should be considered as bytes read from file.
     */
    FILE_IN,

    /**
     * The value to be notified should be considered as bytes written to 
     * file.
     */
    FILE_OUT;
    
}
