package de.uni_hildesheim.sse.monitoring.runtime.instrumentation.asmTree;

import java.lang.annotation.Annotation;

import org.objectweb.asm.tree.ClassNode;

import de.uni_hildesheim.sse.monitoring.runtime.instrumentation.lib.*;

/**
 * Implements the class abstraction for asm.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public class APrimitive extends AType {

    /**
     * Creates a representant for a primitive type.
     * 
     * @param internalName the internal JVM name
     * 
     * @since 1.00
     */
    APrimitive(char internalName) {
        setInternalName(String.valueOf(internalName));
    }
    
    /**
     * Returns the number of declared field.
     * 
     * @return the number of declared field
     * @throws InstrumenterException in case of any problems with the bytecode
     * 
     * @since 1.00
     */
    @Override
    public int getDeclaredFieldCount() throws InstrumenterException {
        return 0;
    }

    /**
     * Returns the declared field at given <code>index</code>.
     * 
     * @param index the declared field at the given index
     * @return the declared field
     * @throws InstrumenterException in case of any problems with the bytecode
     * 
     * @since 1.00
     */
    @Override
    public IField getDeclaredField(int index) throws InstrumenterException {
        return null;
    }
    
    /**
     * Returns the name of the declared field at given <code>index</code>.
     * 
     * @param index the declared field at the given index
     * @return the name of the declared field
     * @throws InstrumenterException in case of any problems with the bytecode
     * 
     * @since 1.00
     */
    @Override
    public String getDeclaredFieldName(int index) throws InstrumenterException {
        return null;
    }

    /**
     * Returns the number of declared behaviors.
     * 
     * @return the number of declared behaviors
     * @throws InstrumenterException in case of any problems with the bytecode
     * 
     * @since 1.00
     */
    @Override
    public int getDeclaredBehaviorCount() throws InstrumenterException {
        return 0;
    }

    /**
     * Returns the declared behavior at given <code>index</code>.
     * 
     * @param index the declared behavior at the given index
     * @return the declared field
     * @throws InstrumenterException in case of any problems with the bytecode
     * 
     * @since 1.00
     */
    @Override
    public IBehavior getDeclaredBehavior(int index)
        throws InstrumenterException {
        return null;
    }

    /**
     * Returns the number of interfaces of this class.
     * 
     * @return the number of interfaces
     * @throws InstrumenterException in case of any problems with the bytecode
     * 
     * @since 1.00
     */
    @Override
    public int getInterfaceCount() throws InstrumenterException {
        return 0;
    }

    /**
     * Returns the interface at the specific <code>index</code>.
     * 
     * @param index the index of the interface
     * @return the interface (to be released by 
     *   {@link #release()}).
     * @throws InstrumenterException in case of any problems with the bytecode
     * 
     * @since 1.00
     */    
    @Override
    public IClass getInterface(int index) throws InstrumenterException {
        return null;
    }

    /**
     * Returns the superclass of this class. Call 
     * {@link #release()} when not used anymore.
     * 
     * @return the super class of this class (may be <b>null</b>)
     * @throws InstrumenterException in case of any problems with the bytecode
     * 
     * @since 1.00
     */
    @Override
    public IClass getSuperclass() throws InstrumenterException {
        return null;
    }

    /**
     * Returns the declaring class of this class. Call 
     * {@link #release()} when not used anymore.
     * 
     * @return the declaring class of this class (may be <b>null</b>)
     * @throws InstrumenterException in case of any problems with the bytecode
     * 
     * @since 1.00
     */
    @Override
    public IClass getDeclaringClass() throws InstrumenterException {
        return null;
    }
    
    @Override
    public String getDeclaringClassName() throws InstrumenterException {
        return null;
    }

    /**
     * Returns whether this class is an interface.
     * 
     * @return <code>true</code> if it is an interface, <code>false</code> else
     * 
     * @since 1.00
     */
    @Override
    public boolean isInterface() {
        return false;
    }

    /**
     * Returns the annotation of the specified <code>annotation</code> type
     * if it is defined for this class.
     * 
     * @param <T> the type of the annotation
     * @param annotation the type of the annotation to be searched for (meta 
     *     class)
     * @param remove if <code>true</code> remove the found annotation as a 
     *   side effect, <code>false</code> do not modify anything
     * @return the instance of the annotation if it is defined on 
     *     <code>method</code>, <b>null</b> otherwise
     * 
     * @since 1.00
     */
    @Override
    public <T extends Annotation> T getAnnotation(Class<T> annotation,
        boolean remove) {
        return null;
    }

    /**
     * Returns the compiled bytecode for this class.
     * 
     * @return the bytecode
     * @throws InstrumenterException in case that bytecode cannot be produced
     */
    @Override
    public byte[] toBytecode() throws InstrumenterException {
        return null;
    }

    /**
     * Returns if the given class is instance of the given <code>type</code>, 
     * whereby <code>type</code> may be a superclass.
     * 
     * @param type the type this class should be a subclass of
     * @return <code>true</code> if this class is a subclass of 
     *     <code>type</code>, <code>false</code> else
     * 
     * @since 1.00
     */
    @Override
    public boolean isInstanceOf(String type) {
        return false;
    }

    /**
     * Releases this instance.
     */
    @Override
    public void release() {
        // cannot be released, do *not* call superclass
    }

    /**
     * Search for a behavior with identical signature in this class.
     * 
     * @param member a member possibly taken from another class
     * @return the matching member in this class, <b>null</b> if not found, 
     *   to be released explicitly
     * @throws InstrumenterException in case that bytecode cannot be produced
     * 
     * @since 1.00
     */
    @Override
    public IBehavior findSignature(IBehavior member)
        throws InstrumenterException {
        return null;
    }
        
    /**
     * Returns the class loader used for loading this class.
     * 
     * @return may be <b>null</b> if the system class loader should be used
     * 
     * @since 1.00
     */
    ClassLoader getClassLoader() {
        return null;
    }
    
    /**
     * Returns whether this type is primitive.
     * 
     * @return <code>true</code> if this type is primitive, 
     *   <code>false</code> else
     * 
     * @since 1.00
     */
    @Override
    public boolean isPrimitive() {
        return true;
    }
    
    /**
     * Returns the class node attached to this type.
     * 
     * @return the class node (may be <b>null</b>)
     * 
     * @since 1.00
     */
    public ClassNode getNode() {
        return null;
    }
    
    /**
     * Returns the component type in case of arrays.
     * 
     * @return the component type (may be <b>null</b>)
     * 
     * @since 1.00
     */
    public AType getComponentType() {
        return null;
    }

    /**
     * Returns whether this type is an instance of <code>type</code>.
     * 
     * @param type the type to check for
     * @return <code>true</code> if this type is an instance of 
     *   <code>type</code>, <code>false</code> else
     * 
     * @since 1.00
     */
    boolean isInstanceOf(AType type) {
        return false;
    }

    @Override
    public boolean isAbstract() {
        return false;
    }

}
