package de.uni_hildesheim.sse.monitoring.runtime.instrumentation.lib;

/**
 * Defines some utility methods.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.30
 */
public class Utils {
    
    /**
     * Stores whether we are running a JVM before version 9.
     * 
     * @since 1.30
     */
    public static final boolean JAVA_BEFORE_9 = 
        System.getProperty("java.version", "").startsWith("1.");

    /**
     * Prevents this class from being initialized from outside.
     * 
     * @since 1.00
     */
    private Utils() {
    }
    
    /**
     * Returns whether the given behavior is a finalizer.
     * 
     * @param name the name of the behavior
     * @param paramCount the parameter count of the behavior
     * @return <code>true</code> if it is a finalizer, <code>false</code> else
     * 
     * @since 1.00
     */
    public static boolean isFinalize(String name , int paramCount) {
        return "finalize".equals(name) && paramCount == 0;
    }
    
}
