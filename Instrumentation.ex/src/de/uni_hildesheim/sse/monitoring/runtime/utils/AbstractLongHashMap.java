package de.uni_hildesheim.sse.monitoring.runtime.utils;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * An abstract hash map for mapping long values. This class does not specify
 * to which kind of objects to map to. Therefore, there is no real get method
 * and no real put method. These methods need to be added by concrete 
 * implementing classes.
 */
public abstract class AbstractLongHashMap {

   /**
    * The default capacity for hash map instances.
    */
    public static final int DEFAULT_CAPACITY = 17;

   /**
    * The maximum allowed capacity for hash map instances.
    */
    public static final int MAXIMUM_CAPACITY = 1 << 30;

   /**
    * The default load factor for hash map instances.
    */
    public static final float DEFAULT_LOADFACTOR = 0.75f;
     
   /**
    * Stores the first bucket for each key.
    */
    private MapElement[] map = null;
     
   /**
    * Stores the number of elements.
    */      
    private int contents = 0;

    /**
     * The number of keys contained in this map.
     */
    private int keySize;
    
   /**
    * Stores the counter for objects created.
    */   
    private int objectCounter = 0;           // Counter for objects created

   /**
    * Stores the current capacity.
    */   
    private int capacity = DEFAULT_CAPACITY;
     
   /**
    * Stores the initial capacity.
    */      
    //private int initialCap = DEFAULT_CAPACITY;
     
   /**
    * Stores the load factor.
    */      
    private float loadFactor = DEFAULT_LOADFACTOR;
     
   /**
    * Stores the maximum load.
    */   
    private int maxLoad = 0;
     
   /**
    * Stores if rehashing is permitted.
    */      
    private boolean rehashing = true;
    
   /**
    * Constructs an empty instance with the default initial capacity and the 
    * default load factor.
    */
    public AbstractLongHashMap() {
        this(DEFAULT_CAPACITY, DEFAULT_LOADFACTOR);
    }

   /**
    * Constructs an empty instance with the given initial capacity and the 
    * default load factor.
    *
    * @param initialCapacity The initial capacity for this hash map.
    */
    public AbstractLongHashMap(int initialCapacity) {
        this(initialCapacity, DEFAULT_LOADFACTOR);
    }

   /**
    * Constructs an empty instance with the given initial capacity and the 
    * given load factor.
    * 
    * @param initialCapacity The initial capacity for this hash map.
    * @param loadFactor      The load factor for this hash map.
    */
    public AbstractLongHashMap(int initialCapacity, float loadFactor) {
        construct(initialCapacity, loadFactor);
    }

   /**
    * Constructs a new LongHashMap with the same mappings as the specified Map. 
    * The LongHashMap is created with default load factor and an initial 
    * capacity sufficient to hold the mappings in the specified Map.
    * 
    * @param map The map whose mappings are to be placed in this map. 
    * @throws NullPointerException if the specified map is <code>null</code>.
    */
    public AbstractLongHashMap(AbstractLongHashMap map) {
        if (map == null) {
            throw new IllegalArgumentException("m may not be null");
        }

        //.... Determine parameters

        loadFactor = DEFAULT_LOADFACTOR;
        capacity   = (int) (map.size() / loadFactor);
        if (capacity < DEFAULT_CAPACITY) {
            // Avoid underflow
            capacity = DEFAULT_CAPACITY;
        } else if (capacity % 2 == 0) {
            // Make sure we have an odd value
            capacity++;
        }

        //.... Standard initialization for the internal map elements

        maxLoad = (int) (loadFactor * capacity + 0.5f);
        //initialCap = capacity;

        objectCounter += 2;
        this.map = new MapElement[capacity];
        putAll(map);
    }

   /**
    * Puts all the keys and values in the specified hash
    * map into this hash map.
    *
    * @param map the source map
    */  
    public abstract void putAll(AbstractLongHashMap map); 

   /**
    * Return the current number of mappings in the hash map.
    *
    * @return The current number of mappings in the hash map.
    */
    public int size() {
        return contents;
    }

   /**
    * Returns <code>true</code> if this map contains no key-value mappings.
    * 
    * @return <code>true</code> if there are no mappings, <code>false</code> 
    * else
    */
    public boolean isEmpty() {
        return contents == 0 ? true : false;
    }

   /**
    * Removes all mappings from this map.
    */
    public void clear() {
        //construct(initialCap, loadFactor);
        
        //MapElement me = null;

        for (int i = 0; i < capacity; i++) {
            if (map[i] != null) {
                /*me = map[i];
                for (int j = 0; j < count[i]; j++) {
                    MapElement release = me;
                    me = me.getNext();
                    releaseMapElement(release);
                }*/ 
                map[i] = null;
            }
        }
        contents = 0;
        keySize = 0;
    }

   /**
    * Return the number of objects created in / by this instance.
    *
    * @return The number of objects created
    */
    public int getObjectCounter() {
        return objectCounter;
    }

   /**
    * Return the current capacity of the instance. If rehashing is enabled 
    * (which it is per default), the capacity may have been increased as 
    * necessary from the initial value.
    * 
    * @return The current capacity for this hash map.
    */
    public int getCapacity() {
        return capacity;
    }

   /**
    * Return the load factor of the instance.
    * 
    * @return The load factor for this hash map.
    */
    public float getLoadFactor() {
        return loadFactor;
    }

   /**
    * Return the keys in the hash map.
    * 
    * @return An array containing the keys for which mappings are stored in 
    * this hash map.
    */
    public long[] keySet() {
        objectCounter++;
        long[] keys = new long[keySize];
        int cnt = 0;
        MapElement me = null;

        for (int i = 0; i < capacity; i++) {
            if (map[i] != null) {
                me = map[i];
                while (null != me) {
                    keys[cnt++] = me.getKey();
                    me = me.getNext();
                }
            }
        }
        return keys;
    }
    
    /**
     * Implements a map element iterator.
     * 
     * @author Holger Eichelberger
     * @since 1.00
     * @version 1.00
     */
    protected class MapIterator implements Iterator<MapElement>, 
        Iterable<MapElement> {
        
        /**
         * Stores the position.
         */
        private int pos = 0;
        
        /**
         * Stores the current element.
         */
        private MapElement current = null;

        /**
         * Initializes the iterator and places it to the first element.
         * 
         * @since 1.00
         */
        public MapIterator() {
            findNext(false);
        }

        /**
         * Returns the next element.
         * 
         * @return the next element
         * @throws NoSuchElementException in case that there is no next element
         */
        public MapElement next() {
            return findNext(true);
        }
        
        /**
         * Returns an iterator over a set of elements of type T.
         * 
         * @return an Iterator.
         */
        public Iterator<MapElement> iterator() {
            return this;
        }

        /**
         * Returns whether there is a next element.
         * 
         * @return <code>true</code> if there is a next element, 
         *   <code>false</code> else
         */
        @Override
        public boolean hasNext() {
            return null != current;
        }

        /**
         * Searches for the next element, either in {@link #map} or in the
         * map element chains and returns the current element ({@link #current} 
         * before call). This method modifies {@link #current} as a side effect.
         * 
         * @param throwException whether this method should throw an exception
         *   if there is no next element
         * @return the current element (before the call), may be 
         *   <b>null</b> if <code>throwException</code> is false
         * @throws NoSuchElementException in case that 
         *   <code>throwException</code> is <code>true</code> and there is no 
         *   next element
         * 
         * @since 1.00
         */
        protected MapElement findNext(boolean throwException) {
            if (null == current && throwException) {
                // to be entered when hasNext said false and next is called 
                // afterwards
                throw new NoSuchElementException();
            }

            MapElement old = current;
            if (null != current) {
                // there is an element in chain to iterate over
                current = current.next;
                if (null == current) {
                    // but the next in chain does not exist, look for next 
                    // bucket
                    pos++;
                }
            }
            if (null == current) {
                // not found so far, skip empty buckets and store next current
                // remains null if it does not exist
                while (pos < map.length && null == map[pos]) {
                    pos++;
                }
                if (pos < map.length) {
                    current = map[pos];
                }
            }
            return old;
        }

        /**
         * Removes the current element. (not supported)
         * 
         * @throws UnsupportedOperationException always
         */
        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

    }
    
    /**
     * Implements an abstract delegating element iterator. This class is 
     * intended to support the realization of value or key iterators. It reuses
     * the map iterator and provides the capability of projecting the result
     * for each individual element. The <code>next</code>
     * method is left open and should be implemented by <code>return 
     * iter.next().<i>doSomethingToGet<b>T</b></i></code> or equivalently
     * <code>return iter.findNext(true).<i>doSomethingToGet<b>T</b></i></code>.
     * 
     * @param <T> the element type to iterate over
     * @author Holger Eichelberger
     * @since 1.00
     * @version 1.00
     */
    protected abstract static class DelegatingMapIterator<T> 
        implements Iterator<T>, Iterable<T> {

        /**
         * Stores the map iterator.
         */
        protected MapIterator iter;
        
        /**
         * Creates a new delegating iterator.
         * 
         * @param iter the iterator to delegate to
         * 
         * @since 1.00
         */
        public DelegatingMapIterator(MapIterator iter) {
            this.iter = iter;
        }
        
        /**
         * Removes the current element. (not supported)
         * 
         * @throws UnsupportedOperationException always
         */
        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        /**
         * Returns an iterator over a set of elements of type T.
         * 
         * @return an Iterator.
         */
        @Override
        public Iterator<T> iterator() {
            return this;
        }

        /**
         * Returns whether there is a next element.
         * 
         * @return <code>true</code> if there is a next element, 
         *   <code>false</code> else
         */
        @Override
        public boolean hasNext() {
            return iter.hasNext();
        }

    }

    /**
     * Returns an iterator over the entries stored in this map.
     * 
     * @return the iterator
     * 
     * @since 1.00
     */
    public Iterable<MapElement> elements() {
        return new MapIterator() {

            @Override
            public MapElement next() {
                return findNext(true);
            }
            
        };
    }

   /**
    * Enable/disable rehashing (defaults to <code>true</code>).
    * 
    * @param rehashing A boolean indicating the desired rehashing status.
    */
    public void setRehash(boolean rehashing) {
        this.rehashing = rehashing;
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
    protected Object putElement(long key, Object value) {
        int index = (int) (key % capacity);
        if (index < 0) {
            index = -index;
        }
        if (map[index] == null) {
            //.... This is a new key since no bucket exists
            objectCounter++;
            map[index] = create(key, value);
            keySize++;
            contents++;
            if (contents > maxLoad) {
                rehash();
            }
            return null;
        } else {
            //.... A bucket already exists for this index: check whether 
            // we already have a mapping for this key
            MapElement me = map[index];
            while (true) {
                if (me.getKey() == key) {
                    return me.atInsert(value);
                } else {
                    if (me.getNext() == null) {
                        // No next element: so we have no mapping for this key
                        objectCounter++;
                        MapElement result = create(key, value);
                        me.setNext(result);
                        contents++;
                        if (contents > maxLoad) {
                            rehash();
                        }
                        return null;
                    } else {
                        me = me.getNext();
                    }
                }
            }
        }
    }

   /**
    * Returns <code>true</code> if this map contains a mapping for the 
    * specified key.
    *
    * @param key The key whose presence in this map is to be tested.
    *
    * @return <code>true</code> if this map contains a mapping for the 
    *     specified key.
    */
    public boolean containsKey(long key) {
        return getElement(key) != null;
    }

   /**
    * Removes the mapping for this key from this map if present. This method is
    * declared protected in order to facilitate reuse of the map element 
    * instance. Please do not return the result of this method to the caller.
    *
    * @param key The key whose mapping is to be removed from the map.
    *
    * @return the map element used to store the mapping (do not return to 
    *     caller)
    */
    protected MapElement removeElement(long key) {
        int index = (int) (key % capacity);
        if (index < 0) {
            index = -index;
        }

        if (map[index] == null) {
            return null;
        } else {
            MapElement me = map[index];
            MapElement prev = null;

            while (true) {
                if (me.getKey() == key) {
                    // Keys match
                    if (me.beforeRemove()) {
                        if (prev == null) {
                            // The first element in the chain matches
                            map[index] = me.getNext();
                        } else {
                            // An element further down in the chain matches - 
                            // delete it from the chain
                            prev.setNext(me.getNext());
                        }
                        contents--;
                    }
                    return me;
                } else {
                    // Keys don't match, try the next element
                    prev = me;
                    me = me.getNext();
                    if (me == null) {
                        return null;
                    }
                }
            }
        }
    }
    

   /**
    * Returns the element matching the key, or <b>null</b> 
    * if no such element exists.
    * 
    * @param key the key to look for
    * @return the element matching the key or <b>null</b>
    */
    public MapElement getElement(long key) {
        int index = (int) (key % capacity);
        if (index < 0) {
            index = -index;
        }

        if (map[index] == null) {
            return null;
        } else {
            MapElement me = map[index];
            while (true) {
                if (me.getKey() == key) {
                    return me;
                } else {
                    me = me.getNext();
                    if (me == null) {
                        return null;
                    }
                }
            }
        }
    }

    /**
     * Remove those elements which are not in <code>keys</code>.
     * 
     * @param keys the keys to check for
     * 
     * @since 1.00
     */
    // destroys keys!!
    public void removeNotIn(long[] keys) {
        MapElement me = null;

        for (int i = 0; i < capacity; i++) {
            if (map[i] != null) {
                me = map[i];
                while (null != me) {
                    long meKey = me.getKey();
                    for (int k = 0; k < keys.length; k++) {
                        if (keys[k] == meKey) {
                            keys[k] = -1;
                        }
                    }
                    me = me.getNext();
                }
            }
        }
        for (int k = 0; k < keys.length; k++) {
            if (keys[k] > 0) {
                removeElement(keys[k]);
            }
        }
    }

   /**
    * Increase the capacity of the map to improve performance.
    */
    private void rehash() {
        if (rehashing) {
            int newCapacity = 2 * capacity + 1;
            if (newCapacity > MAXIMUM_CAPACITY) {
                return;
            }

            objectCounter += 2;
            MapElement[] newMap = new MapElement[newCapacity]; 
  
            MapElement me       = null;
            MapElement t        = null;
            MapElement next     = null;
            int newIndex = 0;
  
            for (int index = 0; index < capacity; index++) {
                me = map[index];
                while (me != null) {
                    next = me.getNext();
                    newIndex = (int) (me.getKey() % newCapacity);
                    if (newIndex < 0) {
                        newIndex = -newIndex;
                    }
                    if (newMap[newIndex] == null) {
                        // No element yet for this new index
                        newMap[newIndex] = me;
                        me.setNext(null);
                    } else {
                        // Hook the element into the beginning of the chain
                        t = newMap[newIndex];
                        newMap[newIndex] = me;
                        me.setNext(t);
                    }
                    me = next;
                }
            }
  
            map = newMap;
            capacity = newCapacity;
            // Max. number of elements before a rehash occurs
            maxLoad  = (int) (loadFactor * capacity + 0.5f);

            newMap = null;
        }
    }
    
    /**
     * Creates an element.
     * 
     * @param key the hash key of the element
     * @param value the generic value to be attached
     * 
     * @return the new element
     * 
     * @since 1.00
     */
    protected MapElement create(long key, Object value) {
        return new MapElement(key);
    }

   /**
    * Construction helper method.
    * 
    * @param initialCapacity The initial capacity for this hash map.
    * @param loadFactor      The load factor for this hash map.
    */
    private void construct(int initialCapacity, float loadFactor) {
        if (initialCapacity < 0) {
            throw new IllegalArgumentException("Invalid initial capacity: " 
                + initialCapacity);
        }
        if (initialCapacity < DEFAULT_CAPACITY) {
            initialCapacity = DEFAULT_CAPACITY;
        }
        if (initialCapacity > MAXIMUM_CAPACITY) {
            initialCapacity = MAXIMUM_CAPACITY;
        }
        if (loadFactor <= 0.0f || Float.isNaN(loadFactor)) {
            throw new IllegalArgumentException("Invalid load factor: " 
                + loadFactor);
        }
    
        //this.initialCap = initialCapacity;
        this.capacity = initialCapacity;
        this.loadFactor = loadFactor;
    
        objectCounter += 2;
        // Max. number of elements before a rehash occurs
        maxLoad = (int) (loadFactor * capacity + 0.5f);
        map =  new MapElement[capacity];
        contents =  0;
    }
    
   /**
    * Implements a map element storing the contents of the buckets.
    */
    public static class MapElement {

       /**
        * Stores the key.
        */
        private long key = 0;
          
       /**
        * Stores the next map element.
        */
        private MapElement next  = null;
          
       /**
        * Constructor of a map element.
        * 
        * @param key the hash key of the element
        */
        protected MapElement(long key) {
            this.key   = key;
        }
    
       /**
        * Getter method for <code>key</code> property.
        * 
        * @return The value for the <code>key</code> property
        */
        public long getKey() {
            return key;
        }
    
       /**
        * Setter method for <code>next</code> property.
        * 
        * @param next The value for the <code>next</code> property
        */
        void setNext(MapElement next) {
            this.next = next;
        }

       /**
        * Getter method for <code>next</code> property.
        *
        * @return The value for the <code>next</code> property
        */
        public MapElement getNext() {
            return next;
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
         * @return the old value (<b>null</b> always)
         * 
         * @since 1.00
         */
        protected Object atInsert(Object value) {
            return null;
        }
        
    } 

}

