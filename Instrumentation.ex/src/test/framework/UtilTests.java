package test.framework;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

import test.AnnotationId;

import de.uni_hildesheim.sse.codeEraser.annotations.Variability;
import de.uni_hildesheim.sse.monitoring.runtime.utils.CleaningLongHashMap;
import de.uni_hildesheim.sse.monitoring.runtime.utils.HashMap;
import de.uni_hildesheim.sse.monitoring.runtime.utils.IntHashMap;
import de.uni_hildesheim.sse.monitoring.runtime.utils.LongHashMap;
import de.uni_hildesheim.sse.monitoring.runtime.utils.LongLinkedList;
import de.uni_hildesheim.sse.monitoring.runtime.utils.LongLongHashMap;

/**
 * Tests the util classes.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
@Variability(id = AnnotationId.VAR_TESTING)
public class UtilTests {

    /**
     * Stores the random number generator.
     */
    private static java.util.Random rnd = new java.util.Random();

    /**
     * Prevents this class from being created from outside.
     * 
     * @since 1.00
     */
    private UtilTests() {
    }
    
    /**
     * Some basic testing of the utility classes.
     * 
     * @param args ignored
     * @throws IOException in case of I/O problems
     * 
     * @since 1.00
     */
    public static final void main(String[] args) throws IOException {
        System.out.println("long hash map:");
        longHashMapTest();
        System.out.println("cleaning long hash map:");
        cleaningLongHashMapTest();
        System.out.println("long long hash map:");
        longLongHashMapTest();
        System.out.println("long linked list:");
        longLinkedListTest();
        System.out.println("int hash map:");
        intHashMapTest();
        System.out.println("hash map:");
        hashMapTest();
    }
    
    /**
     * Simple tests for the {@link LongLongHashMap}.
     * 
     * @throws IOException in case of I/O problems
     * 
     * @since 1.00
     */
    private static final void longLongHashMapTest() throws IOException {
        LongLongHashMap map = new LongLongHashMap();
        final int testSize = 20000;
        java.util.List<Long> list = new java.util.ArrayList<Long>(testSize);
        for (int j = 0; j < 20; j++) {
            System.out.print(".");
            list.clear();
            for (int i = 0; i < testSize; i++) {
                long lng = rnd.nextLong();
                list.add(lng);
                map.put(lng, lng);
            }
            for (long lng : list) {
                if (!map.containsKey(lng)) {
                    System.err.println("not found " + lng);
                } else {
                    long res = map.get(lng);
                    if (res != lng) {
                        System.err.println("not matching " + lng + " " + res);
                    }
                }
            }
            
            FileOutputStream file = new FileOutputStream("tmp.txt");
            DataOutputStream out = new DataOutputStream(file);
            map.write(out);
    
            map.clear();
    
            FileInputStream fin = new FileInputStream("tmp.txt");
            DataInputStream in = new DataInputStream(fin);
            map.read(in);
    
            for (long lng : list) {
                if (!map.containsKey(lng)) {
                    System.err.println("not found " + lng);
                } else {
                    long res = map.get(lng);
                    if (res != lng) {
                        System.err.println("not matching " + lng + " " + res);
                    }
                }
            }
            map.clear();
        }
        System.out.println(" done");
    }
    
    /**
     * Simple tests for the {@link LongHashMap}.
     * 
     * @since 1.00
     */
    private static final void longHashMapTest() {
        final int testSize = 20000;
        LongHashMap<Long> map = new LongHashMap<Long>();
        java.util.List<Long> list = new java.util.ArrayList<Long>(testSize);
        java.util.HashSet<Long> validation1 = new java.util.HashSet<Long>();
        java.util.HashSet<Long> validation2 = new java.util.HashSet<Long>();
        for (int j = 0; j < 20; j++) {
            System.out.print(".");
            list.clear();
            for (int i = 0; i < testSize; i++) {
                long lng = rnd.nextLong();
                if (!validation1.contains(lng)) {
                    list.add(lng);
                    map.put(lng, lng);
                    validation1.add(lng);
                    validation2.add(lng);
                }
            }
            for (long lng : list) {
                Long res = map.get(lng);
                if (null == res) {
                    System.err.println("not found " + lng);
                } else if (res.longValue() != lng) {
                    System.err.println("not matching " + lng + " " 
                        + res.longValue());
                }
            }
            Iterator<LongHashMap.MapElement<Long>> entryIterator 
                = map.entries().iterator();
            Iterator<Long> valueIterator = map.values().iterator();
            while (valueIterator.hasNext() /*&& entryIterator.hasNext()*/) {
                long value = valueIterator.next();
                if (validation1.contains(value)) {
                    validation1.remove(value);
                } else {
                    System.err.println("Values validation: Element " + value 
                        + " not contained.");
                    System.exit(0);
                }
                LongHashMap.MapElement<Long> entry = entryIterator.next();
                value = entry.getValue();
                if (validation2.contains(value)) {
                    validation2.remove(value);
                } else {
                    System.err.println("Entries validation: Element " + value 
                        + " not contained.");
                    System.exit(0);
                }
            }
            if (!validation1.isEmpty() || !validation2.isEmpty()) {
                System.err.println("Iterator count not equal! " 
                    + validation1.size());
                System.exit(0);
            }
            if (!validation2.isEmpty()) {
                System.err.println("Iterator count not equal! " 
                    + validation2.size());
            }
            map.clear();
            validation1.clear();
            validation2.clear();
        }
        System.out.println(" done");
    }

    /**
     * Simple tests for the {@link HashMap}.
     * 
     * @since 1.00
     */
    private static final void hashMapTest() {
        HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();
        HashSet<Integer> test = new HashSet<Integer>();
        final int size = 100;
        for (int i = 0; i < size; i++) {
            map.put(i, i);
            test.add(i);
        }
        if (map.size() != size) {
            System.err.println("Map size wrong.");
            System.exit(0);
        }
        for (int i = 0; i < size; i++) {
            Integer res = map.get(i);
            if (null != res) {
                if (i != res.intValue()) {
                    System.err.println("Values validation: Element " + i 
                        + " value does not match.");
                    System.exit(0);
                }
            } else {
                System.err.println("Values validation: Element " + i 
                    + " not contained.");
                System.exit(0);
            }
        }
        Iterator<Integer> iter = map.keys().iterator();
        while (iter.hasNext()) {
            Integer key = iter.next();
            if (null == key) {
                System.err.println("Keys validation: illegal key.");
                System.exit(0);
            }
            test.remove(key);
        }
        if (test.size() != 0) {
            System.err.println("Keys validation: superfluous keys.");
            System.exit(0);
        }
    }

    
    /**
     * Simple tests for the {@link IntHashMap}.
     * 
     * @since 1.00
     */
    private static final void intHashMapTest() {
        final int testSize = 20000;
        IntHashMap<Integer> map = new IntHashMap<Integer>();
        java.util.List<Integer> list 
            = new java.util.ArrayList<Integer>(testSize);
        java.util.HashSet<Integer> validation1 
            = new java.util.HashSet<Integer>();
        java.util.HashSet<Integer> validation2 
            = new java.util.HashSet<Integer>();
        for (int j = 0; j < 20; j++) {
            System.out.print(".");
            list.clear();
            for (int i = 0; i < testSize; i++) {
                int val = rnd.nextInt();
                if (!validation1.contains(val)) {
                    list.add(val);
                    map.put(val, val);
                    validation1.add(val);
                    validation2.add(val);
                }
            }
            for (int val : list) {
                Integer res = map.get(val);
                if (null == res) {
                    System.err.println("not found " + val);
                } else if (res.longValue() != val) {
                    System.err.println("not matching " + val + " " 
                        + res.longValue());
                }
            }
            Iterator<IntHashMap.MapElement<Integer>> entryIterator 
                = map.entries().iterator();
            Iterator<Integer> valueIterator = map.values().iterator();
            while (valueIterator.hasNext() /*&& entryIterator.hasNext()*/) {
                int value = valueIterator.next();
                if (validation1.contains(value)) {
                    validation1.remove(value);
                } else {
                    System.err.println("Values validation: Element " + value 
                        + " not contained.");
                    System.exit(0);
                }
                IntHashMap.MapElement<Integer> entry = entryIterator.next();
                value = entry.getValue();
                if (validation2.contains(value)) {
                    validation2.remove(value);
                } else {
                    System.err.println("Entries validation: Element " + value 
                        + " not contained.");
                    System.exit(0);
                }
            }
            if (!validation1.isEmpty() || !validation2.isEmpty()) {
                System.err.println("Iterator count not equal! " 
                    + validation1.size());
                System.exit(0);
            }
            if (!validation2.isEmpty()) {
                System.err.println("Iterator count not equal! " 
                    + validation2.size());
            }
            map.clear();
            validation1.clear();
            validation2.clear();
        }
        System.out.println(" done");
    }
    
    /**
     * Simple tests for the {@link CleaningLongHashMap}. The basic functionality
     * should be tested in {@link #longHashMapTest()}.
     * 
     * @since 1.00
     */
    private static final void cleaningLongHashMapTest() {
        // prerequisite: long
        CleaningLongHashMap<Long> map = new CleaningLongHashMap<Long>();
        putCleaning(map, 10, 1, 1);
        removeCleaning(map, 1);
        putCleaning(map, 2, 2, 2);
        removeCleaning(map, 2);
        removeCleaning(map, 2);
        System.out.println(" done");
    }

    /**
     * Tests putting elements into a {@link CleaningLongHashMap}.
     * 
     * @param map the map to be modified as a side effect
     * @param maxCount the number of elements to be inserted
     * @param key the key to insert the <code>value</code>
     * @param value the value to be inserted
     * 
     * @since 1.00
     */
    private static final void putCleaning(CleaningLongHashMap<Long> map, 
        int maxCount, long key, long value) {
        for (int i = 1; i <= maxCount; i++) {
            CleaningLongHashMap.MapElement<Long> elt = map.getElement(key);
            Long old = map.put(key, value);
            if (null == elt) {
                if (null != old) {
                    System.err.println("Unexpected value at put (already " 
                        + "one inserted)");
                }
            } else {
                if (value != old) {
                    System.err.println("Unexpected value at put");
                }
            }
            elt = map.getElement(key);
            if (null == elt) {
                System.err.println("Element not contained");
            } else if (i != elt.getCount()) {
                System.err.println("Counter does not match (got" 
                    + elt.getCount() + " expected " + i + ")");
            }
        }
    }
    
    /**
     * Tests removing an element from a {@link CleaningLongHashMap}.
     * 
     * @param map the map to be modified as a side effect
     * @param key the key to insert the <code>value</code>
     * 
     * @since 1.00
     */
    private static final void removeCleaning(CleaningLongHashMap<Long> map, 
        long key) {
        CleaningLongHashMap.MapElement<Long> elt = map.getElement(key);
        if (null == elt) {
            System.err.println("Element not contained");
        } else {
            int curCount = elt.getCount();
            map.remove(key);
            int expected = curCount - 1;
            if (expected != elt.getCount()) {
                System.err.println("Counter does not match (got" 
                    + elt.getCount() + " expected " + expected + ")");
            } else {
                if (expected > 0) {
                    if (!map.containsKey(key)) {
                        System.err.println("key " + key + "not contained");
                    }
                } else {
                    if (map.containsKey(key)) {
                        System.err.println("key " + key + "still contained");
                    }
                }
            }
        }
    }
    
    /**
     * Simple tests for the {@link LongLinkedList}.
     * 
     * @since 1.00
     */
    private static final void longLinkedListTest() {
        LongLinkedList list = new LongLinkedList();
        LinkedList<Long> refList = new LinkedList<Long>();
        for (int j = 0; j < 20; j++) {
            System.out.print(".");
            list.clear();
            refList.clear();
            for (int i = 0; i < 20000; i++) {
                long lng = rnd.nextLong();
                list.addLast(lng);
                refList.addLast(lng);
            }
            for (int i = 0; i < 20000; i++) {
                long lVal = list.removeFirst();
                long rVal = refList.removeFirst();
                if (lVal != rVal) {
                    System.err.println("not matching at " + i);
                }
            }
            for (int i = 0; i < 20000; i++) {
                long lng = rnd.nextLong();
                list.addFirst(lng);
                refList.addFirst(lng);
            }
            for (int i = 0; i < 20000; i++) {
                long lVal = list.removeLast();
                long rVal = refList.removeLast();
                if (lVal != rVal) {
                    System.err.println("not matching at " + i);
                }
            }
            for (int i = 0; i < 20000; i++) {
                long lng = rnd.nextLong();
                list.addFirst(lng);
                refList.addFirst(lng);
            }
            for (int i = 0; i < 20000; i++) {
                long lVal = list.removeFirst();
                long rVal = refList.removeFirst();
                if (lVal != rVal) {
                    System.err.println("not matching at " + i);
                }
            }
            for (int i = 0; i < 20000; i++) {
                long lng = rnd.nextLong();
                list.addLast(lng);
                refList.addLast(lng);
            }
            for (int i = 0; i < 20000; i++) {
                long lVal = list.removeLast();
                long rVal = refList.removeLast();
                if (lVal != rVal) {
                    System.err.println("not matching at " + i);
                }
            }
        }
        System.out.println(" done");
    }

}
