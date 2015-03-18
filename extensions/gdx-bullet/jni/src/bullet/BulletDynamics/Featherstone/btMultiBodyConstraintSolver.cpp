/*
Bullet Continuous Collision Detection and Physics Library
Copyright (c) 2013 Erwin Coumans  http://bulletphysics.org

This software is provided 'as-is', without any express or implied warranty.
In no event will the authors be held liable for any damages arising from the use of this software.
Permission is granted to anyone to use this software for any purpose, 
including commercial applications, and to alter it and redistribute it freely, 
subject to the following restrictions:

1. The origin of this software must not be misrepresented; you must not claim that you wrote the original software. If you use this software in a product, an acknowledgment in the product documentation would be appreciated but is not required.
2. Altered source versions must be plainly marked as such, and must not be misrepresented as being the original software.
3. This notice may not be removed or altered from any source distribution.
*/

#include "btMultiBodyConstraintSolver.h"
#include "BulletCollision/NarrowPhaseCollision/btPersistentManifold.h"
#include "btMultiBodyLinkCollider.h"

#include "BulletDynamics/ConstraintSolver/btSolverBody.h"
#include "btMultiBodyConstraint.h"
#include "BulletDynamics/ConstraintSolver/btContactSolverInfo.h"

#include "LinearMath/btQuickprof.h"

btScalar btMultiBodyConstraintSolver::solveSingleIteration(int iteration, btCollisionObject** bodies ,int numBodies,btPersistentManifold** manifoldPtr, int numManifolds,btTypedConstraint** constraints,int numConstraints,const btContactSolverInfo& infoGlobal,btIDebugDraw* debugDrawer)
{
	btScalar val = btSequentialImpulseConstraintSolver::solveSingleIteration(iteration, bodies ,numBodies,manifoldPtr, numManifolds,constraints,numConstraints,infoGlobal,debugDrawer);
	
	//solve featherstone non-contact constraints

	//printf("m_multiBodyNonContactConstraints = %d\n",m_multiBodyNonContactConstraints.size());
	for (int j=0;j<m_multiBodyNonContactConstraints.size();j++)
	{
		btMultiBodySolverConstraint& constraint = m_multiBodyNonContactConstraints[j];
		//if (iteration < constraint.m_overrideNumSolverIterations)
			//resolveSingleConstraintRowGenericMultiBody(constraint);
		resolveSingleConstraintRowGeneric(constraint);
	}

	//solve featherstone normal contact
	for (int j=0;j<m_multiBodyNormalContactConstraints.size();j++)
	{
		btMultiBodySolverConstraint& constraint = m_multiBodyNormalContactConstraints[j];
		if (iteration < infoGlobal.m_numIterations)
			resolveSingleConstraintRowGeneric(constraint);
	}
	
	//solve featherstone frictional contact

	for (int j=0;j<this->m_multiBodyFrictionContactConstraints.size();j++)
	{
		if (iteration < infoGlobal.m_numIterations)
		{
			btMultiBodySolverConstraint& frictionConstraint = m_multiBodyFrictionContactConstraints[j];
			btScalar totalImpulse = m_multiBodyNormalContactConstraints[frictionConstraint.m_frictionIndex].m_appliedImpulse;
			//adjust friction limits here
			if (totalImpulse>btScalar(0))
			{
				frictionConstraint.m_lowerLimit = -(frictionConstraint.m_friction*totalImpulse);
				frictionConstraint.m_upperLimit = frictionConstraint.m_friction*totalImpulse;
				resolveSingleConstraintRowGeneric(frictionConstraint);
			}
		}
	}
	return val;
}

btScalar btMultiBodyConstraintSolver::solveGroupCacheFriendlySetup(btCollisionObject** bodies,int numBodies,btPersistentManifold** manifoldPtr, int numManifolds,btTypedConstraint** constraints,int numConstraints,const btContactSolverInfo& infoGlobal,btIDebugDraw* debugDrawer)
{
	m_multiBodyNonContactConstraints.resize(0);
	m_multiBodyNormalContactConstraints.resize(0);
	m_multiBodyFrictionContactConstraints.resize(0);
	m_data.m_jacobians.resize(0);
	m_data.m_deltaVelocitiesUnitImpulse.resize(0);
	m_data.m_deltaVelocities.resize(0);

	for (int i=0;i<numBodies;i++)
	{
		const btMultiBodyLinkCollider* fcA = btMultiBodyLinkCollider::upcast(bodies[i]);
		if (fcA)
		{
			fcA->m_multiBody->setCompanionId(-1);
		}
	}

	btScalar val = btSequentialImpulseConstraintSolver::solveGroupCacheFriendlySetup( bodies,numBodies,manifoldPtr, numManifolds, constraints,numConstraints,infoGlobal,debugDrawer);

	return val;
}

void	btMultiBodyConstraintSolver::applyDeltaVee(btScalar* delta_vee, btScalar impulse, int velocityIndex, int ndof)
{
    for (int i = 0; i < ndof; ++i) 
		m_data.m_deltaVelocities[velocityIndex+i] += delta_vee[i] * impulse;
}

void btMultiBodyConstraintSolver::resolveSingleConstraintRowGeneric(const btMultiBodySolverConstraint& c)
{

	btScalar deltaImpulse = c.m_rhs-btScalar(c.m_appliedImpulse)*c.m_cfm;
	btScalar deltaVelADotn=0;
	btScalar deltaVelBDotn=0;
	btSolverBody* bodyA = 0;
	btSolverBody* bodyB = 0;
	int ndofA=0;
	int ndofB=0;

	if (c.m_multiBodyA)
	{
		ndofA  = c.m_multiBodyA->getNumLinks() + 6;
		for (int i = 0; i < ndofA; ++i) 
			deltaVelADotn += m_data.m_jacobians[c.m_jacAindex+i] * m_data.m_deltaVelocities[c.m_deltaVelAindex+i];
	} else
	{
		bodyA = &m_tmpSolverBodyPool[c.m_solverBodyIdA];
		deltaVelADotn += c.m_contactNormal1.dot(bodyA->internalGetDeltaLinearVelocity()) 	+ c.m_relpos1CrossNormal.dot(bodyA->internalGetDeltaAngularVelocity());
	}

	if (c.m_multiBodyB)
	{
		ndofB = c.m_multiBodyB->getNumLinks() + 6;
		for (int i = 0; i < ndofB; ++i) 
			deltaVelBDotn += m_data.m_jacobians[c.m_jacBindex+i] * m_data.m_deltaVelocities[c.m_deltaVelBindex+i];
	} else
	{
		bodyB = &m_tmpSolverBodyPool[c.m_solverBodyIdB];
		deltaVelBDotn += c.m_contactNormal2.dot(bodyB->internalGetDeltaLinearVelocity())  + c.m_relpos2CrossNormal.dot(bodyB->internalGetDeltaAngularVelocity());
	}

	
	deltaImpulse	-=	deltaVelADotn*c.m_jacDiagABInv;//m_jacDiagABInv = 1./denom
	deltaImpulse	-=	deltaVelBDotn*c.m_jacDiagABInv;
	const btScalar sum = btScalar(c.m_appliedImpulse) + deltaImpulse;
	
	if (sum < c.m_lowerLimit)
	{
		deltaImpulse = c.m_lowerLimit-c.m_appliedImpulse;
		c.m_appliedImpulse = c.m_lowerLimit;
	}
	else if (sum > c.m_upperLimit) 
	{
		deltaImpulse = c.m_upperLimit-c.m_appliedImpulse;
		c.m_appliedImpulse = c.m_upperLimit;
	}
	else
	{
		c.m_appliedImpulse = sum;
	}

	if (c.m_multiBodyA)
	{
		applyDeltaVee(&m_data.m_deltaVelocitiesUnitImpulse[c.m_jacAindex],deltaImpulse,c.m_deltaVelAindex,ndofA);
		c.m_multiBodyA->applyDeltaVee(&m_data.m_deltaVelocitiesUnitImpulse[c.m_jacAindex],deltaImpulse);
	} else
	{
		bodyA->internalApplyImpulse(c.m_contactNormal1*bodyA->internalGetInvMass(),c.m_angularComponentA,deltaImpulse);

	}
	if (c.m_multiBodyB)
	{
		applyDeltaVee(&m_data.m_deltaVelocitiesUnitImpulse[c.m_jacBindex],deltaImpulse,c.m_deltaVelBindex,ndofB);
		c.m_multiBodyB->applyDeltaVee(&m_data.m_deltaVelocitiesUnitImpulse[c.m_jacBindex],deltaImpulse);
	} else
	{
		bodyB->internalApplyImpulse(c.m_contactNormal2*bodyB->internalGetInvMass(),c.m_angularComponentB,deltaImpulse);
	}

}


void btMultiBodyConstraintSolver::resolveSingleConstraintRowGenericMultiBody(const btMultiBodySolverConstraint& c)
{

	btScalar deltaImpulse = c.m_rhs-btScalar(c.m_appliedImpulse)*c.m_cfm;
	btScalar deltaVelADotn=0;
	btScalar deltaVelBDotn=0;
	int ndofA=0;
	int ndofB=0;

	if (c.m_multiBodyA)
	{
		ndofA  = c.m_multiBodyA->getNumLinks() + 6;
		for (int i = 0; i < ndofA; ++i) 
			deltaVelADotn += m_data.m_jacobians[c.m_jacAindex+i] * m_data.m_deltaVelocities[c.m_deltaVelAindex+i];
	}

	if (c.m_multiBodyB)
	{
		ndofB = c.m_multiBodyB->getNumLinks() + 6;
		for (int i = 0; i < ndofB; ++i) 
			deltaVelBDotn += m_data.m_jacobians[c.m_jacBindex+i] * m_data.m_deltaVelocities[c.m_deltaVelBindex+i];
	}

	
	deltaImpulse	-=	deltaVelADotn*c.m_jacDiagABInv;//m_jacDiagABInv = 1./denom
	deltaImpulse	-=	deltaVelBDotn*c.m_jacDiagABInv;
	const btScalar sum = btScalar(c.m_appliedImpulse) + deltaImpulse;
	
	if (sum < c.m_lowerLimit)
	{
		deltaImpulse = c.m_lowerLimit-c.m_appliedImpulse;
		c.m_appliedImpulse = c.m_lowerLimit;
	}
	else if (sum > c.m_upperLimit) 
	{
		deltaImpulse = c.m_upperLimit-c.m_appliedImpulse;
		c.m_appliedImpulse = c.m_upperLimit;
	}
	else
	{
		c.m_appliedImpulse = sum;
	}

	if (c.m_multiBodyA)
	{
		applyDeltaVee(&m_data.m_deltaVelocitiesUnitImpulse[c.m_jacAindex],deltaImpulse,c.m_deltaVelAindex,ndofA);
		c.m_multiBodyA->applyDeltaVee(&m_data.m_deltaVelocitiesUnitImpulse[c.m_jacAindex],deltaImpulse);
	}
	if (c.m_multiBodyB)
	{
		applyDeltaVee(&m_data.m_deltaVelocitiesUnitImpulse[c.m_jacBindex],deltaImpulse,c.m_deltaVelBindex,ndofB);
		c.m_multiBodyB->applyDeltaVee(&m_data.m_deltaVelocitiesUnitImpulse[c.m_jacBindex],deltaImpulse);
	}
}


void btMultiBodyConstraintSolver::setupMultiBodyContactConstraint(btMultiBodySolverConstraint& solverConstraint, 
																 const btVector3& contactNormal,
																 btManifoldPoint& cp, const btContactSolverInfo& infoGlobal,
																 btScalar& relaxation,
																 bool isFriction, btScalar desiredVelocity, btScalar cfmSlip)
{
			
	BT_PROFILE("setupMultiBodyContactConstraint");
	btVector3 rel_pos1;
	btVector3 rel_pos2;

	btMultiBody* multiBodyA = solverConstraint.m_multiBodyA;
	btMultiBody* multiBodyB = solverConstraint.m_multiBodyB;

	const btVector3& pos1 = cp.getPositionWorldOnA();
	const btVector3& pos2 = cp.getPositionWorldOnB();

	btSolverBody* bodyA = multiBodyA ? 0 : &m_tmpSolverBodyPool[solverConstraint.m_solverBodyIdA];
	btSolverBody* bodyB = multiBodyB ? 0 : &m_tmpSolverBodyPool[solverConstraint.m_solverBodyIdB];

	btRigidBody* rb0 = multiBodyA ? 0 : bodyA->m_originalBody;
	btRigidBody* rb1 = multiBodyB ? 0 : bodyB->m_originalBody;

	if (bodyA)
		rel_pos1 = pos1 - bodyA->getWorldTransform().getOrigin(); 
	if (bodyB)
		rel_pos2 = pos2 - bodyB->getWorldTransform().getOrigin();

	relaxation = 1.f;

	if (multiBodyA)
	{
		const int ndofA  = multiBodyA->getNumLinks() + 6;

		solverConstraint.m_deltaVelAindex = multiBodyA->getCompanionId();

		if (solverConstraint.m_deltaVelAindex <0)
		{
			solverConstraint.m_deltaVelAindex = m_data.m_deltaVelocities.size();
			multiBodyA->setCompanionId(solverConstraint.m_deltaVelAindex);
			m_data.m_deltaVelocities.resize(m_data.m_deltaVelocities.size()+ndofA);
		} else
		{
			btAssert(m_data.m_deltaVelocities.size() >= solverConstraint.m_deltaVelAindex+ndofA);
		}

		solverConstraint.m_jacAindex = m_data.m_jacobians.size();
		m_data.m_jacobians.resize(m_data.m_jacobians.size()+ndofA);
		m_data.m_deltaVelocitiesUnitImpulse.resize(m_data.m_deltaVelocitiesUnitImpulse.size()+ndofA);
		btAssert(m_data.m_jacobians.size() == m_data.m_deltaVelocitiesUnitImpulse.size());

		btScalar* jac1=&m_data.m_jacobians[solverConstraint.m_jacAindex];
		multiBodyA->fillContactJacobian(solverConstraint.m_linkA, cp.getPositionWorldOnA(), contactNormal, jac1, m_data.scratch_r, m_data.scratch_v, m_data.scratch_m);
		btScalar* delta = &m_data.m_deltaVelocitiesUnitImpulse[solverConstraint.m_jacAindex];
		multiBodyA->calcAccelerationDeltas(&m_data.m_jacobians[solverConstraint.m_jacAindex],delta,m_data.scratch_r, m_data.scratch_v);
	} else
	{
		btVector3 torqueAxis0 = rel_pos1.cross(contactNormal);
		solverConstraint.m_angularComponentA = rb0 ? rb0->getInvInertiaTensorWorld()*torqueAxis0*rb0->getAngularFactor() : btVector3(0,0,0);
		solverConstraint.m_relpos1CrossNormal = torqueAxis0;
		solverConstraint.m_contactNormal1 = contactNormal;
	}

	if (multiBodyB)
	{
		const int ndofB  = multiBodyB->getNumLinks() + 6;

		solverConstraint.m_deltaVelBindex = multiBodyB->getCompanionId();
		if (solverConstraint.m_deltaVelBindex <0)
		{
			solverConstraint.m_deltaVelBindex = m_data.m_deltaVelocities.size();
			multiBodyB->setCompanionId(solverConstraint.m_deltaVelBindex);
			m_data.m_deltaVelocities.resize(m_data.m_deltaVelocities.size()+ndofB);
		}

		solverConstraint.m_jacBindex = m_data.m_jacobians.size();

		m_data.m_jacobians.resize(m_data.m_jacobians.size()+ndofB);
		m_data.m_deltaVelocitiesUnitImpulse.resize(m_data.m_deltaVelocitiesUnitImpulse.size()+ndofB);
		btAssert(m_data.m_jacobians.size() == m_data.m_deltaVelocitiesUnitImpulse.size());

		multiBodyB->fillContactJacobian(solverConstraint.m_linkB, cp.getPositionWorldOnB(), -contactNormal, &m_data.m_jacobians[solverConstraint.m_jacBindex], m_data.scratch_r, m_data.scratch_v, m_data.scratch_m);
		multiBodyB->calcAccelerationDeltas(&m_data.m_jacobians[solverConstraint.m_jacBindex],&m_data.m_deltaVelocitiesUnitImpulse[solverConstraint.m_jacBindex],m_data.scratch_r, m_data.scratch_v);
	} else
	{
		btVector3 torqueAxis1 = rel_pos2.cross(contactNormal);		
		solverConstraint.m_angularComponentB = rb1 ? rb1->getInvInertiaTensorWorld()*-torqueAxis1*rb1->getAngularFactor() : btVector3(0,0,0);
		solverConstraint.m_relpos2CrossNormal = -torqueAxis1;
		solverConstraint.m_contactNormal2 = -contactNormal;
	}

	{
						
		btVector3 vec;
		btScalar denom0 = 0.f;
		btScalar denom1 = 0.f;
		btScalar* jacB = 0;
		btScalar* jacA = 0;
		btScalar* lambdaA =0;
		btScalar* lambdaB =0;
		int ndofA  = 0;
		if (multiBodyA)
		{
			ndofA  = multiBodyA->getNumLinks() + 6;
			jacA = &m_data.m_jacobians[solverConstraint.m_jacAindex];
			lambdaA = &m_data.m_deltaVelocitiesUnitImpulse[solverConstraint.m_jacAindex];
			for (int i = 0; i < ndofA; ++i)
			{
				btScalar j = jacA[i] ;
				btScalar l =lambdaA[i];
				denom0 += j*l;
			}
		} else
		{
			if (rb0)
			{
				vec = ( solverConstraint.m_angularComponentA).cross(rel_pos1);
				denom0 = rb0->getInvMass() + contactNormal.dot(vec);
			}
		}
		if (multiBodyB)
		{
			const int ndofB  = multiBodyB->getNumLinks() + 6;
			jacB = &m_data.m_jacobians[solverConstraint.m_jacBindex];
			lambdaB = &m_data.m_deltaVelocitiesUnitImpulse[solverConstraint.m_jacBindex];
			for (int i = 0; i < ndofB; ++i)
			{
				btScalar j = jacB[i] ;
				btScalar l =lambdaB[i];
				denom1 += j*l;
			}

		} else
		{
			if (rb1)
			{
				vec = ( -solverConstraint.m_angularComponentB).cross(rel_pos2);
				denom1 = rb1->getInvMass() + contactNormal.dot(vec);
			}
		}

		 if (multiBodyA && (multiBodyA==multiBodyB))
		 {
            // ndof1 == ndof2 in this case
            for (int i = 0; i < ndofA; ++i) 
			{
                denom1 += jacB[i] * lambdaA[i];
                denom1 += jacA[i] * lambdaB[i];
            }
        }

		 btScalar d = denom0+denom1;
		 if (btFabs(d)>SIMD_EPSILON)
		 {
			 
			 solverConstraint.m_jacDiagABInv = relaxation/(d);
		 } else
		 {
			solverConstraint.m_jacDiagABInv  = 1.f;
		 }
		
	}

	
	//compute rhs and remaining solverConstraint fields

	

	btScalar restitution = 0.f;
	btScalar penetration = isFriction? 0 : cp.getDistance()+infoGlobal.m_linearSlop;

	btScalar rel_vel = 0.f;
	int ndofA  = 0;
	int ndofB  = 0;
	{

		btVector3 vel1,vel2;
		if (multiBodyA)
		{
			ndofA  = multiBodyA->getNumLinks() + 6;
			btScalar* jacA = &m_data.m_jacobians[solverConstraint.m_jacAindex];
			for (int i = 0; i < ndofA ; ++i) 
				rel_vel += multiBodyA->getVelocityVector()[i] * jacA[i];
		} else
		{
			if (rb0)
			{
				rel_vel += rb0->getVelocityInLocalPoint(rel_pos1).dot(solverConstraint.m_contactNormal1);
			}
		}
		if (multiBodyB)
		{
			ndofB  = multiBodyB->getNumLinks() + 6;
			btScalar* jacB = &m_data.m_jacobians[solverConstraint.m_jacBindex];
			for (int i = 0; i < ndofB ; ++i) 
				rel_vel += multiBodyB->getVelocityVector()[i] * jacB[i];

		} else
		{
			if (rb1)
			{
				rel_vel += rb1->getVelocityInLocalPoint(rel_pos2).dot(solverConstraint.m_contactNormal2);
			}
		}

		solverConstraint.m_friction = cp.m_combinedFriction;

				
		restitution =  restitutionCurve(rel_vel, cp.m_combinedRestitution);
		if (restitution <= btScalar(0.))
		{
			restitution = 0.f;
		};
	}


	///warm starting (or zero if disabled)
	if (infoGlobal.m_solverMode & SOLVER_USE_WARMSTARTING)
	{
		solverConstraint.m_appliedImpulse = isFriction ? 0 : cp.m_appliedImpulse * infoGlobal.m_warmstartingFactor;

		if (solverConstraint.m_appliedImpulse)
		{
			if (multiBodyA)
			{
				btScalar impulse = solverConstraint.m_appliedImpulse;
				btScalar* deltaV = &m_data.m_deltaVelocitiesUnitImpulse[solverConstraint.m_jacAindex];
				multiBodyA->applyDeltaVee(deltaV,impulse);
				applyDeltaVee(deltaV,impulse,solverConstraint.m_deltaVelAindex,ndofA);
			} else
			{
				if (rb0)
					bodyA->internalApplyImpulse(solverConstraint.m_contactNormal1*bodyA->internalGetInvMass()*rb0->getLinearFactor(),solverConstraint.m_angularComponentA,solverConstraint.m_appliedImpulse);
			}
			if (multiBodyB)
			{
				btScalar impulse = solverConstraint.m_appliedImpulse;
				btScalar* deltaV = &m_data.m_deltaVelocitiesUnitImpulse[solverConstraint.m_jacBindex];
				multiBodyB->applyDeltaVee(deltaV,impulse);
				applyDeltaVee(deltaV,impulse,solverConstraint.m_deltaVelBindex,ndofB);
			} else
			{
				if (rb1)
					bodyB->internalApplyImpulse(-solverConstraint.m_contactNormal2*bodyB->internalGetInvMass()*rb1->getLinearFactor(),-solverConstraint.m_angularComponentB,-(btScalar)solverConstraint.m_appliedImpulse);
			}
		}
	} else
	{
		solverConstraint.m_appliedImpulse = 0.f;
	}

	solverConstraint.m_appliedPushImpulse = 0.f;

	{
		

		btScalar positionalError = 0.f;
		btScalar	velocityError = restitution - rel_vel;// * damping;
					

		btScalar erp = infoGlobal.m_erp2;
		if (!infoGlobal.m_splitImpulse || (penetration > infoGlobal.m_splitImpulsePenetrationThreshold))
		{
			erp = infoGlobal.m_erp;
		}

		if (penetration>0)
		{
			positionalError = 0;
			velocityError = -penetration / infoGlobal.m_timeStep;

		} else
		{
			positionalError = -penetration * erp/infoGlobal.m_timeStep;
		}

		btScalar  penetrationImpulse = positionalError*solverConstraint.m_jacDiagABInv;
		btScalar velocityImpulse = velocityError *solverConstraint.m_jacDiagABInv;

		if (!infoGlobal.m_splitImpulse || (penetration > infoGlobal.m_splitImpulsePenetrationThreshold))
		{
			//combine position and velocity into rhs
			solverConstraint.m_rhs = penetrationImpulse+velocityImpulse;
			solverConstraint.m_rhsPenetration = 0.f;

		} else
		{
			//split position and velocity into rhs and m_rhsPenetration
			solverConstraint.m_rhs = velocityImpulse;
			solverConstraint.m_rhsPenetration = penetrationImpulse;
		}

		solverConstraint.m_cfm = 0.f;
		solverConstraint.m_lowerLimit = 0;
		solverConstraint.m_upperLimit = 1e10f;
	}

}




btMultiBodySolverConstraint&	btMultiBodyConstraintSolver::addMultiBodyFrictionConstraint(const btVector3& normalAxis,btPersistentManifold* manifold,int frictionIndex,btManifoldPoint& cp,btCollisionObject* colObj0,btCollisionObject* colObj1, btScalar relaxation, const btContactSolverInfo& infoGlobal, btScalar desiredVelocity, btScalar cfmSlip)
{
	BT_PROFILE("addMultiBodyFrictionConstraint");
	btMultiBodySolverConstraint& solverConstraint = m_multiBodyFrictionContactConstraints.expandNonInitializing();
	solverConstraint.m_frictionIndex = frictionIndex;
	bool isFriction = true;

	const btMultiBodyLinkCollider* fcA = btMultiBodyLinkCollider::upcast(manifold->getBody0());
	const btMultiBodyLinkCollider* fcB = btMultiBodyLinkCollider::upcast(manifold->getBody1());
	
	btMultiBody* mbA = fcA? fcA->m_multiBody : 0;
	btMultiBody* mbB = fcB? fcB->m_multiBody : 0;

	int solverBodyIdA = mbA? -1 : getOrInitSolverBody(*colObj0,infoGlobal.m_timeStep);
	int solverBodyIdB = mbB ? -1 : getOrInitSolverBody(*colObj1,infoGlobal.m_timeStep);

	solverConstraint.m_solverBodyIdA = solverBodyIdA;
	solverConstraint.m_solverBodyIdB = solverBodyIdB;
	solverConstraint.m_multiBodyA = mbA;
	if (mbA)
		solverConstraint.m_linkA = fcA->m_link;

	solverConstraint.m_multiBodyB = mbB;
	if (mbB)
		solverConstraint.m_linkB = fcB->m_link;

	solverConstraint.m_originalContactPoint = &cp;

	setupMultiBodyContactConstraint(solverConstraint, normalAxis, cp, infoGlobal,relaxation,isFriction, desiredVelocity, cfmSlip);
	return solverConstraint;
}

void	btMultiBodyConstraintSolver::convertMultiBodyContact(btPersistentManifold* manifold,const btContactSolverInfo& infoGlobal)
{
	const btMultiBodyLinkCollider* fcA = btMultiBodyLinkCollider::upcast(manifold->getBody0());
	const btMultiBodyLinkCollider* fcB = btMultiBodyLinkCollider::upcast(manifold->getBody1());
	
	btMultiBody* mbA = fcA? fcA->m_multiBody : 0;
	btMultiBody* mbB = fcB? fcB->m_multiBody : 0;

	btCollisionObject* colObj0=0,*colObj1=0;

	colObj0 = (btCollisionObject*)manifold->getBody0();
	colObj1 = (btCollisionObject*)manifold->getBody1();

	int solverBodyIdA = mbA? -1 : getOrInitSolverBody(*colObj0,infoGlobal.m_timeStep);
	int solverBodyIdB = mbB ? -1 : getOrInitSolverBody(*colObj1,infoGlobal.m_timeStep);

	btSolverBody* solverBodyA = mbA ? 0 : &m_tmpSolverBodyPool[solverBodyIdA];
	btSolverBody* solverBodyB = mbB ? 0 : &m_tmpSolverBodyPool[solverBodyIdB];


	///avoid collision response between two static objects
//	if (!solverBodyA || (solverBodyA->m_invMass.isZero() && (!solverBodyB || solverBodyB->m_invMass.isZero())))
	//	return;

	int rollingFriction=1;

	for (int j=0;j<manifold->getNumContacts();j++)
	{

		btManifoldPoint& cp = manifold->getContactPoint(j);

		if (cp.getDistance() <= manifold->getContactProcessingThreshold())
		{
		
			btScalar relaxation;

			int frictionIndex = m_multiBodyNormalContactConstraints.size();

			btMultiBodySolverConstraint& solverConstraint = m_multiBodyNormalContactConstraints.expandNonInitializing();

			btRigidBody* rb0 = btRigidBody::upcast(colObj0);
			btRigidBody* rb1 = btRigidBody::upcast(colObj1);
			solverConstraint.m_solverBodyIdA = solverBodyIdA;
			solverConstraint.m_solverBodyIdB = solverBodyIdB;
			solverConstraint.m_multiBodyA = mbA;
			if (mbA)
				solverConstraint.m_linkA = fcA->m_link;

			solverConstraint.m_multiBodyB = mbB;
			if (mbB)
				solverConstraint.m_linkB = fcB->m_link;

			solverConstraint.m_originalContactPoint = &cp;

			bool isFriction = false;
			setupMultiBodyContactConstraint(solverConstraint, cp.m_normalWorldOnB,cp, infoGlobal, relaxation, isFriction);

//			const btVector3& pos1 = cp.getPositionWorldOnA();
//			const btVector3& pos2 = cp.getPositionWorldOnB();

			/////setup the friction constraints
#define ENABLE_FRICTION
#ifdef ENABLE_FRICTION
			solverConstraint.m_frictionIndex = frictionIndex;
#if ROLLING_FRICTION
			btVector3 angVelA(0,0,0),angVelB(0,0,0);
			if (rb0)
				angVelA = rb0->getAngularVelocity();
			if (rb1)
				angVelB = rb1->getAngularVelocity();
			btVector3 relAngVel = angVelB-angVelA;

			if ((cp.m_combinedRollingFriction>0.f) && (rollingFriction>0))
			{
				//only a single rollingFriction per manifold
				rollingFriction--;
				if (relAngVel.length()>infoGlobal.m_singleAxisRollingFrictionThreshold)
				{
					relAngVel.normalize();
					applyAnisotropicFriction(colObj0,relAngVel,btCollisionObject::CF_ANISOTROPIC_ROLLING_FRICTION);
					applyAnisotropicFriction(colObj1,relAngVel,btCollisionObject::CF_ANISOTROPIC_ROLLING_FRICTION);
					if (relAngVel.length()>0.001)
						addRollingFrictionConstraint(relAngVel,solverBodyIdA,solverBodyIdB,frictionIndex,cp,rel_pos1,rel_pos2,colObj0,colObj1, relaxation);

				} else
				{
					addRollingFrictionConstraint(cp.m_normalWorldOnB,solverBodyIdA,solverBodyIdB,frictionIndex,cp,rel_pos1,rel_pos2,colObj0,colObj1, relaxation);
					btVector3 axis0,axis1;
					btPlaneSpace1(cp.m_normalWorldOnB,axis0,axis1);
					applyAnisotropicFriction(colObj0,axis0,btCollisionObject::CF_ANISOTROPIC_ROLLING_FRICTION);
					applyAnisotropicFriction(colObj1,axis0,btCollisionObject::CF_ANISOTROPIC_ROLLING_FRICTION);
					applyAnisotropicFriction(colObj0,axis1,btCollisionObject::CF_ANISOTROPIC_ROLLING_FRICTION);
					applyAnisotropicFriction(colObj1,axis1,btCollisionObject::CF_ANISOTROPIC_ROLLING_FRICTION);
					if (axis0.length()>0.001)
						addRollingFrictionConstraint(axis0,solverBodyIdA,solverBodyIdB,frictionIndex,cp,rel_pos1,rel_pos2,colObj0,colObj1, relaxation);
					if (axis1.length()>0.001)
						addRollingFrictionConstraint(axis1,solverBodyIdA,solverBodyIdB,frictionIndex,cp,rel_pos1,rel_pos2,colObj0,colObj1, relaxation);
		
				}
			}
#endif //ROLLING_FRICTION

			///Bullet has several options to set the friction directions
			///By default, each contact has only a single friction direction that is recomputed automatically very frame 
			///based on the relative linear velocity.
			///If the relative velocity it zero, it will automatically compute a friction direction.
			
			///You can also enable two friction directions, using the SOLVER_USE_2_FRICTION_DIRECTIONS.
			///In that case, the second friction direction will be orthogonal to both contact normal and first friction direction.
			///
			///If you choose SOLVER_DISABLE_VELOCITY_DEPENDENT_FRICTION_DIRECTION, then the friction will be independent from the relative projected velocity.
			///
			///The user can manually override the friction directions for certain contacts using a contact callback, 
			///and set the cp.m_lateralFrictionInitialized to true
			///In that case, you can set the target relative motion in each friction direction (cp.m_contactMotion1 and cp.m_contactMotion2)
			///this will give a conveyor belt effect
			///
			if (!(infoGlobal.m_solverMode & SOLVER_ENABLE_FRICTION_DIRECTION_CACHING) || !cp.m_lateralFrictionInitialized)
			{/*
				cp.m_lateralFrictionDir1 = vel - cp.m_normalWorldOnB * rel_vel;
				btScalar lat_rel_vel = cp.m_lateralFrictionDir1.length2();
				if (!(infoGlobal.m_solverMode & SOLVER_DISABLE_VELOCITY_DEPENDENT_FRICTION_DIRECTION) && lat_rel_vel > SIMD_EPSILON)
				{
					cp.m_lateralFrictionDir1 *= 1.f/btSqrt(lat_rel_vel);
					if((infoGlobal.m_solverMode & SOLVER_USE_2_FRICTION_DIRECTIONS))
					{
						cp.m_lateralFrictionDir2 = cp.m_lateralFrictionDir1.cross(cp.m_normalWorldOnB);
						cp.m_lateralFrictionDir2.normalize();//??
						applyAnisotropicFriction(colObj0,cp.m_lateralFrictionDir2,btCollisionObject::CF_ANISOTROPIC_FRICTION);
						applyAnisotropicFriction(colObj1,cp.m_lateralFrictionDir2,btCollisionObject::CF_ANISOTROPIC_FRICTION);
						addMultiBodyFrictionConstraint(cp.m_lateralFrictionDir2,solverBodyIdA,solverBodyIdB,frictionIndex,cp,rel_pos1,rel_pos2,colObj0,colObj1, relaxation);

					}

					applyAnisotropicFriction(colObj0,cp.m_lateralFrictionDir1,btCollisionObject::CF_ANISOTROPIC_FRICTION);
					applyAnisotropicFriction(colObj1,cp.m_lateralFrictionDir1,btCollisionObject::CF_ANISOTROPIC_FRICTION);
					addMultiBodyFrictionConstraint(cp.m_lateralFrictionDir1,solverBodyIdA,solverBodyIdB,frictionIndex,cp,rel_pos1,rel_pos2,colObj0,colObj1, relaxation);

				} else
				*/
				{
					btPlaneSpace1(cp.m_normalWorldOnB,cp.m_lateralFrictionDir1,cp.m_lateralFrictionDir2);

					if ((infoGlobal.m_solverMode & SOLVER_USE_2_FRICTION_DIRECTIONS))
					{
						applyAnisotropicFriction(colObj0,cp.m_lateralFrictionDir2,btCollisionObject::CF_ANISOTROPIC_FRICTION);
						applyAnisotropicFriction(colObj1,cp.m_lateralFrictionDir2,btCollisionObject::CF_ANISOTROPIC_FRICTION);
						addMultiBodyFrictionConstraint(cp.m_lateralFrictionDir2,manifold,frictionIndex,cp,colObj0,colObj1, relaxation,infoGlobal);
					}

					applyAnisotropicFriction(colObj0,cp.m_lateralFrictionDir1,btCollisionObject::CF_ANISOTROPIC_FRICTION);
					applyAnisotropicFriction(colObj1,cp.m_lateralFrictionDir1,btCollisionObject::CF_ANISOTROPIC_FRICTION);
					addMultiBodyFrictionConstraint(cp.m_lateralFrictionDir1,manifold,frictionIndex,cp,colObj0,colObj1, relaxation,infoGlobal);

					if ((infoGlobal.m_solverMode & SOLVER_USE_2_FRICTION_DIRECTIONS) && (infoGlobal.m_solverMode & SOLVER_DISABLE_VELOCITY_DEPENDENT_FRICTION_DIRECTION))
					{
						cp.m_lateralFrictionInitialized = true;
					}
				}

			} else
			{
				addMultiBodyFrictionConstraint(cp.m_lateralFrictionDir1,manifold,frictionIndex,cp,colObj0,colObj1, relaxation,infoGlobal,cp.m_contactMotion1, cp.m_contactCFM1);

				if ((infoGlobal.m_solverMode & SOLVER_USE_2_FRICTION_DIRECTIONS))
					addMultiBodyFrictionConstraint(cp.m_lateralFrictionDir2,manifold,frictionIndex,cp,colObj0,colObj1, relaxation, infoGlobal,cp.m_contactMotion2, cp.m_contactCFM2);

				//setMultiBodyFrictionConstraintImpulse( solverConstraint, solverBodyIdA, solverBodyIdB, cp, infoGlobal);
				//todo:
				solverConstraint.m_appliedImpulse = 0.f;
				solverConstraint.m_appliedPushImpulse = 0.f;
			}
		

#endif //ENABLE_FRICTION

		}
	}
}

void btMultiBodyConstraintSolver::convertContacts(btPersistentManifold** manifoldPtr,int numManifolds, const btContactSolverInfo& infoGlobal)
{
	btPersistentManifold* manifold = 0;

	for (int i=0;i<numManifolds;i++)
	{
		btPersistentManifold* manifold= manifoldPtr[i];
		const btMultiBodyLinkCollider* fcA = btMultiBodyLinkCollider::upcast(manifold->getBody0());
		const btMultiBodyLinkCollider* fcB = btMultiBodyLinkCollider::upcast(manifold->getBody1());
		if (!fcA && !fcB)
		{
			//the contact doesn't involve any Featherstone btMultiBody, so deal with the regular btRigidBody/btCollisionObject case
			convertContact(manifold,infoGlobal);
		} else
		{
			convertMultiBodyContact(manifold,infoGlobal);
		}
	}

	//also convert the multibody constraints, if any

	
	for (int i=0;i<m_tmpNumMultiBodyConstraints;i++)
	{
		btMultiBodyConstraint* c = m_tmpMultiBodyConstraints[i];
		m_data.m_solverBodyPool = &m_tmpSolverBodyPool;
		m_data.m_fixedBodyId = m_fixedBodyId;
		
		c->createConstraintRows(m_multiBodyNonContactConstraints,m_data,	infoGlobal);
	}

}



btScalar btMultiBodyConstraintSolver::solveGroup(btCollisionObject** bodies,int numBodies,btPersistentManifold** manifold,int numManifolds,btTypedConstraint** constraints,int numConstraints,const btContactSolverInfo& info, btIDebugDraw* debugDrawer,btDispatcher* dispatcher)
{
	return btSequentialImpulseConstraintSolver::solveGroup(bodies,numBodies,manifold,numManifolds,constraints,numConstraints,info,debugDrawer,dispatcher);
}


void  btMultiBodyConstraintSolver::solveMultiBodyGroup(btCollisionObject** bodies,int numBodies,btPersistentManifold** manifold,int numManifolds,btTypedConstraint** constraints,int numConstraints,btMultiBodyConstraint** multiBodyConstraints, int numMultiBodyConstraints, const btContactSolverInfo& info, btIDebugDraw* debugDrawer,btDispatcher* dispatcher)
{
	//printf("solveMultiBodyGroup start\n");
	m_tmpMultiBodyConstraints = multiBodyConstraints;
	m_tmpNumMultiBodyConstraints = numMultiBodyConstraints;
	
	btSequentialImpulseConstraintSolver::solveGroup(bodies,numBodies,manifold,numManifolds,constraints,numConstraints,info,debugDrawer,dispatcher);

	m_tmpMultiBodyConstraints = 0;
	m_tmpNumMultiBodyConstraints = 0;
	

}
