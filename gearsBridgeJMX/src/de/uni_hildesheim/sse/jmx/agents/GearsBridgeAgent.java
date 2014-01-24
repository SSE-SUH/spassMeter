package de.uni_hildesheim.sse.jmx.agents;

import java.lang.management.ManagementFactory;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Logger;

import javax.management.ListenerNotFoundException;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanServer;
import javax.management.Notification;
import javax.management.NotificationBroadcaster;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectName;

import de.uni_hildesheim.sse.jmx.configuration.ConfigurationReader;

/**
 * Management Agent for the JMX services.
 * 
 * @author Stephan Dederichs
 * 
 * @since 1.00
 * @version 1.00
 */
public class GearsBridgeAgent implements NotificationBroadcaster {

    /**
     * Constant for the notification type "functions.update".
     * 
     * @since 1.00
     */
    private final String notifyFunctionsUpdate = "functions.update";
    
    /**
     * Count for {@link Notification}s.
     * 
     * @since 1.00
     */
    private int notificationsCount = 0;

    /**
     * Instance of {@link MBeanServer}. The JMX services (MBeans) will be 
     * registered in this instance.
     * 
     * @since 1.00
     */
    private MBeanServer mbs;

    /**
     * {@link List} which hold all registered listeners who will receive events.
     * 
     * @since 1.00
     */
    private List<NotificationListener> listeners = new LinkedList
            <NotificationListener>();

    /**
     * Constructor of the agent.
     * 
     * @since 1.00
     */
    public GearsBridgeAgent() {
        mbs = ManagementFactory.getPlatformMBeanServer();
    }

    /**
     * registers a JMX services under a given {@link ObjectName} in the
     * {@link MBeanServer}.
     * 
     * @param service The JMX services (MBean) which should be registered.
     * @param serviceObjectName The {@link ObjectName} under which the JMX 
     *            services should be registered.
     * 
     * @since 1.00
     */
    public void registerService(Object service, ObjectName serviceObjectName) {
        try {
            mbs.registerMBean(service, serviceObjectName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Reads the configuration file in the given path and configures the
     * {@link MBeanServer} on the basis of the configuration file.
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
        ConfigurationReader reader = new ConfigurationReader(mbs, this, log);
        reader.readConfiguration(path);
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
     * Sends an update {@link Notification} to all registered listeners.
     * 
     * @param source The Object which is triggering the function.
     * @param handback The handback object.
     * 
     * @since 1.00
     */
    public void sendUpdateNotification(Object source, Object handback) {
        Notification notification = new Notification(
                notifyFunctionsUpdate, this, 
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
        notificationTypes[0] = notifyFunctionsUpdate;
        // Creating an MBeanNotificationInfo array and adding the notification
        // types array
        MBeanNotificationInfo[] notifications = new MBeanNotificationInfo[1];
        notifications[0] = new MBeanNotificationInfo(notificationTypes,
                "NotificationTypes",
                "Types of notifications emitted by this broadcaster.");
        return notifications;
    }

    /**
     * Main method for starting the agent.
     * 
     * @param args program parameters.
     * 
     * @since 1.00
     */
    public static void main(String[] args) {
        // create agent
        GearsBridgeAgent agent = new GearsBridgeAgent();
        // Debug-Out
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT+1:00"));
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        System.out.println("Info\t" + sdf.format(cal.getTime())
                + " - GearsBridgeAgent is running...");
        // reading configuration
        String path = "./src/de/uni_hildesheim/sse/jmx/configuration/"
                + "configuration.xml";
        agent.readConfiguration(path, null);
        // doing something...
        agent.proceed();
    }

}
