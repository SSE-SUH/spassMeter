package de.uni_hildesheim.sse.system.android;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class MyService extends Service {

	private final IBinder mBinder = new MyBinder();

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	@Override
	public void onCreate() {
		super.onCreate();

		//Log.w("SERVICE_TEST", "Service OnCreate()");
		
		// TODO entfernen!
		// für BatteryDataGatherer
		// IntentFilter filter = new IntentFilter(
		// Intent.ACTION_BATTERY_CHANGED);
		// registerReceiver(batteryInfoReceiver, filter);

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		//Log.w("SERVICE_TEST", "Service OnDestroy()");
		// TODO entfernen!
		// unregisterReceiver(batteryInfoReceiver);
	}

	public class MyBinder extends Binder {
		public MyService getService() {
			return MyService.this;
		}
	}

}
