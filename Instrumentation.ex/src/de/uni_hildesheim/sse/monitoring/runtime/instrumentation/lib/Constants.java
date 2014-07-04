package de.uni_hildesheim.sse.monitoring.runtime.instrumentation.lib;

/**
 * Some basic instrumentation constants such as class names.
 * 
 * @author Holger Eichelberger
 * @since 1.05
 * @version 1.05
 */
public class Constants {

    /**
     * The class name of "{@value}".
     */
    public static final String JAVA_LANG_THREAD = "java.lang.Thread";

    /**
     * The class name of "{@value}".
     */
    public static final String JAVA_LANG_RUNNABLE = "java.lang.Runnable";

    /**
     * The class name of "{@value}".
     */
    public static final String JAVA_LANG_STRING = "java.lang.String";

    /**
     * The class name of "{@value}".
     */
    public static final String JAVA_IO_SERIALIZABLE = "java.io.Serializable";
    
    /**
     * The class name suffix for one array dimension.
     */
    public static final String ARRAY_SUFFIX = "[]";

    /**
     * The name of the serial version field "{@value}".
     */
    public static final String SERIAL_VERSION_FIELD_NAME = "serialVersionUID";

    /**
     * The class name of "{@value}".
     */
    public static final String JAVA_LANG_STRING_ARRAY1 
        = JAVA_LANG_STRING + ARRAY_SUFFIX;

    /**
     * Prevents external creation.
     * 
     * @since 1.05
     */
    private Constants() {
    }
    
    

}
