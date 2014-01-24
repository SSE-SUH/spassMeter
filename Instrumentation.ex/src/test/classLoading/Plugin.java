package test.classLoading;

import test.AnnotationId;
import de.uni_hildesheim.sse.codeEraser.annotations.Variability;

/**
 * An interface for dynamically loaded classes (in order to invoke something 
 * on).
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
@Variability(id = AnnotationId.VAR_TESTING)
public interface Plugin {
    
    /**
     * Just execute some testing code.
     * 
     * @since 1.00
     */
    public void doit();

}
