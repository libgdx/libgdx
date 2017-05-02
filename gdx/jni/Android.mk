NEON_ABI := armeabi-v7a armeabi-v8a
ifneq ($(filter $(TARGET_ARCH_ABI), $(NEON_ABI)),)
  # Setting LOCAL_ARM_NEON will enable -mfpu=neon which may cause illegal
  # instructions to be generated for armv7a code. Instead target the neon code
  # specifically.
  NEON := c.neon
else
  NEON := c
endif

dec_srcs := \
    libwebp/dec/alpha.c \
    libwebp/dec/buffer.c \
    libwebp/dec/frame.c \
    libwebp/dec/idec.c \
    libwebp/dec/io.c \
    libwebp/dec/quant.c \
    libwebp/dec/tree.c \
    libwebp/dec/vp8.c \
    libwebp/dec/vp8l.c \
    libwebp/dec/webp.c \

dsp_dec_srcs := \
    libwebp/dsp/alpha_processing.c \
    libwebp/dsp/alpha_processing_mips_dsp_r2.c \
    libwebp/dsp/alpha_processing_sse2.c \
    libwebp/dsp/alpha_processing_sse41.c \
    libwebp/dsp/argb.c \
    libwebp/dsp/argb_mips_dsp_r2.c \
    libwebp/dsp/argb_sse2.c \
    libwebp/dsp/cpu.c \
    libwebp/dsp/dec.c \
    libwebp/dsp/dec_clip_tables.c \
    libwebp/dsp/dec_mips32.c \
    libwebp/dsp/dec_mips_dsp_r2.c \
    libwebp/dsp/dec_neon.$(NEON) \
    libwebp/dsp/dec_sse2.c \
    libwebp/dsp/dec_sse41.c \
    libwebp/dsp/filters.c \
    libwebp/dsp/filters_mips_dsp_r2.c \
    libwebp/dsp/filters_sse2.c \
    libwebp/dsp/lossless.c \
    libwebp/dsp/lossless_mips_dsp_r2.c \
    libwebp/dsp/lossless_neon.$(NEON) \
    libwebp/dsp/lossless_sse2.c \
    libwebp/dsp/rescaler.c \
    libwebp/dsp/rescaler_mips32.c \
    libwebp/dsp/rescaler_mips_dsp_r2.c \
    libwebp/dsp/rescaler_neon.$(NEON) \
    libwebp/dsp/rescaler_sse2.c \
    libwebp/dsp/upsampling.c \
    libwebp/dsp/upsampling_mips_dsp_r2.c \
    libwebp/dsp/upsampling_neon.$(NEON) \
    libwebp/dsp/upsampling_sse2.c \
    libwebp/dsp/yuv.c \
    libwebp/dsp/yuv_mips32.c \
    libwebp/dsp/yuv_mips_dsp_r2.c \
    libwebp/dsp/yuv_sse2.c \

utils_dec_srcs := \
    libwebp/utils/bit_reader.c \
    libwebp/utils/color_cache.c \
    libwebp/utils/filters.c \
    libwebp/utils/huffman.c \
    libwebp/utils/quant_levels_dec.c \
    libwebp/utils/random.c \
    libwebp/utils/rescaler.c \
    libwebp/utils/thread.c \
    libwebp/utils/utils.c \

#############################################################
LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)
 
LOCAL_MODULE    := gdx
LOCAL_C_INCLUDES := $(LOCAL_PATH)/libwebp/src

LOCAL_CFLAGS := $(LOCAL_C_INCLUDES:%=-I%) -O2 -Wall -D__ANDROID__ \
                -DHAVE_MALLOC_H -DHAVE_PTHREAD -DWEBP_USE_THREAD \
                -finline-functions -ffast-math \
                -ffunction-sections -fdata-sections \
                -frename-registers -s
LOCAL_CPPFLAGS := $(LOCAL_C_INCLUDES:%=-I%) -O2 -Wall -D__ANDROID__ \
                -DHAVE_MALLOC_H -DHAVE_PTHREAD -DWEBP_USE_THREAD \
                -finline-functions -ffast-math \
                -ffunction-sections -fdata-sections \
                -frename-registers -s
LOCAL_LDLIBS := -lm -lGLESv2 -llog
LOCAL_ARM_MODE  := arm
LOCAL_STATIC_LIBRARIES := cpufeatures

LOCAL_SRC_FILES := android/AndroidGL20.cpp\
	com.badlogic.gdx.graphics.g2d.Gdx2DPixmap.cpp\
	com.badlogic.gdx.graphics.glutils.ETC1.cpp\
	com.badlogic.gdx.math.Matrix4.cpp\
	com.badlogic.gdx.utils.BufferUtils.cpp\
	etc1/etc1_utils.cpp\
	gdx2d/gdx2d.c\
	gdx2d/jpgd.cpp\
	gdx2d/jpgd_c.cpp\
	$(dec_srcs) \
	$(dsp_dec_srcs) \
	$(utils_dec_srcs) \
	memcpy_wrap.c

include $(BUILD_SHARED_LIBRARY)
$(call import-module,android/cpufeatures)
