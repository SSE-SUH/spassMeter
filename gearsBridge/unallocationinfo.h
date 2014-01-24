#ifndef UNALLOCATION_INFO_H
#define UNALLOCATION_INFO_H

#include <string.h>
#include <jni.h>
#include "hashmap.h"
#include "gears/mutex.h"
#include "unallocationinfo_common.h"
#ifdef WITH_JVMTI
#include "jvmti.h"
#endif

// helper functionality for native unallocation information
// shall be a class, implementation shall not be in header...

// we do not use JVMI locks as this code shall work on Android and JVM
static Mutex unallocationLock;
static Mutex freeLock;

typedef HashMap<jint, jlong> AggregatedUnallocationMap;
typedef HashMap<jlong, jlong> UnallocationInfoMap;
static AggregatedUnallocationMap aggregatedUnallocationMap;
static UnallocationInfoMap unallocationInfoMap;

void storeUnallocationInfoTag(jlong tag, jlong size, jint recId);

void storeUnallocationInfo(JNIEnv *env,
#if defined(WITH_JVMTI)
jvmtiEnv *jvmti, 
#endif
jobject allocated, jlong size, jint recId);

void initUnallocationInfoMap();

void transferAggregatedUnallocationToJava(JNIEnv *env, jobject receiver);

void doneUnallocation();

void freeUnallocationInfo(jlong info);

// from extern
void unallocate(jlong tag);

#endif
