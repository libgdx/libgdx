LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)
 
LOCAL_MODULE    := gdx-audio
LOCAL_C_INCLUDES := kissfft vorbis soundtouch/include soundtouch/source/SoundTouch/ 
 
LOCAL_CFLAGS := $(LOCAL_C_INCLUDES:%=-I%) -O2 -Wall -D__ANDROID__ -DFIXED_POINT -D_ARM_ASSEM_ -D__ANDROID__ -DMPG123_NO_CONFIGURE -DOPT_GENERIC -DHAVE_STRERROR -DMPG123_NO_LARGENAME
LOCAL_CPPFLAGS := $(LOCAL_C_INCLUDES:%=-I%) -O2 -Wall -D__ANDROID__ -DFIXED_POINT -D_ARM_ASSEM_ -D__ANDROID__ -DMPG123_NO_CONFIGURE -DOPT_GENERIC -DHAVE_STRERROR -DMPG123_NO_LARGENAME
LOCAL_LDLIBS := -lm
LOCAL_ARM_MODE  := arm
 
LOCAL_SRC_FILES := com.badlogic.gdx.audio.analysis.AudioTools.cpp\
	com.badlogic.gdx.audio.analysis.KissFFT.cpp\
	com.badlogic.gdx.audio.io.Mpg123Decoder.cpp\
	com.badlogic.gdx.audio.io.VorbisDecoder.cpp\
	com.badlogic.gdx.audio.transform.SoundTouch.cpp\
	kissfft/kiss_fft.c\
	kissfft/kiss_fftr.c\
	libmpg123/compat.c\
	libmpg123/dct64.c\
	libmpg123/equalizer.c\
	libmpg123/format.c\
	libmpg123/frame.c\
	libmpg123/icy.c\
	libmpg123/icy2utf8.c\
	libmpg123/id3.c\
	libmpg123/index.c\
	libmpg123/layer1.c\
	libmpg123/layer2.c\
	libmpg123/layer3.c\
	libmpg123/libmpg123.c\
	libmpg123/ntom.c\
	libmpg123/optimize.c\
	libmpg123/parse.c\
	libmpg123/readers.c\
	libmpg123/stringbuf.c\
	libmpg123/synth.c\
	libmpg123/synth_8bit.c\
	libmpg123/synth_arm.S\
	libmpg123/synth_real.c\
	libmpg123/synth_s32.c\
	libmpg123/tabinit.c\
	soundtouch/source/SoundTouch/AAFilter.cpp\
	soundtouch/source/SoundTouch/BPMDetect.cpp\
	soundtouch/source/SoundTouch/cpu_detect_x86.cpp\
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
