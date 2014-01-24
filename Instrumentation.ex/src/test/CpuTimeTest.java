package test;

import test.testing.MonitoringGroupValue;
import test.testing.TestEnvironment;
import test.threadedTest.Data;
import de.uni_hildesheim.sse.codeEraser.annotations.Variability;
import de.uni_hildesheim.sse.monitoring.runtime.annotations.EndSystem;
import de.uni_hildesheim.sse.monitoring.runtime.annotations.Monitor;
import de.uni_hildesheim.sse.monitoring.runtime.annotations.StartSystem;
import de.uni_hildesheim.sse.system.GathererFactory;
import de.uni_hildesheim.sse.system.IThreadDataGatherer;

/**
 * Implements a simple memory allocation test.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
@Variability(id = AnnotationId.VAR_TESTING)
@Monitor
public class CpuTimeTest {

    /**
     * Stores the thread data gatherer.
     */
    private static final IThreadDataGatherer THREAD_GATHERER 
        = GathererFactory.getThreadDataGatherer();
    
    /**
     * Defines the maximum allocation count.
     */
    private static final int MAX_ALLOC = 10000;
    
    /**
     * Stores the elapsed thread time in {@link #execute()}.
     */
    private static long execCpuTime; 

    /**
     * Stores the elapsed system time in {@link #execute()}.
     */
    private static long execSystemTime;

    /**
     * Stores the elapsed thread time in the entire program.
     */
    private static long cpuTime; 

    /**
     * Stores the elapsed system time in the entire program.
     */
    private static long systemTime;
    
    /**
     * Stores the data.
     */
    private Data data;
    
    /**
     * Creates an instance of this test.
     * 
     * @since 1.00
     */
    private CpuTimeTest() {
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
        // exclusion of short calls leads to smaller values in execute as the 
        // thread time tick resolution is too small
        long tmpThreadTime = THREAD_GATHERER.getCurrentCpuTime(); 
        long tmpSystemTime = System.nanoTime();
        for (int i = 0; i < MAX_ALLOC; i++) {
            data = new Data();
            if (i % 50 == 0) {
                System.gc();
            }
        }
        execCpuTime = THREAD_GATHERER.getCurrentCpuTime() - tmpThreadTime; 
        execSystemTime = System.nanoTime() - tmpSystemTime;
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
        long tmpThreadTime = THREAD_GATHERER.getCurrentCpuTime(); 
        long tmpSystemTime = System.nanoTime();

        TestEnvironment.notice(CpuTimeTest.class.getName());
        CpuTimeTest test = new CpuTimeTest();
        if (args.length > 0) {
            if (args[0].equals("continue")) {
                for (int i = 0; i < 15; i++) {
                    test.execute();
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                    }
                }
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
        TestEnvironment.notice("------------------ done: CpuTimeTest");
        cpuTime = THREAD_GATHERER.getCurrentCpuTime() - tmpThreadTime; 
        systemTime = System.nanoTime() - tmpSystemTime;
    }
    
    /**
     * The automated tests using assert statements. This method is called
     * by SPASS-meter at end of monitoring upon the <code>EndSystem</code> 
     * annotation.
     * 
     * @since 1.00
     */
    public static void asserts() {
        String recId = CpuTimeTest.class.getName();
        TestEnvironment.assertEquals("exec", MonitoringGroupValue.CPU_TIME, 
            execCpuTime, 0.01);
        TestEnvironment.assertEquals("exec", MonitoringGroupValue.SYSTEM_TIME, 
            execSystemTime, 0.01);
        long expectedTotalCpuTime;
        long expectedTotalSystemTime;
        if (TestEnvironment.isIndirectTest()) {
            expectedTotalCpuTime = cpuTime;
            expectedTotalSystemTime = systemTime;
        } else {
            expectedTotalCpuTime = cpuTime - execCpuTime;
            expectedTotalSystemTime = systemTime - execSystemTime;
        }
        TestEnvironment.assertEquals(recId, MonitoringGroupValue.CPU_TIME, 
            expectedTotalCpuTime, 0.01);
        TestEnvironment.assertEquals(recId, MonitoringGroupValue.SYSTEM_TIME, 
            expectedTotalSystemTime, 0.01);
        if (TestEnvironment.isIndirectTest()) {
            // this one is not aggregated
            TestEnvironment.assertEquals(TestEnvironment.getProgramId(), 
                MonitoringGroupValue.SYSTEM_TIME, 
                expectedTotalSystemTime, 0.01);
            // here exec is now accounted twice, includes other threads, GC
            TestEnvironment.assertGreater(TestEnvironment.getProgramId(), 
                MonitoringGroupValue.CPU_TIME, 
                expectedTotalCpuTime - execCpuTime);
        } else {
            // just the sum of both as direct accounting
            TestEnvironment.assertEquals(TestEnvironment.getProgramId(), 
                MonitoringGroupValue.SYSTEM_TIME, 
                execSystemTime + expectedTotalSystemTime, 0.01);
            // includes other threads, GC
            TestEnvironment.assertGreaterEquals(TestEnvironment.getProgramId(), 
                MonitoringGroupValue.CPU_TIME, 
                execCpuTime + expectedTotalCpuTime);
        }
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
