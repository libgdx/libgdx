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

%ignore btCollisionWorld::ClosestRayResultCallback::m_rayFromWorld;
%ignore btCollisionWorld::ClosestRayResultCallback::m_rayToWorld;
%ignore btCollisionWorld::ClosestRayResultCallback::m_hitNormalWorld;
%ignore btCollisionWorld::ClosestRayResultCallback::m_hitPointWorld;

%extend btCollisionWorld::ClosestRayResultCallback {
	void getRayFromWorld(btVector3 &out) {
		out = $self->m_rayFromWorld;
	}
	void setRayFromWorld(btVector3 const &value) {
		$self->m_rayFromWorld = value;
	}
	
	void getRayToWorld(btVector3 &out) {
		out = $self->m_rayToWorld;
	}
	void setRayToWorld(btVector3 const &value) {
		$self->m_rayToWorld = value;
	}
	
	void getHitNormalWorld(btVector3 &out) {
		out = $self->m_hitNormalWorld;
	}
	void setHitNormalWorld(btVector3 const &value) {
		$self->m_hitNormalWorld = value;
	}
	
	void getHitPointWorld(btVector3 &out) {
		out = $self->m_hitPointWorld;
	}
	void setHitPointWorld(btVector3 const &value) {
		$self->m_hitPointWorld = value;
	}
};

%ignore btCollisionWorld::AllHitsRayResultCallback::m_rayFromWorld;
%ignore btCollisionWorld::AllHitsRayResultCallback::m_rayToWorld;

%extend btCollisionWorld::AllHitsRayResultCallback {
	void getRayFromWorld(btVector3 &out) {
		out = $self->m_rayFromWorld;
	}
	void setRayFromWorld(btVector3 const &value) {
		$self->m_rayFromWorld = value;
	}
	
	void getRayToWorld(btVector3 &out) {
		out = $self->m_rayToWorld;
	}
	void setRayToWorld(btVector3 const &value) {
		$self->m_rayToWorld = value;
	}
};

%ignore btCollisionWorld::LocalConvexResult::m_hitNormalLocal;
%ignore btCollisionWorld::LocalConvexResult::m_hitPointLocal;

%extend btCollisionWorld::LocalConvexResult {
	void getHitNormalLocal(btVector3 &out) {
		out = $self->m_hitNormalLocal;
	}
	void setHitNormalLocal(btVector3 const &value) {
		$self->m_hitNormalLocal = value;
	}
	
	void getHitPointLocal(btVector3 &out) {
		out = $self->m_hitPointLocal;
	}
	void setHitPointLocal(btVector3 const &value) {
		$self->m_hitPointLocal = value;
	}
};

%ignore btCollisionWorld::ClosestConvexResultCallback::m_rayFromWorld;
%ignore btCollisionWorld::ClosestConvexResultCallback::m_rayToWorld;
%ignore btCollisionWorld::ClosestConvexResultCallback::m_hitNormalWorld;
%ignore btCollisionWorld::ClosestConvexResultCallback::m_hitPointWorld;

%extend btCollisionWorld::ClosestConvexResultCallback {
	void getConvexFromWorld(btVector3 &out) {
		out = $self->m_convexFromWorld;
	}
	void setRayFromWorld(btVector3 const &value) {
		$self->m_convexFromWorld = value;
	}
	
	void getConvexToWorld(btVector3 &out) {
		out = $self->m_convexToWorld;
	}
	void setConvexToWorld(btVector3 const &value) {
		$self->m_convexToWorld = value;
	}
	
	void getHitNormalWorld(btVector3 &out) {
		out = $self->m_hitNormalWorld;
	}
	void setHitNormalWorld(btVector3 const &value) {
		$self->m_hitNormalWorld = value;
	}
	
	void getHitPointWorld(btVector3 &out) {
		out = $self->m_hitPointWorld;
	}
	void setHitPointWorld(btVector3 const &value) {
		$self->m_hitPointWorld = value;
	}
};

%{
#include <BulletCollision/CollisionDispatch/btCollisionWorld.h>
%}
%include "BulletCollision/CollisionDispatch/btCollisionWorld.h"

	