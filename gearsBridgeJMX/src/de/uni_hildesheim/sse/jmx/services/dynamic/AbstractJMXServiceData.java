package de.uni_hildesheim.sse.jmx.services.dynamic;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ReflectionException;
import javax.management.RuntimeOperationsException;

import de.uni_hildesheim.sse.functions.IFunction;
import de.uni_hildesheim.sse.functions.IServiceData;
import de.uni_hildesheim.sse.jmx.services.JMXIdentity;

/**
 * Defines an abstract superclass for the specific data JMX service classes.
 * 
 * @author Stephan Dederichs
 * 
 * @since 1.00
 * @version 1.00
 */
public abstract class AbstractJMXServiceData implements IServiceData, 
    DynamicMBean, NotificationListener {

    /**
     * <code>true</code> if an attribute should be readable.
     * 
     * @since 1.00
     */
    public static final boolean READABLE = true;

    /**
     * <code>true</code> if an attribute should be writeable.
     * 
     * @since 1.00
     */
    public static final boolean WRITEABLE = false;

    /**
     * <code>true</code> if an attribute has an "is"-getter.
     * 
     * @since 1.00
     */
    public static final boolean ISIS = false;

    /**
     * List which contains all functions that are used for a JMX service.
     * 
     * @since 1.00
     */
    private List<IFunction> functions = new LinkedList<IFunction>();

    /**
     * Defines the specific MBeanInfo.
     * 
     * @return A {@link String}[] with the MBeanInfo.
     * 
     * @since 1.00
     */
    public abstract String[] defineMBeanInfo();

    /**
     * Defines the specific MBean attributes.
     * 
     * @return A {@link MBeanAttributeInfo}[] with the specific attributes.
     * 
     * @since 1.00
     */
    public abstract List<MBeanAttributeInfo> defineSpecificAttributes();

    /**
     * Defines the specific MBean constructors.
     * 
     * @return A {@link MBeanConstructorInfo}[] with the specific constructors.
     * 
     * @since 1.00
     */
    public abstract MBeanConstructorInfo[] defineConstructors();

    /**
     * Defines the specific MBean operations.
     * 
     * @return A {@link MBeanOperationInfo}[] with the specific operations.
     * 
     * @since 1.00
     */
    public abstract MBeanOperationInfo[] defineOperations();

    /**
     * Checks the specific attribute names.
     * 
     * @param attributeName the attribute name which will be checked.
     * 
     * @return the value of the attribute.
     * 
     * @throws AttributeNotFoundException If there is no attribute with the 
     *            given name.
     * 
     * @since 1.00
     */
    public abstract Object checkAttributeName(String attributeName)
        throws AttributeNotFoundException;

    /**
     * Invokes the specific methods.
     * 
     * @param operationName The name of the method.
     * @param params The parameters of the method.
     * @param signature The signature of the method.
     * 
     * @return The return value of the method.
     * 
     * @throws ReflectionException ReflectionException.
     * 
     * @since 1.00
     */
    public abstract Object invokeSpecificFunction(String operationName,
        Object[] params, String[] signature) throws ReflectionException;
    
    /**
     * Sets MBean specific attributes.
     * 
     * @param attribute The attribute to set.
     * 
     * @throws AttributeNotFoundException If there is no attribute with the 
     *            given name.
     * 
     * @since 1.00
     */
    public abstract void setSpecificAttributes(Attribute attribute)
        throws AttributeNotFoundException;

    /**
     * Adds a function of the type {@link IFunction} to the specific MBean
     * class.
     * 
     * @param function The function to add.
     * 
     * @since 1.00
     */
    public void addFunction(IFunction function) {
        functions.add(function);
    }

    /**
     * Returns a {@link List} with all attributes, the functions and the
     * specific.
     * 
     * @return a {@link List} with all attributes.
     * 
     * @since 1.00
     */
    public List<MBeanAttributeInfo> defineAttributes() {
        List<MBeanAttributeInfo> mbeanAttrInfo = new LinkedList
            <MBeanAttributeInfo>();

        List<MBeanAttributeInfo> specificAttributes = 
            defineSpecificAttributes();
        mbeanAttrInfo.addAll(specificAttributes);

        // Adding the functions (min, max, avg, ...)
        for (IFunction function : functions) {
            JMXIdentity identity = (JMXIdentity) function.getIdentity();
            mbeanAttrInfo.add(new MBeanAttributeInfo(identity
                    .getAttributeName() + "." + function.getFunctionType(),
                    identity.getAttributeType(), identity.getDescription(),
                    READABLE, WRITEABLE, ISIS));
        }

        return mbeanAttrInfo;
    }

    @Override
    public Object getSpecificAttribute(String attributeName) {
        Object result = null;
        try {
            result = getAttribute(attributeName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
    
    @Override
    public Object getAttribute(String attributeName)
        throws AttributeNotFoundException, MBeanException, ReflectionException {
        // checking if attributeName is null
        if (null == attributeName) {
            IllegalArgumentException iae = new IllegalArgumentException(
                    "Attribute name cannot be null");
            throw new RuntimeOperationsException(iae, "null attribute name");
        }

        // Check the names from the functions (min, max, avg, ...)
        for (IFunction function : functions) {
            if (attributeName.equals(function.getIdentity().getAttributeName() 
                    + "." + function.getFunctionType())) {
                return function.getValue();
            }
        }
        
        // Check the individual attributenames
        return checkAttributeName(attributeName);

    }

    @Override
    public void setAttribute(Attribute attribute)
        throws AttributeNotFoundException, InvalidAttributeValueException,
        MBeanException, ReflectionException {
        // checking if attribute is null
        if (null == attribute) {
            IllegalArgumentException iae = new IllegalArgumentException(
                    "Attribute cannot be null");
            throw new RuntimeOperationsException(iae, "null attribute");
        }
        // checking if attribute name is null
        if (null == attribute.getName()) {
            IllegalArgumentException iae = new IllegalArgumentException(
                    "Attribute name cannot be null");
            throw new RuntimeOperationsException(iae, "null attribute name");
        }
        // checking if attribute value is null
        if (null == attribute.getValue()) {
            IllegalArgumentException iae = new IllegalArgumentException(
                    "Attribute value cannot be null");
            throw new RuntimeOperationsException(iae, "null attribute value");
        }
        // Set individual attributes here
        setSpecificAttributes(attribute);
    }

    @Override
    public AttributeList getAttributes(String[] attributes) {
        // checking if attributes are null
        if (null == attributes) {
            IllegalArgumentException iae = new IllegalArgumentException(
                    "Attributes cannot be null");
            throw new RuntimeOperationsException(iae, "null attributes");
        }
        AttributeList resultList = new AttributeList();
        // getting all required attributes
        for (int i = 0; i < attributes.length; i++) {
            try {
                Object value = getSpecificAttribute((String) attributes[i]);
                resultList.add(new Attribute(attributes[i], value));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return resultList;
    }

    @Override
    public AttributeList setAttributes(AttributeList attributes) {
        // checking if attributes are null
        if (null == attributes) {
            IllegalArgumentException iae = new IllegalArgumentException(
                    "Attributes cannot be null");
            throw new RuntimeOperationsException(iae, "null attributes");
        }
        AttributeList resultList = new AttributeList();
        // setting all required attributes
        for (Iterator<Object> iter = attributes.iterator(); iter.hasNext();) {
            Attribute attr = (Attribute) iter.next();
            try {
                setAttribute(attr);
                String name = attr.getName();
                Object value = attr.getValue();
                resultList.add(new Attribute(name, value));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return resultList;
    }

    @Override
    public Object invoke(String operationName, Object[] params,
            String[] signature) throws MBeanException, ReflectionException {
        // checking if operationName are null
        if (null == operationName) {
            IllegalArgumentException iae = new IllegalArgumentException(
                    "Operation name cannot be null");
            throw new RuntimeOperationsException(iae, "null operation name");
        }

        // invoke general methods here

        // invoke the specific methods
        return invokeSpecificFunction(operationName, params, signature);
    }

    @Override
    public MBeanInfo getMBeanInfo() {
        // define MBeanInfo
        String[] mBeanInfo = defineMBeanInfo();

        // define MBean attributes
        List<MBeanAttributeInfo> mBeanAttrInfoList = defineAttributes();

        // define MBean constructors
        MBeanConstructorInfo[] mBeanConstrInfo = defineConstructors();

        // define MBean operations
        MBeanOperationInfo[] mBeanOperInfo = defineOperations();

        // Making an array out of the list with the attributes
        MBeanAttributeInfo[] mBeanAttrInfo = new MBeanAttributeInfo[
            mBeanAttrInfoList.size()];
        for (int i = 0; i < mBeanAttrInfoList.size(); i++) {
            mBeanAttrInfo[i] = mBeanAttrInfoList.get(i);
        }
        // create MBean info

        MBeanInfo mBeanInformation = new MBeanInfo(mBeanInfo[0], mBeanInfo[1],
                mBeanAttrInfo, mBeanConstrInfo, mBeanOperInfo, null);

        return mBeanInformation;
    }

    @Override
    public void handleNotification(Notification notification, Object handback) {
        for (IFunction f : functions) {
            f.calculate();
        }
    }
    
}
