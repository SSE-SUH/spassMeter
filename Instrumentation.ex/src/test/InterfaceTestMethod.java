package test;

import de.uni_hildesheim.sse.monitoring.runtime.annotations.EndSystem;
import de.uni_hildesheim.sse.monitoring.runtime.annotations.StartSystem;
import test.testing.MonitoringGroupValue;
import test.testing.TestEnvironment;
import test.threadedTest.Data;

/**
 * A test class for testing annotations at interfaces.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public class InterfaceTestMethod implements IInterfaceTestMethod {

    /**
     * Defines the maximum allocation count.
     */
    private static final int MAX_ALLOC = 1000;

    /**
     * Stores the size of a data object.
     */
    private static long dataSize = -1;

    /**
     * Stores the data.
     */
    @SuppressWarnings("unused")
    private Data data;
    
    /**
     * A method which executes the test.
     * 
     * @since 1.00
     */
    public void execute() {
        for (int i = 0; i < MAX_ALLOC; i++) {
            data = new Data();
            if (i % 50 == 0) {
                System.gc();
            }
        }
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
        new InterfaceTestMethod().execute();
    }
    
    /**
     * The automated tests using assert statements. This method is called
     * by SPASS-meter at end of monitoring upon the <code>EndSystem</code> 
     * annotation.
     * 
     * @since 1.00
     */
    public static void asserts() {
        String type = System.getProperty("type", "NONE");
        if ("ALL".equals(type)) {
            // interface method is considered
            TestEnvironment.assertEquals(AnnotationId.ID_INTERFACE, 
                MonitoringGroupValue.ALLOCATED_MEMORY, dataSize * MAX_ALLOC);
        } else {
            // interface method is not considered
            TestEnvironment.assertHasNoValue(AnnotationId.ID_INTERFACE, 
                MonitoringGroupValue.ALLOCATED_MEMORY);
        }
        TestEnvironment.success(AnnotationId.ID_INTERFACE);
    }
    
}
