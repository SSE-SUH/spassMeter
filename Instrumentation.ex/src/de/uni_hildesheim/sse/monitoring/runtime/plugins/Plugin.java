package de.uni_hildesheim.sse.monitoring.runtime.plugins;

/**
 * An interface for plugins to be initialized upon start of the monitoring 
 * framework (either by referring to their class name or reading it from
 * the system property spassMeter.plugins, comma separated). Plugins must 
 * provide a default constructor!
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public interface Plugin {

    /**
     * Is called to (re)start the plugin.
     * 
     * @param args configuration parameters (key, value) unrecognized during
     *     command line parsing by SPASS-meter
     * 
     * @since 1.00
     */
    public void start(IPluginParameter args);

    /**
     * Is called to (re)end the plugin.
     * 
     * @since 1.00
     */
    public void stop();
    
}
