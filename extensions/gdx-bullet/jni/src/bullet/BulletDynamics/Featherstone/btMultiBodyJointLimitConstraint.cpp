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

///This file was written by Erwin Coumans

#include "btMultiBodyJointLimitConstraint.h"
#include "btMultiBody.h"
#include "btMultiBodyLinkCollider.h"
#include "BulletCollision/CollisionDispatch/btCollisionObject.h"


btMultiBodyJointLimitConstraint::btMultiBodyJointLimitConstraint(btMultiBody* body, int link, btScalar lower, btScalar upper)
	//:btMultiBodyConstraint(body,0,link,-1,2,true),
	:btMultiBodyConstraint(body,body,link,link,2,true),
	m_lowerBound(lower),
	m_upperBound(upper)
{
	// the data.m_jacobians never change, so may as well
    // initialize them here
        
    // note: we rely on the fact that data.m_jacobians are
    // always initialized to zero by the Constraint ctor

	unsigned int offset = 6 + (body->isMultiDof() ? body->getLink(link).m_dofOffset : link);

	// row 0: the lower bound
	jacobianA(0)[offset] = 1;
	// row 1: the upper bound
	//jacobianA(1)[offset] = -1;

	jacobianB(1)[offset] = -1;
}
btMultiBodyJointLimitConstraint::~btMultiBodyJointLimitConstraint()
{
}

int btMultiBodyJointLimitConstraint::getIslandIdA() const
{
	if(m_bodyA)
	{
		btMultiBodyLinkCollider* col = m_bodyA->getBaseCollider();
		if (col)
			return col->getIslandTag();
		for (int i=0;i<m_bodyA->getNumLinks();i++)
		{
			if (m_bodyA->getLink(i).m_collider)
				return m_bodyA->getLink(i).m_collider->getIslandTag();
		}
	}
	return -1;
}

int btMultiBodyJointLimitConstraint::getIslandIdB() const
{
	if(m_bodyB)
	{
		btMultiBodyLinkCollider* col = m_bodyB->getBaseCollider();
		if (col)
			return col->getIslandTag();

		for (int i=0;i<m_bodyB->getNumLinks();i++)
		{
			col = m_bodyB->getLink(i).m_collider;
			if (col)
				return col->getIslandTag();
		}
	}
	return -1;
}


void btMultiBodyJointLimitConstraint::createConstraintRows(btMultiBodyConstraintArray& constraintRows,
		btMultiBodyJacobianData& data,
		const btContactSolverInfo& infoGlobal)
{
    // only positions need to be updated -- data.m_jacobians and force
    // directions were set in the ctor and never change.
    
    // row 0: the lower bound
    setPosition(0, m_bodyA->getJointPos(m_linkA) - m_lowerBound);			//multidof: this is joint-type dependent

    // row 1: the upper bound
    setPosition(1, m_upperBound - m_bodyA->getJointPos(m_linkA));

	for (int row=0;row<getNumRows();row++)
	{
		btMultiBodySolverConstraint& constraintRow = constraintRows.expandNonInitializing();
		constraintRow.m_multiBodyA = m_bodyA;
		constraintRow.m_multiBodyB = m_bodyB;
		const btScalar posError = 0;						//why assume it's zero?
		const btVector3 dummy(0, 0, 0);
		
		btScalar rel_vel = fillMultiBodyConstraint(constraintRow,data,jacobianA(row),jacobianB(row),dummy,dummy,dummy,posError,infoGlobal,0,m_maxAppliedImpulse);
		{
			btScalar penetration = getPosition(row);
			btScalar positionalError = 0.f;
			btScalar	velocityError =  - rel_vel;// * damping;
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

			btScalar  penetrationImpulse = positionalError*constraintRow.m_jacDiagABInv;
			btScalar velocityImpulse = velocityError *constraintRow.m_jacDiagABInv;
			if (!infoGlobal.m_splitImpulse || (penetration > infoGlobal.m_splitImpulsePenetrationThreshold))
			{
				//combine position and velocity into rhs
				constraintRow.m_rhs = penetrationImpulse+velocityImpulse;
				constraintRow.m_rhsPenetration = 0.f;

			} else
			{
				//split position and velocity into rhs and m_rhsPenetration
				constraintRow.m_rhs = velocityImpulse;
				constraintRow.m_rhsPenetration = penetrationImpulse;
			}
		}
	}

}
	
	
	

