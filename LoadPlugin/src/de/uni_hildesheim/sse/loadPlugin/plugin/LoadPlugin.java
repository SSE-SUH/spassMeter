package de.uni_hildesheim.sse.loadPlugin.plugin;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.SwingWorker;

import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

import com.sun.tools.jconsole.JConsolePlugin;

import de.uni_hildesheim.sse.loadPlugin.tabs.AbstractTab;
import de.uni_hildesheim.sse.loadPlugin.tabs.CpuLoadTab;
import de.uni_hildesheim.sse.loadPlugin.tabs.MemoryLoadTab;

/**
 * LoadPlugin is a subclass to com.sun.tools.jconsole.JConsolePlugin <br>
 * <br>
 * LoadPlugin is loaded and instantiated by JConsole. One instance is created
 * for each window that JConsole creates. It listens to the connected property
 * change. 
 * <br><br>
 * You can find the JConsole extension API at <code>http://enos.itcollege.ee/
 * ~jpoial/docs/jdk/api/jconsole/spec/index.html?com/sun/tools/jconsole/package
 * -summary.html</code>.
 * 
 * @author Stephan Dederichs
 * 
 * @version 1.00
 * @since 1.00
 */
public class LoadPlugin extends JConsolePlugin implements
        PropertyChangeListener {

    /**
     * Stores a {@link Map} for the tabs which should be displayed in the
     * jconsole.
     */
    private Map<String, JPanel> tabs = null;

    /**
     * Constructor.
     */
    public LoadPlugin() {
        // register itself as a listener
        addContextPropertyChangeListener(this);
    }

    /**
     * Returns a JLoadPlugin tab to be added in JConsole.
     * 
     * @return A JLoadPlugin tab to be added in JConsole.
     * 
     * @see JConsolePlugin#getTabs()
     * 
     * @since 1.00
     */
    @Override
    public Map<String, JPanel> getTabs() {
        if (tabs == null) {
            // use LinkedHashMap because I want a predictable order
            // of the tabs to be added in JConsole, 
            // if more than one tab is added :)
            tabs = new LinkedHashMap<String, JPanel>();
            tabs.put("Memory Load", new MemoryLoadTab(getContext()));
            tabs.put("CPU Load", new CpuLoadTab(getContext()));
        }
        return tabs;
    }

    /**
     * Returns a SwingWorker which is responsible for updating the JLoadPlugin
     * tab.
     * 
     * @return A SwingWorker which is responsible for updating the JLoadPlugin
     *         tab.
     * 
     * @see JConsolePlugin#newSwingWorker()
     * 
     * @since 1.00
     */
    @Override
    public SwingWorker<?, ?> newSwingWorker() {
        return new Worker();
    }
    
    /**
     * SwingWorker responsible for updating the GUI
     * 
     * It first gets the thread and CPU usage information as a background task
     * done by a worker thread so that it will not block the event dispatcher
     * thread.
     * 
     * When the worker thread finishes, the event dispatcher thread will invoke
     * the done() method which will update the UI.
     * 
     * @author Stephan Dederichs
     * 
     * @version 1.00
     * @since 1.00
     */
    class Worker extends SwingWorker<CategoryDataset, Object> {
        /**
         * Constructor.
         * 
         * @since 1.00
         */
        public Worker() {
        }

        @Override
        protected CategoryDataset doInBackground() throws Exception {
            // update chart values
            for (JPanel tab : tabs.values()) {
                if (tab instanceof AbstractTab) {
                    // ugly
                    ((AbstractTab) tab).updateChart();
                }
            }
            return new DefaultCategoryDataset();
        }
    }


    /**
     * Property listener to reset the MBeanServerConnection at reconnection
     * time.
     * 
     * @param ev The {@link PropertyChangeEvent}.
     * 
     * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
     * 
     * @since 1.00
     */
    @Override
    public void propertyChange(PropertyChangeEvent ev) {
        // Handle property changes here.
        // At this time nothing to do here...
    }

}
