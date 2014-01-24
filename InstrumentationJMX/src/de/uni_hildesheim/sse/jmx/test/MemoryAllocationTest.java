package de.uni_hildesheim.sse.jmx.test;

import java.util.ArrayList;

import de.uni_hildesheim.sse.monitoring.runtime.annotations.EndSystem;
import de.uni_hildesheim.sse.monitoring.runtime.annotations.Monitor;
import de.uni_hildesheim.sse.monitoring.runtime.annotations.StartSystem;

/**
 * Implements a simple memory allocation test.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
@Monitor
public class MemoryAllocationTest {

    /**
     * Defines a data object. Object is not accountable without retransformation
     * or replaced rt.jar.
     */
    public class Data {
        
        /**
         * An int attribute.
         */
        private int value;

        /**
         * Constructor.
         * 
         * @param value The value.
         */
        public Data(int value) {
            this.value = value;
        }
        
        /**
         * Getter.
         * 
         * @return The value.
         */
        public int getInt() {
            return value;
        }
        
    }
    
    /**
     * For memory allocation...
     * 
     * @since 1.00
     */
    private static Object[] obj;
    
    /**
     * Creates an instance of this test.
     * 
     * @since 1.00
     */
    private MemoryAllocationTest() {
    }

    /**
     * Executes the test on this instance.
     * 
     * @since 1.00
     */
    private void execute() {
        obj = new Object[5000];
        final int listCapacity = 1000;
        System.out.print(".");
        for (int i = 0; i < obj.length; i++) {
            ArrayList<Data> list = new ArrayList<Data>(listCapacity);
            obj[i] = list;
            for (int j = 0; j < listCapacity; j++) {
                list.add(new Data(j));
            }
        }
        System.out.print("+");
        System.gc();
        obj = null;
    }
    
    /**
     * Starts the test.
     * 
     * @param args ignored
     * 
     * @since 1.00
     */
    @StartSystem
    @EndSystem
    public static void main(String[] args) {
        System.out.println(MemoryAllocationTest.class.getName());
        MemoryAllocationTest test = new MemoryAllocationTest();
        if (args.length > 0) {
            if (args[0].equals("continue")) {
                do {
                    test.execute();
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                    }
                } while(true);
            } else {
                System.out.println("invalid parameter: " + args[0]);
            }
        } else {
            test.execute();
        }
        System.out.println("------------------ done: MemoryAllocationTest");
    }

}
