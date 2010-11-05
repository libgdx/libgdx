################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
CPP_SRCS += \
../jni/Box2D/Body.cpp \
../jni/Box2D/CircleShape.cpp \
../jni/Box2D/Contact.cpp \
../jni/Box2D/DistanceJoint.cpp \
../jni/Box2D/Fixture.cpp \
../jni/Box2D/FrictionJoint.cpp \
../jni/Box2D/GearJoint.cpp \
../jni/Box2D/Joint.cpp \
../jni/Box2D/LineJoint.cpp \
../jni/Box2D/MouseJoint.cpp \
../jni/Box2D/PolygonShape.cpp \
../jni/Box2D/PrismaticJoint.cpp \
../jni/Box2D/PulleyJoint.cpp \
../jni/Box2D/RevoluteJoint.cpp \
../jni/Box2D/Shape.cpp \
../jni/Box2D/World.cpp 

OBJS += \
./jni/Box2D/Body.o \
./jni/Box2D/CircleShape.o \
./jni/Box2D/Contact.o \
./jni/Box2D/DistanceJoint.o \
./jni/Box2D/Fixture.o \
./jni/Box2D/FrictionJoint.o \
./jni/Box2D/GearJoint.o \
./jni/Box2D/Joint.o \
./jni/Box2D/LineJoint.o \
./jni/Box2D/MouseJoint.o \
./jni/Box2D/PolygonShape.o \
./jni/Box2D/PrismaticJoint.o \
./jni/Box2D/PulleyJoint.o \
./jni/Box2D/RevoluteJoint.o \
./jni/Box2D/Shape.o \
./jni/Box2D/World.o 

CPP_DEPS += \
./jni/Box2D/Body.d \
./jni/Box2D/CircleShape.d \
./jni/Box2D/Contact.d \
./jni/Box2D/DistanceJoint.d \
./jni/Box2D/Fixture.d \
./jni/Box2D/FrictionJoint.d \
./jni/Box2D/GearJoint.d \
./jni/Box2D/Joint.d \
./jni/Box2D/LineJoint.d \
./jni/Box2D/MouseJoint.d \
./jni/Box2D/PolygonShape.d \
./jni/Box2D/PrismaticJoint.d \
./jni/Box2D/PulleyJoint.d \
./jni/Box2D/RevoluteJoint.d \
./jni/Box2D/Shape.d \
./jni/Box2D/World.d 


# Each subdirectory must supply rules for building sources it contributes
jni/Box2D/%.o: ../jni/Box2D/%.cpp
	@echo 'Building file: $<'
	@echo 'Invoking: GCC C++ Compiler'
	g++ -fPIC -DFIXED_POINT -DCHECK_OVERFLOW -I"/usr/lib/jvm/java-6-sun-1.6.0.21/include" -I"/home/mzechner/workspace/trunk/gdx/jni" -I"/usr/lib/jvm/java-6-sun-1.6.0.21/include/linux" -O2 -Wall -c -I../jni/ -mfpmath=sse -msse2 -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@:%.o=%.d)" -o"$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '


