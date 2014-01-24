package de.uni_hildesheim.sse.monitoring.runtime.instrumentation;

import de.uni_hildesheim.sse.monitoring.runtime.annotations.Monitor;

/**
 * An instance which collects assigned semantics.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public interface ISemanticsCollector {

    /**
     * Assigns the monitoring semantics for the method <code>methodName</code> 
     * in class <code>cls</code>.
     * 
     * @param cls the name of the class the monitoring semantics is defined for
     * @param methodSignature the signature of the method to assign the 
     *   semantics to
     * @param semantics the monitoring semantics
     * 
     * @since 1.00
     */
    public void assignSemantics(String cls, String methodSignature, 
        Monitor semantics);

}
