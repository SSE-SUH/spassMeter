package de.uni_hildesheim.sse.monitoring.runtime.utils;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import de.uni_hildesheim.sse.monitoring.runtime.plugins.internal.Cleanup;

/**
 * Provide an instance of an annotation with member values.
 * 
 * @author Stuart Douglas (taken from JBoss WELD SVN rev 5138)
 * @author Pete Muir (taken from JBoss WELD SVN rev 5138)
 */
public class AnnotationInstanceProvider implements Cleanup {

    /**
     * Stores the reference to the dynamic annotation instance provider.
     */
    public static final AnnotationInstanceProvider INSTANCE 
        = new AnnotationInstanceProvider();
    
    /**
     * Defines an error message to be emitted in case of problems during 
     * instantiation. Append annotation type and concrete error message.
     */
    private static final String INSTANTIATION_ERROR = 
        "Error instantiating proxy for annotation. Annotation type: ";
    
    /**
     * Stores the annotation cache.
     */
    protected final ConcurrentMap<Class<?>, Class<?>> cache;
   
    /**
     * Creates a new instance provider and initializes the {@link #cache}.
     * 
     * @since 1.00
     */
    public AnnotationInstanceProvider() {
        cache = new ConcurrentHashMap<Class<?>, Class<?>>();
    }

   /**
    * Returns an instance of the given annotation type with member values
    * specified in the map.
    * 
    * @param <T> the concrete type of the annotation (resolved automatically)
    * @param annotation the annotation to return an instance for
    * @param values the individual values for the annotation
    * @return the instance of <code>annotation</code> representing the 
    *     <code>values</code>
    */
    public <T extends Annotation> T get(Class<T> annotation, HashMap<String, 
        Object> values) {
        if (annotation == null) {
            throw new IllegalArgumentException("Must specify an annotation");
        }
        Class<?> clazz = cache.get(annotation);
        // Not safe against data race, but doesn't matter, we can recompute and
        // get the same value
        if (clazz == null) {
            // create the proxy class - we keep this for Java 8 compliance
            clazz = Proxy.getProxyClass(annotation.getClassLoader(), 
                annotation, Serializable.class);
            cache.put(annotation, clazz);
        }
        AnnotationInvocationHandler handler = new AnnotationInvocationHandler(
            values, annotation);
        // create a new instance by obtaining the constructor via reflection
        try {
            return annotation.cast(clazz.getConstructor(
                new Class[] { InvocationHandler.class }).newInstance(
                    new Object[] { handler }));
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException(INSTANTIATION_ERROR 
                + annotation, e);
        } catch (InstantiationException e) {
            throw new IllegalStateException(INSTANTIATION_ERROR
                + annotation, e);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(INSTANTIATION_ERROR
                + annotation, e);
        } catch (InvocationTargetException e) {
            throw new IllegalStateException(INSTANTIATION_ERROR 
                + annotation, e.getCause());
        } catch (SecurityException e) {
            throw new IllegalStateException(INSTANTIATION_ERROR
                + annotation, e);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(INSTANTIATION_ERROR
                + annotation, e);
        }
    }
    
    /**
     * Clears temporary data.
     * 
     * @since 1.00
     */
    public void cleanup() {
        cache.clear();
    }
    
    /**
     * Do memory cleanup if required.
     * 
     * @since 1.00
     */
    public void cleanupIfRequired() {
        cache.clear();
    }
    
}
