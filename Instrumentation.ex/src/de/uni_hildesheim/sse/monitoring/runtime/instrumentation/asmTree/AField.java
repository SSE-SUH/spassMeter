package de.uni_hildesheim.sse.monitoring.runtime.instrumentation.asmTree;

import java.lang.annotation.Annotation;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.FieldNode;

import de.uni_hildesheim.sse.monitoring.runtime.boot.Flags;
import de.uni_hildesheim.sse.monitoring.runtime.instrumentation.lib.*;

/**
 * Implements a field.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public class AField implements IField {

    /**
     * Stores the node represented by this class.
     */
    private FieldNode node;
    
    /**
     * Stores the declaring class.
     */
    private AClass declaring;

    /**
     * Stores the name.
     */
    private String name;
    
    /**
     * Stores the type descriptor.
     */
    private String desc;
    
    /**
     * Attaches the given node.
     * 
     * @param node the node to be attached.
     * @param declaring the declaring class
     * @param name in case of fixed fields such as arrays
     * @param desc descriptor in case of fixed fields such as array lengths
     * 
     * @since 1.00
     */
    void attach(FieldNode node, AClass declaring, String name, String desc) {
        this.node = node;
        this.declaring = declaring;
        if (null != name) {
            this.name = name;
        } else {
            this.name = node.name;
        }
        if (null != desc) {
            this.desc = desc;
        } else {
            this.desc = node.desc;
        }
    }

    /**
     * Returns the annotation of the specified <code>annotation</code> type
     * if it is defined for this member.
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
        T result;
        if (null == node) {
            result = null;
        } else {
            result = (T) AClass.getAnnotation(node.visibleAnnotations, 
                annotation, remove, declaring.getClassLoader());
        }
        return result;
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
        return getDeclaringClassName() + "." + getName();
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
        return name;
    }

    /**
     * Releases this instance.
     */
    @Override
    public void release() {
        node = null;
        declaring = null;
        name = null;
        desc = null;
        AClass.releaseField(this);
    }

    /**
     * Returns the name of the type of this field.
     * 
     * @return the name of the type
     * 
     * @throws InstrumenterException in case of errors
     * @since 1.00
     */
    @Override
    public String getTypeName() throws InstrumenterException {
        return Factory.toCodeName(desc, false);
    }
    
    /**
     * Returns the ASM node representing the field. (internal use)
     * 
     * @return the ASM node
     * 
     * @since 1.00
     */
    public FieldNode getNode() {
        return node;
    }

}
