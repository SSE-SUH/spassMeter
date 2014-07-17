
#ifndef DEFS_H_
#define DEFS_H_

#if !defined(WINVER) || !defined(ANDROID)
#define INCLUDE
#else
#define INCLUDE include
#endif

// Check windows
#if _WIN32 || _WIN64
#if _WIN64
#define ENVIRONMENT64
#else
#define ENVIRONMENT32
#endif
#endif

// Check GCC
#if __GNUC__
#if __x86_64__ || __ppc64__
#define ENVIRONMENT64
#else
#define ENVIRONMENT32
#endif
#endif

#endif
