%module btShapeHull

%{
#include <BulletCollision/CollisionShapes/btShapeHull.h>
%}
%include "BulletCollision/CollisionShapes/btShapeHull.h"


%extend btShapeHull {
	btConvexHullShape* createConvexHullShape() {
		btConvexHullShape *result = new btConvexHullShape();
		for (int i = 0; i < $self->numVertices(); i++) {
			result->addPoint($self->getVertexPointer()[i]);
		}
		return result;
	}
};