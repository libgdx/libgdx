%module(directors="1") btDbvt

%feature("flatnested") btDbvt::ICollide;
%feature("director") ICollide;

%typemap(javadirectorin) btDbvtNode, const btDbvtNode, const btDbvtNode &, btDbvtNode & 	"btDbvtNode.obtainForArgument($1, false)"
%typemap(javadirectorin) btDbvtNode *, const btDbvtNode *, btDbvtNode * const &	"btDbvtNode.obtainForArgument($1, false)"
%typemap(javaout) 	btDbvtNode *, const btDbvtNode *, btDbvtNode * const & {
	return btDbvtNode.internalTemp($jnicall, $owner);
}
%typemap(javacode) btDbvtNode %{
	private final static btDbvtNode temp = new btDbvtNode(0, false);
	/** Obtains a temporary instance, used by native methods that return a btDbvtNode instance */
	public static btDbvtNode internalTemp(long cPtr, boolean own) {
		temp.reset(cPtr, own);
		return temp;
	}
	private static btDbvtNode[] argumentInstances = new btDbvtNode[] {new btDbvtNode(0, false),
		new btDbvtNode(0, false), new btDbvtNode(0, false), new btDbvtNode(0, false)};
	private static int argumentIndex = -1;
	/** Obtains a temporary instance, used for callback methods with one or more btDbvtNode arguments */
	protected static btDbvtNode obtainForArgument(final long swigCPtr, boolean owner) {
		btDbvtNode instance = argumentInstances[argumentIndex = (argumentIndex + 1) & 3];
		instance.reset(swigCPtr, owner);
		return instance;
	}
%}
%typemap(javadirectorin) btDbvtAabbMm, const btDbvtAabbMm, const btDbvtAabbMm &, btDbvtAabbMm & 	"btDbvtAabbMm.obtainForArgument($1, false)"
%typemap(javadirectorin) btDbvtAabbMm *, const btDbvtAabbMm *, btDbvtAabbMm * const &	"btDbvtAabbMm.obtainForArgument($1, false)"
%typemap(javaout) 	btDbvtAabbMm *, const btDbvtAabbMm *, btDbvtAabbMm * const & {
	return btDbvtAabbMm.internalTemp($jnicall, $owner);
}
%typemap(javacode) btDbvtAabbMm %{
	private final static btDbvtAabbMm temp = new btDbvtAabbMm(0, false);
	/** Obtains a temporary instance, used by native methods that return a btDbvtAabbMm instance */
	public static btDbvtAabbMm internalTemp(long cPtr, boolean own) {
		temp.reset(cPtr, own);
		return temp;
	}
	private static btDbvtAabbMm[] argumentInstances = new btDbvtAabbMm[] {new btDbvtAabbMm(0, false),
		new btDbvtAabbMm(0, false), new btDbvtAabbMm(0, false), new btDbvtAabbMm(0, false)};
	private static int argumentIndex = -1;
	/** Obtains a temporary instance, used for callback methods with one or more btDbvtAabbMm arguments */
	protected static btDbvtAabbMm obtainForArgument(final long swigCPtr, boolean owner) {
		btDbvtAabbMm instance = argumentInstances[argumentIndex = (argumentIndex + 1) & 3];
		instance.reset(swigCPtr, owner);
		return instance;
	}
%}

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
	
	btBroadphaseProxy *getDataAsProxy() {
	    return (btBroadphaseProxy*)$self->data;
	}
	
	btCollisionObject *getDataAsProxyClientObject() {
	    return ($self->isleaf()) ? (btCollisionObject*)((btBroadphaseProxy*)$self->data)->m_clientObject : NULL;
	}
};

