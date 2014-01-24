package de.uni_hildesheim.sse.jmx.services.standard;

import de.uni_hildesheim.sse.codeEraser.annotations.Variability;
import de.uni_hildesheim.sse.system.AnnotationConstants;
import de.uni_hildesheim.sse.system.GathererFactory;
import de.uni_hildesheim.sse.system.IBatteryDataGatherer;

/**
 * Defines an object for delegation of the values from the interface
 * {@link IBatteryDataGatherer} to the {@link StandardMBeanAgent}.
 * 
 * @author Stephan Dederichs
 * 
 * @since 1.00
 * @version 1.00
 */
@Variability(id = AnnotationConstants.VAR_ENERGY_DATA)
public class BatteryData implements BatteryDataMBean {

    /**
     * Instance of {@link IBatteryDataGatherer} for gathering required
     * informations.
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.VAR_PROCESSOR_DATA, value = "-1")
    private IBatteryDataGatherer batteryDataGatherer;

    /**
     * Constructor.
     * 
     * @since 1.00
     */
    @Variability(id = AnnotationConstants.VAR_PROCESSOR_DATA, value = "-1")
    public BatteryData() {
        batteryDataGatherer = GathererFactory.getBatteryDataGatherer();
    }

    @Override
    @Variability(id = AnnotationConstants.VAR_ENERGY_DATA, value = "false")
    public boolean hasSystemBattery() {
        return batteryDataGatherer.hasSystemBattery();
    }

    @Override
    @Variability(id = AnnotationConstants.VAR_ENERGY_DATA, value = "-1")
    public int getBatteryLifePercent() {
        return batteryDataGatherer.getBatteryLifePercent();
    }

    @Override
    @Variability(id = AnnotationConstants.VAR_ENERGY_DATA, value = "-1")
    public int getBatteryLifeTime() {
        return batteryDataGatherer.getBatteryLifeTime();
    }

    @Override
    @Variability(id = AnnotationConstants.VAR_ENERGY_DATA, value = "-1")
    public int getPowerPlugStatus() {
        return batteryDataGatherer.getPowerPlugStatus();
    }

}
