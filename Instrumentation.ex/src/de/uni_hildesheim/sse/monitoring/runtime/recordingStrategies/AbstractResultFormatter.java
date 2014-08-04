package de.uni_hildesheim.sse.monitoring.runtime.recordingStrategies;

import java.io.PrintStream;

import de.uni_hildesheim.sse.monitoring.runtime.boot.Flags;
import de.uni_hildesheim.sse.monitoring.runtime.plugins.IMeasurements;
import de.uni_hildesheim.sse.monitoring.runtime.recordingStrategies.
    ProcessData.Measurements;
import de.uni_hildesheim.sse.monitoring.runtime.utils.Formatter;

/**
 * An abstract implementation of the resource formatter as a basis for 
 * implementations.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public abstract class AbstractResultFormatter implements ResultFormatter {

    /**
     * An internal buffer.
     */
    private StringBuilder builder = new StringBuilder();

    /**
     * Stores an instance of the formatter usually used to produce 
     * <code>printf</code> outputs. We need an own instance here because
     * the one in System.out may cause deadlocks. :(
     */
    private java.util.Formatter formatter = new java.util.Formatter(builder);
    
    /**
     * Stores the current output stream.
     */
    private PrintStream out;
    
    /**
     * Stores the recorder element to compare against (usually the program 
     * record).
     */
    private RecorderElement total;
    
    /**
     * Stores if percentages or absolute values should be emitted.
     */
    private boolean showPercentages;
    
    /**
     * Stores the value columns separator char.
     */
    private char separator;
    
    /**
     * Stores the JVM and process measurements.
     */
    private ProcessData data;

    /**
     * Returns if an id should be excluded from printing. [DEBUG ONLY]
     * 
     * @param id the id to be tested
     * @return if it should be excluded
     * 
     * @since 1.00
     */
    public static boolean excludeId(String id) {
        return id.startsWith("frequency@") 
            || id.startsWith("multilevel@")
            || id.startsWith("adaptivity@");
    }
    
    /**
     * Clears all basic data for reuse of this instance.
     */
    public void clear() {
        if (null != this.out) {
            out.flush();
            out.close();
        }
        this.out = null;
        this.total = null;
        this.showPercentages = false;
        this.separator = 0;
        this.data = null;
        this.formatter = null;
    }

    /**
     * Defines the basic data for output. Should be followed by a call to 
     * {@link #setProcessData(ProcessData)}.
     * 
     * @param out the target output stream
     * @param total the recorder element representing the entire program
     * @param showPercentages should percentages be printed or absolute values 
     *     instead
     * 
     * @since 1.00
     */
    public void configure(PrintStream out, RecorderElement total,  
        boolean showPercentages) {
        this.out = out;
        this.total = total;
        this.showPercentages = showPercentages;
/*        this.builder = new StringBuilder();
        this.formatter = new java.util.Formatter(builder);*/
    }
    
    /**
     * Returns if this instance was configured by calling 
     * {@link #configure(PrintStream, RecorderElement, boolean)}.
     * 
     * @return <code>true</code> if it was configured, <code>false</code> else
     * 
     * @since 1.00
     */
    public boolean isConfigured() {
        return null != out;
    }
    
    /**
     * Writes a formatted string to this object's destination using the
     * specified format string and arguments.  The locale used is the one
     * defined during the construction of this formatter.
     *
     * @param format A format string as described in the Java API documentation.
     *
     * @param args Arguments referenced by the format specifiers in the format
     *        string.  
     */
    protected void printf(String format, Object ... args)  {
        if (null != formatter) {
            formatter.format(format, args);
            out.print(builder.toString());
            builder.delete(0, builder.length());
        }
    }
        
    /**
     * Changes the value column separator.
     * 
     * @param separator the value column separator
     * 
     * @since 1.00
     */
    protected void setSeparator(char separator) {
        this.separator = separator;
    }
   
    /**
     * Returns the currently active output stream.
     * 
     * @return the currently active output stream
     * 
     * @since 1.00
     */
    protected PrintStream getOut() {
        return out;
    }

    /**
     * Sets the data on system and JVM process data for printout.
     * 
     * @param data the process data object
     * 
     * @since 1.00
     */
    public void setProcessData(ProcessData data) {
        this.data = data;
    }

    /**
     * Returns the JVM data taken from the process data
     * stored by {@link #setProcessData(ProcessData)}.
     * 
     * @return the JVM measurements object, do not keep this reference
     * 
     * @since 1.00
     */
    protected Measurements getJvmData() {
        return data.getJvm();
    }

    /**
     * Returns the system data taken from the process data
     * stored by {@link #setProcessData(ProcessData)}. Please
     * use {@link #getIoRead} instead of 
     * {@link Measurements#getIoRead()} and {@link #getIoWrite} 
     * instead of {@link Measurements#getIoWrite()} as the method in this class
     * considers the values from {@link #total} in case that no network
     * or file statistics are provided.
     *
     * @return the system measurements object, do not keep this reference
     * 
     * @since 1.00
     */
    protected Measurements getSystemData() {
        return data.getSystem();
    }

    /**
     * Returns the total (corrected) bytes read from external input.
     * This method considers {@link Measurements#getStatus()} and corrects
     * the values if needed based on {@link #total}. This is needed
     * because some concrete platforms may not support I/O process accounting.
     * 
     * @param measurements the measurements object
     * @return the (corrected) number of bytes read from external input
     * 
     * @since 1.00
     */
    protected long getIoRead(Measurements measurements) {
        long result = measurements.getIoRead();
        int status = measurements.getStatus();
        if (!Flags.isSet(status, IMeasurements.STATUS_FILE)) {
            result += total.getFileIn();
        }
        if (!Flags.isSet(status, IMeasurements.STATUS_NET)) {
            result += total.getNetIn();
        }
        return result;
    }

    /**
     * Returns the total (corrected) bytes written to external output.
     * This method considers {@link Measurements#getStatus()} and corrects
     * the values if needed based on {@link #total}. This is needed
     * because some concrete platforms may not support I/O process accounting.
     * 
     * @param measurements the measurements object
     * @return the (corrected) number of bytes written to external output
     * 
     * @since 1.00
     */
    protected long getIoWrite(Measurements measurements) {
        long result = measurements.getIoWrite();
        int status = measurements.getStatus();
        if (!Flags.isSet(status, IMeasurements.STATUS_FILE)) {
            result += total.getFileOut();
        }
        if (!Flags.isSet(status, IMeasurements.STATUS_NET)) {
            result += total.getNetOut();
        }
        return result;
    }

    /**
     * Returns the total element to compare against.
     * 
     * @return the total element
     * 
     * @since 1.00
     */
    protected RecorderElement getTotal() {
        return total;
    }

    /**
     * Prints the column separator.
     * 
     * @since 1.00
     */
    protected void printSeparator() {
        // exit of SUM
        if (null != out) {
            out.print(separator);
        }
    }

    /**
     * Prints a percentage or <code>fraction</code> dependent on 
     * {@link #showPercentages}.
     * 
     * @param fraction the fraction determining the percentage
     * @param total the value for 100%
     * 
     * @since 1.00
     */
    protected void printPercentage(double fraction, double total) {
        // exit of SUM
        if (null != out) {
            if (!showPercentages) {
                printf("%.2f", fraction);
            } else {
                out.print(Formatter.formatPercentage(fraction, total, null));
            }
            out.print(separator);
        }
    }

    /**
     * Prints a percentage or <code>fraction</code> dependent on 
     * {@link #showPercentages}.
     * 
     * @param fraction the fraction determining the percentage
     * @param total the value for 100%
     * 
     * @since 1.00
     */
    protected void printPercentage(long fraction, long total) {
        // exit of SUM
        if (null != out) {
            if (!showPercentages) {
                printf("%d", fraction);
            } else {
                out.print(Formatter.formatPercentage(fraction, total, null));
            }
            out.print(separator);
        }
    }

    /**
     * Prints a percentage or <code>fraction</code> dependent on 
     * {@link #showPercentages} with time prefix of interpreting
     * the values as nano seconds and deriving the time unit.
     * 
     * @param fraction the fraction determining the percentage
     * @param total the value for 100%
     * 
     * @since 1.00
     */
    protected void printPercentageNanoPrefix(long fraction, long total) {
        // exit of SUM
        if (null != out) {
            if (!showPercentages) {
                printf("%d", fraction);
            } else {
                out.print(Formatter.formatPercentage(fraction, total, 
                    Formatter.nanoToUsualTime(fraction)));
            }
            out.print(separator);
        }
    }

    /**
     * Emits a nano time value (formatted) and a column separator.
     * 
     * @param nano the nano value to be emitted
     * 
     * @since 1.00
     */
    protected void printNanoTime(long nano) {
        // exit of SUM
        if (null != out) {
            out.print(Formatter.nanoToUsualTime(nano));
            out.print(separator);
        }
    }

    /**
     * Emits a string and a column separator.
     * 
     * @param string the string to be emitted
     * 
     * @since 1.00
     */
    protected void print(String string) {
        // exit of SUM
        if (null != out) {
            out.print(string);
            out.print(separator);
        }
    }

    /**
     * Emits an int value and a column separator.
     * 
     * @param value the value to be emitted
     * 
     * @since 1.00
     */
    protected void print(int value) {
        // exit of SUM
        if (null != out) {
            out.print(value);
            out.print(separator);
        }
    }

    /**
     * Emits a long value and a column separator.
     * 
     * @param value the value to be emitted
     * 
     * @since 1.00
     */
    protected void print(long value) {
        // exit of SUM
        if (null != out) {
            out.print(value);
            out.print(separator);
        }
    }

    /**
     * Emits a double value (two decimal places) and a column separator.
     * 
     * @param value the value to be emitted
     * 
     * @since 1.00
     */
    protected void print(double value) {
        // exit of SUM
        if (null != out) {
            printf("%.2f", value);
            out.print(separator);
        }
    }

    /**
     * Emits a float value (two decimal places) and a column separator.
     * 
     * @param value the value to be emitted
     * 
     * @since 1.00
     */
    protected void print(float value) {
        // exit of SUM
        if (null != out) {
            printf("%.2f", value);
            out.print(separator);
        }
    }

    /**
     * Emits a percentage value (two decimal places) and a column separator.
     * 
     * @param value the value to be emitted
     * 
     * @since 1.00
     */
    protected void printPercentage(double value) {
        // exit of SUM
        if (null != out) {
            printf("%.2f%%", value);
            out.print(separator);
        }
    }
    
    /**
     * Emits a new empty line.
     * 
     * @since 1.00
     */
    protected void println() {
        // exit of SUM
        if (null != out) {
            out.println();
        }
    }

}
