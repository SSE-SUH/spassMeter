package de.uni_hildesheim.sse.monitoring.runtime.utils;

import java.util.EmptyStackException;

import de.uni_hildesheim.sse.monitoring.runtime.boot.ArrayList;

/**
 * Implements a stack of typed elements.
 * 
 * @param <E> the type of elements to store in the stack
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public class Stack <E> {

    /**
     * Stores the stack elements.
     */
    private ArrayList<E> data = new ArrayList<E>();
    
    /**
     * Creates a new stack.
     * 
     * @since 1.00
     */
    public Stack() {
    }
    
    /**
     * Pushes an item onto the top of this stack. 
     *
     * @param   item   the item to be pushed onto this stack.
     * @return  the <code>item</code> argument.
     * @see     java.util.Vector#addElement
     */
    public E push(E item) {
        data.add(item);
        return item;
    }

    /**
     * Removes the object at the top of this stack and returns that
     * object as the value of this function.
     *
     * @return     The object at the top of this stack.
     * @exception  EmptyStackException  if this stack is empty.
     */
    public E pop() {
        E   obj;
        int len = data.size();
        obj = peek();
        data.remove(len - 1);
        return obj;
    }

    /**
     * Looks at the object at the top of this stack without removing it
     * from the stack.
     *
     * @return     the object at the top of this stack.
     * @exception  EmptyStackException  if this stack is empty.
     */
    public E peek() {
        int len = data.size();

        if (len == 0) {
            throw new EmptyStackException();
        }
        return data.get(len - 1);
    }

    /**
     * Tests if this stack is empty.
     *
     * @return  <code>true</code> if and only if this stack contains
     *          no items; <code>false</code> otherwise.
     */
    public boolean empty() {
        return data.isEmpty();
    }
    
}
