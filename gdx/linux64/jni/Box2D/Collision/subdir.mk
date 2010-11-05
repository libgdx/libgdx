################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
CPP_SRCS += \
../jni/Box2D/Collision/b2BroadPhase.cpp \
../jni/Box2D/Collision/b2CollideCircle.cpp \
../jni/Box2D/Collision/b2CollidePolygon.cpp \
../jni/Box2D/Collision/b2Collision.cpp \
../jni/Box2D/Collision/b2Distance.cpp \
../jni/Box2D/Collision/b2DynamicTree.cpp \
../jni/Box2D/Collision/b2TimeOfImpact.cpp 

OBJS += \
./jni/Box2D/Collision/b2BroadPhase.o \
./jni/Box2D/Collision/b2CollideCircle.o \
./jni/Box2D/Collision/b2CollidePolygon.o \
./jni/Box2D/Collision/b2Collision.o \
./jni/Box2D/Collision/b2Distance.o \
./jni/Box2D/Collision/b2DynamicTree.o \
./jni/Box2D/Collision/b2TimeOfImpact.o 

CPP_DEPS += \
./jni/Box2D/Collision/b2BroadPhase.d \
./jni/Box2D/Collision/b2CollideCircle.d \
./jni/Box2D/Collision/b2CollidePolygon.d \
./jni/Box2D/Collision/b2Collision.d \
./jni/Box2D/Collision/b2Distance.d \
./jni/Box2D/Collision/b2DynamicTree.d \
./jni/Box2D/Collision/b2TimeOfImpact.d 


# Each subdirectory must supply rules for building sources it contributes
jni/Box2D/Collision/%.o: ../jni/Box2D/Collision/%.cpp
	@echo 'Building file: $<'
	@echo 'Invoking: GCC C++ Compiler'
	g++ -fPIC -DFIXED_POINT -DCHECK_OVERFLOW -I"/usr/lib/jvm/java-6-sun-1.6.0.21/include" -I"/home/mzechner/workspace/trunk/gdx/jni" -I"/usr/lib/jvm/java-6-sun-1.6.0.21/include/linux" -O2 -Wall -c -I../jni/ -mfpmath=sse -msse2 -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@:%.o=%.d)" -o"$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '


