package de.uni_hildesheim.sse.monitoring.runtime.boot;

/**
 * A "trinary" boolean value, indicating that in the {@link #DEFAULT} case 
 * the concrete value should be taken from the global configuration.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public enum BooleanValue {

    /**
     * Unknown, take the value from the global configuration.
     */
    DEFAULT,
    
    /**
     * True.
     */
    TRUE,
    
    /**
     * False.
     */
    FALSE;
}
