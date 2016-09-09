%module btManifoldPoint

%typemap(javadirectorin) btManifoldPoint, const btManifoldPoint, const btManifoldPoint &, btManifoldPoint & 	"btManifoldPoint.obtainForArgument($1, false)"
%typemap(javadirectorin) btManifoldPoint *, const btManifoldPoint *, btManifoldPoint * const &		"btManifoldPoint.obtainForArgument($1, false)"

%typemap(javaout) 	btManifoldPoint *, const btManifoldPoint *, btManifoldPoint * const & {
	return btManifoldPoint.obtainTemp($jnicall, $owner);
}

%typemap(javacode) btManifoldPoint %{
	private final static btManifoldPoint temp = new btManifoldPoint(0, false);
	/** Obtains a temporary instance, used by native methods that return a btManifoldPoint instance */
	protected static btManifoldPoint obtainTemp(long cPtr, boolean own) {
		temp.reset(cPtr, own);
		return temp;
	}
	
	private static btManifoldPoint[] argumentInstances = new btManifoldPoint[] {new btManifoldPoint(0, false),
		new btManifoldPoint(0, false), new btManifoldPoint(0, false), new btManifoldPoint(0, false)};
	private static int argumentIndex = -1;
	/** Obtains a temporary instance, used for callback methods with one or more btManifoldPoint arguments */
	protected static btManifoldPoint obtainForArgument(final long swigCPtr, boolean owner) {
		btManifoldPoint instance = argumentInstances[argumentIndex = (argumentIndex + 1) & 3];
		instance.reset(swigCPtr, owner);
		return instance;
	}
%}

%ignore btManifoldPoint::m_localPointA;
%ignore btManifoldPoint::m_localPointB;
%ignore btManifoldPoint::m_positionWorldOnA;
%ignore btManifoldPoint::m_positionWorldOnB;
%ignore btManifoldPoint::m_normalWorldOnB;
%ignore btManifoldPoint::m_lateralFrictionDir1;
%ignore btManifoldPoint::m_lateralFrictionDir2;
%ignore btManifoldPoint::getPositionWorldOnA() const;
%ignore btManifoldPoint::getPositionWorldOnB() const;
//%rename(internalGetPositionWorldOnA) btManifoldPoint::getPositionWorldOnA() const;
//%rename(internalGetPositionWorldOnB) btManifoldPoint::getPositionWorldOnB() const;

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
	
	void getLocalPointA(btVector3 &out) {
		out = $self->m_localPointA;
	}
	
	void setLocalPointA(const btVector3 &value) {
		$self->m_localPointA = value;
	}
	
	void getLocalPointB(btVector3 &out) {
		out = $self->m_localPointB;
	}
	
	void setLocalPointB(const btVector3 &value) {
		$self->m_localPointB = value;
	}
	
	void getPositionWorldOnA(btVector3 &out) {
		out = $self->m_positionWorldOnA;
	}
	
	void setPositionWorldOnA(const btVector3 &value) {
		$self->m_positionWorldOnA = value;
	}
	
	void getPositionWorldOnB(btVector3 &out) {
		out = $self->m_positionWorldOnB;
	}
	
	void setPositionWorldOnB(const btVector3 &value) {
		$self->m_positionWorldOnB = value;
	}
	
	void getNormalWorldOnB(btVector3 &out) {
		out = $self->m_normalWorldOnB;
	}
	
	void setNormalWorldOnB(const btVector3 &value) {
		$self->m_normalWorldOnB = value;
	}
	
	void getLateralFrictionDir1(btVector3 &out) {
		out = $self->m_lateralFrictionDir1;
	}
	
	void setLateralFrictionDir1(const btVector3 &value) {
		$self->m_lateralFrictionDir1 = value;
	}
	
	void getLateralFrictionDir2(btVector3 &out) {
		out = $self->m_lateralFrictionDir2;
	}
	
	void setLateralFrictionDir2(const btVector3 &value) {
		$self->m_lateralFrictionDir2 = value;
	}
};