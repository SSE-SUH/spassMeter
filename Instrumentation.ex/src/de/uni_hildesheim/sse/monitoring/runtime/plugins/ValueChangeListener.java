package de.uni_hildesheim.sse.monitoring.runtime.plugins;

import de.uni_hildesheim.sse.codeEraser.annotations.Variability;
import de.uni_hildesheim.sse.monitoring.runtime.AnnotationConstants;

/**
 * Defines a listener for changing (attribute) values in a monitored
 * program.
 *
 * @author Holger Eichelberger
 * @version 1.0
 */
@Variability(id = AnnotationConstants.MONITOR_VALUES)
public interface ValueChangeListener {

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
        Object newValue);

}
