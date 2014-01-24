package test;

import java.util.Timer;
import java.util.TimerTask;

import de.uni_hildesheim.sse.system.GathererFactory;
import de.uni_hildesheim.sse.system.IProcessorDataGatherer;

/**
 * A simple test calling system load in a timer task.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public class TimerTest {

    /**
     * Prevents this class from being instantiated from outside.
     * 
     * 
     * @since 1.00
     */
    private TimerTest() {
    }
    
    /**
     * The timer task.
     * 
     * @author Holger Eichelberger
     * @since 1.00
     * @version 1.00
     */
    private static class Task extends TimerTask {

        /**
         * Executes the task.
         */
        @Override
        public void run() {
            IProcessorDataGatherer pdg = 
                GathererFactory.getProcessorDataGatherer();
            System.out.println(pdg.getCurrentSystemLoad());
        }
        
    }

    /**
     * Executes the test.
     * 
     * @param args ignored
     * 
     * @since 1.00
     */
    public static void main(String[] args) {
        Timer timer = new Timer();
        timer.schedule(new Task(), 0, 1000);
    }
}
