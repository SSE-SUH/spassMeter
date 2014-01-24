package de.uni_hildesheim.sse.monitoring.runtime.instrumentation.lib;

import java.lang.annotation.Annotation;

/**
 * Defines the interface of a class member.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public interface IMember {
    
    /**
     * Returns the annotation of the specified <code>annotation</code> type
     * if it is defined for <code>member</code>.
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
        boolean remove);

    /**
     * Returns whether this behavior is static.
     * 
     * @return <code>true</code> if it is static, <code>false</code> else
     * 
     * @since 1.00
     */
    public boolean isStatic();

    /**
     * Returns whether this behavior is final.
     * 
     * @return <code>true</code> if it is final, <code>false</code> else
     * 
     * @since 1.00
     */
    public boolean isFinal();

    /**
     * Returns whether this behavior is public.
     * 
     * @return <code>true</code> if it is public, <code>false</code> else
     * 
     * @since 1.00
     */
    public boolean isPublic();

    /**
     * Returns whether this behavior is private.
     * 
     * @return <code>true</code> if it is private, <code>false</code> else
     * 
     * @since 1.00
     */
    public boolean isPrivate();

    /**
     * Returns whether this behavior is protected.
     * 
     * @return <code>true</code> if it is protected, <code>false</code> else
     * 
     * @since 1.00
     */
    public boolean isProtected();
    
    /**
     * Returns whether this behavior is package local.
     * 
     * @return <code>true</code> if it is package local, <code>false</code> else
     * 
     * @since 1.00
     */
    public boolean isPackageLocal();
    
    /**
     * Returns the declaring class. Must released explicity
     * by {@link IClass#release()}.
     * 
     * @return the declaring class
     * 
     * @since 1.00
     */
    public IClass getDeclaringClass();

    /**
     * Returns the name of the declaring class.
     * 
     * @return the name of the declaring class
     * 
     * @since 1.00
     */
    public String getDeclaringClassName();
    
    /**
     * Returns the signature of this member.
     * 
     * @return the signature
     * 
     * @since 1.00
     */
    public String getSignature();

    /**
     * Returns the name of this member.
     * 
     * @return the name of this member
     * 
     * @since 1.00
     */
    public String getName();
    
    /**
     * Releases this instance.
     */
    public void release();
    
}
