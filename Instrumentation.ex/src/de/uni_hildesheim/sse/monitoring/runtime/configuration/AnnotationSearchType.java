package de.uni_hildesheim.sse.monitoring.runtime.configuration;

/**
 * Defines how to consider annotations in super interfaces and super classes.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public enum AnnotationSearchType {

    /**
     * Do not consider annotations in super classes or interfaces at all.
     */
    NONE(false, false),

    /**
     * Consider annotations in super interfaces.
     */
    INTERFACES(true, false),

    /**
     * Consider annotations in super classes.
     */
    SUPERCLASSES(false, true),
    
    /**
     * Consider annotations in super interfaces and classes.
     */
    ALL(true, true);
    
    /**
     * Stores whether super interfaces should be considered.
     */
    private boolean iface;
    
    /**
     * Stores whether super classes should be considered.
     */
    private boolean sclass;
    
    /**
     * Creates a new constant.
     * 
     * @param iface whether super interfaces should be considered
     * @param sclass whether super classes should be considered
     * 
     * @since 1.00
     */
    private AnnotationSearchType(boolean iface, boolean sclass) {
        this.iface = iface;
        this.sclass = sclass;
    }
    
    /**
     * Returns whether super interfaces should be considered.
     * 
     * @return <code>true</code> if they should be considered, 
     *   <code>false</code> if not
     * 
     * @since 1.00
     */
    public boolean considerInterface() {
        return iface;
    }

    /**
     * Returns whether super classes should be considered.
     * 
     * @return <code>true</code> if they should be considered, 
     *   <code>false</code> if not
     * 
     * @since 1.00
     */
    public boolean considerSuperclass() {
        return sclass;
    }
    
}
