package de.uni_hildesheim.sse.serviceConstants;

/**
 * Stores the attribute names of the {@link IProcessorDataGatherer}.
 * 
 * @author Stephan Dederichs
 * 
 * @version 1.00
 * @since 1.00
 */
public class ProcessorDataConstants {

    /**
     * NumberOfProcessors attribute.
     * 
     * @since 1.00
     */
    public static final String NUMBER_OF_PROCESSORS = "NumberOfProcessors";
    
    /**
     * MaxProcessorSpeed attribute.
     * 
     * @since 1.00
     */
    public static final String MAX_PROCESSOR_SPEED = "MaxProcessorSpeed";
    
    /**
     * CurrentProcessorSpeed attribute.
     * 
     * @since 1.00
     */
    public static final String CURRENT_PROCESSOR_SPEED = 
            "CurrentProcessorSpeed";
    
    /**
     * CurrentSystemLoad attribute.
     * 
     * @since 1.00
     */
    public static final String CURRENT_SYSTEM_LOAD = "CurrentSystemLoad";
    
    /**
     * Prevents this class from being created from outside.
     * 
     * @since 1.00
     */
    private ProcessorDataConstants() {
        
    }
    
}
