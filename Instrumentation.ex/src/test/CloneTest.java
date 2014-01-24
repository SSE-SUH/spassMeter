package test;

import test.testing.MonitoringGroupValue;
import test.testing.TestEnvironment;
import de.uni_hildesheim.sse.codeEraser.annotations.Variability;
import de.uni_hildesheim.sse.monitoring.runtime.annotations.EndSystem;
import de.uni_hildesheim.sse.monitoring.runtime.annotations.Monitor;
import de.uni_hildesheim.sse.monitoring.runtime.annotations.StartSystem;

/**
 * Tests if clone is properly instrumented (consider Configuration!).
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
@Variability(id = AnnotationId.VAR_TESTING)
@Monitor(id = AnnotationId.ID_CLONE)
public class CloneTest implements Cloneable {

    /**
     * Stores the instance.
     */
    private static CloneTest instance;

    /**
     * Stores the clone.
     */
    private static CloneTest clone;

    /** 
     * Just something to clone.
     */
    @SuppressWarnings("unused")
    private int i = 5;

    /**
     * Clones this instance.
     * 
     * @return the cloned instance
     */
    @Monitor(id = "exec")
    public CloneTest clone() {
        try {
            return (CloneTest) super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }
    
    /**
     * Executes the test. Record this in an own monitoring 
     * group in order to avoid any overhead by class loading.
     * 
     * @since 1.00
     */
    @Monitor(id = "exec")
    private static void execute() {
        instance = new CloneTest();
        TestEnvironment.notice(instance.toString());
        clone = instance.clone();
        TestEnvironment.notice(clone.toString());
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
        TestEnvironment.notice(CloneTest.class.getName());
        if (args.length > 0) {
            if (args[0].equals("continue")) {
                for (int i = 0; i < 10; i++) {
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
        TestEnvironment.notice("------------------ done: CloneTest");
    }
    
    /**
     * The automated tests using assert statements. This method is called
     * by SPASS-meter at end of monitoring upon the <code>EndSystem</code> 
     * annotation.
     * 
     * @since 1.00
     */
    public static void asserts() {
        long expected = TestEnvironment.getObjectSize(clone) 
            + TestEnvironment.getObjectSize(instance);
        TestEnvironment.assertGreater(AnnotationId.ID_CLONE, 
            MonitoringGroupValue.ALLOCATED_MEMORY, 
            expected);
        TestEnvironment.assertEquals("exec", 
            MonitoringGroupValue.ALLOCATED_MEMORY, expected);
        
        TestEnvironment.assertEquals(AnnotationId.ID_CLONE, 
            MonitoringGroupValue.FILE_READ, 0); 
        TestEnvironment.assertEquals(AnnotationId.ID_CLONE, 
            MonitoringGroupValue.FILE_WRITE, 0); 
        TestEnvironment.assertEquals(AnnotationId.ID_CLONE, 
            MonitoringGroupValue.NET_READ, 0); 
        TestEnvironment.assertEquals(AnnotationId.ID_CLONE, 
            MonitoringGroupValue.NET_WRITE, 0); 
        
        TestEnvironment.success(AnnotationId.ID_CLONE);
    }

    
}

