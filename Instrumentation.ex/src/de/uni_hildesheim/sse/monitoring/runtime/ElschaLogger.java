package de.uni_hildesheim.sse.monitoring.runtime;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

public class ElschaLogger {
    
    private static final PrintWriter OUT;
    
    static {
        PrintWriter out = null;
        
        File logFile = new File("/tmp/SPASSmeterInstr.log");
        if (logFile.exists()) {
            logFile.delete();
        }
        
        try {
            out = new PrintWriter(new BufferedWriter(new FileWriter(logFile)));
        } catch (IOException e) {
            File errFile = new File("/tmp/SPASSmeter.err");
            try {
                errFile.createNewFile();
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        OUT = out;
        
        info("Logging started.");
    }
    
    public static void info(String msg) {
        Date date = new Date();
        OUT.write(date.toLocaleString() + ": " + msg + "\n");
        OUT.flush();
    }

}
