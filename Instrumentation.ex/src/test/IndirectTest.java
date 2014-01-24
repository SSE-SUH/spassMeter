package test;

import test.testing.MonitoringGroupValue;
import test.testing.TestEnvironment;
import test.threadedTest.Data;
import de.uni_hildesheim.sse.codeEraser.annotations.Variability;
import de.uni_hildesheim.sse.monitoring.runtime.annotations.EndSystem;
import de.uni_hildesheim.sse.monitoring.runtime.annotations.Monitor;
import de.uni_hildesheim.sse.monitoring.runtime.annotations.StartSystem;

/**
 * A specific test for direct and indirect monitoring groups in the
 * same thread. 
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
@Variability(id = AnnotationId.VAR_TESTING)
@Monitor(id = AnnotationId.ID_INDIRECT)
public class IndirectTest {

    /**
     * Defines the maximum allocation count.
     */
    private static final int MAX_ALLOC = 1000;
    
    /**
     * Stores the size of a data object.
     */
    private static long dataSize = -1;
    
    /**
     * Stores the allocated test object.
     */
    private static IndirectTest indirectTest;

    /**
     * Stores the allocated sub test object.
     */
    private static SubTest subTest;
    
    /**
     * Stores the data.
     */
    private Data data;

    /**
     * Defines the indirect/overlapping resource usage.
     * 
     * @author Holger Eichelberger
     * @since 1.00
     * @version 1.00
     */
    @Monitor(id = AnnotationId.ID_INDIRECT_SUB)
    private class SubTest {

        /**
         * Stores the data.
         */
        @SuppressWarnings("unused")
        private Data data;

        /**
         * Executes resource allocation.
         * 
         * @since 1.00
         */
        public void execute() {
            // put another instance of myself onto the internal 
            // instrumenter stack
            IndirectTest.this.allocateIndirect(this);
        }

        /**
         * Called to allocate the data.
         * 
         * @since 1.00
         */
        public void allocate() {
            for (int i = 0; i < MAX_ALLOC; i++) {
                data = new Data();
                if (i % 50 == 0) {
                    System.gc();
                }
            }
        }

    }
    
    /**
     * Prevents this class from being instantiated from outside.
     * 
     * @since 1.00
     */
    private IndirectTest() {
    }

    /**
     * Allocates a sub object indirectly in order to cause this element to 
     * occur twice on the internal stack.
     * 
     * @param test the object to call the allocation method for
     * 
     * @since 1.00
     */
    private void allocateIndirect(SubTest test) {
        test.allocate();
    }
    
    /**
     * Executes resource allocation.
     * 
     * @since 1.00
     */
    public void execute() {
        for (int i = 0; i < MAX_ALLOC; i++) {
            data = new Data();
            if (-1 == dataSize) {
                dataSize = TestEnvironment.getObjectSize(data);
            }
            if (i % 50 == 0) {
                System.gc();
            }
        }
    }

    /**
     * Executes the tests - main is not time-instrumented.
     * 
     * @since 1.00
     */
    private static void test() {
        indirectTest = new IndirectTest();
        indirectTest.execute();
        subTest = indirectTest.new SubTest();
        subTest.execute();
    }
    
    /**
     * Starts the test.
     * 
     * @param args ignored
     * 
     * @since 1.00
     */
    @StartSystem
    @EndSystem(invoke = "asserts")
    public static void main(String[] args) {
        dataSize = TestEnvironment.getObjectSize(new Data());
        test();
    }
    
    /**
     * The automated tests using assert statements. This method is called
     * by SPASS-meter at end of monitoring upon the <code>EndSystem</code> 
     * annotation.
     * 
     * @since 1.00
     */
    public static void asserts() {
        long onePartDataSize = dataSize * MAX_ALLOC;
        long totalSize = 2 * onePartDataSize;
        TestEnvironment.assertEquals(AnnotationId.ID_INDIRECT_SUB, 
            MonitoringGroupValue.ALLOCATED_MEMORY, onePartDataSize);
        if (TestEnvironment.isIndirectTest()) {
            // + additionalSize does not work with XML
            TestEnvironment.assertGreater(AnnotationId.ID_INDIRECT, 
                MonitoringGroupValue.ALLOCATED_MEMORY, totalSize);
        } else {
            TestEnvironment.assertGreater(AnnotationId.ID_INDIRECT, 
                MonitoringGroupValue.ALLOCATED_MEMORY, onePartDataSize);
        }
        TestEnvironment.assertGreater(TestEnvironment.getProgramId(), 
            MonitoringGroupValue.ALLOCATED_MEMORY, totalSize);
        TestEnvironment.success(AnnotationId.ID_INDIRECT);
    }
    
}
