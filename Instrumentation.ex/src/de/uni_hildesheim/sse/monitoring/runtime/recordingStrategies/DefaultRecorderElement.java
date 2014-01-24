package de.uni_hildesheim.sse.monitoring.runtime.recordingStrategies;

import de.uni_hildesheim.sse.codeEraser.annotations.Operation;
import de.uni_hildesheim.sse.codeEraser.annotations.Variability;
import de.uni_hildesheim.sse.monitoring.runtime.AnnotationConstants;
import de.uni_hildesheim.sse.monitoring.runtime.boot.StreamType;
import de.uni_hildesheim.sse.monitoring.runtime.configuration.
    MonitoringGroupConfiguration;
import de.uni_hildesheim.sse.monitoring.runtime.utils.AbstractLongHashMap;

/**
 * Implements a recorder element containing the monitoring data for one 
 * monitoring group.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
class DefaultRecorderElement extends RecorderElement {
    
    /**
     * Stores the memory use, i.e. the difference between memory allocation
     * and memory unallocation.
     */
    @Variability(id = {AnnotationConstants.MONITOR_MEMORY_ALLOCATED, 
        AnnotationConstants.MONITOR_MEMORY_USAGE }, op = Operation.AND)
    private long memUse = 0;

    /**
     * Stores the amount of memory allocated to this monitoring group.
     */
    @Variability(id = AnnotationConstants.MONITOR_MEMORY_ALLOCATED)
    private long memAllocated = 0;

    // TODO [performance] check if this can be omitted and only one attribute 
    // is sufficient
    /**
     * Stores the system time when monitoring started on this group (temporary 
     * value). A negative value denotes that recording has not started. Zero may
     * be possible in case of a just started thread.
     */
    @Variability(id = AnnotationConstants.MONITOR_TIME_SYSTEM)
    private TimeMap startTimes = new TimeMap();
//    private transient long startSystemTime = -1;

    /**
     * Stores the system time ticks elapsed and allocated to this class.
     */
    @Variability(id = AnnotationConstants.MONITOR_TIME_SYSTEM)
    private long systemTimeTicks = 0;

    // TODO [performance] check if this can be omitted and only one attribute 
    // is sufficient
    /**
     * Stores the CPU time when monitoring started on this group (temporary 
     * value). A negative value denotes that recording has not started. Zero may
     * be possible in case of a just started thread.
     */
//    @Variability(id = AnnotationConstants.MONITOR_TIME_CPU)
//    private transient long startCpuTime = -1;

    /**
     * Stores the CPU time ticks elapsed and allocated to this class.
     */
    @Variability(id = AnnotationConstants.MONITOR_TIME_CPU)
    private long cpuTimeTicks = 0;
    
    /**
     * Stores the amount of bytes sent to network interfaces.
     */
    @Variability(id = AnnotationConstants.MONITOR_NET_IO)
    private long netOutBytes = 0;

    /**
     * Stores the amount of bytes read from network interfaces.
     */
    @Variability(id = AnnotationConstants.MONITOR_NET_IO)
    private long netInBytes = 0;

    /**
     * Stores the amount of bytes read from files.
     */
    @Variability(id = AnnotationConstants.MONITOR_FILE_IO)
    private long fileOutBytes = 0;

    /**
     * Stores the amount of bytes written to files.
     */
    @Variability(id = AnnotationConstants.MONITOR_FILE_IO)
    private long fileInBytes = 0;

    /**
     * Stores the number of reentrants into this group.
     */
    //private int reentrantCount = 0;
    
    /**
     * Creates a recorder element.
     * 
     * @param conf the configuration of the recorder element
     * 
     * @since 1.00
     */
    protected DefaultRecorderElement(MonitoringGroupConfiguration conf) {
        super(conf);
    }
    
    /**
     * Copies the data stored in <code>from</code> into this instance. (Deep 
     * copy)
     * 
     * @param from the instance from where to copy (may also be a subclass)
     * 
     * @since 1.00
     */
    @Override
    public void copy(RecorderElement from) {
        super.copy(from);
        if (from instanceof DefaultRecorderElement) {
            DefaultRecorderElement dFrom = (DefaultRecorderElement) from;
            memUse = dFrom.memUse;
            memAllocated = dFrom.memAllocated;
            //startSystemTime = dFrom.startSystemTime;
            systemTimeTicks = dFrom.systemTimeTicks;
            //aggregatedSystemTimeTicks = dFrom.aggregatedSystemTimeTicks;
            //startCpuTime = dFrom.startCpuTime;
            startTimes.clear();
            startTimes.putAll(dFrom.startTimes);
            cpuTimeTicks = dFrom.cpuTimeTicks;
            netOutBytes = dFrom.netOutBytes;
            netInBytes = dFrom.netInBytes;
            fileOutBytes = dFrom.fileOutBytes;
            fileInBytes = dFrom.fileInBytes;
            //reentrantCount = dFrom.reentrantCount;
        }
    }

    /**
     * Start the time recording for this instance.
     * 
     * @param nanoTime the current system time in nano seconds
     * @param threadTicks the current number of thread ticks
     * @param threadId the identifier of the thread causing the call
     * @return <code>true</code> if recording started on this instance with
     *     this call, <code>false</code> if recording was yet active, e.g. in
     *     case of recursive or reentrant calls
     * 
     * @since 1.00
     */
    @Override
    boolean startTimeRecording(long nanoTime, long threadTicks, long threadId) {
        boolean recorded = false;
        TimeMap.MapElement entry = startTimes.get(threadId);
        if (null == entry) {
            entry = startTimes.put(threadId, -1, -1);
        } 
        long startCpuTime = entry.getStartCpuTime();
        long startSystemTime = entry.getStartSystemTime();
//        if (0 == reentrantCount) {
        if (startCpuTime < 0) {
            startCpuTime = threadTicks;
            recorded = true;
        }
        if (startSystemTime < 0) {
            startSystemTime = nanoTime;
            recorded = true;
        }
//        reentrantCount++;
        entry.setTimes(startCpuTime, startSystemTime);
        return recorded;
    }

    /**
     * Stops the time recording for this instance.
     * 
     * @param nanoTime the current system time in nano seconds
     * @param threadTicks the current number of thread ticks
     * @param threadId the identifier of the thread causing the call
     * @return <code>true</code> if recording stopped on this instance with
     *     this call, <code>false</code> if recording is still active, e.g. in
     *     case of recursive or reentrant calls, a negative value causes 
     *     stopping time recording for all stored threaded times
     * 
     * @since 1.00
     */
    @Override        
    boolean stopTimeRecording(long nanoTime, long threadTicks, long threadId) {
        boolean recorded = false;
        if (threadId >= 0) {
            recorded = stopTimeRecordingImpl(nanoTime, threadTicks, threadId);
        } else {
            for (AbstractLongHashMap.MapElement elt : startTimes.elements()) {
                recorded |= stopTimeRecordingImpl(nanoTime, threadTicks, 
                    elt.getKey());
            }
        }
        return recorded;
    }
    

    /**
     * Stops the time recording for this instance.
     * 
     * @param nanoTime the current system time in nano seconds
     * @param threadTicks the current number of thread ticks
     * @param threadId the identifier of the thread causing the call
     * @return <code>true</code> if recording stopped on this instance with
     *     this call, <code>false</code> if recording is still active, e.g. in
     *     case of recursive or reentrant calls
     * 
     * @since 1.00
     */
    private boolean stopTimeRecordingImpl(long nanoTime, long threadTicks, 
        long threadId) {
        boolean recorded = false;
        TimeMap.MapElement entry = startTimes.get(threadId);
        if (null == entry) {
            entry = startTimes.put(threadId, -1, -1);
        } 
        long startCpuTime = entry.getStartCpuTime();
        long startSystemTime = entry.getStartSystemTime();
//        reentrantCount--;
//      if (0 == reentrantCount) {
        if (startCpuTime >= 0) {
            cpuTimeTicks += threadTicks - startCpuTime;
            startCpuTime = -1;
            recorded = true;
        }
        if (startSystemTime >= 0) {
            systemTimeTicks += nanoTime - startSystemTime;
            startSystemTime = -1;
            recorded = true;
        }
        entry.setTimes(startCpuTime, startSystemTime);
        return recorded;
//        return true; // check callers
    }

    
    /**
     * Returns the CPU time when recording was started.
     * 
     * @param threadId the identifier of the thread the start time should be 
     *   returned for
     * @return the time when recording was started, may be negative if disabled
     * 
     * @since 1.00
     */
    protected long getStartCpuTime(long threadId) {
        long result;
        TimeMap.MapElement entry = startTimes.get(threadId);
        if (null == entry) {
            result = -1;
        } else {
            result = entry.getStartCpuTime();
        }
        return result;
    }

    /**
     * Returns the system time when recording was started.
     * 
     * @param threadId the identifier of the thread the start time should be 
     *   returned for
     * @return the time when recording was started, may be negative if disabled
     * 
     * @since 1.00
     */
    protected long getStartSystemTime(long threadId) {
        long result;
        TimeMap.MapElement entry = startTimes.get(threadId);
        if (null == entry) {
            result = -1;
        } else {
            result = entry.getStartSystemTime();
        }
        return result;
    }


    /**
     * Records an amount of memory allocated by the underlying recording group.
     * 
     * @param size the amount of memory allocated
     * 
     * @since 1.00
     */
    @Override        
    void memoryAllocated(long size) {
        memUse += size;
        memAllocated += size;
    }
    
    /**
     * Records an amount of memory freed within the underlying recording 
     * group (memory unallocation).
     * 
     * @param size the amount of memory freed
     * 
     * @since 1.00
     */
    @Override        
    void memoryFreed(long size) {
        memUse -= size;
        if (memUse < 0) {
            memUse = 0;
        }
    }
    
    /**
     * Updates freed memory from the JVM instead of recording this information
     * individually (memory unallocation). Should be used for the (entire) 
     * program record only.
     * 
     * @since 1.00
     */
    @Override
    void updateMemoryFreedFromJvm() {
        Runtime rt = Runtime.getRuntime();
        // calculate used memory
        memUse = rt.maxMemory() - rt.freeMemory();
    }
    
    /**
     * Records an amount of bytes read from file or from network.
     * 
     * @param bytes the amount of bytes read
     * @param type the type of stream used for reading
     * 
     * @since 1.00
     */
    @Override        
    void readIo(int bytes, StreamType type) {
        switch (type) {
        case FILE:
            fileInBytes += bytes;
            break;
        case NET:
            netInBytes += bytes;
            break;
        default:
            // do nothing, unknown
            break;
        }
    }

    /**
     * Records an amount of bytes written to file or from network.
     * 
     * @param bytes the amount of bytes written
     * @param type the type of stream used for writing
     * 
     * @since 1.00
     */
    @Override        
    void writeIo(int bytes, StreamType type) {
        switch (type) {
        case FILE:
            fileOutBytes += bytes;
            break;
        case NET:
            netOutBytes += bytes;
            break;
        default:
            // do nothing, unknown
            break;
        }
    }
    
    /**
     * Sets the current CPI time ticks for finishing recording of the entire
     * program.
     * 
     * @param cpuTimeTicks the current CPU time ticks
     * 
     * @since 1.00
     */
    @Override        
    void setCpuTimeTicks(long cpuTimeTicks) {
        this.cpuTimeTicks = cpuTimeTicks;
    }
    
    /**
     * Adds an external amount of input or output bytes to this recording 
     * element. This method is used for recording overhead bytes. (This method
     * is currently available only for network overhead as networked monitoring
     * may lead to uncontrolled recursive network recording).
     * 
     * @param netInBytes the amount of bytes read from network
     * @param netOutBytes the amount of bytes written to network
     * 
     * @since 1.00
     */
    void addNetBytes(long netInBytes, long netOutBytes) {
        this.netInBytes += netInBytes;
        this.netOutBytes += netOutBytes;
    }
    
    /**
     * Returns if data on this instance was recorded or if it a dummy instance
     * created e.g. for some unused (automatic detected) variants.
     * 
     * @return <code>true</code> if valid data was recorded, <code>false</code>
     *    else
     * 
     * @since 1.00
     */
    @Override        
    public boolean wasRecorded() {
        return cpuTimeTicks > 0 || systemTimeTicks > 0 
            /*|| aggregatedSystemTimeTicks > 0*/;
    }

    /**
     * Returns the entire number of system time ticks recorded for this 
     * instance.
     * 
     * @return the number of system time ticks
     * 
     * @since 1.00
     */
    @Override        
    public long getSystemTimeTicks() {
        return systemTimeTicks;
    }

    /**
     * Returns the entire number of CPU time ticks recorded for this 
     * instance.
     * 
     * @return the number of CPU time ticks
     * 
     * @since 1.00
     */
    @Override        
    public long getCpuTimeTicks() {
        return cpuTimeTicks;
    }
    
    /**
     * Returns the amount of memory allocated for this instance.
     * 
     * @return the amount of allocated memory
     */
    @Override        
    public long getMemAllocated() {
        return memAllocated;
    }
    
    /**
     * Returns the amount of memory currently being used, i.e. difference 
     * between allocated and unallocated memory.
     * 
     * @return the amount of memory being used
     */
    @Override        
    public long getMemUse() {
        return memUse;
    }
        
    /**
     * Returns the number of bytes read from files or network.
     * 
     * @return the number of bytes read from files or network
     * 
     * @since 1.00
     */
    @Override        
    public long getIoRead() {
        return netInBytes + fileInBytes;
    }

    /**
     * Returns the number of bytes written to files or network.
     * 
     * @return the number of bytes written to files or network
     * 
     * @since 1.00
     */
    @Override        
    public long getIoWrite() {
        return netOutBytes + fileOutBytes;
    }
    
    /**
     * Returns the number of bytes read from network.
     * 
     * @return the number of bytes read from network
     * 
     * @since 1.00
     */
    @Override        
    public long getNetIn() {
        return netInBytes;
    }

    /**
     * Returns the number of bytes written to network.
     * 
     * @return the number of bytes written to network
     * 
     * @since 1.00
     */
    @Override        
    public long getNetOut() {
        return netOutBytes;
    }

    /**
     * Returns the number of bytes read from files.
     * 
     * @return the number of bytes read from files
     * 
     * @since 1.00
     */
    @Override        
    public long getFileIn() {
        return fileInBytes;
    }

    /**
     * Returns the number of bytes written to files.
     * 
     * @return the number of bytes written to files
     * 
     * @since 1.00
     */
    @Override        
    public long getFileOut() {
        return fileOutBytes;
    }
    
    /**
     * Adds (overhead) time correction information.
     * 
     * @param timeDiff time difference
     * @param threadTimeFraction threaded time fraction
     * @param decrement negative or positive correction
     * @param threadId the thread id of the causing thread
     * 
     * @since 1.00
     */
    @Override        
    void timeCorrection(long timeDiff, long threadTimeFraction, 
        boolean decrement, long threadId) {
        if (decrement) {
            TimeMap.MapElement entry = startTimes.get(threadId);
            if (null == entry) {
                entry = startTimes.put(threadId, -1, -1);
            } 
            if (entry.getStartCpuTime() > 0 && entry.getStartSystemTime() > 0) {
            //if (0 == reentrantCount) {
                // recording finished - change result
                if (systemTimeTicks > 0) {
                    systemTimeTicks -= timeDiff;
                }
            } else {
                // recording - so change start time
                systemTimeTicks += timeDiff;
            }
        } else {
            // assumption overhead counting
            systemTimeTicks += timeDiff;
        }
    }
    
    /**
     * Returns the currently relevant recorder element (in case that 
     * multiple subelements are supported).
     * 
     * @param index the index of the sub element
     * @param factory a factory to create new elements if needed
     * @param max the (current) number of maximum required subelements (
     *     variabilities)
     * @return the subelement or this instance
     * 
     * @since 1.00
     */
    @Override
    RecorderElement getContributing(int index, RecorderElementFactory factory, 
        int max) {
        return this;
    }

    /**
     * Returns a contributing recording element, i.e. one of the elements
     * assigned to individual variants in a variability configuration.
     * 
     * @param index the element to be returned
     * @return the element assigned to the given index (always <b>this</b>)
     * @throws ArrayIndexOutOfBoundsException if 
     *     <code>index&lt;0 || index&gt;={@link #getContributingSize()}</code>
     * 
     * @since 1.00
     */
    @Override
    public RecorderElement getContributing(int index) {
        return this;
    }
    
    /**
     * Returns the number of contributing recording elements, i.e. the elements
     * recording individual variants in a variability configuration.
     * 
     * @return the number of contributing recording elements (always 0)
     * 
     * @since 1.00
     */
    @Override
    public int getContributingSize() {
        return 0;
    }

}