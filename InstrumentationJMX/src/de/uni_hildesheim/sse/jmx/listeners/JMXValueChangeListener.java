package de.uni_hildesheim.sse.jmx.listeners;

import de.uni_hildesheim.sse.jmx.services.JMXServiceRegistry;
import de.uni_hildesheim.sse.jmx.services.JMXValue;
import de.uni_hildesheim.sse.monitoring.runtime.plugins.ValueChangeListener;
import de.uni_hildesheim.sse.monitoring.runtime.plugins.ValueType;

/**
 * Value change listener.
 * 
 * @author Stephan Dederichs
 * 
 * @version 1.00
 * @since 1.00
 */
public class JMXValueChangeListener implements ValueChangeListener {

    @Override
    public void notifyValueChange(String recId, ValueType type, 
        Object newValue) {
        JMXValue valueService = (JMXValue) JMXServiceRegistry.getService(recId);
        if (null == valueService) {
            valueService = new JMXValue(recId, type, newValue);
            JMXServiceRegistry.registerService(recId, valueService);   
        } else {
            valueService.setNewValue(newValue);
        }
    }

}
