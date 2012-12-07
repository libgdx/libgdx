/**
 * Maintain a copy of the data
 */
%module btIndexedMesh

%{
#include <BulletCollision/CollisionShapes/btTriangleIndexVertexArray.h>
%}
%include "BulletCollision/CollisionShapes/btTriangleIndexVertexArray.h"

%extend btIndexedMesh {
	void setTriangleIndexBase(short data[], unsigned long size) {
		short *indices = new short[size];
		memcpy(indices, (short*)data, size*sizeof(short));
		$self->m_triangleIndexBase = (unsigned char*)indices;
	}
	void setVertexBase(float data[], unsigned long size) {
		float *vertices = new float[size];
		memcpy(vertices, (float*)data, size*sizeof(float));
		$self->m_vertexBase = (unsigned char*)vertices;
	}
	// Note that checking for NULL doesn't actually matters
	~btIndexedMesh() {
		if ($self->m_triangleIndexBase != NULL) {
			short *indices = (short*)$self->m_triangleIndexBase;
			delete[] indices;			
			$self->m_triangleIndexBase = NULL;
		}
		if ($self->m_vertexBase != NULL) {
			float *vertices = (float*)$self->m_vertexBase;
			delete[] vertices;
			$self->m_vertexBase = NULL;
		}
		delete $self;
	}
};