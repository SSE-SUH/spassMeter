package de.uni_hildesheim.sse.jmx.services.standard;

import de.uni_hildesheim.sse.codeEraser.annotations.Operation;
import de.uni_hildesheim.sse.codeEraser.annotations.Variability;
import de.uni_hildesheim.sse.system.AnnotationConstants;
import de.uni_hildesheim.sse.system.GathererFactory;
import de.uni_hildesheim.sse.system.IProcessDataGatherer;
import de.uni_hildesheim.sse.system.IoStatistics;

/**
 * Defines an object for delegation of the values from the interface
 * {@link IProcessDataGatherer} to the {@link StandardMBeanAgent}.
 * 
 * @author Stephan Dederichs
 * 
 * @since 1.00
 * @version 1.00
 */
@Variability(id = AnnotationConstants.VAR_ARBITRARY_PROCESS_DATA)
public class ProcessData implements ProcessDataMBean {

    /**
     * Instance of {@link IProcessDataGatherer} for gathering required
     * informations.
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.VAR_ARBITRARY_PROCESS_DATA)
    private IProcessDataGatherer processDataGatherer;

    /**
     * Constructor.
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.VAR_ARBITRARY_PROCESS_DATA)
    public ProcessData() {
        processDataGatherer = GathererFactory.getProcessDataGatherer();
    }

    @Override
    @Variability(id = AnnotationConstants.VAR_ARBITRARY_PROCESS_DATA)
    public boolean isProcessAlive(String pid) {
        return processDataGatherer.isProcessAlive(pid);
    }

    @Override
    @Variability(id = { AnnotationConstants.VAR_ARBITRARY_PROCESS_DATA,
            AnnotationConstants.VAR_IO_DATA }, op = Operation.AND)
    public IoStatistics getProcessIo(String pid) {
        return processDataGatherer.getProcessIo(pid);
    }

    @Override
    @Variability(id = { AnnotationConstants.VAR_ARBITRARY_PROCESS_DATA,
            AnnotationConstants.VAR_ALL_PROCESSES_DATA,
            AnnotationConstants.VAR_IO_DATA }, op = Operation.AND)
    public boolean isNetworkIoDataIncluded(boolean forAll) {
        return processDataGatherer.isNetworkIoDataIncluded(forAll);
    }

    @Override
    @Variability(id = { AnnotationConstants.VAR_ARBITRARY_PROCESS_DATA,
            AnnotationConstants.VAR_ALL_PROCESSES_DATA,
            AnnotationConstants.VAR_IO_DATA }, op = Operation.AND)
    public boolean isFileIoDataIncluded(boolean forAll) {
        return processDataGatherer.isFileIoDataIncluded(forAll);
    }

    @Override
    @Variability(id = { AnnotationConstants.VAR_ARBITRARY_PROCESS_DATA,
            AnnotationConstants.VAR_MEMORY_DATA }, op = Operation.AND)
    public long getProcessMemoryUse(String pid) {
        return processDataGatherer.getProcessMemoryUse(pid);
    }

    @Override
    @Variability(id = { AnnotationConstants.VAR_ARBITRARY_PROCESS_DATA,
            AnnotationConstants.VAR_TIME_DATA }, op = Operation.AND)
    public long getProcessUserTimeTicks(String pid) {
        return processDataGatherer.getProcessUserTimeTicks(pid);
    }

    @Override
    @Variability(id = { AnnotationConstants.VAR_ARBITRARY_PROCESS_DATA,
            AnnotationConstants.VAR_TIME_DATA }, op = Operation.AND)
    public long getProcessKernelTimeTicks(String pid) {
        return processDataGatherer.getProcessKernelTimeTicks(pid);
    }

    @Override
    @Variability(id = { AnnotationConstants.VAR_ARBITRARY_PROCESS_DATA,
            AnnotationConstants.VAR_TIME_DATA }, op = Operation.AND)
    public long getProcessSystemTimeTicks(String pid) {
        return processDataGatherer.getProcessSystemTimeTicks(pid);
    }

    @Override
    @Variability(id = { AnnotationConstants.VAR_ARBITRARY_PROCESS_DATA,
            AnnotationConstants.VAR_LOAD_DATA }, op = Operation.AND)
    public double getProcessProcessorLoad(String pid) {
        return processDataGatherer.getProcessProcessorLoad(pid);
    }

    @Override
    @Variability(id = { AnnotationConstants.VAR_ALL_PROCESSES_DATA,
            AnnotationConstants.VAR_IO_DATA }, op = Operation.AND)
    public IoStatistics getAllProcessesIo() {
        return processDataGatherer.getAllProcessesIo();
    }

}
