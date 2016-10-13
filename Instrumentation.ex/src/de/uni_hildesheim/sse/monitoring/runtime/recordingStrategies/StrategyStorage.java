package de.uni_hildesheim.sse.monitoring.runtime.recordingStrategies;

import java.util.Arrays;

import de.uni_hildesheim.sse.monitoring.runtime.annotations.Helper;
import de.uni_hildesheim.sse.monitoring.runtime.boot.MonitoringGroupSettings;
import de.uni_hildesheim.sse.monitoring.runtime.configuration.Configuration;
import de.uni_hildesheim.sse.monitoring.runtime.configuration.
    MonitoringGroupConfiguration;
import de.uni_hildesheim.sse.monitoring.runtime.recording.ElschaLogger;
import de.uni_hildesheim.sse.monitoring.runtime.utils.HashMap;

/**
 * Implements the recorder strategy storage.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public class StrategyStorage implements RecorderElementFactory {

    /**
     * Stores the recorder elements and performs the internal mapping
     * between group ids and class names.
     */
    private RecorderElementMap recorderElements;

    /**
     * Stores if automatic variability detection is enabled.
     */
    private boolean variabilityDetectionEnabled = true;

    /**
     * Stores default recorder elements which should be available by
     * their name only (fix).
     */
    private HashMap<String, RecorderElement> defaults 
        = new HashMap<String, RecorderElement>();
    
    /**
     * Creates a new recorder strategy.
     * 
     * @since 1.00
     */
    public StrategyStorage() {
        recorderElements = new RecorderElementMap(this);
    }
    
    /**
     * Creates a recorder element.
     * 
     * @param conf the configuration to be considered for creating the element
     * @param forceDefaultInstances if <code>true</code> do only create the 
     *     default instances, otherways also those collecting contributing 
     *     variants can be created
     * @return the created instance
     * 
     * @since 1.00
     */
    @Override
    public RecorderElement create(MonitoringGroupConfiguration conf, 
        boolean forceDefaultInstances) {
        RecorderElement elt = null;
        if (!forceDefaultInstances 
            && Configuration.INSTANCE.measureVariantContributions()) {
            elt = new ContributingRecorderElement(conf);
        }
        // additional condition for variabilities
        if (null == elt) {
            elt = new DefaultRecorderElement(conf);
        }
        return elt;
    }
    
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
    @Override
    public boolean isOk(RecorderElement element, 
        MonitoringGroupConfiguration conf) {
        return true;
    }
    
    
    /**
     * Returns the set of recorder elements used by this strategy.
     * 
     * @return the recorder elements (map)
     * 
     * @since 1.00
     */
    public RecorderElementMap getRecorderElements() {
        return recorderElements;
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
        return recorderElements.getRecorderId(className);
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
        return recorderElements.getSeparatorChar();
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
    public void registerForRecording(String className, String recId, 
        MonitoringGroupConfiguration conf, MonitoringGroupSettings settings) {
    if (className.contains("FamilyElement")) {
        ElschaLogger.info("StrategyStorage.registerForRecording.1 for " + className + ", !recorderElements.containsKey(className) = " + !recorderElements.containsKey(className));
    }
        
        
        if (!recorderElements.containsKey(className)) {
            String[] id = settings.getId();
            Configuration.LOG.info("Mapping " + className + " -> " 
                + Arrays.toString(id));
        if (className.contains("FamilyElement")) {
            ElschaLogger.info("StrategyStorage.registerForRecording.2 for " + className + ", id = " + Arrays.toString(id));
        }
            if (null != id && id.length > 1) {
                RecorderElement[] elements = new RecorderElement[id.length];
                for (int i = 0; i < id.length; i++) {
                    if (null != id[i]) {
                        String rId = Helper.trimId(id[i]);
                        MonitoringGroupConfiguration rConf = Configuration.
                            INSTANCE.getMonitoringGroupConfiguration(rId);
                        if (null == rConf) {
                            // override on demand
                            rConf = MonitoringGroupConfiguration.STUB;
                        } else {
                            // testing consistency
                            if (!conf.isConsistent(rConf)) {
                                logConsistencyWarning(rId, Configuration.
                                    INSTANCE.getPseudoMapping(recId));
                            }
                        }
                        recorderElements.put(className, rId, rConf);
                        elements[i] = recorderElements.getAggregatedRecord(rId);
                    }
                }
                recorderElements.put(recId, elements, conf, settings);
            } else {
                if (null != id && 1 == id.length) {
                    recId = Helper.trimId(id[0]);
                }
                recorderElements.put(className, recId, conf);
                // testing consistency
                if (recorderElements.pseudoElementsSize() > 0) {
                    String outId = recId;
                    RecorderElement elt = recorderElements
                        .getAggregatedRecord(recId);
                    if (null == elt) {
                        elt = recorderElements.getAggregatedRecord(className);
                        outId = className;
                    }
                    if (null != elt) {
                        for (MultipleRecorderElement rElt 
                            : recorderElements.pseudoElements()) {
                            if (rElt.hasElement(elt) && !rElt.
                                getConfiguration().isConsistent(
                                    elt.getConfiguration())) {
                                logConsistencyWarning(outId, "at least one " 
                                    + "multiple group");
                            }
                        }
                    }
                }
            }
        }
    }   

    /**
     * Emits a consistency warning.
     * 
     * @param recId1 the first recorder id
     * @param recId2 the second recorder id
     * 
     * @since 1.00
     */
    private static void logConsistencyWarning(String recId1, String recId2) {
        Configuration.LOG.warning("monitoring group " 
            + "configuration of " + recId1 + " is not " 
            + "consistent with " + recId2);
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
        variabilityDetectionEnabled = enable;
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
        return variabilityDetectionEnabled;
    }
    
    /**
     * Returns the recorder element for the given recording group id.
     * Considers {@link #defaults}.
     * 
     * @param id the group identification
     * @return the recorder element or <b>null</b>
     * 
     * @since 1.00
     */
    protected RecorderElement getRecorderElement(String id) {
        RecorderElement elt = null;
        if (null != id) {
            elt = recorderElements.getAggregatedRecord(id);
            if (null == elt) {
                elt = defaults.get(id);
            }
        }
        return elt;
    }

    /**
     * Registers an default recorder elements which should be available by
     * its name only (fix).
     * 
     * @param recId the recorder id
     * @param elt the element
     */
    public void registerDefaultRecorderElement(String recId, 
        RecorderElement elt) {
        defaults.put(recId, elt);
    }
    
}
