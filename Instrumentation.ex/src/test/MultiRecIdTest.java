package test;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import de.uni_hildesheim.sse.monitoring.runtime.annotations.EndSystem;
import de.uni_hildesheim.sse.monitoring.runtime.annotations.Monitor;
import de.uni_hildesheim.sse.monitoring.runtime.annotations.StartSystem;
import test.testing.MonitoringGroupValue;
import test.testing.TestEnvironment;
import test.threadedTest.Data;

/**
 * Tests multiple recording ids in one recording group.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public class MultiRecIdTest {

    /**
     * Defines the maximum allocation count for part 1.
     */
    private static final int MAX_ALLOC1 = 10;

    /**
     * Defines the maximum allocation count for part 2.
     */
    private static final int MAX_ALLOC2 = 20;

    /**
     * Stores the size of a data object.
     */
    private static long dataSize = -1;
    
    /**
     * The number of bytes read in summary.
     */
    private static long fileRead = -1;

    /**
     * Prevents this class from being instantiated from outside.
     * 
     * @since 1.00
     */
    protected MultiRecIdTest() {
    }
    
    /**
     * Individual part to be recorded for id1.
     * 
     * @author Holger Eichelberger
     * @since 1.00
     * @version 1.00
     */
    @Monitor(id = AnnotationId.ID_MULTI_1)
    private static class Part1 {
        
        /**
         * Stores the data.
         */
        @SuppressWarnings("unused")
        private Data data;
        
        /**
         * Executes this part.
         * 
         * @since 1.00
         */
        public void execute() {
            for (int i = 0; i < MAX_ALLOC1; i++) {
                data = new Data();
                if (i % 50 == 0) {
                    System.gc();
                }
            }
        }
        
    }

    /**
     * Individual part to be recorded for id2.
     * 
     * @author Holger Eichelberger
     * @since 1.00
     * @version 1.00
     */
    @Monitor(id = AnnotationId.ID_MULTI_2)
    private static class Part2 {

        /**
         * Stores the data.
         */
        @SuppressWarnings("unused")
        private Data data;
        
        /**
         * Executes this part.
         * 
         * @since 1.00
         */
        public void execute() {
            for (int i = 0; i < MAX_ALLOC2; i++) {
                data = new Data();
                if (i % 50 == 0) {
                    System.gc();
                }
            }
        }
    }
    
    /**
     * Common part.
     * 
     * @author Holger Eichelberger
     * @since 1.00
     * @version 1.00
     */
    @Monitor(id = {AnnotationId.ID_MULTI_1, AnnotationId.ID_MULTI_2 })
    private static class Part3 {

        /**
         * Executes this part.
         * 
         * @since 1.00
         */
        public void execute() {
            File file = new File("src/test/MultiRecIdTest.java");
            try {
                URL url = file.toURI().toURL();
                fileRead = TestUtils.read(url.openStream());
            } catch (MalformedURLException e) {
            } catch (IOException e) {
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
        // avoid explicit class loading
        dataSize = TestEnvironment.getObjectSize(new Data());
        // do tests
        new Part1().execute();
        new Part2().execute();
        new Part3().execute();
    }
    
    /**
     * The automated tests using assert statements. This method is called
     * by SPASS-meter at end of monitoring upon the <code>EndSystem</code> 
     * annotation.
     * 
     * @since 1.00
     */
    public static void asserts() {
        // own allocation + file + URL in part 3
        TestEnvironment.assertGreater(AnnotationId.ID_MULTI_1, 
            MonitoringGroupValue.ALLOCATED_MEMORY, dataSize * MAX_ALLOC1);
     // own allocation + file + URL in part 3
        TestEnvironment.assertGreater(AnnotationId.ID_MULTI_2, 
            MonitoringGroupValue.ALLOCATED_MEMORY, dataSize * MAX_ALLOC2);
        TestEnvironment.assertEquals(AnnotationId.ID_MULTI_1, 
            MonitoringGroupValue.FILE_READ, fileRead);
        TestEnvironment.success(AnnotationId.ID_INDIRECT);
    }
    
}
