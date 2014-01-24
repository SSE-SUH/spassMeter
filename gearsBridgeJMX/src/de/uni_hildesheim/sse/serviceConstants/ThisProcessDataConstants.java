package de.uni_hildesheim.sse.serviceConstants;

/**
 * Stores the attribute names of the {@link IThisProcessDataGatherer}.
 * 
 * @author Stephan Dederichs
 * 
 * @version 1.00
 * @since 1.00
 */
public class ThisProcessDataConstants {

    /**
     * CurrentProcessID attribute.
     * 
     * @since 1.00
     */
    public static final String CURRENT_PROCESS_ID = "CurrentProcessID";
    
    /**
     * CurrentProcessIo attribute.
     * 
     * @since 1.00
     */
    public static final String CURRENT_PROCESS_IO = "CurrentProcessIo";
    
    /**
     * isNetworkIoDataIncluded attribute.
     * 
     * @since 1.00
     */
    public static final String IS_NETWORK_DATA_INCLUDED = 
            "isNetworkIoDataIncluded";

    /**
     * isFileIoDataIncluded attribute.
     * 
     * @since 1.00
     */
    public static final String IS_FILE_IO_DATA_INCLUDED = 
            "isFileIoDataIncluded";
    
    /**
     * CurrentProcessMemoryUse attribute.
     * 
     * @since 1.00
     */
    public static final String CURRENT_PROCESS_MEMORY_USE = 
            "CurrentProcessMemoryUse";
    
    /**
     * CurrentProcessUserTimeTicks attribute.
     * 
     * @since 1.00
     */
    public static final String CURRENT_PROCESS_USER_TIME_TICKS = 
            "CurrentProcessUserTimeTicks";
    
    /**
     * CurrentProcessKernelTimeTicks attribute.
     * 
     * @since 1.00
     */
    public static final String CURRENT_PROCESS_KERNEL_TIME_TICKS = 
            "CurrentProcessKernelTimeTicks";
    
    /**
     * CurrentProcessSystemTimeTicks attribute.
     * 
     * @since 1.00
     */
    public static final String CURRENT_PROCESS_SYSTEM_TIME_TICKS = 
            "CurrentProcessSystemTimeTicks";
    
    /**
     * CurrentProcessProcessorLoad attribute.
     * 
     * @since 1.00
     */
    public static final String CURRENT_PROCESS_PROCESSOR_LOAD = 
            "CurrentProcessProcessorLoad";
    
    /**
     * AllProcessesIo attribute.
     * 
     * @since 1.00
     */
    public static final String ALL_PROCESS_IO = "AllProcessesIo";
    
    /**
     * Prevents this class from being created from outside.
     * 
     * @since 1.00
     */
    private ThisProcessDataConstants() {
        
    }
    
}
