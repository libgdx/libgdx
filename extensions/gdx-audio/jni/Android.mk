LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)
 
LOCAL_MODULE    := gdx-audio
LOCAL_C_INCLUDES := kissfft vorbis soundtouch/include soundtouch/source/SoundTouch/ 
 
LOCAL_CFLAGS := $(LOCAL_C_INCLUDES:%=-I%) -O2 -Wall -D__ANDROID__ -DFIXED_POINT -D_ARM_ASSEM_ -D__ANDROID__
LOCAL_CPPFLAGS := $(LOCAL_C_INCLUDES:%=-I%) -O2 -Wall -D__ANDROID__ -DFIXED_POINT -D_ARM_ASSEM_ -D__ANDROID__
LOCAL_LDLIBS := -lm
LOCAL_ARM_MODE  := arm
 
LOCAL_SRC_FILES := com.badlogic.gdx.audio.analysis.AudioTools.cpp\
	com.badlogic.gdx.audio.analysis.KissFFT.cpp\
	com.badlogic.gdx.audio.io.VorbisDecoder.cpp\
	kissfft/kiss_fft.c\
	kissfft/kiss_fftr.c\
	soundtouch/source/SoundTouch/AAFilter.cpp\
	soundtouch/source/SoundTouch/BPMDetect.cpp\
	soundtouch/source/SoundTouch/cpu_detect_x86_gcc.cpp\
	soundtouch/source/SoundTouch/FIFOSampleBuffer.cpp\
	soundtouch/source/SoundTouch/FIRFilter.cpp\
	soundtouch/source/SoundTouch/mmx_optimized.cpp\
	soundtouch/source/SoundTouch/PeakFinder.cpp\
	soundtouch/source/SoundTouch/RateTransposer.cpp\
	soundtouch/source/SoundTouch/SoundTouch.cpp\
	soundtouch/source/SoundTouch/sse_optimized.cpp\
	soundtouch/source/SoundTouch/TDStretch.cpp\
	vorbis/bitwise.c\
	vorbis/block.c\
	vorbis/codebook.c\
	vorbis/floor0.c\
	vorbis/floor1.c\
	vorbis/framing.c\
	vorbis/info.c\
	vorbis/mapping0.c\
	vorbis/mdct.c\
	vorbis/registry.c\
	vorbis/res012.c\
	vorbis/sharedbook.c\
	vorbis/synthesis.c\
	vorbis/vorbisfile.c\
	vorbis/window.c
 
include $(BUILD_SHARED_LIBRARY)
