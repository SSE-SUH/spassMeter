package de.uni_hildesheim.sse.wildcat.services;

import java.util.HashMap;
import java.util.Map;

import de.uni_hildesheim.sse.wildcat.gui.WildCATContextTreeWindow;
import de.uni_hildesheim.sse.wildcat.plugins.InstrumentationContextConstants;
import de.uni_hildesheim.sse.wildcat.plugins.WildCATPlugin;
import de.uni_hildesheim.sse.wildcat.sensors.Sensor;

/**
 * Central access to all services.
 * 
 * @author Stephan Dederichs
 * 
 * @since 1.00
 * @version 1.00
 */
public class WServiceRegistry {
    
    /**
     * Stores the WildCAT services.
     * 
     * @since 1.00
     */
    private static Map<String, AbstractWServiceData> services = new 
        HashMap<String, AbstractWServiceData>();
    
    /**
     * Prevents this class from being created from outside.
     * 
     * @since 1.00
     */
    private WServiceRegistry() {
    }
    
    /**
     * Registers a WildCAT service in the {@link Context}.
     * 
     * @param recId The id where to access the WildCAT service.
     * @param service The WildCAT service to register.
     * 
     * @since 1.00
     */
    public static final void registerService(String recId, 
        AbstractWServiceData service) {
        try {
            recId = prepareRecId(recId);
            // Creating an ObjectName
            String path = null;
            Sensor sensor = null;
            if (service instanceof WMonitoringGroup) {
                createSpassMonitorResource(InstrumentationContextConstants.
                    SPASS_MONITOR_DOMAIN_MONITORING_GROUP);
                // creating path
                path = InstrumentationContextConstants.
                        SPASS_MONITOR_DOMAIN_MONITORING_GROUP + "#" + recId;
            } else if (service instanceof WTimer) {
                createSpassMonitorResource(InstrumentationContextConstants.
                    SPASS_MONITOR_DOMAIN_TIMER);
                // creating path
                path = InstrumentationContextConstants.
                        SPASS_MONITOR_DOMAIN_TIMER + "#" + recId;      
            } else if (service instanceof WValue) {
                createSpassMonitorResource(InstrumentationContextConstants.
                    SPASS_MONITOR_DOMAIN_VALUE);
                // creating path
                path = InstrumentationContextConstants.
                        SPASS_MONITOR_DOMAIN_VALUE + "#" + recId; 
            } else if (service instanceof WMeasurements) {
                createSpassMonitorResource(InstrumentationContextConstants.
                    SPASS_MONITOR_DOMAIN_MEASUREMENTS);
                // creating path
                path = InstrumentationContextConstants.
                        SPASS_MONITOR_DOMAIN_MEASUREMENTS + "#" + recId;
            } else if (service instanceof WConfiguration) {
                // creating path
                path = InstrumentationContextConstants.SPASS_MONITOR_DOMAIN 
                        + "#" + recId;                                
            } else {
                return;
            }
            // Only adding if not already added 
            if (null != path && null == services.get(recId)) {
                // creating a new sensor
                sensor = new Sensor(recId);
                // adding the sensor to the class
                service.setSensor(sensor);
                // add service to services
                services.put(recId, service);
                // adding the class to the NotificationBroadcaster
                WildCATPlugin.GEARSBRIDGELAUNCHER.addNotificationListener(
                    service, null, null);
                // add attribute to the context
                WildCATPlugin.GEARSBRIDGELAUNCHER.registerService(sensor, path);
            }
            updateServices(WServiceRegistry.class);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Creating of SPASS-Monitor resources.
     * 
     * @param path The resource to create.
     * 
     * @since 1.00
     */
    public static void createSpassMonitorResource(String path) {
        WildCATPlugin.GEARSBRIDGELAUNCHER.createResource(path);
    }
    
    /**
     * Updates the registered services.
     * 
     * @param source The source of the event.
     * 
     * @since 1.00
     */
    public static final void updateServices(Object source) {
        WildCATPlugin.GEARSBRIDGELAUNCHER.sendUpdateNotification(source, 
            null);
        WildCATContextTreeWindow.updateGUI();
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
    public static final AbstractWServiceData getService(String recId) {
        recId = prepareRecId(recId);
        return services.get(recId);
    }
    
    /**
     * Replacing all illegal characters ("*", ".", "-", "@" and " ") in the 
     * recId with "".
     * 
     * @param recId The String to check.
     * 
     * @return The given recId without "*", ".", "-", "@" and " " in the string.
     * 
     * @since 1.00
     */
    public static String prepareRecId(String recId) {
        String newRecId = recId.replace(".", "");
        newRecId = newRecId.replace("-", "");
        newRecId = newRecId.replace("*", "");
        newRecId = newRecId.replace("$", "");
        newRecId = newRecId.replace("@", "");
        newRecId = newRecId.replace(" ", "");
        return newRecId;
    }
    
}
