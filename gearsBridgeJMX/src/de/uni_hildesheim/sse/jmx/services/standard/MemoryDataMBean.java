package de.uni_hildesheim.sse.jmx.services.standard;

import de.uni_hildesheim.sse.codeEraser.annotations.Variability;
import de.uni_hildesheim.sse.system.AnnotationConstants;
import de.uni_hildesheim.sse.system.IMemoryDataGatherer;

/**
 * Defines an interface for instrumenting the resource {@link MemoryData}.
 * 
 * @author Stephan Dederichs
 * @since 1.00
 * @version 1.00
 */
@Variability(id = AnnotationConstants.VAR_MEMORY_DATA)
public interface MemoryDataMBean extends IMemoryDataGatherer {

}
