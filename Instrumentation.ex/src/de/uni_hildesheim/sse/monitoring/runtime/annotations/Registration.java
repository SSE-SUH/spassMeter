package de.uni_hildesheim.sse.monitoring.runtime.annotations;

import de.uni_hildesheim.sse.monitoring.runtime.boot.ResourceType;
import de.uni_hildesheim.sse.monitoring.runtime.configuration.Annotations;
import de.uni_hildesheim.sse.monitoring.runtime.configuration.AttributeMetaData;

/**
 * Registers the annotations in this package for XML reading.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public class Registration {

    /**
     * Prevents this class from being called from outside.
     * 
     * @since 1.00
     */
    private Registration() {
    }
    
    /**
     * Performs the registration.
     * 
     * @since 1.00
     */
    public static final void register2XML() {
        // do not use ResourceType.ALL - this locks the VM
        ResourceType[] all = new ResourceType[1];
        all[0] = ResourceType.ALL;
        Annotations.register(Monitor.class, 
            new AttributeMetaData("resources", all, all.getClass()));
        Annotations.register(ExcludeFromMonitoring.class);
        Annotations.register(StartSystem.class);
        Annotations.register(EndSystem.class);
        Annotations.register(ValueChange.class);
        Annotations.register(ValueContext.class);
        Annotations.register(Timer.class);
        Annotations.register(VariabilityHandler.class);
        Annotations.register(ConfigurationChange.class);
        Annotations.register(NotifyValue.class);
    }
    
}
