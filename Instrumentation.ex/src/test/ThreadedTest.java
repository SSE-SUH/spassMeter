package test;

import test.testing.MonitoringGroupValue;
import test.testing.TestEnvironment;
import test.threadedTest.Runnable1;
import test.threadedTest.Runnable2;
import test.threadedTest.ThreadFinishedListener;
import test.threadedTest.derived.Runnable3;

import de.uni_hildesheim.sse.codeEraser.annotations.Variability;
import de.uni_hildesheim.sse.monitoring.runtime.annotations.EndSystem;
import de.uni_hildesheim.sse.monitoring.runtime.annotations.
    ExcludeFromMonitoring;
import de.uni_hildesheim.sse.monitoring.runtime.annotations.Monitor;
import de.uni_hildesheim.sse.monitoring.runtime.annotations.StartSystem;

/**
 * Execute some interleaving threads with different 
 * resource consumption.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
@Variability(id = AnnotationId.VAR_TESTING)
@Monitor
public class ThreadedTest implements ThreadFinishedListener {
    
    /**
     * Stores the number of finished threads. Synchronized access
     * required.
     */
    private static int finished = 0;
        
    /**
     * Stores the first runnable.
     */
    private static Runnable1 r1;

    /**
     * Stores the second runnable.
     */
    private static Runnable2 r2;

    /**
     * Stores the third runnable.
     */
    private static Runnable2 r3;
    
    /**
     * Prevents this class from being instantiated from outside.
     * 
     * @since 1.00
     */
    private ThreadedTest() {
    }

    /**
     * To be called when a thread finishes.
     * 
     * @since 1.00
     */
    @ExcludeFromMonitoring
    @Override
    public void finished() {
        synchronized (ThreadedTest.class) {
            finished++;
        }
    }
    
    /**
     * Executes the test.
     * 
     * @since 1.00
     */
    private static void execute() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ThreadedTest listener = new ThreadedTest();
        r1 = new Runnable1(listener, 0 /*8 * MILLI_SEC*/);
        Thread t1 = new Thread(r1);
        r2 = new Runnable2(listener, 0/*19 * MILLI_SEC*/);
        Thread t2 = new Thread(r2);
        r3 = new Runnable3(listener, 0/*25 * MILLI_SEC*/);
        Thread t3 = new Thread(r3);
        TestEnvironment.notice("Starting runnable 1");
        t1.start();
        TestEnvironment.notice("Starting runnable 2");
        t2.start();
        TestEnvironment.notice("Starting runnable 3");
        t3.start();
        while (finished < 3 || t1.isAlive() || t2.isAlive() || t3.isAlive()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
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
        TestEnvironment.notice(ThreadedTest.class.getName());
        if (args.length > 0) {
            if (args[0].equals("continue")) {
                for (int i = 0; i < 15; i++) {
                    execute();
                }
            } else {
                System.out.println("invalid parameter: " + args[0]);
            }
        } else {
            execute();
/*            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }*/
        }
        TestEnvironment.notice("------------------ done: ThreadedTest");
    }

    /**
     * Asserts a chain of values among the monitoring groups.
     * 
     * @param value the value to consider
     * @param includeOuter include the outer group
     * 
     * @since 1.00
     */
    private static void assertChain(MonitoringGroupValue value, 
        boolean includeOuter) {
        Long val1 = TestEnvironment.getValue(Runnable1.class.getName(), 
            value);
        TestEnvironment.assertNotNull(Runnable1.class.getName(), val1);
        if (includeOuter) {
            Long val = TestEnvironment.getValue(ThreadedTest.class.getName(), 
                value);
            TestEnvironment.assertNotNull(ThreadedTest.class.getName(), val);
            TestEnvironment.assertTrue(Runnable1.class.getName(), val < val1);
        }
        Long val2 = TestEnvironment.getValue(Runnable2.class.getName(), 
            value);
        TestEnvironment.assertNotNull(Runnable2.class.getName(), val2);
        TestEnvironment.assertTrue(Runnable2.class.getName(), val1 < val2);
        Long val3 = TestEnvironment.getValue(Runnable3.class.getName(), 
            value);
        TestEnvironment.assertNotNull(Runnable3.class.getName(), val3);
        TestEnvironment.assertTrue(Runnable3.class.getName(), val2 < val3);
    }
    
    /**
     * Asserts the value 0 for all monitoring groups.
     * 
     * @param value the value to test
     * 
     * @since 1.00
     */
    private static void assertZero(MonitoringGroupValue value) {
        TestEnvironment.assertEquals(ThreadedTest.class.getName(), value, 0);
        TestEnvironment.assertEquals(Runnable1.class.getName(), value, 0);
        TestEnvironment.assertEquals(Runnable2.class.getName(), value, 0);
        TestEnvironment.assertEquals(Runnable3.class.getName(), value, 0);
    }
    
    /**
     * The automated tests using assert statements. This method is called
     * by SPASS-meter at end of monitoring upon the <code>EndSystem</code> 
     * annotation.
     * 
     * @since 1.00
     */
    public static void asserts() {
        long tolerance = 100 * 1000 * 1000; // ms
        boolean includeOuter = true;
        if (TestEnvironment.isIndirectTest()) {
            // accounted by setting
            includeOuter = false;
        }
        assertChain(MonitoringGroupValue.ALLOCATED_MEMORY, includeOuter);
        // no statement about used memory - this depends on the JVM
//        assertChain(MonitoringGroupValue.SYSTEM_TIME, false);
        
        // chain sequence is not ensured
        // due to unknown scheduling policy
        TestEnvironment.assertEquals(Runnable1.class.getName(), 
            MonitoringGroupValue.SYSTEM_TIME, 
            r1.getElapsedSystemTime(), tolerance);
        TestEnvironment.assertEquals(Runnable2.class.getName(), 
            MonitoringGroupValue.SYSTEM_TIME, 
            r2.getElapsedSystemTime(), tolerance);
        TestEnvironment.assertEquals(Runnable3.class.getName(), 
            MonitoringGroupValue.SYSTEM_TIME, 
            r3.getElapsedSystemTime(), tolerance);
        
        TestEnvironment.assertEquals(Runnable1.class.getName(), 
            MonitoringGroupValue.CPU_TIME, 
            r1.getElapsedCpuTime(), tolerance);
        TestEnvironment.assertEquals(Runnable2.class.getName(), 
            MonitoringGroupValue.CPU_TIME, 
            r2.getElapsedCpuTime(), tolerance);
        TestEnvironment.assertEquals(Runnable3.class.getName(), 
            MonitoringGroupValue.CPU_TIME, 
            r3.getElapsedCpuTime(), tolerance);
        
        assertZero(MonitoringGroupValue.FILE_READ);
        assertZero(MonitoringGroupValue.FILE_WRITE);
        assertZero(MonitoringGroupValue.NET_READ);
        assertZero(MonitoringGroupValue.NET_WRITE);
        assertZero(MonitoringGroupValue.TOTAL_READ);
        assertZero(MonitoringGroupValue.TOTAL_WRITE);
        TestEnvironment.success(ThreadedTest.class.getName());
    }

}
