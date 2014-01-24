package de.uni_hildesheim.sse.monitoring.runtime.recordingStrategies;

import java.io.PrintStream;

/**
 * A formatter for emitting out monitoring results. First method of using this
 * interface should be {@link #configure} and 
 * {@link #setProcessData(ProcessData)}.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public interface ResultFormatter {

    /**
     * Defines the basic data for output.
     * 
     * @param out the target output stream
     * @param total the recorder element representing the entire program
     * @param showPercentages should percentages be printed or absolute values 
     *     instead
     * 
     * @since 1.00
     */
    public void configure(PrintStream out, RecorderElement total,  
        boolean showPercentages);
    
    /**
     * Returns if this instance was configured by calling 
     * {@link #configure(PrintStream, RecorderElement, boolean)}.
     * 
     * @return <code>true</code> if it was configured, <code>false</code> else
     * 
     * @since 1.00
     */
    public boolean isConfigured();
    
    /**
     * Sets the current process data object for comparisons.
     * 
     * @param data the process data object with system and JVM measurements
     * 
     * @since 1.00
     */
    public void setProcessData(ProcessData data);
    
    /**
     * Prints the information for an individual thread.
     * 
     * @param description an additional description to be printed
     * @param thread the thread information to be printed
     * 
     * @since 1.00
     */
    public void printThreadData(String description, ThreadData thread);
    
    /**
     * Prints out the min/avg/max statistics for the 
     * system and the virtual machine.
     */
    public void printProcessStatistics();

    /**
     * Prints out an individual recorder element.
     * 
     * @param description a description of the element to be printed out
     * @param individual the individual element to be emitted
     * 
     * @since 1.00
     */
    public void printIndividual(String description, RecorderElement individual);
    
    /**
     * Prints out the headline describing the data columns.
     */
    public void printHeadline();
    
    /**
     * Prints a comparison among the <code>individual</code> element and the 
     * total element defined in {@link #setProcessData}.
     * 
     * @param description a description on the elements being compared
     * @param individual the individual element to be compared with the 
     *     total element
     * @param system <code>true</code> if a comparison to system level 
     *     properties, <code>false</code> if a comparison to JVM level 
     *     properties should be done
     * 
     * @since 1.00
     */
    public void printCompare(String description, RecorderElement individual, 
        boolean system);
    
    /**
     * Prints some additional information categorizing the printout.
     * 
     * @param info the category to be printed
     * 
     * @since 1.00
     */
    public void printInfo(InfoCategory info);
    
    /**
     * Defines some information categories to be printed as additional 
     * information.
     * 
     * @author Holger Eichelberger
     * @since 1.00
     * @version 1.00
     */
    public enum InfoCategory {
        
        /**
         * Marks the program as explicitly threaded and headlines the threads
         * to be emitted.
         */
        THREADED,
        
        /**
         * Marks the breakdown section, i.e. where global system measurements 
         * are related to group / variability measurements. 
         */
        BREAKDOWN,
        
        /**
         * Marks the configurations section, i.e. where measurements for 
         * compositions of monitoring groups are emitted.
         */
        CONFIGURATIONS;
    }

    /**
     * Clears all basic data for reuse of this instance.
     */
    public void clear();
    
    /**
     * Prints the current (aggregated) state. This method needs an appropriate
     * call of {@link #setProcessData(ProcessData)} before.
     * 
     * @param elements all (current) elements
     * @param programRecord the program record
     * @param overheadRecord the overhead record
     * 
     * @since 1.00
     */
    public void printCurrentStateStatistics(RecorderElementMap elements, 
        RecorderElement programRecord, RecorderElement overheadRecord);

    /**
     * Prints a footer for the runtime statistics section. This method needs an 
     * appropriate call of {@link #setProcessData(ProcessData)} before.
     * 
     * @param elements all (current) elements
     * @param programRecord the program record
     * @param overheadRecord the overhead record
     * 
     * @since 1.00
     */
    public void printCurrentStateStatisticsFooter(RecorderElementMap elements, 
        RecorderElement programRecord, RecorderElement overheadRecord);

}
