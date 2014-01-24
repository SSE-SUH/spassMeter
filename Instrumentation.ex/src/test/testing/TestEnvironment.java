package test.testing;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import test.AnnotationId;

import de.uni_hildesheim.sse.codeEraser.annotations.Variability;
import de.uni_hildesheim.sse.system.GathererFactory;

/**
 * A class for executing tests on recorded monitoring groups. Use reflection for
 * accessing the relevant classes as not all interfaces are made available due
 * to technical restrictions of the JVM boot/class loading process.
 *
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
@Variability(id = AnnotationId.VAR_TESTING)
public class TestEnvironment {
    
    /**
     * Enable or disable debugging of testing, e.g. printing additional
     * notices.
     * 
     * @see #notice(String)
     */
    public static final boolean DEBUG = false;
    
    /**
     * Stores whether this class is initialized.
     */
    private static boolean initialized = false;
    
    /**
     * Stores the program recorder identification.
     */
    private static String programId;

    /**
     * Stores the overhead recorder identification.
     */
    private static String overheadId;
    
    /**
     * Stores the test plugin which handles the access to the data.
     */
    private static AbstractTestPlugin plugin;
    
    /**
     * Stores the selected timer data.
     */
    private static Map<String, Long> storedTimerData 
        = new HashMap<String, Long>();
    
    /**
     * Prevents this class from being instantiated from outside.
     * 
     * @since 1.00
     */
    protected TestEnvironment() {
    }
    
    /**
     * A method to handle exceptions intended to enable/disable trace output.
     * 
     * @param text the text to be emitted (in any case)
     * @param exception the exception describing the problem which may 
     *    be emitted
     * 
     * @since 1.00
     */
    private static void handleException(String text, Exception exception) {
        System.out.println(text);
        exception.printStackTrace(System.out);
    }
    
    /**
     * Prints a notice during testing (only in case of {@link #DEBUG}). 
     * 
     * @param notice the text to be printed
     * 
     * @see #DEBUG
     * 
     * @since 1.00
     */
    public static final void notice(String notice) {
        if (DEBUG) {
            System.out.println(notice);
        }
    }
    
    /**
     * Tries to register the given plugin.
     * 
     * @param className the name of the class to be registered
     * @param logger the logger to be used (may be <b>null</b>)
     * @param notice print a notice in case of missing classes
     * 
     * @since 1.00
     */
    private static void registerPlugin(String className, ILogger logger, 
        boolean notice) {
        try {
            Class<?> pluginClass = Class.forName(className);
            if (AbstractTestPlugin.class.isAssignableFrom(pluginClass)) {
                Constructor<?> cons = pluginClass.getConstructor(ILogger.class);
                plugin = (AbstractTestPlugin) cons.newInstance(logger);
            }
        } catch (ClassNotFoundException e) {
            notice("Plugin class " + className + " not found");
        } catch (SecurityException e) {
            notice("Plugin class " + className 
                + " cannnot be instantiated (security)");
        } catch (NoSuchMethodException e) {
            notice("Plugin class " + className 
                + " cannnot be instantiated (constructor missing)");
        } catch (IllegalArgumentException e) {
            notice("Plugin class " + className 
                + " cannnot be instantiated (argument incorrect)");
        } catch (InstantiationException e) {
            notice("Plugin class " + className 
                + " cannnot be instantiated");
        } catch (IllegalAccessException e) {
            notice("Plugin class " + className 
                + " cannnot be accessed (constructor)");
        } catch (InvocationTargetException e) {
            notice("Plugin class " + className 
                + " cannnot be instantiated (functional)");
        }
    }
        
    /**
     * Initializes the attributes of this class (lazy initialization).
     * 
     * @return whether this test class was initialized (at all)
     * 
     * @since 1.00
     */
    public static boolean initialize() {
        if (!initialized) {
            ILogger logger = new ILogger() {
                
                @Override
                public void notice(String notice) {
                    TestEnvironment.notice(notice);
                }
                
                @Override
                public void exception(String text, Exception exception) {
                    TestEnvironment.handleException(text, exception);
                }
            };

            // TODO @Holger: done :)
            registerPlugin("de.uni_hildesheim.sse.jmx.test." 
                + "JMXTestPlugin", logger, false);
            registerPlugin("de.uni_hildesheim.sse.wildcat.test." 
                + "WildCATTestPlugin", logger, false);
            String tmp = System.getProperty("spassmeter-test.plugin");
            if (null != tmp) {
                registerPlugin(tmp, logger, false);
            }
            if (null == plugin) {
                plugin = new DefaultTestPlugin(logger);
            }
            
            try {
                // get constants
                
                Class<?> cls = Class.forName("de.uni_hildesheim.sse." 
                    + "monitoring.runtime.annotations.Helper");
                Field f = cls.getDeclaredField("PROGRAM_ID");
                programId = (String) f.get(null);
                f = cls.getDeclaredField("RECORDER_ID");
                overheadId = (String) f.get(null);
                
                // do details in plugin
                
                plugin.initialize();

                // mark as initialized regardless if failed or not
                initialized = true;
            } catch (ClassNotFoundException e) {
                handleException(ILogger.CLASS_NOT_FOUND, e);
            } catch (SecurityException e) {
                handleException(ILogger.CANNOT_CALL, e);
            } catch (NoSuchFieldException e) {
                handleException(ILogger.CANNOT_CALL, e);
            } catch (IllegalArgumentException e) {
                handleException(ILogger.CANNOT_CALL, e);
            } catch (IllegalAccessException e) {
                handleException(ILogger.CANNOT_CALL, e);
            }
        }
        return initialized;
    }
    
    /**
     * Finishes testing with a failure.
     * 
     * @since 1.00
     */
    private static void stopAndFail() {
        Throwable t = new Throwable();
        StackTraceElement[] trace = t.getStackTrace();
        if (null != trace) {
            String pkg = TestEnvironment.class.getPackage().getName(); 
            int i = 0;
            while (i < trace.length 
                && trace[i].getClassName().startsWith(pkg)) {
                i++;
            }
            if (i < trace.length) {
                System.out.println(trace[i]);
            }
        }
        System.exit(-1);
    }
    
    /**
     * Returns the recorder identification for the entire program being 
     * monitored.
     * 
     * @return the recorder identification or <b>null</b> in case that the test
     *   environment was not properly initialized
     * 
     * @since 1.00
     */
    public static String getProgramId() {
        return programId;
    }

    /**
     * Returns the identification for the overhead of the framework .
     * 
     * @return the recorder identification or <b>null</b> in case that the test
     *   environment was not properly initialized
     * 
     * @since 1.00
     */
    public static String getOverheadId() {
        return overheadId;
    }
    
    /**
     * Used to notify about the end of the tests and, thus, the success
     * of the test case.
     * 
     * @param recId the recorder identification (of the test)
     * 
     * @since 1.00
     */
    public static void success(String recId) {
        System.out.println("SUCCESS");
    }
    
    /**
     * Returns the new value of a changed attribute.
     * 
     * @param recId the recorder identification of the attribute
     * @return the new value, may be <b>null</b>
     * 
     * @since 1.00
     */
    public static Object getAttributeData(String recId) {
        Object result = null;
        if (initialize()) {
            if (null != plugin) {
                result = plugin.getAttributeData(recId);
            }
        }
        return result;
    }
    
    /**
     * Asserts a given value in a monitoring group for equality (without 
     * any tolerance).
     * 
     * @param recId the recorder identification
     * @param value the value to examine 
     * @param expected the expected value
     * 
     * @since 1.00
     */
    public static void assertEquals(String recId, MonitoringGroupValue value, 
        long expected) {
        assertEquals(recId, value, expected, 0);
    }
    
    /**
     * Asserts a given value in a monitoring group for equality with given 
     * tolerance.
     * 
     * @param recId the recorder identification
     * @param value the value to examine 
     * @param expected the expected value
     * @param tolerance a value indicating an allowed positive / negative 
     *   range around <code>expected</code>
     * 
     * @since 1.00
     */
    public static void assertEquals(String recId, MonitoringGroupValue value, 
        long expected, long tolerance) {
        if (initialize()) {
            if (null != plugin) {
                Long recResult = plugin.getValue(recId, value);
                evaluateEquals(recId, recResult, expected, tolerance, " for " 
                    + value + " ");
            }
        }
    }
    
    /**
     * Asserts a given value in a monitoring group for equality with given 
     * tolerance in percent.
     * 
     * @param recId the recorder identification
     * @param value the value to examine 
     * @param expected the expected value
     * @param tolerance in percent of <code>expected</code> indicating an 
     *   allowed positive / negative range around <code>expected</code>
     * 
     * @since 1.00
     */
    public static void assertEquals(String recId, MonitoringGroupValue value, 
        long expected, double tolerance) {
        if (initialize()) {
            if (null != plugin) {
                Long recResult = plugin.getValue(recId, value);
                long tol = (long) (expected * tolerance);
                evaluateEquals(recId, recResult, expected, tol, " for " 
                    + value + " ");
            }
        }
    }
    
    /**
     * Returns the specified value from a monitoring group.
     * 
     * @param recId the recorder identification
     * @param value the value to return
     * @return the value or <b>null</b> if not available
     * 
     * @since 1.00
     */
    public static Long getValue(String recId, MonitoringGroupValue value) {
        Long result = null;
        if (initialize()) {
            if (null != plugin) {
                result = plugin.getValue(recId, value);
            }
        }
        return result;
    }

    /**
     * Asserts that a given value in a monitoring group is greater than (or 
     * equal to) a given value.
     * 
     * @param recId the recorder identification
     * @param value the value to examine 
     * @param min the lower bound (expected value)
     * 
     * @since 1.00
     */
    public static void assertGreater(String recId, MonitoringGroupValue value, 
        long min) {
        assertGreaterOrEquals(recId, value, min, false);
    }
    
    /**
     * Asserts that a given value in a monitoring group is greater than (or 
     * equal to) a given value.
     * 
     * @param recId the recorder identification
     * @param value the value to examine 
     * @param min the lower bound (expected value)
     * 
     * @since 1.00
     */
    public static void assertGreaterEquals(String recId, 
        MonitoringGroupValue value, long min) {
        assertGreaterOrEquals(recId, value, min, true);
    }
    
    /**
     * Asserts that a given value in a monitoring group is greater than (or 
     * equal to) a given value.
     * 
     * @param recId the recorder identification
     * @param value the value to examine 
     * @param min the lower bound (expected value)
     * @param includeEquals <code>true</code> if <code>min</code> is valid, 
     *   <code>false</code> else
     * 
     * @since 1.00
     */
    private static void assertGreaterOrEquals(String recId, 
        MonitoringGroupValue value, long min, boolean includeEquals) {
        if (initialize()) {
            if (null != plugin) {
                String msg = ""; 
                Long recResult = plugin.getValue(recId, value);
                if (null != recResult) {
                    if (includeEquals) {
                        if (recResult < min) {
                            msg = ">="; 
                        }
                    } else {
                        if (recResult <= min) {
                            msg = ">"; 
                        }
                    }
                    if (msg.length() > 0) {
                        msg = " (" + recResult + msg + min + ") does not hold.";
                    }
                } else {
                    msg = "no data";
                }
                if (msg.length() > 0) {
                    System.out.println(recId + ": "
                        + recResult + msg);
                    stopAndFail();
                }
            }
        }
    }

    /**
     * Asserts that a given value in a monitoring group is less than (or 
     * equal to) a given value.
     * 
     * @param recId the recorder identification
     * @param value the value to examine 
     * @param max the upper bound (expected value)
     * 
     * @since 1.00
     */
    public static void assertSmaller(String recId, MonitoringGroupValue value, 
        long max) {
        assertSmallerOrEquals(recId, value, max, false);
    }

    /**
     * Asserts that a given value in a monitoring group is less than (or 
     * equal to) a given value.
     * 
     * @param recId the recorder identification
     * @param value the value to examine 
     * @param max the upper bound (expected value)
     * 
     * @since 1.00
     */
    public static void assertSmallerEquals(String recId, 
        MonitoringGroupValue value, long max) {
        assertSmallerOrEquals(recId, value, max, true);
    }
    
    /**
     * Asserts that a given value in a monitoring group is less than (or 
     * equal to) a given value.
     * 
     * @param recId the recorder identification
     * @param value the value to examine 
     * @param max the upper bound (expected value)
     * @param includeEquals <code>true</code> if <code>max</code> is valid, 
     *   <code>false</code> else
     * 
     * @since 1.00
     */
    private static void assertSmallerOrEquals(String recId, 
        MonitoringGroupValue value, long max, boolean includeEquals) {
        if (initialize()) {
            if (null != plugin) {
                String msg = ""; 
                Long recResult = plugin.getValue(recId, value);
                if (null != recResult) {
                    if (includeEquals) {
                        if (recResult > max) {
                            msg = "<="; 
                        }
                    } else {
                        if (recResult >= max) {
                            msg = "<"; 
                        }
                    }
                    if (msg.length() > 0) {
                        msg = " (" + recResult + msg + max + ") does not hold.";
                    }
                } else {
                    msg = "no data";
                }
                if (msg.length() > 0) {
                    System.out.println(recId + ": "
                        + recResult + msg);
                    stopAndFail();
                }
            }
        }
    }
    
    /**
     * Asserts a given value of a timer for equality (without 
     * any tolerance).
     * 
     * @param recId the recorder identification
     * @param expected the expected value
     * 
     * @since 1.00
     */
    public static void assertEqualsTimer(String recId, 
        long expected) {
        assertEqualsTimer(recId, expected, 0);
    }

    /**
     * Asserts a given value of a timer for equality with given 
     * tolerance.
     * 
     * @param recId the recorder identification, may also be a synthetic
     *   identification returned by {@link #storeTimerData(String)}
     * @param expected the expected value
     * @param tolerance a value indicating an allowed positive / negative 
     *   range around <code>expected</code>
     * 
     * @since 1.00
     */
    public static void assertEqualsTimer(String recId, 
        long expected, long tolerance) {
        if (initialize()) {
            Long recResult = null;
            if (null != recId) {
                // be careful - synthetic recId might not be used correctly
                if (storedTimerData.containsKey(recId)) {
                    recResult = storedTimerData.get(recId);
                } else {
                    if (null != plugin) {
                        recResult = plugin.getTimerData(recId);
                    }
                }
            }
            evaluateEquals(recId, recResult, expected, tolerance, "");
        }
    }

    /**
     * Asserts the <code>test</code> value to not null.
     * 
     * @param recId the recorder identification to test for
     * @param test the value to test
     * 
     * @since 1.00
     */
    public static void assertNotNull(String recId, Object test) {
        if (null == test) {
            System.out.println(recId + ": assert to not null failed");
            stopAndFail();
        }
    }

    /**
     * Asserts the <code>test</code> value to null.
     * 
     * @param recId the recorder identification to test for
     * @param test the value to test
     * 
     * @since 1.00
     */
    public static void assertNull(String recId, Object test) {
        if (null != test) {
            System.out.println(recId + ": assert to null failed");
            stopAndFail();
        }
    }
    
    /**
     * Asserts the <code>test</code> value to true.
     * 
     * @param recId the recorder identification to test for
     * @param test the value to test
     * 
     * @since 1.00
     */
    public static void assertTrue(String recId, boolean test) {
        if (!test) {
            System.out.println(recId + ": assert to true failed");
            stopAndFail();
        }
    }

    /**
     * Asserts the <code>test</code> values to be equal.
     * 
     * @param recId the recorder identification to test for
     * @param value the value to be tested
     * @param expected the expected value
     * 
     * @since 1.00
     */
    public static void assertEquals(String recId, long value, 
        long expected) {
        if (value != expected) {
            System.out.println(recId + ": assert " + value + "=" + expected 
                + " failed");
            stopAndFail();
        }
    }
    
    /**
     * Asserts the <code>test</code> values to be equal with respect to 
     * <code>tolerance</code>.
     * 
     * @param recId the recorder identification to test for
     * @param value the value to be tested
     * @param expected the expected value
     * @param tolerance the allowed difference between <code>value</code> and 
     *     <code>expected</code>
     * 
     * @since 1.00
     */
    public static void assertEquals(String recId, double value, 
        double expected, double tolerance) {
        if (Math.abs(value - expected) > tolerance) {
            System.out.println(recId + ": assert |" + value + "-" + expected 
                + "| <= " + tolerance + " failed");
            stopAndFail();
        }
    }
    
    /**
     * Asserts the <code>object</code> to be instance of <code>cls</code>.
     * 
     * @param recId the recorder identification to test for
     * @param object to check the type for
     * @param cls the type to comply with
     * 
     * @since 1.00
     */
    public static void assertType(String recId, Object object, 
        Class<?> cls) {
        if (!cls.isInstance(object)) {
            System.out.println(recId + ": type assertion to " + cls.getName() 
                + " failed for " + object);
            stopAndFail();
        }
    }

    /**
     * Asserts that the specified <code>value</code> was recorded.
     * 
     * @param recId the recorder identification of the monitoring group
     * @param value the value to be returned
     * 
     * @since 1.00
     */
    public static void assertHasValue(String recId, 
        MonitoringGroupValue value) {
        if (initialize()) {
            if (null == plugin.getValue(recId, value)) {
                System.out.println(recId + ": value " + value 
                    + " does not exist");
                stopAndFail();
            }
        }
    }

    /**
     * Asserts that the specified <code>value</code> was not recorded.
     * 
     * @param recId the recorder identification of the monitoring group
     * @param value the value to be returned
     * 
     * @since 1.00
     */
    public static void assertHasNoValue(String recId,
        MonitoringGroupValue value) {
        if (initialize()) {
            if (null != plugin.getValue(recId, value)) {
                System.out.println(recId + ": value " + value 
                    + " does exist");
                stopAndFail();
            }
        }
    }
    
    /**
     * Evaluates the given result from monitoring.
     * 
     * @param recId the recorder identification
     * @param recResult the result from monitoring
     * @param expected the expected value
     * @param tolerance the tolerance
     * @param text additional text to be emitted in case of a mismatch
     * 
     * @since 1.00
     */
    private static void evaluateEquals(String recId, Long recResult, 
        long expected, long tolerance, String text) {
        String msg = "";
        if (null != recResult) {
            long lower = expected - tolerance;
            long upper = expected + tolerance;
            if (recResult < lower) {
                if (0 == tolerance) {
                    msg = "does not match expected " 
                        + expected;
                } else {
                    msg = "is below expected tolerance (" 
                        + expected + " < " + lower + " by "
                        + Math.abs(recResult - lower) + ")";
                }
            } else if (recResult > upper) {
                if (0 == tolerance) {
                    msg = "does not match expected " 
                        + expected;
                } else {
                    msg = "is above expected tolerance (" 
                        + expected + " > " + upper + " by " 
                        + Math.abs(recResult - upper) + ")";
                }
            }
        } else {
            msg = " no data";
        }
        
        if (msg.length() > 0) {
            System.out.println(recId + ": "
                + recResult + text + msg);
            stopAndFail();
        }
    }
    
    /**
     * Stores the current value of the specified timer and returns a generated
     * (synthetic) recorder id which points to that data.
     * 
     * @param recId the recorder identification of the timer
     * @return the synthetic recorder identification for the current value 
     *   of the timer 
     * 
     * @since 1.00
     */
    public static String storeTimerData(String recId) {
        Long data = null;
        do {
            if (null != plugin) {
                data = plugin.getTimerData(recId);
            }
            if (null == data) {
                // it may happen that the test calls this method before
                // the recorder running in parallel has notified the listener
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                }
            }
        } while (null == data);
        String key = recId + "-" + System.currentTimeMillis();
        storedTimerData.put(key, data);
        return key;
    }
    
    /**
     * Returns the size of the given object as allocated by the JVM.
     * 
     * @param object the object the size should be queried for
     * @return the size of the physical memory allocated for <code>object</code>
     *         (in bytes, negative or zero if invalid or not available)
     * 
     * @since 1.00
     */
    public static long getObjectSize(Object object) {
        return GathererFactory.getMemoryDataGatherer().getObjectSize(object);
    }
    
    /**
     * Returns if this test is an indirect test (indirect accounting), i.e. 
     * <code>-Dindirect=true</code> is given as JVM parameter.
     * 
     * @return <code>true</code> if indirect, <code>false</code> else
     * 
     * @since 1.00
     */
    public static boolean isIndirectTest() {
        return System.getProperty("indirect", "").equals("true");
    }
    
    /**
     * Returns whether this test plugin supports configurations.
     * 
     * @return <code>true</code> if it supports configurations, 
     *   <code>false</code> else
     * 
     * @since 1.00
     */
    public static boolean supportsConfigurations() {
        return plugin.supportsConfigurations();
    }
    
    /**
     * Returns the number of configurations recorder.
     * 
     * @return the number of configurations
     * 
     * @since 1.00
     */
    public static int getNumberOfConfigurations() {
        return plugin.getNumberOfConfigurations();
    }

    /**
     * Returns the recording identifier of the specific configuration.
     * 
     * @param index the index of the configuration
     * @return the recording identifier (consisting of the names of the 
     *   individual recorder ids separated by ",")
     * @throws IndexOutOfBoundsException if <code>index &lt; 0 
     *   || index &gt;={@link #getNumberOfConfigurations()}</code>
     * 
     * @since 1.00
     */
    public static String getConfigurationId(int index) {
        return plugin.getConfigurationId(index);
    }

    /**
     * Returns the recorded value of the specific configuration.
     * 
     * @param index the index of the configuration
     * @param value the value to be returned
     * @return the value or <b>null</b> if not accessible etc.
     * @throws IndexOutOfBoundsException if <code>index &lt; 0 
     *   || index &gt;={@link #getNumberOfConfigurations()}</code>
     * 
     * @since 1.00
     */
    public static Long getConfigurationValue(int index, 
        MonitoringGroupValue value) {
        return plugin.getConfigurationValue(index, value);
    }

    /**
     * Asserts a given value in a configuration for equality (without 
     * any tolerance).
     * 
     * @param index the index of the configuration
     * @param value the value to examine 
     * @param expected the expected value
     * @throws IndexOutOfBoundsException if <code>index &lt; 0 
     *   || index &gt;={@link #getNumberOfConfigurations()}</code>
     * 
     * @since 1.00
     */
    public static void assertEquals(int index, MonitoringGroupValue value, 
        long expected) {
        assertEquals(index, value, expected, 0);
    }

    /**
     * Asserts a given value in a configuration for equality with given 
     * tolerance.
     * 
     * @param index the index of the configuration
     * @param value the value to examine 
     * @param expected the expected value
     * @param tolerance a value indicating an allowed positive / negative 
     *   range around <code>expected</code>
     * @throws IndexOutOfBoundsException if <code>index &lt; 0 
     *   || index &gt;={@link #getNumberOfConfigurations()}</code>
     * 
     * @since 1.00
     */
    public static void assertEquals(int index, MonitoringGroupValue value, 
        long expected, long tolerance) {
        if (initialize()) {
            if (null != plugin) {
                String recId = plugin.getConfigurationId(index);
                Long recResult = plugin.getConfigurationValue(index, value);
                evaluateEquals(recId, recResult, expected, tolerance, " for " 
                    + value + " ");
            }
        }
    }

}
