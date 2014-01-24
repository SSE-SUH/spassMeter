package de.uni_hildesheim.sse.monitoring.runtime.plugins;

/**
 * Represents a set of plugin parameters (unrecognized parameter from the
 * command line).
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public interface IPluginParameter {
    
    /**
     * Returns the plugin parameter for the given configuration key.
     * 
     * @param key the key to return the configuration value for
     * @return the configuration value or <b>null</b> if not found
     * 
     * @since 1.00
     */
    public String get(String key);

}
