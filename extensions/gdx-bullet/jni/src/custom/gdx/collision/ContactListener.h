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
#include "../../../bullet/BulletCollision/CollisionDispatch/btCollisionObject.h"
#include "../../../bullet/BulletCollision/CollisionDispatch/btCollisionObjectWrapper.h"
#include "../../../bullet/BulletCollision/CollisionDispatch/btManifoldResult.h"
#include "../../../bullet/BulletCollision/NarrowPhaseCollision/btPersistentManifold.h"

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

/** @author Xoppa */
class ContactListener {
protected:
#ifndef SWIG
	int events;
#endif
public:
	ContactListener(bool dummy);
	virtual ~ContactListener();

#ifndef SWIG
	void setEvents(const int &events);
#endif //SWIG

	void enable();
	void disable();

	void enableOnAdded();
	void disableOnAdded();
	bool isOnAddedEnabled();

	virtual bool onContactAdded(btManifoldPoint& cp,const btCollisionObjectWrapper &colObj0Wrap,int partId0,int index0,
			const btCollisionObjectWrapper &colObj1Wrap,int partId1,int index1) = 0;
	virtual bool onContactAdded(btManifoldPoint& cp,const btCollisionObject* colObj0,int partId0,int index0,
				const btCollisionObject* colObj1,int partId1,int index1) = 0;
	virtual bool onContactAdded(btManifoldPoint& cp,int userValue0,int partId0,int index0,
			int userValue1,int partId1,int index1) = 0;

	virtual bool onContactAdded(btManifoldPoint& cp,const btCollisionObjectWrapper &colObj0Wrap,int partId0,int index0,bool match0,
			const btCollisionObjectWrapper &colObj1Wrap,int partId1,int index1,bool match1) = 0;
	virtual bool onContactAdded(btManifoldPoint& cp,const btCollisionObject* colObj0,int partId0,int index0,bool match0,
				const btCollisionObject* colObj1,int partId1,int index1,bool match1) = 0;
	virtual bool onContactAdded(btManifoldPoint& cp,int userValue0,int partId0,int index0,bool match0,
			int userValue1,int partId1,int index1,bool match1) = 0;

	virtual bool onContactAdded(const btCollisionObjectWrapper &colObj0Wrap,int partId0,int index0,
			const btCollisionObjectWrapper &colObj1Wrap,int partId1,int index1) = 0;
	virtual bool onContactAdded(const btCollisionObject* colObj0,int partId0,int index0,
				const btCollisionObject* colObj1,int partId1,int index1) = 0;
	virtual bool onContactAdded(int userValue0,int partId0,int index0,
			int userValue1,int partId1,int index1) = 0;

	virtual bool onContactAdded(const btCollisionObjectWrapper &colObj0Wrap,int partId0,int index0,bool match0,
			const btCollisionObjectWrapper &colObj1Wrap,int partId1,int index1,bool match1) = 0;
	virtual bool onContactAdded(const btCollisionObject* colObj0,int partId0,int index0,bool match0,
				const btCollisionObject* colObj1,int partId1,int index1,bool match1) = 0;
	virtual bool onContactAdded(int userValue0,int partId0,int index0,bool match0,
			int userValue1,int partId1,int index1,bool match1) = 0;

	void enableOnProcessed();
	void disableOnProcessed();
	bool isOnProcessedEnabled();

	virtual void onContactProcessed(btManifoldPoint& cp, const btCollisionObject* colObj0, const btCollisionObject* colObj1) = 0;
	virtual void onContactProcessed(btManifoldPoint& cp, int userValue0, int userValue1) = 0;

	virtual void onContactProcessed(btManifoldPoint& cp, const btCollisionObject* colObj0, bool match0, const btCollisionObject* colObj1, bool match1) = 0;
	virtual void onContactProcessed(btManifoldPoint& cp, int userValue0, bool match0, int userValue1, bool match1) = 0;

	virtual void onContactProcessed(const btCollisionObject* colObj0, const btCollisionObject* colObj1) = 0;
	virtual void onContactProcessed(int userValue0, int userValue1) = 0;

	virtual void onContactProcessed(const btCollisionObject* colObj0, bool match0, const btCollisionObject* colObj1, bool match1) = 0;
	virtual void onContactProcessed(int userValue0, bool match0, int userValue1, bool match1) = 0;

	void enableOnDestroyed();
	void disableOnDestroyed();
	bool isOnDestroyedEnabled();

	virtual void onContactDestroyed(int manifoldPointUserValue) = 0;

	void enableOnStarted();
	void disableOnStarted();
	bool isOnStartedEnabled();

	virtual void onContactStarted(btPersistentManifold* manifold) = 0;
	virtual void onContactStarted(const btCollisionObject* colObj0, const btCollisionObject* colObj1) = 0;
	virtual void onContactStarted(const int &userValue0, const int &userValue1) = 0;

	virtual void onContactStarted(btPersistentManifold* manifold, const bool &match0, const bool &match1) = 0;
	virtual void onContactStarted(const btCollisionObject* colObj0, const bool &match0, const btCollisionObject* colObj1, const bool &match1) = 0;
	virtual void onContactStarted(const int &userValue0, const bool &match0, const int &userValue1, const bool &match1) = 0;

	void enableOnEnded();
	void disableOnEnded();
	bool isOnEndedEnabled();

	virtual void onContactEnded(btPersistentManifold* manifold) = 0;
	virtual void onContactEnded(const btCollisionObject* colObj0, const btCollisionObject* colObj1) = 0;
	virtual void onContactEnded(const int &userValue0, const int &userValue1) = 0;

	virtual void onContactEnded(btPersistentManifold* manifold, const bool &match0, const bool &match1) = 0;
	virtual void onContactEnded(const btCollisionObject* colObj0, const bool &match0, const btCollisionObject* colObj1, const bool &match1) = 0;
	virtual void onContactEnded(const int &userValue0, const bool &match0, const int &userValue1, const bool &match1) = 0;
};

#endif //ContactListener_H