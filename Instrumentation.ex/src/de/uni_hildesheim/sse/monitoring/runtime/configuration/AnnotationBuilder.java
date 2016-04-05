package de.uni_hildesheim.sse.monitoring.runtime.configuration;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.StringTokenizer;

import de.uni_hildesheim.sse.codeEraser.annotations.Variability;
import de.uni_hildesheim.sse.monitoring.runtime.AnnotationConstants;
import de.uni_hildesheim.sse.monitoring.runtime.boot.ArrayList;
import de.uni_hildesheim.sse.monitoring.runtime.utils.
    AnnotationInstanceProvider;
import de.uni_hildesheim.sse.monitoring.runtime.utils.HashMap;
import de.uni_hildesheim.sse.monitoring.runtime.utils.xml.QdParserException;

/**
 * Implements the default way of creating annotations on demand. Instances of
 * this class are intended to be used as template instances as well as concrete
 * instances.
 * 
 * @param <T> the annotation type
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.11
 */
@Variability(id = AnnotationConstants.CONFIG_XML)
public class AnnotationBuilder<T extends Annotation> 
    implements IAnnotationBuilder<T> {
    
    /**
     * Stores the instance of the annotation (created only once on demand).
     */
    private T instance;
    
    /**
     * Stores the concrete values of the annotation (method name - value).
     */
    private HashMap<String, Object> data = new HashMap<String, Object>();
    
    /**
     * Stores the concrete type of the annotation.
     */
    private Class<T> cls;
    
    /**
     * Stores the data attributes in order to implement generic XML reading.
     */
    private AttributeMetaData[] dataDefinition;

    /**
     * Creates an instance of this builder by providing the meta information
     * for each attribute to be considered.
     * 
     * @param cls the type of the annotation
     * @param dataDefinition the meta data
     * 
     * @since 1.00
     */
    public AnnotationBuilder(Class<T> cls, AttributeMetaData[] dataDefinition) {
        this.cls = cls;

        Method[] declaredMethods = cls.getDeclaredMethods();
        this.dataDefinition = new AttributeMetaData[declaredMethods.length];
        for (int i = 0; i < declaredMethods.length; i++) {
            AttributeMetaData found = null;
            if (null != dataDefinition) {
                for (int j = 0; null == found 
                    && j < dataDefinition.length; j++) {
                    if (dataDefinition[j].getName().equals(
                        declaredMethods[i].getName())) {
                        found = dataDefinition[j];
                    }
                }
            }
            if (null == found) {
                found = new AttributeMetaData(declaredMethods[i]);
            }
            this.dataDefinition[i] = found;
        }
    }

    /**
     * Creates an instance for a concrete annotation by reading the metadata
     * directly from the annotation.
     * 
     * @param cls the annotation class
     * {
     * @since 1.00
     */
    public AnnotationBuilder(Class<T> cls) {
        this(cls, null);
    }
    
    /**
     * Creates an annotation builder from a given annotation builder by copying
     * the relevant data, resetting {@link #instance} and initializing 
     * {@link #data} with default values (see {@link #setDefaults()}.
     * 
     * @param builder the builder to copy from
     * 
     * @since 1.00
     */
    private AnnotationBuilder(AnnotationBuilder<T> builder) {
        this.cls = builder.cls;
        this.dataDefinition = builder.dataDefinition;
        this.instance = null;
        this.data = new HashMap<String, Object>(builder.data.size());
        setDefaults();
    }

    /**
     * Returns the concrete type of the annotation.
     * 
     * @return the concrete type
     * 
     * @since 1.00
     */
    @Override
    public Class<T> getInstanceClass() {
        return cls;
    }

    /**
     * Creates the annotation.
     * 
     * @return the annotation
     * 
     * @since 1.00
     */
    public final T create() {
        if (null == instance) {
            try {
                instance = AnnotationInstanceProvider.INSTANCE
                    .get(getInstanceClass(), data);
            } catch (Exception e) {
            }
            data = null;
        }
        return instance;
    }
    
    /**
     * Is called at the end of an XML element processing step.
     * 
     * @param qName the qualified name of the element
     * @param nested if the element is nested as an unknown element in a 
     *     known element
     * @throws QdParserException in case of any processing error
     * 
     * @since 1.00
     */
    public void endElement(String qName, boolean nested) 
        throws QdParserException {
    }

    /**
     * Is called at the beginning of an XML element processing step.
     * 
     * @param qName the qualified name of the element
     * @param nested if the element is nested as an unknown element in a 
     *     known element
     * @param attributes the actual XML attributes
     * @throws QdParserException in case of any processing error
     * 
     * @since 1.00
     */
    public void startElement(String qName, boolean nested,
        HashMap<String, String> attributes) throws QdParserException {
        int len = 0;
        if (null != dataDefinition) {
            len = dataDefinition.length;
        }
        for (int pos = 0; pos < len; pos++) {
            if (dataDefinition[pos].isNested() == nested) {
                String key = dataDefinition[pos].getName();
                Object deflt = dataDefinition[pos].getDefault();
                Object value = null;
                String attribute = attributes.get(key);
                if (null == attribute) {
                    if (null == deflt) {
                        throw new QdParserException("no value for '" + key 
                            + "' given in '" + qName + "'");
                    }
                    value = deflt;
                } else {
                    Class<?> type = dataDefinition[pos].getType();
                    if (type.equals(String.class)) {
                        value = attribute;
                    } else if (type.equals(Boolean.class)) {
                        value = Boolean.valueOf(attribute);
                    } else if (type.isEnum()) {
                        value = getEnumValue(type, attribute, deflt);
                        if (null == value) {
                            throw new QdParserException("enum value '" 
                                + attribute 
                                + "' for '" + key + "' in '"
                                + qName + "' is not valid");
                        }
                    } else if (type.isArray()) { 
                        StringTokenizer tokens = 
                            new StringTokenizer(attribute, ",");
                        if (type.getComponentType() == String.class) {
                            ArrayList<String> sVals = new ArrayList<String>();
                            while (tokens.hasMoreTokens()) {
                                sVals.add(tokens.nextToken());
                            }                            
                            String[] arrayVals = new String[sVals.size()];
                            sVals.toArray(arrayVals);
                            value = arrayVals;
                        } else {
                            ArrayList<Object> vals = new ArrayList<Object>();
                            while (tokens.hasMoreTokens()) {
                                String token = tokens.nextToken().trim();
                                // assume enum
                                Object val = getEnumValue(type, token, null);
                                if (null == val) {
                                    throw new QdParserException("value '" 
                                        + token 
                                        + "' for '" + key + "' in '" + qName 
                                        + "' is not valid");
                                }
                                vals.add(val);
                            }                            
                            Object[] arrayVals = new Object[vals.size()];
                            vals.toArray(arrayVals);
                            value = arrayVals;
                        }
                    } else {
                        assert false;
                    }
                }
                setData(key, value);
            }
        }
    }
    
    /**
     * Returns whether the values of this annotation just contains defaults.
     * 
     * @return <code>true</code> if only defaults are set, <code>false</code>
     * else
     * 
     * @since 1.11
     */
    public boolean defaultsOnly() {
        boolean only = true;
        for (int pos = 0; only && pos < dataDefinition.length; pos++) {
            Object val = data.get(dataDefinition[pos].getName());
            Object dflt = dataDefinition[pos].getDefault();
            if (null != val) {
                only &= val.equals(dflt);
            } else {
                only &= dflt == null;
            }
        }
        return only;
    }

    /**
     * Returns the matching enum constant.
     * 
     * @param type the type of the enumeration
     * @param attribute the attribute value
     * @param deflt the default value in case that the attribute is not 
     *   defined (may be <b>null</b>)
     * @return the enum value
     * 
     * @since 1.00
     */
    private Object getEnumValue(Class<?> type, String attribute, Object deflt) {
        Object value = null;
        for (Field field : type.getDeclaredFields()) {
            if (field.getName().equals(attribute)) {
                try {
                    value = field.get(null);
                    break;
                } catch (IllegalArgumentException e) {
                    // should not happen, static constant access to existing 
                    // constant
                } catch (IllegalAccessException e) {
                    // should not happen, static constant access to existing 
                    // constant
                }
            }
        }
        return value;
    }
 
    /**
     * Sets a data value directly.
     * 
     * @param key the name of the element / attribute.
     * @param value the assigned value (must match the type in T)
     * 
     * @since 1.00
     */
    public void setData(String key, Object value) {
        data.put(key, value);
    }

    /**
     * Sets a string data from an attribute.
     * 
     * @param key the name of the attribute / element
     * @param attributes the actual attributes 
     * @param deflt the default value in case that the attribute is not 
     *   defined (may be <b>null</b>)
     * @throws QdParserException in case of any processing error
     * 
     * @since 1.00
     */
    protected void setStringData(String key, HashMap<String, 
        String> attributes, String deflt) throws QdParserException {
        String attribute = attributes.get(key);
        if (null == attribute) {
            if (null == deflt) {
                throw new QdParserException("attribute '" 
                    + key + "' must be given");
            }
            attribute = deflt;
        }
        data.put(key, attribute);
    }

    /**
     * Sets an enum data from an attribute.
     * 
     * @param <E> the type of the enum
     * @param key the name of the attribute / element
     * @param attributes the actual attributes 
     * @param values the class of the enum
     * @param deflt the default value in case that the attribute is not 
     *   defined (may be <b>null</b>)
     * @throws QdParserException in case of any processing error
     * 
     * @since 1.00
     */
    protected<E extends Enum<E>> void setEnumData(String key, 
        HashMap<String, String> attributes, Class<E> values, E deflt) 
        throws QdParserException {
        E value = null;
        String attribute = attributes.get(key);
        if (null == attribute) {
            if (null == deflt) {
                throw new QdParserException(
                    "attribute '" + key + "' must be given");
            }
            value = deflt;
        } else {
            for (Field field : values.getDeclaredFields()) {
                if (field.getName().equals(attribute)) {
                    value = deflt;
                    break;
                }
            }
            if (null == value) {
                throw new QdParserException("value '" + attribute + "' for '" 
                    + key + "' is not valid");
            }
        }
        data.put(key, value);
    }

    /**
     * Sets a string data from an attribute.
     * 
     * @param key the name of the attribute / element
     * @param attributes the actual attributes 
     * @param deflt the default value in case that the attribute is not 
     *   defined (may be <b>null</b>)
     * @throws QdParserException in case of any processing error
     * 
     * @since 1.00
     */
    protected void setBooleanData(String key, 
        HashMap<String, String> attributes, 
        Boolean deflt) throws QdParserException {
        String attribute = attributes.get(key);
        Boolean value;
        if (null != attribute) {
            value = Boolean.valueOf(attribute);
        } else {
            if (null == deflt) {
                throw new QdParserException("attribute '" 
                    + key + "' must be given");
            }
            value = deflt;
        }
        data.put(key, value);
    }
    
    /**
     * Prepares a template instance for use.
     * 
     * @return the instance to be used
     * 
     * @since 1.00
     */
    public AnnotationBuilder<T> prepareForUse() {
        return new AnnotationBuilder<T>(this);
    }
    
    /**
     * Initializes the data of the annotation to be created with the default
     * values.
     * 
     * @since 1.00
     */
    private void setDefaults() {
        for (AttributeMetaData meta : dataDefinition) {
            if (null != meta.getDefault()) {
                setData(meta.getName(), meta.getDefault());
            }
        }
    }
    
    /**
     * Returns a textual representation of this object.
     * 
     * @return the textual representation
     */
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(hashCode());
        builder.append(" {");
        int count = 0;
        for (HashMap.Entry<String, Object> ent : data.entries()) {
            builder.append(ent.getKey());
            builder.append("=");
            if (null != ent.getValue() && ent.getValue().getClass().isArray()) {
                Object[] arr = (Object[]) ent.getValue();
                builder.append("[");
                for (int i = 0; i < arr.length; i++) {
                    builder.append(arr[i]);
                    if (i < arr.length - 1) {
                        builder.append(",");
                    }
                }
                builder.append("]");
            } else {
                builder.append(ent.getValue());
            }
            if (count < data.size()) {
                builder.append(",");
            }
            count++;
        }
        builder.append("}");
        return builder.toString();
    }
    
}