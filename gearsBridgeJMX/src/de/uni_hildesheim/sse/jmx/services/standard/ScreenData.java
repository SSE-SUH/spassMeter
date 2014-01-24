package de.uni_hildesheim.sse.jmx.services.standard;

import de.uni_hildesheim.sse.codeEraser.annotations.Variability;
import de.uni_hildesheim.sse.system.AnnotationConstants;
import de.uni_hildesheim.sse.system.GathererFactory;
import de.uni_hildesheim.sse.system.IScreenDataGatherer;

/**
 * Defines an object for delegation of the values from the interface
 * {@link IScreenDataGatherer} to the {@link StandardMBeanAgent}.
 * 
 * @author Stephan Dederichs
 * 
 * @since 1.00
 * @version 1.00
 */
@Variability(id = AnnotationConstants.VAR_SCREEN_DATA)
public class ScreenData implements ScreenDataMBean {

    /**
     * Instance of {@link IScreenDataGatherer} for gathering required
     * informations.
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.VAR_SCREEN_DATA)
    private IScreenDataGatherer screenDataGatherer;

    /**
     * Constructor.
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.VAR_SCREEN_DATA)
    public ScreenData() {
        screenDataGatherer = GathererFactory.getScreenDataGatherer();
    }

    @Override
    @Variability(id = AnnotationConstants.VAR_SCREEN_DATA, value = "-1")
    public int getScreenWidth() {
        return screenDataGatherer.getScreenWidth();
    }

    @Override
    @Variability(id = AnnotationConstants.VAR_SCREEN_DATA, value = "-1")
    public int getScreenHeight() {
        return screenDataGatherer.getScreenHeight();
    }

    @Override
    @Variability(id = AnnotationConstants.VAR_SCREEN_DATA, value = "-1")
    public int getScreenResolution() {
        return screenDataGatherer.getScreenResolution();
    }

}
