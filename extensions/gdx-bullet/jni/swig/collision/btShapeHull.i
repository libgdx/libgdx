%module btShapeHull

%ignore btShapeHull::getVertexPointer;
%ignore btShapeHull::getIndexPointer;

%{
#include <BulletCollision/CollisionShapes/btShapeHull.h>
%}

%extend btShapeHull {
    const btVector3 &getVertex(int idx) {
        return ($self->getVertexPointer()[idx]);
    }
    int getIndex(int idx) {
        return $self->getIndexPointer()[idx];
    }
};

%include "BulletCollision/CollisionShapes/btShapeHull.h"
