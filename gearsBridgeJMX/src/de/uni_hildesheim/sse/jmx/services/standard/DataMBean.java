package de.uni_hildesheim.sse.jmx.services.standard;

import de.uni_hildesheim.sse.codeEraser.annotations.Operation;
import de.uni_hildesheim.sse.codeEraser.annotations.Variability;
import de.uni_hildesheim.sse.system.AnnotationConstants;
import de.uni_hildesheim.sse.system.IDataGatherer;

/**
 * Defines an interface for instrumenting the resource {@link Data}.
 * 
 * @author Stephan Dederichs
 * 
 * @since 1.00
 * @version 1.00
 */
@Variability(id = { AnnotationConstants.VAR_GATHER_DATA,
        AnnotationConstants.VAR_WIFI_DATA }, op = Operation.AND)
public interface DataMBean extends IDataGatherer {

}
