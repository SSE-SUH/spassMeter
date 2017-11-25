package de.uni_hildesheim.sse.monitoring.runtime.recording;

import de.uni_hildesheim.sse.monitoring.runtime.configuration
    .IObjectSizeProvider;
import de.uni_hildesheim.sse.system.GathererFactory;
import de.uni_hildesheim.sse.system.ObjectSizeEstimator;

/**
 * Allows to access the size of an object as allocated by the JVM. This class
 * was introduced to allow native access in case that no agent is running 
 * (static instrumentation).
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public class ObjectSizeProvider implements IObjectSizeProvider {
//TODO check: this class can be simplified - interface was only introduced
// to circumvent initial OSGi problems    
    
    /**
     * Stores the singleton instance.
     */
    private static IObjectSizeProvider instance = createDefaultProvider();

    /**
     * Creates the default provider. Do not delete or rename this method. It 
     * might be overridden by instrumentation in case of static instrumentation.
     * 
     * @return the default provider
     * 
     * @since 1.00
     */
    private static IObjectSizeProvider createDefaultProvider() {
        IObjectSizeProvider result;
        if (GathererFactory.getDataGatherer().supportsJVMTI()) {
            result = new IObjectSizeProvider() {
                public long getObjectSize(Object object) {
                    return GathererFactory.getMemoryDataGatherer().
                        getObjectSize(object);
                }
            };
        } else {
            result = new ObjectSizeProvider();
        }
        return result;
    }
    
    /**
     * Initializes the singleton instance.
     */
    static {
        instance = new ObjectSizeProvider();
    }
    
    /**
     * Changes the singleton instance (may be done by the instrumentation Agent).
     * 
     * @param newInstance the new singleton instance (must not be <b>null</b>).
     * @throws IllegalArgumentException in case that 
     *     <code>newInstance==<b>null</b></code>
     * 
     * @since 1.00
     */
    public static final void setInstance(IObjectSizeProvider newInstance) {
        if (null == newInstance) {
            throw new IllegalArgumentException("newInstance must not be null");
        }
        instance = newInstance;
    }
    
    /**
     * Returns the singleton instance allowing access to object sizes.
     * 
     * @return the singleton instance
     * 
     * @since 1.00
     */
    public static final IObjectSizeProvider getInstance() {
        return instance;
    }
    
    /**
     * Returns the size of the given object as allocated by the JVM. This
     * method is a dummy implementation.
     * 
     * @param object the object the size should be queried for
     * @return the size of the physical memory allocated for <code>object</code>
     *         (in bytes, negative or zero if invalid or not available, 
     *         always 0)
     * 
     * @since 1.00
     */
    @Override
    public long getObjectSize(Object object) {
        return ObjectSizeEstimator.getObjectSize(object);
    }

}
