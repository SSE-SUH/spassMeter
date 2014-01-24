#include <stdio.h>
#include "data_gatherer.h"
#ifdef VAR_WIFI_DATA
#include "gears/stopwatch.h"  // For GetCurrentTimeMillis
#include "gears/device_data_provider.h"
#endif

#ifdef VAR_WIFI_DATA
void gatherWifiData(int64 timeout, WifiData *wifiData) {

	if (timeout <=0) {
		timeout = 1000;
	}

	DeviceListener *listener = new DeviceListener();
	WifiDataProvider *wifi_data_provider = WifiDataProvider::Register(listener);

	bool is_wifi_data_complete = false;
    int64 start_time = GetCurrentTimeMillis();
	do {
	    is_wifi_data_complete = wifi_data_provider->GetData(wifiData);
	} while (!is_wifi_data_complete && GetCurrentTimeMillis() - start_time < timeout);

    wifi_data_provider->Unregister(listener);
}

WifiData getWifiData(int64 timeout) {
    WifiData data;
	gatherWifiData(timeout, &data);
	return data;
}
#endif
