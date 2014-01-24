package de.uni_hildesheim.sse.monitoring.runtime.instrumentation;

/**
 * Logger methods.
 * 
 * @author Holger Eichelberger
 */
public class Logger {
    
    /**
     * Provides access to the default logger instance.
     */
    public static final java.util.logging.Logger LOG 
        = java.util.logging.Logger.getLogger("SPASS-meter");

    /**
     * Prevents external access.
     * 
     * @since 1.00
     */
    private Logger() {
    }
    
}
