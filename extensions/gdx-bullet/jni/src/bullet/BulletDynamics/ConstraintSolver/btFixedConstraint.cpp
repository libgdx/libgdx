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


#include "btFixedConstraint.h"
#include "BulletDynamics/Dynamics/btRigidBody.h"
#include "LinearMath/btTransformUtil.h"
#include <new>


btFixedConstraint::btFixedConstraint(btRigidBody& rbA,btRigidBody& rbB, const btTransform& frameInA,const btTransform& frameInB)
:btTypedConstraint(FIXED_CONSTRAINT_TYPE,rbA,rbB)
{
	m_pivotInA = frameInA.getOrigin();
	m_pivotInB = frameInB.getOrigin();
	m_relTargetAB = frameInA.getRotation()*frameInB.getRotation().inverse();

}

btFixedConstraint::~btFixedConstraint ()
{
}

	
void btFixedConstraint::getInfo1 (btConstraintInfo1* info)
{
	info->m_numConstraintRows = 6;
	info->nub = 6;
}

void btFixedConstraint::getInfo2 (btConstraintInfo2* info)
{
	//fix the 3 linear degrees of freedom

	
	const btVector3& worldPosA = m_rbA.getCenterOfMassTransform().getOrigin();
	const btMatrix3x3& worldOrnA = m_rbA.getCenterOfMassTransform().getBasis();
	const btVector3& worldPosB= m_rbB.getCenterOfMassTransform().getOrigin();
	const btMatrix3x3& worldOrnB = m_rbB.getCenterOfMassTransform().getBasis();
	

	info->m_J1linearAxis[0] = 1;
	info->m_J1linearAxis[info->rowskip+1] = 1;
	info->m_J1linearAxis[2*info->rowskip+2] = 1;

	btVector3 a1 = worldOrnA*m_pivotInA;
	{
		btVector3* angular0 = (btVector3*)(info->m_J1angularAxis);
		btVector3* angular1 = (btVector3*)(info->m_J1angularAxis+info->rowskip);
		btVector3* angular2 = (btVector3*)(info->m_J1angularAxis+2*info->rowskip);
		btVector3 a1neg = -a1;
		a1neg.getSkewSymmetricMatrix(angular0,angular1,angular2);
	}
    
	if (info->m_J2linearAxis)
	{
		info->m_J2linearAxis[0] = -1;
		info->m_J2linearAxis[info->rowskip+1] = -1;
		info->m_J2linearAxis[2*info->rowskip+2] = -1;
	}
	
	btVector3 a2 = worldOrnB*m_pivotInB;
   
	{
	//	btVector3 a2n = -a2;
		btVector3* angular0 = (btVector3*)(info->m_J2angularAxis);
		btVector3* angular1 = (btVector3*)(info->m_J2angularAxis+info->rowskip);
		btVector3* angular2 = (btVector3*)(info->m_J2angularAxis+2*info->rowskip);
		a2.getSkewSymmetricMatrix(angular0,angular1,angular2);
	}

    // set right hand side for the linear dofs
	btScalar k = info->fps * info->erp;
	
	btVector3 linearError = k*(a2+worldPosB-a1-worldPosA);
    int j;
	for (j=0; j<3; j++)
    {



        info->m_constraintError[j*info->rowskip] = linearError[j];
		//printf("info->m_constraintError[%d]=%f\n",j,info->m_constraintError[j]);
    }

		//fix the 3 angular degrees of freedom

	int start_row = 3;
	int s = info->rowskip;
    int start_index = start_row * s;

    // 3 rows to make body rotations equal
	info->m_J1angularAxis[start_index] = 1;
    info->m_J1angularAxis[start_index + s + 1] = 1;
    info->m_J1angularAxis[start_index + s*2+2] = 1;
    if ( info->m_J2angularAxis)
    {
        info->m_J2angularAxis[start_index] = -1;
        info->m_J2angularAxis[start_index + s+1] = -1;
        info->m_J2angularAxis[start_index + s*2+2] = -1;
    }

    // set right hand side for the angular dofs

	btVector3 diff;
	btScalar angle;
	btMatrix3x3 mrelCur = worldOrnA *worldOrnB.inverse();
	btQuaternion qrelCur;
	mrelCur.getRotation(qrelCur);
	btTransformUtil::calculateDiffAxisAngleQuaternion(m_relTargetAB,qrelCur,diff,angle);
	diff*=-angle;
	for (j=0; j<3; j++)
    {
        info->m_constraintError[(3+j)*info->rowskip] = k * diff[j];
    }

}