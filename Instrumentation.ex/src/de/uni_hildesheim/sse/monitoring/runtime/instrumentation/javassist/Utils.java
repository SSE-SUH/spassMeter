package de.uni_hildesheim.sse.monitoring.runtime.instrumentation.javassist;

import de.uni_hildesheim.sse.monitoring.runtime.instrumentation.Logger;
import de.uni_hildesheim.sse.monitoring.runtime.instrumentation.lib.
    InstrumenterException;
import javassist.CannotCompileException;
import javassist.NotFoundException;

/**
 * Some utility methods.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public class Utils {

    /**
     * Prevents external creation of instances.
     * 
     * @since 1.00
     */
    private Utils() {
    }
    
    /**
     * Warns for a Java element which cannot be found.
     * 
     * @param exception the exception
     * 
     * @since 1.00
     */
    public static void warn(NotFoundException exception) {
        Logger.LOG.warning(exception.getMessage());
    }

    /**
     * Warns if the causing exception is a NotFoundException and turns this 
     * <code>exception</code> into an instance of {@link InstrumenterException}.
     * 
     * @param exception the exception to be handled
     * @return the converted exception
     * 
     * @since 1.00
     */
    public static InstrumenterException warnOrConvert(
        CannotCompileException exception) {
        boolean handled = false;
        if (exception.getCause() instanceof NotFoundException) {
            warn((NotFoundException) exception.getCause());
            handled = true;
        } 
        return new InstrumenterException(exception, handled);    
    }

}
