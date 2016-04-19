# environment variable X64 shall container under windows 64 for 64 bit build "vcvarsall.bat amd64" 
# (or the respective architecture) and path shall contain folder of vcvarsall %VC_HOME$\VC

######################################################################
# OS
######################################################################
# Discover the OS
ifeq ($(shell uname),Linux)
  OS = linux
  POSIX = 1
else 
ifeq ($(shell uname),Darwin)
  OS = osx
  POSIX = 1
else 
ifneq ($(OS),symbian)
  IS_WIN32_OR_WINCE = 1
  ifneq ($(OS),wince)
    ifdef X64
	  OS = win64
	else
      OS = win32
	endif
  endif
  WIN = 1
endif
endif
endif

######################################################################
# MODE
######################################################################
# Set default build mode
#   dbg = debug build
#   opt = release build
MODE = dbg

######################################################################
# ARCH
######################################################################
# Set default OS architecture
#   OSX and Symbian builds will override this value (see rules.mk).
#   For other platforms we set just one value.
ifeq ($(OS),wince)
  ARCH = arm
else
ifeq ($(OS),android)
  # default platform for android
  ARCH = arm
else
ifeq ($(OS),osx)
  # On OSX we build a fat binary.
  ARCH = i386+ppc
else
ifeq ($(OS),symbian)
  # Don't set a value here; Symbian builds for two ARCHs by default.
else
ifeq ($(OS),linux)
  ARCH = $(shell gcc -dumpmachine | grep --only-matching x86_64)
ifeq ($(ARCH),)
  # Linux-only -- default to i386 if we're not explicitly x86_64.
  ARCH = i386
endif
else
  # On Win32, we build i386.
  ARCH = i386
endif
endif
endif
endif
endif

#set os specific build variables

######################################################################
# OS == linux
######################################################################
ifeq ($(OS),linux)
CC = gcc
CXX = g++
ECHO=@echo

UBUNTU_INCLUDES = -I /usr/lib/jvm/java-6-openjdk/include/ -I javavm -I javavm/solaris

CPPFLAGS = -DLINUX $(UBUNTU_INCLUDES)
CPPFLAGS += -I. -DNO_GZIP -DNO_GZCOMPRESS 
CPPFLAGS += -DUSING_CCTESTS=1

LBITS := $(shell getconf LONG_BIT)
ifeq ($(LBITS),64)
   #OS_SUFFIX = -64
   #CPPFLAGS += -m64
   CPPFLAGS += -DENVIRONMENT64
else
   CPPFLAGS += -Ze
   # do 32 bit stuff here
endif

COMPILE_FLAGS_dbg = -g -O0
COMPILE_FLAGS_opt = -O2
CPPFLAGS += -c -o $@ -fshort-wchar -funsigned-char -fPIC -fmessage-length=0 -Wall $(COMPILE_FLAGS_$(MODE))
CXXFLAGS += -fno-rtti -Wno-non-virtual-dtor -Wno-ctor-dtor-privacy -Wno-char-subscripts
OBJ_SUFFIX = .o
MKDEP = gcc -M -MF $(@D)/$(*F).pp -MT $@ $(CPPFLAGS) $<

MKDLL = g++
DLL_PREFIX = lib
DLL_SUFFIX = .so
# -Bsymbolic not on 64bit
DLLFLAGS = -o $@ -fPIC -Bsymbolic -pthread -shared 
LINK_EXTRAS = 
endif

######################################################################
# OS == win32 OR wince
######################################################################
ifdef IS_WIN32_OR_WINCE
CC = cl
CXX = cl
# echo.exe outputs "Echo on." (or "Echo off.") if it is called with no arguments
ECHO=@echo

CPPFLAGS_dbg = -D_DEBUG=1 
CPPFLAGS_opt =
CPPFLAGS += -I. /nologo -DSTRICT -D_UNICODE -DUNICODE -D_USRDLL -DWIN32 -D_WINDLL \
            -D_CRT_SECURE_NO_DEPRECATE -DNOMINMAX  -I javavm -I javavm/win32

ifeq ($(OS),win32)
WINVER=0x0500
#WINVER=0x0601
CPPFLAGS += -D_WINDOWS \
            -DWINVER=$(WINVER) \
            -D_WIN32_WINNT=$(WINVER) \
            -D_WIN32_IE=$(WINVER) \
            -D_RICHEDIT_VER=0x0010 \
            -D_MERGE_PROXYSTUB \
            -DBREAKPAD_AVOID_STREAMS \
            -DXP_WIN \
            $(CPPFLAGS_$(MODE))
else ifeq ($(OS),win64)
WINVER=0x0600
CPPFLAGS += -D_WINDOWS \
            -DWINVER=$(WINVER) \
            -D_WIN32_WINNT=$(WINVER) \
            -D_WIN32_IE=$(WINVER) \
            -D_RICHEDIT_VER=0x0010 \
            -D_MERGE_PROXYSTUB \
            -DBREAKPAD_AVOID_STREAMS \
            -DXP_WIN \
            $(CPPFLAGS_$(MODE))
#inofficial flag to remove ASM parts
CPPFLAGS += -DFORX64 \
            $(CPPFLAGS_$(MODE))
else
# For Windows Mobile we need:
#   C defines:  _WIN32_WCE=0x0501
#               _UNDER_CE=0x0501
CPPFLAGS += -D_WIN32_WCE=0x501 \
            -DWINVER=_WIN32_WCE \
            -DUNDER_CE=0x501 \
            -DOS_WINCE \
            -DWIN32_PLATFORM_PSPC \
            -DARM \
            -D_ARM_ \
            -DPOCKETPC2003_UI_MODEL \
            -D_CE_ALLOW_SINGLE_THREADED_OBJECTS_IN_MTA \
            -D_CE_CRT_ALLOW_WIN_MINMAX \
            $(CPPFLAGS_$(MODE))
endif

COMPILE_FLAGS_dbg = /MTd /Zi /Zc:wchar_t-
COMPILE_FLAGS_opt = /MT  /Zi /Zc:wchar_t- /O2
COMPILE_FLAGS = /c /Fo"$@" /Fd"$(@D)/$(*F).pdb" /W3 /GR- $(COMPILE_FLAGS_$(MODE))
# In VC8, the way to disable exceptions is to remove all /EH* flags, and to
# define _HAS_EXCEPTIONS=0 (for C++ headers) and _ATL_NO_EXCEPTIONS (for ATL).
COMPILE_FLAGS += -D_HAS_EXCEPTIONS=0 -D_ATL_NO_EXCEPTIONS
# Do not export UTF functions.
COMPILE_FLAGS += -DU_STATIC_IMPLEMENTATION /wd4244 /wd4800
CXXFLAGS = $(COMPILE_FLAGS) /TP /J
CPPFLAGS += $(COMPILE_FLAGS)

# /RELEASE adds a checksum to the PE header to aid symbol loading.
# /DEBUG causes PDB files to be produced.
# We want both these flags in all build modes, despite their names.
SHARED_LINKFLAGS_dbg = 
SHARED_LINKFLAGS_opt = /INCREMENTAL:NO /OPT:REF /OPT:ICF
SHARED_LINKFLAGS = /NOLOGO /OUT:$@ /RELEASE
ifeq ($(OS),win32)
SHARED_LINKFLAGS += \
	/MACHINE:X86 \
	$(SHARED_LINKFLAGS_$(MODE))
else
ifeq ($(OS),win64)
SHARED_LINKFLAGS += \
	/MACHINE:X64 \
	$(SHARED_LINKFLAGS_$(MODE))
else
SHARED_LINKFLAGS += \
	/MACHINE:THUMB \
	/NODEFAULTLIB:secchk.lib \
	/NODEFAULTLIB:oldnames.lib \
	$(SHARED_LINKFLAGS_$(MODE))
endif
endif

#adjust if needed (link.ms is valid for my machine)
MKDLL = link.ms
DLL_PREFIX =
DLL_SUFFIX = .dll
# We need DLLFLAGS_NOPDB for generating other targets than gears.dll
# (e.g. setup.dll for Windows Mobile).
DLLFLAGS_NOPDB = $(SHARED_LINKFLAGS) /DLL
# Wo only use /SUBSYSTEM on DLLs. For EXEs we omit the flag, and
# the presence of main() or WinMain() determines the subsystem.
ifeq ($(OS),win32)
#DLLFLAGS_NOPDB += /SUBSYSTEM:WINDOWS
else
ifeq ($(OS),win64)
#DLLFLAGS_NOPDB += /SUBSYSTEM:WINDOWS
else
DLLFLAGS_NOPDB += /SUBSYSTEM:WINDOWSCE,5.01
endif
endif
DLLFLAGS = $(DLLFLAGS_NOPDB) /PDB:"$(@D)/$(MODULE).pdb" /INCREMENTAL:NO /OPT:REF /OPT:ICF comsupp.lib Wbemuuid.lib Iphlpapi.lib psapi.lib pdh.lib  kernel32.lib user32.lib gdi32.lib gdiplus.lib uuid.lib sensapi.lib shlwapi.lib shell32.lib advapi32.lib wininet.lib comdlg32.lib user32.lib jvm.lib
EXT_LINKER_CMD_FLAG = @
OBJ_SUFFIX = .obj
MKDEP = python tools/mkdepend.py $< $@ > $(@D)/$(*F).pp
endif

#further OSconfigs

#################################### files #########################################

CPPFLAGS += -DDEBUG=1
#PL configuration

include tools/PLConfig.mk

ENVCMD = $(ECHO) "Default build environment"
ifdef IS_WIN32_OR_WINCE
    ifdef X64
      ENVCMD = $(X64)
	  # currently required as type definitions clash :(
	  HAS_WIFI = 0
	endif
endif

include tools/PLConfig.common.mk

PRODUCT_DIRNAME=jni
MODULE=locutor
OUTDIR = bin-$(MODE)/$(OS)-$(ARCH)$(OS_SUFFIX)
TARGET = $(MODULE)$(DLL_SUFFIX)
PRODUCT_OUTDIR = $(OUTDIR)/$(PRODUCT_DIRNAME)
MAKE_TARGET = $(OUTDIR)/$(PRODUCT_DIRNAME)/$(TARGET)
OBJ_LIST = $(OUTDIR)/obj_list.temp
TOP_DEP = $(PRODUCT_OUTDIR)/de_uni_hildesheim_sse_system_DataGatherer$(OBJ_SUFFIX) 

DIRS += \
		$(PRODUCT_OUTDIR) \
		$(NULL)
		
NAMES= data_gatherer_common \
		de_uni_hildesheim_sse_system_DataGatherer \
		stopwatch \
		mutex \
		unallocationinfo_common \
		unallocationinfo \
		$(NULL)

ifeq ($(WIN),1)
NAMES+= mutex_win32 \
		data_gatherer_win32 \
		time_utils_win32 \
		stopwatch_win32 \
		$(NULL)
endif		
ifeq ($(HAS_WIFI),1)
ifeq ($(WIN),1)
NAMES+= common_win32 \
		wifi_data_provider_common \
		wifi_data_provider_windows_common \
		wifi_data_provider_win32 \
		thread_win32 \
		message_queue \
		message_queue_win32 \
		vista_utils \
		string_utils \
		ConvertUTF \
		thread \
		thread_locals \
		event \
		$(NULL)
endif
ifeq ($(POSIX),1)
NAMES+= stopwatch_posix \
		mutex_posix \
		data_gatherer_posix \
		wifi_data_provider_linux \
		$(NULL)
ifeq ($(HAS_UI),1)
LINK_EXTRAS += /usr/lib/libX11.so \
		$(NULL) 
endif		
endif
endif
		
ifeq ($(POSIX),1)
NAMES += \
		hw \
		osutils \
		version \
		options \
		heuristics \
		cpuinfo \
		sysfs \
		jedec \
		print \
		cpuid \
		cpufreq \
		$(NULL)
NAMES += \
		pwcache \
	    readproc \
		alloc \
		$(NULL)
endif

NAMES_OBJ = $(addsuffix $(OBJ_SUFFIX), $(addprefix $(PRODUCT_OUTDIR)/, $(NAMES)))

################################### RULES ########################################

all: setenv $(DIRS) $(NAMES_OBJ) $(MAKE_TARGET) 

.PHONY : setenv all clean help

setenv:
	$(ENVCMD)

# outdir
clean:
	rm -rf $(OUTDIR)/*

help:
	$(ECHO) "Usage: make [MODE=dbg|opt] [PRODUCT=TEST|DEMO|JNI] [OS=wince]"
	$(ECHO)
	$(ECHO) "  If you omit MODE, the default is dbg."

$(PRODUCT_OUTDIR):
	"mkdir" -p $@

$(PRODUCT_OUTDIR)/%$(OBJ_SUFFIX): %.cc
	@$(MKDEP)
	$(CXX) $(CPPFLAGS) $(CXXFLAGS) $<

$(PRODUCT_OUTDIR)/%$(OBJ_SUFFIX): gears/%.cc
	@$(MKDEP)
	$(CXX) $(CPPFLAGS) $(CXXFLAGS) $<

$(PRODUCT_OUTDIR)/%$(OBJ_SUFFIX): lshw/src/core/%.cc
	@$(MKDEP)
	$(CXX) $(CPPFLAGS) $(CXXFLAGS) $<

$(PRODUCT_OUTDIR)/%$(OBJ_SUFFIX): procps-3.2.8/proc/%.c
	@$(MKDEP)
	$(CC) $(CPPFLAGS) $<	

$(PRODUCT_OUTDIR)/%$(OBJ_SUFFIX): convert_utf/%.c
	@$(MKDEP)
	$(CC) $(CPPFLAGS) $<	
	
$(PRODUCT_OUTDIR)/$(TARGET): $(TOP_DEP)
  ifeq ($(OS),linux)
	$(MKDLL) $(DLLFLAGS) $(NAMES_OBJ) $(LINK_EXTRAS)
  else
	ls -1 $(PRODUCT_OUTDIR)/*.obj > $(OBJ_LIST)
	@echo $(LOCUTOR_OBJ) >> $(OBJ_LIST)
	$(MKDLL) $(DLLFLAGS) $(LINK_EXTRAS) $(EXT_LINKER_CMD_FLAG)$(OBJ_LIST)
  endif	
	

	