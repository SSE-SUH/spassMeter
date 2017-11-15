package de.uni_hildesheim.sse.monitoring.runtime.utils;

import java.util.NoSuchElementException;

import de.uni_hildesheim.sse.monitoring.runtime.boot.ArrayList;
import de.uni_hildesheim.sse.monitoring.runtime.recordingStrategiesElements.RecordingStrategiesElement;

/**
 * Implements a linked list of long values.
 * 
 * @author Aike Sass
 * @since 1.00
 * @version 1.00
 */
public class RecordingStrategiesElementLinkedList {

    /**
     * Defines a list entry.
     * 
     * @author Aike Sass
     * @since 1.00
     * @version 1.00
     */
    private static class Entry {
        
        /**
         * Stores the entry value.
         */
        private RecordingStrategiesElement value;
        
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
        Entry(RecordingStrategiesElement value, Entry next, Entry previous) {
            this.value = value;
            this.next = next;
            this.previous = previous;
        }
    }
    
    /**
     * Stores the {@link Entry} pool.
     */
    private static ArrayList<Entry> entryPool = new ArrayList<Entry>(2000);
    
    /**
     * Defines the dummy header element where to hook into further list 
     * elements. This is not taken from the pool.
     */
    private Entry header = new Entry(null, null, null);
    
    /**
     * Stores the size of the list.
     */
    private int size = 0;
        
    /**
     * Constructs an empty list.
     */
    public RecordingStrategiesElementLinkedList() {
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
    private static final Entry getEntryFromPool(RecordingStrategiesElement value, 
        Entry next, Entry previous) {
        Entry result;
        synchronized (entryPool) {
            int size = entryPool.size();
            if (size > 0) {
                result = entryPool.remove(size - 1);
            } else {
                result = null;
            }
        }
        if (null == result) {
            result = new Entry(value, next, previous);
        } else {
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
    private static final void releaseEntry(Entry entry) {
        entry.value = null;
        entry.next = null;
        entry.previous = null;
        synchronized (entryPool) {
            entryPool.add(entry);
        }
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
    private RecordingStrategiesElement remove(Entry entry) {
        if (entry == header) {
            throw new NoSuchElementException();
        }

        RecordingStrategiesElement result = entry.value;
        entry.previous.next = entry.next;
        entry.next.previous = entry.previous;
        entry.next = null;
        entry.previous = null;
        entry.value = null;
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
    public RecordingStrategiesElement removeFirst() {
        return remove(header.next);
    }

    /**
     * Removes and returns the last element from this list.
     *
     * @return the last element from this list
     * @throws NoSuchElementException if this list is empty
     */
    public RecordingStrategiesElement removeLast() {
        return remove(header.previous);
    }

    /**
     * Inserts the specified element at the beginning of this list.
     *
     * @param value the element to add
     */
    public void addFirst(RecordingStrategiesElement value) {
        addBefore(value, header.next);
    }

    /**
     * Appends the specified element to the end of this list.
     *
     * @param value the element to add
     */
    public void addLast(RecordingStrategiesElement value) {
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
    private Entry addBefore(RecordingStrategiesElement value, Entry entry) {
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
    
    /**
     * Returns whether the list is empty.
     * 
     * @return <code>true</code> if it is empty, <code>false</code> else
     * 
     * @since 1.00
     */
    public boolean isEmpty() {
        return 0 == size;
    }

}
