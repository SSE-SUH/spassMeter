package de.uni_hildesheim.sse.monitoring.runtime.instrumentation.lib;

/**
 * Defines a statement bytecode editor for behaviors. Due to the abstraction
 * of the byte code modification libraries a code modifier instance is 
 * provided which defines a high level interface to required code modifications
 * and recorder notifications.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public class BehaviorEditor {

    /**
     * Stores the code modifier.
     */
    private IStatementModifier modifier;
    
    /**
     * Defines the code modifier.
     * 
     * @param modifier the code modifier
     * 
     * @since 1.00
     */
    public void setCodeModifier(IStatementModifier modifier) {
        this.modifier = modifier;
    }
    
    /**
     * Returns the code modifier instance.
     * 
     * @return the code modifier
     * 
     * @since 1.00
     */
    public IStatementModifier getCodeModifier() {
        return modifier;
    }
    
    /**
     * Instruments a field access for notifying the recorder about value 
     * changes.
     * 
     * @param name the name of the field
     * @param type the type of the field
     * @param isWriter whether the field value is changed
     * @throws InstrumenterException in case that the new code does not compile
     */
    public void editFieldAccess(String name, String type, boolean isWriter) 
        throws InstrumenterException {
    }

    /**
     * Instruments a method call, here starting additional threads. For detailed
     * information please consider the <code>mc*</code> methods in 
     * {@link #getCodeModifier()}.
     * 
     * @param name the name of the method being called
     * @param signature the JVM signature of the method
     * @param targetClass the class being called
     * @throws InstrumenterException in case that the new code does not compile
     */
    public void editMethodCall(String name, String signature, 
        String targetClass) throws InstrumenterException {
    }

    /**
     * Instruments an object creation.
     * 
     * @param type the type to be created
     * @throws InstrumenterException in case that the new code does not compile
     */
    public void editNewExpression(String type) throws InstrumenterException {
        
    }

    /**
     * Instruments the creation of an array.
     * 
     * @throws InstrumenterException in case that the new code does not compile
     */
    public void editNewArray() throws InstrumenterException {
    }

}
