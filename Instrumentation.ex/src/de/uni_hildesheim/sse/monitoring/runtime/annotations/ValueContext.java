package de.uni_hildesheim.sse.monitoring.runtime.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks the context for reusable fields for which writing field access should 
 * be registered by the recorder. This annotation defines the context, i.e. the 
 * id to be used, {@link ValueChange} should use "*" as id.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.FIELD })
@InConfiguration
public @interface ValueContext {

    /**
     * Defines an identification for this field. May end with @ in order to
     * denote a variability (value is appended).
     */
    public String id();

}
