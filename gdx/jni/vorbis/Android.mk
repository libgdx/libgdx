LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := vorbis
LOCAL_SRC_FILES := bitwise.c \
block.c \
codebook.c \
floor0.c \
floor1.c \
framing.c \
info.c \
mapping0.c \
mdct.c \
registry.c \
res012.c \
sharedbook.c \
synthesis.c \
vorbisfile.c \
window.c \
VorbisDecoder.cpp

LOCAL_ARM_MODE := arm
LOCAL_CFLAGS := -D_ARM_ASSEM_ -ffast-math -O2 -Wall 

include $(BUILD_STATIC_LIBRARY)