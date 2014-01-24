package test;

import de.uni_hildesheim.sse.codeEraser.annotations.Variability;

/**
 * A test for removing an entire class (and a method within).
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
@Variability(id = "mem")
public class IntermediaryClass extends SuperClass {

    /**
     * To be removed if "mem2" is not set.
     * 
     * @return <code>false</code> always
     * 
     * @since 1.00
     */
    @Variability(id = "mem2")
    public boolean isEnabled() {
        return false;
    }
}
