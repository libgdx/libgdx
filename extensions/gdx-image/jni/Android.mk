LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)
 
LOCAL_MODULE    := gdx-image
LOCAL_C_INCLUDES := libjpeg/ giflib/ ../../../gdx/jni/gdx2d/ 
 
LOCAL_CFLAGS := $(LOCAL_C_INCLUDES:%=-I%) -O2 -Wall -D__ANDROID__ -DHAVE_CONFIG_H
LOCAL_CPPFLAGS := $(LOCAL_C_INCLUDES:%=-I%) -O2 -Wall -D__ANDROID__
LOCAL_LDLIBS := -lm
LOCAL_ARM_MODE  := arm
 
LOCAL_SRC_FILES := memcpy_wrap.c\
	libjpeg/jcinit.c\
	libjpeg/rdrle.c\
	libjpeg/jchuff.c\
	libjpeg/jdcoefct.c\
	libjpeg/rdbmp.c\
	libjpeg/jdsample.c\
	libjpeg/jdpostct.c\
	libjpeg/jddctmgr.c\
	libjpeg/jcmarker.c\
	libjpeg/jdarith.c\
	libjpeg/jdmarker.c\
	libjpeg/jdapimin.c\
	libjpeg/jcapimin.c\
	libjpeg/jmemmgr.c\
	libjpeg/jidctflt.c\
	libjpeg/rdcolmap.c\
	libjpeg/jidctint.c\
	libjpeg/rdswitch.c\
	libjpeg/wrbmp.c\
	libjpeg/jquant1.c\
	libjpeg/jdatadst.c\
	libjpeg/jcapistd.c\
	libjpeg/jutils.c\
	libjpeg/jccolor.c\
	libjpeg/jaricom.c\
	libjpeg/jcmaster.c\
	libjpeg/jfdctfst.c\
	libjpeg/jcmainct.c\
	libjpeg/wrrle.c\
	libjpeg/jdatasrc.c\
	libjpeg/jdhuff.c\
	libjpeg/jdtrans.c\
	libjpeg/jcarith.c\
	libjpeg/jdmaster.c\
	libjpeg/jdcolor.c\
	libjpeg/jfdctflt.c\
	libjpeg/jidctfst.c\
	libjpeg/jdmerge.c\
	libjpeg/jdinput.c\
	libjpeg/cdjpeg.c\
	libjpeg/jctrans.c\
	libjpeg/jcsample.c\
	libjpeg/rdtarga.c\
	libjpeg/jcdctmgr.c\
	libjpeg/wrppm.c\
	libjpeg/jccoefct.c\
	libjpeg/jcomapi.c\
	libjpeg/wrgif.c\
	libjpeg/jquant2.c\
	libjpeg/jerror.c\
	libjpeg/jcparam.c\
	libjpeg/jfdctint.c\
	libjpeg/rdgif.c\
	libjpeg/jmemansi.c\
	libjpeg/wrtarga.c\
	libjpeg/jdmainct.c\
	libjpeg/jdapistd.c\
	libjpeg/transupp.c\
	libjpeg/rdppm.c\
	libjpeg/jcprepct.c\
	giflib/gifalloc.c\
	giflib/gif_hash.c\
	giflib/gif_err.c\
	giflib/gif_font.c\
	giflib/egif_lib.c\
	giflib/quantize.c\
	giflib/qprintf.c\
	giflib/dev2gif.c\
	giflib/dgif_lib.c\
	com.badlogic.gdx.graphics.g2d.Jpeg.cpp
 
include $(BUILD_SHARED_LIBRARY)
