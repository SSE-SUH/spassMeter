package de.uni_hildesheim.sse.codeEraser.util;

/**
 * Allows to configure a newly created class pool by additional class path 
 * entries. This class avoids additional dependencies to external libraries.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public abstract class OnCreationJarProvider {

    /**
     * Stores the configurator (may be <b>null</b>).
     */
    private static OnCreationJarProvider instance;

    /**
     * Changes the singleton instance.
     * 
     * @param anInstance the new singleton instance
     * 
     * @since 1.00
     */
    public static final void setInstance(OnCreationJarProvider anInstance) {
        instance = anInstance;
    }
    
    /**
     * Returns the singleton instance of this class.
     * 
     * @return the instance, may be <b>null</b>
     * 
     * @since 1.00
     */
    public static final OnCreationJarProvider getInstance() {
        return instance;
    }
    
    /**
     * Returns additional jars to be considered when creating a class pool.
     * Due to internal reasons, the caller must guarantee that the returned
     * array is <b>not</b> modified!
     * 
     * @return additional jars for a new class pool
     * 
     * @since 1.00
     */
    public abstract String[] getJars();
    
}
