package de.uni_hildesheim.sse.jmx.listeners;

import de.uni_hildesheim.sse.jmx.services.JMXServiceRegistry;
import de.uni_hildesheim.sse.jmx.services.JMXMeasurements;
import de.uni_hildesheim.sse.jmx.services.JMXMonitoringGroup;
import de.uni_hildesheim.sse.monitoring.runtime.plugins.IMeasurements;
import de.uni_hildesheim.sse.monitoring.runtime.plugins.IMonitoringGroup;
import de.uni_hildesheim.sse.monitoring.runtime.plugins.
    MonitoringGroupBurstChangeListener;
import de.uni_hildesheim.sse.monitoring.runtime.plugins.
    MonitoringGroupCreationListener;
import de.uni_hildesheim.sse.serviceConstants.MeasurementsConstants;

/**
 * Monitoring group change listener.
 * 
 * @author Stephan Dederichs
 * 
 * @version 1.00
 * @since 1.00
 */
public class JMXMonitoringGroupCreationListener implements
        MonitoringGroupCreationListener, MonitoringGroupBurstChangeListener {
    
    @Override
    public void configurationCreated(String recId, IMonitoringGroup elt) {
        JMXMonitoringGroup monitoringGroupService = new JMXMonitoringGroup(elt);
        JMXServiceRegistry.registerService(recId, monitoringGroupService);
    }

    @Override
    public void contributionCreated(String recId, String contribution,
            IMonitoringGroup elt) {
        JMXMonitoringGroup monitoringGroupService = new JMXMonitoringGroup(elt);
        JMXServiceRegistry.registerService(recId + "." + contribution, 
            monitoringGroupService);
    }

    @Override
    public void monitoringGroupCreated(String recId, IMonitoringGroup elt) {
    
        JMXMonitoringGroup monitoringGroupService = new 
            JMXMonitoringGroup(elt);
        JMXServiceRegistry.registerService(recId, monitoringGroupService);
        
    }

    @Override
    public void notifyBurstChange(IMeasurements system, IMeasurements jvm) {
        // Checking if MBean system (JMXMeasurements) is available
        JMXMeasurements systemService = (JMXMeasurements) JMXServiceRegistry.
                getService(MeasurementsConstants.NAME_SYSTEM_MEASUREMENT);
        if (null == systemService) {
            JMXMeasurements mMBean = new JMXMeasurements(system);
            JMXServiceRegistry.registerService(MeasurementsConstants.
                    NAME_SYSTEM_MEASUREMENT, mMBean);
        }
        // CHecking if MBean jvm (JMXMeasurements) is available
        JMXMeasurements jvmService = (JMXMeasurements) JMXServiceRegistry.
                getService(MeasurementsConstants.NAME_JVM_MEASUREMENT);
        if (null == jvmService) {
            JMXMeasurements mMBean = new JMXMeasurements(jvm);
            JMXServiceRegistry.registerService(MeasurementsConstants.
                    NAME_JVM_MEASUREMENT, mMBean);
        }
        // update all MBeans
        JMXServiceRegistry.updateServices(this);
    }

}
