package test;

import de.uni_hildesheim.sse.monitoring.runtime.annotations.Monitor;

/**
 * An interface for testing annotations at interfaces.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
@Monitor(id = AnnotationId.ID_INTERFACE)
public interface IInterfaceTest {

    /**
     * A method which executes the test.
     * 
     * @since 1.00
     */
    public void execute();
    
}
