package de.uni_hildesheim.sse.wildcat.services;

import javax.management.AttributeNotFoundException;

import de.uni_hildesheim.sse.codeEraser.annotations.Variability;
import de.uni_hildesheim.sse.serviceConstants.VolumeDataConstants;
import de.uni_hildesheim.sse.system.AnnotationConstants;
import de.uni_hildesheim.sse.system.GathererFactory;
import de.uni_hildesheim.sse.system.IVolumeDataGatherer;

/**
 * Defines an object for delegation of the values from the interface
 * {@link IVolumeDataGatherer}. Additional to that it defines its WildCAT
 * service interface.
 * 
 * @author Stephan Dederichs
 * 
 * @since 1.00
 * @version 1.00
 */
@Variability(id = AnnotationConstants.VAR_VOLUME_DATA)
public class WVolumeData extends AbstractWServiceData 
    implements IVolumeDataGatherer {

    /**
     * Instance of {@link IVolumeDataGatherer} for gathering required
     * informations.
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.VAR_VOLUME_DATA)
    private IVolumeDataGatherer volumeDataGatherer;
    
    /**
     * Constructor.
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.VAR_VOLUME_DATA)
    public WVolumeData() {
        volumeDataGatherer = GathererFactory.getVolumeDataGatherer();
    }

    @Override
    @Variability(id = AnnotationConstants.VAR_VOLUME_DATA, value = "0")
    public long getVolumeCapacity() {
        long result = volumeDataGatherer.getVolumeCapacity();
        setSensorValue(VolumeDataConstants.VOLUME_CAPACITY, result);
        return result;
    }

    @Override
    @Variability(id = AnnotationConstants.VAR_VOLUME_DATA, value = "0")
    public long getCurrentVolumeAvail() {
        long result = volumeDataGatherer.getCurrentVolumeAvail();
        setSensorValue(VolumeDataConstants.CURRENT_VOLUME_AVAIL, result);
        return result;
    }

    @Override
    @Variability(id = AnnotationConstants.VAR_VOLUME_DATA, value = "0")
    public long getCurrentVolumeUse() {
        long result = volumeDataGatherer.getCurrentVolumeUse();
        setSensorValue(VolumeDataConstants.CURRENT_VOLUME_USE, result);
        return result;
    }

    // ------------------------------------------------------------------------
    
    @Override
    public void burstValueUpdate() {
        getVolumeCapacity();
        getCurrentVolumeAvail();
        getCurrentVolumeUse();
    }
    
    @Override
    public Object getDataSpecificAttribute(String attributeName)
        throws AttributeNotFoundException {
        // Check for individual attributes
        if (attributeName.equals(VolumeDataConstants.VOLUME_CAPACITY)) {
            return getVolumeCapacity();
        } else if (attributeName.equals(VolumeDataConstants.
                CURRENT_VOLUME_AVAIL)) {
            return getCurrentVolumeAvail();
        } else if (attributeName.equals(VolumeDataConstants.
                CURRENT_VOLUME_USE)) {
            return getCurrentVolumeUse();
        } else {
            throw new AttributeNotFoundException("Invalid attribute: "
                    + attributeName);
        }
    }

}
