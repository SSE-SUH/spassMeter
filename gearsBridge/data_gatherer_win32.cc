
#include "wtypes.h"
#include <windows.h>
#include <vector>
#include <map>
#include "Iphlpapi.h"
#include "Psapi.h"
#include <pdh.h>
#include <pdhmsg.h>

#if (WINVER >= 0x0500)
#ifndef _WIN32_DCOM
#define _WIN32_DCOM
#endif

#include <objbase.h>
#include <atlbase.h>
#include <atlstr.h>
#include <iostream>
#include <wbemidl.h>
#include <comutil.h>
#include <stdlib.h>
# pragma comment(lib, "wbemuuid.lib")
#endif

#include "data_gatherer.h"
#include "de_uni_hildesheim_sse_system_DataGatherer.h"

// ****************
// flags
// ****************

//#define GATHERER_DEBUG
#define AVOID_WMI

// ****************
// common functions
// ****************

#ifdef VAR_DEBUG
static DWORD lastLoadError = 0;
#endif
#if (WINVER >= 0x0500)
static IWbemLocator *locator = 0;
static IWbemServices *service = 0;
static IWbemRefresher *pRefresher = NULL;
static IWbemHiPerfEnum *pEnum = NULL;
static long                    lID = 0;
#endif

// ****************
// hacking windows
// ****************

//#if (WINVER < 0x0500)
// in hertz
int _getProcessorSpeed() {
#ifndef FORX64
	unsigned __int64 start, stop;
	   unsigned __int64 nCtr, nFreq, nCtrStop;

	   QueryPerformanceFrequency((LARGE_INTEGER *)&nFreq);
	   _asm _emit 0x0F
	   _asm _emit 0x31
	   _asm mov DWORD PTR start, eax
	   _asm mov DWORD PTR [start+4], edx
	   QueryPerformanceCounter((LARGE_INTEGER *)&nCtrStop);
	   nCtrStop += nFreq;
	   do
	   {
	      QueryPerformanceCounter((LARGE_INTEGER *)&nCtr);
	   }while (nCtr < nCtrStop);

	   _asm _emit 0x0F
	   _asm _emit 0x31
	   _asm mov DWORD PTR stop, eax
	   _asm mov DWORD PTR [stop+4], edx

	   return (stop-start);
#else
	   return 0;
#endif
}

#ifndef FORX64
int _getProcessorSpeed2() {

	// http://www.codeproject.com/KB/system/Processor_Speed.aspx

	/*
	RdTSC:
	It's the Pentium instruction "ReaD Time Stamp Counter". It measures the
	number of clock cycles that have passed since the processor was reset, as a
	64-bit number. That's what the <CODE>_emit lines do.*/
	#define RdTSC __asm _emit 0x0f __asm _emit 0x31

	// variables for the clock-cycles:

	__int64 cyclesStart = 0, cyclesStop = 0;
	// variables for the High-Res Preformance Counter:

	unsigned __int64 nCtr = 0, nFreq = 0, nCtrStop = 0;


	// retrieve performance-counter frequency per second:

	if(!QueryPerformanceFrequency((LARGE_INTEGER *) &nFreq)) return 0;

	// retrieve the current value of the performance counter:

	QueryPerformanceCounter((LARGE_INTEGER *) &nCtrStop);

	// add the frequency to the counter-value:

	nCtrStop += nFreq;


	_asm {// retrieve the clock-cycles for the start value:
	     RdTSC
	     mov DWORD PTR cyclesStart, eax
	     mov DWORD PTR [cyclesStart + 4], edx
	}

	do{ // retrieve the value of the performance counter
	    // until 1 sec has gone by:
	    QueryPerformanceCounter((LARGE_INTEGER *) &nCtr);
	} while (nCtr < nCtrStop);

	_asm {// retrieve again the clock-cycles after 1 sec. has gone by:
	     RdTSC
	     mov DWORD PTR cyclesStop, eax
	     mov DWORD PTR [cyclesStop + 4], edx
	}

	// stop-start is speed in Hz divided by 1,000,000 is speed in MHz

	return    cyclesStop-cyclesStart;
}
#endif

#if (_WIN32_WINNT < 0x0501)
typedef BOOL ( __stdcall * pfnGetSystemTimes)( LPFILETIME lpIdleTime, LPFILETIME lpKernelTime, LPFILETIME lpUserTime );
static pfnGetSystemTimes s_pfnGetSystemTimes = NULL;
static HMODULE s_hKernel = NULL;
#endif

BOOL GetSystemTimesImpl(LPFILETIME ft_sys_idle, LPFILETIME ft_sys_kernel, LPFILETIME ft_sys_user) {
#if (_WIN32_WINNT >= 0x0501)
    return GetSystemTimes(ft_sys_idle, ft_sys_kernel, ft_sys_user);
#else
    if( s_hKernel == NULL )
	{
		s_hKernel = LoadLibrary( L"Kernel32.dll" );
		if( s_hKernel != NULL )
		{
			s_pfnGetSystemTimes = (pfnGetSystemTimes)GetProcAddress( s_hKernel, "GetSystemTimes" );
			if( s_pfnGetSystemTimes == NULL )
			{
				FreeLibrary( s_hKernel ); s_hKernel = NULL;
			}
		}
	}
	return s_pfnGetSystemTimes(ft_sys_idle, ft_sys_kernel, ft_sys_user);
#endif
}

ULONGLONG subtractTime(const FILETIME &a, const FILETIME &b)
{
    LARGE_INTEGER la, lb;
    la.LowPart = a.dwLowDateTime;
    la.HighPart = a.dwHighDateTime;
    lb.LowPart = b.dwLowDateTime;
    lb.HighPart = b.dwHighDateTime;

    return la.QuadPart - lb.QuadPart;
}

//#if (WINVER < 0x0500)
static FILETIME ftSysIdle, ftSysKernel, ftSysUser;
static double lastCpuLoad;
static bool firstRunLoad = true;
//static ULARGE_INTEGER	 ul_sys_idle_old;
//static ULARGE_INTEGER  ul_sys_kernel_old;
//static ULARGE_INTEGER  ul_sys_user_old;
//static int cpuload_called = 0;

double _calcCpuLoad() {

	// http://en.literateprograms.org/CPU_usage_%28C,_Windows_XP%29

	FILETIME               sys_idle;
	FILETIME               sys_kernel;
	FILETIME               sys_user;

	double usage = 0.0;

	// we cannot directly use GetSystemTimes on C language
	/* add this line :: pfnGetSystemTimes */
	if (GetSystemTimesImpl(&sys_idle,    /* System idle time */
		&sys_kernel,  /* system kernel time */
		&sys_user)) {   /* System user time */

		if (!firstRunLoad) {

		    ULONGLONG sysKernelDiff = subtractTime(sys_kernel, ftSysKernel);
		    ULONGLONG sysUserDiff = subtractTime(sys_user, ftSysUser);
		    ULONGLONG sysIdleDiff = subtractTime(sys_idle, ftSysIdle);

	        ULONGLONG sysTotal = sysKernelDiff + sysUserDiff;

		    usage = 100.0 * ((sysTotal - sysIdleDiff) / ((double)sysTotal));
		    if (isnan(usage)) {
		    	usage = lastCpuLoad;
		    } else {
		    	lastCpuLoad = usage;
		    }
		}

	    ftSysKernel.dwLowDateTime = sys_kernel.dwLowDateTime;
	    ftSysKernel.dwHighDateTime = sys_kernel.dwHighDateTime;

	    ftSysIdle.dwLowDateTime = sys_idle.dwLowDateTime;
	    ftSysIdle.dwHighDateTime = sys_idle.dwHighDateTime;

	    ftSysUser.dwLowDateTime = sys_user.dwLowDateTime;
	    ftSysUser.dwHighDateTime = sys_user.dwHighDateTime;

	    if (firstRunLoad)
	    {
	        firstRunLoad = false;
	        usage = 0.0;
	        lastCpuLoad = 0.0;
	    }
	}
	return usage;
}

static FILETIME prevSysKernel, prevSysUser;
struct ProcessInfo {
	FILETIME prevProcKernel, prevProcUser;
	double lastLoad;
};
typedef std::map<DWORD, ProcessInfo*> ProcessInfoMap;
static ProcessInfoMap prevProcInfo;
//static FILETIME prevProcKernel, prevProcUser;
//static bool firstRunProcessLoad = true;

double _calcProcessCPULoad(HANDLE hProcess) {
	// http://patuxentpc.com/?p=16
    FILETIME sysIdle, sysKernel, sysUser;
    FILETIME procCreation, procExit, procKernel, procUser;
    DWORD id = GetProcessId(hProcess);

    ProcessInfo* info;
    boolean firstCall;
    if (prevProcInfo.find(id) == prevProcInfo.end()) {
    	info = new ProcessInfo();
    	prevProcInfo[id] = info;
    	firstCall = true;
    } else {
    	info = prevProcInfo[id];
    	firstCall = false;
    }

    double result = 0;
    if (!GetSystemTimesImpl(&sysIdle, &sysKernel, &sysUser) ||
        !GetProcessTimes(hProcess, &procCreation, &procExit, &procKernel, &procUser))
    {
        // can't get time info so return
        result = 0.0;
    } else {
		//if (!firstRunProcessLoad) {
    	if (!firstCall) {
			ULONGLONG sysKernelDiff = subtractTime(sysKernel, prevSysKernel);
			ULONGLONG sysUserDiff = subtractTime(sysUser, prevSysUser);

			ULONGLONG procKernelDiff = subtractTime(procKernel, info->prevProcKernel);
			ULONGLONG procUserDiff = subtractTime(procUser, info->prevProcUser);

			ULONGLONG sysTotal = sysKernelDiff + sysUserDiff;
			ULONGLONG procTotal = procKernelDiff + procUserDiff;

			result = ((100.0 * procTotal) / ((double)sysTotal));
			if (isnan(result)) {
				result = info->lastLoad;
			} else {
				info->lastLoad = result;
			}
		}

		// save time info before return
		prevSysKernel.dwLowDateTime = sysKernel.dwLowDateTime;
		prevSysKernel.dwHighDateTime = sysKernel.dwHighDateTime;

		prevSysUser.dwLowDateTime = sysUser.dwLowDateTime;
		prevSysUser.dwHighDateTime = sysUser.dwHighDateTime;

		info->prevProcKernel.dwLowDateTime = procKernel.dwLowDateTime;
		info->prevProcKernel.dwHighDateTime = procKernel.dwHighDateTime;

		info->prevProcUser.dwLowDateTime = procUser.dwLowDateTime;
		info->prevProcUser.dwHighDateTime = procUser.dwHighDateTime;

		// check for first call
		//if (firstRunProcessLoad)
		if (firstCall) {
			//firstRunProcessLoad = false;
			info->lastLoad = 0.0;
			//result = 0.0;
		}
    }
	CloseHandle(hProcess);

    return result;
}

//#endif

void initializeCom() {
#if (WINVER >= 0x0500)
	if (0 != service || 0 != locator) {
		return;
	}

	HRESULT hr = CoInitializeEx( NULL, COINIT_MULTITHREADED );
	if ( FAILED( hr ) )
	{
#ifdef GATHERER_DEBUG
		printf("Locutor: COM initialization failed\n");
#endif
		return;
	}

	// setup process-wide security context
	hr = CoInitializeSecurity( NULL, // we're not a server
							   -1, // we're not a server
							   NULL, // dito
							   NULL, // reserved
							   RPC_C_AUTHN_LEVEL_DEFAULT, // let DCOM decide
							   RPC_C_IMP_LEVEL_IMPERSONATE,
							   NULL,
							   EOAC_NONE,
							   NULL );
	if ( FAILED( hr ) )
	{
#ifdef GATHERER_DEBUG
		printf("Locutor: Security initialization failed\n");
#endif
		return;
	}

	int result = 0;
	{
		// connect to WMI
		hr = CoCreateInstance( CLSID_WbemAdministrativeLocator, NULL,
							CLSCTX_INPROC_SERVER,
							IID_IWbemLocator, reinterpret_cast< void** >( &locator ) );
		if ( FAILED( hr ) )
		{
#ifdef GATHERER_DEBUG
			printf("Locutor: Instantiation of IWbemLocator failed\n");
#endif
			return;
		}

		// connect to local service with current credentials
		hr = locator->ConnectServer( L"root\\cimv2", NULL, NULL, NULL,
									         0L,   // Security flags WBEM_FLAG_CONNECT_USE_MAX_WAIT,
									 NULL, NULL, &service );
		if ( !SUCCEEDED( hr ) ) {
			service = 0;
		}
	}
#endif	
}

// ****************
// WMI helper functions
// ****************

#if (WINVER >= 0x0500)
const int FUNCTION_FIRST_VALUE(0);
const int FUNCTION_MINIMIZE(-1);
const int FUNCTION_MAXIMIZE(1);
const int FUNCTION_SUM(2);
const int FUNCTION_AVERAGE(3);
const int TYPE_UINT16(0);
const int TYPE_UINT32(1);
const int TYPE_UINT64(2);

unsigned __int64 getUInt64(VARIANT *var_val) {
	// should work better in newer win versions but I don't know how!
	unsigned __int64 val = 0;
	if (VT_BSTR == var_val->vt) {
		val = _wtoi64((const wchar_t *)var_val->bstrVal);
	} else if (VT_NULL != var_val->vt && VT_EMPTY != var_val->vt) {
		printf("VARIANT::vt with value %i not handled\n", var_val->vt);
	}
	return val;
}

struct ComValueData {
	LPCWSTR valueName;
	uint16 ui16Result;
	uint32 ui32Result;
	unsigned __int64 ui64Result;
	// function pointer would be nice
	// 0 = first value and stop, -1 = minimize, 1 = maximize, 2=sum
	int function;
	// 0 = int, 1 = unsigned int, 2 = long, 3 = unsigned long, -1 = uint64
	int type;
};

template <class T>
T calculate(T oldVal, T newVal, int function, int enumCount) {
	T result;
	if (0 == enumCount) {
		result = 0;
	} else {
		result = oldVal;
	}

	if (FUNCTION_FIRST_VALUE == function) {
		result = newVal;
	} else if (FUNCTION_MAXIMIZE == function) {
		if (enumCount == 0 || newVal > oldVal) {
			result = newVal;
		}
	} else if (FUNCTION_MINIMIZE == function) {
		if (enumCount == 0 || newVal < oldVal) {
			result = newVal;
		}
	} else if (FUNCTION_SUM == function || FUNCTION_AVERAGE == function) {
		result += newVal;
	}
	return result;
}

int comValue(const BSTR query, ComValueData data[], int dataCount) {
    initializeCom();
	int enumCount = 0;
	if (0 != service) {
		// execute a query
		IEnumWbemClassObject *enumerator;
		// Win32_Processor
		HRESULT hr = service->ExecQuery( L"WQL", query, WBEM_FLAG_FORWARD_ONLY, NULL, &enumerator );
		if ( SUCCEEDED( hr ) )
		{
			// read the first instance from the enumeration (only one on single processor machines)
			IWbemClassObject* object = NULL;
			ULONG retcnt;
			VARIANT var_val;
			int hasAverage = 0;
			while(enumerator) {
				hr = enumerator->Next( WBEM_INFINITE, 1L, &object, &retcnt );
				if (0 == retcnt) {
					break;
				}

#ifdef GATHERER_DEBUG
				hr = object->Get( L"Name", 0, &var_val, NULL, NULL );
				if ( SUCCEEDED( hr ) )
				{
					_bstr_t str = var_val;
					std::cout << "Name: " << str << std::endl;
					VariantClear(&var_val);
				}
#endif
				int i;
				for (i = 0; i < dataCount; i++) {
				    if (FUNCTION_AVERAGE == data[i].function) {
					    hasAverage++;
					}
					if ((0 == data[i].function && 0 == enumCount) || (data[i].function != 0)) {

						hr = object->Get( data[i].valueName, 0, &var_val, NULL, NULL );
						if ( SUCCEEDED( hr ) )
						{
#ifdef GATHERER_DEBUG
							std::cout << "Value: " << data[i].valueName << " " << var_val.uintVal << " " << var_val.uiVal << std::endl;
#endif
							switch (data[i].type) {
							case TYPE_UINT16:
								data[i].ui16Result = calculate(data[i].ui16Result, var_val.uiVal, data[i].function, enumCount);
								break;
							case TYPE_UINT32:
								// extra cast needed for firefox plugin
								data[i].ui32Result = calculate(data[i].ui32Result, (uint32)var_val.uintVal, data[i].function, enumCount);
								break;
							case TYPE_UINT64:
								data[i].ui64Result = calculate(data[i].ui64Result, getUInt64(&var_val), data[i].function, enumCount);
								break;
							}
						}
					}
				}
				VariantClear(&var_val);

				object->Release();
				enumCount++;
			}
			enumerator->Release();
			if (hasAverage > 0 && enumCount > 0) {
				for (int i = 0; i < dataCount; i++) {
					if (FUNCTION_AVERAGE == data[i].function) {
						switch (data[i].type) {
						case TYPE_UINT16:
							data[i].ui16Result /= enumCount;
							break;
						case TYPE_UINT32:
							data[i].ui32Result /= enumCount;
							break;
						case TYPE_UINT64:
							data[i].ui64Result /= enumCount;
							break;
						}
					}
				}
			}
		} else {
#ifdef GATHERER_DEBUG
			std::cout << "Query " << query << " failed" << std::endl;
#endif
		}
	}
	return enumCount;
}

/*#ifdef VAR_DEBUG
int getLastLoadError() {
    return lastLoadError;
}
#endif*/

#if (WINVER < 0x0500)

DWORD GetCurrentProcessorNumber(void)
{
    _asm {mov eax, 1}
    _asm {cpuid}
    _asm {shr ebx, 24}
    _asm {mov eax, ebx}
}

#endif
#endif


unsigned __int64 estimateMaxSpeed(unsigned __int64 curSpeed, unsigned __int64 base, unsigned __int64 estimatedMax) {
	if (estimatedMax <= 0 && curSpeed <= base) {
		estimatedMax = base;
	}
	return estimatedMax;
}

// ****************
// interface
// ****************
#ifdef VAR_SCREEN_DATA
int getScreenWidth() {
	RECT desktop;
	const HWND hDesktop = GetDesktopWindow();
	GetWindowRect(hDesktop, &desktop);

	return desktop.right;
}

int getScreenHeight() {
	RECT desktop;
	const HWND hDesktop = GetDesktopWindow();
	GetWindowRect(hDesktop, &desktop);

	return desktop.bottom;
}

int getScreenResolution() {
	DEVMODE dm;
	dm.dmSize = sizeof(DEVMODE);
	dm.dmDriverExtra = 0;
	EnumDisplaySettings(NULL, ENUM_CURRENT_SETTINGS, &dm);
	return dm.dmLogPixels;
}
#endif

#ifdef VAR_MEMORY_DATA
uint64 getMemoryCapacity() {
	MEMORYSTATUSEX statex;
	statex.dwLength = sizeof (statex);
	GlobalMemoryStatusEx (&statex);

	return statex.ullTotalPhys;
}

uint64 getCurrentMemoryAvail() {
	MEMORYSTATUSEX statex;
	statex.dwLength = sizeof (statex);
	GlobalMemoryStatusEx (&statex);

	return statex.ullAvailPhys;
}

uint64 getCurrentMemoryUse() {
	MEMORYSTATUSEX statex;
	statex.dwLength = sizeof (statex);
	GlobalMemoryStatusEx (&statex);

	return statex.ullTotalPhys - statex.ullAvailPhys;
}
#endif

#ifdef VAR_PROCESSOR_DATA
int getNumberOfProcessors() {
	SYSTEM_INFO sinfo;
	GetSystemInfo(&sinfo);

	return sinfo.dwNumberOfProcessors;
}

// in herz
uint64 getMaxProcessorSpeed() {
    HKEY key, cpu;
    WCHAR id[MAX_PATH + 1];
    int mhz;
    uint64 result;
    DWORD size = 0, rc;

    RegOpenKey(HKEY_LOCAL_MACHINE,
               L"HARDWARE\\DESCRIPTION\\System\\CentralProcessor", &key);

    //just lookup the first id, then assume all cpus are the same.
    rc = RegEnumKey(key, 0, id, sizeof(id));
    if (rc != ERROR_SUCCESS) {
        RegCloseKey(key);
        return _getProcessorSpeed();
    }

    rc = RegOpenKey(key, id, &cpu);
    if (rc != ERROR_SUCCESS) {
        RegCloseKey(key);
        return _getProcessorSpeed();
    }

    size = sizeof(mhz);
    if (RegQueryValueEx(cpu, L"~MHz", NULL, NULL,
                        (LPBYTE)&mhz, &size))
    {
        result = _getProcessorSpeed();
    }

    RegCloseKey(key);
    RegCloseKey(cpu);
    result = mhz;
    return result;
}

// in herz
uint64 getCurrentProcessorSpeed() {
#if (WINVER >= 0x0500)

	ComValueData data[1];
	data[0].valueName = L"CurrentClockSpeed";
	data[0].function = FUNCTION_AVERAGE;
	data[0].type = TYPE_UINT32;

	// http://msdn.microsoft.com/en-us/library/aa394373%28VS.85%29.aspx

	comValue(L"SELECT CurrentClockSpeed FROM Win32_Processor WHERE CpuStatus=1", data, 1);

	return data[0].ui32Result;
#else
	return _getProcessorSpeed();
#endif
}

double getCurrentSystemLoad() {
	return _calcCpuLoad();
}
#endif

#ifdef VAR_VOLUME_DATA
uint64 getVolumeCapacity() {
	// iterate?
	LPCTSTR DiskDirectory = L"C:\\";
	__int64 AvailableBytes;
	__int64 TotalBytes;
	__int64 FreeBytes;

	GetDiskFreeSpaceEx(DiskDirectory, (PULARGE_INTEGER) &AvailableBytes,
		(PULARGE_INTEGER) &TotalBytes, (PULARGE_INTEGER) &FreeBytes);
	return TotalBytes;
}

uint64 getCurrentVolumeAvail() {
	// iterate?
	LPCTSTR DiskDirectory = L"C:\\";
	__int64 AvailableBytes;
	__int64 TotalBytes;
	__int64 FreeBytes;

	GetDiskFreeSpaceEx(DiskDirectory, (PULARGE_INTEGER) &AvailableBytes,
		(PULARGE_INTEGER) &TotalBytes, (PULARGE_INTEGER) &FreeBytes);
	return AvailableBytes;
}

uint64 getCurrentVolumeUse() {
	// iterate?
	LPCTSTR DiskDirectory = L"C:\\";
	__int64 AvailableBytes;
	__int64 TotalBytes;
	__int64 FreeBytes;

	GetDiskFreeSpaceEx(DiskDirectory, (PULARGE_INTEGER) &AvailableBytes,
		(PULARGE_INTEGER) &TotalBytes, (PULARGE_INTEGER) &FreeBytes);
	return TotalBytes - AvailableBytes;
}
#endif

#ifdef VAR_NETWORK_DATA

#if (WINVER < 0x0502)
#define PDH_MAX_COUNTER_PATH    2048
#endif

struct NetworkCounter {
	HCOUNTER hBandwidth;
	HCOUNTER hTotal;
	char* adapter;
};

typedef std::vector<NetworkCounter*> NetworkCounterVector;
static NetworkCounterVector networkCounter;
static HQUERY NetworkQuery;
static FILETIME lastNetworkQuery;
static double lastBandwidth = 0;
static double lastUtilization = 0;
static double lastUsage = 0;

bool checkNetworkQuery() {
	return true;
/*	bool doit = false;
	FILETIME systemTime;
	GetSystemTimeAsFileTime(&systemTime);
	if (0 == lastNetworkQuery.dwLowDateTime && 0 == lastNetworkQuery.dwHighDateTime) {
		lastNetworkQuery.dwLowDateTime = systemTime.dwLowDateTime;
		lastNetworkQuery.dwHighDateTime = systemTime.dwHighDateTime;
		doit = true;
	} else {
		ULONGLONG diff = subtractTime(systemTime, lastNetworkQuery);
		if (diff > 700) {
			doit = true;
			lastNetworkQuery.dwLowDateTime = systemTime.dwLowDateTime;
			lastNetworkQuery.dwHighDateTime = systemTime.dwHighDateTime;
		}
	}
	return doit;*/
}

// network names are not identical in iphelper and pdh
void normalizeNwName(PWSTR name) {
	PWSTR rp = name;
	size_t len = wcslen(name);
	while (*rp != 0 && len > 0) {
		if (!((L'a' <= *rp && *rp <= L'z') || (L'A' <= *rp && *rp <= L'Z')
			|| (L'0' <= *rp && *rp <= L'9'))) {
			*rp = L' ';
		}
		rp++;
		len--;
	}
}

// input buf will be modified!
// search the iftable for a "similar" network name and return that one in a new
// memory area (or null)
char* GetAdapterName(PWSTR pdhName) {
	BYTE *pBuf=NULL;
	DWORD dwSize=0;
	char* result = NULL;

	PMIB_IFTABLE pMIBTable;
	normalizeNwName(pdhName);

	GetIfTable(NULL,&dwSize,FALSE);
	pBuf=(BYTE *)malloc(dwSize);
	pMIBTable=(PMIB_IFTABLE)pBuf;
	if(NO_ERROR == GetIfTable(pMIBTable,&dwSize,FALSE))
	{
		UINT i;
		for(i=0; i < pMIBTable->dwNumEntries; ++i)
		{
			char* description = (char*) pMIBTable->table[i].bDescr; // not 0-terminated!
			CA2W convName(description);
			normalizeNwName(convName);
			if (0 == wcscmp(convName, pdhName)) {
				size_t len = strlen(description);
				result = (char*)malloc(len + 1);
				strncpy(result, description, len);
				result[len] = 0;
			}
			// do not release convName - will become invalid here
		}
	}
	free(pBuf);
	return result;
}

bool isAdapterEnabled(PMIB_IFTABLE pMIBTable, char* adapter) {
	bool enabled = false;
	bool found = false;
	if (NULL != adapter && NULL != pMIBTable ) {
		UINT i;
		for(i=0; !enabled && !found && i < pMIBTable->dwNumEntries; ++i)
		{
			char* description = (char*) pMIBTable->table[i].bDescr;
			if (0 == strcmp(adapter, description))  {
				found = true;
				enabled = (1 == pMIBTable->table[i].dwAdminStatus)
		        	&& IF_OPER_STATUS_NON_OPERATIONAL != pMIBTable->table[i].dwOperStatus
		            && IF_OPER_STATUS_DISCONNECTED != pMIBTable->table[i].dwOperStatus;
			}
		}
	}
	return enabled;
}

void createNetworkCounter(bool force) {
	PDH_STATUS Status;
    PDH_COUNTER_PATH_ELEMENTS PathElements = {0};
	if (!force && NetworkQuery) {
		return;
	}
	lastNetworkQuery.dwLowDateTime = 0;
	lastNetworkQuery.dwHighDateTime = 0;
    Status = PdhOpenQuery(NULL, NULL, &NetworkQuery);
    if (Status != ERROR_SUCCESS) {
        goto Cleanup;
    }
    DWORD dwNameBufferSize = PDH_MAX_COUNTER_PATH;
    WCHAR szObjectName[PDH_MAX_COUNTER_PATH];
    WCHAR szCounterName_Bandwidth[PDH_MAX_COUNTER_PATH];
    WCHAR szCounterName_Total[PDH_MAX_COUNTER_PATH];
    TCHAR szPathBuffer[256] = TEXT("");
    DWORD dwPathBufferSize = sizeof(szPathBuffer);
    DWORD dwExpandedPathsSize = 0;
    PWSTR szExpandedPaths = NULL;

    // retrieve object name via index

    Status = PdhLookupPerfNameByIndex(NULL, 510, szObjectName, &dwNameBufferSize);
    if (Status != ERROR_SUCCESS) {
        goto Cleanup;
    }

    // retrieve counter name via index

    dwNameBufferSize = PDH_MAX_COUNTER_PATH;
    Status = PdhLookupPerfNameByIndex(NULL, 520, szCounterName_Bandwidth, &dwNameBufferSize);
    if (Status != ERROR_SUCCESS) {
        goto Cleanup;
    }

    dwNameBufferSize = PDH_MAX_COUNTER_PATH;
    Status = PdhLookupPerfNameByIndex(NULL, 388, szCounterName_Total, &dwNameBufferSize);
    if (Status != ERROR_SUCCESS) {
        goto Cleanup;
    }

    // combine object name and counter name to path

    PathElements.szMachineName = NULL;
    PathElements.szObjectName = szObjectName;
    PathElements.szInstanceName = TEXT("*");
    PathElements.szParentInstance = NULL;
    PathElements.dwInstanceIndex = -1;
    PathElements.szCounterName = szCounterName_Bandwidth;
    Status = PdhMakeCounterPath(&PathElements, szPathBuffer, &dwPathBufferSize, 0);
    if (Status != ERROR_SUCCESS) {
        goto Cleanup;
    }

    // expand paths

    dwExpandedPathsSize = 1000;
    szExpandedPaths = (PWSTR)malloc(dwExpandedPathsSize * sizeof(WCHAR));

    Status = PdhExpandCounterPath(szPathBuffer, szExpandedPaths, &dwExpandedPathsSize);
    if (Status == PDH_MORE_DATA) {
    	dwExpandedPathsSize++; // final NULL
    	free(szExpandedPaths);
        szExpandedPaths = (PWSTR)malloc(dwExpandedPathsSize * sizeof(WCHAR));
        Status = PdhExpandCounterPath(szPathBuffer, szExpandedPaths, &dwExpandedPathsSize);
    }
    if (Status != ERROR_SUCCESS) {
        goto Cleanup;
    }

    // build pairs of counters for querying network data
    // reuse created counter path in order to obtain matching pairs
    PWSTR szExpandedPathsEnd = szExpandedPaths + dwExpandedPathsSize;
    size_t dwCounterName_Bandwidth_Len = wcslen(szCounterName_Bandwidth);
    size_t dwCounterName_Total_Len = wcslen(szCounterName_Total);
    networkCounter.clear();
    for (PWSTR p = szExpandedPaths; ((p != szExpandedPathsEnd) && (*p != L'\0')); p += wcslen(p) + 1)
    {
	    // PHD-Functions do not work :(
		size_t dwPrefixSize = wcslen(p) - dwCounterName_Bandwidth_Len;
		size_t dwTmpLen = dwPrefixSize + dwCounterName_Total_Len + 1;
		size_t dwTmpSize = dwTmpLen * sizeof(WCHAR);
		PWSTR szTmp = (PWSTR)malloc(dwTmpSize);
		ZeroMemory(szTmp, dwTmpSize);
		wcsncpy_s(szTmp, dwTmpLen, p, dwPrefixSize);

		// determine adapter name and query - but PHD-names do not match
		// network names :(
		PWSTR lastPrefixIncl = p + dwPrefixSize - 3; // - zero - / - )
		PWSTR rp = lastPrefixIncl;
		while (*rp != L'(' && rp > p) {rp--;}
		rp++; // skip (
		DWORD dwAdapterLen = lastPrefixIncl - rp + 1 + 1; // diff + 0
		DWORD dwAdapterSize = dwAdapterLen * sizeof(WCHAR);
		PWSTR szAdapter = (PWSTR)malloc(dwAdapterSize);
		wcsncpy_s(szAdapter, dwAdapterLen, rp, dwAdapterLen - 1);

		ULONG IfIndex = 0;
		char* adapter = GetAdapterName(szAdapter);
		free(szAdapter);

		if (NULL != adapter) {
			NetworkCounter *counter = new NetworkCounter();
			networkCounter.push_back(counter);
			counter->adapter = adapter;

			Status = PdhAddCounter(NetworkQuery, p, 0, &counter->hBandwidth);
			if (Status != ERROR_SUCCESS) {
				counter->hBandwidth = 0;
			}

			wcsncat_s(szTmp, dwTmpLen, szCounterName_Total, dwCounterName_Total_Len);
			Status = PdhAddCounter(NetworkQuery, szTmp, 0, &counter->hTotal);
			if (Status != ERROR_SUCCESS) {
				counter->hTotal = 0;
			}
			free(szTmp);
		}
    }
    // collect initial data
    if (checkNetworkQuery()) {
        Status = PdhCollectQueryData(NetworkQuery);
    }
Cleanup:
    if (szExpandedPaths) {
    	free(szExpandedPaths);
    }
}

// function = 1 : bandwidth (bit/s), 2 : utilization (%), 3 : usage (bytes/s)
double queryData(int function) {
	if (!NetworkQuery) {
		createNetworkCounter(false);
	}

	double result = 0;
    PDH_STATUS Status;
    double maxBandwidth = 0;
    double maxUtilization = 0;
    double maxUsage = 0;
    int considered = 0;
	// collect data, run over all network counter pairs and read out values
    if (checkNetworkQuery()) {
    	Status = PdhCollectQueryData(NetworkQuery);
    } else {
    	Status = PDH_MORE_DATA;
    }
	if (Status == ERROR_SUCCESS)
	{
		BYTE *pBuf=NULL;
		DWORD dwSize=0;
		PMIB_IFTABLE pMIBTable;
		GetIfTable(NULL,&dwSize,FALSE);
		pBuf=(BYTE *)malloc(dwSize);
		pMIBTable=(PMIB_IFTABLE)pBuf;
		if(NO_ERROR == GetIfTable(pMIBTable,&dwSize,FALSE))
		{
			PDH_FMT_COUNTERVALUE DisplayValue;
			DWORD CounterType;
			for (NetworkCounterVector::iterator it = networkCounter.begin();
				it != networkCounter.end(); it++) {
				NetworkCounter *counter = *it;
				if (isAdapterEnabled(pMIBTable, counter->adapter)) {
					double bandwidth = -1;
					double total = -1;
					if (0 != counter->hBandwidth) {
						Status = PdhGetFormattedCounterValue(counter->hBandwidth,
							PDH_FMT_DOUBLE | PDH_FMT_NOSCALE, &CounterType, &DisplayValue);
						if (Status == ERROR_SUCCESS && (DisplayValue.CStatus == PDH_CSTATUS_VALID_DATA || DisplayValue.CStatus == PDH_CSTATUS_NEW_DATA)) {
							bandwidth = DisplayValue.doubleValue;
						}
					}
					if (0 != counter->hTotal) {
						Status = PdhGetFormattedCounterValue(counter->hTotal,
							PDH_FMT_DOUBLE | PDH_FMT_NOSCALE, &CounterType, &DisplayValue);
						if (Status == ERROR_SUCCESS && (DisplayValue.CStatus == PDH_CSTATUS_VALID_DATA || DisplayValue.CStatus == PDH_CSTATUS_NEW_DATA)) {
							total = DisplayValue.doubleValue;
						}
					}
					if (bandwidth >=0 && total >=0) {
						double utility = (total * 8 / bandwidth) * 100; // bandwidth in Bit/s
						if (0 == considered || utility > maxUtilization) {
							maxUtilization = utility;
							maxBandwidth = bandwidth;
							maxUsage = total;
						}
						considered++;
					}
				}
			}
		}
		free(pBuf);
		if (considered > 0) {
	    	lastBandwidth = maxBandwidth;
	    	lastUtilization = maxUtilization;
	    	lastUsage = maxUsage;
		}
	}
	if (0 == considered) {
    	maxBandwidth = lastBandwidth;
    	maxUtilization = lastUtilization;
    	maxUsage = lastUsage;
	}

	if (1 == function) {
		result = maxBandwidth;
	} else if (2 == function) {
		result = maxUtilization;
	} else if (3 == function) {
		result = maxUsage;
	}
	return result;
}

uint64 getCurrentNetSpeed() { 
	// function = 1 : bandwidth (bit/s), 2 : utilization (%), 3 : usage (bytes/s)
    return (uint64)(queryData(3) * 8.0);

/*
	// this is max net (link) speed - minimized!
	BYTE *pBuf=NULL;
	DWORD dwSize=0;
	DWORD dwResult=0;
	BOOL bConnected=FALSE;
	PMIB_IFTABLE pMIBTable;

	GetIfTable(NULL,&dwSize,FALSE);
	pBuf=(BYTE *)malloc(dwSize);
	pMIBTable=(PMIB_IFTABLE)pBuf;
	DWORD minVal = -1; // exclude local and virtual network devices
	if(NO_ERROR == GetIfTable(pMIBTable,&dwSize,FALSE))
	{
		UINT i;
		for(i=0; i < pMIBTable->dwNumEntries; ++i)
		{
			// MIB_IF_TYPE_ETHERNET == pMIBTable->table[i].dwType
			if(pMIBTable->table[i].dwType != IF_TYPE_TUNNEL
			    && pMIBTable->table[i].dwType != IF_TYPE_SOFTWARE_LOOPBACK
				&& 1 == pMIBTable->table[i].dwAdminStatus
			    && IF_OPER_STATUS_OPERATIONAL == pMIBTable->table[i].dwOperStatus)
			{
				char* descr = (char*) pMIBTable->table[i].bDescr;
				// heuristics - skip internal virtual adapters
				if (NULL == strstr(descr, "Miniport") &&
					NULL == strstr(descr, "Virtual")) {
					if (minVal < 0 || pMIBTable->table[i].dwSpeed < minVal) {
						minVal = pMIBTable->table[i].dwSpeed;
					}
				}
			}
		}
	}
	free(pBuf);
	return minVal;
	*/
}

uint64 getMaxNetSpeed() {
	// function = 1 : bandwidth (bit/s), 2 : utilization (%), 3 : usage (bytes/s)
	return (uint64)queryData(1);
/*
	uint64 maxNetSpeed = 0;
	uint64 netSpeed = getCurrentNetSpeed();

	if (maxNetSpeed < netSpeed) {
		maxNetSpeed = 0;
		unsigned __int64 base = 10000;
		// however .... loop leads to excepts
		maxNetSpeed = estimateMaxSpeed(netSpeed, base, maxNetSpeed);
		base *= 10;
		maxNetSpeed = estimateMaxSpeed(netSpeed, base, maxNetSpeed);
		base *= 10;
		maxNetSpeed = estimateMaxSpeed(netSpeed, base, maxNetSpeed);
		base *= 10;
		maxNetSpeed = estimateMaxSpeed(netSpeed, base, maxNetSpeed);
		base *= 10;
		maxNetSpeed = estimateMaxSpeed(netSpeed, base, maxNetSpeed);
		base *= 10;
		maxNetSpeed = estimateMaxSpeed(netSpeed, base, maxNetSpeed);
		base *= 10;
		maxNetSpeed = estimateMaxSpeed(netSpeed, base, maxNetSpeed);
		base *= 10;
		maxNetSpeed = estimateMaxSpeed(netSpeed, base, maxNetSpeed);
		base *= 10;
	}
	return maxNetSpeed;*/
}

double getNetUtilization() {
	// function = 1 : bandwidth (bit/s), 2 : utilization (%), 3 : usage (bytes/s)
	return queryData(2);
}

#endif

#ifdef VAR_ENERGY_DATA
bool hasSystemBattery() {
	SYSTEM_POWER_STATUS status;
	GetSystemPowerStatus(&status);

	return !(status.BatteryFlag & 128);
}

int getBatteryLifePercent() {
	SYSTEM_POWER_STATUS status;
	GetSystemPowerStatus(&status);

	int result = status.BatteryLifePercent;
	if (result > 100) {
		result = -1;
	}
	return result;
}

int getBatteryLifeTime() {
	SYSTEM_POWER_STATUS status;
	GetSystemPowerStatus(&status);

	return status.BatteryLifeTime;
}

int getPowerPlugStatus() {
	SYSTEM_POWER_STATUS status;
	GetSystemPowerStatus(&status);

	int result = status.ACLineStatus;
	if (255 == result) {
		result = -1;
	}
	return result;
}
#endif

#if defined(VAR_CURRENT_PROCESS_DATA) || defined(VAR_ARBITRARY_PROCESS_DATA)
#ifdef VAR_IO_DATA
IoStatistics _getProcessIo(HANDLE hProcess) {
    IoStatistics result;
	result.read = 0;
	result.write = 0;
	if (NULL != hProcess) {
		IO_COUNTERS counters;
		if (GetProcessIoCounters(hProcess, &counters)) {
			result.read += counters.ReadTransferCount;
			result.write += counters.WriteTransferCount;
		}
	}
	CloseHandle(hProcess);
	return result;
}
#endif
#ifdef VAR_MEMORY_DATA
uint64 _getProcessMemory(HANDLE hProcess) {
    uint64 result = 0;
	if (NULL != hProcess) {
        PROCESS_MEMORY_COUNTERS counters;
		if (GetProcessMemoryInfo(hProcess, &counters, sizeof(counters))) {
		    result = counters.WorkingSetSize;
		}
	}
	CloseHandle(hProcess);
	return result;
}
#endif
#ifdef VAR_TIME_DATA
uint64 _getProcessUserTimeTicks(HANDLE hProcess) {
    uint64 result = 0;
	if (NULL != hProcess) {
		FILETIME creationTime;
		FILETIME exitTime;
		FILETIME kernelTime;
		FILETIME userTime;
		if (GetProcessTimes(hProcess, &creationTime, &exitTime, &kernelTime, &userTime)) {
			ULARGE_INTEGER ularge;
			CopyMemory(&ularge,&userTime,sizeof(FILETIME)); 

			result = *(__int64 *)&ularge;
			result *= 100; // measured in 100 ns
		}
	}
	CloseHandle(hProcess);
	return result;
}

uint64 _getProcessKernelTimeTicks(HANDLE hProcess) {
    uint64 result = 0;
	if (NULL != hProcess) {
		FILETIME creationTime;
		FILETIME exitTime;
		FILETIME kernelTime;
		FILETIME userTime;
		if (GetProcessTimes(hProcess, &creationTime, &exitTime, &kernelTime, &userTime)) {
			ULARGE_INTEGER ularge;
			CopyMemory(&ularge,&kernelTime,sizeof(FILETIME)); 

			result = *(__int64 *)&ularge;
			result *= 100; // measured in 100 ns
		}
	}
	CloseHandle(hProcess);
	return result;
}

uint64 _getProcessSystemTimeTicks(HANDLE hProcess) {
    uint64 result = 0;
	if (NULL != hProcess) {
		FILETIME creationTime;
		FILETIME exitTime;
		FILETIME kernelTime;
		FILETIME userTime;
		if (GetProcessTimes(hProcess, &creationTime, &exitTime, &kernelTime, &userTime)) {
			SYSTEMTIME st;
			FILETIME currentTime;
			GetSystemTime(&st);
			SystemTimeToFileTime(&st,&currentTime);
			
			ULARGE_INTEGER currentTimeLarge;
			ULARGE_INTEGER creationTimeLarge;

			CopyMemory(&currentTimeLarge,&currentTime,sizeof(FILETIME)); 
			CopyMemory(&creationTimeLarge,&creationTime,sizeof(FILETIME)); 

			__int64 i64creationTime = *(__int64 *)&creationTimeLarge;
			__int64 i64currentTime = *(__int64 *)&currentTimeLarge;
            result = (i64currentTime - i64creationTime) * 100; // measured in 100 ns
		}
	}
	CloseHandle(hProcess);
	return result;
}
#endif
#endif

// this process

#ifdef VAR_CURRENT_PROCESS_DATA
HANDLE _getCurrentProcess() {
	return GetCurrentProcess();
}
int getCurrentProcessId() {
    return GetCurrentProcessId();
}
#ifdef VAR_IO_DATA
IoStatistics getCurrentProcessIo() {
    return _getProcessIo(_getCurrentProcess());
}
#endif

#ifdef VAR_MEMORY_DATA
uint64 getCurrentProcessMemoryUse() {
    return _getProcessMemory(_getCurrentProcess());
}
#endif

#ifdef VAR_TIME_DATA
uint64 getCurrentProcessUserTimeTicks() {
    return _getProcessUserTimeTicks(_getCurrentProcess());
}

uint64 getCurrentProcessKernelTimeTicks() {
    return _getProcessKernelTimeTicks(_getCurrentProcess());
}

uint64 getCurrentProcessSystemTimeTicks() {
    return _getProcessSystemTimeTicks(_getCurrentProcess());
}
#endif

#ifdef VAR_LOAD_DATA
double getCurrentProcessProcessorLoad() {
    return _calcProcessCPULoad(_getCurrentProcess());
}
#endif
#endif

// arbitrary process
#ifdef VAR_ARBITRARY_PROCESS_DATA

HANDLE _getProcess(const int id) {
    // DWORD
	return OpenProcess(PROCESS_QUERY_INFORMATION |
								PROCESS_VM_READ,
								FALSE, id);
}

#ifdef VAR_IO_DATA
bool isProcessAlive(const int id) {
    HANDLE pHandle = _getProcess(id);
    DWORD lpExitCode;
    bool result = false;
    if (GetExitCodeProcess(pHandle, &lpExitCode)) {
    	result = (lpExitCode == STILL_ACTIVE);
    }
    return result;
}

IoStatistics getProcessIo(const int id) {
    return _getProcessIo(_getProcess(id));
}
#endif
#ifdef VAR_MEMORY_DATA
uint64 getProcessMemoryUse(const int id) {
    return _getProcessMemory(_getProcess(id));
}
#endif
#ifdef VAR_TIME_DATA
uint64 getProcessUserTimeTicks(const int id) {
    return _getProcessUserTimeTicks(_getProcess(id));
}

uint64 getProcessKernelTimeTicks(const int id) {
    return _getProcessKernelTimeTicks(_getProcess(id));
}

uint64 getProcessSystemTimeTicks(const int id) {
    return _getProcessSystemTimeTicks(_getProcess(id));
}
#endif
#ifdef VAR_LOAD_DATA
double getProcessProcessorLoad(const int id) {
    return _calcProcessCPULoad(_getProcess(id));
}
#endif
#endif

#if defined(VAR_ALL_PROCESSES_DATA) && defined(VAR_IO_DATA)
// all processes
IoStatistics getAllProcessesIo() {
    IoStatistics result;
	result.read = 0;
	result.write = 0;
	
	DWORD aProcesses[1024], cbNeeded, cProcesses;
    unsigned int i;

    if ( !EnumProcesses( aProcesses, sizeof(aProcesses), &cbNeeded ) ) {
        return result;
	}

    cProcesses = cbNeeded / sizeof(DWORD);

	IO_COUNTERS counters;
    for ( i = 0; i < cProcesses; i++ ) {
        if( aProcesses[i] != 0 ) {
		    HANDLE hProcess = OpenProcess( PROCESS_QUERY_INFORMATION |
                                   PROCESS_VM_READ,
                                   FALSE, aProcesses[i] );

			if (NULL != hProcess ) {
				if (GetProcessIoCounters(hProcess, &counters)) {
					result.read += counters.ReadTransferCount;
					result.write += counters.WriteTransferCount;
				}
				CloseHandle(hProcess);
			}
		}
    }
	
	return result;
}
#if defined(VAR_ARBITRARY_PROCESS_DATA) || defined(VAR_CURRENT_PROCESS_DATA)
bool isNetworkIoDataIncluded(bool forAll) {
	return true;
}
bool isFileIoDataIncluded(bool forAll) {
	return true;
}
#endif
#endif

// ---------------------------------
// LIFECYCLE
// ---------------------------------
void init() {
	_calcCpuLoad();
}

void done() {
#if (WINVER >= 0x0500)
	boolean comUsed = false;
	if (0 != locator) {
		locator->Release();
		comUsed = true;
	}
	if (0 != service) {
		service->Release();
		comUsed = true;
	}
	if (0 != pRefresher) {
		pRefresher->Release();
		comUsed = true;
	}
	if (0 != pEnum) {
		pEnum->Release();
		comUsed = true;
	}

	if (comUsed) {
	    CoUninitialize(); // terminates calling process, here JVM
	}
#endif
	for (ProcessInfoMap::iterator it = prevProcInfo.begin();
		it != prevProcInfo.end(); it++) {
		delete (*it).second;
	}
	prevProcInfo.clear();

#ifdef VAR_NETWORK_DATA
	if (NetworkQuery) {
		PdhCloseQuery(NetworkQuery);
	}

	for (NetworkCounterVector::iterator it = networkCounter.begin();
		it != networkCounter.end(); it++) {
		NetworkCounter *counter = (*it);
		if (NULL != counter -> adapter) {
			free(counter->adapter);
		}
		delete counter;
	}
	networkCounter.clear();
#endif
}
