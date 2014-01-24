package de.uni_hildesheim.sse.wildcat.plugins;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import de.uni_hildesheim.sse.monitoring.runtime.configuration.Configuration;
import de.uni_hildesheim.sse.monitoring.runtime.plugins.IPluginParameter;
import de.uni_hildesheim.sse.monitoring.runtime.plugins.Plugin;
import de.uni_hildesheim.sse.monitoring.runtime.plugins.PluginRegistry;
import de.uni_hildesheim.sse.wildcat.gui.WildCATContextTreeWindow;
import de.uni_hildesheim.sse.wildcat.launcher.GearsBridgeLauncher;
import de.uni_hildesheim.sse.wildcat.listeners.
    WildCATMonitoringGroupCreationListener;
import de.uni_hildesheim.sse.wildcat.listeners.WildCATTimerChangeListener;
import de.uni_hildesheim.sse.wildcat.listeners.WildCATValueChangeListener;
import de.uni_hildesheim.sse.wildcat.services.WConfiguration;
import de.uni_hildesheim.sse.wildcat.services.WServiceRegistry;

/**
 * WildCAT Plugin for SPASS-monitor.
 * 
 * @author Stephan Dederichs
 * 
 * @since 1.00
 * @version 1.00
 */
public class WildCATPlugin implements Plugin {
    
    /**
     * Stores The {@link GearsBridgeLauncher}.
     * 
     * @since 1.00
     */
    public static final GearsBridgeLauncher GEARSBRIDGELAUNCHER = new 
            GearsBridgeLauncher();
    
    /**
     * WildCAT context GUI.
     * 
     * @since 1.00
     */
    private WildCATContextTreeWindow wnd;
    
    /**
     * Public default constructor.
     * 
     * @since 1.00
     */
    public WildCATPlugin() {
    }

    @Override
    public void start(IPluginParameter args) {
        String path = null;
        // Checking args
        if (null != args) {
            // getting path for configuration file from parameter
            path = args.get("wildcatConfig");
        }
        // if no path specified -> default configuration
        if (null == path) {
            Configuration.LOG.info("Configuration file not specified, "
                + "loading default configuration.");
            path = "../InstrumentationWildCAT/src/de/uni_hildesheim"
                    + "/sse/wildcat/configuration/config.xml";
        }
        
        // Reading configuration
        GEARSBRIDGELAUNCHER.readConfiguration(path, Configuration.LOG);
        // attach listeners
        WildCATMonitoringGroupCreationListener mgListener = new 
                WildCATMonitoringGroupCreationListener();
        PluginRegistry.attachMonitoringGroupCreationListener(mgListener);
        PluginRegistry.attachMonitoringGroupBurstChangeListener(mgListener);
        PluginRegistry.attachTimerChangeListener(new 
                WildCATTimerChangeListener());
        PluginRegistry.attachValueChangeListener(new 
                WildCATValueChangeListener());
        // Initial creatin of required resources
        WServiceRegistry.createSpassMonitorResource(
                InstrumentationContextConstants.SPASS_MONITOR_DOMAIN);
        // Registering the configurationService
        WServiceRegistry.registerService("Configuration", new WConfiguration());
        
        // open GUI only if explicitly specified in parameters
        // parameters == null --> no GUI 
        if (null != args) {
            // checking if parameter "wildcatGUI" is available an open the 
            // GUI if available
            String gui = args.get("wildcatGUI");
            if (null != gui) {
                openGUI();
            }
        }
    }

    @Override
    public void stop() {
    }
    
    /**
     * Opens the WildCAT Context Tree Window.
     * 
     * @since 1.00
     */
    private void openGUI() {
        wnd = new WildCATContextTreeWindow(
                GEARSBRIDGELAUNCHER.getContext());
        wnd.open();
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
                GEARSBRIDGELAUNCHER.sendUpdateNotification(this, null);
                // Updating gui
                wnd.updateGUI();
                // Wait two seconds
                Thread.currentThread().sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Main method for testing.
     * 
     * @param args program parameter.
     */
    public static void main(String[] args) {
        // create plugin
        Plugin plugIn = new WildCATPlugin();
        // Debug-Out
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT+1:00"));
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        System.out.println("Info\t" + sdf.format(cal.getTime())
                + " - WildCATPlugin is running...");
        // starting plugin
        final Map<String, String> params = new HashMap<String, String>();
        params.put("wildcatConfig", 
                "../InstrumentationWildCAT/src/de/uni_hildesheim"
                        + "/sse/wildcat/configuration/config.xml");
        params.put("wildcatGUI", "true");
        plugIn.start(new IPluginParameter() {
            @Override
            public String get(String key) {
                return params.get(key);
            }
        });
        
        // doing something
        ((WildCATPlugin) plugIn).proceed();
    }

}
