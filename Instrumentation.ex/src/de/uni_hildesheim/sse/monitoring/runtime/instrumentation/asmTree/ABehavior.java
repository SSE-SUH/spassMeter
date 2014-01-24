package de.uni_hildesheim.sse.monitoring.runtime.instrumentation.asmTree;

import java.lang.annotation.Annotation;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodNode;

import de.uni_hildesheim.sse.monitoring.runtime.boot.ArrayList;
import de.uni_hildesheim.sse.monitoring.runtime.boot.Flags;
import de.uni_hildesheim.sse.monitoring.runtime.instrumentation.lib.*;

/**
 * Implements a behavior.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public class ABehavior implements IBehavior {

    /**
     * Stores the internal name of a constructor.
     */
    static final String CONSTRUCTOR_NAME = "<init>";

    /**
     * Stores the node represented by this class.
     */
    private MethodNode node;

    /**
     * Stores the declaring class.
     */
    private AClass declaring;

    /**
     * Stores the parameter list (lazy initialization).
     */
    private ArrayList<String> parameter;
    
    /**
     * Attaches the given node.
     * 
     * @param node the node to be attached.
     * @param declaring the declaring class
     * 
     * @since 1.00
     */
    void attach(MethodNode node, AClass declaring) {
        this.node = node;
        this.declaring = declaring;
    }

    /**
     * Returns the annotation of the specified <code>annotation</code> type
     * if it is defined for this behavior.
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
        return (T) AClass.getAnnotation(node.visibleAnnotations, 
            annotation, remove, declaring.getClassLoader());
    }

    /**
     * Returns whether this behavior is static.
     * 
     * @return <code>true</code> if it is static, <code>false</code> else
     * 
     * @since 1.00
     */
    @Override
    public boolean isStatic() {
        return Flags.isSet(node.access, Opcodes.ACC_STATIC);
    }
    
    /**
     * Returns whether this behavior is final.
     * 
     * @return <code>true</code> if it is final, <code>false</code> else
     * 
     * @since 1.00
     */
    @Override
    public boolean isFinal() {
        return Flags.isSet(node.access, Opcodes.ACC_FINAL);
    }
    
    /**
     * Returns whether this behavior is public.
     * 
     * @return <code>true</code> if it is public, <code>false</code> else
     * 
     * @since 1.00
     */
    @Override
    public boolean isPublic() {
        return Flags.isSet(node.access, Opcodes.ACC_PUBLIC);
    }

    /**
     * Returns whether this behavior is private.
     * 
     * @return <code>true</code> if it is private, <code>false</code> else
     * 
     * @since 1.00
     */
    @Override
    public boolean isPrivate() {
        return Flags.isSet(node.access, Opcodes.ACC_PRIVATE);
    }

    /**
     * Returns whether this behavior is protected.
     * 
     * @return <code>true</code> if it is protected, <code>false</code> else
     * 
     * @since 1.00
     */
    @Override
    public boolean isProtected() {
        return Flags.isSet(node.access, Opcodes.ACC_PROTECTED);
    }
    
    /**
     * Returns whether this behavior is package local.
     * 
     * @return <code>true</code> if it is package local, <code>false</code> else
     * 
     * @since 1.00
     */
    @Override
    public boolean isPackageLocal() {
        return !isPublic() && !isPrivate() && !isProtected();
    }

    /**
     * Returns the declaring class.
     * 
     * @return the declaring class
     * 
     * @since 1.00
     */
    @Override
    public IClass getDeclaringClass() {
        declaring.notifyExternalUsage();
        return declaring;
    }

    /**
     * Returns the name of the declaring class.
     * 
     * @return the name of the declaring class
     * 
     * @since 1.00
     */
    @Override
    public String getDeclaringClassName() {
        return declaring.getName();
    }

    /**
     * Returns the signature of this member.
     * 
     * @return the signature
     * 
     * @since 1.00
     */
    @Override
    public String getSignature() {
        StringBuilder tmp = new StringBuilder();
        tmp.append(declaring.getName());
        tmp.append(".");
        tmp.append(getName());
        tmp.append("(");
        try {
            int count = getParameterCount();
            for (int i = 0; i < count; i++) {
                if (i > 0) {
                    tmp.append(",");
                }
                tmp.append(getParameterTypeName(i));
            }
        } catch (InstrumenterException e) {
        }
        tmp.append(")");
        return tmp.toString();
    }

    /**
     * Returns the name of this member.
     * 
     * @return the name of this member
     * 
     * @since 1.00
     */
    @Override
    public String getName() {
        return node.name;
    }

    /**
     * Releases this instance.
     */
    @Override
    public void release() {
        node = null;
        declaring = null;
        parameter = null;
        AClass.releaseBehavior(this);
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
        return Flags.isSet(node.access, Opcodes.ACC_NATIVE);
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
        return Flags.isSet(node.access, Opcodes.ACC_ABSTRACT);
    }

    /**
     * Returns if this behavior is a constructor.
     * 
     * @return <code>true</code> if it is a constructor, <code>false</code> else
     * 
     * @since 1.00
     */
    @Override
    public boolean isConstructor() {
        return node.name.equals(CONSTRUCTOR_NAME);
    }
    
    /**
     * Scans the parameter from the JVM signature and fills {@link #parameter}.
     * 
     * @param signature the signature to be scanned
     * @param toCodeName whether the result shall contain code names or 
     *   internal names
     * @return the parameter scanned from the signature
     * 
     * @since 1.00
     */
    static ArrayList<String> scanParameter(String signature, 
        boolean toCodeName) {
        ArrayList<String> parameter = new ArrayList<String>();
        if (null != signature) {
            int start = 0;
            int len = signature.length();
            while (start < len && '(' == signature.charAt(start)) {
                // should be exactly one
                start++;
            }
            while (')' != signature.charAt(start)) {
                int end = Factory.scanNextDescriptorName(signature, start);
                if (toCodeName) {
                    parameter.add(Factory.toCodeName(signature, start, 
                        end, false));
                } else {
                    parameter.add(signature.substring(start, end + 1));
                }
                start = end + 1;
            }
        }
        return parameter;
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
            parameter = scanParameter(node.desc, true);
        }
        return parameter.size();
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
            parameter = scanParameter(node.desc, true);
        }
        return Factory.getLocalFactory().obtainClass(
            Factory.toInternalName(parameter.get(index)), 
            declaring.getClassLoader());
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
            parameter = scanParameter(node.desc, true);
        }
        return parameter.get(index);
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
        return de.uni_hildesheim.sse.monitoring.runtime.instrumentation.
            lib.Utils.isFinalize(node.name, getParameterCount());
    }

    /**
     * Instruments this behavior.
     * 
     * @param editor the behavior editor
     * @throws InstrumenterException in case of any problem with the bytecode
     * 
     * @since 1.00
     */
    @Override
    public void instrument(BehaviorEditor editor) throws InstrumenterException {
        InsnList instructions = node.instructions;
        if (null != instructions) {
            StatementModifier modifier 
                = StatementModifier.getModifierFromPool();
            try {
                editor.setCodeModifier(modifier);
                modifier.loop(this, editor);
            } catch (InstrumenterException e) {
                throw e;
            } finally {
                editor.setCodeModifier(null);
                StatementModifier.releaseModifier(modifier);
            }
        }
    }
    
    /**
     * Returns the ASM node.
     * 
     * @return the ASM method node
     * 
     * @since 1.00
     */
    public MethodNode getNode() {
        return node;
    }
    
    /**
     * Returns the declaring class using the internal type.
     * 
     * @return the declaring class
     * 
     * @since 1.00
     */
    AClass getDeclaringAClass() {
        return declaring;
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
            invoke = declaring.getName() + "." + invoke;
        }
        return invoke;
    }

    /**
     * Returns whether the parameters of the given behavior contains the same 
     * parameter in the same sequence as the parameters of this behavior.
     * 
     * @param behavior the behavior to check
     * @return <code>true</code> if the parameter arrays are equal, 
     *   <code>false</code> else
     * @throws InstrumenterException in case of code errors
     * 
     * @since 1.00
     */
    boolean checkParameter(IBehavior behavior) throws InstrumenterException {
        boolean equals;
        // ensures lazy initialization
        int count = getParameterCount();
        if (count == behavior.getParameterCount()) {
            equals = true;
            for (int i = 0; equals && i < count; i++) {
                equals = getParameterTypeName(i).equals(
                    behavior.getParameterTypeName(i));
            }
        } else {
            equals = false;
        }
        return equals;
    }
    
    /**
     * Returns the descriptor name of the result type.
     * 
     * @return the result type as descriptor
     * 
     * @since 1.00
     */
    String getResultTypeDescriptorName() {
        String result = node.desc;
        int pos = result.lastIndexOf(')');
        if (pos > 0) {
            result = result.substring(pos + 1);
        } else {
            result = null;
        }
        return result;
    }
    
    /**
     * Returns the internal name of the result type.
     * 
     * @return the internal name
     * 
     * @since 1.00
     */
    String getResultTypeNameI() {
        return Utils.getInternalNameFromDescriptor(
            getResultTypeDescriptorName());
    }

    /**
     * {@inheritDoc}
     */
    public String getJavaSignature() {
        return node.desc;
    }
    
}
