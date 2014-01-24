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
import de.uni_hildesheim.sse.serviceConstants.VolumeDataConstants;
import de.uni_hildesheim.sse.system.AnnotationConstants;
import de.uni_hildesheim.sse.system.GathererFactory;
import de.uni_hildesheim.sse.system.IVolumeDataGatherer;

/**
 * Defines an object for delegation of the values from the interface
 * {@link IVolumeDataGatherer}. Additional to that it defines its JMX service
 * Interface.
 * 
 * @author Stephan Dederichs
 * 
 * @since 1.00
 * @version 1.00
 */
@Variability(id = AnnotationConstants.VAR_VOLUME_DATA)
public class JMXVolumeData extends AbstractJMXServiceData implements
        IVolumeDataGatherer {

    /**
     * Instance of {@link IVolumeDataGatherer} for gathering required
     * informations.
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.VAR_VOLUME_DATA)
    private IVolumeDataGatherer volumeDataGatherer;

    /**
     * Constructor.
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.VAR_VOLUME_DATA)
    public JMXVolumeData() {
        volumeDataGatherer = GathererFactory.getVolumeDataGatherer();
    }

    @Override
    @Variability(id = AnnotationConstants.VAR_VOLUME_DATA, value = "0")
    public long getVolumeCapacity() {
        return volumeDataGatherer.getVolumeCapacity();
    }

    @Override
    @Variability(id = AnnotationConstants.VAR_VOLUME_DATA, value = "0")
    public long getCurrentVolumeAvail() {
        return volumeDataGatherer.getCurrentVolumeAvail();
    }

    @Override
    @Variability(id = AnnotationConstants.VAR_VOLUME_DATA, value = "0")
    public long getCurrentVolumeUse() {
        return volumeDataGatherer.getCurrentVolumeUse();
    }

    // ------------------Dynamic MBean Methods------------------

    @Override
    public Object checkAttributeName(String attributeName)
        throws AttributeNotFoundException {
        // Check for individual attributes
        if (attributeName.equals(VolumeDataConstants.VOLUME_CAPACITY)) {
            return getVolumeCapacity();
        } else if (attributeName.equals(VolumeDataConstants.
                CURRENT_VOLUME_AVAIL)) {
            return getCurrentVolumeAvail();
        } else if (attributeName.equals(VolumeDataConstants.
                CURRENT_VOLUME_USE)) {
            return getCurrentVolumeUse();
        } else {
            throw new AttributeNotFoundException("Invalid attribute: "
                    + attributeName);
        }
    }

    @Override
    public String[] defineMBeanInfo() {
        String[] mBeanInfo = new String[2];
        mBeanInfo[0] = "VolumeData";
        mBeanInfo[1] = "Dynamic MBean for getting information about the "
            + "volume data";
        return mBeanInfo;
    }

    @Override
    public List<MBeanAttributeInfo> defineSpecificAttributes() {
        List<MBeanAttributeInfo> mBeanAttrInfoList = new LinkedList
            <MBeanAttributeInfo>();

        // VolumeCapacity
        mBeanAttrInfoList.add(new MBeanAttributeInfo(VolumeDataConstants.
            VOLUME_CAPACITY, "long",
            "True if the system is equipped with a battery, otherwise false.",
            READABLE, WRITEABLE, ISIS));
        // CurrentVolumeAvail
        mBeanAttrInfoList.add(new MBeanAttributeInfo(VolumeDataConstants.
            CURRENT_VOLUME_AVAIL, "long", 
            "The remaining battery life time in percent.",
            READABLE, WRITEABLE, ISIS));
        // CurrentVolumeUse
        mBeanAttrInfoList.add(new MBeanAttributeInfo(VolumeDataConstants.
            CURRENT_VOLUME_USE, "long", 
            "The remaining battery life time in seconds.",
            READABLE, WRITEABLE, ISIS));

        return mBeanAttrInfoList;
    }

    @Override
    public MBeanConstructorInfo[] defineConstructors() {
        // creating an array for the MBeanConstructorInfo
        MBeanConstructorInfo[] mBeanConstrInfo = new MBeanConstructorInfo[1];
        // empty constructor
        mBeanConstrInfo[0] = new MBeanConstructorInfo("VolumeData",
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
