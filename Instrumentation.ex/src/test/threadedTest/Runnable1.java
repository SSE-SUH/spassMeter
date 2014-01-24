package test.threadedTest;

import de.uni_hildesheim.sse.monitoring.runtime.annotations.Monitor;
import de.uni_hildesheim.sse.system.GathererFactory;
import de.uni_hildesheim.sse.system.IThreadDataGatherer;

/**
 * Defines the first thread runnable.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
@Monitor
public final class Runnable1 extends AbstractRunnable {

    /**
     * A data attribute storing one data instance created.
     */
    private Data data;
    
    /**
     * Creates a new runnable.
     * 
     * @param listener the listener to be notified
     * @param expectedTime the expected runtime of this thread in milliseconds
     * 
     * @since 1.00
     */
    public Runnable1(ThreadFinishedListener listener, long expectedTime) {
        super(listener, expectedTime);
    }
    
    /**
     * Returns the current data.
     * 
     * @return the current data instance
     * 
     * @since 1.00
     */
    public Data getData() {
        return data;
    }
    
    /**
     * Executes this thread.
     */
    @Override
    public void run() {
        long now = System.nanoTime();
        IThreadDataGatherer tdg = GathererFactory.getThreadDataGatherer();
        long st = tdg.getCurrentCpuTime();
        started();
        for (int i = 0; i < 10000; i++) {
            data = new Data();
            if (i % 50 == 0) {
                System.gc();
            }
        }
        finished();
        setElapsedCpuTime(tdg.getCurrentCpuTime() - st);
        setElapsedSystemTime(System.nanoTime() - now);
    }
}