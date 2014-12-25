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
#ifdef _MSC_VER
#pragma once
#endif

#ifndef CustomCollisionDispatcher_H
#define CustomCollisionDispatcher_H

/** For some reason swig doesnt see the needsCollision/needsResponse inheritance (it does see the other virtual methods do).
 *  @author Xoppa */
class CustomCollisionDispatcher : public btCollisionDispatcher {
public:
	CustomCollisionDispatcher (btCollisionConfiguration* collisionConfiguration)
		: btCollisionDispatcher(collisionConfiguration)
	{}
	virtual bool needsCollision(const btCollisionObject* body0,const btCollisionObject* body1) {
		return btCollisionDispatcher::needsCollision(body0, body1);
	}
	virtual bool needsResponse(const btCollisionObject* body0,const btCollisionObject* body1) {
		return btCollisionDispatcher::needsResponse(body0, body1);
	}
};

#endif //CustomCollisionDispatcher_H
