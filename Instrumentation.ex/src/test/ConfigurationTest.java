package test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

import test.testing.MonitoringGroupValue;
import test.testing.TestEnvironment;

import de.uni_hildesheim.sse.monitoring.runtime.annotations.ConfigurationChange;
import de.uni_hildesheim.sse.monitoring.runtime.annotations.EndSystem;
import de.uni_hildesheim.sse.monitoring.runtime.annotations.
    ExcludeFromMonitoring;
import de.uni_hildesheim.sse.monitoring.runtime.annotations.Monitor;
import de.uni_hildesheim.sse.monitoring.runtime.annotations.StartSystem;

/**
 * Tests the explicit configuration accounting.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public class ConfigurationTest {

    /**
     * Stores the file to be created and read.
     */
    private static final File FILE = new File("generated/config.txt");
    
    /**
     * Stores the currently active component.
     */
    private static Component comp;
    
    /**
     * Prevents this class from being initialized from outside.
     * 
     * @since 1.00
     */
    private ConfigurationTest() {
    }
    
    /**
     * A component interface and basic implementation.
     * 
     * @author Holger Eichelberger
     * @since 1.00
     * @version 1.00
     */
    public abstract static class Component {
        
        /**
         * Executes this component.
         * 
         * @since 1.00
         */
        public abstract void doIt();

        /**
         * Reads the stream returned by {@link #readStream()}.
         * Reusable implementation but not in {@link #doIt()} in order to be 
         * accountable.
         * 
         * @since 1.00
         */
        protected void readStream() {
            try {
                InputStream in = getStream();
                if (null != in) {
                    byte[] buf = new byte[1024];
                    int read;
                    do {
                        read = in.read(buf);
                    } while (read > 0);
                    in.close();
                }
            } catch (IOException e) {
            }
        }

        /**
         * Returns the stream to be read.
         * 
         * @return the stream to be read, may be <b>null</b>
         * @throws IOException in case of errors
         * 
         * @since 1.00
         */
        protected abstract InputStream getStream() throws IOException;
    }

    /**
     * A component reading a file directly.
     * 
     * @author Holger Eichelberger
     * @since 1.00
     * @version 1.00
     */
    @Monitor(id = AnnotationId.ID_CONFIG_FILE_COMPONENT)
    public static class FileComponent extends Component {
        
        /**
         * Executes this component.
         * 
         * @since 1.00
         */
        public void doIt() {
            readStream();
        }
        
        /**
         * Returns the stream to be read.
         * 
         * @return the stream to be read, may be <b>null</b>
         * @throws IOException in case of errors
         * 
         * @since 1.00
         */
        public InputStream getStream() throws IOException {
            return new FileInputStream(FILE);
        }
    }
    
    /**
     * A component reading a file from an URL.
     * 
     * @author Holger Eichelberger
     * @since 1.00
     * @version 1.00
     */
    @Monitor(id = AnnotationId.ID_CONFIG_URL_COMPONENT)
    public static class URLComponent extends Component {
        
        /**
         * Executes this component.
         * 
         * @since 1.00
         */
        public void doIt() {
            readStream();
        }

        /**
         * Returns the stream to be read.
         * 
         * @return the stream to be read, may be <b>null</b>
         * @throws IOException in case of errors
         * 
         * @since 1.00
         */
        public InputStream getStream() throws IOException {
            return FILE.toURI().toURL().openStream();
        }

    }

    /**
     * Returns the recording identifier.
     * 
     * @param file use file or URL
     * @return the assigned recording id
     * 
     * @since 1.00
     */
    @ExcludeFromMonitoring
    private static String configToId(boolean file) {
        if (file) {
            return AnnotationId.ID_CONFIG_FILE_COMPONENT;
        } else {
            return AnnotationId.ID_CONFIG_URL_COMPONENT;
        }
    }
    
    /**
     * Handles a component configuration change.
     * 
     * @param file use file or URL
     * 
     * @since 1.00
     */
    @ConfigurationChange(valueExpression = "configToId($1)")
    public static void configHandler(boolean file)  {
        if (file) {
            comp = new FileComponent();
        } else {
            comp = new URLComponent();
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
        try {
            FileOutputStream fos = new FileOutputStream(FILE);
            PrintStream ps = new PrintStream(fos);
            for (int i = 0; i < 1000; i++) {
                ps.print(42);
            }
            fos.close();
            configHandler(true);
            comp.doIt();
            comp.doIt();
            configHandler(false);
            comp.doIt();
        } catch (IOException e) {
            TestEnvironment.notice("Cannot create test file " + FILE
                + ". Aborted.");
            System.exit(0);
        }
    }
    
    /**
     * The automated tests using assert statements. This method is called
     * by SPASS-meter at end of monitoring upon the <code>EndSystem</code> 
     * annotation.
     * 
     * @since 1.00
     */
    public static void asserts() {
        long expected = FILE.length();
        testComponent(AnnotationId.ID_CONFIG_FILE_COMPONENT, 2 * expected);
        testComponent(AnnotationId.ID_CONFIG_URL_COMPONENT, expected);
        if (TestEnvironment.supportsConfigurations()) {
            TestEnvironment.assertTrue(AnnotationId.ID_CONFIG_FILE_COMPONENT, 
                2 == TestEnvironment.getNumberOfConfigurations());
            testConfiguration(AnnotationId.ID_CONFIG_FILE_COMPONENT, 
                2 * expected);
            testConfiguration(AnnotationId.ID_CONFIG_URL_COMPONENT, expected);
        } else {
            TestEnvironment.notice("Actual test environment does not support " 
                + "configurations! Tests skipped.");
        }
        TestEnvironment.success(AnnotationId.ID_CONFIG);
    }
    
    /**
     * Tests all configurations which contain <code>recId</code>.
     * 
     * @param recId the recording identifier to search for
     * @param expectedRead the expected number of read bytes
     * 
     * @since 1.00
     */
    private static void testConfiguration(String recId, long expectedRead) {
        boolean found = false;
        for (int i = 0; i < TestEnvironment.getNumberOfConfigurations(); i++) {
            String id = TestEnvironment.getConfigurationId(i);
            if (id.contains(recId)) {
                TestEnvironment.assertEquals(i, 
                    MonitoringGroupValue.FILE_READ, expectedRead);
                TestEnvironment.assertEquals(i, 
                    MonitoringGroupValue.FILE_WRITE, 0);
                TestEnvironment.assertEquals(i, 
                    MonitoringGroupValue.NET_READ, 0);
                TestEnvironment.assertEquals(i, 
                    MonitoringGroupValue.NET_READ, 0);
                found = true;
            }
        }
        if (!found) {
            TestEnvironment.assertTrue(recId, false);
        }
    }
    
    /**
     * Tests all recordings of a component.
     * 
     * @param recId the recording id of the component
     * @param expectedRead the expected number of bytes read
     * 
     * @since 1.00
     */
    private static void testComponent(String recId, long expectedRead) {
        TestEnvironment.assertEquals(recId, 
            MonitoringGroupValue.FILE_READ, expectedRead);
        TestEnvironment.assertEquals(recId, 
            MonitoringGroupValue.FILE_WRITE, 0);
        TestEnvironment.assertEquals(recId, 
            MonitoringGroupValue.NET_READ, 0);
        TestEnvironment.assertEquals(recId, 
            MonitoringGroupValue.NET_READ, 0);
    }
    
}
