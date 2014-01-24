package de.uni_hildesheim.sse.monitoring.runtime.plugins;

/**
 * Defines a listener for notifications on monitoring group changes. As each
 * modification is notified via this listener, this may be rather inefficient
 * so be careful using this listener! This listener does not receive any 
 * recording ids. If you are interested in this information, please register
 * a {@link MonitoringGroupCreationListener} and store the supplied information.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public interface MonitoringGroupChangeListener {
    
    /**
     * Is called when values in a monitoring group are changed.
     * 
     * @param elt the related information
     * 
     * @since 1.00
     */
    public void monitoringGroupChanged(IMonitoringGroup elt);

    /**
     * Is called when a configuration of monitoring groups is changed. In case 
     * that contributions are configured in the SPASS-meter configuration, this 
     * implies that subsequent contributions are also changed.
     * 
     * @param elt the related information
     * 
     * @since 1.00
     */
    public void configurationChanged(IMonitoringGroup elt);

    /**
     * Provides updated statistical information on the operating system and the 
     * JVM process. This information should be used for providing relative 
     * information on the monitoring groups.
     * 
     * @param system the current system measurements (singleton instance)
     * @param jvm the current JVM process measurements (singleton instance)
     * 
     * @since 1.00
     */
    public void measurementsChanged(IMeasurements system, IMeasurements jvm);

}
