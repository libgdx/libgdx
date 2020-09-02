/*
Bullet Continuous Collision Detection and Physics Library
Copyright (c) 2003-2006 Erwin Coumans  http://continuousphysics.com/Bullet/

This software is provided 'as-is', without any express or implied warranty.
In no event will the authors be held liable for any damages arising from the use of this software.
Permission is granted to anyone to use this software for any purpose, 
including commercial applications, and to alter it and redistribute it freely, 
subject to the following restrictions:

1. The origin of this software must not be misrepresented; you must not claim that you wrote the original software. If you use this software in a product, an acknowledgment in the product documentation would be appreciated but is not required.
2. Altered source versions must be plainly marked as such, and must not be misrepresented as being the original software.
3. This notice may not be removed or altered from any source distribution.
*/

#ifndef BT_SEQUENTIAL_IMPULSE_CONSTRAINT_SOLVER_H
#define BT_SEQUENTIAL_IMPULSE_CONSTRAINT_SOLVER_H

class btIDebugDraw;
class btPersistentManifold;
class btDispatcher;
class btCollisionObject;
#include "BulletDynamics/ConstraintSolver/btTypedConstraint.h"
#include "BulletDynamics/ConstraintSolver/btContactSolverInfo.h"
#include "BulletDynamics/ConstraintSolver/btSolverBody.h"
#include "BulletDynamics/ConstraintSolver/btSolverConstraint.h"
#include "BulletCollision/NarrowPhaseCollision/btManifoldPoint.h"
#include "BulletDynamics/ConstraintSolver/btConstraintSolver.h"

typedef btSimdScalar(*btSingleConstraintRowSolver)(btSolverBody&, btSolverBody&, const btSolverConstraint&);

///The btSequentialImpulseConstraintSolver is a fast SIMD implementation of the Projected Gauss Seidel (iterative LCP) method.
ATTRIBUTE_ALIGNED16(class) btSequentialImpulseConstraintSolver : public btConstraintSolver
{
protected:
	btAlignedObjectArray<btSolverBody>      m_tmpSolverBodyPool;
	btConstraintArray			m_tmpSolverContactConstraintPool;
	btConstraintArray			m_tmpSolverNonContactConstraintPool;
	btConstraintArray			m_tmpSolverContactFrictionConstraintPool;
	btConstraintArray			m_tmpSolverContactRollingFrictionConstraintPool;

	btAlignedObjectArray<int>	m_orderTmpConstraintPool;
	btAlignedObjectArray<int>	m_orderNonContactConstraintPool;
	btAlignedObjectArray<int>	m_orderFrictionConstraintPool;
	btAlignedObjectArray<btTypedConstraint::btConstraintInfo1> m_tmpConstraintSizesPool;
	int							m_maxOverrideNumSolverIterations;
	int m_fixedBodyId;
    // When running solvers on multiple threads, a race condition exists for Kinematic objects that
    // participate in more than one solver.
    // The getOrInitSolverBody() function writes the companionId of each body (storing the index of the solver body
    // for the current solver). For normal dynamic bodies it isn't an issue because they can only be in one island
    // (and therefore one thread) at a time. But kinematic bodies can be in multiple islands at once.
    // To avoid this race condition, this solver does not write the companionId, instead it stores the solver body
    // index in this solver-local table, indexed by the uniqueId of the body.
    btAlignedObjectArray<int>	m_kinematicBodyUniqueIdToSolverBodyTable;  // only used for multithreading

	btSingleConstraintRowSolver m_resolveSingleConstraintRowGeneric;
	btSingleConstraintRowSolver m_resolveSingleConstraintRowLowerLimit;
    btSingleConstraintRowSolver m_resolveSplitPenetrationImpulse;
    int m_cachedSolverMode;  // used to check if SOLVER_SIMD flag has been changed
    void setupSolverFunctions( bool useSimd );

	btScalar	m_leastSquaresResidual;

	void setupFrictionConstraint(	btSolverConstraint& solverConstraint, const btVector3& normalAxis,int solverBodyIdA,int  solverBodyIdB,
									btManifoldPoint& cp,const btVector3& rel_pos1,const btVector3& rel_pos2,
									btCollisionObject* colObj0,btCollisionObject* colObj1, btScalar relaxation, 
									const btContactSolverInfo& infoGlobal,
									btScalar desiredVelocity=0., btScalar cfmSlip=0.);

	void setupTorsionalFrictionConstraint(	btSolverConstraint& solverConstraint, const btVector3& normalAxis,int solverBodyIdA,int  solverBodyIdB,
									btManifoldPoint& cp,btScalar combinedTorsionalFriction, const btVector3& rel_pos1,const btVector3& rel_pos2,
									btCollisionObject* colObj0,btCollisionObject* colObj1, btScalar relaxation, 
									btScalar desiredVelocity=0., btScalar cfmSlip=0.);

	btSolverConstraint&	addFrictionConstraint(const btVector3& normalAxis,int solverBodyIdA,int solverBodyIdB,int frictionIndex,btManifoldPoint& cp,const btVector3& rel_pos1,const btVector3& rel_pos2,btCollisionObject* colObj0,btCollisionObject* colObj1, btScalar relaxation, const btContactSolverInfo& infoGlobal, btScalar desiredVelocity=0., btScalar cfmSlip=0.);
	btSolverConstraint&	addTorsionalFrictionConstraint(const btVector3& normalAxis,int solverBodyIdA,int solverBodyIdB,int frictionIndex,btManifoldPoint& cp,btScalar torsionalFriction, const btVector3& rel_pos1,const btVector3& rel_pos2,btCollisionObject* colObj0,btCollisionObject* colObj1, btScalar relaxation, btScalar desiredVelocity=0, btScalar cfmSlip=0.f);

	
	void setupContactConstraint(btSolverConstraint& solverConstraint, int solverBodyIdA, int solverBodyIdB, btManifoldPoint& cp, 
								const btContactSolverInfo& infoGlobal,btScalar& relaxation, const btVector3& rel_pos1, const btVector3& rel_pos2);

	static void	applyAnisotropicFriction(btCollisionObject* colObj,btVector3& frictionDirection, int frictionMode);

	void setFrictionConstraintImpulse( btSolverConstraint& solverConstraint, int solverBodyIdA,int solverBodyIdB, 
										 btManifoldPoint& cp, const btContactSolverInfo& infoGlobal);

	///m_btSeed2 is used for re-arranging the constraint rows. improves convergence/quality of friction
	unsigned long	m_btSeed2;

	
	btScalar restitutionCurve(btScalar rel_vel, btScalar restitution, btScalar velocityThreshold);

	virtual void convertContacts(btPersistentManifold** manifoldPtr, int numManifolds, const btContactSolverInfo& infoGlobal);

	void	convertContact(btPersistentManifold* manifold,const btContactSolverInfo& infoGlobal);


	btSimdScalar	resolveSplitPenetrationSIMD(btSolverBody& bodyA,btSolverBody& bodyB, const btSolverConstraint& contactConstraint)
    {
        return m_resolveSplitPenetrationImpulse( bodyA, bodyB, contactConstraint );
    }

	btSimdScalar	resolveSplitPenetrationImpulseCacheFriendly(btSolverBody& bodyA,btSolverBody& bodyB, const btSolverConstraint& contactConstraint)
    {
        return m_resolveSplitPenetrationImpulse( bodyA, bodyB, contactConstraint );
    }

	//internal method
	int		getOrInitSolverBody(btCollisionObject& body,btScalar timeStep);
	void	initSolverBody(btSolverBody* solverBody, btCollisionObject* collisionObject, btScalar timeStep);

	btSimdScalar	resolveSingleConstraintRowGeneric(btSolverBody& bodyA,btSolverBody& bodyB,const btSolverConstraint& contactConstraint);
	btSimdScalar	resolveSingleConstraintRowGenericSIMD(btSolverBody& bodyA,btSolverBody& bodyB,const btSolverConstraint& contactConstraint);
	btSimdScalar	resolveSingleConstraintRowLowerLimit(btSolverBody& bodyA,btSolverBody& bodyB,const btSolverConstraint& contactConstraint);
	btSimdScalar	resolveSingleConstraintRowLowerLimitSIMD(btSolverBody& bodyA,btSolverBody& bodyB,const btSolverConstraint& contactConstraint);
	btSimdScalar	resolveSplitPenetrationImpulse(btSolverBody& bodyA,btSolverBody& bodyB, const btSolverConstraint& contactConstraint)
    {
        return m_resolveSplitPenetrationImpulse( bodyA, bodyB, contactConstraint );
    }
		
protected:
	
	
	virtual void solveGroupCacheFriendlySplitImpulseIterations(btCollisionObject** bodies,int numBodies,btPersistentManifold** manifoldPtr, int numManifolds,btTypedConstraint** constraints,int numConstraints,const btContactSolverInfo& infoGlobal,btIDebugDraw* debugDrawer);
	virtual btScalar solveGroupCacheFriendlyFinish(btCollisionObject** bodies,int numBodies,const btContactSolverInfo& infoGlobal);
	virtual btScalar solveSingleIteration(int iteration, btCollisionObject** bodies ,int numBodies,btPersistentManifold** manifoldPtr, int numManifolds,btTypedConstraint** constraints,int numConstraints,const btContactSolverInfo& infoGlobal,btIDebugDraw* debugDrawer);

	virtual btScalar solveGroupCacheFriendlySetup(btCollisionObject** bodies,int numBodies,btPersistentManifold** manifoldPtr, int numManifolds,btTypedConstraint** constraints,int numConstraints,const btContactSolverInfo& infoGlobal,btIDebugDraw* debugDrawer);
	virtual btScalar solveGroupCacheFriendlyIterations(btCollisionObject** bodies,int numBodies,btPersistentManifold** manifoldPtr, int numManifolds,btTypedConstraint** constraints,int numConstraints,const btContactSolverInfo& infoGlobal,btIDebugDraw* debugDrawer);


public:

	BT_DECLARE_ALIGNED_ALLOCATOR();
	
	btSequentialImpulseConstraintSolver();
	virtual ~btSequentialImpulseConstraintSolver();

	virtual btScalar solveGroup(btCollisionObject** bodies,int numBodies,btPersistentManifold** manifold,int numManifolds,btTypedConstraint** constraints,int numConstraints,const btContactSolverInfo& info, btIDebugDraw* debugDrawer,btDispatcher* dispatcher);
		
	///clear internal cached data and reset random seed
	virtual	void	reset();
	
	unsigned long btRand2();

	int btRandInt2 (int n);

	void	setRandSeed(unsigned long seed)
	{
		m_btSeed2 = seed;
	}
	unsigned long	getRandSeed() const
	{
		return m_btSeed2;
	}

	
	virtual btConstraintSolverType	getSolverType() const
	{
		return BT_SEQUENTIAL_IMPULSE_SOLVER;
	}

	btSingleConstraintRowSolver	getActiveConstraintRowSolverGeneric()
	{
		return m_resolveSingleConstraintRowGeneric;
	}
	void setConstraintRowSolverGeneric(btSingleConstraintRowSolver rowSolver)
	{
		m_resolveSingleConstraintRowGeneric = rowSolver;
	}
	btSingleConstraintRowSolver	getActiveConstraintRowSolverLowerLimit()
	{
		return m_resolveSingleConstraintRowLowerLimit;
	}
	void setConstraintRowSolverLowerLimit(btSingleConstraintRowSolver rowSolver)
	{
		m_resolveSingleConstraintRowLowerLimit = rowSolver;
	}

	///Various implementations of solving a single constraint row using a generic equality constraint, using scalar reference, SSE2 or SSE4
	btSingleConstraintRowSolver	getScalarConstraintRowSolverGeneric();
	btSingleConstraintRowSolver	getSSE2ConstraintRowSolverGeneric();
	btSingleConstraintRowSolver	getSSE4_1ConstraintRowSolverGeneric();

	///Various implementations of solving a single constraint row using an inequality (lower limit) constraint, using scalar reference, SSE2 or SSE4
	btSingleConstraintRowSolver	getScalarConstraintRowSolverLowerLimit();
	btSingleConstraintRowSolver	getSSE2ConstraintRowSolverLowerLimit();
	btSingleConstraintRowSolver	getSSE4_1ConstraintRowSolverLowerLimit();
};




#endif //BT_SEQUENTIAL_IMPULSE_CONSTRAINT_SOLVER_H

