package test.asmTree;

import de.uni_hildesheim.sse.monitoring.runtime.annotations.*;

/**
 * Performs class annotation tests.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public class ClassAnnotationTest {

    /**
     * An attribute to be tested for value changes.
     */
    @ValueChange(id = "myId-static")
    @SuppressWarnings("unused")
    private static int valueTestStatic = 0;

    /**
     * An attribute to be tested for value changes.
     */
    @ValueContext(id = "myId-")
    @SuppressWarnings("unused")
    private static int valueContextStatic = 0;

    /**
     * An attribute to be tested for value changes.
     */
    @SuppressWarnings("unused")
    @ValueChange(id = "myId-instance")
    private int valueTestInstance = 0;
    
    /**
     * An attribute to be tested for value changes.
     */
    @SuppressWarnings("unused")
    @ValueContext(id = "myId-")
    private int valueContextInstance = 0;
    
    /**
     * Testing the start annotation in terms of an instance method.
     * 
     * @since 1.00
     */
    @StartSystem
    public void instanceStart() {
    }

    /**
     * Testing the end annotation in terms of an instance method.
     * 
     * @since 1.00
     */
    @EndSystem
    public void instanceStop() {
    }
    
    /**
     * Testing the start annotation in terms of a static method.
     * 
     * @since 1.00
     */
    @StartSystem
    public static void staticStart() {
    }
    
    /**
     * Testing the end annotation in terms of a static method.
     * 
     * @since 1.00
     */
    @EndSystem
    public static void staticStop() {
    }

    /**
     * A variability handler as instance method.
     * 
     * @since 1.00
     */
    @VariabilityHandler
    protected void instanceVarHandler() {
    }
    
    /**
     * Just a local method used as marker in byte code.
     * 
     * @since 1.00
     */
    protected static void local() {
        valueTestStatic++;
        valueContextStatic++;
    }

    /**
     * A variability handler as static method.
     * 
     * @since 1.00
     */
    @VariabilityHandler
    protected static void staticVarHandler() {
    }
    
    /**
     * A monitored instance method.
     * 
     * @since 1.00
     */
    @Monitor
    protected void instanceMonitor() {
        valueTestInstance++;
        valueContextInstance++;
        local();
    }
    
    /**
     * A monitored instance method.
     * 
     * @since 1.00
     */
    @Monitor
    protected static void staticMonitor() {
    }
    
    /**
     * An excluded instance method.
     * 
     * @since 1.00
     */
    @ExcludeFromMonitoring
    protected void instanceExclude() {
    }

    /**
     * An excluded static method.
     * 
     * @since 1.00
     */
    @ExcludeFromMonitoring
    protected static void staticExclude() {
    }

    /**
     * An instance timer test method.
     * 
     * @since 1.00
     */
    @Timer(id = "timer1", state = TimerState.START_FINISH)
    protected void instanceTimer() {
    }

    /**
     * A static timer test method.
     * 
     * @since 1.00
     */
    @Timer(id = "timer1", state = TimerState.START_FINISH)
    protected static void staticTimer() {
    }
    
    /**
     * Instance method to test configuration change.
     * 
     * @param config the configuration
     * 
     * @since 1.00
     */
    @ConfigurationChange(valueExpression = "translate($1)")
    protected void instanceConfigChange(String config) {
    }

    /**
     * Instance method to test configuration change.
     * 
     * @param config the configuration
     * 
     * @since 1.00
     */
    @ConfigurationChange(valueExpression = "translate($1)")
    protected static void staticConfigChange(String config) {
    }
        
    /**
     * Pseudo method to test value expressions.
     * 
     * @param text a param
     * @return <code>text</code>
     * 
     * @since 1.00
     */
    protected static String translate(String text) {
        return text;
    }

    /**
     * A test for notifying values.
     * 
     * @param val the value to notify with
     * 
     * @since 1.00
     */
    @NotifyValue(id = "test", value = MeasurementValue.ALL, 
        notifyDifference = true, expression = "$1")
    protected void instanceNotifyValueAll(int val) {
    }

    /**
     * A test for notifying values.
     * 
     * @param val the value to notify with
     * 
     * @since 1.00
     */
    @NotifyValue(id = "test", value = MeasurementValue.ALL, 
        notifyDifference = true, expression = "$1")
    protected static void staticNotifyValueAll(int val) {
    }

    
    /**
     * A test for notifying values.
     * 
     * @param val the value to notify with
     * 
     * @since 1.00
     */
    @NotifyValue(id = "test", value = MeasurementValue.VALUE, 
        notifyDifference = false, expression = "$1")
    protected void instanceNotifyValue(int val) {
    }

    /**
     * A test for notifying values.
     * 
     * @param val the value to notify with
     * 
     * @since 1.00
     */
    @NotifyValue(id = "test", value = MeasurementValue.VALUE, 
        notifyDifference = false, expression = "$1")
    protected static void staticNotifyValue(int val) {
    }
    
    /**
     * A test for notifying values.
     * 
     * @param val the value to notify with
     * 
     * @since 1.00
     */
    @NotifyValue(id = "test", value = MeasurementValue.VALUE, 
        notifyDifference = true, expression = "$1")
    protected void instanceNotifyValueDiff(int val) {
    }

    /**
     * A test for notifying values.
     * 
     * @param val the value to notify with
     * 
     * @since 1.00
     */
    @NotifyValue(id = "test", value = MeasurementValue.VALUE, 
        notifyDifference = true, expression = "$1")
    protected static void staticNotifyValueDiff(int val) {
    }
    
    /**
     * A test for notifying values.
     * 
     * @param val the value to notify with
     * 
     * @since 1.00
     */
    @NotifyValue(id = "test", value = MeasurementValue.FILE_IN, 
        notifyDifference = false, expression = "$1")
    protected void instanceNotifyFileValue(int val) {
    }

    /**
     * A test for notifying values.
     * 
     * @param val the value to notify with
     * 
     * @since 1.00
     */
    @NotifyValue(id = "test", value = MeasurementValue.FILE_OUT, 
        notifyDifference = false, expression = "$1")
    protected static void staticNotifyFileValue(int val) {
    }
    
    /**
     * A test for notifying values.
     * 
     * @param val the value to notify with
     * 
     * @since 1.00
     */
    @NotifyValue(id = "test", value = MeasurementValue.FILE_IN, 
        notifyDifference = true, 
        expression = "test.asmTree.ClassAnnotationTest.valueTestStatic")
    protected void instanceNotifyFileValueDiff(int val) {
    }

    /**
     * A test for notifying values.
     * 
     * @param val the value to notify with
     * 
     * @since 1.00
     */
    @NotifyValue(id = "test", value = MeasurementValue.FILE_OUT, 
        notifyDifference = true, expression = "valueTestStatic")
    protected static void staticNotifyFileValueDiff(int val) {
    }
    
    //@NotifyValue - static, instance
    //@ConfigurationChange - static, instance
    //@Timer - static, instance
    //@ValueContext - static, instance
    //@ValueChange - static, instance
    //@Monitor - static, instance
    //@ExcludeFromMonitoring - static, instance
    //@StartSystem - static, instance
    //@EndSystem - static, instance
    //@VariabilityHandler - static, instance
    //addFinalizer
    
    /**
     * Execute the instrumented tests.
     * 
     * @param args ignored
     * 
     * @since 1.00
     */
    public static void main(String[] args) {
        staticStart();
        staticExclude();
        staticMonitor();
        staticVarHandler();
        staticConfigChange("");
        staticNotifyFileValue(1);
        staticNotifyFileValueDiff(1);
        staticNotifyValueAll(1);
        staticNotifyValue(1);
        staticNotifyValueDiff(1);
        staticTimer();
        staticStop();
        
        ClassAnnotationTest test = new ClassAnnotationTest();
        test.instanceStart();
        test.instanceExclude();
        test.instanceMonitor();
        test.instanceVarHandler();
        test.instanceConfigChange("");
        test.instanceNotifyFileValue(1);
        test.instanceNotifyFileValueDiff(1);
        test.instanceNotifyValueAll(1);
        test.instanceNotifyValue(1);
        test.instanceNotifyValueDiff(1);
        test.instanceTimer();
        test.instanceStop();
    }

}
