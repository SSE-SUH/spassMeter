package de.uni_hildesheim.sse.monitoring.runtime.configuration;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.uni_hildesheim.sse.codeEraser.annotations.Variability;
import de.uni_hildesheim.sse.monitoring.runtime.AnnotationConstants;
import de.uni_hildesheim.sse.monitoring.runtime.annotations.Helper;
import de.uni_hildesheim.sse.monitoring.runtime.boot.ArrayList;
import de.uni_hildesheim.sse.monitoring.runtime.boot.GroupAccountingType;
import de.uni_hildesheim.sse.monitoring.runtime.boot.MainDefaultType;
import de.uni_hildesheim.sse.monitoring.runtime.boot.ResourceType;
import de.uni_hildesheim.sse.monitoring.runtime.configuration.xml.
    XMLConfiguration;
import de.uni_hildesheim.sse.monitoring.runtime.utils.HashMap;
import de.uni_hildesheim.sse.monitoring.runtime.utils.StreamUtilities;

/**
 * Stores and maintains the configuration of the measurement process. 
 * Configuration settings are described in more detail in the manual.
 * Configuration settings which may be changed during runtime are marked. Others
 * are intended to be modified only internally or not at all during runtime (no
 * setters).
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.11
 */
public class Configuration {

    /**
     * Defines the class name of the default instrumenter.
     */
    public static final String DEFAULT_INSTRUMENTER = 
        "de.uni_hildesheim.sse.monitoring."
        + "runtime.instrumentation.javassist.Factory";
    
    /**
     * Defines the logger.
     */
    public static final Logger LOG = Logger.getLogger("SPASS-meter");
    
    static {
        LOG.setLevel(Level.OFF);
    }

    /**
     * Stores the instance of this file, i.e. a singleton.
     */
    public static final Configuration INSTANCE;

    /**
     * Defines a key for storing the configuration arguments as JVM property.
     * This is required to transport an initialization across different class
     * loader contexts, e.g. in OSGi environments.
     */
    private static final String JVM_PROPERTY_CONFIGURATION_ARGS 
        = "spass-meter.config";
    
    /**
     * May carry the initial arguments for initializing an instance of this
     * class. Intended to be set during pre-instrumentation.
     * <b>Do not rename this attribute as it may be set by instrumentation</b>.
     */
    private static String cmdArgs = null; // do not move / change this attribute
        
    /**
     * The output file. May be <b>null</b> for console output. Default value is 
     * <b>null</b>.
     */
    private String outFileName = null;

    /**
     * Stores the interval (in 500ms units) used for incrementally printing 
     * aggregated events.<br/>
     * This value must be &gt; 0, otherwise this function will be disabled.
     * @since 1.00
     */
    private int outInterval = 0;
    
    /**
     * Print statistic output at the end.
     */
    private boolean printStatistics = true;
    
    /**
     * Stores the base directory.
     */
    private String baseDir = null;
    
    /**
     * Stores if the instrumenter itself should be instrumented (legacy).
     */
    private boolean instrumentInstrumenter = true;
    
    /**
     * Stores if overhead recording is enabled. Default value is 
     * <code>false</code>.
     */
    @Variability(id = "overhead")
    private boolean recordOverhead = false;

    /**
     * Stores if automated configuration detection is enabled.
     */
    private boolean configurationDetection = true;

    /**
     * Stores if variant contributions should be collected.
     */
    private boolean variantContributions = false;

    /**
     * Stores the class name of the instrumenter factory.
     */
    private String instrumenterFactory = System.getProperty(
        "spass-meter.iFactory", DEFAULT_INSTRUMENTER);
        
    /**
     * Stores unrecognized parameters to be passed to plugins.
     */
    private HashMap<String, String> unrecognizedParams 
        = new HashMap<String, String>();
    
    /**
     * Stores the hostname for tcp recording.
     */
    @Variability(id = AnnotationConstants.STRATEGY_TCP)
    private String tcpHostname = "";
    
    /**
     * Stores the port for tcp recording.
     */
    @Variability(id = AnnotationConstants.STRATEGY_TCP)
    private int tcpPort = -1;

    /**
     * If <code>true</code>, performs asynchronous event processing all the 
     * time, if <code>false</code> use local asynchronous calls as long as
     * no network-based recorder strategy is running.
     */
    private boolean localEventProcessing = true;
    
    /**
     * Stores the name of the bootpath jar file.
     * 
     * @since 1.00
     */
    private String bootJar = "";
    
    /**
     * Stores the debug log file (lazy initialization).
     */
    private PrintStream debugLog;
    
    /**
     * Stores the instrumenter configuration taken from XML.
     */
    @Variability(id = AnnotationConstants.CONFIG_XML)
    private XMLConfiguration instrumenterConfig;
    
    /**
     * Stores whether the java library should be instrumented.
     */
    private boolean instrumentJavaLib = true;

    /**
     * Stores whether the java library classes should be retransformed 
     * and instrumented.
     */
    private boolean retransformJavaLib = false;

    /**
     * Stores whether unused annotations should be removed from the resulting
     * code.
     */
    private boolean pruneAnnotations = false;

    /**
     * Stores the scope type, i.e. whether and how group annotations determine 
     * the instrumentation. The default value {@link ScopeType#SUM} due to 
     * legacy reasons.
     */
    private ScopeType scopeType = ScopeType.SUM;
    
    /**
     * Stores whether the current execution performs static instrumentation.
     * This attribute is internal to the framework and should not be accessible
     * to user settings. Needs to be modified by a static instrumentation 
     * facility.
     */
    private boolean staticInstrumentation = false;

    /**
     * Stores the memory accounting type.
     */
    private MemoryAccountingType memoryAccountingType 
        = MemoryAccountingType.CREATION_UNALLOCATION;
/*    
    {
        if (JBOSS_EXPERIMENT) {
            memoryAccountingType = MemoryAccountingType.CREATION;
        } else {
            memoryAccountingType = MemoryAccountingType.CREATION_UNALLOCATION;
        }
    }*/
    
    /**
     * Stores the group accounting type.
     */
    private GroupAccountingType groupAccountingType 
        = GroupAccountingType.DIRECT;

    /**
     * Stores the annotation search type.
     */
    private AnnotationSearchType annotationSearchType 
        = AnnotationSearchType.NONE;
    
    /**
     * Stores the accountable resources (currently all).
     */
    private ResourceType[] accountableResources = ResourceType.SET_ALL;
    
    /**
     * Stores the default resources for monitoring groups(currently all).
     */
    private ResourceType[] defaultGroupResources = ResourceType.SET_ALL;

    /**
     * Stores the SUM resources (currently all).
     */
    private ResourceType[] sumResources = ResourceType.SET_ALL;
    
    /**
     * Stores the resources to be collected anyway.
     */
    private ResourceType[] anywayResources = ResourceType.SET_ANYWAY;
    
    /**
     * Stores whether values in multi groups should be distributed evenly to
     * the contained values or whether the entire value should be added to each
     * group.
     * 
     * @since 1.00
     */
    private boolean multiDistributeValues = true;

    /**
     * Stores whether accountable resources of the multi monitoring group
     * is authoritative or weather the contained groups should be considered.
     * 
     * @since 1.00
     */
    private boolean multiConsiderContained = true;

    /**
     * Stores the default instrumentation behavior at the first main method.
     */
    private MainDefaultType mainDefault = MainDefaultType.START_END_SHUTDOWN;
    
    /**
     * Stores whether threads being ready for running should be explicitly 
     * registered with the native implementation (e.g. needed when the Java
     * implementation does not provide thread-based time monitoring through 
     * JMX).
     * 
     * @since 1.00
     */
    private boolean registerThreads = false;
    
    /**
     * Stores the attached configuration listener.
     */
    private ConfigurationListener listener = null;
    
    /**
     * Stores the names of instrumented classes (preliminary solution).
     */
    private Set<String> instrumented = new HashSet<String>();
    
    /**
     * Stores recorderId/pseudoId mappings for multiple recorder ids.
     */
    private HashMap<String, String> pseudoId = new HashMap<String, String>();

    /**
     * Stores whether excluded parts should be accounted in a global monitoring
     * group or whether they should be ignored and only be accounted for the 
     * entire program.
     * 
     * @since 1.00
     */
    private boolean accountExcluded = false;

    /**
     * Returns whether all class members shall be considered for instrumentation
     * by default or whether SPASS-meter shall apply configuration-based filters
     * such as "plain time" that may need explicit specification of the 
     * monitoring phase (as arbitrary classes are then not considered for 
     * analysis anymore).
     * 
     * @since 1.13
     */
    private boolean allClassMembers = true;
    
    /**
     * Returns whether specific classes shall be excluded.
     */
    private String excludeClasses = null;
    
    /**
     * Allow initialization of constants, particularly those injected by
     * instrumentation.
     */
    static {
        INSTANCE = new Configuration();
        ConfigurationEntry.registerEntry("configDetect", 
            "configurationDetection", ConfigurationEntry.Type.BOOLEAN);
        ConfigurationEntry.registerEntry("varContrib", 
            "variantContributions", ConfigurationEntry.Type.BOOLEAN);
        ConfigurationEntry.registerEntry("instrumentJavaLib", 
            "instrumentJavaLib", ConfigurationEntry.Type.BOOLEAN);
        ConfigurationEntry.registerEntry("retransformJavaLib", 
            "retransformJavaLib", ConfigurationEntry.Type.BOOLEAN);
        ConfigurationEntry.registerEntry("pruneAnnotations", 
            "pruneAnnotations", ConfigurationEntry.Type.BOOLEAN);
        ConfigurationEntry.registerEntry("memAccounting", 
            "memoryAccountingType", ConfigurationEntry.Type.ENUM, 
            MemoryAccountingType.class);
        ConfigurationEntry.registerEntry("groupAccounting", 
            "groupAccountingType", ConfigurationEntry.Type.ENUM, 
            GroupAccountingType.class);
        ConfigurationEntry.registerEntry("accountableResources", 
            "accountableResources", ConfigurationEntry.Type.ARRAY_ENUM, 
            ResourceType.class);        
        ConfigurationEntry.registerEntry("defaultGroupResources", 
            "defaultGroupResources", ConfigurationEntry.Type.ARRAY_ENUM, 
            ResourceType.class);        
        ConfigurationEntry.registerEntry("sumResources", 
            "sumResources", ConfigurationEntry.Type.ARRAY_ENUM, 
            ResourceType.class);        
        ConfigurationEntry.registerEntry("outInterval", 
            "outInterval", ConfigurationEntry.Type.INTEGER);
        ConfigurationEntry.registerEntry("printStatistics", "printStatistics", 
            ConfigurationEntry.Type.BOOLEAN);
        ConfigurationEntry.registerEntry("instrumentInstrumenter", 
            "instrumentInstrumenter", ConfigurationEntry.Type.BOOLEAN);
        ConfigurationEntry.registerEntry("annotationSearch", 
            "annotationSearchType", ConfigurationEntry.Type.ENUM,
            AnnotationSearchType.class);
        ConfigurationEntry.registerEntry("multiDistributeValues", 
            "multiDistributeValues", ConfigurationEntry.Type.BOOLEAN);
        ConfigurationEntry.registerEntry("multiConsiderContained", 
            "multiConsiderContained", ConfigurationEntry.Type.BOOLEAN);
        ConfigurationEntry.registerEntry("mainDefault", 
            "mainDefault", ConfigurationEntry.Type.ENUM, MainDefaultType.class);
        ConfigurationEntry.registerEntry("registerThreads", 
            "registerThreads", ConfigurationEntry.Type.BOOLEAN);
        ConfigurationEntry.registerEntry("accountExcluded", 
            "accountExcluded", ConfigurationEntry.Type.BOOLEAN);
        ConfigurationEntry.registerEntry("localEventProcessing", 
            ConfigurationEntry.Type.BOOLEAN);
        ConfigurationEntry.registerEntry("scopeType", "scopeType",
            ConfigurationEntry.Type.ENUM, ScopeType.class);
        ConfigurationEntry.registerEntry("exclude", 
            "excludeClasses", ConfigurationEntry.Type.STRING);
        ConfigurationEntry.registerEntry("allClassMembers", 
            ConfigurationEntry.Type.BOOLEAN);
        
        
        // SYSTEM_GATHER_INTERVAL_ARG, Integer
        // CLEANUP, Integer
        
        //ConfigurationEntry.registerEntry(name, type);

    }
    
    /**
     * Prevents this class from being instantiated from outside.
     * 
     * @since 1.00
     */
    private Configuration() {
        String args = cmdArgs;
        if (null == args) {
            // configuration transported across different contexts, e.g. in OSGi
            args = System.getProperty(JVM_PROPERTY_CONFIGURATION_ARGS);
        }
        if (null != args) {
            readFromAgentArguments(args);
        }
    }

    /**
     * Writes this instance to the given output stream.
     * 
     * @param out the output stream
     * @throws IOException in case of any I/O error or problem
     * 
     * @since 1.00
     */
    public void write(DataOutputStream out) throws IOException {
        // not local data: bootJar, tcpSettings, debugLog, staticInstrumentation
        // instrumented, pseudoId, instrumenterConfig
        StreamUtilities.writeString(out, baseDir);
        StreamUtilities.writeString(out, outFileName);
        out.writeBoolean(configurationDetection);
        out.writeBoolean(instrumentJavaLib);
        out.writeBoolean(retransformJavaLib);
        out.writeInt(outInterval);
        out.writeBoolean(printStatistics);
        out.writeBoolean(variantContributions);
        out.writeBoolean(instrumentInstrumenter);
        out.writeBoolean(pruneAnnotations);
        out.writeBoolean(multiDistributeValues);
        out.writeBoolean(multiConsiderContained);
        out.writeBoolean(registerThreads);
        out.writeBoolean(accountExcluded);
        out.writeBoolean(allClassMembers);
        out.writeUTF(null == excludeClasses ? "" : excludeClasses);
        out.writeInt(memoryAccountingType.ordinal());
        out.writeInt(groupAccountingType.ordinal());
        out.writeInt(annotationSearchType.ordinal());
        out.writeInt(mainDefault.ordinal());
        out.writeInt(accountableResources.length);
        for (int i = 0; i < accountableResources.length; i++) {
            out.writeInt(accountableResources[i].ordinal());
        }
        out.writeInt(defaultGroupResources.length);
        for (int i = 0; i < defaultGroupResources.length; i++) {
            out.writeInt(defaultGroupResources[i].ordinal());
        }
        out.writeInt(sumResources.length);
        for (int i = 0; i < sumResources.length; i++) {
            out.writeInt(sumResources[i].ordinal());
        }
        out.writeInt(unrecognizedParams.size());
        for (HashMap.Entry<String, String> entry : unrecognizedParams.entries()) {
            StreamUtilities.writeString(out, entry.getKey());
            StreamUtilities.writeString(out, entry.getValue());
        }
    }
    

    /**
     * Reads this instance from the given input stream.
     * 
     * @param in the input stream
     * @throws IOException in case of any I/O error or problem
     * 
     * @since 1.00
     */
    public void read(DataInputStream in) throws IOException {
        // not local data: bootJar, tcpSettings, debugLog, staticInstrumentation
        // instrumented, pseudoId, instrumenterConfig
        baseDir = StreamUtilities.readString(in);
        outFileName = StreamUtilities.readString(in);
        configurationDetection = in.readBoolean();
        instrumentJavaLib = in.readBoolean();
        retransformJavaLib = in.readBoolean();
        outInterval = in.readInt();
        printStatistics = in.readBoolean();
        variantContributions = in.readBoolean();
        instrumentInstrumenter = in.readBoolean();
        pruneAnnotations = in.readBoolean();
        multiDistributeValues = in.readBoolean();
        multiConsiderContained = in.readBoolean();
        registerThreads = in.readBoolean();
        accountExcluded = in.readBoolean();
        allClassMembers = in.readBoolean();
        excludeClasses = in.readUTF();
        if (null != excludeClasses && 0 == excludeClasses.length()) {
            excludeClasses = null;
        }
        memoryAccountingType = MemoryAccountingType.values()[in.readInt()];
        groupAccountingType = GroupAccountingType.values()[in.readInt()];
        annotationSearchType = AnnotationSearchType.values()[in.readInt()];
        mainDefault = MainDefaultType.values()[in.readInt()];
        int tmp = in.readInt();
        ResourceType[] allResources = ResourceType.values();
        accountableResources = new ResourceType[tmp];
        for (int i = 0; i < tmp; i++) {
            accountableResources[i] = allResources[in.readInt()];
        }
        tmp = in.readInt();
        defaultGroupResources = new ResourceType[tmp];
        for (int i = 0; i < tmp; i++) {
            defaultGroupResources[i] = allResources[in.readInt()];
        }
        tmp = in.readInt();
        sumResources = new ResourceType[tmp];
        for (int i = 0; i < tmp; i++) {
            sumResources[i] = allResources[in.readInt()];
        }
        tmp = in.readInt();
        unrecognizedParams.clear();
        for (int i = 0; i < tmp; i++) {
            unrecognizedParams.put(StreamUtilities.readString(in), 
                StreamUtilities.readString(in));
        }
    }
    
    /**
     * Allows to set once a configuration listener (internal use only).
     * 
     * @param listener the configuration listener
     * 
     * @since 1.00
     */
    public void attachListener(ConfigurationListener listener) {
        if (null == this.listener) {
            this.listener = listener;
        } else {
            System.out.println("Configuration listener yet set. Ignored.");
        }
    }
    
    /**
     * Returns if the instrumenter should be instrumented (legacy).
     * 
     * @return <code>true</code> if enabled, <code>false</code> else
     * 
     * @since 1.00
     */
    public boolean instrumentInstrumenter() {
        return instrumentInstrumenter;
    }
    
    /**
     * Returns the memory accounting type [startup only].
     * 
     * @return the memory accounting type
     * 
     * @since 1.00
     */
    public MemoryAccountingType getMemoryAccountingType() {
        return memoryAccountingType;
    }

    /**
     * Returns the group accounting type [startup only].
     * 
     * @return the group accounting type
     * 
     * @since 1.00
     */
    public GroupAccountingType getGroupAccountingType() {
        return groupAccountingType;
    }

    /**
     * Changes the logging level.
     * 
     * @param level the new logging level
     * 
     * @see Level
     * 
     * @since 1.00
     */
    public void setLogLevel(String level) {
        Level lev = Level.parse(level);
        if (null != lev) {
            LOG.setLevel(lev);
        }
    }

    /**
     * Changes the (optional) base directory (to be prepended to file location
     * specifications).
     * 
     * @param baseDir the base directory, may be <b>null</b> if unused
     * 
     * @since 1.00
     */
    public void setBaseDir(String baseDir) {
        this.baseDir = baseDir;
    }
    
    /**
     * Returns the base directory.
     * 
     * @return the base directory, may be <b>null</b> if unspecified
     * 
     * @since 1.00
     */
    public String getBaseDir() {
        return baseDir;
    }

    /**
     * Changes the name of the output file.
     * 
     * @param outFile the name of the output file
     * 
     * @since 1.00
     */
    public void setOutFileName(String outFile) {
        this.outFileName = outFile;
    }
    
    /**
     * Returns the name of the output file.
     * 
     * @return the name of the output file
     * 
     * @since 1.00
     */
    public String getOutFileName() {
        return outFileName;
    }

    /**
     * Changes the name of the bootpath jar file.
     * 
     * @param bootJar the name of the bootpath jar file
     * 
     * @since 1.00
     */
    public void setBootJar(String bootJar) {
        this.bootJar = bootJar;
    }
    
    /**
     * Changes the debug log file.
     * 
     * @param debugLog the new debug log file
     * 
     * @since 1.00
     */
    public void setDebugLog(String debugLog) {
        closeDebugLog();
        try {
            this.debugLog = new PrintStream(new FileOutputStream(debugLog));
        } catch (IOException e) {
        }
    }

    /**
     * Closes the debug log.
     * 
     * @since 1.00
     */
    private void closeDebugLog() {
        if (null != debugLog) {
            debugLog.close();
        }
    }

    /**
     * Closes the configuration (end of recording).
     * 
     * @since 1.00
     */
    public void close() {
        closeDebugLog();
    }

    /**
     * Parses the specified XML configuration.
     * 
     * @param xmlConfig the XML configuration file
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.CONFIG_XML)
    public void setXMLConfig(String xmlConfig) {
        try {
            instrumenterConfig = XMLConfiguration.read(xmlConfig);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    /**
     * Returns the XML configuration.
     * 
     * @return the XML configuration
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.CONFIG_XML)
    public XMLConfiguration getXMLConfig() {
        return instrumenterConfig;
    }
    
    /**
     * Returns the name of the bootpath jar file .
     * 
     * @return the name of the bootpath jar file
     * 
     * @since 1.00
     */
    public String getBootJar() {
        return bootJar;
    }

    
    /**
     * Returns the output file.
     * 
     * @return the output file (by considering {@link #baseDir}
     * 
     * @since 1.00
     */
    public File getOutFile() {
        File result = null;
        if (null != outFileName) {
            if (null != baseDir) {
                result = new File(baseDir, outFileName);
            } else {
                result = new File(outFileName);
            }
        }
        return result;
    }
    
    /**
     * Returns whether statistics shall be printed at the end of monitoring.
     * 
     * @return <code>true</code> if statistics shall be printed, 
     *   <code>false</code> else
     * 
     * @since 1.00
     */
    public boolean printStatistics() {
        return printStatistics;
    }
    
    /**
     * Returns the interval used for incrementally printing 
     * aggregated events. If less than 500 the value is interpreted as
     * 500ms units, if greater or equal 500 the value is interpreted as 
     * absolute time interval in milliseconds.
     * 
     * @return the output interval, disabled if negative
     * 
     * @since 1.00
     */
    public int getOutInterval() {
        return outInterval;
    }

    /**
     * Changes the interval used for incrementally printing 
     * aggregated events. If less than 500 the value is interpreted as
     * 500ms units, if greater or equal 500 the value is interpreted as 
     * absolute time interval in milliseconds. 
     * 
     * @param outInterval the output interval, disables event printing if 
     *     negative
     * 
     * @since 1.00
     */
    public void setOutInterval(int outInterval) {
        this.outInterval = outInterval;
        if (null != listener) {
            listener.notifyOutIntervalChanged(this.outInterval);
        }
    }
    
    /**
     * Returns the resources which are accountable at all.
     * 
     * @return the accountable resources (empty if all)
     */
    public ResourceType[] getAccountableResources() {
        return accountableResources;
    }

    /**
     * Returns the default resources for monitoring groups.
     * 
     * @return the accountable resources by default (empty if all), a 
     *   subset of {@link #getAccountableResources()}
     */
    public ResourceType[] getDefaultGroupResources() {
        return defaultGroupResources;
    }

    /**
     * Returns the resources to be accounted for the SUM (system under 
     * monitoring).
     * 
     * @return the accountable resources for the SUM (empty if all), a 
     *   subset of {@link #getAccountableResources()}
     */
    public ResourceType[] getSumResources() {
        return sumResources;
    }
    
    /**
     * Returns the resources to be accounted anyway.
     * 
     * @return the accountable resources, a 
     *   subset of {@link #getAccountableResources()} 
     *   and {@link ResourceType#SET_ANYWAY}.
     * 
     * @since 1.00
     */
    public ResourceType[] getAnywayResources() {
        return anywayResources;
    }
    
    /**
     * Returns whether the java library should be instrumented.
     * 
     * @return <code>true</code> if the java library should be instrumented, 
     *   <code>false</code> else
     * 
     * @since 1.00
     */
    public boolean instrumentJavaLib() {
        return instrumentJavaLib;
    }

    /**
     * Returns whether java library classes should be retransformed and 
     * instrumented at program start (additional overhead).
     * 
     * @return <code>true</code> if the java library should be retransformed, 
     *   <code>false</code> else
     * 
     * @since 1.00
     */
    public boolean retransformJavaLib() {
        return retransformJavaLib;
    }
    
    /**
     * Returns whether java library classes should be retransformed and 
     * instrumented at program start (additional overhead).
     * 
     * @return <code>true</code> if unused annotations should
     *   be removed from the resulting code, <code>false</code> else
     * 
     * @since 1.00
     */
    public boolean pruneAnnotations() {
        return pruneAnnotations;
    }
    
    /**
     * Returns whether the current execution performs a static instrumentation.
     * This setting is internal to the framework and should not be modified
     * by user settings.
     * 
     * @return <code>true</code> if currently a static instrumentation is 
     *   performed, <code>false</code> else
     * 
     * @since 1.00
     */
    public boolean isStaticInstrumentation() {
        return staticInstrumentation;
    }
    
    /**
     * Changes whether the current execution performs a static instrumentation.
     * This setting is internal to the framework and should not be modified
     * by user settings.
     * 
     * @param staticInstrumentation <code>true</code> if currently a static 
     *   instrumentation is performed, <code>false</code> else
     * 
     * @since 1.00
     */
    public void setStaticInstrumentation(boolean staticInstrumentation) {
        this.staticInstrumentation = staticInstrumentation;
    }
    
    /**
     * Returns if measurement overhead should be collected.
     * 
     * @return <code>true</code> if measurement overhead should be 
     *   collected, <code>false</code> else
     * 
     * @since 1.00
     */
    @Variability(id = "overhead")
    public boolean recordOverhead() {
        // TODO remove
        return recordOverhead;
    }
    
    /**
     * Returns whether automatic configuration detection should be performed.
     * 
     * @return <code>true</code> if configuration detection should be 
     *   performed, <code>false</code> else
     * 
     * @since 1.00
     */
    public boolean configurationDetection() {
        return configurationDetection;
    }

    /**
     * Returns whether individual contributions of variants to configurations 
     * should be measured.
     * 
     * @return <code>true</code> if configuration detection should be 
     *   performed, <code>false</code> else
     * 
     * @since 1.00
     */
    @Variability(id = "calibration")
    public boolean measureVariantContributions() {
        return variantContributions;
    }

    /**
     * Changes TPC settings.
     * 
     * @param hostname the hostname of the TCP recording server
     * @param port the port number of the TCP recording server
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.STRATEGY_TCP)
    public void setTCPData(String hostname, int port) {
        this.tcpHostname = hostname;
        this.tcpPort = port;
    }
    
    /**
     * Returns the hostname.
     * 
     * @return The hostname
     */
    public String getTCPHostname() {
        return tcpHostname;
    }
    
    /**
     * Returns the port.
     * 
     * @return The port
     */
    public int getTCPPort() {
        return tcpPort;
    }
   
    /**
     * Reads the configuration parameters from an agent command line.
     * 
     * @param args the agent command line
     * 
     * @since 1.00
     */
    public void readFromAgentArguments(String args) {
        if (null != args && args.length() > 0) {
            System.setProperty(JVM_PROPERTY_CONFIGURATION_ARGS, args);
            String[] arguments = args.split(",");
            for (String arg : arguments) {
                int pos = arg.indexOf("=");
                String key = "";
                String value = "";
                if (pos > 0) {
                    key = arg.substring(0, pos);
                    if (pos + 1 < arg.length()) {
                        value = arg.substring(pos + 1);
                    }
                }
                if ("logLevel".equals(key)) {
                    setLogLevel(value);
                } else if ("iFactory".equals(key)) {
                    instrumenterFactory = value;
                } else if ("bootjar".equals(key)) {
                    setBootJar(value);
                } else if ("debuglog".equals(key)) {
                    setDebugLog(value);
                } else if ("xmlconfig".equals(key)) {
                    setXMLConfig(value);
                } else if ("printStatistics".equals(key)) {
                    printStatistics = Boolean.valueOf(value);
                } else if ("out".equals(key)) {
                    setOutFileName(value);
                } else if ("tcp".equals(key)) {
                    String[] tcp = value.split(":");
                    boolean error = false;
                    try {
                        if (tcp.length == 2) {
                            setTCPData(tcp[0], Integer.parseInt(tcp[1]));
                        } else {
                            error = true;
                        }
                    } catch (NumberFormatException e) {
                        error = true;
                    }
                    if (error) {
                        System.out.println("wrong tcp args");
                    }
                } else {
                    ConfigurationEntry entry = ConfigurationEntry.getEntry(key);
                    if (null != entry) {
                        try {
                            entry.setValue(value);
                        } catch (IllegalArgumentException e) {
                            System.out.println(e.getMessage());
                        }
                    } else {
                        unrecognizedParams.put(key, value);
                    }
                }
            }
        }
        ensureDefaultResources();
        if (isStaticInstrumentation() 
            && ScopeType.GROUP_INHERIT == getScopeType()) { // TODO think about
            scopeType = ScopeType.GROUP;
        }
    }
    
    /**
     * Ensures that the {@link #defaultGroupResources} is a subset of 
     * {@link #accountableResources}. Adjust {@link #memoryAccountingType}
     * and {@link #isFileIoAccountable} as well as {@link #isNetIoAccountable}
     * accordingly.
     * 
     * @since 1.00
     */
    private void ensureDefaultResources() {
        if (accountableResources.length > 0) {
            defaultGroupResources = ResourceType.ensureSubset(
                accountableResources, defaultGroupResources);
            sumResources = ResourceType.ensureSubset(accountableResources, 
                sumResources);
            anywayResources = ResourceType.ensureSubset(
                ResourceType.SET_ANYWAY, accountableResources);
        }
        if (!ResourceType.contains(accountableResources, ResourceType.MEMORY)) {
            memoryAccountingType = MemoryAccountingType.NONE;
        }
    }
    
    /**
     * Returns the scope type, i.e. the influence of the monitoring group on 
     * the actual instrumentation.
     * 
     * @return the scope type
     * 
     * @since 1.00
     */
    public ScopeType getScopeType() {
        return scopeType;
    }
    
    /**
     * Returns an (unmodifiable) map of unrecognized params detected during
     * parsing the agent options.
     * 
     * @param key the configuration parameter name
     * @return the unrecognized parameter or <b>null</b>
     * 
     * @since 1.00
     */
    public String getUnrecognizedParam(String key) {
        if (null == key) {
            return null;
        }
        return unrecognizedParams.get(key);
    }
    
    /**
     * Provides the opportunity to log to a debug file (if specified
     * in configuration).
     * 
     * @param text the text to log
     * 
     * @since 1.00
     */
    public final void debugLog(String text) {
        if (null != debugLog) {
            debugLog.println(text); 
            debugLog.flush();
        } else {
            System.err.println(text);
        }
    }
    
    /**
     * Returns the monitoring group configuration assigned to 
     * <code>recId</code>.
     * 
     * @param recId the recording id to consider
     * @return the assigned configuration or <b>null</b> if none is known
     * 
     * @since 1.00
     */
    public MonitoringGroupConfiguration getMonitoringGroupConfiguration(
        String recId) {
        MonitoringGroupConfiguration result;
        if (null != instrumenterConfig) {
            result = instrumenterConfig.
                getMonitoringGroupConfiguration(recId);
        } else {
            result = null;
        }
        return result;
    }

    /**
     * Marks a class as instrumented.
     * 
     * @param name the name of the class
     * 
     * @since 1.00
     */
    public void instrumented(String name) {
        Configuration.LOG.info("Instrumented " + name);
        instrumented.add(name);
    }
    
    /**
     * Returns whether a class was instrumented.
     * 
     * @param name the name of the class
     * @return <code>true</code> if it was instrumented, <code>false</code> else
     * 
     * @since 1.00
     */
    public boolean isInstrumented(String name) {
        return instrumented.contains(name);
    }
    
    /**
     * Returns whether the memory used by the program should be inferred from
     * JVM information or by accounting this information individually (memory
     * intensive). [startup-time property]
     * 
     * @return <code>true</code> the information should be inferred from the 
     *   JVM, <code>false</code> individual accounting should be performed
     * 
     * @since 1.00
     */
    public boolean programUseFromJvm() {
        // TODO make configurable
        return true;
    }
    
    /**
     * Returns how annotations in super classes and interfacecs should be taken 
     * into account.
     * 
     * @return the mode how to consider annotations
     * 
     * @since 1.00
     */
    public AnnotationSearchType getAnnotationSearchType() {
        return annotationSearchType;
    }

    /**
     * Returns the first id if there is only one in the array or the (previously
     * generated) pseudo id.
     * 
     * @param id the ids to return the unique id for
     * @return the first id if there is only one, the unique otherwise
     * 
     * @since 1.00
     */
    public String getRecId(String[] id) {
        int count = 0;
        StringBuilder tmp = new StringBuilder();
        if (null != id) {
            for (int i = 0; i < id.length; i++) {
                if (null != id[i] && id[i].length() > 0) {
                    if (count > 0) {
                        tmp.append(",");
                    }
                    tmp.append(id[i].trim());
                    count++;
                }
            }
        }
        String recId = null;
        if (count > 1) {
            // create pseudo id
            recId = tmp.toString();
            if (!pseudoId.containsKey(recId)) {
                String pseudo = Helper.createPseudo(pseudoId.size());
                pseudoId.put(recId, pseudo);
                recId = pseudo;
            } else {
                recId = pseudoId.get(recId);
            }
        } else if (1 == count) {
            recId = tmp.toString();
        } else {
            recId = null;
        }
        return recId;
    }

    /**
     * Returns the ids mapped to the given pseudo id. This method is not 
     * efficiently implemented and should only be used for debugging or logging
     * purpose.
     * 
     * @param recId a pseudo recording id
     * @return the assigned value
     * 
     * @since 1.00
     */
    public String getPseudoMapping(String recId) {
        for (HashMap.Entry<String, String> ent : pseudoId.entries()) {
            if (ent.getValue().equals(recId)) {
                return ent.getKey();
            }
        }
        return null;
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
    public boolean multiGroupsDistributeValues() {
        return multiDistributeValues;
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
    public boolean multiGroupsConsiderContained() {
        return multiConsiderContained;
    }
    
    /**
     * Returns the default instrumentation behavior for the first main
     * method considered by the instrumenter.
     * 
     * @return the behavior specification
     * 
     * @since 1.00
     */
    public MainDefaultType getMainDefault() {
        return mainDefault;
    }
    
    /**
     * Returns whether threads being ready for running should be explicitly 
     * registered with the native implementation (e.g. needed when the Java
     * implementation does not provide thread-based time monitoring through 
     * JMX).
     * 
     * @return <code>true</code> if threads need to be registered explicitly,
     *   <code>false</code> else
     * 
     * @since 1.00
     */
    public boolean registerThreads() {
        return registerThreads;
    }
    
    /**
     * Returns whether excluded parts should be accounted in a global monitoring
     * group or whether they should be ignored and only be accounted for the 
     * entire program.
     * 
     * @return <code>true</code> if they should be accounted in an own 
     *   monitoring group, <code>false</code> if only for the entire program
     * 
     * @since 1.00
     */
    public boolean accountExcluded() {
        return accountExcluded;
    }
    
    /**
     * Returns class name prefixes to be excluded (given in / rather 
     * than . notation).
     * 
     * @return class name prefixes to be excluded parsed into individual
     *   entries, may be <b>null</b> if none shall be excluded
     * 
     * @since 1.11
     */
    public String[] getExcludeClasses() {
        String[] result = null;
        if (null != excludeClasses) {
            ArrayList<String> tmp = new ArrayList<String>();
            int lastPos = 0;
            int length = excludeClasses.length();
            for (int i = 0; i < length; i++) {
                if (',' == excludeClasses.charAt(i)) {
                    tmp.add(excludeClasses.substring(lastPos, i).trim());
                    lastPos = i + 1;
                }
            }
            if (lastPos < length - 1) {
                tmp.add(excludeClasses.substring(lastPos, length).trim());
            }
            if (tmp.size() > 0) {
                result = new String[tmp.size()];
                tmp.toArray(result);
            }
        }
        return result;
    }
    
    /**
     * Returns the instrumenter factory class name.
     * 
     * @return the instrumenter factory class name
     * 
     * @since 1.00
     */
    public String getInstrumenterFactory() {
        return instrumenterFactory;
    }
    
    /**
     * Returns whether local event processing shall be done or whether
     * local synchronous calls shall be used instead.
     * 
     * @return <code>true</code> in case of local asynchronous event processing,
     *   <code>false</code> else
     * 
     * @since 1.00
     */
    public RecordingType getRecordingType() {
        RecordingType type;
        if (null != getTCPHostname() && getTCPPort() > 0) {
            type = RecordingType.TCP;
        } else {
            if (localEventProcessing) {
                type = RecordingType.LOCAL_ASYNCHRONOUS;
            } else {
                type = RecordingType.LOCAL_SYNCHRONOUS;
            }
        }
        return type;
    }
    
    /**
     * Returns whether all class members should be analyzed for instrumentation
     * by default or whether SPASS-meter shall apply filter strategies such as
     * "plain time".
     * 
     * @return <code>true</code> if all class members shall be analyzed by 
     *   default, <code>false</code> if filters shall be applied 
     * 
     * @since 1.13
     */
    public boolean allClassMembers() {
        return allClassMembers;
    }

}
