package test.framework;

import java.io.IOException;

import test.AnnotationId;

import de.uni_hildesheim.sse.codeEraser.annotations.Variability;
import de.uni_hildesheim.sse.monitoring.runtime.recording.RecordingStack;

import java.util.ArrayList;
import java.util.Stack;

/**
 * Tests the recording stack.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
@Variability(id = AnnotationId.VAR_TESTING)
public class RecordingStackTest {

    /**
     * Prevents this class from being created from outside.
     * 
     * @since 1.00
     */
    private RecordingStackTest() {
    }
    
    /**
     * Executes the tests.
     * 
     * @param args ignored
     * @throws IOException in case of I/O problems
     * 
     * @since 1.00
     */
    public static final void main(String[] args) throws IOException {
        System.out.println("recording stack (basic):");
        basicTests();
        System.out.println("recording stack (contents):");
        contentsTests();
    }

    
    /**
     * Basic tests for the {@link RecordingStack}.
     * 
     * @since 1.00
     */
    private static final void basicTests() {
        int size1 = 20;
        int size2 = 10;
        int[] data = new int[size1 + size2];
        RecordingStack stack = new RecordingStack(true);
        boolean ok = true;
        for (int i = 0; ok && i < size1; i++) {
            data[i] = i;
            stack.push(i, data[i], false);
            ok &= checkTrue(stack.top() == data[i], "1: not matching at " + i);
        }
        for (int i = size1 - 1; ok && i >= 0; i--) {
            ok &= checkTrue(stack.top() == data[i], "2: not matching at " + i);
            stack.pop();
        }

        for (int i = 0; ok && i < size1; i++) {
            stack.push(i, data[i], false);
            ok &= checkTrue(stack.top() == data[i], "3: not matching at " + i);
        }
        RecordingStack stack2 = new RecordingStack(true);
        for (int i = 0; ok && i < size2; i++) {
            data[size1 + i] = i;
            stack.push(size1 + i, data[size1 + i], false);
            ok &= checkTrue(stack.top() == data[size1 + i], 
                "4: not matching at " + i);
        }
        stack2.pushAll(stack);
        for (int i = data.length - 1; ok && i > 0; i--) {
            ok &= checkTrue(stack.top() == data[i], "5: not matching at " + i);
            stack.pop();
        }
        if (ok) {
            System.out.println(" done");
        }
    }

    /**
     * Records a stack entry for testing and comparing.
     * 
     * @author eichelberger
     * @since 1.00
     * @version 1.00
     */
    private static class StackEntry {
        
        /**
         * The recording id.
         */
        private int id;
        
        /**
         * The allocation value.
         */
        private long alloc;
        
        /**
         * The indirect flag.
         */
        private boolean indirect;
    }
    
    /**
     * Pushes an entry to the <code>stack</code> under test and the 
     * <code>shadow</code> stack for comparison.
     * 
     * @param stack the stack under test
     * @param shadow the shadow stack maintained for comparison
     * @param id the recording id
     * @param alloc the memory allocation
     * @param indirect whether indirect group allocation shall be recorded
     * @return <code>true</code> if <code>stack</code> and <code>shadow</code>
     *   contain equal data
     * 
     * @since 1.00
     */
    private static boolean push(RecordingStack stack, Stack<StackEntry> shadow, 
        int id, long alloc, boolean indirect) {
        stack.push(id, alloc, indirect);
        StackEntry entry = new StackEntry();
        entry.id = id;
        entry.alloc = alloc;
        entry.indirect = indirect;
        shadow.push(entry);
        return compare(stack, shadow);
    }
    
    /**
     * Adds an allocation to the top of the <code>stack</code> under test and 
     * the <code>shadow</code> stack for comparison.
     * 
     * @param stack the stack under test
     * @param shadow the shadow stack maintained for comparison
     * @param alloc the (incremental) memory allocation
     * @return <code>true</code> if <code>stack</code> and <code>shadow</code>
     *   contain equal data
     * 
     * @since 1.00
     */
    private static boolean addToTop(RecordingStack stack, 
        Stack<StackEntry> shadow, long alloc) {
        stack.top(stack.top() + alloc);
        StackEntry top = shadow.peek();
        top.alloc += alloc;
        return compare(stack, shadow);
    }

    /**
     * Removes the entry to the top of the <code>stack</code> under test and the
     * <code>shadow</code> stack for comparison.
     * 
     * @param stack the stack under test
     * @param shadow the shadow stack maintained for comparison
     * @return <code>true</code> if <code>stack</code> and <code>shadow</code>
     *   contain equal data
     * 
     * @since 1.00
     */
    private static boolean pop(RecordingStack stack, Stack<StackEntry> shadow) {
        stack.pop();
        shadow.pop();
        return compare(stack, shadow);
    }
    
    /**
     * Checks <code>condition</code> for <code>true</code> and emits 
     * <code>msg</code> if the <code>condition</code> is not met.
     * 
     * @param condition the condition to be tested for
     * @param msg the message to be emitted in case that <code>condition</code>
     *   is <code>false</code>
     * @return condition
     * 
     * @since 1.00
     */
    private static boolean checkTrue(boolean condition, String msg) {
        if (!condition) {
            System.out.println(msg);
        }
        return condition;
    }
    
    /**
     * Checks the equality of <code>o1</code> and <code>o2</code> including 
     * <b>null</b>.
     * 
     * @param o1 the first object to check
     * @param o2 the second object to check
     * @return <code>true</code> if <code>o1</code> equals <code>o2</code>, 
     *     <code>false</code> else
     * 
     * @since 1.00
     */
    private static boolean equals(Object o1, Object o2) {
        return (null == o1 && null == o2) || (null != o1 && o1.equals(o2));
    }

    /**
     * Compares the <code>stack</code> under test and the 
     * <code>shadow</code> stack regarding their contents.
     * 
     * @param stack the stack under test
     * @param shadow the shadow stack maintained for comparison
     * @return <code>true</code> if <code>stack</code> and <code>shadow</code>
     *   contain equal data
     * 
     * @since 1.00
     */
    private static boolean compare(RecordingStack stack, 
        Stack<StackEntry> shadow) {

        ArrayList<Integer> shadowUnalloc = new ArrayList<Integer>();
        for (int i = 0; i < shadow.size(); i++) {
            int id = shadow.get(i).id;
            if (!shadowUnalloc.contains(id)) {
                shadowUnalloc.add(id);
            }
        }
        
        boolean ok = true;
        ok &= checkTrue(stack.size() == shadow.size(), 
            "stack sizes do not match");
        ok &= checkTrue(stack.unallocationIdSize() <= shadow.size(), 
            "unallocation size exceeds stack size");
        ok &= checkTrue(stack.unallocationIdSize() == shadowUnalloc.size(), 
            "unallocation size exceeds unallocation size");
        for (int i = 0; ok && i < stack.size(); i++) {
            StackEntry entry = shadow.get(i);
            ok &= checkTrue(entry.indirect == stack.getIndirect(i), 
                "indirect mismatch at '" + i + "'");
            ok &= checkTrue(equals(entry.id, stack.getRecId(i)), 
                "recid mismatch at '" + i + "'");
            ok &= checkTrue(entry.alloc == stack.getMemAlloc(i), 
                "memAlloc mismatch at '" + i + "'");
        }
        for (int i = 0; ok && i < stack.unallocationIdSize(); i++) {
            ok &= checkTrue(
                equals(stack.getUnallocationId(i), shadowUnalloc.get(i)), 
                "unallocation id mismatch '" + i + "'");
        }
        if (!ok) {
            System.out.println("Stack:");
            for (int i = 0; i < stack.size(); i++) {
                System.out.println(i + ": " + stack.getRecId(i)
                    + " " + stack.getMemAlloc(i) + " " + stack.getIndirect(i));
            }
            System.out.println("Stack unalloc:");
            for (int i = 0; i < stack.unallocationIdSize(); i++) {
                System.out.println(i + ": " + stack.getUnallocationId(i));
            }

            System.out.println("Shadow");
            for (int i = 0; i < shadow.size(); i++) {
                StackEntry entry = shadow.get(i);
                System.out.println(i + ": " + entry.id + " " + entry.alloc 
                    + " " + entry.indirect);
            }
            System.out.println("Stack unalloc:");
            for (int i = 0; i < shadowUnalloc.size(); i++) {
                System.out.println(i + ": " + shadowUnalloc.get(i));
            }
            System.out.println();
        }
        return ok;
    }
    
    /**
     * Tests the contents of the stack.
     * 
     * @since 1.00
     */
    private static final void contentsTests() {
        RecordingStack stack = new RecordingStack(true);
        Stack<StackEntry> shadow = new Stack<StackEntry>();
        boolean ok = true;
        final int myId = 1;
        final int myId1 = 2;
        ok &= push(stack, shadow, myId, 0, true);
        ok &= addToTop(stack, shadow, 10);
        ok &= addToTop(stack, shadow, 10);
        ok &= push(stack, shadow, myId1, 0, true);
        ok &= addToTop(stack, shadow, 10);
        ok &= push(stack, shadow, myId, 0, true);
        ok &= addToTop(stack, shadow, 10);
        ok &= addToTop(stack, shadow, 10);
        ok &= pop(stack, shadow);
        ok &= addToTop(stack, shadow, 10);
        ok &= pop(stack, shadow);
        ok &= addToTop(stack, shadow, 10);
        ok &= pop(stack, shadow);

        if (ok) {
            System.out.println(" done");
        }
    }

}
