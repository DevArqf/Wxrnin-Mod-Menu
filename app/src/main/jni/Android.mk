LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE := libdobby
LOCAL_SRC_FILES := dobby/libraries/$(TARGET_ARCH_ABI)/libdobby.a
LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)/dobby/
include $(PREBUILT_STATIC_LIBRARY)


include $(CLEAR_VARS)

# Here is the name of your lib.
# When you change the lib name, change also on System.loadLibrary("") under OnCreate method on StaticActivity.java
# Both must have same name
LOCAL_MODULE    := GameHelper

# -std=c++17 is required to support AIDE app with NDK
LOCAL_CFLAGS := -w -s -Wno-error=format-security -fvisibility=hidden -fpermissive -fexceptions
LOCAL_CPPFLAGS := -w -s -Wno-error=format-security -fvisibility=hidden -Werror -std=c++20
LOCAL_CPPFLAGS += -Wno-error=c++11-narrowing -fpermissive -Wall -fexceptions
LOCAL_LDFLAGS += -Wl,--gc-sections,--strip-all,-llog
LOCAL_LDLIBS := -llog -landroid -lEGL -lGLESv2
LOCAL_ARM_MODE := arm

LOCAL_C_INCLUDES += $(LOCAL_PATH)
LOCAL_STATIC_LIBRARIES := libdobby
# Here you add the cpp file to compile
LOCAL_SRC_FILES := Main.cpp \
	Includes/Utils.cpp \
	Includes/Draw.cpp \
	Includes/ObscuredTypes.cpp \
	Menu/JNILoader.cpp \
	Menu/FeatureModule.cpp \
	Menu/Menu.cpp \
	Menu/Setup.cpp \
	KittyMemory/KittyMemory.cpp \
	KittyMemory/MemoryPatch.cpp \
    KittyMemory/MemoryBackup.cpp \
    KittyMemory/KittyUtils.cpp \
    Hacks/Hooks.cpp \
    Hacks/Visuals.cpp \

include $(BUILD_SHARED_LIBRARY)


# Loader
include $(CLEAR_VARS)

LOCAL_MODULE    := Loader
LOCAL_CFLAGS := -w -s -Wno-error=format-security -fvisibility=hidden -fpermissive -fexceptions
LOCAL_LDLIBS := -llog -landroid

LOCAL_SRC_FILES := Loader/Loader.cpp

include $(BUILD_SHARED_LIBRARY)
