package de.uni_hildesheim.sse.monitoring.runtime.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import de.uni_hildesheim.sse.codeEraser.annotations.Variability;
import de.uni_hildesheim.sse.monitoring.runtime.AnnotationConstants;

/**
 * Defines the basis for generating application-specific monitoring timer
 * events. Program elements may be flagged with this annotation in order to
 * cause the monitoring mechanism to store the current system time 
 * ({@link TimerState#START}), to suspend the timer temporarily 
 * ({@link TimerState#SUSPEND}) in order to resume it later 
 * ({@link TimerState#RESUME}) or to terminate the timer cycle in order to 
 * notify observers about a completed timing event.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.METHOD, ElementType.CONSTRUCTOR })
@Variability(id = AnnotationConstants.MONITOR_TIMERS)
@InConfiguration
public @interface Timer {

    /**
     * An arbitrary id representing the associated timer. In case of "*" the 
     * id is determined by the innermost {@link ValueContext}.
     * 
     * @since 1.00
     */
    String id();
    
    /**
     * Defines the next state of the timer.
     * 
     * @since 1.00
     */
    TimerState state();

    /**
     * Defines where to affect the instrumented method/constructor. Defaults
     * to {@link TimerPosition#DEFAULT}, i.e. 
     * {@link TimerState#getDefaultPosition()}.
     * 
     * @since 1.00
     */
    TimerPosition affectAt() default TimerPosition.DEFAULT;
    
    /** 
     * Should be <code>false</code> (default) if <code>id</code> is
     * thread save and threads must not be considered explicitly, 
     * <code>false</code> else.
     */
    boolean considerThreads() default false;
    
}
