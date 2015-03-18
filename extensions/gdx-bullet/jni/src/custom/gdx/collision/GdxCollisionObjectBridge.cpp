#include "GdxCollisionObjectBridge.h"

#ifndef GdxCollsionObjectBridge_H
#define GdxCollsionObjectBridge_H

/** @author Xoppa */
class GdxCollisionObjectBridge {
public:
	int userValue;
	int contactCallbackFlag;
	int contactCallbackFilter;

	GdxCollisionObjectBridge::GdxCollisionObjectBridge() : userValue(0), contactCallbackFlag(1), contactCallbackFilter(0)
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
