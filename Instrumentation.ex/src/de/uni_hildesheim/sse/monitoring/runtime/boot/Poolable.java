package de.uni_hildesheim.sse.monitoring.runtime.boot;


/**
 * Defines a poolable instance, i.e. an instance that must be obtained from 
 * and returned to an {@link ObjectPool}. 
 * 
 * @param <T> the type of the element itself (used for factory-based cration)
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public interface Poolable <T> {
    
    /**
     * Clears this instance before returning it to the pool.
     * 
     * @since 1.00
     */
    public void clear();

    /**
     * Creates a further instance of this instances (intended as factory method
     * to be executed on a prototypical instance).
     * 
     * @return the created instance
     * 
     * @since 1.00
     */
    public T create();

}
