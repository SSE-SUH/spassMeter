package de.uni_hildesheim.sse.functions;

/**
 * Defines an interface for holding together a function and an attribute 
 * of a class.
 * 
 * @author Stephan Dederichs
 * 
 * @since 1.00
 * @version 1.00
 */
public interface IIdentity {

    /**
     * Returns the instance of the attribute class.
     * 
     * @return The instance of the attribute class.
     * 
     * @since 1.00
     */
    public IServiceData getCls();

    /**
     * Returns the name of the attribute.
     * 
     * @return The name of the attribute.
     * 
     * @since 1.00
     */
    public String getAttributeName();

    /**
     * Returns the type of the attribute in a String representation.
     * 
     * @return The type of the attribute in a String representation.
     * 
     * @since 1.00
     */
    public String getAttributeType();

    /**
     * Returns a description for the attribute. It is displayed in the
     * management application.
     * 
     * @return the description for the attribute.
     * 
     * @since 1.00
     */
    public String getDescription();

    /**
     * Returns a string representation of the identity.
     * 
     * @return A string representation of the identity.
     * 
     * @see Object#toString()
     * 
     * @since 1.00
     */
    @Override
    public String toString();

}
