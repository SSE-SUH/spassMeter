package de.uni_hildesheim.sse.system.deflt;

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

    /**
     * Returns the maximum volume capacity.
     * 
     * @return the maximum volume capacity in bytes
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.VAR_VOLUME_DATA, value = "0")
    private static native long getVolumeCapacity0();

    /**
     * Returns the currently available volume capacity.
     * 
     * @return the currently available volume capacity in bytes
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.VAR_VOLUME_DATA, value = "0")
    private static native long getCurrentVolumeAvail0();

    /**
     * Returns the currently used volume capacity.
     * 
     * @return the currently used volume capacity in bytes
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.VAR_VOLUME_DATA, value = "0")
    private static native long getCurrentVolumeUse0();

    /**
     * Returns the maximum volume capacity.
     * 
     * @return the maximum volume capacity in bytes
     * 
     * @since 1.00
     */
    public long getVolumeCapacity() {
        return getVolumeCapacity0();
    }

    /**
     * Returns the currently available volume capacity.
     * 
     * @return the currently available volume capacity in bytes
     * 
     * @since 1.00
     */
    public long getCurrentVolumeAvail() {
        return getCurrentVolumeAvail0();
    }

    /**
     * Returns the currently used volume capacity.
     * 
     * @return the currently used volume capacity in bytes
     * 
     * @since 1.00
     */
    public long getCurrentVolumeUse() {
        return getCurrentVolumeUse0();
    }

}
