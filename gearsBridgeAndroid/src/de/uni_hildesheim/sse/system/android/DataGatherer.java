package de.uni_hildesheim.sse.system.android;

import de.uni_hildesheim.sse.system.AccessPointData;
import de.uni_hildesheim.sse.system.IDataGatherer;

import java.util.List;
import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;

public class DataGatherer implements IDataGatherer {

	private Context context = null;

	/**
	 * Gathers WiFi signal measurements using the external native library.
	 * 
	 * @param timeout
	 *            in milli seconds when data gathering should be stopped if no
	 *            data was provided by the operating system
	 * @return access point data (or an empty array)
	 */
	@Override
	public AccessPointData[] gatherWifiSignals(int timeout) {
		// TODO timeout wird aktuell nicht verwendet
		List<ScanResult> scanResults = null;

		if (context != null) {
			WifiManager wifiManager = (WifiManager) context
					.getSystemService(Context.WIFI_SERVICE);
			// falls Wifi aktiviert
			if (wifiManager.isWifiEnabled()) {
				wifiManager.startScan();
				scanResults = wifiManager.getScanResults();
			}
		}

		if (scanResults != null) {
			int resultCount = scanResults.size();
			AccessPointData[] results = new AccessPointData[resultCount];
			for (ScanResult sr : scanResults) {
				results[resultCount - 1] = new AccessPointData(sr.BSSID,
						sr.level, -1, sr.frequency, -1, sr.SSID);
				resultCount--;
			}
			return results;
		} else {
			// falls Wifi deaktiviert
			//Log.d("WIFI_TEST", "Wifi ist deaktiviert! Wifi muss "
			//		+ "aktiviert werden um die AccessPoints zu scannnen!");
			return new AccessPointData[0]; // empty Array;
		}
	}

	/**
	 * Returns if (memory) functions relying on JVMTI are supported.
	 * 
	 * @return <code>true</code> if native JVMTI support is available,
	 *         <code>false</code> else
	 */
	@Override
	public boolean supportsJVMTI() {
		// TODO muss ggf. nativ abgefragt werden
		return false;
	}

	/**
	 * Function for setting context.
	 * 
	 * @param context
	 */
	public void setContext(Context context) {
		this.context = context;
	}

    /**
     * Registers the thread id of this thread with the related native thread in 
     * order to provide time monitoring on thread level.
     * 
     * @param threadId the id of the Java thread to be registered
     * @param register register this thread if <code>true</code>, unregister
     *   it if <code>false</code>
     * 
     * @since 1.00
     */
    private static native void registerThread0(long threadId, 
        boolean register);

    /**
     * Registers the thread id of this thread with the related native thread in 
     * order to provide time monitoring on thread level.
     * This method should only be implemented in case that JMX is not supported
     * by the VM, e.g. on Android. This method provides functionality only if 
     * {@link #needsThreadRegistration()} returns <code>true</code>.
     * 
     * @param register register this thread if <code>true</code>, unregister
     *   it if <code>false</code>
     * 
     * @since 1.00
     */
    @Override
    public void registerThisThread(boolean register) {
        registerThread0(Thread.currentThread().getId(), register);
    }
    
    /**
     * Returns weather the underlying (JVM) implementation needs to register
     * SUM threads with native threads. The background for this functionality
     * is that time monitoring of thread execution time is only provided
     * as JMX functionality which is not provided in all JVMs, e.g. prior to 
     * 1.4 or in Android. 
     * 
     * @return <code>true</code> if thread registration is required, 
     *   <code>false</code> if not (default)
     * 
     * @since 1.00
     */
    @Override
    public boolean needsThreadRegistration() {
        return true;
    }

    /**
     * Redefines the given class. However, this method is only functional
     * if {@link #supportsJVMTI()}.
     * 
     * @param clazz the class to be redefined
     * @param bytecode the new bytecode related to <code>class</code>
     * @return the related JVMTI error code, negative is no attempt, 
     *   0 is successful
     * 
     * @since 1.00
     */
    public int redefineClass(Class<?> clazz, byte[] bytecode) {
        return -1;
    }

    /**
     * Redefines the given classes one-by-one. However, this method is only 
     * functional if {@link #supportsJVMTI()}.
     * 
     * @param classes the classes to be redefined
     * @param bytecode the new bytecode related to <code>classes</code>
     * @return the related JVMTI error code, negative is no attempt, 
     *   0 is successful
     * 
     * @since 1.00
     */
    @Override
    public int redefineClasses(Class<?>[] classes, byte[][] bytecode) {
        return -1;
    }

    /**
     * Redefines the given classes at once (interdependencies). However, this 
     * method is only functional if {@link #supportsJVMTI()}.
     * 
     * @param classes the classes to be redefined
     * @param bytecode the new bytecode related to <code>classes</code>
     * @return the related JVMTI error code, negative is no attempt, 
     *   0 is successful
     * 
     * @since 1.00
     */
    @Override
    public int redefineMultiClasses(Class<?>[] classes, byte[][] bytecode) {
        return -1;
    }

}
