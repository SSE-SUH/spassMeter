package test;

import test.testing.TestEnvironment;
import de.uni_hildesheim.sse.codeEraser.annotations.Variability;
import de.uni_hildesheim.sse.monitoring.runtime.annotations.StartSystem;

/**
 * Test the automatically generated shutdown hook.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
@Variability(id = AnnotationId.VAR_TESTING)
public class ShutdownTest {
    
    /**
     * Prevents this class from being instantiated from outside.
     * 
     * @since 1.00
     */
    private ShutdownTest() {
    }

    /**
     * Start the test, monitoring but do not explicitly stop it. ;)
     * 
     * @param args ignored
     * 
     * @since 1.00
     */
    @StartSystem(shutdownHook = true, invoke = "asserts")
    public static void main(String[] args) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }        
        TestEnvironment.notice("------------------ done: ShutdownTest");
        // needs some kind of end/dispose
        System.exit(0);
    }
    
    /**
     * The automated tests using assert statements. This method is called
     * by SPASS-meter at end of monitoring upon the <code>StartSystem</code> 
     * annotation.
     * 
     * @since 1.00
     */
    public static void asserts() {
        // this should just be called from the instrumenter
        TestEnvironment.success(ShutdownTest.class.getName());
    }

}
