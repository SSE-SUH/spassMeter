package de.uni_hildesheim.sse.monitoring.runtime.configuration;

/**
 * Denotes the individual global recording types.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public enum RecordingType {

    /**
     * Local synchronous monitoring (direct call style).
     */
    LOCAL_SYNCHRONOUS(true),
    
    /**
     * Local asynchronous monitoring (internal event style).
     */
    LOCAL_ASYNCHRONOUS(false),

    /**
     * Remote TCP-based monitoring (external event style). Implies a probably
     * valid TCP configuration.
     */
    TCP(true); // legacy setting, check
    
    /**
     * Stores whether a synchronized recorder frontend shall be used.
     */
    private boolean synchronizedRecorder;
    
    /**
     * Creates a new recording type enum value.
     * 
     * @param synchronizedRecorder if a synchronized recorder frontend shall
     *   be used, <code>false</code> else
     * 
     * @since 1.00
     */
    private RecordingType(boolean synchronizedRecorder) {
        this.synchronizedRecorder = synchronizedRecorder;
    }
    
    /**
     * Returns whether a synchronized recorder frontend shall be used.
     * 
     * @return <code>true</code> if a synchronized recorder frontend shall
     *   be used, <code>false</code> else
     * 
     * @since 1.00
     */
    public boolean useSynchronizedRecorder() {
        return synchronizedRecorder;
    }
    
}
