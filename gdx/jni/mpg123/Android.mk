LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := mpg123
LOCAL_SRC_FILES := equalizer.c \
index.c \
layer2.c \
synth.c \
dct64.c \
format.c \
layer3.c \
ntom.c \
parse.c \
readers.c \
frame.c \
layer1.c \
libmpg123.c \
optimize.c \
synth_arm.S \
tabinit.c \
id3.c

LOCAL_ARM_MODE := arm
LOCAL_CFLAGS := -ffast-math -O2 -Wall

include $(BUILD_STATIC_LIBRARY)