package test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import de.uni_hildesheim.sse.codeEraser.annotations.Variability;
import de.uni_hildesheim.sse.monitoring.runtime.annotations.EndSystem;
import de.uni_hildesheim.sse.monitoring.runtime.annotations.Monitor;
import de.uni_hildesheim.sse.monitoring.runtime.annotations.StartSystem;

/**
 * Performs some simple tests stream based I/O over NIO channels.<p>
 * Monitoring must be enabled via the agent JVM parameter!
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
@Variability(id = AnnotationId.VAR_TESTING)
@Monitor(id = "fileIoChannelTest")
public class FileIoChannelTest {

    /**
     * Prevents this class from being instantiated from outside.
     * 
     * @since 1.00
     */
    private FileIoChannelTest() {
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
    @StartSystem
    @EndSystem
    public static void main(String[] args) throws IOException {
        System.out.println(FileIoChannelTest.class.getName());
        File file = new File("src/test/RandomIoTest.java");
        if (file.exists()) {
            ByteBuffer buf = ByteBuffer.allocate(1024);
            FileInputStream in = new FileInputStream(file);
            FileChannel channel = in.getChannel();
            @SuppressWarnings("unused")
            int totalRead = 0;
            int read;
            do {
                read = channel.read(buf);
                totalRead += read;
            } while (read > 0);

            in.close();
            System.out.println("read " + ( file.length() ));
        }

        ByteBuffer buf = ByteBuffer.allocate(1024);
        buf.put(file.getAbsolutePath().getBytes());
        
        File outFile = new File("generated/testfile.txt");
        FileOutputStream fos = new FileOutputStream(outFile);
        fos.getChannel().write(buf);
        fos.close();
        
        long totalWrite = outFile.length();
        
        System.out.println("write " + totalWrite);
        
        if (file.exists()) {
            long totalTransfer = 0L;
            FileInputStream in = new FileInputStream(file);
            FileChannel channel = in.getChannel();
            fos = new FileOutputStream(outFile);             
            totalTransfer += channel.transferTo(0, 
                file.length(), fos.getChannel());
            in.close();
            fos.close();
             
            in = new FileInputStream(file);
            fos = new FileOutputStream(outFile);
            totalTransfer += fos.getChannel().transferFrom(
                in.getChannel(), 0, file.length());
            in.close();
            fos.close();
            System.out.println("transfer " + totalTransfer + " (" 
                + (file.length() * 2) + ")");
        }
        
        System.out.println("------------------ done: FileIO");
    }
    
}
