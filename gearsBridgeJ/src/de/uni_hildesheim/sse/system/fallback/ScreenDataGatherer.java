package de.uni_hildesheim.sse.system.fallback;

import de.uni_hildesheim.sse.codeEraser.annotations.Variability;
import de.uni_hildesheim.sse.system.AnnotationConstants;
import de.uni_hildesheim.sse.system.IScreenDataGatherer;

/**
 * Implements a class for requesting screen information from the system.
 * 
 * @author Holger Eichelberger
 * @since 1.20
 * @version 1.20
 */
@Variability(id = AnnotationConstants.VAR_SCREEN_DATA)
class ScreenDataGatherer implements IScreenDataGatherer {

    @Override
    public int getScreenWidth() {
        return -1;
    }

    @Override
    public int getScreenHeight() {
        return -1;
    }

    @Override
    public int getScreenResolution() {
        return -1;
    }

}
