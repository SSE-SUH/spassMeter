package de.uni_hildesheim.sse.monitoring.runtime.recordingStrategies;

import de.uni_hildesheim.sse.codeEraser.annotations.Operation;
import de.uni_hildesheim.sse.codeEraser.annotations.Variability;
import de.uni_hildesheim.sse.monitoring.runtime.AnnotationConstants;
import de.uni_hildesheim.sse.monitoring.runtime.annotations.TimerState;
import de.uni_hildesheim.sse.monitoring.runtime.boot.MonitoringGroupSettings;
import de.uni_hildesheim.sse.monitoring.runtime.boot.StreamType;
import de.uni_hildesheim.sse.monitoring.runtime.configuration.
    MonitoringGroupConfiguration;
import de.uni_hildesheim.sse.monitoring.runtime.plugins.ValueType;
import de.uni_hildesheim.sse.monitoring.runtime.utils.LongLongHashMap;

/**
 * A pluggable strategy which implements recording. This interface was 
 * introduced because network monitoring should be transparently supported 
 * as well as in-memory-recording. Thus, this interface must not provide access
 * to monitoring groups.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public interface RecorderStrategy {

    /**
     * Force thread registration. To be used 
     * with {@link #register(long, long, long, long)}
     */
    public static final int REGISTER_FORCE = -1;
    
    /**
     * Force thread end. To be used 
     * with {@link #register(long, long, long, long)}
     */
    public static final int REGISTER_END = -2;
    
    /**
     * Notifies the recorder about an amount of bytes written to some I/O 
     * channel.
     * 
     * @param recId a unique identification where to assign this event to
     * @param caller optional class name of the calling class
     * @param threadId the identification of the calling thread
     * @param bytes the number of bytes
     * @param type the type of the channel
     * 
     * @since 1.00
     */
    @Variability(id = {AnnotationConstants.MONITOR_NET_IO, 
            AnnotationConstants.MONITOR_FILE_IO }, op = Operation.AND)
    public void writeIo(String recId, String caller, long threadId, int bytes, 
        StreamType type);
    
    /**
     * Notifies the recorder about an amount of bytes read from some I/O 
     * channel.
     * 
     * @param recId a unique identification where to assign this event to
     * @param caller optional class name of the calling class
     * @param threadId the identification of the calling thread
     * @param bytes the number of bytes
     * @param type the type of the channel
     * 
     * @since 1.00
     */
    @Variability(id = {AnnotationConstants.MONITOR_NET_IO, 
            AnnotationConstants.MONITOR_FILE_IO }, op = Operation.AND)
    public void readIo(String recId, String caller, long threadId, 
        int bytes, StreamType type);

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
    @Variability(id = AnnotationConstants.MONITOR_MEMORY_USAGE)
    public void memoryFreedByRecId(String recId, long size);
    
    /**
     * Notifies the recorder about an object allocated to memory. Please note
     * that dependent on the concrete instrumentation not all allocated objects
     * might be cleaned up.
     * 
     * @param recId a unique identification where to assign this event to
     * @param threadId the identification of the calling thread
     * @param id the unique identification of the object being allocated
     * @param size the affected amount of bytes in memory
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.MONITOR_MEMORY_ALLOCATED)
    public void memoryAllocated(String recId, long threadId, 
        long id, long size);
    
    /**
     * Notifies the recorder about a method being exited.
     * 
     * @param recId a unique identification where to assign this event to
     * @param now the current time in nano seconds
     * @param threadsInfo information on the currently running threads
     * @param exclude is this an exclusion from monitoring
     * @return <code>true</code> if the threadsInfo object should be released 
     *     to the pool, <code>false</code> if it should not be released
     * 
     * @since 1.00
     */
    public boolean exit(String recId, long now, ThreadsInfo threadsInfo, 
        boolean exclude);

    /**
     * Registers a thread id with a recording id. This method is used to
     * assign unregistered method calls to monitored threads as well as to
     * register system threads.
     * 
     * @param threadId the read id to be recorded
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
    public void register(long threadId, long newId, long threadTicks, 
        long now);
    
    /**
     * Notifies the recorder about a method being called.
     * 
     * @param recId a unique identification where to assign this event to
     * @param now the current system time in nano seconds
     * @param threadsInfo information on the currently running threads
     * @param exclude is this an exclusion from monitoring
     * @return <code>true</code> if the threadsInfo object should be released 
     *     to the pool, <code>false</code> if it should not be released
     * 
     * @since 1.00
     */
    public boolean enter(String recId, long now, ThreadsInfo threadsInfo, 
        boolean exclude);

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
    public boolean assignAllTo(String recId, boolean enter, long now, 
        ThreadsInfo threadInfo);
    
    /**
     * Emits the values collected so far.
     * 
     * @param data additional information collected for system and JVM process
     * @return <code>true</code> if the data object should be released 
     *     to the pool, <code>false</code> if it should not be released
     * 
     * @since 1.00
     */
    public boolean printStatistics(ProcessData data);

    /**
     * Called as the last event from the monitored program. This is after 
     * {@link #finishRecording} and optionally
     * {@link #printStatistics}.
     * 
     * @since 1.00
     */
    public void endSystem();
    
    /**
     * Notifies this strategy that time recording should be stopped (first 
     * stage of stopping the recorder).
     * 
     * @param now the current time in nano seconds
     * @param threadsInfo information on the currently running threads
     * @return <code>true</code> if the threadsInfo object should be released 
     *     to the pool, <code>false</code> if it should not be released
     * 
     * @since 1.00
     */
    public boolean stopTimeRecording(long now, ThreadsInfo threadsInfo);

    /**
     * Notifies this strategy that recording should be stopped (second stage 
     * of stopping the recorder).
     * 
     * @param now the current time in nano seconds
     * @param threadId the id of the current thread
     * @param curCpuTime the id-cpu time mapping of all threads
     * @return <code>true</code> if the curCpuTime object should be released 
     *     to the pool, <code>false</code> if it should not be released
     * 
     * @since 1.00
     */
    public boolean finishRecording(long now, long threadId, 
        LongLongHashMap curCpuTime);
    
    /**
     * Notifies this strategy that recording should be start.
     * 
     * @param now the current time in nano seconds
     * @param threadId the thread id of the executing (main) thread
     * @param threadTicks the thread ticks for <code>threadId</code>
     * 
     * @since 1.00
     */
    public void startRecording(long now, long threadId, 
        long threadTicks);
    
    /**
     * Registers a given class for recording. Optional, a (group) 
     * <code>id</code> might be specified to which the measurements are 
     * assigned to. Ids are unique strings without inherent semantics. If
     * not given, the <code>className</code> is used for grouping and assigning
     * measurements (implicit 1-groups).
     * 
     * @param className the name of the class measurements should be registered 
     *        for
     * @param recId an optional group identification (may be empty or 
     *        <b>null</b>)
     * @param conf additional configuration for the monitoring group derived
     *        from <code>settings</code>
     * @param settings the monitoring group settings including all configuration
     *        information (reference should not be stored, will be freed 
     *        explicitly)
     *        
     * @since 1.00
     */
    public void registerForRecording(String className, String recId, 
        MonitoringGroupConfiguration conf, MonitoringGroupSettings settings);

    /**
     * Notify the recorder that a new variability configuration might have 
     * been entered. The recorder should resolve the concrete configuration
     * by tracking the configuration activations.
     * 
     * @param id an optional group identification (may be empty or <b>null</b>)
     * 
     * @since 1.00
     */
    public void enterConfiguration(String id);
    
    /**
     * Maps a class name to its recorder id (called in this interface usually
     * <code>recId</code>).
     * 
     * @param className the class name to be mapped
     * @return the recorder id of <code>className</code>
     * 
     * @since 1.00
     */
    public String getRecorderId(String className);
    
    /**
     * Returns the char used for separating the variability id and its 
     * current value (in configurations).
     * 
     * @return the separator char
     * 
     * @since 1.00
     */
    public char getVariabilitySeparatorChar();
   
    /**
     * Enables or disables automatic variability detection.
     * 
     * @param enable <code>true</code> if variability detection should be 
     *   enabled, <code>false</code> else
     * 
     * @since 1.00
     */
    public void enableVariabilityDetection(boolean enable);

    /**
     * Returns if automatic variability detection is enabled.
     * 
     * @return <code>true</code> if variability detection should be 
     *   enabled, <code>false</code> else
     * 
     * @since 1.00
     */
    public boolean isVariabilityDetectionEnabled();
    
    /**
     * Prints the current (aggregated) state to the output formatter.
     * 
     * @param data additional information collected for system and JVM process
     * @return <code>true</code> if the threadsInfo object should be released 
     *     to the pool, <code>false</code> if it should not be released
     * 
     * @since 1.00
     */
    public boolean printCurrentState(ProcessData data);

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
    @Variability(id = AnnotationConstants.MONITOR_TIMERS)
    public void notifyTimer(String id, TimerState state, long now, 
        long threadId);

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
    public void notifyValueChange(String id, ValueType type, Object value);
    
    /**
     * Notify the listeners about the program record creation (if not done 
     * implicitly before).
     */
    public void notifyProgramRecordCreation();
    
    /**
     * Notify that temporary data e.g. from instrumentation shall be cleared.
     * This is in particular required to synchronize event-based 
     * implementations.
     * 
     * @since 1.00
     */
    public void clearTemporaryData();

    /**
     * Returns the attached storage.
     * 
     * @return the attached storage
     * 
     * @since 1.00
     */
    public StrategyStorage getStorage();
    
}
