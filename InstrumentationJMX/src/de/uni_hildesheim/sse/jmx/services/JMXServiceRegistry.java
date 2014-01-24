package de.uni_hildesheim.sse.jmx.services;

import java.util.HashMap;
import java.util.Map;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import de.uni_hildesheim.sse.jmx.plugins.JMXPlugin;
import de.uni_hildesheim.sse.jmx.services.dynamic.AbstractJMXServiceData;

/**
 * Central access to all services.
 * 
 * @author Stephan Dederichs
 * 
 * @since 1.00
 * @version 1.00
 */
public class JMXServiceRegistry {
    
    /**
     * Stores the JMX services.
     * 
     * @since 1.00
     */
    private static Map<String, AbstractJMXServiceData> services = new 
        HashMap<String, AbstractJMXServiceData>();
    
    /**
     * Prevents this class from being created from outside.
     * 
     * @since 1.00
     */
    private JMXServiceRegistry() {
    }
    
    /**
     * Registers a JMX service at the {@link MBeanServer}.
     * 
     * @param recId The id where to access the JMX service.
     * @param service The JMX service to register.
     * 
     * @since 1.00
     */
    public static final void registerService(String recId, 
        AbstractJMXServiceData service) {
        try {
            recId = prepareRecId(recId);
            // Creating an ObjectName
            ObjectName oName = null;
            if (service instanceof JMXMonitoringGroup) {
                oName = new ObjectName(
                        "SPASS-meter:folder=monitoring groups,name=" + recId);
            } else if (service instanceof JMXTimer) {
                oName = new ObjectName(
                        "SPASS-meter:folder=timer,name=" + recId);
            } else if (service instanceof JMXValue) {
                oName = new ObjectName(
                        "SPASS-meter:folder=values,name=" + recId);
            } else if (service instanceof JMXMeasurements) {
                oName = new ObjectName(
                        "SPASS-meter:folder=system,name=" + recId);
            } else if (service instanceof JMXConfiguration) {
                oName = new ObjectName(
                        "SPASS-meter:name=" + recId);
            } else {
                return;
            }
            // Only adding if not already added 
            if (null == services.get(oName.toString())) {
                services.put(recId, service);
                // Registering the JMX service
                JMXPlugin.GEARSBRIDGEAGENT.registerService(service, 
                    oName);
            }
          
        } catch (MalformedObjectNameException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Updates the gearsBridge service.
     * 
     * @param source The source of the event.
     * 
     * @since 1.00
     */
    public static final void updateServices(Object source) {
        JMXPlugin.GEARSBRIDGEAGENT.sendUpdateNotification(source, 
            null);
    }
    
    /**
     * Returns a service with the recId if registered. Otherwise it will return 
     * null.
     * 
     * @param recId The recId of the service.
     * 
     * @return A service if it is registered, otherwise null.
     * 
     * @since 1.00
     */
    public static final AbstractJMXServiceData getService(String recId) {
        recId = prepareRecId(recId);
        return services.get(recId);
    }
    
    /**
     * Replacing all "*" in the recId with "" because of problems with stars 
     * in the name.
     * 
     * @param recId The String to check.
     * 
     * @return The given recId without "*" in the string.
     * 
     * @since 1.00
     */
    public static String prepareRecId(String recId) {
        return recId.replace("*", "");
    }
    
}
