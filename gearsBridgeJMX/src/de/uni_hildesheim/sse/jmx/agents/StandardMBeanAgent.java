package de.uni_hildesheim.sse.jmx.agents;

import java.lang.management.ManagementFactory;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

import de.uni_hildesheim.sse.jmx.services.standard.BatteryData;
import de.uni_hildesheim.sse.jmx.services.standard.Data;
import de.uni_hildesheim.sse.jmx.services.standard.MemoryData;
import de.uni_hildesheim.sse.jmx.services.standard.NetworkData;
import de.uni_hildesheim.sse.jmx.services.standard.ProcessData;
import de.uni_hildesheim.sse.jmx.services.standard.ProcessorData;
import de.uni_hildesheim.sse.jmx.services.standard.ScreenData;
import de.uni_hildesheim.sse.jmx.services.standard.ThisProcessData;
import de.uni_hildesheim.sse.jmx.services.standard.VolumeData;

/**
 * Defines an agent for the Management of the standard MBeans.
 * 
 * @author Stephan Dederichs
 * 
 * @since 1.00
 * @version 1.00
 */
public class StandardMBeanAgent {

    /**
     * Instance of the {@link BatteryData}.
     * 
     * @since 1.00
     */
    private BatteryData batteryData = new BatteryData();

    /**
     * Private Constructor of the agent. Actually not used from outside.
     * 
     * @since 1.00
     */
    private StandardMBeanAgent() {
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        registerStandardMBeans(mbs);
    }

    /**
     * Registration method for standard MBeans.
     * 
     * @param mbs Instance of the MBeanServer on which the standard MBeans
     *            should be registered.
     * 
     * @since 1.00
     */
    private void registerStandardMBeans(MBeanServer mbs) {
        try {
            // battery data
            ObjectName oBatteryData = new ObjectName("gearsBridgeJ:type="
                    + "system,name=Battery");
            mbs.registerMBean(batteryData, oBatteryData);
            // data
            ObjectName oData = new ObjectName("gearsBridgeJ:type="
                    + "system,name=Data");
            mbs.registerMBean(new Data(), oData);
            // memory data
            ObjectName oMemoryData = new ObjectName("gearsBridgeJ:type"
                    + "=system,name=Memory");
            mbs.registerMBean(new MemoryData(), oMemoryData);
            // network data
            ObjectName oNetworkData = new ObjectName("gearsBridgeJ:type="
                    + "system,name=Network");
            mbs.registerMBean(new NetworkData(), oNetworkData);
            // process data
            ObjectName oProcessData = new ObjectName("gearsBridgeJ:type="
                    + "system,name=Process");
            mbs.registerMBean(new ProcessData(), oProcessData);
            // processor data
            ObjectName oProcessorData = new ObjectName("gearsBridgeJ:type="
                    + "system,name=Processor");
            mbs.registerMBean(new ProcessorData(), oProcessorData);
            // screen data
            ObjectName oScreenData = new ObjectName("gearsBridgeJ:type="
                    + "system,name=Screen");
            mbs.registerMBean(new ScreenData(), oScreenData);
            // this process data
            ObjectName oThisProcessData = new ObjectName("gearsBridgeJ:type="
                    + "system,name=ThisProcess");
            mbs.registerMBean(new ThisProcessData(), oThisProcessData);
            // screen data
            ObjectName oVolumeData = new ObjectName("gearsBridgeJ:type="
                    + "system,name=Volume");
            mbs.registerMBean(new VolumeData(), oVolumeData);
            // register additional MBeans here
        } catch (MalformedObjectNameException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (InstanceAlreadyExistsException e) {
            e.printStackTrace();
        } catch (MBeanRegistrationException e) {
            e.printStackTrace();
        } catch (NotCompliantMBeanException e) {
            e.printStackTrace();
        }
    }

    /**
     * Utility method for continuous running the application.
     * 
     * @since 1.00
     */
    @SuppressWarnings("static-access")
    private void proceed() {
        // Continuous runnning this loop
        while (true) {
            try {
                // Writing some battery informations
                System.out.println("has system battery: "
                        + batteryData.hasSystemBattery()
                        + " / battery life time: "
                        + batteryData.getBatteryLifeTime()
                        + " / battery life percent: "
                        + batteryData.getBatteryLifePercent()
                        + " / power plug status: "
                        + batteryData.getPowerPlugStatus());
                // Wait two seconds
                Thread.currentThread().sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Main method for starting the agent.
     * 
     * @param args program parameters.
     * 
     * @since 1.00
     */
    public static void main(final String[] args) {
        // create agent
        StandardMBeanAgent agent = new StandardMBeanAgent();
        // Debug-Out
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT+1:00"));
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        System.out.println("Info\t" + sdf.format(cal.getTime())
                + " - StandardMBeanAgent is running...");
        // doing something...
        agent.proceed();
    }

}
