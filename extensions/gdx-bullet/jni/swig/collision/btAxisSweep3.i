%module btAxisSweep3

%{
#include <BulletCollision/BroadphaseCollision/btAxisSweep3.h>
%}

%ignore btAxisSweep3Internal<unsigned short>::processAllOverlappingPairs;
%ignore btAxisSweep3Internal<unsigned int>::processAllOverlappingPairs;

%ignore btAxisSweep3;
%ignore bt32BitAxisSweep3;

%include "BulletCollision/BroadphaseCollision/btAxisSweep3.h"

%rename(btAxisSweep3) btAxisSweep3;
%rename(bt32BitAxisSweep3) bt32BitAxisSweep3;

%template(btAxisSweep3InternalShort) btAxisSweep3Internal<unsigned short int>;
%template(btAxisSweep3InternalInt) btAxisSweep3Internal<unsigned int>;

class btAxisSweep3 : public btAxisSweep3Internal<unsigned short int>
{
public:
	btAxisSweep3(const btVector3& worldAabbMin,const btVector3& worldAabbMax, unsigned short int maxHandles = 16384, btOverlappingPairCache* pairCache = 0, bool disableRaycastAccelerator = false);
};

class bt32BitAxisSweep3 : public btAxisSweep3Internal<unsigned int>
{
public:
	bt32BitAxisSweep3(const btVector3& worldAabbMin,const btVector3& worldAabbMax, unsigned int maxHandles = 1500000, btOverlappingPairCache* pairCache = 0, bool disableRaycastAccelerator = false);
};
