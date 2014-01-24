package de.uni_hildesheim.sse.monitoring.runtime.annotations;

import de.uni_hildesheim.sse.codeEraser.annotations.Variability;
import de.uni_hildesheim.sse.monitoring.runtime.AnnotationConstants;

/**
 * Defines the position where to affect instrumented code for 
 * {@link Timer}.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
@Variability(id = AnnotationConstants.MONITOR_TIMERS)
public enum TimerPosition {

    /**
     * Use the default position.
     */
    DEFAULT(false, false),
    
    /**
     * Override the default and force to beginning of a method / constructor.
     */
    BEGINNING(true, false),

    /**
     * Override the default and force to end of a method / constructor.
     */
    END(false, true),
    
    /**
     * Override the default and force to beginning and end of a 
     * method / constructor.
     */
    BOTH(true, true);
    
    /**
     * Stores if this constant marks the beginning.
     */
    private boolean beginning;

    /**
     * Stores if this constant marks the end.
     */
    private boolean end;
    
    /**
     * Creates a new timer position.
     * 
     * @param beginning <code>true</code> if it marks the beginning, 
     *     <code>false</code> else
     * @param end <code>true</code> if it marks the end, 
     *     <code>false</code> else
     * 
     * @since 1.00
     */
    private TimerPosition(boolean beginning, boolean end) {
        this.beginning = beginning;
        this.end = end;
    }

    /**
     * Returns if this constant marks the beginning.
     * 
     * @return <code>true</code> if it marks the beginning, <code>false</code>
     *     else
     */
    public boolean isBeginning() {
        return beginning;
    }

    /**
     * Returns if this constant marks the end.
     * 
     * @return <code>true</code> if it marks the end, <code>false</code>
     *     else
     */
    public boolean isEnd() {
        return end;
    }
}
