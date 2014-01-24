package de.uni_hildesheim.sse.monitoring.runtime.utils;

/**
 * Defines a long hash map being able to cleanup itself. This class assumes that
 * elements with the same key point to the same value. Multiple insertions of 
 * the same key leads to incrementing a counter in the element, deletions to
 * decrementing a counter.
 * 
 * @param <T> the element type
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public class CleaningLongHashMap<T> extends LongHashMap<T> {

    /**
     * Refines the map element by a counter for duplicate entries.
     * 
     * @param <V> the element type
     * @author Holger Eichelberger
     * @since 1.00
     * @version 1.00
     */
    public static class MapElement<V> extends LongHashMap.MapElement<V> {
        
        /**
         * Stores the counter. Values more than <code>1</code> indicate 
         * duplicates.
         */
        private int count = 1;

        /**
         * Constructor of a map element.
         * 
         * @param key the hash key of the element
         * @param value the value to be stored
         */
        MapElement(long key, V value) {
            super(key, value);
        }
        
        /**
         * Returns how often the key was inserted.
         * 
         * @return the number of insertions
         * 
         * @since 1.00
         */
        public int getCount() {
            return count;
        }
        
        /**
         * Perform a check before finally removing this element. 
         * [extension point]
         * 
         * @return <code>true</code> if it really should be removed, 
         *   <code>false</code> if removal should not be carried out
         * 
         * @since 1.00
         */
        @Override
        protected boolean beforeRemove() {
            count--;
            return (count < 1);
        }

        /**
         * Called at insertion of this map element when an element of the
         * same key was inserted before. Here the counter is incremented and
         * a warning is emitted in case that the previous value is not
         * the same as the inserted value. [extension point]
         * 
         * @param value the new value
         * @return the old value
         * 
         * @since 1.00
         */
        @Override
        protected Object atInsert(Object value) {
            V previous = getValue();
            if (value != previous) {
                System.err.println("matching keys should not be replaced " 
                    + "by different values");
            }
            count++;
            return previous;
        }
        
    }
    
    /**
     * Constructs an empty instance with the default initial capacity and the 
     * default load factor.
     */
    public CleaningLongHashMap() {
        super();
    }

    /**
     * Constructs an empty instance with the given initial capacity and the 
     * default load factor.
     *
     * @param initialCapacity The initial capacity for this hash map.
     */
    public CleaningLongHashMap(int initialCapacity) {
        super(initialCapacity);
    }

    /**
     * Constructs an empty instance with the given initial capacity and the 
     * given load factor.
     * 
     * @param initialCapacity The initial capacity for this hash map.
     * @param loadFactor      The load factor for this hash map.
     */
    public CleaningLongHashMap(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

     /**
      * Creates an element.
      * 
      * @param key the hash key of the element
      * @param value the value to be stored
      * 
      * @return the new element
      * 
      * @since 1.00
      */
    @SuppressWarnings("unchecked")
    @Override
    protected LongHashMap.MapElement<T> create(long key, Object value) {
        return new MapElement<T>(key, (T) value);
    }

    /**
     * Returns the element matching the key, or <b>null</b> 
     * if no such element exists.
     * 
     * @param key the key to look for
     * @return the element matching the key or <b>null</b>
     */
    @SuppressWarnings("unchecked")
    public MapElement<T> getElement(long key) {
        return (MapElement<T>) super.getElement(key);
    }

}
