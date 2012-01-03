LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)
 
LOCAL_MODULE    := gdx-stb-truetype
LOCAL_C_INCLUDES := 
 
LOCAL_CFLAGS := $(LOCAL_C_INCLUDES:%=-I%) -O2 -Wall -D__ANDROID__
LOCAL_CPPFLAGS := $(LOCAL_C_INCLUDES:%=-I%) -O2 -Wall -D__ANDROID__
LOCAL_LDLIBS := -lm
LOCAL_ARM_MODE  := arm
 
LOCAL_SRC_FILES := com.badlogic.gdx.graphics.g2d.stbtt.StbTrueType.cpp
 
include $(BUILD_SHARED_LIBRARY)
