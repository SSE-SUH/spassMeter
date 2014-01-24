package de.uni_hildesheim.sse.monitoring.runtime.recordingStrategies;

import de.uni_hildesheim.sse.codeEraser.annotations.Operation;
import de.uni_hildesheim.sse.codeEraser.annotations.Variability;
import de.uni_hildesheim.sse.monitoring.runtime.AnnotationConstants;
import de.uni_hildesheim.sse.monitoring.runtime.boot.DebugState;
import de.uni_hildesheim.sse.monitoring.runtime.boot.GroupAccountingType;
import de.uni_hildesheim.sse.monitoring.runtime.boot.ResourceType;
import de.uni_hildesheim.sse.monitoring.runtime.boot.StreamType;
import de.uni_hildesheim.sse.monitoring.runtime.configuration.Configuration;
import de.uni_hildesheim.sse.monitoring.runtime.configuration.
    MonitoringGroupConfiguration;
import de.uni_hildesheim.sse.monitoring.runtime.plugins.IMonitoringGroup;

/**
 * Defines the element, i.e. the data to be recorded for a monitoring group.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public abstract class RecorderElement implements IMonitoringGroup {

    /**
     * Stores a numerical (unique) identification of the variant this
     * recorder element is assigned to.
     */
    private int varId;
    
    /**
     * Stores the configuration. In previous versions, different subclasses
     * of recorder elements distinguished among debug and non-debug instances
     * and configuration data was directly stored here. As we assume that
     * the default will be defined by the global configuration, this will
     * mostly be a reference to the default monitoring group configuration and
     * therefore this implementation will probably save memory in the average
     * case.
     */
    private MonitoringGroupConfiguration conf;
    
    /**
     * Creates a recorder element.
     * 
     * @param conf the group configuration (either from XML or from source code)
     * 
     * @since 1.00
     */
    protected RecorderElement(MonitoringGroupConfiguration conf) {
        this.conf = conf;
    }
    
    /**
     * Copies the data stored in <code>from</code> into this instance. (Deep 
     * copy)
     * 
     * @param from the instance from where to copy (may also be a subclass)
     * 
     * @since 1.00
     */
    public void copy(RecorderElement from) {
        this.conf = from.conf;
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
    abstract boolean startTimeRecording(long nanoTime, long threadTicks, 
        long threadId);
    
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
    abstract boolean stopTimeRecording(long nanoTime, long threadTicks, 
        long threadId);
    
    /**
     * Records an amount of memory allocated by the underlying recording group.
     * 
     * @param size the amount of memory allocated
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.MONITOR_MEMORY_ALLOCATED)
    abstract void memoryAllocated(long size);
    
    /**
     * Records an amount of memory freed within the underlying recording 
     * group (memory unallocation).
     * 
     * @param size the amount of memory freed
     * 
     * @since 1.00
     */
    @Variability(id = {AnnotationConstants.MONITOR_MEMORY_ALLOCATED, 
            AnnotationConstants.MONITOR_MEMORY_USAGE }, op = Operation.AND)
    abstract void memoryFreed(long size);

    /**
     * Updates freed memory from the JVM instead of recording this information
     * individually (memory unallocation). Should be used for the (entire) 
     * program record only.
     * 
     * @since 1.00
     */
    @Variability(id = {AnnotationConstants.MONITOR_MEMORY_ALLOCATED, 
            AnnotationConstants.MONITOR_MEMORY_USAGE }, op = Operation.AND)
    abstract void updateMemoryFreedFromJvm();
    
    /**
     * Returns the CPU time when recording was started.
     * 
     * @param threadId the identifier of the thread the start time should be 
     *   returned for
     * @return the time when recording was started, may be negative if disabled
     * 
     * @since 1.00
     */
    protected abstract long getStartCpuTime(long threadId);

    /**
     * Returns the system time when recording was started.
     * 
     * @param threadId the identifier of the thread the start time should be 
     *   returned for
     * @return the time when recording was started, may be negative if disabled
     * 
     * @since 1.00
     */
    protected abstract long getStartSystemTime(long threadId);
    
    /**
     * Records an amount of bytes read from file or from network.
     * 
     * @param bytes the amount of bytes read
     * @param type the type of stream used for reading
     * 
     * @since 1.00
     */
    @Variability(id = {AnnotationConstants.MONITOR_NET_IO, 
            AnnotationConstants.MONITOR_FILE_IO }, op = Operation.AND)
    abstract void readIo(int bytes, StreamType type);

    /**
     * Records an amount of bytes written to file or from network.
     * 
     * @param bytes the amount of bytes written
     * @param type the type of stream used for writing
     * 
     * @since 1.00
     */
    @Variability(id = {AnnotationConstants.MONITOR_NET_IO, 
            AnnotationConstants.MONITOR_FILE_IO }, op = Operation.AND)
    abstract void writeIo(int bytes, StreamType type);
    
    /**
     * Sets the current CPU time ticks for finishing recording of the entire
     * program.
     * 
     * @param cpuTimeTicks the current CPU time ticks
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.MONITOR_TIME_CPU)
    abstract void setCpuTimeTicks(long cpuTimeTicks);
    
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
    abstract void addNetBytes(long netInBytes, long netOutBytes);
    
    /**
     * Returns if this instance has the given <code>state</code> selected.
     * 
     * @param state the state to be tested
     * 
     * @return <code>true</code> if the state is selected, <code>false</code>
     *     else
     */
    @Variability(id = AnnotationConstants.DEBUG)
    public boolean hasDebugStates(DebugState state) {
        boolean result;
        DebugState[] states = conf.getDebug();
        if (0 == states.length) {
            result = false;
        } else {
            result = false;
            for (int i = 0; !result && i < states.length; i++) {
                result = states[i] == state;
            }
        }
        return result;
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
    abstract void timeCorrection(long timeDiff, long threadTimeFraction, 
        boolean decrement, long threadId);

    /**
     * Returns the currently relevant recorder element (in case that 
     * multiple subelements are supported).
     * 
     * @param index the index of the sub element (may be negative if not known)
     * @param factory a factory to create new elements if needed
     * @param max the (current) number of maximum required subelements 
     *     (variabilities)
     * @return the subelement or this instance
     * 
     * @since 1.00
     */
    abstract RecorderElement getContributing(int index, 
        RecorderElementFactory factory, int max);

    /**
     * Returns a contributing recording element, i.e. one of the elements
     * assigned to individual variants in a variability configuration.
     * 
     * @param index the element to be returned
     * @return the element assigned to the given index
     * @throws ArrayIndexOutOfBoundsException if 
     *     <code>index&lt;0 || index&gt;={@link #getContributingSize()}</code>
     * 
     * @since 1.00
     */
    public abstract RecorderElement getContributing(int index);
    
    /**
     * Returns the number of contributing recording elements, i.e. the elements
     * recording individual variants in a variability configuration.
     * 
     * @return the number of contributing recording elements
     * 
     * @since 1.00
     */
    public abstract int getContributingSize();
    
    /**
     * Changes the (unique) numerical identification of the variant this
     * recorder element is assigned to.
     * 
     * @param varId the new variability identification
     */
    void setVarId(int varId) {
        this.varId = varId;
    }

    /**
     * Returns the (unique) numerical identification of the variant this
     * recorder element is assigned to.
     * 
     * @return the new variability identification
     */
    public int getVarId() {
        return varId;
    }
    
    /**
     * Stores the accounting type.
     * 
     * @return the group accounting type
     */
    public GroupAccountingType getGroupAccounting() {
        return conf.getGroupAccounting();
    }
    
    /**
     * Stores the resources to be accounted.
     * 
     * @return the resources to be accounted (empty if all)
     */
    public ResourceType[] getResources() {
        return conf.getResources();
    }
    
    /**
     * Returns whether the specified resource should be accounted.
     * 
     * @param type the type of the resource to query for
     * @return <code>true</code> if it should be accounted, 
     *   <code>false</code> else
     * 
     * @since 1.00
     */
    public boolean accountResource(ResourceType type) {
        return ResourceType.contains(getResources(), type);
    }
    
    /**
     * Returns whether indirect accounting is activated, either in this 
     * instance or (if {@link #groupAccounting is GroupAccountingType#DEFAULT})
     * than in {@link Configuration#getGroupAccountingType()}.
     * 
     * @return <code>true</code> if indirect is activated, 
     *      <code>false</code> else
     * 
     * @since 1.00
     */
    public boolean isIndirectAccounting() {
        GroupAccountingType relevant;
        GroupAccountingType groupAccounting = getGroupAccounting();
        if (GroupAccountingType.DEFAULT == groupAccounting) {
            relevant = Configuration.INSTANCE.getGroupAccountingType();
        } else {
            relevant = groupAccounting;
        }
        return GroupAccountingType.INDIRECT == relevant;
    }
    
    /**
     * Returns the monitoring group configuration.
     * 
     * @return the configuration
     * 
     * @since 1.00
     */
    public MonitoringGroupConfiguration getConfiguration() {
        return conf;
    }
    
    /**
     * Factory method for creating test instances.
     * 
     * @param conf the monitoring group configuration
     * @return the created instance
     * 
     * @since 1.00
     */
    public static RecorderElement createForTest(
        MonitoringGroupConfiguration conf) {
        // ugly - refers to derived class!!!
        return new DefaultRecorderElement(conf);
    }
    
    /**
     * Returns whether this element is a pseudo element and should not be 
     * visible to the user.
     * 
     * @return <code>true</code> if this element is visible, <code>false</code>
     *   else
     * 
     * @since 1.00
     */
    public boolean isVisible() {
        return true;
    }
    
    /**
     * Checks the configuration of this recorder element and replaces it if
     * it is {@link MonitoringGroupConfiguration#STUB}.
     * 
     * @param conf the new configuration
     * 
     * @since 1.00
     */
    public void checkConf(MonitoringGroupConfiguration conf) {
        if (this.conf == MonitoringGroupConfiguration.STUB) {
            this.conf = conf;
        }
    }

}
