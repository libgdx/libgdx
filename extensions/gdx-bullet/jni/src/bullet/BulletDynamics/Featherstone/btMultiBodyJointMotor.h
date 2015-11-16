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

#ifndef BT_MULTIBODY_JOINT_MOTOR_H
#define BT_MULTIBODY_JOINT_MOTOR_H

#include "btMultiBodyConstraint.h"
struct btSolverInfo;

class btMultiBodyJointMotor : public btMultiBodyConstraint
{
protected:


	btScalar	m_desiredVelocity;

public:

	btMultiBodyJointMotor(btMultiBody* body, int link, btScalar desiredVelocity, btScalar maxMotorImpulse);
	btMultiBodyJointMotor(btMultiBody* body, int link, int linkDoF, btScalar desiredVelocity, btScalar maxMotorImpulse);
	virtual ~btMultiBodyJointMotor();
    virtual void finalizeMultiDof();

	virtual int getIslandIdA() const;
	virtual int getIslandIdB() const;

	virtual void createConstraintRows(btMultiBodyConstraintArray& constraintRows,
		btMultiBodyJacobianData& data,
		const btContactSolverInfo& infoGlobal);

    virtual void setVelocityTarget(btScalar velTarget)
    {
        m_desiredVelocity = velTarget;
    }

	virtual void debugDraw(class btIDebugDraw* drawer)
	{
		//todo(erwincoumans)
	}
};

#endif //BT_MULTIBODY_JOINT_MOTOR_H

