################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
CPP_SRCS += \
../jni/kissfft/KissFFT.cpp 

C_SRCS += \
../jni/kissfft/kiss_fft.c \
../jni/kissfft/kiss_fftr.c 

OBJS += \
./jni/kissfft/KissFFT.o \
./jni/kissfft/kiss_fft.o \
./jni/kissfft/kiss_fftr.o 

C_DEPS += \
./jni/kissfft/kiss_fft.d \
./jni/kissfft/kiss_fftr.d 

CPP_DEPS += \
./jni/kissfft/KissFFT.d 


# Each subdirectory must supply rules for building sources it contributes
jni/kissfft/%.o: ../jni/kissfft/%.cpp
	@echo 'Building file: $<'
	@echo 'Invoking: GCC C++ Compiler'
	g++ -fPIC -DFIXED_POINT -DCHECK_OVERFLOW -I"/usr/lib/jvm/java-6-sun-1.6.0.21/include" -I"/home/mzechner/workspace/trunk/gdx/jni" -I"/usr/lib/jvm/java-6-sun-1.6.0.21/include/linux" -O2 -Wall -c -I../jni/ -mfpmath=sse -msse2 -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@:%.o=%.d)" -o"$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '

jni/kissfft/%.o: ../jni/kissfft/%.c
	@echo 'Building file: $<'
	@echo 'Invoking: GCC C Compiler'
	gcc -O2 -g3 -Wall -c -fmessage-length=0 -fPIC -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@:%.o=%.d)" -o"$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '


