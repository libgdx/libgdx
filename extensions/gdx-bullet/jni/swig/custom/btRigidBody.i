/*
 *	Interface module for a class with inner structs or classes.
 */
 
%module btRigidBody

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

	btRigidBodyConstructionInfo(	btScalar mass, btMotionState* motionState, btCollisionShape* collisionShape, const btVector3& localInertia=btVector3(0,0,0)):
	m_mass(mass),
		m_motionState(motionState),
		m_collisionShape(collisionShape),
		m_localInertia(localInertia),
		m_linearDamping(btScalar(0.)),
		m_angularDamping(btScalar(0.)),
		m_friction(btScalar(0.5)),
		m_restitution(btScalar(0.)),
		m_linearSleepingThreshold(btScalar(0.8)),
		m_angularSleepingThreshold(btScalar(1.f)),
		m_additionalDamping(false),
		m_additionalDampingFactor(btScalar(0.005)),
		m_additionalLinearDampingThresholdSqr(btScalar(0.01)),
		m_additionalAngularDampingThresholdSqr(btScalar(0.01)),
		m_additionalAngularDampingFactor(btScalar(0.01))
	{
		m_startWorldTransform.setIdentity();
	}
};

%nestedworkaround btRigidBody::btRigidBodyConstructionInfo;

%{
#include <BulletDynamics/Dynamics/btRigidBody.h>
%}
%include "BulletDynamics/Dynamics/btRigidBody.h"

%{
typedef btRigidBody::btRigidBodyConstructionInfo btRigidBodyConstructionInfo;
%}
