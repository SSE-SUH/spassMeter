package test.threadedTest;

import java.util.ArrayList;
import java.util.List;

import de.uni_hildesheim.sse.monitoring.runtime.annotations.Monitor;
import de.uni_hildesheim.sse.system.GathererFactory;
import de.uni_hildesheim.sse.system.IThreadDataGatherer;

/**
 * Defines an extensible thread.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
@Monitor
public class Runnable2 extends AbstractRunnable {

    /**
     * Stores the data.
     */
    protected List<Data> data = new ArrayList<Data>();
    
    /**
     * Creates a new runnable.
     * 
     * @param listener the listener to be notified
     * @param expectedTime the expected runtime of this thread in milliseconds
     * 
     * @since 1.00
     */
    public Runnable2(ThreadFinishedListener listener, long expectedTime) {
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
        for (int i = 0; i < 15000; i++) {
            data.add(new Data());
            if (i % 50 == 0) {
                data.clear();
            }
        }
        finished();
        setElapsedCpuTime(tdg.getCurrentCpuTime() - st);
        setElapsedSystemTime(System.nanoTime() - now);        
    }
}