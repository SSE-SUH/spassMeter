package de.uni_hildesheim.sse.wildcat.services;

import javax.management.AttributeNotFoundException;

import de.uni_hildesheim.sse.codeEraser.annotations.Operation;
import de.uni_hildesheim.sse.codeEraser.annotations.Variability;
import de.uni_hildesheim.sse.serviceConstants.ThisProcessDataConstants;
import de.uni_hildesheim.sse.system.AnnotationConstants;
import de.uni_hildesheim.sse.system.GathererFactory;
import de.uni_hildesheim.sse.system.IThisProcessDataGatherer;
import de.uni_hildesheim.sse.system.IoStatistics;

/**
 * Defines an object for delegation of the values from the interface
 * {@link IThisProcessDataGatherer}. Additional to that it defines its WildCAT
 * service interface.
 * 
 * @author Stephan Dederichs
 * 
 * @since 1.00
 * @version 1.00
 */
@Variability(id = AnnotationConstants.VAR_CURRENT_PROCESS_DATA)
public class WThisProcessData extends AbstractWServiceData 
    implements IThisProcessDataGatherer {

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
    public WThisProcessData() {
        thisProcessDataGatherer = GathererFactory.
                getThisProcessDataGatherer();
    }

    @Override
    @Variability(id = AnnotationConstants.VAR_CURRENT_PROCESS_DATA)
    public String getCurrentProcessID() {
        String result = thisProcessDataGatherer.getCurrentProcessID();
        setSensorValue(ThisProcessDataConstants.
                CURRENT_PROCESS_ID, result);
        return result;
    }

    @Override
    @Variability(id = { AnnotationConstants.VAR_CURRENT_PROCESS_DATA,
            AnnotationConstants.VAR_IO_DATA }, op = Operation.AND)
    public IoStatistics getCurrentProcessIo() {
        IoStatistics result = thisProcessDataGatherer.getCurrentProcessIo();
        setSensorValue(ThisProcessDataConstants.
                CURRENT_PROCESS_IO, result.read);
        return result;
    }

    @Override
    @Variability(id = { AnnotationConstants.VAR_CURRENT_PROCESS_DATA,
            AnnotationConstants.VAR_ALL_PROCESSES_DATA,
            AnnotationConstants.VAR_IO_DATA }, op = Operation.AND)
    public boolean isNetworkIoDataIncluded(boolean forAll) {
        boolean result = thisProcessDataGatherer.
                isNetworkIoDataIncluded(forAll);
        setSensorValue(ThisProcessDataConstants.
                IS_NETWORK_DATA_INCLUDED, result);
        return result;
    }

    @Override
    @Variability(id = { AnnotationConstants.VAR_CURRENT_PROCESS_DATA,
            AnnotationConstants.VAR_ALL_PROCESSES_DATA,
            AnnotationConstants.VAR_IO_DATA }, op = Operation.AND)
    public boolean isFileIoDataIncluded(boolean forAll) {
        boolean result = thisProcessDataGatherer.isFileIoDataIncluded(forAll);
        setSensorValue(ThisProcessDataConstants.
                IS_FILE_IO_DATA_INCLUDED, result);
        return result;
    }

    @Override
    @Variability(id = { AnnotationConstants.VAR_CURRENT_PROCESS_DATA,
            AnnotationConstants.VAR_MEMORY_DATA }, op = Operation.AND)
    public long getCurrentProcessMemoryUse() {
        long result = thisProcessDataGatherer.getCurrentProcessMemoryUse();
        setSensorValue(ThisProcessDataConstants.
                CURRENT_PROCESS_MEMORY_USE, result);
        return result;
    }

    @Override
    @Variability(id = { AnnotationConstants.VAR_CURRENT_PROCESS_DATA,
            AnnotationConstants.VAR_TIME_DATA }, op = Operation.AND)
    public long getCurrentProcessUserTimeTicks() {
        long result = thisProcessDataGatherer.getCurrentProcessUserTimeTicks();
        setSensorValue(ThisProcessDataConstants.
                CURRENT_PROCESS_USER_TIME_TICKS, result);
        return result;
    }

    @Override
    @Variability(id = { AnnotationConstants.VAR_CURRENT_PROCESS_DATA,
            AnnotationConstants.VAR_TIME_DATA }, op = Operation.AND)
    public long getCurrentProcessKernelTimeTicks() {
        long result = thisProcessDataGatherer.
                getCurrentProcessKernelTimeTicks();
        setSensorValue(ThisProcessDataConstants.
                CURRENT_PROCESS_KERNEL_TIME_TICKS, result);
        return result;
    }

    @Override
    @Variability(id = { AnnotationConstants.VAR_CURRENT_PROCESS_DATA,
            AnnotationConstants.VAR_TIME_DATA }, op = Operation.AND)
    public long getCurrentProcessSystemTimeTicks() {
        long result = thisProcessDataGatherer.
                getCurrentProcessSystemTimeTicks();
        setSensorValue(ThisProcessDataConstants.
                CURRENT_PROCESS_SYSTEM_TIME_TICKS, result);
        return result;
    }

    @Override
    @Variability(id = { AnnotationConstants.VAR_CURRENT_PROCESS_DATA,
            AnnotationConstants.VAR_LOAD_DATA }, op = Operation.AND)
    public double getCurrentProcessProcessorLoad() {
        double result = thisProcessDataGatherer.
                getCurrentProcessProcessorLoad();
        setSensorValue(ThisProcessDataConstants.
                CURRENT_PROCESS_PROCESSOR_LOAD, result);
        return result;
    }

    @Override
    @Variability(id = { AnnotationConstants.VAR_ALL_PROCESSES_DATA,
            AnnotationConstants.VAR_IO_DATA }, op = Operation.AND)
    public IoStatistics getAllProcessesIo() {
        IoStatistics result = thisProcessDataGatherer.getAllProcessesIo();
        setSensorValue(ThisProcessDataConstants.ALL_PROCESS_IO, result.read);
        return result;
    }

    // ------------------------------------------------------------------------
    
    @Override
    public void burstValueUpdate() {
        getCurrentProcessID();
        getCurrentProcessIo();
        isNetworkIoDataIncluded(true);
        isFileIoDataIncluded(true);
        getCurrentProcessMemoryUse();
        getCurrentProcessUserTimeTicks();
        getCurrentProcessKernelTimeTicks();
        getCurrentProcessSystemTimeTicks();
        getCurrentProcessProcessorLoad();
        getAllProcessesIo();
    }
    
    @Override
    public Object getDataSpecificAttribute(String attributeName)
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
                IS_NETWORK_DATA_INCLUDED)) {
            return isNetworkIoDataIncluded(true);
        } else if (attributeName.equals(ThisProcessDataConstants.
                IS_FILE_IO_DATA_INCLUDED)) {
            return isFileIoDataIncluded(true);
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

}
