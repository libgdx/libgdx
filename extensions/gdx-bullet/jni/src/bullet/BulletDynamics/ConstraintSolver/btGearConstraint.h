/*
Bullet Continuous Collision Detection and Physics Library
Copyright (c) 2012 Advanced Micro Devices, Inc.  http://bulletphysics.org

This software is provided 'as-is', without any express or implied warranty.
In no event will the authors be held liable for any damages arising from the use of this software.
Permission is granted to anyone to use this software for any purpose, 
including commercial applications, and to alter it and redistribute it freely, 
subject to the following restrictions:

1. The origin of this software must not be misrepresented; you must not claim that you wrote the original software. If you use this software in a product, an acknowledgment in the product documentation would be appreciated but is not required.
2. Altered source versions must be plainly marked as such, and must not be misrepresented as being the original software.
3. This notice may not be removed or altered from any source distribution.
*/



#ifndef BT_GEAR_CONSTRAINT_H
#define BT_GEAR_CONSTRAINT_H

#include "BulletDynamics/ConstraintSolver/btTypedConstraint.h"
///The btGeatConstraint will couple the angular velocity for two bodies around given local axis and ratio.
///See Bullet/Demos/ConstraintDemo for an example use.
class btGearConstraint : public btTypedConstraint
{
protected:
	btVector3	m_axisInA;
	btVector3	m_axisInB;
	bool		m_useFrameA;
	btScalar	m_ratio;

public:
	btGearConstraint(btRigidBody& rbA, btRigidBody& rbB, const btVector3& axisInA,const btVector3& axisInB, btScalar ratio=1.f);
	virtual ~btGearConstraint ();

	///internal method used by the constraint solver, don't use them directly
	virtual void getInfo1 (btConstraintInfo1* info);

	///internal method used by the constraint solver, don't use them directly
	virtual void getInfo2 (btConstraintInfo2* info);

	virtual	void	setParam(int num, btScalar value, int axis = -1) 
	{
		btAssert(0);
	};

	///return the local value of parameter
	virtual	btScalar getParam(int num, int axis = -1) const 
	{ 
		btAssert(0);
		return 0.f;
	}

};

#endif //BT_GEAR_CONSTRAINT_H
