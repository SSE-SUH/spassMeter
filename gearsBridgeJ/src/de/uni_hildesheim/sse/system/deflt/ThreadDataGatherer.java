package de.uni_hildesheim.sse.system.deflt;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

import de.uni_hildesheim.sse.system.IThreadDataGatherer;

/**
 * Implements a thread data gatherer for the Java platform (using JMX 
 * functionality).
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public class ThreadDataGatherer implements IThreadDataGatherer {

    /**
     * Stores the JMX thread bean. [Convenience]
     */
    private static final ThreadMXBean THREAD_MX 
        = ManagementFactory.getThreadMXBean();

    /**
     * Stores whether thread CPU ticks are supported by the JMX thread bean.
     */
    private static final boolean THREAD_CPU_TICKS_ENABLED;

    /**
     * Stores whether an absent implementation of thread times should be 
     * replaced by equally distributed process times.
     */
    private static final boolean SUBSTITUTE_BY_PROCESS = true;
    
    /**
     * Checks JMX capabilities and initializes the threads map.
     */
    static {
        boolean useThreadCpuTicks = false;
        if (THREAD_MX.isThreadCpuTimeSupported()) {
            if (!THREAD_MX.isThreadCpuTimeEnabled()) {
                try {
                    THREAD_MX.setThreadCpuTimeEnabled(true);
                    useThreadCpuTicks = true;
                } catch (UnsupportedOperationException e) {
                } catch (SecurityException e) {
                }
            } else {
                useThreadCpuTicks = true;
            }
        }
        THREAD_CPU_TICKS_ENABLED = useThreadCpuTicks;
    }
    
    /**
     * Returns the identifiers of all currently running threads.
     * 
     * @return the identifiers of all currently running threads, <b>null</b>
     *   if not available
     * 
     * @since 1.00
     */
    public long[] getAllThreadIds() {
        return THREAD_MX.getAllThreadIds();
    }
    
    /**
     * Returns the CPU time consumed by the given thread.
     * 
     * @param threadId the thread identification of the 
     *   thread to return the CPU time for (should be one returned 
     *   by {@link #getAllThreadIds()}.
     * @return the CPU time if available or <code>0</code> if not available
     * 
     * @since 1.00
     */
    public long getCpuTime(long threadId) {
        long result;
        if (THREAD_CPU_TICKS_ENABLED) {
            result = THREAD_MX.getThreadCpuTime(threadId);
        } else {
            result = 
                (ThisProcessDataGatherer.getCurrentProcessKernelTimeTicks0() 
                + ThisProcessDataGatherer.getCurrentProcessUserTimeTicks0()) 
                / THREAD_MX.getThreadCount();
        }
        return result;
    }

    /**
     * Returns the number of the CPU time consumed by all threads of this JVM.
     * 
     * @return the number of all ticks, <code>0</code> if not available
     * 
     * @since 1.00
     */
    public long getAllTicks() {
        long count = 0;
        if (THREAD_CPU_TICKS_ENABLED) {
            for (Thread t : Thread.getAllStackTraces().keySet()) {
                count += THREAD_MX.getThreadCpuTime(t.getId());
            }
        } else if (SUBSTITUTE_BY_PROCESS) {
            return ThisProcessDataGatherer.getCurrentProcessKernelTimeTicks0() 
                + ThisProcessDataGatherer.getCurrentProcessUserTimeTicks0();
        }
        return count;
    }

    /**
     * Returns the CPU time consumed by all threads of this JVM.
     * 
     * @return the consumed CPU time (in nanoseconds), <code>0</code> if 
     *     not available
     * 
     * @since 1.00
     */
    public long getAllCpuTime() {
        long count = 0;
        if (THREAD_CPU_TICKS_ENABLED) {
            for (Thread t : Thread.getAllStackTraces().keySet()) {
                count += THREAD_MX.getThreadCpuTime(t.getId());
            }
        } else if (SUBSTITUTE_BY_PROCESS) {
            count = ThisProcessDataGatherer.getCurrentProcessKernelTimeTicks0() 
                + ThisProcessDataGatherer.getCurrentProcessUserTimeTicks0();
        }
        return count;
    }

    /**
     * Returns the number of the CPU time consumed by the current thread 
     * in this JVM.
     * 
     * @return the consumed CPU time (in nanoseconds), 0 if not available
     * 
     * @since 1.00
     */
    public long getCurrentCpuTime() {
        long result = 0;
        if (THREAD_CPU_TICKS_ENABLED) {
            result = THREAD_MX.getCurrentThreadCpuTime();
        } else if (SUBSTITUTE_BY_PROCESS) {
            return (ThisProcessDataGatherer.getCurrentProcessKernelTimeTicks0() 
                + ThisProcessDataGatherer.getCurrentProcessUserTimeTicks0()) 
                / THREAD_MX.getThreadCount();
        }
        return result;
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
