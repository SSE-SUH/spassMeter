package test;

import de.uni_hildesheim.sse.system.AccessPointData;
import de.uni_hildesheim.sse.system.GathererFactory;
import de.uni_hildesheim.sse.system.IBatteryDataGatherer;
import de.uni_hildesheim.sse.system.IDataGatherer;
import de.uni_hildesheim.sse.system.IMemoryDataGatherer;
import de.uni_hildesheim.sse.system.INetworkDataGatherer;
import de.uni_hildesheim.sse.system.IProcessorDataGatherer;
import de.uni_hildesheim.sse.system.IScreenDataGatherer;
import de.uni_hildesheim.sse.system.IThisProcessDataGatherer;
import de.uni_hildesheim.sse.system.IVolumeDataGatherer;
import de.uni_hildesheim.sse.system.IoStatistics;
import de.uni_hildesheim.sse.system.android.R;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class Test extends Activity implements OnClickListener {
	private Button button;
	private LinearLayout accessPoints;

	private TextView bdg_batteryLifePercent;
	private TextView bdg_batteryLifeTime;
	private TextView bdg_powerPlugStatus;
	private TextView bdg_hasSystemBattery;

	private TextView dg_gatherWifiSignals;

	private TextView mdg_currentMemAvail;
	private TextView mdg_currentMemUse;
	private TextView mdg_currentMemCapacity;
	private TextView mdg_ObjectSize;

	private TextView ndg_currentNetSpeed;
	private TextView ndg_maxNetSpeed;
    private TextView ndg_netUtilization;

	private TextView tpdg_allProcessIo;
	private TextView tpdg_currentProcessID;
	private TextView tpdg_currentProcessIo;
	private TextView tpdg_currentProcessKernelTimeTicks;
	private TextView tpdg_currentProcessMemoryUse;
	private TextView tpdg_currentProcessProcessorLoad;
	private TextView tpdg_currentProcessSystemTimeTicks;
	private TextView tpdg_currentProcessUserTimeTicks;

	// private TextView pdg_allProcessIo;
	// private TextView pdg_processIo;
	// private TextView pdg_currentProcessKernelTimeTicks;
	// private TextView pdg_currentProcessMemoryUse;
	// private TextView pdg_currentProcessProcessorLoad;
	// private TextView pdg_currentProcessSystemTimeTicks;
	// private TextView pdg_currentProcessUserTimeTicks;

	private TextView prdg_currentProcessorSpeed;
	private TextView prdg_currentSystemLoad;
	private TextView prdg_maxProcessorSpeed;
	private TextView prdg_numberOfProcessors;

	private TextView sdg_screenHeight;
	private TextView sdg_screenWidth;
	private TextView sdg_screenResolution;

	private TextView vdg_currentVolumeAvail;
	private TextView vdg_currentVolumeUse;
	private TextView vdg_volumeCapacity;

	/**
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		findViews();
		accessPoints = (LinearLayout) findViewById(R.id.accesspoints);
		button = (Button) findViewById(R.id.refreshBtn);
		button.setOnClickListener(this);
		GathererFactory.loadLibrary(this);
		setGathererData();
	}
	
	private String toString(IoStatistics stat, IThisProcessDataGatherer itpdg, 
	    boolean isAll) {
	    String included = "";
	    if (itpdg.isFileIoDataIncluded(isAll)) {
	        included += "+f";
	    } else {
	        included += "-f";
	    }
        if (itpdg.isNetworkIoDataIncluded(isAll)) {
            included += "+n";
        } else {
            included += "-n";
        }
	    return String.format("read %s write %s %s", 
	        format(stat.read, BYTE_UNIT), format(stat.write, BYTE_UNIT), 
	            included);
	}

	private void setGathererData() {
		IBatteryDataGatherer ibdg = GathererFactory.getBatteryDataGatherer();
		IDataGatherer idg = GathererFactory.getDataGatherer();
		IMemoryDataGatherer imdg = GathererFactory.getMemoryDataGatherer();
		INetworkDataGatherer indg = GathererFactory.getNetworkDataGatherer();
		IProcessorDataGatherer iprdg = GathererFactory
				.getProcessorDataGatherer();
		IThisProcessDataGatherer itpdg = GathererFactory
				.getThisProcessDataGatherer();
		IScreenDataGatherer isdg = GathererFactory.getScreenDataGatherer();
		IVolumeDataGatherer ivdg = GathererFactory.getVolumeDataGatherer();

		// BatteryDataGatherer
		bdg_batteryLifePercent.setText("BatteryLifePercent: "
				+ ibdg.getBatteryLifePercent());
		bdg_batteryLifeTime.setText("BatteryLifeTime: "
				+ ibdg.getBatteryLifeTime());
		bdg_powerPlugStatus.setText("powerPlugStatus: "
				+ ibdg.getPowerPlugStatus());
		bdg_hasSystemBattery.setText("hasSystemBattery: "
				+ ibdg.hasSystemBattery());

		// DataGatherer
		AccessPointData[] apd = idg.gatherWifiSignals(1000);
		dg_gatherWifiSignals.setText("gatherWifiSignals (" + apd.length + ")");
		accessPoints.removeAllViews();
		for (int i = 0; i < apd.length; i++) {
			TextView tv = new TextView(this);
			tv.setText("AccessPoint_" + i + ": " + apd[i].toString());
			accessPoints.addView(tv, i);
		}

		// MemoryDataGatherer
		mdg_currentMemAvail.setText("CurrentMemoryAvail: "
				+ format(imdg.getCurrentMemoryAvail(), BYTE_UNIT));
		mdg_currentMemUse.setText("CurrentMemoryUse: "
				+ format(imdg.getCurrentMemoryUse(), BYTE_UNIT));
		mdg_currentMemCapacity.setText("CurrentMemoryCapacity: "
				+ format(imdg.getMemoryCapacity(), BYTE_UNIT));
		mdg_ObjectSize.setText("ObjectSize: " + imdg.getObjectSize(this)); // TODO
		// this?

		// NetworkDataGatherer
		long curNet = indg.getCurrentNetSpeed();
		long maxNet = indg.getMaxNetSpeed();
		ndg_currentNetSpeed.setText("CurrentNetSpeed: "
				+ format(curNet, BYTE_UNIT) + "Bit") ;
		ndg_maxNetSpeed.setText("maxNetSpeed: " + format(maxNet, BYTE_UNIT) + "Bit");
		double netUtil = 0;
		if (maxNet > 0) {
		    netUtil = curNet / ((double)maxNet) * 100;
		}
		String tmp = String.format("%.2f", netUtil);
		ndg_netUtilization.setText("utilization: " + tmp + "%");

		// ThisProcessDataGatherer
		tpdg_allProcessIo.setText("allProcessIo: " 
		    + toString(itpdg.getAllProcessesIo(), itpdg, true)); 
		tpdg_currentProcessID.setText("CurrentProcessID: "
				+ itpdg.getCurrentProcessID());
		tpdg_currentProcessIo.setText("CurrentProcessIo: " 
		    + toString(itpdg.getCurrentProcessIo(), itpdg, false));
		tpdg_currentProcessKernelTimeTicks
				.setText("CurrentProcessKernelTimeTicks: "
						+ itpdg.getCurrentProcessKernelTimeTicks());
		tpdg_currentProcessMemoryUse.setText("CurrentProcessMemoryUse: "
				+ format(itpdg.getCurrentProcessMemoryUse(), BYTE_UNIT));
		tmp = String.format("CurrentProcessorLoad: %.2f", 
		    itpdg.getCurrentProcessProcessorLoad());
		tpdg_currentProcessProcessorLoad.setText(tmp);
		tpdg_currentProcessSystemTimeTicks
				.setText("CurrentProcessSystemTimeTicks: "
						+ itpdg.getCurrentProcessSystemTimeTicks());
		tpdg_currentProcessUserTimeTicks
				.setText("CurrentProcessUserTimeTicks: "
						+ itpdg.getCurrentProcessUserTimeTicks());

		// ProcessorDataGatherer
		prdg_currentProcessorSpeed.setText("currentProcessorSpeed*: "
				+ iprdg.getCurrentProcessorSpeed());
		tmp = String.format("%.2f", iprdg.getCurrentSystemLoad());
		prdg_currentSystemLoad.setText("currentSystemLoad: " + tmp);
		prdg_maxProcessorSpeed.setText("maxProcessorSpeed*: "
				+ iprdg.getMaxProcessorSpeed());
		prdg_numberOfProcessors.setText("numberOfProcessors*: "
				+ iprdg.getNumberOfProcessors());

		// ScreenDataGatherer
		sdg_screenHeight.setText("screenHeight: " + isdg.getScreenHeight());
		sdg_screenWidth.setText("screenWidth: " + isdg.getScreenWidth());
		sdg_screenResolution.setText("screenResolution: "
				+ isdg.getScreenResolution());

		// VolumeDataGatherer
		vdg_currentVolumeAvail.setText("currentVolumeAvail: "
				+ format(ivdg.getCurrentVolumeAvail(), BYTE_UNIT));
		vdg_currentVolumeUse.setText("currentVolumeUse: "
				+ format(ivdg.getCurrentVolumeUse(), BYTE_UNIT));
		vdg_volumeCapacity.setText("volumeCapacity: "
				+ format(ivdg.getVolumeCapacity(), BYTE_UNIT));

	}

	private void findViews() {
		bdg_batteryLifePercent = (TextView) findViewById(R.id.bdg_batteryLifePercent);
		bdg_batteryLifeTime = (TextView) findViewById(R.id.bdg_batteryLifeTime);
		bdg_powerPlugStatus = (TextView) findViewById(R.id.bdg_powerPlugStatus);
		bdg_hasSystemBattery = (TextView) findViewById(R.id.bdg_hasSystemBattery);

		dg_gatherWifiSignals = (TextView) findViewById(R.id.dg_gatherWifiSignals);

		mdg_currentMemAvail = (TextView) findViewById(R.id.mdg_currentMemAvail);
		mdg_currentMemUse = (TextView) findViewById(R.id.mdg_currentMemUse);
		mdg_currentMemCapacity = (TextView) findViewById(R.id.mdg_currentMemCapacity);
		mdg_ObjectSize = (TextView) findViewById(R.id.mdg_ObjectSize);

		ndg_currentNetSpeed = (TextView) findViewById(R.id.ndg_currentNetSpeed);
		ndg_maxNetSpeed = (TextView) findViewById(R.id.ndg_maxNetSpeed);
        ndg_netUtilization = (TextView) findViewById(R.id.ndg_netUtilization);

		tpdg_allProcessIo = (TextView) findViewById(R.id.tpdg_allProcessIo);
		tpdg_currentProcessID = (TextView) findViewById(R.id.tpdg_currentProcessID);
		tpdg_currentProcessIo = (TextView) findViewById(R.id.tpdg_currentProcessIo);
		tpdg_currentProcessKernelTimeTicks = (TextView) findViewById(R.id.tpdg_currentProcessKernelTimeTicks);
		tpdg_currentProcessMemoryUse = (TextView) findViewById(R.id.tpdg_currentProcessMemoryUse);
		tpdg_currentProcessProcessorLoad = (TextView) findViewById(R.id.tpdg_currentProcessProcessorLoad);
		tpdg_currentProcessSystemTimeTicks = (TextView) findViewById(R.id.tpdg_currentProcessSystemTimeTicks);
		tpdg_currentProcessUserTimeTicks = (TextView) findViewById(R.id.tpdg_currentProcessUserTimeTicks);

		prdg_currentProcessorSpeed = (TextView) findViewById(R.id.prdg_currentProcessorSpeed);
		prdg_currentSystemLoad = (TextView) findViewById(R.id.prdg_currentSystemLoad);
		prdg_maxProcessorSpeed = (TextView) findViewById(R.id.prdg_maxProcessorSpeed);
		prdg_numberOfProcessors = (TextView) findViewById(R.id.prdg_numberOfProcessors);

		sdg_screenHeight = (TextView) findViewById(R.id.sdg_screenHeight);
		sdg_screenWidth = (TextView) findViewById(R.id.sdg_screenWidth);
		sdg_screenResolution = (TextView) findViewById(R.id.sdg_screenResolution);

		vdg_currentVolumeAvail = (TextView) findViewById(R.id.vdg_currentVolumeAvail);
		vdg_currentVolumeUse = (TextView) findViewById(R.id.vdg_currentVolumeUse);
		vdg_volumeCapacity = (TextView) findViewById(R.id.vdg_volumeCapacity);

	}

	private static final int BYTE_UNIT = 1024;
	private static final String[] units = {"", "K", "M", "G", "T", "E"};
	
	private String format(long value, int radix) {
	    if (value < radix) {
	        return String.valueOf(value);
	    } else {
    	    double v = value;
    	    int pos = 0;
    	    while (pos < units.length && v >= radix) {
    	        pos++;
    	        v /= radix;
    	    }
    	    pos = Math.min(pos, units.length - 1);
    	    return String.format("%.2f%s", v, units[pos]);
	    }
	}

	@Override
	public void onClick(View v) {
		setGathererData();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		GathererFactory.changeContext(null);
	}
}
