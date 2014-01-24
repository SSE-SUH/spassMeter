package de.uni_hildesheim.sse.monitoring.runtime.recordingStrategies;

import de.uni_hildesheim.sse.monitoring.runtime.annotations.TimerState;

/**
 * Stores information about a (user-defined) timer. This class represents a
 * simple state machine for handling internal timer events caused by 
 * instrumenting code according to the NotifyTimer annotation.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public class TimerInfo {

    /**
     * Stores the start time of the timer (in milliseconds).
     */
    private long start;
    
    /**
     * Stores the (incrementally aggreagted) value of the timer.
     */
    private long value;
    
    /**
     * Stores the current state of the timer.
     */
    private TimerState state;
    
    /**
     * Creates a new timer info and initializes it as if the timer was
     * started at the given point of time.
     * 
     * @param start the start point of time (in milliseconds)
     * 
     * @since 1.00
     */
    public TimerInfo(long start) {
        // corresponds to initial START
        start(start);
    }
    
    /**
     * Initializes the timer as if it was started at the given point of time.
     * 
     * @param start the start point of time (in milliseconds)
     * 
     * @since 1.00
     */
    private void start(long start) {
        this.state = TimerState.START;
        this.start = start;
        this.value = 0;        
    }
    
    /**
     * Returns the current (incrementally aggregated) value. This value 
     * represents the sum of all stop-resume events or (if no stop occurred)
     * the entire timer period. 
     * 
     * @return the current value of the timer, <code>0</code> if not finished
     *     and not stopped so far
     * 
     * @since 1.00
     */
    public long getValue() {
        return value;
    }
    
    /**
     * Handles a state change to the given <code>state</code> at the given
     * point of time <code>now</code>.
     * 
     * @param state the new state
     * @param now the current time in milliseconds
     * @return <code>true</code> if the timer is finished and observers should
     *    be notified, <code>false</code> else
     * 
     * @since 1.00
     */
    public boolean handleState(TimerState state, long now) {
        boolean notify = false;
        switch (state) {
        case START:
            if (TimerState.FINISH == this.state) {
                // if finished and should be started, start it (see constructor)
                start(now);
            }
            break;
        case SUSPEND:
            if (isActive()) {
                // if is active and should stop, store value and reset start 
                // time
                this.value += now - start;
                this.start = 0;
                this.state = state;
            }
            break;
        case FINISH:
            if (isStopped()) {
                // if stopped and should finish, then just take the current 
                // value
                this.state = state;
                notify = true;
            }
            if (isActive()) {
                // if active and should finish, store value and reset start time
                this.value += now - start;
                this.start = 0;
                this.state = state;
                notify = true;
            }
            break;
        case RESUME:
            if (isStopped()) {
                // if stopped and resumed before just set the new start time
                this.start = now;
                this.state = state;
            }
            break;
        default:
            // should not happen as the other states are not valid here
            assert false;
            break;
        }
        return notify;
    }
    
    /**
     * Returns if the timer is active (started or resumed).
     * 
     * @return <code>true</code> if the timer is active, <code>false</code>
     *     if the timer is in another state
     * 
     * @since 1.00
     */
    public final boolean isActive() {
        return TimerState.START == this.state 
            || TimerState.RESUME == this.state;
    }

    /**
     * Returns if the timer is stopped.
     * 
     * @return <code>true</code> if the timer is stopped, <code>false</code>
     *     if the timer is in another state
     * 
     * @since 1.00
     */
    public final boolean isStopped() {
        return TimerState.SUSPEND == this.state;
    }
    
}
