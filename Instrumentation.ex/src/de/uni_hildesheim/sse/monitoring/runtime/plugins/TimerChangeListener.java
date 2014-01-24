package de.uni_hildesheim.sse.monitoring.runtime.plugins;

import de.uni_hildesheim.sse.codeEraser.annotations.Variability;
import de.uni_hildesheim.sse.monitoring.runtime.AnnotationConstants;

/**
 * Defines a listener for timer events, i.e. when a user defined timer ends
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
@Variability(id = AnnotationConstants.MONITOR_TIMERS)
public interface TimerChangeListener {

    /**
     * Is called when a user defined timer ends.
     * 
     * @param recId the recorder identification
     * @param value the value of the timer
     * 
     * @since 1.00
     */
    public void timerFinished(String recId, long value);
}
