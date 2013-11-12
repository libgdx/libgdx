#include "ClosestNotMeRayResultCallback.h"

ClosestNotMeRayResultCallback::ClosestNotMeRayResultCallback (btCollisionObject* me) 
	: ClosestRayResultCallback(btVector3(0.0, 0.0, 0.0), btVector3(0.0, 0.0, 0.0))
{
	m_me = me;
}

btScalar ClosestNotMeRayResultCallback::addSingleResult(btCollisionWorld::LocalRayResult& rayResult,bool normalInWorldSpace)
{
	if (rayResult.m_collisionObject == m_me)
		return 1.0;

	return ClosestNotMeRayResultCallback::ClosestRayResultCallback::addSingleResult (rayResult, normalInWorldSpace);
}
