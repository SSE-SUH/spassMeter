package de.uni_hildesheim.sse.functions;

/**
 * Defines an interface for the methods a services must offer. 
 * 
 * @author Stephan Dederichs
 * 
 * @since 1.00
 * @version 1.00
 */
public interface IServiceData {

    /**
     * Method for getting a specific attributes.
     * 
     * @param attributeName The name of the attribute.
     * 
     * @return the attribute or <code>null</code> if there is no attribute 
     *            with the given name.
     * 
     * @since 1.00
     */
    public Object getSpecificAttribute(String attributeName);
    
}
