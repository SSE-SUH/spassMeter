package test.classLoading.test;

import de.uni_hildesheim.sse.codeEraser.annotations.Variability;
import de.uni_hildesheim.sse.monitoring.runtime.annotations.Monitor;
import test.AnnotationId;
import test.classLoading.Plugin;

/**
 * A class to be loaded dynamically.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
@Variability(id = AnnotationId.VAR_TESTING)
@Monitor
public class Test implements Plugin {
    
    /**
     * Stores data (just for memory consumption).
     */
    private Integer data;

    /**
     * Returns stored data.
     * 
     * @return stored data
     * 
     * @since 1.00
     */
    public Integer getData() {
        return data;
    }

    /**
     * Executes some code.
     */
    public void doit() {
        for (int i = 0; i < 10000; i++) {
            data = new Integer(i);
            if (i % 50 == 0) {
                System.gc();
            }
        }

    }

}
