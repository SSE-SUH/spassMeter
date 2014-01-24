
#ifndef ANDROID_DEFS_H_
#define ANDROID_DEFS_H_

#include "android/log.h"

typedef unsigned long long uint64;

#define LOG(t) __android_log_write(4, "Locutor", t);

#endif /* ANDROID_DEFS_H_ */
