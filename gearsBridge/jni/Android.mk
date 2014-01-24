# Copyright (C) 2009 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

BROWSER             := AN
#include tools/PLConfig.mk
#AN_CPPFLAGS := $(subst "-DVAR_OBJECT_SIZE=1","", $(AN_CPPFLAGS))
#AN_CPPFLAGS := $(subst "-DVAR_OBJECT_SIZE=0","", $(AN_CPPFLAGS))
#AN_CPPFLAGS := $(subst "-DWITH_JVMTI=1","", $(AN_CPPFLAGS))
#AN_CPPFLAGS := $(subst "-DWITH_JVMTI=0","", $(AN_CPPFLAGS))
#AN_CPPFLAGS := $(subst "-DVAR_WIFI_DATA=1","", $(AN_CPPFLAGS))
#AN_CPPFLAGS := $(subst "-DVAR_WIFI_DATA=0","", $(AN_CPPFLAGS))

LOCAL_MODULE        := locutor
LOCAL_CPP_EXTENSION := .cpp
LOCAL_SRC_FILES     := de_uni_hildesheim_sse_system_DataGatherer.cpp data_gatherer_posix.cpp stopwatch.cpp stopwatch_posix.cpp data_gatherer_common.cpp mutex.cpp mutex_posix.cpp unallocationinfo_common.cpp unallocationinfo.cpp sysinfo.S
#clock_gettime.S

#LOCAL_CFLAGS        := -DBROWSER_FF36=1 -DJS_THREADSAFE -DBROWSER_FF31=1 -DBROWSER_FF3=1 -DBROWSER_FF=1 -DANDROID=1 -DNDK=1 $(AN_CPPFLAGS)
LOCAL_CFLAGS        := -DBROWSER_FF36=1 -DJS_THREADSAFE -DBROWSER_FF31=1 -DBROWSER_FF3=1 -DBROWSER_FF=1 -DANDROID=1 -DOS_ANDROID=1 -DNDK=1 -DVAR_SCREEN_DATA=1 -DVAR_MEMORY_DATA=1 -DVAR_TIME_DATA=1 -DVAR_LOAD_DATA=1 -DVAR_PROCESSOR_DATA=1 -DVAR_VOLUME_DATA=1 -DVAR_NETWORK_DATA=1 -DVAR_ENERGY_DATA=1 -DVAR_IO_DATA=1 -DVAR_ARBITRARY_PROCESS_DATA=1 -DVAR_CURRENT_PROCESS_DATA=1 -DVAR_ALL_PROCESSES_DATA=1 -DVAR_DEBUG=1
LOCAL_C_INCLUDES    := ../third_party/gecko_1.9.2 ../third_party/gecko_1.9.2/linux ../third_party/gecko_1.9.2/linux/gecko_sdk/include ..
LOCAL_LDLIBS        := -llog 
#-lGLESv2

include $(BUILD_SHARED_LIBRARY)
