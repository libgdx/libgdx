%module(directors="1") Bullet3Common

%include "arrays_java.i"

%include "../common/gdxCommon.i"

%{
#include <Bullet3Common/b3Logging.h>
%}
%include "Bullet3Common/b3Logging.h"
