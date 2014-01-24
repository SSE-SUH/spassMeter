package de.uni_hildesheim.sse.jmx.services;

import de.uni_hildesheim.sse.functions.IIdentity;
import de.uni_hildesheim.sse.functions.IServiceData;


/**
 * Defines a class for holding together a function and an attribute of a class.
 * 
 * @author Stephan Dederichs
 * 
 * @since 1.00
 * @version 1.00
 */
public class JMXIdentity implements IIdentity {

    /**
     * Stores the instance of the class from the attribute.
     * 
     * @since 1.00
     */
    private IServiceData cls;

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
     * @param attributeType The type of the attribute in a String representation
     *            (e.g. "int" or "java.lang.String").
     * @param description A description for the attribute. It is displayed in 
     *            the management application.
     * 
     * @since 1.00
     */
    public JMXIdentity(IServiceData cls, String attributeName,
            String attributeType, String description) {
        this.cls = cls;
        this.attributeName = attributeName;
        this.attributeType = attributeType;
        this.description = description;
    }

    
    @Override
    public IServiceData getCls() {
        return cls;
    }

    
    @Override
    public String getAttributeName() {
        return attributeName;
    }

    @Override
    public String getAttributeType() {
        return attributeType;
    }

    @Override
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
