package de.uni_hildesheim.sse.system.android;

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
 * @since 1.00
 * @version 1.00
 */
@Variability(id = AnnotationConstants.VAR_CURRENT_PROCESS_DATA)
class ThisProcessDataGatherer implements IThisProcessDataGatherer {
    
    // this process

    /**
     * Returns the identification the current process.
     * 
     * @return the identification of the current process, may 
     *   be <b>null</b> if invalid
     */
    @Variability(id = AnnotationConstants.VAR_CURRENT_PROCESS_DATA )
    private static native String getCurrentProcessID0();
    
    /**
     * Returns the I/O statistics of the current process.
     * 
     * @return the I/O statistics of the current process, may 
     *   be <b>null</b> if invalid
     */
    @Variability(id = {AnnotationConstants.VAR_CURRENT_PROCESS_DATA, 
            AnnotationConstants.VAR_IO_DATA }, op = Operation.AND)
    private static native IoStatistics getCurrentProcessIo0();

    /**
     * Returns weather native network I/O statistics are included, i.e.
     * weather the system provides required capabilities to access the
     * statistics.
     * 
     * @param forAll query this information for all processes, or 
     *   otherways for a single process 
     * 
     * @return <code>true</code> if the information is provided, 
     *    <code>false</code> else
     * 
     * @since 1.00
     */
    @Variability(id = { AnnotationConstants.VAR_CURRENT_PROCESS_DATA,
            AnnotationConstants.VAR_ALL_PROCESSES_DATA,
            AnnotationConstants.VAR_IO_DATA }, op = Operation.AND)
    private static native boolean isNetworkIoDataIncluded0(boolean forAll);
    
    /**
     * Returns weather native file I/O statistics are included, i.e.
     * weather the system provides required capabilities to access the
     * statistics.
     * 
     * @param forAll query this information for all processes, or 
     *   otherways for a single process 
     * 
     * @return <code>true</code> if the information is provided, 
     *    <code>false</code> else
     * 
     * @since 1.00
     */
    @Variability(id = { AnnotationConstants.VAR_CURRENT_PROCESS_DATA,
            AnnotationConstants.VAR_ALL_PROCESSES_DATA,
            AnnotationConstants.VAR_IO_DATA }, op = Operation.AND)
    private static native boolean isFileIoDataIncluded0(boolean forAll);
    
    /**
     * Returns the memory usage of the current process.
     * 
     * @return the memory usage of the current process in bytes, zero or 
     *   negative if invalid
     */
    @Variability(id = {AnnotationConstants.VAR_CURRENT_PROCESS_DATA, 
            AnnotationConstants.VAR_MEMORY_DATA }, op = Operation.AND)
    private static native long getCurrentProcessMemoryUse0();

    /**
     * Returns the CPU user time ticks of the current process.
     * 
     * @return the CPU user time ticks of the current process in nano seconds, 
     *   zero or negative if invalid
     */
    @Variability(id = {AnnotationConstants.VAR_CURRENT_PROCESS_DATA, 
            AnnotationConstants.VAR_TIME_DATA }, op = Operation.AND)
    private static native long getCurrentProcessUserTimeTicks0();

    /**
     * Returns the CPU kernel time ticks of the current process.
     * 
     * @return the CPU kernel time ticks of the current process in nano 
     *   seconds, zero or negative if invalid
     */
    @Variability(id = {AnnotationConstants.VAR_CURRENT_PROCESS_DATA, 
            AnnotationConstants.VAR_TIME_DATA }, op = Operation.AND)
    private static native long getCurrentProcessKernelTimeTicks0();

    /**
     * Returns the system time ticks of the current process.
     * 
     * @return the system time ticks of the current process in nano seconds, 
     *   zero or negative if invalid
     */
    @Variability(id = {AnnotationConstants.VAR_CURRENT_PROCESS_DATA, 
            AnnotationConstants.VAR_TIME_DATA }, op = Operation.AND)
    private static native long getCurrentProcessSystemTimeTicks0();
//    private native static long getCurrentProcessCycleTimeTicks();

    /**
     * Returns the load produced by the current process.
     * 
     * @return the load produced by the current process in percent, zero or 
     *   negative if invalid
     */
    @Variability(id = {AnnotationConstants.VAR_CURRENT_PROCESS_DATA, 
            AnnotationConstants.VAR_LOAD_DATA }, op = Operation.AND)
    private static native double getCurrentProcessProcessorLoad0();
    
    /**
     * Returns the I/O statistics for all currently running processes.
     * 
     * @return the I/O statistics for all processes, may 
     *   be <b>null</b> if invalid
     */
    @Variability(id = { AnnotationConstants.VAR_ALL_PROCESSES_DATA, 
            AnnotationConstants.VAR_IO_DATA }, op = Operation.AND)
    private static native IoStatistics getAllProcessesIo0(); 

    
    // this process

    /**
     * Returns the identification the current process.
     * 
     * @return the identification of the current process, may 
     *   be <b>null</b> if invalid
     */
    @Variability(id = AnnotationConstants.VAR_CURRENT_PROCESS_DATA )
    public String getCurrentProcessID() {
        return getCurrentProcessID0();
    }
    
    /**
     * Returns the I/O statistics of the current process.
     * 
     * @return the I/O statistics of the current process, may 
     *   be <b>null</b> if invalid
     */
    @Override
    public IoStatistics getCurrentProcessIo() {
        return getCurrentProcessIo0();
    }
    
    /**
     * Returns weather native network I/O statistics are included, i.e.
     * weather the system provides required capabilities to access the
     * statistics.
     * 
     * @param forAll query this information for all processes, or 
     *   otherways for a single process 
     * 
     * @return <code>true</code> if the information is provided, 
     *    <code>false</code> else
     * 
     * @since 1.00
     */
    @Variability(id = { AnnotationConstants.VAR_CURRENT_PROCESS_DATA,
            AnnotationConstants.VAR_ALL_PROCESSES_DATA,
            AnnotationConstants.VAR_IO_DATA }, op = Operation.AND)
    @Override            
    public boolean isNetworkIoDataIncluded(boolean forAll) {
        return isNetworkIoDataIncluded0(forAll);
    }
    
    /**
     * Returns weather native file I/O statistics are included, i.e.
     * weather the system provides required capabilities to access the
     * statistics.
     * 
     * @param forAll query this information for all processes, or 
     *   otherways for a single process 
     * 
     * @return <code>true</code> if the information is provided, 
     *    <code>false</code> else
     * 
     * @since 1.00
     */
    @Variability(id = { AnnotationConstants.VAR_CURRENT_PROCESS_DATA,
            AnnotationConstants.VAR_ALL_PROCESSES_DATA,
            AnnotationConstants.VAR_IO_DATA }, op = Operation.AND)
    @Override
    public boolean isFileIoDataIncluded(boolean forAll) {
        return isFileIoDataIncluded0(forAll);
    }

    /**
     * Returns the memory usage of the current process.
     * 
     * @return the memory usage of the current process in bytes, zero or 
     *   negative if invalid
     */
    @Override
    public long getCurrentProcessMemoryUse() {
        return getCurrentProcessMemoryUse0();
    }

    /**
     * Returns the CPU user time ticks of the current process.
     * 
     * @return the CPU user time ticks of the current process in nano seconds, 
     *   zero or negative if invalid
     */
    @Override
    public long getCurrentProcessUserTimeTicks() {
        return getCurrentProcessUserTimeTicks0();
    }

    /**
     * Returns the CPU kernel time ticks of the current process.
     * 
     * @return the CPU kernel time ticks of the current process in nano 
     *   seconds, zero or negative if invalid
     */
    @Override
    public long getCurrentProcessKernelTimeTicks() {
        return getCurrentProcessKernelTimeTicks0();
    }

    /**
     * Returns the system time ticks of the current process.
     * 
     * @return the system time ticks of the current process in nano seconds, 
     *   zero or negative if invalid
     */
    @Override
    public long getCurrentProcessSystemTimeTicks() {
        return getCurrentProcessSystemTimeTicks0();
    }
    
//    private native static long getCurrentProcessCycleTimeTicks();

    /**
     * Returns the load produced by the current process.
     * 
     * @return the load produced by the current process in percent, zero or 
     *   negative if invalid
     */
    @Override
    public double getCurrentProcessProcessorLoad() {
        return getCurrentProcessProcessorLoad0();
    }
    
    /**
     * Returns the I/O statistics for all currently running processes.
     * 
     * @return the I/O statistics for all processes, may 
     *   be <b>null</b> if invalid
     */
    @Override
    public IoStatistics getAllProcessesIo() {
        return getAllProcessesIo0();
    }

    /**
     * Returns the identification of the current thread.
     * 
     * @return the identification of the current thread
     * 
     * @since 1.00
     */
    public long getCurrentId() {
        return Thread.currentThread().getId();
    }

}
