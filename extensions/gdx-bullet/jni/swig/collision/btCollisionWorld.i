/*
 *	Interface module for a class with inner structs or classes.
 */
 
%module(directors="1") btCollisionWorld

%feature("director") LocalShapeInfo;
%feature("director") LocalRayResult;
%feature("director") RayResultCallback;
%feature("director") ClosestRayResultCallback;
%feature("director") AllHitsRayResultCallback;
%feature("director") LocalConvexResult;
%feature("director") ConvexResultCallback;
%feature("director") ClosestConvexResultCallback;
%feature("director") ContactResultCallback;

	///LocalShapeInfo gives extra information for complex shapes
	///Currently, only btTriangleMeshShape is available, so it just contains triangleIndex and subpart
	struct	LocalShapeInfo
	{
		int	m_shapePart;
		int	m_triangleIndex;
		
		//const btCollisionShape*	m_shapeTemp;
		//const btTransform*	m_shapeLocalTransform;
	};

	struct	LocalRayResult
	{
		LocalRayResult(const btCollisionObject*	collisionObject, 
			LocalShapeInfo*	localShapeInfo,
			const btVector3&		hitNormalLocal,
			btScalar hitFraction)
		:m_collisionObject(collisionObject),
		m_localShapeInfo(localShapeInfo),
		m_hitNormalLocal(hitNormalLocal),
		m_hitFraction(hitFraction)
		{
		}

		const btCollisionObject*		m_collisionObject;
		LocalShapeInfo*			m_localShapeInfo;
		btVector3				m_hitNormalLocal;
		btScalar				m_hitFraction;

	};

	///RayResultCallback is used to report new raycast results
	struct	RayResultCallback
	{
		btScalar	m_closestHitFraction;
		const btCollisionObject*		m_collisionObject;
		short int	m_collisionFilterGroup;
		short int	m_collisionFilterMask;
      //@BP Mod - Custom flags, currently used to enable backface culling on tri-meshes, see btRaycastCallback
      unsigned int m_flags;

		virtual ~RayResultCallback()
		{
		}
		bool	hasHit() const
		{
			return (m_collisionObject != 0);
		}

		RayResultCallback()
			:m_closestHitFraction(btScalar(1.)),
			m_collisionObject(0),
			m_collisionFilterGroup(btBroadphaseProxy::DefaultFilter),
			m_collisionFilterMask(btBroadphaseProxy::AllFilter),
         //@BP Mod
         m_flags(0)
		{
		}

		virtual bool needsCollision(btBroadphaseProxy* proxy0) const
		{
			bool collides = (proxy0->m_collisionFilterGroup & m_collisionFilterMask) != 0;
			collides = collides && (m_collisionFilterGroup & proxy0->m_collisionFilterMask);
			return collides;
		}


		virtual	btScalar	addSingleResult(LocalRayResult& rayResult,bool normalInWorldSpace) = 0;
	};

	struct	ClosestRayResultCallback : public RayResultCallback
	{
		ClosestRayResultCallback(const btVector3&	rayFromWorld,const btVector3&	rayToWorld)
		:m_rayFromWorld(rayFromWorld),
		m_rayToWorld(rayToWorld)
		{
		}

		btVector3	m_rayFromWorld;//used to calculate hitPointWorld from hitFraction
		btVector3	m_rayToWorld;

		btVector3	m_hitNormalWorld;
		btVector3	m_hitPointWorld;

		virtual	btScalar	addSingleResult(LocalRayResult& rayResult,bool normalInWorldSpace)
		{
			//caller already does the filter on the m_closestHitFraction
			btAssert(rayResult.m_hitFraction <= m_closestHitFraction);

			m_closestHitFraction = rayResult.m_hitFraction;
			m_collisionObject = rayResult.m_collisionObject;
			if (normalInWorldSpace)
			{
				m_hitNormalWorld = rayResult.m_hitNormalLocal;
			} else
			{
				///need to transform normal into worldspace
				m_hitNormalWorld = m_collisionObject->getWorldTransform().getBasis()*rayResult.m_hitNormalLocal;
			}
			m_hitPointWorld.setInterpolate3(m_rayFromWorld,m_rayToWorld,rayResult.m_hitFraction);
			return rayResult.m_hitFraction;
		}
	};

	struct	AllHitsRayResultCallback : public RayResultCallback
	{
		AllHitsRayResultCallback(const btVector3&	rayFromWorld,const btVector3&	rayToWorld)
		:m_rayFromWorld(rayFromWorld),
		m_rayToWorld(rayToWorld)
		{
		}

		btAlignedObjectArray<const btCollisionObject*>		m_collisionObjects;

		btVector3	m_rayFromWorld;//used to calculate hitPointWorld from hitFraction
		btVector3	m_rayToWorld;

		btAlignedObjectArray<btVector3>	m_hitNormalWorld;
		btAlignedObjectArray<btVector3>	m_hitPointWorld;
		btAlignedObjectArray<btScalar> m_hitFractions;

		virtual	btScalar	addSingleResult(LocalRayResult& rayResult,bool normalInWorldSpace)
		{
			m_collisionObject = rayResult.m_collisionObject;
			m_collisionObjects.push_back(rayResult.m_collisionObject);
			btVector3 hitNormalWorld;
			if (normalInWorldSpace)
			{
				hitNormalWorld = rayResult.m_hitNormalLocal;
			} else
			{
				///need to transform normal into worldspace
				hitNormalWorld = m_collisionObject->getWorldTransform().getBasis()*rayResult.m_hitNormalLocal;
			}
			m_hitNormalWorld.push_back(hitNormalWorld);
			btVector3 hitPointWorld;
			hitPointWorld.setInterpolate3(m_rayFromWorld,m_rayToWorld,rayResult.m_hitFraction);
			m_hitPointWorld.push_back(hitPointWorld);
			m_hitFractions.push_back(rayResult.m_hitFraction);
			return m_closestHitFraction;
		}
	};


	struct LocalConvexResult
	{
		LocalConvexResult(const btCollisionObject*	hitCollisionObject, 
			LocalShapeInfo*	localShapeInfo,
			const btVector3&		hitNormalLocal,
			const btVector3&		hitPointLocal,
			btScalar hitFraction
			)
		:m_hitCollisionObject(hitCollisionObject),
		m_localShapeInfo(localShapeInfo),
		m_hitNormalLocal(hitNormalLocal),
		m_hitPointLocal(hitPointLocal),
		m_hitFraction(hitFraction)
		{
		}

		const btCollisionObject*		m_hitCollisionObject;
		LocalShapeInfo*			m_localShapeInfo;
		btVector3				m_hitNormalLocal;
		btVector3				m_hitPointLocal;
		btScalar				m_hitFraction;
	};

	///RayResultCallback is used to report new raycast results
	struct	ConvexResultCallback
	{
		btScalar	m_closestHitFraction;
		short int	m_collisionFilterGroup;
		short int	m_collisionFilterMask;
		
		ConvexResultCallback()
			:m_closestHitFraction(btScalar(1.)),
			m_collisionFilterGroup(btBroadphaseProxy::DefaultFilter),
			m_collisionFilterMask(btBroadphaseProxy::AllFilter)
		{
		}

		virtual ~ConvexResultCallback()
		{
		}
		
		bool	hasHit() const
		{
			return (m_closestHitFraction < btScalar(1.));
		}

		

		virtual bool needsCollision(btBroadphaseProxy* proxy0) const
		{
			bool collides = (proxy0->m_collisionFilterGroup & m_collisionFilterMask) != 0;
			collides = collides && (m_collisionFilterGroup & proxy0->m_collisionFilterMask);
			return collides;
		}

		virtual	btScalar	addSingleResult(LocalConvexResult& convexResult,bool normalInWorldSpace) = 0;
	};

	struct	ClosestConvexResultCallback : public ConvexResultCallback
	{
		ClosestConvexResultCallback(const btVector3&	convexFromWorld,const btVector3&	convexToWorld)
		:m_convexFromWorld(convexFromWorld),
		m_convexToWorld(convexToWorld),
		m_hitCollisionObject(0)
		{
		}

		btVector3	m_convexFromWorld;//used to calculate hitPointWorld from hitFraction
		btVector3	m_convexToWorld;

		btVector3	m_hitNormalWorld;
		btVector3	m_hitPointWorld;
		const btCollisionObject*	m_hitCollisionObject;
		
		virtual	btScalar	addSingleResult(LocalConvexResult& convexResult,bool normalInWorldSpace)
		{
//caller already does the filter on the m_closestHitFraction
			btAssert(convexResult.m_hitFraction <= m_closestHitFraction);
						
			m_closestHitFraction = convexResult.m_hitFraction;
			m_hitCollisionObject = convexResult.m_hitCollisionObject;
			if (normalInWorldSpace)
			{
				m_hitNormalWorld = convexResult.m_hitNormalLocal;
			} else
			{
				///need to transform normal into worldspace
				m_hitNormalWorld = m_hitCollisionObject->getWorldTransform().getBasis()*convexResult.m_hitNormalLocal;
			}
			m_hitPointWorld = convexResult.m_hitPointLocal;
			return convexResult.m_hitFraction;
		}
	};

	///ContactResultCallback is used to report contact points
	struct	ContactResultCallback
	{
		short int	m_collisionFilterGroup;
		short int	m_collisionFilterMask;
		
		ContactResultCallback()
			:m_collisionFilterGroup(btBroadphaseProxy::DefaultFilter),
			m_collisionFilterMask(btBroadphaseProxy::AllFilter)
		{
		}

		virtual ~ContactResultCallback()
		{
		}
		
		virtual bool needsCollision(btBroadphaseProxy* proxy0) const
		{
			bool collides = (proxy0->m_collisionFilterGroup & m_collisionFilterMask) != 0;
			collides = collides && (m_collisionFilterGroup & proxy0->m_collisionFilterMask);
			return collides;
		}

		virtual	btScalar	addSingleResult(btManifoldPoint& cp,	const btCollisionObjectWrapper* colObj0Wrap,int partId0,int index0,const btCollisionObjectWrapper* colObj1Wrap,int partId1,int index1) = 0;
	};
	
%nestedworkaround btCollisionWorld::LocalShapeInfo;
%nestedworkaround btCollisionWorld::LocalRayResult;
%nestedworkaround btCollisionWorld::RayResultCallback;
%nestedworkaround btCollisionWorld::ClosestRayResultCallback;
%nestedworkaround btCollisionWorld::AllHitsRayResultCallback;
%nestedworkaround btCollisionWorld::LocalConvexResult;
%nestedworkaround btCollisionWorld::ConvexResultCallback;
%nestedworkaround btCollisionWorld::ClosestConvexResultCallback;
%nestedworkaround btCollisionWorld::ContactResultCallback;

%{
#include <BulletCollision/CollisionDispatch/btCollisionWorld.h>
%}

//%rename(internalSetBroadphase) btCollisionWorld::setBroadphase;
//%javamethodmodifiers btCollisionWorld::setBroadphase "private";
//%rename(internalGetBroadphase) btCollisionWorld::getBroadphase;
//%javamethodmodifiers btCollisionWorld::getBroadphase "private";
//%rename(internalGetDispatcher) btCollisionWorld::getDispatcher;
//%javamethodmodifiers btCollisionWorld::getDispatcher "private";
//%rename(internalSetDebugDrawer) btCollisionWorld::setDebugDrawer;
//%javamethodmodifiers btCollisionWorld::setDebugDrawer "private";
//%rename(internalGetDebugDrawer) btCollisionWorld::getDebugDrawer;
//%javamethodmodifiers btCollisionWorld::getDebugDrawer "private";
//%ignore btCollisionWorld::getCollisionObjectArray;
//%rename(internalGetNumCollisionObjects) btCollisionWorld::getNumCollisionObjects;
//%javamethodmodifiers btCollisionWorld::getNumCollisionObjects "private";
//%rename(internalAddCollisionObject) btCollisionWorld::addCollisionObject;
//%javamethodmodifiers btCollisionWorld::addCollisionObject "private";
//%rename(internalRemoveCollisionObject) btCollisionWorld::removeCollisionObject;
//%javamethodmodifiers btCollisionWorld::removeCollisionObject "private";
//
//%typemap(javacode) btCollisionWorld %{
//	protected final Array<btCollisionObject> collisionObjects;
//	protected void refCollisionObject(final btCollisionObject object) {
//		final int idx = collisionObjects.indexOf(object, false);
//		if (idx >= 0)
//			throw new GdxRuntimeException("Object already added: "+object.toString());
//		object.obtain();
//		collisionObjects.add(object);
//	}
//	protected void unrefCollisionObject(final btCollisionObject object) {
//		final int idx = collisionObjects.indexOf(object, false);
//		if (idx >= 0)
//			unrefCollisionObject(idx);
//	}
//	protected void unrefCollisionObject(final int index) {
//		collisionObjects.removeIndex(index).release();
//	}
//	public int getNumCollisionObjects() {
//		return collisionObjects.size;
//	}
//	public btCollisionObject getCollisionObjects(int index) {
//		return collisionObjects.get(index);
//	}
//	public void addCollisionObject(btCollisionObject collisionObject, short collisionFilterGroup, short collisionFilterMask) {
//		refCollisionObject(collisionObject);
//		internalAddCollisionObject(collisionObject, collisionFilterGroup, collisionFilterMask);
//	}
//	public void addCollisionObject(btCollisionObject collisionObject, short collisionFilterGroup) {
//		refCollisionObject(collisionObject);
//		internalAddCollisionObject(collisionObject, collisionFilterGroup);
//	}
//	public void addCollisionObject(btCollisionObject collisionObject) {
//		refCollisionObject(collisionObject);
//		internalAddCollisionObject(collisionObject);
//	}
//	public void removeCollisionObject(btCollisionObject collisionObject) {
//		internalRemoveCollisionObject(collisionObject);
//		unrefCollisionObject(collisionObject);
//	}
//	public void removeCollisionObject(int index) {
//		internalRemoveCollisionObject(collisionObjects.get(index));
//		unrefCollisionObject(index);
//	}
//	
//	protected btDispatcher dispatcher;
//	protected void refDispatcher(final btDispatcher dispatcher) {
//		if (this.dispatcher != dispatcher) {
//			if (this.dispatcher != null)
//				this.dispatcher.releaser();
//			this.dispatcher = dispatcher;
//			if (this.dispatcher != null)
//				this.dispatcher.obtain();
//		}
//	}
//	
//	protected btBroadphaseInterface broadphasePairCache;
//	protected void refBroadphasePairCache(final btBroadphaseInterface broadphasePairCache) {
//		if (this.broadphasePairCache != broadphasePairCache) {
//			if (this.broadphasePairCache != null)
//				this.broadphasePairCache.releaser();
//			this.broadphasePairCache = broadphasePairCache;
//			if (this.broadphasePairCache != null)
//				this.broadphasePairCache.obtain();
//		}
//	}
//%}


%include "BulletCollision/CollisionDispatch/btCollisionWorld.h"

%{
typedef btCollisionWorld::LocalShapeInfo LocalShapeInfo;
typedef btCollisionWorld::LocalRayResult LocalRayResult;
typedef btCollisionWorld::RayResultCallback RayResultCallback;
typedef btCollisionWorld::ClosestRayResultCallback ClosestRayResultCallback;
typedef btCollisionWorld::AllHitsRayResultCallback AllHitsRayResultCallback;
typedef btCollisionWorld::LocalConvexResult LocalConvexResult;
typedef btCollisionWorld::ConvexResultCallback ConvexResultCallback;
typedef btCollisionWorld::ClosestConvexResultCallback ClosestConvexResultCallback;
typedef btCollisionWorld::ContactResultCallback ContactResultCallback;
%}