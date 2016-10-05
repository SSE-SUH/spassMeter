package de.uni_hildesheim.sse.monitoring.runtime.recording;

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
        
        File logFile = new File("/tmp/SPASSmeter2.log");
        if (logFile.exists()) {
            logFile.delete();
        }
        
        try {
            out = new PrintWriter(new BufferedWriter(new FileWriter(logFile)));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        OUT = out;
        info("Logging started");
    }
    
    public static void info(String msg) {
        Date date = new Date();
        OUT.write(date.toLocaleString() + ": " + msg + "\n");
        OUT.flush();
    }

}