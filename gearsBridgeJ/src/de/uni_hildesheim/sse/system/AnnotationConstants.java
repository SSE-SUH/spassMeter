package de.uni_hildesheim.sse.system;

/**
 * Defines the annotation constants for the eraser-based product line 
 * configuration.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public class AnnotationConstants {

    /**
     * Should access to the size of an object (via JVMTI) be available?
     */
    public static final String VAR_OBJECT_SIZE = "VAR_OBJECT_SIZE";

    /**
     * Should memory unallocation information be provided via JVMTI?
     */
    public static final String WITH_JVMTI_MEMORY = "WITH_JVMTI_MEMORY";

    
    /**
     * Should the legacy methods for accessing a large portion of data
     * derived by {@link de.uni_hildesheim.sse.system.IDataGatherer} be 
     * available? Note that {@link de.uni_hildesheim.sse.system.IDataGatherer} 
     * itself depends also on {@link #VAR_WIFI_DATA}.
     */
    public static final String VAR_GATHER_DATA = "VAR_GATHER_DATA";
    
    /**
     * Should WiFi data be collected?
     */
    public static final String VAR_WIFI_DATA = "VAR_WIFI_DATA";
    
    /**
     * Should screen data be available?
     */
    public static final String VAR_SCREEN_DATA = "VAR_SCREEN_DATA";

    /**
     * Should memory data be available? Note, that this interfers with 
     * {@link #VAR_OBJECT_SIZE} so that switching off this variable may
     * not remove the memory data gatherer.
     */
    public static final String VAR_MEMORY_DATA = "VAR_MEMORY_DATA";
    
    /**
     * Should timing data be available?
     */
    public static final String VAR_TIME_DATA = "VAR_TIME_DATA";
    
    /**
     * Should load data be available?
     */
    public static final String VAR_LOAD_DATA = "VAR_LOAD_DATA";
    
    /**
     * Should processor data be available?
     */
    public static final String VAR_PROCESSOR_DATA = "VAR_PROCESSOR_DATA";
    
    /**
     * Should volume data be available?
     */
    public static final String VAR_VOLUME_DATA = "VAR_VOLUME_DATA";
    
    /**
     * Should network data be available?
     */
    public static final String VAR_NETWORK_DATA = "VAR_NETWORK_DATA";
    
    /**
     * Should energy data be available?
     */
    public static final String VAR_ENERGY_DATA = "VAR_ENERGY_DATA";
    
    /**
     * Should process I/O data be available? Note that this variability 
     * interfers with {@link #VAR_ARBITRARY_PROCESS_DATA} and 
     * {@link #VAR_CURRENT_PROCESS_DATA}.
     */
    public static final String VAR_IO_DATA = "VAR_IO_DATA";
    
    /**
     * Should data on the current (JVM) process be available?
     */
    public static final String VAR_CURRENT_PROCESS_DATA 
        = "VAR_CURRENT_PROCESS_DATA";

    /**
     * Should data on all running processes be available?
     */
    public static final String VAR_ALL_PROCESSES_DATA 
        = "VAR_ALL_PROCESSES_DATA";
    
    /**
     * Should data arbitrary processes to be accessed via a process id be 
     * available?
     */
    public static final String VAR_ARBITRARY_PROCESS_DATA 
        = "VAR_ARBITRARY_PROCESS_DATA";

    /**
     * Should debug information be available? This is not a real variability
     * but in the native implementation this name does not interfere with
     * existing flags.
     */
    public static final String VAR_DEBUG = "VAR_DEBUG";

    /**
     * Prevents this class from being initiated from outside.
     * 
     * @since 1.00
     */
    private AnnotationConstants() {
    }
    
}
