package de.uni_hildesheim.sse.monitoring.runtime.configuration;

import java.lang.annotation.Annotation;

import de.uni_hildesheim.sse.monitoring.runtime.annotations.Registration;
import de.uni_hildesheim.sse.monitoring.runtime.utils.HashMap;

/**
 * Tooling for building initialized annotations.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public class Annotations {
   
    /**
     * Stores the template instances assigned to XML element names.
     */
    private static HashMap<String, AnnotationBuilder<?>> templates =
        new HashMap<String, AnnotationBuilder<?>>();

    /**
     * Prevents this class from being initialized from outside.
     * 
     * @since 1.00
     */
    private Annotations() {
    }
    
    /**
     * Registers a concrete annotation for reading.
     * 
     * @param <T> the type of the annotation (inferred)
     * @param cls the annotation class
     * @param meta optional meta data overriding the automatically extracted 
     *   data
     * 
     * @since 1.00
     */
    public static final <T extends Annotation> void register(Class<T> cls, 
        AttributeMetaData... meta) {
        String key = cls.getSimpleName();
        // transform to XML name
        key = Character.toLowerCase(key.charAt(0)) 
            + key.substring(1, key.length());
        AnnotationBuilder<T> builder = new AnnotationBuilder<T>(cls, meta);
        templates.put(key, builder);
        templates.put(cls.getName(), builder);
    }

    /**
     * Returns an annotation builder template for the given XML element name.
     * 
     * @param name the XML element name
     * @return the annotation builder or <b>null</b> if none is registered for 
     *     <code>name</code>
     * 
     * @since 1.00
     */
    public static AnnotationBuilder<?> getTemplate(String name) {
        return templates.get(name);
    }
    
    /***
     * Calls the annotation registration class in order to obtain the default
     * annotation classes.
     */
    static {
        Registration.register2XML();
    }

}
