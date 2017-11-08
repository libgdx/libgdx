%module btAxisSweep3

%{
#include <BulletCollision/BroadphaseCollision/btAxisSweep3Internal.h>
%}
%include "BulletCollision/BroadphaseCollision/btAxisSweep3Internal.h"

%ignore btAxisSweep3Internal<unsigned short>::processAllOverlappingPairs;
%ignore btAxisSweep3Internal<unsigned int>::processAllOverlappingPairs;

%template(btAxisSweep3InternalShort) btAxisSweep3Internal<unsigned short int>;
%template(btAxisSweep3InternalInt) btAxisSweep3Internal<unsigned int>;

%{
#include <BulletCollision/BroadphaseCollision/btAxisSweep3.h>
%}
%include "BulletCollision/BroadphaseCollision/btAxisSweep3.h"
