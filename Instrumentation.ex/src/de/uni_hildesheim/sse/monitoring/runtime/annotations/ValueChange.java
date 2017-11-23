package de.uni_hildesheim.sse.monitoring.runtime.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks fields for which writing field access should be registered by the 
 * recorder.
 * 
 * @see ValueContext
 * 
 * @author eichelberger
 * @since 1.00
 * @version 1.00
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.FIELD })
@InConfiguration
public @interface ValueChange {

    /**
     * Defines an identification for this field. May end with @ in order to
     * denote a variability (value is appended). In case of "*" the id is
     * determined by the innermost {@link ValueContext}.
     * 
     * @return the identification
     */
    public String id();

}
