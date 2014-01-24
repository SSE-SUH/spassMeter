package de.uni_hildesheim.sse.monitoring.runtime.annotations.internal;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to mark dynamically instrumented methods. 
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.METHOD, ElementType.CONSTRUCTOR })
public @interface MethodInstrumented {

    /**
     * Stores the number of instrumentation runs which currently require
     * the instrumentation for memory resources.
     * 
     * @since 1.00
     */
    public int mem() default 0;
    
}
