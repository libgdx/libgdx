################################################################################
# Automatically-generated file. Do not edit!
################################################################################

-include ../makefile.init

RM := rm -rf

# All of the sources participating in the build are defined here
-include sources.mk
-include subdir.mk
-include jni/vorbis/subdir.mk
-include jni/kissfft/subdir.mk
-include jni/subdir.mk
-include jni/Box2D/subdir.mk
-include jni/Box2D/Dynamics/subdir.mk
-include jni/Box2D/Dynamics/Joints/subdir.mk
-include jni/Box2D/Dynamics/Contacts/subdir.mk
-include jni/Box2D/Common/subdir.mk
-include jni/Box2D/Collision/subdir.mk
-include jni/Box2D/Collision/Shapes/subdir.mk
-include objects.mk

ifneq ($(MAKECMDGOALS),clean)
ifneq ($(strip $(C++_DEPS)),)
-include $(C++_DEPS)
endif
ifneq ($(strip $(C_DEPS)),)
-include $(C_DEPS)
endif
ifneq ($(strip $(CC_DEPS)),)
-include $(CC_DEPS)
endif
ifneq ($(strip $(CPP_DEPS)),)
-include $(CPP_DEPS)
endif
ifneq ($(strip $(CXX_DEPS)),)
-include $(CXX_DEPS)
endif
ifneq ($(strip $(C_UPPER_DEPS)),)
-include $(C_UPPER_DEPS)
endif
endif

-include ../makefile.defs

# Add inputs and outputs from these tool invocations to the build variables 

# All Target
all: libgdx-64.so

# Tool invocations
libgdx-64.so: $(OBJS) $(USER_OBJS)
	@echo 'Building target: $@'
	@echo 'Invoking: GCC C++ Linker'
	g++ -shared -o"libgdx-64.so" $(OBJS) $(USER_OBJS) $(LIBS)
	@echo 'Finished building target: $@'
	@echo ' '

# Other Targets
clean:
	-$(RM) $(OBJS)$(C++_DEPS)$(C_DEPS)$(CC_DEPS)$(LIBRARIES)$(CPP_DEPS)$(CXX_DEPS)$(C_UPPER_DEPS) libgdx-64.so
	-@echo ' '

.PHONY: all clean dependents
.SECONDARY:

-include ../makefile.targets
