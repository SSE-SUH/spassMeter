package de.uni_hildesheim.sse.monitoring.runtime.instrumentation.javassist;

import javassist.CannotCompileException;
import javassist.expr.ExprEditor;
import javassist.expr.FieldAccess;

/**
 * Realizes an instrumenter to inject the instrumentation arguments into
 * the configuration (so that no configuration is needed to run a statically
 * instrumented program).
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
class ConfigurationFieldEditor extends ExprEditor {

    /**
     * Stores the instrumentation args to be written into the Configuration
     * class.
     */
    private String args;
    
    /**
     * Creates a new configuration field editor.
     * 
     * @param args the instrumentation arguments to be considered
     * 
     * @since 1.00
     */
    public ConfigurationFieldEditor(String args) {
        this.args = args;
    }
    
    /**
     * Instruments a field access for notifying the recorder about value 
     * changes.
     * 
     * @param fa the field access
     * @throws CannotCompileException in case that the new code does not compile
     */
    public void edit(FieldAccess fa) throws CannotCompileException {
        if (null != args && fa.isWriter() && fa.isStatic() 
            && fa.getFieldName().equals("cmdArgs")) {
            fa.replace("cmdArgs = \"" + args + "\";");
        }
    }

}
