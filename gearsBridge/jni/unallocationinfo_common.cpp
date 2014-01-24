#include "unallocationinfo_common.h"

#include <stdio.h>
#include "defs.h"

static jmethodID identityHashCodeMethod;
static jmethodID unallocatedMethod;

jint systemHashCode(JNIEnv *env, jobject object) {
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

void initUnallocatedMethod(JNIEnv *env) {
	if (0 == unallocatedMethod) {
		jclass cls = env->FindClass("de/uni_hildesheim/sse/system/IMemoryUnallocationReceiver");
		if (env->ExceptionOccurred()) {
			env->ExceptionDescribe();
		}
		unallocatedMethod = env->GetMethodID(cls, "unallocated", "(IJ)V");
		if (0 == unallocatedMethod) {
			printf("METHOD unallocated NOT FOUND\n");
		}
	}
}

void clearUnallocatedJni() {
	unallocatedMethod = 0;
	identityHashCodeMethod = 0;
}

void callUnallocated(JNIEnv *env, jobject receiver, jint recId, jlong size) {
	if (0 != unallocatedMethod) {
		env->CallVoidMethod(receiver, unallocatedMethod, recId, size);
	}
}
