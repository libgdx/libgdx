/*
Bullet Continuous Collision Detection and Physics Library
Copyright (c) 2003-2009 Erwin Coumans  http://bulletphysics.org

This software is provided 'as-is', without any express or implied warranty.
In no event will the authors be held liable for any damages arising from the use of this software.
Permission is granted to anyone to use this software for any purpose, 
including commercial applications, and to alter it and redistribute it freely, 
subject to the following restrictions:

1. The origin of this software must not be misrepresented; you must not claim that you wrote the original software. If you use this software in a product, an acknowledgment in the product documentation would be appreciated but is not required.
2. Altered source versions must be plainly marked as such, and must not be misrepresented as being the original software.
3. This notice may not be removed or altered from any source distribution.
*/


#ifndef BT_DISCRETE_DYNAMICS_WORLD_MT_H
#define BT_DISCRETE_DYNAMICS_WORLD_MT_H

#include "btDiscreteDynamicsWorld.h"

struct InplaceSolverIslandCallbackMt;

///
/// btDiscreteDynamicsWorldMt -- a version of DiscreteDynamicsWorld with some minor changes to support
///                              solving simulation islands on multiple threads.
///
ATTRIBUTE_ALIGNED16(class) btDiscreteDynamicsWorldMt : public btDiscreteDynamicsWorld
{
protected:
    InplaceSolverIslandCallbackMt* m_solverIslandCallbackMt;

    virtual void	solveConstraints(btContactSolverInfo& solverInfo);

public:
	BT_DECLARE_ALIGNED_ALLOCATOR();

	btDiscreteDynamicsWorldMt(btDispatcher* dispatcher,btBroadphaseInterface* pairCache,btConstraintSolver* constraintSolver,btCollisionConfiguration* collisionConfiguration);
	virtual ~btDiscreteDynamicsWorldMt();
};

#endif //BT_DISCRETE_DYNAMICS_WORLD_H
