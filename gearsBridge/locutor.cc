#include <stdio.h>
#include "gears/geolocation/device_data_provider.h"
#include "data_gatherer.h"
#include <iostream>
#include <conio.h>

typedef std::set<AccessPointData, AccessPointDataLess> AccessPointDataSet;

int main(void) {
	WifiData wifiData = getWifiData(1000);
getCurrentProcessProcessorLoad();

	if (0 == wifiData.access_point_data.size()) {
		printf("WiFi not enabled or no access points detected");
	} else {
		printf("access points:");
	}
	for (AccessPointDataSet::const_iterator iter = wifiData.access_point_data.begin();
         iter != wifiData.access_point_data.end();
         iter++) {
		getCurrentProcessProcessorLoad();

		 AccessPointData data = *iter;
    	std::string16 ssid = data.ssid;
    	std::string16 mac = data.mac_address;
		#if defined(WIN32)
    	printf("strength: %i noise: %i channel: %i ssid: %ls mac: %ls\n",data.radio_signal_strength, data.signal_to_noise, data.channel, ssid.c_str(), mac.c_str());
		#else
		#endif
    }
    printf("\nhardware:\n");
	
    getCurrentProcessProcessorLoad();

	#if defined(WIN32)
    printf(" - screen width: %i screen height: %i resolution: %i\n", getScreenWidth(), getScreenHeight(), getScreenResolution());
    printf(" - avail memory: %I64d used memory: %I64d\n", getMemoryCapacity(), getCurrentMemoryUse());
    printf(" - processor speed: in use %I64d max speed %I64d number of processors: %i load %.2f\n", getCurrentProcessorSpeed(), getMaxProcessorSpeed(), getNumberOfProcessors(), getCurrentSystemLoad());
    printf(" - network speed: in use %I64d max %I64d utilization %.02g\n", getCurrentNetSpeed(), getMaxNetSpeed(), getNetUtilization());
    printf(" - volumes : in use %I64d max %I64d \n", getCurrentVolumeUse(), getVolumeCapacity());
	#else
    printf(" - screen width: %i screen height: %i resolution: %i\n", getScreenWidth(), getScreenHeight(), getScreenResolution());
    printf(" - avail memory: %lld used memory: %lld\n", getMemoryCapacity(), getCurrentMemoryUse());
    printf(" - processor speed: in use %llu max speed %llu number of processors: %i load %.2f\n", getCurrentProcessorSpeed(), getMaxProcessorSpeed(), getNumberOfProcessors(), getCurrentSystemLoad());
    printf(" - network speed: in use %lld max %lld utilization %.2f\n", getCurrentNetSpeed(), getMaxNetSpeed(), getNetUtilization());
    printf(" - volumes : in use %lld max %lld \n", getCurrentVolumeUse(), getVolumeCapacity());
	#endif

    if (hasSystemBattery()) {
        printf(" - battery status: life percent %i life time %i plug %i\n", getBatteryLifePercent(), getBatteryLifeTime(), getPowerPlugStatus());
    } else {
    	printf(" - no battery\n");
    }
    getCurrentProcessProcessorLoad();
	printf("\nproc: %u\n", GetCurrentProcessId());
	
	IoStatistics stat = getCurrentProcessIo();
	printf(" - curIoRead: %lld\n", stat.read); // %u in windows
	printf(" - curIoWrite: %lld\n", stat.write);
	printf(" - curMem: %lld\n", getCurrentProcessMemoryUse());
	printf(" - curUser: %lld\n", getCurrentProcessUserTimeTicks()); 
	printf(" - curKernel: %lld\n", getCurrentProcessKernelTimeTicks()); 
	printf(" - curSystem: %lld\n", getCurrentProcessSystemTimeTicks());
	double load = getCurrentSystemLoad();
	printf(" - load proc %.2f system %.2f\n", getCurrentProcessProcessorLoad(), load);
	stat = getAllProcessesIo();
	printf(" - allIoRead: %lld\n", stat.read);
	printf(" - allIoWrite: %lld\n", stat.write);

    #if defined(WIN32)
    while (!_kbhit())
    {
        Sleep(1000);
        //printf(" - network speed: utilization %.02g\n", getNetUtilization());
        printf(" - network speed: in use %I64d max %I64d utilization %.02g\n", getCurrentNetSpeed(), getMaxNetSpeed(), getNetUtilization());
    }
    #endif

    return 0;
}
