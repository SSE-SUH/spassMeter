package de.uni_hildesheim.sse.jmx.services.standard;

import de.uni_hildesheim.sse.codeEraser.annotations.Operation;
import de.uni_hildesheim.sse.codeEraser.annotations.Variability;
import de.uni_hildesheim.sse.system.AnnotationConstants;
import de.uni_hildesheim.sse.system.GathererFactory;
import de.uni_hildesheim.sse.system.IThisProcessDataGatherer;
import de.uni_hildesheim.sse.system.IoStatistics;

/**
 * Defines an object for delegation of the values from the interface
 * {@link IThisProcessDataGatherer} to the {@link StandardMBeanAgent}.
 * 
 * @author Stephan Dederichs
 * 
 * @since 1.00
 * @version 1.00
 */
@Variability(id = AnnotationConstants.VAR_CURRENT_PROCESS_DATA)
public class ThisProcessData implements ThisProcessDataMBean {

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
    public ThisProcessData() {
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

}
