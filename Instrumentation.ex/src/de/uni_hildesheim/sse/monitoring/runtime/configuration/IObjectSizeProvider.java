package de.uni_hildesheim.sse.monitoring.runtime.configuration;

/**
 * Allows to access the size of an object as allocated by the JVM. This 
 * interface was introduced to allow native access in case that no agent is 
 * running (static instrumentation).
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public interface IObjectSizeProvider {

    /**
     * Returns the size of the given object as allocated by the JVM. This
     * method is a dummy implementation.
     * 
     * @param object the object the size should be queried for
     * @return the size of the physical memory allocated for <code>object</code>
     *         (in bytes, negative or zero if invalid or not available)
     * 
     * @since 1.00
     */
    public long getObjectSize(Object object);

}
