
#include "wtypes.h"
#include <windows.h>
#include "Iphlpapi.h"
#include "Psapi.h"

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
static bool firstRunLoad = true;
//static ULARGE_INTEGER	 ul_sys_idle_old;
//static ULARGE_INTEGER  ul_sys_kernel_old;
//static ULARGE_INTEGER  ul_sys_user_old;
//static int cpuload_called = 0;

double calcCpuLoad() {

	// http://en.literateprograms.org/CPU_usage_%28C,_Windows_XP%29

	FILETIME               sys_idle;
	FILETIME               sys_kernel;
	FILETIME               sys_user;

	//ULARGE_INTEGER         ul_sys_idle;
	//ULARGE_INTEGER         ul_sys_kernel;
	//ULARGE_INTEGER         ul_sys_user;

	double usage = 0;

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

		    usage = 100.0 * (( sysTotal - sysIdleDiff) / ((double)sysTotal));
		    if (isnan(usage)) {
		    	usage = 0;
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
	        usage = 0;
	    }
	}

//	CopyMemory(&ul_sys_idle  , &ft_sys_idle  , sizeof(FILETIME)); // Could been optimized away...
//	CopyMemory(&ul_sys_kernel, &ft_sys_kernel, sizeof(FILETIME)); // Could been optimized away...
//	CopyMemory(&ul_sys_user  , &ft_sys_user  , sizeof(FILETIME)); // Could been optimized away...

//	usage  = 100.0 *
//		((((ul_sys_kernel.QuadPart - ul_sys_kernel_old.QuadPart) +
//		    (ul_sys_user.QuadPart   - ul_sys_user_old.QuadPart)) -
//		    (ul_sys_idle.QuadPart-ul_sys_idle_old.QuadPart)))
//		/ ((double)((ul_sys_kernel.QuadPart - ul_sys_kernel_old.QuadPart) +
//		  (ul_sys_user.QuadPart   - ul_sys_user_old.QuadPart)));

//	ul_sys_idle_old.QuadPart   = ul_sys_idle.QuadPart;
//	ul_sys_user_old.QuadPart   = ul_sys_user.QuadPart;
//	ul_sys_kernel_old.QuadPart = ul_sys_kernel.QuadPart;

	return usage;
}

static FILETIME prevSysKernel, prevSysUser;
static FILETIME prevProcKernel, prevProcUser;
static bool firstRunProcessLoad = true;

double _calcProcessCPULoad(DWORD pid) {
	// http://patuxentpc.com/?p=16
    FILETIME sysIdle, sysKernel, sysUser;
    FILETIME procCreation, procExit, procKernel, procUser;

    if (!GetSystemTimesImpl(&sysIdle, &sysKernel, &sysUser) ||
        !GetProcessTimes(GetCurrentProcess(), &procCreation, &procExit, &procKernel, &procUser))
    {
        // can't get time info so return
        return 0;
    }

    double result;
    if (!firstRunProcessLoad) {
        ULONGLONG sysKernelDiff = subtractTime(sysKernel, prevSysKernel);
        ULONGLONG sysUserDiff = subtractTime(sysUser, prevSysUser);

        ULONGLONG procKernelDiff = subtractTime(procKernel, prevProcKernel);
        ULONGLONG procUserDiff = subtractTime(procUser, prevProcUser);

        ULONGLONG sysTotal = sysKernelDiff + sysUserDiff;
        ULONGLONG procTotal = procKernelDiff + procUserDiff;

        result = ((100.0 * procTotal) / ((double)sysTotal));
	    if (isnan(result)) {
	    	result = 0;
	    }
    }

    // save time info before return
    prevSysKernel.dwLowDateTime = sysKernel.dwLowDateTime;
    prevSysKernel.dwHighDateTime = sysKernel.dwHighDateTime;

    prevSysUser.dwLowDateTime = sysUser.dwLowDateTime;
    prevSysUser.dwHighDateTime = sysUser.dwHighDateTime;

    prevProcKernel.dwLowDateTime = procKernel.dwLowDateTime;
    prevProcKernel.dwHighDateTime = procKernel.dwHighDateTime;

    prevProcUser.dwLowDateTime = procUser.dwLowDateTime;
    prevProcUser.dwHighDateTime = procUser.dwHighDateTime;

    // check for first call
    if (firstRunProcessLoad)
    {
        firstRunProcessLoad = false;
        result = 0;
    }

    return result;
}
/*
static ULARGE_INTEGER lastCPU, lastSysCPU, lastUserCPU;
static int numProcessors = 1, _cpuInitialized = 0;

double _calcProcessCPULoad(DWORD pid) {

	FILETIME ftime, fsys, fuser;
	ULARGE_INTEGER now, sys, user;
	double percent;

	GetSystemTimeAsFileTime(&ftime);
	memcpy(&now, &ftime, sizeof(FILETIME));

	HANDLE hProcess = OpenProcess(PROCESS_QUERY_INFORMATION |
								PROCESS_VM_READ,
								FALSE, pid);
	if (NULL != hProcess) {
		GetProcessTimes(hProcess, &ftime, &ftime, &fsys, &fuser);
		memcpy(&sys, &fsys, sizeof(FILETIME));
		memcpy(&user, &fuser, sizeof(FILETIME));
		percent = (sys.QuadPart - lastSysCPU.QuadPart) +
			(user.QuadPart - lastUserCPU.QuadPart);
		if (lastCPU.QuadPart < now.QuadPart && numProcessors > 0) {
			percent /= (now.QuadPart - lastCPU.QuadPart);
			percent /= numProcessors;
		} else {
			percent = 0;
		}
		lastCPU = now;
		lastUserCPU = user;
		lastSysCPU = sys;
		CloseHandle(hProcess);
		result = percent;
		//result = getCurrentSystemLoad() * percent;
	} else {
	    percent = 0;
	}
	return percent;
}*/

double _getCpuLoad() {
   /* if (0 == cpuload_called) {
    	calcCpuLoad();
    	//Sleep(250); // blocks caller...
    	cpuload_called = 1;
    }*/
    return calcCpuLoad();
}

//#endif


//#if (WINVER < 0x0500)
void _init_cpuload(){
	calcCpuLoad();
/*	SYSTEM_INFO sysInfo;
	FILETIME ftime, fsys, fuser;


	GetSystemInfo(&sysInfo);
	numProcessors = sysInfo.dwNumberOfProcessors;


	GetSystemTimeAsFileTime(&ftime);
	memcpy(&lastCPU, &ftime, sizeof(FILETIME));

	GetProcessTimes(GetCurrentProcess(), &ftime, &ftime, &fsys, &fuser);
	memcpy(&lastSysCPU, &fsys, sizeof(FILETIME));
	memcpy(&lastUserCPU, &fuser, sizeof(FILETIME));*/
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

void testWQL() {
	if (0 != service) {
		// execute a query
		IEnumWbemClassObject *enumerator;
		// Win32_Processor
		HRESULT hr = service->ExecQuery( L"WQL", L"SELECT * FROM Win32_Volume WHERE DriveLetter IS NOT NULL", WBEM_FLAG_FORWARD_ONLY, NULL, &enumerator );
		if ( SUCCEEDED( hr ) )
		{
			IWbemClassObject* object = NULL;
			ULONG retcnt;
			VARIANT var_val;
			_variant_t varValue;
			while(enumerator) {
				hr = enumerator->Next( WBEM_INFINITE, 1L, &object, &retcnt );
				if (0 == retcnt) {
					break;
				}

				hr = object->Get( L"FreeSpace", 0, &var_val, NULL, NULL );
				if ( SUCCEEDED( hr ) )
				{
					unsigned long value = getUInt64(&var_val);
					CString s = CString(var_val.bstrVal);
					std::cout << "FreeSpace: " << s << " " << value << std::endl;
					VariantClear(&var_val);
				} else {
					printf("Value query failed\n");
				}
				VariantClear(&var_val);

				object->Release();
			}
			enumerator->Release();
		} else {
			printf("Query failed\n");
		}
	}
}

#ifdef VAR_DEBUG
int getLastLoadError() {
    return lastLoadError;
}
#endif

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
	if (estimatedMax <= 0 && curSpeed < base) {
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
	ComValueData data[1];
	data[0].valueName = L"MaxClockSpeed";
	data[0].function = FUNCTION_SUM;
	data[0].type = TYPE_UINT32;

	// http://msdn.microsoft.com/en-us/library/aa394373%28VS.85%29.aspx

	comValue(L"SELECT MaxClockSpeed FROM Win32_Processor WHERE CpuStatus=1", data, 1);
	return data[0].ui32Result;
}

// in herz
uint64 getCurrentProcessorSpeed() {
	ComValueData data[1];
	data[0].valueName = L"CurrentClockSpeed";
	data[0].function = FUNCTION_AVERAGE;
	data[0].type = TYPE_UINT32;

	// http://msdn.microsoft.com/en-us/library/aa394373%28VS.85%29.aspx

	comValue(L"SELECT CurrentClockSpeed FROM Win32_Processor WHERE CpuStatus=1", data, 1);
	return data[0].ui32Result;
}

double getCurrentSystemLoad() {
	ComValueData data[1];
	data[0].valueName = L"LoadPercentage";
	data[0].function = FUNCTION_AVERAGE;
	data[0].type = TYPE_UINT16;

	// http://msdn.microsoft.com/en-us/library/aa394373%28VS.85%29.aspx

	comValue(L"SELECT LoadPercentage FROM Win32_Processor WHERE CpuStatus=1", data, 1);
	return (double) data[0].ui16Result;
}
#endif

#ifdef VAR_VOLUME_DATA
uint64 getVolumeCapacity() {
	ComValueData data[1];
	data[0].valueName = L"Capacity";
	data[0].function = FUNCTION_SUM;
	data[0].type = TYPE_UINT64;

	// http://msdn.microsoft.com/en-us/library/aa394515%28VS.85%29.aspx

	comValue(L"SELECT Capacity FROM Win32_Volume WHERE DriveLetter IS NOT NULL", data, 1);
	return data[0].ui64Result;
}

uint64 getCurrentVolumeAvail() {
	ComValueData data[1];
	data[0].valueName = L"FreeSpace";
	data[0].function = FUNCTION_SUM;
	data[0].type = TYPE_UINT64;

	// http://msdn.microsoft.com/en-us/library/aa394515%28VS.85%29.aspx

	int count = comValue(L"SELECT FreeSpace FROM Win32_Volume WHERE DriveLetter IS NOT NULL", data, 1);
	return data[0].ui64Result;
}

uint64 getCurrentVolumeUse() {
	ComValueData data[2];
	data[0].valueName = L"Capacity";
	data[0].function = FUNCTION_SUM;
	data[0].type = TYPE_UINT64;
	data[1].valueName = L"FreeSpace";
	data[1].function = FUNCTION_SUM;
	data[1].type = TYPE_UINT64;

	// http://msdn.microsoft.com/en-us/library/aa394515%28VS.85%29.aspx

	int count = comValue(L"SELECT Capacity, FreeSpace FROM Win32_Volume WHERE DriveLetter IS NOT NULL", data, 2);
	return data[0].ui64Result - data[1].ui64Result;
}
#endif

#ifdef VAR_NETWORK_DATA
uint64 getCurrentNetSpeed() { 
	ComValueData data[1];
	data[0].valueName = L"Speed";
	data[0].function = FUNCTION_MAXIMIZE;
	data[0].type = TYPE_UINT64;

	// http://msdn.microsoft.com/en-us/library/aa394216%28VS.85%29.aspx

	comValue(L"SELECT Speed FROM Win32_NetworkAdapter WHERE NetEnabled='True' AND NOT ServiceName LIKE '%VMnetAdapter%'", data, 1);
	return data[0].ui64Result;
}

uint64 getMaxNetSpeed() {
    uint64 maxNetSpeed = 0;
	uint64 netSpeed = 0;

	ComValueData data[2];
	data[0].valueName = L"MaxSpeed";
	data[0].function = FUNCTION_MAXIMIZE;
	data[0].type = TYPE_UINT64;
	data[1].valueName = L"Speed";
	data[1].function = FUNCTION_MAXIMIZE;
	data[1].type = TYPE_UINT64;

	// http://msdn.microsoft.com/en-us/library/aa394216%28VS.85%29.aspx

	comValue(L"SELECT MaxSpeed, Speed FROM Win32_NetworkAdapter WHERE NetEnabled='True' AND NOT ServiceName LIKE '%VMnetAdapter%'", data, 2);
	maxNetSpeed = data[0].ui64Result;
	netSpeed = data[1].ui64Result;

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
	return maxNetSpeed;
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
#ifdef VAR_LOAD_DATA

double _getProcessCPULoad(DWORD pid) {
	double                  result = -1; // disabled due to load
	HRESULT                 hr = S_OK;
    IWbemConfigureRefresher *pConfig = NULL;
    IWbemObjectAccess       **apEnumAccess = NULL;
	IEnumWbemClassObject    *enumerator;
    long                    lPercentProcessorTimeHandle = 0;
    long                    lIDProcessHandle = 0;
    DWORD                   dwNumObjects = 0;
    DWORD                   dwNumReturned = 0;
    DWORD                   dwIDProcess = 0;
    DWORD                   dwPercentProcessorTime = -1;
    DWORD                   dwProcessId = 0;
    DWORD                   i;

	DWORD                   processorNr = GetCurrentProcessorNumber();
	int procNr = 0;
	uint16 otherProcessorLoad = 0;
	uint16 thisProcessorLoad = 0;

	// lazy init

	initializeCom();

	if (0==service) {
		goto EXIT_HERE;
	}

	// determine load of thread on own processor, as this is performance data updates of the underlying structure are required

	if (NULL == pRefresher) {
		if (FAILED (hr = CoCreateInstance(
			CLSID_WbemRefresher,
			NULL,
			CLSCTX_INPROC_SERVER,
			IID_IWbemRefresher,
			(void**) &pRefresher))) {
			result = -2;
			#ifdef VAR_DEBUG
			lastLoadError = hr;
			#endif
			goto EXIT_HERE;
		}
		if (FAILED (hr = pRefresher->QueryInterface(
			IID_IWbemConfigureRefresher,
			(void **)&pConfig))) {
			result = -3;
			#ifdef VAR_DEBUG
			lastLoadError = hr;
			#endif
			goto EXIT_HERE;
		}

		    // Add an enumerator to the refresher.
		if (FAILED (hr = pConfig->AddEnum(
			service,
			L"Win32_PerfFormattedData_PerfProc_Process",
			0,
			NULL,
			&pEnum,
			&lID))) {
			result = -4;
			#ifdef VAR_DEBUG
			lastLoadError = hr;
			#endif
			goto EXIT_HERE;
		}
		pConfig->Release();
		pConfig = NULL;
	}

	if (NULL != pRefresher && NULL != pEnum) {
		result = 0;
		dwNumReturned = 0;
		dwIDProcess = 0;
		dwNumObjects = 0;

		if (FAILED (hr =pRefresher->Refresh(0L))) {
			result = -5;
			goto EXIT_HERE;
		}

		hr = pEnum->GetObjects(0L,
			dwNumObjects,
			apEnumAccess,
			&dwNumReturned);
		// If the buffer was not big enough,
		// allocate a bigger buffer and retry.
		if (hr == WBEM_E_BUFFER_TOO_SMALL
			&& dwNumReturned > dwNumObjects) {
			apEnumAccess = new IWbemObjectAccess*[dwNumReturned];
			if (NULL == apEnumAccess) {
				hr = E_OUTOFMEMORY;
				goto EXIT_HERE;
			}
			SecureZeroMemory(apEnumAccess,
				dwNumReturned*sizeof(IWbemObjectAccess*));
			dwNumObjects = dwNumReturned;

			if (FAILED (hr = pEnum->GetObjects(0L,
				dwNumObjects,
				apEnumAccess,
				&dwNumReturned))) {
				goto EXIT_HERE;
			}
		} else {
			if (hr == WBEM_S_NO_ERROR) {
				hr = WBEM_E_NOT_FOUND;
				goto EXIT_HERE;
			}
		}

		CIMTYPE PercentProcessorTimeType;
		CIMTYPE ProcessHandleType;
		if (FAILED (hr = apEnumAccess[0]->GetPropertyHandle(
			L"PercentProcessorTime",
			&PercentProcessorTimeType,
			&lPercentProcessorTimeHandle))) {
			result = -6;
			goto EXIT_HERE;
		}
		if (FAILED (hr = apEnumAccess[0]->GetPropertyHandle(
			L"IDProcess",
			&ProcessHandleType,
			&lIDProcessHandle))) {
			result = -6;
			goto EXIT_HERE;
		}

		int found = 0;
		for (i = 0; i < dwNumReturned; i++)
		{
			if (FAILED (hr = apEnumAccess[i]->ReadDWORD(
				lIDProcessHandle,
				&dwIDProcess)))
			{
			    result = -7;
				goto EXIT_HERE;
			}

			if (dwIDProcess == pid) {
				found = 1;
				if (FAILED (hr = apEnumAccess[i]->ReadDWORD(
					lPercentProcessorTimeHandle,
					&dwPercentProcessorTime)))
				{
					result = -7;
					goto EXIT_HERE;
				}
				// fallthough and calculated below
			}
			if (0 == found) {
			    result = -7;
		    }

			// Done with the object
			apEnumAccess[i]->Release();
			apEnumAccess[i] = NULL;
		}
	}

	if (NULL != apEnumAccess) {
		delete [] apEnumAccess;
		apEnumAccess = NULL;
	}

	// get loads of all processors and identify load of my processor (current thread)

	hr = service->ExecQuery( L"WQL", L"SELECT LoadPercentage FROM Win32_Processor WHERE CpuStatus=1", WBEM_FLAG_FORWARD_ONLY, NULL, &enumerator );
	if ( SUCCEEDED( hr ) )
	{
		IWbemClassObject* object = NULL;
		ULONG retcnt;
		VARIANT var_val;
		while(enumerator) {
			hr = enumerator->Next( WBEM_INFINITE, 1L, &object, &retcnt );
			if (0 == retcnt) {
				break;
			}

			hr = object->Get(L"LoadPercentage", 0, &var_val, NULL, NULL );
			if ( SUCCEEDED( hr ) )
			{
				if (procNr == processorNr) {
					thisProcessorLoad += var_val.uiVal;
				} else {
					otherProcessorLoad += var_val.uiVal;
				}
			}
			VariantClear(&var_val);

			object->Release();
			procNr++;
		}
	} else {
#ifdef GATHERER_DEBUG
			std::cout << "Query " << query << " failed" << std::endl;
#endif
	}
	if (NULL != enumerator) {
    	enumerator->Release();
		enumerator  = NULL;
	}
	if (dwPercentProcessorTime >= 0) {
		// be careful about 64bit -> double -> JVM
		double thisProcessProcessorLoad = (int) dwPercentProcessorTime;

		double myLoadOfSystem;
		if (1 == procNr || 0 == otherProcessorLoad) {
			myLoadOfSystem = thisProcessorLoad / 100.0;   // (0;1)
		} else {
			double sysLoad = (thisProcessorLoad + otherProcessorLoad) / procNr;   // (0;100)
			myLoadOfSystem = thisProcessorLoad / sysLoad;   // (0;1)
		}
		myLoadOfSystem *= thisProcessProcessorLoad; // (0;100)
		result = myLoadOfSystem;

		// not related to sysload:
		//int percent = (int) dwPercentProcessorTime;
		//result = (double) percent;
	}

    // exit loop here
    EXIT_HERE:

    if (NULL != apEnumAccess) {
        for (i = 0; i < dwNumReturned; i++) {
            if (apEnumAccess[i] != NULL) {
                apEnumAccess[i]->Release();
                apEnumAccess[i] = NULL;
            }
        }
        delete [] apEnumAccess;
    }
	return result;
}
#endif
#endif

// this process

#ifdef VAR_CURRENT_PROCESS_DATA
HANDLE _getCurrentProcess() {
	return GetCurrentProcess();
//	return OpenProcess(PROCESS_QUERY_INFORMATION |
//								PROCESS_VM_READ,
//								FALSE, GetCurrentProcessId());
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
    return (double)_getProcessCPULoad(GetCurrentProcessId());
}
#endif
#endif

// arbitrary process
#ifdef VAR_ARBITRARY_PROCESS_DATA

HANDLE _getProcess(const char* id) {
    DWORD pid = atoi(id);
	return OpenProcess(PROCESS_QUERY_INFORMATION |
								PROCESS_VM_READ,
								FALSE, pid);
}

#ifdef VAR_IO_DATA
IoStatistics getProcessIo(const char* id) {
    return _getProcessIo(_getProcess(id));
}
#endif
#ifdef VAR_MEMORY_DATA
uint64 getProcessMemoryUse(const char* id) {
    return _getProcessMemory(_getProcess(id));
}
#endif
#ifdef VAR_TIME_DATA
uint64 getProcessUserTimeTicks(const char* id) {
    return _getProcessUserTimeTicks(_getProcess(id));
}

uint64 getProcessKernelTimeTicks(const char* id) {
    return _getProcessKernelTimeTicks(_getProcess(id));
}

uint64 getProcessSystemTimeTicks(const char* id) {
    return _getProcessSystemTimeTicks(_getProcess(id));
}
#endif
#ifdef VAR_LOAD_DATA
double getProcessProcessorLoad(const char* id) {
	_bstr_t bstrt(id);
    return _getProcessCPULoad(atoi(id));
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
#endif

// ---------------------------------
// LIFECYCLE
// ---------------------------------
void init() {
//#if (WINVER < 0x0500)
	_init_cpuload();
//#endif
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
}

// ---------------------------------
// OLD
// ---------------------------------

/*
void gatherScreenData(LocutorSystemInfo *info) {
	RECT desktop;
	const HWND hDesktop = GetDesktopWindow();
	GetWindowRect(hDesktop, &desktop);

	info->screenWidth = desktop.right;
	info->screenHeight = desktop.bottom;

	DEVMODE dm;
	dm.dmSize = sizeof(DEVMODE);
	dm.dmDriverExtra = 0;
	EnumDisplaySettings(NULL, ENUM_CURRENT_SETTINGS, &dm);
	info->screenResolution = dm.dmLogPixels;
}

void gatherMemoryStatusExData(LocutorSystemInfo *info) {
	MEMORYSTATUSEX statex;
	statex.dwLength = sizeof (statex);
	GlobalMemoryStatusEx (&statex);

	info->memorySize = statex.ullTotalPhys;
	info->memoryUse = statex.ullTotalPhys - statex.ullAvailPhys;
}

void gatherSystemInfoData(LocutorSystemInfo *info) {
	SYSTEM_INFO sinfo;
	GetSystemInfo(&sinfo);

	info->numberOfProcessors = sinfo.dwNumberOfProcessors;
}

void gatherSystemPowerStatus(LocutorSystemInfo *info) {
	SYSTEM_POWER_STATUS status;
	GetSystemPowerStatus(&status);

	info->hasSystemBattery = !(status.BatteryFlag & 128);
	info->batteryLifePercent = status.BatteryLifePercent;
	if (info->batteryLifePercent > 100) {
		info->batteryLifePercent = -1;
	}
	info->batteryLifeTime = status.BatteryLifeTime;
	info->powerPlugStatus = status.ACLineStatus;
	if (255 == info->powerPlugStatus) {
		info->powerPlugStatus = -1;
	}
}



void gatherMaxNetSpeed(LocutorSystemInfo *info) {
#if (WINVER >= 0x0500)
	ComValueData data[2];
	data[0].valueName = L"MaxSpeed";
	data[0].function = FUNCTION_MAXIMIZE;
	data[0].type = TYPE_UINT64;
	data[1].valueName = L"Speed";
	data[1].function = FUNCTION_MAXIMIZE;
	data[1].type = TYPE_UINT64;

	// http://msdn.microsoft.com/en-us/library/aa394216%28VS.85%29.aspx

	int count = comValue(L"SELECT MaxSpeed, Speed FROM Win32_NetworkAdapter WHERE NetEnabled='True' AND NOT ServiceName LIKE '%VMnetAdapter%'", data, 2);
	info->maxNetSpeed = data[0].ui64Result;
	info->netSpeed = data[1].ui64Result;
# else
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
			if(MIB_IF_TYPE_ETHERNET == pMIBTable->table[i].dwType && IF_OPER_STATUS_OPERATIONAL == pMIBTable->table[i].dwOperStatus)
			{
				if (minVal < 0 || pMIBTable->table[i].dwSpeed < minVal) {
					minVal = pMIBTable->table[i].dwSpeed;
				}
			}
		}
	}
	free(pBuf);
	info->netSpeed = minVal;
#endif
	if (info->maxNetSpeed < info->netSpeed) {
		info->maxNetSpeed = 0;
		unsigned __int64 base = 10000;
		// however .... loop leads to excepts
		info->maxNetSpeed = estimateMaxSpeed(info->netSpeed, base, info->maxNetSpeed);
		base *= 10;
		info->maxNetSpeed = estimateMaxSpeed(info->netSpeed, base, info->maxNetSpeed);
		base *= 10;
		info->maxNetSpeed = estimateMaxSpeed(info->netSpeed, base, info->maxNetSpeed);
		base *= 10;
		info->maxNetSpeed = estimateMaxSpeed(info->netSpeed, base, info->maxNetSpeed);
		base *= 10;
		info->maxNetSpeed = estimateMaxSpeed(info->netSpeed, base, info->maxNetSpeed);
		base *= 10;
		info->maxNetSpeed = estimateMaxSpeed(info->netSpeed, base, info->maxNetSpeed);
		base *= 10;
		info->maxNetSpeed = estimateMaxSpeed(info->netSpeed, base, info->maxNetSpeed);
		base *= 10;
		info->maxNetSpeed = estimateMaxSpeed(info->netSpeed, base, info->maxNetSpeed);
		base *= 10;
	}
}

void gatherProcessorSpeed(LocutorSystemInfo *info) {
#if (WINVER >= 0x0500)

	ComValueData data[3];
	data[0].valueName = L"MaxClockSpeed";
	data[0].function = FUNCTION_SUM;
	data[0].type = TYPE_UINT32;
	data[1].valueName = L"CurrentClockSpeed";
	data[1].function = FUNCTION_SUM;
	data[1].type = TYPE_UINT32;
	data[2].valueName = L"LoadPercentage";
	data[2].function = FUNCTION_SUM;
	data[2].type = TYPE_UINT16;

	// http://msdn.microsoft.com/en-us/library/aa394373%28VS.85%29.aspx

	int count = comValue(L"SELECT MaxClockSpeed, CurrentClockSpeed, LoadPercentage FROM Win32_Processor WHERE CpuStatus=1", data, 3);
	info->maxProcessorSpeed = data[0].ui32Result;
	info->processorSpeed = data[1].ui32Result;
	info->systemLoad = data[2].ui16Result;
	if (count > 0) {
		info->processorSpeed /= count;
		info->systemLoad /= count;
	}
#else
	info->maxProcessorSpeed = 0;
	info->processorSpeed = _getProcessorSpeed();
	info->systemLoad = _getCpuLoad();
#endif
}

void gatherVolumeData(LocutorSystemInfo *info) {
#if (WINVER >= 0x0500)
	ComValueData data[2];
	data[0].valueName = L"Capacity";
	data[0].function = FUNCTION_SUM;
	data[0].type = TYPE_UINT64;
	data[1].valueName = L"FreeSpace";
	data[1].function = FUNCTION_SUM;
	data[1].type = TYPE_UINT64;

	// http://msdn.microsoft.com/en-us/library/aa394216%28VS.85%29.aspx

	int count = comValue(L"SELECT Capacity, FreeSpace FROM Win32_Volume WHERE DriveLetter IS NOT NULL", data, 2);
	info->maxVolumeCapacity = data[0].ui64Result;
	info->volumeUse = data[1].ui64Result;
#else
	LPCTSTR DiskDirectory = L"C:\\";
	__int64 AvailableBytes;
	__int64 TotalBytes;
	__int64 FreeBytes;

	GetDiskFreeSpaceEx(DiskDirectory, (PULARGE_INTEGER) &AvailableBytes,
		(PULARGE_INTEGER) &TotalBytes, (PULARGE_INTEGER) &FreeBytes);
	info->maxVolumeCapacity = TotalBytes;
	info->volumeUse = AvailableBytes;
#endif
}

*/
