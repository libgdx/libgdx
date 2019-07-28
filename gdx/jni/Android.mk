LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)
 
LOCAL_MODULE    := gdx
LOCAL_C_INCLUDES := 
 
LOCAL_CFLAGS := $(LOCAL_C_INCLUDES:%=-I%) -O2 -Wall -D__ANDROID__
LOCAL_CPPFLAGS := $(LOCAL_C_INCLUDES:%=-I%) -O2 -Wall -D__ANDROID__
LOCAL_LDLIBS := -lm -llog
LOCAL_ARM_MODE  := arm
 
LOCAL_SRC_FILES := com.badlogic.gdx.graphics.g2d.Gdx2DPixmap.cpp\
	com.badlogic.gdx.utils.BufferUtils.cpp\
	com.badlogic.gdx.math.Matrix4.cpp\
	memcpy_wrap.c\
	com.badlogic.gdx.graphics.glutils.ETC1.cpp\
	etc1/etc1_utils.cpp\
	gdx2d/gdx2d.c\
	gdx2d/jpgd_c.cpp\
	gdx2d/jpgd.cpp
 
include $(BUILD_SHARED_LIBRARY)
