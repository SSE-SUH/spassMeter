package de.uni_hildesheim.sse.system.android;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiInfo;
import de.uni_hildesheim.sse.codeEraser.annotations.Variability;
import de.uni_hildesheim.sse.system.AnnotationConstants;
import de.uni_hildesheim.sse.system.INetworkDataGatherer;

@Variability(id = AnnotationConstants.VAR_NETWORK_DATA)
public class NetworkDataGatherer implements INetworkDataGatherer {

    /**
     * Stores the android context.
     */
	private Context context = null;
	
	/**
	 * Stores the last refesh time (needed when App is sleeping).
	 */
	private long lastTimestamp = -1;
	
	/**
	 * Stores the last current network usage.
	 */
	private long lastCurrent = -1;
	
	/**
	 * Stores the current maximum network speed.
	 */
	private long maxNetSpeed = 0;
	
	/**
	 * Stores the current network speed.
	 */
	private long curNetSpeed = 0;
	
	/**
	 * Stores the timer for regular updates.
	 */
	private Timer timer = new Timer();

	/**
	 * Updates the values.
	 * 
	 * @author Holger Eichelberger
	 * @since 1.00
	 * @version 1.00
	 */
	private class Task extends TimerTask {

	    /**
	     * Executes the update action.
	     */
        @Override
        public void run() {
            updateValues();
        }
	    
	}

	/**
	 * Creates the network data gatherer and starts a regular update task.
	 * 
	 * @since 1.00
	 */
	public NetworkDataGatherer() {
	    updateValues();
	    timer.schedule(new Task(), 1000, 1000);
	}
	
	// This is a rather initial implementation restricted to WiFi, not passing
	// through network via USB from PC or bluetooth...
	
	/**
	 * Returns the (average) available speed of the currently enabled network
	 * device(s).
	 * 
	 * @return the current speed of the network in Bit per second (negative or
	 *         zero if invalid)
	 */
	@Override
    @Variability(id = AnnotationConstants.VAR_NETWORK_DATA, value = "0")
	public long getCurrentNetSpeed() {
	    return curNetSpeed;
	}
	
	/**
	 * Updates the values as current network use must regularly be updated
	 * (bit/sec).
	 * 
	 * @since 1.00
	 */
	private void updateValues() {
	    long now = System.currentTimeMillis();
	    if (lastTimestamp > 0) {
	        if (now - lastTimestamp > 20000) {
	            // if too old force refresh
	            lastTimestamp = -1;
	            lastCurrent = -1;
	        }
	    }
	    
        WifiInfo wi = null;
        if (context != null) {
            WifiManager wm = (WifiManager) context
                .getSystemService(Context.WIFI_SERVICE);
            if (wm.isWifiEnabled()) {
                wi = wm.getConnectionInfo();
            }
        }

        // BSSID is null if not connected, similarly getNetworkId() == -1
        if (wi != null && null != wi.getBSSID() && wi.getLinkSpeed() >= 0) {
            maxNetSpeed = wi.getLinkSpeed() * 1024 * 1024;

            File sysClassNet = new File("/sys/class/net");
            if (sysClassNet.exists()) {
                String mac = wi.getMacAddress();
                // first shot - it might be eth0
                long cur = getCurrent(new File(sysClassNet, "eth0"), mac);
                if (cur < 0) {
                    // if not, try all and stop at the first matching
                    File[] devices = sysClassNet.listFiles();
                    if (null != devices) {
                        // cave - is null if it does not exist
                        for (int d = 0; cur < 0 && d < devices.length; d++) {
                            cur = getCurrent(devices[d], mac);
                        }
                    }
                }
                long result = Math.max(0, cur * 8);
                if (lastCurrent < 0) {
                    // no last current, then this may be the start of the 
                    // gatherer or after a long time of inactivity - we do not
                    // have a numberOfBytes/second
                    lastCurrent = result;
                    result = 0;
                } else {
                    // we have a last and a current value - the result is the 
                    // difference (for 1 second)
                    long last = lastCurrent;
                    lastCurrent = result;
                    result = result - last;
                }
                // set time stam and mark lastCurrent as active
                lastTimestamp = now;
                curNetSpeed = result;
            } 
        }
	}

	/**
	 * Read a one line file (as it is common in unix proc and sys filesystems).
	 * 
	 * @param file the file to read
	 * @return the contents of the file or <b>null</b> if the file was not found
	 *    or any access error occurred
	 * 
	 * @since 1.00
	 */
	private static String readFile(File file) {
	    String result = null;
	    try {
	        LineNumberReader lnr = new LineNumberReader(new FileReader(file));
	        result = lnr.readLine();
	        lnr.close();
	    } catch (IOException ioe) {
	    }
	    return result;
	}
	
	/**
	 * Returns the current network usage for a network device.
	 * 
	 * @param netDeviceDir the directory of the device to be considered
	 * @param mac the MAC address of interest (scan criterion)
	 * @return the current network usage or <code>-1</code> if the related
	 *    proc/sys files were not found or the <code>mac</code> address did
	 *    not match <code>netDeviceDir</code>
	 * 
	 * @since 1.00
	 */
	private static long getCurrent(File netDeviceDir, String mac) {
	    long result = -1;
        File address = new File(netDeviceDir, "address");
        if (address.exists()) {
            String fileMac = readFile(address);
            if (mac.equalsIgnoreCase(fileMac)) {
                result = 0;
                File statistics = new File(netDeviceDir, "statistics");
                String tmp = readFile(new File(statistics, "rx_bytes"));
                if (null != tmp) {
                    try {
                        result += Long.parseLong(tmp);
                    } catch (NumberFormatException e) {
                    }
                }
                tmp = readFile(new File(statistics, "tx_bytes"));
                if (null != tmp) {
                    try {
                        result += Long.parseLong(tmp);
                    } catch (NumberFormatException e) {
                    }
                }
            }
        }
        return result;
	}
	
	/**
	 * Returns the maximum speed of the currently enabled network device(s).
	 * 
	 * @return the maximum speed of the network in Bit per second (negative or
	 *         zero if invalid)
	 */
	@Override
    @Variability(id = AnnotationConstants.VAR_NETWORK_DATA, value = "0")
	public long getMaxNetSpeed() {
	    return maxNetSpeed;
	}
	
	/**
	 * Function for setting context.
	 * @param context
	 */
	public void setContext(Context context) {
		this.context = context;
	}
}
