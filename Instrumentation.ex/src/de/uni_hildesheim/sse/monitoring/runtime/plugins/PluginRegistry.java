package de.uni_hildesheim.sse.monitoring.runtime.plugins;

import de.uni_hildesheim.sse.codeEraser.annotations.Variability;
import de.uni_hildesheim.sse.monitoring.runtime.AnnotationConstants;

/**
 * Central access to all plugins.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public class PluginRegistry {

    /**
     * Stores the value change listener (initialized with a default listener).
     */
    @Variability(id = AnnotationConstants.MONITOR_VALUES)
    private static ValueChangeListener valueChangeListener 
        = new DefaultValueChangeListener();

    /**
     * Stores the timer listener (initialized with a default listener).
     */
    @Variability(id = AnnotationConstants.MONITOR_TIMERS)
    private static TimerChangeListener timerChangeListener
        = new DefaultTimerChangeListener();

    /**
     * Stores the monitoring group change listener.
     */
    private static MonitoringGroupChangeListener 
        monitoringGroupChangeListener = null;

    /**
     * Stores the monitoring group change listener.
     */
    private static MonitoringGroupBurstChangeListener 
        monitoringGroupBurstChangeListener = null;
    
    /**
     * Stores the monitoring group change listener.
     */
    private static MonitoringGroupCreationListener 
        monitoringGroupCreationListener = null;
    
    
    /**
     * Prevents this class from being created from outside.
     */
    private PluginRegistry() {
    }
    
    /**
     * Attaches a value change listener.
     *
     * @param listener the new value change listener
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.MONITOR_VALUES)
    public static final void attachValueChangeListener(
        ValueChangeListener listener) {
        valueChangeListener = listener;
    }

    /**
     * Returns the current value change listener.
     *
     * @return the current value change listener
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.MONITOR_VALUES)
    public static final ValueChangeListener getValueChangeListener() {
        return valueChangeListener;
    }

    /**
     * Attaches a value change listener.
     *
     * @param listener the new value change listener
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.MONITOR_TIMERS)
    public static final void attachTimerChangeListener(
        TimerChangeListener listener) {
        timerChangeListener = listener;
    }

    /**
     * Returns the current value change listener.
     *
     * @return the current value change listener
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.MONITOR_TIMERS)
    public static final TimerChangeListener getTimerChangeListener() {
        return timerChangeListener;
    }

    /**
     * Attaches a monitoring group change listener.
     *
     * @param listener the new monitoring group change listener
     * 
     * @since 1.00
     */
    public static final void attachMonitoringGroupChangeListener(
        MonitoringGroupChangeListener listener) {
        monitoringGroupChangeListener = listener;
    }

    /**
     * Attaches a monitoring group burst change listener.
     *
     * @param listener the new monitoring group burst change listener
     * 
     * @since 1.00
     */
    public static final void attachMonitoringGroupBurstChangeListener(
        MonitoringGroupBurstChangeListener listener) {
        monitoringGroupBurstChangeListener = listener;
    }

    /**
     * Attaches a monitoring group creation listener.
     *
     * @param listener the new monitoring group creation listener
     * 
     * @since 1.00
     */
    public static final void attachMonitoringGroupCreationListener(
        MonitoringGroupCreationListener listener) {
        monitoringGroupCreationListener = listener;
    }

    
    /**
     * Returns the current monitoring group change listener.
     *
     * @return the current monitoring group change listener
     * 
     * @since 1.00
     */
    public static final MonitoringGroupChangeListener 
        getMonitoringGroupChangeListener() {
        return monitoringGroupChangeListener;
    }

    /**
     * Returns the current monitoring group change listener.
     *
     * @return the current monitoring group change listener
     * 
     * @since 1.00
     */
    public static final MonitoringGroupBurstChangeListener 
        getMonitoringGroupBurstChangeListener() {
        return monitoringGroupBurstChangeListener;
    }

    
    /**
     * Returns the current monitoring group creation listener.
     *
     * @return the current monitoring group creation listener
     * 
     * @since 1.00
     */
    public static final MonitoringGroupCreationListener 
        getMonitoringGroupCreationListener() {
        return monitoringGroupCreationListener;
    }

}
