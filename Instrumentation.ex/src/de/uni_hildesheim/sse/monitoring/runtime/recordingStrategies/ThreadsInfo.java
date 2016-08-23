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
public class ThreadsInfo implements Poolable<ThreadsInfo> {

    /**
     * Defines an object pool for this class.
     * 
     * The pool size needs to be twice as large as the enter and exit pools.
     */
    public static final ObjectPool<ThreadsInfo> POOL = 
        new ObjectPool<ThreadsInfo>(new ThreadsInfo(), 2000);
        
    /**
     * Stores the current time ticks of the (initially) creating thread.
     */
    private long currentThreadTicks;
    
    /**
     * Stores the id of the (initially) creating thread.
     */
    private long currentThreadId;
    
    /**
     * Stores the current instance identifier (<code>0</code> means none).
     */
    private long currentInstanceId;
    
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
     * Specifies the instance id to initialize (reusable)
     * instances.
     * 
     * @param currentInstanceId the current instance id (<code>0</code> for disabled)
     * 
     * @since 1.00
     */
    public void setInstanceId(long currentInstanceId) {
        this.currentInstanceId = currentInstanceId;
    }

    /**
     * Specifies both ids to initialize (reusable)
     * instances.
     * 
     * @param currentThreadId the current thread id
     * @param currentInstanceId the current instance id (<code>0</code> for disabled)
     * 
     * @since 1.00
     */
    public void setIds(long currentThreadId, long currentInstanceId) {
        this.currentThreadId = currentThreadId;
        this.currentInstanceId = currentInstanceId;
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
        out.writeLong(currentInstanceId);
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
        currentInstanceId = in.readLong();
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
     * Returns the current instance id.
     * 
     * @return the instance id (disabled if <code>0</code>)
     */
    public long getCurrentInstanceId() {
        return currentInstanceId;
    }
    
    /**
     * Returns a textual representation.
     * 
     * @return the textual representation
     */
    public String toString() {
        return currentThreadId + " " + currentThreadTicks + " " + currentInstanceId;
    }

}
