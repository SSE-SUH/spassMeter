package de.uni_hildesheim.sse.monitoring.runtime.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Implements a linked list of long values.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public class LongLinkedList {

    /**
     * Defines a list entry.
     * 
     * @author Holger Eichelberger
     * @since 1.00
     * @version 1.00
     */
    private static class Entry {
        
        /**
         * Stores the entry value.
         */
        private long value;
        
        /**
         * Stores the next entry.
         */
        private Entry next;
        
        /**
         * Stores the previous entry.
         */
        private Entry previous;

        /**
         * Creates a new entry.
         * 
         * @param value the element value
         * @param next the next entry
         * @param previous the previous entry
         * 
         * @since 1.00
         */
        Entry(long value, Entry next, Entry previous) {
            this.value = value;
            this.next = next;
            this.previous = previous;
        }
    }
    
    /**
     * Stores the {@link Entry} pool.
     */
    private static List<Entry> entryPool = new ArrayList<Entry>(5);
    
    /**
     * Defines the dummy header element where to hook into further list 
     * elements. This is not taken from the pool.
     */
    private Entry header = new Entry(0, null, null);
    
    /**
     * Stores the size of the list.
     */
    private int size = 0;
        
    /**
     * Constructs an empty list.
     */
    public LongLinkedList() {
        header.next = header; 
        header.previous = header;
    }

    /**
     * Returns an entry from the shared pool.
     * 
     * @param value the value of the returned entry
     * @param next the next entry of the returned entry
     * @param previous the previous entry of the returned entry
     * @return the entry from the shared pool.
     * 
     * @since 1.00
     */
    private static final synchronized Entry getEntryFromPool(long value, 
        Entry next, Entry previous) {
        int size = entryPool.size();
        Entry result;
        if (0 == size) {
            result = new Entry(value, next, previous);
        } else {
            result = entryPool.remove(size - 1);
            result.value = value;
            result.next = next;
            result.previous = previous;
        }
        return result;
    }

    /**
     * Releases and clears the specified {@link Entry}
     * to the shared pool.
     *
     * @param entry the {@link Entry} to be released (must not 
     *     be <b>null</b>)
     */  
    private static final synchronized void releaseEntry(Entry entry) {
        entry.value = 0;
        entry.next = null;
        entry.previous = null;
        entryPool.add(entry);
    }    

    /**
     * Removes the given entry.
     * 
     * @param entry the entry to be removed
     * @return the value of the removed entry
     * 
     * @throws NoSuchElementException if the entry cannot be removed
     * 
     * @since 1.00
     */
    private long remove(Entry entry) {
        if (entry == header) {
            throw new NoSuchElementException();
        }

        long result = entry.value;
        entry.previous.next = entry.next;
        entry.next.previous = entry.previous;
        entry.next = null;
        entry.previous = null;
        entry.value = 0;
        size--;
        releaseEntry(entry);
        return result;
    }
    
    /**
     * Removes and returns the first element from this list.
     *
     * @return the first element from this list
     * @throws NoSuchElementException if this list is empty
     */
    public long removeFirst() {
        return remove(header.next);
    }

    /**
     * Removes and returns the last element from this list.
     *
     * @return the last element from this list
     * @throws NoSuchElementException if this list is empty
     */
    public long removeLast() {
        return remove(header.previous);
    }

    /**
     * Inserts the specified element at the beginning of this list.
     *
     * @param value the element to add
     */
    public void addFirst(long value) {
        addBefore(value, header.next);
    }

    /**
     * Appends the specified element to the end of this list.
     *
     * @param value the element to add
     */
    public void addLast(long value) {
        addBefore(value, header);
    }

    /**
     * Adds a new entry before <code>entry</code>.
     * 
     * @param value the entry value
     * @param entry the entry to insert into before
     * @return the new entry
     * 
     * @since 1.00
     */
    private Entry addBefore(long value, Entry entry) {
        Entry newEntry = getEntryFromPool(value, entry, entry.previous);
        newEntry.previous.next = newEntry;
        newEntry.next.previous = newEntry;
        size++;
        return newEntry;
    }

    /**
     * Removes all of the elements from this list.
     */
    public void clear() {
        Entry e = header.next;
        while (e != header) {
            Entry next = e.next;
            releaseEntry(e);
            e = next;
        }
        header.next = header;
        header.previous = header;
        size = 0;
    }
    
    /**
     * Returns the number of elements in this list.
     *
     * @return the number of elements in this list
     */
    public int size() {
        return size;
    }

}
