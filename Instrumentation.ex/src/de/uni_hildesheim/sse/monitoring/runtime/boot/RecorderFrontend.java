package de.uni_hildesheim.sse.monitoring.runtime.boot;

import de.uni_hildesheim.sse.codeEraser.annotations.Operation;
import de.uni_hildesheim.sse.codeEraser.annotations.Variability;
import de.uni_hildesheim.sse.monitoring.runtime.AnnotationConstants;
import de.uni_hildesheim.sse.monitoring.runtime.annotations.TimerState;

/**
 * Defines a frontend to be called from instrumented code. This class helps
 * separating the real recorder implementation from the calls. This is needed
 * because this class is loaded (automatically by the agent) to the boot class
 * path in order to be available also from (instrumented) Java library classes. 
 * If the real implementation would be available, this would disturb class 
 * loading and have negative effects on the instrumented program. Thus, the
 * instrumentation is responsible for setting {@link #instance} properly and
 * instrumented code needs to call methods of the recorder (frontend) via this
 * class.<p>
 * Methods intended for Java monitoring are tagged with [Java call] and should
 * not be called during native monitoring. Methods tagged with [native call] are
 * intended for native monitoring and should not be called during Java 
 * monitoring except of indirect calls from [Java call] methods or in case that 
 * methods are tagged twice. We assume that <code>System.nanoTime</code> and
 * <code>System.currentTimeMillis</code> work properly in a native environment.
 * Required thread ids are indirectly obtained from 
 * {@link de.uni_hildesheim.sse.system.IThreadDataGatherer#getCurrentId()}. In 
 * case that the returned identifiers do not map to
 * native identifiers, replace the native implementation of 
 * {@link de.uni_hildesheim.sse.system.IThreadDataGatherer}.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.20
 */
public abstract class RecorderFrontend {
    
    // checkstyle: stop member visibility check"
    
    /**
     * Stores the instance of this class. At runtime we assume that this 
     * attribute is initialized properly and never <b>null</b> when the 
     * instrumenter is running.
     */
    public static RecorderFrontend instance;

    // checkstyle: resume member visibility check
    
    /**
     * Registers a given class for recording. Optional, a (group) of
     * <code>id</code>s might be specified to which the measurements are 
     * assigned to. Ids are unique strings without inherent semantics. If
     * not given, the <code>className</code> is used for grouping and assigning
     * measurements (implicit 1-groups). [Java call, native call]
     * 
     * @param className the name of the class measurements should be registered 
     *        for
     * @param settings the monitoring group settings including all configuration
     *        information (reference should not be stored, will be freed 
     *        explicitly)
     * 
     * @since 1.00
     */
    public abstract void registerForRecording(String className, 
        MonitoringGroupSettings settings);

    /**
     * Register the given input stream as overhead stream, i.e. set the 
     * attribute added by instrumentation via reflection so that the recorder
     * is notified. [Java call]
     * 
     * @param in the stream to be registered
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.MONITOR_OVERHEAD)
    public abstract void registerAsOverheadStream(java.io.InputStream in);

    /**
     * Register the given output stream as overhead stream, i.e. set the 
     * attribute added by instrumentation via reflection so that the recorder
     * is notified. [Java call]
     * 
     * @param out the stream to be registered
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.MONITOR_OVERHEAD)
    public abstract void registerAsOverheadStream(
        java.io.OutputStream out);

    /**
     * Notifies about start of monitoring. [Java call, native call]
     * 
     * @since 1.00
     */
    public abstract void notifyProgramStart();

    /**
     * Notifies about end of monitoring (this might occur at different point
     * of time than {@link #endSystem()} and at multiple times). [Java call, 
     * native call]
     * 
     * @since 1.00
     */
    public abstract void notifyProgramEnd();

    /**
     * Changes if automated variability detection should be enabled or not.
     * [Java call, native call]
     * 
     * @param enable <code>true</code> if it should be enabled (can be 
     * switched off by annotation) or if manual notification via annotation
     * should be considered only
     * 
     * @since 1.00
     */
    public abstract void enableVariabilityDetection(boolean enable);

    /**
     * Notifies about a started thread. [Java call]
     * 
     * @param thread the started thread
     * 
     * @since 1.00
     */
    public abstract void notifyThreadStart(Thread thread);

    /**
     * Notifies about a started thread. [native call]
     * 
     * @param newThreadId the identification of the started thread
     * 
     * @since 1.00
     */
    public abstract void notifyThreadStart(long newThreadId);

    /**
     * Notifies about the end of the current thread. [Java call]
     * 
     * @since 1.00
     */
    public abstract void notifyThreadEnd();

    /**
     * Notifies about an ended thread. [native call]
     * 
     * @param newThreadId the identification of the ended thread
     * 
     * @since 1.00
     */
    public abstract void notifyThreadEnd(long newThreadId);
    
    /**
     * Notifies that the following values (if not further calls 
     * to this method or other recording ids occur) should be assigned
     * to the given <code>recId</code>. [Java call, native call]
     * 
     * @param recId the recording id (may be also the variability prefix)
     * @param enter <code>true</code> if this is a method enter event, 
     *   <code>false</code> else
     * 
     * @since 1.00
     */
    public abstract void assignAllTo(String recId, boolean enter);

    /**
     * Emits the values collected so far. [Java call, native call]
     * 
     * @since 1.00
     */
    public abstract void printStatistics();

    /**
     * Notifies about the end of the monitored program. [Java call, native call]
     * 
     * @since 1.00
     */
    public abstract void endSystem();

    /**
     * Notifies the recorder about a method being called. [Java call, 
     * native call]
     * 
     * @param caller the type of the caller
     * @param recId the recorder id (may be <b>null</b>)
     * @param exclude is this an exclusion from monitoring
     * @param directId is <code>recId</code> direct, e.g. in case of an 
     *    annotated method
     * @param instanceId the optional instance identifier, <code>0</code> means 
     *    disabled
     * 
     * @since 1.20
     */
    public abstract void enter(String caller, String recId, boolean exclude, 
        boolean directId, long instanceId);

    /**
     * Notifies the recorder about a method being exited. [Java call, 
     * native call]
     * 
     * @param caller the type of the caller
     * @param recId the recorder id (may be <b>null</b>)
     * @param exclude is this an exclusion from monitoring
     * @param directId is <code>recId</code> direct, e.g. in case of an 
     *    annotated method
     * @param instanceId the optional instance identifier, <code>0</code> means 
     *    disabled
     * 
     * @since 1.00
     */
    public abstract void exit(String caller, String recId, boolean exclude, 
        boolean directId, long instanceId);

    /**
     * Notify the recorder that a new variability configuration might have 
     * been entered. The recorder should resolve the concrete configuration
     * by tracking the configuration activations. [Java call, native call]
     * 
     * @param ids an optional list of group identifications
     * 
     * @since 1.00
     */
    public abstract void configurationChange(String ids);

    /**
     * Notifies the recorder about an object allocated to memory. Please note
     * that dependent on the concrete instrumentation not all allocated objects
     * might be cleaned up. [Java call]
     * 
     * @param allocated the allocated
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.MONITOR_MEMORY_ALLOCATED)
    public abstract void memoryAllocated(Object allocated);

    /**
     * Notifies the recorder about an object allocated to memory. This method
     * is required for explicit notifications. [Java call]
     * 
     * @param tag an object which serves as the identification for object size 
     *   to be freed
     * @param size the allocated memory size
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.MONITOR_MEMORY_ALLOCATED)
    public abstract void memoryAllocated(Object tag, long size);
    
    /**
     * Notifies the recorder about an object allocated to memory. 
     * that dependent on the concrete instrumentation not all allocated objects
     * might be cleaned up. [native call]
     * 
     * @param tag the identification of the memory object, e.g. its memory 
     *   address
     * @param size the allocated memory size
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.MONITOR_MEMORY_ALLOCATED)
    public abstract void memoryAllocated(long tag, long size);

    /**
     * Notifies the recorder about an object freed from memory. Please note
     * that dependent on the concrete instrumentation not all memory frees
     * might be detected, e.g. those of immediate system classes (e.g. 
     * <code>Object</code> cannot be instrumented, garbage collector not called
     * when JVM is running, etc.). [Java call]
     * 
     * @param freed the freed object
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.MONITOR_MEMORY_USAGE)
    public abstract void memoryFreed(Object freed); 

    /**
     * Notifies the recorder about an object freed from memory. This method
     * is required for explicit notifications. [Java call]
     * 
     * @param tag an object which serves as the identification for object size 
     *   to be freed
     * @param size the size of the freed object
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.MONITOR_MEMORY_USAGE)
    public abstract void memoryFreed(Object tag, long size);
    
    /**
     * Notifies the recorder about an object freed from memory. Please note
     * that dependent on the concrete instrumentation not all memory frees
     * might be detected, e.g. those of immediate system classes (e.g. 
     * <code>Object</code> cannot be instrumented, garbage collector not called
     * when JVM is running, etc.). [Java call, native call]
     * 
     * @param tag the identification of the object to be freed, e.g. its memory
     *   address
     * @param size the size of the freed object
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.MONITOR_MEMORY_USAGE)
    public abstract void memoryFreed(long tag, long size);

    /**
     * Notifies the recorder about an amount of bytes read from some I/O 
     * channel. [Java call, native call]
     * 
     * @param recId an the target group identification
     * @param caller the type of the caller
     * @param bytes the number of bytes
     * @param type the type of the channel
     * @return bytes
     * 
     * @since 1.00
     */
    @Variability(id = { AnnotationConstants.MONITOR_NET_IO, 
            AnnotationConstants.MONITOR_FILE_IO }, op = Operation.AND)
    public abstract int readIo(String recId, String caller, 
        int bytes, StreamType type);
    
    /**
     * Notifies the recorder about an amount of bytes written to some I/O 
     * channel. [Java call, native call]
     * 
     * @param recId an the target group identification
     * @param caller the type of the caller
     * @param bytes the number of bytes
     * @param type the type of the channel
     * @return bytes
     * 
     * @since 1.00
     */
    @Variability(id = { AnnotationConstants.MONITOR_NET_IO, 
            AnnotationConstants.MONITOR_FILE_IO }, op = Operation.AND)
    public abstract int writeIo(String recId, String caller,
        int bytes, StreamType type);

    /**
     * Notifies the recorder about changing the value context. [Java call, 
     * native call]
     * 
     * @param id the value identification representing the context
     * @param push if <code>true</code> push the context to the context stack, 
     *     <code>false</code> else
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.MONITOR_VALUES)
    public abstract void changeValueContext(String id, boolean push);

    /**
     * Notifies the recorder about a changing value. [Java call, native call]
     * 
     * @param recId the recorder id (optional, may be <b>null</b>)
     * @param newValue the value after the value change
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.MONITOR_VALUES)
    public abstract void notifyValueChange(String recId, Object newValue);

    /**
     * Notifies the recorder about a changing value. [Java call, native call]
     * 
     * @param recId the recorder id (optional, may be <b>null</b>)
     * @param newValue the value after the value change
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.MONITOR_VALUES)
    public abstract void notifyValueChange(String recId, int newValue);
    
    /**
     * Notifies the recorder about a changing value. [Java call, native call]
     * 
     * @param recId the recorder id (optional, may be <b>null</b>)
     * @param newValue the value after the value change
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.MONITOR_VALUES)
    public abstract void notifyValueChange(String recId, byte newValue);

    /**
     * Notifies the recorder about a changing value. [Java call, native call]
     * 
     * @param recId the recorder id (optional, may be <b>null</b>)
     * @param newValue the value after the value change
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.MONITOR_VALUES)
    public abstract void notifyValueChange(String recId, char newValue);

    /**
     * Notifies the recorder about a changing value. [Java call, native call]
     * 
     * @param recId the recorder id (optional, may be <b>null</b>)
     * @param newValue the value after the value change
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.MONITOR_VALUES)
    public abstract void notifyValueChange(String recId, short newValue);

    /**
     * Notifies the recorder about a changing value. [Java call, native call]
     * 
     * @param recId the recorder id (optional, may be <b>null</b>)
     * @param newValue the value after the value change
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.MONITOR_VALUES)
    public abstract void notifyValueChange(String recId, long newValue);

    /**
     * Notifies the recorder about a changing value. [Java call, native call]
     * 
     * @param recId the recorder id (optional, may be <b>null</b>)
     * @param newValue the value after the value change
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.MONITOR_VALUES)
    public abstract void notifyValueChange(String recId, double newValue);

    /**
     * Notifies the recorder about a changing value. [Java call, native call]
     * 
     * @param recId the recorder id (optional, may be <b>null</b>)
     * @param newValue the value after the value change
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.MONITOR_VALUES)
    public abstract void notifyValueChange(String recId, float newValue);

    /**
     * Notifies the recorder about a changing value. [Java call, native call]
     * 
     * @param recId the recorder id (optional, may be <b>null</b>)
     * @param newValue the value after the value change
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.MONITOR_VALUES)
    public abstract void notifyValueChange(String recId, String newValue);
    
    /**
     * Notifies the recorder about a changing value.
     * 
     * @param recId the recorder id (optional, may be <b>null</b>)
     * @param newValue the value after the value change
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.MONITOR_VALUES)
    public abstract void notifyValueChange(String recId, boolean newValue);

    /**
     * Prints the current (aggregated) state to the output formatter. 
     * [Java call, native call]
     * 
     * @since 1.00
     */
    public abstract void printCurrentState();

    /**
     * Notifies about a timer event. [Java call, native call]
     * 
     * @param id the timer identification (may overlap with recorder ids)
     * @param state the new timer state
     * @param considerThreads <code>false</code> if the <code>id</code> is
     *     thread save and threads must not be considered explicitly, 
     *     <code>false</code> else
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.MONITOR_TIMERS)
    public abstract void notifyTimer(String id, TimerState state, 
        boolean considerThreads);

    /**
     * Registers a JVM thread with its native counterpart. Needed only if the
     * JVM/JDK is not able to handle this, e.g. in 1.4 or Android. [Java call]
     * 
     * @param register register or unregister the thread
     * 
     * @since 1.00
     */
    public abstract void registerThisThread(boolean register);

    /**
     * Notify that temporary data e.g. from instrumentation shall be cleared.
     * The specific cleanup implementation may decide upon this request whether
     * a cleanup is actually required.
     * 
     * @since 1.00
     */
    public abstract void clearTemporaryData();
}
