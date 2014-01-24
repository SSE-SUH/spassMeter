export NDK=../android-ndk-r5b/android-ndk-r4
#export NDK=../android-ndk-r5b/android-ndk-r5b-linux
export ANDROID_NDK_ROOT=$NDK
export NDK_LOG=10
sed 's/Java_de_uni_1hildesheim_sse_system_deflt_/Java_de_uni_1hildesheim_sse_system_android_/g' de_uni_hildesheim_sse_system_DataGatherer.cc > jni/de_uni_hildesheim_sse_system_DataGatherer.cpp
sed 's/Java_de_uni_1hildesheim_sse_system_deflt_/Java_de_uni_1hildesheim_sse_system_android_/g' de_uni_hildesheim_sse_system_DataGatherer.h > jni/de_uni_hildesheim_sse_system_DataGatherer.h 
#cp -u de_uni_hildesheim_sse_system_DataGatherer.cc jni/de_uni_hildesheim_sse_system_DataGatherer.cpp
cp -u data_gatherer_posix.cc jni/data_gatherer_posix.cpp
cp -u data_gatherer_common.cc jni/data_gatherer_common.cpp
cp -u unallocationInfo_common.cc jni/unallocationinfo_common.cpp
cp -u unallocationInfo.cc jni/unallocationinfo.cpp
cp -u data_gatherer.h jni/data_gatherer.h
cp -u unallocationinfo_common.h jni
cp -u defs.h jni
cp -u unallocationinfo.h jni
cp -u android_defs.h jni
cp -u hashmap.h jni
cp -u Android.mk jni
cp -u Application.mk jni
mkdir -p jni/gears
cp -u gears/device_data_provider.h jni/gears
cp -u gears/basictypes.h jni/gears/
cp -u gears/atomic_ops.h jni/gears/
cp -u gears/common.h jni/gears/
cp -u gears/mutex.h jni/gears/
cp -u gears/stopwatch.h jni/gears/
cp -u gears/mutex.cc jni/mutex.cpp
cp -u gears/mutex_posix.cc jni/mutex_posix.cpp
cp -u gears/stopwatch.cc jni/stopwatch.cpp
cp -u gears/stopwatch_posix.cc jni/stopwatch_posix.cpp
cp -u gears/scoped_refptr.h jni/gears/
cp -u gears/string16.h jni/gears/

$NDK/ndk-build $*