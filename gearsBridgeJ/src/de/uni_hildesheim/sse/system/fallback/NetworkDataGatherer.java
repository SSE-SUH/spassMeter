package de.uni_hildesheim.sse.system.fallback;

import de.uni_hildesheim.sse.codeEraser.annotations.Variability;
import de.uni_hildesheim.sse.system.AnnotationConstants;
import de.uni_hildesheim.sse.system.INetworkDataGatherer;

/**
 * Implements a class for requesting network information from the system.
 * 
 * @author Holger Eichelberger
 * @since 1.20
 * @version 1.20
 */
@Variability(id = AnnotationConstants.VAR_NETWORK_DATA)
class NetworkDataGatherer implements INetworkDataGatherer {
    
    @Override
    public long getCurrentNetSpeed() {
        return -1;
    }

    @Override
    public long getMaxNetSpeed() {
        return -1;
    }
}
