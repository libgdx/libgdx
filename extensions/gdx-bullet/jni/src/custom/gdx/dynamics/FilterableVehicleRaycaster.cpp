#include "FilterableVehicleRaycaster.h"

void FilterableVehicleRaycaster::setCollisionFilterMask(short collisionFilterMask) {
	m_collisionFilterMask = collisionFilterMask;
}

void FilterableVehicleRaycaster::setCollisionFilterGroup(short collisionFilterGroup) {
	m_collisionFilterGroup = collisionFilterGroup;
}

void* FilterableVehicleRaycaster::castRay(const btVector3& from, const btVector3& to, btVehicleRaycasterResult& result) {
	btCollisionWorld::ClosestRayResultCallback rayCallback(from,to);
	rayCallback.m_collisionFilterMask = m_collisionFilterMask;
	rayCallback.m_collisionFilterGroup = m_collisionFilterGroup;
	m_testWorld->rayTest(from, to, rayCallback);
	if (rayCallback.hasHit()) {
		const btRigidBody* body = btRigidBody::upcast(rayCallback.m_collisionObject);
		if (body && body->hasContactResponse()) {
			result.m_hitPointInWorld = rayCallback.m_hitPointWorld;
			result.m_hitNormalInWorld = rayCallback.m_hitNormalWorld;
			result.m_hitNormalInWorld.normalize();
			result.m_distFraction = rayCallback.m_closestHitFraction;
			return (void*)body;
		}
	}
	return 0;
}
