package test.threadedTest;

import de.uni_hildesheim.sse.monitoring.runtime.annotations.
    ExcludeFromMonitoring;

/**
 * A base class for notifying runnables.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public abstract class AbstractRunnable implements Runnable {
    
    /**
     * Stores the finished thread listener.
     */
    private ThreadFinishedListener listener;
    
    /**
     * Stores the expected runtime (to be filled by waiting).
     */
    private long expectedTime;
    
    /**
     * Stores the start time of this thread.
     */
    private long startTime;
    
    /**
     * Stores the elapsed CPU time.
     */
    private long elapsedCpuTime;

    /**
     * Stores the elapsed system time.
     */    
    private long elapsedSystemTime;
    
    /**
     * Creates a new runnable.
     * 
     * @param listener the listener to be notified
     * @param expectedTime the expected runtime of this thread in milliseconds
     * 
     * @since 1.00
     */
    public AbstractRunnable(ThreadFinishedListener listener, 
        long expectedTime) {
        this.listener = listener;
        this.expectedTime = expectedTime;
    }
    
    /**
     * Call this at the beginning of a thread.
     * 
     * @since 1.00
     */
    protected void started() {
        startTime = System.currentTimeMillis();
    }
    
    /**
     * Call this at the end of a thread.
     * 
     * @since 1.00
     */
    protected void finished() {
        long now = System.currentTimeMillis();
        if (expectedTime > 0) {
            long diff = expectedTime - (now - startTime);
            if (diff > 0) {
                try {
                    Thread.sleep(diff);
                } catch (InterruptedException e) {
                    System.out.println("Sleep was interrupted");
                }
            }
        }
        if (null != listener) {
            listener.finished();
        }
    }
    
    /**
     * Returns the elapsed CPU time.
     * 
     * @return the elapsed CPU time
     * 
     * @since 1.00
     */
    @ExcludeFromMonitoring
    public long getElapsedCpuTime()  {
        return elapsedCpuTime;
    }
    
    /**
     * Sets the elapsed CPU time.
     * 
     * @param time the elapsed CPU time
     * 
     * @since 1.00
     */
    @ExcludeFromMonitoring
    protected void setElapsedCpuTime(long time) {
        this.elapsedCpuTime = time;
    }

    /**
     * Returns the elapsed system time.
     * 
     * @return the elapsed system time
     * 
     * @since 1.00
     */
    @ExcludeFromMonitoring
    public long getElapsedSystemTime()  {
        return elapsedSystemTime;
    }
    
    /**
     * Sets the elapsed system time.
     * 
     * @param time the elapsed system time
     * 
     * @since 1.00
     */
    @ExcludeFromMonitoring
    protected void setElapsedSystemTime(long time) {
        this.elapsedSystemTime = time;
    }
    
}
