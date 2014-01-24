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
import de.uni_hildesheim.sse.serviceConstants.MemoryDataConstants;
import de.uni_hildesheim.sse.system.AnnotationConstants;
import de.uni_hildesheim.sse.system.GathererFactory;
import de.uni_hildesheim.sse.system.IMemoryDataGatherer;
import de.uni_hildesheim.sse.system.IMemoryUnallocationReceiver;

/**
 * Defines an object for delegation of the values from the interface
 * {@link IMemoryDataGatherer}. Additional to that it defines its JMX service
 * Interface.
 * 
 * @author Stephan Dederichs
 * 
 * @since 1.00
 * @version 1.00
 */
@Variability(id = AnnotationConstants.VAR_MEMORY_DATA)
public class JMXMemoryData extends AbstractJMXServiceData implements
        IMemoryDataGatherer {

    /**
     * Instance of {@link IMemoryDataGatherer} for gathering required
     * informations.
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.VAR_MEMORY_DATA)
    private IMemoryDataGatherer memoryDataGatherer;

    /**
     * Constructor.
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.VAR_MEMORY_DATA)
    public JMXMemoryData() {
        memoryDataGatherer = GathererFactory.getMemoryDataGatherer();
    }

    @Override
    @Variability(id = AnnotationConstants.VAR_MEMORY_DATA, value = "-1")
    public long getMemoryCapacity() {
        return memoryDataGatherer.getMemoryCapacity();
    }

    @Override
    @Variability(id = AnnotationConstants.VAR_MEMORY_DATA, value = "-1")
    public long getCurrentMemoryAvail() {
        return memoryDataGatherer.getCurrentMemoryAvail();
    }

    @Override
    @Variability(id = AnnotationConstants.VAR_MEMORY_DATA, value = "-1")
    public long getCurrentMemoryUse() {
        return memoryDataGatherer.getCurrentMemoryUse();
    }

    @Override
    public long getObjectSize(Object object) {
        return memoryDataGatherer.getObjectSize(object);
    }

    @Override
    public void receiveUnallocations(IMemoryUnallocationReceiver arg0) {
        memoryDataGatherer.receiveUnallocations(arg0);
    }

    @Override
    public void recordUnallocation(Object arg0, long arg1, String arg2) {
        memoryDataGatherer.recordUnallocation(arg0, arg1, arg2);
    }

    @Override
    public void recordUnallocation(Object arg0, long arg1, int arg2,
        String... arg3) {
        memoryDataGatherer.recordUnallocation(arg0, arg1, arg2, arg3);
    }

    @Override
    public void recordUnallocationByTag(long arg0) {
        memoryDataGatherer.recordUnallocationByTag(arg0);
    }

    @Override
    public void recordUnallocationByTag(long arg0, long arg1, String arg2) {
        memoryDataGatherer.recordUnallocationByTag(arg0, arg1, arg2);
    }

    @Override
    public void recordUnallocationByTag(long arg0, long arg1, int arg2,
        String... arg3) {
        memoryDataGatherer.recordUnallocationByTag(arg0, arg1, arg2, arg3);
    }
    
    // ------------------Dynamic MBean Methods------------------

    @Override
    public Object checkAttributeName(String attributeName)
        throws AttributeNotFoundException {
        // Check for individual attributes
        if (attributeName.equals(MemoryDataConstants.MEMORY_CAPACITY)) {
            return getMemoryCapacity();
        } else if (attributeName.equals(MemoryDataConstants.
                CURRENT_MEMORY_AVAIL)) {
            return getCurrentMemoryAvail();
        } else if (attributeName.equals(MemoryDataConstants.
                CURRENT_MEMORY_USE)) {
            return getCurrentMemoryUse();
        } else {
            throw new AttributeNotFoundException("Invalid attribute: "
                    + attributeName);
        }
    }

    @Override
    public String[] defineMBeanInfo() {
        String[] mBeanInfo = new String[2];
        mBeanInfo[0] = "MemoryData";
        mBeanInfo[1] = "Dynamic MBean for getting information about the memory"
            + " data";
        return mBeanInfo;
    }

    @Override
    public List<MBeanAttributeInfo> defineSpecificAttributes() {
        List<MBeanAttributeInfo> mBeanAttrInfoList = new LinkedList
            <MBeanAttributeInfo>();

        // MemoryCapacity
        mBeanAttrInfoList.add(new MBeanAttributeInfo(MemoryDataConstants.
            MEMORY_CAPACITY, "long",
            "The size of the physical memory.", READABLE, WRITEABLE, ISIS));
        // CurrentMemoryAvail
        mBeanAttrInfoList.add(new MBeanAttributeInfo(MemoryDataConstants.
            CURRENT_MEMORY_AVAIL, "long",
            "The size of the physical memory being currently available.",
            READABLE, WRITEABLE, ISIS));
        // CurrentMemoryUse
        mBeanAttrInfoList.add(new MBeanAttributeInfo(MemoryDataConstants.
            CURRENT_MEMORY_USE, "long",
            "The size of the physical memory being currently in use.",
            READABLE, WRITEABLE, ISIS));

        return mBeanAttrInfoList;
    }

    @Override
    public MBeanConstructorInfo[] defineConstructors() {
        // creating an array for the MBeanConstructorInfo
        MBeanConstructorInfo[] mBeanConstrInfo = new MBeanConstructorInfo[1];
        // empty constructor
        mBeanConstrInfo[0] = new MBeanConstructorInfo("MemoryData",
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
