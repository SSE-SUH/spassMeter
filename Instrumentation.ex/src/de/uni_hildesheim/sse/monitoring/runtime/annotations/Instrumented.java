package de.uni_hildesheim.sse.monitoring.runtime.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * May be used to mark a class which was instrumented. Intended, to mark 
 * classes processed by static pre-instrumentation, i.e. to avoid double 
 * instrumentation in case of mixed monitoring (some classes instrumented
 * statically, some at runtime).<br/>
 * This annotation shall be moved to <code>de.uni_hildesheim.sse
 * .monitoring.runtime.annotations.internal</code>.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.TYPE })
public @interface Instrumented {
    // TODO move to internal, requires static reinstrumentation
}
