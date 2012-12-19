%module btConvexHullShape

%{
#include <BulletCollision/CollisionShapes/btConvexHullShape.h>
%}
%include "BulletCollision/CollisionShapes/btConvexHullShape.h"

%extend btConvexHullShape {
	btConvexHullShape(const btShapeHull *hull) {
		btConvexHullShape *result = new btConvexHullShape(); 
		for (int i = 0; i < hull->numVertices(); i++) {
			result->addPoint(hull->getVertexPointer()[i]);
		}		
		return result;
	}
};