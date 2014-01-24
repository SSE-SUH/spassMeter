package de.uni_hildesheim.sse.jmx.services.dynamic;

import java.util.LinkedList;
import java.util.List;

import javax.management.Attribute;
import javax.management.AttributeNotFoundException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.ReflectionException;

import de.uni_hildesheim.sse.codeEraser.annotations.Operation;
import de.uni_hildesheim.sse.codeEraser.annotations.Variability;
import de.uni_hildesheim.sse.serviceConstants.ThisProcessDataConstants;
import de.uni_hildesheim.sse.system.AnnotationConstants;
import de.uni_hildesheim.sse.system.GathererFactory;
import de.uni_hildesheim.sse.system.IThisProcessDataGatherer;
import de.uni_hildesheim.sse.system.IoStatistics;

/**
 * Defines an object for delegation of the values from the interface
 * {@link IThisProcessDataGatherer}. Additional to that it defines its JMX 
 * service Interface.
 * 
 * @author Stephan Dederichs
 * 
 * @since 1.00
 * @version 1.00
 */
@Variability(id = AnnotationConstants.VAR_CURRENT_PROCESS_DATA)
public class JMXThisProcessData extends AbstractJMXServiceData implements
        IThisProcessDataGatherer {

    /**
     * Instance of {@link IThisProcessDataGatherer} for gathering required
     * informations.
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.VAR_CURRENT_PROCESS_DATA)
    private IThisProcessDataGatherer thisProcessDataGatherer;

    /**
     * Constructor.
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.VAR_CURRENT_PROCESS_DATA)
    public JMXThisProcessData() {
        thisProcessDataGatherer = GathererFactory.getThisProcessDataGatherer();
    }

    @Override
    @Variability(id = AnnotationConstants.VAR_CURRENT_PROCESS_DATA)
    public String getCurrentProcessID() {
        return thisProcessDataGatherer.getCurrentProcessID();
    }

    @Override
    @Variability(id = { AnnotationConstants.VAR_CURRENT_PROCESS_DATA,
            AnnotationConstants.VAR_IO_DATA }, op = Operation.AND)
    public IoStatistics getCurrentProcessIo() {
        return thisProcessDataGatherer.getCurrentProcessIo();
    }

    @Override
    @Variability(id = { AnnotationConstants.VAR_CURRENT_PROCESS_DATA,
            AnnotationConstants.VAR_ALL_PROCESSES_DATA,
            AnnotationConstants.VAR_IO_DATA }, op = Operation.AND)
    public boolean isNetworkIoDataIncluded(boolean forAll) {
        return thisProcessDataGatherer.isNetworkIoDataIncluded(forAll);
    }

    @Override
    @Variability(id = { AnnotationConstants.VAR_CURRENT_PROCESS_DATA,
            AnnotationConstants.VAR_ALL_PROCESSES_DATA,
            AnnotationConstants.VAR_IO_DATA }, op = Operation.AND)
    public boolean isFileIoDataIncluded(boolean forAll) {
        return thisProcessDataGatherer.isFileIoDataIncluded(forAll);
    }

    @Override
    @Variability(id = { AnnotationConstants.VAR_CURRENT_PROCESS_DATA,
            AnnotationConstants.VAR_MEMORY_DATA }, op = Operation.AND)
    public long getCurrentProcessMemoryUse() {
        return thisProcessDataGatherer.getCurrentProcessMemoryUse();
    }

    @Override
    @Variability(id = { AnnotationConstants.VAR_CURRENT_PROCESS_DATA,
            AnnotationConstants.VAR_TIME_DATA }, op = Operation.AND)
    public long getCurrentProcessUserTimeTicks() {
        return thisProcessDataGatherer.getCurrentProcessUserTimeTicks();
    }

    @Override
    @Variability(id = { AnnotationConstants.VAR_CURRENT_PROCESS_DATA,
            AnnotationConstants.VAR_TIME_DATA }, op = Operation.AND)
    public long getCurrentProcessKernelTimeTicks() {
        return thisProcessDataGatherer.getCurrentProcessKernelTimeTicks();
    }

    @Override
    @Variability(id = { AnnotationConstants.VAR_CURRENT_PROCESS_DATA,
            AnnotationConstants.VAR_TIME_DATA }, op = Operation.AND)
    public long getCurrentProcessSystemTimeTicks() {
        return thisProcessDataGatherer.getCurrentProcessSystemTimeTicks();
    }

    @Override
    @Variability(id = { AnnotationConstants.VAR_CURRENT_PROCESS_DATA,
            AnnotationConstants.VAR_LOAD_DATA }, op = Operation.AND)
    public double getCurrentProcessProcessorLoad() {
        return thisProcessDataGatherer.getCurrentProcessProcessorLoad();
    }

    @Override
    @Variability(id = { AnnotationConstants.VAR_ALL_PROCESSES_DATA,
            AnnotationConstants.VAR_IO_DATA }, op = Operation.AND)
    public IoStatistics getAllProcessesIo() {
        return thisProcessDataGatherer.getAllProcessesIo();
    }

    // ------------------Dynamic MBean Methods------------------

    @Override
    public Object checkAttributeName(String attributeName)
        throws AttributeNotFoundException {
        // Check for individual attributes
        if (attributeName.equals(ThisProcessDataConstants.
                CURRENT_PROCESS_ID)) {
            return getCurrentProcessID();
        } else if (attributeName.equals(ThisProcessDataConstants.
                CURRENT_PROCESS_IO)) {
            return getCurrentProcessIo().read;
        } else if (attributeName.equals(ThisProcessDataConstants.
                CURRENT_PROCESS_MEMORY_USE)) {
            return getCurrentProcessMemoryUse();
        } else if (attributeName.equals(ThisProcessDataConstants.
                CURRENT_PROCESS_USER_TIME_TICKS)) {
            return getCurrentProcessUserTimeTicks();
        } else if (attributeName.equals(ThisProcessDataConstants.
                CURRENT_PROCESS_KERNEL_TIME_TICKS)) {
            return getCurrentProcessKernelTimeTicks();
        } else if (attributeName.equals(ThisProcessDataConstants.
                CURRENT_PROCESS_SYSTEM_TIME_TICKS)) {
            return getCurrentProcessSystemTimeTicks();
        } else if (attributeName.equals(ThisProcessDataConstants.
                CURRENT_PROCESS_PROCESSOR_LOAD)) {
            return getCurrentProcessProcessorLoad();
        } else if (attributeName.equals(ThisProcessDataConstants.
                ALL_PROCESS_IO)) {
            return getAllProcessesIo().read;
        } else {
            throw new AttributeNotFoundException("Invalid attribute: "
                    + attributeName);
        }
    }

    @Override
    public String[] defineMBeanInfo() {
        String[] mBeanInfo = new String[2];
        mBeanInfo[0] = "ThisProcessData";
        mBeanInfo[1] = "Dynamic MBean for getting information about the this "
            + "process data";
        return mBeanInfo;
    }

    @Override
    public List<MBeanAttributeInfo> defineSpecificAttributes() {
        List<MBeanAttributeInfo> mBeanAttrInfoList = new LinkedList
            <MBeanAttributeInfo>();

        // CURRENT_PROCESS_ID
        mBeanAttrInfoList.add(new MBeanAttributeInfo(ThisProcessDataConstants.
            CURRENT_PROCESS_ID, "java.lang.String", 
            "The identification the current process.",
            READABLE, WRITEABLE, ISIS));
        // CURRENT_PROCESS_IO
        mBeanAttrInfoList.add(new MBeanAttributeInfo(ThisProcessDataConstants.
            CURRENT_PROCESS_IO, "long",
            "The I/O statistics of the current process.", 
            READABLE, WRITEABLE, ISIS));
        // CURRENT_PROCESS_MEMORY_USE
        mBeanAttrInfoList.add(new MBeanAttributeInfo(ThisProcessDataConstants.
            CURRENT_PROCESS_MEMORY_USE, "long", 
            "The memory usage of the current process.", 
            READABLE, WRITEABLE, ISIS));
        // CURRENT_PROCESS_USER_TIME_TICKS
        mBeanAttrInfoList.add(new MBeanAttributeInfo(ThisProcessDataConstants.
            CURRENT_PROCESS_USER_TIME_TICKS, "long",
            "The CPU user time ticks of the current process.", 
            READABLE, WRITEABLE, ISIS));
        // CURRENT_PROCESS_KERNEL_TIME_TICKS
        mBeanAttrInfoList.add(new MBeanAttributeInfo(ThisProcessDataConstants.
            CURRENT_PROCESS_KERNEL_TIME_TICKS, "long",
            "The CPU kernel time ticks of the current process.", 
            READABLE, WRITEABLE, ISIS));
        // CURRENT_PROCESS_SYSTEM_TIME_TICKS
        mBeanAttrInfoList.add(new MBeanAttributeInfo(ThisProcessDataConstants.
            CURRENT_PROCESS_SYSTEM_TIME_TICKS, "long",
            "The system time ticks of the current process.", 
            READABLE, WRITEABLE, ISIS));
        // CURRENT_PROCESS_PROCESSOR_LOAD
        mBeanAttrInfoList.add(new MBeanAttributeInfo(ThisProcessDataConstants.
            CURRENT_PROCESS_PROCESSOR_LOAD, "double",
            "The load produced by the current process.", 
            READABLE, WRITEABLE, ISIS));
        // ALL_PROCESS_IO
        mBeanAttrInfoList.add(new MBeanAttributeInfo(ThisProcessDataConstants.
            ALL_PROCESS_IO, "long",
            "The I/O statistics for all currently running processes.",
            READABLE, WRITEABLE, ISIS));

        return mBeanAttrInfoList;
    }

    @Override
    public MBeanConstructorInfo[] defineConstructors() {
        // creating an array for the MBeanConstructorInfo
        MBeanConstructorInfo[] mBeanConstrInfo = new MBeanConstructorInfo[1];
        // empty constructor
        mBeanConstrInfo[0] = new MBeanConstructorInfo("ThisProcessData",
                "Public default constructor ...", null);

        return mBeanConstrInfo;
    }

    @Override
    public MBeanOperationInfo[] defineOperations() {
        MBeanOperationInfo[] mBeanOperInfo = new MBeanOperationInfo[2];
        MBeanParameterInfo[] signature = new MBeanParameterInfo[1];
        signature[0] = new MBeanParameterInfo("forAll", "java.lang.Boolean",
            "query this information for all processes, or otherways for a "
                + "single process");
        mBeanOperInfo[0] = new MBeanOperationInfo(
            "isNetworkIoDataIncluded",
            "Returns weather native network I/O statistics are included, i.e. "
                + "weather the system provides required capabilities to access "
                + "the statistics.",
            signature, "java.lang.Boolean", MBeanOperationInfo.INFO);
        mBeanOperInfo[1] = new MBeanOperationInfo(
            "isFileIoDataIncluded",
            "Returns weather native file I/O statistics are included, i.e."
                + " weather the system provides required capabilities to "
                + "access the statistics.",
            signature, "java.lang.Boolean", MBeanOperationInfo.INFO);
        return mBeanOperInfo;
    }

    @Override
    public Object invokeSpecificFunction(String operationName, Object[] params,
            String[] signature) throws ReflectionException {
        if (operationName.equals("isNetworkIoDataIncluded")) {
            return isNetworkIoDataIncluded(Boolean.getBoolean(params[0]
                    .toString()));
        } else if (operationName.equals("isFileIoDataIncluded")) {
            return isFileIoDataIncluded(Boolean
                    .getBoolean(params[0].toString()));
        } else {
            throw new ReflectionException(new NoSuchMethodException(
                    operationName), "Invalid operation name: " + operationName);
        }
    }

    @Override
    public void setSpecificAttributes(Attribute attribute)
        throws AttributeNotFoundException {        
    }
    
}