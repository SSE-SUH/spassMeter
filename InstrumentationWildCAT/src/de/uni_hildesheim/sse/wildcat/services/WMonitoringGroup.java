package de.uni_hildesheim.sse.wildcat.services;

import javax.management.AttributeNotFoundException;

import de.uni_hildesheim.sse.codeEraser.annotations.Operation;
import de.uni_hildesheim.sse.codeEraser.annotations.Variability;
import de.uni_hildesheim.sse.monitoring.runtime.AnnotationConstants;
import de.uni_hildesheim.sse.monitoring.runtime.plugins.IMonitoringGroup;
import de.uni_hildesheim.sse.serviceConstants.MonitoringGroupConstants;

/**
 * WildCAT service for events from the {@link IMonitoringGroup}s.
 * 
 * @author Stephan Dederichs
 * 
 * @version 1.00
 * @since 1.00
 */
public class WMonitoringGroup extends AbstractWServiceData 
    implements IMonitoringGroup {

    /**
     * Stores the related {@link IMonitoringGroup} to this JMX service.
     * 
     * @since 1.00
     */
    private IMonitoringGroup monitoringGroup;
    
    /**
     * Creates an instance of {@link WMonitoringGroup}.
     * 
     * @param monitoringGroup The related {@link IMonitoringGroup} to this 
     *            wildcat service.
     * 
     * @since 1.00
     */
    public WMonitoringGroup(IMonitoringGroup monitoringGroup) {
        this.monitoringGroup = monitoringGroup;
    }
    
    @Override
    public long getCpuTimeTicks() {
        long result = monitoringGroup.getCpuTimeTicks();
        setSensorValue(MonitoringGroupConstants.CPU_TIME_TICKS, result);
        return result;
    }

    @Override
    @Variability(id = AnnotationConstants.MONITOR_FILE_IO)
    public long getFileIn() {
        long result = monitoringGroup.getFileIn();
        setSensorValue(MonitoringGroupConstants.FILE_IN, result);
        return result;
    }

    @Override
    @Variability(id = AnnotationConstants.MONITOR_FILE_IO)
    public long getFileOut() {
        long result = monitoringGroup.getFileOut();
        setSensorValue(MonitoringGroupConstants.FILE_OUT, result);
        return result;
    }

    @Override
    @Variability(id = {AnnotationConstants.MONITOR_NET_IO, 
            AnnotationConstants.MONITOR_FILE_IO }, op = Operation.AND)
    public long getIoRead() {
        long result = monitoringGroup.getIoRead();
        setSensorValue(MonitoringGroupConstants.IO_READ, result);
        return result;
    }

    @Override
    @Variability(id = {AnnotationConstants.MONITOR_NET_IO, 
            AnnotationConstants.MONITOR_FILE_IO }, op = Operation.AND)
    public long getIoWrite() {
        long result = monitoringGroup.getIoWrite();
        setSensorValue(MonitoringGroupConstants.IO_WRITE, result);
        return result;
    }

    @Override
    @Variability(id = AnnotationConstants.MONITOR_MEMORY_ALLOCATED)
    public long getMemAllocated() {
        long result = monitoringGroup.getMemAllocated();
        setSensorValue(MonitoringGroupConstants.MEM_ALLOCATED, result);
        return result;
    }

    @Override
    @Variability(id = {AnnotationConstants.MONITOR_MEMORY_ALLOCATED, 
            AnnotationConstants.MONITOR_MEMORY_USAGE }, op = Operation.AND)
    public long getMemUse() {
        long result = monitoringGroup.getMemUse();
        setSensorValue(MonitoringGroupConstants.MEM_USE, result);
        return result;
    }

    @Override
    @Variability(id = AnnotationConstants.MONITOR_NET_IO)
    public long getNetIn() {
        long result = monitoringGroup.getNetIn();
        setSensorValue(MonitoringGroupConstants.NET_IN, result);
        return result;
    }

    @Override
    @Variability(id = AnnotationConstants.MONITOR_NET_IO)
    public long getNetOut() {
        long result = monitoringGroup.getNetOut();
        setSensorValue(MonitoringGroupConstants.NET_OUT, result);
        return result;
    }

    @Override
    @Variability(id = AnnotationConstants.MONITOR_TIME_SYSTEM)
    public long getSystemTimeTicks() {
        long result = monitoringGroup.getSystemTimeTicks();
        setSensorValue(MonitoringGroupConstants.SYSTEM_TIME_TICKS, result);
        return result;
    }

    @Override
    public boolean wasRecorded() {
        boolean result = monitoringGroup.wasRecorded();
        setSensorValue(MonitoringGroupConstants.WAS_RECORDED, result);
        return result;
    }
    
    // ------------------------------------------------------------------------
    
    @Override
    public void burstValueUpdate() {
        getCpuTimeTicks();
        getFileIn();
        getFileOut();
        getIoRead();
        getIoWrite();
        getMemAllocated();
        getMemUse();
        getNetIn();
        getNetOut();
        getSystemTimeTicks();
        wasRecorded();
    }

    @Override
    public Object getDataSpecificAttribute(String attributeName)
        throws AttributeNotFoundException {
        if (attributeName.equals(MonitoringGroupConstants.
                CPU_TIME_TICKS)) {
            return getCpuTimeTicks();
        } else if (attributeName.equals(MonitoringGroupConstants.
                FILE_IN)) {
            return getFileIn();
        } else if (attributeName.equals(MonitoringGroupConstants.
                FILE_OUT)) {
            return getFileOut();
        } else if (attributeName.equals(MonitoringGroupConstants.
                IO_READ)) {
            return getIoRead();
        } else if (attributeName.equals(MonitoringGroupConstants.
                IO_WRITE)) {
            return getIoWrite();
        } else if (attributeName.equals(MonitoringGroupConstants.
                MEM_ALLOCATED)) {
            return getMemAllocated();
        } else if (attributeName.equals(MonitoringGroupConstants.
                MEM_USE)) {
            return getMemUse();
        } else if (attributeName.equals(MonitoringGroupConstants.
                NET_IN)) {
            return getNetIn();
        } else if (attributeName.equals(MonitoringGroupConstants.
                NET_OUT)) {
            return getNetOut();
        } else if (attributeName.equals(MonitoringGroupConstants.
                SYSTEM_TIME_TICKS)) {
            return getSystemTimeTicks();
        } else if (attributeName.equals(MonitoringGroupConstants.
                WAS_RECORDED)) {
            return wasRecorded();
        } else {
            throw new AttributeNotFoundException("Invalid attribute: "
                    + attributeName);
        }
    }

}
