package de.uni_hildesheim.sse.monitoring.runtime.utils;

/**
 * A hash map mapping long values to objects.
 * 
 * @param <T> the value type
 */
public class LongHashMap<T> extends AbstractLongHashMap {

    /**
     * Constructs an empty instance with the default initial capacity and the 
     * default load factor.
     */
    public LongHashMap() {
        super();
    }

    /**
     * Constructs an empty instance with the given initial capacity and the 
     * default load factor.
     *
     * @param initialCapacity The initial capacity for this hash map.
     */
    public LongHashMap(int initialCapacity) {
        super(initialCapacity);
    }

    /**
     * Constructs an empty instance with the given initial capacity and the 
     * given load factor.
     * 
     * @param initialCapacity The initial capacity for this hash map.
     * @param loadFactor      The load factor for this hash map.
     */
    public LongHashMap(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    /**
     * Constructs a new LongHashMap with the same mappings as the specified Map.
     * The LongHashMap is created with default load factor and an initial 
     * capacity sufficient to hold the mappings in the specified Map.
     * 
     * @param map The map whose mappings are to be placed in this map. 
     * @throws NullPointerException if the specified map is <code>null</code>.
     */
    public LongHashMap(LongHashMap<T> map) {
        super(map);
    }

    /**
     * Puts all the keys and values in the specified hash
     * map into this hash map.
     *
     * @param map the source map
     */  
    @SuppressWarnings("unchecked")
    @Override
    public void putAll(AbstractLongHashMap map) {
        LongHashMap<T> lMap = (LongHashMap<T>) map;
        long[] keys = map.keySet();
        for (int i = 0; i < map.size(); i++) {
            put(keys[i], lMap.get(keys[i]));
        }
    }

    /**
     * Returns an iterator over the values stored in this map.
     * 
     * @return the iterator
     * 
     * @since 1.00
     */
    public Iterable<T> values() {
        return new DelegatingMapIterator<T>(new MapIterator()) {

            @SuppressWarnings("unchecked")
            @Override
            public T next() {
                return ((MapElement<T>) iter.findNext(true)).value;
            }
            
        };
    }
    
    /**
     * Returns an iterator over the entries stored in this map.
     * 
     * @return the iterator
     * 
     * @since 1.00
     */
    public Iterable<MapElement<T>> entries() {
        return new DelegatingMapIterator<MapElement<T>>(new MapIterator()) {

            @SuppressWarnings("unchecked")
            @Override
            public MapElement<T> next() {
                return (MapElement<T>) iter.findNext(true);
            }
            
        };
    }

    /**
     * Associates the specified value with the specified key in this map. If 
     * the map previously contained a mapping for this key, the old value is 
     * replaced.
     *
     * @param key The key with which the specified value is to be associated.
     * @param value The value to be associated with the specified key. 
     *
     * @return Previous value associated with specified key, or 
     *     <code>null</code> if there was no mapping for key. A 
     *     <code>null</code> return can also indicate that the LongHashMap 
     *     previously associated <code>null</code> 
     *         with the specified key.
     */
    @SuppressWarnings("unchecked")
    public T put(long key, T value) {
        return (T) putElement(key, value);
    }

   /**
    * Returns the value to which the specified key is mapped in this identity 
    * hash map, or <code>null</code> if the map contains no mapping for this 
    * key. A return value of <code>null</code> does not necessarily indicate 
    * that the map contains no mapping for the key; it is also possible that 
    * the map explicitly maps the key to <code>null</code>. The 
    * <code>containsKey</code> method may be used to distinguish these two 
    * cases.
    *
    * @param key The key whose associated value is to be returned. 
    *
    * @return The value to which this map maps the specified key, or 
    *         <code>null</code> if the map contains no mapping for this key.
    */
    @SuppressWarnings("unchecked")
    public T get(long key) {
        MapElement<T> me = (MapElement<T>) getElement(key);
        if (me == null) {
            return null;
        } else {
            return me.getValue();
        }
    }
    
   /**
    * Removes the mapping for this key from this map if present.
    *
    * @param key The key whose mapping is to be removed from the map.
    *
    * @return Previous value associated with specified key, or 
    *         <code>null</code>  if there was no mapping for key. A 
    *         <code>null</code> return can also indicate that the map 
    *         previously associated <code>null</code>  with the specified key.
    */
    @SuppressWarnings("unchecked")
    public T remove(long key) {
        MapElement<T> me = (MapElement<T>) removeElement(key);
        T result;
        if (null != me) {
            result = me.getValue();
        } else {
            result = null;
        }
        return result;
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
    * Implements a map element storing the contents of the buckets.
    */
    public static class MapElement <V> extends AbstractLongHashMap.MapElement {

       /**
        * Stores the value.
        */    
        private V value = null;
          
       /**
        * Constructor of a map element.
        * 
        * @param key the hash key of the element
        * @param value the value to be stored
        */
        MapElement(long key, V value) {
            super(key);
            this.value = value;
        }
    
       /**
        * Setter method for <code>value</code> property.
        *
        * @param value The value for the <code>value</code> property
        */
        void setValue(V value) {
            this.value = value;
        }
    
       /**
        * Getter method for <code>value</code> property.
        * 
        * @return The value for the <code>value</code> property
        */
        public V getValue() {
            return value;
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
        protected boolean beforeRemove() {
            return true;
        }

        /**
         * Called at insertion of this map element when an element of the
         * same key was inserted before. The default behavior is to overwrite
         * the value. [extension point]
         * 
         * @param value the new value
         * @return the old value
         * 
         * @since 1.00
         */
        @SuppressWarnings("unchecked")
        @Override
        protected Object atInsert(Object value) {
            V previous = this.value;
            this.value = (V) value;
            return previous;
        }
        
    } 

}

