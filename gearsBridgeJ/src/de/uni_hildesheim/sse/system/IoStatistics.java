package de.uni_hildesheim.sse.system;

/**
 * A class holding values for reading and writing I/O data.
 * Attributes in this class are public because they are filled by JNI. Do 
 * not rename class or attributes!
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public class IoStatistics {
    
    // checkstyle: stop member visibility check
    
    /**
     * Stores the number of bytes read.
     */
    public long read;  // bytes
    
    /**
     * Stores the number of bytes written.
     */
    public long write; // bytes
    
    // checkstyle: resume member visibility check
}