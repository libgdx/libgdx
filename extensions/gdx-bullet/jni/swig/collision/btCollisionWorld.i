%module(directors="1") btCollisionWorld

%feature("flatnested") btCollisionWorld::LocalShapeInfo;
%feature("director") LocalShapeInfo;
%feature("flatnested") btCollisionWorld::LocalRayResult;
%feature("director") LocalRayResult;
%feature("flatnested") btCollisionWorld::RayResultCallback;
%feature("director") RayResultCallback;
%feature("flatnested") btCollisionWorld::ClosestRayResultCallback;
%feature("director") ClosestRayResultCallback;
%feature("flatnested") btCollisionWorld::AllHitsRayResultCallback;
%feature("director") AllHitsRayResultCallback;
%feature("flatnested") btCollisionWorld::LocalConvexResult;
%feature("director") LocalConvexResult;
%feature("flatnested") btCollisionWorld::ConvexResultCallback;
%feature("director") ConvexResultCallback;
%feature("flatnested") btCollisionWorld::ClosestConvexResultCallback;
%feature("director") ClosestConvexResultCallback;
%feature("flatnested") btCollisionWorld::ContactResultCallback;
%feature("director") ContactResultCallback;

%{
#include <BulletCollision/CollisionDispatch/btCollisionWorld.h>
%}
%include "BulletCollision/CollisionDispatch/btCollisionWorld.h"
