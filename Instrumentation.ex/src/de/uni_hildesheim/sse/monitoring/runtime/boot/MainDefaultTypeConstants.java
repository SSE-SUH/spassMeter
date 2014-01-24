package de.uni_hildesheim.sse.monitoring.runtime.boot;

/**
 * Defines bit flags for main default instrumentation.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
class MainDefaultTypeConstants {
    
    /**
     * The bit flag for instrumenting at start of main.
     */
    static final int AT_START = 1 << 0;

    /**
     * The bit flag for instrumenting at end of main.
     */
    static final int AT_END = 1 << 1;
    
    /**
     * The bit flag for instrumenting at shutdown of the JVM.
     */
    static final int AT_SHUTDOWN = 1 << 2;
    
    /**
     * Prevents this class from being instantiated from outside.
     * 
     * @since 1.00
     */
    private MainDefaultTypeConstants() {
    }
 
}
