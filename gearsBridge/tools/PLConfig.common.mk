ifeq ($(HAS_WIFI),1)
CPPFLAGS += -DVAR_WIFI_DATA=1
endif
ifeq ($(HAS_UI),1)
CPPFLAGS += -DVAR_HAS_UI=1
endif
