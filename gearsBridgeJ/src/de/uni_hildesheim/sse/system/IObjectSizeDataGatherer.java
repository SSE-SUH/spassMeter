package de.uni_hildesheim.sse.system;

/**
 * Allows the access to object sizes even if no Java instrumentation agent
 * is attached to this VM (uses native JVMTI agent attachment).
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public interface IObjectSizeDataGatherer {

    /**
     * Returns the size of the given object as allocated by the JVM. This
     * method should (!) return the same value as in 
     * <code>java.lang.instrument.Instrumentation</code>.
     * 
     * @param object the object the size should be queried for
     * @return the size of the physical memory allocated for <code>object</code>
     *         (in bytes, negative or zero if invalid or not available)
     * 
     * @since 1.00
     */
    public long getObjectSize(Object object);

}
