/**
 * Maintain a copy of the data
 */
%module btIndexedMesh

%{
#include <BulletCollision/CollisionShapes/btTriangleIndexVertexArray.h>
%}
%include "BulletCollision/CollisionShapes/btTriangleIndexVertexArray.h"

%extend btIndexedMesh {
	void setTriangleIndexBase(short *data) {
		$self->m_triangleIndexBase = (unsigned char*)data;
	}
	void setVertexBase(float *data) {
		$self->m_vertexBase = (unsigned char*)data;
	}
};