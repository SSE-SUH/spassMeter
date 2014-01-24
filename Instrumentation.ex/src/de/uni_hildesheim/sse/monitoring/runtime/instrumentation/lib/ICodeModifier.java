package de.uni_hildesheim.sse.monitoring.runtime.instrumentation.lib;

import java.lang.annotation.Annotation;

import de.uni_hildesheim.sse.codeEraser.annotations.Variability;
import de.uni_hildesheim.sse.monitoring.runtime.AnnotationConstants;
import de.uni_hildesheim.sse.monitoring.runtime.annotations.ConfigurationChange;
import de.uni_hildesheim.sse.monitoring.runtime.annotations.Monitor;
import de.uni_hildesheim.sse.monitoring.runtime.annotations.NotifyValue;
import de.uni_hildesheim.sse.monitoring.runtime.annotations.Timer;
import de.uni_hildesheim.sse.monitoring.runtime.annotations.TimerState;
import de.uni_hildesheim.sse.monitoring.runtime.utils.HashMap;

/**
 * Defines the interface of a code modifier which operates on class or 
 * behavior level.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public interface ICodeModifier {

    /**
     * The attribute storing the static (instrumented) configuration.
     */
    public static final String CONFIGURATION_FIELD_NAME = "cmdArgs";
    
    /**
     * Returns the timer notification call for the given (overriding) state.
     * 
     * @param behavior the behavior to insert around
     * @param state the overriding state
     * @param notifyTimer the original timer annotation
     * @param before insert before or after
     * @throws InstrumenterException in case of byte code problems
     * 
     * @since 1.00
     */
    public void notifyTimerCall(IBehavior behavior, TimerState state, 
        Timer notifyTimer, boolean before) throws InstrumenterException;

    /**
     * Returns the call to register a thread with its native implementation.
     * 
     * 
     * @param cls the class to insert the notification to
     * @param behavior the behavior to modify (may be <b>null</b> in case of
     *     a not found <code>run</code> method)
     * @param isMain is this the registration call for the main method
     * @throws InstrumenterException in case of byte code problems
     * 
     * @since 1.00
     */
    public void notifyRegisterThread(IClass cls, IBehavior behavior, 
        boolean isMain) throws InstrumenterException;

    /**
     * Processes value notifications on the given method.
     * 
     * @param behavior the method to be changed
     * @param recId the recorder identification
     * @param ann the value annotation
     * @throws InstrumenterException if any instrumented code is not valid
     * 
     * @since 1.00
     */
    public void valueNotification(IBehavior behavior, String recId,
        NotifyValue ann) throws InstrumenterException;

    /**
     * Instrument the random access file. In this method we instrument only a
     * part because we cannot instrument native methods. The rest is done in
     * DelegatingRandomAccessFile (do not link directly).
     * 
     * @param cls the class to be instrumented
     * @throws InstrumenterException if code problems occur in injected code
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.MONITOR_FILE_IO)
    public void instrumentRandomAccessFile(IClass cls) 
        throws InstrumenterException;

    /**
     * Instruments the socket output stream class.
     * 
     * @param cls the class to be instrumented
     * @throws InstrumenterException if code problems occur in injected code
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.MONITOR_NET_IO)
    public void instrumentSocketOutputStream(IClass cls) 
        throws InstrumenterException;

    /**
     * Instruments the socket input stream class.
     * 
     * @param cls the class to be instrumented
     * @throws InstrumenterException if code problems occur in injected code
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.MONITOR_NET_IO)
    public void instrumentSocketInputStream(IClass cls) 
        throws InstrumenterException;

    /**
     * Instruments the a method for ending the recorder.
     * 
     * @param behavior the behavior / method to be changed
     * @param printStatistics if statistics should be printed at the end of the 
     *     program
     * @param invoke an additional method to be invoked after monitoring (fqn 
     *     class name "." method)
     * @throws InstrumenterException in case that the new code cannot be 
     *     compiled and injection was erroneous
     * 
     * @since 1.00
     */
    public void instrumentEndSystem(IBehavior behavior, 
        boolean printStatistics, String invoke) throws InstrumenterException;

    /**
     * Instruments the a method for starting the recorder.
     * 
     * @param behavior the behavior / method to be changed
     * @param shutdownHook insert a shutdown hook
     * @param invoke a method to call at the end of execution (testing)
     * @throws InstrumenterException in case that the new code cannot be 
     *     compiled and injection was erroneous
     * 
     * @since 1.00
     */
    public void instrumentStartSystem(
        IBehavior behavior, boolean shutdownHook, String invoke)
        throws InstrumenterException;

    /**
     * Performs the instrumentation for a configuration change, i.e. to notify 
     * the recorder about an explicit change of 
     * 
     * @param behavior the behavior to be changed
     * @param annotation the annotation containing information on the 
     *     configuration change
     * @throws InstrumenterException in case that the new code cannot be 
     *     compiled and injection was erroneous
     * 
     * @since 1.00
     */
    public void instrumentConfigurationChange(
        IBehavior behavior, ConfigurationChange annotation) 
        throws InstrumenterException;

    /**
     * Instrument an existing finalize method in order to notify the recorder
     * about freeing this object.
     * 
     * @param method the method being considered for instrumentation
     * @throws InstrumenterException in case of code errors
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.MONITOR_MEMORY_USAGE)
    public void instrumentFinalize(IBehavior method) 
        throws InstrumenterException;
    
    /**
     * Instruments an existing constructor in order to notify the recorder
     * about memory allocation.
     * 
     * @param method the method being considered for instrumentation
     * @throws InstrumenterException in case of code errors
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.MONITOR_MEMORY_USAGE)
    public void instrumentConstructor(IBehavior method) 
        throws InstrumenterException;
    
    /**
     * Instruments a method for timing measurement.
     * 
     * @param behavior the method / behavior to be changed
     * @param mGroup the group annotation causing this instrumentation (must 
     *     not be <b>null</b>)
     * @param exclude is this an exclusion from monitoring
     * @param directId is the cause direct, e.g. in case of an annotated method
     * @throws InstrumenterException in case that the new code cannot be 
     *     compiled and injection was erroneous
     * 
     * @since 1.00
     */
    public void instrumentTiming(IBehavior behavior, Monitor mGroup, 
        boolean exclude, boolean directId) throws InstrumenterException;

    /**
     * Adds an registering initializer to <code>cls</code>.
     * 
     * @param cls the class the initializer should be added to
     * @param mGroup the related annotation
     * @throws InstrumenterException in case that the new code cannot be 
     *     compiled and injection was erroneous
     * 
     * @since 1.00
     */
    public void addRegisteringInitializer(IClass cls, 
        Monitor mGroup) throws InstrumenterException;

    /**
     * Instruments the given method as a variability handler, i.e. disables
     * automatic variability detection.
     * 
     * @param behavior the behavior to be instrumented
     * @throws InstrumenterException in case of code errors
     * 
     * @since 1.00
     */
    public void instrumentVariabilityHandler(IBehavior behavior) 
        throws InstrumenterException;

    /**
     * Adds a finalizer method to this class or to the finalizer method of 
     * a super class (except of object). If a finalizer method was defined
     * in a superclass (probably final) then this method is instrumented. If 
     * none is found, a new finalizer method is added.
     * 
     * @param cls the class to add the finalizer to
     * @param overhead instrument for overhead or for "normal" classes
     * @throws InstrumenterException in case of code errors
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.MONITOR_MEMORY_USAGE)
    public void addFinalizer(IClass cls, boolean overhead) 
        throws InstrumenterException;

    /**
     * Adds an annotation to the given class.
     * 
     * @param cls the class to add the annotation
     * @param ann the annotation to add
     * @param values name-value mappings representing the values, may be 
     *   <b>null</b> for markers
     * @throws InstrumenterException in case that modifying the byte code fails
     */
    public void addAnnotation(IClass cls, Class<? extends Annotation> ann, 
        HashMap<String, Object> values) throws InstrumenterException;

    /**
     * Adds an annotation to the given behavior.
     * 
     * @param behavior the behavior to add the annotation
     * @param ann the annotation to add
     * @param values name-value mappings representing the values, may be 
     *   <b>null</b> for markers
     * @throws InstrumenterException in case that modifying the byte code fails
     */
    public void addAnnotation(IBehavior behavior, 
        Class<? extends Annotation> ann, HashMap<String, Object> values) 
        throws InstrumenterException;
    
    /**
     * Stores the command line configuration to the configuration class.
     * 
     * @param cls the class instance of the configuration class
     * @param args the command line arguments
     * @throws InstrumenterException in case that modifying the byte code fails
     * 
     * @since 1.00
     */
    public void storeConfiguration(IClass cls, String args) 
        throws InstrumenterException;

}
