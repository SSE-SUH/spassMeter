package de.uni_hildesheim.sse.monitoring.runtime.recordingStrategies;

import de.uni_hildesheim.sse.monitoring.runtime.boot.MonitoringGroupSettings;
import de.uni_hildesheim.sse.monitoring.runtime.configuration.
    MonitoringGroupConfiguration;

/**
 * Defines a basic recorder strategy implementing common parts.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public abstract class AbstractRecorderStrategy implements RecorderStrategy {

    /**
     * Stores the strategy storage.
     */
    private StrategyStorage storage;
    
    /**
     * Creates a new recorder strategy.
     * 
     * @param storage the storage object
     * 
     * @since 1.00
     */
    public AbstractRecorderStrategy(StrategyStorage storage) {
        this.storage = storage;
    }
    
    /**
     * Returns the set of recorder elements used by this strategy.
     * 
     * @return the recorder elements (map)
     * 
     * @since 1.00
     */
    public RecorderElementMap getRecorderElements() {
        return storage.getRecorderElements();
    }
    
    /**
     * Maps a class name to its recorder id (called in this interface usually
     * <code>recId</code>).
     * 
     * @param className the class name to be mapped
     * @return the recorder id of <code>className</code>
     * 
     * @since 1.00
     */
    public String getRecorderId(String className) {
        return storage.getRecorderId(className);
    }
    
    /**
     * Returns the char used for separating the variability id and its 
     * current value (in configurations).
     * 
     * @return the separator char
     * 
     * @since 1.00
     */
    public char getVariabilitySeparatorChar() {
        return storage.getVariabilitySeparatorChar();
    }
    
    /**
     * Registers a given class for recording. Optional, a (group) 
     * <code>id</code> might be specified to which the measurements are 
     * assigned to. Ids are unique strings without inherent semantics. If
     * not given, the <code>className</code> is used for grouping and assigning
     * measurements (implicit 1-groups).
     * 
     * @param className the name of the class measurements should be registered 
     *        for
     * @param recId an optional group identification (may be empty or 
     *        <b>null</b>)
     * @param conf additional configuration for the monitoring group derived
     *        from <code>settings</code>
     * @param settings the monitoring group settings including all configuration
     *        information (reference should not be stored, will be freed 
     *        explicitly)
     *        
     * @since 1.00
     */
    @Override
    public void registerForRecording(String className, String recId, 
        MonitoringGroupConfiguration conf, MonitoringGroupSettings settings) {
        storage.registerForRecording(className, recId, conf, settings);
    }   

    /**
     * Enables or disables automatic variability detection.
     * 
     * @param enable <code>true</code> if variability detection should be 
     *   enabled, <code>false</code> else
     * 
     * @since 1.00
     */
    public void enableVariabilityDetection(boolean enable) {
        storage.enableVariabilityDetection(enable);
    }

    /**
     * Returns if automatic variability detection is enabled.
     * 
     * @return <code>true</code> if variability detection should be 
     *   enabled, <code>false</code> else
     * 
     * @since 1.00
     */
    public boolean isVariabilityDetectionEnabled() {
        return storage.isVariabilityDetectionEnabled();
    }
    
    /**
     * Returns the recorder element for the given recording group id.
     * 
     * @param id the group identification
     * @return the recorder element or <b>null</b>
     * 
     * @since 1.00
     */
    protected final RecorderElement getRecorderElement(String id) {
        return storage.getRecorderElement(id);
    }
    
    /**
     * Returns the attached storage.
     * 
     * @return the attached storage
     * 
     * @since 1.00
     */
    public StrategyStorage getStorage() {
        return storage;
    }

}
