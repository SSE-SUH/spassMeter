package de.uni_hildesheim.sse.wildcat.services;

import javax.management.AttributeNotFoundException;

import de.uni_hildesheim.sse.codeEraser.annotations.Variability;
import de.uni_hildesheim.sse.serviceConstants.ScreenDataConstants;
import de.uni_hildesheim.sse.system.AnnotationConstants;
import de.uni_hildesheim.sse.system.GathererFactory;
import de.uni_hildesheim.sse.system.IScreenDataGatherer;

/**
 * Defines an object for delegation of the values from the interface
 * {@link IScreenDataGatherer}. Additional to that it defines its WildCAT
 * service interface.
 * 
 * @author Stephan Dederichs
 * 
 * @since 1.00
 * @version 1.00
 */
@Variability(id = AnnotationConstants.VAR_SCREEN_DATA)
public class WScreenData extends AbstractWServiceData 
    implements IScreenDataGatherer {

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
    public WScreenData() {
        screenDataGatherer = GathererFactory.getScreenDataGatherer();
    }

    @Override
    @Variability(id = AnnotationConstants.VAR_SCREEN_DATA, value = "-1")
    public int getScreenWidth() {
        int result = screenDataGatherer.getScreenWidth();
        setSensorValue(ScreenDataConstants.SCREEN_WIDTH, result);
        return result;
    }

    @Override
    @Variability(id = AnnotationConstants.VAR_SCREEN_DATA, value = "-1")
    public int getScreenHeight() {
        int result = screenDataGatherer.getScreenHeight();
        setSensorValue(ScreenDataConstants.SCREEN_HEIGHT, result);
        return result;
    }

    @Override
    @Variability(id = AnnotationConstants.VAR_SCREEN_DATA, value = "-1")
    public int getScreenResolution() {
        int result = screenDataGatherer.getScreenResolution();
        setSensorValue(ScreenDataConstants.SCREEN_RESOLUTION, result);
        return result;
    }

    // ------------------------------------------------------------------------
    
    @Override
    public void burstValueUpdate() {
        getScreenWidth();
        getScreenHeight();
        getScreenResolution();
    }
    
    @Override
    public Object getDataSpecificAttribute(String attributeName)
        throws AttributeNotFoundException {
        // Check for individual attributes
        if (attributeName.equals(ScreenDataConstants.SCREEN_WIDTH)) {
            return getScreenWidth();
        } else if (attributeName.equals(ScreenDataConstants.SCREEN_HEIGHT)) {
            return getScreenHeight();
        } else if (attributeName.equals(ScreenDataConstants.
                SCREEN_RESOLUTION)) {
            return getScreenResolution();
        } else {
            throw new AttributeNotFoundException("Invalid attribute: "
                    + attributeName);
        }
    }

}
