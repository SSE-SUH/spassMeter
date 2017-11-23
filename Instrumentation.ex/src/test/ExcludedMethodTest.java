package test;

import java.util.ArrayList;
import java.util.List;

import test.testing.MonitoringGroupValue;
import test.testing.TestEnvironment;

import de.uni_hildesheim.sse.codeEraser.annotations.Variability;
import de.uni_hildesheim.sse.monitoring.runtime.annotations.EndSystem;
import de.uni_hildesheim.sse.monitoring.runtime.annotations.
    ExcludeFromMonitoring;
import de.uni_hildesheim.sse.monitoring.runtime.annotations.Monitor;
import de.uni_hildesheim.sse.monitoring.runtime.annotations.StartSystem;

/**
 * Performs simple tests on excluded methods.<p>
 * Monitoring must be enabled via the agent JVM parameter!
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
@Variability(id = AnnotationId.VAR_TESTING)
@Monitor(id = AnnotationId.ID_EXCLUDEMETHOD)
public class ExcludedMethodTest {

    /**
     * Defines the maximum number of repeats of {@link #WAIT_TIME}.
     */
    private static final long WAIT_MAX = 10;
    
    /**
     * Defines the waiting time for the monitoring group which does nothing 
     * else.
     */
    private static final long WAIT_TIME = 200;
    
    /**
     * stores the allocated objects.
     */
    private List<Object> mem = new ArrayList<Object>();

    /**
     * Prevents this class from being instantiated from outside.
     * 
     * @since 1.00
     */
    private ExcludedMethodTest() {
    }
    
    /**
     * Allocates memory, should only occur on in program accounting.
     * 
     * @since 1.00
     */
    public void testMemory() {
        for (int i = 0; i < 20000; i++) {
            mem.add(new Object());
        }
    }

    /**
     * Should affect runtime accounting of this class.
     * 
     * @since 1.00
     */
    @ExcludeFromMonitoring()
    public void testRuntime() {
        for (int i = 0; i < WAIT_MAX; i++) {
            try {
                TestEnvironment.notice(".");
                Thread.sleep(WAIT_TIME);
            } catch (InterruptedException e) {
            }
        }
    }
    
    /**
     * Executes the test.
     * 
     * @since 1.00
     */
    private static void execute() {
        ExcludedMethodTest instance = new ExcludedMethodTest();
        instance.testMemory();
        TestEnvironment.notice("waiting");
        instance.testRuntime();
    }
    
    /**
     * Aims at reading / writing to both attributes.
     * 
     * @param args command line arguments (ignored)
     * 
     * @since 1.00
     */
    @StartSystem
    @EndSystem(invoke = "asserts")
    public static void main(String[] args) {
        TestEnvironment.notice(ExcludedMethodTest.class.getName());
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
        TestEnvironment.notice("------------------ done: ExcludedMethod");
    }

    /**
     * The automated tests using assert statements. This method is called
     * by SPASS-meter at end of monitoring upon the <code>EndSystem</code> 
     * annotation.
     * 
     * @since 1.00
     */
    public static void asserts() {
        // everything happens in this monitoring group... 
        TestEnvironment.assertGreater(AnnotationId.ID_EXCLUDEMETHOD, 
            MonitoringGroupValue.ALLOCATED_MEMORY, 0);
        TestEnvironment.assertGreater(AnnotationId.ID_EXCLUDEMETHOD, 
            MonitoringGroupValue.USED_MEMORY, 0);
        TestEnvironment.assertEquals(AnnotationId.ID_EXCLUDEMETHOD, 
            MonitoringGroupValue.NET_READ, 0);
        TestEnvironment.assertEquals(AnnotationId.ID_EXCLUDEMETHOD, 
            MonitoringGroupValue.NET_WRITE, 0);
        TestEnvironment.assertEquals(AnnotationId.ID_EXCLUDEMETHOD, 
            MonitoringGroupValue.FILE_READ, 0);
        TestEnvironment.assertEquals(AnnotationId.ID_EXCLUDEMETHOD, 
            MonitoringGroupValue.FILE_WRITE, 0);
        TestEnvironment.assertEquals(AnnotationId.ID_EXCLUDEMETHOD, 
            MonitoringGroupValue.TOTAL_READ, 0);
        TestEnvironment.assertEquals(AnnotationId.ID_EXCLUDEMETHOD, 
            MonitoringGroupValue.TOTAL_WRITE, 0);
        TestEnvironment.assertGreater(TestEnvironment.getProgramId(), 
            MonitoringGroupValue.SYSTEM_TIME, 
            WAIT_TIME * WAIT_MAX * 1000 * 1000);
        // exists and consumes time
        TestEnvironment.assertGreater(AnnotationId.ID_EXCLUDEMETHOD, 
            MonitoringGroupValue.SYSTEM_TIME, 0);
        // exists and consumes time
        TestEnvironment.assertGreater(AnnotationId.ID_EXCLUDEMETHOD, 
            MonitoringGroupValue.CPU_TIME, 0);
        TestEnvironment.assertEquals(TestEnvironment.getProgramId(), 
            MonitoringGroupValue.NET_READ, 0);
        TestEnvironment.assertEquals(TestEnvironment.getProgramId(), 
            MonitoringGroupValue.NET_WRITE, 0);
        TestEnvironment.assertEquals(TestEnvironment.getProgramId(), 
            MonitoringGroupValue.FILE_READ, 0);
        TestEnvironment.assertEquals(TestEnvironment.getProgramId(), 
            MonitoringGroupValue.FILE_WRITE, 0);
        TestEnvironment.assertEquals(TestEnvironment.getProgramId(), 
            MonitoringGroupValue.TOTAL_READ, 0);
        TestEnvironment.assertEquals(TestEnvironment.getProgramId(), 
            MonitoringGroupValue.TOTAL_WRITE, 0);

        TestEnvironment.success(AnnotationId.ID_EXCLUDEMETHOD);
    }
}
