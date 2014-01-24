package de.uni_hildesheim.sse.monitoring.runtime.annotations;

import de.uni_hildesheim.sse.codeEraser.annotations.Variability;
import de.uni_hildesheim.sse.monitoring.runtime.AnnotationConstants;

/**
 * Defines some timer states to be used with {@link Timer}. The timer 
 * mechanism follows a simple state machine of start-finish or 
 * start-suspend-resume-finish notifications (further unmatching events are 
 * ignored).
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
@Variability(id = AnnotationConstants.MONITOR_TIMERS)
public enum TimerState {

    /**
     * Start the time recording at the given point of execution 
     * ({@link TimerPosition#BEGINNING}).
     */
    START(TimerPosition.BEGINNING),
    
    /**
     * Suspend the time recording temporarily in order to 
     * {@link #RESUME resume} it later ({@link TimerPosition#END}).
     */
    SUSPEND(TimerPosition.END),
    
    /**
     * Resume time recording after a previous {@link #SUSPEND} 
     * ({@link TimerPosition#BEGINNING}).
     */
    RESUME(TimerPosition.BEGINNING),
    
    /**
     * Finish the timer, notify observers and clear the timer storage 
     * ({@link TimerPosition#END}).
     */
    FINISH(TimerPosition.END),
    
    /**
     * Start and finish the timer in one method / constructor 
     * ({@link TimerPosition#BOTH}). 
     */
    START_FINISH(TimerPosition.BOTH),
    
    /**
     * Resume and stop in one method / constructor 
     * ({@link TimerPosition#BOTH}). 
     */
    RESUME_SUSPEND(TimerPosition.BOTH),

    /**
     * Suspend and resume in one method / constructor 
     * ({@link TimerPosition#BOTH}). 
     */
    SUSPEND_RESUME(TimerPosition.BOTH);
    
    /**
     * Stores the default position for affecting the instrumented system.
     */
    private TimerPosition defaultPosition;
    
    /**
     * Creates a new timer state with default position.
     * 
     * @param defaultPosition the default position
     * 
     * @since 1.00
     */
    private TimerState(TimerPosition defaultPosition) {
        assert null != defaultPosition;
        this.defaultPosition = defaultPosition;
    }

    /**
     * Returns the default position for affecting instrumented code.
     * 
     * @return the default position (must not be <b>null</b>)
     * 
     * @since 1.00
     */
    public TimerPosition getDefaultPosition() {
        return defaultPosition;
    }
}
