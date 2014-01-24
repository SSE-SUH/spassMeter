
#ifndef DATA_GATHERER_H_
#define DATA_GATHERER_H_

#ifdef ANDROID
#include "android_defs.h"
#endif

#ifdef VAR_WIFI_DATA
#include "gears/stopwatch.h"  // For GetCurrentTimeMillis
#include "gears/device_data_provider.h"

typedef std::set<AccessPointData, AccessPointDataLess> AccessPointDataSet;
#else
#include "gears/basictypes.h"
#endif

#ifdef VAR_WIFI_DATA
class DeviceListener :
	public WifiDataProvider::ListenerInterface {
 public:
	~DeviceListener() {}
    void DeviceDataUpdateAvailable(
        DeviceDataProvider<WifiData> *provider) {
    	// could also be used...
    }
};
#endif

// ----------------------- new functions ------------------------

struct IoStatistics {
    uint64 read;
	uint64 write;
};

// screen
#ifdef VAR_SCREEN_DATA
int getScreenWidth(); // in pixel
int getScreenHeight(); // in pixel
int getScreenResolution(); // in dpi
#endif

// main memory
#ifdef VAR_MEMORY_DATA
uint64 getMemoryCapacity(); // in bytes
uint64 getCurrentMemoryAvail(); // in bytes
uint64 getCurrentMemoryUse(); // in bytes
#endif

// processor 
#ifdef VAR_PROCESSOR_DATA
int getNumberOfProcessors(); // count
uint64 getMaxProcessorSpeed(); // in hertz
uint64 getCurrentProcessorSpeed(); // in hertz
double getCurrentSystemLoad(); // in percent
#endif

// volumes
#ifdef VAR_VOLUME_DATA
uint64 getVolumeCapacity(); // in bytes
uint64 getCurrentVolumeAvail(); // in bytes
uint64 getCurrentVolumeUse(); // in bytes
#endif

// network

#ifdef VAR_NETWORK_DATA
uint64 getCurrentNetSpeed(); // in bit / sec
uint64 getMaxNetSpeed(); // in bit / sec
double getNetUtilization(); // in percent
#endif

#ifdef VAR_ENERGY_DATA
// power
bool hasSystemBattery();
int getBatteryLifePercent(); // percent or negative if not available
int getBatteryLifeTime(); // seconds or negative if not available
int getPowerPlugStatus(); // 1=plugged, 0=unplugged, negative if not available
#endif

// this process

#ifdef VAR_CURRENT_PROCESS_DATA
int getCurrentProcessId();
#ifdef VAR_IO_DATA
IoStatistics getCurrentProcessIo(); // bytes
#endif
#ifdef VAR_MEMORY_DATA
uint64 getCurrentProcessMemoryUse(); // bytes
#endif
#ifdef VAR_TIME_DATA
uint64 getCurrentProcessUserTimeTicks(); // amount in nanoseconds
uint64 getCurrentProcessKernelTimeTicks(); // amount in nanoseconds
uint64 getCurrentProcessSystemTimeTicks(); // amount in nanoseconds
uint64 getCurrentProcessCycleTimeTicks(); // amount in nanoseconds, may be 0 if not supported (since vista/server 2008)
#endif
#ifdef VAR_LOAD_DATA
double getCurrentProcessProcessorLoad();
#endif
#endif

/*#ifdef VAR_DEBUG
int getLastLoadError();
#endif*/

// arbitrary process

#ifdef VAR_ARBITRARY_PROCESS_DATA
bool isProcessAlive(const int id);
#ifdef VAR_IO_DATA
IoStatistics getProcessIo(const int id); // bytes
#endif
#ifdef VAR_MEMORY_DATA
uint64 getProcessMemoryUse(const int id); // bytes
#endif
#ifdef VAR_TIME_DATA
uint64 getProcessUserTimeTicks(const int id); // amount in nanoseconds
uint64 getProcessKernelTimeTicks(const int id); // amount in nanoseconds
uint64 getProcessSystemTimeTicks(const int id); // amount in nanoseconds
#endif
#ifdef VAR_LOAD_DATA
double getProcessProcessorLoad(const int id);
#endif
#endif

// all processes

#if defined(VAR_ALL_PROCESSES_DATA) && defined(VAR_IO_DATA)
IoStatistics getAllProcessesIo(); // bytes
#if defined(VAR_ARBITRARY_PROCESS_DATA) || defined(VAR_CURRENT_PROCESS_DATA)
bool isNetworkIoDataIncluded(bool forAll);
bool isFileIoDataIncluded(bool forAll);
#endif
#endif

// native thread timing

#ifdef ANDROID
bool supportsCpuThreadTiming();
long getCpuThreadTime(long threadId);
void registerThread(long threadId, bool reg);
#endif

// ---------------------------------

#ifdef VAR_WIFI_DATA
WifiData getWifiData(int64 timeout);
#endif

void done();
void init();

#endif /* DATA_GATHERER_H_ */
