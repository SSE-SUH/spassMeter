package de.uni_hildesheim.sse.monitoring.runtime.preprocess;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.tools.ant.DirectoryScanner;

import de.uni_hildesheim.sse.monitoring.runtime.configuration.Configuration;
import de.uni_hildesheim.sse.monitoring.runtime.instrumentation.lib.*;

/**
 * Implements a simple byte code preprocessor (static instrumentation).
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.22
 */
public class Preprocess extends OnTheFlyJarProcessor {
    
    /**
     * The main method of the preprocessor. 
     * 
     * @param args first parameter the source jar(s) [if multiple, separated by comma, may be ANT patterns], second 
     *   the target jar or directory (then the name of the source jar is used), the third 
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
            System.exit(0);
        }
        List<File> input = toFiles(args[0]);
        String params = null;
        if (3 == args.length) {
            params = args[2];
            Configuration.INSTANCE.readFromAgentArguments(params);
        }
        for (int f = 0; f < input.size(); f++) {
            File in = input.get(f);
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
    
    /**
     * Turns a single input file, a list of comma-separated file names or ANT file patterns into a list of 
     * input files.
     * 
     * @param input the input string (comma separated in case of multiple ones)
     * @return the list of input files
     * 
     * @since 1.22
     */
    private static List<File> toFiles(String input) {
        List<File> result = new ArrayList<File>();
        StringTokenizer tokens = new StringTokenizer(input, ",");
        while (tokens.hasMoreTokens()) {
            String token = tokens.nextToken();
            if (token.indexOf("?") > 0 || token.indexOf("*") > 0) {
                int pos = token.lastIndexOf(File.separator);
                DirectoryScanner ds = new DirectoryScanner();
                if (pos > 0 && pos + 1 < token.length()) {
                    ds.setBasedir(token.substring(0, pos + 1));
                    ds.setIncludes(new String[]{token.substring(pos + 1)});
                } else {
                    ds.setBasedir(".");
                    ds.setIncludes(new String[]{token});
                }
                ds.scan();
                String[] matches = ds.getIncludedFiles();
                for (String m : matches) {
                    result.add(new File(m));
                }
            } else {
                result.add(new File(token));
            }
        }        
        return result;
    }
    
}
