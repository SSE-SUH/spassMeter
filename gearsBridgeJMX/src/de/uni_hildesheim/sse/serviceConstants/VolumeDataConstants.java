package de.uni_hildesheim.sse.serviceConstants;

/**
 * Stores the attribute names of the {@link IVolumeDataGatherer}.
 * 
 * @author Stephan Dederichs
 * 
 * @version 1.00
 * @since 1.00
 */
public class VolumeDataConstants {

    /**
     * VolumeCapacity attribute.
     * 
     * @since 1.00
     */
    public static final String VOLUME_CAPACITY = "VolumeCapacity";
    
    /**
     * CurrentVolumeAvail attribute.
     * 
     * @since 1.00
     */
    public static final String CURRENT_VOLUME_AVAIL = "CurrentVolumeAvail";
    
    /**
     * CurrentVolumeUse attribute.
     * 
     * @since 1.00
     */
    public static final String CURRENT_VOLUME_USE = "CurrentVolumeUse";
    
    /**
     * Prevents this class from being created from outside.
     * 
     * @since 1.00
     */
    private VolumeDataConstants() {
        
    }
    
}
