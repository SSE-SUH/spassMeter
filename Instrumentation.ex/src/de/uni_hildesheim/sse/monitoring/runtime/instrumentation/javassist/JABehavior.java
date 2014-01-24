package de.uni_hildesheim.sse.monitoring.runtime.instrumentation.javassist;

import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtMethod;
import javassist.Modifier;
import javassist.NotFoundException;
import javassist.bytecode.MethodInfo;
import de.uni_hildesheim.sse.monitoring.runtime.instrumentation.lib.
    BehaviorEditor;
import de.uni_hildesheim.sse.monitoring.runtime.instrumentation.lib.
    InstrumenterException;
import de.uni_hildesheim.sse.monitoring.runtime.instrumentation.lib.IBehavior;
import de.uni_hildesheim.sse.monitoring.runtime.instrumentation.lib.IClass;
import de.uni_hildesheim.sse.monitoring.runtime.instrumentation.lib.Utils;

/**
 * Implements a behavior.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public class JABehavior extends JAMember implements IBehavior {

    /**
     * Stores the behavior.
     */
    private CtBehavior behavior;
    
    /**
     * Stores the parameter (temporarily).
     */
    private CtClass[] parameter;

    /**
     * Stores the declaring class name.
     */
    private String clazz = null;

    /**
     * Stores a string containing an expression
     * how to access "this". 
     */
    private String sthis;
    
    /**
     * Returns the signature of this member.
     * 
     * @return the signature
     * 
     * @since 1.00
     */
    @Override
    public String getSignature() {
        return behavior.getLongName();
    }
    
    /**
     * Returns the name of the declaring class.
     * 
     * @return the name of the declaring class
     * 
     * @since 1.00
     */
    public String getDeclaringClassName() {
        return clazz;
    }
    
    /**
     * Returns whether this behavior is abstract.
     * 
     * @return <code>true</code> if it is abstract, <code>false</code> else
     * 
     * @since 1.00
     */
    @Override
    public boolean isAbstract() {
        return Modifier.isAbstract(behavior.getModifiers());
    }

    /**
     * Returns whether this behavior is native.
     * 
     * @return <code>true</code> if it is native, <code>false</code> else
     * 
     * @since 1.00
     */
    @Override
    public boolean isNative() {
        return Modifier.isNative(behavior.getModifiers());
    }
    
    /**
     * Returns whether this behavior is a finalizer.
     * 
     * @return <code>true</code> if it is a finalizer, <code>false</code> else
     * @throws InstrumenterException in case of any problem with the bytecode
     * 
     * @since 1.00
     */
    @Override
    public boolean isFinalize() throws InstrumenterException {
        return Utils.isFinalize(behavior.getName(), getParameterCount());
    }

    /**
     * Returns the number of parameters.
     * 
     * @return the number of parameters
     * @throws InstrumenterException in case of any problem with the bytecode
     * 
     * @since 1.00
     */
    @Override
    public int getParameterCount() throws InstrumenterException {
        if (null == parameter) {
            try {
                parameter = behavior.getParameterTypes();
            } catch (NotFoundException e) {
                throw new InstrumenterException(e);
            }
        }
        int result;
        if (null == parameter) {
            result = 0;
        } else {
            result = parameter.length;
        }
        return result;
    }

    /**
     * Returns the type of the specified parameter.
     * 
     * @param index the index of the parameter
     * @return the parameter type
     * @throws InstrumenterException in case of any problem with the bytecode
     * 
     * @since 1.00
     */
    @Override
    public IClass getParameterType(int index) throws InstrumenterException {
        if (null == parameter) {
            try {
                parameter = behavior.getParameterTypes();
            } catch (NotFoundException e) {
                throw new InstrumenterException(e);
            }
        }
        JAClass result;
        if (null != parameter) {
            result = JAClass.getClassFromPool(parameter[index]);
        } else {
            result = null;
        }
        return result;
    }

    /**
     * Returns the name of the type of the specified parameter.
     * 
     * @param index the index of the parameter
     * @return the parameter type name
     * @throws InstrumenterException in case of any problem with the bytecode
     * 
     * @since 1.00
     */
    @Override
    public String getParameterTypeName(int index) throws InstrumenterException {
        if (null == parameter) {
            try {
                parameter = behavior.getParameterTypes();
            } catch (NotFoundException e) {
                throw new InstrumenterException(e);
            }
        }
        String result;
        if (null != parameter) {
            result = parameter[index].getName();
        } else {
            result = null;
        }
        return result;
    }
    
    /**
     * Attaches a javassist behavior.
     * 
     * @param behavior the javassist behavior
     * 
     * @since 1.00
     */
    void attach(CtBehavior behavior) {
        super.attach(behavior);
        this.behavior = behavior;
        MethodInfo methodInfo2 = behavior.getMethodInfo2();
        clazz = behavior.getDeclaringClass().getName();
        if (methodInfo2.isConstructor()) {
            sthis = "$0";
        } else if (methodInfo2.isStaticInitializer() 
            || Modifier.isStatic(behavior.getModifiers())) {
            sthis = "null";
        } else if (behavior instanceof CtMethod) {
            sthis = "$0";
        }
    }
    
    /**
     * Releases this instance.
     */
    @Override
    public void release() {
        behavior = null;
        parameter = null;
        clazz = null;
        sthis = null;
        super.release();
        JAClass.releaseBehavior(this);
    }

    
    
    // revise!!!
    
    

    /**
     * Returns the declaring class name.
     * 
     * @return the name of the declaring class
     */
    public String getClazz() {
        return clazz;
    }

    /**
     * Returns a string containing an expression
     * how to access "this". 
     * 
     * @return <b>null</b> in case of a static method, 
     *      an expression (for the byte code library) otherways
     */
    public String getSThis() {
        return sthis;
    }

    /**
     * Returns an expression with determines the actual class name dependent
     * on the behavior context.
     * 
     * @return the expression deriving the class name
     * 
     * @since 1.00
     */
    public String getClassNameExpression() {
        return getClassExpression(false) + ".getName()";
    }

    /**
     * Returns an expression with determines the actual class dependent
     * on the behavior context.
     * 
     * @param forceStatic even in case that the dynamic variant is possible
     *    return a static expression     
     * @return the expression deriving the class name
     * 
     * @since 1.00
     */
    private String getClassExpression(boolean forceStatic) {
        String expression;
        if (forceStatic || behavior.getMethodInfo().isStaticInitializer() 
            || Modifier.isStatic(behavior.getModifiers())) {
            //expression = "$class.getName()";
            expression = getClazz() + ".class";
        } else {
            expression = "$0.getClass()";
        }
        return expression;
    }

    
    /**
     * Returns the class expression augmented by the corresponding class 
     * loader expression.
     * 
     * @param forceStatic even in case that the dynamic variant is possible
     *    return a static expression
     * @return the class loader expression
     * 
     * @since 1.00
     */
    public String getClassLoaderExpression(boolean forceStatic) {
        return getClassExpression(forceStatic) + ".getClassLoader()";
    }
    
    /**
     * Inserts the given code before the existing behavior body. This method 
     * selects dynamically between insertBeforeBody and insertBefore.
     * 
     * @param code the new code to be inserted
     * @throws CannotCompileException in case that the new code cannot be 
     *     compiled
     * 
     * @since 1.00
     */
    public void insertBefore(String code) throws CannotCompileException {
        if (isConstructor()) {
            CtConstructor cons = (CtConstructor) behavior;
            cons.insertBeforeBody(code);
        } else {
            behavior.insertBefore(code);
        }
    }
    
    /**
     * Returns if this behavior is a constructor.
     * 
     * @return <code>true</code> if it is a constructor, <code>false</code> else
     * 
     * @since 1.00
     */
    public boolean isConstructor() {
        return behavior.getMethodInfo2().isConstructor();
    }
    
    /**
     * Adds a catch clause.
     * 
     * @param code the new code for the catch clause
     * @param exceptionType the type of exception to be added
     * @throws CannotCompileException in case that the new catch clause cannot
     *   be compiled
     * 
     * @since 1.00
     */
    public void addCatch(String code, CtClass exceptionType) 
        throws CannotCompileException {
        behavior.addCatch(code, exceptionType);
    }
    
    /**
     * Inserts code after the existing code.
     * 
     * @param code the code to be inserted
     * @throws CannotCompileException in case that the new code cannot be 
     *     compiled
     * 
     * @since 1.00
     */
    public void insertAfter(String code) throws CannotCompileException {
        behavior.insertAfter(code);
    }
    
    /**
     * Adds a local variable top the behavior.
     * 
     * @param name the name of the new variable
     * @param type the type of the new variable
     * @throws CannotCompileException in case that the new variable cannot be 
     *   compiled
     * 
     * @since 1.00
     */
    public void addLocalVariable(String name, CtClass type) 
        throws CannotCompileException {
        behavior.addLocalVariable(name, type);
    }

    /**
     * Instruments this behavior.
     * 
     * @param editor the behavior editor
     * @throws InstrumenterException in case of any problem with the bytecode
     * 
     * @since 1.00
     */
    public void instrument(BehaviorEditor editor) throws InstrumenterException {
        try {
            AllDelegatingEditor delEd = 
                AllDelegatingEditor.getFromPool(editor);
            behavior.instrument(delEd);
            AllDelegatingEditor.releaseClass(delEd);
        } catch (CannotCompileException e) {
            Throwable t = e.getCause();
            if (t instanceof InstrumenterException) {
                InstrumenterException ie = (InstrumenterException) t;
                if (!ie.isHandled()) {
                    throw (InstrumenterException) t;
                }
            } else {
                throw new InstrumenterException("in " + getDeclaringClassName()
                    + "." + getName() + " ", e);
            }
        }
    }
    
    /**
     * Returns whether the given parameter array contains the same parameter
     * in the same sequence as the parameters of this behavior.
     * 
     * @param param the types to check for
     * @return <code>true</code> if the parameter arrays are equal, 
     *   <code>false</code> else
     * @throws InstrumenterException in case of code errors
     * 
     * @since 1.00
     */
    boolean checkParameter(CtClass[] param) throws InstrumenterException {
        if (null == parameter) {
            try {
                parameter = behavior.getParameterTypes();
            } catch (NotFoundException e) {
                throw new InstrumenterException(e);
            }
        }
        boolean equals;
        if (param.length == parameter.length) {
            equals = true;
            for (int i = 0; equals && i < param.length; i++) {
                equals = param[i].equals(parameter[i]);
            }
        } else {
            equals = false;
        }
        return equals;
    }
    
    /**
     * Returns the behavior instance for internal use.
     * 
     * @return the behavior instance
     * 
     * @since 1.00
     */
    CtBehavior getBehavior() {
        return behavior;
    }
    
    /**
     * Checks the invoke statement of an annotation and prepends the declaring
     * class if not given.
     * 
     * @param invoke the invoke statement
     * @return the processed invoke statement
     * 
     * @since 1.00
     */
    public String expandInvoke(String invoke) {
        if (null != invoke && invoke.length() > 0 && invoke.indexOf('.') < 0) {
            invoke = getClazz() + "." + invoke;
        }
        return invoke;
    }
    
    /**
     * {@inheritDoc}
     */
    public String getJavaSignature() {
        return behavior.getSignature();
    }
    
}
