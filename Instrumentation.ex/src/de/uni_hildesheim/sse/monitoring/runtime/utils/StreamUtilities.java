package de.uni_hildesheim.sse.monitoring.runtime.utils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Some utility methods for streams.
 * 
 * @author Stephan Dederichs
 * @since 1.00
 * @version 1.00
 */
public class StreamUtilities {

    /**
     * Prevents this class from being instantiated from outside.
     * 
     * @since 1.00
     */
    private StreamUtilities() {
    }
    
    /**
     * Writes a string to the given output stream and emits an additional short
     * to transfer if <code>text</code> is <b>null</b>.
     * 
     * @param out the output stream
     * @param text the text to be emitted
     * @throws IOException in case of any I/O error or problem
     * 
     * @since 1.00
     */
    public static void writeString(DataOutputStream out, String text) 
        throws IOException {
        if (null == text) {
            out.writeShort(0);
        } else {
            out.writeShort(1);
            out.writeUTF(text);
        }
    }

    /**
     * Reads a string from the given input stream and converts it 
     * to <b>null</b> if required.
     * 
     * @param in the input stream
     * @return the text to be read from <code>in</code>
     * @throws IOException in case of any I/O error or problem
     * 
     * @since 1.00
     */
    public static String readString(DataInputStream in) throws IOException {
        String result;
        short marker = in.readShort();
        if (0 == marker) {
            result = null;
        } else {
            result = in.readUTF();
        }
        return result;
    }

}
