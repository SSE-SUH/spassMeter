package test;

import test.testing.MonitoringGroupValue;
import test.testing.TestEnvironment;
import test.threadedTest.Data;
import de.uni_hildesheim.sse.codeEraser.annotations.Variability;
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
@Variability(id = AnnotationId.VAR_TESTING)
@Monitor
public class MemoryAllocationTest {

    /**
     * Defines the maximum allocation count.
     */
    private static final int MAX_ALLOC = 10000;
        
    /**
     * Stores the data.
     */
    private Data data;
    
    /**
     * Creates an instance of this test.
     * 
     * @since 1.00
     */
    private MemoryAllocationTest() {
        // enforce class loading
        data = new Data();
    }

    /**
     * Executes the test on this instance. Record this in an own monitoring 
     * group in order to avoid any overhead by class loading.
     * 
     * @since 1.00
     */
    @Monitor(id = "exec")
    private void execute() {
        for (int i = 0; i < MAX_ALLOC; i++) {
            data = new Data();
            if (i % 50 == 0) {
                System.gc();
            }
            if (0 == i % 100) {
                System.out.print(".");
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
        TestEnvironment.notice(MemoryAllocationTest.class.getName());
        MemoryAllocationTest test = new MemoryAllocationTest();
        if (args.length > 0) {
            if (args[0].equals("continue")) {
                for (int i = 0; i < 15; i++) {
                    test.execute();
                    System.out.print("-");                    
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                    }
                }
                System.out.println();                
            } else {
                System.out.println("invalid parameter: " + args[0]);
            }
        } else {
            test.execute();
        }
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
        }
        TestEnvironment.notice("------------------ done: MemoryAllocationTest");
    }
    
    /**
     * The automated tests using assert statements. This method is called
     * by SPASS-meter at end of monitoring upon the <code>EndSystem</code> 
     * annotation.
     * 
     * @since 1.00
     */
    public static void asserts() {
        long dataSize = TestEnvironment.getObjectSize(new Data());
        String recId = MemoryAllocationTest.class.getName();
        long allocationPerRound = dataSize * MAX_ALLOC;
        if (TestEnvironment.isIndirectTest()) {
            TestEnvironment.assertGreater(recId, 
                MonitoringGroupValue.ALLOCATED_MEMORY, 
                allocationPerRound);
        } else {
            TestEnvironment.assertGreater(recId, 
                MonitoringGroupValue.ALLOCATED_MEMORY, 
                0);
        }
        TestEnvironment.assertEquals("exec", 
            MonitoringGroupValue.ALLOCATED_MEMORY, 
            allocationPerRound);
        TestEnvironment.assertEquals(recId, MonitoringGroupValue.FILE_READ, 0);
        TestEnvironment.assertEquals(recId, MonitoringGroupValue.FILE_WRITE, 0);
        TestEnvironment.assertEquals(recId, MonitoringGroupValue.NET_READ, 0);
        TestEnvironment.assertEquals(recId, MonitoringGroupValue.NET_WRITE, 0);
        TestEnvironment.assertEquals(recId, MonitoringGroupValue.TOTAL_READ, 0);
        TestEnvironment.assertEquals(recId, 
            MonitoringGroupValue.TOTAL_WRITE, 0);
        
        TestEnvironment.success(recId);
    }

}
