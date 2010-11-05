################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
CPP_SRCS += \
../jni/Box2D/Dynamics/b2Body.cpp \
../jni/Box2D/Dynamics/b2ContactManager.cpp \
../jni/Box2D/Dynamics/b2Fixture.cpp \
../jni/Box2D/Dynamics/b2Island.cpp \
../jni/Box2D/Dynamics/b2World.cpp \
../jni/Box2D/Dynamics/b2WorldCallbacks.cpp 

OBJS += \
./jni/Box2D/Dynamics/b2Body.o \
./jni/Box2D/Dynamics/b2ContactManager.o \
./jni/Box2D/Dynamics/b2Fixture.o \
./jni/Box2D/Dynamics/b2Island.o \
./jni/Box2D/Dynamics/b2World.o \
./jni/Box2D/Dynamics/b2WorldCallbacks.o 

CPP_DEPS += \
./jni/Box2D/Dynamics/b2Body.d \
./jni/Box2D/Dynamics/b2ContactManager.d \
./jni/Box2D/Dynamics/b2Fixture.d \
./jni/Box2D/Dynamics/b2Island.d \
./jni/Box2D/Dynamics/b2World.d \
./jni/Box2D/Dynamics/b2WorldCallbacks.d 


# Each subdirectory must supply rules for building sources it contributes
jni/Box2D/Dynamics/%.o: ../jni/Box2D/Dynamics/%.cpp
	@echo 'Building file: $<'
	@echo 'Invoking: GCC C++ Compiler'
	g++ -fPIC -DFIXED_POINT -DCHECK_OVERFLOW -I"/usr/lib/jvm/java-6-sun-1.6.0.21/include" -I"/home/mzechner/workspace/trunk/gdx/jni" -I"/usr/lib/jvm/java-6-sun-1.6.0.21/include/linux" -O2 -Wall -c -I../jni/ -mfpmath=sse -msse2 -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@:%.o=%.d)" -o"$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '


