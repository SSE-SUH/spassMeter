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
import de.uni_hildesheim.sse.serviceConstants.NetworkDataConstants;
import de.uni_hildesheim.sse.system.AnnotationConstants;
import de.uni_hildesheim.sse.system.GathererFactory;
import de.uni_hildesheim.sse.system.INetworkDataGatherer;

/**
 * Defines an object for delegation of the values from the interface
 * {@link INetworkDataGatherer}. Additional to that it defines its JMX service
 * Interface.
 * 
 * @author Stephan Dederichs
 * 
 * @since 1.00
 * @version 1.00
 */
@Variability(id = AnnotationConstants.VAR_NETWORK_DATA)
public class JMXNetworkData extends AbstractJMXServiceData implements
        INetworkDataGatherer {

    /**
     * Instance of {@link INetworkDataGatherer} for gathering required
     * informations.
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.VAR_NETWORK_DATA)
    private INetworkDataGatherer networkDataGetherer;

    /**
     * Constructor.
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.VAR_NETWORK_DATA)
    public JMXNetworkData() {
        networkDataGetherer = GathererFactory.getNetworkDataGatherer();
    }

    @Override
    @Variability(id = AnnotationConstants.VAR_NETWORK_DATA, value = "0")
    public long getCurrentNetSpeed() {
        return networkDataGetherer.getCurrentNetSpeed();
    }

    @Override
    @Variability(id = AnnotationConstants.VAR_NETWORK_DATA, value = "0")
    public long getMaxNetSpeed() {
        return networkDataGetherer.getMaxNetSpeed();
    }

    // ------------------Dynamic MBean Methods------------------

    @Override
    public Object checkAttributeName(String attributeName)
        throws AttributeNotFoundException {
        // Check for individual attributes
        if (attributeName.equals(NetworkDataConstants.CURRENT_NET_SPEED)) {
            return getCurrentNetSpeed();
        } else if (attributeName.equals(NetworkDataConstants.MAX_NET_SPEED)) {
            return getMaxNetSpeed();
        } else {
            throw new AttributeNotFoundException("Invalid attribute: "
                    + attributeName);
        }
    }

    @Override
    public String[] defineMBeanInfo() {
        String[] mBeanInfo = new String[2];
        mBeanInfo[0] = "NetworkData";
        mBeanInfo[1] = "Dynamic MBean for getting information about the"
            + " network data";
        return mBeanInfo;
    }

    @Override
    public List<MBeanAttributeInfo> defineSpecificAttributes() {
        List<MBeanAttributeInfo> mBeanAttrInfoList = new LinkedList
            <MBeanAttributeInfo>();

        // CurrentNetSpeed
        mBeanAttrInfoList.add(new MBeanAttributeInfo(NetworkDataConstants.
            CURRENT_NET_SPEED, "long",
            "The (average) available speed of the currently enabled network " 
                + "device(s).",
            READABLE, WRITEABLE, ISIS));
        // MaxNetSpeed
        mBeanAttrInfoList.add(new MBeanAttributeInfo(NetworkDataConstants.
            MAX_NET_SPEED, "long",
            "The maximum speed of the currently enabled network device(s).",
            READABLE, WRITEABLE, ISIS));

        return mBeanAttrInfoList;
    }

    @Override
    public MBeanConstructorInfo[] defineConstructors() {
        // creating an array for the MBeanConstructorInfo
        MBeanConstructorInfo[] mBeanConstrInfo = new MBeanConstructorInfo[1];
        // empty constructor
        mBeanConstrInfo[0] = new MBeanConstructorInfo("NetworkData",
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
