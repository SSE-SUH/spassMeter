package de.uni_hildesheim.sse.jmx.services;

import java.util.LinkedList;
import java.util.List;

import javax.management.Attribute;
import javax.management.AttributeNotFoundException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanOperationInfo;
import javax.management.ReflectionException;

import de.uni_hildesheim.sse.jmx.services.dynamic.AbstractJMXServiceData;
import de.uni_hildesheim.sse.monitoring.runtime.plugins.IMeasurements;
import de.uni_hildesheim.sse.serviceConstants.MeasurementsConstants;

/**
 * JMX service for events from the {@link JMXMonitoringGroupCreationListener#
 * notifyBurstChange(IMeasurements, IMeasurements)}.
 * 
 * @author Stephan Dederichs
 * 
 * @version 1.00
 * @since 1.00
 */
public class JMXMeasurements extends AbstractJMXServiceData 
    implements IMeasurements {

    /**
     * Stores a {@link IMeasurements}.
     * 
     * @since 1.00
     */
    private IMeasurements measurement;
    
    /**
     * Creates an instance of {@link JMXMeasurements}.
     * 
     * @param measurement The {@link IMeasurements}.
     * 
     * @since 1.00
     */
    public JMXMeasurements(IMeasurements measurement) {
        this.measurement = measurement;
    }
    
    @Override
    public double getAvgLoad() {
        return measurement.getAvgLoad();
    }

    @Override
    public double getAvgMemUse() {
        return measurement.getAvgMemUse();
    }

    @Override
    public long getIoRead() {
        return measurement.getIoRead();
    }

    @Override
    public long getIoWrite() {
        return measurement.getIoWrite();
    }

    @Override
    public double getMaxLoad() {
        return measurement.getMaxLoad();
    }

    @Override
    public long getMaxMemUse() {
        return measurement.getMinMemUse();
    }

    @Override
    public double getMinLoad() {
        return measurement.getMinLoad();
    }

    @Override
    public long getMinMemUse() {
        return measurement.getMinMemUse();
    }

    @Override
    public int getStatus() {
        return measurement.getStatus();
    }

    @Override
    public long getSystemTime() {
        return measurement.getSystemTime();
    }
    
    // ------------------Dynamic MBean Methods------------------
    
    @Override
    public String[] defineMBeanInfo() {
        String[] mBeanInfo = new String[2];
        mBeanInfo[0] = "IMeasurments";
        mBeanInfo[1] = "Dynamic MBean for IMeasurments.";
        return mBeanInfo;
    }

    @Override
    public List<MBeanAttributeInfo> defineAttributes() {
        // Stores the attributes
        List<MBeanAttributeInfo> mBeanAttrInfoList = new 
                LinkedList<MBeanAttributeInfo>();
        
        // AVG_LOAD
        mBeanAttrInfoList.add(new MBeanAttributeInfo(
            MeasurementsConstants.AVG_LOAD, "double",
            "Returns the average load.",
            READABLE, WRITEABLE, ISIS));
        // AVG_MEM_USE
        mBeanAttrInfoList.add(new MBeanAttributeInfo(
            MeasurementsConstants.AVG_MEM_USE, "double",
            "Returns the average memory usage.",
            READABLE, WRITEABLE, ISIS));
        // IO_READ
        mBeanAttrInfoList.add(new MBeanAttributeInfo(
            MeasurementsConstants.IO_READ, "long",
            "Returns the number of bytes read from external input.",
            READABLE, WRITEABLE, ISIS));
        // IO_WRITE
        mBeanAttrInfoList.add(new MBeanAttributeInfo(
            MeasurementsConstants.IO_WRITE, "long",
            "Returns the number of bytes written to external input.",
            READABLE, WRITEABLE, ISIS));
        // MAX_LOAD
        mBeanAttrInfoList.add(new MBeanAttributeInfo(
            MeasurementsConstants.MAX_LOAD, "double",
            "Returns the maximum load.",
            READABLE, WRITEABLE, ISIS));
        // MAX_MEM_USE
        mBeanAttrInfoList.add(new MBeanAttributeInfo(
            MeasurementsConstants.MAX_MEM_USE, "long",
            "Returns the maximum memory usage.",
            READABLE, WRITEABLE, ISIS));
        // MIN_LOAD
        mBeanAttrInfoList.add(new MBeanAttributeInfo(
            MeasurementsConstants.MIN_LOAD, "double",
            "Returns the minimum load.",
            READABLE, WRITEABLE, ISIS));
        // MIN_MEM_USE
        mBeanAttrInfoList.add(new MBeanAttributeInfo(
            MeasurementsConstants.MIN_MEM_USE, "long",
            "Returns the minimum memory usage.",
            READABLE, WRITEABLE, ISIS));
        // STATUS
        mBeanAttrInfoList.add(new MBeanAttributeInfo(
            MeasurementsConstants.STATUS, "int",
            "Returns the status flags as combination of {@link #STATUS_FILE}, "
                + "{@link #STATUS_NET}.",
            READABLE, WRITEABLE, ISIS));
        // SYSTEM_TIME
        mBeanAttrInfoList.add(new MBeanAttributeInfo(
            MeasurementsConstants.SYSTEM_TIME, "long",
            "Returns the duration of the process in system time.",
            READABLE, WRITEABLE, ISIS));
        
        return mBeanAttrInfoList;
    }

    @Override
    public MBeanConstructorInfo[] defineConstructors() {
        // creating an array for the MBeanConstructorInfo
        MBeanConstructorInfo[] mBeanConstrInfo = new MBeanConstructorInfo[1];
        // empty constructor
        mBeanConstrInfo[0] = new MBeanConstructorInfo("IMeasurements",
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
        if (attributeName.equals(MeasurementsConstants.AVG_LOAD)) {
            return getAvgLoad();
        } else if (attributeName.equals(MeasurementsConstants.AVG_MEM_USE)) {
            return getAvgMemUse();
        } else if (attributeName.equals(MeasurementsConstants.IO_READ)) {
            return getIoRead();
        } else if (attributeName.equals(MeasurementsConstants.IO_WRITE)) {
            return getIoWrite();
        } else if (attributeName.equals(MeasurementsConstants.MAX_LOAD)) {
            return getMaxLoad();
        } else if (attributeName.equals(MeasurementsConstants.MAX_MEM_USE)) {
            return getMaxMemUse();
        } else if (attributeName.equals(MeasurementsConstants.MIN_LOAD)) {
            return getMinLoad();
        } else if (attributeName.equals(MeasurementsConstants.MIN_MEM_USE)) {
            return getMinMemUse();
        } else if (attributeName.equals(MeasurementsConstants.STATUS)) {
            return getStatus();
        } else if (attributeName.equals(MeasurementsConstants.SYSTEM_TIME)) {
            return getSystemTime();
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
