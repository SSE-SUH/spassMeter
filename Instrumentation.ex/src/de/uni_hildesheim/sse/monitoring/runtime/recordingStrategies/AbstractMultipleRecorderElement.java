package de.uni_hildesheim.sse.monitoring.runtime.recordingStrategies;

import de.uni_hildesheim.sse.codeEraser.annotations.Variability;
import de.uni_hildesheim.sse.monitoring.runtime.AnnotationConstants;
import de.uni_hildesheim.sse.monitoring.runtime.boot.ResourceType;
import de.uni_hildesheim.sse.monitoring.runtime.boot.StreamType;
import de.uni_hildesheim.sse.monitoring.runtime.configuration.
    MonitoringGroupConfiguration;

/**
 * Defines a recording element which represents multiple recorder elements 
 * by delegating to stored monitoring groups. This class has a different
 * semantics than a {@link ContributingRecorderElement} because it passes the
 * same recorded value to all represented recorder elements.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public abstract class AbstractMultipleRecorderElement extends RecorderElement {
    
    /**
     * Stores the contributing variant measurements.
     */
    private RecorderElement[] elements;
    
    /**
     * Stores whether values should be distributed in accountable fractions
     * or passed to the elements as a whole.
     */
    private boolean distributeValues;
    
    /**
     * Creates a recorder element. Distribute values.
     * 
     * @param conf the monitoring group configuration
     * 
     * @since 1.00
     */
    protected AbstractMultipleRecorderElement(
        MonitoringGroupConfiguration conf) {
        this(conf, null, true);
    }

    /**
     * Creates a recorder element.
     * 
     * @param conf the monitoring group configuration
     * @param elements the elements to be stored
     * @param distributeValues account the fraction of the values according to
     *     the accountable elements or the entire value (indirect)
     * 
     * @since 1.00
     */
    protected AbstractMultipleRecorderElement(
        MonitoringGroupConfiguration conf, RecorderElement[] elements, 
        boolean distributeValues) {
        super(conf);
        this.elements = elements;
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
        if (from instanceof AbstractMultipleRecorderElement) {
            AbstractMultipleRecorderElement cFrom = 
                (AbstractMultipleRecorderElement) from;
            if (null != cFrom.elements) {
                elements = new RecorderElement[cFrom.elements.length];
                for (int i = 0; i < cFrom.elements.length; i++) {
                    elements[i] = cFrom.elements[i];
                }
            }
        }
    }
    
    /**
     * Distributes a long value to the accountable resources.
     * 
     * @param value the value to be distributed
     * @param type the resource type
     * @return the distributed value (i.e. a fraction or the entire value 
     *   depending on {@link #distributeValues})
     * 
     * @since 1.00
     */
    protected long distributeLongValue(long value, ResourceType type) {
        if (distributeValues) {
            int count = countAccountable(type);
            if (count > 0) {
                value /= count;
            }
        }
        return value;
    }

    /**
     * Distributes an int value to the accountable resources.
     * 
     * @param value the value to be distributed
     * @param type the resource type
     * @return the distributed value (i.e. a fraction or the entire value 
     *   depending on {@link #distributeValues})
     * 
     * @since 1.00
     */
    protected int distributeIntValue(int value, ResourceType type) {
        if (distributeValues) {
            int count = countAccountable(type);
            if (count > 0) {
                value /= count;
            }
        }
        return value;
    }
    
    /**
     * Counts the accountable resources.
     * 
     * @param type the resource type
     * @return the number of accountable resources
     * 
     * @since 1.00
     */
    protected int countAccountable(ResourceType type) {
        int count = 0;
        if (distributeValues) {
            count = 0;
            for (int i = 0; i < elements.length; i++) {
                if (enableRecording(elements[i], type)) {
                    count++;
                }
            }
        } 
        return count;
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
    boolean startTimeRecording(long nanoTime, long threadTicks, long threadId) {
        boolean result = false;
        final ResourceType resource = ResourceType.CPU_TIME;
        if (enableRecording(this, resource)) {
            for (int i = 0; i < elements.length; i++) {
                if (enableRecording(elements[i], resource)) {
                    result |= elements[i].startTimeRecording(
                        nanoTime, threadTicks, threadId);
                }
            }
        }
        return result;
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
    boolean stopTimeRecording(long nanoTime, long threadTicks, long threadId) {
        boolean result = false;
        final ResourceType resource = ResourceType.CPU_TIME;
        if (enableRecording(this, resource)) {
            int count = countAccountable(resource);
            if (count > 0) {
                RecorderElement elt = firstElement();
                threadTicks = (threadTicks - elt.getStartCpuTime(threadId)) 
                    / count;
                nanoTime = (nanoTime - elt.getStartSystemTime(threadId)) 
                    / count;
            }
            for (int i = 0; i < elements.length; i++) {
                // should be true for all, particularly for the first one
                if (enableRecording(elements[i], resource)) {
                    result |= elements[i].stopTimeRecording(
                        nanoTime, threadTicks, threadId);
                }
            }
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
    void memoryAllocated(long size) {
        final ResourceType resource = ResourceType.MEMORY;
        if (enableRecording(this, resource)) {
            size = distributeLongValue(size, resource);
            for (int i = 0; i < elements.length; i++) {
                if (enableRecording(elements[i], resource)) {
                    elements[i].memoryAllocated(size);
                }
            }
        }
    }
    
    /**
     * Records an amount of memory freed within the underlying recording 
     * group (memory unallocation).
     * 
     * @param size the amount of memory freed
     * 
     * @since 1.00
     */
    void memoryFreed(long size) {
        final ResourceType resource = ResourceType.MEMORY;
        if (enableRecording(this, resource)) {
            size = distributeLongValue(size, resource);
            for (int i = 0; i < elements.length; i++) {
                if (enableRecording(elements[i], resource)) {
                    elements[i].memoryFreed(size);
                }
            }
        }
    }
    
    /**
     * Updates freed memory from the JVM instead of recording this information
     * individually (memory unallocation). Should be used for the (entire) 
     * program record only. This method does nothing because the program 
     * record should not be realized by this classes and unallocation recorded
     * by instances should be notified individually via 
     * {@link #memoryFreed(long)}.
     * 
     * @since 1.00
     */
    @Override
    void updateMemoryFreedFromJvm() {
    }
    
    /**
     * Records an amount of bytes read from file or from network.
     * 
     * @param bytes the amount of bytes read
     * @param type the type of stream used for reading
     * 
     * @since 1.00
     */
    void readIo(int bytes, StreamType type) {
        final ResourceType resource = type.getResource();
        if (enableRecording(this, resource)) {
            bytes = distributeIntValue(bytes, resource);
            for (int i = 0; i < elements.length; i++) {
                if (enableRecording(elements[i], resource)) {
                    elements[i].readIo(bytes, type);
                }
            }
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
    void writeIo(int bytes, StreamType type) {
        final ResourceType resource = type.getResource();
        if (enableRecording(this, resource)) {
            bytes = distributeIntValue(bytes, resource);
            for (int i = 0; i < elements.length; i++) {
                if (enableRecording(elements[i], resource)) {
                    elements[i].writeIo(bytes, type);
                }
            }
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
    void setCpuTimeTicks(long cpuTimeTicks) {
        final ResourceType resource = ResourceType.CPU_TIME;
        if (enableRecording(this, resource)) {
            cpuTimeTicks = distributeLongValue(cpuTimeTicks, resource);
            for (int i = 0; i < elements.length; i++) {
                if (enableRecording(elements[i], resource)) {
                    elements[i].setCpuTimeTicks(cpuTimeTicks);
                }
            }
        }
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
        final ResourceType resource = ResourceType.NET_IO;
        if (enableRecording(this, resource)) {
            for (int i = 0; i < elements.length; i++) {
                if (enableRecording(elements[i], resource)) {
                    elements[i].addNetBytes(netInBytes, netOutBytes);
                }
            }
        }
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
    void timeCorrection(long timeDiff, long threadTimeFraction, 
        boolean decrement, long threadId) {
        final ResourceType resource = ResourceType.CPU_TIME;
        if (enableRecording(this, resource)) {
            int count = countAccountable(resource);
            if (count > 0) {
                timeDiff = timeDiff / count;
                threadTimeFraction = threadTimeFraction / count;
            }
            for (int i = 0; i < elements.length; i++) {
                if (enableRecording(elements[i], resource)) {
                    elements[i].timeCorrection(timeDiff, 
                        threadTimeFraction, decrement, threadId);
                }
            }
        }
    }

    /**
     * Returns the first non-null element.
     * 
     * @return the first element (might be <b>null</b>).
     * 
     * @since 1.00
     */
    protected RecorderElement firstElement() {
        RecorderElement elt = null;
        for (int i = 0; null == elt && i < elements.length; i++) {
            elt = elements[i];
        }
        return elt;
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
    public boolean wasRecorded() {
        boolean recorded = false;
        if (null != elements) {
            for (int i = 0; !recorded && i < elements.length; i++) {
                if (null != elements[i]) {
                    recorded = elements[i].wasRecorded();
                }
            }
        }
        return recorded;
    }
    
    /**
     * Returns the CPU time when recording was started.
     * 
     * @param threadId the identifier of the thread the start time should be 
     *   returned for
     * @return the time when recording was started
     * 
     * @since 1.00
     */
    public long getStartCpuTime(long threadId) {
        long result = 0;
        RecorderElement elt = firstElement();
        if (null != elt) {
            result = elt.getStartCpuTime(threadId);
        }
        return result;
    }

    /**
     * Returns the system time when recording was started.
     * 
     * @param threadId the identifier of the thread the start time should be 
     *   returned for
     * @return the time when recording was started
     * 
     * @since 1.00
     */
    public long getStartSystemTime(long threadId) {
        long result = 0;
        RecorderElement elt = firstElement();
        if (null != elt) {
            result = elt.getStartSystemTime(threadId);
        }
        return result;
    }

    /**
     * Returns the entire number of system time ticks recorded for this 
     * instance.
     * 
     * @return the number of system time ticks
     * 
     * @since 1.00
     */
    public long getSystemTimeTicks() {
        long ticks = 0;
        for (int i = 0; i < elements.length; i++) {
            if (null != elements[i]) {
                ticks += elements[i].getSystemTimeTicks();
            }
        }
        return ticks;
    }

    /**
     * Returns the entire number of CPU time ticks recorded for this 
     * instance.
     * 
     * @return the number of CPU time ticks
     * 
     * @since 1.00
     */
    public long getCpuTimeTicks() {
        long ticks = 0;
        for (int i = 0; i < elements.length; i++) {
            if (null != elements[i]) {
                ticks += elements[i].getCpuTimeTicks();
            }
        }
        return ticks;
    }
    
    /**
     * Returns the amount of memory allocated for this instance.
     * 
     * @return the amount of allocated memory
     */
    public long getMemAllocated() {
        long mem = 0;
        for (int i = 0; i < elements.length; i++) {
            if (null != elements[i]) {
                mem += elements[i].getMemAllocated();
            }
        }
        return mem;
    }
    
    /**
     * Returns the amount of memory currently being used, i.e. difference 
     * between allocated and unallocated memory.
     * 
     * @return the amount of memory being used
     */
    public long getMemUse() {
        long mem = 0;
        for (int i = 0; i < elements.length; i++) {
            if (null != elements[i]) {
                mem += elements[i].getMemUse();
            }
        }
        return mem;
    }
    
    /**
     * Returns the number of bytes read from files or network.
     * 
     * @return the number of bytes read from files or network
     * 
     * @since 1.00
     */
    public long getIoRead() {
        long io = 0;
        for (int i = 0; i < elements.length; i++) {
            if (null != elements[i]) {
                io += elements[i].getIoRead();
            }
        }
        return io;
    }

    /**
     * Returns the number of bytes written to files or network.
     * 
     * @return the number of bytes written to files or network
     * 
     * @since 1.00
     */
    public long getIoWrite() {
        long io = 0;
        for (int i = 0; i < elements.length; i++) {
            if (null != elements[i]) {
                io += elements[i].getIoWrite();
            }
        }
        return io;
    }
    
    /**
     * Returns the number of bytes read from network.
     * 
     * @return the number of bytes read from network
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.MONITOR_NET_IO)
    public long getNetIn() {
        long in = 0;
        for (int i = 0; i < elements.length; i++) {
            if (null != elements[i]) {
                in += elements[i].getNetIn();
            }
        }
        return in;
    }

    /**
     * Returns the number of bytes written to network.
     * 
     * @return the number of bytes written to network
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.MONITOR_NET_IO)
    public long getNetOut() {
        long out = 0;
        for (int i = 0; i < elements.length; i++) {
            if (null != elements[i]) {
                out += elements[i].getNetOut();
            }
        }
        return out;
    }

    /**
     * Returns the number of bytes read from files.
     * 
     * @return the number of bytes read from files
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.MONITOR_FILE_IO)
    public long getFileIn() {
        long in = 0;
        for (int i = 0; i < elements.length; i++) {
            if (null != elements[i]) {
                in += elements[i].getFileIn();
            }
        }
        return in;
    }

    /**
     * Returns the number of bytes written to files.
     * 
     * @return the number of bytes written to files
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.MONITOR_FILE_IO)
    public long getFileOut() {
        long out = 0;
        for (int i = 0; i < elements.length; i++) {
            if (null != elements[i]) {
                out += elements[i].getFileOut();
            }
        }
        return out;        
    }
    
    /**
     * Returns a stored recording element.
     * 
     * @param index the element to be returned
     * @return the element assigned to the given index
     * @throws ArrayIndexOutOfBoundsException if 
     *     <code>index&lt;0 || index&gt;={@link #getElementCount()}</code>
     * 
     * @since 1.00
     */
    public RecorderElement getElement(int index) {
        RecorderElement result;
        if (null == elements || index < 0 || index > elements.length) {
            result = this;
        } else {
            result = elements[index];
        }
        return result;
    }
    
    /**
     * Returns whether the given element is stored in this element.
     * 
     * @param rElt the element to search for
     * @return <code>true</code> if it is stored, <code>false</code> else
     * 
     * @since 1.00
     */
    public boolean hasElement(RecorderElement rElt) {
        boolean found = false;
        for (int i = 0; !found && i < elements.length; i++) {
            found = rElt == elements[i];
        }
        return found;
    }

    /**
     * Returns the number of stored recording elements.
     * 
     * @return the number of contributing recording elements
     * 
     * @since 1.00
     */
    public int getElementCount() {
        int result;
        if (null == elements) {
            result = 0;
        } else {
            result = elements.length;
        }
        return result;
    }
    
    /**
     * Changes a given element. This function does not enlarge the array so
     * call {@link #ensureSize(int, int)} before if needed.
     * 
     * @param index where to change the element
     * @param elt the new recorder element
     * @throws ArrayIndexOutOfBoundsException if <code>index &lt; 0 
     *     || index &gt;={@link #getElementCount()}</code>
     * 
     * @since 1.00
     */
    protected void setElement(int index, RecorderElement elt) {
        elements[index] = elt;
    }

    /**
     * Enlarges the array storing the elements if needed.
     * 
     * @param index the index to be used
     * @param max the expected maximum size
     * 
     * @since 1.00
     */
    protected void ensureSize(int index, int max) {
        if (null == elements) {
            elements = new RecorderElement[max]; 
        } else if (index > elements.length) {
            RecorderElement[] temp = 
                new RecorderElement[Math.max(index + 1, max)];
            System.arraycopy(elements, 0, temp, 0, elements.length);
            elements = temp;
        }
    }

    /**
     * Returns whether the given element should record the specified resource.
     * 
     * @param elt the element to be considered
     * @param resource the resource to be tested
     * @return <code>true</code> if recording should be done, 
     *     <code>false</code> else
     * 
     * @since 1.00
     */
    protected abstract boolean enableRecording(RecorderElement elt, 
        ResourceType resource);

}
