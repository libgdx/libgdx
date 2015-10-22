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

#ifndef BT_MULTIBODY_DYNAMICS_WORLD_H
#define BT_MULTIBODY_DYNAMICS_WORLD_H

#include "BulletDynamics/Dynamics/btDiscreteDynamicsWorld.h"


class btMultiBody;
class btMultiBodyConstraint;
class btMultiBodyConstraintSolver;
struct MultiBodyInplaceSolverIslandCallback;

///The btMultiBodyDynamicsWorld adds Featherstone multi body dynamics to Bullet
///This implementation is still preliminary/experimental.
class btMultiBodyDynamicsWorld : public btDiscreteDynamicsWorld
{
protected:
	btAlignedObjectArray<btMultiBody*> m_multiBodies;
	btAlignedObjectArray<btMultiBodyConstraint*> m_multiBodyConstraints;
	btAlignedObjectArray<btMultiBodyConstraint*> m_sortedMultiBodyConstraints;
	btMultiBodyConstraintSolver*	m_multiBodyConstraintSolver;
	MultiBodyInplaceSolverIslandCallback*	m_solverMultiBodyIslandCallback;

	virtual void	calculateSimulationIslands();
	virtual void	updateActivationState(btScalar timeStep);
	virtual void	solveConstraints(btContactSolverInfo& solverInfo);
	
public:

	btMultiBodyDynamicsWorld(btDispatcher* dispatcher,btBroadphaseInterface* pairCache,btMultiBodyConstraintSolver* constraintSolver,btCollisionConfiguration* collisionConfiguration);
	
	virtual ~btMultiBodyDynamicsWorld ();

	virtual void	addMultiBody(btMultiBody* body, short group= btBroadphaseProxy::DefaultFilter, short mask=btBroadphaseProxy::AllFilter);

	virtual void	removeMultiBody(btMultiBody* body);

	virtual void	addMultiBodyConstraint( btMultiBodyConstraint* constraint);

	virtual void	removeMultiBodyConstraint( btMultiBodyConstraint* constraint);

	virtual void	integrateTransforms(btScalar timeStep);

	virtual void	debugDrawWorld();
	
	virtual void	debugDrawMultiBodyConstraint(btMultiBodyConstraint* constraint);
};
#endif //BT_MULTIBODY_DYNAMICS_WORLD_H
