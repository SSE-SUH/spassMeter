package de.uni_hildesheim.sse.monitoring.runtime.recordingStrategies;

import de.uni_hildesheim.sse.monitoring.runtime.configuration.
    MonitoringGroupConfiguration;

/**
 * A factory for creating recorder elements. This class is needed, because
 * different concrete instances of recorder elements can be created, e.g. 
 * those collecting information on the contributing variants, with debug 
 * states, etc.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public interface RecorderElementFactory {
    
    /**
     * Creates a recorder element.
     * 
     * @param conf the configuration of the recorder element
     * @param forceDefaultInstances if <code>true</code> do only create the 
     *     default instances, otherways also those collecting contributing 
     *     variants can be created
     * @return the created instance
     * 
     * @since 1.00
     */
    public RecorderElement create(MonitoringGroupConfiguration conf, 
        boolean forceDefaultInstances);
    
    /**
     * Returns if the given <code>element</code> is adequate (e.g. supports 
     * debugging) or if a new instance must be created and information must be
     * taken over. This method is relevant, as instances may be created before
     * the annotated class is loaded, e.g. during automated variant detection.
     * 
     * @param element the element to be tested
     * @param conf the configuration associated with <code>element</code>
     * @return <code>true</code> if the instance is adequate, 
     *     <code>false</code> if <code>element</code> must be revised
     * 
     * @since 1.00
     */
    public boolean isOk(RecorderElement element, 
        MonitoringGroupConfiguration conf);

}