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
import de.uni_hildesheim.sse.monitoring.runtime.plugins.ValueType;
import de.uni_hildesheim.sse.serviceConstants.ValueConstants;

/**
 * JMX service for events from the {@link ValueChangeListener}.
 * 
 * @author Stephan Dederichs
 * 
 * @version 1.00
 * @since 1.00
 */
public class JMXValue extends AbstractJMXServiceData {

    /**
     * Stores the recId.
     * 
     * @since 1.00
     */
    private String recId;
    
    /**
     * Stores the type.
     * 
     * @since 1.00
     */
    private ValueType type;
    
    /**
     * Stores the newValue.
     * 
     * @since 1.00
     */
    private Object newValue;
    
    /**
     * Creates an instance of {@link JMXValue}.
     * 
     * @param recId The recId.
     * @param type The type.
     * @param newValue The newValue.
     * 
     * @since 1.00
     */
    public JMXValue(String recId, ValueType type, Object newValue) {
        this.recId = recId;
        this.type = type;
        this.newValue = newValue;
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
     * Returns the type.
     * 
     * @return The type
     * 
     * @since 1.00
     */
    public String getType() {
        return type.toString();
    }
    
    /**
     * Returns the newValue.
     * 
     * @return The newValue.
     * 
     * @since 1.00
     */
    public Object getNewValue() {
        return newValue;
    }
    
    /**
     * Sets the newValue.
     * 
     * @param newValue The newValue.
     * 
     * @since 1.00
     */
    public void setNewValue(Object newValue) {
        this.newValue = newValue;
    }
    
    // ------------------Dynamic MBean Methods------------------
    
    @Override
    public String[] defineMBeanInfo() {
        String[] mBeanInfo = new String[2];
        mBeanInfo[0] = "Value";
        mBeanInfo[1] = "Dynamic MBean for a value.";
        return mBeanInfo;
    }

    @Override
    public List<MBeanAttributeInfo> defineAttributes() {
        // Stores the attributes
        List<MBeanAttributeInfo> mBeanAttrInfoList = new 
                LinkedList<MBeanAttributeInfo>();
        
        // newValue
        mBeanAttrInfoList.add(new MBeanAttributeInfo(ValueConstants.
            NEW_VALUE, "java.lang.Object",
            "Returns the newValue.",
            READABLE, WRITEABLE, ISIS));
        
        // type
        mBeanAttrInfoList.add(new MBeanAttributeInfo(ValueConstants.
            TYPE, "java.lang.String",
            "Returns the type.",
            READABLE, WRITEABLE, ISIS));

        return mBeanAttrInfoList;
    }

    @Override
    public MBeanConstructorInfo[] defineConstructors() {
        // creating an array for the MBeanConstructorInfo
        MBeanConstructorInfo[] mBeanConstrInfo = new MBeanConstructorInfo[1];
        // empty constructor
        mBeanConstrInfo[0] = new MBeanConstructorInfo("Value",
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
        
        if (attributeName.equals(ValueConstants.NEW_VALUE)) {
            return getNewValue();
        } else if (attributeName.equals(ValueConstants.TYPE)) {
            return getType();
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
