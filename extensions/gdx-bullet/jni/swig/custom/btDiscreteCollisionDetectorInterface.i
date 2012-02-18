/*
 *	Interface module for a class with inner structs or classes.
 */
 
%module btDiscreteCollisionDetectorInterface

// Nested struct or class copied from Bullet header
struct Result
{

	virtual ~Result(){}	

	///setShapeIdentifiersA/B provides experimental support for per-triangle material / custom material combiner
	virtual void setShapeIdentifiersA(int partId0,int index0)=0;
	virtual void setShapeIdentifiersB(int partId1,int index1)=0;
	virtual void addContactPoint(const btVector3& normalOnBInWorld,const btVector3& pointInWorld,btScalar depth)=0;
};

// Nested struct or class copied from Bullet header
struct ClosestPointInput
{
	ClosestPointInput()
		:m_maximumDistanceSquared(btScalar(BT_LARGE_FLOAT)),
		m_stackAlloc(0)
	{
	}

	btTransform m_transformA;
	btTransform m_transformB;
	btScalar	m_maximumDistanceSquared;
	btStackAlloc* m_stackAlloc;
};

%nestedworkaround btDiscreteCollisionDetectorInterface::Result;
%nestedworkaround btDiscreteCollisionDetectorInterface::ClosestPointInput;

%{
#include <BulletCollision/NarrowPhaseCollision/btDiscreteCollisionDetectorInterface.h>
%}

/*
 * For some reason SWIG wants to generate constructors for abstract btStorageResult,
 * so list the types here instead of %including the whole header.
 */
struct btDiscreteCollisionDetectorInterface;

%{
typedef btDiscreteCollisionDetectorInterface::Result Result;
typedef btDiscreteCollisionDetectorInterface::ClosestPointInput ClosestPointInput;
%}
