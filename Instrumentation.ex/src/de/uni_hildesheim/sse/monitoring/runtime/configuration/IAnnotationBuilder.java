package de.uni_hildesheim.sse.monitoring.runtime.configuration;

import java.lang.annotation.Annotation;

import de.uni_hildesheim.sse.codeEraser.annotations.Variability;
import de.uni_hildesheim.sse.monitoring.runtime.AnnotationConstants;

/**
 * Defines the public interface of classes which are able to create
 * annotations on demand (from a description in XML).
 * 
 * @param <T> the type of the annotation
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
@Variability(id = AnnotationConstants.CONFIG_XML)
public interface IAnnotationBuilder<T extends Annotation> {
    
    /**
     * Creates the annotation.
     * 
     * @return the annotation
     * 
     * @since 1.00
     */
    public T create();
    
    /**
     * Returns the concrete type of the annotation.
     * 
     * @return the concrete type
     * 
     * @since 1.00
     */
    public Class<T> getInstanceClass();

}
