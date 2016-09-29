package de.uni_hildesheim.sse.monitoring.runtime.recordingStrategies;

import de.uni_hildesheim.sse.monitoring.runtime.instrumentation.ElschaLogger;
import de.uni_hildesheim.sse.monitoring.runtime.recordingStrategiesElements.*;

/**
 * Implements abstract event recorder strategy in order to buffer events
 * locally and process them in parallel.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public class DefaultEventRecorderStrategy 
    extends AbstractEventRecorderStrategy {

    /**
     * The strategy to process the events.
     */
    private RecorderStrategy strategy;
    
    /**
     * Constructor. Creates the recorder strategy.
     * 
     * @param strategy the strategy to process the events
     * 
     * @since 1.00
     */
    public DefaultEventRecorderStrategy(RecorderStrategy strategy) {
        super(strategy.getStorage());
        this.strategy = strategy;
        start();
    }

    /**
     * Handles an event.
     * 
     * @param event the event to be handled
     * @throws HandleException any kind of throwable exception which should 
     *    cause aborting the event loop
     * 
     * @since 1.00
     */
    protected void handleEvent(RecordingStrategiesElement event) 
        throws HandleException {
       ElschaLogger.info("DefaultEventRecorderStrategy.hendleEvent with event = " + event);
        event.process(strategy);
    }
}