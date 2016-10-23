package de.uni_hildesheim.sse.system.fallback;

import de.uni_hildesheim.sse.codeEraser.annotations.Operation;
import de.uni_hildesheim.sse.codeEraser.annotations.Variability;
import de.uni_hildesheim.sse.system.AnnotationConstants;
import de.uni_hildesheim.sse.system.IProcessDataGatherer;
import de.uni_hildesheim.sse.system.IoStatistics;

/**
 * Requests process information from the system.
 * 
 * @author Holger Eichelberger
 * @since 1.20
 * @version 1.20
 */
@Variability(id = AnnotationConstants.VAR_ARBITRARY_PROCESS_DATA)
class ProcessDataGatherer implements IProcessDataGatherer {

    @Override
    public IoStatistics getProcessIo(int pid) {
        return null;
    }
    
    @Variability(id = { AnnotationConstants.VAR_ARBITRARY_PROCESS_DATA,
            AnnotationConstants.VAR_ALL_PROCESSES_DATA,
            AnnotationConstants.VAR_IO_DATA }, op = Operation.AND)
    @Override
    public boolean isNetworkIoDataIncluded(boolean forAll) {
        return false;
    }
    
    @Variability(id = { AnnotationConstants.VAR_ARBITRARY_PROCESS_DATA,
            AnnotationConstants.VAR_ALL_PROCESSES_DATA,
            AnnotationConstants.VAR_IO_DATA }, op = Operation.AND)
    @Override
    public boolean isFileIoDataIncluded(boolean forAll) {
        return false;
    }

    @Override
    public long getProcessMemoryUse(int pid) {
        return -1;
    }

    @Override
    public long getProcessUserTimeTicks(int pid) {
        return -1;
    }

    @Override
    public long getProcessKernelTimeTicks(int pid) {
        return -1;
    }

    @Override
    public long getProcessSystemTimeTicks(int pid) {
        return -1;
    }

    @Override
    public double getProcessProcessorLoad(int pid) {
        return -1;
    }
    
    // all processes

    @Override
    public IoStatistics getAllProcessesIo() {
        return null;
    }
    
    @Override
    @Variability(id = AnnotationConstants.VAR_ARBITRARY_PROCESS_DATA)
    public boolean isProcessAlive(int pid) {
        return false;
    }
    
}
