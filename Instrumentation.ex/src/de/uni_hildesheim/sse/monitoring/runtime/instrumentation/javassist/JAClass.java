package de.uni_hildesheim.sse.monitoring.runtime.instrumentation.javassist;

import java.lang.annotation.Annotation;

import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtField;
import javassist.NotFoundException;
import de.uni_hildesheim.sse.codeEraser.util.Annotations;
import de.uni_hildesheim.sse.codeEraser.util.ClassPool;
import de.uni_hildesheim.sse.monitoring.runtime.boot.ArrayList;
import de.uni_hildesheim.sse.monitoring.runtime.instrumentation.lib.
    InstrumenterException;
import de.uni_hildesheim.sse.monitoring.runtime.instrumentation.lib.IField;
import de.uni_hildesheim.sse.monitoring.runtime.instrumentation.lib.IBehavior;
import de.uni_hildesheim.sse.monitoring.runtime.instrumentation.lib.IClass;

/**
 * Implements the class abstraction.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public class JAClass extends IClass {

    /**
     * Stores the class pool.
     */
    private static final ArrayList<JAClass> CLASS_POOL 
        = new ArrayList<JAClass>(3);

    /**
     * Stores the field pool.
     */
    private static final ArrayList<JAField> FIELD_POOL 
        = new ArrayList<JAField>(1);

    /**
     * Stores the behavior pool.
     */
    private static final ArrayList<JABehavior> BEHAVIOR_POOL 
        = new ArrayList<JABehavior>(1);
    
    /**
     * Stores the class.
     */
    private CtClass cl;

    /**
     * Stores the class loader.
     */
    private ClassLoader loader;
    
    /**
     * Stores the interfaces (temporarily).
     */
    private CtClass[] interfaces;
    
    /**
     * Stores the declared fields (temporarily).
     */
    private CtField[] declaredFields;

    /**
     * Stores the declared behaviors (temporarily).
     */
    private CtBehavior[] declaredBehaviors;    
    
    /**
     * Returns a {@link JAClass} from the pool.
     * 
     * @param ctcl the class to attach
     * @return a class instance
     * 
     * @since 1.00
     */
    static final synchronized JAClass getClassFromPool(CtClass ctcl) {
        JAClass cl;
        int size = CLASS_POOL.size();
        if (size > 0) {
            cl = CLASS_POOL.remove(size - 1);
        } else {
            cl = new JAClass();
        }
        cl.attach(ctcl);
        return cl;
    }
    
    /**
     * Releases the given instance.
     * 
     * @param cl the instance to release
     * 
     * @since 1.00
     */
    static final synchronized void releaseClass(JAClass cl) {
        CLASS_POOL.add(cl);
    }
    
    /**
     * Returns a {@link JAField} from the pool.
     * 
     * @param field the field to attach
     * @return a class instance
     * 
     * @since 1.00
     */
    static final synchronized JAField getFieldFromPool(CtField field) {
        JAField result;
        int size = FIELD_POOL.size();
        if (size > 0) {
            result = FIELD_POOL.remove(size - 1);
        } else {
            result = new JAField();
        }
        result.attach(field);
        return result;
    }
    
    /**
     * Releases the given instance.
     * 
     * @param field the instance to release
     * 
     * @since 1.00
     */
    static final synchronized void releaseField(JAField field) {
        FIELD_POOL.add(field);
    }

    /**
     * Returns a {@link JABehavior} from the pool.
     * 
     * @param behavior the behavior to attach
     * @return a class instance
     * 
     * @since 1.00
     */
    static final synchronized JABehavior getBehaviorFromPool(
        CtBehavior behavior) {
        JABehavior result;
        int size = BEHAVIOR_POOL.size();
        if (size > 0) {
            result = BEHAVIOR_POOL.remove(size - 1);
        } else {
            result = new JABehavior();
        }
        result.attach(behavior);
        return result;
    }
    
    /**
     * Releases the given instance.
     * 
     * @param be the instance to release
     * 
     * @since 1.00
     */
    static final synchronized void releaseBehavior(JABehavior be) {
        BEHAVIOR_POOL.add(be);
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
        if (null == declaredFields) {
            declaredFields = cl.getDeclaredFields();
        }
        int result;
        if (null == declaredFields) {
            result = 0;
        } else {
            result = declaredFields.length;
        }
        return result;
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
        if (null == declaredFields) {
            declaredFields = cl.getDeclaredFields();
        }
        IField result;
        if (null == declaredFields) {
            result = null;
        } else {
            result = getFieldFromPool(declaredFields[index]);
        }
        return result;
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
    public String getDeclaredFieldName(int index) 
        throws InstrumenterException {
        if (null == declaredFields) {
            declaredFields = cl.getDeclaredFields();
        }
        String result;
        if (null == declaredFields) {
            result = null;
        } else {
            result = declaredFields[index].getName();
        }
        return result;
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
        if (null == declaredBehaviors) {
            declaredBehaviors = cl.getDeclaredBehaviors();
        }
        int result;
        if (null == declaredBehaviors) {
            result = 0;
        } else {
            result = declaredBehaviors.length;
        }
        return result;
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
        if (null == declaredBehaviors) {
            declaredBehaviors = cl.getDeclaredBehaviors();
        }
        IBehavior result;
        if (null == declaredBehaviors) {
            result = null;
        } else {
            result = getBehaviorFromPool(declaredBehaviors[index]);
        }
        return result;
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
        if (null == interfaces) {
            try {
                interfaces = cl.getInterfaces();
            } catch (NotFoundException e) {
                throw new InstrumenterException(e);
            }
        }
        int result;
        if (null == interfaces) {
            result = 0;
        } else {
            result = interfaces.length;
        }
        return result;
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
        if (null == interfaces) {
            try {
                interfaces = cl.getInterfaces();
            } catch (NotFoundException e) {
                throw new InstrumenterException(e);
            }
        }
        JAClass result;
        if (null != interfaces) {
            result = getClassFromPool(interfaces[index]);
        } else {
            result = null;
        }
        return result;
    }

    
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
    public <T extends Annotation> T getAnnotation(Class<T> annotation, 
        boolean remove) {
        return Annotations.getAnnotation(cl, annotation, remove);
    }

    /**
     * Returns the name of the class.
     * 
     * @return the name of the class
     * 
     * @since 1.00
     */
    public String getName() {
        return cl.getName();
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
        JAClass result;
        try {
            CtClass su = cl.getSuperclass();
            if (null != su) {
                result = getClassFromPool(su);
            } else {
                result = null;
            }
        } catch (NotFoundException e) {
            throw new InstrumenterException(e);
        }
        return result;
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
        JAClass result;
        try {
            CtClass su = cl.getDeclaringClass();
            if (null != su) {
                result = getClassFromPool(su);
            } else {
                result = null;
            }
        } catch (NotFoundException e) {
            throw new InstrumenterException(e);
        }
        return result;
    }

    
    /**
     * Releases this instance.
     */
    @Override
    public void release() {
        interfaces = null;
        declaredBehaviors = null;
        declaredFields = null;
        interfaces = null;
        if (null != loader) {
            cl.detach();
            // needed to cope with dynamic class loading, otherways the
            // class loader reference is kept in the class pool and 
            // cannot be released on demand
            ClassPool.removeClassLoader(loader);
        }
        cl = null;
        loader = null;
        releaseClass(this);
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
        return cl.isInterface();
    }

    /**
     * Returns the compiled bytecode for this class.
     * 
     * @return the bytecode
     * @throws InstrumenterException in case that bytecode cannot be produced
     */
    @Override
    public byte[] toBytecode() throws InstrumenterException {
        try {
            return cl.toBytecode();
        } catch (Exception e) {
            throw new InstrumenterException(e);
        }
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
    public boolean isInstanceOf(String type) {
        return isInstanceOf(cl, type);
    }

    /**
     * Returns if the given class is instance of the given <code>type</code>, 
     * whereby <code>type</code> may be a superclass.
     * 
     * @param clazz the class to be tested
     * @param type the type <code>clazz</code> should be a subclass of
     * @return <code>true</code> if <code>clazz</code> is a subclass of 
     *     <code>type</code>, <code>false</code> else
     * 
     * @since 1.00
     */
    public static final boolean isInstanceOf(CtClass clazz, String type) {
        boolean found = false;
        if (null != clazz) {
            found = clazz.getName().equals(type);
            if (!found) {
                try {
                    found = isInstanceOf(clazz.getSuperclass(), type);
                } catch (NotFoundException e) {
                }
                if (!found) {
                    try {
                        CtClass[] impl = clazz.getInterfaces();
                        if (null != impl) {
                            for (int i = 0; !found && i < impl.length; i++) {
                                found = isInstanceOf(impl[i], type);
                            }
                        }
                    } catch (NotFoundException e) {
                    }
                }
            }
        }
        return found;
    }
    
    /**
     * Attaches the given javassist class.
     * 
     * @param cl the javassist class
     * @param loader the class loader
     * 
     * @since 1.00
     */
    protected void attach(CtClass cl, ClassLoader loader) {
        this.cl = cl;
        this.loader = loader;
    }
    
    /**
     * Attaches the given javassist class without explicit classloader.
     * 
     * @param cl the javassist class
     * 
     * @since 1.00
     */
    void attach(CtClass cl) {
        this.cl = cl;
    }

    /**
     * Returns the attached class.
     * 
     * @return the attached class
     * 
     * @since 1.00
     */
    CtClass getCtClass() {
        return cl;
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
        IBehavior found = null;
        try {
            JABehavior behavior = (JABehavior) member;
            if (null == declaredBehaviors) {
                declaredBehaviors = cl.getDeclaredBehaviors();
            }
            for (int i  = 0; null == found 
                && i < declaredBehaviors.length; i++) {
                if (behavior.getName().equals(
                    declaredBehaviors[i].getName()) 
                    && behavior.checkParameter(
                        declaredBehaviors[i].getParameterTypes())) {
                    found = getBehaviorFromPool(declaredBehaviors[i]);
                }
            }
        } catch (NotFoundException e) {
            throw new InstrumenterException(e);
        }
        return found;
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
        return cl.isPrimitive();
    }

    
}
