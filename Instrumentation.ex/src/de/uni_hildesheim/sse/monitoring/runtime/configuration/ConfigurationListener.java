package de.uni_hildesheim.sse.monitoring.runtime.configuration;

/**
 * Notifies observers about selected configuration changes. To be registered in 
 * {@link ConfigurationListener}. Intended as internal notification mechanism 
 * only.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public interface ConfigurationListener {
    
    /**
     * Notifies observers about a changed out interval.
     * 
     * @param newValue the new out interval
     * 
     * @since 1.00
     */
    public void notifyOutIntervalChanged(int newValue);

}
