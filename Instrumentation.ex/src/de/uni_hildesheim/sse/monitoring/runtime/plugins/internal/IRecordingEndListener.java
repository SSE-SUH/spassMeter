package de.uni_hildesheim.sse.monitoring.runtime.plugins.internal;

/**
 * Defines a listener which is called when recording ends.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public interface IRecordingEndListener {
    
    /**
     * Is called when recording ends.
     * 
     * @since 1.00
     */
    public void notifyRecordingEnd();

}
