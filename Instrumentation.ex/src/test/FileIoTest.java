package test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import test.testing.TestEnvironment;
import test.testing.MonitoringGroupValue;

import de.uni_hildesheim.sse.codeEraser.annotations.Variability;
import de.uni_hildesheim.sse.monitoring.runtime.annotations.EndSystem;
import de.uni_hildesheim.sse.monitoring.runtime.annotations.
    ExcludeFromMonitoring;
import de.uni_hildesheim.sse.monitoring.runtime.annotations.Monitor;
import de.uni_hildesheim.sse.monitoring.runtime.annotations.StartSystem;

/**
 * Performs some simple tests on reader/writer and stream based I/O.<br/>
 * Monitoring must be enabled via the agent JVM parameter!
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
@Variability(id = AnnotationId.VAR_TESTING)
@Monitor(id = AnnotationId.ID_FILE_IO)
public class FileIoTest {

    /**
     * Stores the total number of bytes read.
     */
    private static long totalRead = 0;

    /**
     * Stores the total number of bytes written.
     */
    private static long totalWrite = 0;
    
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
    protected FileIoTest() {
    }
    
    /**
     * Executes the test, i.e. reads a (source code) file and writes
     * some output to another file via streams and reader/writer.
     * 
     * @throws IOException any kind of I/O problem while reading / writing
     * 
     * @since 1.00
     */
    private static void execute() throws IOException {
        byte[] array = new byte[1024];
        FileInputStream in = new FileInputStream(inFile);
        int read;
        do {
            read = in.read(array, 0, array.length);
            totalRead += read;
        } while (read > 0);
        totalRead++; // also the 0
        in.close();

        FileReader fr = new FileReader(inFile);
        char[] buf = new char[1024];
        do {
            read = fr.read(buf, 0, buf.length);
            totalRead += read;
        } while (read > 0);
        totalRead++; // also the 0
        fr.close();
        TestEnvironment.notice("expected: read " + totalRead);

        FileOutputStream fos = new FileOutputStream(outFile);
        PrintWriter pw = new PrintWriter(fos);
        pw.print(outFile.getAbsolutePath());
        pw.close();
        totalWrite = outFile.length();
        
        FileWriter fw = new FileWriter(outFile);
        pw = new PrintWriter(fw);
        pw.print(outFile.getAbsolutePath());
        pw.close();
        totalWrite += outFile.length();
        
        TestEnvironment.notice("expected: write " + totalWrite);
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
    @EndSystem(invoke = "asserts()")
    public static void main(String[] args) throws IOException {
        setupFiles();
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
        TestEnvironment.notice("------------------ done: FileIo");
    }
    
    /**
     * The automated tests using assert statements. This method is called
     * by SPASS-meter at end of monitoring upon the <code>EndSystem</code> 
     * annotation.
     * 
     * @since 1.00
     */
    public static void asserts() {
        // for this class
        
        TestEnvironment.assertEquals(AnnotationId.ID_FILE_IO, 
            MonitoringGroupValue.FILE_READ, totalRead);
        TestEnvironment.assertEquals(AnnotationId.ID_FILE_IO, 
            MonitoringGroupValue.FILE_WRITE, totalWrite);
        TestEnvironment.assertEquals(AnnotationId.ID_FILE_IO, 
            MonitoringGroupValue.NET_READ, 0);
        TestEnvironment.assertEquals(AnnotationId.ID_FILE_IO, 
            MonitoringGroupValue.NET_WRITE, 0);
        TestEnvironment.assertEquals(AnnotationId.ID_FILE_IO, 
            MonitoringGroupValue.TOTAL_READ, totalRead);
        TestEnvironment.assertEquals(AnnotationId.ID_FILE_IO, 
            MonitoringGroupValue.TOTAL_WRITE, totalWrite);
        
        // for the entire program (no difference)
        
        TestEnvironment.assertEquals(TestEnvironment.getProgramId(), 
            MonitoringGroupValue.FILE_READ, totalRead);
        TestEnvironment.assertEquals(TestEnvironment.getProgramId(), 
            MonitoringGroupValue.FILE_WRITE, totalWrite);
        TestEnvironment.assertEquals(TestEnvironment.getProgramId(), 
            MonitoringGroupValue.NET_READ, 0);
        TestEnvironment.assertEquals(TestEnvironment.getProgramId(), 
            MonitoringGroupValue.NET_WRITE, 0);
        TestEnvironment.assertEquals(TestEnvironment.getProgramId(), 
            MonitoringGroupValue.TOTAL_READ, totalRead);
        TestEnvironment.assertEquals(TestEnvironment.getProgramId(), 
            MonitoringGroupValue.TOTAL_WRITE, totalWrite);
        
        TestEnvironment.success(AnnotationId.ID_FILE_IO);
    }
    
}
