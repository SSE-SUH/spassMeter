package test.threadedTest.derived;

import test.threadedTest.Data;
import test.threadedTest.Runnable2;
import test.threadedTest.ThreadFinishedListener;
import de.uni_hildesheim.sse.monitoring.runtime.annotations.Monitor;
import de.uni_hildesheim.sse.system.GathererFactory;
import de.uni_hildesheim.sse.system.IThreadDataGatherer;

/**
 * A derived runnable.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
@Monitor
public class Runnable3 extends Runnable2 {

    /**
     * Creates a new runnable.
     * 
     * @param listener the listener to be notified
     * @param expectedTime the expected runtime of this thread in milliseconds
     * 
     * @since 1.00
     */
    public Runnable3(ThreadFinishedListener listener, long expectedTime) {
        super(listener, expectedTime);
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
        for (int i = 0; i < 30000; i++) {
            data.add(new Data());
            if (i % 50 == 0) {
                data.clear();
            }
            if (i % 50 == 0) {
                System.gc();
            }
        }
        finished();
        setElapsedCpuTime(tdg.getCurrentCpuTime() - st);
        setElapsedSystemTime(System.nanoTime() - now);
    }
    
}