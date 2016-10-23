package de.uni_hildesheim.sse.system.fallback;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

import de.uni_hildesheim.sse.system.IThreadDataGatherer;

/**
 * Implements a thread data gatherer for the Java platform (using JMX 
 * functionality). Be careful as JMX for CPU time on Java 1.7 pollutes the 
 * memory with long arrays.
 * 
 * @author Holger Eichelberger
 * @since 1.20
 * @version 1.20
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
    
    @Override
    public long[] getAllThreadIds() {
        return THREAD_MX.getAllThreadIds();
    }
    
    @Override
    public long getCpuTime(long threadId) {
        long result = 0;
        if (THREAD_CPU_TICKS_ENABLED) {
            result = THREAD_MX.getThreadCpuTime(threadId);
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
        }
        return count;
    }

    @Override
    public long getAllCpuTime() {
        long count = 0;
        if (THREAD_CPU_TICKS_ENABLED) {
            for (Thread t : Thread.getAllStackTraces().keySet()) {
                count += THREAD_MX.getThreadCpuTime(t.getId());
            }
        }
        return count;
    }

    @Override
    public long getCurrentCpuTime() {
        long result = 0;
        if (THREAD_CPU_TICKS_ENABLED) {
            result = THREAD_MX.getCurrentThreadCpuTime();
        }
        return result;
    }

    @Override
    public long getCurrentId() {
        return Thread.currentThread().getId();
    }
    
}
