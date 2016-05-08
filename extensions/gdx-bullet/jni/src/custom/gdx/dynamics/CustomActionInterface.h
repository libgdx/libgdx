#ifndef CustomActionInterface_H
#define CustomActionInterface_H

#include "../../../bullet/BulletCollision/CollisionDispatch/btCollisionWorld.h"

class CustomActionInterface : public btActionInterface {
public:
	virtual void updateAction(btScalar timeStep)=0;

	virtual void debugDraw()=0;

	virtual void updateAction(btCollisionWorld* collisionWorld, btScalar timeStep) {
	    updateAction(timeStep);
	}

	virtual void debugDraw(btIDebugDraw* debugDrawer) { debugDraw(); }
};

#endif // CustomActionInterface_H
