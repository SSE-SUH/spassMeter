package de.uni_hildesheim.sse.monitoring.runtime.recordingStrategies;

import java.io.PrintStream;
import java.util.Iterator;

import de.uni_hildesheim.sse.monitoring.runtime.recordingStrategies.
    ProcessData.Measurements;

/**
 * A formatter for tab-separated elements.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public class TabFormatter extends AbstractResultFormatter {

    /**
     * Creates a new tab formatter.
     * 
     * @since 1.00
     */
    public TabFormatter() {
        setSeparator('\t');
    }
    
    /**
     * Prints the information for an individual thread.
     * 
     * @param description an additional description to be printed
     * @param thread the thread information to be printed
     * 
     * @since 1.00
     */
    @Override
    public void printThreadData(String description, ThreadData thread) {
        print(description);
        printSeparator(); // mem use
        printSeparator(); // mem alloc
        printSeparator();
        printSeparator(); // agg sys time
        print(thread.getCpuTimeTicks());
        printSeparator(); // total read
        printSeparator(); // net in
        printSeparator(); // net out
        printSeparator(); // total write
        printSeparator(); // file in
        printSeparator(); // file out
        printSeparator(); // jvm load
        printSeparator(); // sys load
        println();
    }
    
    /**
     * Prints the process statistics for the given measurements instance.
     * 
     * @param description a description for the output (to be augmented by 
     *     "min", "max" and "avg")
     * @param measure the instance to be printed
     * 
     * @since 1.00
     */
    private void printProcessStatistics(String description, 
        Measurements measure) {
        print(description + " min");
        print(measure.getMinMemUse()); // mem use
        printSeparator(); // mem alloc
        printSeparator(); // sys time
        printSeparator(); // agg sys time
        printSeparator(); // cpu time
        printSeparator(); // io read
        printSeparator(); // net in
        printSeparator(); // file in
        printSeparator(); // io write
        printSeparator(); // net out
        printSeparator(); // file out
        printSeparator();
        printPercentage(measure.getMinLoad());
        println();

        print(description + " avg");
        print(measure.getAvgMemUse()); // mem use
        printSeparator(); // mem alloc
        printSeparator(); // sys time
        printSeparator(); // agg sys time
        printSeparator(); // cpu time
        print(getIoRead(measure)); // io read
        printSeparator(); // net in
        printSeparator(); // file in
        print(getIoWrite(measure)); // io write
        printSeparator(); // net out
        printSeparator(); // file out
        printSeparator();
        printPercentage(measure.getAvgLoad());
        println();
        
        print(description + " max");
        print(measure.getMaxMemUse()); // mem use
        printSeparator(); // mem alloc
        printSeparator(); // sys time
        printSeparator(); // agg sys time
        printSeparator(); // cpu time
        printSeparator(); // io read
        printSeparator(); // net in
        printSeparator(); // file in
        printSeparator(); // io write
        printSeparator(); // net out
        printSeparator(); // file out
        printSeparator();
        printPercentage(measure.getMaxLoad());
        println();
    }
    
    /**
     * Prints out the min/avg/max statistics for the 
     * system and the virtual machine.
     */
    public void printProcessStatistics() {
        printProcessStatistics("System", getSystemData());
        printProcessStatistics("JVM", getJvmData());
    }
    
    /**
     * Prints out an individual recorder element.
     * 
     * @param description a description of the element to be printed out
     * @param individual the individual element to be emitted
     * 
     * @since 1.00
     */
    @Override
    public void printIndividual(String description, 
        RecorderElement individual) {
        RecorderElement total = getTotal();
        double jvmLoad = getJvmData().getAvgLoad();
        
        if (null != description) {
            print(description);
        }
        if (null == total) {
            print(individual.getMemUse());
            print(individual.getMemAllocated());
            print(individual.getSystemTimeTicks());
            print(0); // legacy
            print(individual.getCpuTimeTicks());
            print(individual.getIoRead());
            print(individual.getNetIn());
            print(individual.getFileIn());
            print(individual.getIoWrite());
            print(individual.getNetOut());
            print(individual.getFileOut());
            printSeparator(); // load
            printSeparator(); // load
        } else {
            if (null != individual) {
                printPercentage(individual.getMemUse(), total.getMemUse());
                printPercentage(individual.getMemAllocated(), 
                    total.getMemAllocated());
                print(individual.getSystemTimeTicks());
                print(0); // legacyPercentageNanoPrefix(
                printPercentageNanoPrefix(individual.getCpuTimeTicks(), 
                    total.getCpuTimeTicks());
                printPercentage(individual.getIoRead(), total.getIoRead());
                printPercentage(individual.getNetIn(), total.getNetIn());
                printPercentage(individual.getFileIn(), total.getFileIn());
                printPercentage(individual.getIoWrite(), total.getIoWrite());
                printPercentage(individual.getNetOut(), total.getNetOut());
                printPercentage(individual.getFileOut(), total.getFileOut());
                double timeFraction;
                if (0 == total.getCpuTimeTicks()) {
                    timeFraction = 0; 
                } else {
                    timeFraction = individual.getCpuTimeTicks()
                        / ((double) total.getCpuTimeTicks());
                }
                printPercentage(100 * timeFraction);
                printPercentage(jvmLoad * timeFraction);
            }
        }
        println();
    }

    /**
     * Prints out the headline describing the data columns.
     */
    @Override
    public void printHeadline() {
        print("description");
        print("mem use");
        print("mem alloc");
        print("sys time");
        print("agg sys time");
        print("cpu time");
        print("in");
        print("netin");
        print("filein");
        print("out");
        print("netout");
        print("filout");
        print("jvm load");
        print("sys load");
        println();
    }
    
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
    @Override
    public void printCompare(String description, RecorderElement individual, 
        boolean system) {
        RecorderElement total = getTotal();
        Measurements jvm = getJvmData();
        Measurements sys = getSystemData();

        double jvmLoad = jvm.getAvgLoad();
        double sysLoad = sys.getAvgLoad();
        
        print(description);
        if (null == total || null == individual) {
            //data.print("JVM");
            printPercentage(jvm.getAvgMemUse(), sys.getAvgMemUse());
            printSeparator(); // mem alloc
            printSeparator(); // sys time
            printSeparator(); // agg sys time
            printSeparator(); // cpu time
            printPercentage(getIoRead(jvm), getIoRead(sys));
            printSeparator(); // net in
            printSeparator(); // file in
            printPercentage(getIoWrite(jvm), getIoWrite(sys));
            printSeparator(); // net out
            printSeparator(); // file out
            printPercentage(jvmLoad);
            printPercentage(sysLoad);
        } else {
            double memUse;
            long ioRead;
            long ioWrite;
            if (system) {
                memUse = sys.getAvgMemUse(); // was max before
                ioRead = getIoRead(sys);
                ioWrite = getIoWrite(sys);
            } else {
                memUse = jvm.getAvgMemUse(); // was max before
                ioRead = getIoRead(jvm);
                ioWrite = getIoWrite(jvm);
            }
            
            //out.print("\n - vs. sys: mem ");
            printPercentage(individual.getMemUse(), memUse);
            print(individual.getMemAllocated());
            print(individual.getSystemTimeTicks());
            print(0); // legacy
            print(individual.getCpuTimeTicks());
            printPercentage(individual.getIoRead(), ioRead);
            print(individual.getNetIn());
            print(individual.getFileIn());
            printPercentage(individual.getIoWrite(), ioWrite);
            print(individual.getNetOut());
            print(individual.getFileOut());
            double timeFraction;
            if (0 == total.getCpuTimeTicks()) {
                timeFraction = 0;
            } else {
                timeFraction = individual.getCpuTimeTicks() 
                    / ((double) total.getCpuTimeTicks());
            }
            if (system) {
                printSeparator();
                printPercentage(jvmLoad * timeFraction);
            } else {
                printPercentage(timeFraction * 100);
                printSeparator();
            }
        }
        println();
    }
    
    /**
     * Prints some additional information categorizing the printout.
     * 
     * @param info the category to be printed
     * 
     * @since 1.00
     */
    public void printInfo(InfoCategory info) {
        PrintStream out = getOut();
        if (null != out) {
            switch (info) {
            case THREADED:
                out.println(" (threaded)");
                break;
            case BREAKDOWN:
                out.println("\n\nBREAKDOWN");
                break;
            case CONFIGURATIONS:
                out.println("\n\nCONFIGURATIONS");
                break;
            default:
                // no output at all
                break;
            }
        }
    }
    
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
    @Override
    public void printCurrentStateStatistics(RecorderElementMap elements, 
        RecorderElement programRecord, RecorderElement overheadRecord) {
        print(System.currentTimeMillis());
        printSeparator();
        printMeasurementsStatistics(getSystemData());
        printSeparator();
        printMeasurementsStatistics(getJvmData());
        printSeparator();
        printRecorderElementStatistics(programRecord);
        printSeparator();
        printRecorderElementStatistics(overheadRecord);
        printSeparator();
        Iterator<String> iter = elements.recorderIds();
        while (iter.hasNext()) {
            String id = iter.next();
            if (!excludeId(id)) {
                printRecorderElementStatistics(
                    elements.getAggregatedRecord(id));
                printSeparator();
            }
        }
        println();
    }

    /**
     * Stores runtime statistical data for <code>measurements</code>.
     * 
     * @param measurements the measurements object to be printed
     * 
     * @since 1.00
     */
    private void printMeasurementsStatistics(Measurements measurements) {
        print(measurements.getSystemTime());
        print(measurements.getAvgMemUse());
        print(getIoRead(measurements));
        print(getIoWrite(measurements));
        print(measurements.getAvgLoad());
    }

    /**
     * Prints the footer description line for a {@link Measurements} object.
     * 
     * @param description short description on the related measurement objects
     * 
     * @since 1.00
     */
    private void printMeasurementsStatisticsFooter(String description) {
        print(description);
        print("sys time");
        print("mem use");
        print("io read");
        print("io write");
        print("avg load");
    }

    /**
     * Stores runtime statistical data for <code>elt</code>.
     * 
     * @param elt the recorder element object to be printed
     * 
     * @since 1.00
     */
    private void printRecorderElementStatistics(RecorderElement elt) {
        if (null != elt) {
            print(0); // legacy
            print(elt.getMemAllocated());
            print(elt.getMemUse());
            print(elt.getIoRead());
            print(elt.getNetIn());
            print(elt.getFileIn());
            print(elt.getIoWrite());
            print(elt.getNetOut());
            print(elt.getFileOut());
        } else {
            for (int i = 0; i < 8; i++) {
                printSeparator();   
            }
        }
    }

    /**
     * Prints the footer description line for a {@link RecorderElement} object.
     * 
     * @param description short description on the related recorder element 
     *   objects
     * 
     * @since 1.00
     */
    private void printRecorderElementStatisticsFooter(String description) {
        print(description);
        print("sys time");
        print("mem alloc");
        print("mem use");
        print("io read");
        print("net read");
        print("file read");
        print("io write");
        print("net write");
        print("file write");
    }

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
    @Override
    public void printCurrentStateStatisticsFooter(RecorderElementMap elements, 
        RecorderElement programRecord, RecorderElement overheadRecord) {
        
        print("current ms");
        printMeasurementsStatisticsFooter("sys");
        printMeasurementsStatisticsFooter("jvm");
        printRecorderElementStatisticsFooter("program");
        printRecorderElementStatisticsFooter("overhead");
        Iterator<String> iter = elements.recorderIds();
        while (iter.hasNext()) {
            String id = iter.next();
            if (!excludeId(id)) {
                printRecorderElementStatisticsFooter(id);
            }
        }
        println();
        println();
    }

}
