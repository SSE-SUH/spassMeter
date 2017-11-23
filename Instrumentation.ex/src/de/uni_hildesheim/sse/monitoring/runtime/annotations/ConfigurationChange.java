package de.uni_hildesheim.sse.monitoring.runtime.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotates a method or constructor which is responsible for executing a 
 * configuration change. Configuration changes may be detected automatically
 * (see agent configuration) or defined at a certain point of execution (which
 * usually adds more precision to the monitoring results. If automatic 
 * configuration detection is deactivated, this annotation deactivates 
 * {@link VariabilityHandler} on the same constructor or method.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.METHOD, ElementType.CONSTRUCTOR })
@InConfiguration
public @interface ConfigurationChange {

    /**
     * A fixed id denoting the new configuration. If not empty, it overrides
     * the {@link #valueExpression()}.
     * 
     * @return the id expression, none if empty
     * 
     * @since 1.00
     */
    public String idExpression() default "";
    
    /**
     * A programming language expression which determines the configuration. The
     * parameters are denoted as $1, ..., $n with $0 as
     * this for instance methods nor null for static methods. Currently, we 
     * require only method calls to be supported - calculations can be done
     * in static methods being excluded from monitoring.
     * 
     * @return the value expression
     * 
     * @since 1.00
     */
    public String valueExpression();
    
}
