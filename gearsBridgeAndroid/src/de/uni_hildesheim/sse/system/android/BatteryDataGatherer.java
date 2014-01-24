package de.uni_hildesheim.sse.system.android;

import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.util.Log;
import de.uni_hildesheim.sse.codeEraser.annotations.Variability;
import de.uni_hildesheim.sse.system.AnnotationConstants;
import de.uni_hildesheim.sse.system.IBatteryDataGatherer;

public class BatteryDataGatherer extends BroadcastReceiver implements
		IBatteryDataGatherer {

    /**
     * Use the native implementation instead of the
     * Java-based one. It seems that the Java-based one
     * does not properly close file descriptors in the long
     * run.
     */
    public static final boolean USE_NATIVE = false;
    
	/**
	 * The remaining battery life time in percent.
	 */
	private int batteryLifePercent = -1;

	/**
	 * The power plug status, i.e. if the device is plugged in.
	 */
	private int powerPlugStatus = -1;

	/**
	 * Boolean if the system is equipped with a battery.
	 */
	private boolean hasSystemBattery = true;

	/**
	 * The remaining battery life time in seconds.
	 */
	private int batteryLifeTime = -1;

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
	
    private static int readOneLineIntFile(String fileName, int dflt) {
        int result = dflt;
        try {           
            LineNumberReader lnr = new LineNumberReader(
                new FileReader(fileName));
            String line;
            do {
                line = lnr.readLine();
                if (null != line) {
                    result = Integer.parseInt(line);
                }
            } while (null != line);
            lnr.close();
        } catch (IOException e) {
            Log.e("SCROLL", e.getMessage());
        }
        return result;
    }
    
	/**
	 * Returns the remaining battery life time in percent.
	 * 
	 * @return the percentage of the remaining battery life time in percent,
	 *         negative if this value is unknown
	 */
	@Override
	public int getBatteryLifePercent() {
	    if (USE_NATIVE) {
	        return readOneLineIntFile(
	            "/sys/class/power_supply/battery/capacity", -1);
	        //return getBatteryLifePercent0();
	    } else {
	        return batteryLifePercent;
	    }
	}

	/**
	 * Returns the power plug status, i.e. if the device is plugged in.
	 * 
	 * @return <code>1</code> if the device is plugged in, <code>0</code> if
	 *         this device is not plugged in, a negative value if the status is
	 *         unknown
	 */
	@Override
	public int getPowerPlugStatus() {
	    if (USE_NATIVE) {
            int status = readOneLineIntFile(
                "/sys/class/power_supply/battery/charging_source", -1);
            if (status > 1) {
                status = 1;
            }
            return status;
	        //return getPowerPlugStatus0();
	    } else {
	        return powerPlugStatus;
	    }
	}

	/**
	 * Returns if the system is equipped with a battery.
	 * 
	 * @return <code>true</code> if there is a battery, <code>false</code> if
	 *         not (power plug only)
	 */
	@Override
	public boolean hasSystemBattery() {
	    if (USE_NATIVE) {
            int val = readOneLineIntFile(
                "/sys/class/power_supply/battery/present", 0);
            return 1 == val;
	        //return hasSystemBattery0();
	    } else {
	        return hasSystemBattery;
	    }
	}

	/**
	 * Returns the remaining battery life time in seconds.
	 * 
	 * @return the percentage of the remaining battery life time in seconds,
	 *         negative if this value is unknown
	 */
	@Override
	public int getBatteryLifeTime() {
	    if (USE_NATIVE) {
	        return getBatteryLifeTime0();
	    } else {
	        return batteryLifeTime;    
	    }
	}

	// Setter
	private void setBatteryLifePercent(int batteryLifePercent) {
		this.batteryLifePercent = batteryLifePercent;
	}

	private void setPowerPlugStatus(int powerPlugStatus) {
		this.powerPlugStatus = powerPlugStatus;
	}

	private void setHasSystemBattery(boolean hasSystemBattery) {
		this.hasSystemBattery = hasSystemBattery;
	}

	private void setBatteryLifeTime(int batteryLifeTime) {
		this.batteryLifeTime = batteryLifeTime;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
	    if (!USE_NATIVE) { 
    		int scale = -1;
    		int level = -1;
    		int plug = -1;
    		// aktualisiere BatteryDataGatherer-Werte...
    		setHasSystemBattery(intent.getBooleanExtra(
    				BatteryManager.EXTRA_PRESENT, true));
    
    		plug = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
    
    		// 0 --> not plugged in / device on battery
    		// 1 --> plugged in
    		// -1 --> unknown
    		if (plug > 0) {
    			plug = 1;
    		} else if (plug < 0) {
    			plug = -1;
    		}
    
    		setPowerPlugStatus(plug);
    
    		level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
    		scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
    
    		if (level >= 0) {
    			setBatteryLifePercent(scale / 100 * level);
    		} else {
    			setBatteryLifePercent(-1);
    		}
	    }
	}
}
