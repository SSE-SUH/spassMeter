package test;

import test.testing.TestEnvironment;
import de.uni_hildesheim.sse.codeEraser.annotations.Variability;
import de.uni_hildesheim.sse.monitoring.runtime.annotations.EndSystem;
import de.uni_hildesheim.sse.monitoring.runtime.annotations.StartSystem;

/**
 * Test code insertion before <code>System.exit</code>.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
@Variability(id = AnnotationId.VAR_TESTING)
public class SystemExitTest {
    
    /**
     * Prevents this class from being instantiated from outside.
     * 
     * @since 1.00
     */
    private SystemExitTest() {
    }

    /**
     * Start the test and requires monitoring to be stopped before system exit.
     * 
     * @param args ignored
     * 
     * @since 1.00
     */
    @StartSystem
    @EndSystem(invoke = "asserts")
    public static void main(String[] args) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }        
        TestEnvironment.notice("------------------ done: ShutdownTest");
        // Code should run here, i.e. there should be an output file
        System.exit(0);
    }
    
    /**
     * The automated tests using assert statements. This method is called
     * by SPASS-meter at end of monitoring upon the <code>EndSystem</code> 
     * annotation.
     * 
     * @since 1.00
     */
    public static void asserts() {
        // this should just be called from the instrumenter
        TestEnvironment.success(SystemExitTest.class.getName());
    }

}
