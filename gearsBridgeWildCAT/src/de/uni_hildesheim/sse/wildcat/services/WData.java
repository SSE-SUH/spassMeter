package de.uni_hildesheim.sse.wildcat.services;

import javax.management.AttributeNotFoundException;

import de.uni_hildesheim.sse.codeEraser.annotations.Operation;
import de.uni_hildesheim.sse.codeEraser.annotations.Variability;
import de.uni_hildesheim.sse.serviceConstants.DataConstants;
import de.uni_hildesheim.sse.system.AccessPointData;
import de.uni_hildesheim.sse.system.AnnotationConstants;
import de.uni_hildesheim.sse.system.GathererFactory;
import de.uni_hildesheim.sse.system.IDataGatherer;

/**
 * Defines an object for delegation of the values from the interface
 * {@link IDataGatherer}. Additional to that it defines its WildCAT
 * service interface.
 * 
 * @author Stephan Dederichs
 * 
 * @since 1.00
 * @version 1.00
 */
@Variability(id = { AnnotationConstants.VAR_GATHER_DATA,
        AnnotationConstants.VAR_WIFI_DATA }, op = Operation.AND)
public class WData extends AbstractWServiceData implements IDataGatherer {

    /**
     * Instance of {@link IDataGatherer} for gathering required informations.
     * 
     * @since 1.00
     */
    @Variability(id = { AnnotationConstants.VAR_GATHER_DATA,
            AnnotationConstants.VAR_WIFI_DATA }, op = Operation.AND)
    private IDataGatherer dataGatherer;
    
    /**
     * Stores the timeout.
     * 
     * @since 1.00
     */
    private int timeout;
    
    /**
     * Constructor.
     * 
     * @since 1.00
     */
    @Variability(id = { AnnotationConstants.VAR_GATHER_DATA,
            AnnotationConstants.VAR_WIFI_DATA }, op = Operation.AND)
    public WData() {
        dataGatherer = GathererFactory.getDataGatherer();
        timeout = 0;
    }

    @Override
    @Variability(id = AnnotationConstants.VAR_WIFI_DATA)
    public AccessPointData[] gatherWifiSignals(int timeout) {
        AccessPointData[] result = dataGatherer.gatherWifiSignals(timeout);
        StringBuilder sb = new StringBuilder();
        for (AccessPointData a : result) {
            sb.append(a.toString());
        }
        setSensorValue(DataConstants.WIFI_SIGNALS, sb.toString());
        return result;
    }

    @Override
    public boolean supportsJVMTI() {
        boolean result = dataGatherer.supportsJVMTI();
        setSensorValue(DataConstants.SUPPORTS_JVMTI, result);
        return result;
    }

    @Override
    public boolean needsThreadRegistration() {
        return dataGatherer.needsThreadRegistration();
    }

    @Override
    public void registerThisThread(boolean arg0) {
        dataGatherer.registerThisThread(arg0);
    }
    
    // ------------------------------------------------------------------------
    
    @Override
    public void burstValueUpdate() {
        supportsJVMTI();
        getTimeout();
        gatherWifiSignals(timeout);
    }
    
    @Override
    public Object getDataSpecificAttribute(String valueName) 
        throws AttributeNotFoundException {
        // Check for individual attributes
        if (valueName.equals(DataConstants.SUPPORTS_JVMTI)) {
            return supportsJVMTI();
        } else if (valueName.equals(DataConstants.WIFI_SIGNALS)) {
            return gatherWifiSignals(timeout);
        } else if (valueName.equals(DataConstants.TIMEOUT)) {
            return getTimeout();
        } else {
            throw new AttributeNotFoundException("Invalid attribute: "
                    + valueName);
        }
    }
    
    /**
     * Returns the timeout value.
     *  
     * @return The timeout value.
     * 
     * @since 1.00
     */
    private int getTimeout() {
        int result = timeout;
        setSensorValue(DataConstants.TIMEOUT, result);
        return timeout;
    }

}
