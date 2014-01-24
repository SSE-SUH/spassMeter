package test.testing;

import test.AnnotationId;
import de.uni_hildesheim.sse.codeEraser.annotations.Variability;

/**
 * Defines constants to access values of a monitoring group.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
@Variability(id = AnnotationId.VAR_TESTING)
public enum MonitoringGroupValue {
    
    /**
     * Denotes the elapsed system time.
     */
    SYSTEM_TIME,

    /**
     * Denotes the elapsed CPU time.
     */
    CPU_TIME,

    /**
     * Denotes the allocated memory.
     */
    ALLOCATED_MEMORY,
    
    /**
     * Denotes the used memory.
     */
    USED_MEMORY,
    
    /**
     * Denotes the total input.
     */
    TOTAL_READ,
    
    /**
     * Denotes the file input.
     */
    FILE_READ,
    
    /**
     * Denotes the network input.
     */
    NET_READ,

    /**
     * Denotes the total output.
     */
    TOTAL_WRITE,
    
    /**
     * Denotes the file output.
     */
    FILE_WRITE,
    
    /**
     * Denotes the network output.
     */
    NET_WRITE;
}