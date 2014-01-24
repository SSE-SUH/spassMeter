package de.uni_hildesheim.sse.system.deflt;

import de.uni_hildesheim.sse.codeEraser.annotations.Variability;
import de.uni_hildesheim.sse.system.AnnotationConstants;
import de.uni_hildesheim.sse.system.IScreenDataGatherer;

/**
 * Implements a class for requesting screen information from the system.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
@Variability(id = AnnotationConstants.VAR_SCREEN_DATA)
class ScreenDataGatherer implements IScreenDataGatherer {

    /**
     * Returns the physical screen width.
     * 
     * @return the physical screen width (in pixel)
     *         (negative or zero if invalid)
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.VAR_SCREEN_DATA, value = "-1")
    private static native int getScreenWidth0();

    /**
     * Returns the physical screen height.
     * 
     * @return the physical screen height (in pixel)
     *         (negative or zero if invalid)
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.VAR_SCREEN_DATA, value = "-1")
    private static native int getScreenHeight0();

    /**
     * Returns the physical screen resolution.
     * 
     * @return the physical screen resolution (in dpi)
     *         (negative or zero if invalid)
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.VAR_SCREEN_DATA, value = "-1")
    private static native int getScreenResolution0();

    /**
     * Returns the physical screen width.
     * 
     * @return the physical screen width (in pixel)
     *         (negative or zero if invalid)
     * 
     * @since 1.00
     */
    public int getScreenWidth() {
        return getScreenWidth0();
    }

    /**
     * Returns the physical screen height.
     * 
     * @return the physical screen height (in pixel)
     *         (negative or zero if invalid)
     * 
     * @since 1.00
     */
    public int getScreenHeight() {
        return getScreenHeight0();
    }

    /**
     * Returns the physical screen resolution.
     * 
     * @return the physical screen resolution (in dpi)
     *         (negative or zero if invalid)
     * 
     * @since 1.00
     */
    public int getScreenResolution() {
        return getScreenResolution0();
    }

}
