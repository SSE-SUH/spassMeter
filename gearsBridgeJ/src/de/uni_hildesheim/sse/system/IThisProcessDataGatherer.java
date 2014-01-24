package de.uni_hildesheim.sse.system;

import de.uni_hildesheim.sse.codeEraser.annotations.Operation;
import de.uni_hildesheim.sse.codeEraser.annotations.Variability;

/**
 * Defines an interface for requesting information on the current process, i.e.
 * the JVM from the system.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
@Variability(id = AnnotationConstants.VAR_CURRENT_PROCESS_DATA)
public interface IThisProcessDataGatherer {

    /**
     * Returns the identification the current process.
     * 
     * @return the identification of the current process, may 
     *   be <b>null</b> if invalid
     */
    @Variability(id = AnnotationConstants.VAR_CURRENT_PROCESS_DATA )
    public String getCurrentProcessID();
    
    /**
     * Returns the I/O statistics of the current process.
     * 
     * @return the I/O statistics of the current process, may 
     *   be <b>null</b> if invalid
     */
    @Variability(id = { AnnotationConstants.VAR_CURRENT_PROCESS_DATA,
            AnnotationConstants.VAR_IO_DATA }, op = Operation.AND)
    public IoStatistics getCurrentProcessIo();
    
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
    @Variability(id = { AnnotationConstants.VAR_CURRENT_PROCESS_DATA,
            AnnotationConstants.VAR_ALL_PROCESSES_DATA,
            AnnotationConstants.VAR_IO_DATA }, op = Operation.AND)
    public boolean isFileIoDataIncluded(boolean forAll);

    /**
     * Returns the memory usage of the current process.
     * 
     * @return the memory usage of the current process in bytes, zero or 
     *   negative if invalid
     */
    @Variability(id = { AnnotationConstants.VAR_CURRENT_PROCESS_DATA,
            AnnotationConstants.VAR_MEMORY_DATA }, op = Operation.AND)
    public long getCurrentProcessMemoryUse();

    /**
     * Returns the CPU user time ticks of the current process.
     * 
     * @return the CPU user time ticks of the current process in nano seconds, 
     *   zero or negative if invalid
     */
    @Variability(id = { AnnotationConstants.VAR_CURRENT_PROCESS_DATA,
            AnnotationConstants.VAR_TIME_DATA }, op = Operation.AND)
    public long getCurrentProcessUserTimeTicks();

    /**
     * Returns the CPU kernel time ticks of the current process.
     * 
     * @return the CPU kernel time ticks of the current process in nano 
     *   seconds, zero or negative if invalid
     */
    @Variability(id = { AnnotationConstants.VAR_CURRENT_PROCESS_DATA,
            AnnotationConstants.VAR_TIME_DATA }, op = Operation.AND)
    public long getCurrentProcessKernelTimeTicks();

    /**
     * Returns the system time ticks of the current process.
     * 
     * @return the system time ticks of the current process in nano seconds, 
     *   zero or negative if invalid
     */
    @Variability(id = { AnnotationConstants.VAR_CURRENT_PROCESS_DATA,
            AnnotationConstants.VAR_TIME_DATA }, op = Operation.AND)
    public long getCurrentProcessSystemTimeTicks();

    /**
     * Returns the load produced by the current process.
     * 
     * @return the load produced by the current process in percent, zero or 
     *   negative if invalid
     */
    @Variability(id = { AnnotationConstants.VAR_CURRENT_PROCESS_DATA,
            AnnotationConstants.VAR_LOAD_DATA }, op = Operation.AND)
    public double getCurrentProcessProcessorLoad();
    
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