package de.uni_hildesheim.sse.wildcat.test;

import test.testing.AbstractTestPlugin;
import test.testing.ILogger;
import test.testing.MonitoringGroupValue;
import de.uni_hildesheim.sse.serviceConstants.MonitoringGroupConstants;
import de.uni_hildesheim.sse.wildcat.services.AbstractWServiceData;
import de.uni_hildesheim.sse.wildcat.services.WServiceRegistry;
import de.uni_hildesheim.sse.wildcat.services.WTimer;
import de.uni_hildesheim.sse.wildcat.services.WValue;

/**
 * Implements a test environment for WildCAT.
 * 
 * @author Stephan Dederichs
 * @since 1.00
 * @version 1.00
 */
public class WildCATTestPlugin extends AbstractTestPlugin {

    /**
     * Creates a new test plugin for WildCAT.
     * 
     * @param logger The logger to be used (may be <b>null</b>).
     * 
     * @since 1.00
     */
    public WildCATTestPlugin(ILogger logger) {
        super(logger);
    }

    @Override
    public void initialize() {
        // Nothing to do here...
    }
    
    @Override
    public Long getValue(String recId, MonitoringGroupValue value) {
        String query = null; 
        switch (value) {
        case CPU_TIME:
            query = MonitoringGroupConstants.CPU_TIME_TICKS;
            break;
        case ALLOCATED_MEMORY:
            query = MonitoringGroupConstants.MEM_ALLOCATED;
            break;
        case FILE_READ:
            query = MonitoringGroupConstants.FILE_IN;
            break;
        case FILE_WRITE:
            query = MonitoringGroupConstants.FILE_OUT;
            break;
        case NET_READ:
            query = MonitoringGroupConstants.NET_IN;
            break;
        case NET_WRITE:
            query = MonitoringGroupConstants.NET_OUT;
            break;
        case SYSTEM_TIME:
            query = MonitoringGroupConstants.SYSTEM_TIME_TICKS;
            break;
        case TOTAL_READ:
            query = MonitoringGroupConstants.IO_READ;
            break;
        case TOTAL_WRITE:
            query = MonitoringGroupConstants.IO_WRITE;
            break;
        case USED_MEMORY:
            query = MonitoringGroupConstants.MEM_USE;
            break;
        default:
            System.out.println(value.toString() + " default");
            query = null;
            break;
        }
        AbstractWServiceData service = getService(recId);
        Long result = null;
        if (null != service) {
            try {
                result = Long.parseLong(service.getAttribute(query).toString());
            } catch (Exception e) {
                System.out.println(value.toString() + " - " + query);
                System.out.println("service: " + service);
                result = -1L;
                e.printStackTrace();
            }
        }
        return result;
    }
    
    @Override
    public Long getTimerData(String recId) {
        WTimer timer = (WTimer) getService(recId);
        Long result = null;
        if (null != timer) {
            result = timer.getValue();
        }
        return result;
    }
    
    @Override
    public Object getAttributeData(String recId) {
        WValue value = (WValue) getService(recId);
        Object result = null;
        if (null != value) {
            result = value.getNewValue();
        }
        return result;
    }
    
    /**
     * Returns the {@link AbstractJMXServiceData} for the required recId or 
     * null if no service is found.
     * 
     * @param recId The recId for the service.
     * 
     * @return The {@link AbstractJMXServiceData} for the required recId or 
     *            null if no service is found.
     *            
     * @since 1.00
     */
    public AbstractWServiceData getService(String recId) {
        return WServiceRegistry.getService(recId);
    }

    /**
     * Returns whether this test plugin supports configurations.
     * 
     * @return <code>true</code> if it supports configurations, 
     *   <code>false</code> else
     * 
     * @since 1.00
     */
    public boolean supportsConfigurations() {
        return false;
    }
    
    /**
     * Returns the number of configurations recorder.
     * 
     * @return the number of configurations
     * 
     * @since 1.00
     */
    public int getNumberOfConfigurations() {
        return 0;
    }

    /**
     * Returns the recording identifier of the specific configuration.
     * 
     * @param index the index of the configuration
     * @return the recording identifier (consisting of the names of the 
     *   individual recorder ids separated by ",")
     * @throws IndexOutOfBoundsException if <code>index &lt; 0 
     *   || index &gt;={@link #getNumberOfConfigurations()}</code>
     * 
     * @since 1.00
     */
    public String getConfigurationId(int index) {
        throw new IndexOutOfBoundsException();
    }

    /**
     * Returns the recorded value of the specific configuration.
     * 
     * @param index the index of the configuration
     * @param value the value to be returned
     * @return the value or <b>null</b> if not accessible etc.
     * @throws IndexOutOfBoundsException if <code>index &lt; 0 
     *   || index &gt;={@link #getNumberOfConfigurations()}</code>
     * 
     * @since 1.00
     */
    public Long getConfigurationValue(int index, 
        MonitoringGroupValue value) {
        throw new IndexOutOfBoundsException();
    }

}
