package de.uni_hildesheim.sse.monitoring.runtime.configuration;

import java.lang.reflect.Method;

import de.uni_hildesheim.sse.codeEraser.annotations.Variability;
import de.uni_hildesheim.sse.monitoring.runtime.AnnotationConstants;

/**
 * Defines an attribute of an annotation.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
@Variability(id = AnnotationConstants.CONFIG_XML)
public class AttributeMetaData {
    
    /**
     * Defines the name of the attribute, i.e. the name of the value in the 
     * XML file and the related "method" of the annotation.
     */
    private String name;
    
    /**
     * Specifies the default value (to be automatically synchronized in future
     * with the annotations). 
     */
    private Object deflt;
    
    /**
     * Stores the type of the attribute.
     */
    private Class<?> type;
    
    /**
     * Creates a metadata object from a method.
     * 
     * @param method the method to extract the metadata from
     * 
     * @since 1.00
     */
    public AttributeMetaData(Method method) {
        this(method.getName(), method.getDefaultValue(), 
            method.getReturnType());
    }
    
    /**
     * Creates an attribute metadata object.
     * 
     * @param name the name of the attribute
     * @param deflt the default value (<b>null</b> if none is defined)
     * @param type its type
     * 
     * @since 1.00
     */
    public AttributeMetaData(String name, Object deflt, Class<?> type) {
        this.name = name;
        this.deflt = deflt;
        this.type = type;
        
        // normalize type
        if (type == Boolean.TYPE) {
            this.type = Boolean.class;
        } else if (type == Integer.TYPE) {
            this.type = Integer.class;
        } else if (type == Long.TYPE) {
            this.type = Long.class;
        } else if (type == Double.TYPE) {
            this.type = Double.class;
        } else if (type == Float.TYPE) {
            this.type = Float.class;
        } else if (type == Character.TYPE) {
            this.type = Character.class;
        } else if (type == Short.TYPE) {
            this.type = Short.class;
        } else if (type == Byte.TYPE) {
            this.type = Byte.class;
        }
    }
    
    /**
     * Returns the name of the attribute / element.
     * 
     * @return the key
     */
    public String getName() {
        return name;
    }
    
    /**
     * Returns the default value of the attribute / element.
     * 
     * @return the default value (<b>null</b> if there is none)
     */
    public Object getDefault() {
        return deflt;
    }
    
    /**
     * Returns the type of the attribute / element.
     * 
     * @return the type
     */
    public Class<?> getType() {
        return type;
    }
    
    /**
     * Returns of this attribute is nested into non-registered elements.
     * 
     * @return currently <code>false</code> as default
     * 
     * @since 1.00
     */
    public boolean isNested() {
        return false;
    }
    
    /**
     * Returns a textual description.
     * 
     * @return the textual description
     */
    public String toString() {
        return name + " " + type.getName() + " " + deflt;
    }
    
}