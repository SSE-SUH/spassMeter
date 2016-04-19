//#include "de_uni_hildesheim_sse_system_DataGatherer.h"
#include <jni.h>
//experiment: direct access to JVM thread time, not via JMX
#ifndef ANDROID
  #ifdef _WINDOWS
    #include <windows.h>
  #endif
  #include "jvm.h"
  #include "jmm.h"
#endif
#ifdef WITH_JVMTI
#include "jvmti.h"
#include <list>
#endif
#ifdef VAR_WIFI_DATA  
#include "gears/device_data_provider.h"
typedef std::set<AccessPointData, AccessPointDataLess> AccessPointDataSet;
#else
#include <stdio.h>
#include "gears/mutex.h"
#ifdef ANDROID
#include "android/log.h"
#include "android_defs.h"
#include <pthread.h>
#include <time.h>
#include <errno.h>
#endif
#endif
#include "data_gatherer.h"
#include "unallocationinfo.h"

#ifndef WINVER
char* _itoa(int value, char* result, int base) {
	// check that the base if valid
	if (base < 2 || base > 36) { *result = '\0'; return result; }

	char* ptr = result, *ptr1 = result, tmp_char;
	int tmp_value;

	do {
		tmp_value = value;
		value /= base;
		*ptr++ = "zyxwvutsrqponmlkjihgfedcba9876543210123456789abcdefghijklmnopqrstuvwxyz" [35 + (tmp_value - value * base)];
	} while ( value );

	// Apply negative sign
	if (tmp_value < 0) *ptr++ = '-';
	*ptr-- = '\0';
	while(ptr1 < ptr) {
		tmp_char = *ptr;
		*ptr--= *ptr1;
		*ptr1++ = tmp_char;
	}
	return result;
}
#endif

#ifdef WITH_JVMTI
// stores a pointer to the current (one) JVMTI environment (initialized in on_load)
static jvmtiEnv *jvmti = 0;
#endif
#ifndef ANDROID
const JmmInterface *jmm_interface = NULL;
#endif

// ------------ helper ----------------

jfieldID getField(JNIEnv *env, jclass c, const char *name,
			const char *sig) {
	jfieldID field = env->GetFieldID(c, name, sig);
	if (0 == field) {
		printf("%s NOT FOUND\n", name);
	}
	return field;
}

jobject toJavaIoStatistics(JNIEnv *env, IoStatistics ioStatistics) {
	jclass ios_class = env->FindClass("de/uni_hildesheim/sse/system/IoStatistics");
	if (env->ExceptionOccurred()) {
		env->ExceptionDescribe();
	}
	jmethodID ios_cons = env->GetMethodID(ios_class, "<init>", "()V");
	if (0 == ios_cons) {
		printf("CONSTRUCTOR IoStatistics NOT FOUND\n");
	}
	jobject ios = env->NewObject(ios_class, ios_cons);
	jfieldID field = getField(env, ios_class, "read", "J");
	env->SetLongField(ios, field, ioStatistics.read);
	field = getField(env, ios_class, "write", "J");
	env->SetLongField(ios, field, ioStatistics.write);
	return ios;
}

#ifdef VAR_WIFI_DATA
/*
 * Converts a Gears WIFI data collection to a java Array consisting of
 * AccessPointData instances.
 */
jobjectArray wifiToAPArray(JNIEnv *env, WifiData *wifiData) {
	jclass ap_class = env->FindClass("de/uni_hildesheim/sse/system/AccessPointData");
	if (env->ExceptionOccurred()) {
		env->ExceptionDescribe();
	}
	jmethodID ap_cons = env->GetMethodID(ap_class, "<init>", "()V");
	if (0 == ap_cons) {
		printf("CONSTRUCTOR ACCESSPOINTDATA NOT FOUND\n");
	}

	jfieldID macAddress = getField(env, ap_class, "macAddress", "Ljava/lang/String;");
	jfieldID radioSignalStrength = getField(env, ap_class, "radioSignalStrength", "I");
	jfieldID age = getField(env, ap_class, "age", "I");
	jfieldID channel = getField(env, ap_class, "channel", "I");
	jfieldID signalToNoise = getField(env, ap_class, "signalToNoise", "I");
	jfieldID ssid = getField(env, ap_class, "ssid", "Ljava/lang/String;");

	int size = wifiData->access_point_data.size();
	jobjectArray result = env->NewObjectArray(size, ap_class, 0);
//	field = getField(env, lsi_class, "accessPoints", "[Lde/uni_hildesheim/sse/system/AccessPointData;");
//    env->SetObjectField(lsi, field, result);

	int count = 0;
	for (AccessPointDataSet::const_iterator iter = wifiData->access_point_data.begin();
         iter != wifiData->access_point_data.end();
         iter++) {
		AccessPointData data = *iter;
		jobject ap = env->NewObject(ap_class, ap_cons);
		env->SetObjectArrayElement(result, count, ap);
		jstring str = env->NewString(data.mac_address.c_str(), data.mac_address.length());
		env->SetObjectField(ap, macAddress, str);
		env->SetIntField(ap, radioSignalStrength, data.radio_signal_strength);
		env->SetIntField(ap, age, data.age);
		env->SetIntField(ap, channel, data.channel);
		env->SetIntField(ap, signalToNoise, data.signal_to_noise);
		str = env->NewString(data.ssid.c_str(), data.ssid.length());
		env->SetObjectField(ap, ssid, str);
		count++;
	}
	return result;
}
#endif

#ifdef WITH_JVMTI

void createAndThrowThrowableFromJVMTIErrorCode(JNIEnv * jnienv,
		jvmtiError errorCode) {
    const char * throwableClassName = NULL;
    const char * message            = NULL;

    switch ( errorCode ) {
        case JVMTI_ERROR_NULL_POINTER:
                throwableClassName = "java/lang/NullPointerException";
                break;

        case JVMTI_ERROR_ILLEGAL_ARGUMENT:
                throwableClassName = "java/lang/IllegalArgumentException";
                break;

        case JVMTI_ERROR_OUT_OF_MEMORY:
                throwableClassName = "java/lang/OutOfMemoryError";
                break;

        case JVMTI_ERROR_CIRCULAR_CLASS_DEFINITION:
                throwableClassName = "java/lang/ClassCircularityError";
                break;

        case JVMTI_ERROR_FAILS_VERIFICATION:
                throwableClassName = "java/lang/VerifyError";
                break;

        case JVMTI_ERROR_UNSUPPORTED_REDEFINITION_METHOD_ADDED:
                throwableClassName = "java/lang/UnsupportedOperationException";
                message = "class redefinition failed: attempted to add a method";
                break;

        case JVMTI_ERROR_UNSUPPORTED_REDEFINITION_SCHEMA_CHANGED:
                throwableClassName = "java/lang/UnsupportedOperationException";
                message = "class redefinition failed: attempted to change the schema (add/remove fields)";
                break;

        case JVMTI_ERROR_UNSUPPORTED_REDEFINITION_HIERARCHY_CHANGED:
                throwableClassName = "java/lang/UnsupportedOperationException";
                message = "class redefinition failed: attempted to change superclass or interfaces";
                break;

        case JVMTI_ERROR_UNSUPPORTED_REDEFINITION_METHOD_DELETED:
                throwableClassName = "java/lang/UnsupportedOperationException";
                message = "class redefinition failed: attempted to delete a method";
                break;

        case JVMTI_ERROR_UNSUPPORTED_REDEFINITION_CLASS_MODIFIERS_CHANGED:
                throwableClassName = "java/lang/UnsupportedOperationException";
                message = "class redefinition failed: attempted to change the class modifiers";
                break;

        case JVMTI_ERROR_UNSUPPORTED_REDEFINITION_METHOD_MODIFIERS_CHANGED:
                throwableClassName = "java/lang/UnsupportedOperationException";
                message = "class redefinition failed: attempted to change method modifiers";
                break;

        case JVMTI_ERROR_UNSUPPORTED_VERSION:
                throwableClassName = "java/lang/UnsupportedClassVersionError";
                break;

        case JVMTI_ERROR_NAMES_DONT_MATCH:
                throwableClassName = "java/lang/NoClassDefFoundError";
                message = "class names don't match";
                break;

        case JVMTI_ERROR_INVALID_CLASS_FORMAT:
                throwableClassName = "java/lang/ClassFormatError";
                break;

        case JVMTI_ERROR_UNMODIFIABLE_CLASS:
                throwableClassName = "java/lang/instrument/UnmodifiableClassException";
                break;

        case JVMTI_ERROR_INVALID_CLASS:
                throwableClassName = "java/lang/InternalError";
                message = "class redefinition failed: invalid class";
                break;

        case JVMTI_ERROR_CLASS_LOADER_UNSUPPORTED:
                throwableClassName = "java/lang/UnsupportedOperationException";
                message = "unsupported operation";
                break;

        case JVMTI_ERROR_INTERNAL:
        default:
                throwableClassName = "java/lang/InternalError";
                break;
        }

    if ( message != NULL ) {
    	message = "";
    }
    jclass exceptionClass = jnienv->FindClass(throwableClassName);
    jboolean result = jnienv->ExceptionCheck();
    if ( result ) {
        jnienv->ExceptionClear();
    }
    jnienv->ThrowNew(exceptionClass, message);
}

#endif

// ------------ interface functions ----------------

#ifdef VAR_WIFI_DATA  
extern "C" JNIEXPORT jobjectArray JNICALL Java_de_uni_1hildesheim_sse_system_deflt_DataGatherer_gatherWifiSignals0
  (JNIEnv *env, jclass c, jint timeout) {
	WifiData wifiData = getWifiData(timeout);
	return wifiToAPArray(env, &wifiData);
}
#endif

// screen
#ifdef VAR_SCREEN_DATA
extern "C" JNIEXPORT jint JNICALL Java_de_uni_1hildesheim_sse_system_deflt_ScreenDataGatherer_getScreenWidth0
  (JNIEnv *env, jclass c) {
    return getScreenWidth();
}

extern "C" JNIEXPORT jint JNICALL Java_de_uni_1hildesheim_sse_system_deflt_ScreenDataGatherer_getScreenHeight0
  (JNIEnv *env, jclass c) {
    return getScreenHeight();
}

extern "C" JNIEXPORT jint JNICALL Java_de_uni_1hildesheim_sse_system_deflt_ScreenDataGatherer_getScreenResolution0
  (JNIEnv *env, jclass c) {
	return getScreenResolution();
}
#endif

// main memory

#ifdef VAR_MEMORY_DATA
extern "C" JNIEXPORT jlong JNICALL Java_de_uni_1hildesheim_sse_system_deflt_MemoryDataGatherer_getMemoryCapacity0
  (JNIEnv *env, jclass c) {
   return getMemoryCapacity();
}

extern "C" JNIEXPORT jlong JNICALL Java_de_uni_1hildesheim_sse_system_deflt_MemoryDataGatherer_getCurrentMemoryAvail0
  (JNIEnv *env, jclass c) {
    return getCurrentMemoryAvail();
}

extern "C" JNIEXPORT jlong JNICALL Java_de_uni_1hildesheim_sse_system_deflt_MemoryDataGatherer_getCurrentMemoryUse0
  (JNIEnv *env, jclass c) {
    return getCurrentMemoryUse();
}

// all in one, be careful
extern "C" JNIEXPORT jint JNICALL Java_de_uni_1hildesheim_sse_system_deflt_DataGatherer_redefineMultiClasses0
  (JNIEnv *env, jclass c, jobjectArray classes, jobjectArray bytecode) {
	jint status = -1;
#if defined(WITH_JVMTI)
    jsize cCount = env->GetArrayLength(classes);
    jsize bCount = env->GetArrayLength(bytecode);
    if (cCount == bCount && cCount > 0 && 0 != jvmti) {
    	jvmtiClassDefinition *clsDef = (jvmtiClassDefinition*) malloc(cCount * sizeof(jvmtiClassDefinition));
    	for (int i = 0; i < cCount; i++) {
    		clsDef[i].klass = (jclass) env->GetObjectArrayElement(classes, i);
    		jbyteArray bcCol = (jbyteArray) env->GetObjectArrayElement(bytecode, i);
    		int bcSize = env->GetArrayLength(bcCol);
    		clsDef[i].class_byte_count = bcSize;
    		clsDef[i].class_bytes = (unsigned char*) malloc(bcSize);
    		env->GetByteArrayRegion(bcCol, 0, bcSize, (jbyte*) clsDef[i].class_bytes);
    	}
    	status = jvmti->RedefineClasses(cCount, clsDef);
    	for (int i = 0; i < cCount; i++) {
    		free((void*) clsDef[i].class_bytes);
    	}
    	free(clsDef);
    }
#endif
    return status;
}

// multiple but one-by-one
extern "C" JNIEXPORT jint JNICALL Java_de_uni_1hildesheim_sse_system_deflt_DataGatherer_redefineClasses0
  (JNIEnv *env, jclass c, jobjectArray classes, jobjectArray bytecode) {
	jint status = -1;
#if defined(WITH_JVMTI)
    jsize cCount = env->GetArrayLength(classes);
    jsize bCount = env->GetArrayLength(bytecode);
    if (cCount == bCount && cCount > 0 && 0 != jvmti) {
    	jvmtiClassDefinition clsDef;
    	for (int i = 0; i < cCount; i++) {
    		clsDef.klass = (jclass) env->GetObjectArrayElement(classes, i);
    		jbyteArray bcCol = (jbyteArray) env->GetObjectArrayElement(bytecode, i);
    		int bcSize = env->GetArrayLength(bcCol);
    		clsDef.class_byte_count = bcSize;
    		clsDef.class_bytes = (unsigned char*) malloc(bcSize);
    		env->GetByteArrayRegion(bcCol, 0, bcSize, (jbyte*) clsDef.class_bytes);
        	status = jvmti->RedefineClasses(1, &clsDef);
    		free((void*) clsDef.class_bytes);
    	}
    }
#endif
    return status;
}

// just one
extern "C" JNIEXPORT jint JNICALL Java_de_uni_1hildesheim_sse_system_deflt_DataGatherer_redefineClass0
  (JNIEnv *env, jclass c, jclass clazz, jbyteArray bytecode) {
	jint status = -1;
#if defined(WITH_JVMTI)
    if (0 != jvmti) {
    	jvmtiClassDefinition clsDef;
        clsDef.klass = clazz;
		int bcSize = env->GetArrayLength(bytecode);
    	clsDef.class_byte_count = bcSize;
    	clsDef.class_bytes = (unsigned char*) malloc(bcSize);
		env->GetByteArrayRegion(bytecode, 0, bcSize, (jbyte*) clsDef.class_bytes);
    	status = jvmti->RedefineClasses(1, &clsDef);
		free((void*) clsDef.class_bytes);
    }
#endif
    return status;
}

extern "C" JNIEXPORT void JNICALL Java_de_uni_1hildesheim_sse_system_deflt_MemoryDataGatherer_recordUnallocation0
  (JNIEnv *env, jclass c, jobject allocated, jlong size, jint recId)  {

    storeUnallocationInfo(env,
	#if defined(WITH_JVMTI)
	jvmti, 
	#endif
	allocated, size, recId);
}

extern "C" JNIEXPORT void JNICALL Java_de_uni_1hildesheim_sse_system_deflt_MemoryDataGatherer_recordUnallocation2
  (JNIEnv *env, jclass c, jlong tag, jlong size, jint recId)  {
    storeUnallocationInfoTag(tag, size, recId);
}

extern "C" JNIEXPORT void JNICALL Java_de_uni_1hildesheim_sse_system_deflt_MemoryDataGatherer_recordUnallocation4
  (JNIEnv *env, jclass c, jlong tag)  {
	unallocate(tag);
}


extern "C" JNIEXPORT void JNICALL Java_de_uni_1hildesheim_sse_system_deflt_MemoryDataGatherer_receiveUnallocations0
  (JNIEnv *env, jclass c, jobject receiver) {
	transferAggregatedUnallocationToJava(env, receiver);
}


#if defined(WITH_JVMTI) && defined(WITH_JVMTI_MEMORY)

static void JNICALL ObjectFreeHandler(jvmtiEnv *jvmti_env, jlong tag_ptr) {
    freeUnallocationInfo(tag_ptr);
}
#endif
#endif
#ifdef VAR_OBJECT_SIZE
extern "C" JNIEXPORT jlong JNICALL Java_de_uni_1hildesheim_sse_system_deflt_MemoryDataGatherer_getObjectSize0
  (JNIEnv * env, jclass c, jobject objectToSize) {
#ifdef WITH_JVMTI
    if (0 != jvmti) {
		jlong       objectSize  = -1;
		jvmtiError  jvmtierror  = JVMTI_ERROR_NONE;

		jvmtierror = jvmti->GetObjectSize(objectToSize, &objectSize);
		//jplis_assert(jvmtierror == JVMTI_ERROR_NONE);
		if ( jvmtierror != JVMTI_ERROR_NONE ) {
			createAndThrowThrowableFromJVMTIErrorCode(env, jvmtierror);
		}

		//mapThrownThrowableIfNecessary(jnienv, mapAllCheckedToInternalErrorMapper);
		return objectSize;
    } else {
	    return 0;
	}
# else
	return 0;
#endif
}
#endif

extern "C" JNIEXPORT jboolean JNICALL Java_de_uni_1hildesheim_sse_system_deflt_DataGatherer_supportsJVMTI0
  (JNIEnv * env, jclass c) {
     #ifdef WITH_JVMTI
	 return (0 != jvmti);
	 #else
	 return 0;
	 #endif
}

// processor 

#ifdef VAR_PROCESSOR_DATA
extern "C" JNIEXPORT jint JNICALL Java_de_uni_1hildesheim_sse_system_deflt_ProcessorDataGatherer_getNumberOfProcessors0
  (JNIEnv *env, jclass c) {
    return getNumberOfProcessors();
}

extern "C" JNIEXPORT jint JNICALL Java_de_uni_1hildesheim_sse_system_deflt_ProcessorDataGatherer_getMaxProcessorSpeed0
  (JNIEnv *env, jclass c) {
    return getMaxProcessorSpeed();
}

extern "C" JNIEXPORT jint JNICALL Java_de_uni_1hildesheim_sse_system_deflt_ProcessorDataGatherer_getCurrentProcessorSpeed0
  (JNIEnv *env, jclass c) {
    return  getCurrentProcessorSpeed();
}

extern "C" JNIEXPORT double JNICALL Java_de_uni_1hildesheim_sse_system_deflt_ProcessorDataGatherer_getCurrentSystemLoad0
  (JNIEnv *env, jclass c) {
    return getCurrentSystemLoad();
}
#endif

// volumes

#ifdef VAR_VOLUME_DATA
extern "C" JNIEXPORT jlong JNICALL Java_de_uni_1hildesheim_sse_system_deflt_VolumeDataGatherer_getVolumeCapacity0
  (JNIEnv *env, jclass c) {
    return getVolumeCapacity();
}

extern "C" JNIEXPORT jlong JNICALL Java_de_uni_1hildesheim_sse_system_deflt_VolumeDataGatherer_getCurrentVolumeAvail0
  (JNIEnv *env, jclass c) {
    return getCurrentVolumeAvail();
}

extern "C" JNIEXPORT jlong JNICALL Java_de_uni_1hildesheim_sse_system_deflt_VolumeDataGatherer_getCurrentVolumeUse0
  (JNIEnv *env, jclass c) {
    return getCurrentVolumeUse();
}
#endif

// network

#ifdef VAR_NETWORK_DATA
extern "C" JNIEXPORT jlong JNICALL Java_de_uni_1hildesheim_sse_system_deflt_NetworkDataGatherer_getCurrentNetSpeed0
  (JNIEnv *env, jclass c) {
    return getCurrentNetSpeed();
}

extern "C" JNIEXPORT jlong JNICALL Java_de_uni_1hildesheim_sse_system_deflt_NetworkDataGatherer_getMaxNetSpeed0
  (JNIEnv *env, jclass c) {
    return getMaxNetSpeed();
}

#endif

// power

#ifdef VAR_ENERGY_DATA
extern "C" JNIEXPORT jboolean JNICALL Java_de_uni_1hildesheim_sse_system_deflt_BatteryDataGatherer_hasSystemBattery0
  (JNIEnv *env, jclass c) {
    return hasSystemBattery();
}

extern "C" JNIEXPORT jint JNICALL Java_de_uni_1hildesheim_sse_system_deflt_BatteryDataGatherer_getBatteryLifePercent0
  (JNIEnv *env, jclass c) {
    return getBatteryLifePercent();
}

extern "C" JNIEXPORT jint JNICALL Java_de_uni_1hildesheim_sse_system_deflt_BatteryDataGatherer_getBatteryLifeTime0
  (JNIEnv *env, jclass c) {
    return getBatteryLifeTime();
}

extern "C" JNIEXPORT jint JNICALL Java_de_uni_1hildesheim_sse_system_deflt_BatteryDataGatherer_getPowerPlugStatus0
  (JNIEnv *env, jclass c) {
    return getPowerPlugStatus();
}
#endif

// this process

#ifdef VAR_CURRENT_PROCESS_DATA
extern "C" JNIEXPORT jstring JNICALL Java_de_uni_1hildesheim_sse_system_deflt_ThisProcessDataGatherer_getCurrentProcessID0
  (JNIEnv *env, jclass c) {
	char buf[20];
	_itoa(getCurrentProcessId(), buf, 10);
	return env->NewStringUTF(buf);
}

#ifdef VAR_IO_DATA
extern "C" JNIEXPORT jobject JNICALL Java_de_uni_1hildesheim_sse_system_deflt_ThisProcessDataGatherer_getCurrentProcessIo0
  (JNIEnv *env, jclass c) {
    return toJavaIoStatistics(env, getCurrentProcessIo());
}
#endif

#ifdef VAR_MEMORY_DATA
extern "C" JNIEXPORT jlong JNICALL Java_de_uni_1hildesheim_sse_system_deflt_ThisProcessDataGatherer_getCurrentProcessMemoryUse0
  (JNIEnv *env, jclass c) {
    return getCurrentProcessMemoryUse();
}
#endif

#ifdef VAR_TIME_DATA
extern "C" JNIEXPORT jlong JNICALL Java_de_uni_1hildesheim_sse_system_deflt_ThisProcessDataGatherer_getCurrentProcessUserTimeTicks0
  (JNIEnv *env, jclass c) {
    return getCurrentProcessUserTimeTicks();
}

extern "C" JNIEXPORT jlong JNICALL Java_de_uni_1hildesheim_sse_system_deflt_ThisProcessDataGatherer_getCurrentProcessKernelTimeTicks0
  (JNIEnv *env, jclass c) {
    return getCurrentProcessKernelTimeTicks();
}

extern "C" JNIEXPORT jlong JNICALL Java_de_uni_1hildesheim_sse_system_deflt_ThisProcessDataGatherer_getCurrentProcessSystemTimeTicks0
  (JNIEnv *env, jclass c) {
    return getCurrentProcessSystemTimeTicks();
}

/*
extern "C" JNIEXPORT jlong Java_de_uni_1hildesheim_sse_system_deflt_ThisProcessDataGatherer_getCurrentProcessCycleTimeTicks0
  (JNIEnv *env, jclass c) {
    return getCurrentProcessSystemTimeTicks();
}*/
#endif

#ifdef VAR_LOAD_DATA
extern "C" JNIEXPORT jdouble Java_de_uni_1hildesheim_sse_system_deflt_ThisProcessDataGatherer_getCurrentProcessProcessorLoad0
  (JNIEnv *env, jclass c) {
    return getCurrentProcessProcessorLoad();
}
#endif
#endif

/*#ifdef VAR_DEBUG
extern "C" JNIEXPORT jlong Java_de_uni_1hildesheim_sse_system_deflt_DataGatherer_getLastError
  (JNIEnv *env, jclass c) {
    #ifdef _WINDOWS
    return GetLastError();
	#else
	return 0;
	#endif
}

extern "C" JNIEXPORT jlong JNICALL Java_de_uni_1hildesheim_sse_system_deflt_DataGatherer_getLastLoadError
  (JNIEnv *env, jclass c) {
    return getLastLoadError();
}
#endif*/

// arbitrary process
#ifdef VAR_ARBITRARY_PROCESS_DATA

extern "C" JNIEXPORT jboolean JNICALL Java_de_uni_1hildesheim_sse_system_deflt_ProcessDataGatherer_isProcessAlive0
  (JNIEnv *env, jclass c, jint pid) {
	return isProcessAlive(pid);
}

#ifdef VAR_IO_DATA
extern "C" JNIEXPORT jobject JNICALL Java_de_uni_1hildesheim_sse_system_deflt_ProcessDataGatherer_getProcessIo0
  (JNIEnv *env, jclass c, jint pid) {
	return toJavaIoStatistics(env, getProcessIo(pid));
}
#endif

#ifdef VAR_MEMORY_DATA
extern "C" JNIEXPORT jlong JNICALL Java_de_uni_1hildesheim_sse_system_deflt_ProcessDataGatherer_getProcessMemoryUse0
  (JNIEnv *env, jclass c, jint pid) {
    return getProcessMemoryUse(pid);
}
#endif

#ifdef VAR_TIME_DATA
extern "C" JNIEXPORT jlong JNICALL Java_de_uni_1hildesheim_sse_system_deflt_ProcessDataGatherer_getProcessUserTimeTicks0
  (JNIEnv *env, jclass c, jint pid) {
    return getProcessUserTimeTicks(pid);
}

extern "C" JNIEXPORT jlong JNICALL Java_de_uni_1hildesheim_sse_system_deflt_ProcessDataGatherer_getProcessKernelTimeTicks0
  (JNIEnv *env, jclass c, jint pid) {
    return getProcessKernelTimeTicks(pid);
}

extern "C" JNIEXPORT jlong JNICALL Java_de_uni_1hildesheim_sse_system_deflt_ProcessDataGatherer_getProcessSystemTimeTicks0
  (JNIEnv *env, jclass c, jint pid) {
    return getProcessSystemTimeTicks(pid);
}

#endif

#ifdef VAR_LOAD_DATA
extern "C" JNIEXPORT jdouble Java_de_uni_1hildesheim_sse_system_deflt_ProcessDataGatherer_getProcessProcessorLoad0
  (JNIEnv *env, jclass c, jint pid) {
    return getProcessProcessorLoad(pid);
}
#endif
#endif

// all processes

#ifdef VAR_ALL_PROCESSES_DATA
#ifdef VAR_IO_DATA
extern "C" JNIEXPORT jobject JNICALL Java_de_uni_1hildesheim_sse_system_deflt_ThisProcessDataGatherer_getAllProcessesIo0
  (JNIEnv *env, jclass c) {
    return toJavaIoStatistics(env, getAllProcessesIo());
}

extern "C" JNIEXPORT jobject Java_de_uni_1hildesheim_sse_system_deflt_ProcessDataGatherer_getAllProcessesIo0
  (JNIEnv *env, jclass c) {
    return toJavaIoStatistics(env, getAllProcessesIo());
}
#ifdef VAR_ARBITRARY_PROCESS_DATA
extern "C" JNIEXPORT jboolean JNICALL Java_de_uni_1hildesheim_sse_system_deflt_ProcessDataGatherer_isNetworkIoDataIncluded0
  (JNIEnv *, jclass c, jboolean forAll) {
	return isNetworkIoDataIncluded(forAll);
}
extern "C" JNIEXPORT jboolean JNICALL Java_de_uni_1hildesheim_sse_system_deflt_ProcessDataGatherer_isFileIoDataIncluded0
  (JNIEnv *, jclass c, jboolean forAll) {
	return isFileIoDataIncluded(forAll);
}
#endif
#ifdef VAR_CURRENT_PROCESS_DATA
extern "C" JNIEXPORT jboolean JNICALL Java_de_uni_1hildesheim_sse_system_deflt_ThisProcessDataGatherer_isNetworkIoDataIncluded0
  (JNIEnv *, jclass c, jboolean forAll) {
	return isNetworkIoDataIncluded(forAll);
}
extern "C" JNIEXPORT jboolean JNICALL Java_de_uni_1hildesheim_sse_system_deflt_ThisProcessDataGatherer_isFileIoDataIncluded0
  (JNIEnv *, jclass c, jboolean forAll) {
	return isFileIoDataIncluded(forAll);
}
#endif

#endif
#endif

// native thread timing

#ifdef ANDROID
extern "C" JNIEXPORT jboolean JNICALL Java_de_uni_1hildesheim_sse_system_deflt_ThreadDataGatherer_supportsCpuThreadTiming0
  (JNIEnv *, jclass) {
	return supportsCpuThreadTiming();
}
extern "C" JNIEXPORT jlong JNICALL Java_de_uni_1hildesheim_sse_system_deflt_ThreadDataGatherer_getCpuThreadTime0
  (JNIEnv *, jclass, jlong threadId) {
	return getCpuThreadTime(threadId);
}
extern "C" JNIEXPORT void JNICALL Java_de_uni_1hildesheim_sse_system_deflt_DataGatherer_registerThread0
  (JNIEnv *, jclass, jlong threadId, jboolean reg) {
	registerThread(threadId, reg);
}
#else
//experiment: direct access to JVM thread time, not via JMX
extern "C" JNIEXPORT jlong JNICALL Java_de_uni_1hildesheim_sse_system_deflt_ThreadDataGatherer_getCpuThreadTime0
  (JNIEnv *env, jclass, jlong threadId) {
    if (NULL == jmm_interface) {
        return 0;
    } else {
	    return jmm_interface->GetThreadCpuTimeWithKind(env, threadId, JNI_TRUE); //user+sys
	}
}	
#endif

// lifecycle

extern "C" JNIEXPORT jint JNICALL JNI_OnLoad( JavaVM *vm, void *pvt ) {
  #ifdef WITH_JVMTI
	jvmtiEnv *temp_jvmti;
	jint res = vm->GetEnv((void**)&temp_jvmti, JVMTI_VERSION_1_0);
	if (res != JNI_OK || temp_jvmti == NULL) {
		printf("ERROR: Unable to access JVMTI Version 1 (0x%x),"
				" is your J2SE a 1.5 or newer version?"
				" JNIEnv's GetEnv() returned %d\n",
			   JVMTI_VERSION_1, res);
	} else {
	    jvmti = temp_jvmti;

	    jvmtiCapabilities capa;
	    (void)memset(&capa, 0, sizeof(jvmtiCapabilities));
	    capa.can_tag_objects = 1;
	    capa.can_generate_object_free_events = 1;
	    capa.can_redefine_classes = 1;
	    jvmtiError error = jvmti->AddCapabilities(&capa);

	    if (JVMTI_ERROR_NONE != error) {
	        printf("Unable to get necessary JVMTI capabilities (%d).", error);
		} else {
			jvmtiEventCallbacks callbacks;
			(void)memset(&callbacks, 0, sizeof(callbacks));
			callbacks.ObjectFree = &ObjectFreeHandler;

			error = jvmti->SetEventCallbacks(&callbacks, (jint)sizeof(callbacks));
			if (JVMTI_ERROR_NONE != error) {
		        printf("Cannot set jvmti callbacks (%d).", error);
			} else {
			    error = jvmti->SetEventNotificationMode(JVMTI_ENABLE,
			        JVMTI_EVENT_OBJECT_FREE, (jthread)NULL);
				if (JVMTI_ERROR_NONE != error) {
					printf("Cannot set jvmti event notification mode (%d).", error);
				}
			}
		}
	}
  #endif
  #ifndef ANDROID
	JNIEnv* env;
	res = vm->GetEnv((void**)&env, JNI_VERSION_1_2);
	if (res != JNI_OK || env == NULL) {
		printf("ERROR: Unable to access JNI Version 1 (0x%x),"
				" is your J2SE a 1.2 or newer version?"
				" JNIEnv's GetEnv() returned %d\n",
			   JNI_VERSION_1_2, res);
	} else {
		jmm_interface = (JmmInterface*) JVM_GetManagement(JMM_VERSION_1_0);
		if (jmm_interface == NULL) {
		    printf("ERROR: Unable to access JMM Version 1 (0x%x),"
				" is your J2SE a 1.2 or newer version?",
			   JMM_VERSION_1_0);
		}
	}
  #endif
	initUnallocationInfoMap();
	return JNI_VERSION_1_2;
}

JNIEXPORT void JNICALL JNI_OnUnload( JavaVM *vm, void *pvt ) {
  #ifdef WITH_JVMTI
	if (0 != jvmti) {
		jvmti->DisposeEnvironment();
		jvmti = 0;
	}
  #endif
	doneUnallocation();
	done();
}
