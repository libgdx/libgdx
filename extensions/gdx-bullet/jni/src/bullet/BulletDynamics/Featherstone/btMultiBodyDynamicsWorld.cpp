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

#include "btMultiBodyDynamicsWorld.h"
#include "btMultiBodyConstraintSolver.h"
#include "btMultiBody.h"
#include "btMultiBodyLinkCollider.h"
#include "BulletCollision/CollisionDispatch/btSimulationIslandManager.h"
#include "LinearMath/btQuickprof.h"
#include "btMultiBodyConstraint.h"

	


void	btMultiBodyDynamicsWorld::addMultiBody(btMultiBody* body, short group, short mask)
{
	m_multiBodies.push_back(body);

}

void	btMultiBodyDynamicsWorld::removeMultiBody(btMultiBody* body)
{
	m_multiBodies.remove(body);
}

void	btMultiBodyDynamicsWorld::calculateSimulationIslands()
{
	BT_PROFILE("calculateSimulationIslands");

	getSimulationIslandManager()->updateActivationState(getCollisionWorld(),getCollisionWorld()->getDispatcher());

    {
        //merge islands based on speculative contact manifolds too
        for (int i=0;i<this->m_predictiveManifolds.size();i++)
        {
            btPersistentManifold* manifold = m_predictiveManifolds[i];
            
            const btCollisionObject* colObj0 = manifold->getBody0();
            const btCollisionObject* colObj1 = manifold->getBody1();
            
            if (((colObj0) && (!(colObj0)->isStaticOrKinematicObject())) &&
                ((colObj1) && (!(colObj1)->isStaticOrKinematicObject())))
            {
				getSimulationIslandManager()->getUnionFind().unite((colObj0)->getIslandTag(),(colObj1)->getIslandTag());
            }
        }
    }
    
	{
		int i;
		int numConstraints = int(m_constraints.size());
		for (i=0;i< numConstraints ; i++ )
		{
			btTypedConstraint* constraint = m_constraints[i];
			if (constraint->isEnabled())
			{
				const btRigidBody* colObj0 = &constraint->getRigidBodyA();
				const btRigidBody* colObj1 = &constraint->getRigidBodyB();

				if (((colObj0) && (!(colObj0)->isStaticOrKinematicObject())) &&
					((colObj1) && (!(colObj1)->isStaticOrKinematicObject())))
				{
					getSimulationIslandManager()->getUnionFind().unite((colObj0)->getIslandTag(),(colObj1)->getIslandTag());
				}
			}
		}
	}

	//merge islands linked by Featherstone link colliders
	for (int i=0;i<m_multiBodies.size();i++)
	{
		btMultiBody* body = m_multiBodies[i];
		{
			btMultiBodyLinkCollider* prev = body->getBaseCollider();

			for (int b=0;b<body->getNumLinks();b++)
			{
				btMultiBodyLinkCollider* cur = body->getLink(b).m_collider;
				
				if (((cur) && (!(cur)->isStaticOrKinematicObject())) &&
					((prev) && (!(prev)->isStaticOrKinematicObject())))
				{
					int tagPrev = prev->getIslandTag();
					int tagCur = cur->getIslandTag();
					getSimulationIslandManager()->getUnionFind().unite(tagPrev, tagCur);
				}
				if (cur && !cur->isStaticOrKinematicObject())
					prev = cur;
				
			}
		}
	}

	//merge islands linked by multibody constraints
	{
		for (int i=0;i<this->m_multiBodyConstraints.size();i++)
		{
			btMultiBodyConstraint* c = m_multiBodyConstraints[i];
			int tagA = c->getIslandIdA();
			int tagB = c->getIslandIdB();
			if (tagA>=0 && tagB>=0)
				getSimulationIslandManager()->getUnionFind().unite(tagA, tagB);
		}
	}

	//Store the island id in each body
	getSimulationIslandManager()->storeIslandActivationState(getCollisionWorld());

}


void	btMultiBodyDynamicsWorld::updateActivationState(btScalar timeStep)
{
	BT_PROFILE("btMultiBodyDynamicsWorld::updateActivationState");

	
	
	for ( int i=0;i<m_multiBodies.size();i++)
	{
		btMultiBody* body = m_multiBodies[i];
		if (body)
		{
			body->checkMotionAndSleepIfRequired(timeStep);
			if (!body->isAwake())
			{
				btMultiBodyLinkCollider* col = body->getBaseCollider();
				if (col && col->getActivationState() == ACTIVE_TAG)
				{
					col->setActivationState( WANTS_DEACTIVATION);
					col->setDeactivationTime(0.f);
				}
				for (int b=0;b<body->getNumLinks();b++)
				{
					btMultiBodyLinkCollider* col = body->getLink(b).m_collider;
					if (col && col->getActivationState() == ACTIVE_TAG)
					{
						col->setActivationState( WANTS_DEACTIVATION);
						col->setDeactivationTime(0.f);
					}
				}
			} else
			{
				btMultiBodyLinkCollider* col = body->getBaseCollider();
				if (col && col->getActivationState() != DISABLE_DEACTIVATION)
					col->setActivationState( ACTIVE_TAG );

				for (int b=0;b<body->getNumLinks();b++)
				{
					btMultiBodyLinkCollider* col = body->getLink(b).m_collider;
					if (col && col->getActivationState() != DISABLE_DEACTIVATION)
						col->setActivationState( ACTIVE_TAG );
				}
			}

		}
	}

	btDiscreteDynamicsWorld::updateActivationState(timeStep);
}


SIMD_FORCE_INLINE	int	btGetConstraintIslandId2(const btTypedConstraint* lhs)
{
	int islandId;
	
	const btCollisionObject& rcolObj0 = lhs->getRigidBodyA();
	const btCollisionObject& rcolObj1 = lhs->getRigidBodyB();
	islandId= rcolObj0.getIslandTag()>=0?rcolObj0.getIslandTag():rcolObj1.getIslandTag();
	return islandId;

}


class btSortConstraintOnIslandPredicate2
{
	public:

		bool operator() ( const btTypedConstraint* lhs, const btTypedConstraint* rhs ) const
		{
			int rIslandId0,lIslandId0;
			rIslandId0 = btGetConstraintIslandId2(rhs);
			lIslandId0 = btGetConstraintIslandId2(lhs);
			return lIslandId0 < rIslandId0;
		}
};



SIMD_FORCE_INLINE	int	btGetMultiBodyConstraintIslandId(const btMultiBodyConstraint* lhs)
{
	int islandId;
	
	int islandTagA = lhs->getIslandIdA();
	int islandTagB = lhs->getIslandIdB();
	islandId= islandTagA>=0?islandTagA:islandTagB;
	return islandId;

}


class btSortMultiBodyConstraintOnIslandPredicate
{
	public:

		bool operator() ( const btMultiBodyConstraint* lhs, const btMultiBodyConstraint* rhs ) const
		{
			int rIslandId0,lIslandId0;
			rIslandId0 = btGetMultiBodyConstraintIslandId(rhs);
			lIslandId0 = btGetMultiBodyConstraintIslandId(lhs);
			return lIslandId0 < rIslandId0;
		}
};

struct MultiBodyInplaceSolverIslandCallback : public btSimulationIslandManager::IslandCallback
{
	btContactSolverInfo*	m_solverInfo;
	btMultiBodyConstraintSolver*		m_solver;
	btMultiBodyConstraint**		m_multiBodySortedConstraints;
	int							m_numMultiBodyConstraints;
	
	btTypedConstraint**		m_sortedConstraints;
	int						m_numConstraints;
	btIDebugDraw*			m_debugDrawer;
	btDispatcher*			m_dispatcher;
	
	btAlignedObjectArray<btCollisionObject*> m_bodies;
	btAlignedObjectArray<btPersistentManifold*> m_manifolds;
	btAlignedObjectArray<btTypedConstraint*> m_constraints;
	btAlignedObjectArray<btMultiBodyConstraint*> m_multiBodyConstraints;


	MultiBodyInplaceSolverIslandCallback(	btMultiBodyConstraintSolver*	solver,
									btDispatcher* dispatcher)
		:m_solverInfo(NULL),
		m_solver(solver),
		m_multiBodySortedConstraints(NULL),
		m_numConstraints(0),
		m_debugDrawer(NULL),
		m_dispatcher(dispatcher)
	{

	}

	MultiBodyInplaceSolverIslandCallback& operator=(MultiBodyInplaceSolverIslandCallback& other)
	{
		btAssert(0);
		(void)other;
		return *this;
	}

	SIMD_FORCE_INLINE void setup ( btContactSolverInfo* solverInfo, btTypedConstraint** sortedConstraints, int numConstraints, btMultiBodyConstraint** sortedMultiBodyConstraints,	int	numMultiBodyConstraints,	btIDebugDraw* debugDrawer)
	{
		btAssert(solverInfo);
		m_solverInfo = solverInfo;

		m_multiBodySortedConstraints = sortedMultiBodyConstraints;
		m_numMultiBodyConstraints = numMultiBodyConstraints;
		m_sortedConstraints = sortedConstraints;
		m_numConstraints = numConstraints;

		m_debugDrawer = debugDrawer;
		m_bodies.resize (0);
		m_manifolds.resize (0);
		m_constraints.resize (0);
		m_multiBodyConstraints.resize(0);
	}

	
	virtual	void	processIsland(btCollisionObject** bodies,int numBodies,btPersistentManifold**	manifolds,int numManifolds, int islandId)
	{
		if (islandId<0)
		{
			///we don't split islands, so all constraints/contact manifolds/bodies are passed into the solver regardless the island id
			m_solver->solveMultiBodyGroup( bodies,numBodies,manifolds, numManifolds,m_sortedConstraints, m_numConstraints, &m_multiBodySortedConstraints[0],m_numConstraints,*m_solverInfo,m_debugDrawer,m_dispatcher);
		} else
		{
				//also add all non-contact constraints/joints for this island
			btTypedConstraint** startConstraint = 0;
			btMultiBodyConstraint** startMultiBodyConstraint = 0;

			int numCurConstraints = 0;
			int numCurMultiBodyConstraints = 0;

			int i;
			
			//find the first constraint for this island

			for (i=0;i<m_numConstraints;i++)
			{
				if (btGetConstraintIslandId2(m_sortedConstraints[i]) == islandId)
				{
					startConstraint = &m_sortedConstraints[i];
					break;
				}
			}
			//count the number of constraints in this island
			for (;i<m_numConstraints;i++)
			{
				if (btGetConstraintIslandId2(m_sortedConstraints[i]) == islandId)
				{
					numCurConstraints++;
				}
			}

			for (i=0;i<m_numMultiBodyConstraints;i++)
			{
				if (btGetMultiBodyConstraintIslandId(m_multiBodySortedConstraints[i]) == islandId)
				{
					
					startMultiBodyConstraint = &m_multiBodySortedConstraints[i];
					break;
				}
			}
			//count the number of multi body constraints in this island
			for (;i<m_numMultiBodyConstraints;i++)
			{
				if (btGetMultiBodyConstraintIslandId(m_multiBodySortedConstraints[i]) == islandId)
				{
					numCurMultiBodyConstraints++;
				}
			}

			if (m_solverInfo->m_minimumSolverBatchSize<=1)
			{
				m_solver->solveGroup( bodies,numBodies,manifolds, numManifolds,startConstraint,numCurConstraints,*m_solverInfo,m_debugDrawer,m_dispatcher);
			} else
			{
				
				for (i=0;i<numBodies;i++)
					m_bodies.push_back(bodies[i]);
				for (i=0;i<numManifolds;i++)
					m_manifolds.push_back(manifolds[i]);
				for (i=0;i<numCurConstraints;i++)
					m_constraints.push_back(startConstraint[i]);
				
				for (i=0;i<numCurMultiBodyConstraints;i++)
					m_multiBodyConstraints.push_back(startMultiBodyConstraint[i]);
				
				if ((m_constraints.size()+m_manifolds.size())>m_solverInfo->m_minimumSolverBatchSize)
				{
					processConstraints();
				} else
				{
					//printf("deferred\n");
				}
			}
		}
	}
	void	processConstraints()
	{

		btCollisionObject** bodies = m_bodies.size()? &m_bodies[0]:0;
		btPersistentManifold** manifold = m_manifolds.size()?&m_manifolds[0]:0;
		btTypedConstraint** constraints = m_constraints.size()?&m_constraints[0]:0;
		btMultiBodyConstraint** multiBodyConstraints = m_multiBodyConstraints.size() ? &m_multiBodyConstraints[0] : 0;
	
		m_solver->solveMultiBodyGroup( bodies,m_bodies.size(),manifold, m_manifolds.size(),constraints, m_constraints.size() ,multiBodyConstraints, m_multiBodyConstraints.size(), *m_solverInfo,m_debugDrawer,m_dispatcher);
		m_bodies.resize(0);
		m_manifolds.resize(0);
		m_constraints.resize(0);
		m_multiBodyConstraints.resize(0);
	}

};



btMultiBodyDynamicsWorld::btMultiBodyDynamicsWorld(btDispatcher* dispatcher,btBroadphaseInterface* pairCache,btMultiBodyConstraintSolver* constraintSolver,btCollisionConfiguration* collisionConfiguration)
	:btDiscreteDynamicsWorld(dispatcher,pairCache,constraintSolver,collisionConfiguration),
	m_multiBodyConstraintSolver(constraintSolver)
{
	//split impulse is not yet supported for Featherstone hierarchies
	getSolverInfo().m_splitImpulse = false;
	getSolverInfo().m_solverMode |=SOLVER_USE_2_FRICTION_DIRECTIONS;
	m_solverMultiBodyIslandCallback = new MultiBodyInplaceSolverIslandCallback(constraintSolver,dispatcher);
}

btMultiBodyDynamicsWorld::~btMultiBodyDynamicsWorld ()
{
	delete m_solverMultiBodyIslandCallback;
}




void	btMultiBodyDynamicsWorld::solveConstraints(btContactSolverInfo& solverInfo)
{

	btAlignedObjectArray<btScalar> scratch_r;
	btAlignedObjectArray<btVector3> scratch_v;
	btAlignedObjectArray<btMatrix3x3> scratch_m;


	BT_PROFILE("solveConstraints");
	
	m_sortedConstraints.resize( m_constraints.size());
	int i; 
	for (i=0;i<getNumConstraints();i++)
	{
		m_sortedConstraints[i] = m_constraints[i];
	}
	m_sortedConstraints.quickSort(btSortConstraintOnIslandPredicate2());
	btTypedConstraint** constraintsPtr = getNumConstraints() ? &m_sortedConstraints[0] : 0;

	m_sortedMultiBodyConstraints.resize(m_multiBodyConstraints.size());
	for (i=0;i<m_multiBodyConstraints.size();i++)
	{
		m_sortedMultiBodyConstraints[i] = m_multiBodyConstraints[i];
	}
	m_sortedMultiBodyConstraints.quickSort(btSortMultiBodyConstraintOnIslandPredicate());

	btMultiBodyConstraint** sortedMultiBodyConstraints = m_sortedMultiBodyConstraints.size() ?  &m_sortedMultiBodyConstraints[0] : 0;
	

	m_solverMultiBodyIslandCallback->setup(&solverInfo,constraintsPtr,m_sortedConstraints.size(),sortedMultiBodyConstraints,m_sortedMultiBodyConstraints.size(), getDebugDrawer());
	m_constraintSolver->prepareSolve(getCollisionWorld()->getNumCollisionObjects(), getCollisionWorld()->getDispatcher()->getNumManifolds());
	
	/// solve all the constraints for this island
	m_islandManager->buildAndProcessIslands(getCollisionWorld()->getDispatcher(),getCollisionWorld(),m_solverMultiBodyIslandCallback);


	{
		BT_PROFILE("btMultiBody addForce and stepVelocities");
		for (int i=0;i<this->m_multiBodies.size();i++)
		{
			btMultiBody* bod = m_multiBodies[i];

			bool isSleeping = false;
			
			if (bod->getBaseCollider() && bod->getBaseCollider()->getActivationState() == ISLAND_SLEEPING)
			{
				isSleeping = true;
			} 
			for (int b=0;b<bod->getNumLinks();b++)
			{
				if (bod->getLink(b).m_collider && bod->getLink(b).m_collider->getActivationState()==ISLAND_SLEEPING)
					isSleeping = true;
			} 

			if (!isSleeping)
			{
				scratch_r.resize(bod->getNumLinks()+1);
				scratch_v.resize(bod->getNumLinks()+1);
				scratch_m.resize(bod->getNumLinks()+1);

				bod->clearForcesAndTorques();
				bod->addBaseForce(m_gravity * bod->getBaseMass());

				for (int j = 0; j < bod->getNumLinks(); ++j) 
				{
					bod->addLinkForce(j, m_gravity * bod->getLinkMass(j));
				}

				bod->stepVelocities(solverInfo.m_timeStep, scratch_r, scratch_v, scratch_m);
			}
		}
	}

	m_solverMultiBodyIslandCallback->processConstraints();
	
	m_constraintSolver->allSolved(solverInfo, m_debugDrawer);

}

void	btMultiBodyDynamicsWorld::integrateTransforms(btScalar timeStep)
{
	btDiscreteDynamicsWorld::integrateTransforms(timeStep);

	{
		BT_PROFILE("btMultiBody stepPositions");
		//integrate and update the Featherstone hierarchies
		btAlignedObjectArray<btQuaternion> world_to_local;
		btAlignedObjectArray<btVector3> local_origin;

		for (int b=0;b<m_multiBodies.size();b++)
		{
			btMultiBody* bod = m_multiBodies[b];
			bool isSleeping = false;
			if (bod->getBaseCollider() && bod->getBaseCollider()->getActivationState() == ISLAND_SLEEPING)
			{
				isSleeping = true;
			} 
			for (int b=0;b<bod->getNumLinks();b++)
			{
				if (bod->getLink(b).m_collider && bod->getLink(b).m_collider->getActivationState()==ISLAND_SLEEPING)
					isSleeping = true;
			}


			if (!isSleeping)
			{
				int nLinks = bod->getNumLinks();

				///base + num links
				world_to_local.resize(nLinks+1);
				local_origin.resize(nLinks+1);

				bod->stepPositions(timeStep);

	 

				world_to_local[0] = bod->getWorldToBaseRot();
				local_origin[0] = bod->getBasePos();

				if (bod->getBaseCollider())
				{
					btVector3 posr = local_origin[0];
					float pos[4]={posr.x(),posr.y(),posr.z(),1};
					float quat[4]={-world_to_local[0].x(),-world_to_local[0].y(),-world_to_local[0].z(),world_to_local[0].w()};
					btTransform tr;
					tr.setIdentity();
					tr.setOrigin(posr);
					tr.setRotation(btQuaternion(quat[0],quat[1],quat[2],quat[3]));

					bod->getBaseCollider()->setWorldTransform(tr);

				}
      
				for (int k=0;k<bod->getNumLinks();k++)
				{
					const int parent = bod->getParent(k);
					world_to_local[k+1] = bod->getParentToLocalRot(k) * world_to_local[parent+1];
					local_origin[k+1] = local_origin[parent+1] + (quatRotate(world_to_local[k+1].inverse() , bod->getRVector(k)));
				}


				for (int m=0;m<bod->getNumLinks();m++)
				{
					btMultiBodyLinkCollider* col = bod->getLink(m).m_collider;
					if (col)
					{
						int link = col->m_link;
						btAssert(link == m);

						int index = link+1;

						btVector3 posr = local_origin[index];
						float pos[4]={posr.x(),posr.y(),posr.z(),1};
						float quat[4]={-world_to_local[index].x(),-world_to_local[index].y(),-world_to_local[index].z(),world_to_local[index].w()};
						btTransform tr;
						tr.setIdentity();
						tr.setOrigin(posr);
						tr.setRotation(btQuaternion(quat[0],quat[1],quat[2],quat[3]));

						col->setWorldTransform(tr);
					}
				}
			} else
			{
				bod->clearVelocities();
			}
		}
	}
}



void	btMultiBodyDynamicsWorld::addMultiBodyConstraint( btMultiBodyConstraint* constraint)
{
	m_multiBodyConstraints.push_back(constraint);
}

void	btMultiBodyDynamicsWorld::removeMultiBodyConstraint( btMultiBodyConstraint* constraint)
{
	m_multiBodyConstraints.remove(constraint);
}
