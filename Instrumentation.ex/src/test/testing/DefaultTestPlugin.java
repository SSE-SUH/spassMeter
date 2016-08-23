package test.testing;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import test.AnnotationId;
import de.uni_hildesheim.sse.codeEraser.annotations.Variability;
import de.uni_hildesheim.sse.monitoring.runtime.boot.ArrayList;
import de.uni_hildesheim.sse.monitoring.runtime.recordingStrategies.*;
import de.uni_hildesheim.sse.monitoring.runtime.utils.HashMap;
import de.uni_hildesheim.sse.monitoring.runtime.utils.HashMap.Entry;

/**
 * Implements a test environment which obtains data directly from 
 * SPASS-meter. This requires low-level access as the data is not directly
 * available via interfaces (layer separation).
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
@Variability(id = AnnotationId.VAR_TESTING)
public class DefaultTestPlugin extends AbstractTestPlugin {

    /**
     * Stores identified monitoring groups.
     */
    private static HashMap<String, Object> monitoringGroups 
        = new HashMap<String, Object>();
    
    /**
     * Stores the accessor methods.
     */
    private static HashMap<MonitoringGroupValue, Method> accessors 
        = new HashMap<MonitoringGroupValue, Method>();
    
    /**
     * Stores the high level mapping between values and accessor names.
     */
    private static HashMap<MonitoringGroupValue, String> accessorNames 
        = new HashMap<MonitoringGroupValue, String>();
    
    /**
     * Stores the method to access the monitoring groups.
     */
    private static Method groupAccess;

    /**
     * Stores the method to access the configurations.
     */
    private static Method configAccess;

    /**
     * Stores the method to access the instance recorder elements.
     */
    private static Method instanceAccess;

    /**
     * Stores the method to access the ids of instance recorder elements.
     */
    private static Method instanceIdAccess;
    
    /**
     * Stores the recorder strategy.
     */
    private static Object strategy;

    /**
     * Stores the recorder element map. Lazy initialization during test.
     */
    private static RecorderElementMap recorderElementMap;
    
    /**
     * Stores the configurations. Lazy initialization during test.
     */
    private static ArrayList<HashMap.Entry<String, RecorderElement>> configurations;
    
    /**
     * Stores the translation instance for internal configurations to
     * readable configuration names. Lazy initialization during test.
     */
    private ConfigurationToName conf2Name;
    
    /**
     * Creates a new test plugin.
     * 
     * @param logger the logger to be used (may be <b>null</b>)
     * 
     * @since 1.00
     */
    public DefaultTestPlugin(ILogger logger) {
        super(logger);
    }
    
    @Override
    public void initialize() {
        try {
            accessorNames.put(MonitoringGroupValue.SYSTEM_TIME, 
                "getSystemTimeTicks");
            accessorNames.put(MonitoringGroupValue.CPU_TIME, 
                "getCpuTimeTicks");
            accessorNames.put(MonitoringGroupValue.ALLOCATED_MEMORY, 
                "getMemAllocated");
            accessorNames.put(MonitoringGroupValue.USED_MEMORY, "getMemUse");
            accessorNames.put(MonitoringGroupValue.TOTAL_READ, "getIoRead");
            accessorNames.put(MonitoringGroupValue.FILE_READ, "getFileIn");
            accessorNames.put(MonitoringGroupValue.NET_READ, "getNetIn");
            accessorNames.put(MonitoringGroupValue.TOTAL_WRITE, "getIoWrite");
            accessorNames.put(MonitoringGroupValue.FILE_WRITE, "getFileOut");
            accessorNames.put(MonitoringGroupValue.NET_WRITE, "getNetOut");
            
            initDefaultListeners();
            
            // stop recording, access strategy and receive map instance
            
            Class<?> recorderClass = Class.forName(
                "de.uni_hildesheim.sse.monitoring.runtime.recording.Recorder");
            Field strategyField = recorderClass.getDeclaredField(
                "STRATEGY");
            strategyField.setAccessible(true);
            strategy = strategyField.get(null);
            Class<?> recorderStrategyClass = Class.forName(
                "de.uni_hildesheim.sse.monitoring.runtime.recordingStrategies.AbstractRecorderStrategy");
            groupAccess = recorderStrategyClass.getDeclaredMethod("getRecorderElement", String.class);
            groupAccess.setAccessible(true);
            configAccess = recorderStrategyClass.getDeclaredMethod("getRecorderElements");
            configAccess.setAccessible(true);
            
            Class<?> recorderElementClass = Class.forName(
                "de.uni_hildesheim.sse.monitoring.runtime.recordingStrategies.RecorderElement");
            instanceIdAccess = recorderElementClass.getDeclaredMethod("instanceRecorderIds");
            instanceIdAccess.setAccessible(true);
            instanceAccess = recorderElementClass.getDeclaredMethod("getInstanceRecorderElement", Long.TYPE);
            instanceAccess.setAccessible(true);
        } catch (ClassNotFoundException e) {
            exception(ILogger.CLASS_NOT_FOUND, e);
        } catch (SecurityException e) {
            exception(ILogger.CANNOT_CALL, e);
        } catch (NoSuchFieldException e) {
            exception(ILogger.CANNOT_CALL, e);
        } catch (IllegalArgumentException e) {
            exception(ILogger.CANNOT_CALL, e);
        } catch (IllegalAccessException e) {
            exception(ILogger.CANNOT_CALL, e);
        } catch (NoSuchMethodException e) {
            exception(ILogger.CANNOT_CALL, e);
        } 
    }

    /**
     * Returns a specified value from a monitoring group.
     * 
     * @param recId the recorder identification of the monitoring group
     * @param value the value (identification) to be returned
     * @return the value or <b>null</b> if not accessible etc.
     * 
     * @since 1.00
     */
    @Override
    public Long getValue(String recId, MonitoringGroupValue value) {
        return obtainValue(getMonitoringGroup(recId), value);
    }

    /**
     * Obtains a value from a given monitoring group.
     * 
     * @param monitoringGroup the monitoring group object to return the 
     *   value from
     * @param value the value (identification) to be returned
     * @return the value or <b>null</b> if not accessible etc.
     * 
     * @since 1.00
     */
    private Long obtainValue(Object monitoringGroup, 
        MonitoringGroupValue value) {
        Long result = null;
        if (null != monitoringGroup) {
            Method accessor = getAccessor(value);
            if (null != accessor) {
                try {
                    result = (Long) accessor.invoke(monitoringGroup, 
                        (Object[]) null);
                } catch (ClassCastException e1) {
                    exception(ILogger.CANNOT_CAST_DATA, e1);
                } catch (IllegalArgumentException e1) {
                    exception(ILogger.CANNOT_GET_DATA, e1);
                } catch (IllegalAccessException e1) {
                    exception(ILogger.CANNOT_GET_DATA, e1);
                } catch (InvocationTargetException e1) {
                    exception(ILogger.CANNOT_GET_DATA, e1);
                }
            } else {
                System.out.println(ILogger.CANNOT_GET_DATA);
            }
        } 
        return result;
    }

    /**
     * Returns the accessor for a given value.
     * 
     * @param value the value to access
     * @return the accessor or <b>null</b> if not found
     * 
     * @since 1.00
     */
    protected Method getAccessor(MonitoringGroupValue value) {
        Method result = null;
        if (!accessors.containsKey(value)) {
            try {
                Class<?> recorderStrategy = Class.forName("de.uni_hildesheim." 
                    + "sse.monitoring.runtime.plugins.IMonitoringGroup");
                String name = accessorNames.get(value);
                if (null != name) {
                    result = recorderStrategy.getMethod(name, 
                        (Class<?>[]) null);
                } else {
                    notice(ILogger.CANNOT_GET_DATA);
                }
            } catch (ClassNotFoundException e) {
                exception(ILogger.CANNOT_GET_DATA, e);
            } catch (SecurityException e) {
                exception(ILogger.CANNOT_GET_DATA, e);
            } catch (NoSuchMethodException e) {
                exception(ILogger.CANNOT_GET_DATA, e);
            }
        } else {
            result = accessors.get(value);
        }
        return result;
    }
    
    /**
     * Returns the monitoring group for the given recorder id.
     * 
     * @param recId the recorder id to return the group for
     * @return the monitoring group or <b>null</b> if not found
     * 
     * @since 1.00
     */
    protected Object getMonitoringGroup(String recId) {
        Object result = null;
        if (null != recId) {
            if (!monitoringGroups.containsKey(recId)) {
                if (null != groupAccess) {
                    try {
                        result = groupAccess.invoke(strategy, recId);
                    } catch (IllegalArgumentException e) {
                        exception(ILogger.CANNOT_GET_GROUP, e);
                    } catch (IllegalAccessException e) {
                        exception(ILogger.CANNOT_GET_GROUP, e);
                    } catch (InvocationTargetException e) {
                        exception(ILogger.CANNOT_GET_GROUP, e);
                    }
                }
                monitoringGroups.put(recId, result);
            } else {
                result = monitoringGroups.get(recId); 
            }
        }
        return result;
    }

    @Override
    public String[] getInstanceIdentifiers(String recId) {
        String[] result = null;
        Object mGroup = getMonitoringGroup(recId);
        if (null != mGroup) {
            if (null != instanceIdAccess && null != instanceAccess) {
                try {
                    Object tmp = instanceIdAccess.invoke(mGroup);
                    if (tmp instanceof long[]) {
                        long[] ids = (long[]) tmp;
                        result = new String[ids.length];
                        for (int i = 0; i < result.length; i++) {
                            result[i] = recId + RECID_INSTANCEID_SEPARATOR + ids[i];
                            monitoringGroups.put(result[i], instanceAccess.invoke(mGroup, ids[i]));
                        }
                    } 
                } catch (IllegalArgumentException e) {
                    exception(ILogger.CANNOT_GET_GROUP, e);
                } catch (IllegalAccessException e) {
                    exception(ILogger.CANNOT_GET_GROUP, e);
                } catch (InvocationTargetException e) {
                    exception(ILogger.CANNOT_GET_GROUP, e);
                }
            }
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
    public boolean supportsConfigurations() {
        initConfigurations();
        return true;
    }
    
    /**
     * Initializes the internal set of configurations.
     * 
     * @since 1.00
     */
    private void initConfigurations() {
        if (null == configurations) {
            try {
                recorderElementMap = (RecorderElementMap) 
                    configAccess.invoke(strategy, (Object[]) null);
                conf2Name = recorderElementMap.getConfigurationMapping();
                Iterable<Entry<String, RecorderElement>> conf = 
                    recorderElementMap.configurationToRecording();
                configurations = new ArrayList<HashMap.Entry<String, RecorderElement>>();
                for (Entry<String, RecorderElement> entry : conf) {
                    configurations.add(entry);
                }
            } catch (IllegalArgumentException e) {
                exception(ILogger.CANNOT_GET_CONFIGURATION, e);
            } catch (IllegalAccessException e) {
                exception(ILogger.CANNOT_GET_CONFIGURATION, e);
            } catch (InvocationTargetException e) {
                exception(ILogger.CANNOT_GET_CONFIGURATION, e);
            }
        }
    }
    
    /**
     * Returns the number of configurations recorder.
     * 
     * @return the number of configurations
     * 
     * @since 1.00
     */
    public int getNumberOfConfigurations() {
        initConfigurations();
        int result;
        if (null == configurations) {
            result = 0;
        } else {
            result = configurations.size();
        }
        return result;
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
    public String getConfigurationId(int index) {
        initConfigurations();
        String result;
        if (null != configurations) {
            HashMap.Entry<String, RecorderElement> entry 
                = configurations.get(index);
            String confId = entry.getKey();
            result = conf2Name.formatConfiguration(confId, ", ");
        } else {
            result = null;
        }
        return result;
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
    public Long getConfigurationValue(int index, 
        MonitoringGroupValue value) {
        initConfigurations();
        Long result;
        if (null != configurations) {
            HashMap.Entry<String, RecorderElement> entry 
                = configurations.get(index);
            result = obtainValue(entry.getValue(), value);
        } else {
            result = null;
        }
        return result;
    }

}
