package de.uni_hildesheim.sse.jmx.services.dynamic;

import java.util.LinkedList;
import java.util.List;

import javax.management.Attribute;
import javax.management.AttributeNotFoundException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanOperationInfo;
import javax.management.ReflectionException;

import de.uni_hildesheim.sse.codeEraser.annotations.Variability;
import de.uni_hildesheim.sse.serviceConstants.BatteryDataConstants;
import de.uni_hildesheim.sse.system.AnnotationConstants;
import de.uni_hildesheim.sse.system.GathererFactory;
import de.uni_hildesheim.sse.system.IBatteryDataGatherer;

/**
 * Defines an object for delegation of the values from the interface
 * {@link IBatteryDataGatherer}. Additional to that it defines its JMX service
 * Interface.
 * 
 * @author Stephan Dederichs
 * 
 * @since 1.00
 * @version 1.00
 */
public class JMXBatteryData extends AbstractJMXServiceData implements
        IBatteryDataGatherer {

    /**
     * Instance of {@link IBatteryDataGatherer} for gathering required
     * informations.
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.VAR_PROCESSOR_DATA, value = "-1")
    private IBatteryDataGatherer batteryDataGatherer;

    /**
     * Creates an instance of {@link JMXBatteryData}.
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.VAR_PROCESSOR_DATA, value = "-1")
    public JMXBatteryData() {
        batteryDataGatherer = GathererFactory.getBatteryDataGatherer();
    }

    @Override
    @Variability(id = AnnotationConstants.VAR_ENERGY_DATA, value = "false")
    public boolean hasSystemBattery() {
        return batteryDataGatherer.hasSystemBattery();
    }

    @Override
    @Variability(id = AnnotationConstants.VAR_ENERGY_DATA, value = "-1")
    public int getBatteryLifePercent() {
        return batteryDataGatherer.getBatteryLifePercent();
    }

    @Override
    @Variability(id = AnnotationConstants.VAR_ENERGY_DATA, value = "-1")
    public int getBatteryLifeTime() {
        return batteryDataGatherer.getBatteryLifeTime();
    }

    @Override
    @Variability(id = AnnotationConstants.VAR_ENERGY_DATA, value = "-1")
    public int getPowerPlugStatus() {
        return batteryDataGatherer.getPowerPlugStatus();
    }

    // ------------------Dynamic MBean Methods------------------

    @Override
    public Object checkAttributeName(String attributeName)
        throws AttributeNotFoundException {
        // Check for individual attributes
        if (attributeName.equals(BatteryDataConstants.HAS_SYSTEM_BATTERY)) {
            return hasSystemBattery();
        } else if (attributeName.equals(BatteryDataConstants.
                BATTERY_LIFE_PERCENT)) {
            return getBatteryLifePercent();
        } else if (attributeName.equals(BatteryDataConstants.
                BATTERY_LIFE_TIME)) {
            return getBatteryLifeTime();
        } else if (attributeName.equals(BatteryDataConstants.
                POWER_PLUG_STATUS)) {
            return getPowerPlugStatus();
        } else {
            throw new AttributeNotFoundException("Invalid attribute: "
                    + attributeName);
        }
    }

    @Override
    public String[] defineMBeanInfo() {
        String[] mBeanInfo = new String[2];
        mBeanInfo[0] = "BateryData";
        mBeanInfo[1] = "Dynamic MBean for getting information about the " 
            + "battery data";
        return mBeanInfo;
    }

    @Override
    public List<MBeanAttributeInfo> defineSpecificAttributes() {
        List<MBeanAttributeInfo> mBeanAttrInfoList = new 
            LinkedList<MBeanAttributeInfo>();

        // HasSystemBattery
        mBeanAttrInfoList.add(new MBeanAttributeInfo(BatteryDataConstants.
            HAS_SYSTEM_BATTERY, "java.lang.Boolean",
            "True if the system is equipped with a battery, otherwise false.",
            READABLE, WRITEABLE, ISIS));
        // BatteryLifePercent
        mBeanAttrInfoList.add(new MBeanAttributeInfo(BatteryDataConstants.
            BATTERY_LIFE_PERCENT, "int", 
            "The remaining battery life time in percent.", READABLE,
            WRITEABLE, ISIS));
        // BatteryLifeTime
        mBeanAttrInfoList.add(new MBeanAttributeInfo(BatteryDataConstants.
            BATTERY_LIFE_TIME, "int",
            "The remaining battery life time in seconds.", READABLE,
            WRITEABLE, ISIS));
        // PowerPlugStatus
        mBeanAttrInfoList.add(new MBeanAttributeInfo(BatteryDataConstants.
            POWER_PLUG_STATUS, "int",
            "The power plug status, i.e. if the device is plugged in.",
            READABLE, WRITEABLE, ISIS));

        return mBeanAttrInfoList;
    }

    @Override
    public MBeanConstructorInfo[] defineConstructors() {
        // creating an array for the MBeanConstructorInfo
        MBeanConstructorInfo[] mBeanConstrInfo = new MBeanConstructorInfo[1];
        // empty constructor
        mBeanConstrInfo[0] = new MBeanConstructorInfo("BatteryData",
                "Public default constructor ...", null);

        return mBeanConstrInfo;
    }

    @Override
    public MBeanOperationInfo[] defineOperations() {
        MBeanOperationInfo[] mBeanOperInfo = new MBeanOperationInfo[0];
        return mBeanOperInfo;
    }

    @Override
    public Object invokeSpecificFunction(String operationName, Object[] params,
            String[] signature) throws ReflectionException {
        return null;
    }

    @Override
    public void setSpecificAttributes(Attribute attribute)
        throws AttributeNotFoundException {        
    }

}
