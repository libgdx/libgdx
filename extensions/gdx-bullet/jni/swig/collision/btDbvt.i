%module btDbvt

%{
#include <BulletCollision/BroadphaseCollision/btDbvt.h>
%}
%include "BulletCollision/BroadphaseCollision/btDbvt.h"

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