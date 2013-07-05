/*
 *	Interface module for a class with inner structs or classes.
 */
 
%module btRigidBody


%{
#include <BulletDynamics/Dynamics/btRigidBody.h>
typedef btRigidBody::btRigidBodyConstructionInfo btRigidBodyConstructionInfo;
%}


%rename(mass) btRigidBodyConstructionInfo::m_mass;
%rename(startWorldTransform) btRigidBodyConstructionInfo::m_startWorldTransform;
%rename(localInertia) btRigidBodyConstructionInfo::m_localInertia;
%rename(linearDamping) btRigidBodyConstructionInfo::m_linearDamping;
%rename(angularDamping) btRigidBodyConstructionInfo::m_angularDamping;
%rename(friction) btRigidBodyConstructionInfo::m_friction;
%rename(restitution) btRigidBodyConstructionInfo::m_restitution;
%rename(linearSleepingThreshold) btRigidBodyConstructionInfo::m_linearSleepingThreshold;
%rename(angularSleepingThreshold) btRigidBodyConstructionInfo::m_angularSleepingThreshold;
%rename(additionalDamping) btRigidBodyConstructionInfo::m_additionalDamping;
%rename(additionalDampingFactor) btRigidBodyConstructionInfo::m_additionalDampingFactor;
%rename(additionalLinearDampingThresholdSqr) btRigidBodyConstructionInfo::m_additionalLinearDampingThresholdSqr;
%rename(additionalAngularDampingThresholdSqr) btRigidBodyConstructionInfo::m_additionalAngularDampingThresholdSqr;
%rename(additionalAngularDampingFactor) btRigidBodyConstructionInfo::m_additionalAngularDampingFactor;

%javamethodmodifiers btRigidBodyConstructionInfo::m_motionState "private";
%javamethodmodifiers btRigidBodyConstructionInfo::m_collisionShape "private";

%javamethodmodifiers btRigidBodyConstructionInfo::btRigidBodyConstructionInfo "private";

%typemap(javacode) btRigidBodyConstructionInfo %{
	protected btMotionState motionState;
	protected btCollisionShape collisionShape;
	
	public void setMotionState(btMotionState motionState) {
		this.motionState = motionState;
		setM_motionState(motionState);
	}
	
	public btMotionState getMotionState() {
		return motionState != null ? motionState : (motionState = getM_motionState());
	}
	
	public void setCollisionShape(btCollisionShape collisionShape) {
		this.collisionShape = collisionShape;
		setM_collisionShape(collisionShape);
	}
	
	public btCollisionShape getCollisionShape() {
		return collisionShape != null ? collisionShape : (collisionShape = getM_collisionShape());
	}
	
	public btRigidBodyConstructionInfo(float mass, btMotionState motionState, btCollisionShape collisionShape, Vector3 localInertia) {
		this(false, mass, motionState, collisionShape, localInertia);
		this.motionState = motionState;
		this.collisionShape = collisionShape;
	}
	
	public btRigidBodyConstructionInfo(float mass, btMotionState motionState, btCollisionShape collisionShape) {
		this(false, mass, motionState, collisionShape);
		this.motionState = motionState;
		this.collisionShape = collisionShape;
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
    this.collisionShape = constructionInfo.getCollisionShape();
    this.motionState = constructionInfo.getMotionState();
  }

  public btRigidBody(float mass, btMotionState motionState, btCollisionShape collisionShape, Vector3 localInertia) {
    this(false, mass, motionState, collisionShape, localInertia);
    this.collisionShape = collisionShape;
    this.motionState = motionState;
  }

  public btRigidBody(float mass, btMotionState motionState, btCollisionShape collisionShape) {
	  this(false, mass, motionState, collisionShape);
	    this.collisionShape = collisionShape;
	    this.motionState = motionState;
  }
  
  public btMotionState getMotionState() {
	  return motionState != null ? motionState : (motionState = internalGetMotionState());
  }
  
  public void setMotionState(btMotionState motionState) {
	  this.motionState = motionState;
	  internalSetMotionState(motionState);
  }
%}

%include "BulletDynamics/Dynamics/btRigidBody.h"
