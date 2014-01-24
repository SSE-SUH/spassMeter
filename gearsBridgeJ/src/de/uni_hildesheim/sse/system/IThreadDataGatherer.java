package de.uni_hildesheim.sse.system;

/**
 * Defines a data gatherer for thread data.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public interface IThreadDataGatherer {
    
    /**
     * Returns the identifiers of all currently running threads.
     * 
     * @return the identifiers of all currently running threads, <b>null</b>
     *   if not available
     * 
     * @since 1.00
     */
    public long[] getAllThreadIds();
    
    /**
     * Returns the CPU time consumed by the given thread.
     * 
     * @param threadId the thread identification of the 
     *   thread to return the CPU time for (should be one returned 
     *   by {@link #getAllThreadIds()}.
     * @return the consumed CPU time (in nanoseconds) if available 
     *   or <code>0</code> if not available
     * 
     * @since 1.00
     */
    public long getCpuTime(long threadId);

    /**
     * Returns the CPU time consumed by all threads of this JVM.
     * 
     * @return the consumed CPU time (in nanoseconds), 0 if not available
     * 
     * @since 1.00
     */
    public long getAllCpuTime();

    /**
     * Returns the number of the CPU time consumed by the current thread 
     * in this JVM.
     * 
     * @return the consumed CPU time (in nanoseconds), 0 if not available
     * 
     * @since 1.00
     */
    public long getCurrentCpuTime();
    
    /**
     * Returns the identification of the current thread.
     * 
     * @return the identification of the current thread
     * 
     * @since 1.00
     */
    public long getCurrentId();
    
}
