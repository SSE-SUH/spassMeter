package test;

import test.testing.TestEnvironment;
import de.uni_hildesheim.sse.codeEraser.annotations.Variability;
import de.uni_hildesheim.sse.monitoring.runtime.annotations.EndSystem;
import de.uni_hildesheim.sse.monitoring.runtime.annotations.Monitor;
import de.uni_hildesheim.sse.monitoring.runtime.annotations.StartSystem;
import de.uni_hildesheim.sse.monitoring.runtime.annotations.Timer;
import de.uni_hildesheim.sse.monitoring.runtime.annotations.TimerPosition;
import de.uni_hildesheim.sse.monitoring.runtime.annotations.TimerState;

/**
 * Tests the explicit timer annotation.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
@Variability(id = AnnotationId.VAR_TESTING)
@Monitor(id = AnnotationId.ID_TIMER)
public class TimerTest {

    /**
     * Defines the recorder identification for the first timer.
     */
    public static final String ID_TIMER_START_FINISH = "startFinishTimer";

    /**
     * Defines the recorder identification for the second timer.
     */
    public static final String ID_TIMER_START_FINISH2 = "startFinish2Method";
    
    /**
     * Stores the time for waiting in order to check the timer.
     */
    private static final long WAIT_TIME = 2000;

    /**
     * Stores a fragment of {@link #WAIT_TIME} (half) in order to test
     * steps / timer states.
     */
    private static final long WAIT_TIME_2 = WAIT_TIME / 2;
    
    /**
     * Tolerance when comparing received values (here in milliseconds).
     */
    private static final long TOLERANCE = 50;
    
    /**
     * Stores the time for the first part of the test - start and stop a timer
     * in one step (method).
     */
    private static long timeStartStopOneStep = 0;
    
    /**
     * Stores the synthetic recorder id for the first part of the test - start 
     * and stop a timer in one step (method).
     */
    private static String recIdStartStopOneStep = null; 

    /**
     * Stores the time for the second part of the test - start and stop a timer
     * in two steps (methods).
     */
    private static long timeStartStopTwoSteps = 0;

    /**
     * Stores the synthetic recorder id for the second part of the test - start 
     * and stop a timer in two steps (methods).
     */
    private static String recIdStartStopTwoSteps = null; 
    
    /**
     * Stores the time for the third part of the test - restart, stop and 
     * resume a timer in two steps (methods).
     */
    private static long timeRestartStopResumeTwoSteps = 0;

    /**
     * Stores the synthetic recorder id  for the third part of the test - 
     * restart, stop and resume a timer in two steps (methods).
     */
    private static String recIdRestartStopResumeTwoSteps = null; 
    
    /**
     * Stores the time for the third part of the test - restart, stop and 
     * resume a timer in one step (method).
     */
    private static long timeRestartStopResumeOneStep = 0;

    /**
     * Stores the synthetic recorder id  for the third part of the test - 
     * restart, stop and resume a timer in one step (method).
     */
    private static String recIdRestartStopResumeOneStep = null; 
    
    /**
     * Prevents this class from being instantiated from outside.
     * 
     * @since 1.00
     */
    private TimerTest() {
    }
    
    /**
     * Some processing in one method.
     * 
     * @since 1.00
     */
    @Timer(id = ID_TIMER_START_FINISH, state = TimerState.START_FINISH)
    private static void timerInOneMethod() {
        try {
            Thread.sleep(WAIT_TIME);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Starts processing and the timer.
     * 
     * @since 1.00
     */
    @Timer(id = ID_TIMER_START_FINISH2, state = TimerState.START)
    private static void startProcessing() {
        try {
            Thread.sleep(WAIT_TIME_2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }        
    }

    /**
     * Finishes processing and the timer.
     * 
     * @since 1.00
     */
    @Timer(id = ID_TIMER_START_FINISH2, state = TimerState.FINISH)
    private static void endProcessing() {
        try {
            Thread.sleep(WAIT_TIME_2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }        
    }
    
    /**
     * Interrupts and continues processing and thus stops and resumes the timer.
     * 
     * @since 1.00
     */
    @Timer(id = ID_TIMER_START_FINISH2, state = TimerState.SUSPEND_RESUME)
    private static void interruptAndContinueProcessing() {
        try {
            Thread.sleep(WAIT_TIME_2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }        
    }

    /**
     * Interrupts processing and thus stops the timer (override default 
     * instrumentation position, would be end).
     * 
     * @since 1.00
     */
    @Timer(id = ID_TIMER_START_FINISH2, state = TimerState.SUSPEND, 
        affectAt = TimerPosition.BEGINNING)
    private static void interruptProcessing() {
        try {
            Thread.sleep(WAIT_TIME_2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }        
    }

    /**
     * Continues processing and thus resumes the timer (override default 
     * instrumentation position, would be beginning).
     * 
     * @since 1.00
     */
    @Timer(id = ID_TIMER_START_FINISH2, state = TimerState.RESUME, 
        affectAt = TimerPosition.END)
    private static void continueProcessing() {
        try {
            Thread.sleep(WAIT_TIME_2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }        
    }    
    
    /**
     * Executes the test.
     * 
     * @since 1.00
     */
    private static void execute() {
        // start and stop in one step
        long start = System.currentTimeMillis();
        timerInOneMethod();
        long end = System.currentTimeMillis();
        timeStartStopOneStep = end - start;
        recIdStartStopOneStep 
            = TestEnvironment.storeTimerData(ID_TIMER_START_FINISH);
        TestEnvironment.notice("should be approx. 2000 (vs. real time " 
            + timeStartStopOneStep + ")");
 
        // start and stop in two steps
        start = System.currentTimeMillis();
        startProcessing();
        endProcessing();
        end = System.currentTimeMillis();
        timeStartStopTwoSteps = end - start;
        recIdStartStopTwoSteps 
            = TestEnvironment.storeTimerData(ID_TIMER_START_FINISH2);
        TestEnvironment.notice("should be approx. 2000 (vs. real time " 
            + timeStartStopTwoSteps + ")");

        // restart, stop and resume in two steps
        start = System.currentTimeMillis();
        startProcessing();
        interruptProcessing();
        continueProcessing();
        endProcessing();
        end = System.currentTimeMillis();
        timeRestartStopResumeTwoSteps = end - start;
        recIdRestartStopResumeTwoSteps
            = TestEnvironment.storeTimerData(ID_TIMER_START_FINISH2);
        TestEnvironment.notice("should be approx. 2000 (vs. real time " 
            + timeRestartStopResumeTwoSteps + ")");
        
        // restart and stop and resume in one step
        start = System.currentTimeMillis();
        startProcessing();
        interruptAndContinueProcessing();
        endProcessing();
        end = System.currentTimeMillis();
        timeRestartStopResumeOneStep = end - start;
        recIdRestartStopResumeOneStep
            = TestEnvironment.storeTimerData(ID_TIMER_START_FINISH2);
        TestEnvironment.notice("should be approx. 2000 (vs. real time " 
            + timeRestartStopResumeOneStep + ")");
    }    
    
    /**
     * Starts the test.
     * 
     * @param args command line arguments (ignored)
     * 
     * @since 1.00
     */
    @StartSystem
    @EndSystem(invoke = "asserts")
    public static void main(String[] args) {
        TestEnvironment.initialize(); // explicit due to events
        TestEnvironment.notice(TimerTest.class.getName());
        if (args.length > 0) {
            if (args[0].equals("continue")) {
                for (int i = 0; i < 15; i++) {
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
        TestEnvironment.notice("------------------ done: TimerTest");
    }

    /**
     * The automated tests using assert statements. This method is called
     * by SPASS-meter at end of monitoring upon the <code>EndSystem</code> 
     * annotation.
     * 
     * @since 1.00
     */
    public static void asserts() {
        TestEnvironment.assertEqualsTimer(
            ID_TIMER_START_FINISH, WAIT_TIME, TOLERANCE);
        // just to be sure - same as ID_TIMER_START_FINISH
        TestEnvironment.assertEqualsTimer(
            recIdStartStopOneStep, WAIT_TIME, TOLERANCE);
        // cannot use real timer id as value is being overridden
        TestEnvironment.assertEqualsTimer(recIdStartStopTwoSteps, 
            WAIT_TIME, TOLERANCE);
        TestEnvironment.assertEqualsTimer(recIdRestartStopResumeTwoSteps, 
            WAIT_TIME, TOLERANCE);
        TestEnvironment.assertEqualsTimer(recIdRestartStopResumeOneStep, 
            WAIT_TIME, TOLERANCE);
        TestEnvironment.success(AnnotationId.ID_TIMER);
    }

}
