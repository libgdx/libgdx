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
    src/dec/alpha.c \
    src/dec/buffer.c \
    src/dec/frame.c \
    src/dec/idec.c \
    src/dec/io.c \
    src/dec/quant.c \
    src/dec/tree.c \
    src/dec/vp8.c \
    src/dec/vp8l.c \
    src/dec/webp.c \

dsp_dec_srcs := \
    src/dsp/alpha_processing.c \
    src/dsp/alpha_processing_mips_dsp_r2.c \
    src/dsp/alpha_processing_sse2.c \
    src/dsp/alpha_processing_sse41.c \
    src/dsp/argb.c \
    src/dsp/argb_mips_dsp_r2.c \
    src/dsp/argb_sse2.c \
    src/dsp/cpu.c \
    src/dsp/dec.c \
    src/dsp/dec_clip_tables.c \
    src/dsp/dec_mips32.c \
    src/dsp/dec_mips_dsp_r2.c \
    src/dsp/dec_neon.$(NEON) \
    src/dsp/dec_sse2.c \
    src/dsp/dec_sse41.c \
    src/dsp/filters.c \
    src/dsp/filters_mips_dsp_r2.c \
    src/dsp/filters_sse2.c \
    src/dsp/lossless.c \
    src/dsp/lossless_mips_dsp_r2.c \
    src/dsp/lossless_neon.$(NEON) \
    src/dsp/lossless_sse2.c \
    src/dsp/rescaler.c \
    src/dsp/rescaler_mips32.c \
    src/dsp/rescaler_mips_dsp_r2.c \
    src/dsp/rescaler_neon.$(NEON) \
    src/dsp/rescaler_sse2.c \
    src/dsp/upsampling.c \
    src/dsp/upsampling_mips_dsp_r2.c \
    src/dsp/upsampling_neon.$(NEON) \
    src/dsp/upsampling_sse2.c \
    src/dsp/yuv.c \
    src/dsp/yuv_mips32.c \
    src/dsp/yuv_mips_dsp_r2.c \
    src/dsp/yuv_sse2.c \

utils_dec_srcs := \
    src/utils/bit_reader.c \
    src/utils/color_cache.c \
    src/utils/filters.c \
    src/utils/huffman.c \
    src/utils/quant_levels_dec.c \
    src/utils/random.c \
    src/utils/rescaler.c \
    src/utils/thread.c \
    src/utils/utils.c \

#############################################################
LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)
 
LOCAL_MODULE    := gdx
LOCAL_C_INCLUDES += $(LOCAL_PATH)/src $(LOCAL_PATH)/libwebp/src

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
	$(dec_srcs:%=libwebp/%) \
	$(dsp_dec_srcs:%=libwebp/%) \
	$(utils_dec_srcs:%=libwebp/%) \
	memcpy_wrap.c

include $(BUILD_SHARED_LIBRARY)
$(call import-module,android/cpufeatures)
