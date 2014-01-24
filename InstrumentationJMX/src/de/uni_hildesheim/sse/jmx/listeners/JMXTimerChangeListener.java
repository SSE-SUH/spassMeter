package de.uni_hildesheim.sse.jmx.listeners;

import de.uni_hildesheim.sse.jmx.services.JMXServiceRegistry;
import de.uni_hildesheim.sse.jmx.services.JMXTimer;
import de.uni_hildesheim.sse.monitoring.runtime.plugins.TimerChangeListener;

/**
 * Timer change listener.
 * 
 * @author Stephan Dederichs
 * 
 * @version 1.00
 * @since 1.00
 */
public class JMXTimerChangeListener implements TimerChangeListener {
    
    @Override
    public void timerFinished(String recId, long value) {
        JMXTimer timerService = (JMXTimer) JMXServiceRegistry.getService(recId);
        if (null == timerService) {
            timerService = new JMXTimer(recId, value);
            JMXServiceRegistry.registerService(recId, timerService);
        } else {
            timerService.setValue(value);
        }
    }

}
