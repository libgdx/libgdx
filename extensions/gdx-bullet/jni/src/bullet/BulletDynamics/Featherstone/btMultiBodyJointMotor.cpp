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

#include "btMultiBodyJointMotor.h"
#include "btMultiBody.h"
#include "btMultiBodyLinkCollider.h"
#include "BulletCollision/CollisionDispatch/btCollisionObject.h"


btMultiBodyJointMotor::btMultiBodyJointMotor(btMultiBody* body, int link, btScalar desiredVelocity, btScalar maxMotorImpulse)
	:btMultiBodyConstraint(body,body,link,link,1,true),
	m_desiredVelocity(desiredVelocity)	
{
	m_maxAppliedImpulse = maxMotorImpulse;
	// the data.m_jacobians never change, so may as well
    // initialize them here
        
    // note: we rely on the fact that data.m_jacobians are
    // always initialized to zero by the Constraint ctor

    // row 0: the lower bound
    jacobianA(0)[6 + link] = 1;
}
btMultiBodyJointMotor::~btMultiBodyJointMotor()
{
}

int btMultiBodyJointMotor::getIslandIdA() const
{
	btMultiBodyLinkCollider* col = m_bodyA->getBaseCollider();
	if (col)
		return col->getIslandTag();
	for (int i=0;i<m_bodyA->getNumLinks();i++)
	{
		if (m_bodyA->getLink(i).m_collider)
			return m_bodyA->getLink(i).m_collider->getIslandTag();
	}
	return -1;
}

int btMultiBodyJointMotor::getIslandIdB() const
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
	return -1;
}


void btMultiBodyJointMotor::createConstraintRows(btMultiBodyConstraintArray& constraintRows,
		btMultiBodyJacobianData& data,
		const btContactSolverInfo& infoGlobal)
{
    // only positions need to be updated -- data.m_jacobians and force
    // directions were set in the ctor and never change.
    
  

	for (int row=0;row<getNumRows();row++)
	{
		btMultiBodySolverConstraint& constraintRow = constraintRows.expandNonInitializing();
		
		btScalar penetration = 0;
		fillConstraintRowMultiBodyMultiBody(constraintRow,data,jacobianA(row),jacobianB(row),infoGlobal,m_desiredVelocity,-m_maxAppliedImpulse,m_maxAppliedImpulse);
	}

}
	
