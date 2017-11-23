package de.uni_hildesheim.sse.monitoring.runtime.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks the start of the system (to be instrumented at the beginning of the 
 * annotated method). This might be the main method or in case of GUI programs 
 * a common end method. Leads to starting the recorder.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.METHOD, ElementType.CONSTRUCTOR })
@InConfiguration
public @interface StartSystem {

    /**
     * Ignored.
     * 
     * @return ignored
     * 
     * @since 1.00
     */
    int statisticsInterval() default 0;

    /**
     * Ignored.
     * 
     * @return ignored
     * 
     * @since 1.00
     */
    boolean swingTimer() default false;
    
    /**
     * Specifies whether a shutdown hook should be inserted instead of 
     * {@link EndSystem}. It is recommended to use {@link EndSystem} instead
     * as this may lead to locking the JVM at the end, particularly when waiting
     * for events to be processed.
     * 
     * @return <code>true</code> insert a shutdown hook, <code>false</code> else
     * 
     * @since 1.00
     */
    boolean shutdownHook() default false;
    
    /**
     * Specifies a method to be invoked at the end of monitoring in case
     * of a shutdown hook.
     * 
     * @return the method to be invoked, none if empty
     * 
     * @since 1.00
     */
    String invoke() default "";
}
