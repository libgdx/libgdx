LOCAL_PATH := $(call my-dir)
#include $(CLEAR_VARS)
#include $(LOCAL_PATH)/mpg123/Android.mk

include $(CLEAR_VARS)

GDX_CFLAGS 	  := -O2 -Wall -D__ANDROID__
GDX_SRC_FILES := AudioTools.cpp \
				 BufferUtils.cpp \
				 MD5Jni.cpp \
				 Matrix4.cpp
#				 Mpg123Decoder.cpp

include $(LOCAL_PATH)/Box2D/Android.mk
include $(LOCAL_PATH)/kissfft/Android.mk
include $(LOCAL_PATH)/vorbis/Android.mk	
include $(LOCAL_PATH)/gdx2d/Android.mk				 
include $(LOCAL_PATH)/etc1/Android.mk	

LOCAL_MODULE    := gdx
LOCAL_ARM_MODE  := arm
LOCAL_SRC_FILES := $(GDX_SRC_FILES)
LOCAL_CFLAGS    := $(GDX_CFLAGS)
#LOCAL_SHARED_LIBRARIES := libmpg123

include $(BUILD_SHARED_LIBRARY)

