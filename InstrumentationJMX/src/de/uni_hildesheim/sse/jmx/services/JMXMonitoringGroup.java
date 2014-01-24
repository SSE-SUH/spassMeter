package de.uni_hildesheim.sse.jmx.services;

import java.util.LinkedList;
import java.util.List;

import javax.management.Attribute;
import javax.management.AttributeNotFoundException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanOperationInfo;
import javax.management.ReflectionException;

import de.uni_hildesheim.sse.codeEraser.annotations.Operation;
import de.uni_hildesheim.sse.codeEraser.annotations.Variability;
import de.uni_hildesheim.sse.jmx.services.dynamic.AbstractJMXServiceData;
import de.uni_hildesheim.sse.monitoring.runtime.AnnotationConstants;
import de.uni_hildesheim.sse.monitoring.runtime.plugins.IMonitoringGroup;
import de.uni_hildesheim.sse.serviceConstants.MonitoringGroupConstants;

/**
 * JMX service for a {@link IMonitoringGroup}.
 * 
 * @author Stephan Dederichs
 * 
 * @version 1.00
 * @since 1.00
 */
public class JMXMonitoringGroup extends AbstractJMXServiceData implements 
    IMonitoringGroup {
    
    /**
     * Stores the related {@link IMonitoringGroup} to this JMX service.
     * 
     * @since 1.00
     */
    private IMonitoringGroup monitoringGroup;
    
    /**
     * Creates an instance of {@link JMXMonitoringGroup}.
     * 
     * @param monitoringGroup The related {@link IMonitoringGroup} to this 
     *            JMX service.
     * 
     * @since 1.00
     */
    public JMXMonitoringGroup(IMonitoringGroup monitoringGroup) {
        this.monitoringGroup = monitoringGroup;
    }

    @Override
    public long getCpuTimeTicks() {
        return monitoringGroup.getCpuTimeTicks();
    }

    @Override
    @Variability(id = AnnotationConstants.MONITOR_FILE_IO)
    public long getFileIn() {
        return monitoringGroup.getFileIn();
    }

    @Override
    @Variability(id = AnnotationConstants.MONITOR_FILE_IO)
    public long getFileOut() {
        return monitoringGroup.getFileOut();
    }

    @Override
    @Variability(id = {AnnotationConstants.MONITOR_NET_IO, 
            AnnotationConstants.MONITOR_FILE_IO }, op = Operation.AND)
    public long getIoRead() {
        return monitoringGroup.getIoRead();
    }

    @Override
    @Variability(id = {AnnotationConstants.MONITOR_NET_IO, 
            AnnotationConstants.MONITOR_FILE_IO }, op = Operation.AND)
    public long getIoWrite() {
        return monitoringGroup.getIoWrite();
    }

    @Override
    @Variability(id = AnnotationConstants.MONITOR_MEMORY_ALLOCATED)
    public long getMemAllocated() {
        return monitoringGroup.getMemAllocated();
    }

    @Override
    @Variability(id = {AnnotationConstants.MONITOR_MEMORY_ALLOCATED, 
            AnnotationConstants.MONITOR_MEMORY_USAGE }, op = Operation.AND)
    public long getMemUse() {
        return monitoringGroup.getMemUse();
    }

    @Override
    @Variability(id = AnnotationConstants.MONITOR_NET_IO)
    public long getNetIn() {
        return monitoringGroup.getNetIn();
    }

    @Override
    @Variability(id = AnnotationConstants.MONITOR_NET_IO)
    public long getNetOut() {
        return monitoringGroup.getNetOut();
    }

    @Override
    @Variability(id = AnnotationConstants.MONITOR_TIME_SYSTEM)
    public long getSystemTimeTicks() {
        return monitoringGroup.getSystemTimeTicks();
    }

    @Override
    public boolean wasRecorded() {
        return monitoringGroup.wasRecorded();
    }
    
    // ------------------Dynamic MBean Methods------------------

    @Override
    public String[] defineMBeanInfo() {
        String[] mBeanInfo = new String[2];
        mBeanInfo[0] = "MonitoringGroup";
        mBeanInfo[1] = "Dynamic MBean for a monitoring group.";
        return mBeanInfo;
    }

    @Override
    public List<MBeanAttributeInfo> defineAttributes() {
        // Stores the attributes
        List<MBeanAttributeInfo> mBeanAttrInfoList = new 
                LinkedList<MBeanAttributeInfo>();
        
        // CpuTimeTicks
        mBeanAttrInfoList.add(new MBeanAttributeInfo(MonitoringGroupConstants.
            CPU_TIME_TICKS, "long",
            "Returns the entire number of CPU time ticks recorded for "
                + "this instance.",
            READABLE, WRITEABLE, ISIS));
        // FileIn
        mBeanAttrInfoList.add(new MBeanAttributeInfo(MonitoringGroupConstants.
            FILE_IN, "long",
            "Returns the number of bytes read from files.",
            READABLE, WRITEABLE, ISIS));
        // FileOut
        mBeanAttrInfoList.add(new MBeanAttributeInfo(MonitoringGroupConstants.
            FILE_OUT, "long",
            "Returns the number of bytes written to files.",
            READABLE, WRITEABLE, ISIS));
        // IoRead
        mBeanAttrInfoList.add(new MBeanAttributeInfo(MonitoringGroupConstants.
            IO_READ, "long",
            "Returns the number of bytes read from files or network.",
            READABLE, WRITEABLE, ISIS));
        // IoWrite
        mBeanAttrInfoList.add(new MBeanAttributeInfo(MonitoringGroupConstants.
            IO_WRITE, "long",
            "Returns the number of bytes written to files or network.",
            READABLE, WRITEABLE, ISIS));
        // MemAllocated
        mBeanAttrInfoList.add(new MBeanAttributeInfo(MonitoringGroupConstants.
            MEM_ALLOCATED, "long",
            "Returns the amount of memory allocated for this instance.",
            READABLE, WRITEABLE, ISIS));
        // MemUse
        mBeanAttrInfoList.add(new MBeanAttributeInfo(MonitoringGroupConstants.
            MEM_USE, "long",
            "Returns the amount of memory currently being used, i.e. "
                + "difference between allocated and unallocated memory.",
            READABLE, WRITEABLE, ISIS));
        // NetIn
        mBeanAttrInfoList.add(new MBeanAttributeInfo(MonitoringGroupConstants.
            NET_IN, "long",
            "Returns the number of bytes read from network.",
            READABLE, WRITEABLE, ISIS));
        // NetOut
        mBeanAttrInfoList.add(new MBeanAttributeInfo(MonitoringGroupConstants.
            NET_OUT, "long",
            "Returns the number of bytes written to network.",
            READABLE, WRITEABLE, ISIS));
        // SystemTimeTicks
        mBeanAttrInfoList.add(new MBeanAttributeInfo(MonitoringGroupConstants.
            SYSTEM_TIME_TICKS, "long",
            "Returns the entire number of system time ticks recorded for "
                + "this instance.",
            READABLE, WRITEABLE, ISIS));
        // wasRecorded
        mBeanAttrInfoList.add(new MBeanAttributeInfo(MonitoringGroupConstants.
            WAS_RECORDED, "java.lang.Boolean",
            "Returns if data on this instance was recorded or if it a "
                + "dummy instance created e.g. for some unused (automatic "
                + "detected) variants.",
            READABLE, WRITEABLE, ISIS));
            
        return mBeanAttrInfoList;
    }

    @Override
    public MBeanConstructorInfo[] defineConstructors() {
        // creating an array for the MBeanConstructorInfo
        MBeanConstructorInfo[] mBeanConstrInfo = new MBeanConstructorInfo[1];
        // empty constructor
        mBeanConstrInfo[0] = new MBeanConstructorInfo("MonitoringGroup",
                "Public default constructor ...", null);
    
        return mBeanConstrInfo;
    }

    @Override
    public MBeanOperationInfo[] defineOperations() {
        MBeanOperationInfo[] mBeanOperInfo = new MBeanOperationInfo[0];
        return mBeanOperInfo;
    }

    @Override
    public Object checkAttributeName(String attributeName)
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
    
    @Override
    public List<MBeanAttributeInfo> defineSpecificAttributes() {
        return null;
    }

    @Override
    public Object invokeSpecificFunction(String operationName, Object[] params,
            String[] signature) throws ReflectionException {
        return null;
    }

    @Override
    public void setSpecificAttributes(Attribute arg0)
        throws AttributeNotFoundException {
    }
    
}
