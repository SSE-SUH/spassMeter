package de.uni_hildesheim.sse.monitoring.runtime.plugins;

import de.uni_hildesheim.sse.codeEraser.annotations.Variability;
import de.uni_hildesheim.sse.monitoring.runtime.AnnotationConstants;
import de.uni_hildesheim.sse.monitoring.runtime.configuration.Configuration;

/**
 * Implements a basic listener for changing (attribute) values which always 
 * emits that the value (event) was not handled.
 *
 * @author Holger Eichelberger
 * @version 1.0
 */
@Variability(id = AnnotationConstants.MONITOR_VALUES)
public class DefaultValueChangeListener implements ValueChangeListener {

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
        System.out.println("currently unhandled value change " 
            + newValue + " (" + recId + ")");
        Configuration.LOG.info("currently unhandled value change " 
            + newValue + " (" + recId + ")");
    }

}