package de.uni_hildesheim.sse.monitoring.runtime.recordingStrategies;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.StringTokenizer;

import de.uni_hildesheim.sse.monitoring.runtime.annotations.Helper;
import de.uni_hildesheim.sse.monitoring.runtime.boot.ArrayList;
import de.uni_hildesheim.sse.monitoring.runtime.boot.BooleanValue;
import de.uni_hildesheim.sse.monitoring.runtime.boot.MonitoringGroupSettings;
import de.uni_hildesheim.sse.monitoring.runtime.configuration.Configuration;
import de.uni_hildesheim.sse.monitoring.runtime.configuration.
    MonitoringGroupConfiguration;
import de.uni_hildesheim.sse.monitoring.runtime.plugins.
    MonitoringGroupCreationListener;
import de.uni_hildesheim.sse.monitoring.runtime.plugins.PluginRegistry;
import de.uni_hildesheim.sse.monitoring.runtime.utils.HashMap;
import de.uni_hildesheim.sse.monitoring.runtime.utils.HashMap.Entry;

/**
 * Implements a double indirected map which may assign {@link RecorderElement 
 * recorder elements} to class names which then can be referenced by ids, i.e.
 * a recorder element may be retrieved by its class name or by a grouping id 
 * which represents multiple class names.<br/>
 * The interface of this class is incomplete as (nearly) only required methods
 * are realized (pragmatic agility).
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
// TODO: separate concerns!
public class RecorderElementMap {

    /**
     * Stores the class name to group-id mappings. Multiple class names might
     * be assigned to one id.
     */
    private HashMap<String, String> classNameToId = 
        new HashMap<String, String>();
    
    /**
     * Stores the class name / id mapping to recorder elements.
     */
    private HashMap<String, RecorderElement> mappedClasses = 
        new HashMap<String, RecorderElement>();
    
    /**
     * Stores the recorder ids (in sequence of registration).
     */
    private ArrayList<String> recorderIds = 
        new ArrayList<String>();

    /**
     * Stores the records for the individual configurations.
     */
    private HashMap<String, RecorderElement> mappedConfigurations =
        new HashMap<String, RecorderElement>();
    
    /**
     * Stores the current configuration record for fast access.
     */
    private RecorderElement currentConfigurationRecord = null;
    
    /**
     * Stores the records for the recording ids.
     */
    private HashMap<String, PositionRecord> idToConfiguration = 
        new HashMap<String, PositionRecord>();

    /**
     * Stores the last recently used mappings between variability (i.e. 
     * the substring before {@link #separatorChar}) and the assigned position.
     */
    private HashMap<String, PositionRecord> lruRecords = 
        new HashMap<String, PositionRecord>();

    /**
     * Stores the variability identification to integer mapping (simplified
     * recognition of variabilities during assignment to recording elements).
     */
    private HashMap<String, Integer> varIds = 
        new HashMap<String, Integer>();
    
    /**
     * Stores the pseudo recorder elements.
     */
    private LinkedList<MultipleRecorderElement> pseudoElt 
        = new LinkedList<MultipleRecorderElement>();
    
    /**
     * Stores the current configuration as array of numbers (can be transformed
     * to a string as map key).
     */
    private char[] currentConfiguration = new char[0];
    
    /**
     * Stores the variant separator, i.e. the separator to be used between 
     * variability and variant (e.g. drawer@none).
     */
    private char separatorChar = '@';

    /**
     * Stores the variability separator, i.e. the separator to be used between
     * two variabilities.
     */
    private char variabilitySeparatorChar = ',';
    
    /**
     * Stores the variability separator, i.e. the separator to be used between
     * two variabilities.
     */
    private String variabilitySeparator 
        = Character.toString(variabilitySeparatorChar);
    
    /**
     * Stores the recorder element factory.
     */
    private RecorderElementFactory factory;
    
    /**
     * Creates a new map.
     * 
     * @param factory the recorder element factory
     * 
     * @since 1.00
     */
    public RecorderElementMap(RecorderElementFactory factory) {
        if (null == factory) {
            throw new IllegalArgumentException("factory must not be null");
        }
        this.factory = factory;
    }

    /**
     * Returns the char separating variabilities from concrete variants in 
     * recorder ids.
     * 
     * @return the separating character
     * 
     * @since 1.00
     */
    public char getSeparatorChar() {
        return separatorChar;
    }
    
    /**
     * Returns the part of the <code>recId</code> representing the variability 
     * identification.
     * 
     * @param recId the recording identification
     * @return the variability identification or <b>null</b> if not applicable
     * 
     * @since 1.00
     */
    public String getVariabilityId(String recId) {
        String result;
        if (null != recId) {
            // use this or the containing top id
            int pos = recId.indexOf(variabilitySeparatorChar);
            if (pos > 0) {
                // consider the @ in the prefix!
                result = recId.substring(0, pos + 1);
            } else {
                result = null;
            }
        } else {
            result = null;
        }
        return result;
    }
    
    /**
     * Returns the numerical identification of the variability identification
     * (retrieved by {@link #getVariabilityId(String)}.
     * 
     * @param recId the recording identification
     * @return the (unique) numerical identification, a negative number if none 
     *   was assigned
     * 
     * @since 1.00
     */
    public int getVariabilityIdNum(String recId) {
        int result = -1;
        String varId = getVariabilityId(recId);
        if (null != varId) {
            Integer numId = varIds.get(varId);
            if (null != numId) {
                result = numId;
            }
        }
        return result;
    }

    /**
     * Adds a pseudo recorder element which represents multiple recorder 
     * elements.
     * 
     * @param recId the unique (pseudo) identification
     * @param elements the elements
     * @param conf the recorder configuration
     * @param settings the monitoring group settings including all configuration
     *        information (reference should not be stored, will be freed 
     *        explicitly)
     * 
     * @since 1.00
     */
    public void put(String recId, RecorderElement[] elements, 
        MonitoringGroupConfiguration conf, MonitoringGroupSettings settings) {
        
        // called infrequently and only if required -> postpone translation
        BooleanValue tmp = settings.getDistributeValues();
        boolean distributeValues;
        if (BooleanValue.DEFAULT == tmp) {
            distributeValues = Configuration.INSTANCE.
                multiGroupsDistributeValues();
        } else  {
            distributeValues = (tmp == BooleanValue.TRUE);
        }

        tmp = settings.getConsiderContained();
        boolean considerContained;
        if (BooleanValue.DEFAULT == tmp) {
            considerContained = Configuration.INSTANCE.
                multiGroupsConsiderContained();
        } else  {
            considerContained = (tmp == BooleanValue.TRUE);
        }
        
        MultipleRecorderElement elt = new MultipleRecorderElement(conf, 
            elements, distributeValues, considerContained);
        mappedClasses.put(recId, elt);
        pseudoElt.addLast(elt);
//        pseudoElt.add(elt);
    }
    
    /**
     * Returns the pseudo elements.
     * 
     * @return the pseudo elements.
     * 
     * @since 1.00
     */
    public LinkedList<MultipleRecorderElement> pseudoElements() {
        return pseudoElt;
    }
    
    /**
     * Returns the current number of pseudo elements.
     * 
     * @return the current number of pseudo elements
     * 
     * @since 1.00
     */
    public int pseudoElementsSize() {
        return pseudoElt.size();
    }
    
    /**
     * Stores a new recorder element. Optional, a (group) 
     * <code>id</code> might be specified to which the measurements are 
     * assigned to. Ids are unique strings without inherent semantics. If
     * not given, the <code>className</code> is used for grouping and assigning
     * measurements (implicit 1-groups).
     * 
     * @param className the name of the class measurements should be registered 
     *   for
     * @param id an optional group identification (may be empty or <b>null</b>)
     * @param conf an optional monitoring group configuration associated with 
     *   <code>id</code> (may be <b>null</b>)
     * 
     * @since 1.00
     */
    public void put(String className, String id, 
        MonitoringGroupConfiguration conf) {
        String mappingName = className;
        if (null != id && id.length() > 0) {
            mappingName = id;
            if (null != className) {
                classNameToId.put(className, id);
            }
        }
        int varId = -1;
        if (null != mappingName && !mappedClasses.containsKey(mappingName)) {
            PositionRecord posRecord = null;
            boolean isOverhead 
                = mappingName.equals(Helper.RECORDER_ID);
            if (!idToConfiguration.containsKey(mappingName)
                && Configuration.INSTANCE.measureVariantContributions()
                && !isOverhead) {
                
                String idName = null;
                char idValue = 1; // always active
                int confPos = -1;
                recorderIds.add(mappingName);
                
                // identify variability and value via separatorChar
                // identify last recently used value and add 1
                if (0 != separatorChar) {
                    int pos = mappingName.indexOf(separatorChar);
                    if (pos > 0) {
                        idName = mappingName.substring(0, pos);
                        if (!varIds.containsKey(idName)) {
                            varIds.put(idName, varIds.size());
                        } 
                        varId = varIds.get(idName);

                        //idValue = mappingName.substring(pos + 1);
                        PositionRecord lru = lruRecords.get(idName);
                        if (null != lru) {
                            idValue = (char) (lru.value + 1);
                            confPos = lru.index;
                        } 
                    }
                } else {
                    idName = mappingName;
                }
                
                // create position record and extend configuration accordingly
                posRecord = new PositionRecord();
                lruRecords.put(idName, posRecord);
                idToConfiguration.put(mappingName, posRecord);
                posRecord.value = idValue;
                if (confPos < 0) {
                    confPos = lruRecords.size() - 1;
                    char[] tmp = new char[lruRecords.size()];
                    System.arraycopy(currentConfiguration, 0, tmp, 0, 
                        currentConfiguration.length);
                    currentConfiguration = tmp;
                }
                posRecord.index = confPos;
            }
            RecorderElement registered = mappedClasses.get(mappingName);
            if (null != registered) {
                if (!factory.isOk(registered, conf)) {
                    RecorderElement newElement 
                        = factory.create(conf, true);
                    newElement.copy(registered);
                    mappedClasses.put(mappingName, newElement);
                }
            } else {
                registered = factory.create(conf, true);
                mappedClasses.put(mappingName, registered);
            }
            registered.checkConf(conf);
            registered.setVarId(varId);
            MonitoringGroupCreationListener listener 
                = PluginRegistry.getMonitoringGroupCreationListener();
            if (null != listener) {
                if (null != posRecord) {
                    listener.configurationCreated(mappingName, registered);
                } else {
                    listener.monitoringGroupCreated(mappingName, registered);
                }
            }
        }
    }
    
    /**
     * Changes the current configuration record, i.e. caches the record
     * assigned to the current configuration.
     * 
     * @since 1.00
     */
    private void setCurrentConfigurationRecord() {
        String confId = new String(currentConfiguration);
        currentConfigurationRecord = 
            mappedConfigurations.get(confId);
        if (null == currentConfigurationRecord) {
            currentConfigurationRecord = factory.create(
                MonitoringGroupConfiguration.DEFAULT, false);
            mappedConfigurations.put(confId, currentConfigurationRecord);
        }
    }
    
    /**
     * Notifies this class about explicitly entering a configuration, i.e. the
     * change of an individual variability (used for automatic detection).
     * 
     * @param id the new configuration entry (variability@variant)
     * 
     * @since 1.00
     */
    public void enterConfiguration(String id) {
        PositionRecord pos = idToConfiguration.get(id);
        if (null != pos && currentConfiguration[pos.index] != pos.value) {
            currentConfiguration[pos.index] = pos.value;
            setCurrentConfigurationRecord();
        }
    }

    /**
     * Changes the entire variability configuration, i.e. sets all currently
     * active variants new. This is used for manual variability recording.
     * 
     * @param ids the recording ids for the currently active configuration
     * @param force if <code>true</code> dummy recording elements are created
     *     for recording groups which have not been registered so far (but which
     *     probably will during the run of the monitored program, if <code>false
     *     </code> only registered ids will be considered (may lead to 
     *     incomplete configurations)
     * 
     * @since 1.00
     */
    public void enterCompleteConfiguration(String ids, boolean force) {
        StringTokenizer tokens = new StringTokenizer(ids, variabilitySeparator);
        boolean changed = false;
        while (tokens.hasMoreTokens()) {
            String id = tokens.nextToken();
            if (force && !idToConfiguration.containsKey(ids)) {
                put(null, id, MonitoringGroupConfiguration.DEFAULT);
            }
            PositionRecord pos = idToConfiguration.get(id);
            if (null != pos && currentConfiguration[pos.index] != pos.value) {
                currentConfiguration[pos.index] = pos.value;
                changed = true;
            } else if (null == pos) {
                System.err.println(
                    "enterComplete: id not found for configuration: " + id);
            }
        }
        if (changed) {
            setCurrentConfigurationRecord();
        }
    }
    
    /**
     * Information to be stored for a recording id on the index into the
     * configuration string and the related char value.
     * 
     * @author Holger Eichelberger
     * @since 1.00
     * @version 1.00
     */
    private static class PositionRecord {
        
        /**
         * The index in the configuration string.
         */
        private int index;
        
        /**
         * The number of the variant as char.
         */
        private char value;
    }
    
    /**
     * Returns the current configuration record.
     * 
     * @param recId the requested variant at the specified position
     * @return the related recorder element recording the variant
     * 
     * @since 1.00
     */
    public RecorderElement getCurrentConfigurationRecord(String recId) {
        RecorderElement result = currentConfigurationRecord;
        if (null != result 
            && Configuration.INSTANCE.measureVariantContributions()) {
            PositionRecord pos = idToConfiguration.get(recId);
            if (null != pos) {
                int size = result.getContributingSize();
                result = result.getContributing(pos.index, factory, 
                    currentConfiguration.length);
                if (size != result.getContributingSize())  {
                    MonitoringGroupCreationListener listener 
                        = PluginRegistry.getMonitoringGroupCreationListener();
                    if (null != listener) {
                        String cName = getConfigurationMapping().getName(
                            pos.index);
                        listener.contributionCreated(recId, cName, result);
                    }
                }
            }
        } 
        return result;
    }
    
    /**
     * Returns a possible recording id for the given <code>recId</code>.
     * If <code>recId</code> denotes a recoding group, the recorder element
     * for that group is returned. Otherways, <code>recId</code> is considered
     * as a possible prefix to a variant (if enabled) and the related active
     * variant is returned.
     * 
     * @param recId the recorder id or a recorder id prefix
     * @return the related recorder id (or <b>null</b> in case of none)
     * 
     * @since 1.00
     */
    public String getPossibleAggregatedRecorderId(String recId) {
        String result = recId;
        if (null == getAggregatedRecord(recId)) {
            // TODO optimize
            // search for variant starting with the given recId as prefix
            for (HashMap.Entry<String, PositionRecord> entry 
                : idToConfiguration.entries()) {
                String id = entry.getKey();
                if (id.startsWith(recId)) {
                    PositionRecord rec = entry.getValue();
                    if (rec.value == currentConfiguration[rec.index]) {
                        result = id;
                        break;
                    }
                }
            }
        }
        return result;
    }
    
    /**
     * Returns the current configuration identification. To map back this strg
     * 
     * @return the current configuration identification (each char is a number 
     *   representing the internal id of the variant)
     * 
     * @since 1.00
     */
    public String getCurrentConfigurationId() {
        return new String(currentConfiguration);
    }
    
    /**
     * Returns an instance which is responsible for mapping back a configuration
     * string to a readable configuration description.
     * 
     * @return the (reusable) instance being responsible for configuration 
     *     mapping
     * 
     * @since 1.00
     */
    public ConfigurationToName getConfigurationMapping() {
        ConfigurationToName result = 
            new ConfigurationToName(currentConfiguration.length);
        for (HashMap.Entry<String, PositionRecord> entry 
            : idToConfiguration.entries()) {
            PositionRecord rec = entry.getValue();
            result.put(entry.getKey(), separatorChar, rec.index, rec.value);
        }
        return result;
    }

    /**
     * Maps a class name back to the key used for {@link #mappedClasses}.
     * This method queries {@link #classNameToId} if an id was registered
     * for <code>className</code>. If an id was registered, 
     * {@link #mappedClasses} is queried with that id
     * 
     * @param className the class name to be searched for
     * @return the key in {@link #mappedClasses}
     * 
     * @since 1.00
     */
    private String getId(String className) {
        String id = classNameToId.get(className);
        if (null == id) {
            id = className;
        }
        return id;
    }
    
    /**
     * Returns the recording id for the given class name. This method considers
     * registration of classes in monitoring groups.
     * 
     * @param className the class name to look for
     * @return the recording id
     * 
     * @since 1.00
     */
    public String getRecorderId(String className) {
        String id = getId(className);
        if (!mappedClasses.containsKey(id)) {
            id = null;
        }
        return id;
    }
    
    /**
     * Does this map contains a (redirected) entry for <code>className</code>?
     * 
     * @param recId the recorder identification
     * @return <code>true</code> if this map contains an entry, 
     *     <code>false</code> else
     * 
     * @since 1.00
     */
    public boolean containsKey(String recId) {
        return mappedClasses.containsKey(recId);
    }

    /**
     * Returns the (redirected) entry assigned to <code>recId</code>?
     * 
     * @param recId the recorder identification
     * @return the entry or <b>null</b> if not found
     * 
     * @since 1.00
     */
    public RecorderElement getAggregatedRecord(String recId) {
        return mappedClasses.get(recId);
    }
    
    /**
     * Returns all mappings to recorder elements. Keys might be ids or class 
     * names.
     * 
     * @return all mappings to recorder elements
     * 
     * @since 1.00
     */
    public Iterable<Entry<String, RecorderElement>> idToRecordingSet() {
        return mappedClasses.entries();
    }
    
    /**
     * Returns whether recorder elements have been stored (not 
     * classname-id-mappings).
     * 
     * @return <code>true</code> if recorder elements have been stored, 
     *      <code>false</code> else
     * 
     * @since 1.00
     */
    public boolean isEmpty() {
        return mappedClasses.isEmpty();
    }
    
    /**
     * Returns all stored recorder elements.
     * 
     * @return all stored recorder elements
     * 
     * @since 1.00
     */
    public Iterable<RecorderElement> values() {
        return mappedClasses.values();
    }

    /**
     * Returns all stored recorder ids (in sequence of registration).
     * 
     * @return all stored recorder ids
     * 
     * @since 1.00
     */
    public Iterator<String> recorderIds() {
        return recorderIds.iterator();
    }
    
    /**
     * Returns all ids used for internal mapping.
     * 
     * @return all ids
     * 
     * @since 1.00
     */
    /*public Collection<String> idKeys() {
        return classNameToId.keySet();
    }*/
    
    /**
     * Returns the set of all configurations recorded until now.
     * 
     * @return the set of all configurations
     * 
     * @since 1.00
     */
    public Iterable<String> configurations() {
        return mappedConfigurations.keys();
    }
    
    /**
     * Returns the bindings between configurations and recording elements.
     * 
     * @return the bindings
     * 
     * @since 1.00
     */
    public Iterable<Entry<String, RecorderElement>> configurationToRecording() {
        return mappedConfigurations.entries();
    }
    
    /**
     * Returns the current length of the configuration. May change over time in
     * case of automated detection of variabilities.
     * 
     * @return the length of the configuration
     * 
     * @since 1.00
     */
    public int getConfigurationLength() {
        return currentConfiguration.length;
    }
    
    /**
     * Formats a configuration key to a string, i.e. converts chars to numbers
     * and inserts a separator char.
     * 
     * @param key the key to be formatted
     * @param check if <code>true</code> emit only valid configurations
     * @return a representation of the configuration, <b>null</b> else
     * 
     * @since 1.00
     */
    public String configurationKeyToString(String key, boolean check) {
        String result = null;
        if (!check || key.length() == currentConfiguration.length) {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < key.length(); i++) {
                int value = (int) key.charAt(i);
                if (check && 0 == value) {
                    builder = null;
                    break;
                }
                builder.append(value);
                if (i < key.length()) {
                    builder.append("|");
                }
            }
            if (null != builder) {
                result = builder.toString();
            }
        }
        return result;
    }
    
    /**
     * Returns the recording id for the given recorder <code>element</code>.
     * This method is intended for <b>debugging only</b>.
     * 
     * @param element the recorder element
     * @return the assigned recording identification (may be <b>null</b>)
     * 
     * @since 1.00
     */
    // debug only
    String getRecorderId(RecorderElement element) {
        String result = null;
        if (null != element) {
            Iterator<HashMap.Entry<String, RecorderElement>> iter 
                = mappedClasses.entries().iterator();
            while (null == result && iter.hasNext()) {
                HashMap.Entry<String, RecorderElement> entry = iter.next();
                if (entry.getValue() == element) {
                    result = entry.getKey();
                }
            }
        }
        return result;
    }
    
}
