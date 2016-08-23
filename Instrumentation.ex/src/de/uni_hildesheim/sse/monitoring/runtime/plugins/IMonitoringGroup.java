package de.uni_hildesheim.sse.monitoring.runtime.plugins;

import de.uni_hildesheim.sse.codeEraser.annotations.Operation;
import de.uni_hildesheim.sse.codeEraser.annotations.Variability;
import de.uni_hildesheim.sse.monitoring.runtime.AnnotationConstants;
import de.uni_hildesheim.sse.monitoring.runtime.recordingStrategies.RecorderElement;
import de.uni_hildesheim.sse.monitoring.runtime.utils.LongHashMap.MapElement;

/**
 * Provides a read-only interface to the data collected for a monitoring group
 * or a configuration (active set of monitoring groups). This interface is 
 * intended for use in the listener interfaces of SPASS-meter. Therefore, this 
 * interface does currently not provide access to (possible) subelements as 
 * they are reported via separate listener methods.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.20
 */
public interface IMonitoringGroup {

    /**
     * Returns if data on this instance was recorded or if it a dummy instance
     * created e.g. for some unused (automatic detected) variants.
     * 
     * @return <code>true</code> if valid data was recorded, <code>false</code>
     *    else
     * 
     * @since 1.00
     */
    public boolean wasRecorded();

    /**
     * Returns the entire number of system time ticks recorded for this 
     * instance.
     * 
     * @return the number of system time ticks
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.MONITOR_TIME_SYSTEM)
    public long getSystemTimeTicks();

    /**
     * Returns the entire number of CPU time ticks recorded for this 
     * instance.
     * 
     * @return the number of CPU time ticks
     * 
     * @since 1.00
     */
    public long getCpuTimeTicks();
    
    /**
     * Returns the amount of memory allocated for this instance.
     * 
     * @return the amount of allocated memory
     */
    @Variability(id = AnnotationConstants.MONITOR_MEMORY_ALLOCATED)
    public long getMemAllocated();
    
    /**
     * Returns the amount of memory currently being used, i.e. difference 
     * between allocated and unallocated memory.
     * 
     * @return the amount of memory being used
     */
    @Variability(id = {AnnotationConstants.MONITOR_MEMORY_ALLOCATED, 
            AnnotationConstants.MONITOR_MEMORY_USAGE }, op = Operation.AND)
    public long getMemUse();
        
    /**
     * Returns the number of bytes read from files or network.
     * 
     * @return the number of bytes read from files or network
     * 
     * @since 1.00
     */
    @Variability(id = {AnnotationConstants.MONITOR_NET_IO, 
            AnnotationConstants.MONITOR_FILE_IO }, op = Operation.AND)
    public long getIoRead();

    /**
     * Returns the number of bytes written to files or network.
     * 
     * @return the number of bytes written to files or network
     * 
     * @since 1.00
     */
    @Variability(id = {AnnotationConstants.MONITOR_NET_IO, 
            AnnotationConstants.MONITOR_FILE_IO }, op = Operation.AND)
    public long getIoWrite();

    /**
     * Returns the number of bytes read from network.
     * 
     * @return the number of bytes read from network
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.MONITOR_NET_IO)
    public long getNetIn();

    /**
     * Returns the number of bytes written to network.
     * 
     * @return the number of bytes written to network
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.MONITOR_NET_IO)
    public long getNetOut();

    /**
     * Returns the number of bytes read from files.
     * 
     * @return the number of bytes read from files
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.MONITOR_FILE_IO)
    public long getFileIn();

    /**
     * Returns the number of bytes written to files.
     * 
     * @return the number of bytes written to files
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.MONITOR_FILE_IO)
    public long getFileOut();
    
    /**
     * Returns an instance recorder element for recording how the data in this recorder element
     * is composed.
     * 
     * @param instanceId the instance identifier
     * @return the instance recorder element
     * 
     * @since 1.20
     */
    public RecorderElement getInstanceRecorderElement(long instanceId);

    /**
     * Returns all instance recorder elements.
     * 
     * @return the instance recorder elements
     * 
     * @since 1.20
     */
    public Iterable<MapElement<RecorderElement>> instanceRecorderElements();


    /**
     * Returns all instance recorder keys.
     * 
     * @return the instance recorder keys
     * 
     * @since 1.20
     */
    public long[] instanceRecorderIds();
    
}
