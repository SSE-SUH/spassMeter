package de.uni_hildesheim.sse.serviceConstants;

/**
 * Stores the attribute names of the {@link IMemoryDataGatherer}.
 * 
 * @author Stephan Dederichs
 * 
 * @version 1.00
 * @since 1.00
 */
public class MemoryDataConstants {

    /**
     * MemoryCapacity attribute.
     * 
     * @since 1.00
     */
    public static final String MEMORY_CAPACITY = "MemoryCapacity";
    
    /**
     * CurrentMemoryAvail attribute.
     * 
     * @since 1.00
     */
    public static final String CURRENT_MEMORY_AVAIL = "CurrentMemoryAvail";
    
    /**
     * CurrentMemoryUse attribute.
     * 
     * @since 1.00
     */
    public static final String CURRENT_MEMORY_USE = "CurrentMemoryUse";
    
    /**
     * Prevents this class from being created from outside.
     * 
     * @since 1.00
     */
    private MemoryDataConstants() {
        
    }
    
}
