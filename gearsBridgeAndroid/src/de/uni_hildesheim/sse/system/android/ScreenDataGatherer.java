package de.uni_hildesheim.sse.system.android;

import de.uni_hildesheim.sse.system.IScreenDataGatherer;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.WindowManager;

public class ScreenDataGatherer implements IScreenDataGatherer {

	private Context context = null;

	/**
	 * Returns the physical screen height.
	 * 
	 * @return the physical screen height (in pixel) (negative or zero if
	 *         invalid)
	 */
	@Override
	public int getScreenHeight() {
		DisplayMetrics dm = null;
		if (context != null) {
			dm = new DisplayMetrics();
			WindowManager window = (WindowManager) context
					.getSystemService(Context.WINDOW_SERVICE);
			window.getDefaultDisplay().getMetrics(dm);
		}
		
		if(dm != null) {
			return dm.heightPixels;
		} else {
			return -1;
		}
	}

	/**
	 * Returns the physical screen width.
	 * 
	 * @return the physical screen width (in pixel) (negative or zero if
	 *         invalid)
	 */
	@Override
	public int getScreenWidth() {
		DisplayMetrics dm = null;
		if (context != null) {
			dm = new DisplayMetrics();
			WindowManager window = (WindowManager) context
					.getSystemService(Context.WINDOW_SERVICE);
			window.getDefaultDisplay().getMetrics(dm);
		}
		
		if(dm != null) {
			return dm.widthPixels;
		} else {
			return -1;
		}
	}

	/**
	 * Returns the physical screen resolution.
	 * 
	 * @return the physical screen resolution (in dpi) (negative or zero if
	 *         invalid)
	 */
	@Override
	public int getScreenResolution() {
		DisplayMetrics dm = null;
		if (context != null) {
			dm = new DisplayMetrics();
			WindowManager window = (WindowManager) context
					.getSystemService(Context.WINDOW_SERVICE);
			window.getDefaultDisplay().getMetrics(dm);
		}
		
		if(dm != null) {
			return dm.densityDpi;
		} else {
			return -1;
		}
	}

	/**
	 * Function for setting context.
	 * 
	 * @param context
	 */
	public void setContext(Context context) {
		this.context = context;
	}

}
