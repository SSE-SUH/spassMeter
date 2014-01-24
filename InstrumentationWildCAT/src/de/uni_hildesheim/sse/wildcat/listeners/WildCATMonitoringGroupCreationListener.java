package de.uni_hildesheim.sse.wildcat.listeners;

import de.uni_hildesheim.sse.monitoring.runtime.plugins.IMeasurements;
import de.uni_hildesheim.sse.monitoring.runtime.plugins.IMonitoringGroup;
import de.uni_hildesheim.sse.monitoring.runtime.plugins.
    MonitoringGroupBurstChangeListener;
import de.uni_hildesheim.sse.monitoring.runtime.plugins.
    MonitoringGroupCreationListener;
import de.uni_hildesheim.sse.serviceConstants.MeasurementsConstants;
import de.uni_hildesheim.sse.wildcat.services.WMeasurements;
import de.uni_hildesheim.sse.wildcat.services.WMonitoringGroup;
import de.uni_hildesheim.sse.wildcat.services.WServiceRegistry;

/**
 * Monitoring group change listener.
 * 
 * @author Stephan Dederichs
 * 
 * @version 1.00
 * @since 1.00
 */
public class WildCATMonitoringGroupCreationListener implements
    MonitoringGroupCreationListener, MonitoringGroupBurstChangeListener {

    @Override
    public void configurationCreated(String recId, IMonitoringGroup elt) {
        WMonitoringGroup monitoringGroupService = new WMonitoringGroup(elt);
        WServiceRegistry.registerService(recId, 
            monitoringGroupService);
    }

    @Override
    public void contributionCreated(String recId, String contribution,
            IMonitoringGroup elt) {
        WMonitoringGroup monitoringGroupService = new WMonitoringGroup(elt);
        WServiceRegistry.registerService(recId + "." 
            + contribution, monitoringGroupService);
    }

    @Override
    public void monitoringGroupCreated(String recId, IMonitoringGroup elt) {
        WMonitoringGroup monitoringGroupService = new WMonitoringGroup(elt);
        WServiceRegistry.registerService(recId, 
            monitoringGroupService);
    }

    @Override
    public void notifyBurstChange(IMeasurements system, IMeasurements jvm) {
        // Checking if MBean system (JMXMeasurements) is available
        WMeasurements systemService = (WMeasurements) WServiceRegistry.
                getService(MeasurementsConstants.NAME_SYSTEM_MEASUREMENT);
        if (null == systemService) {
            systemService = new WMeasurements(system);
            WServiceRegistry.registerService(MeasurementsConstants.
                    NAME_SYSTEM_MEASUREMENT, systemService);
        }
        // CHecking if MBean jvm (JMXMeasurements) is available
        WMeasurements jvmService = (WMeasurements) WServiceRegistry.
                getService(MeasurementsConstants.NAME_JVM_MEASUREMENT);
        if (null == jvmService) {
            jvmService = new WMeasurements(jvm);
            WServiceRegistry.registerService(MeasurementsConstants.
                    NAME_JVM_MEASUREMENT, jvmService);
        }
        WServiceRegistry.updateServices(this);
    }

}
