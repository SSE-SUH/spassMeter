package de.uni_hildesheim.sse.system;

import de.uni_hildesheim.sse.codeEraser.annotations.Variability;

/**
 * Defines an interface for requesting energy information from the system.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
@Variability(id = AnnotationConstants.VAR_ENERGY_DATA)
public interface IBatteryDataGatherer {

    /**
     * Returns if the system is equipped with a battery.
     * 
     * @return <code>true</code> if there is a battery, <code>false</code> if 
     *   not (power plug only)
     */
    @Variability(id = AnnotationConstants.VAR_ENERGY_DATA, value = "false")
    public abstract boolean hasSystemBattery();

    /**
     * Returns the remaining battery life time in percent.
     * 
     * @return the percentage of the remaining battery life time in percent,
     *     negative if this value is unknown 
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.VAR_ENERGY_DATA, value = "-1")
    public abstract int getBatteryLifePercent();

    /**
     * Returns the remaining battery life time in seconds.
     * 
     * @return the percentage of the remaining battery life time in seconds,
     *     negative if this value is unknown 
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.VAR_ENERGY_DATA, value = "-1")
    public abstract int getBatteryLifeTime();

    /**
     * Returns the power plug status, i.e. if the device is plugged in.
     * 
     * @return <code>1</code> if the device is plugged in, <code>0</code>
     *    if this device is not plugged in, a negative value if the status
     *    is unknown 
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.VAR_ENERGY_DATA, value = "-1")
    public abstract int getPowerPlugStatus();

}