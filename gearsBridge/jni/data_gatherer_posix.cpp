#include <stdlib.h>
#include <stdio.h>
#include <fcntl.h>
#include "hashmap.h"
#include "data_gatherer.h"
#include "sys/resource.h"
#include "sys/time.h"
#include "sys/sysinfo.h"
#include "sys/types.h"
#include "unistd.h"
#include "linux/hdreg.h"
#include <sys/ioctl.h>
#include <errno.h>

// unclear
// -file i/o android (propagate)
// -volume
// -network

#define PROC_PID_STAT  "/proc/%i/stat"
#define PROC_TID_STAT  "/proc/%i/task/%i/stat"
#define PROC_PID_IO    "/proc/%i/io"
#define PROC_PID_NET   "/proc/%i/net"
#define PROC_NET_DEV   "/proc/net/dev"
#define PROC_STAT      "/proc/stat"
#define PROC_DISKSTATS "/proc/diskstats"
#define DEV_DEVID      "/dev/%s"
#define SYS_DEVICES_SYSTEM_CPU_CPUID_CPUFREQ_CPUINFO_MAX_FREQ "/sys/devices/system/cpu/cpu%i/cpufreq/cpuinfo_max_freq"
#define SYS_DEVICES_SYSTEM_CPU_CPUID_CPUFREQ_SCALING_CUR_FREQ "/sys/devices/system/cpu/cpu%i/cpufreq/scaling_cur_freq"
#define SYS_CLASS_POWERSUPPLY_BATTERY_CAPACITY "/sys/class/power_supply/battery/capacity"
#define SYS_CLASS_POWERSUPPLY_BATTERY_PRESENT "/sys/class/power_supply/battery/present"
#define SYS_CLASS_POWERSUPPLY_BATTERY_CHARGING_SOURCE "/sys/class/power_supply/battery/charging_source"
#define PATTERN_STAT_USERTIME       "%*d %*s %*c %*d %*d %*d %*d %*d %*u %*u %*u %*u %*u %lu"
#define PATTERN_STAT_KERNELTIME     "%*d %*s %*c %*d %*d %*d %*d %*d %*u %*u %*u %*u %*u %*u %lu"
#define PATTERN_STAT_USERKERNELTIME "%*d %*s %*c %*d %*d %*d %*d %*d %*u %*u %*u %*u %*u %lu %lu"

#define PID2INT(a) a
#define MAX(a, b) (a > b ? a : b)
#define MIN(a, b) (a < b ? a : b)

#ifndef ANDROID
#include "procps-3.2.8/proc/readproc.h"
#include "lshw/src/core/cpuid.h"
#include "lshw/src/core/cpuinfo.h"
#include "lshw/src/core/cpufreq.h"
#include "lshw/src/core/hw.h"
#include "lshw/src/core/print.h"
 #ifdef osx
 #define LOG(t) printf("\%s\n", t);
 #else
  #ifdef VAR_HAS_UI
 #include <X11/Xlib.h>
  #endif
#if !defined(LOG)
 #define LOG(t) printf("\%s\n", t); // overwritten in common.h
#endif
 #endif
 #ifdef CLOCK_THREAD_CPUTIME_ID
 #define USE_CLOCK_FOR_THREAD_CPU 1
 #else
 #define USE_PROC_FOR_THREAD_CPU 1
 #endif
#else
#include "android/log.h"
//include "GLES2/gl2.h"
#include "sys/system_properties.h"
#include "android_defs.h"
#include <pthread.h>
#include <time.h>
#define LOG(t) __android_log_write(4, "Locutor", t);
#undef USE_CLOCK_FOR_THREAD_CPU
#undef USE_PROC_FOR_THREAD_CPU
#endif

FILE* _getIntFd(const char* namePattern, int pid) {
	char procname[BUFSIZ];
	sprintf(procname, namePattern, pid);
    return fopen(procname, "r");
}

int _readOneValueIntFile(const char* name, int dflt) {
	int result = dflt;
    //todo check for subdirectories such as BAT0
	FILE* fd = fopen(name, "r");
    if (NULL != fd) {
    	fscanf(fd, "%d", &result);
        fclose(fd);
    }
    return result;
}

void logFile(FILE* fd) {
	char buf[2000];
	char* res;
	do {
		res = fgets(buf, 2000, fd);
		if (NULL != res) {
			LOG(buf);
		}
	} while (NULL != res);
	fseek(fd, SEEK_SET, 0);
}

// screen
#ifdef VAR_SCREEN_DATA
int getScreenWidth() {
	int result = 0;
#ifdef VAR_HAS_UI
  #ifdef ANDROID
//		GLint m_viewport[2];
//		glGetIntegerv(GL_MAX_VIEWPORT_DIMS, m_viewport);
//		result = m_viewport[0];
  #else
#ifdef osx
	 Rect outAvailableRect;
	 GetAvailableWindowPositioningBounds ( GetMainDevice(), &outAvailableRect);
	 result = outAvailableRect.right - outAvailableRect.left;
#else
	 Display *display = XOpenDisplay(NULL);
	 result = DisplayWidth(display,0);
	 XCloseDisplay(display);
#endif
#endif
#endif
	return result;
}

int getScreenHeight() {
  int result = 0;
#ifdef VAR_HAS_UI
  #ifdef ANDROID
//		GLint m_viewport[2];
//		glGetIntegerv(GL_MAX_VIEWPORT_DIMS, m_viewport);
//		result = m_viewport[0];
  #else
	#ifdef osx
		 Rect outAvailableRect;
		 GetAvailableWindowPositioningBounds ( GetMainDevice(), &outAvailableRect);
		 //screenwidth = outAvailableRect.right - outAvailableRect.left;
		 result = screenheight = outAvailableRect.bottom - outAvailableRect.top;
	#else
		 Display *display = XOpenDisplay(NULL);
		 //screenwidth = DisplayWidth(display_name,screen);
		 result = DisplayHeight(display,0);
		 XCloseDisplay(display);
	#endif
  #endif
#endif
  return result;
}

int getScreenResolution() {
	int result;
#ifdef VAR_HAS_UI
#ifdef ANDROID
	result = 160;
	int tmp;
	char buf[PROP_VALUE_MAX];
	if (__system_property_get("qemu.sf.lcd_density", buf) > 0) {
		if (sscanf(buf, "%d", &tmp)) {
			result = tmp;
		}
	} else if (__system_property_get("ro.sf.lcd_density", buf) > 0) {
		if (sscanf(buf, "%d", &tmp)) {
			result = tmp;
		}
	}
#else
    #ifdef osx
	result = 0;
	#else
	    Display *display = XOpenDisplay(NULL);
	    int scr = 0;
		double xres = ((((double) DisplayWidth(display,scr)) * 25.4) /
	        ((double) DisplayWidthMM(display,scr)));
	    double yres = ((((double) DisplayHeight(display,scr)) * 25.4) /
	        ((double) DisplayHeightMM(display,scr)));
	    int xr = (int) (xres + 0.5);
	    int yr = (int) (yres + 0.5);
        result = MIN(xr, yr);
	    XCloseDisplay(display);
	#endif
#endif
#else
    result = 0;
#endif
	return result;
}
#endif

// processors
#ifdef VAR_PROCESSOR_DATA
int getNumberOfProcessors() {
#ifdef ANDROID
	int cpuId = 0;
	while (true) {
		FILE* fd = _getIntFd(SYS_DEVICES_SYSTEM_CPU_CPUID_CPUFREQ_CPUINFO_MAX_FREQ, cpuId);
		if (NULL != fd) {
	    	fclose(fd);
		} else {
			break;
		}
		cpuId++;
	};
	return MAX(1, cpuId);
#else
	return get_nprocs_conf();
#endif
}

uint64 getMaxProcessorSpeed() {
    unsigned long long result = 0;
#ifdef ANDROID
	int cpuId = 0;
	while (true) {
		FILE* fd = _getIntFd(SYS_DEVICES_SYSTEM_CPU_CPUID_CPUFREQ_CPUINFO_MAX_FREQ, cpuId);
		if (NULL != fd) {
			unsigned long long freq = 0;
			int res = fscanf(fd, "%llu", &freq);
			if (res > 0) {
				result = MAX(result, freq);
			}
	    	fclose(fd);
		} else {
			break;
		}
		cpuId++;
	};
#else
    hwNode computer("computer", hw::system);
	scan_cpuinfo(computer);
	scan_cpuid(computer);
	scan_cpufreq(computer);
	hwNode *core = computer.getChild("core");
	if (NULL != core) {
		int childs = core->countChildren();
		for (int c=0; c < childs; c++) {
			hwNode *cpu = core->getChild(c);
			unsigned long long capacity = cpu->getCapacity();
			if (0 == capacity) {
				// rule... if cpufreq is not available, then
				// the CPU is running on maximum frequency cpu->getSize()
				// checked by sudo dmidecode
				capacity = cpu->getSize();
			}
			if (capacity > result) {
				result = capacity;
			}
		}
	}
#endif
    return result;
}

uint64 getCurrentProcessorSpeed() {
	uint64 result = 0;
#ifdef ANDROID
	unsigned long long tmpResult = 0;
	int cpuId = 0;
	while (true) {
		FILE* fd = _getIntFd(SYS_DEVICES_SYSTEM_CPU_CPUID_CPUFREQ_SCALING_CUR_FREQ, cpuId);
		if (NULL != fd) {
			unsigned long long freq = 0;
			int res = fscanf(fd, "%llu", &freq);
			if (res > 0) {
				tmpResult += freq;
			}
	    	fclose(fd);
		} else {
			break;
		}
		cpuId++;
	};
	if (cpuId > 0) {
		result = tmpResult / cpuId;
	}
#else
    hwNode computer("computer",
      hw::system);
	unsigned long long sum = 0;
	unsigned long long count = 0;
	scan_cpuinfo(computer);
	scan_cpuid(computer);
	hwNode *core = computer.getChild("core");
	if (NULL != core) {
		int childs = core->countChildren();
		for (int c=0; c < childs; c++) {
			hwNode *cpu = core->getChild(c);
			sum += cpu->getSize();
			count++;
		}
	}
	if (count > 0) {
		result = (uint64) (sum / ((double) count));
	}
#endif
    return result;
}
#endif

// memory
#ifdef VAR_MEMORY_DATA
uint64 getMemoryCapacity() {
    uint64 result;
	struct sysinfo info;
	if (0 == sysinfo(&info)) {
	    result = info.totalram * info.mem_unit;
	} else {
	    result = 0;
	}
	return result;
}

uint64 getCurrentMemoryAvail() {
    uint64 result;
	struct sysinfo info;
	if (0 == sysinfo(&info)) {
	    result = info.freeram * info.mem_unit;
	} else {
	    result = 0;
	}
	return result;
}

uint64 getCurrentMemoryUse() {
	return getMemoryCapacity() - getCurrentMemoryAvail();
}
#endif

// processors
#ifdef VAR_PROCESSOR_DATA
double getCurrentSystemLoad() {
    double result;
	struct sysinfo info;
	if (0 == sysinfo(&info)) {
	    double scale = 65536.0;
		// http://www.linuxquestions.org/questions/programming-9/load-average-return-values-from-sysinfo-309720/
		// see man uptime
	    result = info.loads[0] / scale; // 0=1 min avg, 1=5 min avg 2=15 min avg
	} else {
	    result = 0;
	}
    return result;
}
#endif

// volumes
#ifdef VAR_VOLUME_DATA
uint64 getVolumeCapacity() {
    return 0;
}

uint64 getCurrentVolumeAvail() {
    return 0;
}

uint64 getCurrentVolumeUse() {
    return 0;
}
#endif

#ifdef VAR_NETWORK_DATA
// network
uint64 getCurrentNetSpeed() {
    return 0;
}

uint64 getMaxNetSpeed() {
    return 0;
}

double getNetUtilization() {
	return 0;
}

#endif

#ifdef VAR_ENERGY_DATA
// power
bool hasSystemBattery() {
	return _readOneValueIntFile(SYS_CLASS_POWERSUPPLY_BATTERY_PRESENT, 0);
}

int getBatteryLifePercent() {
	return _readOneValueIntFile(SYS_CLASS_POWERSUPPLY_BATTERY_CAPACITY, -1);
} 

int getBatteryLifeTime() {
    return -1;
}

int getPowerPlugStatus() {
	int result = _readOneValueIntFile(SYS_CLASS_POWERSUPPLY_BATTERY_CHARGING_SOURCE, -1);
    if (result > 0) {
        result = 1;
    }
    return result;
}
#endif

// -------------------- implement process functions ---------------------

// arbitrary process
#ifdef VAR_ARBITRARY_PROCESS_DATA

pid_t _charToPid(int id) {
    return (pid_t) id;
}

bool isProcessAlive(const int id) {
	FILE* fd = _getIntFd(PROC_PID_STAT, id);
	bool result = false;
	if (NULL != fd) {
		result = true;
		fclose(fd);
	}
	return result;
}
#endif

#if defined(VAR_CURRENT_PROCESS_DATA) || defined(VAR_ARBITRARY_PROCESS_DATA)

template <typename T>
T _scanValue(const char* name, const char* scanPattern, T deflt) {
	T result = deflt;
	FILE* fd = fopen(name, "r");
	if (NULL != fd) {
		fscanf(fd, scanPattern, &result);
		fclose(fd);
	}
	return result;
}

template <typename T>
T _scanValue(const char* namePattern, pid_t pid, const char* scanPattern, T deflt) {
	char name[BUFSIZ];
	sprintf(name, namePattern, pid);
	return _scanValue(name, scanPattern, deflt);
}

long _skip(FILE* fd, const char delimiter, bool exclude = false, bool stopEOL = false) {
	if (feof(fd)) {
		return 0;
	}
	long pos = ftell(fd);
	int c;
	bool stop;
	do {
		c = fgetc(fd);
		stop = (delimiter == c);
		if (stopEOL) {
			stop |= ('\n' == c);
		}
	} while (EOF != c && !stop);
	if (exclude && delimiter == c) {
		ungetc(c, fd);
	}
	return ftell(fd) - pos;
}

long _skipLine(FILE* fd) {
	return _skip(fd, '\n');
}

long _skipWhitespace(FILE* fd, bool stopEOL = false) {
	if (feof(fd)) {
		return 0;
	}
	long pos = ftell(fd);
	bool stop;
	int c;
	do {
		c = fgetc(fd);
		stop = (' ' != c  && '\t' != c);
		if (stopEOL) {
			stop |= ('\n' == c);
		}
	} while (EOF != c && !stop);
	if (!stopEOL || (stopEOL && '\n' != c)) {
		ungetc(c, fd);
	}
	return ftell(fd) - pos;
}

bool _readSendTransmit(FILE* fd, IoStatistics *data) {
	bool done = false;
	unsigned long rcx;
	unsigned long txs;
	if (NULL != fd) {
		if (_skipLine(fd) > 0 && _skipLine(fd) > 0) {
		    int res = 0;
		    do {
		    	int skip = _skip(fd, ':');
		    	if (skip > 0) {
		    		_skipWhitespace(fd);
		    		rcx = 0;
		    		txs = 0;
		    		res = fscanf(fd, "%lu %*u %*u %*u %*u %*u %*u %*u %lu %*u %*u %*u %*u %*u %*u %*u\n", &rcx, &txs);
		    		data->read += rcx;
		    		data->write += txs;
		    	} else {
		    		res = 0;
		    	}
		    } while (res > 0 && !feof(fd));
		    done = true;
		}
		fclose(fd);
	}
	return done;
}


#ifdef VAR_IO_DATA

IoStatistics _getProcessIo(pid_t pid) {
    IoStatistics info;

	info.read = 0;
	info.write = 0;
	_readSendTransmit(_getIntFd(PROC_PID_NET, pid), &info);
	FILE *fd = _getIntFd(PROC_PID_IO, pid);
	if (NULL != fd) {
		unsigned long rd, wt;
		// http://www.linuxhowtos.org/System/procstat.htm
		fscanf(fd,
		   "%*s %*d "
		   "%*s %*d "
		   "%*s %*d "
		   "%*s %*d "
		   "%*s %ld "
		   "%*s %ld "
		   "%*s %*d ",
		   &rd, &wt);
		info.read += rd;
		info.write += wt;
		fclose(fd);
	}
	return info;
}
#endif
#ifdef VAR_MEMORY_DATA
uint64 _getProcessMemoryUse(pid_t pid) {
#ifdef ANDROID
	return _scanValue(PROC_PID_STAT, pid, "%*d %*s %*c %*d %*d %*d %*d %*d %*u %*u %*u %*u %*u %*u %*u %*d %*d %*d %*d %*d %*d %*u %lu", 0);
#else
	uint64 result = 0;
    proc_t table;
	if (NULL != get_proc_stats(pid, &table)) {
		result = table.vm_size * 1024;
	}
    return result;
#endif
}
#endif
#ifdef VAR_TIME_DATA
uint64 _getProcessUserTimeTicks(pid_t pid) {
#ifdef ANDROID
	return _scanValue(PROC_PID_STAT, pid, PATTERN_STAT_USERTIME, 0);
	//todo check divide by sysconf(_SC_CLK_TCK)
#else
	uint64 result = 0;
    proc_t table;
	if (NULL != get_proc_stats(pid, &table)) {
		result = table.cutime;
	}
    return result;
#endif
}

uint64 _getProcessKernelTimeTicks(pid_t pid) {
#ifdef ANDROID
	return _scanValue(PROC_PID_STAT, pid, PATTERN_STAT_KERNELTIME, 0);
	//todo check divide by sysconf(_SC_CLK_TCK)
#else
	uint64 result = 0;
    proc_t table;
	if (NULL != get_proc_stats(pid, &table)) {
		result = table.cstime;
	}
    return result;
#endif
}

uint64 _getProcessSystemTimeTicks(pid_t pid) {
#ifdef ANDROID
	return _scanValue(PROC_PID_STAT, pid, "%*d %*s %*c %*d %*d %*d %*d %*d %*u %*u %*u %*u %*u %*u %*u %*d %*d %*d %*d %*d %*d %llu", 0);
#else
	uint64 result = 0;
    proc_t table;
	time_t now;
	time(&now);
	if (NULL != get_proc_stats(pid, &table)) {
		result = now - table.start_time;
	}
    return result;
#endif
}

uint64 _getProcessCycleTimeTicks(pid_t pid) {
#ifdef ANDROID
	uint64 result = 0;
	FILE* fd = _getIntFd(PROC_PID_STAT, PID2INT(pid));
    if (NULL != fd) {
		long unsigned utime = 0;
		long unsigned ktime = 0;
		fscanf(fd, "%*d %*s %*c %*d %*d %*d %*d %*d %*u %*u %*u %*u %*u %lu %lu", &utime, &ktime);
		result = utime + ktime;
        fclose(fd);
        //todo check divide by sysconf(_SC_CLK_TCK)
    }
    return result;
#else
	uint64 result = 0;
    proc_t table;
	if (NULL != get_proc_stats(pid, &table)) {
		result = table.utime + table.cstime;
	}
    return result;
#endif
}
#endif
#ifdef VAR_LOAD_DATA

unsigned long long prevSysKernel, prevSysUser;

struct ProcessInfo {
    unsigned long long prevProcKernel, prevProcUser;
	double lastLoad;
};
typedef HashMap<pid_t, ProcessInfo*> ProcessInfoMap;
static ProcessInfoMap prevProcInfo;

double _getProcessProcessorLoad(pid_t pid) {
    unsigned long long sysIdle = 0, sysKernel = 0, sysUser = 0;
    unsigned long long procKernel = 0, procUser = 0;

    ProcessInfo* info;
    bool firstCall;
    if (NULL == prevProcInfo.findp(pid)) {
    	info = new ProcessInfo();
    	prevProcInfo.insert(pid, info);
    	firstCall = true;
    } else {
    	info = prevProcInfo[pid];
    	firstCall = false;
    }

    double result = 0;

    FILE* fd;
    #ifdef ANDROID
	fd = _getIntFd(PROC_PID_STAT, pid);
	if (NULL != fd) {
        fscanf(fd, "%*d %*s %*c %*d %*d %*d %*d %*d %*u %*u %*u %*u %*u %*u %llu %llu", &procUser, &procKernel);
		fclose(fd);
	}
    #else
	proc_t table;
	if (NULL != get_proc_stats(pid, &table)) {
		procUser = table.utime;
		procKernel = table.cstime;
	}
    #endif
	fd = fopen(PROC_STAT, "r");
	if (NULL != fd) {
	    // http://www.linuxhowtos.org/System/procstat.htm
 	    fscanf(fd, "cpu %llu %*u %llu %llu %*u %*u %*u", &sysUser, &sysKernel, &sysIdle);
		fclose(fd);
	}

	if ((sysUser + sysIdle + sysKernel) > 0 && (procUser + procKernel) > 0) {
    	if (!firstCall) {
    		unsigned long long sysKernelDiff = sysKernel - prevSysKernel;
    		unsigned long long sysUserDiff = sysUser - prevSysUser;
    		unsigned long long procKernelDiff = procKernel - info->prevProcKernel;
    		unsigned long long procUserDiff = procUser - info->prevProcUser;
    		unsigned long long sysTotal = sysKernelDiff + sysUserDiff;
    		unsigned long long procTotal = procKernelDiff + procUserDiff;

    		if (sysTotal > 0) {
    	        result = ((100.0 * procTotal) / ((double)sysTotal));
			    if (isnan(result) || isinf(result)) {
				    result = info->lastLoad;
			    } else {
				    info->lastLoad = result;
			    }
    		} else {
				result = info->lastLoad;
    		}
		}

		// save time info before return
		prevSysKernel = sysKernel;
		prevSysUser = sysUser;

		info->prevProcKernel = procKernel;
		info->prevProcUser = procUser;

		// check for first call
		if (firstCall) {
			info->lastLoad = 0.0;
		}
	}
    return result;
}
#endif
#endif

// ------------------------ this process ----------------------------
#ifdef VAR_CURRENT_PROCESS_DATA
int getCurrentProcessId() {
    return getpid();
}
#ifdef VAR_IO_DATA
IoStatistics getCurrentProcessIo() {
    return _getProcessIo(getpid());
}
#endif

#ifdef VAR_MEMORY_DATA
uint64 getCurrentProcessMemoryUse() {
	uint64 result;
	struct rusage usage;
	if (0==getrusage(RUSAGE_SELF, &usage)) {
	    result = usage.ru_maxrss * 1024;
	} else {
	    result = 0;
	}
    return result;
}
#endif

#ifdef VAR_TIME_DATA
uint64 getCurrentProcessUserTimeTicks() {
	uint64 result;
	struct rusage usage;
	if (0==getrusage(RUSAGE_SELF, &usage)) {
	    result = usage.ru_utime.tv_sec * 1000 * 1000 + usage.ru_utime.tv_usec;
	} else {
	    result = 0;
	}
    return result;
}

uint64 getCurrentProcessKernelTimeTicks() {
	uint64 result;
	struct rusage usage;
	if (0==getrusage(RUSAGE_SELF, &usage)) {
	    result = usage.ru_stime.tv_sec * 1000 * 1000 + usage.ru_stime.tv_usec;
	} else {
	    result = 0;
	}
    return result;
}

uint64 getCurrentProcessSystemTimeTicks() {
    return _getProcessSystemTimeTicks(getpid());
}
#endif

#ifdef VAR_LOAD_DATA
double getCurrentProcessProcessorLoad() {
    return _getProcessProcessorLoad(getpid());
}
#endif
#endif

#ifdef VAR_DEBUG
int getLastLoadError() {
    return 0;
}
#endif

// arbitrary process
#ifdef VAR_ARBITRARY_PROCESS_DATA
#ifdef VAR_IO_DATA
IoStatistics getProcessIo(const int id) {
    return _getProcessIo(id);
}
#endif
#ifdef VAR_MEMORY_DATA
uint64 getProcessMemoryUse(const int id) {
    return _getProcessMemoryUse(id);
}
#endif
#ifdef VAR_TIME_DATA
uint64 getProcessUserTimeTicks(const int id) {
    return _getProcessUserTimeTicks(id);
}

uint64 getProcessKernelTimeTicks(const int id) {
    return _getProcessKernelTimeTicks(id);
}

uint64 getProcessSystemTimeTicks(const int id) {
    return _getProcessSystemTimeTicks(id);
}
#endif
#ifdef VAR_LOAD_DATA
double getProcessProcessorLoad(const int id) {
    return _getProcessProcessorLoad(id);
}
#endif
#endif


#if defined(VAR_ALL_PROCESSES_DATA) && defined(VAR_IO_DATA)
// all processes
IoStatistics getAllProcessesIo() {
    IoStatistics info;
	info.read = 0;
	info.write = 0;

#ifdef ANDROID
	_readSendTransmit(fopen(PROC_NET_DEV, "r"), &info);

	unsigned long read = 0;
	unsigned long write = 0;
	struct hd_driveid *id;
	int res, res2;
	FILE* fd = fopen(PROC_DISKSTATS, "r");
	char dev[100];
	char filename[BUFSIZ];
	if (NULL != fd) {
		do {
			// # reads, #reads merged, #sect read, #ms reading, #writes, #sect written #ms writing #ios in progress #ms ios #weighted ios
			res = fscanf(fd, "%*u %*u %s %*u %*u %lu %*u %*u %lu %*u %*u %*u %*u %*u\n", dev, &read, &write);
			if (res > 0) {
				// prefer sda1 over sda
				res2 = sscanf(dev, "%*a[a-zA-z]%*u");
				if (res2 > 0) {
					sprintf(filename, DEV_DEVID, dev);
					int devFd = open(filename, O_RDONLY|O_NONBLOCK);
					if (devFd >= 0) {
						if (ioctl(devFd, HDIO_GET_IDENTITY, id)) {
							info.read += read * id->sector_bytes;
							info.write += write * id->sector_bytes;
						}
						close(devFd);
					}
				}
			}
		} while (res > 0 && !feof(fd));
		fclose(fd);
	}
#else
	info.read = 0;
	info.write = 0;
	proc_t table;
	PROCTAB* PT = openproc(PROC_FILLMEM | PROC_FILLSTAT);
	if (NULL != PT) {
		info.read = 0;
		info.write = 0;
		while (NULL != readproc(PT, &table)) {
			IoStatistics procStat = _getProcessIo(table.tgid);
			info.read += procStat.read;
			info.write += procStat.write;
		}
		closeproc(PT);
	}
#endif
	return info;
}
#if defined(VAR_ARBITRARY_PROCESS_DATA) || defined(VAR_CURRENT_PROCESS_DATA)
bool isNetworkIoDataIncluded(bool forAll) {
	return true;
}

static bool processFileIoInclude;
bool isFileIoDataIncluded(bool forAll) {
	if (forAll) {
		return true;
	} else {
		return processFileIoInclude;
	}
}
#endif
#endif

// native thread timing

#ifdef ANDROID
typedef HashMap<long, pthread_t> ThreadInfoMap;
static ThreadInfoMap threadInfo;

bool supportsCpuThreadTiming() {
#if defined(USE_PROC_FOR_THREAD_CPU) || defined(USE_CLOCK_FOR_THREAD_CPU)
	return true;
#else
	return false;
#endif
}

long getCpuThreadTime(long threadId) {
	long result = 0;
    #ifdef USE_CLOCK_FOR_THREAD_CPU
	pthread_t* tId = threadInfo.findp(threadId);
	if (NULL != tId) {
		clockid_t cid;
		struct timespec ts;
		result = -1; // signal problem

		if (0 == pthread_getcpuclockid(*tId, &cid)) {
			int t = clock_gettime(cid, &ts);			
			if (t == 0) {
			    result = ts.tv_nsec;
			}
		}
	}
	#elif defined(USE_PROC_FOR_THREAD_CPU)
	char name[BUFSIZ];
	sprintf(name, PROC_TID_STAT, getpid(), threadId);
	FILE* fd = fopen(name, "r");
	if (NULL != fd) {
		long kTime = 0;
		long uTime = 0;
		int res = fscanf(fd, PATTERN_STAT_USERKERNELTIME, &uTime, &kTime);
		if (res > 0) {
			result = kTime + uTime;
		}
		fclose(fd);
	}
    #else
	result = 0;
    #endif
	return result;
}
void registerThread(long threadId, bool reg) {
	if (reg) {
	    if (NULL == threadInfo.findp(threadId)) {
	    	threadInfo.insert(threadId, pthread_self());
	    }
	} else {
		threadInfo.remove(threadId);
	}
}
#endif

void init() {
#if defined(VAR_ALL_PROCESSES_DATA) && defined(VAR_IO_DATA) && (defined(VAR_ARBITRARY_PROCESS_DATA) || defined(VAR_CURRENT_PROCESS_DATA))
	processFileIoInclude = false;
	FILE* fd = _getIntFd(PROC_PID_IO, 1);
	if (NULL != fd) {
		processFileIoInclude = true;
		fclose(fd);
	}
#endif
}

void done() {
#ifdef ANDROID
	for (ProcessInfoMap::Iterator it = prevProcInfo.first();
		it; it.next()) {
		delete it.value();
	}
	prevProcInfo.done();
	threadInfo.done();
#endif
// do cleanup if needed
}
