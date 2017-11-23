package de.uni_hildesheim.sse.monitoring.runtime.configuration.xml;


/**
 * Defines a type resolver as a binding between 
 * annotations and the low-level instrumenter (with type 
 * resolution).
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public interface IResolver {

    /**
     * Returns if the given <code>type</code> is equal or a subclass of to the 
     * class object represented by <code>class</code>. We use Object instead of 
     * <code>java.lang.Class</code> here, because we do not know the concrete
     * type used by the instrumenter.
     * 
     * @param cls the class object
     * @param type the type to be checked for
     * @return <code>true</code> if <code>type</code> is of the type of 
     *     <code>cls</code> or a subclass of <code>cls</code>
     * 
     * @since 1.00
     */
    public boolean isInstanceOf(Object cls, String type);
    
}
