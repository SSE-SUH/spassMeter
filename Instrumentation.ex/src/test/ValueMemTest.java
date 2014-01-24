package test;

import test.testing.MonitoringGroupValue;
import test.testing.TestEnvironment;
import de.uni_hildesheim.sse.codeEraser.annotations.Variability;
import de.uni_hildesheim.sse.monitoring.runtime.annotations.EndSystem;
import de.uni_hildesheim.sse.monitoring.runtime.annotations.MeasurementValue;
import de.uni_hildesheim.sse.monitoring.runtime.annotations.Monitor;
import de.uni_hildesheim.sse.monitoring.runtime.annotations.NotifyValue;
import de.uni_hildesheim.sse.monitoring.runtime.annotations.StartSystem;

/**
 * Performs some specific tests recording values, here contextualized recording
 * ids and values in external libs.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
@Variability(id = AnnotationId.VAR_TESTING)
@Monitor
public class ValueMemTest {

    /**
     * The size to notify.
     */
    private static final long ALLOCSIZE = 1025;

    /**
     * The size to notify.
     */
    private static final long UNALLOCSIZE = 100;

    /**
     * Prevents this class from being instantiated.
     * 
     * @since 1.00
     */
    private ValueMemTest() {
    }
    
    /**
     * Emulates a method which receives a newly created object for which the
     * size cannot be determined (Android objects).
     * 
     * @param object the object
     * 
     * @since 1.00
     */
    @NotifyValue(expression = "test.ValueMemTest.ALLOCSIZE", 
        value = MeasurementValue.MEM_ALLOCATED, tagExpression = "$1")
    private void allocate(Object object) {
    }

    /**
     * Emulates a method which is called before unallocation for which the
     * size cannot be determined (Android objects).
     * 
     * @param object the object
     * 
     * @since 1.00
     */
    @NotifyValue(expression = "test.ValueMemTest.UNALLOCSIZE", 
        value = MeasurementValue.MEM_UNALLOCATED, tagExpression = "$1")
    private void unallocate(Object object) {
    }
    
    /**
     * Executes the test.
     * 
     * @since 1.00
     */
    private static void execute() {
        ValueMemTest obj = new ValueMemTest();
        Object test = new Object();
        obj.allocate(test);
        obj.unallocate(test);
    }
    
    /**
     * Starts the tests.
     * 
     * @param args ignored
     * 
     * @since 1.00
     */
    @StartSystem
    @EndSystem(invoke = "asserts")
    public static void main(String[] args) {
        TestEnvironment.initialize(); // explicit due to events
        TestEnvironment.notice(ValueMemTest.class.getName());
        if (args.length > 0) {
            if (args[0].equals("continue")) {
                for (int i = 0; i < 20; i++) {
                    execute();    
                }
            } else {
                System.out.println("invalid parameter: " + args[0]);
            }
        } else {
            execute();
        }
        TestEnvironment.notice("------------------ done: ValueTest");
    }

    /**
     * The automated tests using assert statements. This method is called
     * by SPASS-meter at end of monitoring upon the <code>EndSystem</code> 
     * annotation.
     * 
     * @since 1.00
     */
    public static void asserts() {
        String recId = ValueMemTest.class.getName();
        TestEnvironment.assertGreater(recId,
            MonitoringGroupValue.ALLOCATED_MEMORY, ALLOCSIZE);
        long alloc = TestEnvironment.getValue(recId, 
            MonitoringGroupValue.ALLOCATED_MEMORY);
        long used = TestEnvironment.getValue(recId, 
            MonitoringGroupValue.USED_MEMORY);
        if (alloc != used) {
            // be tolerant in case that unallocation is not measured... (debug)
            TestEnvironment.assertEquals(recId,
                MonitoringGroupValue.USED_MEMORY, alloc - UNALLOCSIZE);
        }

        TestEnvironment.success(recId);
    }

}
