package de.uni_hildesheim.sse.system;

/**
 * Defines a helper class for returning information on an object unallocation.
 * Do neither move this object, nor rename it or its attributes as it is 
 * accessed from native code. 
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public class ObjectUnallocationInfo {
    
    // checkstyle: stop member visibility check
    
    /**
     * Stores the id used for native tagging.
     */
    public long tag;
    
    /**
     * Stores the allocated size in bytes.
     */
    public long size;

    // checkstyle: resume member visibility check

}
