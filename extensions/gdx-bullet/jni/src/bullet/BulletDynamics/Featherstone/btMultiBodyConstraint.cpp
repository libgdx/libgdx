#include "btMultiBodyConstraint.h"
#include "BulletDynamics/Dynamics/btRigidBody.h"

btMultiBodyConstraint::btMultiBodyConstraint(btMultiBody* bodyA,btMultiBody* bodyB,int linkA, int linkB, int numRows, bool isUnilateral)
	:m_bodyA(bodyA),
	m_bodyB(bodyB),
	m_linkA(linkA),
	m_linkB(linkB),
	m_num_rows(numRows),
	m_isUnilateral(isUnilateral),
	m_maxAppliedImpulse(100)
{
	m_jac_size_A = (6 + bodyA->getNumLinks());
	m_jac_size_both = (m_jac_size_A + (bodyB ? 6 + bodyB->getNumLinks() : 0));
	m_pos_offset = ((1 + m_jac_size_both)*m_num_rows);
	m_data.resize((2 + m_jac_size_both) * m_num_rows);
}

btMultiBodyConstraint::~btMultiBodyConstraint()
{
}



btScalar btMultiBodyConstraint::fillConstraintRowMultiBodyMultiBody(btMultiBodySolverConstraint& constraintRow,
														btMultiBodyJacobianData& data,
														btScalar* jacOrgA,btScalar* jacOrgB,
														const btContactSolverInfo& infoGlobal,
														btScalar desiredVelocity,
														btScalar lowerLimit,
														btScalar upperLimit)
{
			
	
	
	constraintRow.m_multiBodyA = m_bodyA;
	constraintRow.m_multiBodyB = m_bodyB;

	btMultiBody* multiBodyA = constraintRow.m_multiBodyA;
	btMultiBody* multiBodyB = constraintRow.m_multiBodyB;

	if (multiBodyA)
	{
		
		const int ndofA  = multiBodyA->getNumLinks() + 6;

		constraintRow.m_deltaVelAindex = multiBodyA->getCompanionId();

		if (constraintRow.m_deltaVelAindex <0)
		{
			constraintRow.m_deltaVelAindex = data.m_deltaVelocities.size();
			multiBodyA->setCompanionId(constraintRow.m_deltaVelAindex);
			data.m_deltaVelocities.resize(data.m_deltaVelocities.size()+ndofA);
		} else
		{
			btAssert(data.m_deltaVelocities.size() >= constraintRow.m_deltaVelAindex+ndofA);
		}

		constraintRow.m_jacAindex = data.m_jacobians.size();
		data.m_jacobians.resize(data.m_jacobians.size()+ndofA);
		data.m_deltaVelocitiesUnitImpulse.resize(data.m_deltaVelocitiesUnitImpulse.size()+ndofA);
		btAssert(data.m_jacobians.size() == data.m_deltaVelocitiesUnitImpulse.size());
		for (int i=0;i<ndofA;i++)
			data.m_jacobians[constraintRow.m_jacAindex+i] = jacOrgA[i];
		
		btScalar* delta = &data.m_deltaVelocitiesUnitImpulse[constraintRow.m_jacAindex];
		multiBodyA->calcAccelerationDeltas(&data.m_jacobians[constraintRow.m_jacAindex],delta,data.scratch_r, data.scratch_v);
	} 

	if (multiBodyB)
	{
		const int ndofB  = multiBodyB->getNumLinks() + 6;

		constraintRow.m_deltaVelBindex = multiBodyB->getCompanionId();
		if (constraintRow.m_deltaVelBindex <0)
		{
			constraintRow.m_deltaVelBindex = data.m_deltaVelocities.size();
			multiBodyB->setCompanionId(constraintRow.m_deltaVelBindex);
			data.m_deltaVelocities.resize(data.m_deltaVelocities.size()+ndofB);
		}

		constraintRow.m_jacBindex = data.m_jacobians.size();
		data.m_jacobians.resize(data.m_jacobians.size()+ndofB);

		for (int i=0;i<ndofB;i++)
			data.m_jacobians[constraintRow.m_jacBindex+i] = jacOrgB[i];

		data.m_deltaVelocitiesUnitImpulse.resize(data.m_deltaVelocitiesUnitImpulse.size()+ndofB);
		btAssert(data.m_jacobians.size() == data.m_deltaVelocitiesUnitImpulse.size());
		multiBodyB->calcAccelerationDeltas(&data.m_jacobians[constraintRow.m_jacBindex],&data.m_deltaVelocitiesUnitImpulse[constraintRow.m_jacBindex],data.scratch_r, data.scratch_v);
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
			jacA = &data.m_jacobians[constraintRow.m_jacAindex];
			lambdaA = &data.m_deltaVelocitiesUnitImpulse[constraintRow.m_jacAindex];
			for (int i = 0; i < ndofA; ++i)
			{
				btScalar j = jacA[i] ;
				btScalar l =lambdaA[i];
				denom0 += j*l;
			}
		} 
		if (multiBodyB)
		{
			const int ndofB  = multiBodyB->getNumLinks() + 6;
			jacB = &data.m_jacobians[constraintRow.m_jacBindex];
			lambdaB = &data.m_deltaVelocitiesUnitImpulse[constraintRow.m_jacBindex];
			for (int i = 0; i < ndofB; ++i)
			{
				btScalar j = jacB[i] ;
				btScalar l =lambdaB[i];
				denom1 += j*l;
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
			 
			 constraintRow.m_jacDiagABInv = 1.f/(d);
		 } else
		 {
			constraintRow.m_jacDiagABInv  = 1.f;
		 }
		
	}

	
	//compute rhs and remaining constraintRow fields

	


	btScalar rel_vel = 0.f;
	int ndofA  = 0;
	int ndofB  = 0;
	{

		btVector3 vel1,vel2;
		if (multiBodyA)
		{
			ndofA  = multiBodyA->getNumLinks() + 6;
			btScalar* jacA = &data.m_jacobians[constraintRow.m_jacAindex];
			for (int i = 0; i < ndofA ; ++i) 
				rel_vel += multiBodyA->getVelocityVector()[i] * jacA[i];
		} 
		if (multiBodyB)
		{
			ndofB  = multiBodyB->getNumLinks() + 6;
			btScalar* jacB = &data.m_jacobians[constraintRow.m_jacBindex];
			for (int i = 0; i < ndofB ; ++i) 
				rel_vel += multiBodyB->getVelocityVector()[i] * jacB[i];

		}

		constraintRow.m_friction = 0.f;

		constraintRow.m_appliedImpulse = 0.f;
		constraintRow.m_appliedPushImpulse = 0.f;
		
		btScalar	velocityError =  desiredVelocity - rel_vel;// * damping;

		btScalar erp = infoGlobal.m_erp2;

		btScalar velocityImpulse = velocityError *constraintRow.m_jacDiagABInv;

		if (!infoGlobal.m_splitImpulse)
		{
			//combine position and velocity into rhs
			constraintRow.m_rhs = velocityImpulse;
			constraintRow.m_rhsPenetration = 0.f;

		} else
		{
			//split position and velocity into rhs and m_rhsPenetration
			constraintRow.m_rhs = velocityImpulse;
			constraintRow.m_rhsPenetration = 0.f;
		}


		constraintRow.m_cfm = 0.f;
		constraintRow.m_lowerLimit = lowerLimit;
		constraintRow.m_upperLimit = upperLimit;

	}
	return rel_vel;
}


void	btMultiBodyConstraint::applyDeltaVee(btMultiBodyJacobianData& data, btScalar* delta_vee, btScalar impulse, int velocityIndex, int ndof)
{
	for (int i = 0; i < ndof; ++i) 
		data.m_deltaVelocities[velocityIndex+i] += delta_vee[i] * impulse;
}


void btMultiBodyConstraint::fillMultiBodyConstraintMixed(btMultiBodySolverConstraint& solverConstraint, 
																	btMultiBodyJacobianData& data,
																 const btVector3& contactNormalOnB,
																 const btVector3& posAworld, const btVector3& posBworld, 
																 btScalar position,
																 const btContactSolverInfo& infoGlobal,
																 btScalar& relaxation,
																 bool isFriction, btScalar desiredVelocity, btScalar cfmSlip)
{
			
	
	btVector3 rel_pos1 = posAworld;
	btVector3 rel_pos2 = posBworld;

	solverConstraint.m_multiBodyA = m_bodyA;
	solverConstraint.m_multiBodyB = m_bodyB;
	solverConstraint.m_linkA = m_linkA;
	solverConstraint.m_linkB = m_linkB;
	

	btMultiBody* multiBodyA = solverConstraint.m_multiBodyA;
	btMultiBody* multiBodyB = solverConstraint.m_multiBodyB;

	const btVector3& pos1 = posAworld;
	const btVector3& pos2 = posBworld;

	btSolverBody* bodyA = multiBodyA ? 0 : &data.m_solverBodyPool->at(solverConstraint.m_solverBodyIdA);
	btSolverBody* bodyB = multiBodyB ? 0 : &data.m_solverBodyPool->at(solverConstraint.m_solverBodyIdB);

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
			solverConstraint.m_deltaVelAindex = data.m_deltaVelocities.size();
			multiBodyA->setCompanionId(solverConstraint.m_deltaVelAindex);
			data.m_deltaVelocities.resize(data.m_deltaVelocities.size()+ndofA);
		} else
		{
			btAssert(data.m_deltaVelocities.size() >= solverConstraint.m_deltaVelAindex+ndofA);
		}

		solverConstraint.m_jacAindex = data.m_jacobians.size();
		data.m_jacobians.resize(data.m_jacobians.size()+ndofA);
		data.m_deltaVelocitiesUnitImpulse.resize(data.m_deltaVelocitiesUnitImpulse.size()+ndofA);
		btAssert(data.m_jacobians.size() == data.m_deltaVelocitiesUnitImpulse.size());

		btScalar* jac1=&data.m_jacobians[solverConstraint.m_jacAindex];
		multiBodyA->fillContactJacobian(solverConstraint.m_linkA, posAworld, contactNormalOnB, jac1, data.scratch_r, data.scratch_v, data.scratch_m);
		btScalar* delta = &data.m_deltaVelocitiesUnitImpulse[solverConstraint.m_jacAindex];
		multiBodyA->calcAccelerationDeltas(&data.m_jacobians[solverConstraint.m_jacAindex],delta,data.scratch_r, data.scratch_v);
	} else
	{
		btVector3 torqueAxis0 = rel_pos1.cross(contactNormalOnB);
		solverConstraint.m_angularComponentA = rb0 ? rb0->getInvInertiaTensorWorld()*torqueAxis0*rb0->getAngularFactor() : btVector3(0,0,0);
		solverConstraint.m_relpos1CrossNormal = torqueAxis0;
		solverConstraint.m_contactNormal1 = contactNormalOnB;
	}

	if (multiBodyB)
	{
		const int ndofB  = multiBodyB->getNumLinks() + 6;

		solverConstraint.m_deltaVelBindex = multiBodyB->getCompanionId();
		if (solverConstraint.m_deltaVelBindex <0)
		{
			solverConstraint.m_deltaVelBindex = data.m_deltaVelocities.size();
			multiBodyB->setCompanionId(solverConstraint.m_deltaVelBindex);
			data.m_deltaVelocities.resize(data.m_deltaVelocities.size()+ndofB);
		}

		solverConstraint.m_jacBindex = data.m_jacobians.size();

		data.m_jacobians.resize(data.m_jacobians.size()+ndofB);
		data.m_deltaVelocitiesUnitImpulse.resize(data.m_deltaVelocitiesUnitImpulse.size()+ndofB);
		btAssert(data.m_jacobians.size() == data.m_deltaVelocitiesUnitImpulse.size());

		multiBodyB->fillContactJacobian(solverConstraint.m_linkB, posBworld, -contactNormalOnB, &data.m_jacobians[solverConstraint.m_jacBindex], data.scratch_r, data.scratch_v, data.scratch_m);
		multiBodyB->calcAccelerationDeltas(&data.m_jacobians[solverConstraint.m_jacBindex],&data.m_deltaVelocitiesUnitImpulse[solverConstraint.m_jacBindex],data.scratch_r, data.scratch_v);
	} else
	{
		btVector3 torqueAxis1 = rel_pos2.cross(contactNormalOnB);		
		solverConstraint.m_angularComponentB = rb1 ? rb1->getInvInertiaTensorWorld()*-torqueAxis1*rb1->getAngularFactor() : btVector3(0,0,0);
		solverConstraint.m_relpos2CrossNormal = -torqueAxis1;
		solverConstraint.m_contactNormal2 = -contactNormalOnB;
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
			jacA = &data.m_jacobians[solverConstraint.m_jacAindex];
			lambdaA = &data.m_deltaVelocitiesUnitImpulse[solverConstraint.m_jacAindex];
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
				denom0 = rb0->getInvMass() + contactNormalOnB.dot(vec);
			}
		}
		if (multiBodyB)
		{
			const int ndofB  = multiBodyB->getNumLinks() + 6;
			jacB = &data.m_jacobians[solverConstraint.m_jacBindex];
			lambdaB = &data.m_deltaVelocitiesUnitImpulse[solverConstraint.m_jacBindex];
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
				denom1 = rb1->getInvMass() + contactNormalOnB.dot(vec);
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
	btScalar penetration = isFriction? 0 : position+infoGlobal.m_linearSlop;

	btScalar rel_vel = 0.f;
	int ndofA  = 0;
	int ndofB  = 0;
	{

		btVector3 vel1,vel2;
		if (multiBodyA)
		{
			ndofA  = multiBodyA->getNumLinks() + 6;
			btScalar* jacA = &data.m_jacobians[solverConstraint.m_jacAindex];
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
			btScalar* jacB = &data.m_jacobians[solverConstraint.m_jacBindex];
			for (int i = 0; i < ndofB ; ++i) 
				rel_vel += multiBodyB->getVelocityVector()[i] * jacB[i];

		} else
		{
			if (rb1)
			{
				rel_vel += rb1->getVelocityInLocalPoint(rel_pos2).dot(solverConstraint.m_contactNormal2);
			}
		}

		solverConstraint.m_friction = 0.f;//cp.m_combinedFriction;

				
		restitution =  restitution * -rel_vel;//restitutionCurve(rel_vel, cp.m_combinedRestitution);
		if (restitution <= btScalar(0.))
		{
			restitution = 0.f;
		};
	}


	///warm starting (or zero if disabled)
	/*
	if (infoGlobal.m_solverMode & SOLVER_USE_WARMSTARTING)
	{
		solverConstraint.m_appliedImpulse = isFriction ? 0 : cp.m_appliedImpulse * infoGlobal.m_warmstartingFactor;

		if (solverConstraint.m_appliedImpulse)
		{
			if (multiBodyA)
			{
				btScalar impulse = solverConstraint.m_appliedImpulse;
				btScalar* deltaV = &data.m_deltaVelocitiesUnitImpulse[solverConstraint.m_jacAindex];
				multiBodyA->applyDeltaVee(deltaV,impulse);
				applyDeltaVee(data,deltaV,impulse,solverConstraint.m_deltaVelAindex,ndofA);
			} else
			{
				if (rb0)
					bodyA->internalApplyImpulse(solverConstraint.m_contactNormal1*bodyA->internalGetInvMass()*rb0->getLinearFactor(),solverConstraint.m_angularComponentA,solverConstraint.m_appliedImpulse);
			}
			if (multiBodyB)
			{
				btScalar impulse = solverConstraint.m_appliedImpulse;
				btScalar* deltaV = &data.m_deltaVelocitiesUnitImpulse[solverConstraint.m_jacBindex];
				multiBodyB->applyDeltaVee(deltaV,impulse);
				applyDeltaVee(data,deltaV,impulse,solverConstraint.m_deltaVelBindex,ndofB);
			} else
			{
				if (rb1)
					bodyB->internalApplyImpulse(-solverConstraint.m_contactNormal2*bodyB->internalGetInvMass()*rb1->getLinearFactor(),-solverConstraint.m_angularComponentB,-(btScalar)solverConstraint.m_appliedImpulse);
			}
		}
	} else
	*/
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
		solverConstraint.m_lowerLimit = -m_maxAppliedImpulse;
		solverConstraint.m_upperLimit = m_maxAppliedImpulse;
	}

}
