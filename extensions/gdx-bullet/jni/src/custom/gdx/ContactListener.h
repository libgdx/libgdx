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

#ifndef ContactListener_H
#define ContactListener_H

#include "GdxCollisionObjectBridge.h"
#include "../../bullet/BulletCollision/CollisionDispatch/btCollisionObject.h"
#include "../../bullet/BulletCollision/CollisionDispatch/btCollisionObjectWrapper.h"
#include "../../bullet/BulletCollision/CollisionDispatch/btManifoldResult.h"
#include "../../bullet/BulletCollision/NarrowPhaseCollision/btPersistentManifold.h"

enum ContactCallbackEvent {
	TYPE_MASK = 0x0F,
	BY_MASK = 12,
	FILTERED = 1,
	INCLUDE_POINT = 2,
	BY_WRAPPER = 4,
	BY_MANIFOLD = 4,
	BY_OBJECT = 8,
	BY_VALUE = 12,

	SHIFT_ON_ADDED = 0,
	SHIFT_ON_PROCESSED = 4,
	SHIFT_ON_DESTROYED = 8,
	SHIFT_ON_STARTED = 12,
	SHIFT_ON_ENDED = 16,

	ON_ADDED_UNFILTERED_WRAPPER					= (BY_WRAPPER) << SHIFT_ON_ADDED,
	ON_ADDED_UNFILTERED_OBJECT					= (BY_OBJECT) << SHIFT_ON_ADDED,
	ON_ADDED_UNFILTERED_VALUE					= (BY_VALUE) << SHIFT_ON_ADDED,
	ON_ADDED_UNFILTERED_WRAPPER_INCLUDEPOINT	= (BY_WRAPPER | INCLUDE_POINT) << SHIFT_ON_ADDED,
	ON_ADDED_UNFILTERED_OBJECT_INCLUDEPOINT		= (BY_OBJECT | INCLUDE_POINT) << SHIFT_ON_ADDED,
	ON_ADDED_UNFILTERED_VALUE_INCLUDEPOINT		= (BY_VALUE | INCLUDE_POINT) << SHIFT_ON_ADDED,
	ON_ADDED_FILTERED_WRAPPER					= (FILTERED | BY_WRAPPER) << SHIFT_ON_ADDED,
	ON_ADDED_FILTERED_OBJECT					= (FILTERED | BY_OBJECT) << SHIFT_ON_ADDED,
	ON_ADDED_FILTERED_VALUE						= (FILTERED | BY_VALUE) << SHIFT_ON_ADDED,
	ON_ADDED_FILTERED_WRAPPER_INCLUDEPOINT		= (FILTERED | BY_WRAPPER | INCLUDE_POINT) << SHIFT_ON_ADDED,
	ON_ADDED_FILTERED_OBJECT_INCLUDEPOINT		= (FILTERED | BY_OBJECT | INCLUDE_POINT) << SHIFT_ON_ADDED,
	ON_ADDED_FILTERED_VALUE_INCLUDEPOINT		= (FILTERED | BY_VALUE | INCLUDE_POINT) << SHIFT_ON_ADDED,

	ON_PROCESSED_UNFILTERED_OBJECT				= (BY_OBJECT) << SHIFT_ON_PROCESSED,
	ON_PROCESSED_UNFILTERED_VALUE				= (BY_VALUE) << SHIFT_ON_PROCESSED,
	ON_PROCESSED_UNFILTERED_OBJECT_INCLUDEPOINT	= (BY_OBJECT | INCLUDE_POINT) << SHIFT_ON_PROCESSED,
	ON_PROCESSED_UNFILTERED_VALUE_INCLUDEPOINT	= (BY_VALUE | INCLUDE_POINT) << SHIFT_ON_PROCESSED,
	ON_PROCESSED_FILTERED_OBJECT				= (FILTERED | BY_OBJECT) << SHIFT_ON_PROCESSED,
	ON_PROCESSED_FILTERED_VALUE					= (FILTERED | BY_VALUE) << SHIFT_ON_PROCESSED,
	ON_PROCESSED_FILTERED_OBJECT_INCLUDEPOINT	= (FILTERED | BY_OBJECT | INCLUDE_POINT) << SHIFT_ON_PROCESSED,
	ON_PROCESSED_FILTERED_VALUE_INCLUDEPOINT	= (FILTERED | BY_VALUE | INCLUDE_POINT) << SHIFT_ON_PROCESSED,

	ON_DESTROYED								= BY_VALUE << SHIFT_ON_DESTROYED,

	ON_STARTED_UNFILTERED_MANIFOLD				= (BY_MANIFOLD) << SHIFT_ON_STARTED,
	ON_STARTED_UNFILTERED_OBJECT				= (BY_OBJECT) << SHIFT_ON_STARTED,
	ON_STARTED_UNFILTERED_VALUE					= (BY_VALUE) << SHIFT_ON_STARTED,
	ON_STARTED_FILTERED_MANIFOLD				= (FILTERED | BY_MANIFOLD) << SHIFT_ON_STARTED,
	ON_STARTED_FILTERED_OBJECT					= (FILTERED | BY_OBJECT) << SHIFT_ON_STARTED,
	ON_STARTED_FILTERED_VALUE					= (FILTERED | BY_VALUE) << SHIFT_ON_STARTED,

	ON_ENDED_UNFILTERED_MANIFOLD				= (BY_MANIFOLD) << SHIFT_ON_ENDED,
	ON_ENDED_UNFILTERED_OBJECT					= (BY_OBJECT) << SHIFT_ON_ENDED,
	ON_ENDED_UNFILTERED_VALUE					= (BY_VALUE) << SHIFT_ON_ENDED,
	ON_ENDED_FILTERED_MANIFOLD					= (FILTERED | BY_MANIFOLD) << SHIFT_ON_ENDED,
	ON_ENDED_FILTERED_OBJECT					= (FILTERED | BY_OBJECT) << SHIFT_ON_ENDED,
	ON_ENDED_FILTERED_VALUE						= (FILTERED | BY_VALUE) << SHIFT_ON_ENDED,
};

#ifndef SWIG
static bool ContactAddedListener_CB_wrapper_point(btManifoldPoint& cp,const btCollisionObjectWrapper* colObj0Wrap,int partId0,int index0,const btCollisionObjectWrapper* colObj1Wrap,int partId1,int index1);
static bool ContactAddedListener_CB_object_point(btManifoldPoint& cp,const btCollisionObjectWrapper* colObj0Wrap,int partId0,int index0,const btCollisionObjectWrapper* colObj1Wrap,int partId1,int index1);
static bool ContactAddedListener_CB_value_point(btManifoldPoint& cp,const btCollisionObjectWrapper* colObj0Wrap,int partId0,int index0,const btCollisionObjectWrapper* colObj1Wrap,int partId1,int index1);
static bool ContactAddedListener_CB_wrapper_filter_point(btManifoldPoint& cp,const btCollisionObjectWrapper* colObj0Wrap,int partId0,int index0,const btCollisionObjectWrapper* colObj1Wrap,int partId1,int index1);
static bool ContactAddedListener_CB_object_filter_point(btManifoldPoint& cp,const btCollisionObjectWrapper* colObj0Wrap,int partId0,int index0,const btCollisionObjectWrapper* colObj1Wrap,int partId1,int index1);
static bool ContactAddedListener_CB_value_filter_point(btManifoldPoint& cp,const btCollisionObjectWrapper* colObj0Wrap,int partId0,int index0,const btCollisionObjectWrapper* colObj1Wrap,int partId1,int index1);
static bool ContactAddedListener_CB_wrapper(btManifoldPoint& cp,const btCollisionObjectWrapper* colObj0Wrap,int partId0,int index0,const btCollisionObjectWrapper* colObj1Wrap,int partId1,int index1);
static bool ContactAddedListener_CB_object(btManifoldPoint& cp,const btCollisionObjectWrapper* colObj0Wrap,int partId0,int index0,const btCollisionObjectWrapper* colObj1Wrap,int partId1,int index1);
static bool ContactAddedListener_CB_value(btManifoldPoint& cp,const btCollisionObjectWrapper* colObj0Wrap,int partId0,int index0,const btCollisionObjectWrapper* colObj1Wrap,int partId1,int index1);
static bool ContactAddedListener_CB_wrapper_filter(btManifoldPoint& cp,const btCollisionObjectWrapper* colObj0Wrap,int partId0,int index0,const btCollisionObjectWrapper* colObj1Wrap,int partId1,int index1);
static bool ContactAddedListener_CB_object_filter(btManifoldPoint& cp,const btCollisionObjectWrapper* colObj0Wrap,int partId0,int index0,const btCollisionObjectWrapper* colObj1Wrap,int partId1,int index1);
static bool ContactAddedListener_CB_value_filter(btManifoldPoint& cp,const btCollisionObjectWrapper* colObj0Wrap,int partId0,int index0,const btCollisionObjectWrapper* colObj1Wrap,int partId1,int index1);

static bool ContactProcessedListener_CB_object_point(btManifoldPoint& cp,void *body0, void *body1);
static bool ContactProcessedListener_CB_value_point(btManifoldPoint& cp,void *body0, void *body1);
static bool ContactProcessedListener_CB_object_filter_point(btManifoldPoint& cp,void *body0, void *body1);
static bool ContactProcessedListener_CB_value_filter_point(btManifoldPoint& cp,void *body0, void *body1);
static bool ContactProcessedListener_CB_object(btManifoldPoint& cp,void *body0, void *body1);
static bool ContactProcessedListener_CB_value(btManifoldPoint& cp,void *body0, void *body1);
static bool ContactProcessedListener_CB_object_filter(btManifoldPoint& cp,void *body0, void *body1);
static bool ContactProcessedListener_CB_value_filter(btManifoldPoint& cp,void *body0, void *body1);

static bool ContactDestroyedListener_CB(void *userPersistentData);

static void ContactStartedListener_CB_manifold(btPersistentManifold* const &manifold);
static void ContactStartedListener_CB_object(btPersistentManifold* const &manifold);
static void ContactStartedListener_CB_value(btPersistentManifold* const &manifold);
static void ContactStartedListener_CB_manifold_filter(btPersistentManifold* const &manifold);
static void ContactStartedListener_CB_object_filter(btPersistentManifold* const &manifold);
static void ContactStartedListener_CB_value_filter(btPersistentManifold* const &manifold);

static void ContactEndedListener_CB_manifold(btPersistentManifold* const &manifold);
static void ContactEndedListener_CB_object(btPersistentManifold* const &manifold);
static void ContactEndedListener_CB_value(btPersistentManifold* const &manifold);
static void ContactEndedListener_CB_manifold_filter(btPersistentManifold* const &manifold);
static void ContactEndedListener_CB_object_filter(btPersistentManifold* const &manifold);
static void ContactEndedListener_CB_value_filter(btPersistentManifold* const &manifold);

class ContactListener;
ContactListener *currentContactAddedListener = 0;
ContactListener *currentContactProcessedListener = 0;
ContactListener *currentContactDestroyedListener = 0;
ContactListener *currentContactStartedListener = 0;
ContactListener *currentContactEndedListener = 0;
#endif //SWIG

/** @author Xoppa */
class ContactListener {
protected:
#ifndef SWIG
	int events;
#endif
public:
	ContactListener(bool dummy) : events(0) {
	}

	~ContactListener() {
		disable();
	}

#ifndef SWIG
	void setEvents(const int &events) {
		this->events = events;
	}
#endif //SWIG

	void enable() {
		enableOnAdded();
		enableOnProcessed();
		enableOnDestroyed();
		enableOnStarted();
		enableOnEnded();
	}

	void disable() {
		disableOnAdded();
		disableOnProcessed();
		disableOnDestroyed();
		disableOnStarted();
		disableOnEnded();
	}

	void enableOnAdded() {
		const int e = (events >> SHIFT_ON_ADDED) & TYPE_MASK;
		if (e == 0)
			return;
		const bool filter = (e & FILTERED) != 0;
		const bool point = (e & INCLUDE_POINT) != 0;
		const int by = (e & BY_MASK);
		currentContactAddedListener = this;
		if (by == BY_OBJECT)
			gContactAddedCallback = filter ?
					(point ? ContactAddedListener_CB_object_filter_point : ContactAddedListener_CB_object_filter) :
					(point ? ContactAddedListener_CB_object_point : ContactAddedListener_CB_object);
		else if (by == BY_VALUE)
			gContactAddedCallback = filter ?
					(point ? ContactAddedListener_CB_value_filter_point : ContactAddedListener_CB_value_filter) :
					(point ? ContactAddedListener_CB_value_point : ContactAddedListener_CB_value);
		else
			gContactAddedCallback = filter ?
					(point ? ContactAddedListener_CB_wrapper_filter_point: ContactAddedListener_CB_wrapper_filter) :
					(point ? ContactAddedListener_CB_wrapper_point : ContactAddedListener_CB_wrapper);
	}

	void disableOnAdded() {
		if (currentContactAddedListener == this) {
			gContactAddedCallback = 0;
			currentContactAddedListener = 0;
		}
	}

	inline bool isOnAddedEnabled() {
		return currentContactAddedListener == this;
	}

	virtual bool onContactAdded(btManifoldPoint& cp,const btCollisionObjectWrapper* colObj0Wrap,int partId0,int index0,
			const btCollisionObjectWrapper* colObj1Wrap,int partId1,int index1) = 0;
	virtual bool onContactAdded(btManifoldPoint& cp,const btCollisionObject* colObj0,int partId0,int index0,
				const btCollisionObject* colObj1,int partId1,int index1) = 0;
	virtual bool onContactAdded(btManifoldPoint& cp,int userValue0,int partId0,int index0,
			int userValue1,int partId1,int index1) = 0;

	virtual bool onContactAdded(btManifoldPoint& cp,const btCollisionObjectWrapper* colObj0Wrap,int partId0,int index0,bool match0,
			const btCollisionObjectWrapper* colObj1Wrap,int partId1,int index1,bool match1) = 0;
	virtual bool onContactAdded(btManifoldPoint& cp,const btCollisionObject* colObj0,int partId0,int index0,bool match0,
				const btCollisionObject* colObj1,int partId1,int index1,bool match1) = 0;
	virtual bool onContactAdded(btManifoldPoint& cp,int userValue0,int partId0,int index0,bool match0,
			int userValue1,int partId1,int index1,bool match1) = 0;

	virtual bool onContactAdded(const btCollisionObjectWrapper* colObj0Wrap,int partId0,int index0,
			const btCollisionObjectWrapper* colObj1Wrap,int partId1,int index1) = 0;
	virtual bool onContactAdded(const btCollisionObject* colObj0,int partId0,int index0,
				const btCollisionObject* colObj1,int partId1,int index1) = 0;
	virtual bool onContactAdded(int userValue0,int partId0,int index0,
			int userValue1,int partId1,int index1) = 0;

	virtual bool onContactAdded(const btCollisionObjectWrapper* colObj0Wrap,int partId0,int index0,bool match0,
			const btCollisionObjectWrapper* colObj1Wrap,int partId1,int index1,bool match1) = 0;
	virtual bool onContactAdded(const btCollisionObject* colObj0,int partId0,int index0,bool match0,
				const btCollisionObject* colObj1,int partId1,int index1,bool match1) = 0;
	virtual bool onContactAdded(int userValue0,int partId0,int index0,bool match0,
			int userValue1,int partId1,int index1,bool match1) = 0;

	void enableOnProcessed() {
		const int e = (events >> SHIFT_ON_PROCESSED) & TYPE_MASK;
		if (e == 0)
			return;
		const bool filter = (e & FILTERED) != 0;
		const bool point = (e & INCLUDE_POINT) != 0;
		const int by = (e & BY_MASK);
		currentContactProcessedListener = this;
		if (by == BY_VALUE)
			gContactProcessedCallback = filter ?
					(point ? ContactProcessedListener_CB_value_filter_point : ContactProcessedListener_CB_value_filter) :
					(point ? ContactProcessedListener_CB_value_point : ContactProcessedListener_CB_value);
		else
			gContactProcessedCallback = filter ?
					(point ? ContactProcessedListener_CB_object_filter_point : ContactProcessedListener_CB_object_filter) :
					(point ? ContactProcessedListener_CB_object_point : ContactProcessedListener_CB_object);
	}

	void disableOnProcessed() {
		if (currentContactProcessedListener == this) {
			gContactProcessedCallback = 0;
			currentContactProcessedListener = 0;
		}
	}

	inline bool isOnProcessedEnabled() {
		return currentContactProcessedListener == this;
	}

	virtual void onContactProcessed(btManifoldPoint& cp, const btCollisionObject* colObj0, const btCollisionObject* colObj1) = 0;
	virtual void onContactProcessed(btManifoldPoint& cp, int userValue0, int userValue1) = 0;

	virtual void onContactProcessed(btManifoldPoint& cp, const btCollisionObject* colObj0, bool match0, const btCollisionObject* colObj1, bool match1) = 0;
	virtual void onContactProcessed(btManifoldPoint& cp, int userValue0, bool match0, int userValue1, bool match1) = 0;

	virtual void onContactProcessed(const btCollisionObject* colObj0, const btCollisionObject* colObj1) = 0;
	virtual void onContactProcessed(int userValue0, int userValue1) = 0;

	virtual void onContactProcessed(const btCollisionObject* colObj0, bool match0, const btCollisionObject* colObj1, bool match1) = 0;
	virtual void onContactProcessed(int userValue0, bool match0, int userValue1, bool match1) = 0;

	void enableOnDestroyed() {
		const int e = (events >> SHIFT_ON_DESTROYED) & TYPE_MASK;
		if (e == 0)
			return;
		const bool filter = (e & FILTERED) != 0;
		const bool point = (e & INCLUDE_POINT) != 0;
		const int by = (e & BY_MASK);
		currentContactDestroyedListener = this;
		gContactDestroyedCallback = ContactDestroyedListener_CB;
	}

	void disableOnDestroyed() {
		if (currentContactDestroyedListener == this) {
			gContactDestroyedCallback = 0;
			currentContactDestroyedListener = 0;
		}
	}

	inline bool isOnDestroyedEnabled() {
		return currentContactDestroyedListener == this;
	}

	virtual void onContactDestroyed(int manifoldPointUserValue) = 0;

	void enableOnStarted() {
		const int e = (events >> SHIFT_ON_STARTED) & TYPE_MASK;
		if (e == 0)
			return;
		const bool filter = (e & FILTERED) != 0;
		const bool point = (e & INCLUDE_POINT) != 0;
		const int by = (e & BY_MASK);
		currentContactStartedListener = this;
		if (by == BY_OBJECT)
			gContactStartedCallback = filter ? ContactStartedListener_CB_object_filter : ContactStartedListener_CB_object;
		else if (by == BY_VALUE)
			gContactStartedCallback = filter ? ContactStartedListener_CB_value_filter : ContactStartedListener_CB_value;
		else
			gContactStartedCallback = filter ? ContactStartedListener_CB_manifold_filter : ContactStartedListener_CB_manifold;
	}

	void disableOnStarted() {
		if (currentContactStartedListener == this) {
			gContactStartedCallback = 0;
			currentContactStartedListener = 0;
		}
	}

	inline bool isOnStartedEnabled() {
		return currentContactStartedListener == this;
	}

	virtual void onContactStarted(btPersistentManifold* manifold) = 0;
	virtual void onContactStarted(const btCollisionObject* colObj0, const btCollisionObject* colObj1) = 0;
	virtual void onContactStarted(const int &userValue0, const int &userValue1) = 0;

	virtual void onContactStarted(btPersistentManifold* manifold, const bool &match0, const bool &match1) = 0;
	virtual void onContactStarted(const btCollisionObject* colObj0, const bool &match0, const btCollisionObject* colObj1, const bool &match1) = 0;
	virtual void onContactStarted(const int &userValue0, const bool &match0, const int &userValue1, const bool &match1) = 0;

	void enableOnEnded() {
		const int e = (events >> SHIFT_ON_ENDED) & TYPE_MASK;
		if (e == 0)
			return;
		const bool filter = (e & FILTERED) != 0;
		const bool point = (e & INCLUDE_POINT) != 0;
		const int by = (e & BY_MASK);
		currentContactEndedListener = this;
		if (by == BY_OBJECT)
			gContactEndedCallback = filter ? ContactEndedListener_CB_object_filter : ContactEndedListener_CB_object;
		else if (by == BY_VALUE)
			gContactEndedCallback = filter ? ContactEndedListener_CB_value_filter : ContactEndedListener_CB_value;
		else
			gContactEndedCallback = filter ? ContactEndedListener_CB_manifold_filter : ContactEndedListener_CB_manifold;
	}

	void disableOnEnded() {
		if (currentContactEndedListener == this) {
			gContactEndedCallback = 0;
			currentContactEndedListener = 0;
		}
	}

	bool isOnEndedEnabled() {
		return currentContactEndedListener == this;
	}

	virtual void onContactEnded(btPersistentManifold* manifold) = 0;
	virtual void onContactEnded(const btCollisionObject* colObj0, const btCollisionObject* colObj1) = 0;
	virtual void onContactEnded(const int &userValue0, const int &userValue1) = 0;

	virtual void onContactEnded(btPersistentManifold* manifold, const bool &match0, const bool &match1) = 0;
	virtual void onContactEnded(const btCollisionObject* colObj0, const bool &match0, const btCollisionObject* colObj1, const bool &match1) = 0;
	virtual void onContactEnded(const int &userValue0, const bool &match0, const int &userValue1, const bool &match1) = 0;
};

#ifndef SWIG
// ContactAdded
inline bool ContactAddedListener_CB_wrapper_point(btManifoldPoint& cp,const btCollisionObjectWrapper* colObj0Wrap,int partId0,int index0,const btCollisionObjectWrapper* colObj1Wrap,int partId1,int index1) {
	return currentContactAddedListener->onContactAdded(cp, colObj0Wrap, partId0, index0, colObj1Wrap, partId1, index1);
}
inline bool ContactAddedListener_CB_object_point(btManifoldPoint& cp,const btCollisionObjectWrapper* colObj0Wrap,int partId0,int index0,const btCollisionObjectWrapper* colObj1Wrap,int partId1,int index1) {
	return currentContactAddedListener->onContactAdded(cp, colObj0Wrap->m_collisionObject, partId0, index0, colObj1Wrap->m_collisionObject, partId1, index1);
}
inline bool ContactAddedListener_CB_value_point(btManifoldPoint& cp,const btCollisionObjectWrapper* colObj0Wrap,int partId0,int index0,const btCollisionObjectWrapper* colObj1Wrap,int partId1,int index1) {
	return currentContactAddedListener->onContactAdded(cp,
			((GdxCollisionObjectBridge*)(colObj0Wrap->m_collisionObject->getUserPointer()))->userValue,
			partId0, index0,
			((GdxCollisionObjectBridge*)(colObj1Wrap->m_collisionObject->getUserPointer()))->userValue,
			partId1, index1);
}

inline bool ContactAddedListener_CB_wrapper_filter_point(btManifoldPoint& cp,const btCollisionObjectWrapper* colObj0Wrap,int partId0,int index0,const btCollisionObjectWrapper* colObj1Wrap,int partId1,int index1) {
	const bool match0 = gdxCheckFilter(colObj0Wrap->m_collisionObject, colObj1Wrap->m_collisionObject);
	const bool match1 = gdxCheckFilter(colObj1Wrap->m_collisionObject, colObj0Wrap->m_collisionObject);
	return (!match0 && !match1) ? false :
		currentContactAddedListener->onContactAdded(cp, colObj0Wrap, partId0, index0, match0, colObj1Wrap, partId1, index1, match1);
}
inline bool ContactAddedListener_CB_object_filter_point(btManifoldPoint& cp,const btCollisionObjectWrapper* colObj0Wrap,int partId0,int index0,const btCollisionObjectWrapper* colObj1Wrap,int partId1,int index1) {
	const bool match0 = gdxCheckFilter(colObj0Wrap->m_collisionObject, colObj1Wrap->m_collisionObject);
	const bool match1 = gdxCheckFilter(colObj1Wrap->m_collisionObject, colObj0Wrap->m_collisionObject);
	return (!match0 && !match1) ? false :
		currentContactAddedListener->onContactAdded(cp, colObj0Wrap->m_collisionObject, partId0, index0, match0, colObj1Wrap->m_collisionObject, partId1, index1, match1);
}
inline bool ContactAddedListener_CB_value_filter_point(btManifoldPoint& cp,const btCollisionObjectWrapper* colObj0Wrap,int partId0,int index0,const btCollisionObjectWrapper* colObj1Wrap,int partId1,int index1) {
	const bool match0 = gdxCheckFilter(colObj0Wrap->m_collisionObject, colObj1Wrap->m_collisionObject);
	const bool match1 = gdxCheckFilter(colObj1Wrap->m_collisionObject, colObj0Wrap->m_collisionObject);
	return (!match0 && !match1) ? false :
		currentContactAddedListener->onContactAdded(cp,
				((GdxCollisionObjectBridge*)(colObj0Wrap->m_collisionObject->getUserPointer()))->userValue,
				partId0, index0, match0,
				((GdxCollisionObjectBridge*)(colObj1Wrap->m_collisionObject->getUserPointer()))->userValue,
				partId1, index1, match1);
}

inline bool ContactAddedListener_CB_wrapper(btManifoldPoint& cp,const btCollisionObjectWrapper* colObj0Wrap,int partId0,int index0,const btCollisionObjectWrapper* colObj1Wrap,int partId1,int index1) {
	return currentContactAddedListener->onContactAdded(colObj0Wrap, partId0, index0, colObj1Wrap, partId1, index1);
}
inline bool ContactAddedListener_CB_object(btManifoldPoint& cp,const btCollisionObjectWrapper* colObj0Wrap,int partId0,int index0,const btCollisionObjectWrapper* colObj1Wrap,int partId1,int index1) {
	return currentContactAddedListener->onContactAdded(colObj0Wrap->m_collisionObject, partId0, index0, colObj1Wrap->m_collisionObject, partId1, index1);
}
inline bool ContactAddedListener_CB_value(btManifoldPoint& cp,const btCollisionObjectWrapper* colObj0Wrap,int partId0,int index0,const btCollisionObjectWrapper* colObj1Wrap,int partId1,int index1) {
	return currentContactAddedListener->onContactAdded(
			((GdxCollisionObjectBridge*)(colObj0Wrap->m_collisionObject->getUserPointer()))->userValue,
			partId0, index0,
			((GdxCollisionObjectBridge*)(colObj1Wrap->m_collisionObject->getUserPointer()))->userValue,
			partId1, index1);
}

inline bool ContactAddedListener_CB_wrapper_filter(btManifoldPoint& cp,const btCollisionObjectWrapper* colObj0Wrap,int partId0,int index0,const btCollisionObjectWrapper* colObj1Wrap,int partId1,int index1) {
	const bool match0 = gdxCheckFilter(colObj0Wrap->m_collisionObject, colObj1Wrap->m_collisionObject);
	const bool match1 = gdxCheckFilter(colObj1Wrap->m_collisionObject, colObj0Wrap->m_collisionObject);
	return (!match0 && !match1) ? false :
		currentContactAddedListener->onContactAdded(colObj0Wrap, partId0, index0, match0, colObj1Wrap, partId1, index1, match1);
}
inline bool ContactAddedListener_CB_object_filter(btManifoldPoint& cp,const btCollisionObjectWrapper* colObj0Wrap,int partId0,int index0,const btCollisionObjectWrapper* colObj1Wrap,int partId1,int index1) {
	const bool match0 = gdxCheckFilter(colObj0Wrap->m_collisionObject, colObj1Wrap->m_collisionObject);
	const bool match1 = gdxCheckFilter(colObj1Wrap->m_collisionObject, colObj0Wrap->m_collisionObject);
	return (!match0 && !match1) ? false :
		currentContactAddedListener->onContactAdded(colObj0Wrap->m_collisionObject, partId0, index0, match0, colObj1Wrap->m_collisionObject, partId1, index1, match1);
}
inline bool ContactAddedListener_CB_value_filter(btManifoldPoint& cp,const btCollisionObjectWrapper* colObj0Wrap,int partId0,int index0,const btCollisionObjectWrapper* colObj1Wrap,int partId1,int index1) {
	const bool match0 = gdxCheckFilter(colObj0Wrap->m_collisionObject, colObj1Wrap->m_collisionObject);
	const bool match1 = gdxCheckFilter(colObj1Wrap->m_collisionObject, colObj0Wrap->m_collisionObject);
	return (!match0 && !match1) ? false :
		currentContactAddedListener->onContactAdded(
				((GdxCollisionObjectBridge*)(colObj0Wrap->m_collisionObject->getUserPointer()))->userValue,
				partId0, index0, match0,
				((GdxCollisionObjectBridge*)(colObj1Wrap->m_collisionObject->getUserPointer()))->userValue,
				partId1, index1, match1);
}
// ContactProcessed
inline bool ContactProcessedListener_CB_object_point(btManifoldPoint& cp,void *body0, void *body1) {
	currentContactProcessedListener->onContactProcessed(cp, (btCollisionObject*)body0, (btCollisionObject*)body1);
	return false;
}
inline bool ContactProcessedListener_CB_value_point(btManifoldPoint& cp,void *body0, void *body1) {
	currentContactProcessedListener->onContactProcessed(cp,
			((GdxCollisionObjectBridge*)(((btCollisionObject*)body0)->getUserPointer()))->userValue,
			((GdxCollisionObjectBridge*)(((btCollisionObject*)body1)->getUserPointer()))->userValue);
	return false;
}
inline bool ContactProcessedListener_CB_object_filter_point(btManifoldPoint& cp,void *body0, void *body1) {
	const bool match0 = gdxCheckFilter((btCollisionObject*)body0, (btCollisionObject*)body1);
	const bool match1 = gdxCheckFilter((btCollisionObject*)body1, (btCollisionObject*)body0);
	if (match0 || match1)
			currentContactProcessedListener->onContactProcessed(cp, (btCollisionObject*)body0, match0, (btCollisionObject*)body1, match1);
	return false;
}
inline bool ContactProcessedListener_CB_value_filter_point(btManifoldPoint& cp,void *body0, void *body1) {
	const bool match0 = gdxCheckFilter((btCollisionObject*)body0, (btCollisionObject*)body1);
	const bool match1 = gdxCheckFilter((btCollisionObject*)body1, (btCollisionObject*)body0);
	if (match0 || match1)
			currentContactProcessedListener->onContactProcessed(cp,
						((GdxCollisionObjectBridge*)(((btCollisionObject*)body0)->getUserPointer()))->userValue, match0,
						((GdxCollisionObjectBridge*)(((btCollisionObject*)body1)->getUserPointer()))->userValue, match1);
	return false;
}
inline bool ContactProcessedListener_CB_object(btManifoldPoint& cp,void *body0, void *body1) {
	currentContactProcessedListener->onContactProcessed((btCollisionObject*)body0, (btCollisionObject*)body1);
	return false;
}
inline bool ContactProcessedListener_CB_value(btManifoldPoint& cp,void *body0, void *body1) {
	currentContactProcessedListener->onContactProcessed(
			((GdxCollisionObjectBridge*)(((btCollisionObject*)body0)->getUserPointer()))->userValue,
			((GdxCollisionObjectBridge*)(((btCollisionObject*)body1)->getUserPointer()))->userValue);
	return false;
}
inline bool ContactProcessedListener_CB_object_filter(btManifoldPoint& cp,void *body0, void *body1) {
	const bool match0 = gdxCheckFilter((btCollisionObject*)body0, (btCollisionObject*)body1);
	const bool match1 = gdxCheckFilter((btCollisionObject*)body1, (btCollisionObject*)body0);
	if (match0 || match1)
			currentContactProcessedListener->onContactProcessed((btCollisionObject*)body0, match0, (btCollisionObject*)body1, match1);
	return false;
}
inline bool ContactProcessedListener_CB_value_filter(btManifoldPoint& cp,void *body0, void *body1) {
	const bool match0 = gdxCheckFilter((btCollisionObject*)body0, (btCollisionObject*)body1);
	const bool match1 = gdxCheckFilter((btCollisionObject*)body1, (btCollisionObject*)body0);
	if (match0 || match1)
			currentContactProcessedListener->onContactProcessed(
						((GdxCollisionObjectBridge*)(((btCollisionObject*)body0)->getUserPointer()))->userValue, match0,
						((GdxCollisionObjectBridge*)(((btCollisionObject*)body1)->getUserPointer()))->userValue, match1);
	return false;
}
// ContactDestroyed
inline bool ContactDestroyedListener_CB(void *userPersistentData) {
	int val;
	*(const void **)&val = userPersistentData;
	currentContactDestroyedListener->onContactDestroyed(val);
	return false;
}
// ContactStarted
inline void ContactStartedListener_CB_manifold(btPersistentManifold* const &manifold) {
	currentContactStartedListener->onContactStarted(manifold);
}
inline void ContactStartedListener_CB_object(btPersistentManifold* const &manifold) {
	currentContactStartedListener->onContactStarted(manifold->getBody0(), manifold->getBody1());
}
inline void ContactStartedListener_CB_value(btPersistentManifold* const &manifold) {
	currentContactStartedListener->onContactStarted(
		((GdxCollisionObjectBridge*)(manifold->getBody0()->getUserPointer()))->userValue,
		((GdxCollisionObjectBridge*)(manifold->getBody1()->getUserPointer()))->userValue);
}
inline void ContactStartedListener_CB_manifold_filter(btPersistentManifold* const &manifold) {
	const bool match0 = gdxCheckFilter(manifold->getBody0(), manifold->getBody1());
	const bool match1 = gdxCheckFilter(manifold->getBody1(), manifold->getBody0());
	if (match0 || match1)
		currentContactStartedListener->onContactStarted(manifold, match0, match1);
}
inline void ContactStartedListener_CB_object_filter(btPersistentManifold* const &manifold) {
	const bool match0 = gdxCheckFilter(manifold->getBody0(), manifold->getBody1());
	const bool match1 = gdxCheckFilter(manifold->getBody1(), manifold->getBody0());
	if (match0 || match1)
		currentContactStartedListener->onContactStarted(manifold->getBody0(), match0, manifold->getBody1(), match1);
}
inline void ContactStartedListener_CB_value_filter(btPersistentManifold* const &manifold) {
	const bool match0 = gdxCheckFilter(manifold->getBody0(), manifold->getBody1());
	const bool match1 = gdxCheckFilter(manifold->getBody1(), manifold->getBody0());
	if (match0 || match1)
		currentContactStartedListener->onContactStarted(
			((GdxCollisionObjectBridge*)(manifold->getBody0()->getUserPointer()))->userValue, match0,
			((GdxCollisionObjectBridge*)(manifold->getBody1()->getUserPointer()))->userValue, match1);
}
// ContactEnded
inline void ContactEndedListener_CB_manifold(btPersistentManifold* const &manifold) {
	currentContactEndedListener->onContactEnded(manifold);
}
inline void ContactEndedListener_CB_object(btPersistentManifold* const &manifold) {
	currentContactEndedListener->onContactEnded(manifold->getBody0(), manifold->getBody1());
}
inline void ContactEndedListener_CB_value(btPersistentManifold* const &manifold) {
	currentContactEndedListener->onContactEnded(
		((GdxCollisionObjectBridge*)(manifold->getBody0()->getUserPointer()))->userValue,
		((GdxCollisionObjectBridge*)(manifold->getBody1()->getUserPointer()))->userValue);
}
inline void ContactEndedListener_CB_manifold_filter(btPersistentManifold* const &manifold) {
	const bool match0 = gdxCheckFilter(manifold->getBody0(), manifold->getBody1());
	const bool match1 = gdxCheckFilter(manifold->getBody1(), manifold->getBody0());
	if (match0 || match1)
		currentContactEndedListener->onContactEnded(manifold, match0, match1);
}
inline void ContactEndedListener_CB_object_filter(btPersistentManifold* const &manifold) {
	const bool match0 = gdxCheckFilter(manifold->getBody0(), manifold->getBody1());
	const bool match1 = gdxCheckFilter(manifold->getBody1(), manifold->getBody0());
	if (match0 || match1)
		currentContactEndedListener->onContactEnded(manifold->getBody0(), match0, manifold->getBody1(), match1);
}
inline void ContactEndedListener_CB_value_filter(btPersistentManifold* const &manifold) {
	const bool match0 = gdxCheckFilter(manifold->getBody0(), manifold->getBody1());
	const bool match1 = gdxCheckFilter(manifold->getBody1(), manifold->getBody0());
	if (match0 || match1)
		currentContactEndedListener->onContactEnded(
			((GdxCollisionObjectBridge*)(manifold->getBody0()->getUserPointer()))->userValue, match0,
			((GdxCollisionObjectBridge*)(manifold->getBody1()->getUserPointer()))->userValue, match1);
}
#endif //SWIG

#endif //ContactListener_H