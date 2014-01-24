package de.uni_hildesheim.sse.monitoring.runtime.plugins;

import de.uni_hildesheim.sse.monitoring.runtime.annotations.Helper;

/**
 * Defines a listener for creations of monitoring group changes. This listener
 * informs you exclusively on creating the elements.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public interface MonitoringGroupCreationListener {
    
    /**
     * Declares the recorder identification used to notify the recordings
     * of the entire program.
     */
    public static final String ID_PROGRAM = Helper.PROGRAM_ID;

    /**
     * Declares the recorder identification used to notify the recordings
     * of the overhead (data depends on configuration).
     */
    public static final String ID_OVERHEAD = Helper.RECORDER_ID;
    
    /**
     * Declares the recorder identification used to notify the recordings
     * of excluded SUM parts. May not be present if not configured.
     */
    public static final String EXCLUDED_ID = Helper.EXCLUDED_ID;
    
    /**
     * Is called when a monitoring group is created.
     * 
     * @param recId the recording identification
     * @param elt the related information
     *
     * @since 1.00
     */
    public void monitoringGroupCreated(String recId, IMonitoringGroup elt);

    /**
     * Is called when a configuration of monitoring groups is created. The 
     * availability of this information is dependent on the 
     * configuration of SPASS-meter.
     * 
     * @param recId the recording identification
     * @param elt the related information
     * 
     * @since 1.00
     */
    public void configurationCreated(String recId, IMonitoringGroup elt);

    /**
     * Is called when a contribution to a monitoring group is created . This is 
     * a subevent of {@link #configurationCreated(String, IMonitoringGroup)}. 
     * The availability of this information is dependent on the configuration
     * in SPASS-meter. 
     * 
     * @param recId the recording identification
     * @param contribution the identification of the contribution
     * @param elt the related information
     * 
     * @since 1.00
     */
    public void contributionCreated(String recId, String contribution, 
        IMonitoringGroup elt);

}
