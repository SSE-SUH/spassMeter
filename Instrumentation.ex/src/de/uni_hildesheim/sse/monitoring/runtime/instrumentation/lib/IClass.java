package de.uni_hildesheim.sse.monitoring.runtime.instrumentation.lib;

import java.lang.annotation.Annotation;

import de.uni_hildesheim.sse.monitoring.runtime.configuration.Configuration;

/**
 * Represents a class.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public abstract class IClass {

    /**
     * Returns the number of declared fields.
     * 
     * @return the number of declared fields
     * @throws InstrumenterException in case of any problems with the bytecode
     * 
     * @since 1.00
     */
    public abstract int getDeclaredFieldCount() throws InstrumenterException;

    /**
     * Returns the declared field at given <code>index</code>. Must released 
     * explicity by {@link IField#release()}.
     * 
     * @param index the declared field at the given index
     * @return the declared field
     * @throws InstrumenterException in case of any problems with the bytecode
     * 
     * @since 1.00
     */
    public abstract IField getDeclaredField(int index) 
        throws InstrumenterException;
    
    /**
     * Returns the name of the declared field at given <code>index</code>.
     * 
     * @param index the declared field at the given index
     * @return the name of the declared field
     * @throws InstrumenterException in case of any problems with the bytecode
     * 
     * @since 1.00
     */
    public abstract String getDeclaredFieldName(int index) 
        throws InstrumenterException;
    
    /**
     * Returns the number of declared behaviors.
     * 
     * @return the number of declared behaviors
     * @throws InstrumenterException in case of any problems with the bytecode
     * 
     * @since 1.00
     */
    public abstract int getDeclaredBehaviorCount() throws InstrumenterException;

    /**
     * Returns the declared behavior at given <code>index</code>. Must 
     * released explicity by {@link IBehavior#release()}.
     * 
     * @param index the declared behavior at the given index
     * @return the declared field
     * @throws InstrumenterException in case of any problems with the bytecode
     * 
     * @since 1.00
     */
    public abstract IBehavior getDeclaredBehavior(int index) 
        throws InstrumenterException;
    
    /**
     * Returns the number of interfaces of this class.
     * 
     * @return the number of interfaces
     * @throws InstrumenterException in case of any problems with the bytecode
     * 
     * @since 1.00
     */
    public abstract int getInterfaceCount() throws InstrumenterException;
    
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
    public abstract IClass getInterface(int index) throws InstrumenterException;

    /**
     * Returns the superclass of this class. Call 
     * {@link #release()} when not used anymore.
     * 
     * @return the super class of this class (may be <b>null</b>)
     * @throws InstrumenterException in case of any problems with the bytecode
     * 
     * @since 1.00
     */
    public abstract IClass getSuperclass() throws InstrumenterException;
    
    /**
     * Returns the declaring class of this class. Call 
     * {@link #release()} when not used anymore.
     * 
     * @return the declaring class of this class (may be <b>null</b>)
     * @throws InstrumenterException in case of any problems with the bytecode
     * 
     * @since 1.00
     */
    public abstract IClass getDeclaringClass() throws InstrumenterException;
    
    /**
     * Returns the name of the class.
     * 
     * @return the name of the class
     * 
     * @since 1.00
     */
    public abstract String getName();
    
    /**
     * Returns whether this class is an interface.
     * 
     * @return <code>true</code> if it is an interface, <code>false</code> else
     * 
     * @since 1.00
     */
    public abstract boolean isInterface();
    
    /**
     * Returns whether this class is an abstract class.
     * 
     * @return <code>true</code> if it is an abstract class, <code>false</code> 
     * else
     * 
     * @since 1.00
     */
    public abstract boolean isAbstract();
    
    /**
     * Returns the annotation of the specified <code>annotation</code> type
     * if it is defined for <code>cls</code>.
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
    public abstract <T extends Annotation> T getAnnotation(Class<T> annotation, 
        boolean remove);
    
    /**
     * Returns the compiled bytecode for this class.
     * 
     * @return the bytecode
     * @throws InstrumenterException in case that bytecode cannot be produced
     */
    public abstract byte[] toBytecode() throws InstrumenterException;
    
    /**
     * Returns whether this class is instance of the given <code>type</code>, 
     * whereby <code>type</code> may be a superclass.
     * 
     * @param type the type this class should be a subclass of
     * @return <code>true</code> if this class is a subclass of 
     *     <code>type</code>, <code>false</code> else
     * 
     * @since 1.00
     */
    public abstract boolean isInstanceOf(String type);

    /**
     * Returns whether this type is primitive.
     * 
     * @return <code>true</code> if this type is primitive, 
     *   <code>false</code> else
     * 
     * @since 1.00
     */
    public abstract boolean isPrimitive();
    
    /**
     * Releases this instance.
     */
    public abstract void release();
    
    /**
     * Search for a behavior with identical signature in this class.
     * 
     * @param member a member possibly taken from another class
     * @return the matching member in this class, <b>null</b> if not found
     * @throws InstrumenterException in case that bytecode cannot be produced
     * 
     * @since 1.00
     */
    public abstract IBehavior findSignature(IBehavior member) 
        throws InstrumenterException;

    /**
     * Searches the superclasses of this class for a final finalizer 
     * method.
     * 
     * @return <code>true</code> if one of the superclasses defines 
     *    a final finalizer (with a final class the entire hierarchy would
     *    be erroneous), <code>false</code> else
     * 
     * @since 1.00
     */
    public boolean hasSuperClassFinalFinalizer() {
        boolean found = false;
        try {
            IClass superCl = getSuperclass();
            if (null != superCl) {
                if (Configuration.INSTANCE.isInstrumented(superCl.getName())) {
                    found = true;
                } else {
                    int count = superCl.getDeclaredBehaviorCount();
                    for (int i = 0; !found && i < count; i++) {
                        IBehavior method = superCl.getDeclaredBehavior(i);
                        if (method.isFinal() && method.isFinalize()) {
                            found = true;
                        }
                        method.release();
                    }
                }
                if (!found) {
                    found = superCl.hasSuperClassFinalFinalizer();
                }
                superCl.release();
            }
        } catch (InstrumenterException ex) {
        }
        return found;
    }
    
    /**
     * Finds a superclass with a method having the given name and without 
     * parameter.
     * 
     * @param name the name of the method to search for
     * @return the found superclass or <b>null</b>
     * 
     * @since 1.00
     */
    public IClass findSuperclassWithMethodWoParameter(String name) {
        IClass result = null;
        try {
            IClass sClass = getSuperclass();
            if (null != sClass) {
                int count = sClass.getDeclaredBehaviorCount();
                for (int i = 0; i < count; i++) {
                    IBehavior behav = sClass.getDeclaredBehavior(i);
                    if (behav.getName().equals(name) 
                        && 0 == behav.getParameterCount()) {
                        result = sClass;
                    }
                    behav.release();
                }
                if (null == result) {
                    result = sClass.findSuperclassWithMethodWoParameter(name);
                    sClass.release();
                }
            }
        } catch (InstrumenterException e) {
        }
        return result;
    }

}
