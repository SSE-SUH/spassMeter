package de.uni_hildesheim.sse.monitoring.runtime.plugins;

/**
 * Implements a default listener for timer events which prints out that a 
 * timer event happened.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public class DefaultTimerChangeListener implements TimerChangeListener {

    /**
     * Is called when a user defined timer ends.
     * 
     * @param recId the recorder identification
     * @param value the value of the timer
     * 
     * @since 1.00
     */
    @Override
    public void timerFinished(String recId, long value) {
        System.out.println("TIMER EVENT: " 
            + recId + " " + value);
    }

}
