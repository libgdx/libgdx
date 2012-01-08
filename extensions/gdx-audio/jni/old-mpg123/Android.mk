LOCAL_MODULE    := libmpg123
LOCAL_ARM_MODE  := arm
LOCAL_CFLAGS    := -O2 -Wall

LOCAL_SRC_FILES = mpg123/equalizer.c \
				 mpg123/index.c \
				 mpg123/layer2.c \
				 mpg123/synth.c \
				 mpg123/dct64.c \
				 mpg123/format.c \
				 mpg123/layer3.c \
				 mpg123/ntom.c \
				 mpg123/parse.c \
				 mpg123/readers.c \
				 mpg123/frame.c \
				 mpg123/layer1.c \
				 mpg123/libmpg123.c \
				 mpg123/optimize.c \
				 mpg123/synth_arm.S \
				 mpg123/tabinit.c \
				 mpg123/id3.c

include $(BUILD_SHARED_LIBRARY)