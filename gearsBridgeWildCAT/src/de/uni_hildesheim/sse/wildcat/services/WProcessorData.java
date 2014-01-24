package de.uni_hildesheim.sse.wildcat.services;

import javax.management.AttributeNotFoundException;

import de.uni_hildesheim.sse.codeEraser.annotations.Variability;
import de.uni_hildesheim.sse.serviceConstants.ProcessorDataConstants;
import de.uni_hildesheim.sse.system.AnnotationConstants;
import de.uni_hildesheim.sse.system.GathererFactory;
import de.uni_hildesheim.sse.system.IProcessorDataGatherer;

/**
 * Defines an object for delegation of the values from the interface
 * {@link IProcessorDataGatherer}. Additional to that it defines its WildCAT
 * service interface.
 * 
 * @author Stephan Dederichs
 * 
 * @since 1.00
 * @version 1.00
 */
public class WProcessorData extends AbstractWServiceData 
    implements IProcessorDataGatherer {

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
    public WProcessorData() {
        processorDataGatherer = GathererFactory.getProcessorDataGatherer();
    }

    @Override
    @Variability(id = AnnotationConstants.VAR_PROCESSOR_DATA, value = "-1")
    public int getNumberOfProcessors() {
        int result = processorDataGatherer.getNumberOfProcessors();
        setSensorValue(ProcessorDataConstants.NUMBER_OF_PROCESSORS, result);
        return result;
    }

    @Override
    @Variability(id = AnnotationConstants.VAR_PROCESSOR_DATA, value = "-1")
    public int getMaxProcessorSpeed() {
        int result = processorDataGatherer.getMaxProcessorSpeed();
        setSensorValue(ProcessorDataConstants.MAX_PROCESSOR_SPEED, result);
        return result;
    }

    @Override
    @Variability(id = AnnotationConstants.VAR_PROCESSOR_DATA, value = "-1")
    public int getCurrentProcessorSpeed() {
        int result = processorDataGatherer.getCurrentProcessorSpeed();
        setSensorValue(ProcessorDataConstants.CURRENT_PROCESSOR_SPEED, result);
        return result;
    }

    @Override
    @Variability(id = AnnotationConstants.VAR_PROCESSOR_DATA, value = "-1")
    public double getCurrentSystemLoad() {
        double result = processorDataGatherer.getCurrentSystemLoad();
        setSensorValue(ProcessorDataConstants.CURRENT_SYSTEM_LOAD, result);
        return result;
    }

    // ------------------------------------------------------------------------
    
    @Override
    public void burstValueUpdate() {
        getNumberOfProcessors();
        getMaxProcessorSpeed();
        getCurrentProcessorSpeed();
        getCurrentSystemLoad();
    }
    
    @Override
    public Object getDataSpecificAttribute(String attributeName)
        throws AttributeNotFoundException {
        // Check for individual attributes
        if (attributeName.equals(ProcessorDataConstants.
                NUMBER_OF_PROCESSORS)) {
            return getNumberOfProcessors();
        } else if (attributeName.equals(ProcessorDataConstants.
                MAX_PROCESSOR_SPEED)) {
            return getMaxProcessorSpeed();
        } else if (attributeName.equals(ProcessorDataConstants.
                CURRENT_PROCESSOR_SPEED)) {
            return getCurrentProcessorSpeed();
        } else if (attributeName.equals(ProcessorDataConstants.
                CURRENT_SYSTEM_LOAD)) {
            return getCurrentSystemLoad();
        } else {
            throw new AttributeNotFoundException("Invalid attribute: "
                    + attributeName);
        }
    }

}
