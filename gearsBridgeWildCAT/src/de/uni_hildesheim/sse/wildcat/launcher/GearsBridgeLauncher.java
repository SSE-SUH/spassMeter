package de.uni_hildesheim.sse.wildcat.launcher;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import java.util.logging.Logger;

import javax.management.ListenerNotFoundException;
import javax.management.MBeanNotificationInfo;
import javax.management.Notification;
import javax.management.NotificationBroadcaster;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;

import org.ow2.wildcat.Context;
import org.ow2.wildcat.ContextException;
import org.ow2.wildcat.ContextFactory;
import org.ow2.wildcat.sensors.DateTimeSensor;
import org.ow2.wildcat.sensors.JavaRuntimeSensor;
import org.ow2.wildcat.sensors.Sensor;
import org.ow2.wildcat.sensors.SystemPropertiesSensor;

import de.uni_hildesheim.sse.wildcat.configuration.ConfigurationReader;

/**
 * Launcher for gearsBridgeWildCAT.
 * 
 * @author Stievi
 * 
 * @version 1.00
 * @since 1.00
 */
public class GearsBridgeLauncher implements NotificationBroadcaster {
    
    /**
     * Constant for the notification type "update".
     * 
     * @since 1.00
     */
    private static final String notifyBurstUpdate = "burst.update";
    
    /**
     * Count for {@link Notification}s.
     * 
     * @since 1.00
     */
    private int notificationsCount = 0;
    
    /**
     * {@link List} which hold all registered listeners who will receive events.
     * 
     * @since 1.00
     */
    private List<NotificationListener> listeners = new LinkedList
            <NotificationListener>();
    
    /**
     * Stores the WildCAT context.
     * 
     * @since 1.00
     */
    private Context ctx;
    
    /**
     * Constructor.
     * 
     * @since 1.00
     */
    public GearsBridgeLauncher() {
        ctx = ContextFactory.getDefaultFactory().createContext(
                GearsBridgeContextConstants.CONTEXT_NAME);
    }
    
    /**
     * Returns the {@link Context}.
     * 
     * @return The {@link Context}.
     * 
     * @since 1.00
     */
    public Context getContext() {
        return ctx;
    }
    
    /**
     * Creates a resource in the {@link Context}.
     * 
     * @param path The resource path.
     * 
     * @since 1.00
     */
    public void createResource(String path) {
        // adding the resource to the context
        try {
            ctx.createResource(path);
        } catch (ContextException e) {
            Calendar cal = new GregorianCalendar(
                    TimeZone.getTimeZone("GMT+1:00"));
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
            System.out.println("Error\t" + sdf.format(cal.getTime())
                    + " - Warning while creating WildCAT resource.");
        }
    }
    
    /**
     * Registers a service in the {@link Context}.
     * 
     * @param sensor The sensor to register.
     * @param path The path to reach the sensor.
     * 
     * @since 1.00
     */
    public void registerService(de.uni_hildesheim.sse.wildcat.sensors.Sensor 
            sensor, String path) {
        try {
            ctx.attachAttribute(path, sensor);
        } catch (ContextException e) {
            Calendar cal = new GregorianCalendar(
                    TimeZone.getTimeZone("GMT+1:00"));
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
            System.out.println("Error\t" + sdf.format(cal.getTime())
                    + " - Warning while creating WildCAT resource.");
        }
    }
    
    /**
     * Registers the WildCAT build in sensors.
     * <ul>
     * <li>{@link DateTimeSensor}: DateTimeSensor, exposes date and time from 
     *     the JVM. </li>
     * <li>{@link JavaRuntimeSensor}: JavaRuntimeSensor, exposes JVM Runtime 
     *     info. </li>
     * <li>{@link SystemPropertiesSensor}: SystemPropertiesSensor exposes the 
     *     System.getProperties() object. </li>
     * </ul>
     * 
     * @since 1.00
     */
    private void registerWildCATBuildInServices() {
        try {
            // adding the resource to the context
            ctx.createResource(GearsBridgeContextConstants.WILDCAT_DATA_DOMAIN);
            
            // JavaRunTimeSensor
            Sensor javaRuntimeSensor = new JavaRuntimeSensor();
            ctx.attachAttribute(GearsBridgeContextConstants.WILDCAT_DATA_DOMAIN 
                    + "#JavaRuntime", javaRuntimeSensor);
            
            // SystemPropertySensor
            Sensor systemPropertiesSensor = new SystemPropertiesSensor();
            ctx.attachAttribute(GearsBridgeContextConstants.WILDCAT_DATA_DOMAIN 
                    + "#SystemProperties", systemPropertiesSensor);
            
            // DateTimeSensor
            Sensor dateTimeSensor = new DateTimeSensor();
            ctx.attachAttribute(GearsBridgeContextConstants.WILDCAT_DATA_DOMAIN 
                    + "#DateTime", dateTimeSensor);
        } catch (ContextException e) {
            e.printStackTrace();
        }        
    }
    
    /**
     * Reads the configuration file in the given path and configures the
     * {@link Context} on the basis of the configuration file.
     * 
     * @param path The path to the configuration file.
     * @param log For logging.
     * 
     * @since 1.00
     */
    public void readConfiguration(String path, Logger log) {
        if (null != log) {
            log.info("reading configuration...");   
        } else {
            System.out.println("reading configuration...");
        }
        ConfigurationReader reader = new ConfigurationReader(ctx, this, log);
        reader.readConfiguration(path);
        // Register WildCAT build in services
        registerWildCATBuildInServices();
    }

    /**
     * Utility Method for continuous running the application.
     * 
     * @since 1.00
     */
    @SuppressWarnings("static-access")
    public void proceed() {
        // Continuous runnning this loop
        while (true) {
            try {
                // sending a notification to all listeners
                sendUpdateNotification(this, null);
                // Wait two seconds
                Thread.currentThread().sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Prints all gearsBridge parts in the {@link Context}.
     * 
     * @param domains An Array with the domains to print.
     * 
     * @since 1.00
     */
    private void printData(String[] domains) {
        for (String domain : domains) {
            try {
                Set<String> services = ctx.list(domain);
                System.out.println("Available services in the domain \"" 
                        + domain + "\": " 
                        + services);
                if (null != services) {
                    for (String s : services) {
                        System.out.println("\tAvailable values in \"" + s 
                                + "\": " + ctx.getValue(domain +  s));    
                    }
                }
            } catch (ContextException e) {
                e.printStackTrace();
            }
        }
        System.out.println("-#-#-#-#-#-#-#-#-#-#-#-#-#-#-#-#-#-#-#-#-");
    }
    
    /**
     * Sends an update {@link Notification} to all registered listeners.
     * 
     * @param source The Object which is triggering the function.
     * @param handback The handback object.
     * 
     * @since 1.00
     */
    public void sendUpdateNotification(Object source, Object handback) {
        Notification notification = new Notification(
                notifyBurstUpdate, this, 
                notificationsCount, "Update of all functions");
        for (NotificationListener l : listeners) {
            l.handleNotification(notification, handback);
        }
        // incrementing the count
        notificationsCount++;
    }

    @Override
    public void addNotificationListener(NotificationListener listener,
        NotificationFilter filter, Object handback)
        throws IllegalArgumentException {
        // Adding new listener to the list with all listeners
        listeners.add(listener);
    }

    @Override
    public void removeNotificationListener(NotificationListener listener)
        throws ListenerNotFoundException {
        // Remove given listener from the list with all listeners
        listeners.remove(listener);
    }

    @Override
    public MBeanNotificationInfo[] getNotificationInfo() {
        // Creating an array for the notification type constants and adding the
        // constants
        String[] notificationTypes = new String[1];
        notificationTypes[0] = notifyBurstUpdate;
        // Creating an MBeanNotificationInfo array and adding the notification
        // types array
        MBeanNotificationInfo[] notifications = new MBeanNotificationInfo[1];
        notifications[0] = new MBeanNotificationInfo(notificationTypes,
                "NotificationTypes",
                "Types of notifications emitted by this broadcaster.");
        return notifications;
    }

    /**
     * Starts the {@link GearsBridgeLauncher}.
     * 
     * @param args Program arguments.
     * 
     * @since 1.00
     */
    public static void main(String[] args) {
        GearsBridgeLauncher gbl = new GearsBridgeLauncher();
        // Debug-Out
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT+1:00"));
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        System.out.println("Info\t" + sdf.format(cal.getTime())
                + " - GearsBridgeLauncher is running...");

        // reading configuration
        String path = "./src/de/uni_hildesheim/sse/wildcat/configuration/"
                + "configuration.xml";
        gbl.readConfiguration(path, null);
        
        gbl.printData(new String[]{
            GearsBridgeContextConstants.GEARSBRIDGE_DATA_DOMAIN,
            GearsBridgeContextConstants.WILDCAT_DATA_DOMAIN});
        
        // doing something...
        gbl.proceed();
    }

}
