################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
C_SRCS += \
../jni/kissfft/kiss_fft.c \
../jni/kissfft/kiss_fftr.c 

OBJS += \
./jni/kissfft/kiss_fft.o \
./jni/kissfft/kiss_fftr.o 

C_DEPS += \
./jni/kissfft/kiss_fft.d \
./jni/kissfft/kiss_fftr.d 


# Each subdirectory must supply rules for building sources it contributes
jni/kissfft/%.o: ../jni/kissfft/%.c
	@echo 'Building file: $<'
	@echo 'Invoking: GCC C Compiler'
	gcc -O0 -g3 -Wall -c -fmessage-length=0 -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@:%.o=%.d)" -o"$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '


