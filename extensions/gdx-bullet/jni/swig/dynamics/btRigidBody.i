%module btRigidBody

%{
#include <BulletDynamics/Dynamics/btRigidBody.h>
%}

%rename(i_motionState) btRigidBody::btRigidBodyConstructionInfo::m_motionState;
%javamethodmodifiers btRigidBody::btRigidBodyConstructionInfo::m_motionState "private";
%rename(i_collisionShape) btRigidBody::btRigidBodyConstructionInfo::m_collisionShape;
%javamethodmodifiers btRigidBody::btRigidBodyConstructionInfo::m_collisionShape "private";

%javamethodmodifiers btRigidBody::btRigidBodyConstructionInfo::btRigidBodyConstructionInfo "private";

%ignore btRigidBody::btRigidBodyConstructionInfo::btRigidBodyConstructionInfo(btScalar mass, btMotionState* motionState, btCollisionShape* collisionShape);
%ignore btRigidBody::btRigidBodyConstructionInfo::btRigidBodyConstructionInfo(btScalar mass, btMotionState* motionState, btCollisionShape* collisionShape, const btVector3& localInertia);

%typemap(javaout) 	btRigidBody *, const btRigidBody *, btRigidBody * const & {
	return btRigidBody.getInstance($jnicall, $owner);
}

%typemap(javaout) 	btRigidBody, const btRigidBody, btRigidBody & {
	return btRigidBody.getInstance($jnicall, $owner);
}

%typemap(javadirectorin) btRigidBody *, const btRigidBody *, btRigidBody * const &	"btRigidBody.getInstance($1, false)"

%typemap(javacode) btRigidBody::btRigidBodyConstructionInfo %{
	protected btMotionState motionState;
	
	public void setMotionState(btMotionState motionState) {
		refMotionState(motionState);
		setI_motionState(motionState);
	}
	
	protected void refMotionState(btMotionState motionState) {
		if (this.motionState == motionState)
			return;
		if (this.motionState != null)
			this.motionState.release();
		this.motionState = motionState;
		if (this.motionState != null)
			this.motionState.obtain();
	}
	
	public btMotionState getMotionState() {
		return motionState;
	}
	
	protected btCollisionShape collisionShape;
	
	public void setCollisionShape(btCollisionShape collisionShape) {
		refCollisionShape(collisionShape);
		setI_collisionShape(collisionShape);
	}
	
	protected void refCollisionShape(btCollisionShape shape) {
		if (collisionShape == shape)
			return;
		if (collisionShape != null)
			collisionShape.release();
		collisionShape = shape;
		if (collisionShape != null)
			collisionShape.obtain();
	}
	
	public btCollisionShape getCollisionShape() {
		return collisionShape;
	}
	
	public btRigidBodyConstructionInfo(float mass, btMotionState motionState, btCollisionShape collisionShape, Vector3 localInertia) {
		this(false, mass, motionState, collisionShape, localInertia);
		refMotionState(motionState);
		refCollisionShape(collisionShape);
	}
	
	public btRigidBodyConstructionInfo(float mass, btMotionState motionState, btCollisionShape collisionShape) {
		this(false, mass, motionState, collisionShape);
		refMotionState(motionState);
		refCollisionShape(collisionShape);
	}
	
	@Override
	public void dispose() {
		if (motionState != null)
			motionState.release();
		motionState = null;
		if (collisionShape != null)
			collisionShape.release();
		collisionShape = null;
		super.dispose();
	}
%}

%extend btRigidBody::btRigidBodyConstructionInfo {
	btRigidBodyConstructionInfo(bool dummy, btScalar mass, btMotionState* motionState, btCollisionShape* collisionShape, const btVector3& localInertia=btVector3(0,0,0)) {
		return new btRigidBody::btRigidBodyConstructionInfo(mass, motionState, collisionShape, localInertia); 
	}
};

%ignore btRigidBody::upcast(const btCollisionObject*);
%ignore btRigidBody::upcast(btCollisionObject*);
%ignore btRigidBody::btRigidBody(const btRigidBodyConstructionInfo& constructionInfo);
%ignore btRigidBody::btRigidBody(btScalar mass, btMotionState* motionState, btCollisionShape* collisionShape, const btVector3& localInertia=btVector3(0,0,0));

%javamethodmodifiers btRigidBody::btRigidBody "private";

%rename(internalGetMotionState) btRigidBody::getMotionState;
%javamethodmodifiers btRigidBody::getMotionState "private";
%rename(internalSetMotionState) btRigidBody::setMotionState;
%javamethodmodifiers btRigidBody::setMotionState "private";
%ignore btRigidBody::getCollisionShape;

%extend btRigidBody {
	btRigidBody(bool dummy, const btRigidBody::btRigidBodyConstructionInfo& constructionInfo) {
		return new btRigidBody(constructionInfo); 
	}
	btRigidBody(bool dummy, btScalar mass, btMotionState* motionState, btCollisionShape* collisionShape, const btVector3& localInertia=btVector3(0,0,0)) {
		return new btRigidBody(mass, motionState, collisionShape, localInertia);
	}
};

%typemap(javacode) btRigidBody %{
	protected btMotionState motionState;
	
	/** @return The existing instance for the specified pointer, or null if the instance doesn't exist */
	public static btRigidBody getInstance(final long swigCPtr) {
		return (btRigidBody)btCollisionObject.getInstance(swigCPtr);
	}
		
	/** @return The existing instance for the specified pointer, or a newly created instance if the instance didn't exist */
	public static btRigidBody getInstance(final long swigCPtr, boolean owner) {
		if (swigCPtr == 0)
			return null;
		btRigidBody result = getInstance(swigCPtr);
		if (result == null)
				result = new btRigidBody(swigCPtr, owner);
		return result;
	}
	
	public btRigidBody(btRigidBodyConstructionInfo constructionInfo) {
		this(false, constructionInfo);
		refCollisionShape(constructionInfo.getCollisionShape());
		refMotionState(constructionInfo.getMotionState());
	}
	
	public btRigidBody(float mass, btMotionState motionState, btCollisionShape collisionShape, Vector3 localInertia) {
		this(false, mass, motionState, collisionShape, localInertia);
		refCollisionShape(collisionShape);
		refMotionState(motionState);
	}
	
	public btRigidBody(float mass, btMotionState motionState, btCollisionShape collisionShape) {
		this(false, mass, motionState, collisionShape);
		refCollisionShape(collisionShape);
		refMotionState(motionState);
	}
  
	public void setMotionState(btMotionState motionState) {
		refMotionState(motionState);
		internalSetMotionState(motionState);
	}
	
	protected void refMotionState(btMotionState motionState) {
		if (this.motionState == motionState)
			return;
		if (this.motionState != null)
			this.motionState.release();
		this.motionState = motionState;
		if (this.motionState != null)
			this.motionState.obtain();
	}
	
	public btMotionState getMotionState() {
		return motionState;
	}
	
	@Override
	public void dispose() {
		if (motionState != null)
			motionState.release();
		motionState = null;
		super.dispose();
	}
%}

%include "BulletDynamics/Dynamics/btRigidBody.h"
