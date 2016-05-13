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
    private ArrayList<T> pool = new ArrayList<T>(5);
    
    /**
     * Stores a prototype instance for creating instances.
     */
    // TODO [performance] check if newInstaces from class is faster
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
		if (0 == size) { // || (maxSize > 0 && size >= maxSize)
			result = prototype.create();
		} else {
			result = pool.remove(size - 1);
		}
//System.out.println("OBTAIN: " + System.identityHashCode(result) + " " + result.getClass() + "" + maxSize + " " + pool.size());
        return result;
    }

   /**
    * Releases and clears the specified instance
    * to the shared pool.
    *
    * @param instance the instance to be released (must not be <b>null</b>)
    */  
    public final synchronized void release(T instance) {
//System.out.println("RELEASE?: " + System.identityHashCode(instance) + " " + instance.getClass() + " " + maxSize + " " + pool.size());
		instance.clear();
        if (maxSize == 0 || (maxSize > 0 && pool.size() < maxSize)) {
//System.out.println("RELEASE: " + System.identityHashCode(instance) + " " + instance.getClass() + " " + maxSize + " " + pool.size());
            pool.add(instance);   
        } //else {
//System.out.println("NO RELEASE: " + System.identityHashCode(instance) + " " + instance.getClass() + " " + maxSize + " " + pool.size());  
//        }
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
