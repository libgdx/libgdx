################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
CPP_SRCS += \
../jni/AudioTools.cpp \
../jni/BufferUtils.cpp \
../jni/MD5Jni.cpp \
../jni/Mpg123Decoder.cpp \
../jni/Sprite2.cpp 

OBJS += \
./jni/AudioTools.o \
./jni/BufferUtils.o \
./jni/MD5Jni.o \
./jni/Mpg123Decoder.o \
./jni/Sprite2.o 

CPP_DEPS += \
./jni/AudioTools.d \
./jni/BufferUtils.d \
./jni/MD5Jni.d \
./jni/Mpg123Decoder.d \
./jni/Sprite2.d 


# Each subdirectory must supply rules for building sources it contributes
jni/%.o: ../jni/%.cpp
	@echo 'Building file: $<'
	@echo 'Invoking: GCC C++ Compiler'
	g++ -fPIC -DFIXED_POINT -DCHECK_OVERFLOW -I"/usr/lib/jvm/java-6-sun-1.6.0.21/include" -I"/home/mzechner/workspace/trunk/gdx/jni" -I"/usr/lib/jvm/java-6-sun-1.6.0.21/include/linux" -O2 -Wall -c -I../jni/ -mfpmath=sse -msse2 -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@:%.o=%.d)" -o"$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '


