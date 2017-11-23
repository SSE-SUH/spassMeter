package de.uni_hildesheim.sse.monitoring.runtime.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks the end of the system (to be instrumented at the end of the annotated 
 * method). This might be the main method or in case of GUI programs a common
 * end method. Leads to stopping the recorder.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.METHOD, ElementType.CONSTRUCTOR })
@InConfiguration
public @interface EndSystem {

    /**
     * Specifies a method to be invoked at the end of monitoring.
     *
     * @return the command to invoke, none if empty
     * 
     * @since 1.00
     */
    String invoke() default "";
}
