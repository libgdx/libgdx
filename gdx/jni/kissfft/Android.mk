LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := kissfft
LOCAL_SRC_FILES := kiss_fft.c \
kiss_fftr.c \
KissFFT.cpp \

LOCAL_ARM_MODE := arm
LOCAL_CFLAGS := -DFIXED_POINT -O2 -Wall

include $(BUILD_STATIC_LIBRARY)