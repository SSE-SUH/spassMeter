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
import de.uni_hildesheim.sse.serviceConstants.TimerConstants;

/**
 * JMX service for events from the {@link TimerChangeListener}.
 * 
 * @author Stephan Dederichs
 * 
 * @version 1.00
 * @since 1.00
 */
public class JMXTimer extends AbstractJMXServiceData {
    
    /**
     * Stores the recId.
     * 
     * @since 1.00
     */
    private String recId;
    
    /**
     * Stores a value.
     * 
     * @since 1.00
     */
    private long value;
    
    /**
     * Creates an instance of {@link JMXTimer}.
     * 
     * @param recId The recId.
     * @param value The value to store.
     * 
     * @since 1.00
     */
    public JMXTimer(String recId, long value) {
        this.recId = recId;
        this.value = value;
    }

    /**
     * Returns the recId.
     * 
     * @return The recId.
     * 
     * @since 1.00
     */
    public String getRecId() {
        return recId;
    }
    
    /**
     * Returns the value.
     * 
     * @return The value.
     * 
     * @since 1.00
     */
    public long getValue() {
        return value;
    }
    
    /**
     * Sets the value.
     * 
     * @param value The value to set.
     * 
     * @since 1.00
     */
    public void setValue(long value) {
        this.value = value;
    }
    
    // ------------------Dynamic MBean Methods------------------
    
    @Override
    public String[] defineMBeanInfo() {
        String[] mBeanInfo = new String[2];
        mBeanInfo[0] = "Timer";
        mBeanInfo[1] = "Dynamic MBean for a timer.";
        return mBeanInfo;
    }

    @Override
    public List<MBeanAttributeInfo> defineAttributes() {
        // Stores the attributes
        List<MBeanAttributeInfo> mBeanAttrInfoList = new 
                LinkedList<MBeanAttributeInfo>();
        
        // value
        mBeanAttrInfoList.add(new MBeanAttributeInfo(
            TimerConstants.VALUE, "long",
            "Returns the value of the timer.",
            READABLE, WRITEABLE, ISIS));

        return mBeanAttrInfoList;
    }

    @Override
    public MBeanConstructorInfo[] defineConstructors() {
        // creating an array for the MBeanConstructorInfo
        MBeanConstructorInfo[] mBeanConstrInfo = new MBeanConstructorInfo[1];
        // empty constructor
        mBeanConstrInfo[0] = new MBeanConstructorInfo("Timer",
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
        
        if (attributeName.equals(TimerConstants.VALUE)) {
            return value;
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
