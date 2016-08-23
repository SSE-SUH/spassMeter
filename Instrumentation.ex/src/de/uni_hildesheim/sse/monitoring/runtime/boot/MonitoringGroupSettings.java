package de.uni_hildesheim.sse.monitoring.runtime.boot;

/**
 * Helper class to transfer the values of a monitoring group (also compiled in
 * instrumented code) to the recorder. Be careful: Due to the JVM init and load
 * process, neither annotation instances nor the global configuration may be 
 * used here.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.20
 */
public class MonitoringGroupSettings {

    /**
     * Stores instances of this class for reuse. As class loading usually
     * happens not in parallel, not many instances will be stored here.
     */
    private static final ArrayList<MonitoringGroupSettings> POOL 
        = new ArrayList<MonitoringGroupSettings>(5);
    
    /**
     * Contains arbitrary ids for monitoring and grouping.
     * 
     * @since 1.00
     */
    private String[] id;

    /**
     * Returns a combination of debug states for additional information to
     * be emitted during monitoring.
     * 
     * @since 1.00
     */
    private DebugState[] debugStates;

    /**
     * Stores the group accounting to be applied. By default this value is set
     * to {@link GroupAccountingType#DEFAULT} and, therefore, taken from the 
     * global configuration.
     * 
     * @since 1.00
     */
    private GroupAccountingType gType; 
    
    /**
     * Stores the resources to be accounted. By default, all resources defined
     * in {@link ResourceType} are accounted.
     * 
     * @since 1.00
     */
    private ResourceType[] resources;

    /**
     * Stores the instance identifier kind.
     * 
     * @since 1.20
     */
    private InstanceIdentifierKind instanceIdentifierKind;
    
    /**
     * Stores whether values in multi groups should be distributed evenly to
     * the contained values or whether the entire value should be added to each
     * group.
     * 
     * @since 1.00
     */
    private BooleanValue distributeValues;

    /**
     * Stores whether accountable resources of the multi monitoring group
     * is authoritative or weather the contained groups should be considered.
     * 
     * @since 1.00
     */
    private BooleanValue considerContained;

    /**
     * Creates a new instance. Prevents creation from outside in order to enable
     * pooling.
     * 
     * @since 1.00
     */
    private MonitoringGroupSettings() {
        clear();
    }
    
    /**
     * Creates a further instance of this instances (intended as factory method
     * to be executed on a prototypical instance). 
     * 
     * @return the created instance
     * 
     * @since 1.00
     */
    public MonitoringGroupSettings create() {
        return new MonitoringGroupSettings();
    }
    
    /**
     * Cleans the attributes of this instance.
     * 
     * @since 1.00
     */
    public void clear() {
        id = null;
        debugStates = DebugState.DEFAULT;
        gType = GroupAccountingType.DEFAULT;
        resources = ResourceType.SET_DEFAULT;
        distributeValues = BooleanValue.DEFAULT;
        considerContained = BooleanValue.DEFAULT;
        instanceIdentifierKind = InstanceIdentifierKind.NONE;
    }
    
    /**
     * Sets the basic values needed for monitoring.
     * 
     * @param id the recorder ids
     * @param debugStates the intended debugging flags
     * @param gType the group accounting type
     * @param resources the accountable resources
     * @param instanceIdentifierKind the instance identifier kind
     * 
     * @since 1.20
     */
    public void setBasics(String[] id, DebugState[] debugStates, 
        GroupAccountingType gType, ResourceType[] resources, InstanceIdentifierKind instanceIdentifierKind) {
        this.id = id;
        this.debugStates = debugStates;
        this.gType = gType;
        this.resources = resources;
        this.instanceIdentifierKind = instanceIdentifierKind;
    }

    /**
     * Sets advanced values for multi monitoring groups.
     * 
     * @param distributeValues whether values should be distributed evenly
     * @param considerContained whether accountable resources of contained 
     *        groups should be considered or whether the group is authoritive
     * 
     * @since 1.00
     */
    public void setMulti(BooleanValue distributeValues, 
        BooleanValue considerContained) {
        this.distributeValues = distributeValues;
        this.considerContained = considerContained;
    }
    
    /**
     * Contains arbitrary ids for monitoring and grouping.
     * 
     * @return the ids
     * 
     * @since 1.00
     */
    public String[] getId() {
        return id;
    }
    
    /**
     * Returns a combination of debug states for additional information to
     * be emitted during monitoring.
     * 
     * @return the debugging states
     * 
     * @since 1.00
     */
    public DebugState[] getDebugStates() {
        return debugStates;
    }
    
    /**
     * Returns the group accounting to be applied. By default this value is set
     * to {@link GroupAccountingType#DEFAULT} and, therefore, taken from the 
     * global configuration.
     * 
     * @return the accounting type
     * 
     * @since 1.00
     */
    public GroupAccountingType getAccountingType() {
        return gType;
    }

    /**
     * Returns the resources to be accounted. By default, all resources defined
     * in {@link ResourceType} are accounted.
     * 
     * @return the accountable resources
     * 
     * @since 1.00
     */
    public ResourceType[] getResources() {
        return resources;
    }
    
    /**
     * Returns the instance identifier kind.
     * 
     * @return the instance identifier kind
     * 
     * @since 1.20
     */
    public InstanceIdentifierKind getInstanceIdentifierKind() {
        return instanceIdentifierKind;
    }

    /**
     * Returns whether values in multi groups should be distributed evenly to
     * the contained values or whether the entire value should be added to each
     * group.
     * 
     * @return <code>true</code> if values should be distributed, 
     *   <code>false</code> if not
     * 
     * @since 1.00
     */
    public BooleanValue getDistributeValues() {
        return distributeValues;
    }

    /**
     * Returns whether accountable resources of the multi monitoring group
     * is authoritative or weather the contained groups should be considered.
     * 
     * @return <code>true</code> if the contained groups values should be 
     *   considered, <code>false</code> if the multi group is authoritative
     * 
     * @since 1.00
     */
    public BooleanValue getConsiderContained() {
        return considerContained;
    }
    
    /**
     * Returns an instance from the shared pool. This explicit method is needed 
     * due to problems with javassist and Android.
     * 
     * @return  the instance from the pool. This instance has 
     *          to be released by {@link #release(MonitoringGroupSettings)}.
     *
     * @since   SugiBib 1.20
     */    
    public static final MonitoringGroupSettings getFromPool() {
        int size = POOL.size();
        MonitoringGroupSettings result;
        if (0 == size) {
            result = new MonitoringGroupSettings();
        } else {
            result = POOL.remove(size - 1);
        }
        return result;
    }
    
    /**
     * Releases and clears the specified instance
     * to the shared pool. This explicit method is needed due to problems
     * with javassist and Android.
     *
     * @param instance the instance to be released (must not be <b>null</b>)
     */  
    public static final void release(MonitoringGroupSettings instance) {
        instance.clear();
        POOL.add(instance);   
    }

}
