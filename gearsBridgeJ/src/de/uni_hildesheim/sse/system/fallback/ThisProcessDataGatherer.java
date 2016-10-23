package de.uni_hildesheim.sse.system.fallback;

import de.uni_hildesheim.sse.codeEraser.annotations.Operation;
import de.uni_hildesheim.sse.codeEraser.annotations.Variability;
import de.uni_hildesheim.sse.system.AnnotationConstants;
import de.uni_hildesheim.sse.system.IThisProcessDataGatherer;
import de.uni_hildesheim.sse.system.IoStatistics;

/**
 * Implements a class for requesting information on the current process, i.e.
 * the JVM from the system.
 * 
 * @author Holger Eichelberger
 * @since 1.20
 * @version 1.20
 */
@Variability(id = AnnotationConstants.VAR_CURRENT_PROCESS_DATA)
class ThisProcessDataGatherer implements IThisProcessDataGatherer {
    
    @Override
    @Variability(id = AnnotationConstants.VAR_CURRENT_PROCESS_DATA )
    public String getCurrentProcessID() {
        return null;
    }
    
    @Override
    public IoStatistics getCurrentProcessIo() {
        return null;
    }
    
    @Variability(id = { AnnotationConstants.VAR_CURRENT_PROCESS_DATA,
            AnnotationConstants.VAR_ALL_PROCESSES_DATA,
            AnnotationConstants.VAR_IO_DATA }, op = Operation.AND)
    @Override            
    public boolean isNetworkIoDataIncluded(boolean forAll) {
        return false;
    }
    
    @Variability(id = { AnnotationConstants.VAR_CURRENT_PROCESS_DATA,
            AnnotationConstants.VAR_ALL_PROCESSES_DATA,
            AnnotationConstants.VAR_IO_DATA }, op = Operation.AND)
    @Override
    public boolean isFileIoDataIncluded(boolean forAll) {
        return false;
    }

    @Override
    public long getCurrentProcessMemoryUse() {
        return -1;
    }

    @Override
    public long getCurrentProcessUserTimeTicks() {
        return -1;
    }

    @Override
    public long getCurrentProcessKernelTimeTicks() {
        return -1;
    }

    @Override
    public long getCurrentProcessSystemTimeTicks() {
        return -1;
    }
    
    @Override
    public double getCurrentProcessProcessorLoad() {
        return -1;
    }
    
    @Override
    public IoStatistics getAllProcessesIo() {
        return null;
    }
    
}
