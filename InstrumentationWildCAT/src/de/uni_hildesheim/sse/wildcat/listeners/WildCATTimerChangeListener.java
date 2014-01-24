package de.uni_hildesheim.sse.wildcat.listeners;

import de.uni_hildesheim.sse.monitoring.runtime.plugins.TimerChangeListener;
import de.uni_hildesheim.sse.wildcat.services.WServiceRegistry;
import de.uni_hildesheim.sse.wildcat.services.WTimer;

/**
 * Timer change listener.
 * 
 * @author Stephan Dederichs
 * 
 * @version 1.00
 * @since 1.00
 */
public class WildCATTimerChangeListener implements TimerChangeListener {

    @Override
    public void timerFinished(String recId, long value) {
        WTimer timerService = (WTimer) WServiceRegistry.getService(recId);
        if (null == timerService) {
            timerService = new WTimer(recId, value);
            WServiceRegistry.registerService(recId, timerService);
        } else {
            timerService.setValue(value);
        }
    }

}
