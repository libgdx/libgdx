/*
 *	Interface module for a class with inner structs or classes.
 */
 
%module btCollisionObject

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