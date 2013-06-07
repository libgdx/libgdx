%module(directors="1") btMotionState

%feature("director") btMotionState;

%{
#include <LinearMath/btMotionState.h>
%}
%include "LinearMath/btMotionState.h"
