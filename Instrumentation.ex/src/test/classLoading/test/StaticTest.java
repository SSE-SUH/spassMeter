package test.classLoading.test;

import test.AnnotationId;
import de.uni_hildesheim.sse.codeEraser.annotations.Variability;
import de.uni_hildesheim.sse.monitoring.runtime.annotations.Monitor;

/**
 * Test if static initializers are considered by instrumentation.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
@Variability(id = AnnotationId.VAR_TESTING)
@Monitor
public class StaticTest {

    /**
     * Stores the data... just for memorys allocation.
     */
    @SuppressWarnings("unused")
    private static Integer data;
    
    /**
     * Static class initializer to be tested.
     */
    static {
        System.out.println("STATIC CALLED");
        for (int i = 0; i < 10000; i++) {
            data = new Integer(i);
            if (i % 50 == 0) {
                System.gc();
            }
        }
    }
    
}
