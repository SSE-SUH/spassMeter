package de.uni_hildesheim.sse.wildcat.services;

import javax.management.AttributeNotFoundException;

import de.uni_hildesheim.sse.monitoring.runtime.configuration.Configuration;
import de.uni_hildesheim.sse.serviceConstants.ConfigurationConstants;

/**
 * WildCAT service for the {@link Configuration} of the SPASS-monitor.
 * 
 * @author Stephan Dederichs
 * 
 * @version 1.00
 * @since 1.00
 */
public class WConfiguration extends AbstractWServiceData {

    /**
     * Creates an instance of {@link WConfiguration}.
     * 
     * @since 1.00
     */
    public WConfiguration() {
    }
    
    /**
     * Returns the outInterval of the configuration.
     * 
     * @return The outInterval of the configuration.
     * 
     * @since 1.00
     */
    public int getOutInterval() {
        int result = Configuration.INSTANCE.getOutInterval();
        setSensorValue(ConfigurationConstants.OUT_INTERVAL, result);
        return result;
    }
    
    /**
     * Sets the outInterval of the configuration.
     *  
     * @param outInterval The outInterval to set.
     * 
     * @since 1.00
     */
    public void setOutInterval(int outInterval) {
        setSensorValue(ConfigurationConstants.OUT_INTERVAL, outInterval);
        Configuration.INSTANCE.setOutInterval(outInterval);
    }
    
    /**
     * Returns the outFileName of the configuration.
     * 
     * @return The outFileName of the configuration.
     * 
     * @since 1.00
     */
    public String getOutFileName() {
        String result = Configuration.INSTANCE.getOutFileName();
        setSensorValue(ConfigurationConstants.OUT_FILE_NAME, result);
        return result;
    }
    
    // ------------------------------------------------------------------------
    
    @Override
    public void burstValueUpdate() {
        getOutInterval();
        getOutFileName();
    }
    
    @Override
    public Object getDataSpecificAttribute(String valueName) 
        throws AttributeNotFoundException {
        // Check for individual attributes
        if (valueName.equals(ConfigurationConstants.OUT_FILE_NAME)) {
            return getOutFileName();
        } else if (valueName.equals(ConfigurationConstants.OUT_INTERVAL)) {
            return getOutInterval();
        } else {
            throw new AttributeNotFoundException("Invalid attribute: "
                    + valueName);
        }
    }
    
}
