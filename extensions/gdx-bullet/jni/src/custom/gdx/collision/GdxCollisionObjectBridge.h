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

#ifndef GdxCollsionObjectBridge_H
#define GdxCollsionObjectBridge_H

#include "../../../bullet/BulletCollision/CollisionDispatch/btCollisionObject.h"

/** @author Xoppa */
class GdxCollisionObjectBridge {
public:
	int userValue;
	int contactCallbackFlag;
	int contactCallbackFilter;

	GdxCollisionObjectBridge() : userValue(0), contactCallbackFlag(1), contactCallbackFilter(0)
	{}
};

inline bool gdxCheckFilter(const int filter, const int flag) {
	return (filter & flag) == flag;
}

inline bool gdxCheckFilter(const btCollisionObject* colObj0, const btCollisionObject* colObj1) {
	return gdxCheckFilter(((GdxCollisionObjectBridge*)(colObj0->getUserPointer()))->contactCallbackFilter,
			((GdxCollisionObjectBridge*)(colObj1->getUserPointer()))->contactCallbackFlag);
}

#endif // GdxCollsionObjectBridge_H
