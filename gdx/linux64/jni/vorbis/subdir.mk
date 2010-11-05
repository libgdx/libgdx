################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
CPP_SRCS += \
../jni/vorbis/VorbisDecoder.cpp 

C_SRCS += \
../jni/vorbis/bitwise.c \
../jni/vorbis/block.c \
../jni/vorbis/codebook.c \
../jni/vorbis/floor0.c \
../jni/vorbis/floor1.c \
../jni/vorbis/framing.c \
../jni/vorbis/info.c \
../jni/vorbis/mapping0.c \
../jni/vorbis/mdct.c \
../jni/vorbis/registry.c \
../jni/vorbis/res012.c \
../jni/vorbis/sharedbook.c \
../jni/vorbis/synthesis.c \
../jni/vorbis/vorbisfile.c \
../jni/vorbis/window.c 

OBJS += \
./jni/vorbis/VorbisDecoder.o \
./jni/vorbis/bitwise.o \
./jni/vorbis/block.o \
./jni/vorbis/codebook.o \
./jni/vorbis/floor0.o \
./jni/vorbis/floor1.o \
./jni/vorbis/framing.o \
./jni/vorbis/info.o \
./jni/vorbis/mapping0.o \
./jni/vorbis/mdct.o \
./jni/vorbis/registry.o \
./jni/vorbis/res012.o \
./jni/vorbis/sharedbook.o \
./jni/vorbis/synthesis.o \
./jni/vorbis/vorbisfile.o \
./jni/vorbis/window.o 

C_DEPS += \
./jni/vorbis/bitwise.d \
./jni/vorbis/block.d \
./jni/vorbis/codebook.d \
./jni/vorbis/floor0.d \
./jni/vorbis/floor1.d \
./jni/vorbis/framing.d \
./jni/vorbis/info.d \
./jni/vorbis/mapping0.d \
./jni/vorbis/mdct.d \
./jni/vorbis/registry.d \
./jni/vorbis/res012.d \
./jni/vorbis/sharedbook.d \
./jni/vorbis/synthesis.d \
./jni/vorbis/vorbisfile.d \
./jni/vorbis/window.d 

CPP_DEPS += \
./jni/vorbis/VorbisDecoder.d 


# Each subdirectory must supply rules for building sources it contributes
jni/vorbis/%.o: ../jni/vorbis/%.cpp
	@echo 'Building file: $<'
	@echo 'Invoking: GCC C++ Compiler'
	g++ -fPIC -DFIXED_POINT -DCHECK_OVERFLOW -I"/usr/lib/jvm/java-6-sun-1.6.0.21/include" -I"/home/mzechner/workspace/trunk/gdx/jni" -I"/usr/lib/jvm/java-6-sun-1.6.0.21/include/linux" -O2 -Wall -c -I../jni/ -mfpmath=sse -msse2 -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@:%.o=%.d)" -o"$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '

jni/vorbis/%.o: ../jni/vorbis/%.c
	@echo 'Building file: $<'
	@echo 'Invoking: GCC C Compiler'
	gcc -O2 -g3 -Wall -c -fmessage-length=0 -fPIC -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@:%.o=%.d)" -o"$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '


