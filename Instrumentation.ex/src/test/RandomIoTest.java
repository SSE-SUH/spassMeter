package test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;

import test.testing.MonitoringGroupValue;
import test.testing.TestEnvironment;

import de.uni_hildesheim.sse.codeEraser.annotations.Variability;
import de.uni_hildesheim.sse.monitoring.runtime.annotations.EndSystem;
import de.uni_hildesheim.sse.monitoring.runtime.annotations.
    ExcludeFromMonitoring;
import de.uni_hildesheim.sse.monitoring.runtime.annotations.Monitor;
import de.uni_hildesheim.sse.monitoring.runtime.annotations.StartSystem;

/**
 * Performs some simple tests on random I/O.<p>
 * Monitoring must be enabled via the agent JVM parameter!
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
@Variability(id = AnnotationId.VAR_TESTING)
@Monitor(id = AnnotationId.ID_RANDOMIO)
public class RandomIoTest {

    /**
     * Stores the number of bytes read.
     */
    private static long randomRead = 0;

    /**
     * Stores the number of bytes written.
     */
    private static long randomWrite = 0;
    
    /**
     * Created at the beginning.
     */
    private static File inFile;
    
    /**
     * Written during test.
     */
    private static File outFile;
    
    /**
     * Prevents this class from being instantiated from outside.
     * 
     * @since 1.00
     */
    private RandomIoTest() {
    }
    
    /**
     * Executes the test, i.e. reads a (source code) file and writes
     * some output to another file.
     * 
     * @throws IOException any kind of I/O problem while reading / writing
     * 
     * @since 1.00
     */
    private static void execute() throws IOException {
        RandomAccessFile raf = new RandomAccessFile(inFile, "r");
        String line;
        do {
            line = raf.readLine();
        } while (null != line);
        raf.close();
        randomRead = inFile.length() + 1; // 0 end
        TestEnvironment.notice("random read " + randomRead);
            
        raf = new RandomAccessFile(outFile, "rw");
        raf.writeBytes(outFile.getAbsolutePath());
        raf.close();
        randomWrite = outFile.length();
        TestEnvironment.notice("random write " + randomWrite);
    }
    
    /**
     * Sets up the required files.
     * 
     * @throws IOException any kind of I/O problem while reading / writing
     * 
     * @since 1.00
     */
    @ExcludeFromMonitoring
    private static void setupFiles() throws IOException {
        File tempDir = new File(System.getProperty("java.io.tmpdir"));
        inFile = new File(tempDir, "spass-in.tmp");
        inFile.deleteOnExit();
        PrintWriter out = new PrintWriter(new FileWriter(inFile));
        for (int i = 0; i < 100; i++) {
            out.println("Instrumentation is EASy...");
        }
        out.close();
        outFile = new File(tempDir, "spass-out.tmp");
        outFile.deleteOnExit();
    }    
    
    /**
     * Starts the test.
     * 
     * @param args command line arguments (ignored)
     * @throws IOException any kind of I/O problem while reading / writing
     * 
     * @since 1.00
     */
    @StartSystem
    @EndSystem(invoke = "asserts")
    public static void main(String[] args) throws IOException {
        setupFiles();
        TestEnvironment.notice(RandomIoTest.class.getName());
        if (args.length > 0) {
            if (args[0].equals("continue")) {
                for (int i = 0; i < 20; i++) {
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
        TestEnvironment.notice("------------------ done: RandomIo");
    }
    
    /**
     * The automated tests using assert statements. This method is called
     * by SPASS-meter at end of monitoring upon the <code>EndSystem</code> 
     * annotation.
     * 
     * @since 1.00
     */
    public static void asserts() {
        TestEnvironment.assertEquals(AnnotationId.ID_RANDOMIO, 
            MonitoringGroupValue.NET_READ, 0);
        TestEnvironment.assertEquals(AnnotationId.ID_RANDOMIO, 
            MonitoringGroupValue.NET_WRITE, 0);
        TestEnvironment.assertEquals(AnnotationId.ID_RANDOMIO, 
            MonitoringGroupValue.FILE_READ, randomRead, 1); 
        TestEnvironment.assertEquals(AnnotationId.ID_RANDOMIO, 
            MonitoringGroupValue.FILE_WRITE, randomWrite);
        
        TestEnvironment.success(AnnotationId.ID_RANDOMIO);
    }


}
