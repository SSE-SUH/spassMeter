package de.uni_hildesheim.sse.monitoring.runtime.instrumentation.javassist;

import java.lang.annotation.Annotation;

import javassist.CtClass;
import javassist.CtMember;
import javassist.Modifier;

import de.uni_hildesheim.sse.codeEraser.util.Annotations;
import de.uni_hildesheim.sse.monitoring.runtime.instrumentation.lib.IClass;
import de.uni_hildesheim.sse.monitoring.runtime.instrumentation.lib.IMember;

/**
 * Implements a member.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public abstract class JAMember implements IMember {

    /**
     * Stores the member.
     */
    private CtMember member;
    
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
    public <T extends Annotation> T getAnnotation(Class<T> annotation, 
        boolean remove) {
        return Annotations.getAnnotation(member, annotation, remove);
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
        return Modifier.isStatic(member.getModifiers());
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
        return Modifier.isFinal(member.getModifiers());
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
        return Modifier.isPublic(member.getModifiers());
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
        return Modifier.isPrivate(member.getModifiers());
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
        return Modifier.isProtected(member.getModifiers());
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
        return Modifier.isPackage(member.getModifiers());
    }

    /**
     * Returns the signature of this member.
     * 
     * @return the signature
     * 
     * @since 1.00
     */
    public String getSignature() {
        System.err.println("no signature for member type: " 
            + member.getClass().getName());
        return "";
    }

    /**
     * Returns the name of the declaring class.
     * 
     * @return the name of the declaring class
     * 
     * @since 1.00
     */
    public String getDeclaringClassName() {
        return member.getDeclaringClass().getName();
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
        CtClass decl = member.getDeclaringClass();
        JAClass result;
        if (null != decl) {
            result = JAClass.getClassFromPool(decl);
        } else {
            result = null;
        }
        return result;
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
        return member.getName();
    }
    
    /**
     * Attaches the javassist member.
     * 
     * @param member the member to attach
     * 
     * @since 1.00
     */
    void attach(CtMember member) {
        this.member = member;
    }

    /**
     * Releases this instance.
     */
    public void release() {
        member = null;
    }
    
}
