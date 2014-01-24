package de.uni_hildesheim.sse.jmx.services.standard;

import de.uni_hildesheim.sse.codeEraser.annotations.Variability;
import de.uni_hildesheim.sse.system.AnnotationConstants;
import de.uni_hildesheim.sse.system.GathererFactory;
import de.uni_hildesheim.sse.system.INetworkDataGatherer;

/**
 * Defines an object for delegation of the values from the interface
 * {@link INetworkDataGatherer} to the {@link StandardMBeanAgent}.
 * 
 * @author Stephan Dederichs
 * 
 * @since 1.00
 * @version 1.00
 */
@Variability(id = AnnotationConstants.VAR_NETWORK_DATA)
public class NetworkData implements NetworkDataMBean {

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
    public NetworkData() {
        networkDataGetherer = GathererFactory.getNetworkDataGatherer();
    }

    @Override
    @Variability(id = AnnotationConstants.VAR_NETWORK_DATA, value = "0")
    public long getCurrentNetSpeed() {
        return networkDataGetherer.getCurrentNetSpeed();
    }

    @Override
    @Variability(id = AnnotationConstants.VAR_NETWORK_DATA, value = "0")
    public long getMaxNetSpeed() {
        return networkDataGetherer.getMaxNetSpeed();
    }

}
