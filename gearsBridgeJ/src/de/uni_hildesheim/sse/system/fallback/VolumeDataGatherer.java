package de.uni_hildesheim.sse.system.fallback;

import de.uni_hildesheim.sse.codeEraser.annotations.Variability;
import de.uni_hildesheim.sse.system.AnnotationConstants;
import de.uni_hildesheim.sse.system.IVolumeDataGatherer;

/**
 * Implements a class for requesting volume information from the system.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
@Variability(id = AnnotationConstants.VAR_VOLUME_DATA)
class VolumeDataGatherer implements IVolumeDataGatherer {

    @Override
    public long getVolumeCapacity() {
        return -1;
    }

    @Override
    public long getCurrentVolumeAvail() {
        return -1;
    }

    @Override
    public long getCurrentVolumeUse() {
        return -1;
    }

}
