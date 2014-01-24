package test.asmTree;

import java.io.File;
import java.io.IOException;

import de.uni_hildesheim.sse.monitoring.runtime.instrumentation.asmTree.AClass;
import de.uni_hildesheim.sse.monitoring.runtime.instrumentation.asmTree.Factory;
import de.uni_hildesheim.sse.monitoring.runtime.instrumentation.lib.IClass;
import de.uni_hildesheim.sse.monitoring.runtime.instrumentation.lib.
    InstrumenterException;
import de.uni_hildesheim.sse.monitoring.runtime.preprocess.ClassProcessor;
import de.uni_hildesheim.sse.monitoring.runtime.preprocess.OnTheFlyJarProcessor;

/**
 * Instruments JAR files for testing.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public class InstrumentJar extends OnTheFlyJarProcessor 
    implements ClassProcessor {

    /**
     * Counts the number of classes instrumented.
     */
    private int count = 0;

    /**
     * Executes the test.
     * 
     * @param args the JARs to be instrumented for test; at least one JAR
     *     must be given
     * 
     * @since 1.00
     */
    public static void main(String[] args) {
        if (0 == args.length) {
            System.err.println("needs at least one JAR as argument");
        } else {
            System.setProperty("spass-meter.iFactory", de.uni_hildesheim.
                sse.monitoring.runtime.instrumentation.asmTree.
                Factory.class.getName());
            AClass.setCheckCode(true);
            try {
                for (int i = 0; i < args.length; i++) {
                    File in = new File(args[i]);
                    System.out.println("instrumenting " + in);
                    String outName = in.getName();
                    outName = outName.substring(0, outName.lastIndexOf('.')) 
                        + "-gen.jar";
                    File out = new File("generated", outName);
                    Factory factory = Factory.getLocalFactory();
                    factory.doPruning(true);
                    factory.addClassLoader(
                        OnTheFlyJarProcessor.class.getClassLoader());
                    factory.addToClassPath(in.getAbsolutePath());
        
                    InstrumentJar instance = new InstrumentJar();
                    long time = System.currentTimeMillis();
                    instance.writeClasses(in.getAbsolutePath(), 
                        out.getAbsolutePath(), instance);
                    time = System.currentTimeMillis() - time;
                    double printTime;
                    String printUnit;
                    if (time < 1000) {
                        printTime = time;
                        printUnit = "ms";
                    } else {
                        printTime = time / 1000;
                        printUnit = "s";
                        if (printTime > 60) {
                            printTime = time / 60;
                            printUnit = "min";
                        }
                    }
                    System.out.printf(
                        "instrumented %d classes in %.2f %s to %s", 
                        instance.count, printTime, printUnit, out);
                    factory.cleanup();
                } 
            } catch (InstrumenterException e) {
                e.printStackTrace(System.err);
            } catch (IOException e) {
                e.printStackTrace(System.err);
            }
        }
    }
    
    /**
     * Returns whether the specified class should be processed.
     * 
     * @param name the name of the class
     * @return <code>true</code> if the class should be processed, 
     *     <code>false</code> if not but written to the output JAR, 
     *     if <b>null</b> the class should not be processed and not be written
     *     to the output JAR
     * 
     * @since 1.00
     */
    @Override
    public Boolean doProcess(String name) {
        return Boolean.TRUE;
    }
    
    /**
     * Processes the specified class.
     * 
     * @param cl the class to be processed
     * @throws InstrumenterException in case that the new code or the code
     *   modifications cannot be compiled
     * 
     * @since 1.00
     */
    @Override
    public void process(IClass cl) throws InstrumenterException {
        try {
            InstrumentAClass.instrument(cl);
            count++;
            Factory.getLocalFactory().cleanupIfRequired();
        } catch (InstrumenterException e) {
            throw new InstrumenterException("error while instrumenting " 
                + cl.getName() + " " + e.getMessage());
        }
    }
    
}
