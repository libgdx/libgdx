#include "ContactListener.h"

ContactListener *currentContactAddedListener = 0;
ContactListener *currentContactProcessedListener = 0;
ContactListener *currentContactDestroyedListener = 0;
ContactListener *currentContactStartedListener = 0;
ContactListener *currentContactEndedListener = 0;

inline bool ContactAddedListener_CB_wrapper_point(btManifoldPoint& cp,const btCollisionObjectWrapper* colObj0Wrap,int partId0,int index0,const btCollisionObjectWrapper* colObj1Wrap,int partId1,int index1) {
	return currentContactAddedListener->onContactAdded(cp, *colObj0Wrap, partId0, index0, *colObj1Wrap, partId1, index1);
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
		currentContactAddedListener->onContactAdded(cp, *colObj0Wrap, partId0, index0, match0, *colObj1Wrap, partId1, index1, match1);
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
	return currentContactAddedListener->onContactAdded(*colObj0Wrap, partId0, index0, *colObj1Wrap, partId1, index1);
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
		currentContactAddedListener->onContactAdded(*colObj0Wrap, partId0, index0, match0, *colObj1Wrap, partId1, index1, match1);
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

ContactListener::ContactListener(bool dummy) 
	: events(0) {}

ContactListener::~ContactListener() {
	disable();
}

void ContactListener::setEvents(const int &events) {
	this->events = events;
}

void ContactListener::enable() {
	enableOnAdded();
	enableOnProcessed();
	enableOnDestroyed();
	enableOnStarted();
	enableOnEnded();
}

void ContactListener::disable() {
	disableOnAdded();
	disableOnProcessed();
	disableOnDestroyed();
	disableOnStarted();
	disableOnEnded();
}

void ContactListener::enableOnAdded() {
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

void ContactListener::disableOnAdded() {
	if (currentContactAddedListener == this) {
		gContactAddedCallback = 0;
		currentContactAddedListener = 0;
	}
}

bool ContactListener::isOnAddedEnabled() {
	return currentContactAddedListener == this;
}

void ContactListener::enableOnProcessed() {
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

void ContactListener::disableOnProcessed() {
	if (currentContactProcessedListener == this) {
		gContactProcessedCallback = 0;
		currentContactProcessedListener = 0;
	}
}

bool ContactListener::isOnProcessedEnabled() {
	return currentContactProcessedListener == this;
}

void ContactListener::enableOnDestroyed() {
	const int e = (events >> SHIFT_ON_DESTROYED) & TYPE_MASK;
	if (e == 0)
		return;
	const bool filter = (e & FILTERED) != 0;
	const bool point = (e & INCLUDE_POINT) != 0;
	const int by = (e & BY_MASK);
	currentContactDestroyedListener = this;
	gContactDestroyedCallback = ContactDestroyedListener_CB;
}

void ContactListener::disableOnDestroyed() {
	if (currentContactDestroyedListener == this) {
		gContactDestroyedCallback = 0;
		currentContactDestroyedListener = 0;
	}
}

bool ContactListener::isOnDestroyedEnabled() {
	return currentContactDestroyedListener == this;
}

void ContactListener::enableOnStarted() {
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

void ContactListener::disableOnStarted() {
	if (currentContactStartedListener == this) {
		gContactStartedCallback = 0;
		currentContactStartedListener = 0;
	}
}

bool ContactListener::isOnStartedEnabled() {
	return currentContactStartedListener == this;
}
	
void ContactListener::enableOnEnded() {
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

void ContactListener::disableOnEnded() {
	if (currentContactEndedListener == this) {
		gContactEndedCallback = 0;
		currentContactEndedListener = 0;
	}
}

bool ContactListener::isOnEndedEnabled() {
	return currentContactEndedListener == this;
}