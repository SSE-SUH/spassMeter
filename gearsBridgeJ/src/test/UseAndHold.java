package test;

import de.uni_hildesheim.sse.system.GathererFactory;
import de.uni_hildesheim.sse.system.IThisProcessDataGatherer;

/**
 * Uses the library and holds the lock on the native library. The aim of this 
 * class is to test whether unpacking / loading the native library works with
 * two JVMs on the same physical machine using the native library.
 * 
 * @author Holger Eichelberger
 */
public class UseAndHold {

    /**
     * Prevents external creation.
     */
    private UseAndHold() {
    }

    /**
     * Loads the library and emits the current load.
     * 
     * @param args the command line arguments - ignored
     */
    public static void main(String[] args) {
        IThisProcessDataGatherer gatherer 
            = GathererFactory.getThisProcessDataGatherer();
        while (true) {
            double load = gatherer.getCurrentProcessProcessorLoad();
            System.out.println("Load " + load);
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
            }
        }
    }

}
