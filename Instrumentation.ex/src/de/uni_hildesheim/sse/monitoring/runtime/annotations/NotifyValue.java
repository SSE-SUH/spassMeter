package de.uni_hildesheim.sse.monitoring.runtime.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Notify about a value to be considered as monitored value, e.g. in case of 
 * commonly used structures or protocols. Currenty, this annotation can
 * be used in four different ways:
 * <ol>
 *   <li>Notify about a value at the beginning of a method 
 *       ({@link #notifyDifference()} is false in this case). The value
 *       is determined by the expression in {@link #expression()} and assigned 
 *       to {@link #value()}. {@link #id()} may be given or the 
 *       surrounding group id is used.</li>
 *   <li>Notify about a difference of a value taken at the beginning
 *       of a method (determined by the expression in {@link #expression()} and 
 *       assigned to {@link #value()}) and at the end of the method. 
 *       ({@link #notifyDifference()} is true 
 *       in this case). {@link #id()} may be given or the surrounding 
 *       group id is used.</li>
 *   <li>Notify about a value change in a library object, e.g. an 
 *       IncrementalAverageCalculator defined in a jar library which cannot 
 *       accessed as source modifies a value - then <code>expression</code>
 *       may notify the new value to the recorder. Id may be the direct id or
 *       be taken from {@link ValueContext}. In this case 
 *       {@link #notifyDifference()} is ignored.</li>
 *   <li>Assign all values to {@link #id()} if {@link #value()} is 
 *       {@link MeasurementValue#ALL}. In this case, {@link #id()} may refer
 *       to a concrete id, the variability prefix or the group id is taken
 *       if the id is not given. {@link #notifyDifference()} and 
 *       {@link #expression()} are ignored.</li>
 * </ol>
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.METHOD, ElementType.CONSTRUCTOR })
@InConfiguration
public @interface NotifyValue {
    
    /**
     * The monitoring group to be notified.
     * 
     * @return the group identification
     * @see Monitor
     * 
     * @since 1.00
     */
    String id() default "";
    
    /**
     * An expression determining the value to be notified. Usually a static
     * expression on parameters of the method, denoted as $1, ..., $n with $0 as
     * this for instance methods nor null for static methods. Currently, we 
     * require only method calls to be supported - calculations can be done
     * in static methods being excluded from monitoring.
     * 
     * @return return the expression determining the value
     * 
     * @since 1.00
     */
    String expression();
    
    /**
     * The value to be modified.
     * 
     * @return the value/resource
     * 
     * @since 1.00
     */
    MeasurementValue value();
    
    /**
     * An optional expression which is used to determine the tag for the memory
     * allocation. This should point to the object being allocated or 
     * unallocated. Only effective in combination with 
     * {@link MeasurementValue#MEM_ALLOCATED} or 
     * {@link MeasurementValue#MEM_UNALLOCATED}.
     * 
     * @return the memory tag, none if empty (default)
     * 
     * @since 1.00
     */
    String tagExpression() default "";
    
    /**
     * Notify the value itself or the difference between entering the method
     * and exiting it.
     * 
     * @return <code>true</code> for difference, <code>false</code> else (default)
     * 
     * @since 1.00
     */
    boolean notifyDifference() default false;
    
    /**
     * Currently unused.
     * 
     * @since 1.00
     */
//    String onType() default "";

}
