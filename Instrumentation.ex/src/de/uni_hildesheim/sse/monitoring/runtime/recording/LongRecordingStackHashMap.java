package de.uni_hildesheim.sse.monitoring.runtime.recording;


/**
 * Reimplements a specific version of <code>java.util.HashMap</code> 
 * for matching thread ids (long) to {@link RecordingStack recording stacks}.
 * 
 * @author Holger Eichelberger (based on SUN/ORACLE)
 * @since 1.00
 * @version 1.00
 */
public class LongRecordingStackHashMap {

    /**
     * Implements a bucket entry.
     * 
     * @author from SUN/ORACLE
     * @since 1.00
     * @version 1.00
     */
    private static class Entry {
        
        /**
         * Stores the key.
         */
        private final long key;
        
        /**
         * Stores the value.
         */
        private RecordingStack value;
        
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
         * @param key the key
         * @param value the value
         * @param next the next entry
         */
        Entry(int hash, long key, RecordingStack value, Entry next) {
            this.value = value;
            this.next = next;
            this.key = key;
            this.hash = hash;
        }

        /**
         * Returns a textual representation of this instance.
         * 
         * @return the textual representation
         */
        public final String toString() {
            return key + "=" + value;
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
     * Constructs an empty <tt>HashMap</tt> with the specified initial
     * capacity and load factor.
     *
     * @param  initialCapacity the initial capacity
     * @param  loadFactor      the load factor
     * @throws IllegalArgumentException if the initial capacity is negative
     *         or the load factor is nonpositive
     */
    public LongRecordingStackHashMap(int initialCapacity, float loadFactor) {
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
    public LongRecordingStackHashMap(int initialCapacity) {
        this(initialCapacity, DEFAULT_LOAD_FACTOR);
    }

    /**
     * Constructs an empty <tt>HashMap</tt> with the default initial capacity
     * (16) and the default load factor (0.75).
     */
    public LongRecordingStackHashMap() {
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
    public boolean containsKey(long key) {
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
    static int hash(long key) {
        // This function ensures that hashCodes that differ only by
        // constant multiples at each bit position have a bounded
        // number of collisions (approximately 8 at default load factor).
        int k = (int) (key ^ (key >>> 32));
        k ^= (k >>> 20) ^ (k >>> 12);
        return k ^ (k >>> 7) ^ (k >>> 4);
    }
    
    /**
     * Returns the entry associated with the specified key in the
     * HashMap.  Returns null if the HashMap contains no mapping
     * for the key.
     * @param key the key to return the stored object for
     * @return the stored object or <b>null</b>
     */
    final Entry getEntry(long key) {
        int hash = hash(key);
        for (Entry e = table[indexFor(hash, table.length)];
             e != null;
             e = e.next) {
            if (e.hash == hash && e.key == key) {
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
    public RecordingStack put(long key, RecordingStack value) {
        int hash = hash(key);
        int i = indexFor(hash, table.length);
        for (Entry e = table[i]; e != null; e = e.next) {
            if (e.hash == hash && e.key == key) {
                RecordingStack oldValue = e.value;
                e.value = value;
                return oldValue;
            }
        }

        addEntry(hash, key, value, i);
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
    void addEntry(int hash, long key, RecordingStack value, int bucketIndex) {
        Entry e = table[bucketIndex];
        table[bucketIndex] = new Entry(hash, key, value, e);
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
     * Returns the value to which the specified key is mapped,
     * or {@code null} if this map contains no mapping for the key.
     *
     * @param key the key to search for
     * @return the stored value, <b>null</b> if not found
     *
     * @see #put(long, RecordingStack)
     */
    public RecordingStack get(long key) {
        int hash = hash(key);
        for (Entry e = table[indexFor(hash, table.length)];
             e != null;
             e = e.next) {
            if (e.hash == hash && e.key == key) {
                return e.value;
            }
        }
        return null;
    }
    
    /**
     * Removes the mapping of <code>key</code> and returns its value.
     * 
     * @param key the key to remove the entry
     * 
     * @since 1.00
     */
    public void remove(long key) {
        int hash = hash(key);
        int i = indexFor(hash, table.length);
        Entry prev = table[i];
        Entry e = prev;

        while (e != null) {
            Entry next = e.next;
            if (e.hash == hash && e.key == key) {
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
    
    /**
     * Clones the stack identified with <code>from</code>
     * to the stack identified with <code>to</code>.
     * 
     * @param from the source stack
     * @param to the target stack
     * 
     * @since 1.00
     */
    public void clone(long from, long to) {
        RecordingStack destination = get(to);
        if (null == destination) {
            RecordingStack origin = get(from);
            destination = new RecordingStack(origin);
            put(to, destination);
        }
    }
    
}
