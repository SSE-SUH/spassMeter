package test.testing;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import test.AnnotationId;
import de.uni_hildesheim.sse.codeEraser.annotations.Variability;


/**
 * Defines an interface to the test environment. When providing new test plugins
 * e.g. for JMX based value access, please note that the following methods 
 * should be (re)implemented:
 * <ol>
 *  <li>{@link #initialize()}</li>
 *  <li>{@link #getValue(String, MonitoringGroupValue)}</li>
 *  <li>{@link #getTimerData(String)}</li>
 *  <li>{@link #getAttributeData(String)}</li>
 * </ol>
 * {@link #getTimerData(String)} and {@link #getAttributeData(String)} require
 * a call to {@link #initDefaultListeners()} from {@link #initialize()}.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
@Variability(id = AnnotationId.VAR_TESTING)
public abstract class AbstractTestPlugin {

    /**
     * The separator between recording and instance ids.
     */
    public static final String RECID_INSTANCEID_SEPARATOR = "-";
    
    /**
     * Stores the exception handler. May be <b>null</b>.
     */
    private ILogger logger;
    
    /**
     * Stores the timer listener.
     */
    private TimerListener timerListener;
    
    /**
     * Stores the value listener.
     */
    private ValueListener valueListener;
    
    /**
     * Creates a new test plugin.
     * 
     * @param logger the logger to be used (may be <b>null</b>)
     * 
     * @since 1.00
     */
    public AbstractTestPlugin(ILogger logger) {
        this.logger = logger;
    }
    
    /**
     * Initialize this plugin. Please do initialization activities here
     * and not in the constructor so that the {@link TestEnvironment} can
     * be initialized properly before.
     * 
     * @since 1.00
     */
    public abstract void initialize();
    
    /**
     * Returns a specified value from a monitoring group.
     * 
     * @param recId the recorder identification of the monitoring group
     * @param value the value to be returned
     * @return the value or <b>null</b> if not accessible etc.
     * 
     * @since 1.00
     */
    public abstract Long getValue(String recId, MonitoringGroupValue value);
    
    /**
     * A method to handle exceptions intended to enable/disable trace output.
     * 
     * @param text the text to be emitted (in any case)
     * @param exception the exception describing the problem which may 
     *    be emitted
     * 
     * @since 1.00
     */
    protected void exception(String text, Exception exception) {
        if (null != logger) {
            logger.exception(text, exception);
        } else {
            // fallback
            System.out.println(text);
            exception.printStackTrace(System.out);
        }
    }

    /**
     * Prints a notice during testing. 
     * 
     * @param notice the text to be printed
     * 
     * @since 1.00
     */
    protected void notice(String notice) {
        if (null != logger) {
            logger.notice(notice);
        } else {
            // fallback
            System.out.println(notice);
        }
    }

    /**
     * Provides a default initialization of the listeners provided
     * by this package. This method is not called by default. This method
     * changes {@link #timerListener} and {@link #valueListener} as a side 
     * effect and, thus, enables the default implementations of.. 
     * 
     * @since 1.00
     */
    protected void initDefaultListeners() {
        try {
            timerListener = new TimerListener();
            valueListener = new ValueListener();
            Class<?> registryClass = Class.forName("de.uni_hildesheim.sse." 
                + "monitoring.runtime.plugins.PluginRegistry");
            Class<?> evtClass = Class.forName("de.uni_hildesheim.sse." 
                + "monitoring.runtime.plugins.TimerChangeListener");
            Method method = registryClass.getMethod(
                "attachTimerChangeListener", evtClass);
            method.invoke(null, timerListener);
            
            evtClass = Class.forName("de.uni_hildesheim.sse." 
                + "monitoring.runtime.plugins.ValueChangeListener");
            method = registryClass.getMethod(
                "attachValueChangeListener", evtClass);
            method.invoke(null, valueListener);
        } catch (ClassNotFoundException e) {
            exception(ILogger.CLASS_NOT_FOUND, e);
        } catch (SecurityException e) {
            exception(ILogger.CANNOT_CALL, e);
        } catch (IllegalArgumentException e) {
            exception(ILogger.CANNOT_CALL, e);
        } catch (IllegalAccessException e) {
            exception(ILogger.CANNOT_CALL, e);
        } catch (NoSuchMethodException e) {
            exception(ILogger.CANNOT_CALL, e);
        } catch (InvocationTargetException e) {
            exception(ILogger.CANNOT_CALL, e);
        }
    }
    
    /**
     * Returns the (current) value of a timer.
     * 
     * @param recId the recorder identification
     * @return the value of the specified timer or <b>null</b> if not found
     * 
     * @since 1.00
     */
    public Long getTimerData(String recId) {
        Long result = null;
        if (null != timerListener) {
            result = timerListener.getValue(recId);
        }
        return result;
    }
    
    /**
     * Returns the new value of a changed attribute.
     * 
     * @param recId the recorder identification of the attribute
     * @return the new value, may be <b>null</b>
     * 
     * @since 1.00
     */
    public Object getAttributeData(String recId) {
        Object result = null;
        if (null != valueListener) {
            result = valueListener.getNewValue(recId);
        }
        return result;
    }
    
    /**
     * Returns whether this test plugin supports configurations.
     * 
     * @return <code>true</code> if it supports configurations, 
     *   <code>false</code> else
     * 
     * @since 1.00
     */
    public abstract boolean supportsConfigurations();
    
    /**
     * Returns the number of configurations recorder.
     * 
     * @return the number of configurations
     * 
     * @since 1.00
     */
    public abstract int getNumberOfConfigurations();

    /**
     * Returns the recording identifier of the specific configuration.
     * 
     * @param index the index of the configuration
     * @return the recording identifier  (consisting of the names of the 
     *   individual recorder ids separated by ",")
     * @throws IndexOutOfBoundsException if <code>index &lt; 0 
     *   || index &gt;={@link #getNumberOfConfigurations()}</code>
     * 
     * @since 1.00
     */
    public abstract String getConfigurationId(int index);

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
    public abstract Long getConfigurationValue(int index, 
        MonitoringGroupValue value);
    
    /**
     * Returns the instance identifiers for a certain monitoring group. Caches available instance
     * identifier recording groups as recId-instanceId (using {@link #RECID_INSTANCEID_SEPARATOR} as
     * separator). 
     * 
     * @param recId the recorder id to return the group for
     * @return the available instance identifiers, may be <b>null</b> for none
     * 
     * @since 1.20
     */
    public abstract String[] getInstanceIdentifiers(String recId);
    
}
