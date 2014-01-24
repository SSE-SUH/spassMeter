package de.uni_hildesheim.sse.system;

import de.uni_hildesheim.sse.codeEraser.annotations.Operation;
import de.uni_hildesheim.sse.codeEraser.annotations.Variability;

/**
 * Defines an interface for requesting process information from the system.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
@Variability(id = AnnotationConstants.VAR_ARBITRARY_PROCESS_DATA)
public interface IProcessDataGatherer {

    /**
     * Returns if the given process is alive.
     * 
     * @param pid the process id 
     * @return <code>true</code> if the process is alive, <code>false</code>
     *   else
     */
    @Variability(id = AnnotationConstants.VAR_ARBITRARY_PROCESS_DATA)
    public boolean isProcessAlive(int pid);
    
    /**
     * Returns the I/O statistics of the given process.
     * 
     * @param pid the process id
     * @return the I/O statistics of the given process, may 
     *   be <b>null</b> if invalid
     */
    @Variability(id = { AnnotationConstants.VAR_ARBITRARY_PROCESS_DATA,
            AnnotationConstants.VAR_IO_DATA }, op = Operation.AND)
    public IoStatistics getProcessIo(int pid);
    
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
    public boolean isNetworkIoDataIncluded(boolean forAll);
    
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
    public boolean isFileIoDataIncluded(boolean forAll);

    /**
     * Returns the memory usage of the given process.
     * 
     * @param pid the process id 
     * @return the memory usage of the given process in bytes, zero or 
     *   negative if invalid
     */
    @Variability(id = { AnnotationConstants.VAR_ARBITRARY_PROCESS_DATA,
            AnnotationConstants.VAR_MEMORY_DATA }, op = Operation.AND)
    public long getProcessMemoryUse(int pid);

    /**
     * Returns the CPU user time ticks of the current process.
     * 
     * @param pid the process id 
     * @return the CPU user time ticks of the current process in nano seconds, 
     *   zero or negative if invalid
     */
    @Variability(id = { AnnotationConstants.VAR_ARBITRARY_PROCESS_DATA,
            AnnotationConstants.VAR_TIME_DATA }, op = Operation.AND)
    public long getProcessUserTimeTicks(int pid);

    /**
     * Returns the CPU kernel time ticks of the current process.
     * 
     * @param pid the process id 
     * @return the CPU kernel time ticks of the current process in nano 
     *   seconds, zero or negative if invalid
     */
    @Variability(id = { AnnotationConstants.VAR_ARBITRARY_PROCESS_DATA,
            AnnotationConstants.VAR_TIME_DATA }, op = Operation.AND)
    public long getProcessKernelTimeTicks(int pid);

    /**
     * Returns the system time ticks of the current process.
     * 
     * @param pid the process id
     * @return the system time ticks of the current process in nano seconds, 
     *   zero or negative if invalid
     */
    @Variability(id = { AnnotationConstants.VAR_ARBITRARY_PROCESS_DATA,
            AnnotationConstants.VAR_TIME_DATA }, op = Operation.AND)
    public long getProcessSystemTimeTicks(int pid);

    /**
     * Returns the load produced by the given process.
     * 
     * @param pid the process id
     * @return the load produced by the given process in percent, zero or 
     *   negative if invalid
     */
    @Variability(id = { AnnotationConstants.VAR_ARBITRARY_PROCESS_DATA,
            AnnotationConstants.VAR_LOAD_DATA }, op = Operation.AND)
    public double getProcessProcessorLoad(int pid);

    /**
     * Returns the I/O statistics for all currently running processes.
     * 
     * @return the I/O statistics for all processes, may 
     *   be <b>null</b> if invalid
     */
    @Variability(id = { AnnotationConstants.VAR_ALL_PROCESSES_DATA, 
            AnnotationConstants.VAR_IO_DATA }, op = Operation.AND)
    public IoStatistics getAllProcessesIo();

}