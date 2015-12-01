%module(directors="1") btDbvt

%feature("flatnested") btDbvt::ICollide;
%feature("director") ICollide;

%{
#include <BulletCollision/BroadphaseCollision/btDbvt.h>
%}
%include "BulletCollision/BroadphaseCollision/btDbvt.h"

%{
#include <BulletCollision/BroadphaseCollision/btDbvtBroadphase.h>
%}
%include "BulletCollision/BroadphaseCollision/btDbvtBroadphase.h"

%extend btDbvt {
	static void	collideKDOP(const btDbvtNode* root,
		const btScalar* normals,
		const btScalar* offsets,
		int count,
		btDbvt::ICollide &policy) {
		btDbvt::collideKDOP(root, (btVector3*)normals, offsets, count, policy);
	}
	
	static void	collideOCL(	const btDbvtNode* root,
		const btScalar* normals,
		const btScalar* offsets,
		const btVector3& sortaxis,
		int count,								
		btDbvt::ICollide &policy,
		bool fullsort=true) {
		btDbvt::collideOCL(root, (btVector3*)normals, offsets, sortaxis, count, policy, fullsort);
	}
};

%extend btDbvtBroadphase {
	btDbvt *getSet(const int &index) {
		return &($self->m_sets[index]);
	}
	btDbvt *getSet0() {
		return &($self->m_sets[0]);
	}
	btDbvt *getSet1() {
		return &($self->m_sets[1]);
	}
};

%extend btDbvtNode {
	btDbvtNode *getChild(const int &index) {
		return $self->childs[index];
	}
	
	btDbvtNode *getChild0() {
		return $self->childs[0];
	}
	
	btDbvtNode *getChild1() {
		return $self->childs[1];
	}
};

