package de.uni_hildesheim.sse.monitoring.runtime.utils;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Reimplements a simple version of the hashmap <code>java.util.HashMap</code> 
 * in order to release dependencies during instrumentation.
 * 
 * @param <K> the key type
 * @param <V> the value type
 * @author from SUN/ORACLE
 * @since 1.00
 * @version 1.00
 */
public class HashMap<K, V> {

    /**
     * Implements a bucket entry.
     * 
     * @param <K> the key type
     * @param <V> the value type
     * @author from SUN/ORACLE
     * @since 1.00
     * @version 1.00
     */
    public static class Entry<K, V> {
        
        /**
         * Stores the key.
         */
        private final K key;
        
        /**
         * Stores the value.
         */
        private V value;
        
        /**
         * Stores the next entry.
         */
        private Entry<K, V> next;
        
        /**
         * Stores the hash value.
         */
        private final int hash;

        /**
         * Creates new entry.
         * 
         * @param hash the hash value
         * @param key the key
         * @param value the value
         * @param next the next entry
         */
        Entry(int hash, K key, V value, Entry<K, V> next) {
            this.value = value;
            this.next = next;
            this.key = key;
            this.hash = hash;
        }

        /**
         * Returns the key.
         * 
         * @return the key
         * 
         * @since 1.00
         */
        public final K getKey() {
            return key;
        }

        /**
         * Returns the value.
         * 
         * @return the value
         * 
         * @since 1.00
         */
        public final V getValue() {
            return value;
        }

        /**
         * Changes the value.
         * 
         * @param newValue the new value
         * @return the old value
         * 
         * @since 1.00
         */
        public final V setValue(V newValue) {
            V oldValue = value;
            value = newValue;
            return oldValue;
        }

        /**
         * Checks for equality with <code>object</code>.
         *
         * @param object the object to check for
         * @return <code>true</code> if <code>object</code> is equal to this,
         *     <code>false</code> else
         */
        public final boolean equals(Object object) {
            if (!(object instanceof Entry)) {
                return false;
            }
            @SuppressWarnings("rawtypes")
            Entry e = (Entry) object;
            Object k1 = getKey();
            Object k2 = e.getKey();
            if (k1 == k2 || (k1 != null && k1.equals(k2))) {
                Object v1 = getValue();
                Object v2 = e.getValue();
                if (v1 == v2 || (v1 != null && v1.equals(v2))) {
                    return true;
                }
            }
            return false;
        }

        /**
         * Returns the hash code for this instance.
         * 
         * @return the hash code
         */
        public final int hashCode() {
            return (key == null   ? 0 : key.hashCode()) 
                ^ (value == null ? 0 : value.hashCode());
        }

        /**
         * Returns a textual representation of this instance.
         * 
         * @return the textual representation
         */
        public final String toString() {
            return getKey() + "=" + getValue();
        }

        /**
         * This method is invoked whenever the value in an entry is
         * overwritten by an invocation of put(k,v) for a key k that's already
         * in the HashMap.
         * 
         * @param map the hash map
         */
        void recordAccess(HashMap<K, V> map) {
        }

        /**
         * This method is invoked whenever the entry is
         * removed from the table.
         * 
         * @param map the hash map
         */
        void recordRemoval(HashMap<K, V> map) {
        }
    }

    
    /**
     * The default initial capacity - MUST be a power of two.
     */
    private static final int DEFAULT_INITIAL_CAPACITY = 16;

    /**
     * The maximum capacity, used if a higher value is implicitly specified
     * by either of the constructors with arguments.
     * MUST be a power of two &lt;= 1&lt;&lt;30.
     */
    private static final int MAXIMUM_CAPACITY = 1 << 30;

    /**
     * The load factor used when none specified in constructor.
     */
    private static final float DEFAULT_LOAD_FACTOR = 0.75f;

    /**
     * The table, resized as necessary. Length MUST Always be a power of two.
     */
    @SuppressWarnings("rawtypes")
    private transient Entry[] table;

    /**
     * The number of key-value mappings contained in this map.
     */
    private transient int size;

    /**
     * The number of keys contained in this map.
     */
    private transient int keySize;
    
    /**
     * The next size value at which to resize (capacity * load factor).
     * @serial
     */
    private int threshold;

    /**
     * The load factor for the hash table.
     *
     * @serial
     */
    private final float loadFactor;

    /**
     * Constructs an empty <tt>HashMap</tt> with the specified initial
     * capacity and load factor.
     *
     * @param  initialCapacity the initial capacity
     * @param  loadFactor      the load factor
     * @throws IllegalArgumentException if the initial capacity is negative
     *         or the load factor is nonpositive
     */
    public HashMap(int initialCapacity, float loadFactor) {
        if (initialCapacity < 0) {
            throw new IllegalArgumentException("Illegal initial capacity: " 
                + initialCapacity);
        }
        if (initialCapacity > MAXIMUM_CAPACITY) {
            initialCapacity = MAXIMUM_CAPACITY;
        }
        if (loadFactor <= 0 || Float.isNaN(loadFactor)) {
            throw new IllegalArgumentException("Illegal load factor: " 
                + loadFactor);
        }

        // Find a power of 2 >= initialCapacity
        int capacity = 1;
        while (capacity < initialCapacity) {
            capacity <<= 1;
        }

        this.loadFactor = loadFactor;
        threshold = (int) (capacity * loadFactor);
        table = new Entry[capacity];
    }

    /**
     * Constructs an empty <tt>HashMap</tt> with the specified initial
     * capacity and the default load factor (0.75).
     *
     * @param  initialCapacity the initial capacity.
     * @throws IllegalArgumentException if the initial capacity is negative.
     */
    public HashMap(int initialCapacity) {
        this(initialCapacity, DEFAULT_LOAD_FACTOR);
    }

    /**
     * Constructs an empty <tt>HashMap</tt> with the default initial capacity
     * (16) and the default load factor (0.75).
     */
    public HashMap() {
        this.loadFactor = DEFAULT_LOAD_FACTOR;
        threshold = (int) (DEFAULT_INITIAL_CAPACITY * DEFAULT_LOAD_FACTOR);
        table = new Entry[DEFAULT_INITIAL_CAPACITY];
    }
    
    /**
     * Returns <tt>true</tt> if this map contains a mapping for the
     * specified key.
     *
     * @param   key   The key whose presence in this map is to be tested
     * @return <tt>true</tt> if this map contains a mapping for the specified
     * key.
     */
    public boolean containsKey(Object key) {
        return getEntry(key) != null;
    }

    /**
     * Applies a supplemental hash function to a given hashCode, which
     * defends against poor quality hash functions.  This is critical
     * because HashMap uses power-of-two length hash tables, that
     * otherwise encounter collisions for hashCodes that do not differ
     * in lower bits. Note: Null keys always map to hash 0, thus index 0.
     * 
     * @param key the key to hash
     * @return the hash value
     */
    static int hash(int key) {
        // This function ensures that hashCodes that differ only by
        // constant multiples at each bit position have a bounded
        // number of collisions (approximately 8 at default load factor).
        key ^= (key >>> 20) ^ (key >>> 12);
        return key ^ (key >>> 7) ^ (key >>> 4);
    }
    
    /**
     * Returns the entry associated with the specified key in the
     * HashMap.  Returns null if the HashMap contains no mapping
     * for the key.
     * @param key the key to return the stored object for
     * @return the stored object or <b>null</b>
     */
    @SuppressWarnings("unchecked")
    final Entry<K, V> getEntry(Object key) {
        int hash = (key == null) ? 0 : hash(key.hashCode());
        for (Entry<K, V> e = table[indexFor(hash, table.length)];
             e != null;
             e = e.next) {
            Object k;
            if (e.hash == hash && ((k = e.key) == key 
                || (key != null && key.equals(k)))) {
                return e;
            }
        }
        return null;
    }

    /**
     * Returns index for hash code h.
     * 
     * @param hash the hash code
     * @param length the length of the table
     * @return the index
     */
    static int indexFor(int hash, int length) {
        return hash & (length - 1);
    } 
    
    /**
     * Associates the specified value with the specified key in this map.
     * If the map previously contained a mapping for the key, the old
     * value is replaced.
     *
     * @param key key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     * @return the previous value associated with <tt>key</tt>, or
     *         <tt>null</tt> if there was no mapping for <tt>key</tt>.
     *         (A <tt>null</tt> return can also indicate that the map
     *         previously associated <tt>null</tt> with <tt>key</tt>.)
     */
    @SuppressWarnings("unchecked")
    public V put(K key, V value) {
        if (key == null) {
            return putForNullKey(value);
        }
        int hash = hash(key.hashCode());
        int i = indexFor(hash, table.length);
        for (Entry<K, V> e = table[i]; e != null; e = e.next) {
            Object k;
            if (e.hash == hash && ((k = e.key) == key || key.equals(k))) {
                V oldValue = e.value;
                e.value = value;
                e.recordAccess(this);
                return oldValue;
            }
        }

        addEntry(hash, key, value, i);
        return null;
    }

    /**
     * Offloaded version of put for null keys.
     * 
     * @param value the value
     * @return the old value
     */
    @SuppressWarnings("unchecked")
    private V putForNullKey(V value) {
        for (Entry<K, V> e = table[0]; e != null; e = e.next) {
            if (e.key == null) {
                V oldValue = e.value;
                e.value = value;
                e.recordAccess(this);
                return oldValue;
            }
        }
        addEntry(0, null, value, 0);
        return null;
    }
    
    /**
     * Adds a new entry with the specified key, value and hash code to
     * the specified bucket.  It is the responsibility of this
     * method to resize the table if appropriate.
     *
     * Subclass overrides this to alter the behavior of put method.
     * @param hash the hash code
     * @param key the key to add
     * @param value the value to add
     * @param bucketIndex the target bucket index
     */
    @SuppressWarnings("unchecked")
    void addEntry(int hash, K key, V value, int bucketIndex) {
        Entry<K, V> e = table[bucketIndex];
        table[bucketIndex] = new Entry<K, V>(hash, key, value, e);
        keySize++;
        if (size++ >= threshold) {
            resize(2 * table.length);
        }
    }
    
    /**
     * Rehashes the contents of this map into a new array with a
     * larger capacity.  This method is called automatically when the
     * number of keys in this map reaches its threshold.
     *
     * If current capacity is MAXIMUM_CAPACITY, this method does not
     * resize the map, but sets threshold to Integer.MAX_VALUE.
     * This has the effect of preventing future calls.
     *
     * @param newCapacity the new capacity, MUST be a power of two;
     *        must be greater than current capacity unless current
     *        capacity is MAXIMUM_CAPACITY (in which case value
     *        is irrelevant).
     */
    @SuppressWarnings("rawtypes")
    void resize(int newCapacity) {
        Entry[] oldTable = table;
        int oldCapacity = oldTable.length;
        if (oldCapacity == MAXIMUM_CAPACITY) {
            threshold = Integer.MAX_VALUE;
            return;
        }

        Entry[] newTable = new Entry[newCapacity];
        transfer(newTable);
        table = newTable;
        threshold = (int) (newCapacity * loadFactor);
    }
    
    /**
     * Transfers all entries from current table to newTable.
     * 
     * @param newTable the target table
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    void transfer(Entry[] newTable) {
        Entry[] src = table;
        int newCapacity = newTable.length;
        for (int j = 0; j < src.length; j++) {
            Entry<K, V> e = src[j];
            if (e != null) {
                src[j] = null;
                do {
                    Entry<K, V> next = e.next;
                    int i = indexFor(e.hash, newCapacity);
                    e.next = newTable[i];
                    newTable[i] = e;
                    e = next;
                } while (e != null);
            }
        }
    }
    
    /**
     * Returns the value to which the specified key is mapped,
     * or {@code null} if this map contains no mapping for the key.
     *
     * @param key the key to search for
     * @return the stored value, <b>null</b> if not found
     *
     * @see #put(Object, Object)
     */
    @SuppressWarnings("unchecked")
    public V get(Object key) {
        if (key == null) {
            return getForNullKey();
        }
        int hash = hash(key.hashCode());
        for (Entry<K, V> e = table[indexFor(hash, table.length)];
             e != null;
             e = e.next) {
            Object k;
            if (e.hash == hash && ((k = e.key) == key || key.equals(k))) {
                return e.value;
            }
        }
        return null;
    }
    
    /**
     * Removes the mapping of <code>key</code> and returns its value.
     * 
     * @param key the key to remove the entry
     * @return the value, <b>null</b> if there is not mapping for 
     *   <code>key</code>
     * 
     * @since 1.00
     */
    @SuppressWarnings("unchecked")
    public V remove(Object key) {
        int hash = (key == null) ? 0 : hash(key.hashCode());
        int i = indexFor(hash, table.length);
        Entry<K, V> prev = table[i];
        Entry<K, V> e = prev;

        while (e != null) {
            Entry<K, V> next = e.next;
            Object k;
            if (e.hash == hash 
                && ((k = e.key) == key || (key != null && key.equals(k)))) {
                size--;
                if (prev == e) {
                    table[i] = next;
                } else {
                    prev.next = next;
                }
                e.recordRemoval(this);
                return e.getValue();
            }
            prev = e;
            e = next;
        }
        return null;
    }

    /**
     * Offloaded version of get() to look up null keys.  Null keys map
     * to index 0.  This null case is split out into separate methods
     * for the sake of performance in the two most commonly used
     * operations (get and put), but incorporated with conditionals in
     * others.
     * 
     * @return the result for a null key
     */
    @SuppressWarnings("unchecked")
    private V getForNullKey() {
        for (Entry<K, V> e = table[0]; e != null; e = e.next) {
            if (e.key == null) {
                return e.value;
            }
        }
        return null;
    }

    /**
     * Returns the number of key-value mappings in this map.
     *
     * @return the number of key-value mappings in this map
     */
    public int size() {
        return size;
    }
    
    /**
     * Returns the number of keys in this map.
     * 
     * @return the number of keys
     * 
     * @since 1.00
     */
    public int keySize() {
        return keySize;
    }

    /**
     * Returns <tt>true</tt> if this map contains no key-value mappings.
     *
     * @return <tt>true</tt> if this map contains no key-value mappings
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Implements a generic iterator.
     * 
     * @param <V> the type of elements to iterate over
     * @author Holger Eichelberger
     * @since 1.00
     * @version 1.00
     */
    private abstract class MapIterator<I> implements Iterator<I>, Iterable<I> {
        
        /**
         * Stores the current position relative to {@link LongHashMap#map}.
         */
        private int currentPos = 0;
        
        /**
         * Stores the next position relative to {@link LongHashMap#map}.
         */
        private int pos = 0;
        
        /**
         * Stores the next element in bucket chain.
         */
        private Entry<K, V> next = null;

        /**
         * Stores the current element in bucket chain.
         */
        private Entry<K, V> current = null;

        /**
         * Stores the previous element in bucket chain.
         */
        private Entry<K, V> prev = null;
        
        /**
         * Initializes the iterator and places it to the first element.
         * 
         * @since 1.00
         */
        public MapIterator() {
            findNext(false);
        }
        
        /**
         * Returns an iterator over a set of elements of type T.
         * 
         * @return an Iterator.
         */
        public Iterator<I> iterator() {
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
            return null != next;
        }

        /**
         * Searches for the next element, either in {@link #map} or in the
         * map element chains and returns the current element ({@link #next} 
         * before call). This method modifies {@link #next} as a side effect.
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
        @SuppressWarnings("unchecked")
        protected Entry<K, V> findNext(boolean throwException) {
            if (null == next && throwException) {
                // to be entered when hasNext said false and next is called 
                // afterwards
                throw new NoSuchElementException();
            }

            currentPos = pos;
            prev = current;
            current = next;
            if (null != next) {
                // there is an element in chain to iterate over
                next = next.next;
                if (null == next) {
                    // but the next in chain does not exist, look for next 
                    // bucket
                    pos++;
                }
            }
            if (null == next) {
                // not found so far, skip empty buckets and store next current
                // remains null if it does not exist
                while (pos < table.length && null == table[pos]) {
                    pos++;
                }
                if (pos < table.length) {
                    next = (Entry<K, V>) table[pos];
                }
            }
            return current;
        }

        @Override
        public void remove() {
            if (null != current && currentPos < table.length) {
                current.recordRemoval(HashMap.this);
                size--;
                if (table[currentPos] == current) {
                    // start of bucket chain, no chain -> start = null
                    table[currentPos] = current.next;
                } else {
                    prev.next = next.next;
                }
            } else {
                throw new IllegalStateException();
            }
        }

    }

    /**
     * Returns an iterator over the values stored in this map.
     * 
     * @return the iterator
     * 
     * @since 1.00
     */
    public Iterable<V> values() {
        return new MapIterator<V>() {

            @Override
            public V next() {
                return findNext(true).value;
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
    public Iterable<Entry<K, V>> entries() {
        return new MapIterator<Entry<K, V>>() {

            @Override
            public Entry<K, V> next() {
                return findNext(true);
            }
            
        };
    }
    
    /**
     * A specific iterator for the keys.
     * 
     * @param <K> the key type
     * @author Holger Eichelberger
     * @since 1.00
     * @version 1.00
     */
    private class KeyIterator<I> implements Iterable<I>, Iterator<I> {

        /**
         * Stores the current iterator position.
         */
        private int pos = 0;
        
        /**
         * Creates a new iterator.
         * 
         * @since 1.00
         */
        KeyIterator() {
            while (pos < table.length && null == table[pos]) {
                pos++;
            }
        }

        /**
         * Returns the iterator.
         * 
         * @return the iterator
         */
        public Iterator<I> iterator() {
            return this;
        }

        /**
         * Returns whether there is a next element.
         * 
         * @return <code>true</code> if there is a next element, 
         *   <code>false</code> else
         */
        public boolean hasNext() {
            return pos + 1 < table.length;
        }

        /**
         * Returns the next element in the iterator sequence.
         * 
         * @return the next element
         */
        @SuppressWarnings("unchecked")
        public I next() {
            if (pos >= table.length) {
                throw new NoSuchElementException();
            }
            I result = (I) table[pos++].key;
            while (pos < table.length && null == table[pos]) {
                pos++;
            }
            return result;
        }
        
        /**
         * Removes the actual element. Not implemented.
         */
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
    
    /**
     * Returns an iterator over the keys stored in this map.
     * 
     * @return the iterator
     * 
     * @since 1.00
     */
    public Iterable<K> keys() {
        return new KeyIterator<K>();
    }


    /**
     * Removes all of the mappings from this map.
     * The map will be empty after this call returns.
     */
    public void clear() {
        for (int i = 0; i < table.length; i++) {
            table[i] = null;
        }
        size = 0;
        keySize = 0;
    }
    
    /**
     * Returns a textual representation of this map.
     * 
     * @return the textual representation
     */
    @SuppressWarnings("unchecked")
    public String toString() {
        StringBuilder builder = new StringBuilder("{");
        boolean appended = false;
        for (int i = 0; i < table.length; i++) {
            Entry<K, V> next = (Entry<K, V>) table[i];
            while (null != next) {
                if (appended) {
                    builder.append(", ");
                }
                builder.append(next.key);
                builder.append(" = ");
                builder.append(next.value);
                next = next.next;
                appended = true;
            }
        }
        builder.append("}");
        return builder.toString();
    }

    /**
     * Stores all keys of this map into <code>array</code>.
     * 
     * @param array the array to be modified as a side effect, nothing will 
     *   happen if <code>array == <b>null</b></code>, if array size is less 
     *   than {@link #keySize()} then only <code>array.length</code> keys will
     *   be copied 
     * 
     * @since 1.00
     */
    @SuppressWarnings("unchecked")
    public void keysToArray(K[] array) {
        if (null != array) {
            int pos = 0;
            for (int i = 0; pos < array.length && i < table.length; i++) {
                if (null != table[i]) {
                    K key = (K) table[i].key;
                    if (null != key) {
                        array[pos++] = key;
                    }
                }
            }
        }
    }
    
    /**
     * Stores all keys of this map into the resulting map. Values are not 
     * stored.
     * 
     * @return a map with all keys
     * 
     * @since 1.00
     */
    @SuppressWarnings("unchecked")
    public HashMap<K, V> keysToMap() {
        HashMap<K, V> result = new HashMap<K, V>();
        for (int i = 0; i < table.length; i++) {
            if (null != table[i]) {
                K key = (K) table[i].key;
                if (null != key) {
                    result.put(key, null);
                }
            }
        }
        return result;
    }

    /**
     * Puts all elements in <code>data</code> into this hash map.
     * 
     * @param data the elements to process
     * 
     * @since 1.00
     */
    @SuppressWarnings("unchecked")
    public void putAll(HashMap<K, V> data) {
        for (int i = 0; i < data.table.length; i++) {
            Entry<K, V> next = (Entry<K, V>) data.table[i];
            while (null != next) {
                put(next.key, next.value);
                next = next.next;
            }
        }
    }

    /**
     * Puts all elements in <code>data</code> into this hash map. Only keys
     * are inserted, values are set to <b>null</b>.
     * 
     * @param data the elements to process
     * 
     * @since 1.00
     */
    @SuppressWarnings("unchecked")
    public void putAllKeys(HashMap<K, ?> data) {
        for (int i = 0; i < data.table.length; i++) {
            Entry<K, ?> next = (Entry<K, ?>) data.table[i];
            while (null != next) {
                put(next.key, null);
                next = next.next;
            }
        }
    }
    
}
