package de.uni_hildesheim.sse.monitoring.runtime.plugins.internal;

/**
 * Allows to cleanup temporary data.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public interface Cleanup {

    /**
     * Cleans up temporary data in memory.
     * 
     * @since 1.00
     */
    public void cleanup();
    
    /**
     * Do memory cleanup if required.
     * 
     * @since 1.00
     */
    public void cleanupIfRequired();
    
}
