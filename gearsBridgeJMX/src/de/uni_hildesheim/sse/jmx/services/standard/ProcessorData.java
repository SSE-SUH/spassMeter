package de.uni_hildesheim.sse.jmx.services.standard;

import de.uni_hildesheim.sse.codeEraser.annotations.Variability;
import de.uni_hildesheim.sse.system.AnnotationConstants;
import de.uni_hildesheim.sse.system.GathererFactory;
import de.uni_hildesheim.sse.system.IProcessorDataGatherer;

/**
 * Defines an object for delegation of the values from the interface
 * {@link IProcessorDataGatherer} to the {@link StandardMBeanAgent}.
 * 
 * @author Stephan Dederichs
 * 
 * @since 1.00
 * @version 1.00
 */
public class ProcessorData implements ProcessorDataMBean {

    /**
     * Instance of {@link IProcessorDataGatherer} for gathering required
     * informations.
     * 
     * @since 1.00
     */
    private IProcessorDataGatherer processorDataGatherer;

    /**
     * Constructor.
     * 
     * @since 1.00
     */
    public ProcessorData() {
        processorDataGatherer = GathererFactory.getProcessorDataGatherer();
    }

    @Override
    @Variability(id = AnnotationConstants.VAR_PROCESSOR_DATA, value = "-1")
    public int getNumberOfProcessors() {
        return processorDataGatherer.getNumberOfProcessors();
    }

    @Override
    @Variability(id = AnnotationConstants.VAR_PROCESSOR_DATA, value = "-1")
    public int getMaxProcessorSpeed() {
        return processorDataGatherer.getMaxProcessorSpeed();
    }

    @Override
    @Variability(id = AnnotationConstants.VAR_PROCESSOR_DATA, value = "-1")
    public int getCurrentProcessorSpeed() {
        return processorDataGatherer.getCurrentProcessorSpeed();
    }

    @Override
    @Variability(id = AnnotationConstants.VAR_PROCESSOR_DATA, value = "-1")
    public double getCurrentSystemLoad() {
        return processorDataGatherer.getCurrentSystemLoad();
    }

}
