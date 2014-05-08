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
