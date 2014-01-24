package de.uni_hildesheim.sse.monitoring.runtime.configuration.xml;

import java.lang.annotation.Annotation;

import de.uni_hildesheim.sse.monitoring.runtime.configuration.
    IAnnotationBuilder;
import de.uni_hildesheim.sse.monitoring.runtime.utils.HashMap;

/**
 * Defines the type of a specialized map for annotation builders.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
class AnnotationBuilderMap extends HashMap<
    Class<? extends Annotation>, IAnnotationBuilder<?>> {
    
}
