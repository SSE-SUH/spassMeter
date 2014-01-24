package de.uni_hildesheim.sse.system;

import de.uni_hildesheim.sse.codeEraser.annotations.Variability;

/**
 * Defines an interface for requesting screen information from the system.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
@Variability(id = AnnotationConstants.VAR_SCREEN_DATA)
public interface IScreenDataGatherer {

    /**
     * Returns the physical screen width.
     * 
     * @return the physical screen width (in pixel)
     *         (negative or zero if invalid)
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.VAR_SCREEN_DATA, value = "-1")
    public int getScreenWidth();

    /**
     * Returns the physical screen height.
     * 
     * @return the physical screen height (in pixel)
     *         (negative or zero if invalid)
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.VAR_SCREEN_DATA, value = "-1")
    public int getScreenHeight();

    /**
     * Returns the physical screen resolution.
     * 
     * @return the physical screen resolution (in dpi)
     *         (negative or zero if invalid)
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.VAR_SCREEN_DATA, value = "-1")
    public int getScreenResolution();

}
