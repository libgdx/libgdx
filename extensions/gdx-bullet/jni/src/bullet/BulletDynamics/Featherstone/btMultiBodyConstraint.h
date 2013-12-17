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

#ifndef BT_MULTIBODY_CONSTRAINT_H
#define BT_MULTIBODY_CONSTRAINT_H

#include "LinearMath/btScalar.h"
#include "LinearMath/btAlignedObjectArray.h"
#include "btMultiBody.h"

class btMultiBody;
struct btSolverInfo;

#include "btMultiBodySolverConstraint.h"

struct btMultiBodyJacobianData
{
	btAlignedObjectArray<btScalar>		m_jacobians;
	btAlignedObjectArray<btScalar>		m_deltaVelocitiesUnitImpulse;
	btAlignedObjectArray<btScalar>		m_deltaVelocities;
	btAlignedObjectArray<btScalar>		scratch_r;
	btAlignedObjectArray<btVector3>		scratch_v;
	btAlignedObjectArray<btMatrix3x3>	scratch_m;
	btAlignedObjectArray<btSolverBody>*	m_solverBodyPool;
	int									m_fixedBodyId;

};


class btMultiBodyConstraint
{
protected:

	btMultiBody*	m_bodyA;
    btMultiBody*	m_bodyB;
    int				m_linkA;
    int				m_linkB;

    int				m_num_rows;
    int				m_jac_size_A;
    int				m_jac_size_both;
    int				m_pos_offset;

	bool			m_isUnilateral;

	btScalar		m_maxAppliedImpulse;


    // data block laid out as follows:
    // cached impulses. (one per row.)
    // jacobians. (interleaved, row1 body1 then row1 body2 then row2 body 1 etc)
    // positions. (one per row.)
    btAlignedObjectArray<btScalar> m_data;

	void	applyDeltaVee(btMultiBodyJacobianData& data, btScalar* delta_vee, btScalar impulse, int velocityIndex, int ndof);

	void fillMultiBodyConstraintMixed(btMultiBodySolverConstraint& solverConstraint, 
																	btMultiBodyJacobianData& data,
																 const btVector3& contactNormalOnB,
																 const btVector3& posAworld, const btVector3& posBworld, 
																 btScalar position,
																 const btContactSolverInfo& infoGlobal,
																 btScalar& relaxation,
																 bool isFriction, btScalar desiredVelocity=0, btScalar cfmSlip=0);

		btScalar fillConstraintRowMultiBodyMultiBody(btMultiBodySolverConstraint& constraintRow,
														btMultiBodyJacobianData& data,
														btScalar* jacOrgA,btScalar* jacOrgB,
														const btContactSolverInfo& infoGlobal,
														btScalar desiredVelocity,
														btScalar lowerLimit,
														btScalar upperLimit);

public:

	btMultiBodyConstraint(btMultiBody* bodyA,btMultiBody* bodyB,int linkA, int linkB, int numRows, bool isUnilateral);
	virtual ~btMultiBodyConstraint();



	virtual int getIslandIdA() const =0;
	virtual int getIslandIdB() const =0;
	
	virtual void createConstraintRows(btMultiBodyConstraintArray& constraintRows,
		btMultiBodyJacobianData& data,
		const btContactSolverInfo& infoGlobal)=0;

	int	getNumRows() const
	{
		return m_num_rows;
	}

	btMultiBody*	getMultiBodyA()
	{
		return m_bodyA;
	}
    btMultiBody*	getMultiBodyB()
	{
		return m_bodyB;
	}

	// current constraint position
    // constraint is pos >= 0 for unilateral, or pos = 0 for bilateral
    // NOTE: ignored position for friction rows.
    btScalar getPosition(int row) const 
	{ 
		return m_data[m_pos_offset + row]; 
	}

    void setPosition(int row, btScalar pos) 
	{ 
		m_data[m_pos_offset + row] = pos; 
	}

	
	bool isUnilateral() const
	{
		return m_isUnilateral;
	}

	// jacobian blocks.
    // each of size 6 + num_links. (jacobian2 is null if no body2.)
    // format: 3 'omega' coefficients, 3 'v' coefficients, then the 'qdot' coefficients.
    btScalar* jacobianA(int row) 
	{ 
		return &m_data[m_num_rows + row * m_jac_size_both]; 
	}
    const btScalar* jacobianA(int row) const 
	{ 
		return &m_data[m_num_rows + (row * m_jac_size_both)]; 
	}
    btScalar* jacobianB(int row) 
	{ 
		return &m_data[m_num_rows + (row * m_jac_size_both) + m_jac_size_A]; 
	}
    const btScalar* jacobianB(int row) const 
	{ 
		return &m_data[m_num_rows + (row * m_jac_size_both) + m_jac_size_A]; 
	}

	btScalar	getMaxAppliedImpulse() const
	{
		return m_maxAppliedImpulse;
	}
	void	setMaxAppliedImpulse(btScalar maxImp)
	{
		m_maxAppliedImpulse = maxImp;
	}
	

};

#endif //BT_MULTIBODY_CONSTRAINT_H

