LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := gdx
LOCAL_ARM_MODE := arm
LOCAL_SRC_FILES := AudioTools.cpp Mpg123Decoder.cpp KissFFT.cpp NativeFFT.cpp kissfft/kiss_fft.c kissfft/kiss_fftr.c mpg123/equalizer.c mpg123/index.c mpg123/layer2.c mpg123/synth.c mpg123/dct64.c mpg123/format.c mpg123/layer3.c mpg123/ntom.c mpg123/parse.c mpg123/readers.c mpg123/frame.c mpg123/layer1.c mpg123/libmpg123.c mpg123/optimize.c mpg123/synth_arm.S mpg123/tabinit.c mpg123/id3.c
				   
LOCAL_CFLAGS := -DFIXED_POINT -ffast-math -O3 -Wall
LOCAL_CPPFLAGS := -DFIXED_POINT

include $(BUILD_SHARED_LIBRARY)
