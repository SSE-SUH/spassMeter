package test;

import de.uni_hildesheim.sse.codeEraser.annotations.Variability;

/**
 * Some common helper methods.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
@Variability(id = AnnotationId.VAR_TESTING)
public class TestHelper {

    /**
     * Stores the test helper.
     * 
     * @since 1.00
     */
    private TestHelper() {
    }
    
    /**
     * Calculates the size of an UTF encoded string as to be sent/read
     * by the data in/out streams. Taken from the data stream implementation.
     * 
     * @param str the string the size should be counted for
     * @return the length of the size
     * 
     * @since 1.00
     */
    public static final int getUtfLen(String str) {
        int strlen = str.length();
        int c;
        int utflen = 0;
        for (int i = 0; i < strlen; i++) {
            c = str.charAt(i);
            if ((c >= 0x0001) && (c <= 0x007F)) {
                utflen++;
            } else if (c > 0x07FF) {
                utflen += 3;
            } else {
                utflen += 2;
            }
        }
        return utflen;
    }
    
}
