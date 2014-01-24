package de.uni_hildesheim.sse.wildcat.services;

import javax.management.AttributeNotFoundException;

import de.uni_hildesheim.sse.monitoring.runtime.plugins.IMeasurements;
import de.uni_hildesheim.sse.serviceConstants.MeasurementsConstants;

/**
 * WildCAT service for events from the {@link IMeasurements}.
 * 
 * @author Stephan Dederichs
 * 
 * @version 1.00
 * @since 1.00
 */
public class WMeasurements extends AbstractWServiceData 
    implements IMeasurements {

    /**
     * Stores a {@link IMeasurements}.
     * 
     * @since 1.00
     */
    private IMeasurements measurement;
    
    /**
     * Creates an instance of {@link WMeasurements}.
     * 
     * @param measurement The {@link IMeasurements}.
     * 
     * @since 1.00
     */
    public WMeasurements(IMeasurements measurement) {
        this.measurement = measurement;
    }
    
    @Override
    public double getAvgLoad() {
        double result = measurement.getAvgLoad();
        setSensorValue(MeasurementsConstants.AVG_LOAD, result);
        return result;
    }

    @Override
    public double getAvgMemUse() {
        double result = measurement.getAvgMemUse();
        setSensorValue(MeasurementsConstants.AVG_MEM_USE, result);
        return result;
    }

    @Override
    public long getIoRead() {
        long result = measurement.getIoRead();
        setSensorValue(MeasurementsConstants.IO_READ, result);
        return result;
    }

    @Override
    public long getIoWrite() {
        long result = measurement.getIoWrite();
        setSensorValue(MeasurementsConstants.IO_WRITE, result);
        return result;
    }

    @Override
    public double getMaxLoad() {
        double result = measurement.getMaxLoad();
        setSensorValue(MeasurementsConstants.MAX_LOAD, result);
        return result;
    }

    @Override
    public long getMaxMemUse() {
        long result = measurement.getMinMemUse();
        setSensorValue(MeasurementsConstants.MAX_MEM_USE, result);
        return result;
    }

    @Override
    public double getMinLoad() {
        double result = measurement.getMinLoad();
        setSensorValue(MeasurementsConstants.MIN_LOAD, result);
        return result;
    }

    @Override
    public long getMinMemUse() {
        long result = measurement.getMinMemUse();
        setSensorValue(MeasurementsConstants.MIN_MEM_USE, result);
        return result;
    }

    @Override
    public int getStatus() {
        int result = measurement.getStatus();
        setSensorValue(MeasurementsConstants.STATUS, result);
        return result;
    }

    @Override
    public long getSystemTime() {
        long result = measurement.getSystemTime();
        setSensorValue(MeasurementsConstants.SYSTEM_TIME, result);
        return result;
    }
    
    // ------------------------------------------------------------------------
    
    @Override
    public void burstValueUpdate() {
        getAvgLoad();
        getAvgMemUse();
        getIoRead();
        getIoWrite();
        getMaxLoad();
        getMaxMemUse();
        getMinLoad();
        getMinMemUse();
        getStatus();
        getSystemTime();
    }

    @Override
    public Object getDataSpecificAttribute(String attributeName)
        throws AttributeNotFoundException {
        if (attributeName.equals(MeasurementsConstants.AVG_LOAD)) {
            return getAvgLoad();
        } else if (attributeName.equals(MeasurementsConstants.AVG_MEM_USE)) {
            return getAvgMemUse();
        } else if (attributeName.equals(MeasurementsConstants.IO_READ)) {
            return getIoRead();
        } else if (attributeName.equals(MeasurementsConstants.IO_WRITE)) {
            return getIoWrite();
        } else if (attributeName.equals(MeasurementsConstants.MAX_LOAD)) {
            return getMaxLoad();
        } else if (attributeName.equals(MeasurementsConstants.MAX_MEM_USE)) {
            return getMaxMemUse();
        } else if (attributeName.equals(MeasurementsConstants.MIN_LOAD)) {
            return getMinLoad();
        } else if (attributeName.equals(MeasurementsConstants.MIN_MEM_USE)) {
            return getMinMemUse();
        } else if (attributeName.equals(MeasurementsConstants.STATUS)) {
            return getStatus();
        } else if (attributeName.equals(MeasurementsConstants.SYSTEM_TIME)) {
            return getSystemTime();
        } else {
            throw new AttributeNotFoundException("Invalid attribute: "
                    + attributeName);
        }
    }

}
