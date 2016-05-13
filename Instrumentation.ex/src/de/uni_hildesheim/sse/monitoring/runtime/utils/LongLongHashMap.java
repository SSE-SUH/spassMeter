package de.uni_hildesheim.sse.monitoring.runtime.utils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import de.uni_hildesheim.sse.monitoring.runtime.boot.ArrayList;

/**
 * A hash map mapping long values to objects.
 */
public class LongLongHashMap {
    
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
    * Stores the {@link MapElement} pool.
    */
    private static ArrayList<MapElement> mapElementPool 
        = new ArrayList<MapElement>(5);
     
   /**
    * Stores the first bucket for each key.
    */
    private MapElement[] map = null;
     
   /**
    * Stores the number of elements.
    */      
    private int contents = 0;
     
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
    public LongLongHashMap() {
        this(DEFAULT_CAPACITY, DEFAULT_LOADFACTOR);
    }

   /**
    * Constructs an empty instance with the given initial capacity and the 
    * default load factor.
    *
    * @param initialCapacity The initial capacity for this hash map.
    */
    public LongLongHashMap(int initialCapacity) {
        this(initialCapacity, DEFAULT_LOADFACTOR);
    }

   /**
    * Constructs an empty instance with the given initial capacity and the 
    * given load factor.
    * 
    * @param initialCapacity The initial capacity for this hash map.
    * @param loadFactor      The load factor for this hash map.
    */
    public LongLongHashMap(int initialCapacity, float loadFactor) {
        construct(initialCapacity, loadFactor);
    }

   /**
    * Constructs a new LongHashMap with the same mappings as the specified Map. 
    * The LongLongHashMap is created with default load factor and an initial 
    * capacity sufficient to hold the mappings in the specified Map.
    * 
    * @param map The map whose mappings are to be placed in this map. Throws:
    * @throws NullPointerException if the specified map is <code>null</code>.
    */
    public LongLongHashMap(LongLongHashMap map) {
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

        maxLoad    = (int) (loadFactor * capacity + 0.5f);
        //initialCap = capacity;

        objectCounter += 2;
        this.map = new MapElement[capacity];

        //.... Copy the elements to the new map 

        long[] keys = map.keySet();
        for (int i = 0; i < map.size(); i++) {
            put(keys[i], map.get(keys[i]));
        }
    }

   /**
    * Puts all the keys and values in the specified hash
    * map into this hash map.
    *
    * @param map the source map
    * 
    * @since SugiBib 1.30
    */  
    public void putAll(LongLongHashMap map) {
        long[] keys = map.keySet();
        for (int i = 0; i < map.size(); i++) {
            put(keys[i], map.get(keys[i]));
        }
    }

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
    * @return <code>true</code> in case of no elements, <code>false</code> else
    */
    public boolean isEmpty() {
        return contents == 0 ? true : false;
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
        long[] keys = new long[contents];
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
     * Writes the map to the given stream.
     * 
     * @param out the stream to write to
     * @throws IOException in case of any I/O problem
     */
    public void write(DataOutputStream out) throws IOException {
        MapElement me = null;
        out.writeInt(size());
        for (int i = 0; i < capacity; i++) {
            if (map[i] != null) {
                me = map[i];
                while (null != me) {
                    out.writeLong(me.getKey());
                    out.writeLong(me.getValue());
                    me = me.getNext();
                }
            }
        }
    }
     
    /**
     * Reads contents of this map from the given input stream.
     * 
     * @param in the stream to read from
     * @throws IOException in case of any I/O problem
     * 
     * @since 1.00
     */
    public void read(DataInputStream in) throws IOException {
        int size = in.readInt();
        for (int i = 0; i < size; i++) {
            long key = in.readLong();
            long value = in.readLong();
            put(key, value);
        }
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
    */
    public void put(long key, long value) {
        int index = (int) (key % capacity);
        if (index < 0) {
            index = -index;
        }

        //.... This is a new key since no bucket exists
        if (map[index] == null) {
            objectCounter++;
            map[index] = getMapElementFromPool(key, value);
            contents++;
            if (contents > maxLoad) {
                rehash();
            }
            //.... A bucket already exists for this index: check whether 
            // we already have a mapping for this key
        } else {
            MapElement me = map[index];
            while (true) {
                if (me.getKey() == key) {
                    // We have a mapping: just replace the value for this elt
                    me.setValue(value);
                    return;
                } else {
                    if (me.getNext() == null) {
                        // No next element: so we have no mapping for this key
                        objectCounter++;
                        me.setNext(getMapElementFromPool(key, value));
                        contents++;
                        if (contents > maxLoad) {
                            rehash();
                        }
                        return;
                    } else {
                        me = me.getNext();
                    }
                }
            }
        }
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
    public long get(long key) {
        MapElement me = exists(key);
        if (me == null) {
            throw new IllegalArgumentException("not found");
        } else {
            return me.getValue();
        }
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
     * @param deflt the default value to be return if <code>key</code> is not 
     *   found
     *
     * @return The value to which this map maps the specified key, or 
     *         <code>null</code> if the map contains no mapping for this key
     *         or <code>deflt</code>.
     */
    public long get(long key, long deflt) {
        MapElement me = exists(key);
        if (me == null) {
            return deflt;
        } else {
            return me.getValue();
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
        return exists(key) != null;
    }

   /**
    * Removes the mapping for this key from this map if present.
    *
    * @param key The key whose mapping is to be removed from the map.
    */
    public void remove(long key) {
        int index = (int) (key % capacity);
        if (index < 0) {
            index = -index;
        }

        if (map[index] == null) {
            return;
        } else {
            MapElement me   = map[index];
            MapElement prev = null;

            while (true) {
                if (me.getKey() == key) {
                    // Keys match
                    if (prev == null) {
                        // The first element in the chain matches
                        map[index] = me.getNext();
                    } else {
                        // An element further down in the chain matches - 
                        // delete it from the chain
                        prev.setNext(me.getNext());
                    }
                    releaseMapElement(me);
                    contents--;
                    return;
                } else {
                    // Keys don't match, try the next element
                    prev = me;
                    me = me.getNext();
                    if (me == null) {
                        return;
                    }
                }
            }
        }
    }

   /**
    * Helper method: returns the element matching the key, or <code>null</code> 
    * if no such element exists.
    * 
    * @param key the key to search for
    * @return the element matching the key
    */
    private MapElement exists(long key) {
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
    static class MapElement {

       /**
        * Stores the key.
        */
        private long key = 0;
          
       /**
        * Stores the value.
        */    
        private long value = 0;
          
       /**
        * Stores the next map element.
        */
        private MapElement next  = null;
          
       /**
        * Constructor of a map element.
        * 
        * @param key the hash key of the element
        * @param value the value to be stored
        */
        public MapElement(long key, long value) {
            this.key = key;
            this.value = value;
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
        * Setter method for <code>value</code> property.
        *
        * @param value The value for the <code>value</code> property
        */
        void setValue(long value) {
            this.value = value;
        }

        /**
         * Setter method for <code>key</code> property.
         *
         * @param key The value for the <code>key</code> property
         */
        void setKey(long key) {
            this.key = key;
        }
        
       /**
        * Getter method for <code>value</code> property.
        * 
        * @return The value for the <code>value</code> property
        */
        public long getValue() {
            return value;
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
    } 
    
    /**
     * Cleans up this map by removing all entries.
     * 
     * @since 1.00
     */
    public void clear() {
        //construct(initialCap, loadFactor);
        MapElement me = null;

        for (int i = 0; i < capacity; i++) {
            if (map[i] != null) {
                me = map[i];
                while (null != me) {
                    releaseMapElement(me);
                    me = me.getNext();
                }
                map[i] = null;
            }
        }
        contents = 0;
    }
    
    /**
     * Clean and finalize.
     * 
     * @throws Throwable any exception that may occur during finalization
     */
    protected void finalize() throws Throwable {
        clear();
        super.finalize();
    }
     
   /**
    * Returns a {@link MapElement} from the shared pool.
    * 
    * @param key the key value the resulting element should be initialized with
    * @param value the value the resulting element should be initialized with
    * @return  the instance from the pool. This instance has 
    *          to be released by {@link #releaseMapElement(MapElement)}.
    */    
    private static final synchronized MapElement getMapElementFromPool(long key,
        long value) {
        int size = mapElementPool.size();
        MapElement result;
        if (0 == size) {
            result = new MapElement(key, value);
        } else {
            result = mapElementPool.remove(size - 1);
        }
        result.setValue(value);
        result.setKey(key);
        return result;
    }

    /**
     * Releases and clears the specified {@link MapElement}
     * to the shared pool.
     *
     * @param mapElement the {@link MapElement} to be released (must not 
     *     be <b>null</b>)
     */  
    private static final synchronized void releaseMapElement(
        MapElement mapElement) {
        mapElement.setNext(null);
        mapElementPool.add(mapElement);
    }

}

