#include "ClosestNotMeConvexResultCallback.h"

ClosestNotMeConvexResultCallback::ClosestNotMeConvexResultCallback (btCollisionObject* me,const btVector3& fromA,const btVector3& toA) : 
	ClosestConvexResultCallback(fromA,toA),
	m_me(me),
	m_allowedPenetration(0.0f) {}

btScalar ClosestNotMeConvexResultCallback::addSingleResult(btCollisionWorld::LocalConvexResult& convexResult,bool normalInWorldSpace)
{
	if (convexResult.m_hitCollisionObject == m_me)
		return 1.0f;

	//ignore result if there is no contact response
	if(!convexResult.m_hitCollisionObject->hasContactResponse())
		return 1.0f;

	btVector3 linVelA,linVelB;
	linVelA = m_convexToWorld-m_convexFromWorld;
	linVelB = btVector3(0,0,0);//toB.getOrigin()-fromB.getOrigin();

	btVector3 relativeVelocity = (linVelA-linVelB);
	//don't report time of impact for motion away from the contact normal (or causes minor penetration)
	if (convexResult.m_hitNormalLocal.dot(relativeVelocity)>=-m_allowedPenetration)
		return 1.f;

	return ClosestConvexResultCallback::addSingleResult (convexResult, normalInWorldSpace);
}

bool ClosestNotMeConvexResultCallback::needsCollision(btBroadphaseProxy* proxy0)
{
	//don't collide with itself
	if (proxy0->m_clientObject == m_me)
		return false;

	///don't do CCD when the collision filters are not matching
	if (!ClosestConvexResultCallback::needsCollision(proxy0))
		return false;

	btCollisionObject* otherObj = (btCollisionObject*) proxy0->m_clientObject;

	return false;
}
