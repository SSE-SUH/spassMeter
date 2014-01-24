package test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;

import test.testing.MonitoringGroupValue;
import test.testing.TestEnvironment;
import de.uni_hildesheim.sse.codeEraser.annotations.Variability;
import de.uni_hildesheim.sse.monitoring.runtime.annotations.EndSystem;
import de.uni_hildesheim.sse.monitoring.runtime.annotations.Monitor;
import de.uni_hildesheim.sse.monitoring.runtime.annotations.StartSystem;

/**
 * A specific test for direct and indirect monitoring groups in the
 * same thread. Here we check files but not network as network leads to reading
 * internal security policies by the Java library. 
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
@Variability(id = AnnotationId.VAR_TESTING)
@Monitor(id = AnnotationId.ID_INDIRECT_FILE)
public class IndirectTestFile {

    /**
     * Stores the number of bytes read.
     */
    private static long fileRead = 0;

    /**
     * Stores the number of bytes written.
     */
    private static long fileWrite = 0;

    /**
     * Defines the indirect/overlapping resource usage.
     * 
     * @author Holger Eichelberger
     * @since 1.00
     * @version 1.00
     */
    @Monitor(id = AnnotationId.ID_INDIRECT_SUB)
    private class SubTest {

        /**
         * Executes resource allocation.
         * 
         * @since 1.00
         */
        public void execute() {
            // put another instance of myself onto the internal 
            // instrumenter stack
            IndirectTestFile.this.allocateIndirect(this);
        }

        /**
         * Called to allocate the data.
         * 
         * @since 1.00
         */
        public void allocate() {
            String server = "www.uni-hildesheim.de";

            File file = new File("../Instrumentation/src/test/UrlTest.java");
            try {
                URL url = file.toURI().toURL();
                fileRead = TestUtils.read(url.openStream());
            } catch (MalformedURLException e) {
            } catch (IOException e) {
            }
            
            file = new File("../Instrumentation/generated/gen.txt");
            try {
                FileOutputStream fos = new FileOutputStream(file);
                PrintStream p = new PrintStream(fos);
                p.println(server);
                fos.close();
                fileWrite = file.length();
            } catch (IOException e) {
            }
        }

    }
    
    /**
     * Prevents this class from being instantiated from outside.
     * 
     * @since 1.00
     */
    private IndirectTestFile() {
    }

    /**
     * Allocates a sub object indirectly in order to cause this element to 
     * occur twice on the internal stack.
     * 
     * @param test the object to call the allocation method for
     * 
     * @since 1.00
     */
    private void allocateIndirect(SubTest test) {
        test.allocate();
    }
    
    /**
     * Executes resource allocation.
     * 
     * @since 1.00
     */
    public void execute() {
        // do nothing
    }

    /**
     * Executes the tests - main is not time-instrumented.
     * 
     * @since 1.00
     */
    private static void test() {
        IndirectTestFile indirectTest = new IndirectTestFile();
        indirectTest.execute();
        SubTest subTest = indirectTest.new SubTest();
        subTest.execute();
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
        test();
    }
    
    /**
     * The automated tests using assert statements. This method is called
     * by SPASS-meter at end of monitoring upon the <code>EndSystem</code> 
     * annotation.
     * 
     * @since 1.00
     */
    public static void asserts() {
        if (TestEnvironment.isIndirectTest()) {
            TestEnvironment.assertEquals(AnnotationId.ID_INDIRECT_FILE, 
                MonitoringGroupValue.FILE_READ, fileRead);
            TestEnvironment.assertEquals(AnnotationId.ID_INDIRECT_FILE, 
                MonitoringGroupValue.FILE_WRITE, fileWrite);
        } else {
            TestEnvironment.assertEquals(AnnotationId.ID_INDIRECT_FILE, 
                MonitoringGroupValue.FILE_READ, 0);
            TestEnvironment.assertEquals(AnnotationId.ID_INDIRECT_FILE, 
                MonitoringGroupValue.FILE_WRITE, 0);
        }
        TestEnvironment.assertEquals(AnnotationId.ID_INDIRECT_FILE, 
            MonitoringGroupValue.NET_READ, 0);
        TestEnvironment.assertEquals(AnnotationId.ID_INDIRECT_FILE, 
            MonitoringGroupValue.NET_WRITE, 0);
        
        TestEnvironment.assertEquals(AnnotationId.ID_INDIRECT_SUB, 
            MonitoringGroupValue.FILE_READ, fileRead);
        TestEnvironment.assertEquals(AnnotationId.ID_INDIRECT_SUB, 
            MonitoringGroupValue.FILE_WRITE, fileWrite);
        TestEnvironment.assertEquals(AnnotationId.ID_INDIRECT_SUB, 
            MonitoringGroupValue.NET_READ, 0);
        TestEnvironment.assertEquals(AnnotationId.ID_INDIRECT_SUB, 
            MonitoringGroupValue.NET_WRITE, 0);

        TestEnvironment.assertEquals(TestEnvironment.getProgramId(), 
            MonitoringGroupValue.FILE_READ, fileRead);
        TestEnvironment.assertEquals(TestEnvironment.getProgramId(), 
            MonitoringGroupValue.FILE_WRITE, fileWrite);
        TestEnvironment.assertEquals(TestEnvironment.getProgramId(), 
            MonitoringGroupValue.NET_READ, 0);
        TestEnvironment.assertEquals(TestEnvironment.getProgramId(), 
            MonitoringGroupValue.NET_WRITE, 0);

        TestEnvironment.success(AnnotationId.ID_INDIRECT_FILE);
    }
    
}
