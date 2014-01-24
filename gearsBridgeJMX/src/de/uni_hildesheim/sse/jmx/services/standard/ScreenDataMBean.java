package de.uni_hildesheim.sse.jmx.services.standard;

import de.uni_hildesheim.sse.codeEraser.annotations.Variability;
import de.uni_hildesheim.sse.system.AnnotationConstants;
import de.uni_hildesheim.sse.system.IScreenDataGatherer;

/**
 * Defines an interface for instrumenting the resource {@link ScreenData}.
 * 
 * @author Stephan Dederichs
 * 
 * @since 1.00
 * @version 1.00
 */
@Variability(id = AnnotationConstants.VAR_SCREEN_DATA)
public interface ScreenDataMBean extends IScreenDataGatherer {

}
