JNIPATH := $(call my-dir)
LOCAL_PATH := $(JNIPATH)

include $(call all-subdir-makefiles)

LOCAL_PATH := $(JNIPATH)
include $(CLEAR_VARS)

LOCAL_MODULE    := gdx
LOCAL_SRC_FILES := AudioTools.cpp \
				   BufferUtils.cpp \
				   MD5Jni.cpp \
				   Mpg123Decoder.cpp

LOCAL_ARM_MODE := arm

LOCAL_STATIC_LIBRARIES := kissfft mpg123 vorbis box2d

include $(BUILD_SHARED_LIBRARY)

