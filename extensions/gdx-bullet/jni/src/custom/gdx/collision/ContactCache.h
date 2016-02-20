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

#ifndef ContactCache_H
#define ContactCache_H

#include "ContactListener.h"
#include "../../../bullet/LinearMath/btAlignedObjectArray.h"

#ifndef SWIG
struct ContactPair;
#endif //SWIG

/** @author Xoppa */
class ContactCache {
protected:
#ifndef SWIG
	bool filter;
	int events;
	btAlignedObjectArray<ContactPair> cache;
	//std::map<ContactPair, float> cache;
#endif
public:
	float cacheTime;

	ContactCache(bool dummy);
	virtual ~ContactCache();

#ifndef SWIG
	void setEvents(const int &events);
#endif //SWIG

	void enable();
	void disable();
	bool isEnabled();

	//virtual void onContactStarted(btPersistentManifold* manifold) = 0;
	//virtual void onContactStarted(const btCollisionObject* colObj0, const btCollisionObject* colObj1) = 0;
	//virtual void onContactStarted(const int &userValue0, const int &userValue1) = 0;

	virtual void onContactStarted(btPersistentManifold* manifold, const bool &match0, const bool &match1) = 0;
	//virtual void onContactStarted(const btCollisionObject* colObj0, const bool &match0, const btCollisionObject* colObj1, const bool &match1) = 0;
	//virtual void onContactStarted(const int &userValue0, const bool &match0, const int &userValue1, const bool &match1) = 0;

	//virtual void onContactEnded(btPersistentManifold* manifold) = 0;
	//virtual void onContactEnded(const btCollisionObject* colObj0, const btCollisionObject* colObj1) = 0;
	//virtual void onContactEnded(const int &userValue0, const int &userValue1) = 0;

	//virtual void onContactEnded(btPersistentManifold* manifold, const bool &match0, const bool &match1) = 0;
	virtual void onContactEnded(const btCollisionObject* colObj0, const bool &match0, const btCollisionObject* colObj1, const bool &match1) = 0;
	//virtual void onContactEnded(const int &userValue0, const bool &match0, const int &userValue1, const bool &match1) = 0;

	void clear();

	void update(float delta);

#ifndef SWIG
	int indexOf(const btCollisionObject* const &obj0, const btCollisionObject* const &obj1);

	void contactStarted(btPersistentManifold* manifold);

	void contactEnded(btPersistentManifold* manifold);
#endif //SWIG
};

#endif //ContactCache_H
