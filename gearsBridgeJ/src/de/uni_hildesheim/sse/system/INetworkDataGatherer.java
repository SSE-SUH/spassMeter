package de.uni_hildesheim.sse.system;

import de.uni_hildesheim.sse.codeEraser.annotations.Variability;

/**
 * Defines an interface for requesting network information from the system.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
@Variability(id = AnnotationConstants.VAR_NETWORK_DATA)
public interface INetworkDataGatherer {

    /**
     * Returns the (average) available speed of the currently 
     * enabled network device(s).
     * 
     * @return the current speed of the network in Bit per second
     *         (negative or zero if invalid)
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.VAR_NETWORK_DATA, value = "0")
    public long getCurrentNetSpeed();

    /**
     * Returns the maximum speed of the currently 
     * enabled network device(s).
     * 
     * @return the maximum speed of the network in Bit per second
     *         (negative or zero if invalid)
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.VAR_NETWORK_DATA, value = "0")
    public long getMaxNetSpeed();
}
