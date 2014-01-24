#ifndef UNALLOCATION_INFO_COMMON_H
#define UNALLOCATION_INFO_COMMON_H

#include <jni.h>
#include "defs.h"

jint systemHashCode(JNIEnv *env, jobject object);

void initUnallocatedMethod(JNIEnv *env);

void callUnallocated(JNIEnv *env, jobject receiver, jint recId, jlong size);

void clearUnallocatedJni();

#endif
