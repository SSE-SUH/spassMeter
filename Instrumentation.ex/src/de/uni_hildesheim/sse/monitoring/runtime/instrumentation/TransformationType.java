package de.uni_hildesheim.sse.monitoring.runtime.instrumentation;

/**
 * Characterizes the type of transformation.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public enum TransformationType {

    /**
     * The external static transformation.
     */
    STATIC(true),
    
    /**
     * The default on-load transformation.
     */
    ON_LOAD(true),
    
    /**
     * Retransformation by the agent.
     */
    RETRANSFORMATION(false),
    
    /**
     * Class redefinition transformation.
     */
    REDEFINITION(false);
    
    /**
     * Stores whether the structure may be altered.
     */
    private boolean mayAlterStructure;
    
    /**
     * Creates a new enum constant.
     * 
     * @param mayAlterStructure whether the structure of the class under 
     *   transformation may be altered
     * 
     * @since 1.00
     */
    private TransformationType(boolean mayAlterStructure) {
        this.mayAlterStructure = mayAlterStructure;
    }
    
    /**
     * Returns whether the structure of the class under transformation may be 
     * altered.
     * 
     * @return <code>true</code> if it may be altered, <code>false</code> else
     * 
     * @since 1.00
     */
    public final boolean mayAlterStructure() {
        return mayAlterStructure;
    }

}
