LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)
 
LOCAL_MODULE    := gdxaudio
LOCAL_C_INCLUDES := kissfft vorbis 
 
LOCAL_CFLAGS := $(LOCAL_C_INCLUDES:%=-I%) -O2 -Wall -D__ANDROID__-DFIXED_POINT -D_ARM_ASSEM_ -D__ANDROID__
LOCAL_CPPFLAGS := $(LOCAL_C_INCLUDES:%=-I%) -O2 -Wall -D__ANDROID__-DFIXED_POINT -D_ARM_ASSEM_ -D__ANDROID__
LOCAL_LDLIBS := -lm
LOCAL_ARM_MODE  := arm
 
LOCAL_SRC_FILES := com.badlogic.gdx.audio.analysis.AudioTools.cpp\
	com.badlogic.gdx.audio.analysis.KissFFT.cpp\
	com.badlogic.gdx.audio.io.VorbisDecoder.cpp
 
include $(BUILD_SHARED_LIBRARY)
