package de.uni_hildesheim.sse.monitoring.runtime.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.rmi.dgc.VMID;

/**
 * Allows writing information to a file for debugging.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public class DebugStream {

    /**
     * For logging according to the VM.
     */
    public static final VMID ID = new VMID();
    
    /**
     * Prevents external creation.
     * 
     * @since 1.00
     */
    private DebugStream() {
    }
    
    /**
     * Prints the <code>text</code> to <code>file</code>. The output
     * includes the JVM id.
     * 
     * @param file the file name to write
     * @param text the text to write
     * 
     * @since 1.00
     */
    public static final void println(String file, String text) {
        try {
            PrintStream out = new PrintStream(
                new FileOutputStream(new File(file), true));
            out.println(ID.hashCode() + " " + text);
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
