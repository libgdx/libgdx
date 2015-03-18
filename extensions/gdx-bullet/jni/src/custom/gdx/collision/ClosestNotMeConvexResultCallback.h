#ifndef ClosestNotMeConvexResultCallback_H
#define ClosestNotMeConvexResultCallback_H

#include "../../../bullet/LinearMath/btVector3.h"
#include "../../../bullet/BulletCollision/CollisionDispatch/btCollisionObject.h"
#include "../../../bullet/BulletCollision/CollisionDispatch/btCollisionWorld.h"

class ClosestNotMeConvexResultCallback : public btCollisionWorld::ClosestConvexResultCallback
{
public:

	btCollisionObject* m_me;
	btScalar m_allowedPenetration;

public:
	ClosestNotMeConvexResultCallback (btCollisionObject* me,const btVector3& fromA,const btVector3& toA);
	virtual btScalar addSingleResult(btCollisionWorld::LocalConvexResult& convexResult,bool normalInWorldSpace);
	virtual bool needsCollision(btBroadphaseProxy* proxy0);
};

#endif // ClosestNotMeConvexResultCallback_H
