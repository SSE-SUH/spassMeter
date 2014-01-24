package de.uni_hildesheim.sse.loadPlugin.tabs;

import java.awt.FlowLayout;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.jfree.data.time.TimeSeries;

import com.sun.tools.jconsole.JConsoleContext;

import de.uni_hildesheim.sse.serviceConstants.MemoryDataConstants;
import de.uni_hildesheim.sse.serviceConstants.MonitoringGroupConstants;
import de.uni_hildesheim.sse.serviceConstants.ThisProcessDataConstants;

/**
 * MemoryLoadTab an implementation of the class {@link AbstractTab} 
 * to display memory load information.
 * 
 * @author Stephan Dederichs
 * 
 * @version 1.00
 * @since 1.00
 */
public class MemoryLoadTab extends AbstractTab {
    
    /**
     * Stores a default serial version UID.
     * 
     * @since 1.00
     */
    private static final long serialVersionUID = 1L;
    
    /**
     * Stores the {@link ObjectName}s for the displayed data.
     * 
     * @since 1.00
     */
    private Map<String, ObjectName> dataObjectNames;

    /**
     * Creates an instance of the class {@link MemoryLoadTab}.
     * 
     * @param context The {@link JConsoleContext}.
     * 
     * @since 1.00
     */
    public MemoryLoadTab(JConsoleContext context) {
        super(new FlowLayout(), context);
    }

    

    @Override
    public void initializeTabSpecific() {
        setXAxisLabel("time (hh:mm:ss)");
        setYAxisLabel("memory use (mb)");
        // set show megabyte value in the chart
        setDataPosition(2);
        // adding data names 
        addDataName("system memory use", 
            MemoryDataConstants.CURRENT_MEMORY_USE);
        addDataName("this process memory use", 
            ThisProcessDataConstants.CURRENT_PROCESS_MEMORY_USE);
        addDataName("program memory use", 
            MonitoringGroupConstants.MEM_USE);
        
        // creating and adding ObjectNames
        try {
            dataObjectNames = new LinkedHashMap<String, ObjectName>();
            dataObjectNames.put("system memory use", 
                new ObjectName("GearsBridge:folder=system,name=Memory"));
            dataObjectNames.put("this process memory use",
                new ObjectName("GearsBridge:folder=system,name=ThisProcess"));
            dataObjectNames.put("program memory use",
                new ObjectName(
                    "SPASS-meter:folder=monitoring groups,name=program"));
            
            // creating data name and ObjectName for another monitoring group 
            // if registered in the MBean server
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
                if (!oName.endsWith("overhead") && !oName.endsWith("recorder")
                    && !oName.endsWith("program")) {
                    addDataName(o.getKeyPropertyList().get("name"), 
                        MonitoringGroupConstants.MEM_USE);
                    dataObjectNames.put(o.getKeyPropertyList().get("name"), o);
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
        addDataDisplayUnit("Memory type");
        addDataDisplayUnit("Byte");
        addDataDisplayUnit("Kilobyte");
        addDataDisplayUnit("Megabyte");
    }



    @Override
    public void gatherData() {
        for (String key : getDataNames()) {
            String attributeName = getDataNameValue(key);
            try {
                if (null != attributeName) {
                    long value = Long.parseLong(getMBeanServerConnection().
                        getAttribute(dataObjectNames.get(key), 
                            attributeName).toString());
                    LinkedList<Object> values = new LinkedList<Object>();
                    values.add(value); // byte
                    values.add(value / 1024); // kilobyte
                    values.add(value / 1024 / 1024); // megabyte
                    addNewData(key, values);
                }
            } catch (AttributeNotFoundException e) {
                System.out.println("Key " + key + " cannot "
                    + "not be measured.");
            } catch (InstanceNotFoundException e) {
                System.out.println("Key " + key + " cannot "
                    + "not be measured.");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
}
