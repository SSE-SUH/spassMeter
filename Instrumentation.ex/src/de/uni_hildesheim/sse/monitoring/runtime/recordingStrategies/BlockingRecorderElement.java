package de.uni_hildesheim.sse.monitoring.runtime.recordingStrategies;

import de.uni_hildesheim.sse.codeEraser.annotations.Variability;
import de.uni_hildesheim.sse.monitoring.runtime.AnnotationConstants;
import de.uni_hildesheim.sse.monitoring.runtime.boot.DebugState;
import de.uni_hildesheim.sse.monitoring.runtime.boot.GroupAccountingType;
import de.uni_hildesheim.sse.monitoring.runtime.boot.ResourceType;
import de.uni_hildesheim.sse.monitoring.runtime.boot.StreamType;
import de.uni_hildesheim.sse.monitoring.runtime.configuration.
    MonitoringGroupConfiguration;

/**
 * Implements a blocking recorder element which just does nothing and prevents
 * recording. This is used to realize excluded methods and their call tree.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public class BlockingRecorderElement extends RecorderElement {

    /**
     * Creates a recorder element.
     * 
     * @since 1.00
     */
    protected BlockingRecorderElement() {
        super(MonitoringGroupConfiguration.DEFAULT);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean wasRecorded() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getSystemTimeTicks() {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getCpuTimeTicks() {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getMemAllocated() {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getMemUse() {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getStartCpuTime(long threadId) {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getStartSystemTime(long threadId) {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getIoRead() {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getIoWrite() {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getNetIn() {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getNetOut() {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getFileIn() {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getFileOut() {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    boolean startTimeRecording(long nanoTime, long threadTicks, long threadId) {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    boolean stopTimeRecording(long nanoTime, long threadTicks, long threadId) {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    void memoryAllocated(long size) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    void memoryFreed(long size) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    void updateMemoryFreedFromJvm() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    void readIo(int bytes, StreamType type) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    void writeIo(int bytes, StreamType type) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    void setCpuTimeTicks(long cpuTimeTicks) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    void addNetBytes(long netInBytes, long netOutBytes) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    void timeCorrection(long timeDiff, long threadTimeFraction,
        boolean decrement, long threadId) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    RecorderElement getContributing(int index, RecorderElementFactory factory,
        int max) {
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RecorderElement getContributing(int index) {
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getContributingSize() {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isIndirectAccounting() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean accountResource(ResourceType type) {
        return false;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public GroupAccountingType getGroupAccounting() {
        return GroupAccountingType.DIRECT;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ResourceType[] getResources() {
        return ResourceType.SET_NONE;
    }

    /**
     * {@inheritDoc}
     */
    @Variability(id = AnnotationConstants.DEBUG)
    public boolean hasDebugStates(DebugState state) {
        return false;
    }

}
