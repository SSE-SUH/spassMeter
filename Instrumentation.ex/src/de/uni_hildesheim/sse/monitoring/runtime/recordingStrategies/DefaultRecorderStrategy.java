package de.uni_hildesheim.sse.monitoring.runtime.recordingStrategies;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeSet;
import java.util.logging.Level;

import de.uni_hildesheim.sse.codeEraser.annotations.Variability;
import de.uni_hildesheim.sse.monitoring.runtime.AnnotationConstants;
import de.uni_hildesheim.sse.monitoring.runtime.annotations.Helper;
import de.uni_hildesheim.sse.monitoring.runtime.annotations.TimerState;
import de.uni_hildesheim.sse.monitoring.runtime.boot.DebugState;
import de.uni_hildesheim.sse.monitoring.runtime.boot.MonitoringGroupSettings;
import de.uni_hildesheim.sse.monitoring.runtime.boot.ResourceType;
import de.uni_hildesheim.sse.monitoring.runtime.boot.StreamType;
import de.uni_hildesheim.sse.monitoring.runtime.configuration.Configuration;
import de.uni_hildesheim.sse.monitoring.runtime.configuration.
    MonitoringGroupConfiguration;
import de.uni_hildesheim.sse.monitoring.runtime.plugins.
    MonitoringGroupBurstChangeListener;
import de.uni_hildesheim.sse.monitoring.runtime.plugins.
    MonitoringGroupChangeListener;
import de.uni_hildesheim.sse.monitoring.runtime.plugins.
    MonitoringGroupCreationListener;
import de.uni_hildesheim.sse.monitoring.runtime.plugins.PluginRegistry;
import de.uni_hildesheim.sse.monitoring.runtime.plugins.TimerChangeListener;
import de.uni_hildesheim.sse.monitoring.runtime.plugins.ValueChangeListener;
import de.uni_hildesheim.sse.monitoring.runtime.plugins.ValueType;
import de.uni_hildesheim.sse.monitoring.runtime.plugins.internal.Cleanup;
import de.uni_hildesheim.sse.monitoring.runtime.plugins.internal.
    InternalPluginRegistry;
import de.uni_hildesheim.sse.monitoring.runtime.utils.LongHashMap;
import de.uni_hildesheim.sse.monitoring.runtime.utils.LongHashMap.MapElement;
import de.uni_hildesheim.sse.monitoring.runtime.utils.LongLongHashMap;
import de.uni_hildesheim.sse.monitoring.runtime.wrap.
    InstrumentedFileOutputStream;

/**
 * Implements the default recorder strategy for in-memory recording. Please
 * note that in this class and dependent classes no local access to threads
 * or timing calculation should be done, because this and dependent classes
 * may be executed remotely. Thus, any relevant client-related information
 * must be passed in by appropriate parameters.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
@Variability(id = AnnotationConstants.STRATEGY_DEFAULT)
public class DefaultRecorderStrategy extends AbstractRecorderStrategy {

    /**
     * Defines whether debugging output should be enabled.
     */
    public static final boolean ENABLE_DEBUG = false;
    
    // HINT: Do always check first if recording element is existent
    // and assign then to configuration... otherways, also unassignable
    // general resources are assigned to configurations
    
    /**
     * Stores the recording element for the program itself, i.e. all unassigned
     * recordings.
     */
    private RecorderElement programRecord = null;

    /**
     * Stores the pseudo recording element for the blocking monitoring overhead,
     * i.e. exclusion of monitoring.
     */
    private RecorderElement excluded;
    
    /**
     * Stores the relation among thread id and information collected on 
     * running threads.
     */
    private final LongHashMap<ThreadData> threads;
    
    /**
     * Stores information about the timers.
     */
    private final Map<String, TimerInfo> timers;
    
    /**
     * Stores the formatter for output formatting.
     */
    private ResultFormatter formatter;
    
    /**
     * Stores the output log stream. The attribute is valid only for the 
     * lifetime of this object.
     */
    private OutputStream logStream;
    
    /**
     * Stores if the formatter was configured.
     */
    private boolean formatterConfigured;

    /**
     * Internal singleton instance passed to the plugin event mechanism
     * in case of new system measurement information.
     */
    private ProcessData.Measurements systemNotificationInstance = null;    

    /**
     * Internal singleton instance passed to the plugin event mechanism
     * in case of new JVM measurement information.
     */
    private ProcessData.Measurements jvmNotificationInstance = null;

    /**
     * Creates the recorder strategy.
     * 
     * @param formatter the formatter for printing the results
     * 
     * @since 1.00
     */
    public DefaultRecorderStrategy(ResultFormatter formatter) {
        super(new StrategyStorage());
        this.formatter = formatter;
        MonitoringGroupConfiguration conf 
            = MonitoringGroupConfiguration.DEFAULT;
        MonitoringGroupSettings settings = 
            MonitoringGroupSettings.getFromPool();
        settings.setBasics(null, conf.getDebug(), conf.getGroupAccounting(), 
            conf.getResources()); // do not set multi as irrelevant here

        programRecord = getStorage().create(conf, true);
        getStorage().registerDefaultRecorderElement(Helper.PROGRAM_ID, 
            programRecord);
        if (Configuration.INSTANCE.accountExcluded()) {
            registerForRecording(getClass().getName(), Helper.EXCLUDED_ID, 
                conf, settings);
            excluded = getRecorderElement(Helper.EXCLUDED_ID);
        } else {
            // no registration
            excluded = new BlockingRecorderElement();
        }
        
        // notification done externally!
        registerForRecording(getClass().getName(), Helper.RECORDER_ID, 
            conf, settings);

        MonitoringGroupSettings.release(settings);
        // listener notification done implicitly by RecorderElementMap
        threads = new LongHashMap<ThreadData>();
        timers = new HashMap<String, TimerInfo>();
    }

    /**
     * Notifies this strategy that time recording should be stopped (first 
     * stage of stopping the recorder).
     * 
     * @param now the current time in nano seconds
     * @param threadInfo the information on the current thread and its timing
     * @return <code>true</code> if the threadsInfo object should be released 
     *     to the pool, <code>false</code> if it should not be released
     * 
     * @since 1.00
     */
    @Override
    public boolean stopTimeRecording(long now, ThreadsInfo threadInfo) {
        //handleAggregatedSystemTime(now, threadInfo, null, false, 
        //    GroupAccountingType.INDIRECT 
        //    == Configuration.INSTANCE.getGroupAccountingType());
        programRecord.stopTimeRecording(now, 
            threadInfo.getCurrentThreadTicks(), -1);
        long id = threadInfo.getCurrentThreadId();
        ThreadData data = threads.get(id);
        if (null != data) {
            data.stopTimeRecording(System.nanoTime(), 
                threadInfo.getCurrentThreadTicks());
        }
        return true; // release always
    }
    
    /**
     * Notifies this strategy that recording should be stopped (second stage 
     * of stopping the recorder).
     * 
     * @param now the current time in nano seconds
     * @param threadId the id of the current thread
     * @param curCpuTimes the current thread ticks
     * @return <code>true</code> if the curCpuTime object should be released 
     *     to the pool, <code>false</code> if it should not be released
     * 
     * @since 1.00
     */
    @Override
    public boolean finishRecording(long now, long threadId, 
        LongLongHashMap curCpuTimes) {
        MonitoringGroupChangeListener listener = 
            PluginRegistry.getMonitoringGroupChangeListener();
        
        // finish unfinished threads
        long elapsedCpu = 0;
        for (LongHashMap.MapElement<ThreadData> element 
            : threads.entries()) {
            ThreadData data = element.getValue();
            long id = element.getKey();
            long ticks = 0;
            if (curCpuTimes.containsKey(id)) {
                ticks = curCpuTimes.get(id);
                data.end(ticks);
            } // thread not running
            elapsedCpu += data.getTotalCpu();
        }

        boolean consProgram = null != programRecord 
            && programRecord.accountResource(ResourceType.CPU_TIME);
        if (consProgram) {
            // adjust overhead time
            programRecord.setCpuTimeTicks(elapsedCpu);
            if (null != listener) {
                listener.monitoringGroupChanged(programRecord);
            }
        }
        return true; // release always
    }
    
    /**
     * Notifies this strategy that recording should be start.
     * 
     * @param now the current time in nano seconds
     * @param threadId the thread id of the executing (main) thread
     * @param threadTicks the thread ticks for <code>threadId</code>
     * 
     * @since 1.00
     */
    @Override
    public void startRecording(long now, long threadId, long threadTicks) {
        programRecord.startTimeRecording(now, threadTicks, 
            threadId);
        ThreadData data = threads.get(threadId);
        if (null != data) {
            data.startTimeRecording(System.nanoTime(), threadTicks);
        }
    }
    
    /**
     * Implements a comparator for thread data entries (for output).
     * 
     * @author Holger Eichelberger
     * @since 1.00
     * @version 1.00
     */
    private static class ThreadDataMapElementComparator 
        implements Comparator<MapElement<ThreadData>> {

        /**
         * Compares two given map entries.
         * 
         * @param o1 the first entry to be compared
         * @param o2 the second entry
         * @return <code>-1</code> if <code>o1</code> is considered as smaller
         *   than <code>o2</code>, <code>0</code> if both are considered equal.
         *   <code>1</code> otherways
         */
        @Override
        public int compare(MapElement<ThreadData> o1,
            MapElement<ThreadData> o2) {
            long k1 = o1.getKey();
            long k2 = o2.getKey();
            return (k1 < k2 ? -1 : (k1 == k2 ? 0 : 1));
        }
        
    }

    /**
     * Implements a comparator for recorder element mappings (for output).
     * 
     * @author Holger Eichelberger
     * @since 1.00
     * @version 1.00
     */
    private static class RecorderElementEntryComparator 
        implements Comparator<Map.Entry<String, RecorderElement>> {

        /**
         * Compares two given map entries.
         * 
         * @param o1 the first entry to be compared
         * @param o2 the second entry
         * @return <code>-1</code> if <code>o1</code> is considered as smaller
         *   than <code>o2</code>, <code>0</code> if both are considered equal.
         *   <code>1</code> otherways
         */
        @Override
        public int compare(Entry<String, RecorderElement> o1,
            Entry<String, RecorderElement> o2) {
            return o1.getKey().compareTo(o2.getKey());
        }
        
    }
    
    /**
     * Configures the formatter for output based on the data in 
     * {@link Configuration} and stores an output file instance in 
     * {@link #logStream}.
     * 
     * @since 1.00
     */
    private void configureFormatter() {
        if (!formatterConfigured) {
            Configuration config = Configuration.INSTANCE;
    
            PrintStream out = System.out;
            File outFile = config.getOutFile();
            if (null != outFile) {
                Configuration.LOG.info("Writing results to " 
                    + outFile.getAbsolutePath());
                try {
                    FileOutputStream fos;
                    if (config.recordOverhead()) {
                        fos = new InstrumentedFileOutputStream(outFile, 
                            Helper.RECORDER_ID);
                    } else {
                        fos = new FileOutputStream(outFile);
                    }
                    logStream = new BufferedOutputStream(fos);
                    out = new PrintStream(logStream);
                } catch (java.io.IOException ioe) {
                    System.err.println(ioe.getMessage());
                }
            }
// TODO printData.showPercentages = !(outFile.endsWith(".tsv"));
            formatter.configure(out, programRecord, false);
            formatterConfigured = true;
        }
    }

    /**
     * Emits the values collected so far.
     * 
     * @param pData process data
     * @return <code>true</code> if the data object should be released 
     *     to the pool, <code>false</code> if it should not be released
     * 
     * @since 1.00
     */
    @Override
    public boolean printStatistics(ProcessData pData) {
        if (Configuration.INSTANCE.programUseFromJvm()) {
            programRecord.updateMemoryFreedFromJvm();
        }
        // do not instrument here because usually run outside recording
        configureFormatter();
        RecorderElementMap recorderElements = getRecorderElements();

        if (Configuration.INSTANCE.getOutInterval() > 0) {
            formatter.printCurrentStateStatisticsFooter(getRecorderElements(), 
                programRecord, null);
        }
        
        ConfigurationToName conf2Name = 
            recorderElements.getConfigurationMapping();
        formatter.setProcessData(pData);
        
        formatter.printHeadline();
        if (!threads.isEmpty()) { // always not empty
            formatter.printInfo(ResultFormatter.InfoCategory.THREADED);
            TreeSet<MapElement<ThreadData>> threadMapSet 
                = new TreeSet<MapElement<ThreadData>>(
                    new ThreadDataMapElementComparator());
            for (MapElement<ThreadData> entry 
                : threads.entries()) {
                threadMapSet.add(entry);
            }
            Iterator<MapElement<ThreadData>> iter2 = threadMapSet.iterator();
            while (iter2.hasNext()) {
                LongHashMap.MapElement<ThreadData> entry = iter2.next();
                ThreadData data = entry.getValue();
                if (data.isEnded()) {
                    formatter.printThreadData(String.valueOf(entry.getKey()), 
                        entry.getValue());
                }
            }
        }
        
        formatter.printIndividual("Program", programRecord);
        TreeSet<Map.Entry<String, RecorderElement>> recorderElementSet = 
            new TreeSet<Map.Entry<String, RecorderElement>>(
                new RecorderElementEntryComparator());
        recorderElementSet.addAll(recorderElements.idToRecordingSet());
        Iterator<Map.Entry<String, RecorderElement>> iter 
            = recorderElementSet.iterator();
        while (iter.hasNext()) {
            Map.Entry<String, RecorderElement> entry = iter.next();
            RecorderElement elt = entry.getValue();
            if (elt.wasRecorded() && elt.isVisible()
                && !Helper.RECORDER_ID.equals(entry.getKey())) {
                formatter.printIndividual(entry.getKey(), elt);
            }
        }
        formatter.printInfo(ResultFormatter.InfoCategory.BREAKDOWN);
        iter = recorderElementSet.iterator();
        
        formatter.printProcessStatistics();
        formatter.printCompare("JVM vs. System", null, true);
        while (iter.hasNext()) {
            Map.Entry<String, RecorderElement> entry = iter.next();
            RecorderElement elt = entry.getValue();
            if ((elt.wasRecorded() && elt.isVisible()) 
                || Helper.RECORDER_ID.equals(entry.getKey())) {
                formatter.printCompare(entry.getKey() + " vs. sys", elt, true);
                formatter.printCompare(entry.getKey() + " vs. jvm", elt, false);
            }
        }

        formatter.printInfo(ResultFormatter.InfoCategory.CONFIGURATIONS);
        recorderElementSet.clear();
        recorderElementSet.addAll(recorderElements.configurationToRecording());
        iter = recorderElementSet.iterator();
        String confSeparator = ", ";
        //printData.out.println(conf2Name.configurationHeadline(confSeparator));

        while (iter.hasNext()) {
            Map.Entry<String, RecorderElement> entry = iter.next();
            String conf = entry.getKey();
            RecorderElement elt = entry.getValue();
            if (elt.wasRecorded()) {
                String confText 
                    = recorderElements.configurationKeyToString(conf, true);
                if (null != confText) {
                    String readable = 
                        conf2Name.formatConfiguration(conf, confSeparator);
                    String description = confText + " " + readable;
                    formatter.printIndividual(description, elt);
                    formatter.printCompare(description 
                        + " vs. sys", elt, true);
                    formatter.printCompare(description 
                        + " vs. jvm", elt, false);
                    for (int c = 0; c < elt.getContributingSize(); c++) {
                        RecorderElement contr = elt.getContributing(c);
                        if (null != contr) {
                            String cName = conf2Name.getName(c);
                            formatter.printIndividual(cName, contr);
                        }
                    }
                }
            }
        }
        
        if (recorderElements.isEmpty()) {
            System.err.println("No class registered for statistics " 
                + "calculation. It might be that the instrumentation agent is " 
                + "not running.");
        }
        formatter.clear();
        return true; // release always
    }
    
    /**
     * Called as the last event from the monitored program. This is after 
     * {@link #finishRecording} and optionally
     * {@link #printStatistics}. This method closes {@link #logStream} if it
     * was opened before.
     * 
     * @since 1.00
     */
    @Override
    public void endSystem() {
        if (null != logStream) {
            try {
                logStream.close();
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        }
    }
    
    /**
     * Notifies the recorder about a method being called.
     * 
     * @param recId a unique identification where to assign this event to
     * @param now the current system time in nano seconds
     * @param threadInfo timing information on the currently running thread
     * @param exclude exclude from monitoring
     * @return <code>true</code> if the threadsInfo object should be released 
     *     to the pool, <code>false</code> if it should not be released
     * 
     * @since 1.00
     */
    @Override
    public boolean enter(String recId, long now, ThreadsInfo threadInfo, 
        boolean exclude) {
        MonitoringGroupChangeListener listener 
            = PluginRegistry.getMonitoringGroupChangeListener();
        // search for internal thread info / stack - creation / activation 
        // is needed by assignByStackTrace
        long id = threadInfo.getCurrentThreadId();
        long threadTicks = threadInfo.getCurrentThreadTicks();
        ThreadData threadData = getThread(id, threadTicks, now);
        // local aggregation to recId as specified in scope definition
        RecorderElement elt = assignByStackTrace(recId, null, 
            threadInfo.getCurrentThreadId(), exclude);
        // it would be good but due to remote recording...
        // record time as late as possible, i.e. as close as possible to the 
        // method entry, so recording is not included
        boolean indirect = false;
        if (null != elt) {
            indirect = elt.isIndirectAccounting();
            if (!indirect && null != threadData) {
                // disable top if not indirect - implied if indirect
                RecorderElement top = threadData.top();
                if (null != top && top != elt 
                    && top.accountResource(ResourceType.CPU_TIME)) {
                    top.stopTimeRecording(now, threadTicks, id);
                }
            }
            threadData.push(elt);

            if (elt.accountResource(ResourceType.CPU_TIME)) {
                if (elt.startTimeRecording(now, threadTicks, id)) {
                    if (isVariabilityDetectionEnabled()) {
                        getRecorderElements().enterConfiguration(recId);
                    }
                }
                if (null != listener) {
                    listener.monitoringGroupChanged(elt);
                }
                // local aggregation to configuration of running groups
                RecorderElement cElt = getRecorderElements()
                    .getCurrentConfigurationRecord(recId);
                if (null != cElt 
                    && cElt.accountResource(ResourceType.CPU_TIME)) {
                    cElt.startTimeRecording(now, threadTicks, id);
                    if (null != listener) {
                        listener.configurationChanged(elt);
                    }
                }
                if (ENABLE_DEBUG) {
                    log(elt, cElt, DebugState.METHOD_ENTER, now, 
                        ResourceType.CPU_TIME);
                }
            }
        } else {
            indirect = false;
        }
        
        if (null != threadData) {
            threadData.startTimeRecording(now, 
                threadInfo.getCurrentThreadTicks());
        }
        return true; // release always
    }
    
    /**
     * Notifies that the following values (if not further calls 
     * to this method or other recording ids occur) should be assigned
     * to the given <code>recId</code>.
     * 
     * @param recId the recording id (may be also the variability prefix)
     * @param enter <code>true</code> if this is a method enter event, 
     *   <code>false</code> else
     * @param now the current system time
     * @param threadInfo information on the current thread
     * @return <code>true</code> if the threadsInfo object should be released 
     *     to the pool, <code>false</code> if it should not be released
     * 
     * @since 1.00
     */
    @Override
    public boolean assignAllTo(String recId, boolean enter, long now, 
        ThreadsInfo threadInfo) {
        recId = getRecorderElements().getPossibleAggregatedRecorderId(recId);
        if (enter) {
            enter(recId, now, threadInfo, false);
        } else {
            exit(recId, now, threadInfo, false);
        }
        return true;
    }

    /**
     * Notifies the recorder about a method being called.
     * 
     * @param recId a unique identification where to assign this event to
     * @param now the current time in nano seconds 
     * @param threadInfo timing information on the currently running thread
     * @param exclude exclude from monitoring
     * @return <code>true</code> if the threadsInfo object should be released 
     *     to the pool, <code>false</code> if it should not be released
     * 
     * @since 1.00
     */
    @Override
    public boolean exit(String recId, long now, ThreadsInfo threadInfo, 
        boolean exclude) {

        MonitoringGroupChangeListener listener 
            = PluginRegistry.getMonitoringGroupChangeListener();
        RecorderElement elt = assignByStackTrace(recId, null, 
            threadInfo.getCurrentThreadId(), exclude);
        long id = threadInfo.getCurrentThreadId();
        long threadTicks = threadInfo.getCurrentThreadTicks();
        ThreadData threadData = threads.get(id);
        // local aggregation to recId as specified in scope definition
        if (null != elt) {
            boolean indirect = elt.isIndirectAccounting();
            //handleAggregatedSystemTime(now, threadInfo, recId, false, 
            //    elt.isIndirectAccounting());
            if (elt.accountResource(ResourceType.CPU_TIME) && (!indirect 
                || (indirect && !threadData.isOnStack(elt, false)))) {
                elt.stopTimeRecording(now, threadTicks, id);

                if (null != listener) {
                    listener.monitoringGroupChanged(elt);
                }
                // local aggregation to configuration of running groups
                RecorderElement cElt = getRecorderElements()
                    .getCurrentConfigurationRecord(recId);
                if (null != cElt 
                    && cElt.accountResource(ResourceType.CPU_TIME)) {
                    cElt.stopTimeRecording(now, threadTicks, id);
                    if (null != listener) {
                        listener.configurationChanged(cElt);
                    }
                }
                if (ENABLE_DEBUG) {
                    log(elt, cElt, DebugState.METHOD_EXIT, now, 
                        ResourceType.CPU_TIME);
                }
            }
            if (null != threadData) {
                threadData.pop(elt);
                if (!indirect) {
                    // reenable top if not indirect - implied if indirect
                    RecorderElement top = threadData.top();
                    if (null != top && top != elt && top.accountResource(
                        ResourceType.CPU_TIME)) {
                        top.startTimeRecording(now, threadTicks, id);
                    }
                }
            }
        }
        if (null != threadData) {
            threadData.stopTimeRecording(now, threadTicks);
        }
        return true; // release always
    }

    /**
     * Registers a thread id with a recording id. This method is used to
     * assign unregistered method calls to monitored threads as well as to
     * register system threads
     * 
     * @param threadId the thread id to be recorded
     * @param newId <ul>
     *   <li>if positive, the new thread</li>
     *   <li>if {@link #REGISTER_FORCE} the thread specified by 
     *     <code>threadId</code> and <code>threadTicks</code> is registered
     *     with the recorder</li>
     *   <li>if {@link #REGISTER_END} the thread specified by 
     *     <code>threadId</code> and <code>threadTicks</code> is marked as 
     *     terminated in the recorder</li>
     *   </ul>
     * @param threadTicks the ticks of <code>threadId</code>
     * @param now the current time in nano seconds
     * 
     * @since 1.00
     */
    @Override
    public void register(long threadId, long newId, long threadTicks, 
        long now) {
        if (REGISTER_END == newId) {
            ThreadData thread = threads.get(threadId);
            if (null != thread) {
                thread.end(threadTicks);
            }
        } else if (REGISTER_FORCE == newId) {
            // enforce thread registration
            getThread(threadId, threadTicks, now);
        } else {
            RecorderElement elt = assignByStackTrace(null, null, 
                threadId, false);
            ThreadData thread = getThread(threadId, threadTicks, now);
            if (null != elt) {
                thread.setRecorderElement(elt);
            }
            if (null != thread) {
                ThreadData newThread = getThread(newId, 0, now);
                // synchronize with Recorder.notifyThreadStarted
                // TODO restrict here?
                newThread.copyStackFrom(thread);
            }
        }
    }

/*
    public void register(long threadId, long newId, long threadTicks, 
        long now) {
        if (newId < 0) {
            // enforce thread registration
            ThreadData thread = runningThreads.get(threadId);
            if (null == thread) {
                thread = new ThreadData();
                thread.startTimeRecording(now, threadTicks);
                runningThreads.put(threadId, thread);
            }            
        } else {
            RecorderElement elt = assignByStackTrace(null, null, 
                threadId, false);
            ThreadData thread = runningThreads.get(threadId);
            if (null != elt) {
                if (null == thread) {
                    thread = new ThreadData();
                    runningThreads.put(threadId, thread);
    //TODO                thread.startTimeRecording(now, threadTicks);
                }
                thread.setRecorderElement(elt);
            }
            if (null != thread) {
                // synchronize with Recorder.notifyThreadStarted
                // TODO restrict here?
                ThreadData newThread = runningThreads.get(newId);
                if (null == newThread) {
                    newThread = new ThreadData();
                    runningThreads.put(newId, newThread);
    //TODO                newThread.startTimeRecording(now, 0);
//                }
                newThread.copyStackFrom(thread);
            }
        }
    }
 */

    /**
     * Returns or registers a thread to {@link #runningThreads} if not 
     * registered before.
     * 
     * @param id the Java thread id
     * @param ticks the current thread ticks
     * @param now the current system time in nanoseconds
     * @return the thread data object, either created or a previously 
     *     registered one
     * 
     * @since 1.00
     */
    private ThreadData getThread(long id, long ticks, long now) {
        ThreadData thread = threads.get(id);
        if (null == thread) {
            thread = new ThreadData();
            threads.put(id, thread);
        }
        // stored only if not used before, considers thread reuse
        thread.start(ticks); 
        return thread;
    }

    /**
     * Notifies the recorder about an object allocated to memory. Please note
     * that dependent on the concrete instrumentation not all allocated objects
     * might be cleaned up.
     * 
     * @param recId a unique identification where to assign this event to
     * @param threadId the identification of the current thread
     * @param id the unique identification of the object being allocated (may be
     *     <code>0</code> to avoid recording of uninstrumented classes)
     * @param size the affected amount of bytes in memory
     * 
     * @since 1.00
     */
    @Override
    public void memoryAllocated(String recId, long threadId, long id, 
        long size) {
        MonitoringGroupChangeListener listener 
            = PluginRegistry.getMonitoringGroupChangeListener();
        
        if (null != programRecord && programRecord.accountResource(
            ResourceType.MEMORY)) {
            programRecord.memoryAllocated(size);
            if (null != listener) {
                if (Configuration.INSTANCE.programUseFromJvm()) {
                    // do this only regularly in case that there is a listener
                    programRecord.updateMemoryFreedFromJvm();
                }
                listener.monitoringGroupChanged(programRecord);
            }
        }
        // local aggregation to recId as specified in scope definition
        RecorderElement elt = assignByStackTrace(recId, null, threadId, false);
        if (null != elt 
            && elt.accountResource(ResourceType.MEMORY)) {
            elt.memoryAllocated(size);
            if (elt.isIndirectAccounting()) {
                ThreadData thread = threads.get(threadId);
                int tPos = null == thread ? -1 : thread.stackSize() - 2;
                while (tPos >= 0) {
                    if (!thread.isStackElementDuplicated(tPos)) {
                        RecorderElement sElt = thread.getStackElement(tPos);
                        if (sElt != elt 
                            && sElt.accountResource(ResourceType.MEMORY)) {
                            sElt.memoryAllocated(size);
                            if (null != listener) {
                                listener.monitoringGroupChanged(sElt);
                            }
                        }
                    }
                    tPos--;
                }
            }

            // local aggregation to configuration of running groups
            RecorderElement cElt = 
                getRecorderElements().getCurrentConfigurationRecord(recId);
            if (null != cElt 
                && cElt.accountResource(ResourceType.MEMORY)) {
                cElt.memoryAllocated(size);
                if (null != listener) {
                    listener.configurationChanged(cElt);
                }
            }
            if (ENABLE_DEBUG) {
                log(elt, cElt, DebugState.MEMORY_ALLOCATION, size, 
                    ResourceType.MEMORY);
            }
        }
    }

    /**
     * Notifies the recorder about an object freed from memory. Please note
     * that dependent on the concrete instrumentation not all memory frees
     * might be detected, e.g. those of immediate system classes (e.g. 
     * <code>Object</code> cannot be instrumented).
     * 
     * @param recId a unique identification where to assign this event to
     * @param size the affected amount of bytes in memory
     * 
     * @since 1.00
     */
    @Override
    public void memoryFreedByRecId(String recId, long size) {
        // aggregation is done on native side
        RecorderElement elt = getRecorderElement(recId);
        if (null != elt) {
            MonitoringGroupChangeListener listener 
                = PluginRegistry.getMonitoringGroupChangeListener();
            elt.memoryFreed(size);
            if (null != listener) {
                listener.monitoringGroupChanged(elt);
            }

            // local aggregation to configuration of running groups
            RecorderElement cElt = 
                getRecorderElements().getCurrentConfigurationRecord(recId);
            if (!recId.equals(Helper.RECORDER_ID)) {
                if (null != cElt && cElt.accountResource(ResourceType.MEMORY)) {
                    cElt.memoryFreed(size);
                    if (null != listener) {
                        listener.configurationChanged(cElt);
                    }
                }
                if (ENABLE_DEBUG) {
                    log(elt, cElt, DebugState.MEMORY_FREE, size, 
                        ResourceType.MEMORY);
                }
            }
        }
    }
    
    /**
     * Debugging helper method.
     * 
     * @param elt the recorder element for the component / group 
     *      (must not be <b>null</b>)
     * @param cElt the configuration recorder element (may be <b>null</b>)
     * @param state the debug state to be checked for this log (don't check 
     *      if <b>null</b>)
     * @param value the value to be printed
     * @param rType the type of the accounted resource
     * 
     * @since 1.00
     */
    private void log(RecorderElement elt, RecorderElement cElt, 
        DebugState state, Object value, ResourceType rType) {
        if (null != elt && (null == state || elt.hasDebugStates(state))) {
            StringBuilder buf = new StringBuilder();
            // do not register instance because of debugging
            if (elt.hasDebugStates(DebugState.CONFIGURATION)) {
                if (null != cElt && cElt.accountResource(rType)) {
                    ConfigurationToName ctn = getRecorderElements()
                        .getConfigurationMapping();
                    if (null != state) {
                        buf.append(state.getMarker());
                        buf.append(" ");
                    }
                    buf.append(ctn.formatConfiguration(getRecorderElements()
                        .getCurrentConfigurationId(), ","));
                    buf.append(" ");
                    buf.append(value);
                    buf.append(" ");
                    buf.append(cElt);
                }
            } else {
                if (null != state) {
                    buf.append(state.getMarker());
                    buf.append(" ");
                }
                buf.append(value);
            }
            StackTraceElement[] trc = Thread.currentThread().getStackTrace();
            for (int i = 0; i < trc.length; i++) {
                if (!trc[i].getClassName().startsWith(
                    getClass().getPackage().getName())) {
                    buf.append(" ");
                    buf.append(trc[i]);
                }
            }
            Level level = Configuration.LOG.getLevel();
            Configuration.LOG.setLevel(Level.ALL);
            Configuration.LOG.info(buf.toString());
            Configuration.LOG.setLevel(level);
        }
    }
    
    /**
     * Notifies the recorder about an amount of bytes read from some I/O 
     * channel.
     * 
     * @param recId a unique identification where to assign this event to
     * @param caller optional class name of the calling class
     * @param threadId the identification of the current thread
     * @param bytes the number of bytes
     * @param type the type of the channel
     * 
     * @since 1.00
     */
    @Override
    public void readIo(String recId, String caller, long threadId, int bytes, 
        StreamType type) {
        MonitoringGroupChangeListener listener 
            = PluginRegistry.getMonitoringGroupChangeListener();
        ResourceType resource = type.getResource();

        if (null != programRecord 
            && programRecord.accountResource(resource)) {
            programRecord.readIo(bytes, type);
            if (null != listener) {
                listener.monitoringGroupChanged(programRecord);
            }
        }
        RecorderElement elt = assignByStackTrace(
            recId, caller, threadId, false);
        // local aggregation to recId as specified in scope definition
        if (null != elt && elt.accountResource(resource)) {
            elt.readIo(bytes, type);
            if (elt.isIndirectAccounting()) {
                ThreadData thread = threads.get(threadId);
                int tPos;
                if (null != thread) {
                    tPos = thread.stackSize() - 2;
                } else {
                    tPos = -1;
                }
                while (tPos >= 0) {
                    if (!thread.isStackElementDuplicated(tPos)) {
                        RecorderElement tElt = thread.getStackElement(tPos);
                        if (tElt != elt && tElt.accountResource(resource)) {
                            tElt.readIo(bytes, type);
                            if (null != listener) {
                                listener.monitoringGroupChanged(tElt);
                            }
                        }
                    }
                    tPos--;
                }
            }

            // local aggregation to configuration of running groups
            RecorderElement cElt = 
                getRecorderElements().getCurrentConfigurationRecord(recId);
            if (ENABLE_DEBUG) {
                if (type.matches(DebugState.NET_IN)) {
                    log(elt, cElt, DebugState.NET_IN, bytes, resource);
                }
                if (type.matches(DebugState.FILE_IN)) {
                    log(elt, cElt, DebugState.FILE_IN, bytes, resource);
                }
            }
            if (null != cElt && cElt.accountResource(resource)) {
                cElt.readIo(bytes, type);
                if (null != listener) {
                    listener.configurationChanged(cElt);
                }
            }
        } 
    }

    /**
     * Notifies the recorder about an amount of bytes written to some I/O 
     * channel.
     * 
     * @param recId a unique identification where to assign this event to
     * @param caller optional class name of the calling class
     * @param threadId the identification of the current thread
     * @param bytes the number of bytes
     * @param type the type of the channel
     * 
     * @since 1.00
     */
    @Override
    public void writeIo(String recId, String caller, long threadId, int bytes, 
        StreamType type) {
        ResourceType resource = type.getResource();
        MonitoringGroupChangeListener listener 
            = PluginRegistry.getMonitoringGroupChangeListener();
        
        // global aggregation to program
        if (null != programRecord 
            && programRecord.accountResource(resource)) {
            programRecord.writeIo(bytes, type);
            if (null != listener) {
                listener.monitoringGroupChanged(programRecord);
            }
        }
        RecorderElement elt = assignByStackTrace(
            recId, caller, threadId, false);
        // local aggregation to recId as specified in scope definition
        if (null != elt && elt.accountResource(resource)) {
            elt.writeIo(bytes, type);
            if (elt.isIndirectAccounting()) {
                // aggregate to entire stack
                ThreadData thread = threads.get(threadId);
                int tPos;
                if (null != thread) {
                    tPos = thread.stackSize() - 2;
                } else {
                    tPos = -1;
                }
                while (tPos >= 0) {
                    if (!thread.isStackElementDuplicated(tPos)) {
                        RecorderElement tElt = thread.getStackElement(tPos);
                        if (tElt != elt && tElt.accountResource(resource)) {
                            tElt.writeIo(bytes, type);
                            if (null != listener) {
                                listener.monitoringGroupChanged(tElt);
                            }
                        }
                    }
                    tPos--;
                }
            }

            // local aggregation to configuration of running groups
            RecorderElement cElt = 
                getRecorderElements().getCurrentConfigurationRecord(recId);
            if (ENABLE_DEBUG) {
                if (type.matches(DebugState.NET_OUT)) {
                    log(elt, cElt, DebugState.NET_OUT, bytes, resource);
                }
                if (type.matches(DebugState.FILE_OUT)) {
                    log(elt, cElt, DebugState.FILE_OUT, bytes, resource);
                }
            }
            if (null != cElt && cElt.accountResource(resource)) {
                cElt.writeIo(bytes, type);
                if (null != listener) {
                    listener.configurationChanged(cElt);
                }
            }
        }
    }
    
    /**
     * Returns the recorder element for the given recording group id.
     * 
     * @param id the group identification
     * @return the recorder element or <b>null</b>
     * 
     * @since 1.00
     */
    /*@Override
    protected RecorderElement getRecorderElement(String id) {
        RecorderElement elt = null;
        if (null != id) {
            elt = super.getRecorderElement(id);
            if (null == elt) {
                // more or less for testing and unified access
                if (id.equals(Helper.PROGRAM_ID)) {
                    elt = programRecord;
                } else if (id.equals(Helper.RECORDER_ID)) {
                    elt = overheadRecord;
                }
            }
        }
        return elt;
    }*/
    
    /**
     * Notify the recorder that a new variability configuration might have 
     * been entered. The recorder should resolve the concrete configuration
     * by tracking the configuration activations.
     * 
     * @param ids an optional (list of) group identification
     * 
     * @since 1.00
     */
    @Override
    public void enterConfiguration(String ids) {
        getRecorderElements().enterCompleteConfiguration(ids, true);
    }
    
    /**
     * Retrieves the recorder element to be used for recording based on the 
     * given recorder identification and the thread identification. This method
     * searches (if available) the current thread and within the thread the 
     * current (recorded) call stack (most specific call if appropriate). 
     * Otherwise it considers the default recording element of the thread (if
     * assigned during thread startup) or simply the top of the stack.
     * 
     * @param recId the recorder element
     * @param caller optional class name of the calling class
     * @param threadId the thread identification
     * @param exclude force exclusion from monitoring
     * @return the most appropriate recorder element (may be <b>null</b>, 
     *   denotes that the information provided cannot be used to derive the
     *   recorder element and, thus, the information should be recorded for the
     *   entire program only).
     * 
     * @since 1.00
     */
    protected RecorderElement assignByStackTrace(String recId, String caller, 
        long threadId, boolean exclude) {
        RecorderElement result = null;
        if (exclude) {
            result = excluded;
        } else {
            ThreadData thread = threads.get(threadId);
            if (null != recId) {
                // search for top-level, else use recId
                int numId = getRecorderElements().getVariabilityIdNum(recId);
                if (null != thread && numId >= 0) {
                    result = thread.top(numId);
                } 
                if (null == result) {
                    result = getRecorderElement(recId);
                }
            } 
            if (null != caller) {
                // an abstract class which is called directly may not
                // be marked to be monitored but should be assigned 
                // correctly - caller will contain the actual class type
                String id = getRecorderId(caller);
                if (null != id) {
                    result = getRecorderElement(id);
                }
            }
            if (null == result && null != thread) {
                result = thread.top();
                if (null == result) {
                    result = thread.getRecorderElement();
                }
            }
        }
        return result;
    }
    
    /**
     * Prints the current (aggregated) state to the output formatter.
     * 
     * @param pData additional information collected for system and JVM process
     * @return <code>true</code> if the threadsInfo object should be released 
     *     to the pool, <code>false</code> if it should not be released
     * 
     * @since 1.00
     */
    @Override
    public boolean printCurrentState(ProcessData pData) {
        MonitoringGroupBurstChangeListener listener 
            = PluginRegistry.getMonitoringGroupBurstChangeListener();
        MonitoringGroupChangeListener chListener 
            = PluginRegistry.getMonitoringGroupChangeListener();        
        if (null != listener || null != chListener) {
            if (null == systemNotificationInstance) {
                systemNotificationInstance = new ProcessData.Measurements();
            }
            if (null == jvmNotificationInstance) {
                jvmNotificationInstance = new ProcessData.Measurements();
            }
            systemNotificationInstance.copyFrom(pData.getSystem());
            jvmNotificationInstance.copyFrom(pData.getJvm());
            if (Configuration.INSTANCE.programUseFromJvm()) {
                programRecord.updateMemoryFreedFromJvm();
            }
        }
        if (null != listener) {
            listener.notifyBurstChange(systemNotificationInstance, 
                jvmNotificationInstance);
        }
        if (null != chListener) {
            chListener.measurementsChanged(systemNotificationInstance, 
                jvmNotificationInstance);            
        }        
            
        configureFormatter();
        formatter.setProcessData(pData);
        RecorderElementMap elts = getRecorderElements();
        formatter.printCurrentStateStatistics(elts, 
            programRecord, null);
        return true; // release always
    }

    /**
     * Notifies about a timer event.
     * 
     * @param id the timer identification (may overlap with recorder ids)
     * @param state the new timer state
     * @param now the current system time in milliseconds
     * @param threadId the id of the currently executing thread, may be 
     *     negative if it should be ignored
     * 
     * @since 1.00
     */
    @Override
    @Variability(id = AnnotationConstants.MONITOR_TIMERS)
    public void notifyTimer(String id, TimerState state, long now, 
        long threadId) {
        if (threadId >= 0) {
            // TODO revise, currently very primitive
            id += threadId;
        }
        TimerInfo info = timers.get(id);
        if (null == info) {
            if (TimerState.START == state) {
                info = new TimerInfo(now);
                timers.put(id, info);
            }
        } else {
            if (info.handleState(state, now)) {
                TimerChangeListener listener 
                    = PluginRegistry.getTimerChangeListener();
                if (null != listener) {
                    listener.timerFinished(id, info.getValue());
                }
            }
        }
    }
    
    /**
     * Notifies about a changing attribute value.
     * 
     * @param id the identification of the attribute (may overlap with 
     *    recorder ids)
     * @param type the type of the value
     * @param value the new value (after changing)
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.MONITOR_VALUES)
    public void notifyValueChange(String id, ValueType type, Object value) {
        ValueChangeListener listener 
            = PluginRegistry.getValueChangeListener();
        if (null != listener) {
            listener.notifyValueChange(id, type, value);
        }
    }
    
    /**
     * Notify the listeners about the program record creation (if not done 
     * implicitly before).
     */
    public void notifyProgramRecordCreation() {
        MonitoringGroupCreationListener listener 
            = PluginRegistry.getMonitoringGroupCreationListener();
        if (null != listener) {
            listener.monitoringGroupCreated(
                MonitoringGroupCreationListener.ID_PROGRAM, programRecord);
        }
    }
    
    /**
     * Prints the stack trace elements for which the code is not located in
     * the recorder packages.
     * 
     * @param firstOnly print the first occurrence outside only, else the entire
     *     stack outside
     * 
     * @since 1.00
     */
    @SuppressWarnings("unused")
    private static void printNonRecorderStackElement(boolean firstOnly) {
        StackTraceElement[] elt = Thread.currentThread().getStackTrace();
        String pkg = DefaultRecorderStrategy.class.getPackage().getName();
        int pos = pkg.lastIndexOf('.');
        if (pos > 0) {
            pkg = pkg.substring(0, pos);
        }
        for (int i = 0; i < elt.length; i++) {
            if (!elt[i].getClassName().startsWith(pkg) 
                && !elt[i].getClassName().startsWith("java.lang")) {
                System.out.println(elt[i]);
                if (firstOnly) {
                    break;
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clearTemporaryData() {
        Cleanup clean = InternalPluginRegistry.getInstrumenterCleanup();
        if (null != clean) {
            clean.cleanupIfRequired();
        }
    }

}
