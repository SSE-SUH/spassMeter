package de.uni_hildesheim.sse.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Defines an array iterator.
 * 
 * @param <T> the type of the elements in the array (iterator)
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public class ArrayIterator<T> implements Iterator<T>, Iterable<T> {

    /**
     * The data array to be iterated.
     */
    private T[] data;
    
    /**
     * The position of the array in the iterator.
     */
    private int position;
    
    /**
     * Constructs a new iterator.
     * 
     * @param data the data array
     * 
     * @since 1.00
     */
    public ArrayIterator(T[] data) {
        assert null != data;
        this.data = data;
        position = 0;
    }
    
    /**
     * Returns <tt>true</tt> if the iteration has more elements. (In other
     * words, returns <tt>true</tt> if <tt>next</tt> would return an element
     * rather than throwing an exception.)
     *
     * @return <tt>true</tt> if the iterator has more elements.
     */
    public boolean hasNext() {
        return position < data.length;
    }

    /**
     * Returns the next element in the iteration.
     *
     * @return the next element in the iteration.
     * @exception NoSuchElementException iteration has no more elements.
     */
    public T next() {
        if (position < 0 || position >= data.length) {
            throw new NoSuchElementException();
        }
        return data[position++];
    }

    /**
     * Removes from the underlying collection the last element returned by the
     * iterator (optional operation). 
     *
     * @exception UnsupportedOperationException if the <tt>remove</tt>
     *        operation is not supported by this Iterator (always).
     */
    public void remove() {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns an iterable, i.e. itself.
     * 
     * @return itself
     */
    public Iterator<T> iterator() {
        return this;
    }

}
