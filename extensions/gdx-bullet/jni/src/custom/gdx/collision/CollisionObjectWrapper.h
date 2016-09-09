/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

#ifndef CollisionObjectWrapper_H
#define CollisionObjectWrapper_H

#include "../../../bullet/BulletCollision/CollisionDispatch/btCollisionObject.h"
#include "../../../bullet/BulletCollision/CollisionDispatch/btCollisionObjectWrapper.h"

/** @author Xoppa */
class CollisionObjectWrapper {
	btCollisionObjectWrapper wrapper;

public:
	CollisionObjectWrapper(btCollisionObjectWrapper* parent, btCollisionShape* shape, btCollisionObject* collisionObject, btTransform& worldTransform, int partId=-1, int index=-1) :
		wrapper(parent, shape, collisionObject, worldTransform, partId, index) {}

	CollisionObjectWrapper(btCollisionShape* shape, btCollisionObject* collisionObject, btTransform& worldTransform, int partId=-1, int index=-1) :
		wrapper(0, shape, collisionObject, worldTransform, partId, index) {}

	CollisionObjectWrapper(btCollisionObjectWrapper* parent, btCollisionObject* collisionObject, int partId=-1, int index=-1) :
		wrapper(parent, collisionObject->getCollisionShape(),collisionObject, collisionObject->getWorldTransform(), partId, index) {}

	CollisionObjectWrapper(btCollisionObject* collisionObject, int partId=-1, int index=-1) :
		wrapper(0, collisionObject->getCollisionShape(),collisionObject, collisionObject->getWorldTransform(), partId, index) {}

	btCollisionObjectWrapper *getWrapper() { return &wrapper; }
};

#endif //CollisionObjectWrapper_H
