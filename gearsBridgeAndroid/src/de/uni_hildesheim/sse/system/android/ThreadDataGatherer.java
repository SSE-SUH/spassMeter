package de.uni_hildesheim.sse.system.android;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import android.os.Process;

import de.uni_hildesheim.sse.system.IThreadDataGatherer;

/**
 * Defines a data gatherer for thread data. This data need not to be exact,
 * because Android does not implement JMX (else we could just reuse the default
 * implementation). This class either uses native thread timing (if available)
 * or estimates the thread timing based on the thread priorities.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public class ThreadDataGatherer implements IThreadDataGatherer{

    /**
     * Stores the current threads (usually {@link #getAllThreadIds()} will
     * be called shortly before {@link #getCpuTime(long)}. Then we can reuse
     * the thread list and ignore inaccuracies.
     */
    private Set<Thread> threads;
    
    /**
     * Stores the timestamp of the last snapshot in {@link #threads}.
     */
    private long timestamp = 0;
    
    /**
     * Stores the time range in milliseconds for which a snapshot in 
     * {@link #threads} will be considered as valid.
     */
    private static final long VALID_TIMERANGE = 500;
    
    /**
     * Stores if native timing should be activated.
     */
    private static boolean NATIVE_TIMING = supportsCpuThreadTiming0();
    // does not work on my android as some system calls do not return data
    
    /**
     * Stores the estimated thread times in case that the underlying plattform
     * does not support thread CPU times.
     */
    private Map<Long, Long> estimatedThreadTimes;
    
    /**
     * Returns if the OS supports CPU thread timing.
     * 
     * @return <code>true</code> if the OS supports CPU timing, 
     *     <code>false</code> else
     * 
     * @since 1.00
     */
    private static native boolean supportsCpuThreadTiming0();
    
    /**
     * Returns the CPU time consumed by the given thread.
     * 
     * @param threadId the thread to return the CPU time consumption for
     * @return the CPU time consumption
     * 
     * @since 1.00
     */
    private static native long getCpuThreadTime0(long threadId);
    
    /**
     * Actualizes the snapshot of the threads if needed.
     * 
     * @since 1.00
     */
    private void actualize() {
        long now = System.currentTimeMillis();
        if (0 == timestamp || now - timestamp > VALID_TIMERANGE) {
            threads = Thread.getAllStackTraces().keySet();
            timestamp = now;
        }
    }
    
    /**
     * Returns the CPU time consumed by all threads of this JVM.
     * 
     * @return the consumed CPU time (in nanoseconds), 0 if not available
     * 
     * @since 1.00
     */
    @Override
    public synchronized long getAllCpuTime() {
        return Process.getElapsedCpuTime() * 1000 * 1000;
    }

    /**
     * Returns the identifiers of all currently running threads.
     * 
     * @return the identifiers of all currently running threads, <b>null</b>
     *   if not available
     * 
     * @since 1.00
     */
    @Override
    public synchronized long[] getAllThreadIds() {
        actualize();
        long[] result = new long[threads.size()];
        int i = 0;
        for (Thread t : threads) {
            result[i++] = t.getId();
        }
        return result;
    }

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
    @Override
    public synchronized long getCpuTime(long threadId) {
        long result = -1;
        if (NATIVE_TIMING && null != estimatedThreadTimes) {
            result = getCpuThreadTime0(threadId);
        }
        if (result < 0) {
            // estimate values from thread priority and ensure weakly monotonic
            // increasing times
            final long timelineId = -1;
            long allCpu = getAllCpuTime();
            if (null == estimatedThreadTimes) {
                estimatedThreadTimes = new HashMap<Long, Long>();
            } else {
                long timeline = estimatedThreadTimes.get(timelineId);
                long distributable = allCpu - timeline;
                result = 0;
                actualize();
                double prioSum = 0;
                for (Thread t : Thread.getAllStackTraces().keySet()) {
                    if (t.isAlive() && !t.isInterrupted()) {
                        prioSum += t.getPriority();
                    }
                }
                for (Thread t : Thread.getAllStackTraces().keySet()) {
                    if (t.isAlive() && !t.isInterrupted()) {
                        Long l = estimatedThreadTimes.get(t.getId());
                        if (null == l) {
                            l = 0L;
                        }
                        l += (long) (distributable 
                            * (t.getPriority() / prioSum));
                        estimatedThreadTimes.put(t.getId(), l);
                    }
                }
            }
            estimatedThreadTimes.put(timelineId, allCpu);
            Long l = estimatedThreadTimes.get(threadId);
            if (null == l) {
                result = 0;
            } else {
                result = l;
            }
        }
        return result;
    }

    /**
     * Returns the number of the CPU time consumed by the current thread 
     * in this JVM.
     * 
     * @return the consumed CPU time (in nanoseconds), 0 if not available
     * 
     * @since 1.00
     */
    @Override
    public synchronized long getCurrentCpuTime() {
        return getCpuTime(Thread.currentThread().getId());
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
