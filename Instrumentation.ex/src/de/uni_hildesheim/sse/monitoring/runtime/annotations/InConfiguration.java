package de.uni_hildesheim.sse.monitoring.runtime.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Defines a meta-annotation for marking annotations to be considered in 
 * external configurations.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
@Target(value = ElementType.ANNOTATION_TYPE)
public @interface InConfiguration {

}
