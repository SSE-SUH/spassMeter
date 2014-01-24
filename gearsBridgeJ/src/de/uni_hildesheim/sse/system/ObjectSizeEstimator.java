package de.uni_hildesheim.sse.system;

import java.lang.reflect.Array;
import java.lang.reflect.Field;

/**
 * Defines an estimation for the object size. This is useful in case that no
 * instrumentation agent and no JVMTI instance are accessible (e.g. in current
 * implementations of the Android JVM). This class is intended to be 
 * extensible, i.e. it can be subclassed and a new instance can be registered
 * via {@link #setInstance(ObjectSizeEstimator)}. This instance is then 
 * considered by {@link #getObjectSize(Object)} to estimate the object size.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public class ObjectSizeEstimator {

    /**
     * Stores the size descriptor.
     */
    private static ObjectSizeEstimator instance = new ObjectSizeEstimator();

    /**
     * Defines the size of the basic types (boolean, int, etc.).
     */
    private static final long BASIC_TYPE_SIZE = 4;

    /**
     * Defines the size of the precise basic types (long, double).
     */
    private static final long PRECISE_TYPE_SIZE = 8;

    /**
     * Defines the size of the reference types.
     */
    private static final long REFERENCE_TYPE_SIZE = 4;
    
    /**
     * Stores the size of an empty class.
     */
    private static long emptyClassSize = 8;
    
    /**
     * Stores the size of an empty array.
     */
    private static long emptyArraySize = emptyClassSize + 8; // however

    /**
     * Stores the alignment size of a class.
     */
    private static long classAlignment = 8;
    
    /**
     * Prevents this class from being instantiated from outside.
     * 
     * @since 1.00
     */
    protected ObjectSizeEstimator() {
    }
    
    /**
     * Changes the default estimator instance.
     * 
     * @param estimator the new estimator
     * @throws IllegalArgumentException if <code>estimator == <b>null</b></code>
     * 
     * @since 1.00
     */
    public static void setInstance(ObjectSizeEstimator estimator) {
        if (null == estimator) {
            throw new IllegalArgumentException("'estimator' must not be null");
        }
        instance = estimator;
    }
    
    /**
     * Returns the memory size of the given type.
     * 
     * @param type the type to be considered
     * @return the size in bytes
     * 
     * @since 1.00
     */
    public static long getTypeSize(Class<?> type) {
        long result = 0;
        if (Integer.TYPE == type) {
            result = BASIC_TYPE_SIZE;
        } else if (Character.TYPE == type) {
            result = BASIC_TYPE_SIZE;
        } else if (Boolean.TYPE == type) {
            result = BASIC_TYPE_SIZE;
        } else if (Double.TYPE == type) {
            result = PRECISE_TYPE_SIZE;
        } else if (Long.TYPE == type) {
            result = PRECISE_TYPE_SIZE;
        } else if (Short.TYPE == type) {
            result = BASIC_TYPE_SIZE;
        } else if (Float.TYPE == type) {
            result = BASIC_TYPE_SIZE;
        } else if (Byte.TYPE == type) {
            result = BASIC_TYPE_SIZE;
        } else {
            result = REFERENCE_TYPE_SIZE;
        }
        return result;
    }

    /**
     * Returns the memory size of the given type. Currently this method is 
     * intended to be used during the instrumentation so type name comparisons 
     * are not as optimized as they may be.
     * 
     * @param type the type to be considered
     * @return the size in bytes
     * 
     * @since 1.00
     */
    public static long getTypeSize(String type) {
        long result = 0;
        
        // intended to be used during instrumentation, type optimizations not
        // as optimal as they can be
        if ("int".equals(type)) {
            result = BASIC_TYPE_SIZE;
        } else if ("char".equals(type)) {
            result = BASIC_TYPE_SIZE;
        } else if ("boolean".equals(type)) {
            result = BASIC_TYPE_SIZE;
        } else if ("double".equals(type)) {
            result = PRECISE_TYPE_SIZE;
        } else if ("long".equals(type)) {
            result = PRECISE_TYPE_SIZE;
        } else if ("short".equals(type)) {
            result = BASIC_TYPE_SIZE;
        } else if ("float".equals(type)) {
            result = BASIC_TYPE_SIZE;
        } else if ("byte".equals(type)) {
            result = BASIC_TYPE_SIZE;
        } else {
            result = REFERENCE_TYPE_SIZE;
        }
        return result;
    }
    
    /**
     * Returns the size of an empty class.
     * 
     * @return the size of an empty class
     * 
     * @since 1.00
     */
    public long getEmptyClassSize() {
        return emptyClassSize;
    }
    
    /**
     * Changes the size of an empty class.
     * 
     * @param emptyClassSize the new size of an empty class
     * 
     * @since 1.00
     */
    protected static void setEmptyClassSize(long emptyClassSize) {
        emptyClassSize = Math.max(0, emptyClassSize);
    }

    /**
     * Returns the size of an empty array.
     * 
     * @return the size of an empty array
     * 
     * @since 1.00
     */
    public long getEmptyArraySize() {
        return emptyArraySize;
    }

    /**
     * Changes the size of an empty array.
     * 
     * @param emptyArraySize the new size of an empty array 
     *     (here <code>8</code>)
     * 
     * @since 1.00
     */
    protected static void setEmptyArraySize(long emptyArraySize) {
        emptyArraySize = Math.max(0, emptyArraySize);
    }

    /**
     * Returns the class alignment size.
     * 
     * @return the class alignment size
     * 
     * @since 1.00
     */
    public long getClassAlignment() {
        return classAlignment;
    }

    /**
     * Changes the size of the class alignment.
     * 
     * @param classAlignment the new class alignment
     * 
     * @since 1.00
     */
    protected static void setClassAlignment(long classAlignment) {
        classAlignment = Math.max(0, classAlignment);
    }
    
    /**
     * Aligns the size of a class (here by <code>8</code>).
     * 
     * @param size the unaligned size
     * @return the aligned size
     * 
     * @since 1.00
     */
    public static long alignClassSize(long size) {
        if (size % classAlignment > 0) {
            // alignmnent
            size = ((size / classAlignment) + 1) * classAlignment;
        }
        return size;
    }

    /**
     * Returns the memory size of an array.
     * 
     * @param length the length of the array
     * @param componentSize the size of the individual components (based on the 
     *     component type)
     * @return the size of the array
     * 
     * @since 1.00
     */
    public static long getArraySize(int length, long componentSize) {
        long result = emptyArraySize;
        if (length > 1) { // assumption
            result += length * componentSize;
        }
        return alignClassSize(result);
    }

    /**
     * Returns the memory size of a class.
     * 
     * @param fieldsSize the size of the individual fields (see 
     *   {@link #getTypeSize(Class)} or {@link #getTypeSize(String)}).
     * @return the size of the class
     * 
     * @since 1.00
     */
    public static final long getClassSize(long fieldsSize) {
        return alignClassSize(emptyClassSize + fieldsSize); 
    }
    
    /**
     * Returns the size of the given object as allocated by the JVM. This
     * method is a dummy implementation.
     * 
     * @param object the object the size should be queried for
     * @return the size of the physical memory allocated for <code>object</code>
     *         (in bytes, negative or zero if invalid or not available, 
     *         always 0)
     * 
     * @since 1.00
     */
    public long calcObjectSize(Object object) {
        long result = 0;
        if (null != object) {
            Class<?> cls = object.getClass();
            // assumed basic size for empty class
            if (cls.isArray()) {
                result = getArraySize(Array.getLength(object), 
                    getTypeSize(cls.getComponentType()));
            } else {
                long fieldsSize = 0;
                do {
                    for (Field field : cls.getDeclaredFields()) {
                        fieldsSize += getTypeSize(field.getType());
                    }
                    cls = cls.getSuperclass();
                } while (null != cls);
                result = getClassSize(fieldsSize);
            }
        }
        return result;
    }

    /**
     * Returns the size of the given object as allocated by the JVM. This
     * method is a dummy implementation.
     * 
     * @param object the object the size should be queried for
     * @return the size of the physical memory allocated for <code>object</code>
     *         (in bytes, negative or zero if invalid or not available, 
     *         always 0)
     * 
     * @since 1.00
     */
    public static long getObjectSize(Object object) {
        return instance.calcObjectSize(object);
    }
    
}
