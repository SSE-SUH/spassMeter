package de.uni_hildesheim.sse.jmx.services.standard;

import de.uni_hildesheim.sse.codeEraser.annotations.Variability;
import de.uni_hildesheim.sse.system.AnnotationConstants;
import de.uni_hildesheim.sse.system.GathererFactory;
import de.uni_hildesheim.sse.system.IVolumeDataGatherer;

/**
 * Defines an object for delegation of the values from the interface
 * {@link IVolumeDataGatherer} to the {@link StandardMBeanAgent}.
 * 
 * @author Stephan Dederichs
 * 
 * @since 1.00
 * @version 1.00
 */
@Variability(id = AnnotationConstants.VAR_VOLUME_DATA)
public class VolumeData implements VolumeDataMBean {

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
    public VolumeData() {
        volumeDataGatherer = GathererFactory.getVolumeDataGatherer();
    }

    @Override
    @Variability(id = AnnotationConstants.VAR_VOLUME_DATA, value = "0")
    public long getVolumeCapacity() {
        return volumeDataGatherer.getVolumeCapacity();
    }

    @Override
    @Variability(id = AnnotationConstants.VAR_VOLUME_DATA, value = "0")
    public long getCurrentVolumeAvail() {
        return volumeDataGatherer.getCurrentVolumeAvail();
    }

    @Override
    @Variability(id = AnnotationConstants.VAR_VOLUME_DATA, value = "0")
    public long getCurrentVolumeUse() {
        return volumeDataGatherer.getCurrentVolumeUse();
    }

}
