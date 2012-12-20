%module btSoftBody
/*
struct	btSoftBodysCti
{
	const btCollisionObject*	m_colObj;		 
	btVector3		m_normal;	 
	btScalar		m_offset;	 
};	


struct	btSoftBodysMedium
{
	btVector3		m_velocity;	 
	btScalar		m_pressure;	 
	btScalar		m_density;	 
};
struct	btSoftBodyElement
{
	void*			m_tag;			// User data
	btSoftBodyElement() : m_tag(0) {}
};
struct	btSoftBodyMaterial : btSoftBodyElement
{
	btScalar				m_kLST;			// Linear stiffness coefficient [0,1]
	btScalar				m_kAST;			// Area/Angular stiffness coefficient [0,1]
	btScalar				m_kVST;			// Volume stiffness coefficient [0,1]
	int						m_flags;		// Flags
};
struct	btSoftBodyFeature : btSoftBodyElement
{
	btSoftBodyMaterial*				m_material;		// Material
};
struct	btSoftBodyNode : btSoftBodyFeature
{
	btVector3				m_x;			// Position
	btVector3				m_q;			// Previous step position
	btVector3				m_v;			// Velocity
	btVector3				m_f;			// Force accumulator
	btVector3				m_n;			// Normal
	btScalar				m_im;			// 1/mass
	btScalar				m_area;			// Area
	btDbvtNode*				m_leaf;			// Leaf data
	int						m_battach:1;	// Attached
};
struct	btSoftBodyLink : btSoftBodyFeature
{
	btSoftBodyNode*					m_n[2];			// Node pointers
	btScalar		m_rl;			// Rest length		
	int						m_bbending:1;	// Bending link
	btScalar				m_c0;			// (ima+imb)*kLST
	btScalar				m_c1;			// rl^2
	btScalar				m_c2;			// |gradient|^2/c0
	btVector3				m_c3;			// gradient
};
struct	btSoftBodyFace : btSoftBodyFeature
{
	btSoftBodyNode*			m_n[3];			// Node pointers
	btVector3				m_normal;		// Normal
	btScalar				m_ra;			// Rest area
	btDbvtNode*				m_leaf;			// Leaf data
};

%nestedworkaround btSoftBody::sCti;
%nestedworkaround btSoftBody::sMedium;
%nestedworkaround btSoftBody::Element;
%nestedworkaround btSoftBody::Material;
%nestedworkaround btSoftBody::Feature;
%nestedworkaround btSoftBody::Node;
%nestedworkaround btSoftBody::Link;
%nestedworkaround btSoftBody::Face;
*/

%template(btSparseSdf3) btSparseSdf<3>;

%{
#include <BulletSoftBody/btSoftBody.h>
%}
%include "BulletSoftBody/btSoftBody.h"

%extend btSoftBody {
	int getNodeCount() {
		return $self->m_nodes.size();
	}
	/*
	 * buffer: must be at least vertexCount*vertexSize big
	 * vertexCount: the amount of vertices to copy (must be equal to or less than getNodeCount())
	 * vertexSize: the size in byes of one vertex (must be dividable by sizeof(btScalar))
	 * posOffset: the offset within a vertex to the position (must be dividable by sizeof(btScalar))
	 */
	void getVertices(btScalar *buffer, int vertexCount, int vertexSize, int posOffset) {
		int offset = posOffset / (sizeof(btScalar));
		int size = vertexSize / (sizeof(btScalar));
		for (int i = 0; i < vertexCount; i++) {
			const int o = i*size+offset;
			const float *src = $self->m_nodes[i].m_x.m_floats;
			buffer[o] = src[0];
			buffer[o+1] = src[1];
			buffer[o+2] = src[2];
		}
	}
	
	int getFaceCount() {
		return $self->m_faces.size();
	}
	
	void getIndices(short *buffer, int triangleCount) {
		const size_t nodeSize = sizeof(btSoftBody::Node);
		const intptr_t nodeOffset = (long)(&self->m_nodes[0]);
		for (int i = 0; i < triangleCount; i++) {
			const int idx = i * 3;
			buffer[idx] = ((intptr_t)(self->m_faces[i].m_n[0]) - nodeOffset) / nodeSize;
			buffer[idx+1] = ((intptr_t)(self->m_faces[i].m_n[1]) - nodeOffset) / nodeSize;
			buffer[idx+2] = ((intptr_t)(self->m_faces[i].m_n[2]) - nodeOffset) / nodeSize;
		}
	}
};

/*
%{
	typedef btSoftBody::sCti btSoftBodysCti;
	typedef btSoftBody::sMedium btSoftBodysMedium;
	typedef btSoftBody::Element btSoftBodyElement;
	typedef btSoftBody::Material btSoftBodyMaterial;
	typedef btSoftBody::Feature btSoftBodyFeature;
	typedef btSoftBody::Node btSoftBodyNode;
	typedef btSoftBody::Link btSoftBodyLink;
	typedef btSoftBody::Face btSoftBodyFace;
%}

%ignore btAlignedObjectArray<btSoftBodyFace>::findBinarySearch;
%ignore btAlignedObjectArray<btSoftBodyFace>::findLinearSearch;
%ignore btAlignedObjectArray<btSoftBodyNode>::findBinarySearch;
%ignore btAlignedObjectArray<btSoftBodyNode>::findLinearSearch;
%template(btSoftBodyFaceAlignedObjectArray) btAlignedObjectArray<btSoftBodyFace>;
%template(btSoftBodyNodeAlignedObjectArray) btAlignedObjectArray<btSoftBodyNode>;
*/