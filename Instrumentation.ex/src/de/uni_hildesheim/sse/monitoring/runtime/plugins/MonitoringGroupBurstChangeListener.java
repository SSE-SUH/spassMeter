package de.uni_hildesheim.sse.monitoring.runtime.plugins;

/**
 * Defines a listener for burst notifications on monitoring group changes. 
 * Using this listener is much more comfortable (and performant) than using 
 * {@link MonitoringGroupChangeListener} which works on a per-change basis
 * while this is called based on regular events according to the out interval
 * in the configuration of SPASS-meter.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public interface MonitoringGroupBurstChangeListener {

    /**
     * Is called on a regular basis to notify the listener about various 
     * changes in previously registered data. This method provides information
     * 
     * @param system the current system measurements (singleton instance)
     * @param jvm the current JVM process measurements (singleton instance)
     * 
     * @since 1.00
     */
    public void notifyBurstChange(IMeasurements system, IMeasurements jvm);
    
}
