package de.uni_hildesheim.sse.monitoring.runtime.recording;

import test.AnnotationId;
import de.uni_hildesheim.sse.codeEraser.annotations.Variability;
import de.uni_hildesheim.sse.monitoring.runtime.AnnotationConstants;

/**
 * A specific stack of boolean elements. Double array allocation and no 
 * querying for enabled or disabled unallocation recording is due to performance
 * considerations (without recId SPECjvm2008 116,45 ops/min 
 * - after 115,99 ops/min on Romans laptop). Alternatives with specific object
 * creation lower performance by 3 ops/min.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.21
 */
public class RecordingStack {
    
    /**
     * Defines the stack increment in case that there is no more space and also
     * the initial stack size.
     */
    private static final int STACK_INCREMENT = 12;
    
    /**
     * Stores the accumulated memory allocations per entered recording group. 
     * Negative is (temporarily disabled), non-negative is the actual value. 
     * The number of elements in this attribute is {@link #size}. [performance]
     */
    private long[] memAlloc;

    /**
     * Stores the recording ids used for the actually entered recording groups. 
     * The number of elements in this attribute is {@link #size}. [performance]
     */
    @Variability(id = AnnotationConstants.MONITOR_MEMORY_USAGE)
    private int[] recId;

    /**
     * Stores the whether indirect group recording is enabled for the given 
     * group. The number of elements in this attribute is {@link #size}. 
     * [performance]
     */
    @Variability(id = AnnotationConstants.MONITOR_MEMORY_USAGE)
    private boolean[] indirect;
    
    /**
     * Stores the number of elements in {@link #data}.
     */
    private int size;
    
    /**
     * Stores the recording ids for unallocation monitoring. Only relevant ids
     * are stored. Thus, data in this attribute may differ from {@link #recId}.
     * The number of elements is {@link #unallocationIdSize}.
     */
    private int[] unallocationId;
    
    /**
     * Stores the number of elements in {@link #unallocationId}.
     */
    private int unallocationIdSize;
    
    /**
     * Creates a boolean stack.
     * 
     * @param unalloc whether unallocation information shall be 
     *   stored [performance]
     * 
     * @since 1.00
     */
    public RecordingStack(boolean unalloc) {
        memAlloc = new long[STACK_INCREMENT];
        if (unalloc) {
            recId = new int[STACK_INCREMENT];
            indirect = new boolean[STACK_INCREMENT];
            unallocationId = new int[STACK_INCREMENT];
        }
    }
    
    /**
     * Creates a stack from the given <code>origin</code> stack by adding all
     * elements.
     * 
     * @param origin the stack to take the contents from
     * 
     * @since 1.00
     */
    public RecordingStack(RecordingStack origin) {
        this(null == origin ? false : null != origin.recId);
        if (null != origin) {
            pushAll(origin);
        } 
    }
    
    /**
     * Returns the number of elements in the stack.
     * 
     * @return the number of elements
     * 
     * @since 1.00
     */
    public int size() {
        return size;
    }
    
    /**
     * Returns whether the stack is empty.
     * 
     * @return <code>true</code> if it is empty, <code>false</code> else
     * 
     * @since 1.00
     */
    public boolean isEmpty() {
        return 0 == size;
    }
    
    /**
     * Pushes a value on the stack.
     * 
     * @param id the recording id of the new element on the top
     * @param alloc the memory allocation value to be pushed
     * @param groupIndirect perform deep indirect monitoring (or not)
     * 
     * @since 1.00
     */
    public void push(int id, long alloc, boolean groupIndirect) {
        //assert memAlloc.length == recId.length;
        if (size + 1 > memAlloc.length) {
            int curLen = memAlloc.length;
            int newSize = curLen + STACK_INCREMENT;
            long[] longTmp = new long[newSize];
            System.arraycopy(memAlloc, 0, longTmp, 0, curLen);
            memAlloc = longTmp;

            if (null != recId) {
                int[] idTmp = new int[newSize];
                System.arraycopy(recId, 0, idTmp, 0, curLen);
                recId = idTmp;
    
                boolean[] booleanTmp = new boolean[newSize];
                System.arraycopy(indirect, 0, booleanTmp, 0, curLen);
                indirect = booleanTmp;
    
                // just tread the unallocationIds the same although typically 
                // less entries will be in this array
                idTmp = new int[newSize];
                System.arraycopy(unallocationId, 0, idTmp, 0, curLen);
                unallocationId = idTmp;
            }
        }
        memAlloc[size] = alloc;
        if (null != recId) {
            recId[size] = id;
            indirect[size] = groupIndirect;
            
            int i = size - 1;
            while (i >= 0) {
                if (id == recId[i]) {
                    break;
                }
                i--;
            }
            if (i < 0) {
                unallocationId[unallocationIdSize++] = id;
            }
        }
        
        size++;
    }
    
    /**
     * Pushes the entire stack <code>from</code>.
     * 
     * @param from the stack to be pushed
     * 
     * @since 1.00
     */
    public void pushAll(RecordingStack from) {
        //assert null == recId || memAlloc.length == recId.length;
        if (from.size > 0) {
            if (size + 1 + from.size > memAlloc.length) {
                int newLen = memAlloc.length + from.size + STACK_INCREMENT;
                long[] memAllocTmp = new long[newLen];
                if (size > 0) {
                    System.arraycopy(memAlloc, 0, memAllocTmp, 0, size - 1);
                }                
                memAlloc = memAllocTmp;
                
                if (null != recId) {
                    int[] recIdTmp = new int[newLen];
                    boolean[] indirectTmp = new boolean[newLen];
                    int[] unallocTmp = new int[newLen];
                    if (size > 0) {
                        System.arraycopy(memAlloc, 0, memAllocTmp, 0, size - 1);
                        System.arraycopy(recId, 0, recIdTmp, 0, size - 1);
                        System.arraycopy(indirect, 0, indirectTmp, 0, size - 1);
                        System.arraycopy(unallocationId, 0, unallocTmp, 0, 
                            unallocationIdSize - 1);
                    }
                    recId = recIdTmp;
                    indirect = indirectTmp;
                    unallocationId = unallocTmp;
                }
            }
            int start = 0;
            if (size > 0) {
                start = size - 1;
            }
            System.arraycopy(from.memAlloc, 0, memAlloc, start, from.size);
            if (null != recId) {
                System.arraycopy(from.recId, 0, recId, start, from.size);
                System.arraycopy(from.indirect, 0, indirect, start, from.size);
                System.arraycopy(from.unallocationId, 0, unallocationId, 
                    unallocationIdSize, from.unallocationIdSize);
                unallocationIdSize += from.unallocationIdSize;
            }
            size += from.size;
        }
    }

    /**
     * Returns the top element and changes the top value as a side effect. 
     * [performance]
     * 
     * @param alloc the new allocation value on the top
     * @return the value on the top before executing this operation
     * 
     * @since 1.00
     */
    public long top(long alloc) {
        long result;
        if (size > 0) {
            int pos = size - 1;
            result = memAlloc[pos];
            memAlloc[pos] = alloc;
        } else {
            result = -1;
        }
        return result;
    }

    /**
     * Returns the top memory allocation.
     * 
     * @return the memory allocation value on the top (may be negative 
     *   if disabled, non-negative if enabled)
     * 
     * @since 1.00
     */
    public long top() {
        long result;
        if (size > 0) {
            result = memAlloc[size - 1];
        } else {
            result = -1;
        }
        return result;
    }

    /**
     * Pops the top element from the stack.
     * 
     * @since 1.00
     */
    public void pop() {
        if (size > 0)  {
            if (unallocationIdSize > 0) {
                int pos = size - 1;
                int unallocPos = unallocationIdSize - 1;
                int id = unallocationId[unallocPos];
                // if top is not the same as unallocationId - do nothing
                // if top is the same as unallocationId - check whether there 
                //    are more on stack; if yes, do nothing; if no, pop 
                //    unallocationId
                if (id == recId[pos]) {
                    int i = pos - 1;
                    while (i >= 0) {
                        if (id == recId[i]) {
                            break;
                        }
                        i--;
                    }
                    if (i < 0) {
                        // unallocationId[pos] = null; // keep [performance]
                        unallocationIdSize--;
                    }
                    
                }
                
            }
            // keep references and values [performance]
            size--; 
        }
    }

    /**
     * Records the unallocation data for the given external <code>tag</code> 
     * of size <code>allocSize</code>. This method will take the allocation
     * mode in {@link #indirect} as well as the unallocation ids in 
     * {@link #unallocationId} into account. [native call]
     * 
     * @param tag an (external) allocation tag
     * @param allocSize the allocated memory size of <code>allocated</code>
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.MONITOR_MEMORY_USAGE)
    public void recordUnallocation(long tag, long allocSize) {
        if (size > 0 && null != recId) {
            int pos = size - 1;
            if (indirect[pos]) {
                return;
            } else {
                SystemMonitoring.MEMORY_DATA_GATHERER.recordUnallocationByTag(
                    tag, allocSize, recId[pos]);
            }
        }
    }
    
    /**
     * Records the unallocation data for the given <code>allocated</code> object
     * of size <code>allocSize</code>. This method will take the allocation
     * mode in {@link #indirect} as well as the unallocation ids in 
     * {@link #unallocationId} into account. [Java call]
     * 
     * @param allocated the allocated object
     * @param allocSize the allocated memory size of <code>allocated</code>, 
     *   if the allocation is already recorded, the <code>allocSize</code>
     *   is considered as an increment/decrement to the actual size
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.MONITOR_MEMORY_USAGE)
    public void recordUnallocation(Object allocated, long allocSize) {
        if (size > 0 && null != recId) {
            int pos = size - 1;
            if (indirect[pos]) {
                return;
            } else {
                SystemMonitoring.MEMORY_DATA_GATHERER.recordUnallocation(
                    allocated, allocSize, recId[pos]);
            }
        }
    }
    
    /**
     * Returns the size of the unallocation id stack. [Testing]
     * 
     * @return the size of the unallocation id stack
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationId.VAR_TESTING)
    public int unallocationIdSize() {
        return unallocationIdSize;
    }

    /**
     * Returns the unallocation id at the specified index. [Testing]
     * 
     * @param index the index the id should be returned for 
     *   (<code>0 &lt;= index &lt;{@link #unallocationIdSize()}</code>
     * @return the unallocation id
     * @throws IllegalArgumentException in case that {@link #recId} 
     *     is <b>null</b>
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationId.VAR_TESTING)
    public int getUnallocationId(int index) {
        if (null == recId) {
            throw new IllegalArgumentException();
        } else {
            return unallocationId[index];
        }
    }
    
    /**
     * Returns the indirect group accounting flag at the specified index. 
     * [Testing]
     * 
     * @param index the index the id should be returned for
     *   (<code>0 &lt;= index &lt;{@link #size()}</code>
     * @return the group accounting flag
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationId.VAR_TESTING)
    public boolean getIndirect(int index) {
        if (null == recId) {
            return false;
        } else {
            return indirect[index];
        }
    }

    /**
     * Returns the accounted memory allocation at the specified index. 
     * [Testing]
     * 
     * @param index the index the id should be returned for
     *   (<code>0 &lt;= index &lt;{@link #size()}</code>
     * @return the memory allocation; if negative, allocation is (temporarily 
     *   disabled), else the actual value
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationId.VAR_TESTING)
    public long getMemAlloc(int index) {
        return memAlloc[index];
    }

    /**
     * Returns the recording id at the specified index. 
     * [Testing]
     * 
     * @param index the index the id should be returned for
     *   (<code>0 &lt;= index &lt;{@link #size()}</code>
     * @return the recording id
     * @throws IllegalArgumentException in case that {@link #recId} 
     *     is <b>null</b>
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationId.VAR_TESTING)
    public int getRecId(int index) {
        if (null == recId) {
            throw new IllegalArgumentException();
        } else {
            return recId[index];
        }
    }
    
}
