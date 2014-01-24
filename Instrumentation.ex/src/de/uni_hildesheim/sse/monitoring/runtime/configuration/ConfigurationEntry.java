package de.uni_hildesheim.sse.monitoring.runtime.configuration;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Defines a generic configuration entry.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public class ConfigurationEntry {

    /**
     * Defines the supported types of configuration entries.
     * 
     * @author Holger Eichelberger
     * @since 1.00
     * @version 1.00
     */
    public enum Type {

        /**
         * The entry is a string.
         */
        STRING(false),
        
        /**
         * The entry is an integer.
         */
        INTEGER(false),

        /**
         * The entry is a boolean.
         */        
        BOOLEAN(false),
        
        /**
         * An enum value (requires additional information).
         */
        ENUM(false),
        
        /**
         * A 1-dimensional array (requires additional information).
         */
        ARRAY_ENUM(true, ENUM);
        
        /**
         * Stores whether this type is an array.
         */
        private boolean array;
        
        /**
         * Stores the element type.
         */
        private Type elementType;
        
        /**
         * Creates a new constant. The element type is this instance.
         * 
         * @param array whether this type is an array
         * 
         * @since 1.00
         */
        private Type(boolean array) {
            this.array = array;
            this.elementType = this;
        }
        
        /**
         * Creates a new constant.
         * 
         * @param array whether this type is an array
         * @param elementType the type of contained elements
         * 
         * @since 1.00
         */
        private Type(boolean array, Type elementType) {
            this.array = array;
            this.elementType = elementType;
        }
        
        /**
         * Returns whether this type is an array.
         * 
         * @return <code>true</code> if it is an array, <code>false</code> else
         * 
         * @since 1.00
         */
        public boolean isArray() {
            return array;
        }
        
        /**
         * Returns whether this type is primitive.
         * 
         * @return <code>true</code> if it is an array, <code>false</code> else
         * 
         * @since 1.00
         */
        public boolean isPrimitive() {
            return !array;
        }
        
        /**
         * Returns the element type.
         * 
         * @return the element type (may be this instance in case of 
         *    {@link #isPrimitive})
         */
        public Type getElementType() {
            return elementType; 
        }
    }
    
    /**
     * Stores the configuration entries.
     */
    private static final Map<String, ConfigurationEntry> ENTRIES 
        = new HashMap<String, ConfigurationEntry>();

    /**
     * Stores the type of this configuration entry.
     */
    private Type type;
    
    /**
     * Stores the field representing the attribute of this configuration entry.
     */
    private Field field;
    
    /**
     * Stores the name of the entry.
     */
    private String name;

    /**
     * Stores additional information characterizing {@link #type}.
     */
    private Class<?> elemType;
    
    /**
     * Creates a new configuration entry.
     * 
     * @param name the name of the entry
     * @param fieldName the name of the field where to store the value to
     * @param type the type of the entry
     * @param elemType additional type information when <code>type</code> 
     *   is {@link Type#ENUM} or {@link Type#ARRAY_ENUM}
     * 
     * @throws IllegalArgumentException in case that <code>name</code> is 
     *     <b>null</b>, <code>type</code> is <b>null</b> or that the related
     *     attribute in the configuration does not exist.
     * 
     * @since 1.00
     */
    private ConfigurationEntry(String name, String fieldName, Type type, 
        Class<?> elemType) {
        if (null == fieldName) {
            throw new IllegalArgumentException("'fieldName' must not be null");
        }
        if (null == name) {
            throw new IllegalArgumentException("'name' must not be null");
        }
        if (null == type) {
            throw new IllegalArgumentException("'type' must not be null");
        }
        try {
            this.field = Configuration.class.getDeclaredField(fieldName);
            this.field.setAccessible(true);
        } catch (SecurityException e) {
            throw new IllegalArgumentException(e.getMessage());
        } catch (NoSuchFieldException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
        this.type = type;
        this.name = name;
        this.elemType = elemType;
    }

    /**
     * Registers a new configuration entry.
     * 
     * @param name the name of the entry
     * @param fieldName the name of the field where to store the value to
     * @param type the type of the entry
     * @param elemType additional type information when <code>type</code> 
     *   is {@link Type#ENUM} or {@link Type#ARRAY_ENUM}
     * 
     * @throws IllegalArgumentException in case that <code>name</code> is 
     *     <b>null</b>, <code>type</code> is <b>null</b> or that the related
     *     attribute in the configuration does not exist.
     * 
     * @since 1.00
     */
    public static final void registerEntry(String name, String fieldName, 
        Type type, Class<?> elemType) {
        if (Type.ENUM == type || Type.ARRAY_ENUM == type) {
            if (null == elemType) {
                throw new IllegalArgumentException("no characterizing type " 
                    + "information for " + type + " given");
            }
            if (!elemType.isEnum()) {
                throw new IllegalArgumentException("characterizing type for " 
                    + type + " must be an enum");
            }
        }
        ConfigurationEntry entry = new ConfigurationEntry(name, 
            fieldName, type, elemType);
        ENTRIES.put(entry.getName(), entry);
    }

    /**
     * Registers a new configuration entry.
     * 
     * @param name the name of the entry
     * @param fieldName the name of the field where to store the value to
     * @param type the type of the entry
     * 
     * @throws IllegalArgumentException in case that <code>name</code> is 
     *     <b>null</b>, <code>type</code> is <b>null</b> or that the related
     *     attribute in the configuration does not exist.
     * 
     * @since 1.00
     */
    public static final void registerEntry(String name, String fieldName, 
        Type type) {
        registerEntry(name, fieldName, type, null);
    }
    
    /**
     * Registers a new configuration entry.
     * 
     * @param name the name of the entry (also the name of the field)
     * @param type the type of the entry
     * 
     * @throws IllegalArgumentException in case that <code>name</code> is 
     *     <b>null</b>, <code>type</code> is <b>null</b> or that the related
     *     attribute in the configuration does not exist.
     * 
     * @since 1.00
     */
    public static final void registerEntry(String name, 
        Type type) {
        registerEntry(name, name, type, null);
    }

    /**
     * Returns the entry matching the given <code>name</code>.
     * 
     * @param name the name of the profile to be returned
     * @return the configuration entry or <b>null</b> if none was found
     * 
     * @throws IllegalArgumentException if <code>name</code> is <b>null</b>
     *   
     * @since 1.00
     */
    public static final ConfigurationEntry getEntry(String name) {
        if (null == name) {
            throw new IllegalArgumentException("'name' must not be null");
        }
        return ENTRIES.get(name);
    }
    
    /**
     * Returns all registered configuration entries.
     * 
     * @return all registered configuration entries
     * 
     * @since 1.00
     */
    public static final Iterator<ConfigurationEntry> getEntries() {
        return ENTRIES.values().iterator();
    }

    /**
     * Returns the name of this entry.
     * 
     * @return the name of this entry
     * 
     * @since 1.00
     */
    public String getName() {
        return name;
    }

    /**
     * Converts the given string to the type of this entry and applies it to
     * the configuration instance.
     * 
     * @param value the value to be set for this entry
     * 
     * @throws IllegalArgumentException in case that the <code>value</code>
     *   cannot be set for some reason (e.g. cannot be converted to target 
     *   <code>type</code>)
     * 
     * @since 1.00
     */
    public void setValue(String value) {
        Object param = null;
        switch (type) {
        case INTEGER:
        case STRING:
        case BOOLEAN:
        case ENUM:
            param = getPrimitiveValue(value, type, elemType);
            break;
        case ARRAY_ENUM:
            StringTokenizer tokens = new StringTokenizer(value, ",");
            List<Object> values = new ArrayList<Object>();
            while (tokens.hasMoreTokens()) {
                Object val = getPrimitiveValue(tokens.nextToken().trim(), 
                    type.getElementType(), elemType);
                if (null != val) {
                    // otherways not applicable
                    values.add(val);
                }
            }
            param = Array.newInstance(elemType, values.size());
            for (int i = 0; i < values.size(); i++) {
                Array.set(param, i, values.get(i));
            }
            break;
        default:
            break;
        }
        try {
            field.set(Configuration.INSTANCE, param);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(e.getMessage());
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    /**
     * Converts the given string to the given type (in case that the 
     * type is primitive) and returns the converted object result.
     * 
     * @param value the value to be set for this entry
     * @param type the type to convert for
     * @param elemType the additional information characterizing e.g. enums
     * @return the result of the conversion (<b>null</b> if not applicable)
     * 
     * @since 1.00
     */
    private static Object getPrimitiveValue(String value, Type type, 
        Class<?> elemType) {
        Object result = null;
        switch (type) {
        case INTEGER:
            try {
                result = Integer.valueOf(value);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(e.getMessage());
            }
            break;
        case STRING:
            result = value;
            break;
        case BOOLEAN:
            result = Boolean.valueOf(value);
            break;
        case ENUM:
            Field[] fields = elemType.getDeclaredFields();
            String tmp = value.toUpperCase();
            for (Field field : fields) {
                if (field.getName().equals(tmp)) {
                    try {
                        result = field.get(null);
                    } catch (IllegalArgumentException e) {
                        throw new IllegalArgumentException(e.getMessage());
                    } catch (IllegalAccessException e) {
                        throw new IllegalArgumentException(e.getMessage());
                    }
                    break;
                }
            }
            break;
        default:
            break;
        }
        return result;
    }
}