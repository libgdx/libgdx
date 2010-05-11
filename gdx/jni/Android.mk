LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := gdx
LOCAL_ARM_MODE := arm
LOCAL_SRC_FILES := \
AudioTools.cpp \
Mpg123Decoder.cpp \
vorbis/VorbisDecoder.cpp \
kissfft/KissFFT.cpp \
Box2D/Body.cpp \
Box2D/CircleShape.cpp \
Box2D/Contact.cpp \
Box2D/Fixture.cpp \
Box2D/Joint.cpp \
Box2D/PolygonShape.cpp \
Box2D/Shape.cpp \
Box2D/World.cpp \
Box2D/Box2D.h \
Box2D/DistanceJoint.cpp \
Box2D/FrictionJoint.cpp \
Box2D/GearJoint.cpp \
Box2D/LineJoint.cpp \
Box2D/MouseJoint.cpp \
Box2D/PrismaticJoint.cpp \
Box2D/PulleyJoint.cpp \
Box2D/RevoluteJoint.cpp \
Box2D/Collision/b2BroadPhase.cpp \
Box2D/Collision/b2BroadPhase.h \
Box2D/Collision/b2CollideCircle.cpp \
Box2D/Collision/b2CollidePolygon.cpp \
Box2D/Collision/b2Collision.cpp \
Box2D/Collision/b2Collision.h \
Box2D/Collision/b2Distance.cpp \
Box2D/Collision/b2Distance.h \
Box2D/Collision/b2DynamicTree.cpp \
Box2D/Collision/b2DynamicTree.h \
Box2D/Collision/b2TimeOfImpact.cpp \
Box2D/Collision/b2TimeOfImpact.h \
Box2D/Collision/Shapes/b2CircleShape.cpp \
Box2D/Collision/Shapes/b2CircleShape.h \
Box2D/Collision/Shapes/b2PolygonShape.cpp \
Box2D/Collision/Shapes/b2PolygonShape.h \
Box2D/Collision/Shapes/b2Shape.h \
Box2D/Common/b2BlockAllocator.cpp \
Box2D/Common/b2BlockAllocator.h \
Box2D/Common/b2Math.cpp \
Box2D/Common/b2Math.h \
Box2D/Common/b2Settings.cpp \
Box2D/Common/b2Settings.h \
Box2D/Common/b2StackAllocator.cpp \
Box2D/Common/b2StackAllocator.h \
Box2D/Dynamics/b2Body.cpp \
Box2D/Dynamics/b2Body.h \
Box2D/Dynamics/b2ContactManager.cpp \
Box2D/Dynamics/b2ContactManager.h \
Box2D/Dynamics/b2Fixture.cpp \
Box2D/Dynamics/b2Fixture.h \
Box2D/Dynamics/b2Island.cpp \
Box2D/Dynamics/b2Island.h \
Box2D/Dynamics/b2TimeStep.h \
Box2D/Dynamics/b2World.cpp \
Box2D/Dynamics/b2World.h \
Box2D/Dynamics/b2WorldCallbacks.cpp \
Box2D/Dynamics/b2WorldCallbacks.h \ \
Box2D/Dynamics/Contacts/b2CircleContact.cpp \
Box2D/Dynamics/Contacts/b2CircleContact.h \
Box2D/Dynamics/Contacts/b2Contact.cpp \
Box2D/Dynamics/Contacts/b2Contact.h \
Box2D/Dynamics/Contacts/b2ContactSolver.cpp \
Box2D/Dynamics/Contacts/b2ContactSolver.h \
Box2D/Dynamics/Contacts/b2PolygonAndCircleContact.cpp \
Box2D/Dynamics/Contacts/b2PolygonAndCircleContact.h \
Box2D/Dynamics/Contacts/b2PolygonContact.cpp \
Box2D/Dynamics/Contacts/b2PolygonContact.h \
Box2D/Dynamics/Contacts/b2TOISolver.cpp \
Box2D/Dynamics/Contacts/b2TOISolver.h \
Box2D/Dynamics/Joints/b2DistanceJoint.cpp \
Box2D/Dynamics/Joints/b2DistanceJoint.h \
Box2D/Dynamics/Joints/b2FrictionJoint.cpp \
Box2D/Dynamics/Joints/b2FrictionJoint.h \
Box2D/Dynamics/Joints/b2GearJoint.cpp \
Box2D/Dynamics/Joints/b2GearJoint.h \
Box2D/Dynamics/Joints/b2Joint.cpp \
Box2D/Dynamics/Joints/b2Joint.h \
Box2D/Dynamics/Joints/b2LineJoint.cpp \
Box2D/Dynamics/Joints/b2LineJoint.h \
Box2D/Dynamics/Joints/b2MouseJoint.cpp \
Box2D/Dynamics/Joints/b2MouseJoint.h \
Box2D/Dynamics/Joints/b2PrismaticJoint.cpp \
Box2D/Dynamics/Joints/b2PrismaticJoint.h \
Box2D/Dynamics/Joints/b2PulleyJoint.cpp \
Box2D/Dynamics/Joints/b2PulleyJoint.h \
Box2D/Dynamics/Joints/b2RevoluteJoint.cpp \
Box2D/Dynamics/Joints/b2RevoluteJoint.h \
Box2D/Dynamics/Joints/b2WeldJoint.cpp \
Box2D/Dynamics/Joints/b2WeldJoint.h \
kissfft/kiss_fft.c \
kissfft/kiss_fftr.c \
mpg123/equalizer.c \
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
mpg123/id3.c \
vorbis/bitwise.c \
vorbis/block.c \
vorbis/codebook.c \
vorbis/floor0.c \
vorbis/floor1.c \
vorbis/framing.c \
vorbis/info.c \
vorbis/mapping0.c \
vorbis/mdct.c \
vorbis/registry.c \
vorbis/res012.c \
vorbis/sharedbook.c \
vorbis/synthesis.c \
vorbis/vorbisfile.c \
vorbis/window.c
				   
				   
LOCAL_CFLAGS := -DFIXED_POINT -ffast-math -O3 -Wall -I$(LOCAL_PATH) -D_ARM_ASSEM_
LOCAL_CPPFLAGS := -DFIXED_POINT -I$LOCAL_PATH/libvorbis/ -D_ARM_ASSEM_

include $(BUILD_SHARED_LIBRARY)
