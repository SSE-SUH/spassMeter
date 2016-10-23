package de.uni_hildesheim.sse.system.fallback;

import de.uni_hildesheim.sse.codeEraser.annotations.Variability;
import de.uni_hildesheim.sse.system.AnnotationConstants;
import de.uni_hildesheim.sse.system.IBatteryDataGatherer;

/**
 * Realizes a class requesting energy information from the system.
 * 
 * @author Holger Eichelberger
 * @since 1.20
 * @version 1.20
 */
@Variability(id = AnnotationConstants.VAR_ENERGY_DATA)
class BatteryDataGatherer implements IBatteryDataGatherer {

    @Override
    @Variability(id = AnnotationConstants.VAR_ENERGY_DATA, value = "false")
    public boolean hasSystemBattery() {
        return false;
    }
    
    @Override
    @Variability(id = AnnotationConstants.VAR_ENERGY_DATA, value = "-1")
    public int getBatteryLifePercent() {
        return -1;
    }

    @Override
    @Variability(id = AnnotationConstants.VAR_ENERGY_DATA, value = "-1")
    public int getBatteryLifeTime() {
        return -1;
    }

    @Override
    @Variability(id = AnnotationConstants.VAR_ENERGY_DATA, value = "-1")
    public int getPowerPlugStatus() {
        return -1;
    }

}
