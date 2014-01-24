package de.uni_hildesheim.sse.codeEraser.util;

import java.lang.annotation.Annotation;

import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMember;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;
import javassist.bytecode.FieldInfo;
import javassist.bytecode.MethodInfo;

/**
 * Provides methods for accessing and querying annotations. Some of the methods
 * are provided in similar form and implementation in javassist but removal of
 * annotations is complicated. Therefore we repeat this functionality here.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public class Annotations {

    /**
     * Prevents this class from being instantiated from outside.
     * 
     * @since 1.00
     */
    private Annotations() {
    }

    // getting uncasted annotations from javassist instances
    
    /**
     * Returns the annotation instance of the specified 
     * <code>annotation</code> type if it is defined for <code>cls</code>. This
     * method is indeed the same as defined in javassist but it provides the 
     * opportunity to also remove the annotation.
     * 
     * @param <T> the type of the annotation
     * @param cls the class to be considered for querying the 
     *     <code>annotation</code>
     * @param annotation the type of the annotation to be searched for (meta 
     *     class)
     * @param remove if <code>true</code> remove the found annotation as a 
     *     side effect, <code>false</code> do not modify anything
     * @return the instance of the annotation if it is defined on 
     *     <code>method</code>, <b>null</b> otherwise
     * @throws ClassNotFoundException in case that the target class for casting
     *   cannot be found
     * 
     * @since 1.00
     */
    public static final <T extends Annotation> Object getAnnotationObject(
        CtClass cls, Class<T> annotation, boolean remove) 
        throws ClassNotFoundException {
        Object annotationObject = null;
        ClassFile cf = cls.getClassFile2();
        String typeName = annotation.getName();
        AnnotationsAttribute ainfo = (AnnotationsAttribute)
            cf.getAttribute(AnnotationsAttribute.invisibleTag);  
        if (null != ainfo) {
            annotationObject = checkAnnotation(ainfo, typeName, 
                cls.getClassPool(), remove);
        }
        if (null == annotationObject) {
            ainfo = (AnnotationsAttribute)
                cf.getAttribute(AnnotationsAttribute.visibleTag);
            if (null != ainfo) {
                annotationObject = checkAnnotation(ainfo, typeName, 
                    cls.getClassPool(), remove);
            }
        }
        return annotationObject;
    }
    
    /**
     * Returns the annotation instance of the specified <code>annotation</code> 
     * type if it is defined for <code>field</code>. This
     * method is indeed the same as defined in javassist but it provides the 
     * opportunity to also remove the annotation.
     * 
     * @param <T> the type of the annotation
     * @param field the field to be considered for querying the 
     *     <code>annotation</code>
     * @param annotation the type of the annotation to be searched for (meta 
     *     class)
     * @param remove if <code>true</code> remove the found annotation as a 
     *     side effect, <code>false</code> do not modify anything
     * @return the instance of the annotation if it is defined on 
     *     <code>method</code>, <b>null</b> otherwise
     * @throws ClassNotFoundException in case that the target class for casting
     *   cannot be found
     * 
     * @since 1.00
     */
    public static final <T extends Annotation> Object getAnnotationObject(
        CtField field, Class<T> annotation, boolean remove) 
        throws ClassNotFoundException {
        Object annotationObject = null;
        FieldInfo fi = field.getFieldInfo2();
        String typeName = annotation.getName();
        AnnotationsAttribute ainfo = (AnnotationsAttribute)
            fi.getAttribute(AnnotationsAttribute.invisibleTag);  
        if (null != ainfo) {
            annotationObject = checkAnnotation(ainfo, typeName, 
                field.getDeclaringClass().getClassPool(), remove);
        }
        if (null == annotationObject) {
            ainfo = (AnnotationsAttribute)
                fi.getAttribute(AnnotationsAttribute.visibleTag);
            if (null != ainfo) {
                annotationObject = checkAnnotation(ainfo, typeName, 
                    field.getDeclaringClass().getClassPool(), remove);
            }
        }
        return annotationObject;
    }

    /**
     * Returns the annotation instance of the specified <code>annotation</code> 
     * type if it is defined for <code>field</code>. This
     * method is indeed the same as defined in javassist but it provides the 
     * opportunity to also remove the annotation.
     * 
     * @param <T> the type of the annotation
     * @param behavior the behavior to be considered for querying the 
     *     <code>annotation</code>
     * @param annotation the type of the annotation to be searched for (meta 
     *     class)
     * @param remove if <code>true</code> remove the found annotation as a 
     *     side effect, <code>false</code> do not modify anything
     * @return the instance of the annotation if it is defined on 
     *     <code>method</code>, <b>null</b> otherwise
     * @throws ClassNotFoundException in case that the target class for casting
     *   cannot be found
     * 
     * @since 1.00
     */
    public static final <T extends Annotation> Object getAnnotationObject(
        CtBehavior behavior, Class<T> annotation, boolean remove) 
        throws ClassNotFoundException {
        Object annotationObject = null;
        MethodInfo mi = behavior.getMethodInfo2();
        String typeName = annotation.getName();
        AnnotationsAttribute ainfo = (AnnotationsAttribute)
            mi.getAttribute(AnnotationsAttribute.invisibleTag);  
        if (null != ainfo) {
            annotationObject = checkAnnotation(ainfo, typeName, 
                behavior.getDeclaringClass().getClassPool(), remove);
        }
        if (null == annotationObject) {
            ainfo = (AnnotationsAttribute)
                mi.getAttribute(AnnotationsAttribute.visibleTag);
            if (null != ainfo) {
                annotationObject = checkAnnotation(ainfo, typeName, 
                    behavior.getDeclaringClass().getClassPool(), remove);
            }
        }
        return annotationObject;
    }
    
    /**
     * Returns the annotation instance of the specified <code>annotation</code> 
     * type if it is defined for <code>member</code>. This
     * method is indeed the same as defined in javassist but it provides the 
     * opportunity to also remove the annotation.
     * 
     * @param <T> the type of the annotation
     * @param member the member to be considered for querying the 
     *     <code>annotation</code>
     * @param annotation the type of the annotation to be searched for (meta 
     *     class)
     * @param remove if <code>true</code> remove the found annotation as a 
     *   side effect, <code>false</code> do not modify anything
     * @return the instance of the annotation if it is defined on 
     *     <code>method</code>, <b>null</b> otherwise
     * @throws ClassNotFoundException in case that the target class for casting
     *   cannot be found
     * 
     * @since 1.00
     */
    public static final <T extends Annotation> Object getAnnotationObject(
        CtMember member, Class<T> annotation, boolean remove) 
        throws ClassNotFoundException {
        Object annotationObject = null;
        if (member instanceof CtBehavior) {
            annotationObject = getAnnotationObject((CtBehavior) member, 
                annotation, remove);
        } else if (member instanceof CtField) {
            annotationObject = getAnnotationObject((CtField) member, 
                annotation, remove);
        }
        return annotationObject;
    }
    
    // query existence 
    
    /**
     * Returns if the given <code>member</code> has the specified 
     * <code>annotation</code> type. This method is slightly faster than
     * {@link #getAnnotation(CtMember, Class)} in case that the annotation
     * instance is not needed.
     * 
     * @param <T> the type of the annotation
     * @param member the member to be considered for querying the 
     *     <code>annotation</code>
     * @param annotation the type of the annotation to be searched for (meta 
     *     class)
     * @param remove if <code>true</code> remove the found annotation as a 
     *   side effect, <code>false</code> do not modify anything
     * @return <code>true</code> if an annotation of type 
     *     <code>annotation</code> is defined on <code>method</code>
     * 
     * @since 1.00
     */
    public static final <T extends Annotation> boolean hasAnnotation(
        CtMember member, Class<T> annotation, boolean remove) {
        boolean result = false;
        try {
            result = null != //member.getAnnotation(annotation);
                getAnnotationObject(member, annotation, remove);
        } catch (ClassNotFoundException e) {
            result = false;
        }
        return result;
    }

    /**
     * Returns if the given <code>cls</code> has the specified 
     * <code>annotation</code> type. This method is slightly faster than
     * {@link #getAnnotation(CtClass, Class)} in case that the annotation
     * instance is not needed.
     * 
     * @param <T> the type of the annotation
     * @param cls the class to be considered for querying the 
     *     <code>annotation</code>
     * @param annotation the type of the annotation to be searched for (meta 
     *     class)
     * @param remove if <code>true</code> remove the found annotation as a 
     *   side effect, <code>false</code> do not modify anything
     * @return <code>true</code> if an annotation of type 
     *     <code>annotation</code> is defined on <code>method</code>
     * 
     * @since 1.00
     */
    public static final <T extends Annotation> boolean hasAnnotation(
        CtClass cls, Class<T> annotation, boolean remove) {
        boolean result = false;
        try {
            result = null != //cls.getAnnotation(annotation);
                getAnnotationObject(cls, annotation, remove);
        } catch (ClassNotFoundException e) {
            result = false;
        }
        return result;
    }

    // query typed annotation
    
    /**
     * Returns the annotation of the specified <code>annotation</code> type
     * if it is defined for <code>member</code>.
     * 
     * @param <T> the type of the annotation
     * @param member the member to be considered for querying the 
     *     <code>annotation</code>
     * @param annotation the type of the annotation to be searched for (meta 
     *     class)
     * @param remove if <code>true</code> remove the found annotation as a 
     *   side effect, <code>false</code> do not modify anything
     * @return the instance of the annotation if it is defined on 
     *     <code>method</code>, <b>null</b> otherwise
     * 
     * @since 1.00
     */
    public static final <T extends Annotation> T getAnnotation(
        CtMember member, Class<T> annotation, boolean remove) {
        T result =  null;
        try {
            Object annotationObject = //member.getAnnotation(annotation);
                getAnnotationObject(member, annotation, remove);
            if (null != annotationObject) {
                result = annotation.cast(annotationObject);
            }
        } catch (ClassNotFoundException e) {
        } catch (ClassCastException e) {
            // should not happen, because then annotationObject should 
            // be null
        }
        return result;
    }
    
    /**
     * Returns the annotation of the specified <code>annotation</code> type
     * if it is defined for <code>cls</code>.
     * 
     * @param <T> the type of the annotation
     * @param cls the class to be considered for querying the 
     *     <code>annotation</code>
     * @param annotation the type of the annotation to be searched for (meta 
     *     class)
     * @param remove if <code>true</code> remove the found annotation as a 
     *   side effect, <code>false</code> do not modify anything
     * @return the instance of the annotation if it is defined on 
     *     <code>method</code>, <b>null</b> otherwise
     * 
     * @since 1.00
     */
    public static final <T extends Annotation> T getAnnotation(
        CtClass cls, Class<T> annotation, boolean remove) {
        T result =  null;
        try {
            Object annotationObject = //cls.getAnnotation(annotation);
                getAnnotationObject(cls, annotation, remove);
            if (null != annotationObject) {
                result = annotation.cast(annotationObject);
            }
        } catch (ClassNotFoundException e) {
        } catch (ClassCastException e) {
            // should not happen, because then annotationObject should 
            // be null
        }
        return result;
    }
    
    // recursive queries
    
    /**
     * Returns the annotation of the specified <code>annotation</code> type
     * if it is defined for <code>member</code>.
     * 
     * @param <T> the type of the annotation
     * @param member the member to be considered for querying the 
     *     <code>annotation</code>
     * @param annotation the type of the annotation to be searched for (meta 
     *     class)
     * @param recurse <code>false</code> consider only <code>cl</code>,
     *   <code>true</code> consider also superclasses and interfaces
     * @return the instance of the annotation if it is defined on 
     *     <code>method</code>, <b>null</b> otherwise
     * 
     * @since 1.00
     */
    public static final <T extends Annotation> T getAnnotationRec(
        CtMember member, Class<T> annotation, boolean recurse) {
        T result =  null;
        try {
            Object annotationObject = member.getAnnotation(annotation);
            if (null != annotationObject) {
                result = annotation.cast(annotationObject);
            } else if (recurse) {
                CtClass parent = member.getDeclaringClass();
                result = getMemberAnnotation(member, parent.getSuperclass(), 
                    annotation);
                if (null == result) {
                    CtClass[] interfaces = parent.getInterfaces();
                    for (int i = 0; null == result 
                        && i < interfaces.length; i++) {
                        result = getMemberAnnotation(member, interfaces[i], 
                            annotation);
                    }
                }
            }
        } catch (ClassNotFoundException e) {
        } catch (NotFoundException e) {
        } catch (ClassCastException e) {
            // should not happen, because then annotationObject should be null
        }
        return result;
    }

    /**
     * Returns the annotation of the specified <code>annotation</code> type
     * if it is defined for a member on <code>cl</code> with identical 
     * signature as <code>member</code>.
     * 
     * @param <T> the type of the annotation
     * @param member the member to search for
     * @param cl search this class and also superclasses and interfaces
     * @param annotation the type of the annotation to be searched for (meta 
     *     class)
     * @return the instance of the annotation if it is defined on 
     *     <code>method</code>, <b>null</b> otherwise
     * 
     * @since 1.00
     */
    private static <T extends Annotation> T getMemberAnnotation(
        CtMember member, CtClass cl, Class<T> annotation) {
        T result = null;
        if (null != cl && member instanceof CtMethod) {
            try {
                CtMethod method = (CtMethod) member;
                CtClass[] param = method.getParameterTypes();
                CtMethod[] methods = cl.getDeclaredMethods();
                CtMethod found = null;
                for (int m = 0; null == found && m < methods.length; m++) {
                    if (method.getName().equals(methods[m].getName())) {
                        CtClass[] mParam = methods[m].getParameterTypes();
                        if (mParam.length == param.length) {
                            found = methods[m];
                            for (int p = 0; null != found 
                                && p < param.length; p++) {
                                if (!mParam[p].getName().equals(
                                    param[p].getName())) {
                                    found = null;
                                }
                            }
                        }
                    }
                }
                if (null != found) {
                    Object annotationObject = 
                        found.getAnnotation(annotation);
                    if (null != annotationObject) {
                        result = annotation.cast(annotationObject);
                    }  else {
                        CtClass parent = cl.getSuperclass();
                        result = getMemberAnnotation(member, parent, 
                            annotation);
                        if (null == result) {
                            CtClass[] interfaces = cl.getInterfaces();
                            for (int i = 0; null == result 
                                && i < interfaces.length; i++) {
                                result = getMemberAnnotation(member, 
                                    interfaces[i], annotation);
                            }
                        }
                    }
                }
            } catch (NotFoundException e) {
            } catch (ClassNotFoundException e) {
            }
        }
        return result;
    }

    /**
     * Returns the annotation of the specified <code>annotation</code> type
     * if it is defined for <code>cl</code>.
     * 
     * @param <T> the type of the annotation
     * @param cl the class to be considered for querying the 
     *     <code>annotation</code>
     * @param annotation the type of the annotation to be searched for (meta 
     *     class)
     * @param recurse <code>false</code> consider only <code>cl</code>,
     *   <code>true</code> consider also superclasses and interfaces
     * @return the instance of the annotation if it is defined on 
     *     <code>method</code>, <b>null</b> otherwise
     * 
     * @since 1.00
     */
    public static <T extends Annotation> T getAnnotationRec(
        CtClass cl, Class<T> annotation, boolean recurse) {
        T result =  null;
        if (null != cl) {
            try {
                Object annotationObject = cl.getAnnotation(annotation);
                if (null != annotationObject) {
                    result = annotation.cast(annotationObject);
                } else if (recurse) {
                    result = getAnnotationRec(cl.getSuperclass(), annotation, 
                        recurse);
                    if (null == result) {
                        CtClass[] interfaces = cl.getInterfaces();
                        for (int i = 0; null == result 
                            && i < interfaces.length; i++) {
                            result = getAnnotationRec(interfaces[i], 
                                annotation, recurse);
                        }
                    }
                }
            } catch (ClassNotFoundException e) {
            } catch (ClassCastException e) {
                // should not happen, because then annotationObject should be 
                // null
            } catch (NotFoundException e) {
                // should not happen
            }
        }
        return result;
    }
    
    // internal query emulation of javassist (for removal of annotations)
    
    /**
     * Checks for an annotation, returns the assigned annotation object and
     * removes the annotation if required.
     * 
     * @param attribute the attribute to be searched for the annotation
     * @param typeName the type name of the annotation to be returned
     * @param cp the current class pool (needed for type conversion)
     * @param remove if <code>true</code> remove the found annotation as a 
     *   side effect, <code>false</code> do not modify anything
     * 
     * @return the attached annotation or <b>null</b> if none of the given
     *   type is attached
     * @throws ClassNotFoundException in case that the target class for casting
     *   cannot be found
     * 
     * @since 1.00
     */
    private static Object checkAnnotation(AnnotationsAttribute attribute, 
        String typeName, javassist.ClassPool cp, boolean remove)
        throws ClassNotFoundException {
        Object result = null;
        if (attribute != null) {
            javassist.bytecode.annotation.Annotation[] annotations 
                = attribute.getAnnotations();
            if (annotations != null) {
                for (int i = 0; i < annotations.length; i++) {
                    if (annotations[i].getTypeName().equals(typeName)) {
                        result = toAnnoType(annotations[i], cp);
                        if (remove) {
                            javassist.bytecode.annotation.Annotation[] tmp 
                                = new javassist.bytecode.annotation.Annotation[
                                    annotations.length - 1];
                            int pos = 0;
                            for (int j = 0; j < annotations.length; j++) {
                                if (i != j) {
                                    tmp[pos++] = annotations[j];
                                }
                            }
                            attribute.setAnnotations(tmp);
                        }
                    }
                }
            }
        }
        return result;
    }

    /**
     * Converts the annotation to an annotation object.
     * 
     * @param anno the annotation to be converted
     * @param cp the class pool needed for conversion
     * @return the converted type
     * @throws ClassNotFoundException in case that the target class for casting
     *   cannot be found
     * 
     * @since 1.00
     */
    private static Object toAnnoType(
        javassist.bytecode.annotation.Annotation anno, javassist.ClassPool cp)
        throws ClassNotFoundException {
        try {
            ClassLoader cl = cp.getClassLoader();
            return anno.toAnnotationType(cl, cp);
        } catch (ClassNotFoundException e) {
            ClassLoader cl2 = cp.getClass().getClassLoader();
            return anno.toAnnotationType(cl2, cp);
        }
    }

}
