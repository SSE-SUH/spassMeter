package de.uni_hildesheim.sse.system.android;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import de.uni_hildesheim.sse.system.IBatteryDataGatherer;
import de.uni_hildesheim.sse.system.IDataGatherer;
import de.uni_hildesheim.sse.system.IMemoryDataGatherer;
import de.uni_hildesheim.sse.system.INetworkDataGatherer;
import de.uni_hildesheim.sse.system.IProcessDataGatherer;
import de.uni_hildesheim.sse.system.IProcessorDataGatherer;
import de.uni_hildesheim.sse.system.IScreenDataGatherer;
import de.uni_hildesheim.sse.system.IThisProcessDataGatherer;
import de.uni_hildesheim.sse.system.IThreadDataGatherer;
import de.uni_hildesheim.sse.system.IVolumeDataGatherer;

public class GathererFactory extends
		de.uni_hildesheim.sse.system.GathererFactory {

	private Context context = null;

	private BatteryDataGatherer batteryDataGatherer = null;
	private ScreenDataGatherer screenDataGatherer = null;
	private MemoryDataGatherer memoryDataGatherer = null;
	private NetworkDataGatherer networkDataGatherer = null;
	private DataGatherer dataGatherer = null;

	private ServiceConnection serviceCon = new ServiceConnection() {
		private MyService service = null;

		public void onServiceConnected(ComponentName className, IBinder binder) {
			service = ((MyService.MyBinder) binder).getService();
		}

		public void onServiceDisconnected(ComponentName className) {
			if (service != null) {
				service = null;
			}
		}
	};

	@SuppressWarnings("unused")
    @Override
	protected IBatteryDataGatherer createBatteryDataGatherer() {
		batteryDataGatherer = new BatteryDataGatherer();
		if (!BatteryDataGatherer.USE_NATIVE && context != null) {
			IntentFilter filter = new IntentFilter(
					Intent.ACTION_BATTERY_CHANGED);
			context.registerReceiver(batteryDataGatherer, filter);
		}
		return batteryDataGatherer;
	}

	@Override
	protected IDataGatherer createDataGatherer() {
		dataGatherer = new DataGatherer();
		if (context != null) {
			dataGatherer.setContext(context);
		}
		return dataGatherer;
	}

	@Override
	protected IMemoryDataGatherer createMemoryDataGatherer() {
		memoryDataGatherer = new MemoryDataGatherer();
		if (context != null) {
			memoryDataGatherer.setContext(context);
		}
		return memoryDataGatherer;
	}

	@Override
	protected INetworkDataGatherer createNetworkDataGatherer() {
		networkDataGatherer = new NetworkDataGatherer();
		if (context != null) {
			networkDataGatherer.setContext(context);
		}
		return networkDataGatherer;
	}

	@Override
	protected IProcessDataGatherer createProcessDataGatherer() {
		return new ProcessDataGatherer();
	}

	@Override
	protected IProcessorDataGatherer createProcessorDataGatherer() {
		return new ProcessorDataGatherer();
	}

	@Override
	protected IScreenDataGatherer createScreenDataGatherer() {
		screenDataGatherer = new ScreenDataGatherer();
		if (context != null) {
			screenDataGatherer.setContext(context);
		}
		return screenDataGatherer;
	}

	@Override
	protected IThisProcessDataGatherer createThisProcessDataGatherer() {
		return new ThisProcessDataGatherer();
	}

	@Override
	protected IVolumeDataGatherer createVolumeDataGatherer() {
		return new VolumeDataGatherer();
	}

	@Override
	public void initialize() {
		if (null == System.getProperty(PROPERTY_EXTERNAL_LIB)) {
			System.loadLibrary("locutor");
			System.setProperty(PROPERTY_EXTERNAL_LIB, "locutor");
		}
	}

	@SuppressWarnings("unused")
    @Override
	public void setContext(Object obj) {
		if (obj instanceof Context) {
			context = (Context) obj;
			context.bindService(new Intent(context, MyService.class),
					serviceCon, Context.BIND_AUTO_CREATE);
			if (screenDataGatherer != null) {
				screenDataGatherer.setContext(context);
			}
			if (networkDataGatherer != null) {
				networkDataGatherer.setContext(context);
			}
			if (memoryDataGatherer != null) {
				memoryDataGatherer.setContext(context);
			}
			if (dataGatherer != null) {
				dataGatherer.setContext(context);
			}
			if (!BatteryDataGatherer.USE_NATIVE && batteryDataGatherer != null) {
				IntentFilter filter = new IntentFilter(
						Intent.ACTION_BATTERY_CHANGED);
				context.registerReceiver(batteryDataGatherer, filter);
			}
		}
		// für unbindService
		if (obj == null && context != null && serviceCon != null) {
			context.unbindService(serviceCon);

			if (screenDataGatherer != null) {
				screenDataGatherer.setContext(null);
			}
			if (networkDataGatherer != null) {
				networkDataGatherer.setContext(null);
			}
			if (memoryDataGatherer != null) {
				memoryDataGatherer.setContext(null);
			}
			if (dataGatherer != null) {
				dataGatherer.setContext(null);
			}
			if (!BatteryDataGatherer.USE_NATIVE 
			    && batteryDataGatherer != null) {
				context.unregisterReceiver(batteryDataGatherer);
			}
		}
	}

    @Override
    protected IThreadDataGatherer createThreadDataGatherer() {
        return new ThreadDataGatherer();
    }
}
