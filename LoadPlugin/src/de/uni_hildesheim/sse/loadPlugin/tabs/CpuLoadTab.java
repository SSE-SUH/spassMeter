package de.uni_hildesheim.sse.loadPlugin.tabs;

import java.awt.FlowLayout;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import org.jfree.data.time.TimeSeries;

import com.sun.tools.jconsole.JConsoleContext;

import de.uni_hildesheim.sse.serviceConstants.MonitoringGroupConstants;
import de.uni_hildesheim.sse.serviceConstants.ProcessorDataConstants;
import de.uni_hildesheim.sse.serviceConstants.ThisProcessDataConstants;

/**
 * CPULoadTab is an implementation of the class {@link AbstractTab} 
 * to display CPU load information.
 * 
 * @author Stephan Dederichs
 * 
 * @version 1.00
 * @since 1.00
 */
public class CpuLoadTab extends AbstractTab {

    /**
     * Stores the minimum scaling.
     */
    public static final double MIN_VAL = 0.000001;
    
    /**
     * Enable LoBaRIS ONs. Should be replaced by GUI selection in future.
     */
    private static final boolean LOBARIS = false; 
    // TODO replace by GUI selection
        
    /**
     * Stores a default serial version UID.
     * 
     * @since 1.00
     */
    private static final long serialVersionUID = 1L;
    
    /**
     * Stores the display name for the system load.
     * 
     * @since 1.00
     */
    private final String systemLoad = "System load";
    
    /**
     * Stores the display name for the jvm process load.
     * 
     * @since 1.00
     */
    private final String jvmLoad = "JVM process load";
    
    /**
     * Stores the display name for the program load.
     * 
     * @since 1.00
     */
    private final String programLoad = "Program load";
    
    /**
     * Stores the {@link ObjectName}s of the relevant monitoring groups.
     * 
     * @since 1.00
     */
    private List<ObjectName> relevantMonitoringGroups;
    
    /**
     * Stores the {@link ObjectName}s for the displayed data.
     * 
     * @since 1.00
     */
    private Map<String, ObjectName> dataObjectNames;
    
    /**
     * Stores the (derived) program load (in %).
     * 
     * @since 1.00
     */
    private double programLoadValue;
    
    /**
     * Stores the this process load (in %).
     * 
     * @since 1.00
     */
    private double jvmLoadValue;
    
    /**
     * Stores the aggregated system time of the program.
     * 
     * @since 1.00
     */
    private double programSysTime;
    
    /**
     * Stores the aggregates jvm system time ticks.
     * 
     * @since 1.00
     */
    private double jvmSystemTimeTicks;
    
    /**
     * Constructor.
     * 
     * @param context The {@link JConsoleContext}.
     * 
     * @since 1.00
     */
    public CpuLoadTab(JConsoleContext context) {
        super(new FlowLayout(), context);
    }
    
    @Override
    public void initializeTabSpecific() {
        setXAxisLabel("time (hh:mm:ss)");
        setYAxisLabel("cpu use (%)");
        // For storing relevant monitoring groups
        relevantMonitoringGroups = new ArrayList<ObjectName>();
        
        // adding data names 
        addDataName(systemLoad, 
            ProcessorDataConstants.CURRENT_SYSTEM_LOAD);
        addDataName(jvmLoad, 
            ThisProcessDataConstants.CURRENT_PROCESS_PROCESSOR_LOAD);
        addDataName(programLoad, 
            MonitoringGroupConstants.SYSTEM_TIME_TICKS);
        
        // creating and adding ObjectNames
        try {
            dataObjectNames = new LinkedHashMap<String, ObjectName>();
            dataObjectNames.put(systemLoad,
                new ObjectName("GearsBridge:folder=system,name=Processor"));
            dataObjectNames.put(jvmLoad,
                new ObjectName("GearsBridge:folder=system,name=ThisProcess"));
            dataObjectNames.put(programLoad,
                new ObjectName(
                    "SPASS-meter:folder=monitoring groups,name=program"));
    
            if (LOBARIS) {
                addMonitoringGroup("MapProvider@client", 
                    MonitoringGroupConstants.SYSTEM_TIME_TICKS, 
                    new ObjectName("SPASS-meter:folder=monitoring groups,name=" 
                        + "MapProvider@client"));
                addMonitoringGroup("MapProvider@server", 
                    MonitoringGroupConstants.SYSTEM_TIME_TICKS, 
                    new ObjectName("SPASS-meter:folder=monitoring groups,name=" 
                        + "MapProvider@server"));
            } else {
                // creating data name and ObjectName for another monitoring
                // group if registered in the MBean server
                ObjectName monitoringGroups = new ObjectName(
                    "SPASS-meter:folder=monitoring groups,name=*");
                // ObjectNames of all at the MBean server registered monitoring 
                // groups
                Set<ObjectName> set = getMBeanServerConnection().
                    queryNames(monitoringGroups, null);
                // search for a monitoring group which is not "overhead", 
                // "recorder" or "program"
                for (ObjectName o : set) {
                    String oName = o.getCanonicalName();
                    if (!oName.endsWith("overhead") 
                        && !oName.endsWith("recorder")
                        && !oName.endsWith("program")) {
                        addMonitoringGroup(o.getKeyPropertyList().get("name"), 
                                MonitoringGroupConstants.MEM_USE, o);
                    }
                }
            }
        } catch (MalformedObjectNameException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        // adding TimeSeries 
        for (String s : getDataNames()) {
            addTimeSeries(s, new TimeSeries(s));
        }
                
        // adding data display units (for the table)
        addDataDisplayUnit("Load type");
        addDataDisplayUnit("Load (in %)");
    }

    /**
     * Adds a monitoring group and a time series.
     * 
     * @param displayName the display name for this monitoring group.
     * @param attribute the relevant attriute for this monitoring group.
     * @param on the object name for the monitoring group.
     * 
     * @since 1.00
     */
    private void addMonitoringGroup(String displayName, String attribute, 
        ObjectName on) {
        addDataName(displayName, attribute);
        dataObjectNames.put(displayName, on);
        relevantMonitoringGroups.add(on);
    }
    
    @Override
    public void gatherData() {
        for (String key : getDataNames()) {
            String attributeName = getDataNameValue(key);
            Object value = null;
            try {
                if (null != attributeName) {
                    value = getMBeanServerConnection()
                        .getAttribute(dataObjectNames.get(key), attributeName);
                }
                
                if (key.equals(jvmLoad)) {
                    jvmLoadValue = (Double) value;
                } else if (key.equals(programLoad)) {
                    value = calculateProgramLoad(key, value);
                } else if (!key.equals(systemLoad)) { 
                    // only for relevant monitoring groups
                    // NOT for the system load!
                    value = calculateMonitorinGroupLoad(key, value);
                }
            } catch (InstanceNotFoundException e) {
                value = 0; // instance not present at the moment - don't worry
            } catch (AttributeNotFoundException e) {
                System.out.println("Current " + key + " could " 
                    + "not be measured.");
                value = 0;
            } catch (Exception e) {
                e.printStackTrace();
                value = 0;
            }
            if (null != value) {
                LinkedList<Object> values = new LinkedList<Object>();
                values.add(value); // Load (in %)
                addNewData(key, values);                
            }
        }      
    }

    /**
     * Returns the program load. 
     * Scales down program load via system time used by the program convert 
     * programAggSysTime to double in order to use floating point division 
     * in the remainder of this constructor.
     * 
     * @param key The key for the dataObjectNames map.
     * @param value The value from the MBean server.
     * 
     * @return The program load.
     * 
     * @throws NumberFormatException
     *             NumberFormatException
     * @throws AttributeNotFoundException
     *             AttributeNotFoundException
     * @throws InstanceNotFoundException
     *             InstanceNotFoundException
     * @throws MBeanException
     *             MBeanException
     * @throws ReflectionException
     *             ReflectionException
     * @throws IOException
     *             IOException
     * 
     * @since 1.00
     */
    private Object calculateProgramLoad(String key, Object value) 
        throws NumberFormatException, AttributeNotFoundException, 
        InstanceNotFoundException, MBeanException, ReflectionException, 
        IOException {
        programSysTime = Double.parseDouble(
            getMBeanServerConnection().getAttribute(
                dataObjectNames.get(key), MonitoringGroupConstants.
                SYSTEM_TIME_TICKS).toString());
        jvmSystemTimeTicks = Double.parseDouble(
            getMBeanServerConnection().getAttribute(
                dataObjectNames.get(jvmLoad), ThisProcessDataConstants.
                CURRENT_PROCESS_SYSTEM_TIME_TICKS).toString()); 
        double loadFraction = programSysTime / jvmSystemTimeTicks;
        value = Math.min(jvmLoadValue, jvmLoadValue * loadFraction);
        
        programLoadValue = Double.parseDouble(value.toString());
        
        return value;
    }
    
    /**
     * Returns the monitoring group load (in %) based on the differences.
     * 
     * @param key The key for the dataObjectNames map.
     * @param value The value from the MBean server.
     * 
     * @return the monitoring group load (in %).
     * 
     * @since 1.00
     */
    private Object calculateMonitorinGroupLoad(String key, Object value) {
        double result = 0;
        if (null != getLastData(key)) {
            Long lastAggSys = ((Double) getLastData(key).get(0)).longValue();
            
            if (null != lastAggSys) {
                if (lastAggSys < 0) {
                    result = 0; // do not show
                } else if (0 == lastAggSys) {
                    result = MIN_VAL; // it is at least present
                } else {
                    // scale down monitoring group load
                    long dAggSysTime = (Long) value - lastAggSys;
                    double loadFraction = dAggSysTime / programSysTime;
                    return Math.min(programLoadValue, 
                        programLoadValue * loadFraction);
                }
            }
        }
        return result;
    }

}
