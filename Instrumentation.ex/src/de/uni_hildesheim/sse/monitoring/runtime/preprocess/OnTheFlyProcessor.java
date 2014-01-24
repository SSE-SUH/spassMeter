package de.uni_hildesheim.sse.monitoring.runtime.preprocess;

import de.uni_hildesheim.sse.monitoring.runtime.annotations.Instrumented;
import de.uni_hildesheim.sse.monitoring.runtime.configuration.Configuration;
import de.uni_hildesheim.sse.monitoring.runtime.instrumentation.
    AbstractClassTransformer;
import de.uni_hildesheim.sse.monitoring.runtime.instrumentation.
    TransformationType;
import de.uni_hildesheim.sse.monitoring.runtime.instrumentation.lib.*;

/**
 * Implements the frontend for the on-the-fly processor.
 * 
 * @author eichelberger
 * @since 1.00
 * @version 1.00
 */
public class OnTheFlyProcessor extends AbstractClassTransformer 
    implements ClassProcessor {

    /**
     * Stores the instrumentation arguments (may be <b>null</b>).
     */
    private String args;
    
    /**
     * Stores whether the processor should run in lazy mode, i.e. if it should
     * ignore class not found exceptions during instrumentation.
     */
    private boolean lazy = false;
    
    /**
     * Creates a new on the fly processor with given instrumentation arguments
     * (see 
     * {@link de.uni_hildesheim.sse.monitoring.runtime.instrumentation.Agent}).
     * 
     * @param args the instrumentation arguments, may be <b>null</b>
     * 
     * @since 1.00
     */
    public OnTheFlyProcessor(String args) {
        super(true);
        this.args = args;
    }

    /**
     * Changes whether the processor should run in lazy mode, i.e. if it should
     * ignore class not found exceptions during instrumentation.
     * 
     * @param lazy ignore errors or not
     */
    public void setLazy(boolean lazy) {
        this.lazy = lazy;
    }
    
    /**
     * Returns if the specified class should be processed.
     * 
     * @param name the name of the class (in Java notation, not VM notation)
     * @return <code>true</code> if the class should be processed, 
     *     <code>false</code> if not but written to the output JAR, 
     *     if <b>null</b> the class should not be processed and not be written
     *     to the output JAR
     * 
     * @since 1.00
     */
    @Override
    public Boolean doProcess(String name) {
        return Configuration.class.getName().equals(name) 
            || shouldInstrument(javaFqnToInternalVmFqnTo(name));
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
        ICodeModifier modifier = IFactory.getInstance().getCodeModifier();
        String name = cl.getName();
        boolean cnt = true;
        if (null != args && Configuration.class.getName().equals(name)) {
            try {
                modifier.storeConfiguration(cl, args);
                cnt = shouldInstrument(javaFqnToInternalVmFqnTo(name));
            } catch (InstrumenterException e) {
            } 
        }
        if (cnt) {
            try {
                transform(javaFqnToInternalVmFqnTo(name), cl, 
                    TransformationType.STATIC);
            } catch (InstrumenterException e) {
                if (!lazy) {
                    throw e;
                } else {
                    System.err.println("Warning in " + cl.getName() 
                        + ": " + e.getMessage());
                }
            }
        }
        modifier.addAnnotation(cl, Instrumented.class, null);
    }
    
    /**
     * Is called to handle an instrumentation which occurred during 
     * instrumenting a method. By default, this method does not handle this
     * exception and causes throwing for further handling
     * 
     * @param exception the exception occurred
     * @return <code>true</code> if the exception was handled and 
     *     instrumentation can continue with the next method, <code>false</code>
     *     if the exception should be thrown by the caller for further handling
     * 
     * @since 1.00
     */
/*    protected boolean handleMethodInstrumentationException(
        InstrumenterException exception) {
        boolean result;
        if (lazy) {
            // allow the instrumenter to continue in lazy mode
            System.err.println("Warning: " + exception.getMessage());
            result = true;
        } else {
            result = false;
        }
        return result;
    }*/

}
