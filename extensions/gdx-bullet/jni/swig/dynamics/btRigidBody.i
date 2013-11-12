%module btRigidBody

%{
#include <BulletDynamics/Dynamics/btRigidBody.h>
typedef btRigidBody::btRigidBodyConstructionInfo btRigidBodyConstructionInfo;
%}

%rename(i_motionState) btRigidBodyConstructionInfo::m_motionState;
%javamethodmodifiers btRigidBodyConstructionInfo::m_motionState "private";
%rename(i_collisionShape) btRigidBodyConstructionInfo::m_collisionShape;
%javamethodmodifiers btRigidBodyConstructionInfo::m_collisionShape "private";

%javamethodmodifiers btRigidBodyConstructionInfo::btRigidBodyConstructionInfo "private";

%typemap(javacode) btRigidBodyConstructionInfo %{
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

// Nested struct or class copied from Bullet header
struct btRigidBodyConstructionInfo
{
	btScalar			m_mass;

	///When a motionState is provided, the rigid body will initialize its world transform from the motion state
	///In this case, m_startWorldTransform is ignored.
	btMotionState*		m_motionState;
	btTransform	m_startWorldTransform;

	btCollisionShape*	m_collisionShape;
	btVector3			m_localInertia;
	btScalar			m_linearDamping;
	btScalar			m_angularDamping;

	///best simulation results when friction is non-zero
	btScalar			m_friction;
	///best simulation results using zero restitution.
	btScalar			m_restitution;

	btScalar			m_linearSleepingThreshold;
	btScalar			m_angularSleepingThreshold;

	//Additional damping can help avoiding lowpass jitter motion, help stability for ragdolls etc.
	//Such damping is undesirable, so once the overall simulation quality of the rigid body dynamics system has improved, this should become obsolete
	bool				m_additionalDamping;
	btScalar			m_additionalDampingFactor;
	btScalar			m_additionalLinearDampingThresholdSqr;
	btScalar			m_additionalAngularDampingThresholdSqr;
	btScalar			m_additionalAngularDampingFactor;
private:
	btRigidBodyConstructionInfo();
};

%extend btRigidBodyConstructionInfo {
	btRigidBodyConstructionInfo(bool dummy, btScalar mass, btMotionState* motionState, btCollisionShape* collisionShape, const btVector3& localInertia=btVector3(0,0,0)) {
		return new btRigidBodyConstructionInfo(mass, motionState, collisionShape, localInertia); 
	}
};

%nestedworkaround btRigidBody::btRigidBodyConstructionInfo;

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
	btRigidBody(bool dummy, const btRigidBodyConstructionInfo& constructionInfo) {
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
