package de.uni_hildesheim.sse.system;

/**
 * Defines a connected class to be called when memory unallocation is perceived
 * and natively aggregated memory data shall be transmitted back to Java.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public interface IMemoryUnallocationReceiver {
    
    // DO NOT MOVE THIS INTERFACE OR CHANGE ITS METHDS - called from native code
    
    /**
     * Is used as a callback to receive unallocation information.
     * 
     * @param recId the recording id (as unique int)
     * @param size the allocation size
     * 
     * @since 1.00
     */
    public void unallocated(int recId, long size);
}
