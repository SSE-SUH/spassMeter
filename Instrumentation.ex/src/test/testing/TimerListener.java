package test.testing;

import java.util.HashMap;
import java.util.Map;

import test.AnnotationId;

import de.uni_hildesheim.sse.codeEraser.annotations.Variability;
import de.uni_hildesheim.sse.monitoring.runtime.plugins.TimerChangeListener;

/**
 * Implements a timer listener for testing.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
@Variability(id = AnnotationId.VAR_TESTING)
public class TimerListener implements TimerChangeListener {

    /**
     * Stores the recorded events.
     */
    private Map<String, Long> data = new HashMap<String, Long>();
    
    /**
     * Returns the value for a recorder id.
     * 
     * @param recId the recorder id
     * @return the value or <b>null</b> if no event was recorder
     * 
     * @since 1.00
     */
    public Long getValue(String recId) {
        return data.get(recId);
    }

    /**
     * Returns the recorder ids for the notified timer events.
     * 
     * @return the recorder ids
     * 
     * @since 1.00
     */
    public Iterable<String> recIds() {
        return data.keySet();
    }
    
    /**
     * Is called when a user defined timer ends.
     * 
     * @param recId the recorder identification
     * @param value the value of the timer
     * 
     * @since 1.00
     */
    public void timerFinished(String recId, long value) {
        data.put(recId, value);
    }
    
}
