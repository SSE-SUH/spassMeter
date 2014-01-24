package de.uni_hildesheim.sse.wildcat.services;

import javax.management.AttributeNotFoundException;

import de.uni_hildesheim.sse.codeEraser.annotations.Variability;
import de.uni_hildesheim.sse.serviceConstants.NetworkDataConstants;
import de.uni_hildesheim.sse.system.AnnotationConstants;
import de.uni_hildesheim.sse.system.GathererFactory;
import de.uni_hildesheim.sse.system.INetworkDataGatherer;

/**
 * Defines an object for delegation of the values from the interface
 * {@link INetworkDataGatherer}. Additional to that it defines its WildCAT
 * service interface.
 * 
 * @author Stephan Dederichs
 * 
 * @since 1.00
 * @version 1.00
 */
@Variability(id = AnnotationConstants.VAR_NETWORK_DATA)
public class WNetworkData extends AbstractWServiceData 
    implements INetworkDataGatherer {

    /**
     * Instance of {@link INetworkDataGatherer} for gathering required
     * informations.
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.VAR_NETWORK_DATA)
    private INetworkDataGatherer networkDataGetherer;
    
    /**
     * Constructor.
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.VAR_NETWORK_DATA)
    public WNetworkData() {
        networkDataGetherer = GathererFactory.getNetworkDataGatherer();
    }

    @Override
    @Variability(id = AnnotationConstants.VAR_NETWORK_DATA, value = "0")
    public long getCurrentNetSpeed() {
        long result = networkDataGetherer.getCurrentNetSpeed();
        setSensorValue(NetworkDataConstants.CURRENT_NET_SPEED, result);
        return result;
    }

    @Override
    @Variability(id = AnnotationConstants.VAR_NETWORK_DATA, value = "0")
    public long getMaxNetSpeed() {
        long result = networkDataGetherer.getMaxNetSpeed();
        setSensorValue(NetworkDataConstants.MAX_NET_SPEED, result);
        return result;
    }

    // ------------------------------------------------------------------------
    
    @Override
    public void burstValueUpdate() {
        getCurrentNetSpeed();
        getMaxNetSpeed();
    }
    
    @Override
    public Object getDataSpecificAttribute(String attributeName)
        throws AttributeNotFoundException {
        // Check for individual attributes
        if (attributeName.equals(NetworkDataConstants.CURRENT_NET_SPEED)) {
            return getCurrentNetSpeed();
        } else if (attributeName.equals(NetworkDataConstants.MAX_NET_SPEED)) {
            return getMaxNetSpeed();
        } else {
            throw new AttributeNotFoundException("Invalid attribute: "
                    + attributeName);
        }
    }

}
