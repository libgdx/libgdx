%module btSoftBody

%typemap(javacode) Element %{
	public void takeOwnership() {
		swigCMemOwn = true;
	}
	
	public void releaseOwnership() {
		swigCMemOwn = false;
	}
%}

struct	sCti
{
	const btCollisionObject*	m_colObj;		 
	btVector3		m_normal;	 
	btScalar		m_offset;	 
};
struct	sMedium
{
	btVector3		m_velocity;	 
	btScalar		m_pressure;	 
	btScalar		m_density;	 
};
struct	Element
{
	void*			m_tag;			// User data
	Element() : m_tag(0) {}
};
struct	Material : Element
{
	btScalar				m_kLST;			// Linear stiffness coefficient [0,1]
	btScalar				m_kAST;			// Area/Angular stiffness coefficient [0,1]
	btScalar				m_kVST;			// Volume stiffness coefficient [0,1]
	int						m_flags;		// Flags
};
struct	Feature : Element
{
	Material*				m_material;		// Material
};
struct	Node : Feature
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
struct	Link : Feature
{
	Node*					m_n[2];			// Node pointers
	btScalar		m_rl;			// Rest length		
	int						m_bbending:1;	// Bending link
	btScalar				m_c0;			// (ima+imb)*kLST
	btScalar				m_c1;			// rl^2
	btScalar				m_c2;			// |gradient|^2/c0
	btVector3				m_c3;			// gradient
};
struct	Face : Feature
{
	Node*			m_n[3];			// Node pointers
	btVector3				m_normal;		// Normal
	btScalar				m_ra;			// Rest area
	btDbvtNode*				m_leaf;			// Leaf data
};
/*struct eAeroModel { enum _ {
	V_Point,			///Vertex normals are oriented toward velocity
	V_TwoSided,			///Vertex normals are flipped to match velocity	
	V_TwoSidedLiftDrag, ///Vertex normals are flipped to match velocity and lift and drag forces are applied
	V_OneSided,			///Vertex normals are taken as it is	
	F_TwoSided,			///Face normals are flipped to match velocity
	F_TwoSidedLiftDrag,	///Face normals are flipped to match velocity and lift and drag forces are applied 
	F_OneSided,			///Face normals are taken as it is		
	END
};};
struct	Config
{
	eAeroModel::_			aeromodel;		// Aerodynamic model (default: V_Point)
	btScalar				kVCF;			// Velocities correction factor (Baumgarte)
	btScalar				kDP;			// Damping coefficient [0,1]
	btScalar				kDG;			// Drag coefficient [0,+inf]
	btScalar				kLF;			// Lift coefficient [0,+inf]
	btScalar				kPR;			// Pressure coefficient [-inf,+inf]
	btScalar				kVC;			// Volume conversation coefficient [0,+inf]
	btScalar				kDF;			// Dynamic friction coefficient [0,1]
	btScalar				kMT;			// Pose matching coefficient [0,1]		
	btScalar				kCHR;			// Rigid contacts hardness [0,1]
	btScalar				kKHR;			// Kinetic contacts hardness [0,1]
	btScalar				kSHR;			// Soft contacts hardness [0,1]
	btScalar				kAHR;			// Anchors hardness [0,1]
	btScalar				kSRHR_CL;		// Soft vs rigid hardness [0,1] (cluster only)
	btScalar				kSKHR_CL;		// Soft vs kinetic hardness [0,1] (cluster only)
	btScalar				kSSHR_CL;		// Soft vs soft hardness [0,1] (cluster only)
	btScalar				kSR_SPLT_CL;	// Soft vs rigid impulse split [0,1] (cluster only)
	btScalar				kSK_SPLT_CL;	// Soft vs rigid impulse split [0,1] (cluster only)
	btScalar				kSS_SPLT_CL;	// Soft vs rigid impulse split [0,1] (cluster only)
	btScalar				maxvolume;		// Maximum volume ratio for pose
	btScalar				timescale;		// Time scale
	int						viterations;	// Velocities solver iterations
	int						piterations;	// Positions solver iterations
	int						diterations;	// Drift solver iterations
	int						citerations;	// Cluster solver iterations
	int						collisions;		// Collisions flags
	tVSolverArray			m_vsequence;	// Velocity solvers sequence
	tPSolverArray			m_psequence;	// Position solvers sequence
	tPSolverArray			m_dsequence;	// Drift solvers sequence
};*/

%nestedworkaround btSoftBody::sCti;
%nestedworkaround btSoftBody::sMedium;
%nestedworkaround btSoftBody::Element;
%nestedworkaround btSoftBody::Material;
%nestedworkaround btSoftBody::Feature;
%nestedworkaround btSoftBody::Node;
%nestedworkaround btSoftBody::Link;
%nestedworkaround btSoftBody::Face;
//%nestedworkaround btSoftBody::Config;
//%nestedworkaround btSoftBody::eAeroModel;

%template(btSparseSdf3) btSparseSdf<3>;

%{
#include <BulletSoftBody/btSoftBody.h>
%}
%include "BulletSoftBody/btSoftBody.h"

%extend btSoftBody {
	
	/*int offset = posOffset / sizeof(btScalar);
			int size = vertexSize / sizeof(btScalar);
			btAlignedObjectArray<btVector3>	vtx;
			vtx.resize(vertexCount);
			for (int i = 0; i < vertexCount; i++) {
				const int o = i*size+offset;
				vtx[i] = btVector3(vertices[o], vertices[o+1], vertices[o+2]);
			}
			btSoftBody *result = new btSoftBody(worldInfo, vtx.size(), &vtx[0], 0);*/
	
	/**
	 * vertexCount: the amount of vertices
	 * vertexSize: the size in bytes of one vertex
	 * posOffset: the offset within a vertex to the position
	 * triangleCount: the amount of triangle (size per triangle = 3 * sizeof(short))
	 */
	btSoftBody(btSoftBodyWorldInfo *worldInfo, float *vertices, int vertexCount, int vertexSize, int posOffset, short *indices, int triangleCount) {
		int offset = posOffset / sizeof(btScalar);
		int size = vertexSize / sizeof(btScalar);
		btAlignedObjectArray<btVector3>	vtx;
		vtx.resize(vertexCount);
		for (int i = 0; i < vertexCount; i++) {
			const int o = i*size+offset;
			vtx[i] = btVector3(vertices[o], vertices[o+1], vertices[o+2]);
		}
		btSoftBody *result = new btSoftBody(worldInfo, vtx.size(), &vtx[0], 0);
		
		/*btSoftBody *result = new btSoftBody(worldInfo, vertexCount, 0, 0);
		for (int i = 0; i < vertexCount; i++) {
			const int o = i*size+offset;
			result->m_nodes[i].m_x.m_floats[0] = vertices[o];
			result->m_nodes[i].m_x.m_floats[1] = vertices[o+1];
			result->m_nodes[i].m_x.m_floats[2] = vertices[o+2];
		}*/
		
		btAlignedObjectArray<bool> chks;
		chks.resize(vertexCount * vertexCount, false);
		for(int i=0, ni=triangleCount*3;i<ni;i+=3)
		{
			const int idx[]={indices[i],indices[i+1],indices[i+2]};
#define IDX(_x_,_y_) ((_y_)*vertexCount+(_x_))
			for(int j=2,k=0;k<3;j=k++)
			{
				if(!chks[IDX(idx[j],idx[k])])
				{
					chks[IDX(idx[j],idx[k])]=true;
					chks[IDX(idx[k],idx[j])]=true;
					result->appendLink(idx[j],idx[k]);
				}
			}
#undef IDX
			result->appendFace(idx[0],idx[1],idx[2]);
		}
		
		result->updateBounds();
		return result;
	}
	
	int getNodeCount() {
		return $self->m_nodes.size();
	}
	
	btSoftBody::Node *getNode(int idx) {
		return &($self->m_nodes[idx]);
	}
	
	/*
	 * buffer: must be at least vertexCount*vertexSize big
	 * vertexCount: the amount of vertices to copy (must be equal to or less than getNodeCount())
	 * vertexSize: the size in bytes of one vertex (must be dividable by sizeof(btScalar))
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
	
	btSoftBody::Face *getFace(int idx) {
		return &($self->m_faces[idx]);
	}
	
	void getIndices(short *buffer, int triangleCount) {
		const size_t nodeSize = sizeof(btSoftBody::Node);
		const intptr_t nodeOffset = (intptr_t)(&self->m_nodes[0]);
		for (int i = 0; i < triangleCount; i++) {
			const int idx = i * 3;
			buffer[idx] = ((intptr_t)(self->m_faces[i].m_n[0]) - nodeOffset) / nodeSize;
			buffer[idx+1] = ((intptr_t)(self->m_faces[i].m_n[1]) - nodeOffset) / nodeSize;
			buffer[idx+2] = ((intptr_t)(self->m_faces[i].m_n[2]) - nodeOffset) / nodeSize;
		}
	}

	void setConfig_kVCF(btScalar v) { $self->m_cfg.kVCF = v; }
	void setConfig_kDP(btScalar v) { $self->m_cfg.kDP = v; }
	void setConfig_kDG(btScalar v) { $self->m_cfg.kDG = v; }
	void setConfig_kLF(btScalar v) { $self->m_cfg.kLF = v; }
	void setConfig_kPR(btScalar v) { $self->m_cfg.kPR = v; }
	void setConfig_kVC(btScalar v) { $self->m_cfg.kVC = v; }
	void setConfig_kDF(btScalar v) { $self->m_cfg.kDF = v; }
	void setConfig_kMT(btScalar v) { $self->m_cfg.kMT = v; }
	void setConfig_kCHR(btScalar v) { $self->m_cfg.kCHR = v; }
	void setConfig_kKHR(btScalar v) { $self->m_cfg.kKHR = v; }
	void setConfig_kSHR(btScalar v) { $self->m_cfg.kSHR = v; }
	void setConfig_kAHR(btScalar v) { $self->m_cfg.kAHR = v; }
	void setConfig_kSRHR_CL(btScalar v) { $self->m_cfg.kSRHR_CL = v; }
	void setConfig_kSKHR_CL(btScalar v) { $self->m_cfg.kSKHR_CL = v; }
	void setConfig_kSSHR_CL(btScalar v) { $self->m_cfg.kSSHR_CL = v; }
	void setConfig_kSR_SPLT_CL(btScalar v) { $self->m_cfg.kSR_SPLT_CL = v; }
	void setConfig_kSK_SPLT_CL(btScalar v) { $self->m_cfg.kSK_SPLT_CL = v; }
	void setConfig_kSS_SPLT_CL(btScalar v) { $self->m_cfg.kSS_SPLT_CL = v; }
	void setConfig_maxvolume(btScalar v) { $self->m_cfg.maxvolume = v; }
	void setConfig_ktimescale(btScalar v) { $self->m_cfg.timescale = v; }
	void setConfig_viterations(int v) { $self->m_cfg.viterations = v; }
	void setConfig_piterations(int v) { $self->m_cfg.piterations = v; }
	void setConfig_diterations(int v) { $self->m_cfg.diterations = v; }
	void setConfig_citerations(int v) { $self->m_cfg.citerations = v; }
	void setConfig_collisions(int v) { $self->m_cfg.collisions = v; }
};


%{
	typedef btSoftBody::sCti sCti;
	typedef btSoftBody::sMedium sMedium;
	typedef btSoftBody::Element Element;
	typedef btSoftBody::Material Material;
	typedef btSoftBody::Feature Feature;
	typedef btSoftBody::Node Node;
	typedef btSoftBody::Link Link;
	typedef btSoftBody::Face Face;
	//typedef btSoftBody::eAeroModel eAeroModel;
	//typedef btSoftBody::Config Config;
%}
/*
%ignore btAlignedObjectArray<btSoftBodyFace>::findBinarySearch;
%ignore btAlignedObjectArray<btSoftBodyFace>::findLinearSearch;
%ignore btAlignedObjectArray<btSoftBodyNode>::findBinarySearch;
%ignore btAlignedObjectArray<btSoftBodyNode>::findLinearSearch;
%template(btSoftBodyFaceAlignedObjectArray) btAlignedObjectArray<btSoftBodyFace>;
%template(btSoftBodyNodeAlignedObjectArray) btAlignedObjectArray<btSoftBodyNode>;
*/