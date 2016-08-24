package de.uni_hildesheim.sse.monitoring.runtime.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import de.uni_hildesheim.sse.monitoring.runtime.boot.BooleanValue;
import de.uni_hildesheim.sse.monitoring.runtime.boot.DebugState;
import de.uni_hildesheim.sse.monitoring.runtime.boot.GroupAccountingType;
import de.uni_hildesheim.sse.monitoring.runtime.boot.InstanceIdentifierKind;
import de.uni_hildesheim.sse.monitoring.runtime.boot.ResourceType;

/**
 * The central annotation. Mark a class, method or constructor with this 
 * annotation in order to monitor and aggregate values to it.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.20
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.TYPE, ElementType.METHOD, 
    ElementType.CONSTRUCTOR, ElementType.PACKAGE })
@InConfiguration
public @interface Monitor {

    /**
     * Contains arbitrary ids for monitoring and grouping. If none is given, 
     * the class name is used. If given, classes with same id are recorded as 
     * one group. If multiple are given, resources are accounted to the 
     * multiple groups. If given in form <i>name1@name2</i> <i>name1</i> is 
     * interpreted as identifier of a variability, <i>name2</i> as name of a 
     * variant implementing the variability.
     * 
     * @since 1.00
     */
    public String[] id() default "";
           
    /**
     * Define any combination of debug states for additional information to
     * be emitted during monitoring.
     * 
     * @since 1.00
     */
    public DebugState[] debug() default { };
    
    /**
     * Define the group accounting to be applied. By default this value is set
     * to {@link GroupAccountingType#DEFAULT} and, therefore, taken from the 
     * global configuration.
     * 
     * @since 1.00
     */
    public GroupAccountingType groupAccounting() 
        default GroupAccountingType.DEFAULT;
    
    /**
     * Defines the resources to be accounted. By default, all resources defined
     * as default in the configuration shall be accounted.
     * 
     * @since 1.00
     */
    public ResourceType[] resources() default { };

    /**
     * Returns whether values in multi groups should be distributed evenly to
     * the contained values or whether the entire value should be added to each
     * group.
     * 
     * @since 1.00
     */
    public BooleanValue distributeValues() default BooleanValue.DEFAULT;

    /**
     * Returns whether accountable resources of the multi monitoring group
     * is authoritative (<code>false</code>) or weather the contained groups 
     * should be considered (<code>true</code>).
     * 
     * @since 1.00
     */
    public BooleanValue considerContained() default BooleanValue.DEFAULT;

    /**
     * Returns the instance identifier to be used for more detailed monitoring.
     * 
     * @since 1.20
     */
    public InstanceIdentifierKind instanceIdentifierKind() default InstanceIdentifierKind.NONE;
    // DEFAULT works only with JDK 1.8

}
