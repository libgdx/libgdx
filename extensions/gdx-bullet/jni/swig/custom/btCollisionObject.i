/*
 *	Interface module for a class with inner structs or classes.
 */
 
%module btCollisionObject

%typemap(javaout) 	btCollisionObject *, const btCollisionObject * {
	long cPtr = $jnicall;
	return (cPtr == 0) ? null : btCollisionObject.getInstance(cPtr, $owner);
}

%typemap(javabody) btCollisionObject %{
	public final static com.badlogic.gdx.utils.LongMap<btCollisionObject> instances = new com.badlogic.gdx.utils.LongMap<btCollisionObject>();
	
	public static btCollisionObject getInstance(final long swigCPtr, boolean owner) {
		btCollisionObject result = instances.get(swigCPtr);
		if (result == null)
			result = new btCollisionObject(swigCPtr, owner);
		return result;
	}
	
	private long swigCPtr;
	protected boolean swigCMemOwn;
	
	protected btCollisionObject(long cPtr, boolean cMemoryOwn) {
		swigCMemOwn = cMemoryOwn;
		swigCPtr = cPtr;
		instances.put(cPtr, this);
	}
	
	public void takeOwnership() {
		swigCMemOwn = true;
	}
	
	public void releaseOwnership() {
		swigCMemOwn = false;
	}
	
	public static long getCPtr($javaclassname obj) {
		return (obj == null) ? 0 : obj.swigCPtr;
	}
%}

%{
#include <BulletCollision/CollisionDispatch/btCollisionObject.h>
%}
%include "BulletCollision/CollisionDispatch/btCollisionObject.h"

%extend btCollisionObject {

	void getAnisotropicFriction(btVector3 & out) {
		out = $self->getAnisotropicFriction();
	}

    void getWorldTransform(btTransform & out) {
		out = $self->getWorldTransform();
	}
	
    void getInterpolationWorldTransform(btTransform & out) {
		out = $self->getInterpolationWorldTransform();
	}
	
	void getInterpolationLinearVelocity(btVector3 & out) {
		out = $self->getInterpolationLinearVelocity();
	}
		
	void getInterpolationAngularVelocity(btVector3 & out) {
		out = $self->getInterpolationAngularVelocity();
	}
};