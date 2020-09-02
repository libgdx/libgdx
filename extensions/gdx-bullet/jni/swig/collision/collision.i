%module(directors="1") Collision

%feature("director") btBroadphaseAabbCallback;
%feature("director") btBroadphaseRayCallback;
%feature("director") btConvexTriangleCallback;
%feature("director") btGhostPairCallback;
%feature("director") btInternalTriangleIndexCallback;
%feature("director") btNodeOverlapCallback;
%feature("director") btOverlapCallback;
%feature("director") btOverlapFilterCallback;
//%feature("director") btOverlappingPairCallback;
%feature("director") btTriangleCallback;
%feature("director") btTriangleConvexcastCallback;
%feature("director") btTriangleRaycastCallback;
// FIXME reuse btDispatcher and fix ptr/array typemap/pool

%include "arrays_java.i"

%import "../linearmath/linearmath.i"

%include "../common/gdxCommon.i"

%include "../../swig-src/linearmath/classes.i"

%ignore btManifoldPoint::getLifeTime;
%ignore btManifoldPoint::getAppliedImpulse;
%ignore btVoronoiSimplexSolver::setEqualVertexThreshold;
%ignore btVoronoiSimplexSolver::getEqualVertexThreshold;

%typemap(javaimports) SWIGTYPE	%{
import com.badlogic.gdx.physics.bullet.BulletBase;
import com.badlogic.gdx.physics.bullet.linearmath.*;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Matrix4;
%}
%pragma(java) jniclassimports=%{
import com.badlogic.gdx.physics.bullet.BulletBase;
import com.badlogic.gdx.physics.bullet.linearmath.*;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Pool;
%}
%pragma(java) moduleimports=%{
import com.badlogic.gdx.physics.bullet.BulletBase;
import com.badlogic.gdx.physics.bullet.linearmath.*;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Matrix4;
%}

// Required because bullet uses a macro for this
typedef btVoronoiSimplexSolver btSimplexSolverInterface;

%{
#include <BulletCollision/NarrowPhaseCollision/btDiscreteCollisionDetectorInterface.h>
%}
%include "BulletCollision/NarrowPhaseCollision/btDiscreteCollisionDetectorInterface.h"

%include "./btCollisionShape.i"

CREATE_POOLED_OBJECT(btBroadphaseProxy, com/badlogic/gdx/physics/bullet/collision/btBroadphaseProxy);
CREATE_POOLED_OBJECT(btBroadphasePair, com/badlogic/gdx/physics/bullet/collision/btBroadphasePair);
%{
#include <BulletCollision/BroadphaseCollision/btBroadphaseProxy.h>
%}
%include "BulletCollision/BroadphaseCollision/btBroadphaseProxy.h"

%rename(getOverlappingPairCacheConst) btBroadphaseInterface::getOverlappingPairCache() const;
%{
#include <BulletCollision/BroadphaseCollision/btBroadphaseInterface.h>
%}
%include "BulletCollision/BroadphaseCollision/btBroadphaseInterface.h"

%{
#include <BulletCollision/BroadphaseCollision/btQuantizedBvh.h>
%}
%include "BulletCollision/BroadphaseCollision/btQuantizedBvh.h"

%{
#include <BulletCollision/BroadphaseCollision/btSimpleBroadphase.h>
%}
%include "BulletCollision/BroadphaseCollision/btSimpleBroadphase.h"

%ignore btMultiSapBroadphase::btMultiSapProxy::m_bridgeProxies;

%{
#include <BulletCollision/BroadphaseCollision/btCollisionAlgorithm.h>
%}
%include "BulletCollision/BroadphaseCollision/btCollisionAlgorithm.h"

%feature("nodirector") btOverlappingPairCallback::addOverlappingPair
%{
#include <BulletCollision/BroadphaseCollision/btOverlappingPairCallback.h>
%}
%include "BulletCollision/BroadphaseCollision/btOverlappingPairCallback.h"

%include "./btAxisSweep3.i"

%rename(getInternalManifoldPoolConst) btDispatcher::getInternalManifoldPool() const;
%{
#include <BulletCollision/BroadphaseCollision/btDispatcher.h>
%}
%include "BulletCollision/BroadphaseCollision/btDispatcher.h"

%rename(getOverlappingPairArrayPtrConst) btOverlappingPairCache::getOverlappingPairArrayPtr() const;
%rename(getOverlappingPairArrayPtrConst) btHashedOverlappingPairCache::getOverlappingPairArrayPtr() const;
%rename(getOverlappingPairArrayConst) btHashedOverlappingPairCache::getOverlappingPairArray() const;
%rename(getOverlappingPairArrayConst) btSortedOverlappingPairCache::getOverlappingPairArray() const;
%{
#include <BulletCollision/BroadphaseCollision/btOverlappingPairCache.h>
%}
%include "BulletCollision/BroadphaseCollision/btOverlappingPairCache.h"

%{
#include <BulletCollision/CollisionShapes/btCollisionShape.h>
%}
%include "BulletCollision/CollisionShapes/btCollisionShape.h"

%{
#include <BulletCollision/CollisionShapes/btConvexShape.h>
%}
%include "BulletCollision/CollisionShapes/btConvexShape.h"

%{
#include <BulletCollision/CollisionShapes/btConvexInternalShape.h>
%}
%include "BulletCollision/CollisionShapes/btConvexInternalShape.h"

%{
#include <BulletCollision/CollisionShapes/btPolyhedralConvexShape.h>
%}
%include "BulletCollision/CollisionShapes/btPolyhedralConvexShape.h"

%{
#include <BulletCollision/CollisionShapes/btConcaveShape.h>
%}
%include "BulletCollision/CollisionShapes/btConcaveShape.h"

%{
#include <BulletCollision/CollisionShapes/btTriangleCallback.h>
%}
%include "BulletCollision/CollisionShapes/btTriangleCallback.h"

%rename(getAtIndexConst) btHashMap< btHashInt,btTriangleInfo >::getAtIndex(int) const;
%rename(getKeyAtIndexConst) btHashMap< btHashInt,btTriangleInfo >::getKeyAtIndex(int) const;
%rename(findConst) btHashMap< btHashInt,btTriangleInfo >::find(btHashInt const &) const;
%rename(operatorSubscriptConst) btHashMap< btHashInt,btTriangleInfo >::operator [](btHashInt const &) const;
%template(btHashMapInternalShortBtHashIntBtTriangleInfo) btHashMap<btHashInt,btTriangleInfo>;
%{
#include <BulletCollision/CollisionShapes/btTriangleInfoMap.h>
%}
%include "BulletCollision/CollisionShapes/btTriangleInfoMap.h"

%{
#include <BulletCollision/CollisionShapes/btStaticPlaneShape.h>
%}
%include "BulletCollision/CollisionShapes/btStaticPlaneShape.h"

%include "./btHeightfieldTerrainShape.i"

%rename(getMeshInterfaceConst) btTriangleMeshShape::getMeshInterface() const;
%{
#include <BulletCollision/CollisionShapes/btTriangleMeshShape.h>
%}
%include "BulletCollision/CollisionShapes/btTriangleMeshShape.h"

%include "./btBvhTriangleMeshShape.i"

%{
#include <BulletCollision/CollisionShapes/btBoxShape.h>
%}
%include "BulletCollision/CollisionShapes/btBoxShape.h"

%{
#include <BulletCollision/CollisionShapes/btCapsuleShape.h>
%}
%include "BulletCollision/CollisionShapes/btCapsuleShape.h"

%{
#include <BulletCollision/CollisionShapes/btBox2dShape.h>
%}
%include "BulletCollision/CollisionShapes/btBox2dShape.h"

%{
#include <BulletCollision/CollisionShapes/btCollisionMargin.h>
%}
%include "BulletCollision/CollisionShapes/btCollisionMargin.h"

%rename(getVertexPtrConst) btTriangleShape::getVertexPtr(int) const;
%{
#include <BulletCollision/CollisionShapes/btTriangleShape.h>
%}
%include "BulletCollision/CollisionShapes/btTriangleShape.h"

%{
#include <BulletCollision/CollisionShapes/btSphereShape.h>
%}
%include "BulletCollision/CollisionShapes/btSphereShape.h"

%{
#include <BulletCollision/CollisionShapes/btStridingMeshInterface.h>
%}
%include "BulletCollision/CollisionShapes/btStridingMeshInterface.h"

%{
#include <BulletCollision/CollisionShapes/btMinkowskiSumShape.h>
%}
%include "BulletCollision/CollisionShapes/btMinkowskiSumShape.h"

%{
#include <BulletCollision/CollisionShapes/btConvexPolyhedron.h>
%}
%include "BulletCollision/CollisionShapes/btConvexPolyhedron.h"

%{
#include <BulletCollision/CollisionShapes/btOptimizedBvh.h>
%}
%include "BulletCollision/CollisionShapes/btOptimizedBvh.h"

%{
#include <BulletCollision/CollisionShapes/btTriangleBuffer.h>
%}
%include "BulletCollision/CollisionShapes/btTriangleBuffer.h"

%include "./btTriangleIndexVertexArray.i"

%{
#include <BulletCollision/CollisionShapes/btMaterial.h>
%}
%include "BulletCollision/CollisionShapes/btMaterial.h"

%rename(getChildShapeConst) btScaledBvhTriangleMeshShape::getChildShape() const;
%{
#include <BulletCollision/CollisionShapes/btScaledBvhTriangleMeshShape.h>
%}
%include "BulletCollision/CollisionShapes/btScaledBvhTriangleMeshShape.h"

%include "./btShapeHull.i"

%include "./btConvexHullShape.i"

%{
#include <BulletCollision/CollisionShapes/btTriangleIndexVertexMaterialArray.h>
%}
%include "BulletCollision/CollisionShapes/btTriangleIndexVertexMaterialArray.h"

%{
#include <BulletCollision/CollisionShapes/btCylinderShape.h>
%}
%include "BulletCollision/CollisionShapes/btCylinderShape.h"

%{
#include <BulletCollision/CollisionShapes/btTriangleMesh.h>
%}
%include "BulletCollision/CollisionShapes/btTriangleMesh.h"

%{
#include <BulletCollision/CollisionShapes/btConeShape.h>
%}
%include "BulletCollision/CollisionShapes/btConeShape.h"

%rename(getMeshInterfaceConst) btConvexTriangleMeshShape::getMeshInterface() const;
%{
#include <BulletCollision/CollisionShapes/btConvexTriangleMeshShape.h>
%}
%include "BulletCollision/CollisionShapes/btConvexTriangleMeshShape.h"

%{
#include <BulletCollision/CollisionShapes/btEmptyShape.h>
%}
%include "BulletCollision/CollisionShapes/btEmptyShape.h"

%{
#include <BulletCollision/CollisionShapes/btMultimaterialTriangleMeshShape.h>
%}
%include "BulletCollision/CollisionShapes/btMultimaterialTriangleMeshShape.h"

%{
#include <BulletCollision/CollisionShapes/btTetrahedronShape.h>
%}
%include "BulletCollision/CollisionShapes/btTetrahedronShape.h"

%rename(getChildShapeConst) btUniformScalingShape::getChildShape() const;
%{
#include <BulletCollision/CollisionShapes/btUniformScalingShape.h>
%}
%include "BulletCollision/CollisionShapes/btUniformScalingShape.h"

%rename(getUnscaledPointsConst) btConvexPointCloudShape::getUnscaledPoints() const;
%{
#include <BulletCollision/CollisionShapes/btConvexPointCloudShape.h>
%}
%include "BulletCollision/CollisionShapes/btConvexPointCloudShape.h"

%rename(getChildShapeConst) btConvex2dShape::getChildShape() const;
%{
#include <BulletCollision/CollisionShapes/btConvex2dShape.h>
%}
%include "BulletCollision/CollisionShapes/btConvex2dShape.h"

%include "./btCollisionObject.i"
%include "./btDbvt.i"
%include "./btCompoundShape.i"

%rename(atConst) btAlignedObjectArray< btCollisionObject * >::at(int) const;
%rename(atConst) btAlignedObjectArray< const btCollisionObject * >::at(int) const;
%rename(operatorSubscriptConst) btAlignedObjectArray< btCollisionObject * >::operator [](int) const;
%rename(operatorSubscriptConst) btAlignedObjectArray< const btCollisionObject * >::operator [](int) const;
%template(btCollisionObjectArray) btAlignedObjectArray<btCollisionObject *>;
%template(btCollisionObjectConstArray) btAlignedObjectArray<const btCollisionObject*>;

%include "./btCollisionObjectWrapper.i"

%{
#include <BulletCollision/CollisionDispatch/btCollisionCreateFunc.h>
%}
%include "BulletCollision/CollisionDispatch/btCollisionCreateFunc.h"

%{
#include <BulletCollision/CollisionDispatch/btEmptyCollisionAlgorithm.h>
%}
%include "BulletCollision/CollisionDispatch/btEmptyCollisionAlgorithm.h"

%{
#include <BulletCollision/CollisionDispatch/btActivatingCollisionAlgorithm.h>
%}
%include "BulletCollision/CollisionDispatch/btActivatingCollisionAlgorithm.h"

%{
#include <BulletCollision/CollisionDispatch/btConvexConcaveCollisionAlgorithm.h>
%}
%include "BulletCollision/CollisionDispatch/btConvexConcaveCollisionAlgorithm.h"

%{
#include <BulletCollision/CollisionDispatch/btConvexPlaneCollisionAlgorithm.h>
%}
%include "BulletCollision/CollisionDispatch/btConvexPlaneCollisionAlgorithm.h"

%{
#include <BulletCollision/CollisionDispatch/btCompoundCollisionAlgorithm.h>
%}
%include "BulletCollision/CollisionDispatch/btCompoundCollisionAlgorithm.h"

%{
#include <BulletCollision/CollisionDispatch/btCompoundCompoundCollisionAlgorithm.h>
%}
%include "BulletCollision/CollisionDispatch/btCompoundCompoundCollisionAlgorithm.h"

%{
#include <BulletCollision/CollisionDispatch/btCollisionConfiguration.h>
%}
%include "BulletCollision/CollisionDispatch/btCollisionConfiguration.h"

%{
#include <BulletCollision/CollisionDispatch/btDefaultCollisionConfiguration.h>
%}
%include "BulletCollision/CollisionDispatch/btDefaultCollisionConfiguration.h"

%rename(getPersistentManifoldConst) btManifoldResult::getPersistentManifold() const;
%{
#include <BulletCollision/CollisionDispatch/btManifoldResult.h>
%}
%include "BulletCollision/CollisionDispatch/btManifoldResult.h"

%rename(getOverlappingPairArrayPtrConst) btHashedSimplePairCache::getOverlappingPairArrayPtr() const;
%rename(getOverlappingPairArrayConst) btHashedSimplePairCache::getOverlappingPairArray() const;
%{
#include <BulletCollision/CollisionDispatch/btHashedSimplePairCache.h>
%}
%include "BulletCollision/CollisionDispatch/btHashedSimplePairCache.h"

%{
#include <BulletCollision/CollisionDispatch/btSphereSphereCollisionAlgorithm.h>
%}
%include "BulletCollision/CollisionDispatch/btSphereSphereCollisionAlgorithm.h"

%{
#include <BulletCollision/CollisionDispatch/btBoxBoxCollisionAlgorithm.h>
%}
%include "BulletCollision/CollisionDispatch/btBoxBoxCollisionAlgorithm.h"

%{
#include <BulletCollision/CollisionDispatch/btBox2dBox2dCollisionAlgorithm.h>
%}
%include "BulletCollision/CollisionDispatch/btBox2dBox2dCollisionAlgorithm.h"

%rename(getElementConst) btUnionFind::getElement(int) const;
%{
#include <BulletCollision/CollisionDispatch/btUnionFind.h>
%}
%include "BulletCollision/CollisionDispatch/btUnionFind.h"

%{
#include <BulletCollision/CollisionDispatch/btSphereTriangleCollisionAlgorithm.h>
%}
%include "BulletCollision/CollisionDispatch/btSphereTriangleCollisionAlgorithm.h"

%{
#include <BulletCollision/CollisionDispatch/btSimulationIslandManager.h>
%}
%include "BulletCollision/CollisionDispatch/btSimulationIslandManager.h"

%rename(getOverlappingObjectConst) btGhostObject::getOverlappingObject(int) const;
%rename(getOverlappingPairsConst) btGhostObject::getOverlappingPairs() const;
%rename(upcastConstBtCollisionObject) btGhostObject::upcast(btCollisionObject const *);
//%feature("nodirector") btGhostObject::addOverlappingPair
%{
#include <BulletCollision/CollisionDispatch/btGhostObject.h>
%}
%include "BulletCollision/CollisionDispatch/btGhostObject.h"

%include "./btCollisionWorld.i"

%{
#include <gdx/collision/ClosestNotMeConvexResultCallback.h>
%}
%include "gdx/collision/ClosestNotMeConvexResultCallback.h"

%{
#include <gdx/collision/ClosestNotMeRayResultCallback.h>
%}
%include "gdx/collision/ClosestNotMeRayResultCallback.h"

%{
#include <BulletCollision/CollisionDispatch/btConvex2dConvex2dAlgorithm.h>
%}
%include "BulletCollision/CollisionDispatch/btConvex2dConvex2dAlgorithm.h"

%{
#include <BulletCollision/CollisionDispatch/btBoxBoxDetector.h>
%}
%include "BulletCollision/CollisionDispatch/btBoxBoxDetector.h"

%{
#include <BulletCollision/CollisionDispatch/btSphereBoxCollisionAlgorithm.h>
%}
%include "BulletCollision/CollisionDispatch/btSphereBoxCollisionAlgorithm.h"

%rename(getManifoldByIndexInternalConst) btCollisionDispatcher::getManifoldByIndexInternal(int) const;
%rename(getCollisionConfigurationConst) btCollisionDispatcher::getCollisionConfiguration() const;
%{
#include <BulletCollision/CollisionDispatch/btCollisionDispatcher.h>
%}
%include "BulletCollision/CollisionDispatch/btCollisionDispatcher.h"

%{
#include <BulletCollision/CollisionDispatch/btCollisionDispatcherMt.h>
%}
%include "BulletCollision/CollisionDispatch/btCollisionDispatcherMt.h"

%{
#include <BulletCollision/CollisionDispatch/btConvexConvexAlgorithm.h>
%}
%include "BulletCollision/CollisionDispatch/btConvexConvexAlgorithm.h"

%{
#include <BulletCollision/CollisionDispatch/SphereTriangleDetector.h>
%}
%include "BulletCollision/CollisionDispatch/SphereTriangleDetector.h"

%{
#include <BulletCollision/CollisionDispatch/btInternalEdgeUtility.h>
%}
%include "BulletCollision/CollisionDispatch/btInternalEdgeUtility.h"

%{
#include <BulletCollision/NarrowPhaseCollision/btConvexCast.h>
%}
%include "BulletCollision/NarrowPhaseCollision/btConvexCast.h"

%{
#include <BulletCollision/NarrowPhaseCollision/btSubSimplexConvexCast.h>
%}
%include "BulletCollision/NarrowPhaseCollision/btSubSimplexConvexCast.h"

%{
#include <BulletCollision/NarrowPhaseCollision/btPolyhedralContactClipping.h>
%}
%include "BulletCollision/NarrowPhaseCollision/btPolyhedralContactClipping.h"

%rename(getContactPointConst) btPersistentManifold::getContactPoint(int) const;
%{
#include <BulletCollision/NarrowPhaseCollision/btPersistentManifold.h>
%}
%include "BulletCollision/NarrowPhaseCollision/btPersistentManifold.h"

%rename(atConst) btAlignedObjectArray< btPersistentManifold* >::at(int) const;
%rename(operatorSubscriptConst) btAlignedObjectArray< btPersistentManifold* >::operator [](int) const;
%template(btPersistentManifoldArray) btAlignedObjectArray<btPersistentManifold*>;

%{
#include <BulletCollision/NarrowPhaseCollision/btGjkPairDetector.h>
%}
%include "BulletCollision/NarrowPhaseCollision/btGjkPairDetector.h"

%{
#include <BulletCollision/NarrowPhaseCollision/btConvexPenetrationDepthSolver.h>
%}
%include "BulletCollision/NarrowPhaseCollision/btConvexPenetrationDepthSolver.h"

%{
#include <BulletCollision/NarrowPhaseCollision/btMinkowskiPenetrationDepthSolver.h>
%}
%include "BulletCollision/NarrowPhaseCollision/btMinkowskiPenetrationDepthSolver.h"

%{
#include <BulletCollision/NarrowPhaseCollision/btGjkConvexCast.h>
%}
%include "BulletCollision/NarrowPhaseCollision/btGjkConvexCast.h"

%include "./btManifoldPoint.i"

%{
#include <BulletCollision/NarrowPhaseCollision/btContinuousConvexCollision.h>
%}
%include "BulletCollision/NarrowPhaseCollision/btContinuousConvexCollision.h"

%{
#include <BulletCollision/NarrowPhaseCollision/btRaycastCallback.h>
%}
%include "BulletCollision/NarrowPhaseCollision/btRaycastCallback.h"

%{
#include <BulletCollision/NarrowPhaseCollision/btGjkEpa2.h>
%}
%include "BulletCollision/NarrowPhaseCollision/btGjkEpa2.h"

%{
#include <BulletCollision/NarrowPhaseCollision/btGjkEpaPenetrationDepthSolver.h>
%}
%include "BulletCollision/NarrowPhaseCollision/btGjkEpaPenetrationDepthSolver.h"

%{
#include <BulletCollision/NarrowPhaseCollision/btPointCollector.h>
%}
%include "BulletCollision/NarrowPhaseCollision/btPointCollector.h"

%{
#include <BulletCollision/NarrowPhaseCollision/btSimplexSolverInterface.h>
%}
%include "BulletCollision/NarrowPhaseCollision/btSimplexSolverInterface.h"

%rename(m_numVerticesVar) btVoronoiSimplexSolver::numVertices;
%{
#include <BulletCollision/NarrowPhaseCollision/btVoronoiSimplexSolver.h>
%}
%include "BulletCollision/NarrowPhaseCollision/btVoronoiSimplexSolver.h"

%include "./btMultiSphereShape.i"

%include "./CustomCollisionDispatcher.i"

%include "./ContactListener.i"

%include "./ContactCache.i"

%include "./btBroadphasePairArray.i"

%include "./gimpact.i"

%{
#include <BulletCollision/CollisionDispatch/btCollisionWorldImporter.h>
%}
%include "BulletCollision/CollisionDispatch/btCollisionWorldImporter.h"

%{
#include <BulletCollision/NarrowPhaseCollision/btComputeGjkEpaPenetration.h>
%}
%include "BulletCollision/NarrowPhaseCollision/btComputeGjkEpaPenetration.h"

%{
#include <BulletCollision/NarrowPhaseCollision/btGjkCollisionDescription.h>
%}
%include "BulletCollision/NarrowPhaseCollision/btGjkCollisionDescription.h"

%{
#include <BulletCollision/NarrowPhaseCollision/btGjkEpa3.h>
%}
%include "BulletCollision/NarrowPhaseCollision/btGjkEpa3.h"

%{
#include <BulletCollision/NarrowPhaseCollision/btMprPenetration.h>
%}
%include "BulletCollision/NarrowPhaseCollision/btMprPenetration.h"
