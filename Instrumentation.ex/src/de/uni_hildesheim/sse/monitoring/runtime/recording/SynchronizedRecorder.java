package de.uni_hildesheim.sse.monitoring.runtime.recording;

import de.uni_hildesheim.sse.codeEraser.annotations.Operation;
import de.uni_hildesheim.sse.codeEraser.annotations.Variability;
import de.uni_hildesheim.sse.monitoring.runtime.AnnotationConstants;
import de.uni_hildesheim.sse.monitoring.runtime.annotations.TimerState;
import de.uni_hildesheim.sse.monitoring.runtime.boot.RecorderFrontend;
import de.uni_hildesheim.sse.monitoring.runtime.boot.StreamType;
import de.uni_hildesheim.sse.monitoring.runtime.configuration.ObjectSizeCache;
import de.uni_hildesheim.sse.monitoring.runtime.utils.LinkedList;

/**
 * Implements the frontend to be called from instrumented code. This class is
 * responsible for translating any program-specific information such as thread
 * instances, object instances or platform/OS measurements to symbolic 
 * information such as thread ids, recording ids or measurement values, 
 * particularly for monitoring over network. Therefore most
 * members are static. Currently, we use individual methods for the "events"
 * registered in the code. In fact, this might be done via event objects but
 * so far the impact on the memory footprint is not clear (TBD).
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public class SynchronizedRecorder extends Recorder {

    /**
     * The internal lock for the message producer-consumer.
     */
    private Object unallocationLock = new Object();

    /**
     * Stores the unallocation information (preventing deadlocks).
     */
    private LinkedList<Unallocation> unallocations 
        = new LinkedList<Unallocation>();
    
    /**
     * Prevents this class from being instantiated from outside.
     * 
     * @since 1.00
     */
    private SynchronizedRecorder() {
        new UnallocationThread().start();
    }
    
    /**
     * Cause loading this class.
     * 
     * @since 1.00
     */
    public static void initialize() {
        RecorderFrontend.instance = new SynchronizedRecorder();
    }
    
    /**
     * Notifies about start of monitoring. [Java call, native call]
     * 
     * @since 1.00
     */
    @Override
    public final synchronized void notifyProgramStart() {
        super.notifyProgramStart();
    }
    
    /**
     * Notifies about end of monitoring (this might occur at different point
     * of time than {@link #endSystem()} and at multiple times). [Java call, 
     * native call]
     * 
     * @since 1.00
     */
    @Override
    public final synchronized void notifyProgramEnd() {
        super.notifyProgramEnd();
    }

    /**
     * Notifies the recorder about a method being called. [Java call, 
     * native call]
     * 
     * @param caller the type of the caller
     * @param recId the recorder id (may be <b>null</b>)
     * @param exclude is this an exclusion from monitoring
     * @param directId is <code>recId</code> direct, e.g. in case of an 
     *    annotated method
     * 
     * @since 1.00
     */
    @Override
    public void enter(String caller, String recId, 
        boolean exclude, boolean directId) {
        super.enter(caller, recId, exclude, directId);
    }
    
    /**
     * Notifies the recorder about a method being exited. [Java call, 
     * native call]
     * 
     * @param caller the type of the caller
     * @param recId the recorder id (may be <b>null</b>)
     * @param exclude is this an exclusion from monitoring
     * @param directId is <code>recId</code> direct, e.g. in case of an 
     *    annotated method
     * 
     * @since 1.00
     */
    @Override
    public final synchronized void exit(String caller, String recId, 
        boolean exclude, boolean directId) {
        super.exit(caller, recId, exclude, directId);
    }
    
    /**
     * Notify the recorder that a new variability configuration might have 
     * been entered. The recorder should resolve the concrete configuration
     * by tracking the configuration activations. [Java call, native call]
     * 
     * @param ids an optional list of group identifications
     * 
     * @since 1.00
     */
    @Override
    public final synchronized void configurationChange(String ids) {
        super.configurationChange(ids);
    }

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
    @Override
    public final synchronized void memoryAllocated(Object allocated) {
        super.memoryAllocated(allocated);
    }
    
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
    @Override
    public final synchronized void memoryAllocated(long tag, long size) {
        super.memoryAllocated(tag, size);
    }
    
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
    @Override
    public final void memoryFreed(Object freed) {
        if (isRecording && null != freed) {
            memoryFreed(getId(freed), 
                ObjectSizeCache.INSTANCE.getSize(freed, false));
        }
    }

    /**
     * Stores the information for a particular unallocation.
     * 
     * @author Holger Eichelberger
     * @since 1.00
     * @version 1.00
     */
    private static class Unallocation {
        
        /**
         * The identification of the object to be freed, e.g. its 
         * memory address.
         */
        private long tag;

        /**
         * The size of the freed object.
         */
        private long size;
        
        /**
         * Creates an unallocation information.
         * 
         * @param tag the identification of the object to be freed, e.g. its 
         *   memory address
         * @param size the size of the freed object
         * 
         * @since 1.00
         */
        private Unallocation(long tag, long size) {
            this.tag = tag;
            this.size = size;
        }
        
    }
    
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
    @Override
    public final void memoryFreed(long tag, long size) {
        if (isRecording) {
            synchronized (unallocationLock) {
                unallocations.addLast(new Unallocation(tag, size));
                unallocationLock.notify();
            }
        }
    }

    /**
     * Handles the unallocation (by calling the overridden superclass method).
     * 
     * @param tag the identification of the object to be freed, e.g. its memory
     *   address
     * @param size the size of the freed object
     * 
     * @since 1.00
     */
    private final void memoryFreedImpl(long tag, long size) {
        super.memoryFreed(tag, size);
    }

    /**
     * Implements a thread which sends the gathered data to the specified
     * server.
     * 
     * @author Stephan Dederichs, Holger Eichelberger
     * @since 1.00
     * @version 1.00
     */
    private class UnallocationThread extends Thread {

        /**
         * Responsible for sending the information to the server.
         * 
         * @since 1.00
         */
        @Override
        public void run() {
            try {
                while (isRecording) {
                    Unallocation u;
                    synchronized (unallocationLock) {
                        while (unallocations.isEmpty()) {
                            unallocationLock.wait(200);
                        }
                        if (!unallocations.isEmpty()) {
                            u = unallocations.removeFirst();
                        } else {
                            u = null;
                        }
                    }
                    if (null != u) {
                        memoryFreedImpl(u.tag, u.size);
                    }
                }
            } catch (InterruptedException e) {
            }
        }
        
    }

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
    @Override
    public final synchronized int readIo(String recId, String caller, 
        int bytes, StreamType type) {
        return super.readIo(recId, caller, bytes, type);
    }

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
    @Override
    public final synchronized int writeIo(String recId, String caller,
        int bytes, StreamType type) {
        return super.writeIo(recId, caller, bytes, type);
    }
    
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
    @Override
    public final synchronized void changeValueContext(String id, boolean push) {
        super.changeValueContext(id, push);
    }
    
    /**
     * Notifies the recorder about a changing value. [Java call, native call]
     * 
     * @param recId the recorder id (optional, may be <b>null</b>)
     * @param newValue the value after the value change
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.MONITOR_VALUES)
    @Override
    public final synchronized void notifyValueChange(String recId, 
        Object newValue) {
        super.notifyValueChange(recId, newValue);
    }

    /**
     * Notifies the recorder about a changing value. [Java call, native call]
     * 
     * @param recId the recorder id (optional, may be <b>null</b>)
     * @param newValue the value after the value change
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.MONITOR_VALUES)
    @Override
    public final synchronized void notifyValueChange(String recId, 
        int newValue) {
        super.notifyValueChange(recId, newValue);
    }

    /**
     * Notifies the recorder about a changing value. [Java call, native call]
     * 
     * @param recId the recorder id (optional, may be <b>null</b>)
     * @param newValue the value after the value change
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.MONITOR_VALUES)
    @Override
    public final synchronized void notifyValueChange(String recId, 
        byte newValue) {
        super.notifyValueChange(recId, newValue);
    }

    /**
     * Notifies the recorder about a changing value. [Java call, native call]
     * 
     * @param recId the recorder id (optional, may be <b>null</b>)
     * @param newValue the value after the value change
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.MONITOR_VALUES)
    @Override
    public final synchronized void notifyValueChange(String recId, 
        char newValue) {
        super.notifyValueChange(recId, newValue);
    }

    /**
     * Notifies the recorder about a changing value. [Java call, native call]
     * 
     * @param recId the recorder id (optional, may be <b>null</b>)
     * @param newValue the value after the value change
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.MONITOR_VALUES)
    @Override
    public final synchronized void notifyValueChange(String recId, 
        short newValue) {
        super.notifyValueChange(recId, newValue);
    }
    
    /**
     * Notifies the recorder about a changing value. [Java call, native call]
     * 
     * @param recId the recorder id (optional, may be <b>null</b>)
     * @param newValue the value after the value change
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.MONITOR_VALUES)
    @Override
    public final synchronized void notifyValueChange(String recId, 
        long newValue) {
        super.notifyValueChange(recId, newValue);
    }

    /**
     * Notifies the recorder about a changing value. [Java call, native call]
     * 
     * @param recId the recorder id (optional, may be <b>null</b>)
     * @param newValue the value after the value change
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.MONITOR_VALUES)
    @Override
    public final synchronized void notifyValueChange(String recId, 
        double newValue) {
        super.notifyValueChange(recId, newValue);
    }

    /**
     * Notifies the recorder about a changing value. [Java call, native call]
     * 
     * @param recId the recorder id (optional, may be <b>null</b>)
     * @param newValue the value after the value change
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.MONITOR_VALUES)
    @Override
    public final synchronized void notifyValueChange(String recId, 
        float newValue) {
        super.notifyValueChange(recId, newValue);
    }

    /**
     * Notifies the recorder about a changing value. [Java call, native call]
     * 
     * @param recId the recorder id (optional, may be <b>null</b>)
     * @param newValue the value after the value change
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.MONITOR_VALUES)
    @Override
    public final synchronized void notifyValueChange(String recId, 
        boolean newValue) {
        super.notifyValueChange(recId, newValue);
    }

    /**
     * Notifies the recorder about a changing value. [Java call, native call]
     * 
     * @param recId the recorder id (optional, may be <b>null</b>)
     * @param newValue the value after the value change
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.MONITOR_VALUES)
    @Override
    public final synchronized void notifyValueChange(String recId, 
        String newValue) {
        super.notifyValueChange(recId, newValue);
    }
    
    /**
     * Emits the values collected so far. [Java call, native call]
     * 
     * @since 1.00
     */
    public final synchronized void printStatistics() {
        super.printStatistics();
    }

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
    @Override
    public synchronized void notifyTimer(String id, TimerState state, 
        boolean considerThreads) {
        super.notifyTimer(id, state, considerThreads);
    }
    
}
