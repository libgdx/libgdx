LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)
 
LOCAL_MODULE    := gdx-bullet
LOCAL_C_INCLUDES := libjpeg/ 
 
LOCAL_CFLAGS := $(LOCAL_C_INCLUDES:%=-I%) -O2 -Wall -D__ANDROID__
LOCAL_CPPFLAGS := $(LOCAL_C_INCLUDES:%=-I%) -O2 -Wall -D__ANDROID__
LOCAL_LDLIBS := -lm
LOCAL_ARM_MODE  := arm
 
LOCAL_SRC_FILES := libjpeg/cdjpeg.c\
	libjpeg/jaricom.c\
	libjpeg/jcapimin.c\
	libjpeg/jcapistd.c\
	libjpeg/jcarith.c\
	libjpeg/jccoefct.c\
	libjpeg/jccolor.c\
	libjpeg/jcdctmgr.c\
	libjpeg/jchuff.c\
	libjpeg/jcinit.c\
	libjpeg/jcmainct.c\
	libjpeg/jcmarker.c\
	libjpeg/jcmaster.c\
	libjpeg/jcomapi.c\
	libjpeg/jcparam.c\
	libjpeg/jcprepct.c\
	libjpeg/jcsample.c\
	libjpeg/jctrans.c\
	libjpeg/jdapimin.c\
	libjpeg/jdapistd.c\
	libjpeg/jdarith.c\
	libjpeg/jdatadst.c\
	libjpeg/jdatasrc.c\
	libjpeg/jdcoefct.c\
	libjpeg/jdcolor.c\
	libjpeg/jddctmgr.c\
	libjpeg/jdhuff.c\
	libjpeg/jdinput.c\
	libjpeg/jdmainct.c\
	libjpeg/jdmarker.c\
	libjpeg/jdmaster.c\
	libjpeg/jdmerge.c\
	libjpeg/jdpostct.c\
	libjpeg/jdsample.c\
	libjpeg/jdtrans.c\
	libjpeg/jerror.c\
	libjpeg/jfdctflt.c\
	libjpeg/jfdctfst.c\
	libjpeg/jfdctint.c\
	libjpeg/jidctflt.c\
	libjpeg/jidctfst.c\
	libjpeg/jidctint.c\
	libjpeg/jmemansi.c\
	libjpeg/jmemmgr.c\
	libjpeg/jquant1.c\
	libjpeg/jquant2.c\
	libjpeg/jutils.c\
	libjpeg/rdbmp.c\
	libjpeg/rdcolmap.c\
	libjpeg/rdgif.c\
	libjpeg/rdppm.c\
	libjpeg/rdrle.c\
	libjpeg/rdswitch.c\
	libjpeg/rdtarga.c\
	libjpeg/transupp.c\
	libjpeg/wrbmp.c\
	libjpeg/wrgif.c\
	libjpeg/wrppm.c\
	libjpeg/wrrle.c\
	libjpeg/wrtarga.c
 
include $(BUILD_SHARED_LIBRARY)
