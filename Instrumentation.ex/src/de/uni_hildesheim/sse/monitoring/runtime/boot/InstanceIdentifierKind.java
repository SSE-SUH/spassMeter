package de.uni_hildesheim.sse.monitoring.runtime.boot;

/**
 * Defines possible methods to record monitoring information in monitoring 
 * groups also on instance level.
 * 
 * @author Holger Eichelberger
 * @since 1.20
 * @version 1.20
 */
public enum InstanceIdentifierKind {
    
    /**
     * No instance identifier. This is the default behavior.
     */
    NONE,
    
    /**
     * Use the thread id as identifier.
     */
    THREAD_ID,
    
    /**
     * Use the system identity hash code of the instance as identifier.
     */
    IDENTITY_HASHCODE;
    
    /**
     * The default instance identifier kind.
     */
    public static final InstanceIdentifierKind DEFAULT = NONE;

}
