LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_LDLIBS := -llog -ldl

LOCAL_MODULE:= parabola
LOCAL_SRC_FILES := $(LOCAL_PATH)/parabola/parabola.c $(LOCAL_PATH)/parabola/Jni_call.c

include $(BUILD_SHARED_LIBRARY)
