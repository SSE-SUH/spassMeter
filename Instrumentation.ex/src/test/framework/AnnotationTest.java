package test.framework;

import java.util.Arrays;

import test.AnnotationId;

import de.uni_hildesheim.sse.codeEraser.annotations.Variability;
import de.uni_hildesheim.sse.monitoring.runtime.annotations.*;
import de.uni_hildesheim.sse.monitoring.runtime.boot.DebugState;
import de.uni_hildesheim.sse.monitoring.runtime.utils.
    AnnotationInstanceProvider;
import de.uni_hildesheim.sse.monitoring.runtime.utils.HashMap;

/**
 * Tests the dynamic instantiation of the annotations of the monitoring system.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
@Variability(id = AnnotationId.VAR_TESTING)
public class AnnotationTest {

    /**
     * Stores the annotation provider instance.
     */
    private static final AnnotationInstanceProvider PROVIDER 
        = new AnnotationInstanceProvider(); 

    /**
     * Prevents this class from being instantiated from outside.
     * 
     * 
     * @since 1.00
     */
    private AnnotationTest() {
    }
    
    /**
     * Tests the dynamic instantiation of the {@link Monitor} annotation.
     * 
     * @since 1.00
     */
    private static void testMonitor() {
        HashMap<String, Object> values = new HashMap<String, Object>();
        values.put("id", new String[] {"testAnnotation"});
        DebugState[] debug = {DebugState.FILE_IN, DebugState.FILE_OUT};
        values.put("debug", debug);
        Monitor annotation = PROVIDER.get(Monitor.class, values);
        if (null == annotation) {
            System.err.println("Monitor annotation not created");
        } else {
            if (!Arrays.equals((String[]) values.get("id"), annotation.id())) {
                System.err.println("\"id\" not identical " 
                    + Arrays.toString(annotation.id())
                    + " " + values.get("id"));
            } else if (!Arrays.equals(debug, annotation.debug())) {
                System.err.println("\"debug\" not identical " + annotation);
            } else {
                System.out.println("Monitor ok");
            }
        }
    }
    
    /**
     * Tests the dynamic instantiation of the {@link Timer} annotation.
     * 
     * @since 1.00
     */
    private static void testTimer() {
        HashMap<String, Object> values = new HashMap<String, Object>();
        values.put("id", "testTimer");
        values.put("state", TimerState.RESUME);
        values.put("affectAt", TimerPosition.BOTH);
        values.put("considerThreads", Boolean.TRUE);
        Timer annotation = PROVIDER.get(Timer.class, values);
        if (null == annotation) {
            System.err.println("Timer annotation not created");
        } else {
            if (!values.get("id").equals(annotation.id())) {
                System.err.println("\"id\" not identical");
            } else if (!values.get("state").equals(annotation.state())) {
                System.err.println("\"state\" not identical");
            } else if (!values.get("affectAt").equals(annotation.affectAt())) {
                System.err.println("\"affectAt\" not identical");
            } else if (!values.get("considerThreads").equals(annotation.
                considerThreads())) {
                System.err.println("\"considerThreads\" not identical");
            } else {
                System.out.println("Timer ok");
            }
        }
    }

    /**
     * Tests the dynamic instantiation of the {@link ConfigurationChange} 
     * annotation.
     * 
     * @since 1.00
     */
    private static void testConfigurationChange() {
        HashMap<String, Object> values = new HashMap<String, Object>();
        values.put("idExpression", "SHUTDOWN");
        values.put("valueExpression", "String.valueOf(param)");
        ConfigurationChange annotation 
            = PROVIDER.get(ConfigurationChange.class, values);
        if (null == annotation) {
            System.err.println("ConfigurationChange annotation not created");
        } else {
            if (!values.get("idExpression").equals(annotation.idExpression())) {
                System.err.println("\"idExpression\" not identical");
            } else if (!values.get("valueExpression").equals(
                annotation.valueExpression())) {
                System.err.println("\"valueExpression\" not identical");
            } else {
                System.out.println("ConfigurationChange ok");
            }
        }
    }

    /**
     * Tests the dynamic instantiation of the {@link EndSystem} 
     * annotation.
     * 
     * @since 1.00
     */
    private static void testEndSystem() {
        HashMap<String, Object> values = new HashMap<String, Object>();
        EndSystem annotation = PROVIDER.get(EndSystem.class, values);
        if (null == annotation) {
            System.err.println("EndSystem annotation not created");
        } else {
            System.out.println("EndSystem ok");
        }
    }

    /**
     * Tests the dynamic instantiation of the {@link ExcludeFromMonitoring} 
     * annotation.
     * 
     * @since 1.00
     */
    private static void testExcludeFromMonitoring() {
        HashMap<String, Object> values = new HashMap<String, Object>();
        ExcludeFromMonitoring annotation 
            = PROVIDER.get(ExcludeFromMonitoring.class, values);
        if (null == annotation) {
            System.err.println("ExcludeFromMonitoring annotation not created");
        } else {
            System.out.println("ExcludeFromMonitoring ok");
        }
    }

    /**
     * Tests the dynamic instantiation of the {@link VariabilityHandler} 
     * annotation.
     * 
     * @since 1.00
     */
    private static void testVariabilityHandler() {
        HashMap<String, Object> values = new HashMap<String, Object>();
        VariabilityHandler annotation 
            = PROVIDER.get(VariabilityHandler.class, values);
        if (null == annotation) {
            System.err.println("VariabilityHandler annotation not created");
        } else {
            System.out.println("VariabilityHandler ok");
        }
    }
    
    /**
     * Tests the dynamic instantiation of the {@link NotifyValue} 
     * annotation.
     * 
     * @since 1.00
     */
    private static void testNotifyValue() {
        HashMap<String, Object> values = new HashMap<String, Object>();
        values.put("id", "notifyValue");
        values.put("value", MeasurementValue.FILE_OUT);
        values.put("notifyDifference", Boolean.TRUE);
        NotifyValue annotation 
            = PROVIDER.get(NotifyValue.class, values);
        if (null == annotation) {
            System.err.println("NotifyValue annotation not created");
        } else {
            if (!values.get("id").equals(annotation.id())) {
                System.err.println("\"id\" not identical");
            } else if (!values.get("value").equals(annotation.value())) {
                System.err.println("\"value\" not identical");
            } else if (!values.get("notifyDifference").equals(
                annotation.notifyDifference())) {
                System.err.println("\"notifyDifference\" not identical");
            } else {
                System.out.println("NotifyValue ok");
            }
        }
    }

    /**
     * Tests the dynamic instantiation of the {@link StartSystem} 
     * annotation.
     * 
     * @since 1.00
     */
    private static void testStartSystem() {
        HashMap<String, Object> values = new HashMap<String, Object>();
        values.put("statisticsInterval", 1025);
        try {
            // use default value
            values.put("swingTimer", 
                StartSystem.class.getMethod("swingTimer").getDefaultValue());
        } catch (NoSuchMethodException e) {
            System.err.println("cannot get swingTimer");
        }
        StartSystem annotation = PROVIDER.get(StartSystem.class, values);
        if (null == annotation) {
            System.err.println("StartSystem annotation not created");
        } else {
            if (!values.get("statisticsInterval").equals(
                annotation.statisticsInterval())) {
                System.err.println("\"statisticsInterval\" not identical");
            } else if (!values.get("swingTimer").equals(
                annotation.swingTimer())) {
                System.err.println("\"swingTimer\" not identical");
            } else {
                System.out.println("StartSystem ok");
            }
        }
    }

    /**
     * Tests the dynamic instantiation of the {@link ValueChange} 
     * annotation.
     * 
     * @since 1.00
     */
    private static void testValueChange() {
        HashMap<String, Object> values = new HashMap<String, Object>();
        values.put("id", "valueChange");
        ValueChange annotation 
            = PROVIDER.get(ValueChange.class, values);
        if (null == annotation) {
            System.err.println("ValueChange annotation not created");
        } else {
            if (!values.get("id").equals(annotation.id())) {
                System.err.println("\"id\" not identical");
            } else {
                System.out.println("ValueChange ok");
            }
        }
    }
    
    /**
     * Executes the tests.
     * 
     * @since 1.00
     */
    private static void execute() {
        testMonitor();
        testTimer();
        testConfigurationChange();
        testEndSystem();
        testExcludeFromMonitoring();
        testVariabilityHandler();
        testNotifyValue();
        testStartSystem();
        testValueChange();
    }

    /**
     * Starts the tests.
     * 
     * @param args ignored
     * 
     * @since 1.00
     */
    @SuppressWarnings("static-access")
    public static void main(String[] args) {
        System.out.println(AnnotationTest.class.getName());
        if (args.length > 0) {
            if (args[0].equals("continue")) {
                for (int i = 0; i < 20; i++) {
                    execute();
                    try {
                        Thread.currentThread().sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                System.out.println("invalid parameter: " + args[0]);
            }
        } else {
            execute();
        }
        System.out.println("------------------ done: AnnotationTest");
    }
    
}
