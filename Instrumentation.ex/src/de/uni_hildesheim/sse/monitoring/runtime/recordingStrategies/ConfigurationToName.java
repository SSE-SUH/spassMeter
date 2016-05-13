package de.uni_hildesheim.sse.monitoring.runtime.recordingStrategies;

import de.uni_hildesheim.sse.monitoring.runtime.utils.HashMap;

/**
 * Realizes a class which outputs for a given configuration (in binary String 
 * notation (see {@link RecorderElementMap}) a readable text form. Instances of 
 * this class are created on request by {@link RecorderElementMap}.
 * 
 * @author eichelberger
 * @since 1.00
 * @version 1.00
 */
public class ConfigurationToName {
    
    /**
     * Stores the correspondences between position in the configuration and 
     * variability at the respective positions. Within an entry, the mapping
     * between charId and its name is stored.
     */
    private PosEntry[] valToName;
    
    /**
     * Stops the not-available-string. Default value is <code>N/A</code>.
     */
    private String notAvailable = "N/A";

    /**
     * Stores an entry i9n the configuration denoting a concrete variability,
     * its binary-charId-to-variant-mapping and the name of the variability.
     * 
     * @author Holger Eichelberger
     * @since 1.00
     * @version 1.00
     */
    private class PosEntry {
        
        /**
         * Binary-charId-to-variant-mapping. All variants in this map
         * instantiate the same variability as "parent", thus have the same
         * variability-name-prefix.
         */
        private HashMap<Character, String> valuesToName;
        
        /**
         * Name ofthe variability.
         */
        private String confName;
    }

    /**
     * Creates an new configuration-to-name mapping.
     * 
     * @param size the number of variants in the configuration (length of the
     *     configuration)
     * 
     * @since 1.00
     */
    ConfigurationToName(int size) {
        valToName = new PosEntry[size];
    }
    
    /**
     * Modifies the text to be printed in case of not-available variants (i.e.
     * the variant was not detected automatically).
     * 
     * @param notAvailable the new not-available String
     * 
     * @since 1.00
     */
    public void setNotAvailable(String notAvailable) {
        this.notAvailable = notAvailable;
    }
    
    /**
     * Adds a variant to this formatting instance.
     * 
     * @param id the name (identification) of the variant given as 
     *     <i>variability-name</i><i><b>separator</b></i><i>variant-name</i>
     * @param separator the separator between variability and variant
     * @param index the position of the variability in the configuration
     * @param value the binary representant of the variant
     * 
     * @since 1.00
     */
    void put(String id, char separator, int index, char value) {
        PosEntry entry = valToName[index];
        if (null == entry) {
            entry = new PosEntry();
            valToName[index] = entry;
            entry.valuesToName = new HashMap<Character, String>();
            entry.confName = id;
            int pos = id.indexOf(separator);
            if (pos > 0) {
                entry.confName = id.substring(0, pos);
            }
        }
        entry.valuesToName.put(value, id);
    }
    
    /**
     * Returns a headline for configurations, i.e. the names of the variants
     * separated by <code>separator</code>.
     * 
     * @param separator the text for separating the variants (usually ",")
     * @return the headline for configurations
     * 
     * @since 1.00
     */
    public String configurationHeadline(String separator) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < valToName.length; i++) {
            if (null != valToName[i]) {
                result.append(valToName[i].confName);
            } else {
                result.append(notAvailable);
            }
            if (i < valToName.length) {
                result.append(separator);
            }
        }
        return result.toString();
    }
    
    /**
     * Formats a binary string notation of a configuration to readable text.
     * 
     * @param configuration the configuration to be printed
     * @param separator the separator to be used, e.g. "|"
     * @return the formatted configuration as readable text
     * 
     * @since 1.00
     */
    public String formatConfiguration(String configuration, String separator) {
        int len = configuration.length();
        if (len > valToName.length) {
            len = valToName.length;
        }
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < len; i++) {
            if (null != valToName[i]) {
                String name = valToName[i].valuesToName.get(
                    configuration.charAt(i));
                if (null == name) {
                    result.append(notAvailable);
                } else {
                    result.append(name);
                }
            } else {
                result.append(notAvailable);
            }
            if (i < valToName.length) {
                result.append(separator);
            }
        }
        return result.toString();
    }
    
    /**
     * Returns the name of the variant at the specified position.
     * 
     * @param index the index of the variant to be printed
     * @return the name of the variant
     * 
     * @since 1.00
     */
    public String getName(int index) {
        return valToName[index].confName;
    }

}
