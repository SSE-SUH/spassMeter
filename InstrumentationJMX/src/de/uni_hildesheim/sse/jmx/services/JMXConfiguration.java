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
import de.uni_hildesheim.sse.monitoring.runtime.configuration.Configuration;
import de.uni_hildesheim.sse.serviceConstants.ConfigurationConstants;

/**
 * JMX service for the {@link Configuration} of the SPASS-monitor.
 * 
 * @author Stephan Dederichs
 * 
 * @version 1.00
 * @since 1.00
 */
public class JMXConfiguration extends AbstractJMXServiceData {
    
    /**
     * Creates an instance of {@link JMXConfiguration}.
     * 
     * @since 1.00
     */
    public JMXConfiguration() {
    }
    
    /**
     * Returns the outInterval of the configuration.
     * 
     * @return The outInterval of the configuration.
     * 
     * @since 1.00
     */
    public int getOutInterval() {
        return Configuration.INSTANCE.getOutInterval();
    }
    
    /**
     * Sets the outInterval of the configuration.
     *  
     * @param outInterval The outInterval to set.
     * 
     * @since 1.00
     */
    public void setOutInterval(int outInterval) {
        Configuration.INSTANCE.setOutInterval(outInterval);
    }
    
    /**
     * Returns the outFileName of the configuration.
     * 
     * @return The outFileName of the configuration.
     * 
     * @since 1.00
     */
    public String getOutFileName() {
        return Configuration.INSTANCE.getOutFileName();
    }
    
    
    // ------------------Dynamic MBean Methods------------------

    @Override
    public String[] defineMBeanInfo() {
        String[] mBeanInfo = new String[2];
        mBeanInfo[0] = "Configuration";
        mBeanInfo[1] = "Stores and maintains the configuration of the "
            + "measurement process.";
        return mBeanInfo;
    }

    @Override
    public List<MBeanAttributeInfo> defineAttributes() {
        // Stores the attributes
        List<MBeanAttributeInfo> mBeanAttrInfoList = new 
                LinkedList<MBeanAttributeInfo>();
        
        // outVal
        mBeanAttrInfoList.add(new MBeanAttributeInfo(ConfigurationConstants.
            OUT_INTERVAL, "int",
            "Stores the interval (in 500ms units) used for incrementally "
                + "printing aggregated events.",
            READABLE, true, ISIS));
        // outFileName
        mBeanAttrInfoList.add(new MBeanAttributeInfo(ConfigurationConstants.
            OUT_FILE_NAME, "java.lang.String",
            "The output file. May be null for console output. Default value "
                + "is null",
            READABLE, WRITEABLE, ISIS));

        return mBeanAttrInfoList;
    }

    @Override
    public MBeanConstructorInfo[] defineConstructors() {
        // creating an array for the MBeanConstructorInfo
        MBeanConstructorInfo[] mBeanConstrInfo = new MBeanConstructorInfo[1];
        // empty constructor
        mBeanConstrInfo[0] = new MBeanConstructorInfo("Configuration",
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
        
        if (attributeName.equals(ConfigurationConstants.OUT_INTERVAL)) {
            return getOutInterval();
        } else if (attributeName.equals(ConfigurationConstants.
                OUT_FILE_NAME)) {
            return getOutFileName();
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
    public void setSpecificAttributes(Attribute attribute)
        throws AttributeNotFoundException {
      
        if (attribute.getName().equals(ConfigurationConstants.OUT_INTERVAL)) {
            setOutInterval(Integer.parseInt(attribute.getValue().toString()));
        } else {
            throw new AttributeNotFoundException("Invalid attribute: " 
                + attribute.getName());
        }
    }

    @Override
    public Object invokeSpecificFunction(String operationName, Object[] params,
            String[] signature) throws ReflectionException {
        return null;
    }

}
