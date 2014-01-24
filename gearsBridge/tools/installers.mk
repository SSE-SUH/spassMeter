# define the general and individual installers targets
# changes are needed as most files are prepared and compiled in Gears
# not tested for targets $(SYMBIAN_INSTALLER_SISX), $(SYMBIAN_EMULATOR_INSTALLER)

INCLUDE_BUILD_INFO = false

# additional lists (copied from Gears)

COMMON_RESOURCES = \
	$(GEARS_ROOT)/ui/common/blank.gif \
	$(GEARS_ROOT)/ui/common/button_bg.gif \
	$(GEARS_ROOT)/ui/common/button_corner_black.gif \
	$(GEARS_ROOT)/ui/common/button_corner_blue.gif \
	$(GEARS_ROOT)/ui/common/button_corner_grey.gif \
	ui/common/icon_32x32.png \
	$(GEARS_ROOT)/ui/common/local_data.png \
	$(GEARS_ROOT)/ui/common/location_data.png \
	$(NULL)

FF36_RESOURCES = \
	$(FF36_OUTDIR)/genfiles/browser-overlay.js \
	$(FF36_OUTDIR)/genfiles/browser-overlay.xul \
	$(FF36_OUTDIR)/genfiles/permissions_dialog.html \
	$(FF36_OUTDIR)/genfiles/settings_dialog.html \
	$(FF36_OUTDIR)/genfiles/shortcuts_dialog.html \
	$(NULL)
# End: resource lists that MUST be kept in sync with "win32_msi.wxs.m4"

OPERA_RESOURCES = \
	$(OPERA_OUTDIR)/genfiles/permissions_dialog.html \
	$(OPERA_OUTDIR)/genfiles/settings_dialog.html \
	$(OPERA_OUTDIR)/genfiles/shortcuts_dialog.html \
	$(OPERA_OUTDIR)/genfiles/blank.gif \
	$(OPERA_OUTDIR)/genfiles/icon_32x32.png \
	$(OPERA_OUTDIR)/genfiles/local_data.png \
	$(OPERA_OUTDIR)/genfiles/location_data.png \
	$(NULL)

# INSTALLERS

ifeq ($(OS),linux)
installers:: $(FFMERGED_INSTALLER_XPI)
else
ifeq ($(OS),osx)
installers:: $(SF_INSTALLER_PKG) $(FFMERGED_INSTALLER_XPI)
else
ifeq ($(OS),win32)
installers:: $(FFMERGED_INSTALLER_XPI) $(WIN32_INSTALLER_MSI) $(NPAPI_INSTALLER_MSI)
else
ifeq ($(OS),wince)
installers:: $(IEMOBILE_WINCE_INSTALLER_CAB) $(OPERA_WINCE_INSTALLER_CAB)
else
ifeq ($(OS),symbian)
ifeq ($(ARCH),arm)
installers:: $(SYMBIAN_INSTALLER_SISX)
else
installers:: $(SYMBIAN_EMULATOR_INSTALLER)
endif
endif
endif
endif
endif
endif

ifeq ($(OS),android)
# Installer (build the zip)
installers:: $(ANDROID_INSTALLER_ZIP_PACKAGE)
# Rule to invoke the install.sh script with the detected environment
# variables and MODE set. This install Gears directly into a live
# emulator.
.PHONY:	adb-install
adb-install:
	ANDROID_BUILD_TOP=$(ANDROID_BUILD_TOP) \
		ANDROID_PRODUCT_OUT=$(ANDROID_PRODUCT_OUT) \
		ANDROID_TOOLCHAIN=$(ANDROID_TOOLCHAIN) \
		$(ADB_INSTALL) $(MODE)
endif # android

# We can't list the following as dependencies, because no BROWSER is defined
# for this target, therefore our $(BROWSER)_FOO variables and rules don't exist.
# For $(FFMERGED_INSTALLER_XPI):
#   $(FF2_MODULE_DLL) $(FF3_MODULE_DLL) $(FF31_MODULE_DLL) $(FF36_MODULE_DLL) $(FF36_MODULE_TYPELIB) $(FF36_RESOURCES) $(FF36_M4FILES_I18N) $(FF36_OUTDIR)/genfiles/chrome.manifest
# For $(SF_INSTALLER_PKG):
#   $(SF_PLUGIN_BUNDLE) $(SF_INPUTMANAGER_BUNDLE)
# In order to make sure the Installer is always up to date despite these missing
# dependencies, we list it as a phony target, so it's always rebuilt.
.PHONY: $(FFMERGED_INSTALLER_XPI) $(SF_INSTALLER_PKG)

ifeq ($(OS),osx)
ifeq ($(HAVE_ICEBERG),1)
# This rule generates a package installer for the Plugin and InputManager.
$(SF_INSTALLER_PKG):
	$(ICEBERG) -v $(SF_OUTDIR)/genfiles/installer.packproj
else
$(SF_INSTALLER_PKG):
	$(warning To create a Safari installer for Gears, you must install Iceberg \
  from http://s.sudre.free.fr/Software/Iceberg.html.  You can install the \
  Safari version manually by running tools/osx/install_gears.sh script)
endif
endif

ifeq ($(OS),android)
# Installer which packages up relevant Android Gears files into a .zip
$(ANDROID_INSTALLER_DLL): $(NPAPI_MODULE_DLL)
	@echo "Copy $<"
	@mkdir -p $(ANDROID_INSTALLER_OUTDIR)
	@cp $< $@
	@echo "Strip $<"
	@$(CROSS_PREFIX)strip $@

# TODO(jripley): The compressed html files are not working. Use the
# uncompressed ones for now.
#$(ANDROID_INSTALLER_OUTDIR)/%.html: $(NPAPI_OUTDIR)/genfiles/%.html.compress
$(ANDROID_INSTALLER_OUTDIR)/%.html: $(NPAPI_OUTDIR)/genfiles/%.html
	@echo "Copy `basename $@`"
	@mkdir -p $(ANDROID_INSTALLER_OUTDIR)
	@cp $< $@

$(ANDROID_INSTALLER_ZIP_PACKAGE): $(patsubst %.html,$(ANDROID_INSTALLER_OUTDIR)/%.html,$(NPAPI_HTML_COMPRESSED_FILES)) $(ANDROID_INSTALLER_DLL)
	@echo "Build Android package"
	@mkdir -p $(ANDROID_INSTALLER_OUTDIR)
	@-rm -f $(INSTALLERS_OUTDIR)/$(INSTALLER_BASE_NAME).zip
	@(cd $(INSTALLERS_OUTDIR) && zip -r $(INSTALLER_BASE_NAME).zip `basename $(ANDROID_INSTALLER_OUTDIR)`)
	@echo "Clean files"
	@rm -rf $(ANDROID_INSTALLER_OUTDIR)
	@ls -l $(INSTALLERS_OUTDIR)/$(INSTALLER_BASE_NAME).zip
endif # android

ifeq ($(OS),osx)
$(FFMERGED_INSTALLER_XPI): $(COMMON_RESOURCES) $(COMMON_M4FILES_I18N) $(OSX_LAUNCHURL_EXE)
else
$(FFMERGED_INSTALLER_XPI): $(COMMON_RESOURCES) 
endif
	rm -rf $(INSTALLERS_OUTDIR)/$(INSTALLER_BASE_NAME)
	"mkdir" -p $(INSTALLERS_OUTDIR)/$(INSTALLER_BASE_NAME)
	"mkdir" -p $(INSTALLERS_OUTDIR)/$(INSTALLER_BASE_NAME)/components
	"mkdir" -p $(INSTALLERS_OUTDIR)/$(INSTALLER_BASE_NAME)/resources
	"mkdir" -p $(INSTALLERS_OUTDIR)/$(INSTALLER_BASE_NAME)/lib
	"mkdir" -p $(INSTALLERS_OUTDIR)/$(INSTALLER_BASE_NAME)/lib/ff36
	"mkdir" -p $(INSTALLERS_OUTDIR)/$(INSTALLER_BASE_NAME)/lib/ff35
	"mkdir" -p $(INSTALLERS_OUTDIR)/$(INSTALLER_BASE_NAME)/lib/ff30
	"mkdir" -p $(INSTALLERS_OUTDIR)/$(INSTALLER_BASE_NAME)/lib/ff2
	cp base/firefox/static_files/components/stub.js $(INSTALLERS_OUTDIR)/$(INSTALLER_BASE_NAME)/components
	cp base/firefox/static_files/components/bootstrap.js $(INSTALLERS_OUTDIR)/$(INSTALLER_BASE_NAME)/components
	cp base/firefox/static_files/lib/updater.js $(INSTALLERS_OUTDIR)/$(INSTALLER_BASE_NAME)/lib
	cp $(FF36_OUTDIR)/genfiles/install.rdf $(INSTALLERS_OUTDIR)/$(INSTALLER_BASE_NAME)/install.rdf
	cp $(FF36_OUTDIR)/genfiles/chrome.manifest $(INSTALLERS_OUTDIR)/$(INSTALLER_BASE_NAME)/chrome.manifest
ifneq ($(OS),win32)
    # TODO(playmobil): Inspector should be located in extensions dir on win32.
	"mkdir" -p $(INSTALLERS_OUTDIR)/$(INSTALLER_BASE_NAME)/resources/inspector
	cp -R inspector/* $(INSTALLERS_OUTDIR)/$(INSTALLER_BASE_NAME)/resources/inspector
	cp sdk/gears_init.js $(INSTALLERS_OUTDIR)/$(INSTALLER_BASE_NAME)/resources/inspector/common
	cp sdk/samples/sample.js $(INSTALLERS_OUTDIR)/$(INSTALLER_BASE_NAME)/resources/inspector/common
endif
	"mkdir" -p $(INSTALLERS_OUTDIR)/$(INSTALLER_BASE_NAME)/chrome/chromeFiles/content
	"mkdir" -p $(INSTALLERS_OUTDIR)/$(INSTALLER_BASE_NAME)/chrome/chromeFiles/locale
	cp $(FF36_RESOURCES) $(INSTALLERS_OUTDIR)/$(INSTALLER_BASE_NAME)/chrome/chromeFiles/content
	cp $(COMMON_RESOURCES) $(INSTALLERS_OUTDIR)/$(INSTALLER_BASE_NAME)/chrome/chromeFiles/content
	cp -R $(GEARS_ROOT)/$(FF36_OUTDIR)/genfiles/i18n/* $(INSTALLERS_OUTDIR)/$(INSTALLER_BASE_NAME)/chrome/chromeFiles/locale
	cp -R $(GEARS_ROOT)/$(COMMON_OUTDIR)/genfiles/i18n/* $(INSTALLERS_OUTDIR)/$(INSTALLER_BASE_NAME)/chrome/chromeFiles/locale
	cp $(FF36_MODULE_TYPELIB) $(INSTALLERS_OUTDIR)/$(INSTALLER_BASE_NAME)/components
	cp $(FF36_MODULE_DLL) $(INSTALLERS_OUTDIR)/$(INSTALLER_BASE_NAME)/lib/ff36/$(DLL_PREFIX)$(MODULE)$(DLL_SUFFIX)
	cp $(FF31_MODULE_DLL) $(INSTALLERS_OUTDIR)/$(INSTALLER_BASE_NAME)/lib/ff35/$(DLL_PREFIX)$(MODULE)$(DLL_SUFFIX)
	cp $(FF3_MODULE_DLL) $(INSTALLERS_OUTDIR)/$(INSTALLER_BASE_NAME)/lib/ff30/$(DLL_PREFIX)$(MODULE)$(DLL_SUFFIX)
ifneq ($(ARCH),x86_64)
	cp $(FF2_MODULE_DLL) $(INSTALLERS_OUTDIR)/$(INSTALLER_BASE_NAME)/lib/ff2/$(DLL_PREFIX)$(MODULE)$(DLL_SUFFIX)
endif
ifeq ($(OS),osx)
	cp $(OSX_LAUNCHURL_EXE) $(INSTALLERS_OUTDIR)/$(INSTALLER_BASE_NAME)/resources/
else # not OSX
ifeq ($(OS),linux)
else # not LINUX (and not OSX)
ifeq ($(MODE),dbg)
ifdef IS_WIN32_OR_WINCE
ifeq ($(INCLUDE_BUILD_INFO),true)
	cp $(FF36_OUTDIR)/$(MODULE).pdb $(INSTALLERS_OUTDIR)/$(INSTALLER_BASE_NAME)/lib/ff36/$(MODULE).pdb
	cp $(FF31_OUTDIR)/$(MODULE).pdb $(INSTALLERS_OUTDIR)/$(INSTALLER_BASE_NAME)/lib/ff35/$(MODULE).pdb
	cp $(FF3_OUTDIR)/$(MODULE).pdb $(INSTALLERS_OUTDIR)/$(INSTALLER_BASE_NAME)/lib/ff30/$(MODULE).pdb
	cp $(FF2_OUTDIR)/$(MODULE).pdb $(INSTALLERS_OUTDIR)/$(INSTALLER_BASE_NAME)/lib/ff2/$(MODULE).pdb
endif
endif
endif
endif # not LINUX
endif # not OSX

    # Mark files writeable to allow .xpi rebuilds
	chmod -R 777 $(INSTALLERS_OUTDIR)/$(INSTALLER_BASE_NAME)/*
	(cd $(INSTALLERS_OUTDIR)/$(INSTALLER_BASE_NAME) && zip -r ../$(INSTALLER_BASE_NAME).xpi .)

$(SF_INSTALLER_PLUGIN_BUNDLE): $(SF_INSTALLER_PLUGIN_EXE)
	rm -rf "$@"
	mkdir -p "$@/Contents/Resources/"
	mkdir -p "$@/Contents/Resources/AdvancedStatsSheet.nib"
	mkdir -p "$@/Contents/MacOS"
# Copy Info.plist
	cp "base/safari/advanced_stats_sheet.plist" "$@/Contents/Info.plist"
# Copy binary
	cp "$(SF_INSTALLER_PLUGIN_EXE)" "$@/Contents/MacOS/InstallerPlugin"
# Copy nib file
	cp base/safari/advanced_stats_sheet.nib/* $@/Contents/Resources/AdvancedStatsSheet.nib/

$(SF_PLUGIN_PROXY_BUNDLE): $(SF_PLUGIN_BUNDLE) $(SF_PROXY_DLL)
# --- Gears.plugin ---
# Create fresh copies of the Gears.plugin directories.
	rm -rf $@
	mkdir -p $@/Contents/Resources/
	mkdir -p $@/Contents/MacOS
# Copy Info.plist
	cp $($(BROWSER)_OUTDIR)/genfiles/Info.plist $@/Contents/
# Copy proxy DLL
	cp $(SF_PROXY_DLL) $@/Contents/MacOS/libgears.dylib
# Copy Gears.bundle
	cp -R $(SF_PLUGIN_BUNDLE) $@/Contents/Resources/
# Copy uninstaller
	cp "tools/osx/uninstall.command" "$@/Contents/Resources/"
	/usr/bin/touch -c $@

$(SF_PLUGIN_BUNDLE): $(CRASH_SENDER_EXE) $(OSX_CRASH_INSPECTOR_EXE) $(OSX_LAUNCHURL_EXE) $(SF_MODULE_DLL) $(SF_M4FILES) $(SF_M4FILES_I18N)
# --- Gears.bundle ---
# Create fresh copies of the Gears.bundle directories.
	rm -rf $@
	mkdir -p $@/Contents/Resources/English.lproj
	mkdir -p $@/Contents/MacOS
# Add Info.plist file & localized strings.
	cp $($(BROWSER)_OUTDIR)/genfiles/Info.plist $@/Contents/
	cp tools/osx/English.lproj/InfoPlist.strings $@/Contents/Resources/English.lproj/InfoPlist.strings
# Copy Native dialog resources
	cp -R ui/safari/*.nib $@/Contents/Resources/
# Copy breakpad exes.
	cp -r $(CRASH_SENDER_EXE) $@/Contents/Resources/
	cp -r $(OSX_CRASH_INSPECTOR_EXE) $@/Contents/Resources/
# Copy the actual plugin.
	cp  "$(SF_MODULE_DLL)" "$@/Contents/MacOS/"
# Copy launch_url
	mkdir -p $@/Contents/Resources/
	cp "$(OSX_LAUNCHURL_EXE)" "$@/Contents/Resources/"
	/usr/bin/touch -c $@

$(SF_INPUTMANAGER_BUNDLE): $(SF_INPUTMANAGER_EXE)
# Create fresh copies of the GoogleGearsEnabler directories.
	rm -rf $@
	mkdir -p $@/GearsEnabler.bundle/Contents/Resources/English.lproj/
	mkdir -p $@/GearsEnabler.bundle/Contents/MacOS
# Add Info Info.plist file & localized strings.
	cat tools/osx/Enabler-Info.plist | sed 's/$${EXECUTABLE_NAME}/GearsEnabler/' | sed 's/$${PRODUCT_NAME}/GearsEnabler/' > $@/GearsEnabler.bundle/Contents/Info.plist
	cp tools/osx/Info $@/
	cp tools/osx/English.lproj/InfoPlist.strings $@/GearsEnabler.bundle/Contents/Resources/English.lproj/InfoPlist.strings
# Copy the InputManager.
	cp "$(SF_INPUTMANAGER_EXE)" "$@/GearsEnabler.bundle/Contents/MacOS/"
	/usr/bin/touch -c $@/GearsEnabler.bundle

WIN32_INSTALLER_WIXOBJ = $(OUTDIR_COMMON)/win32_msi.wxiobj
$(WIN32_INSTALLER_MSI): $(WIN32_INSTALLER_WIXOBJ) $(IE_MODULE_DLL) $(FFMERGED_INSTALLER_XPI)
	light.exe -out $@ $(WIN32_INSTALLER_WIXOBJ)

$(IEMOBILE_WINCE_INSTALLER_CAB): $(IEMOBILE_INFSRC) $(IEMOBILE_MODULE_DLL) $(IEMOBILE_WINCESETUP_DLL)
	cabwiz.exe $(IEMOBILE_INFSRC) /compress /err $(COMMON_OUTDIR)/genfiles/$(INFSRC_BASE_NAME).log
	mv -f $(COMMON_OUTDIR)/genfiles/$(INFSRC_BASE_NAME)_iemobile.cab $@

$(OPERA_WINCE_INSTALLER_CAB): $(OPERA_INFSRC) $(OPERA_MODULE_DLL) $(OPERA_WINCESETUP_DLL) $(OPERA_RESOURCES)
	cabwiz.exe $(OPERA_INFSRC) /compress /err $(COMMON_OUTDIR)/genfiles/$(INFSRC_BASE_NAME).log
	mv -f $(COMMON_OUTDIR)/genfiles/$(INFSRC_BASE_NAME)_op.cab $@

NPAPI_INSTALLER_WIXOBJ = $(OUTDIR_COMMON)/npapi_msi.wxiobj
# We must disable certain WiX integrity check errors ("ICE") to successfully
# create a per-user installer.
$(NPAPI_INSTALLER_MSI): $(NPAPI_INSTALLER_WIXOBJ) $(NPAPI_MODULE_DLL)
	light.exe -out $@ $(NPAPI_INSTALLER_WIXOBJ) -sice:ICE39 -sice:ICE64 -sice:ICE91

# INSTALLER-RELATED INTERMEDIATE TARGETS

ifeq ($(OS),win32)
NAMESPACE_GUID = 36F65206-5D4E-4752-9D52-27708E10DA79

# You can change the names of PRODUCT_ID vars, but NEVER change their values!
OUR_WIN32_PRODUCT_ID = \
  $(shell $(GGUIDGEN) $(NAMESPACE_GUID) OUR_PRODUCT_ID-$(VERSION))
OUR_COMPONENT_GUID_FF_COMPONENTS_DIR_FILES = \
  $(shell $(GGUIDGEN) $(NAMESPACE_GUID) OUR_COMPONENT_GUID_FF_COMPONENTS_DIR_FILES-$(VERSION))
OUR_COMPONENT_GUID_FF_CONTENT_DIR_FILES = \
  $(shell $(GGUIDGEN) $(NAMESPACE_GUID) OUR_COMPONENT_GUID_FF_CONTENT_DIR_FILES-$(VERSION))
OUR_COMPONENT_GUID_FF_DIR_FILES = \
  $(shell $(GGUIDGEN) $(NAMESPACE_GUID) OUR_COMPONENT_GUID_FF_DIR_FILES-$(VERSION))
OUR_COMPONENT_GUID_FF_LIB_DIR_FILES = \
  $(shell $(GGUIDGEN) $(NAMESPACE_GUID) OUR_COMPONENT_GUID_FF_LIB_DIR_FILES-$(VERSION))
OUR_COMPONENT_GUID_FF2_DIR_FILES = \
  $(shell $(GGUIDGEN) $(NAMESPACE_GUID) OUR_COMPONENT_GUID_FF2_DIR_FILES-$(VERSION))
OUR_COMPONENT_GUID_FF30_DIR_FILES = \
  $(shell $(GGUIDGEN) $(NAMESPACE_GUID) OUR_COMPONENT_GUID_FF30_DIR_FILES-$(VERSION))
OUR_COMPONENT_GUID_FF35_DIR_FILES = \
  $(shell $(GGUIDGEN) $(NAMESPACE_GUID) OUR_COMPONENT_GUID_FF35_DIR_FILES-$(VERSION))
OUR_COMPONENT_GUID_FF36_DIR_FILES = \
  $(shell $(GGUIDGEN) $(NAMESPACE_GUID) OUR_COMPONENT_GUID_FF36_DIR_FILES-$(VERSION))
OUR_COMPONENT_GUID_FF_REGISTRY = \
  $(shell $(GGUIDGEN) $(NAMESPACE_GUID) OUR_COMPONENT_GUID_FF_REGISTRY-$(VERSION))
OUR_COMPONENT_GUID_IE_FILES = \
  $(shell $(GGUIDGEN) $(NAMESPACE_GUID) OUR_COMPONENT_GUID_IE_FILES-$(VERSION))
OUR_COMPONENT_GUID_IE_REGISTRY = \
  $(shell $(GGUIDGEN) $(NAMESPACE_GUID) OUR_COMPONENT_GUID_IE_REGISTRY-$(VERSION))
OUR_COMPONENT_GUID_SHARED_FILES = \
  $(shell $(GGUIDGEN) $(NAMESPACE_GUID) OUR_COMPONENT_GUID_SHARED_FILES-$(VERSION))
OUR_COMPONENT_GUID_SHARED_VERSIONED_FILES = \
  $(shell $(GGUIDGEN) $(NAMESPACE_GUID) OUR_COMPONENT_GUID_SHARED_VERSIONED_FILES-$(VERSION))
OUR_COMPONENT_GUID_SHARED_REGISTRY = \
  $(shell $(GGUIDGEN) $(NAMESPACE_GUID) OUR_COMPONENT_GUID_SHARED_REGISTRY-$(VERSION))

OUR_NPAPI_PRODUCT_ID = \
  $(shell $(GGUIDGEN) $(NAMESPACE_GUID) OUR_2ND_PRODUCT_ID-$(VERSION))
OUR_COMPONENT_GUID_NPAPI_FILES = \
  $(shell $(GGUIDGEN) $(NAMESPACE_GUID) OUR_COMPONENT_GUID_NPAPI_FILES-$(VERSION))
OUR_COMPONENT_GUID_NPAPI_REGISTRY = \
  $(shell $(GGUIDGEN) $(NAMESPACE_GUID) OUR_COMPONENT_GUID_NPAPI_REGISTRY-$(VERSION))

WIX_DEFINES_I18N = $(foreach lang,$(subst -,_,$(I18N_LANGS)),-dOurComponentGUID_FFLang$(lang)DirFiles=$(shell $(GGUIDGEN) $(NAMESPACE_GUID) OUR_COMPONENT_GUID_FF_$(lang)_DIR_FILES-$(VERSION)))

# MSI version numbers must have the form <major>.<minor>.<build>. To meet this,
# we combine our build and patch version numbers like so:
# MSI_VERSION = <major>.<minor>.<BUILD * 100 + PATCH>.
# Note: This assumes that the BUILD and PATCH variables adhere to the range
# requirements in version.mk. See comments in version.mk for more details.
#MSI_BUILD = $(shell dc -e "$(BUILD) 100 * $(PATCH) + p")
MSI_BUILD=`expr $(BUILD) \* 100 \+ $(PATCH)`
MSI_VERSION = $(MAJOR).$(MINOR).$(MSI_BUILD)

$(OUTDIR_COMMON)/%.wxiobj: $(OUTDIR_COMMON_GENFILES)/%.wxs #VPATH OUTDIR_COMMON_GENFILES
	candle.exe -out $@ $< \
	  -dOurWin32ProductId=$(OUR_WIN32_PRODUCT_ID) \
	  -dOurCommonPath=$(OUTDIR)/$(OS)-$(ARCH)/common \
	  -dOurIEPath=$(OUTDIR)/$(OS)-$(ARCH)/ie \
	  -dOurFFPath=$(INSTALLERS_OUTDIR)/$(INSTALLER_BASE_NAME) \
	  -dOurComponentGUID_FFComponentsDirFiles=$(OUR_COMPONENT_GUID_FF_COMPONENTS_DIR_FILES) \
	  -dOurComponentGUID_FFContentDirFiles=$(OUR_COMPONENT_GUID_FF_CONTENT_DIR_FILES) \
	  -dOurComponentGUID_FFDirFiles=$(OUR_COMPONENT_GUID_FF_DIR_FILES) \
	  -dOurComponentGUID_FFLibDirFiles=$(OUR_COMPONENT_GUID_FF_LIB_DIR_FILES) \
	  -dOurComponentGUID_FF2DirFiles=$(OUR_COMPONENT_GUID_FF2_DIR_FILES) \
	  -dOurComponentGUID_FF30DirFiles=$(OUR_COMPONENT_GUID_FF30_DIR_FILES) \
	  -dOurComponentGUID_FF35DirFiles=$(OUR_COMPONENT_GUID_FF35_DIR_FILES) \
	  -dOurComponentGUID_FF36DirFiles=$(OUR_COMPONENT_GUID_FF36_DIR_FILES) \
	  -dOurComponentGUID_FFRegistry=$(OUR_COMPONENT_GUID_FF_REGISTRY) \
	  -dOurComponentGUID_IEFiles=$(OUR_COMPONENT_GUID_IE_FILES) \
	  -dOurComponentGUID_IERegistry=$(OUR_COMPONENT_GUID_IE_REGISTRY) \
	  -dOurComponentGUID_SharedFiles=$(OUR_COMPONENT_GUID_SHARED_FILES) \
	  -dOurComponentGUID_SharedVersionedFiles=$(OUR_COMPONENT_GUID_SHARED_VERSIONED_FILES) \
	  -dOurComponentGUID_SharedRegistry=$(OUR_COMPONENT_GUID_SHARED_REGISTRY) \
	  -dOurNpapiProductId=$(OUR_NPAPI_PRODUCT_ID) \
	  -dOurNpapiPath=$(OUTDIR)/$(OS)-$(ARCH)/npapi \
	  -dOurComponentGUID_NpapiFiles=$(OUR_COMPONENT_GUID_NPAPI_FILES) \
	  -dOurComponentGUID_NpapiRegistry=$(OUR_COMPONENT_GUID_NPAPI_REGISTRY) \
	  -dOurMsiVersion=$(MSI_VERSION) \
	  $(WIX_DEFINES_I18N)
endif
