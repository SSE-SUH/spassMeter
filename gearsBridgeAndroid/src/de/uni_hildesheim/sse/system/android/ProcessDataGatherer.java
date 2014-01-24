package de.uni_hildesheim.sse.system.android;

import de.uni_hildesheim.sse.codeEraser.annotations.Operation;
import de.uni_hildesheim.sse.codeEraser.annotations.Variability;
import de.uni_hildesheim.sse.system.AnnotationConstants;
import de.uni_hildesheim.sse.system.IProcessDataGatherer;
import de.uni_hildesheim.sse.system.IoStatistics;

/**
 * Defines an interface for requesting process information from the system.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
@Variability(id = AnnotationConstants.VAR_ARBITRARY_PROCESS_DATA)
class ProcessDataGatherer implements IProcessDataGatherer {
    
    // arbitrary process

    /**
     * Returns if the given process is alive.
     * 
     * @param pid the process id (to be system independent we just use strings
     *   even if this may be inefficient)
     * @return <code>true</code> if the process is alive, <code>false</code>
     *   else
     */
    @Variability(id = AnnotationConstants.VAR_ARBITRARY_PROCESS_DATA)
    private static native boolean isProcessAlive0(String pid);
    
    /**
     * Returns the I/O statistics of the given process.
     * 
     * @param pid the process id (to be system independent we just use strings
     *   even if this may be inefficient)
     * @return the I/O statistics of the given process, may 
     *   be <b>null</b> if invalid
     */
    @Variability(id = {AnnotationConstants.VAR_ARBITRARY_PROCESS_DATA, 
            AnnotationConstants.VAR_IO_DATA }, op = Operation.AND)
    private static native IoStatistics getProcessIo0(String pid);

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
    @Variability(id = { AnnotationConstants.VAR_ARBITRARY_PROCESS_DATA,
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
    @Variability(id = { AnnotationConstants.VAR_ARBITRARY_PROCESS_DATA,
            AnnotationConstants.VAR_ALL_PROCESSES_DATA,
            AnnotationConstants.VAR_IO_DATA }, op = Operation.AND)
    private static native boolean isFileIoDataIncluded0(boolean forAll);    
    
    /**
     * Returns the memory usage of the given process.
     * 
     * @param pid the process id (to be system independent we just use strings
     *   even if this may be inefficient)
     * @return the memory usage of the given process in bytes, zero or 
     *   negative if invalid
     */
    @Variability(id = {AnnotationConstants.VAR_ARBITRARY_PROCESS_DATA,
            AnnotationConstants.VAR_ALL_PROCESSES_DATA,
            AnnotationConstants.VAR_MEMORY_DATA }, op = Operation.AND)
    private static native long getProcessMemoryUse0(String pid);

    /**
     * Returns the CPU user time ticks of the current process.
     * 
     * @param pid the process id (to be system independent we just use strings
     *   even if this may be inefficient)
     * @return the CPU user time ticks of the current process in nano seconds, 
     *   zero or negative if invalid
     */
    @Variability(id = {AnnotationConstants.VAR_ARBITRARY_PROCESS_DATA, 
            AnnotationConstants.VAR_TIME_DATA }, op = Operation.AND)
    private static native long getProcessUserTimeTicks0(String pid);

    /**
     * Returns the CPU kernel time ticks of the current process.
     * 
     * @param pid the process id (to be system independent we just use strings
     *   even if this may be inefficient)
     * @return the CPU kernel time ticks of the current process in nano 
     *   seconds, zero or negative if invalid
     */
    @Variability(id = {AnnotationConstants.VAR_ARBITRARY_PROCESS_DATA, 
            AnnotationConstants.VAR_TIME_DATA }, op = Operation.AND)
    private static native long getProcessKernelTimeTicks0(String pid);

    /**
     * Returns the system time ticks of the current process.
     * 
     * @param pid the process id (to be system independent we just use strings
     *   even if this may be inefficient)
     * @return the system time ticks of the current process in nano seconds, 
     *   zero or negative if invalid
     */
    @Variability(id = {AnnotationConstants.VAR_ARBITRARY_PROCESS_DATA, 
            AnnotationConstants.VAR_TIME_DATA }, op = Operation.AND)
    private static native long getProcessSystemTimeTicks0(String pid);

//    private native static long getCurrentProcessCycleTimeTicks(String pid);

    /**
     * Returns the load produced by the given process.
     * 
     * @param pid the process id (to be system independent we just use strings
     *   even if this may be inefficient)
     * @return the load produced by the given process in percent, zero or 
     *   negative if invalid
     */
    @Variability(id = {AnnotationConstants.VAR_ARBITRARY_PROCESS_DATA, 
            AnnotationConstants.VAR_LOAD_DATA }, op = Operation.AND)
    private static native double getProcessProcessorLoad0(String pid);
    
    // all processes

    /**
     * Returns the I/O statistics for all currently running processes.
     * 
     * @return the I/O statistics for all processes, may 
     *   be <b>null</b> if invalid
     */
    @Variability(id = { AnnotationConstants.VAR_ALL_PROCESSES_DATA, 
            AnnotationConstants.VAR_IO_DATA }, op = Operation.AND)
    private static native IoStatistics getAllProcessesIo0(); 

    // arbitrary process

    /**
     * Returns the I/O statistics of the given process.
     * 
     * @param pid the process id (to be system independent we just use strings
     *   even if this may be inefficient)
     * @return the I/O statistics of the given process, may 
     *   be <b>null</b> if invalid
     */
    @Override
    public IoStatistics getProcessIo(String pid) {
        return getProcessIo0(pid);
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
    @Variability(id = { AnnotationConstants.VAR_ARBITRARY_PROCESS_DATA,
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
    @Variability(id = { AnnotationConstants.VAR_ARBITRARY_PROCESS_DATA,
            AnnotationConstants.VAR_ALL_PROCESSES_DATA,
            AnnotationConstants.VAR_IO_DATA }, op = Operation.AND)
    @Override
    public boolean isFileIoDataIncluded(boolean forAll) {
        return isFileIoDataIncluded0(forAll);
    }

    /**
     * Returns the memory usage of the given process.
     * 
     * @param pid the process id (to be system independent we just use strings
     *   even if this may be inefficient)
     * @return the memory usage of the given process in bytes, zero or 
     *   negative if invalid
     */
    @Override
    public long getProcessMemoryUse(String pid) {
        return getProcessMemoryUse0(pid);
    }

    /**
     * Returns the CPU user time ticks of the current process.
     * 
     * @param pid the process id (to be system independent we just use strings
     *   even if this may be inefficient)
     * @return the CPU user time ticks of the current process in nano seconds, 
     *   zero or negative if invalid
     */
    @Override
    public long getProcessUserTimeTicks(String pid) {
        return getProcessUserTimeTicks0(pid);
    }

    /**
     * Returns the CPU kernel time ticks of the current process.
     * 
     * @param pid the process id (to be system independent we just use strings
     *   even if this may be inefficient)
     * @return the CPU kernel time ticks of the current process in nano 
     *   seconds, zero or negative if invalid
     */
    @Override
    public long getProcessKernelTimeTicks(String pid) {
        return getProcessKernelTimeTicks0(pid);
    }

    /**
     * Returns the system time ticks of the current process.
     * 
     * @param pid the process id (to be system independent we just use strings
     *   even if this may be inefficient)
     * @return the system time ticks of the current process in nano seconds, 
     *   zero or negative if invalid
     */
    @Override
    public long getProcessSystemTimeTicks(String pid) {
        return getProcessSystemTimeTicks0(pid);
    }

//    private native static long getCurrentProcessCycleTimeTicks(String pid);

    /**
     * Returns the load produced by the given process.
     * 
     * @param pid the process id (to be system independent we just use strings
     *   even if this may be inefficient)
     * @return the load produced by the given process in percent, zero or 
     *   negative if invalid
     */
    @Override
    public double getProcessProcessorLoad(String pid) {
        return getProcessProcessorLoad0(pid);
    }
    
    // all processes

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
     * Returns if the given process is alive.
     * 
     * @param pid the process id (to be system independent we just use strings
     *   even if this may be inefficient)
     * @return <code>true</code> if the process is alive, <code>false</code>
     *   else
     */
    @Variability(id = AnnotationConstants.VAR_ARBITRARY_PROCESS_DATA)
    public boolean isProcessAlive(String pid) {
        return isProcessAlive0(pid);
    }
    
}
