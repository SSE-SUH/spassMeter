package de.uni_hildesheim.sse.codeEraser.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a variable part in code. {@link #value()} may specify an explicit value
 * to replace the class, method or attribute.
 * <ul>
 *  <li>For class: value contains the fully qualified name of a replacement 
 *      class. Please note, that then at least the constructors with compatible
 *      types of the class to be replaced must be present in the replacing
 *      class.</li>
 *  <li>For attribute: value contains an (accessible) expression determining 
 *      the replacement value</li>
 *  <li>For method: value contains an (accessible) expression determining the 
 *      replacement value</li>
 *  <li>For constructor: value is ignored</li>
 * </ul>
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
@Retention(RetentionPolicy.CLASS)
@Target(value = {ElementType.TYPE, ElementType.CONSTRUCTOR, 
    ElementType.METHOD, ElementType.FIELD })
public @interface Variability {
    
    /**
     * At least one id this variability reacts on.
     * 
     * @since 1.00
     */
    public String[] id();

    /**
     * Remove code if (combination of) id is <code>false</code>;
     * default is <code>true</code>.
     * 
     * @since 1.00
     */
    public boolean removeIfDisabled() default true;

    /**
     * Combination operation, default {@link Operation#AND}.
     * 
     * @since 1.00
     */
    public Operation op() default Operation.AND;
    
    /**
     * Stores the default value for replacement.
     * 
     * @since 1.00
     */
    public String value() default "";

}
