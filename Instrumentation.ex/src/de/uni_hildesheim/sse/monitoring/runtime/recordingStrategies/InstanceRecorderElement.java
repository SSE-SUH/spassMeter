package de.uni_hildesheim.sse.monitoring.runtime.recordingStrategies;

import de.uni_hildesheim.sse.codeEraser.annotations.Operation;
import de.uni_hildesheim.sse.codeEraser.annotations.Variability;
import de.uni_hildesheim.sse.monitoring.runtime.AnnotationConstants;
import de.uni_hildesheim.sse.monitoring.runtime.boot.StreamType;
import de.uni_hildesheim.sse.monitoring.runtime.configuration.MonitoringGroupConfiguration;

/**
 * A sub-recorder element for a breakdown to individual instances.
 * 
 * @author Holger Eichelberger
 * @since 1.20
 * @version 1.20
 */
public class InstanceRecorderElement extends DefaultRecorderElement {

    /**
     * Stores the parent.
     */
    private RecorderElement parent;
    
    /**
     * Creates an instance.
     * 
     * @param conf the monitoring group configuration
     * @param parent the parent
     * 
     * @since 1.00
     */
    InstanceRecorderElement(MonitoringGroupConfiguration conf, RecorderElement parent) {
        super(conf);
        this.parent = parent;
    }

    @Override
    boolean startTimeRecording(long nanoTime, long threadTicks, long threadId) {
        boolean result = super.startTimeRecording(nanoTime, threadTicks, threadId);
        parent.startTimeRecording(nanoTime, threadTicks, threadId);
        return result;
    }
    
    @Override
    boolean stopTimeRecording(long nanoTime, long threadTicks, long threadId) {
        boolean result = super.stopTimeRecording(nanoTime, threadTicks, threadId);
        parent.stopTimeRecording(nanoTime, threadTicks, threadId);
        return result;
    }

    @Override
    void timeCorrection(long timeDiff, long threadTimeFraction, 
        boolean decrement, long threadId) {
        super.timeCorrection(timeDiff, threadTimeFraction, decrement, threadId);
        parent.timeCorrection(timeDiff, threadTimeFraction, decrement, threadId);
    }

    @Override
    @Variability(id = AnnotationConstants.MONITOR_MEMORY_ALLOCATED)
    void memoryAllocated(long size) {
        super.memoryAllocated(size);
        parent.memoryAllocated(size);
    }

    @Override
    @Variability(id = {AnnotationConstants.MONITOR_MEMORY_ALLOCATED, 
            AnnotationConstants.MONITOR_MEMORY_USAGE }, op = Operation.AND)
    void memoryFreed(long size) {
        super.memoryFreed(size);
        parent.memoryFreed(size);
    }

    @Variability(id = {AnnotationConstants.MONITOR_NET_IO, 
            AnnotationConstants.MONITOR_FILE_IO }, op = Operation.AND)
    @Override
    void readIo(int bytes, StreamType type) {
        super.readIo(bytes, type);
        parent.writeIo(bytes, type);
    }

    @Variability(id = {AnnotationConstants.MONITOR_NET_IO, 
            AnnotationConstants.MONITOR_FILE_IO }, op = Operation.AND)
    @Override
    void writeIo(int bytes, StreamType type) {
        super.writeIo(bytes, type);
        parent.writeIo(bytes, type);
    }
    
    @Variability(id = AnnotationConstants.MONITOR_TIME_CPU)
    @Override
    void setCpuTimeTicks(long cpuTimeTicks) {
        super.setCpuTimeTicks(cpuTimeTicks);
        parent.setCpuTimeTicks(cpuTimeTicks);
    }
    
    @Override
    void addNetBytes(long netInBytes, long netOutBytes) {
        super.addNetBytes(netInBytes, netOutBytes);
        parent.addNetBytes(netInBytes, netOutBytes);
    }

}
