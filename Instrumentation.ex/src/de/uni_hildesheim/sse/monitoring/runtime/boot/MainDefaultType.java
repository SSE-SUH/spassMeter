package de.uni_hildesheim.sse.monitoring.runtime.boot;

import static de.uni_hildesheim.sse.monitoring.runtime.boot.
    MainDefaultTypeConstants.*;

/**
 * Defines the default instrumentation behavior in case that no annotations
 * or configuration is given for the start and end of monitoring activities.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public enum MainDefaultType {
    
    /**
     * Do nothing, rely on annotations only.
     */
    NONE(0),
    
    /**
     * Instrument startup of monitoring at beginning of main by default.
     */
    START(AT_START),

    /**
     * Instrument end of monitoring at end of main by default.
     */
    END(AT_END),

    /**
     * Instrument end of monitoring by shutdown hook.
     */
    SHUTDOWN(AT_SHUTDOWN),

    /**
     * Combines {@link #START} and {@link #END}.
     */
    START_END(AT_START | AT_END),
    
    /**
     * Combines {@link #START} and {@link #END}.
     * 
     * @deprecated use {@link #START_END} instead
     */
    START_STOP(AT_START | AT_END),

    /**
     * Combines {@link #START} and {@link #SHUTDOWN}.
     */
    START_SHUTDOWN(AT_START | AT_SHUTDOWN),

    /**
     * Combines {@link #START} and {@link #END} and {@link #SHUTDOWN}.
     */
    START_END_SHUTDOWN(AT_START | AT_END | AT_SHUTDOWN);

    /**
     * Stores the flags from {@link MainDefaultTypeConstants}.
     */
    private int flags;
    
    /**
     * Creates a new constant with given flags.
     * 
     * @param flags the flags to be set ({@link MainDefaultTypeConstants})
     * 
     * @since 1.00
     */
    private MainDefaultType(int flags) {
        this.flags = flags;
    }
    
    /**
     * Returns whether default instrumentation should happen at the beginning.
     * 
     * @return <code>true</code> if it should happen at the beginning, 
     *   <code>false</code> else
     * 
     * @since 1.00
     */
    public boolean atStart() {
        return Flags.isSet(flags, AT_START);
    }

    /**
     * Returns whether default instrumentation should happen at the end.
     * 
     * @return <code>true</code> if it should happen at the end, 
     *   <code>false</code> else
     * 
     * @since 1.00
     */
    public boolean atStop() {
        return Flags.isSet(flags, AT_END);
    }

    /**
     * Returns whether default instrumentation should happen as a shutdown hook.
     * 
     * @return <code>true</code> if it should happen as a shutdown hook, 
     *   <code>false</code> else
     * 
     * @since 1.00
     */
    public boolean atShutdown() {
        return Flags.isSet(flags, AT_SHUTDOWN);
    }

}
