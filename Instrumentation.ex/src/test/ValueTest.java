package test;

import test.testing.TestEnvironment;
import de.uni_hildesheim.sse.codeEraser.annotations.Variability;
import de.uni_hildesheim.sse.monitoring.runtime.annotations.EndSystem;
import de.uni_hildesheim.sse.monitoring.runtime.annotations.MeasurementValue;
import de.uni_hildesheim.sse.monitoring.runtime.annotations.NotifyValue;
import de.uni_hildesheim.sse.monitoring.runtime.annotations.StartSystem;
import de.uni_hildesheim.sse.monitoring.runtime.annotations.ValueChange;
import de.uni_hildesheim.sse.monitoring.runtime.annotations.ValueContext;

/**
 * Performs some specific tests recording values, here contextualized recording
 * ids and values in external libs.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
@Variability(id = AnnotationId.VAR_TESTING)
public class ValueTest {

    /**
     * Stores the fixed value to be set to the dummy attribute.
     */
    public static final double MY_DUMMY_VALUE = 100;
    
    /**
     * Stores the recorder identification for the average value context.
     */
    private static final String ID_AVERAGE = "myAverage";

    /**
     * Stores the recorder identification for the dummy value.
     */
    private static final String ID_DUMMY = "myDummy";

    /**
     * Stores the recorder identification postfix for the count attribute (in 
     * context).
     */
    private static final String ID_COUNT_POSTFIX = "-count";
    
    /**
     * Stores the count of values (to validate the average).
     */
    private static int count = 0;
    
    /**
     * Stores the sum of values (to validate the average).
     */
    private static double sum = 0;
        
    /**
     * Defines a class for incrementally updating values. Here, the problem
     * is that ids should be based on instances rather than on static 
     * information. Therefore, we tag the attribute with 
     * 
     * @author eichelberger
     * @since 1.00
     * @version 1.00
     */
    private class MovingAverage {
        
        /**
         * The incrementally updated value. The annotation says that value
         * changes should directly be accounted to the id of the current 
         * (call) context.
         */
        @ValueChange(id = "*")
        private double value;

        /**
         * The number of updates. The annotation says that value
         * changes should be accounted to the id of the current 
         * (call) context plus suffix "-count".
         */
        @ValueChange(id = "*" + ID_COUNT_POSTFIX)
        private int count;
        
        /**
         * Updates the currently stored average value by considering 
         * <code>increment</code> as an additional value.
         * 
         * @param increment the additional value
         * @return the new average
         * 
         * @since 1.00
         */
        public double update(double increment) {
            if (0 == count) {
                value = increment;
                count++;
            } else {
                value = ((value * count) + increment) / ++count;
            }
            return value;
        }
        
        /**
         * Returns the number of updates.
         * 
         * @return the number of updates
         * 
         * @since 1.00
         */
        @SuppressWarnings("unused")
        public int getCount() {
            return count;
        }
        
        /**
         * Returns the current average.
         * 
         * @return the current average
         * 
         * @since 1.00
         */
        public double getValue() {
            return value;
        }
    }
    
    /**
     * Represents an external class which cannot be annotated.
     * 
     * @author Holger Eichelberger
     * @since 1.00
     * @version 1.00
     */
    private static class ExternalLibDummy {
        
        /**
         * Stores an arbitrary value.
         */
        private double value;
        
        /**
         * Changes the value.
         * 
         * @param value the new value
         * 
         * @since 1.00
         */
        public void setValue(double value) {
            this.value = value;
        }
        
        /**
         * Returns the stored value.
         * 
         * @return the value
         * 
         * @since 1.00
         */
        @SuppressWarnings("unused")
        public double getValue() {
            return value;
        }
    }

    /**
     * Stores the reference to the external lib. Here no context is needed
     * because context works only on subsequently annotated classes.
     */
    private static ExternalLibDummy myDummy = new ExternalLibDummy();
    
    /**
     * Stores the reference to the moving average value. This is annotated
     * by a context annotation so that every call to this object will 
     * replace the contextualizing id (above) by "myAverage".
     */
    @ValueContext(id = ID_AVERAGE)
    private MovingAverage myAverage = new MovingAverage();

    /**
     * Prevents this class from being instantiated from outside.
     * 
     * @since 1.00
     */
    private ValueTest() {
    }
    
    /**
     * Simulates a call to the external unannotated class and notifies about
     * a value change.
     * 
     * @since 1.00
     */
    @NotifyValue(id = ID_DUMMY, expression = "myDummy.getValue()", 
        notifyDifference = false, value = MeasurementValue.VALUE)
    private static void testExternal() {
        myDummy.setValue(MY_DUMMY_VALUE);
    }
    
    /**
     * Executes the test.
     * 
     * @since 1.00
     */
    private static void execute() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ValueTest test = new ValueTest();
        test.myAverage.update(10);
        sum += 10;
        count++;
        TestEnvironment.notice(String.valueOf(test.myAverage.getValue()));
        test.myAverage.update(5);
        sum += 5;
        count++;
        TestEnvironment.notice(String.valueOf(test.myAverage.getValue()));
        test.myAverage.update(20);
        sum += 20;
        count++;
        TestEnvironment.notice(String.valueOf(test.myAverage.getValue()));

        testExternal();
    }
    
    /**
     * Starts the tests.
     * 
     * @param args ignored
     * 
     * @since 1.00
     */
    @StartSystem
    @EndSystem(invoke = "asserts")
    public static void main(String[] args) {
        TestEnvironment.initialize(); // explicit due to events
        TestEnvironment.notice(ValueTest.class.getName());
        if (args.length > 0) {
            if (args[0].equals("continue")) {
                for (int i = 0; i < 20; i++) {
                    execute();    
                }
            } else {
                System.out.println("invalid parameter: " + args[0]);
            }
        } else {
            execute();
        }
        TestEnvironment.notice("------------------ done: ValueTest");
    }

    /**
     * The automated tests using assert statements. This method is called
     * by SPASS-meter at end of monitoring upon the <code>EndSystem</code> 
     * annotation.
     * 
     * @since 1.00
     */
    public static void asserts() {
        String recId = ValueTest.class.getName();
        Object tmp = TestEnvironment.getAttributeData(ID_DUMMY);
        TestEnvironment.assertType(recId, tmp, Double.class);
        TestEnvironment.assertEquals(recId,
            MY_DUMMY_VALUE, ((Double) tmp).doubleValue(), 0.001);

        tmp = TestEnvironment.getAttributeData(ID_AVERAGE);
        TestEnvironment.assertType(recId, tmp, Double.class);
        TestEnvironment.assertEquals(recId,
            sum / count, ((Double) tmp).doubleValue(), 0.0001);
        
        tmp = TestEnvironment.getAttributeData(ID_AVERAGE + ID_COUNT_POSTFIX);
        TestEnvironment.assertType(recId, tmp, Integer.class);
        TestEnvironment.assertEquals(recId, ((Integer) tmp).intValue(), count);
        
        TestEnvironment.success(recId);
    }

}
