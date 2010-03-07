LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := gdx
LOCAL_ARM_MODE := arm
LOCAL_SRC_FILES := NativeMP3Decoder.cpp kissfft/kiss_fft.c kissfft/kiss_fftr.c mad/bit.c mad/decoder.c mad/fixed.c mad/frame.c mad/huffman.c mad/layer12.c mad/layer3.c mad/stream.c mad/synth.c mad/timer.c mad/version.c
LOCAL_CFLAGS := -DHAVE_CONFIG_H -DFPM_ARM -ffast-math -O3

include $(BUILD_SHARED_LIBRARY)
