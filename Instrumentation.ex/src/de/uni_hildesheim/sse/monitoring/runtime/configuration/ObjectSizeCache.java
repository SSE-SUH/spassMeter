package de.uni_hildesheim.sse.monitoring.runtime.configuration;

import de.uni_hildesheim.sse.monitoring.runtime.recording.ObjectSizeProvider;

/**
 * Implements an object size cache, a simplified and typed version the 
 * hashmap <code>java.util.HashMap</code>.
 * 
 * @author from SUN/ORACLE
 * @since 1.00
 * @version 1.00
 */
public class ObjectSizeCache {

    /**
     * Implements a bucket entry.
     * 
     * @author from SUN/ORACLE
     * @since 1.00
     * @version 1.00
     */
    private static class Entry {
        
        /**
         * Stores the key (initially the class name, later the Class itself).
         */
        private final Object key;
        
        /**
         * Stores the associated size.
         */
        private long size;
        
        /**
         * Stores the next entry.
         */
        private Entry next;
        
        /**
         * Stores the hash value.
         */
        private final int hash;

        /**
         * Creates new entry.
         * 
         * @param hash the hash value
         * @param key the class name or the Class
         * @param size the memory size of the class
         * @param next the next entry
         */
        Entry(int hash, Object key, long size, Entry next) {
            this.size = size;
            this.next = next;
            this.key = key;
            this.hash = hash;
        }

    }

    /**
     * Stores the singleton instance of this class.
     */
    public static final ObjectSizeCache INSTANCE = new ObjectSizeCache();
    
    /**
     * Stores the object size provider.
     */
    private static IObjectSizeProvider objectSizeProvider 
        = ObjectSizeProvider.getInstance();
    
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
    private transient Entry[] table;

    /**
     * The number of key-value mappings contained in this map.
     */
    private transient int size;
    
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
     * Constructs an empty <tt>HashMap</tt> with the default initial capacity
     * (16) and the default load factor (0.75).
     */
    private ObjectSizeCache() {
        this.loadFactor = DEFAULT_LOAD_FACTOR;
        threshold = (int) (DEFAULT_INITIAL_CAPACITY * DEFAULT_LOAD_FACTOR);
        table = new Entry[DEFAULT_INITIAL_CAPACITY];
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
     * @param key the name of the class or the Class to associate the size to
     * @param size the memory size to be associated with 
     *   the <code>className</code>
     */
    public void setSize(Object key, long size) {
        int hash = hash(key.hashCode());
        int i = indexFor(hash, table.length);
        for (Entry e = table[i]; e != null; e = e.next) {
            Object k;
            if (e.hash == hash 
                && ((k = e.key) == key || key.equals(k))) {
                e.size = size;
                return;
            }
        }
        addEntry(hash, key, size, i);
    }

    /**
     * Adds a new entry with the specified key, value and hash code to
     * the specified bucket.  It is the responsibility of this
     * method to resize the table if appropriate.
     *
     * Subclass overrides this to alter the behavior of put method.
     * @param hash the hash code
     * @param key the class name of the class or the Class 
     * @param size the memory size of the class with <code>className</code>
     * @param bucketIndex the target bucket index
     */
    void addEntry(int hash, Object key, long size, int bucketIndex) {
        Entry e = table[bucketIndex];
        table[bucketIndex] = new Entry(hash, key, size, e);
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
    void transfer(Entry[] newTable) {
        Entry[] src = table;
        int newCapacity = newTable.length;
        for (int j = 0; j < src.length; j++) {
            Entry e = src[j];
            if (e != null) {
                src[j] = null;
                do {
                    Entry next = e.next;
                    int i = indexFor(e.hash, newCapacity);
                    e.next = newTable[i];
                    newTable[i] = e;
                    e = next;
                } while (e != null);
            }
        }
    }
    
    /**
     * Returns the size of the class specified by <code>className</code>. 
     * Please note that this method is intended to be used while 
     * instrumentation for improving the runtime performance of the memory size
     * determination. 
     * 
     * @param className the name of the class
     * @return the memory size of the class denoted by <code>className</code>, 
     *   <code>0</code> if the class is not known to this cache.
     * 
     * @since 1.00
     */
    public long getClassSize(String className) {
        int hash = hash(className.hashCode());
        Object k;
        for (Entry e = table[indexFor(hash, table.length)]; e != null; 
            e = e.next) {
            if (e.hash == hash 
                && ((k = e.key) == className || className.equals(k))) {
                return e.size;
            }
        }
        return 0;
    }
    
    /**
     * Returns the memory size of <code>object</code>. In case that the class
     * of <code>object</code> is already known to this cache, the stored size
     * is returned. Else the {@link #objectSizeProvider} is used to determine 
     * the size.
     *
     * @param object the object to determine the size for
     * @param remember if <code>true</code> remember an unknown size, 
     *   <code>false</code> if an unknown size may be ignroed
     * @return the memory size of <code>object</code>
     */
    public long getSize(Object object, boolean remember) {
        Class<?> cls = object.getClass();
        boolean isArray = cls.isArray();
        /*if (isArray) {
            // not faster than directly querying the objectSizeProvider
            // do not query cache as in case of references it is just 
            // the reference
            int length = Array.getLength(object);
            long componentSize;
            if (length > 0) {
                componentSize = ObjectSizeEstimator.getTypeSize(
                    cls.getComponentType());
            } else {
                componentSize = 0;
            }
            return ObjectSizeEstimator.getArraySize(length, componentSize);
        } else*/ 
        if (!isArray) {
            int hash = hash(cls.hashCode());
            Object k;
            for (Entry e = table[indexFor(hash, table.length)]; e != null;
                 e = e.next) {
                if (e.hash == hash && ((k = e.key) == cls 
                    || cls.equals(k))) {
                    return e.size;
                }
            }

            String className = cls.getName();
            hash = hash(className.hashCode());
            for (Entry e = table[indexFor(hash, table.length)]; e != null;
                 e = e.next) {
                if (e.hash == hash && ((k = e.key) == className 
                    || className.equals(k))) {
                    long result = e.size;
                    remove(className);
                    setSize(cls, result);
                    return result;
                }
            }
        } 
        long value = objectSizeProvider.getObjectSize(object);
        if (!isArray && remember) {
            setSize(cls, value);
        }
        return value;
    }
    
    /**
     * Removes the mapping of <code>key</code> to its memory size.
     * 
     * @param key the name of the class or the Class the mapping to the 
     *     memory size shall be removed
     * 
     * @since 1.00
     */
    public void remove(Object key) {
        int hash = hash(key.hashCode());
        int i = indexFor(hash, table.length);
        Entry prev = table[i];
        Entry e = prev;

        while (e != null) {
            Entry next = e.next;
            Object k;
            if (e.hash == hash && ((k = e.key) == key || key.equals(k))) {
                size--;
                if (prev == e) {
                    table[i] = next;
                } else {
                    prev.next = next;
                }
                return;
            }
            prev = e;
            e = next;
        }
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
     * Returns <tt>true</tt> if this map contains no key-value mappings.
     *
     * @return <tt>true</tt> if this map contains no key-value mappings
     */
    public boolean isEmpty() {
        return size == 0;
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
    }
    
    // TODO entries

}
