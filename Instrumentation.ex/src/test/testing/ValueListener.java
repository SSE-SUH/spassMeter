package test.testing;

import java.util.HashMap;
import java.util.Map;

import test.AnnotationId;

import de.uni_hildesheim.sse.codeEraser.annotations.Variability;
import de.uni_hildesheim.sse.monitoring.runtime.plugins.ValueChangeListener;
import de.uni_hildesheim.sse.monitoring.runtime.plugins.ValueType;

/**
 * Implements a value listener for testing.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
@Variability(id = AnnotationId.VAR_TESTING)
public class ValueListener implements ValueChangeListener {

    /**
     * Stores the data of an event.
     * 
     * @author Holger Eichelberger
     * @since 1.00
     * @version 1.00
     */
    private class Event {
        
        /**
         * Stores the type of the value (not in original type - reflection).
         */
        private Object type;
        
        /**
         * Stores the new value.
         */
        private Object newValue;
        
        /**
         * Creates an event object.
         * 
         * @param type the type of the event
         * @param newValue the value
         * 
         * @since 1.00
         */
        private Event(Object type, Object newValue) {
            this.type = type;
            this.newValue = newValue;
        }
    }
    
    /**
     * Stores the recorded events.
     */
    private Map<String, Event> data = new HashMap<String, Event>();

    /**
     * Returns the new value for a recorder id.
     * 
     * @param recId the recorder id
     * @return the value or <b>null</b> if no event was recorded
     * 
     * @since 1.00
     */
    public Object hasEvent(String recId) {
        return data.containsKey(recId);
    }
    
    /**
     * Returns the new value for a recorder id.
     * 
     * @param recId the recorder id
     * @return the value or <b>null</b> if no event was recorded
     * 
     * @see #hasEvent(String)
     * 
     * @since 1.00
     */
    public Object getNewValue(String recId) {
        Event evt = data.get(recId);
        if (null != evt) {
            return evt.newValue;
        }
        return null; 
    }
    
    /**
     * Returns the type of the value for a recorder id.
     * 
     * @param recId the recorder id
     * @return the value or <b>null</b> if no event was recorded
     *
     * @see #hasEvent(String)
     * 
     * @since 1.00
     */
    public Object getType(String recId) {
        Event evt = data.get(recId);
        if (null != evt) {
            return evt.type;
        }
        return null; 
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
     * Notifies the recorder about a changing value.
     * 
     * @param recId the recorder id (optional, may be <b>null</b>)
     * @param type the type of the value
     * @param newValue the value after the value change
     * 
     * @since 1.00
     */
    public void notifyValueChange(String recId, ValueType type, 
        Object newValue) {
        data.put(recId, new Event(type, newValue));
    }
}
