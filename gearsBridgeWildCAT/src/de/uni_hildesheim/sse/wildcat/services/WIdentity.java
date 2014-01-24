package de.uni_hildesheim.sse.wildcat.services;

import de.uni_hildesheim.sse.functions.IIdentity;



/**
 * Defines a class for holding together a function and an attribute of a class.
 * 
 * @author Stephan Dederichs
 * 
 * @since 1.00
 * @version 1.00
 */
public class WIdentity implements IIdentity {

    /**
     * Stores the instance of the class from the attribute.
     * 
     * @since 1.00
     */
    private AbstractWServiceData cls;

    /**
     * Stores the name of the attribute.
     * 
     * @since 1.00
     */
    private String attributeName;

    /**
     * Stores the type of the attribute in a String representation.
     * 
     * @since 1.00
     */
    private String attributeType;

    /**
     * Stores a description for the attribute. It is displayed in the management
     * application.
     * 
     * @since 1.00
     */
    private String description;

    /**
     * Creates a new Identity for a class and an attribute.
     * 
     * @param cls Instance of the attribute class.
     * @param attributeName Name of the attribute.
     * @param attributeType The type of the attribute in a String 
     *            representation (e.g. "int" or "java.lang.String").
     * @param description A description for the attribute. It is displayed in 
     *            the management application.
     * 
     * @since 1.00
     */
    public WIdentity(AbstractWServiceData cls, String attributeName,
            String attributeType, String description) {
        this.cls = cls;
        this.attributeName = attributeName;
        this.attributeType = attributeType;
        this.description = description;
    }

    /**
     * Returns the instance of the attribute class.
     * 
     * @return The instance of the attribute class.
     * 
     * @since 1.00
     */
    public AbstractWServiceData getCls() {
        return cls;
    }

    /**
     * Returns the name of the attribute.
     * 
     * @return The name of the attribute.
     * 
     * @since 1.00
     */
    public String getAttributeName() {
        return attributeName;
    }

    /**
     * Returns the type of the attribute in a String representation.
     * 
     * @return The type of the attribute in a String representation.
     * 
     * @since 1.00
     */
    public String getAttributeType() {
        return attributeType;
    }

    /**
     * Returns a description for the attribute. It is displayed in the
     * management application.
     * 
     * @return the description for the attribute.
     * 
     * @since 1.00
     */
    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return cls.getClass().getSimpleName() + ", attribute name: "
                + attributeName + ", attribute type: " + attributeType
                + ", description: " + description;
    }

}
