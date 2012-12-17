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