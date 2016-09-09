// NOTE: In BulletDynamics/Vehicle/btRaycastVehicle.h on line 22 the global class btVehicleTuning must be removed,
// It is never used by bullet (btVehicleTuning is a subclass of btRaycastVehicle),
// but it conflicts with swig because swig can't handle nested structs.

%module btRaycastVehicle

%{
#include <BulletDynamics/Vehicle/btRaycastVehicle.h>
%}
%include "BulletDynamics/Vehicle/btRaycastVehicle.h"
