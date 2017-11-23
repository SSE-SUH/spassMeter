package de.uni_hildesheim.sse.monitoring.runtime.boot;

import de.uni_hildesheim.sse.codeEraser.annotations.Operation;
import de.uni_hildesheim.sse.codeEraser.annotations.Variability;
import de.uni_hildesheim.sse.monitoring.runtime.AnnotationConstants;
import de.uni_hildesheim.sse.monitoring.runtime.annotations.TimerState;

/**
 * Provides static access to {@link RecorderFrontend}. This is needed because
 * calls to the recorder before recording is started would otherwise result
 * in a <code>NullPointerException</code>. Conversely, the recorder 
 * {@link RecorderFrontend} is needed in order to hide the dependencies of the 
 * concrete implementation which would lead to a dependency cycle while 
 * instrumentation and class loading.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.20
 */
public class RecorderAccess {

    /**
     * Prevents this class from being instantiated from outside.
     * 
     * @since 1.00
     */
    private RecorderAccess() {
    }
    
    /**
     * Registers a given class for recording. Optional, a (group) of
     * <code>id</code>s might be specified to which the measurements are 
     * assigned to. Ids are unique strings without inherent semantics. If
     * not given, the <code>className</code> is used for grouping and assigning
     * measurements (implicit 1-groups).
     * 
     * @param className the name of the class measurements should be registered 
     *        for
     * @param settings the monitoring group settings including all configuration
     *        information (reference should not be stored, will be freed 
     *        explicitly)
     * 
     * @since 1.00
     */
    public static void registerForRecording(String className, 
        MonitoringGroupSettings settings) {
        if (null != RecorderFrontend.instance) {
            RecorderFrontend.instance.registerForRecording(className, 
                settings);
        }
    }

    /**
     * Register the given input stream as overhead stream, i.e. set the 
     * attribute added by instrumentation via reflection so that the recorder
     * is notified. 
     * 
     * @param in the stream to be registered
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.MONITOR_OVERHEAD)
    public static void registerAsOverheadStream(java.io.InputStream in) {
        if (null != RecorderFrontend.instance) {
            RecorderFrontend.instance.registerAsOverheadStream(in);
        }
    }

    /**
     * Register the given output stream as overhead stream, i.e. set the 
     * attribute added by instrumentation via reflection so that the recorder
     * is notified. 
     * 
     * @param out the stream to be registered
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.MONITOR_OVERHEAD)
    public static void registerAsOverheadStream(java.io.OutputStream out) {
        if (null != RecorderFrontend.instance) {
            RecorderFrontend.instance.registerAsOverheadStream(out);
        }
    }

    /**
     * Notifies about start of monitoring.
     * 
     * @since 1.00
     */
    public static void notifyProgramStart() {
        if (null != RecorderFrontend.instance) {
            RecorderFrontend.instance.notifyProgramStart();
        }
    }

    /**
     * Notifies about end of monitoring (this might occur at different point
     * of time than {@link #endSystem()} and at multiple times).
     * 
     * @since 1.00
     */
    public static void notifyProgramEnd() {
        if (null != RecorderFrontend.instance) {
            RecorderFrontend.instance.notifyProgramEnd();
        }
    }

    /**
     * Changes if automated variability detection should be enabled or not.
     * 
     * @param enable <code>true</code> if it should be enabled (can be 
     * switched off by annotation) or if manual notification via annotation
     * should be considered only
     * 
     * @since 1.00
     */
    public static void enableVariabilityDetection(boolean enable) {
        if (null != RecorderFrontend.instance) {
            RecorderFrontend.instance.enableVariabilityDetection(enable);
        }
    }

    /**
     * Notifies about a started thread.
     * 
     * @param thread the started thread
     * 
     * @since 1.00
     */
    public static void notifyThreadStart(Thread thread) {
        if (null != RecorderFrontend.instance) {
            RecorderFrontend.instance.notifyThreadStart(thread);
        }
    }
    
    /**
     * Notifies about the end of the current thread. [Java call]
     * 
     * @since 1.00
     */
    public static final void notifyThreadEnd() {
        if (null != RecorderFrontend.instance) {
            RecorderFrontend.instance.notifyThreadEnd();
        }
    }

    /**
     * Notifies about an ended thread. [native call]
     * 
     * @param threadId the identification of the ended thread
     * 
     * @since 1.00
     */
    public final void notifyThreadEnd(long threadId) {
        if (null != RecorderFrontend.instance) {
            RecorderFrontend.instance.notifyThreadEnd(threadId);
        }
    }

    /**
     * Notifies that the following values (if not further calls 
     * to this method or other recording ids occur) should be assigned
     * to the given <code>recId</code>.
     * 
     * @param recId the recording id (may be also the variability prefix)
     * @param enter <code>true</code> if this is a method enter event, 
     *   <code>false</code> else
     * 
     * @since 1.00
     */
    public static void assignAllTo(String recId, boolean enter) {
        if (null != RecorderFrontend.instance) {
            RecorderFrontend.instance.assignAllTo(recId, enter);
        }
    }

    /**
     * Emits the values collected so far.
     * 
     * @since 1.00
     */
    public static void printStatistics() {
        if (null != RecorderFrontend.instance) {
            RecorderFrontend.instance.printStatistics();
        }
    }

    /**
     * Notifies about the end of the monitored program.
     * 
     * @since 1.00
     */
    public static void endSystem() {
        if (null != RecorderFrontend.instance) {
            RecorderFrontend.instance.endSystem();
        }
    }

    /**
     * Notifies the recorder about a method being called.
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
    public static void enter(String caller, String recId, boolean exclude, 
        boolean directId, long instanceId) {
        if (null != RecorderFrontend.instance) {
            RecorderFrontend.instance.enter(caller, recId, exclude, directId, instanceId);
        }
    }

    /**
     * Notifies the recorder about a method being exited.
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
    public static void exit(String caller, String recId, boolean exclude, 
        boolean directId, long instanceId) {
        if (null != RecorderFrontend.instance) {
            RecorderFrontend.instance.exit(caller, recId, exclude, directId, instanceId);
        }
    }

    /**
     * Notify the recorder that a new variability configuration might have 
     * been entered. The recorder should resolve the concrete configuration
     * by tracking the configuration activations.
     * 
     * @param ids an optional list of group identifications
     * 
     * @since 1.00
     */
    public static void configurationChange(String ids) {
        if (null != RecorderFrontend.instance) {
            RecorderFrontend.instance.configurationChange(ids);
        }
    }

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
    public static void memoryAllocated(Object tag, long size) {
        if (null != RecorderFrontend.instance) {
            RecorderFrontend.instance.memoryAllocated(tag, size);
        }
    }

    /**
     * Notifies the recorder about an object allocated to memory. Please note
     * that dependent on the concrete instrumentation not all allocated objects
     * might be cleaned up.
     * 
     * @param allocated the allocated
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.MONITOR_MEMORY_ALLOCATED)
    public static void memoryAllocated(Object allocated) {
        if (null != RecorderFrontend.instance) {
            RecorderFrontend.instance.memoryAllocated(allocated);
        }
    }

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
    public static void memoryFreed(Object tag, long size) {
        if (null != RecorderFrontend.instance) {
            RecorderFrontend.instance.memoryFreed(tag, size);
        }
    }
    
    /**
     * Notifies the recorder about an object freed from memory. Please note
     * that dependent on the concrete instrumentation not all memory frees
     * might be detected, e.g. those of immediate system classes (e.g. 
     * <code>Object</code> cannot be instrumented, garbage collector not called
     * when JVM is running, etc.).
     * 
     * @param freed the freed object
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.MONITOR_MEMORY_USAGE)
    public static void memoryFreed(Object freed) {
        if (null != RecorderFrontend.instance) {
            RecorderFrontend.instance.memoryFreed(freed);
        }
    }

    /**
     * Notifies the recorder about an object freed from memory. Please note
     * that dependent on the concrete instrumentation not all memory frees
     * might be detected, e.g. those of immediate system classes (e.g. 
     * <code>Object</code> cannot be instrumented, garbage collector not called
     * when JVM is running, etc.).
     * 
     * @param tag the identification of the object to be freed
     * @param size the size of the freed object
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.MONITOR_MEMORY_USAGE)
    public static void memoryFreed(long tag, long size) {
        if (null != RecorderFrontend.instance) {
            RecorderFrontend.instance.memoryFreed(tag, size);
        }
    }

    /**
     * Notifies the recorder about an amount of bytes read from some I/O 
     * channel.
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
    public static int readIo(String recId, String caller, 
        int bytes, StreamType type) {
        if (null != RecorderFrontend.instance) {
            RecorderFrontend.instance.readIo(recId, caller, bytes, type);
        }
        return bytes;
    }
    
    /**
     * Notifies the recorder about an amount of bytes written to some I/O 
     * channel.
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
    public static int writeIo(String recId, String caller,
        int bytes, StreamType type) {
        if (null != RecorderFrontend.instance) {
            RecorderFrontend.instance.writeIo(recId, caller, bytes, type);
        }
        return bytes;
    }

    /**
     * Notifies the recorder about changing the value context.
     * 
     * @param id the value identification representing the context
     * @param push if <code>true</code> push the context to the context stack, 
     *     <code>false</code> else
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.MONITOR_VALUES)
    public static void changeValueContext(String id, boolean push) {
        if (null != RecorderFrontend.instance) {
            RecorderFrontend.instance.changeValueContext(id, push);
        }
    }

    /**
     * Notifies the recorder about a changing value.
     * 
     * @param recId the recorder id (optional, may be <b>null</b>)
     * @param newValue the value after the value change
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.MONITOR_VALUES)
    public static void notifyValueChange(String recId, Object newValue) {
        if (null != RecorderFrontend.instance) {
            RecorderFrontend.instance.notifyValueChange(recId, newValue);
        }
    }

    /**
     * Notifies the recorder about a changing value.
     * 
     * @param recId the recorder id (optional, may be <b>null</b>)
     * @param newValue the value after the value change
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.MONITOR_VALUES)
    public static void notifyValueChange(String recId, int newValue) {
        if (null != RecorderFrontend.instance) {
            RecorderFrontend.instance.notifyValueChange(recId, newValue);
        }
    }
    
    /**
     * Notifies the recorder about a changing value.
     * 
     * @param recId the recorder id (optional, may be <b>null</b>)
     * @param newValue the value after the value change
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.MONITOR_VALUES)
    public static void notifyValueChange(String recId, byte newValue) {
        if (null != RecorderFrontend.instance) {
            RecorderFrontend.instance.notifyValueChange(recId, newValue);
        }
    }

    /**
     * Notifies the recorder about a changing value.
     * 
     * @param recId the recorder id (optional, may be <b>null</b>)
     * @param newValue the value after the value change
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.MONITOR_VALUES)
    public static void notifyValueChange(String recId, char newValue) {
        if (null != RecorderFrontend.instance) {
            RecorderFrontend.instance.notifyValueChange(recId, newValue);
        }
    }

    /**
     * Notifies the recorder about a changing value.
     * 
     * @param recId the recorder id (optional, may be <b>null</b>)
     * @param newValue the value after the value change
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.MONITOR_VALUES)
    public static void notifyValueChange(String recId, short newValue) {
        if (null != RecorderFrontend.instance) {
            RecorderFrontend.instance.notifyValueChange(recId, newValue);
        }
    }

    /**
     * Notifies the recorder about a changing value.
     * 
     * @param recId the recorder id (optional, may be <b>null</b>)
     * @param newValue the value after the value change
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.MONITOR_VALUES)
    public static void notifyValueChange(String recId, long newValue) {
        if (null != RecorderFrontend.instance) {
            RecorderFrontend.instance.notifyValueChange(recId, newValue);
        }
    }

    /**
     * Notifies the recorder about a changing value.
     * 
     * @param recId the recorder id (optional, may be <b>null</b>)
     * @param newValue the value after the value change
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.MONITOR_VALUES)
    public static void notifyValueChange(String recId, double newValue) {
        if (null != RecorderFrontend.instance) {
            RecorderFrontend.instance.notifyValueChange(recId, newValue);
        }
    }

    /**
     * Notifies the recorder about a changing value.
     * 
     * @param recId the recorder id (optional, may be <b>null</b>)
     * @param newValue the value after the value change
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.MONITOR_VALUES)
    public static void notifyValueChange(String recId, float newValue) {
        if (null != RecorderFrontend.instance) {
            RecorderFrontend.instance.notifyValueChange(recId, newValue);
        }
    }

    /**
     * Notifies the recorder about a changing value.
     * 
     * @param recId the recorder id (optional, may be <b>null</b>)
     * @param newValue the value after the value change
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.MONITOR_VALUES)
    public static void notifyValueChange(String recId, String newValue) {
        if (null != RecorderFrontend.instance) {
            RecorderFrontend.instance.notifyValueChange(recId, newValue);
        }
    }
    
    /**
     * Notifies the recorder about a changing value.
     * 
     * @param recId the recorder id (optional, may be <b>null</b>)
     * @param newValue the value after the value change
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.MONITOR_VALUES)
    public static void notifyValueChange(String recId, boolean newValue) {
        if (null != RecorderFrontend.instance) {
            RecorderFrontend.instance.notifyValueChange(recId, newValue);
        }
    }

    /**
     * Prints the current (aggregated) state to the output formatter.
     * 
     * @since 1.00
     */
    public static void printCurrentState() {
        if (null != RecorderFrontend.instance) {
            RecorderFrontend.instance.printCurrentState();
        }
    }

    /**
     * Notifies about a timer event.
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
    public static void notifyTimer(String id, TimerState state, 
        boolean considerThreads) {
        if (null != RecorderFrontend.instance) {
            RecorderFrontend.instance.notifyTimer(id, state, considerThreads);
        }
    }
    
    /**
     * Registers a JVM thread with its native counterpart. Needed only if the
     * JVM/JDK is not able to handle this, e.g. in 1.4 or Android.
     * 
     * @param register register or unregister the thread
     * 
     * @since 1.00
     */
    public static void registerThisThread(boolean register) {
        if (null != RecorderFrontend.instance) {
            RecorderFrontend.instance.registerThisThread(register);
        }
    }

}
