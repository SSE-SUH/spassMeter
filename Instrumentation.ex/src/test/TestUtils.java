package test;

import java.io.IOException;
import java.io.InputStream;

/**
 * Some commonly used tests.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public class TestUtils {

    /**
     * Prevents this class from being instantiated from outside.
     * 
     * @since 1.00
     */
    private TestUtils() {
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
    public static int read(InputStream in) throws IOException {
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
    
}
