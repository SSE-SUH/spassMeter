package de.uni_hildesheim.sse.codeEraser.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Changes the value of a variable. Does currently not work for constants.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
@Retention(RetentionPolicy.CLASS)
@Target(value = {ElementType.FIELD })
public @interface SetValue {

    /**
     * At least one id this variability reacts on.
     * 
     * @since 1.00
     */
    public String id();

}
