package de.uni_hildesheim.sse.monitoring.runtime.plugins;

/**
 * Provides reading access to the internal average system measurements used
 * for deriving information relative to system information.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public interface IMeasurements {

    /**
     * Defines a flag which signals that file usage statistics are available.
     * 
     * @since 1.00
     */
    public static final int STATUS_FILE = 1 << 0;

    /**
     * Defines a flag which signals that net usage statistics are available.
     * 
     * @since 1.00
     */
    public static final int STATUS_NET = 1 << 1;
    
    /**
     * Returns the status flags as combination of 
     * of {@link #STATUS_FILE}, 
     * {@link #STATUS_NET}. 
     * 
     * @return the current new status
     * 
     * @see de.uni_hildesheim.sse.monitoring.runtime.boot.Flags
     * 
     * @since 1.00
     */
    public int getStatus();
    
    /**
     * Returns the minimum load.
     * 
     * @return the minimum load (in percent)
     * 
     * @since 1.00
     */
    public double getMinLoad();

    /**
     * Returns the average load.
     * 
     * @return the average load (in percent)
     * 
     * @since 1.00
     */
    public double getAvgLoad();

    /**
     * Returns the maximum load.
     * 
     * @return the maximum load (in percent)
     * 
     * @since 1.00
     */
    public double getMaxLoad();

    /**
     * Returns the minimum memory usage.
     * 
     * @return the minimum memory usage (in bytes)
     * 
     * @since 1.00
     */
    public long getMinMemUse();

    /**
     * Returns the average memory usage.
     * 
     * @return the average memory usage (in bytes)
     * 
     * @since 1.00
     */
    public double getAvgMemUse();

    /**
     * Returns the maximum memory usage.
     * 
     * @return the maximum memory usage (in bytes)
     * 
     * @since 1.00
     */
    public long getMaxMemUse();

    /**
     * Returns the duration of the process in system time.
     * 
     * @return the duration in system time, is 0 for the entire system
     * 
     * @since 1.00
     */
    public long getSystemTime();

    /**
     * Returns the number of bytes read from external input.
     * 
     * @return the number of bytes read from external
     * 
     * @since 1.00
     */
    public long getIoRead();

    /**
     * Returns the number of bytes written to external input.
     * 
     * @return the number of bytes written to external
     * 
     * @since 1.00
     */
    public long getIoWrite();
    
}
