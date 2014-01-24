package de.uni_hildesheim.sse.monitoring.runtime.instrumentation.lib;

import java.lang.annotation.Annotation;

import de.uni_hildesheim.sse.codeEraser.annotations.Variability;
import de.uni_hildesheim.sse.monitoring.runtime.AnnotationConstants;
import de.uni_hildesheim.sse.monitoring.runtime.boot.StreamType;
import de.uni_hildesheim.sse.monitoring.runtime.configuration.
    AnnotationSearchType;

/**
 * The abstract code modifications needed for SPASS-meter.
 * The interface of this class aims at avoiding the interfaces
 * defined in this instrumenter abstraction part. The reason is that
 * the more of the concrete implmentations are created, the less
 * efficient the implementation will be (as this runs over method
 * code). 
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public interface IStatementModifier {

    /**
     * Replace the created type in a new array or new object expression.
     * 
     * @param newType the type to be created
     * @param accountMemory should the allocated memory be accounted
     * @throws InstrumenterException in case of errors in the byte code
     * 
     * @since 1.00
     */
    public void replaceCreatedType(String newType, boolean accountMemory) 
        throws InstrumenterException;

    /**
     * Appends a memory allocation to the current statement and uses the result
     * of the current statement as input for the notification of the memory 
     * allocation. Insert value context change notifications if appropriate.
     * 
     * @param contextId the id for value context changes (may be <b>null</b>)
     * @throws InstrumenterException in case of errors in the byte code
     * 
     * @since 1.00
     */
    public void appendMemoryAllocated(String contextId) 
        throws InstrumenterException;
    
    /**
     * Add a attribute value notification call for the given recorder id.
     * 
     * @param recId the recorder id to notify (may be <b>null</b>)
     * @throws InstrumenterException in case of errors in the byte code
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.MONITOR_VALUES)
    public void notifyValueChanged(String recId) throws InstrumenterException;

    /**
     * Rechains a stream creation by inserting a delegating stream.
     * 
     * @param type the type of the stream (may be <b>null</b> in case that
     *   <code>typeExpression</code> specifies the type of the stream as a
     *   Java expression)
     * @param typeExpression an alternative expression taking precedence over
     *   <code>type</code>, may be <b>null</b>
     * @param input if <code>true</code> return an expression for an input 
     *   stream, for an output stream else
     * @throws InstrumenterException in case that the new code does not compile
     * 
     * @since 1.00
     */
    public void rechainStreamCreation(StreamType type, 
        TypeExpressions typeExpression, boolean input) 
        throws InstrumenterException;

    /**
     * Notifies about a thread start and insert value context change 
     * notifications if appropriate.
     * 
     * @param contextId the id for value context changes (may be <b>null</b>)
     * @throws InstrumenterException in case that the new code does not compile
     * 
     * @since 1.00
     */
    public void notifyThreadStarted(String contextId) 
        throws InstrumenterException;

    /**
     * Notifies about an individual data transmission call and insert value 
     * context change notifications if appropriate.
     * 
     * @param contextId the id for value context changes (may be <b>null</b>)
     * @param write is this a write or a read operation
     * @throws InstrumenterException in case that the new code does not compile
     * 
     * @since 1.00
     */
    public void notifyIoDatagramTransmission(String contextId, boolean write) 
        throws InstrumenterException;

    /**
     * Leave the underlying code as it is and just insert value context change
     * notifications if appropriate.
     * 
     * @param contextId the id for value context changes (may be <b>null</b>)
     * @throws InstrumenterException in case that the new code does not compile
     * 
     * @since 1.00
     */
    public void notifyContextChange(String contextId) 
        throws InstrumenterException;

    /**
     * Returns the name of the declaring class of the current method.
     * This method works properly only if the statement being processed is a 
     * method call. [performance]
     * 
     * @return the name of the declaring class.
     * @throws InstrumenterException in case that the declaring class is 
     *   not found
     * 
     * @since 1.00
     */
    public String mcDeclaringClassName() throws InstrumenterException;
    
    /**
     * Returns the declaring class of the current method is instance of the 
     * given <code>type</code>, whereby <code>type</code> may be a superclass.
     * This method works properly only if the statement being processed is a 
     * method call. [performance]
     * 
     * @param type the type the declaring class should be a subclass of
     * @return <code>true</code> if this class is a subclass of 
     *     <code>type</code>, <code>false</code> else
     * 
     * @throws InstrumenterException in case that the declaring class cannot 
     *     be found
     *     
     * @since 1.00
     */
    public boolean mcDeclaringClassInstanceOf(String type) 
        throws InstrumenterException;
    
    /**
     * Returns the number of parameters of the current method call. This method 
     * works properly only if the statement being processed is a method call.
     * [performance]
     * 
     * @return the number of parameters of the current method call
     * 
     * @throws InstrumenterException in case that the declaring class cannot 
     *     be found
     * 
     * @since 1.00
     */
    public int mcParameterCount() throws InstrumenterException;
    
    /**
     * Returns the type name of the given parameter of the current method call. 
     * This method works properly only if the statement being processed is a 
     * method call. [performance]
     * 
     * @param index the index of the parameter, 
     *     <code>0&lt;=index&lt;{@link #mcParameterCount()}</code>
     * @return the type name 
     * 
     * @throws InstrumenterException in case that the declaring class cannot 
     *     be found
     * @throws IndexOutOfBoundsException in case that <code>index</code> is 
     *     wrong
     *
     * @since 1.00
     */
    public String mcParameterTypeName(int index) throws InstrumenterException;

    /**
     * Returns whether the current method call goes to a static 
     * method. This method works properly only if the statement being 
     * processed is a method call. [performance]
     * 
     * @return <code>true</code> if the called method is static, 
     *   <code>false</code> else
     *   
     * @throws InstrumenterException in case that the declaring class cannot 
     *     be found
     * 
     * @since 1.00
     */
    public boolean mcIsStatic() throws InstrumenterException;

    /**
     * Returns the annotation of the specified <code>annotation</code> type
     * if it is defined for the current field access. This method does not
     * support pruning of annotations. This method works properly only if the 
     * statement being processed is a field access. [performance]
     * 
     * @param <T> the type of the annotation
     * @param annotation the type of the annotation to be searched for (meta 
     *     class)
     * @param search the annotation search type
     * @return the instance of the annotation if it is defined on 
     *     <code>method</code>, <b>null</b> otherwise
     * 
     * @since 1.00
     */
    public <T extends Annotation> T fAnnotation(Class<T> annotation, 
        AnnotationSearchType search);

    /**
     * Returns the name of the declaring class of the current field.
     * This method works properly only if the statement being processed is a 
     * field access. [performance]
     * 
     * @return the name of the declaring class.
     * @throws InstrumenterException in case that the declaring class is 
     *   not found
     * 
     * @since 1.00
     */
    public String fDeclaringClassName() throws InstrumenterException;

}
