package de.uni_hildesheim.sse.monitoring.runtime.utils;

import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Class that handles the method calls for a java.lang.reflect.Proxy that
 * implements an annotation.
 * 
 * The generated handler takes a Map<String,Object> as a constructor argument
 * with the keys equal to the annotation method names. For Annotation, array and
 * Enum types the values must exactly match the return type or a
 * ClassCastException will result.
 * 
 * For character types the the value must be an instance of java.lang.Character
 * or String.
 * 
 * Numeric types do not have to match exactly, as they are converted using
 * java.lang.Number's xxxValue methods.
 * 
 * @author Stuart Douglas (taken from JBoss WELD SVN rev 5138)
 */
class AnnotationInvocationHandler implements InvocationHandler, Serializable {
   
    /**
     * Defines a serialization proxy used to replace instances of 
     * {@link AnnotationInvocationHandler} in a serialized stream.
     * 
     * @author Stuart Douglas (taken from JBoss WELD SVN rev 5138)
     */
    private static class SerializationProxy {

        /**
         * Stores the values corresponding to the annotation values.
         */
        private final HashMap<String, Object> valueMap;

        /**
         * Stores the annotation type to emulate.
         */
        private final Class<? extends Annotation> annotationType;

        /**
         * Creates a new proxy instance.
         * 
         * @param valueMap the values corresponding to the annotation values
         * @param annotationType the annotation type to emulate
         */
        private SerializationProxy(HashMap<String, Object> valueMap, 
            Class<? extends Annotation> annotationType) {
            this.valueMap = valueMap;
            this.annotationType = annotationType;
        } 
      
        /**
         * Replaces the proxy instance in a serialized input stream by
         * the original {@link AnnotationInvocationHandler} instance.
         * 
         * @return the original instance
         * @throws ObjectStreamException in case of problems during 
         *     deserialization
         * 
         * @since 1.00
         */
        private Object readResolve() throws ObjectStreamException {
            return new AnnotationInvocationHandler(valueMap, annotationType);
        }
      
    }

   /**
    * Defines the serialization id.
    */
    private static final long serialVersionUID = 4801508041776645033L;

    /**
     * Stores the values corresponding to the annotation values.
     */
    private final HashMap<String, Object> valueMap;

    /**
     * Stores the annotation type to emulate.
     */
    private final Class<? extends Annotation> annotationType;

    /**
     * Stores the members declared by {@link #annotationType}.
     */
    private final Method[] members;

    /**
     * Creates a new invocation handler taking over the annotation type and the
     * values to return for the annotation (instance).
     * 
     * @param valueMap the concrete values for the individual annotation 
     *     attributes
     * @param annotationType the annotation type to emulate
     * 
     * @since 1.00
     */
    AnnotationInvocationHandler(HashMap<String, Object> valueMap, 
        Class<? extends Annotation> annotationType) {
        this.valueMap = valueMap;
        this.annotationType = annotationType;
        this.members = annotationType.getDeclaredMethods();
    }
   
    /**
     * {@inheritDoc}
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) 
        throws Throwable {
        if (method.getName().equals("equals")) {
            return equals(args[0]);
        } else if (method.getName().equals("hashCode")) {
            return hashCode();
        } else if (method.getName().equals("toString")) {
            return toString();
        } else if (method.getName().equals("annotationType")) {
            return annotationType;
        } else {
            Object val = valueMap.get(method.getName());
            if (val != null) {
                Class<?> r = method.getReturnType();
                if (Integer.class.isAssignableFrom(r) || r == int.class) {
                    val = ((Number) val).intValue();
                } else if (Long.class.isAssignableFrom(r) || r == long.class) {
                    val = ((Number) val).longValue();
                } else if (Short.class.isAssignableFrom(r) 
                    || r == short.class) {
                    val = ((Number) val).shortValue();
                } else if (Byte.class.isAssignableFrom(r) || r == byte.class) {
                    val = ((Number) val).shortValue();
                } else if (Double.class.isAssignableFrom(r) 
                    || r == double.class) {
                    val = ((Number) val).doubleValue();
                } else if (Float.class.isAssignableFrom(r) 
                    || r == float.class) {
                    val = ((Number) val).floatValue();
                } else if (Character.class.isAssignableFrom(r) 
                    || r == char.class) {
                    if (String.class.isAssignableFrom(val.getClass())) {
                        val = val.toString().charAt(0);
                    }
                } else if (r.isArray()) {
                    if (method.getName().equals("id")) {
                        // hack - usually do not modify arrays returned by 
                        // annotations
                        if (val instanceof Object[]) {
                            val = ((Object[]) val).clone();
                        } else if (val instanceof java.util.List) {
                            // required for ASM...
                            java.util.List<?> li = (java.util.List<?>) val;
                            int size = li.size();
                            String[] sVal = new String[size];
                            for (int i = 0; i < size; i++) {
                                sVal[i] = li.get(i).toString();
                            }
                            val = sVal;
                        }
                    }
                }
            }
            return val;
        }
    }

    /**
     *  {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuilder string = new StringBuilder();
        string.append('@').append(annotationType.getName()).append('(');
        for (int i = 0; i < members.length; i++) {
            string.append(members[i].getName()).append('=');
            Object value = valueMap.get(members[i].getName());
            //Object value = invoke(members[i], this);
            if (value instanceof boolean[]) {
                appendInBraces(string, Arrays.toString((boolean[]) value));
            } else if (value instanceof byte[]) {
                appendInBraces(string, Arrays.toString((byte[]) value));
            } else if (value instanceof short[]) {
                appendInBraces(string, Arrays.toString((short[]) value));
            } else if (value instanceof int[]) {
                appendInBraces(string, Arrays.toString((int[]) value));
            } else if (value instanceof long[]) {
                appendInBraces(string, Arrays.toString((long[]) value));
            } else if (value instanceof float[]) {
                appendInBraces(string, Arrays.toString((float[]) value));
            } else if (value instanceof double[]) {
                appendInBraces(string, Arrays.toString((double[]) value));
            } else if (value instanceof char[]) {
                appendInBraces(string, Arrays.toString((char[]) value));
            } else if (value instanceof String[])   {
                String[] strings = (String[]) value;
                String[] quoted = new String[strings.length];
                for (int j = 0; j < strings.length; j++) {
                    quoted[j] = "\"" + strings[j] + "\"";
                }
                appendInBraces(string, Arrays.toString(quoted));
            } else if (value instanceof Class<?>[]) {
                Class<?>[] classes = (Class<?>[]) value;
                String[] names = new String[classes.length];
                for (int j = 0; j < classes.length; j++) {
                    names[j] = classes[j].getName() + ".class";
                }
                appendInBraces(string, Arrays.toString(names));
            } else if (value instanceof Object[]) {
                appendInBraces(string, Arrays.toString((Object[]) value));
            } else if (value instanceof String) {
                string.append('"').append(value).append('"');
            } else if (value instanceof Class<?>) {
                string.append(((Class<?>) value).getName()).append(".class");
            } else {
                string.append(value);
            }
            if (i < members.length - 1) {
                string.append(", ");
            }
        }
        return string.append(')').toString();
    }

    /**
     * Appends the given string in braces to <code>buf</code>.
     * 
     * @param buf the buffer to append to
     * @param text the string to append
     * 
     * @since 1.00
     */
    private void appendInBraces(StringBuilder buf, String text) {
        buf.append('{')
            .append(text.substring(1, text.length() - 1)).append('}');
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object other) {
        if (other instanceof Annotation) {
            Annotation that = (Annotation) other;
            if (this.annotationType.equals(that.annotationType())) {
                for (Method member : members) {
                    Object thisValue = valueMap.get(member.getName());
                    Object thatValue = invoke(member, that);
                    if (thisValue instanceof byte[] 
                        && thatValue instanceof byte[]) {
                        if (!Arrays.equals((byte[]) thisValue, 
                            (byte[]) thatValue)) {
                            return false;
                        }
                    } else if (thisValue instanceof short[] 
                        && thatValue instanceof short[]) {
                        if (!Arrays.equals((short[]) thisValue, 
                            (short[]) thatValue)) {
                            return false;
                        }
                    } else if (thisValue instanceof int[] 
                        && thatValue instanceof int[]) {
                        if (!Arrays.equals((int[]) thisValue, 
                            (int[]) thatValue)) {
                            return false;
                        }
                    } else if (thisValue instanceof long[] 
                        && thatValue instanceof long[]) {
                        if (!Arrays.equals((long[]) thisValue, 
                            (long[]) thatValue)) {
                            return false;
                        }
                    } else if (thisValue instanceof float[] 
                        && thatValue instanceof float[]) {
                        if (!Arrays.equals((float[]) thisValue, 
                            (float[]) thatValue)) {
                            return false;
                        }
                    } else if (thisValue instanceof double[] 
                        && thatValue instanceof double[]) {
                        if (!Arrays.equals((double[]) thisValue, 
                            (double[]) thatValue)) {
                            return false;
                        }
                    } else if (thisValue instanceof char[] 
                        && thatValue instanceof char[]) {
                        if (!Arrays.equals((char[]) thisValue, 
                            (char[]) thatValue)) {
                            return false;
                        }
                    } else if (thisValue instanceof boolean[] 
                        && thatValue instanceof boolean[]) {
                        if (!Arrays.equals((boolean[]) thisValue, 
                            (boolean[]) thatValue)) {
                            return false;
                        }
                    } else if (thisValue instanceof Object[] 
                        && thatValue instanceof Object[]) {
                        if (!Arrays.equals((Object[]) thisValue, 
                            (Object[]) thatValue)) {
                            return false;
                        }
                    } else {
                        if (!thisValue.equals(thatValue)) {
                            return false;
                        }
                    }
                }
                return true;
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int hashCode = 0;
        for (Method member : members) {
            int memberNameHashCode = 127 * member.getName().hashCode();
            Object value = valueMap.get(member.getName());
            int memberValueHashCode;
            if (value instanceof boolean[]) {
                memberValueHashCode = Arrays.hashCode((boolean[]) value);
            } else if (value instanceof short[]) {
                memberValueHashCode = Arrays.hashCode((short[]) value);
            } else if (value instanceof int[]) {
                memberValueHashCode = Arrays.hashCode((int[]) value);
            } else if (value instanceof long[]) {
                memberValueHashCode = Arrays.hashCode((long[]) value);
            } else if (value instanceof float[]) {
                memberValueHashCode = Arrays.hashCode((float[]) value);
            } else if (value instanceof double[]) {
                memberValueHashCode = Arrays.hashCode((double[]) value);
            } else if (value instanceof byte[]) {
                memberValueHashCode = Arrays.hashCode((byte[]) value);
            } else if (value instanceof char[]) {
                memberValueHashCode = Arrays.hashCode((char[]) value);
            } else if (value instanceof Object[]) {
                memberValueHashCode = Arrays.hashCode((Object[]) value);
            } else {
                memberValueHashCode = value.hashCode();
            }
            hashCode += memberNameHashCode ^ memberValueHashCode;
        }
        return hashCode;
    }
   
    /**
     * Defines a replacement object for this in a serialized stream 
     * ({@link SerializationProxy}).
     * 
     * @return the serialization proxy to be used instead of this instance
     * @throws ObjectStreamException in case of any problem during serialization
     * 
     * @since 1.00
     */
    private Object writeReplace() throws ObjectStreamException {
        return new SerializationProxy(valueMap, annotationType);
    }
   
    /**
     * Disables typical serialization input as this should be done by 
     * {@link SerializationProxy}.
     * 
     * @param in the input stream
     * @throws IOException in case of any I/O problem
     * @throws ClassNotFoundException in case that the current object cannot be
     *    casted appropriately
     * 
     * @since 1.00
     */
    private void readObject(java.io.ObjectInputStream in) 
        throws IOException, ClassNotFoundException {
        throw new UnsupportedOperationException("Must use SerializationProxy");
    }

    /**
     * Calls a method without parameters (getter) on the given 
     * <code>instance</code>.
     * 
     * @param method the method to be called
     * @param instance the instance on which to call the <code>method</code>
     * @return the result of the execution
     * 
     * @since 1.00
     */
    private static Object invoke(Method method, Object instance) {
        try {
            if (!method.isAccessible()) {
                method.setAccessible(true);
            }
            return method.invoke(instance);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Error checking value of member method " 
                + method.getName() + " on " + method.getDeclaringClass(), e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Error checking value of member method " 
                + method.getName() + " on " + method.getDeclaringClass(), e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException("Error checking value of member method " 
                + method.getName() + " on " + method.getDeclaringClass(), e);
        }
    }

}

