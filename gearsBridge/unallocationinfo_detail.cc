#ifndef UNALLOCATION_INFO_H
#define UNALLOCATION_INFO_H

#include <string.h>
#include <jni.h>
#include "hashmap.h"

// helper functionality for native unallocation information
// shall be a class, implementation shall not be in header...

struct UnallocationInfo {
	long size;
	int count;
	int* recId;
};

typedef HashMap<int, long> AggregatedUnallocationMap;
typedef HashMap<long, UnallocationInfo*> UnallocationInfoMap;
static AggregatedUnallocationMap aggregatedUnallocationMap;
static UnallocationInfoMap unallocationInfoMap;
static jmethodID identityHashCodeMethod;
static jmethodID unallocatedMethod;

inline char* newString(size_t len, const char* data = 0) {
	char* result = new char[len + 1];
	result[len] = 0;
	if (NULL != data) {
		strncpy(result, data, len);
	}
	return result;
}

inline char* cloneString(char* src) {
	return newString(strlen(src), src);
}

inline void deleteString(char* s) {
	delete s;
}

char* newJString(JNIEnv *env, jstring s) {
	jsize strLen = env->GetStringUTFLength(s);
	const char* str = env->GetStringUTFChars(s, NULL);
	char* result = newString(strLen, str);
	env->ReleaseStringUTFChars(s, str);
	return result;
}

int systemHashCode(JNIEnv *env, jobject object) {
	if (0 == identityHashCodeMethod) {
		jclass sys_class = env->FindClass("java/lang/System");
		if (env->ExceptionOccurred()) {
			env->ExceptionDescribe();
		}
		identityHashCodeMethod = env->GetMethodID(sys_class, "identityHashCode", "(Ljava/lang/Object;)I");
		if (0 == identityHashCodeMethod) {
			printf("METHOD identityHashCode NOT FOUND!\n");
		}
	}
	if (0 != identityHashCodeMethod) {
		return env->CallIntMethod(NULL, identityHashCodeMethod, object);
	} else {
		return 0;
	}
}

UnallocationInfo * getUnallocationInfo(JNIEnv *env, 
#if defined(WITH_JVMTI)
jvmtiEnv *jvmti, 
#endif
jobject allocated) {
	UnallocationInfo* result = NULL;
#if defined(WITH_JVMTI)
    if (0 != jvmti) {
    	jlong tag_ptr;
    	jvmtiError error = jvmti->GetTag(allocated, &tag_ptr);
    	if (JVMTI_ERROR_NONE != error) {
			printf("ERROR: cannot get object tag (%d)\n", error);
		} else {
			result = (UnallocationInfo*) tag_ptr;
		}
    } else {
		printf("ERROR: No JVMTI environment present for setTag\n");
    }
#else
    UnallocationInfo** find = unallocationInfoMap.findp(systemHashCode(env, allocated));
	if (NULL != find) {
	    result = *find;
	}
#endif
	return result;
}

void storeUnallocationInfo(JNIEnv *env, 
#if defined(WITH_JVMTI)
jvmtiEnv *jvmti, 
#endif
jobject allocated, UnallocationInfo *info) {
#if defined(WITH_JVMTI)
    if (0 != jvmti) {
    	jvmtiError error = jvmti->SetTag(allocated, (jlong) info);
    	if (JVMTI_ERROR_NONE != error) {
			printf("ERROR: cannot tag object (%d)\n", error);
		}
    } else {
		printf("ERROR: No JVMTI environment present for setTag\n");
    }
#else
    unallocationInfoMap.insert(systemHashCode(env, allocated), info);
#endif
}

UnallocationInfo* createSimpleUnallocationInfo(JNIEnv *env,
	jlong size, jint recId) {
	UnallocationInfo* info = new UnallocationInfo;
	info->size = size;
	info->count = 0;
	info->recId = (int*) recId; // save memory!
	return info;
}

/*UnallocationInfo* createMultiUnallocationInfo(JNIEnv *env,
	jlong size, jint count, jobjectArray recId) {
	UnallocationInfo* info = new UnallocationInfo;
	info->count = count;
	info->size = size;
	info->recId = new char*[count];
	for (int i = 0; i < count; i++) {
		jstring id = (jstring) env->GetObjectArrayElement(recId, i);
		info->recId[i] = newJString(env, id);
	}
	return info;
}*/

void initUnallocationInfoMap() {
}

void deleteUnallocationInfo(UnallocationInfo* info) {
	if (info->count > 0) {
	    delete info->recId;
	}
	delete info;
}

void transferAggregatedUnallocationToJava(JNIEnv *env, jobject receiver) {
	initUnallocatedMethod(env);
	// clear data structure anyway
	for (AggregatedUnallocationMap::Iterator it = aggregatedUnallocationMap.first();
		it; it.next()) {
		int recId = it.key();
		long size = it.value();
		callUnallocated(env, receiver, recId, (jlong) size);
	}
	aggregatedUnallocationMap.clear();
}

void doneUnallocation() {
	for (UnallocationInfoMap::Iterator it = unallocationInfoMap.first();
		it; it.next()) {
		deleteUnallocationInfo(it.value());
	}
	unallocationInfoMap.done();
	aggregatedUnallocationMap.done();
	clearUnallocatedJni();
}

inline void transferToAggMap(int recId, long size) {
	long* recIdSize = aggregatedUnallocationMap.findp(recId);
	if (NULL == recIdSize) {
		aggregatedUnallocationMap.insert(recId, size);
	} else {
		*recIdSize += size;
	}
}

inline void freeUnallocationInfo(UnallocationInfo* info) {
	if (0 == info->count) {
		transferToAggMap((int)info->recId, info->size);
	} else {
		for (int i = 0; i < info->count; i++) {
			transferToAggMap(info->recId[i], info->size);
 		}
 	}
 	deleteUnallocationInfo(info);
}
