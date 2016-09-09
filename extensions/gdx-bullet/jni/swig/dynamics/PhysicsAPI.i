%module PhysicsAPI

%feature("director") InternalTickCallback;

%{
#include <Bullet-C-Api.h>
%}
%include "Bullet-C-Api.h"
