package de.uni_hildesheim.sse.monitoring.runtime.instrumentation.lib;

/**
 * Defines the interface for a behavior.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public interface IBehavior extends IMember {
    
    /**
     * Returns whether this behavior is abstract.
     * 
     * @return <code>true</code> if it is abstract, <code>false</code> else
     * 
     * @since 1.00
     */
    public boolean isAbstract();

    /**
     * Returns whether this behavior is native.
     * 
     * @return <code>true</code> if it is native, <code>false</code> else
     * 
     * @since 1.00
     */
    public boolean isNative();
    
    /**
     * Returns if this behavior is a constructor.
     * 
     * @return <code>true</code> if it is a constructor, <code>false</code> else
     * 
     * @since 1.00
     */
    public boolean isConstructor();
    
    /**
     * Returns the number of parameters.
     * 
     * @return the number of parameters
     * @throws InstrumenterException in case of any problem with the bytecode
     * 
     * @since 1.00
     */
    public int getParameterCount() throws InstrumenterException;

    /**
     * Returns the type of the specified parameter. Must released explicity
     * by {@link IClass#release()}.
     * 
     * @param index the index of the parameter
     * @return the parameter type
     * @throws InstrumenterException in case of any problem with the bytecode
     * 
     * @since 1.00
     */
    public IClass getParameterType(int index) throws InstrumenterException;

    /**
     * Returns the name of the type of the specified parameter.
     * 
     * @param index the index of the parameter
     * @return the parameter type name
     * @throws InstrumenterException in case of any problem with the bytecode
     * 
     * @since 1.00
     */
    public String getParameterTypeName(int index) throws InstrumenterException;
    // TODO check whether this is java.lang.String[]
    
    /**
     * Returns whether this behavior is a finalizer.
     * 
     * @return <code>true</code> if it is a finalizer, <code>false</code> else
     * @throws InstrumenterException in case of any problem with the bytecode
     * 
     * @since 1.00
     */
    public boolean isFinalize() throws InstrumenterException;
    
    /**
     * Instruments this behavior.
     * 
     * @param editor the behavior editor
     * @throws InstrumenterException in case of any problem with the bytecode
     * 
     * @since 1.00
     */
    public void instrument(BehaviorEditor editor) throws InstrumenterException;
    
    /**
     * Checks the invoke statement of an annotation and prepends the declaring
     * class if not given.
     * 
     * @param invoke the invoke statement
     * @return the processed invoke statement
     * 
     * @since 1.00
     */
    public String expandInvoke(String invoke);

    /**
     * Returns the Java (JVM) signature of this behavior. This is different
     * to {@link #getSignature()} as that method returns a readable signature.
     * 
     * @return the JVM signature
     * 
     * @since 1.00
     */
    public String getJavaSignature();
}
