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
import de.uni_hildesheim.sse.serviceConstants.ProcessorDataConstants;
import de.uni_hildesheim.sse.system.AnnotationConstants;
import de.uni_hildesheim.sse.system.GathererFactory;
import de.uni_hildesheim.sse.system.IProcessorDataGatherer;

/**
 * Defines an object for delegation of the values from the interface
 * {@link IProcessorDataGatherer}. Additional to that it defines its JMX service
 * Interface.
 * 
 * @author Stephan Dederichs
 * 
 * @since 1.00
 * @version 1.00
 */
public class JMXProcessorData extends AbstractJMXServiceData implements
        IProcessorDataGatherer {

    /**
     * Instance of {@link IProcessorDataGatherer} for gathering required
     * informations.
     * 
     * @since 1.00
     */
    private IProcessorDataGatherer processorDataGatherer;

    /**
     * Constructor.
     * 
     * @since 1.00
     */
    public JMXProcessorData() {
        processorDataGatherer = GathererFactory.getProcessorDataGatherer();
    }

    @Override
    @Variability(id = AnnotationConstants.VAR_PROCESSOR_DATA, value = "-1")
    public int getNumberOfProcessors() {
        return processorDataGatherer.getNumberOfProcessors();
    }

    @Override
    @Variability(id = AnnotationConstants.VAR_PROCESSOR_DATA, value = "-1")
    public int getMaxProcessorSpeed() {
        return processorDataGatherer.getMaxProcessorSpeed();
    }

    @Override
    @Variability(id = AnnotationConstants.VAR_PROCESSOR_DATA, value = "-1")
    public int getCurrentProcessorSpeed() {
        return processorDataGatherer.getCurrentProcessorSpeed();
    }

    @Override
    @Variability(id = AnnotationConstants.VAR_PROCESSOR_DATA, value = "-1")
    public double getCurrentSystemLoad() {
        return processorDataGatherer.getCurrentSystemLoad();
    }

    // ------------------Dynamic MBean Methods------------------

    @Override
    public Object checkAttributeName(String attributeName)
        throws AttributeNotFoundException {
        // Check for individual attributes
        if (attributeName.equals(ProcessorDataConstants.
                NUMBER_OF_PROCESSORS)) {
            return getNumberOfProcessors();
        } else if (attributeName.equals(ProcessorDataConstants.
                MAX_PROCESSOR_SPEED)) {
            return getMaxProcessorSpeed();
        } else if (attributeName.equals(ProcessorDataConstants.
                CURRENT_PROCESSOR_SPEED)) {
            return getCurrentProcessorSpeed();
        } else if (attributeName.equals(ProcessorDataConstants.
                CURRENT_SYSTEM_LOAD)) {
            return getCurrentSystemLoad();
        } else {
            throw new AttributeNotFoundException("Invalid attribute: "
                    + attributeName);
        }
    }

    @Override
    public String[] defineMBeanInfo() {
        String[] mBeanInfo = new String[2];
        mBeanInfo[0] = "ProcessorData";
        mBeanInfo[1] = "Dynamic MBean for getting information about the "
            + "processor data";
        return mBeanInfo;
    }

    @Override
    public List<MBeanAttributeInfo> defineSpecificAttributes() {
        List<MBeanAttributeInfo> mBeanAttrInfoList = new LinkedList
            <MBeanAttributeInfo>();

        // NumberOfProcessors
        mBeanAttrInfoList.add(new MBeanAttributeInfo(ProcessorDataConstants.
            NUMBER_OF_PROCESSORS, "int", 
            "The number of processors.", 
            READABLE, WRITEABLE, ISIS));
        // MaxProcessorSpeed
        mBeanAttrInfoList.add(new MBeanAttributeInfo(ProcessorDataConstants.
            MAX_PROCESSOR_SPEED, "int", 
            "The maximum physical speed of the processor(s).",
            READABLE, WRITEABLE, ISIS));
        // CurrentProcessorSpeed
        mBeanAttrInfoList.add(new MBeanAttributeInfo(ProcessorDataConstants.
            CURRENT_PROCESSOR_SPEED, "int", 
            "The (average) physical speed of the processor(s).",
            READABLE, WRITEABLE, ISIS));
        // CurrentSystemLoad
        mBeanAttrInfoList.add(new MBeanAttributeInfo(ProcessorDataConstants.
            CURRENT_SYSTEM_LOAD, "double", 
            "The the current estimated CPU load.", 
            READABLE, WRITEABLE, ISIS));

        return mBeanAttrInfoList;
    }

    @Override
    public MBeanConstructorInfo[] defineConstructors() {
        // creating an array for the MBeanConstructorInfo
        MBeanConstructorInfo[] mBeanConstrInfo = new MBeanConstructorInfo[1];
        // empty constructor
        mBeanConstrInfo[0] = new MBeanConstructorInfo("ProcessorData",
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
