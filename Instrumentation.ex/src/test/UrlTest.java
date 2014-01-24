package test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;

import test.testing.MonitoringGroupValue;
import test.testing.TestEnvironment;

import de.uni_hildesheim.sse.codeEraser.annotations.Variability;
import de.uni_hildesheim.sse.monitoring.runtime.annotations.EndSystem;
import de.uni_hildesheim.sse.monitoring.runtime.annotations.Monitor;
import de.uni_hildesheim.sse.monitoring.runtime.annotations.StartSystem;

/**
 * Defines some tests in order to find out if the accounting or URLs is correct.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
@Variability(id = AnnotationId.VAR_TESTING)
@Monitor(id = AnnotationId.ID_URL)
public class UrlTest {
        
    /**
     * Stores the bytes read from network.
     */
    private static long netRead = 0;

    /**
     * Stores the bytes read from file.
     */
    private static long fileRead = 0;
    
    /**
     * Prevents this class from being instantiated from outside.
     * 
     * 
     * @since 1.00
     */
    private UrlTest() {
    }
    
    /**
     * Reads the input stream and returns the number of input bytes.
     * 
     * @param in the input stream
     * @return the number of bytes read
     * @throws IOException in case of any error
     * 
     * @since 1.00
     */
    private static int read(InputStream in) throws IOException {
        byte[] buf = new byte[1024];
        int count = 0;
        int read;
        do {
            read = in.read(buf);
            if (read > 0) {
                count += read;
            }
        } while (read >= 0);
        return count;
    }
    
    /**
     * Executes the test.
     * 
     * @since 1.00
     */
    private static void execute() {
        String server = "www.uni-hildesheim.de";
        
        InputStream in = null;
        try {
            InetAddress address = InetAddress.getByName(server);
            if (address.isReachable(2000)) {
                URL url = new URL("http://" + server);
                in = url.openStream();
            }
        } catch (UnknownHostException e) {
        } catch (IOException e) {
        }
        if (null != in) {
            try {
                netRead = read(in);
                TestEnvironment.notice("net in " + netRead);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        try {
            File file = new File("../Instrumentation/src/test/UrlTest.java");
            URL url = file.toURI().toURL();
            fileRead = read(url.openStream());
            TestEnvironment.notice("file in: " + fileRead);
        } catch (MalformedURLException e) {
        } catch (IOException e) {
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
        TestEnvironment.notice(UrlTest.class.getName());
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
        TestEnvironment.notice("------------------ done: UrlTest");
    }

    /**
     * The automated tests using assert statements. This method is called
     * by SPASS-meter at end of monitoring upon the <code>EndSystem</code> 
     * annotation.
     * 
     * @since 1.00
     */
    public static void asserts() {
        // NET_WRITE may be 0 or not, and it may change during test due to 
        // net volatility -> careful test
        TestEnvironment.assertGreaterEquals(AnnotationId.ID_URL, 
            MonitoringGroupValue.NET_READ, netRead);
        TestEnvironment.assertGreaterEquals(AnnotationId.ID_URL, 
            MonitoringGroupValue.FILE_READ, fileRead);
        TestEnvironment.assertEquals(AnnotationId.ID_URL, 
            MonitoringGroupValue.FILE_WRITE, 0);
        
        TestEnvironment.success(AnnotationId.ID_URL);
    }

}
