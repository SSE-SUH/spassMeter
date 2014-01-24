package de.uni_hildesheim.sse.monitoring.runtime.recordingStrategies;

import de.uni_hildesheim.sse.monitoring.runtime.utils.CleaningLongHashMap;

/**
 * Stores monitored information about a running or finished thread. In 
 * particular, this class maintains a local stack of recording elements in order
 * to assign nested method calls to the correct threads.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public class ThreadData {

    /**
     * Stores the stack pool, i.e. the pool of stack copies (without 
     * duplicates).
     */
    private static final CleaningLongHashMap<RecorderElement[]> STACKPOOL 
        = new CleaningLongHashMap<RecorderElement[]>();
    
    /**
     * An internal increment in case that hash values are identical.
     */
    private static final int STACKPOOL_HASH_INCREMENT = 11;
    
    /**
     * Defines the stack increment in case that there is no more space and also
     * the initial stack size.
     */
    private static final int STACK_INCREMENT = 3;

    /**
     * Stores the CPU time ticks when using this thread started. This 
     * corresponds to {@link #totalUseTimeTicks}.
     * This information should not be confused with {@link #cpuTimeTicks} as
     * that time information needs to be collected in pairs of start/stop calls
     * and may be completed by this information when finalizing this thread.
     */
    private long startUseTimeTicks = -1;

    /**
     * Stores the total CPU usage time of this thread.
     */
    private long totalUseTimeTicks = 0;
    
    /**
     * Stores the number of assigned CPU time ticks.
     */
    private long cpuTimeTicks = 0;

    /**
     * Stores the number of assigned CPU time ticks when first entering the 
     * thread.
     */
    private long startCpuTimeTicks = -1;
    
    /**
     * Stores the nesting of reentrant calls.
     */
    private long nesting = 0;
    
    /**
     * Stores the recorder element assigned to this thread (to be used if 
     * no other recording element can be assigned).
     */
    private RecorderElement recorderElement = null;
    
    /**
     * Stores the current size of the stack.
     */
    private int stackSize = 0;
    
    /**
     * Stores the recording elements retrieved from the stack. 
     * This must not comply to the real stack but just states which recorder
     * element occurred how often on the stack so far.
     */
    private RecorderElement[] stack = new RecorderElement[STACK_INCREMENT];
    
    /**
     * Stores whether the element at a certain stack position is a duplicate.
     */
    private boolean[] stackDuplicates = new boolean[STACK_INCREMENT];
    
    /**
     * Is called to notify about the start of recording.
     * 
     * @param nanoTime the current time in nano seconds
     * @param threadTicks the CPU time ticks consumed by this thread
     * 
     * @since 1.00
     */
    void startTimeRecording(long nanoTime, long threadTicks) {
        if (0 == nesting) {
            startCpuTimeTicks = threadTicks;
        }
        nesting++;
    }
    
    /**
     * Is called to notify the end of recording.
     * 
     * @param nanoTime the current time in nano seconds
     * @param threadTicks the CPU time ticks consumed by this thread
     * 
     * @since 1.00
     */
    void stopTimeRecording(long nanoTime, long threadTicks) {
        nesting--;
        if (0 == nesting && startCpuTimeTicks >= 0) {
            cpuTimeTicks += threadTicks - startCpuTimeTicks;
            startCpuTimeTicks = -1;
        } 
    }
    
    /**
     * Starts recording for this thread (again) as the thread was created or 
     * reused. Note, that a thread may be reused and, thus, start and end 
     * multiple times.
     * 
     * @param threadTicks the CPU time when this thread ends
     * 
     * @since 1.00
     */
    public void start(long threadTicks) {
        if (startUseTimeTicks < 0) {
            startUseTimeTicks = threadTicks;
        }
    }
    
    /**
     * Ends recording for this thread as the thread ended. Note, that a thread
     * may be reused and, thus, start and end multiple times.
     * 
     * @param threadTicks the CPU time when this thread ends
     * 
     * @since 1.00
     */
    public void end(long threadTicks) {
        if (startUseTimeTicks > 0) {
            if (threadTicks > 0) {
                totalUseTimeTicks += threadTicks - startUseTimeTicks;
                startUseTimeTicks = -1;
            }
        }
    }
    
    /**
     * Returns the total use of this thread in CPU ticks.
     * 
     * @return the total CPU ticks
     * 
     * @since 1.00
     */
    public long getTotalCpu() {
        return totalUseTimeTicks;
    }

    /**
     * Returns if recording is finished.
     * 
     * @return <code>true</code> if this thread is currently being recorded, 
     *    <code>false</code> else
     * 
     * @since 1.00
     */
    public boolean isEnded() {
        return cpuTimeTicks > 0;
    }

    /**
     * Returns the number of CPU time ticks allocated to this thread.
     * 
     * @return the number of CPU time ticks
     * 
     * @since 1.00
     */
    public long getCpuTimeTicks() {
        return cpuTimeTicks;
    }

    /**
     * Pushes the recorder element (as a method call equivalent) to the call
     * stack.
     * 
     * @param elt the recorder element to be pushed
     * 
     * @since 1.00
     */
    public void push(RecorderElement elt) {
        if (stackSize >= stack.length) {
            RecorderElement[] tmpR = stack;
            stack = new RecorderElement[stackSize + STACK_INCREMENT];
            System.arraycopy(tmpR, 0, stack, 0, stackSize);
            boolean[] tmpB = stackDuplicates;
            stackDuplicates = new boolean[stackSize + STACK_INCREMENT];
            System.arraycopy(tmpB, 0, stackDuplicates, 0, stackSize);
        }
        stack[stackSize] = elt;
        int pos = stackSize - 1;
        while (pos >= 0 && stack[pos] != elt) {
            pos--;
        }
        if (pos >= 0) {
            stackDuplicates[stackSize] = true;
        }
        stackSize++;
    }

    /**
     * Pushes the given recorder element (as a method call equivalent) from the 
     * call stack.
     * 
     * @param elt the recorder element to be popped (just to be sure)
     * 
     * @since 1.00
     */
    public void pop(RecorderElement elt) {
        int lastElt = stackSize - 1;
        for (int i = lastElt; i >= 0; i--) {
            RecorderElement sElt = stack[i];
            if (elt == sElt) {
                if (i == lastElt) {
                    stackDuplicates[i] = false;
                    stack[i] = null;
                } else {
                    System.arraycopy(stack, i + 1, stack, 
                        i, stackSize - i - 1);
                    System.arraycopy(stackDuplicates, i + 1, stackDuplicates, 
                        i, stackSize - i - 1);
                }
                stackSize--;
                break;
            }
        }
    }
    
    /**
     * Returns the top of the call stack.
     * 
     * @return the top of the call stack
     * 
     * @since 1.00
     */
    public RecorderElement top() {
        RecorderElement result = null;
        if (stackSize > 0) {
            result = stack[stackSize - 1];
        }
        return result;
    }

    /**
     * Returns the index of the given element.
     * 
     * @param elt the element to search for
     * @return <code>-1</code> of not found or the index otherwise
     * 
     * @since 1.00
     */
    public int stackIndex(RecorderElement elt) {
        int pos = -1;
        for (int i = 0; i < stack.length; i++) {
            if (stack[i] == elt) {
                pos = i;
                break;
            }
        }
        return pos;
    }
    
    /**
     * Returns the size of the stack.
     * 
     * @return the size of the stack
     * 
     * @since 1.00
     */
    public int stackSize() {
        return stackSize;
    }
    
    /**
     * Returns the element at the given stack position.
     * 
     * @param index the index to return the element for
     * @return the element
     * @throws ArrayIndexOutOfBoundsException in case that the index is invalid
     * 
     * @since 1.00
     */
    public RecorderElement getStackElement(int index) {
        return stack[index];
    }

    /**
     * Returns whether the element at the given stack position is a duplicate.
     * 
     * @param index the index to return the element for
     * @return whether the element is a duplicate
     * @throws ArrayIndexOutOfBoundsException in case that the index is invalid
     * 
     * @since 1.00
     */
    public boolean isStackElementDuplicated(int index) {
        return stackDuplicates[index];
    }
    
    /**
     * Returns whether the given <code>elt</code> is on the stack.
     * 
     * @param elt the element to search for
     * @param includeTop include the top element or not
     * @return <code>true</code> if <code>elt</code> is on the stack, 
     * 
     * @since 1.00
     */
    public boolean isOnStack(RecorderElement elt, boolean includeTop) {
        int pos = stackSize - 1;
        if (!includeTop) {
            pos--;
        }
        while (pos >= 0 && stack[pos] != elt) {
            pos--;
        }
        return pos >= 0;
    }
    
    /**
     * Returns the top of the call stack and the top of the elements
     * with same <code>varId</code>.
     * 
     * @param varId the variability identification to be searched for
     * @return the top of the call stack regarding <code>varId</code>
     * 
     * @since 1.00
     */
    public RecorderElement top(int varId) {
        RecorderElement result = null;
        int pos = stackSize;
        while (pos - 1 > 0 && stack[pos - 1].getVarId() == varId) {
            pos--;
        }
        if (pos < stackSize) {
            result = stack[pos];
        }
        return result;
    }

    /**
     * Returns the stack size.
     * 
     * @return the stack size
     * 
     * @since 1.00
     */
    public int getStackSize() {
        return stackSize;
    }
    
    /**
     * Returns a textual description of this instance for debugging.
     * 
     * @return a textual instance
     */
    public String toString() {
        StringBuilder buf = new StringBuilder(super.toString());
        buf.append(" [");
        for (int i = 0; i < stackSize; i++) {
            buf.append(stack[i]);
            if (i + 1 < stackSize) {
                buf.append(", ");
            }
        }
        buf.append("]");
        return buf.toString();
    }

    /**
     * Returns the recorder element to be used if no other recorder element
     * can be assigned.
     * 
     * @return the recorder element (may be <b>null</b>)
     * 
     * @since 1.00
     */
    protected RecorderElement getRecorderElement() {
        return recorderElement;
    }

    /**
     * Changes the recorder element to be used if no other recorder element
     * can be assigned.
     * 
     * @param recorderElement new the recorder element (may be <b>null</b>)
     * 
     * @since 1.00
     */
    protected void setRecorderElement(RecorderElement recorderElement) {
        this.recorderElement = recorderElement;
    }

    /**
     * Copies the stack entries from <code>threadData</code> to this thread.
     * 
     * @param threadData the object to copy from
     * 
     * @since 1.00
     */
    public void copyStackFrom(ThreadData threadData) {
        if (threadData.stack.length > stack.length) {
            stack = new RecorderElement[threadData.stack.length];
        }
        System.arraycopy(threadData.stack, 0, stack, 0, 
            threadData.stack.length);
        if (threadData.stackDuplicates.length > stackDuplicates.length) {
            stackDuplicates = new boolean[threadData.stackDuplicates.length];
        }
        System.arraycopy(threadData.stackDuplicates, 0, stackDuplicates, 0, 
            threadData.stackDuplicates.length);
    }
            
    /**
     * Returns a the stack without duplicates. This method uses shared memory
     * in order to reduce memory usage, i.e. on same stack constellations 
     * excluding duplicates the same array may be returned. Instances returned 
     * by this method must be released by 
     * {@link #releaseStackCopy(RecorderElement[])}
     * 
     * @return the current stack (do <b>not</b> modify)
     * 
     * @since 1.00
     */
    public RecorderElement[] createStackCopy() {
        long hash = 0;
        int equalCount = 0;
        RecorderElement[] result;
        for (int i = 0; i < stackSize; i++) {
            if (!stackDuplicates[i]) {
                hash ^= stack[i].hashCode();
                equalCount++;
            }
        }
        do {
            RecorderElement[] tmp = STACKPOOL.get(hash);
            if (null == tmp) {
                result = new RecorderElement[equalCount];
                int j = 0;
                for (int i = 0; i < stackSize; i++) {
                    if (!stackDuplicates[i]) {
                        result[j] = stack[i];
                        j++;
                    }
                }
                STACKPOOL.put(hash, result);
                break;
            } else {
                boolean equals = (tmp.length == equalCount);
                int j = 0;
                for (int i = 0; equals && i < stackSize; i++) {
                    if (!stackDuplicates[i]) {
                        equals = (stack[i] == tmp[j++]);
                    }
                }
                if (!equals) {
                    hash += STACKPOOL_HASH_INCREMENT;
                } else {
                    // just increase the counter
                    result = tmp;
                    STACKPOOL.put(hash, result);
                    break;
                }
            }
        } while (true);
        return result;
    }
    
    /**
     * Releases the given stack (to be obtained from 
     * {@link #createStackCopy()}).
     * 
     * @param stack the stack to be released
     * 
     * @since 1.00
     */
    public static void releaseStack(RecorderElement[] stack) {
        long hash = 0;
        for (int i = 0; i < stack.length; i++) {
            hash ^= stack[i].hashCode();
        }
        do {
            RecorderElement[] tmp = STACKPOOL.get(hash);
            if (null == tmp) {
                // should not happen but then terminate loop, illegal use
                break;
            } else if (tmp == stack) {
                STACKPOOL.remove(hash);
                break;
            } else {
                hash += STACKPOOL_HASH_INCREMENT;
            }
        } while (true);
    }
    
    

}
