package de.uni_hildesheim.sse.monitoring.runtime.instrumentation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

public class ElschaPermanentLogger {
    
    private static final PrintWriter OUT;
    
    static {
        PrintWriter out = null;
        
        File logFile = new File("/tmp/SPASSmeter3.log");
                
        try {
            out = new PrintWriter(new BufferedWriter(new FileWriter(logFile)));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        OUT = out;
        info("Logging started");
        
        
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                ElschaPermanentLogger.info("Programm shuts down.");
            }
        }));
    }
    
    public static void info(String msg) {
        Date date = new Date();
        OUT.write(date.toLocaleString() + ": " + msg + "\n");
        OUT.flush();
    }

}
