package test;

import test.testing.TestEnvironment;
import test.threadedTest.Data;
import de.uni_hildesheim.sse.codeEraser.annotations.Variability;
import de.uni_hildesheim.sse.monitoring.runtime.annotations.Monitor;

/**
 * Implements a simple memory allocation test. Aims at breaking down the results to instances via
 * the identity hashcode.
 * 
 * @author Holger Eichelberger
 * @since 1.20
 * @version 1.20
 */
@Variability(id = AnnotationId.VAR_TESTING)
@Monitor
public class InstanceIdentifierTest2 {
    
    /**
     * Defines the maximum allocation count.
     */
    private static final int MAX_ALLOC = 10000;

    /**
     * A common interface to be used for pattern matching.
     * 
     * @author Holger Eichelberger
     * @since 1.20
     * @version 1.20
     */
    public interface InstanceIdentifierTestInterface {
        
        /**
         * Run the test code.
         * 
         * @since 1.20
         */
        public void run();
    }
    
    /**
     * Implements the test.
     * 
     * @author Holger Eichelberger
     * @since 1.20
     * @version 1.20
     */
    public static class Test1 implements InstanceIdentifierTestInterface {

        /**
         * Stores the data.
         */
        private Data data;

        /**
         * Creates an instance of this test.
         * 
         * @since 1.00
         */
        private Test1() {
            // enforce class loading
            data = new Data();
        }
        
        @Override
        public void run() {
            for (int i = 0; i < MAX_ALLOC; i++) {
                data = new Data();
                if (i % 50 == 0) {
                    System.gc();
                }
            }
        }
        
        /**
         * Returns the data.
         * 
         * @return the data
         */
        public Data getData() {
            return data;
        }
        
    }

    /**
     * Prevents external instantiation.
     * 
     * @since 1.20
     */
    private InstanceIdentifierTest2() {
    }

    /**
     * Starts the test.
     * 
     * @param args ignored
     * 
     * @since 1.20
     */
    public static void main(String[] args) {
        TestEnvironment.notice(InstanceIdentifierTest2.class.getName());
        InstanceIdentifierTestInterface test = new Test1();
        if (args.length > 0) {
            if (args[0].equals("continue")) {
                for (int i = 0; i < 15; i++) {
                    test.run();
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                    }
                }
            } else {
                System.out.println("invalid parameter: " + args[0]);
            }
        } else {
            test.run();
        }
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
        }
        TestEnvironment.notice("------------------ done: InstanceIdentifierTest2");
    }
    
    /**
     * The automated tests using assert statements. This method is called
     * by SPASS-meter at end of monitoring upon the <code>EndSystem</code> 
     * annotation.
     * 
     * @since 1.20
     */
    public static void asserts() {
        String recId = Test1.class.getName();
        
        String[] ids = TestEnvironment.getInstanceIdentifiers(recId);
        TestEnvironment.assertNotNull(recId, ids);
        TestEnvironment.assertEquals(recId, ids.length, 1);

        TestEnvironment.success(recId);
    }

}
