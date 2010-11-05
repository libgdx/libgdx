################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
CPP_SRCS += \
../jni/Box2D/Dynamics/Contacts/b2CircleContact.cpp \
../jni/Box2D/Dynamics/Contacts/b2Contact.cpp \
../jni/Box2D/Dynamics/Contacts/b2ContactSolver.cpp \
../jni/Box2D/Dynamics/Contacts/b2PolygonAndCircleContact.cpp \
../jni/Box2D/Dynamics/Contacts/b2PolygonContact.cpp \
../jni/Box2D/Dynamics/Contacts/b2TOISolver.cpp 

OBJS += \
./jni/Box2D/Dynamics/Contacts/b2CircleContact.o \
./jni/Box2D/Dynamics/Contacts/b2Contact.o \
./jni/Box2D/Dynamics/Contacts/b2ContactSolver.o \
./jni/Box2D/Dynamics/Contacts/b2PolygonAndCircleContact.o \
./jni/Box2D/Dynamics/Contacts/b2PolygonContact.o \
./jni/Box2D/Dynamics/Contacts/b2TOISolver.o 

CPP_DEPS += \
./jni/Box2D/Dynamics/Contacts/b2CircleContact.d \
./jni/Box2D/Dynamics/Contacts/b2Contact.d \
./jni/Box2D/Dynamics/Contacts/b2ContactSolver.d \
./jni/Box2D/Dynamics/Contacts/b2PolygonAndCircleContact.d \
./jni/Box2D/Dynamics/Contacts/b2PolygonContact.d \
./jni/Box2D/Dynamics/Contacts/b2TOISolver.d 


# Each subdirectory must supply rules for building sources it contributes
jni/Box2D/Dynamics/Contacts/%.o: ../jni/Box2D/Dynamics/Contacts/%.cpp
	@echo 'Building file: $<'
	@echo 'Invoking: GCC C++ Compiler'
	g++ -fPIC -DFIXED_POINT -DCHECK_OVERFLOW -I"/usr/lib/jvm/java-6-sun-1.6.0.21/include" -I"/home/mzechner/workspace/trunk/gdx/jni" -I"/usr/lib/jvm/java-6-sun-1.6.0.21/include/linux" -O2 -Wall -c -I../jni/ -mfpmath=sse -msse2 -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@:%.o=%.d)" -o"$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '


