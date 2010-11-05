################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
CPP_SRCS += \
../jni/Box2D/Dynamics/Joints/b2DistanceJoint.cpp \
../jni/Box2D/Dynamics/Joints/b2FrictionJoint.cpp \
../jni/Box2D/Dynamics/Joints/b2GearJoint.cpp \
../jni/Box2D/Dynamics/Joints/b2Joint.cpp \
../jni/Box2D/Dynamics/Joints/b2LineJoint.cpp \
../jni/Box2D/Dynamics/Joints/b2MouseJoint.cpp \
../jni/Box2D/Dynamics/Joints/b2PrismaticJoint.cpp \
../jni/Box2D/Dynamics/Joints/b2PulleyJoint.cpp \
../jni/Box2D/Dynamics/Joints/b2RevoluteJoint.cpp \
../jni/Box2D/Dynamics/Joints/b2WeldJoint.cpp 

OBJS += \
./jni/Box2D/Dynamics/Joints/b2DistanceJoint.o \
./jni/Box2D/Dynamics/Joints/b2FrictionJoint.o \
./jni/Box2D/Dynamics/Joints/b2GearJoint.o \
./jni/Box2D/Dynamics/Joints/b2Joint.o \
./jni/Box2D/Dynamics/Joints/b2LineJoint.o \
./jni/Box2D/Dynamics/Joints/b2MouseJoint.o \
./jni/Box2D/Dynamics/Joints/b2PrismaticJoint.o \
./jni/Box2D/Dynamics/Joints/b2PulleyJoint.o \
./jni/Box2D/Dynamics/Joints/b2RevoluteJoint.o \
./jni/Box2D/Dynamics/Joints/b2WeldJoint.o 

CPP_DEPS += \
./jni/Box2D/Dynamics/Joints/b2DistanceJoint.d \
./jni/Box2D/Dynamics/Joints/b2FrictionJoint.d \
./jni/Box2D/Dynamics/Joints/b2GearJoint.d \
./jni/Box2D/Dynamics/Joints/b2Joint.d \
./jni/Box2D/Dynamics/Joints/b2LineJoint.d \
./jni/Box2D/Dynamics/Joints/b2MouseJoint.d \
./jni/Box2D/Dynamics/Joints/b2PrismaticJoint.d \
./jni/Box2D/Dynamics/Joints/b2PulleyJoint.d \
./jni/Box2D/Dynamics/Joints/b2RevoluteJoint.d \
./jni/Box2D/Dynamics/Joints/b2WeldJoint.d 


# Each subdirectory must supply rules for building sources it contributes
jni/Box2D/Dynamics/Joints/%.o: ../jni/Box2D/Dynamics/Joints/%.cpp
	@echo 'Building file: $<'
	@echo 'Invoking: GCC C++ Compiler'
	g++ -fPIC -DFIXED_POINT -DCHECK_OVERFLOW -I"/usr/lib/jvm/java-6-sun-1.6.0.21/include" -I"/home/mzechner/workspace/trunk/gdx/jni" -I"/usr/lib/jvm/java-6-sun-1.6.0.21/include/linux" -O2 -Wall -c -I../jni/ -mfpmath=sse -msse2 -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@:%.o=%.d)" -o"$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '


