package test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import de.uni_hildesheim.sse.codeEraser.annotations.Variability;

/**
 * Provides a program with resource consumption which is not annotated (test
 * whether this particular issue works).
 * Monitoring must be enabled via the agent JVM parameter!
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
@Variability(id = AnnotationId.VAR_TESTING)
public class NoAnnotationTest {

    /**
     * Prevents this class from being instantiated from outside.
     * 
     * @since 1.00
     */
    private NoAnnotationTest() {
    }
    
    /**
     * Executes the test, i.e. reads a (source code) file and writes
     * some output to another file via streams and reader/writer.
     * 
     * @param args command line arguments (ignored)
     * @throws IOException any kind of I/O problem while reading / writing
     * 
     * @since 1.00
     */
    public static void main(String[] args) throws IOException {
        System.out.println(NoAnnotationTest.class.getName());
        File file = new File("src/test/RandomIoTest.java");
        if (file.exists()) {
            byte[] array = new byte[1024];
            FileInputStream in = new FileInputStream(file);
            int totalRead = 0;
            int read;
            do {
                read = in.read(array, 0, array.length);
                totalRead += read;
            } while (read > 0);
            in.close();

            FileReader fr = new FileReader(file);
            char[] buf = new char[1024];
            do {
                read = fr.read(buf, 0, buf.length);
                totalRead += read;
            } while (read > 0);
            fr.close();
            System.out.println("read " + totalRead);
            System.out.println("file length " + ( 2 * file.length() ));
        }
        
        file = new File("generated/testfile.txt");
        FileOutputStream fos = new FileOutputStream(file);
        PrintWriter pw = new PrintWriter(fos);
        pw.print(file.getAbsolutePath());
        pw.close();
        
        long totalWrite = file.length();
        FileWriter fw = new FileWriter(file);
        pw = new PrintWriter(fw);
        pw.print(file.getAbsolutePath());
        pw.close();
        totalWrite += file.length();
        
        System.out.println("write " + totalWrite);
        System.out.println("------------------ done: FileIO");
    }
    
}
