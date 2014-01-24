package test;

import de.uni_hildesheim.sse.system.GathererFactory;
import de.uni_hildesheim.sse.system.ObjectSizeEstimator;

/**
 * Implements a test for the object size estimator. This class requires native 
 * JVMTI support. The methods in this class are defined public for reuse.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public class ObjectSizeTest {

    /**
     * Prevents this class from being instantiated from outside.
     * 
     * @since 1.00
     */
    private ObjectSizeTest() {
    }
    
    /**
     * Tests a given object, i.e. the estimated size vs. the size delivered by
     * the JVM.
     * 
     * @param object the object 
     * @param description a brief description of the text
     * @return <code>true</code> if successful, <code>false</code> else
     * 
     * @since 1.00
     */
    public static boolean test(Object object, String description) {
        long mdg = GathererFactory.getMemoryDataGatherer()
            .getObjectSize(object);
        long est = ObjectSizeEstimator.getObjectSize(object);
        boolean ok = (mdg == est);
        System.out.printf("reference %d\testimated %d\t%b\t%s\n", 
            mdg, est, ok, description);
        return ok;
    }
    
    /**
     * Defines an empty class for testing.
     * 
     * @author Holger Eichelberger
     * @since 1.00
     * @version 1.00
     */
    private static class T1 {
    }

    /**
     * Defines a class with one primitive attribute for testing.
     * 
     * @author Holger Eichelberger
     * @since 1.00
     * @version 1.00
     */
    private static class T2 {
        
        // checkstyle: stop javadoc check
        @SuppressWarnings("unused")
        private int val;
        // checkstyle: resume javadoc check
    }

    /**
     * Defines a class with one primitive and an object attribute for testing.
     * 
     * @author Holger Eichelberger
     * @since 1.00
     * @version 1.00
     */
    private static class T3 {
        // checkstyle: stop javadoc check
        @SuppressWarnings("unused")
        private int val;
        @SuppressWarnings("unused")
        private T2 obj;
        // checkstyle: resume javadoc check
    }

    /**
     * Defines a class with one primitive, an object attribute and an array 
     * attribute for testing.
     * 
     * @author Holger Eichelberger
     * @since 1.00
     * @version 1.00
     */
    private static class T4 {
        // checkstyle: stop javadoc check
        @SuppressWarnings("unused")
        private int val;
        @SuppressWarnings("unused")
        private T2 obj;
        @SuppressWarnings("unused")
        private T2[] array;
        // checkstyle: resume javadoc check
    }

    /**
     * Defines a class with one primitive, an object attribute, an array 
     * attribute and an inner class attribute for testing.
     * 
     * @author Holger Eichelberger
     * @since 1.00
     * @version 1.00
     */
    private static class T5 {
        // checkstyle: stop javadoc check
        @SuppressWarnings("unused")
        private int val;
        @SuppressWarnings("unused")
        private T2 obj;
        @SuppressWarnings("unused")
        private T2[] array;
        private Inner inner = new Inner();
        // checkstyle: resume javadoc check

        /**
         * Returns the instance of the inner class for testing.
         * 
         * @return the instance of the inner class
         */
        public Inner getInner() {
            return inner;
        }

        /**
         * Defines an inner class for testing.
         * 
         * @author Holger Eichelberger
         * @since 1.00
         * @version 1.00
         */
        public class Inner {
            // checkstyle: stop javadoc check
            @SuppressWarnings("unused")
            private int val;
            // checkstyle: resume javadoc check
        }
    }
    
    /**
     * A derived class to test if this is considered properly
     * in object size estimation.
     * 
     * @author Holger Eichelberger
     * @since 1.00
     * @version 1.00
     */
    private static class T6 extends T2 {

        // checkstyle: stop javadoc check
        @SuppressWarnings("unused")
        private int val4;

        @SuppressWarnings("unused")
        private int val5;

        @SuppressWarnings("unused")
        private int val6;
        // checkstyle: resume javadoc check

    }

    /**
     * A derived class to test the size of a high-precision variable (double).
     * 
     * @author Holger Eichelberger
     * @since 1.00
     * @version 1.00
     */
    private static class T7 {

        // checkstyle: stop javadoc check

        @SuppressWarnings("unused")
        private double val1;

        // checkstyle: resume javadoc check

    }

    /**
     * Executes all tests and returns the overall result.
     * 
     * @return <code>true</code> if successful, <code>false</code> else
     * 
     * @since 1.00
     */
    public static boolean runTests() {
        boolean ok = true;
        ok &= test(new Object(), "one object");
        ok &= test(new int[0], "empty int array");
        ok &= test(new int[1], "length-1 int array");
        ok &= test(new int[10], "length-10 int array");
        ok &= test(new int[100], "length-100 int array");
        ok &= test(new Object[0], "empty object array");
        ok &= test(new Object[1], "length-1 object array");
        ok &= test(new Object[10], "length-10 object array");
        ok &= test(new Object[100], "length-100 object array");
        ok &= test(new T1(), "empty class (T1)");
        ok &= test(new T2(), "class with one int attribute (T2)");
        ok &= test(new T3(), "class with int and object attribute (T3)");
        ok &= test(new T4(), "class with int, object and array attribute (T4)");
        T5 t5 = new T5();
        ok &= test(t5, "class with int, object, array and inner class " 
            + "attribute (T5)");
        ok &= test(t5.getInner(), "inner class size (T5.inner)");
        ok &= test(new T6(), "derived class from T2 with additional int");
        ok &= test(new T7(), "class with one double attribute (T7)");
        return ok;
    }

    /**
     * Checks for JVMTI support and executes the tests.
     * 
     * @param args ignored
     * 
     * @since 1.00
     */
    public static void main(String[] args) {
        if (GathererFactory.getDataGatherer().supportsJVMTI()) {
            boolean ok = runTests();
            System.out.println("Result: " + ok);
        } else {
            System.err.println("Test cannot be executed because JVMTI is not" 
                + "supported as reference implementation");
        }
    }

}
