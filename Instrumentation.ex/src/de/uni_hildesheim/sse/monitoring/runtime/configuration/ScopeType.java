package de.uni_hildesheim.sse.monitoring.runtime.configuration;

/**
 * Defines the scope type, i.e. how group monitoring semantics influence the
 * instrumentation. However, this shall always be {@link #GROUP_INHERIT} but
 * due to incremental development of SPASS-meter, i.e. legacy modes and results,
 * we keep these three modes. Therefore, the default mode is {@link #SUM}.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public enum ScopeType {

    /**
     * The SUM monitoring semantics define the instrumentation.
     */
    SUM(false),
    
    /**
     * The group monitoring semantics define the instrumentation but only
     * for the annotated classes.
     */
    GROUP(true), // TODO check with LOCAL!
    
    /**
     * The group monitoring semantics is inherited to all classes which
     * are referenced from the group. [experimental]
     */
    GROUP_INHERIT(true);
 
    /**
     * Stores whether this type is a group-related type.
     */
    private boolean isGroup;
    
    /**
     * Creates a new type.
     * 
     * @param isGroup whether this type is a group-related type
     * 
     * @since 1.00
     */
    private ScopeType(boolean isGroup) {
        this.isGroup = isGroup;
    }
    
    /**
     * Returns whether this type is a group-related type.
     * 
     * @return <code>true</code> if this type is a group-related type, 
     *   <code>false</code> else
     * 
     * @since 1.00
     */
    public boolean isGroup() {
        return isGroup;
    }
}
