package de.uni_hildesheim.sse.system;

import de.uni_hildesheim.sse.codeEraser.annotations.Variability;

/**
 * Defines an interface for requesting volume information from the system.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
@Variability(id = AnnotationConstants.VAR_VOLUME_DATA)
public interface IVolumeDataGatherer {

    /**
     * Returns the maximum volume capacity.
     * 
     * @return the maximum volume capacity in bytes, negative if invalid
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.VAR_VOLUME_DATA, value = "0")
    public long getVolumeCapacity();

    /**
     * Returns the currently available volume capacity.
     * 
     * @return the currently available volume capacity in bytes, negative 
     * if invalid
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.VAR_VOLUME_DATA, value = "0")
    public long getCurrentVolumeAvail();

    /**
     * Returns the currently used volume capacity.
     * 
     * @return the currently used volume capacity in bytes, negative if invalid
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.VAR_VOLUME_DATA, value = "0")
    public long getCurrentVolumeUse();

}
