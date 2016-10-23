package de.uni_hildesheim.sse.monitoring.runtime.utils;

import java.util.NoSuchElementException;

/**
 * Implements a linked list.
 *
 * @param <T> the entry type
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public class LinkedList <T> {

    /**
     * Defines a list entry.
     * 
     * @param <T> the entry type
     * @author Holger Eichelberger
     * @since 1.00
     * @version 1.00
     */
    private static class Entry<T> {
        
        /**
         * Stores the entry value.
         */
        private T value;
        
        /**
         * Stores the next entry.
         */
        private Entry<T> next;
        
        /**
         * Stores the previous entry.
         */
        private Entry<T> previous;

        /**
         * Creates a new entry.
         * 
         * @param value the element value
         * @param next the next entry
         * @param previous the previous entry
         * 
         * @since 1.00
         */
        Entry(T value, Entry<T> next, Entry<T> previous) {
            this.value = value;
            this.next = next;
            this.previous = previous;
        }
    }
    
    /**
     * Defines the dummy header element where to hook into further list 
     * elements. This is not taken from the pool.
     */
    private Entry<T> header = new Entry<T>(null, null, null);
    
    /**
     * Stores the size of the list.
     */
    private int size = 0;
        
    /**
     * Constructs an empty list.
     */
    public LinkedList() {
        header.next = header; 
        header.previous = header;
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
    private T remove(Entry<T> entry) {
        if (entry == header) {
            throw new NoSuchElementException();
        }

        T result = entry.value;
        entry.previous.next = entry.next;
        entry.next.previous = entry.previous;
        entry.next = null;
        entry.previous = null;
        entry.value = null;
        size--;
        return result;
    }
    
    /**
     * Removes and returns the first element from this list.
     *
     * @return the first element from this list
     * @throws NoSuchElementException if this list is empty
     */
    public T removeFirst() {
        return remove(header.next);
    }

    /**
     * Removes and returns the last element from this list.
     *
     * @return the last element from this list
     * @throws NoSuchElementException if this list is empty
     */
    public T removeLast() {
        return remove(header.previous);
    }

    /**
     * Inserts the specified element at the beginning of this list.
     *
     * @param value the element to add
     */
    public void addFirst(T value) {
        addBefore(value, header.next);
    }

    /**
     * Appends the specified element to the end of this list.
     *
     * @param value the element to add
     */
    public void addLast(T value) {
        addBefore(value, header);
    }
    
    /**
     * Appends all elements in this list to the end of <code>list</code>.
     * 
     * @param list the target list
     * 
     * @since 1.00
     */
    public void appendAllTo(LinkedList<T> list) {
        if (size > 0) {
            Entry<T> start = header.next;
            Entry<T> end = header.previous;
            
            // unhook in this
            header.next = header;
            header.previous = header;
            
            // hook into before header in list
            start.previous = list.header.previous;
            end.next = list.header;
            start.previous.next = start;
            end.next.previous = end;
            
            // transfer size and set size to 0
            list.size += size;
            size = 0;
        }
    }
    
    /**
     * Returns a textual representation of this object.
     * 
     * @return a textual representation
     */
    public String toString() {
        StringBuilder tmp = new StringBuilder("{");
        Entry<T> e = header.next;
        while (e != header) {
            if (e != header.next) {
                tmp.append(", ");
            }
            tmp.append(e.value);
            e = e.next;
        }
        tmp.append("}");
        return tmp.toString();
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
    private Entry<T> addBefore(T value, Entry<T> entry) {
        Entry<T> newEntry = new Entry<T>(value, entry, entry.previous);
        newEntry.previous.next = newEntry;
        newEntry.next.previous = newEntry;
        size++;
        return newEntry;
    }

    /**
     * Removes all of the elements from this list.
     */
    public void clear() {
        Entry<T> e = header.next;
        while (e != header) {
            Entry<T> next = e.next;
            // release
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
