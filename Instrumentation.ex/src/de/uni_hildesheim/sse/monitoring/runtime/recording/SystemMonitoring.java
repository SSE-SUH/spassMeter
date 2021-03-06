package de.uni_hildesheim.sse.monitoring.runtime.recording;

import java.util.Timer;
import java.util.TimerTask;

import de.uni_hildesheim.sse.monitoring.runtime.boot.RecorderFrontend;
import de.uni_hildesheim.sse.monitoring.runtime.boot.ResourceType;
import de.uni_hildesheim.sse.monitoring.runtime.configuration.Configuration;
import de.uni_hildesheim.sse.monitoring.runtime.configuration.
    ConfigurationListener;
//import de.uni_hildesheim.sse.monitoring.runtime.instrumentation.lib.IFactory;
import de.uni_hildesheim.sse.monitoring.runtime.recordingStrategies.ProcessData;
import de.uni_hildesheim.sse.monitoring.runtime.recordingStrategies.
    ProcessData.Measurements;
import de.uni_hildesheim.sse.monitoring.runtime.recordingStrategies.ThreadsInfo;
import de.uni_hildesheim.sse.monitoring.runtime.utils.LongLongHashMap;
import de.uni_hildesheim.sse.system.GathererFactory;
import de.uni_hildesheim.sse.system.IMemoryDataGatherer;
import de.uni_hildesheim.sse.system.IProcessorDataGatherer;
import de.uni_hildesheim.sse.system.IThisProcessDataGatherer;
import de.uni_hildesheim.sse.system.IThreadDataGatherer;

/**
 * Does basic monitoring of the underlying operating system.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.20
 */
public class SystemMonitoring {
    
    /**
     * Stores the (default) timer execution period for the load counter.
     */
    public static final long LOAD_COUNTER_PERIOD = 1000;

    /**
     * Stores the memory data gatherer.
     */
    public static final IMemoryDataGatherer MEMORY_DATA_GATHERER 
        = GathererFactory.getMemoryDataGatherer();
    
    /**
     * Stores the thread data gatherer.
     */
    private static final IThreadDataGatherer THREAD_DATA_GATHERER 
        = GathererFactory.getThreadDataGatherer();

    /**
     * Stores the process data gatherer.
     */
    private static final IThisProcessDataGatherer PROCESS_DATA_GATHERER 
        = GathererFactory.getThisProcessDataGatherer();

    /**
     * Stores the processor data gatherer.
     */
    private static final IProcessorDataGatherer PROCESSOR_DATA_GATHERER 
        = GathererFactory.getProcessorDataGatherer();
    
    /**
     * Stores the central timer instance.
     */
    private static Timer timer;
    
    /**
     * Stores the sum of all system loads.
     */
    private static double sysLoadSum = 0;

    /**
     * Stores the local process data object used for collecting minimum and
     * maximum information.
     */
    private static ProcessData processData = ProcessData.POOL.getFromPool();
        
    /**
     * Stores the sum of all JVM loads.
     */
    private static double jvmLoadSum = 0;

    /**
     * Stores the sum of all JVM memory usages.
     */
    private static long jvmMemUseSum = 0;

    /**
     * Stores the sum of all system memory usages.
     */
    private static long sysMemUseSum = 0;
    
    /**
     * Stores the number of load ticks (probes) taken.
     */
    private static long loadTickCount = 0;
    
    /**
     * Account for JVM I/O.
     */
    private static boolean accountForJvmIo = true;
    
    /**
     * Account for System I/O.
     */
    private static boolean accountForSystemIo = true;

    /**
     * Account for process data.
     */
    private static boolean accountForProcessData = true;
    
    /**
     * Stores the number of elapsed out interval counts.
     * Initialized to <code>-1</code> to force output
     * on first opportunity depending on {@link Configuration#getOutInterval()}.
     */
    private static int outIntervalCount = -1;
    
    /**
     * Stores the load and statistics collecting thread.
     */
    private static final LoadCounterTask LOAD_COUNTER 
        = new LoadCounterTask();

    /**
     * Stores the load and statistics collecting thread.
     */
    private static LogEventTask logEventTask = new LogEventTask();
    
    /**
     * Stores the thread timings.
     */
    //private static final Map<Long, Long> THREADS = new HashMap<Long, Long>();
    
    /**
     * Prevents instance creation from outside.
     * 
     * @since 1.00
     */
    private SystemMonitoring() {
    }
    
    /**
     * A runnable for periodically collecting measurements on the system.
     * 
     * @author Holger Eichelberger
     * @since 1.00
     * @version 1.00
     */
    private static class LoadCounterTask extends TimerTask {
        
        /**
         * Stores if this runnable should keep on running and if it was
         * started (<b>null</b> not started, <code>true</code> started and
         * running, <b>false</b> should stop).
         */
        private Boolean run = null;
        
        /**
         * {@inheritDoc}
         * 
         * @since 1.00
         */
        @Override
        public boolean cancel() {
            run = false;
            return super.cancel();
        }
        
        /**
         * Returns if it is running (better if it should not stop).
         * 
         * @return <code>true</code> if it is running, <code>false</code> if
         * it should stop or if it was stopped
         * 
         * @since 1.00
         */
        public boolean isRunning() {
            return Boolean.TRUE == run;
        }
        
        /**
         * Execute the data collection by calling {@link #collectOnce()}.
         */
        public void run() {
            run = true;
            collectOnce();
        }
        
        /**
         * Collect the data.
         * 
         * @since 1.00
         */
        public void collectOnce() {
            Measurements jvm = processData.getJvm();
            Measurements sys = processData.getSystem();
            
            double load;
            long memUse;
            if (jvmLoadSum >= 0) {
                load = PROCESS_DATA_GATHERER.getCurrentProcessProcessorLoad();
                jvmLoadSum += load;
                jvm.setMinMaxLoad(load);
            }

            if (jvmMemUseSum >= 0) {
                memUse = PROCESS_DATA_GATHERER.getCurrentProcessMemoryUse();
                jvmMemUseSum += memUse;
                jvm.setMinMaxMemUse(memUse);
            }

            if (sysLoadSum >= 0) {
                load = PROCESSOR_DATA_GATHERER.getCurrentSystemLoad();
                sysLoadSum += load;
                sys.setMinMaxLoad(load);
            }

            if (sysMemUseSum >= 0) {
                memUse = MEMORY_DATA_GATHERER.getCurrentMemoryUse();
                sysMemUseSum += memUse;
                sys.setMinMaxMemUse(memUse);
            }
            
            int maxOutInterval = Configuration.INSTANCE.getOutInterval();
            // start legacy
            if (maxOutInterval > 0) {
                outIntervalCount++;
                if (outIntervalCount >= maxOutInterval) {
                    outIntervalCount = 0;
                }
                if (0 == outIntervalCount) {
                    RecorderFrontend.instance.printCurrentState();
                }
            }
            // end legacy
            loadTickCount++;
            RecorderFrontend.instance.clearTemporaryData();
        }
        
    }
    
    /**
     * Defines a task for creating regular data events.
     * 
     * @author Holger Eichelberger
     * @since 1.00
     * @version 1.00
     */
    private static class LogEventTask extends TimerTask {
        
        /**
         * Runs the task.
         */
        public void run() {
            RecorderFrontend.instance.printCurrentState();
        }
    }
    
    /**
     * Implements a configuration listener in order to react to selected 
     * configuration changes at runtime.
     * 
     * @author Holger Eichelberger
     * @since 1.00
     * @version 1.00
     */
    private static class SystemConfigurationListener 
        implements ConfigurationListener {
        
        /**
         * Notifies observers about a changed out interval.
         * 
         * @param newValue the new out interval
         * 
         * @since 1.00
         */
        public void notifyOutIntervalChanged(int newValue) {
            if (null != timer) {
                logEventTask.cancel();
                timer.purge();
                logEventTask = new LogEventTask();
                if (newValue >= 500) {
                    timer.schedule(logEventTask, 0, newValue);
                }
            }
        }
    }

    /**
     * Checks JMX capabilities and initializes the threads map.
     */
    static {
        Configuration.INSTANCE.attachListener(
            new SystemConfigurationListener());
    }
    
    /**
     * Returns the number of all thread ticks (CPU and user) consumed by 
     * all threads of this JVM.
     * 
     * @return the number of all ticks
     * 
     * @since 1.00
     */
    public static final long getAllTicks() {
        return THREAD_DATA_GATHERER.getAllCpuTime();
    }

    /**
     * Returns the current ticks, i.e. the consumed thread CPU and thread user 
     * time of the current thread.
     * 
     * @return the current thread ticks
     * 
     * @since 1.00
     */
    public static final long getCurrentTicks() {
        return THREAD_DATA_GATHERER.getCurrentCpuTime();
    }

    /**
     * Returns the ticks of the given thread.
     * 
     * @param threadId the id of the thread to return the ticks for
     * @return the current thread ticks
     * 
     * @since 1.00
     */
    public static final long getTicks(long threadId) {
        return THREAD_DATA_GATHERER.getCpuTime(threadId);
    }
    
    /**
     * Returns the current thread ticks for all threads. The keys contain the
     * thread ids, the values the current thread ticks.
     * 
     * @return the current id-tick mapping
     * 
     * @since 1.00
     */
    public static final LongLongHashMap getAllThreadTicks() {
        LongLongHashMap result = new LongLongHashMap();
        long[] threads = THREAD_DATA_GATHERER.getAllThreadIds();
        if (null != threads) {
            for (int i = 0; i < threads.length; i++) {
                result.put(threads[i], THREAD_DATA_GATHERER.getCpuTime(threads[i]));
            }
        }
        return result;
    }

    /**
     * Returns the ids of all running threads.
     * 
     * @return the ids of all running threads
     * 
     * @since 1.00
     */
    public static long[] getAllThreadIds() {
        return THREAD_DATA_GATHERER.getAllThreadIds();
    }
    
    /**
     * Starts the load and statistics counting thread.
     * 
     * @since 1.00
     */
    public static void startTimer() {
        if (null == timer) {
            Configuration config = Configuration.INSTANCE;
            if (!config.accountForGlobalResource(true, ResourceType.CPU_TIME)) {
                jvmLoadSum = -1;
            }
            if (!config.accountForGlobalResource(true, ResourceType.MEMORY)) {
                jvmMemUseSum = -1;
            }
            if (!config.accountForGlobalResource(false, ResourceType.CPU_TIME)) {
                sysLoadSum = -1;
            }
            if (!config.accountForGlobalResource(false, ResourceType.MEMORY)) {
                sysMemUseSum = -1;
            }
            accountForJvmIo = config.accountForGlobalResource(true, ResourceType.FILE_IO) 
                || config.accountForGlobalResource(true, ResourceType.NET_IO);            
            accountForSystemIo = config.accountForGlobalResource(false, ResourceType.FILE_IO) 
                || config.accountForGlobalResource(false, ResourceType.NET_IO);
            accountForProcessData = sysLoadSum >= 0 || jvmLoadSum >= 0 || jvmMemUseSum >= 0 || sysMemUseSum >= 0;
            accountForProcessData |= accountForJvmIo || accountForSystemIo;
            timer = new Timer();
        }
        if (!LOAD_COUNTER.isRunning()) { // as load counter also cleans up instrumenter, we cannot completely switch off
            timer.schedule(LOAD_COUNTER, 0, LOAD_COUNTER_PERIOD);
        }
        int outInterval = Configuration.INSTANCE.getOutInterval();
        // legacy
        if (outInterval >= 500) {
            timer.schedule(logEventTask, 0, outInterval);
        }
    }
    
    /**
     * Suspends the load and statistics counting thread.
     * 
     * @since 1.00
     */
    public static void stopTimer() {
        if (LOAD_COUNTER.isRunning()) {
            LOAD_COUNTER.cancel();
        }
        logEventTask.cancel();
        if (null != timer) {
            timer.purge();
        }
    }
    
    /**
     * Finishes the load and statistics counting thread.
     * 
     * @since 1.00
     */    
    public static void finishTimer() {
        stopTimer();
        if (null != timer) {
            timer.cancel();
        }
        ProcessData.POOL.release(processData);
    }
    
    /**
     * Returns information about the currently running thread and other 
     * (monitored and unmonitored remaining threads). This information is 
     * relevant
     * 
     * @param tid the thread id
     * @param instanceid the instance id (disabled if <code>0</code>)
     * @return the information object on currently running threads
     * 
     * @since 1.20
     */
    public static ThreadsInfo getThreadInfo(long tid, long instanceid) {
        //Thread current = Thread.currentThread();
        ThreadsInfo result = ThreadsInfo.POOL.getFromPool();
        result.setIds(tid, instanceid);
        // next replaces calculateThreadTimeTicks
        result.setCurrentThreadTicks(THREAD_DATA_GATHERER.getCpuTime(tid));
        //calculateThreadTimeTicks(result);
        return result;
    }

    /**
     * Returns the current thread id. This method was inserted in order to
     * be adjusted in case of native monitoring.
     * 
     * @return the identification of the current thread
     * 
     * @since 1.00
     */
    public static long getCurrentThreadId() {
        // replicated in Recorder.memoryAllocated
        long result;
        if (null == THREAD_DATA_GATHERER) {
            // may happen during initialization
            result = Thread.currentThread().getId();
        } else {
            result = THREAD_DATA_GATHERER.getCurrentId();
        }
        return result;
    }

    /**
     * Creates a process data object with the data collected in this class.
     * 
     * @return a process data object, must be released by 
     *     {@link ProcessData#POOL}, may be <b>null</b> if not available
     * 
     * @since 1.00
     */
    public static ProcessData getProcessData() {
        ProcessData result;
        if (accountForProcessData) {
            result = ProcessData.POOL.getFromPool();
            result.copyFrom(processData);
    
            Measurements jvm = result.getJvm();
            Measurements sys = result.getSystem();
            if (loadTickCount > 0) {
                if (sysLoadSum >= 0) {
                    sys.setAvgLoad(Math.max(0.01, sysLoadSum / loadTickCount));
                }
                if (jvmLoadSum >= 0) {
                    jvm.setAvgLoad(Math.max(0.01, jvmLoadSum / loadTickCount));
                }
                if (jvmMemUseSum >= 0) {
                    jvm.setAvgMemUse(jvmMemUseSum / ((double) loadTickCount));
                }
                if (sysMemUseSum >= 0) {
                    sys.setAvgMemUse(sysMemUseSum / ((double) loadTickCount));
                }
            }
            result.readRemainingFromSystem(PROCESS_DATA_GATHERER, accountForJvmIo, accountForSystemIo);
        } else {
            result = null;
        }
        return result;
    }
    
}
