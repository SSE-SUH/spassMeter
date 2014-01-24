package de.uni_hildesheim.sse.monitoring.runtime.preprocess;

import java.io.File;
import java.io.IOException;
import java.util.StringTokenizer;

import de.uni_hildesheim.sse.monitoring.runtime.configuration.Configuration;
import de.uni_hildesheim.sse.monitoring.runtime.instrumentation.lib.*;

/**
 * Implements a simple byte code preprocessor (static instrumentation).
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public class Preprocess extends OnTheFlyJarProcessor {
    
    /**
     * The main method of the preprocessor. 
     * 
     * @param args first parameter the source jar, second the target jar or 
     *   directory (then the name of the source jar is used), the third 
     *   (optional) the instrumentation parameters (see
     *   {@link de.uni_hildesheim.sse.monitoring.runtime.instrumentation.Agent})
     * 
     * @since 1.00
     */
    public static void main(String[] args) {
        Configuration.INSTANCE.setStaticInstrumentation(true);
        IFactory factory = IFactory.getInstance();
        if (args.length < 2 || args.length > 3) {
            System.out.println("illegal number of arguments: " 
                + "<in(s)> <out(dir)> [<params>]");
        }
        StringTokenizer tokens = new StringTokenizer(args[0], ",");
        String params = null;
        if (3 == args.length) {
            params = args[2];
            Configuration.INSTANCE.readFromAgentArguments(params);
        }
        while (tokens.hasMoreTokens()) {
            File in = new File(tokens.nextToken());
            File out = new File(args[1]);
            try {
                if (out.isDirectory()) {
                    out = new File(out, in.getName());
                }
                if (!out.exists() || out.lastModified() < in.lastModified()) {
                    System.out.println("instrumenting " + in);
                    factory.doPruning(true);
                    factory.addClassLoader(
                        OnTheFlyJarProcessor.class.getClassLoader());
                    factory.addToClassPath(in.getAbsolutePath());
    
                    Preprocess instance = new Preprocess();
                    OnTheFlyProcessor ofProcessor 
                        = new OnTheFlyProcessor(params);
                    ofProcessor.setLazy(true);
                    instance.writeClasses(in.getAbsolutePath(), 
                        out.getAbsolutePath(), ofProcessor);
                    System.out.println("instrumented to " + out);
                    factory.cleanup();
                }
            } catch (IOException e) {
                System.err.println(e.getMessage());
                out.delete();
            } catch (InstrumenterException e) {
                System.err.println(e.getMessage());
                out.delete();
            }
        }
    }
    
}
