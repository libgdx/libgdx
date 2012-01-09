LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)
 
LOCAL_MODULE    := gdx-tokamak
LOCAL_C_INCLUDES := . tokamak/include 
 
LOCAL_CFLAGS := $(LOCAL_C_INCLUDES:%=-I%) -O2 -Wall -D__ANDROID__
LOCAL_CPPFLAGS := $(LOCAL_C_INCLUDES:%=-I%) -O2 -Wall -D__ANDROID__
LOCAL_LDLIBS := -lm -lm
LOCAL_ARM_MODE  := arm
 
LOCAL_SRC_FILES := tokamak/boxcylinder.cpp\
	tokamak/collision.cpp\
	tokamak/collisionbody.cpp\
	tokamak/constraint.cpp\
	tokamak/cylinder.cpp\
	tokamak/dcd.cpp\
	tokamak/lines.cpp\
	tokamak/ne_interface.cpp\
	tokamak/perflinux.cpp\
	tokamak/region.cpp\
	tokamak/restcontact.cpp\
	tokamak/rigidbody.cpp\
	tokamak/rigidbodybase.cpp\
	tokamak/scenery.cpp\
	tokamak/simulator.cpp\
	tokamak/solver.cpp\
	tokamak/sphere.cpp\
	tokamak/stack.cpp\
	tokamak/tricollision.cpp\
	tokamak/useopcode.cpp
 
include $(BUILD_SHARED_LIBRARY)
