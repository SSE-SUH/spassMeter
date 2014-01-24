package de.uni_hildesheim.sse.system.deflt;

import de.uni_hildesheim.sse.codeEraser.annotations.Variability;
import de.uni_hildesheim.sse.system.AnnotationConstants;
import de.uni_hildesheim.sse.system.IBatteryDataGatherer;

/**
 * Realizes a class requesting energy information from the system.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
@Variability(id = AnnotationConstants.VAR_ENERGY_DATA)
class BatteryDataGatherer implements IBatteryDataGatherer {

    // power

    /**
     * Returns if the system is equipped with a battery.
     * 
     * @return <code>true</code> if there is a battery, <code>false</code> if 
     *   not (power plug only)
     */
    @Variability(id = AnnotationConstants.VAR_ENERGY_DATA, value = "false")
    private static native boolean hasSystemBattery0();
    
    /**
     * Returns the remaining battery life time in percent.
     * 
     * @return the percentage of the remaining battery life time in percent,
     *     negative if this value is unknown 
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.VAR_ENERGY_DATA, value = "-1")
    private static native int getBatteryLifePercent0();

    /**
     * Returns the remaining battery life time in seconds.
     * 
     * @return the percentage of the remaining battery life time in seconds,
     *     negative if this value is unknown 
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.VAR_ENERGY_DATA, value = "-1")
    private static native int getBatteryLifeTime0();

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
    private static native int getPowerPlugStatus0();

    
    // power

    /**
     * Returns if the system is equipped with a battery.
     * 
     * @return <code>true</code> if there is a battery, <code>false</code> if 
     *   not (power plug only)
     */
    @Override
    @Variability(id = AnnotationConstants.VAR_ENERGY_DATA, value = "false")
    public boolean hasSystemBattery() {
        return hasSystemBattery0();
    }
    
    /**
     * Returns the remaining battery life time in percent.
     * 
     * @return the percentage of the remaining battery life time in percent,
     *     negative if this value is unknown 
     * 
     * @since 1.00
     */
    @Override
    @Variability(id = AnnotationConstants.VAR_ENERGY_DATA, value = "-1")
    public int getBatteryLifePercent() {
        return getBatteryLifePercent0();
    }

    /**
     * Returns the remaining battery life time in seconds.
     * 
     * @return the percentage of the remaining battery life time in seconds,
     *     negative if this value is unknown 
     * 
     * @since 1.00
     */
    @Override
    @Variability(id = AnnotationConstants.VAR_ENERGY_DATA, value = "-1")
    public int getBatteryLifeTime() {
        return getBatteryLifeTime0();
    }

    /**
     * Returns the power plug status, i.e. if the device is plugged in.
     * 
     * @return <code>1</code> if the device is plugged in, <code>0</code>
     *    if this device is not plugged in, a negative value if the status
     *    is unknown 
     * 
     * @since 1.00
     */
    @Override
    @Variability(id = AnnotationConstants.VAR_ENERGY_DATA, value = "-1")
    public int getPowerPlugStatus() {
        return getPowerPlugStatus0();
    }

}
