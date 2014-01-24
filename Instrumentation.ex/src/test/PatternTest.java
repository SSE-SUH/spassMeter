package test;

import test.testing.MonitoringGroupValue;
import test.testing.TestEnvironment;
import de.uni_hildesheim.sse.codeEraser.annotations.Variability;

/**
 * This test currently works only with XML configuration. If this test works
 * the two inner classes should occur as individual monitoring groups.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
@Variability(id = AnnotationId.VAR_TESTING)
public class PatternTest {

    /**
     * Prevents this (utility) class from being instantiated from outside.
     * 
     * @since 1.00
     */
    private PatternTest() {
    }
    
    /**
     * A common interface to be used for pattern matching.
     * 
     * @author Holger Eichelberger
     * @since 1.00
     * @version 1.00
     */
    public interface PatternTestInterface {
        
        /**
         * Run the test code.
         * 
         * @since 1.00
         */
        public void run();
    }
    
    /**
     * Defines an inner class to be matched.
     * 
     * @author Holger Eichelberger
     * @since 1.00
     * @version 1.00
     */
    public static class Test1 implements PatternTestInterface {

        /**
         * Run the test code.
         * 
         * @since 1.00
         */
        @Override
        public void run() {
            Object[] data = new Object[10];
            for (int i = 1; i < data.length; i++) {
                data[i] = new Object();
            }
        }
    }

    /**
     * Defines an inner class to be matched.
     * 
     * @author Holger Eichelberger
     * @since 1.00
     * @version 1.00
     */
    public static class Test2 implements PatternTestInterface {

        /**
         * Run the test code.
         * 
         * @since 1.00
         */
        @Override
        public void run() {
            Object[] data = new Object[20];
            for (int i = 1; i < data.length; i++) {
                data[i] = new Object();
            }
        }
    }

    /**
     * Defines an inner class to be matched by the type but not by the 
     * name pattern.
     * 
     * @author Holger Eichelberger
     * @since 1.00
     * @version 1.00
     */
    public static class MyTest2 implements PatternTestInterface {

        /**
         * Run the test code.
         * 
         * @since 1.00
         */
        @Override
        public void run() {
            Object[] data = new Object[20];
            for (int i = 1; i < data.length; i++) {
                data[i] = new Object();
            }
        }
    }    
    
    /**
     * Executes the test.
     * 
     * @since 1.00
     */
    private static void execute() {
        TestEnvironment.notice("testing...");
        PatternTestInterface pti = new Test1();
        pti.run();
        pti = new Test2();
        pti.run();
        pti = new MyTest2();
        pti.run();
    }

    /**
     * Starts the test.
     * 
     * @param args ignored
     * 
     * @since 1.00
     */
    public static void main(String[] args) {
        TestEnvironment.notice(PatternTest.class.getName());
        if (args.length > 0) {
            if (args[0].equals("continue")) {
                for (int i = 0; i < 20; i++) {
                    execute();
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                System.out.println("invalid parameter: " + args[0]);
            }
        } else {
            execute();
        }
        TestEnvironment.notice("------------------ done: PatternTest");
    }

    /**
     * The automated tests using assert statements. This method is called
     * by SPASS-meter at end of monitoring upon the <code>EndSystem</code> 
     * annotation.
     * 
     * @since 1.00
     */
    public static void asserts() {
        String recId = PatternTest.class.getName();
        String recId1 = Test1.class.getName();
        String recId2 = Test2.class.getName();
        String recId3 = MyTest2.class.getName();

        TestEnvironment.assertNotNull(recId1, TestEnvironment.getValue(recId1, 
            MonitoringGroupValue.CPU_TIME));
        TestEnvironment.assertNotNull(recId2, TestEnvironment.getValue(recId2, 
            MonitoringGroupValue.CPU_TIME));
        if (System.getProperty("type", "").equals("type")) {
            TestEnvironment.assertNotNull(recId3, 
                TestEnvironment.getValue(recId3, 
                    MonitoringGroupValue.CPU_TIME));
        } else if (System.getProperty("type", "").equals("name")) {
            TestEnvironment.assertNull(recId3, TestEnvironment.getValue(recId3, 
                MonitoringGroupValue.CPU_TIME));
        }
        TestEnvironment.success(recId);
    }
}
