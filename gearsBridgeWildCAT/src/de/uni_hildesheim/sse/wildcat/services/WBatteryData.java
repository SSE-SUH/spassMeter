package de.uni_hildesheim.sse.wildcat.services;

import javax.management.AttributeNotFoundException;

import de.uni_hildesheim.sse.codeEraser.annotations.Variability;
import de.uni_hildesheim.sse.serviceConstants.BatteryDataConstants;
import de.uni_hildesheim.sse.system.AnnotationConstants;
import de.uni_hildesheim.sse.system.GathererFactory;
import de.uni_hildesheim.sse.system.IBatteryDataGatherer;

/**
 * Defines an object for delegation of the values from the interface
 * {@link IBatteryDataGatherer}. Additional to that it defines its WildCAT
 * service interface.
 * 
 * @author Stephan Dederichs
 * 
 * @since 1.00
 * @version 1.00
 */
@Variability(id = AnnotationConstants.VAR_ENERGY_DATA)
public class WBatteryData extends AbstractWServiceData 
    implements IBatteryDataGatherer {

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
    public WBatteryData() {
        batteryDataGatherer = GathererFactory.getBatteryDataGatherer();
    }

    @Override
    @Variability(id = AnnotationConstants.VAR_ENERGY_DATA, value = "false")
    public boolean hasSystemBattery() {
        boolean result = batteryDataGatherer.hasSystemBattery();
        setSensorValue(BatteryDataConstants.HAS_SYSTEM_BATTERY, result);
        return result;
    }

    @Override
    @Variability(id = AnnotationConstants.VAR_ENERGY_DATA, value = "-1")
    public int getBatteryLifePercent() {
        int result = batteryDataGatherer.getBatteryLifePercent();
        setSensorValue(BatteryDataConstants.BATTERY_LIFE_PERCENT, result);
        return result;
    }

    @Override
    @Variability(id = AnnotationConstants.VAR_ENERGY_DATA, value = "-1")
    public int getBatteryLifeTime() {
        int result = batteryDataGatherer.getBatteryLifeTime();
        setSensorValue(BatteryDataConstants.BATTERY_LIFE_TIME, result);
        return result;
    }

    @Override
    @Variability(id = AnnotationConstants.VAR_ENERGY_DATA, value = "-1")
    public int getPowerPlugStatus() {
        int result = batteryDataGatherer.getPowerPlugStatus();
        setSensorValue(BatteryDataConstants.POWER_PLUG_STATUS, result);
        return result;
    }

    // ------------------------------------------------------------------------
        
    @Override
    public void burstValueUpdate() {
        getBatteryLifePercent();
        getBatteryLifeTime();
        hasSystemBattery();
        getPowerPlugStatus();
    }
    
    @Override
    public Object getDataSpecificAttribute(String valueName)
        throws AttributeNotFoundException {
        // Check for individual attributes
        if (valueName.equals(BatteryDataConstants.HAS_SYSTEM_BATTERY)) {
            return hasSystemBattery();
        } else if (valueName.equals(BatteryDataConstants.
                BATTERY_LIFE_PERCENT)) {
            return getBatteryLifePercent();
        } else if (valueName.equals(BatteryDataConstants.
                BATTERY_LIFE_TIME)) {
            return getBatteryLifeTime();
        } else if (valueName.equals(BatteryDataConstants.
                POWER_PLUG_STATUS)) {
            return getPowerPlugStatus();
        } else {
            throw new AttributeNotFoundException("Invalid attribute: "
                    + valueName);
        }
    }

}
