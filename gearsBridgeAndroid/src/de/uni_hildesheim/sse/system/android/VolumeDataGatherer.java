package de.uni_hildesheim.sse.system.android;

import de.uni_hildesheim.sse.system.IVolumeDataGatherer;
import android.os.Environment;
import android.os.StatFs;

public class VolumeDataGatherer implements IVolumeDataGatherer {

	/**
	 * Funktion prüft, ob ein externer Speicher vorhanden ist. TRUE falls
	 * vorhanden, sonst FALSE;
	 * 
	 * @return TRUE für vorhandenen externen Speicher, sonst FALSE;
	 */
	public boolean externalVolumeAvailable() {
		return android.os.Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED);
	}

	/**
	 * Returns the currently available volume capacity.
	 * 
	 * @return the currently available volume capacity in bytes
	 */
	@Override
	public long getCurrentVolumeAvail() {
		if (externalVolumeAvailable()) {
			StatFs stat = new StatFs(Environment.getExternalStorageDirectory()
					.getPath());
			long tmpBlockSize = stat.getBlockSize();
			long tmpAvailable = stat.getAvailableBlocks();

			return tmpBlockSize * tmpAvailable;
		} else {
			// falls kein externer Speicher vorhanden
			return -1;
		}
	}

	/**
	 * Returns the currently used volume capacity.
	 * 
	 * @return the currently used volume capacity in bytes
	 */
	@Override
	public long getCurrentVolumeUse() {
		if (externalVolumeAvailable()) {
			StatFs stat = new StatFs(Environment.getExternalStorageDirectory()
					.getPath());
			long tmpBlockSize = stat.getBlockSize();
			long tmpCapacity = stat.getBlockCount();
			long tmpAvailable = stat.getAvailableBlocks();

			long currentVolumeAvail = tmpBlockSize * tmpAvailable;
			long volumeCapacity = tmpBlockSize * tmpCapacity;
			return volumeCapacity - currentVolumeAvail;
		} else {
			// falls kein externer Speicher vorhanden
			return -1;
		}
	}

	/**
	 * Returns the maximum volume capacity.
	 * 
	 * @return the maximum volume capacity in bytes
	 */
	@Override
	public long getVolumeCapacity() {
		if (externalVolumeAvailable()) {
			StatFs stat = new StatFs(Environment.getExternalStorageDirectory()
					.getPath());
			long tmpBlockSize = stat.getBlockSize();
			long tmpCapacity = stat.getBlockCount();

			return tmpBlockSize * tmpCapacity;
		} else {
			// falls kein externer Speicher vorhanden
			return -1;
		}
	}

}
