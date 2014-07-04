package de.uni_hildesheim.sse.monitoring.runtime.plugins.internal;

import de.uni_hildesheim.sse.monitoring.runtime.configuration
    .IRecordingEndListener;

/**
 * Stores the central cleanup instance. Needed to decouple the recorder from
 * instrumentation. This class 
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public class InternalPluginRegistry {

    /**
     * Stores the recording end listener.
     */
    private static IRecordingEndListener recordingEndListener = null;

    /**
     * Stores the instance being responsible for internal instrumenter
     * cleanups.
     */
    private static Cleanup instrumenterCleanup = null;
    
    /**
     * Prevents this instance from being instantiated from outside.
     * 
     * @since 1.00
     */
    private InternalPluginRegistry() {
    }
    
    /**
     * Attaches a new recording end listener (can be set only once due to the 
     * character of an internal extension).
     *
     * @param listener the new recording end listener
     * 
     * @since 1.00
     */
    public static final void attachRecordingEndListener(
        IRecordingEndListener listener) {
        if (null == recordingEndListener) {
            recordingEndListener = listener;
        } else {
            System.out.println("Recording end listeneryet defined. Ignored.");
        }
    }

    /**
     * Returns the current recording end listener.
     *
     * @return the current recording end listener
     * 
     * @since 1.00
     */
    public static final IRecordingEndListener getRecordingEndListener() {
        return recordingEndListener;
    }
    
    /**
     * Attaches the internal instrumenter cleanup.
     * 
     * @param cleanup the cleanup instance
     * 
     * @since 1.00
     */
    public static final void attachInstrumenterCleanup(Cleanup cleanup) {
        instrumenterCleanup = cleanup;
    }
    
    /**
     * Returns the internal instrumenter cleanup instance.
     * 
     * @return the instance (may be <b>null</b>)
     * 
     * @since 1.00
     */
    public static final Cleanup getInstrumenterCleanup() {
        return instrumenterCleanup;
    }

}
