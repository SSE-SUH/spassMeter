package de.uni_hildesheim.sse.monitoring.runtime.plugins;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import de.uni_hildesheim.sse.codeEraser.annotations.Variability;
import de.uni_hildesheim.sse.monitoring.runtime.AnnotationConstants;
import de.uni_hildesheim.sse.monitoring.runtime.utils.StreamUtilities;

/**
 * Defines types of values for which changes may be notified.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
@Variability(id = AnnotationConstants.MONITOR_VALUES)
public enum ValueType {
    
    /**
     * Represents a double.
     */
    DOUBLE(Double.TYPE),

    /**
     * Represents a float.
     */
    FLOAT(Float.TYPE),
    
    /**
     * Represents an int.
     */
    INT(Integer.TYPE),
    
    /**
     * Represents a long.
     */
    LONG(Long.TYPE),

    /**
     * Represents a char.
     */
    CHAR(Character.TYPE),

    /**
     * Represents a boolean.
     */
    BOOLEAN(Boolean.TYPE),
    
    /**
     * Represents a short.
     */
    SHORT(Short.TYPE),

    /**
     * Represents a short.
     */
    BYTE(Byte.TYPE),

    /**
     * Represents a string.
     */
    STRING(String.class),

    /**
     * Represents an arbitrary object.
     */
    OBJECT(null);
    
    /**
     * Stores the associated class.
     */
    private Class<?> cls;
    
    /**
     * Creates a constant and associates the class.
     * 
     * @param cls the associated class
     * 
     * @since 1.00
     */
    private ValueType(Class<?> cls) {
        this.cls = cls;
    }

    /**
     * Converts an arbitrary object to a value type.
     * 
     * @param object the object to be converted
     * @return the value type (default {@link #OBJECT})
     * 
     * @since 1.00
     */
    public static ValueType fromObject(Object object) {
        ValueType result = OBJECT;
        if (null != object) {
            ValueType[] types = values();
            for (int i = 0; OBJECT == result && i < types.length; i++) {
                if (types[i].cls == object.getClass()) {
                    result = types[i];
                }
            }
        }
        return result;
    }

    /**
     * Writes a corresponding value to the given input stream.
     * 
     * @param out the output stream
     * @param value a corresponding (castable) value
     * @throws IOException in case of any I/O related error
     * @throws ClassCastException in case that <code>value</code> does not map
     *   to the respective wrapper classes
     * 
     * @since 1.00
     */
    public void write(DataOutputStream out, Object value) throws IOException {
        switch (this) {
        case BOOLEAN:
            out.writeBoolean(((Boolean) value).booleanValue());
            break;
        case CHAR:
            out.writeChar(((Character) value).charValue());
            break;
        case DOUBLE:
            out.writeDouble(((Double) value).doubleValue());
            break;
        case FLOAT:
            out.writeFloat(((Float) value).floatValue());
            break;
        case INT:
            out.writeInt(((Integer) value).intValue());
            break;
        case LONG:
            out.writeLong(((Long) value).longValue());
            break;
        case SHORT:
            out.writeShort(((Short) value).shortValue());
            break;
        case BYTE:
            out.writeByte(((Byte) value).byteValue());
            break;
        case STRING:
        case OBJECT:
            String tmp = null;
            if (null != value) {
                tmp = value.toString();
            }
            StreamUtilities.writeString(out, tmp);
            break;
        default:
            break;
        }
    }
    
    /**
     * Reads a corresponding value from the given input stream.
     * 
     * @param in the input stream
     * @return the corresponding value (may be <b>null</b>)
     * @throws IOException in case of any I/O related error
     * 
     * @since 1.00
     */
    public Object read(DataInputStream in) throws IOException {
        Object result = null;
        switch (this) {
        case BOOLEAN:
            result = in.readBoolean();
            break;
        case CHAR:
            result = in.readChar();
            break;
        case DOUBLE:
            result = in.readDouble();
            break;
        case FLOAT:
            result = in.readFloat();
            break;
        case INT:
            result = in.readInt();
            break;
        case LONG:
            result = in.readLong();
            break;
        case SHORT:
            result = in.readShort();
            break;
        case BYTE:
            result = in.readByte();
            break;
        case STRING:
        case OBJECT:
            result = StreamUtilities.readString(in);
            break;
        default:
            break;
        }
        return result;
    }
    
}
