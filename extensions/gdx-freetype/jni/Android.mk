LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)
 
LOCAL_MODULE    := gdx-freetype
LOCAL_C_INCLUDES := freetype-2.4.8/include 
 
LOCAL_CFLAGS := $(LOCAL_C_INCLUDES:%=-I%) -O2 -Wall -D__ANDROID__ -std=c99 -DFT2_BUILD_LIBRARY
LOCAL_CPPFLAGS := $(LOCAL_C_INCLUDES:%=-I%) -O2 -Wall -D__ANDROID__ -std=c99 -DFT2_BUILD_LIBRARY
LOCAL_LDLIBS := -lm
LOCAL_ARM_MODE  := arm
 
LOCAL_SRC_FILES := com.badlogic.gdx.graphics.g2d.freetype.FreeType.cpp\
	freetype-2.4.8/src/autofit/autofit.c\
	freetype-2.4.8/src/base/ftbase.c\
	freetype-2.4.8/src/base/ftbbox.c\
	freetype-2.4.8/src/base/ftbdf.c\
	freetype-2.4.8/src/base/ftbitmap.c\
	freetype-2.4.8/src/base/ftcid.c\
	freetype-2.4.8/src/base/ftdebug.c\
	freetype-2.4.8/src/base/ftfstype.c\
	freetype-2.4.8/src/base/ftgasp.c\
	freetype-2.4.8/src/base/ftglyph.c\
	freetype-2.4.8/src/base/ftgxval.c\
	freetype-2.4.8/src/base/ftinit.c\
	freetype-2.4.8/src/base/ftlcdfil.c\
	freetype-2.4.8/src/base/ftmm.c\
	freetype-2.4.8/src/base/ftotval.c\
	freetype-2.4.8/src/base/ftpatent.c\
	freetype-2.4.8/src/base/ftpfr.c\
	freetype-2.4.8/src/base/ftstroke.c\
	freetype-2.4.8/src/base/ftsynth.c\
	freetype-2.4.8/src/base/ftsystem.c\
	freetype-2.4.8/src/base/fttype1.c\
	freetype-2.4.8/src/base/ftwinfnt.c\
	freetype-2.4.8/src/base/ftxf86.c\
	freetype-2.4.8/src/bdf/bdf.c\
	freetype-2.4.8/src/bzip2/ftbzip2.c\
	freetype-2.4.8/src/cache/ftcache.c\
	freetype-2.4.8/src/cff/cff.c\
	freetype-2.4.8/src/cid/type1cid.c\
	freetype-2.4.8/src/gxvalid/gxvalid.c\
	freetype-2.4.8/src/gzip/ftgzip.c\
	freetype-2.4.8/src/lzw/ftlzw.c\
	freetype-2.4.8/src/otvalid/otvalid.c\
	freetype-2.4.8/src/pcf/pcf.c\
	freetype-2.4.8/src/pfr/pfr.c\
	freetype-2.4.8/src/psaux/psaux.c\
	freetype-2.4.8/src/pshinter/pshinter.c\
	freetype-2.4.8/src/psnames/psnames.c\
	freetype-2.4.8/src/raster/raster.c\
	freetype-2.4.8/src/sfnt/sfnt.c\
	freetype-2.4.8/src/smooth/smooth.c\
	freetype-2.4.8/src/truetype/truetype.c\
	freetype-2.4.8/src/type1/type1.c\
	freetype-2.4.8/src/type42/type42.c\
	freetype-2.4.8/src/winfonts/winfnt.c
 
include $(BUILD_SHARED_LIBRARY)
