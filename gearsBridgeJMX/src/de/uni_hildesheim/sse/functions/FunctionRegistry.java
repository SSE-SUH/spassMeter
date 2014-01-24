package de.uni_hildesheim.sse.functions;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

/**
 * Central access to all functions.
 * 
 * @author Stephan Dederichs
 * 
 * @since 1.00
 * @version 1.00
 */
public class FunctionRegistry {

    /**
     * stores all available {@link IFunction}s.
     * 
     * @since 1.00
     */
    private static Map<String, IFunction> functionTypes = new HashMap
        <String, IFunction>();

    /**
     * Prevents this class from being created from outside.
     * 
     * @since 1.00
     */
    private FunctionRegistry() {
    }

    /**
     * Method for adding a functions to the registry.
     * 
     * @param className The class name of the function which should be added.
     * @param displayName The display name of the function which should be 
     *            added.
     * 
     * @since 1.00
     */
    public static final void attachFunction(String className, 
        String displayName) {
        // Creating an instance from given class
        IFunction instance = getInstance(className);
        if (null != instance) {
            // setting the functionType
            instance.setFunctionType(displayName);
            // adding the instance to the map
            functionTypes.put(displayName, instance);
        }
    }

    /**
     * Method for adding the standard functions to the registry.
     * 
     * @param className The class name of the function which should be added.
     * 
     * @since 1.00
     */
    private static final void attachFunction(String className) {
        // Creating an instance from given class
        IFunction instance = getInstance(className);
        // adding the instance to the map
        functionTypes.put(instance.getFunctionType(), instance);
    }

    /**
     * Adds the standard functions "min", "max" and "avg" to the registry.
     * 
     * @since 1.00
     */
    public static final void attachStandardFunctions() {
        attachFunction("de.uni_hildesheim.sse.functions.MinFunction");
        attachFunction("de.uni_hildesheim.sse.functions.MaxFunction");
        attachFunction("de.uni_hildesheim.sse.functions.AvgFunction");
    }

    /**
     * Creates an instance of the given classname.
     * 
     * @param className The name of the class which should be instanciated.
     * 
     * @return An instance of the given classname.
     * 
     * @since 1.00
     */
    private static final IFunction getInstance(String className) {
        IFunction function = null;
        try {
            // Creating the class
            Class<?> cls = Class.forName(className);
            // Creating an instance
            function = (IFunction) cls.newInstance();
        } catch (ClassNotFoundException e) {
            Calendar cal = new GregorianCalendar(
                    TimeZone.getTimeZone("GMT+1:00"));
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
            System.out.println("WARNING\t"
                + sdf.format(cal.getTime())
                + " - Function "
                + e.getMessage()
                + " not found. This function will be skipped and"
                + " is not available.");
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return function;
    }

    /**
     * Returns the function addicting to the functionType.
     * 
     * @param functionType The functionType.
     * 
     * @return The function addicting to the functionType.
     * 
     * @since 1.00
     */
    public static final IFunction getFunction(String functionType) {
        IFunction function = functionTypes.get(functionType);
        // Creating a new instance for specific use if the registry contains the
        // functionType
        if (null != function) {
            IFunction newInstance = getInstance(function.getClass()
                    .getCanonicalName());
            newInstance.setFunctionType(function.getFunctionType());
            newInstance.setIdentity(function.getIdentity());
            return newInstance;
        }
        return function;
    }

}
