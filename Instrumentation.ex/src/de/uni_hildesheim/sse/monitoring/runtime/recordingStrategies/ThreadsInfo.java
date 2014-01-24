package de.uni_hildesheim.sse.monitoring.runtime.recordingStrategies;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import de.uni_hildesheim.sse.monitoring.runtime.boot.ObjectPool;
import de.uni_hildesheim.sse.monitoring.runtime.boot.Poolable;

/**
 * Contains information about changes in running threads between
 * to points in time. Used also to store and transfer information on the current
 * thread. In SPASS-monitor we distinguish between monitored running threads and
 * remainder threads (started outside monitoring, e.g. as part of the JVM). This
 * class is intended to store information about the monitored running threads
 * and the aggregated information on the remainder threads.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public class ThreadsInfo /*extends LongLongHashMap*/ 
    implements Poolable<ThreadsInfo> {

    /**
     * Defines a pseudo thread id representing all remaining active threads
     * currently not being monitored.
     */
    //public static final long REMAINDER_THREAD_ID = -1;
    
    /**
     * Defines an object pool for this class.
     */
    public static final ObjectPool<ThreadsInfo> POOL = 
        new ObjectPool<ThreadsInfo>(new ThreadsInfo(), 500);
        
    /**
     * Stores the current time ticks of the (initially) creating thread.
     */
    private long currentThreadTicks;
    
    /**
     * Stores the id of the (initially) creating thread.
     */
    private long currentThreadId;
    
    /**
     * Stores the thread time ticks aggregated in this instance in 
     * {@link #put(long, long)}.
     */
    //private transient long storedTicks;
    
    /**
     * Creates a new thread information object. The specified values are
     * for transporting and passing the current values to the receiver method
     * (possibly over network). The parameters are not determined automatically
     * in this constructor, because the instance creation can also be done with 
     * transmitted data on a remote machine.<br/>
     * Do <b>not</b> call this constructor from outside as the instances of this
     * class are pooled {@link #POOL}!
     * 
     * @since 1.00
     */
    ThreadsInfo() {
        // do not call this from outside, use the ObjectPools
        //super();
    }
    
    /**
     * Creates a further instance of this instances (intended as factory method
     * to be executed on a prototypical instance).
     * 
     * @return the created instance
     * 
     * @since 1.00
     */
    public ThreadsInfo create() {
        return new ThreadsInfo();
    }

    /**
     * Obtains an instance from the object pool and reads the related
     * data from a data input stream (convenience method).
     * 
     * @param in the input stream
     * @return the instance read from the stream
     * @throws IOException in case that an I/O error occurred while reading
     */
    public static ThreadsInfo readFromPool(DataInputStream in) 
        throws IOException {
        ThreadsInfo result = POOL.getFromPool();
        result.read(in);
        return result;
    }
    
    /**
     * Specifies the thread id to initialize (reusable)
     * instances.
     * 
     * @param currentThreadId the current thread id
     * 
     * @since 1.00
     */
    public void setThreadId(long currentThreadId) {
        // do not call this from outside, use the ObjectPools
        this.currentThreadId = currentThreadId;
    }

    /**
     * Specifies the thread time ticks to initialize (reusable)
     * instances.
     * 
     * @param currentThreadTicks the current thread time ticks (CPU time)
     * 
     * @since 1.00
     */
    public void setCurrentThreadTicks(long currentThreadTicks) {
        this.currentThreadTicks = currentThreadTicks;
    }
    
    /**
     * Clears this instance before returning it to the pool.
     * 
     * @since 1.00
     */
    public void clear() {
        //super.clear();
        //storedTicks = 0;
    }
    
    /**
     * Writes this instance to a data output stream.
     * 
     * @param out the output stream
     * @throws IOException in case that an I/O error occurred while writing
     */
    public void write(DataOutputStream out) throws IOException {
        out.writeLong(currentThreadTicks);
        out.writeLong(currentThreadId);
        //super.write(out);
    }

    /**
     * Reads this instance from a data input stream.
     * 
     * @param in the input stream
     * @throws IOException in case that an I/O error occurred while reading
     */
    public void read(DataInputStream in) throws IOException {
        currentThreadTicks = in.readLong();
        currentThreadId = in.readLong();
        //super.read(in);
    }

    /**
     * Returns the time ticks consumed by the current thread as given in the
     * constructor. Note, that the result is not the current id of the thread 
     * executing this method!
     * 
     * @return the time ticks consumed by the current thread
     */
    public long getCurrentThreadTicks() {
        return currentThreadTicks;
    }

    /**
     * Returns the current thread id as given in the constructor. Note,
     * that the result is not the current id of the thread executing this
     * method!
     * 
     * @return the threadId the current thread id
     */
    public long getCurrentThreadId() {
        return currentThreadId;
    }
    
    /**
     * Returns all ticks added to this class as values in 
     * {@link #put(long, long)}.
     * 
     * @return the sum of all ticks added to this class
     * 
     * @since 1.00
     */
    /*public long getStoredTicks() {
        return storedTicks;
    }*/
    
    /**
     * Adds information about a thread. This method updates {@link #storedTicks}
     * accordingly.
     * 
     * @param key the thread id
     * @param value the runtime taken by the thread (in nano seconds) or, 
     *     dependent on use and as intended, the difference of runtime between 
     *     two points in time (in this case threads with no change in runtime 
     *     should not be added
     */
    /*public void put(long key, long value) {
        storedTicks += value;
        super.put(key, value);
    }*/
    
    /**
     * Returns the sum of the (difference) time ticks for all remainder threads.
     * 
     * @return the time ticks for the remainder threads
     * 
     * @since 1.00
     */
    /*public long getReminderThreadsTicks() {
        return get(ThreadsInfo.REMAINDER_THREAD_ID);
    }*/
    
    /**
     * Returns a textual representation.
     * 
     * @return the textual representation
     */
    public String toString() {
        return currentThreadId + " " + currentThreadTicks /*+ " " + storedTicks
            + " " + super.toString()*/;
    }

}
