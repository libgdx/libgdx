%module btManifoldPoint

CREATE_POOLED_OBJECT(btManifoldPoint, com/badlogic/gdx/physics/bullet/btManifoldPoint);

%{
#include <BulletCollision/NarrowPhaseCollision/btManifoldPoint.h>
%}
%include "BulletCollision/NarrowPhaseCollision/btManifoldPoint.h"

%extend btManifoldPoint {
	int getUserValue() {
		int result;
		*(const void **)&result = $self->m_userPersistentData;
		return result;
	}
	
	void setUserValue(int value) {
		$self->m_userPersistentData = (void*)value;
	}
};