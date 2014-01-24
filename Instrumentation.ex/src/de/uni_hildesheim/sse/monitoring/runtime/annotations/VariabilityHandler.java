package de.uni_hildesheim.sse.monitoring.runtime.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Denotes a variability handler method in which no configuration changes 
 * should be detected automatically.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.METHOD })
@InConfiguration
public @interface VariabilityHandler {

}
