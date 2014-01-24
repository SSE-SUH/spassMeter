package de.uni_hildesheim.sse.wildcat.listeners;

import de.uni_hildesheim.sse.monitoring.runtime.plugins.ValueChangeListener;
import de.uni_hildesheim.sse.monitoring.runtime.plugins.ValueType;
import de.uni_hildesheim.sse.wildcat.services.WServiceRegistry;
import de.uni_hildesheim.sse.wildcat.services.WValue;

/**
 * Value change listener.
 * 
 * @author Stephan Dederichs
 * 
 * @version 1.00
 * @since 1.00
 */
public class WildCATValueChangeListener implements ValueChangeListener {

    @Override
    public void notifyValueChange(String recId, ValueType type, 
            Object newValue) {
        WValue valueService = (WValue) WServiceRegistry.getService(recId);
        if (null == valueService) {
            valueService = new WValue(recId, type, newValue);
            WServiceRegistry.registerService(recId, valueService);   
        } else {
            valueService.setNewValue(newValue);
        }
    }
    
}
