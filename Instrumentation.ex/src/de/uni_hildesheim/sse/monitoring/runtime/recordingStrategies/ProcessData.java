package de.uni_hildesheim.sse.monitoring.runtime.recordingStrategies;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import de.uni_hildesheim.sse.monitoring.runtime.boot.ArrayList;
import de.uni_hildesheim.sse.monitoring.runtime.boot.Flags;
import de.uni_hildesheim.sse.monitoring.runtime.plugins.IMeasurements;
import de.uni_hildesheim.sse.system.GathererFactory;
import de.uni_hildesheim.sse.system.IThisProcessDataGatherer;
import de.uni_hildesheim.sse.system.IoStatistics;

/**
 * Stores and gathers data on the currently running (JVM) process and the 
 * underlying operating system. Several data collected in this class needs
 * native system support to be obtained.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public class ProcessData {

    /**
     * Stores a set of measurements, i.e. one for the JVM process and one for 
     * the system.
     * 
     * @author eichelberger
     * @since 1.00
     * @version 1.00
     */
    public static class Measurements implements IMeasurements {
        
        /**
         * Stores the minimum load collected during monitoring.
         */
        private double minLoad;

        /**
         * Stores the average load collected during monitoring.
         */
        private double avgLoad;

        /**
         * Stores the actual load.
         */
        private double load;
        
        /**
         * Stores the maximum load collected during monitoring.
         */
        private double maxLoad;

        /**
         * Stores the minimum memory use collected during monitoring.
         */
        private long minMemUse;

        /**
         * Stores the average memory use collected during monitoring.
         */
        private double avgMemUse;
        
        /**
         * Stores the actual memory use.
         */
        private double memUse;

        /**
         * Stores the maximum memory use collected during monitoring.
         */
        private long maxMemUse;

        /**
         * Stores the system time consumed by the process.
         */
        private long systemTime;

        /**
         * Stores the amount of bytes read from I/O.
         */
        private long ioRead;

        /**
         * Stores the amount of bytes written to I/O.
         */
        private long ioWrite;
        
        /**
         * Stores the data acquisition status for this process. Combination
         * of {@link ProcessData#STATUS_FILE}, 
         * {@link ProcessData#STATUS_NET}.
         */
        private int status;

        /**
         * Creates a new instance and initializes the attributes.
         * 
         * @since 1.00
         */
        Measurements() {
            clear();
        }
        
        /**
         * Clears this instance.
         * 
         * @since 1.00
         */
        void clear() {
            minLoad = Double.MAX_VALUE;
            avgLoad = 0;
            load = 0;
            maxLoad = Double.MIN_VALUE;
            minMemUse = Long.MAX_VALUE;
            memUse = 0;
            avgMemUse = 0;
            maxMemUse = Long.MIN_VALUE;
            systemTime = 0;
            ioRead = 0;
            ioWrite = 0;
            status = 0;
        }
        
        /**
         * Copies all values from the given <code>measurements</code> instance.
         * 
         * @param measurements the instance to copy from
         * 
         * @since 1.00
         */
        public void copyFrom(Measurements measurements) {
            this.minLoad = measurements.minLoad;
            this.avgLoad = measurements.avgLoad;
            this.load = measurements.load;
            this.maxLoad = measurements.maxLoad;
            this.minMemUse = measurements.minMemUse;
            this.avgMemUse = measurements.avgMemUse;
            this.memUse = measurements.memUse;
            this.maxMemUse = measurements.maxMemUse;
            this.systemTime = measurements.systemTime;
            this.ioRead = measurements.ioRead;
            this.ioWrite = measurements.ioWrite;
            this.status = measurements.status;
        }

        /**
         * Changes minimum and maximum load values at once (calculates
         * minimum and maximum compared with stored values).
         * 
         * @param load the new load value
         */
        public void setMinMaxLoad(double load) {
            this.minLoad = Math.min(this.minLoad, load);
            this.maxLoad = Math.max(this.maxLoad, load);
            this.load = load;
        }

        /**
         * Changes the average load value.
         * 
         * @param avgLoad the average load value
         */
        public void setAvgLoad(double avgLoad) {
            this.avgLoad = avgLoad;
        }
        
        /**
         * Changes minimum and maximum memory usage values at once (calculates
         * minimum and maximum compared with stored values).
         * 
         * @param use the current number of bytes allocated
         */
        public void setMinMaxMemUse(long use) {
            this.minMemUse = Math.min(this.minMemUse, use);
            this.maxMemUse = Math.max(this.maxMemUse, use);
            this.memUse = use;
        }
        
        /**
         * Changes the status flags as combination of 
         * of {@link ProcessData#STATUS_FILE}, 
         * {@link ProcessData#STATUS_NET}.
         * 
         * @param status the new status
         * 
         * @see de.uni_hildesheim.sse.monitoring.runtime.boot.Flags
         * 
         * @since 1.00
         */
        public void setStatus(int status) {
            this.status = status;
        }

        /**
         * Returns the status flags as combination of 
         * of {@link ProcessData#STATUS_FILE}, 
         * {@link ProcessData#STATUS_NET}. 
         * 
         * @return the current new status
         * 
         * @see de.uni_hildesheim.sse.monitoring.runtime.boot.Flags
         * 
         * @since 1.00
         */
        @Override
        public int getStatus() {
            return status;
        }

        /**
         * Changes the average memory usage value.
         * 
         * @param avgMemUse the average number of bytes allocated
         */
        public void setAvgMemUse(double avgMemUse) {
            this.avgMemUse = avgMemUse;
        }
        
        /**
         * Returns the minimum load.
         * 
         * @return the minimum load (in percent)
         * 
         * @since 1.00
         */
        @Override
        public double getMinLoad() {
            return minLoad;
        }

        /**
         * Returns the average load.
         * 
         * @return the average load (in percent)
         * 
         * @since 1.00
         */
        @Override
        public double getAvgLoad() {
            return avgLoad;
        }

        /**
         * Returns the maximum load.
         * 
         * @return the maximum load (in percent)
         * 
         * @since 1.00
         */
        @Override
        public double getMaxLoad() {
            return maxLoad;
        }

        /**
         * Returns the minimum memory usage.
         * 
         * @return the minimum memory usage (in bytes)
         * 
         * @since 1.00
         */
        @Override
        public long getMinMemUse() {
            return minMemUse;
        }

        /**
         * Returns the average memory usage.
         * 
         * @return the average memory usage (in bytes)
         * 
         * @since 1.00
         */
        @Override
        public double getAvgMemUse() {
            return avgMemUse;
        }

        /**
         * Returns the maximum memory usage.
         * 
         * @return the maximum memory usage (in bytes)
         * 
         * @since 1.00
         */
        @Override
        public long getMaxMemUse() {
            return maxMemUse;
        }

        /**
         * Returns the duration of the process in system time.
         * 
         * @return the duration in system time, is 0 for the entire system
         * 
         * @since 1.00
         */
        @Override
        public long getSystemTime() {
            // TODO fill with uptime
            return systemTime;
        }

        /**
         * Returns the number of bytes read from external input.
         * 
         * @return the number of bytes read from external
         * 
         * @since 1.00
         */
        @Override
        public long getIoRead() {
            return ioRead;
        }

        /**
         * Returns the number of bytes written to external input.
         * 
         * @return the number of bytes written to external
         * 
         * @since 1.00
         */
        @Override
        public long getIoWrite() {
            return ioWrite;
        }
        
        /**
         * Sends the stored data to the output stream.
         * 
         * @param out the stream where to write the data to
         * @throws IOException in case of any I/O problem
         * 
         * @since 1.00
         */
        public void send(DataOutputStream out) throws IOException {
            out.writeDouble(avgLoad);
            out.writeDouble(load);
            out.writeDouble(minLoad);
            out.writeDouble(maxLoad);
            
            out.writeDouble(avgMemUse);
            out.writeDouble(memUse);
            out.writeLong(minMemUse);
            out.writeLong(maxMemUse);
       
            out.writeLong(systemTime);
            out.writeLong(ioRead);
            out.writeLong(ioWrite);
            
            out.writeInt(status);
        }

        /**
         * Reads data of this instance from the given input stream.
         * 
         * @param in the input stream from where to read data from
         * @throws IOException in case of any I/O problem
         * 
         * @since 1.00
         */
        public void read(DataInputStream in) throws IOException {
            avgLoad = in.readDouble();
            load = in.readDouble();
            minLoad = in.readDouble();
            maxLoad = in.readDouble();

            avgMemUse = in.readDouble();
            memUse = in.readDouble();
            minMemUse = in.readLong();
            maxMemUse = in.readLong();

            systemTime = in.readLong();
            ioRead = in.readLong();
            ioWrite = in.readLong();
            
            status = in.readInt();
        }

        @Override
        public double getLoad() {
            return load;
        }

        @Override
        public double getActMemUse() {
            return memUse;
        }
        
    }

    /**
     * Stores the pooled instances.
     */
    private static final ArrayList<ProcessData> POOL 
        = new ArrayList<ProcessData>();

    /**
     * Stores the system measurement data.
     * 
     * @since 1.00
     */
    private Measurements system;

    /**
     * Stores the JVM measurement data.
     * 
     * @since 1.00
     */
    private Measurements jvm;
    
    /**
     * Creates a new process data object. Use {@link #getFromPool()}.
     * 
     * @since 1.00
     */
    private ProcessData() {
        this.system = new Measurements();
        this.jvm = new Measurements();
    }
    
    /**
     * Returns an usable instance from the pool.
     * 
     * @return an instance
     * 
     * @since 1.00
     */
    public static synchronized ProcessData getFromPool() {
        ProcessData result;
        if (POOL.size() > 0) {
            result = POOL.remove(POOL.size() - 1);
        } else {
            result = new ProcessData();
        }
        return result;
    }
    
    /**
     * Releases the given <code>instance</code>. Note that when calling
     * this method no references to {@link #getJvm()} and {@link #getSystem()}
     * should be kept outside.
     * 
     * @param instance the instance to be released
     * 
     * @since 1.00
     */
    public static synchronized void release(ProcessData instance) {
        instance.clear();
        POOL.add(instance);
    }
        
    /**
     * Clears this instance.
     * 
     * @since 1.00
     */
    void clear() {
        system.clear();
        jvm.clear();
    }
    
    /**
     * Returns the system measurement object.
     * 
     * @return the system measurement object
     * 
     * @since 1.00
     */
    public Measurements getSystem() {
        return system;
    }

    /**
     * Returns the JVM measurement object.
     * 
     * @return the JVM measurement object
     * 
     * @since 1.00
     */
    public Measurements getJvm() {
        return jvm;
    }
    
    /**
     * Reads data not obtained from monitoring directly from the underlying 
     * system using native methods.
     * 
     * @since 1.00
     */
    public void readRemainingFromSystem() {
        IThisProcessDataGatherer tpdg = 
            GathererFactory.getThisProcessDataGatherer();
        jvm.systemTime = tpdg.getCurrentProcessSystemTimeTicks();
        IoStatistics iostat = tpdg.getCurrentProcessIo();
        if (null != iostat) {
            jvm.ioRead = iostat.read;
            jvm.ioWrite = iostat.write;
        }
        int status = 0;
        status = Flags.change(status, IMeasurements.STATUS_FILE, 
            tpdg.isFileIoDataIncluded(false));
        jvm.status = Flags.change(status, IMeasurements.STATUS_NET, 
            tpdg.isNetworkIoDataIncluded(false));
        iostat = tpdg.getAllProcessesIo();
        if (null != iostat) {
            system.ioRead = iostat.read;
            system.ioWrite = iostat.write;
        }
        status = 0;
        status = Flags.change(status, 
            IMeasurements.STATUS_FILE, tpdg.isFileIoDataIncluded(true));
        system.status = Flags.change(status, 
            IMeasurements.STATUS_NET, tpdg.isNetworkIoDataIncluded(true));
    }

    /**
     * Sends JVM and system data to the output stream.
     * 
     * @param out the stream where to write the data to
     * @throws IOException in case of any I/O problem
     * 
     * @since 1.00
     */
    public void send(DataOutputStream out) throws IOException {
        system.send(out);
        jvm.send(out);
    }

    /**
     * Reads data of this instance from the given input stream.
     * 
     * @param in the input stream from where to read data from
     * @throws IOException in case of any I/O problem
     * 
     * @since 1.00
     */
    public void read(DataInputStream in) throws IOException {
        system.read(in);
        jvm.read(in);
    }
    
    /**
     * Copies all values from the given <code>data</code> instance.
     * 
     * @param data the instance to copy from
     * 
     * @since 1.00
     */
    public void copyFrom(ProcessData data) {
        system.copyFrom(data.getSystem());
        jvm.copyFrom(data.getJvm());
    }

}
