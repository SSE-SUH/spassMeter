package de.uni_hildesheim.sse.monitoring.runtime.boot;



/**
 * Defines the basic types of streams.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
// TODO Unify with other constants
public enum StreamType {
    
    /**
     * Empty dummy.
     */
    NONE(null),
    
    /**
     * Network streams.
     */
    NET(ResourceType.NET_IO, DebugState.NET_IN, DebugState.NET_OUT),
    
    /**
     * File streams.
     */
    FILE(ResourceType.FILE_IO, DebugState.FILE_IN, DebugState.FILE_OUT);
    
    /**
     * Stores the related resource.
     */
    private ResourceType resource;
    
    /**
     * Stores related debugging states enabling further output for the given
     * stream types.
     */
    private DebugState[] states;
    
    /**
     * Creates a new constant with specified debugging states.
     * 
     * @param resource the related resource
     * @param states the debugging states
     * 
     * @since 1.00
     */
    private StreamType(ResourceType resource, DebugState... states) {
        this.states = states;
    }
    
    /**
     * Checks if this type matches the given debugging state.
     * 
     * @param state the state to be tested
     * @return <code>true</code> if the given <code>state</code> is matched by
     *   this constant, <code>false</code> else
     * 
     * @since 1.00
     */
    public boolean matches(DebugState state) {
        boolean found = false;
        if (null != states) {
            for (int i = 0; !found && i < states.length; i++) {
                found = (states[i] == state);
            }
        }
        return found;
    }
    
    /**
     * Returns the related resource.
     * 
     * @return the related res
     * 
     * @since 1.00
     */
    public ResourceType getResource() {
        return resource;
    }
    
    /**
     * Returns the stream type for an URL.
     * 
     * @param protocol the URL to return the stream type for
     * @return the stream type, may be {@link #NONE}
     * 
     * @since 1.00
     */
    public static final StreamType getForURL(String protocol) {
        StreamType result = NONE; // do not wrap stream in default case
        // because it is yet instrumented
        if ("file".equalsIgnoreCase(protocol)) {
            result = FILE;
        }
        return result;
    }
    
    /**
     * Returns the code to access this constant.
     * 
     * @return the code to access this constant
     * 
     * @since 1.00
     */
    public String toCode() {
        return StreamType.class.getName() + "." + name();
    }
    
}