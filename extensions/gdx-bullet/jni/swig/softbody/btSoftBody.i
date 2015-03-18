%module btSoftBody

%template(btSparseSdf3) btSparseSdf<3>;

%typemap(javaimports) btSoftBody %{
import com.badlogic.gdx.physics.bullet.BulletBase;
import com.badlogic.gdx.physics.bullet.linearmath.*;
import com.badlogic.gdx.physics.bullet.collision.*;
import com.badlogic.gdx.physics.bullet.dynamics.*;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.g3d.model.MeshPart;
%}

%{
#include <BulletSoftBody/btSoftBody.h>
%}

%ignore btSoftBody::getWorldInfo;
%ignore btSoftBody::getRestLengthScale;
%ignore btSoftBody::setRestLengthScale;
%ignore btSoftBody::getWindVelocity;
%ignore btSoftBody::setSoftBodySolver;
%ignore btSoftBody::getSoftBodySolver;

%include "BulletSoftBody/btSoftBody.h"

%extend btSoftBody {
	/**
	 * vertexCount: the amount of vertices
	 * vertexSize: the size in bytes of one vertex
	 * posOffset: the offset within a vertex to the position
	 * normalOffset: the offset within a vertex to the normal or negative if none
	 * triangleCount: the amount of triangle (size per triangle = 3 * sizeof(short))
	 */
	btSoftBody(btSoftBodyWorldInfo *worldInfo, float *vertices, int vertexSize, int posOffset, int normalOffset, short *indices, int indexOffset, int numVertices, short *indexMap, int indexMapOffset) {		
		int poffset = posOffset / sizeof(btScalar);
		int noffset = normalOffset / sizeof(btScalar);
		int size = vertexSize / sizeof(btScalar);
		btAlignedObjectArray<btVector3>	points;
		
		btSoftBody *result = new btSoftBody(worldInfo);
		btSoftBody::Material* pm = result->appendMaterial();
		pm->m_kLST = 1;
		pm->m_kAST = 1;
		pm->m_kVST = 1;
		pm->m_flags = btSoftBody::fMaterial::Default;
		
		const btScalar margin = result->getCollisionShape()->getMargin();
		int nodeCount = 0;
		result->m_nodes.resize(numVertices);
		for (int i = 0; i < numVertices; i++) {
			const float * const &verts = &vertices[indices[indexOffset+i]*size+poffset];
			btVector3 point(verts[0], verts[1], verts[2]);
			int idx = -1;
			for (int j = 0; j < nodeCount; j++) {
				if (result->m_nodes[j].m_x==point) {
					idx = j;
					break;
				}
			}
			if (idx < 0) {
				btSoftBody::Node &node = result->m_nodes[nodeCount];
				memset(&node,0,sizeof(btSoftBody::Node));
				node.m_x = point;
				node.m_q = node.m_x;
				node.m_im = 1;
				node.m_leaf = result->m_ndbvt.insert(btDbvtVolume::FromCR(node.m_x,margin),&node);
				node.m_material = pm;
				if (noffset >= 0) {
					node.m_n.m_floats[0] = vertices[indices[indexOffset+i]*size+noffset];
					node.m_n.m_floats[1] = vertices[indices[indexOffset+i]*size+noffset+1];
					node.m_n.m_floats[2] = vertices[indices[indexOffset+i]*size+noffset+2];
				}
				points.push_back(point);
				idx = nodeCount;
				nodeCount++;
			}
			indexMap[indexMapOffset+i] = (short)idx; 
		}
		result->m_nodes.resize(nodeCount);
		
		//const int vertexCount = points.size();
		//btSoftBody *result = new btSoftBody(worldInfo, vertexCount, &points[0], 0);
		
		btAlignedObjectArray<bool> chks;
		chks.resize(nodeCount * nodeCount, false);
		for(int i=0; i<numVertices; i+=3)
		{
			const int idx[]={indexMap[indexMapOffset+i],indexMap[indexMapOffset+i+1],indexMap[indexMapOffset+i+2]};
#define IDX(_x_,_y_) ((_y_)*nodeCount+(_x_))
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
	
	void getVertices(float *vertices, int vertexSize, int posOffset, short *indices, int indexOffset, int numVertices, short *indexMap, int indexMapOffset) {
		int poffset = posOffset / (sizeof(btScalar));
		int size = vertexSize / (sizeof(btScalar));
		for (int i = 0; i < numVertices; i++) {
			const int vidx = indices[indexOffset+i]*size+poffset;
			const int pidx = indexMap[indexMapOffset+i];
			const float * const &point = $self->m_nodes[pidx].m_x.m_floats;
			vertices[vidx  ] = point[0];
			vertices[vidx+1] = point[1];
			vertices[vidx+2] = point[2];
		}
	}
	
	void getVertices(float *vertices, int vertexSize, int posOffset, int normalOffset, short *indices, int indexOffset, int numVertices, short *indexMap, int indexMapOffset) {
		int poffset = posOffset / (sizeof(btScalar));
		int noffset = normalOffset / (sizeof(btScalar));
		int size = vertexSize / (sizeof(btScalar));
		for (int i = 0; i < numVertices; i++) {
			const int vidx = indices[indexOffset+i]*size+poffset;
			const int nidx = indices[indexOffset+i]*size+noffset;
			const int pidx = indexMap[indexMapOffset+i];
			const float * const &point = $self->m_nodes[pidx].m_x.m_floats;
			const float * const &normal = $self->m_nodes[pidx].m_n.m_floats;
			vertices[vidx  ] = point[0];
			vertices[vidx+1] = point[1];
			vertices[vidx+2] = point[2];
			vertices[nidx  ] = normal[0];
			vertices[nidx+1] = normal[1];
			vertices[nidx+2] = normal[2];
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
	void setConfig_timescale(btScalar v) { $self->m_cfg.timescale = v; }
	void setConfig_viterations(int v) { $self->m_cfg.viterations = v; }
	void setConfig_piterations(int v) { $self->m_cfg.piterations = v; }
	void setConfig_diterations(int v) { $self->m_cfg.diterations = v; }
	void setConfig_citerations(int v) { $self->m_cfg.citerations = v; }
	void setConfig_collisions(int v) { $self->m_cfg.collisions = v; }
};