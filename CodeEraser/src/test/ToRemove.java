package test;

/**
 * A class to be removed via the settings in {@link IntermediaryClass}.
 * This works only, if the processor may consider (non-flat) values.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public class ToRemove extends IntermediaryClass {

    /**
     * An attribute to be returned (overriding the method in the superclass).
     */
    private boolean enable = true;
    
    /**
     * Creates a new instance.
     * 
     * @since 1.00
     */
    public ToRemove() {
    }

    /**
     * To be removed if "mem2" is not set (via {@link IntermediaryClass} and
     * non-flat interpretation of annotations).
     * 
     * @return <code>false</code> always
     * 
     * @since 1.00
     */
    public boolean isEnabled() {
        return enable;
    }
    
}
