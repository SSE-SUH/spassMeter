package de.uni_hildesheim.sse.system.deflt;

import de.uni_hildesheim.sse.codeEraser.annotations.Variability;
import de.uni_hildesheim.sse.system.AnnotationConstants;
import de.uni_hildesheim.sse.system.INetworkDataGatherer;

/**
 * Implements a class for requesting network information from the system.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
@Variability(id = AnnotationConstants.VAR_NETWORK_DATA)
class NetworkDataGatherer implements INetworkDataGatherer {

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
    public static native long getCurrentNetSpeed0();

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
    public static native long getMaxNetSpeed0();

    
    /**
     * Returns the (average) available speed of the currently 
     * enabled network device(s).
     * 
     * @return the current speed of the network in Bit per second
     *         (negative or zero if invalid)
     * 
     * @since 1.00
     */
    public long getCurrentNetSpeed() {
        return getCurrentNetSpeed0();
    }

    /**
     * Returns the maximum speed of the currently 
     * enabled network device(s).
     * 
     * @return the maximum speed of the network in Bit per second
     *         (negative or zero if invalid)
     * 
     * @since 1.00
     */
    public long getMaxNetSpeed() {
        return getMaxNetSpeed0();
    }
}
