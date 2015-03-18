#include "ContactCache.h"

ContactCache *currentContactCache = 0;

inline void ContactCacheStarted_CB(btPersistentManifold* const &manifold) {
	currentContactCache->contactStarted(manifold);
}
inline void ContactCacheEnded_CB(btPersistentManifold* const &manifold) {
	currentContactCache->contactEnded(manifold);
}

struct ContactPair {
	const btCollisionObject *object0;
	const btCollisionObject *object1;
	float time;

	ContactPair() : object0(0), object1(0), time(0) {}

	ContactPair(const ContactPair &rhs) : object0(rhs.object0), object1(rhs.object1), time(rhs.time) {}

	ContactPair(const btCollisionObject* const &object0, const btCollisionObject* const &object1, const float &time) : object0(object0), object1(object1), time(time) {}

	ContactPair &operator=(const ContactPair &rhs) {
		object0 = rhs.object0;
		object1 = rhs.object1;
		time = rhs.time;
		return *this;
	}

	inline bool operator==(const ContactPair &rhs) const {
		return ((rhs.object0 == object0) && (rhs.object1 == object1)) || ((rhs.object0 == object1) && (rhs.object1 == object0));
	}

	inline bool operator<(const ContactPair &rhs) const {
		if (*this == rhs)
			return false;
		return object0 < rhs.object0;
	}

	inline bool equals(const btCollisionObject* const &obj0, const btCollisionObject* const &obj1) const {
		return ((obj0 == object0) && (obj1 == object1)) || ((obj0 == object1) && (obj1 == object0));
	}
};

ContactCache::ContactCache(bool dummy)
	: events(0), cacheTime(0.2f), filter(true) {}

ContactCache::~ContactCache() {
	disable();
}

void ContactCache::setEvents(const int &events) {
	this->events = events;
}

void ContactCache::enable() {
	currentContactCache = this;
	gContactStartedCallback = ContactCacheStarted_CB;
	gContactEndedCallback = ContactCacheEnded_CB;
}

void ContactCache::disable() {
	if (currentContactCache == this) {
		currentContactCache = 0;
		if (gContactStartedCallback == ContactCacheStarted_CB)
			gContactStartedCallback = 0;
		if (gContactEndedCallback == ContactCacheEnded_CB)
			gContactEndedCallback = 0;
	}
}

bool ContactCache::isEnabled() {
	return (currentContactCache == this) && (gContactStartedCallback == ContactCacheStarted_CB) && (gContactEndedCallback == ContactCacheEnded_CB);
}

void ContactCache::clear() {
	cache.clear();
}

void ContactCache::update(float delta) {
	for (int i = cache.size() - 1; i >= 0; --i) {
		ContactPair &pair = cache.at(i);
		if ((pair.time -= delta) < 0) {
			const btCollisionObject* const &object0 = pair.object0;
			const btCollisionObject* const &object1 = pair.object1;
			const bool match0 = gdxCheckFilter(object0, object1);
			const bool match1 = gdxCheckFilter(object1, object0);
			if (!filter || match0 || match1)
				onContactEnded(object0, match0, object1, match1);
			cache.swap(i, cache.size()-1);
			cache.pop_back();
		}
	}
}

int ContactCache::indexOf(const btCollisionObject* const &obj0, const btCollisionObject* const &obj1) {
	for (int i = cache.size() - 1; i >= 0; --i) {
		ContactPair &pair = cache.at(i);
		if (pair.equals(obj0, obj1))
			return i;
	}
	return -1;
}

void ContactCache::contactStarted(btPersistentManifold* manifold) {
	const bool match0 = gdxCheckFilter(manifold->getBody0(), manifold->getBody1());
	const bool match1 = gdxCheckFilter(manifold->getBody1(), manifold->getBody0());
	if (filter && !match0 && !match1)
		return;
	const int idx = indexOf(manifold->getBody0(), manifold->getBody1());
	if (idx >= 0) {
		cache.swap(idx, cache.size()-1);
		cache.pop_back();
	}
	else
		onContactStarted(manifold, match0, match1);
}

void ContactCache::contactEnded(btPersistentManifold* manifold) {
	const bool match0 = gdxCheckFilter(manifold->getBody0(), manifold->getBody1());
	const bool match1 = gdxCheckFilter(manifold->getBody1(), manifold->getBody0());
	if (filter && !match0 && !match1)
		return;
	const int idx = indexOf(manifold->getBody0(), manifold->getBody1());
	if (idx >= 0)
		cache[idx].time = cacheTime;
	else
		cache.push_back(ContactPair(manifold->getBody0(), manifold->getBody1(), cacheTime));
}
