package de.uni_hildesheim.sse.monitoring.runtime.boot;

/**
 * Defines possible values for debug states, i.e. for situations where 
 * additional debugging information should be emitted during monitoring.
 * 
 * @author eichelberger
 * @since 1.00
 * @version 1.00
 */
public enum DebugState {

    /**
     * When variability configuration is changing.
     */
    CONFIGURATION(""),
    
    /**
     * When freeing memory.
     */
    MEMORY_FREE("mem_free"),

    /**
     * When entering a method.
     */
    METHOD_ENTER("enter"),

    /**
     * When exiting a method.
     */
    METHOD_EXIT("exit"),

    /**
     * When allocating memory.
     */
    MEMORY_ALLOCATION("mem_alloc"),
    
    /**
     * When writing to network.
     */
    NET_IN("net in"),

    /**
     * When reading from network.
     */
    NET_OUT("net out"),

    /**
     * When reading from file.
     */
    FILE_IN("file in"),

    /**
     * When writing to file.
     */
    FILE_OUT("file out");
    
    /**
     * Provides a constant empty array. Do not remove - used by code generation.
     */
    public static final DebugState[] NONE = new DebugState[0];

    /**
     * Provides the default value. Do not remove - used by code generation.
     */
    public static final DebugState[] DEFAULT = new DebugState[0];
    
    /**
     * Stores the marker to be printed out with the debugging info.
     */
    private String marker;
    
    /**
     * Creates a new constant.
     * 
     * @param marker the marker to be printed out with the debugging info
     * 
     * @since 1.00
     */
    private DebugState(String marker) {
        this.marker = marker;
    }
    
    /**
     * Returns the marker to be printed out with the debugging info.
     * 
     * @return the marker
     * 
     * @since 1.00
     */
    public String getMarker() {
        return marker;
    }
    
}
