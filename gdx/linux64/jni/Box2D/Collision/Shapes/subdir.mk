################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
CPP_SRCS += \
../jni/Box2D/Collision/Shapes/b2CircleShape.cpp \
../jni/Box2D/Collision/Shapes/b2PolygonShape.cpp 

OBJS += \
./jni/Box2D/Collision/Shapes/b2CircleShape.o \
./jni/Box2D/Collision/Shapes/b2PolygonShape.o 

CPP_DEPS += \
./jni/Box2D/Collision/Shapes/b2CircleShape.d \
./jni/Box2D/Collision/Shapes/b2PolygonShape.d 


# Each subdirectory must supply rules for building sources it contributes
jni/Box2D/Collision/Shapes/%.o: ../jni/Box2D/Collision/Shapes/%.cpp
	@echo 'Building file: $<'
	@echo 'Invoking: GCC C++ Compiler'
	g++ -fPIC -DFIXED_POINT -DCHECK_OVERFLOW -I"/usr/lib/jvm/java-6-sun-1.6.0.21/include" -I"/home/mzechner/workspace/trunk/gdx/jni" -I"/usr/lib/jvm/java-6-sun-1.6.0.21/include/linux" -O2 -Wall -c -I../jni/ -mfpmath=sse -msse2 -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@:%.o=%.d)" -o"$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '


