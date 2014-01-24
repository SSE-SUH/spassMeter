package test.asmTree;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.URL;

import de.uni_hildesheim.sse.monitoring.runtime.annotations.ValueChange;
import de.uni_hildesheim.sse.monitoring.runtime.boot.RecorderAccess;
import de.uni_hildesheim.sse.monitoring.runtime.recording.Recorder;
import de.uni_hildesheim.sse.monitoring.runtime.wrap.ShutdownMonitor;

/**
 * Tests individual ASM instrumentations.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public class ClassInternalTest {

    /**
     * A value for testing value changes.
     */
    @SuppressWarnings("unused")
    @ValueChange(id = "myValue")
    private static int value;

    /**
     * Used to test URL situation from commons logging in 
     * {@link #testReChainLogging()}.
     */
    private URL val;
    
    /**
     * .
     * 
     * @since 1.00
     */
    private ClassInternalTest() {
    }

    /**
     * Singular test for memory allocation instrumentation.
     * 
     * @since 1.00
     */
    public static void testMemoryAllocated() {
        Object o = new Object();
        System.out.println(o);
    }

    /**
     * Singular test for type replacement.
     * 
     * @since 1.00
     */
    public static void testReplaceCreatedType() {
        try {
            FileInputStream fis = 
                new FileInputStream("src/test/asmTree/Test.java");
            fis.read();
            fis.close();
        } catch (IOException e) {
            e.printStackTrace(System.out);
        }
    }
    
    /**
     * Singular test for datagram transmission.
     * 
     * @since 1.00
     */
    public static void testIoDatagramTransmission() {
        try {
            byte[] sendData = "TEST".getBytes();
            DatagramSocket clientSocket = new DatagramSocket();
            clientSocket.setSoTimeout(3);
            InetAddress ipAddress = InetAddress.getByName("localhost");
            DatagramPacket sendPacket = new DatagramPacket(sendData, 
                sendData.length, ipAddress, 9999);
            clientSocket.send(sendPacket);
        } catch (IOException e) {
            // happens at send
        }
    }
    
    /**
     * Singular test for value changes.
     * 
     * @since 1.00
     */
    public static void testValueChange() {
        value++; 
    }
    
    /**
     * Singular test for starting a thread.
     * 
     * @since 1.00
     */
    public static void testThreadStart() {
        Thread thread = new Thread();
        thread.start();
    }
    
    /**
     * Singular test for re-chaining streams.
     * 
     * @since 1.00
     */
    public static void testReChain() {
        try {
            URL url = new File(".").toURI().toURL();
            LineNumberReader lnr = new LineNumberReader(
                new InputStreamReader(url.openStream()));
            lnr.close();
        } catch (IOException e) {
        }
    }
    
    /**
     * Test URL-Situation from commons logging.
     * 
     * @since 1.00
     */
    public void testReChainLogging() {
        try {
            InputStream is = val.openStream();
            if (null == is) {
                System.out.println("is null");
            } else {
                System.out.println("found");
            }
        } catch (IOException e) {
        }
    } 
    
    /**
     * Specific situation from rt.jar. Cast as direct parameter not accepted.
     * 
     * @param object an object to be casted to a URL
     * 
     * @since 1.00
     */
    public void testReChainCast(Object object) {
        try {
            InputStream is = ((URL) object).openStream();
            LineNumberReader lnr = new LineNumberReader(
                new InputStreamReader(is));
            lnr.close();
        } catch (IOException e) {
        }
    }
    
    /**
     * Returns the URL stored in this class.
     * 
     * @return the URL
     * 
     * @since 1.00
     */
    private URL getURL() {
        return val;
    }
    
    /**
     * A method call as parameter as a specific situation from 
     * <code>rt.jar</code>.
     * 
     * @since 1.00
     */
    public void testReChainInvoke() {
        try {
            InputStream is = getURL().openStream();
            LineNumberReader lnr = new LineNumberReader(
                new InputStreamReader(is));
            lnr.close();
        } catch (IOException e) {
        }      
    }

    /*
    (ok) appendMemoryAllocated(String)
    (ok) replaceCreatedType(String newType, boolean accountMemory)
    (ok) static
    (ok) public void notifyValueChanged(String recId)
    (ok) public void rechainStreamCreation(StreamType type,
    (ok) public void notifyThreadStarted(String contextId)
    (ok) notifyIoDatagramTransmission(String contextId, boolean write)
    ? public void notifyContextChange(String contextId)
    */
    
    /**
     * .
     * 
     * @param args a
     * @throws IOException a
     * 
     * @since 1.00
     */
    public static void main(String[] args) throws IOException {
        Recorder.initialize();
        RecorderAccess.notifyProgramStart();
        testMemoryAllocated();
        testReplaceCreatedType();
        testIoDatagramTransmission();
        testValueChange();
        testThreadStart();
        testReChain();
        
        ClassInternalTest classInternalTest = new ClassInternalTest();
        classInternalTest.val = new File(".").toURI().toURL();
        classInternalTest.testReChainLogging();
        classInternalTest.testReChainCast(classInternalTest.val);
        classInternalTest.testReChainInvoke();
        
        ShutdownMonitor.endMonitoring(true, 
            ClassInternalTest.class.getClassLoader(), null);
    }
    
}
