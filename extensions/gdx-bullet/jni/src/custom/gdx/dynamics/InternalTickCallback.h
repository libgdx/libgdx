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

#ifndef InternalTickCallback_H
#define InternalTickCallback_H

#include "../../../bullet/BulletDynamics/Dynamics/btDynamicsWorld.h"

static void InternalTickCallback_CB(btDynamicsWorld *world, btScalar timeStep);

/** @author xoppa */
class InternalTickCallback {
protected:
	btDynamicsWorld *mWorld;
	bool mIsPreTick;
public:
	InternalTickCallback(btDynamicsWorld *dynamicsWorld = NULL, bool isPreTick = false);
	virtual void onInternalTick(btDynamicsWorld *dynamicsWorld, btScalar timeStep) { }
	void detach();
	void attach(btDynamicsWorld *dynamicsWorld, bool isPreTick);
	void attach();
	static void detach(btDynamicsWorld *dynamicsWorld, bool isPreTick);
};

static void InternalTickCallback_CB(btDynamicsWorld *world, btScalar timeStep) {
	InternalTickCallback *cb = static_cast<InternalTickCallback *>(world->getWorldUserInfo());
	cb->onInternalTick(world, timeStep);
}

#endif // InternalTickCallback_H
