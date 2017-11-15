package de.uni_hildesheim.sse.monitoring.runtime.boot;


/**
 * Defines an reusable object pool.
 *
 * @param <T> the poolable type
 * @author  Holger Eichelberger
 */
public class ObjectPool<T extends Poolable<T>> {

    /**
    * Stores the pool.
    */
    private ArrayList<T> pool; // TODO check replace by linked list
    
    /**
     * Stores a prototype instance for creating instances.
     */
    private T prototype;
    
    /**
     * Stores the maximum size of the pool. If 0, no maximum size is given
     * ant the pool may grow as needed (may be inefficient).
     */
    private int maxSize = 0;
    
    /**
     * Creates a new unlimited pool.
     * 
     * @param prototype the prototype instance for object creation (must not 
     * be <b>null</b>)
     * 
     * @since 1.00
     */
    public ObjectPool(T prototype) {
        this.prototype = prototype;
        this.pool = new ArrayList<T>(10);
    }
    
    /**
     * Creates a new pool.
     * 
     * @param prototype the prototype instance for object creation (must not 
     * be <b>null</b>)
     * @param maxSize the maximum size of the pool
     * 
     * @since 1.00
     */
    public ObjectPool(T prototype, int maxSize) {
        this.prototype = prototype;
        this.maxSize = maxSize;
        // reversing the usual capacity increase of ArrayList
        int poolSize = maxSize; // ((maxSize - 1) * 2) / 3 - 1;
        if (poolSize < 10) {
            poolSize = 10;
        }
        this.pool = new ArrayList<T>(poolSize);
    }
    
   /**
    * Returns an instance from the shared pool.
    * 
    * @return  the instance from the pool. This instance has 
    *          to be released by {@link #release(Poolable)}.
    *
    * @since   SugiBib 1.20
    */    
    public final synchronized T getFromPool() {
        int size = pool.size();
        T result;
        if (0 == size) {
            result = prototype.create();
        } else {
            result = pool.remove(size - 1);
        }
        return result;
    }

   /**
    * Releases and clears the specified instance
    * to the shared pool.
    *
    * @param instance the instance to be released (must not be <b>null</b>)
    */  
    public final synchronized void release(T instance) {
        instance.clear();
        if (maxSize == 0 || (maxSize > 0 && pool.size() < maxSize)) {
            pool.add(instance);   
        }
    }
    
    /**
     * Returns the size of the pool.
     * 
     * @return the current size of the pool
     * 
     * @since 1.00
     */
    public synchronized int size() {
        return pool.size();
    }

}
