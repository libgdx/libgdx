LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)
 
LOCAL_MODULE    := gdx-freetype
LOCAL_C_INCLUDES := freetype-2.9.1/include 
 
LOCAL_CFLAGS := $(LOCAL_C_INCLUDES:%=-I%) -O2 -Wall -D__ANDROID__  -DFT2_BUILD_LIBRARY
LOCAL_CPPFLAGS := $(LOCAL_C_INCLUDES:%=-I%) -O2 -Wall -D__ANDROID__  -DFT2_BUILD_LIBRARY
LOCAL_LDLIBS := -lm
LOCAL_ARM_MODE  := arm
 
LOCAL_SRC_FILES := freetype-2.9.1/src/pcf/pcf.c\
	freetype-2.9.1/src/type1/type1.c\
	freetype-2.9.1/src/cache/ftcache.c\
	freetype-2.9.1/src/sfnt/sfnt.c\
	freetype-2.9.1/src/winfonts/winfnt.c\
	freetype-2.9.1/src/bdf/bdf.c\
	freetype-2.9.1/src/smooth/smooth.c\
	freetype-2.9.1/src/type42/type42.c\
	freetype-2.9.1/src/cff/cff.c\
	freetype-2.9.1/src/gzip/ftgzip.c\
	freetype-2.9.1/src/lzw/ftlzw.c\
	freetype-2.9.1/src/gxvalid/gxvalid.c\
	freetype-2.9.1/src/psaux/psaux.c\
	freetype-2.9.1/src/cid/type1cid.c\
	freetype-2.9.1/src/pfr/pfr.c\
	freetype-2.9.1/src/otvalid/otvalid.c\
	freetype-2.9.1/src/autofit/autofit.c\
	freetype-2.9.1/src/pshinter/pshinter.c\
	freetype-2.9.1/src/truetype/truetype.c\
	freetype-2.9.1/src/raster/raster.c\
	freetype-2.9.1/src/psnames/psnames.c\
	freetype-2.9.1/src/base/ftinit.c\
	freetype-2.9.1/src/base/ftgxval.c\
	freetype-2.9.1/src/base/ftbase.c\
	freetype-2.9.1/src/base/ftpfr.c\
	freetype-2.9.1/src/base/ftbitmap.c\
	freetype-2.9.1/src/base/ftcid.c\
	freetype-2.9.1/src/base/ftglyph.c\
	freetype-2.9.1/src/base/ftsystem.c\
	freetype-2.9.1/src/base/ftbdf.c\
	freetype-2.9.1/src/base/ftpatent.c\
	freetype-2.9.1/src/base/fttype1.c\
	freetype-2.9.1/src/base/ftwinfnt.c\
	freetype-2.9.1/src/base/ftdebug.c\
	freetype-2.9.1/src/base/ftmm.c\
	freetype-2.9.1/src/base/ftfstype.c\
	freetype-2.9.1/src/base/ftbbox.c\
	freetype-2.9.1/src/base/ftstroke.c\
	freetype-2.9.1/src/base/ftotval.c\
	freetype-2.9.1/src/base/ftgasp.c\
	freetype-2.9.1/src/base/ftsynth.c\
	freetype-2.9.1/src/bzip2/ftbzip2.c\
	com.badlogic.gdx.graphics.g2d.freetype.FreeType.cpp
 
include $(BUILD_SHARED_LIBRARY)
