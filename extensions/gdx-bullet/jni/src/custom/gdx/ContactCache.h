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
#include <map>

#ifndef SWIG
static void ContactCacheStarted_CB(btPersistentManifold* const &manifold);
static void ContactCacheEnded_CB(btPersistentManifold* const &manifold);

class ContactCache;
ContactCache *currentContactCache = 0;

struct ContactPair {
	const btCollisionObject *object0;
	const btCollisionObject *object1;

	ContactPair() : object0(0), object1(0) {}

	ContactPair(const ContactPair &rhs) : object0(rhs.object0), object1(rhs.object1) {}

	ContactPair(const btCollisionObject* const &object0, const btCollisionObject* const &object1) : object0(object0), object1(object1) {}

	ContactPair &operator=(const ContactPair &rhs) {
		object0 = rhs.object0;
		object1 = rhs.object1;
		return *this;
	}

	bool operator==(const ContactPair &rhs) const {
		return ((rhs.object0 == object0) && (rhs.object1 == object1)) || ((rhs.object0 == object1) && (rhs.object1 == object0));
	}

	bool operator<(const ContactPair &rhs) const {
		if (*this == rhs)
			return false;
		return object0 < rhs.object0;
	}
};
#endif //SWIG

/** @author Xoppa */
class ContactCache {
protected:
#ifndef SWIG
	bool filter;
	int events;
	std::map<ContactPair, float> cache;
#endif
public:
	float cacheTime;

	ContactCache(bool dummy) : events(0), cacheTime(0.2f), filter(true) {
	}

	~ContactCache() {
		disable();
	}

#ifndef SWIG
	void setEvents(const int &events) {
		this->events = events;
	}
#endif //SWIG

	void enable() {
		currentContactCache = this;
		gContactStartedCallback = ContactCacheStarted_CB;
		gContactEndedCallback = ContactCacheEnded_CB;
	}

	void disable() {
		if (currentContactCache == this) {
			currentContactCache = 0;
			if (gContactStartedCallback == ContactCacheStarted_CB)
				gContactStartedCallback = 0;
			if (gContactEndedCallback == ContactCacheEnded_CB)
				gContactEndedCallback = 0;
		}
	}

	bool isEnabled() {
		return (currentContactCache == this) && (gContactStartedCallback == ContactCacheStarted_CB) && (gContactEndedCallback == ContactCacheEnded_CB);
	}

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

	void clear() {
		cache.clear();
	}

	void update(float delta) {
		std::map<ContactPair, float>::iterator it = cache.begin();
		while (it != cache.end()) {
			if ((it->second -= delta) < 0) {
				const btCollisionObject* const &object0 = it->first.object0;
				const btCollisionObject* const &object1 = it->first.object1;
				const bool match0 = gdxCheckFilter(object0, object1);
				const bool match1 = gdxCheckFilter(object1, object0);
				if (!filter || match0 || match1)
					onContactEnded(object0, match0, object1, match1);
				cache.erase(it++);
			} else
				++it;
		}
	}

#ifndef SWIG
	void contactStarted(btPersistentManifold* manifold) {
		const bool match0 = gdxCheckFilter(manifold->getBody0(), manifold->getBody1());
		const bool match1 = gdxCheckFilter(manifold->getBody1(), manifold->getBody0());
		if (filter && !match0 && !match1)
			return;
		const ContactPair pair(manifold->getBody0(), manifold->getBody1());
		std::map<ContactPair, float>::iterator it = cache.find(pair);
		if (it != cache.end())
			cache.erase(it);
		else {
			onContactStarted(manifold, match0, match1);
		}
	}
	void contactEnded(btPersistentManifold* manifold) {
		const bool match0 = gdxCheckFilter(manifold->getBody0(), manifold->getBody1());
		const bool match1 = gdxCheckFilter(manifold->getBody1(), manifold->getBody0());
		if (filter && !match0 && !match1)
			return;
		const ContactPair pair(manifold->getBody0(), manifold->getBody1());
		cache[pair] = cacheTime;
	}
#endif //SWIG
};

#ifndef SWIG
inline void ContactCacheStarted_CB(btPersistentManifold* const &manifold) {
	currentContactCache->contactStarted(manifold);
}
inline void ContactCacheEnded_CB(btPersistentManifold* const &manifold) {
	currentContactCache->contactEnded(manifold);
}
#endif //SWIG

#endif //ContactCache_H