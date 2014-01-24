package test.threadedTest;

/**
 * Defines a notification interface for finished threads.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public interface ThreadFinishedListener {

    /**
     * To be called when a thread finishes.
     * 
     * @since 1.00
     */
    public void finished();
}
