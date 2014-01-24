package de.uni_hildesheim.sse.jmx.plugins;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import de.uni_hildesheim.sse.jmx.agents.GearsBridgeAgent;
import de.uni_hildesheim.sse.jmx.listeners.JMXMonitoringGroupCreationListener;
import de.uni_hildesheim.sse.jmx.listeners.JMXTimerChangeListener;
import de.uni_hildesheim.sse.jmx.listeners.JMXValueChangeListener;
import de.uni_hildesheim.sse.jmx.services.JMXConfiguration;
import de.uni_hildesheim.sse.jmx.services.JMXServiceRegistry;
import de.uni_hildesheim.sse.monitoring.runtime.configuration.Configuration;
import de.uni_hildesheim.sse.monitoring.runtime.plugins.IPluginParameter;
import de.uni_hildesheim.sse.monitoring.runtime.plugins.Plugin;
import de.uni_hildesheim.sse.monitoring.runtime.plugins.PluginRegistry;

/**
 * JMX Plugin for SPASS-monitor.
 * 
 * @author Stephan Dederichs
 * 
 * @since 1.00
 * @version 1.00
 */
public class JMXPlugin implements Plugin {

    /**
     * Stores The {@link GearsBridgeAgent}.
     * 
     * @since 1.00
     */
    public static final GearsBridgeAgent GEARSBRIDGEAGENT = new 
        GearsBridgeAgent();
    
    /**
     * Public default constructor.
     * 
     * @since 1.00
     */
    public JMXPlugin() {
    }

    @Override
    public void start(IPluginParameter args) {
        String path = null;
        // Checking args
        if (null != args) {
            // getting path for configuration file from parameter
            path = args.get("jmxConfig");
        }
        // if no path specified -> default configuration
        if (null == path) {
            Configuration.LOG.info("Configuration file not specified, "
                + "loading default configuration.");
            path = "../InstrumentationJMX/src/de/uni_hildesheim/sse/jmx/"
                + "configuration/config.xml";
        }

        // Reading configuration      
        GEARSBRIDGEAGENT.readConfiguration(path, Configuration.LOG);
        // attach listeners
        JMXMonitoringGroupCreationListener mgListener = 
            new JMXMonitoringGroupCreationListener();
        PluginRegistry.attachMonitoringGroupCreationListener(mgListener);
        PluginRegistry.attachMonitoringGroupBurstChangeListener(mgListener);
        PluginRegistry.attachTimerChangeListener(new JMXTimerChangeListener());
        PluginRegistry.attachValueChangeListener(new JMXValueChangeListener());
        // Registering the configurationService
        JMXServiceRegistry.registerService("Configuration", 
                new JMXConfiguration());
    }

    @Override
    public void stop() {
    }

    /**
     * Main method for testing.
     * 
     * @param args program parameter.
     */
    public static void main(String[] args) {
        // create plugin
        Plugin plugIn = new JMXPlugin();
        // Debug-Out
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT+1:00"));
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        System.out.println("Info\t" + sdf.format(cal.getTime())
                + " - JMXPlugin is running...");
        // starting plugin
        final Map<String, String> params = new HashMap<String, String>();
        params.put("jmxConfig", "../InstrumentationJMX/src/de/uni_hildesheim"
            + "/sse/jmx/configuration/config.xml");
        plugIn.start(new IPluginParameter() {
            @Override
            public String get(String key) {
                return params.get(key);
            }
        });
        
        GEARSBRIDGEAGENT.proceed();
    }

}
