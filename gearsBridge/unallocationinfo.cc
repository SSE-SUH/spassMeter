#include "unallocationinfo.h"
#include "unallocationinfo_common.h"
#include "defs.h"

jlong createTagInfo(jlong size, jint recId) {
	jlong lTmp = (jint) size;
	int shift = (sizeof(jlong) / 2) * 8;
	return lTmp << shift | recId;
}

jlong getSize(jlong info) {
	int shift = (sizeof(jlong) / 2) * 8;
	return info >> shift;
}

jint getRecId(jlong info) {
	return (jint) info;
}

void storeUnallocationInfoTag(jlong tag, jlong size, jint recId) {
    unallocationLock.Lock();
    jlong* infop = unallocationInfoMap.findp(tag);
	if (NULL != infop) {
		jlong info = *infop;
		jlong iSize = getSize(info);
		*infop = createTagInfo((jlong) (iSize + size), recId);
	} else {
		jlong info = createTagInfo(size, recId);
	    unallocationInfoMap.insert(tag, info);
	}
    unallocationLock.Unlock();
}

void storeUnallocationInfo(JNIEnv *env,
#if defined(WITH_JVMTI)
jvmtiEnv *jvmti,
#endif
jobject allocated, jlong size, jint recId) {
#if defined(WITH_JVMTI)
    if (0 != jvmti) {
    	jlong info = createTagInfo(size, recId);
    	jvmtiError error = jvmti->SetTag(allocated, info);
    	if (JVMTI_ERROR_NONE != error) {
			printf("ERROR: cannot tag object (%d)\n", error);
		}
    } else {
		printf("ERROR: No JVMTI environment present for setTag\n");
    }
#else
    storeUnallocationInfoTag(systemHashCode(env, allocated), size, recId);
#endif
}

void initUnallocationInfoMap() {
}

void transferAggregatedUnallocationToJava(JNIEnv *env, jobject receiver) {
	initUnallocatedMethod(env);
	// clear data structure anyway
    freeLock.Lock();
	AggregatedUnallocationMap* tmp = new AggregatedUnallocationMap(
		aggregatedUnallocationMap);
	aggregatedUnallocationMap.clear();
	freeLock.Unlock();
	for (AggregatedUnallocationMap::Iterator it = tmp->first();
		it; it.next()) {
		jint recId = it.key();
		jlong size = it.value();
		callUnallocated(env, receiver, recId, size);
	}
	delete tmp;
}

void doneUnallocation() {
	unallocationInfoMap.done();
	aggregatedUnallocationMap.done();
	clearUnallocatedJni();
}

void transferToAggMap(jint recId, jlong size) {
	jlong* recIdSize = aggregatedUnallocationMap.findp(recId);
	if (NULL == recIdSize) {
		aggregatedUnallocationMap.insert(recId, size);
	} else {
		*recIdSize += size;
	}
}

void freeUnallocationInfo(jlong info) {
	jlong size = getSize(info);
	jint recId = getRecId(info);
    freeLock.Lock();
    transferToAggMap(recId, size);
    freeLock.Unlock();
}

// from extern
void unallocate(jlong tag) {
	jlong local = 0;
    jlong* infop = NULL;
    unallocationLock.Lock();
    infop = unallocationInfoMap.findp(tag);
    if (NULL != infop) {
    	local = *infop;
		unallocationInfoMap.remove(tag);
	}
    unallocationLock.Unlock();
    if (local != 0) { // would mean id = 0, size = 0
		freeUnallocationInfo(local);
    }
}
