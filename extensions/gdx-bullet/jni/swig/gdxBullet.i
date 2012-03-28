/*
 *	Main SWIG 2.0 input file for Bullet.
 */

%module(directors="1") gdxBullet

/* 
 * Allow public access to the CPtr methods on proxy classes and wrappers.
 * 
 * public was the default in SWIG <= 2.0.4, but changed to protected in
 * 2.0.5. Getitng pointers to native Bullet objects can be useful (for 
 * instance, to map them back to associated Java scene objects), so make
 * the getCPtr method public.
 */
SWIG_JAVABODY_PROXY(protected, public, SWIGTYPE)
SWIG_JAVABODY_TYPEWRAPPER(protected, protected, public, SWIGTYPE)

/* Configures types that need down cast support */
%include "gdxDownCast.i"

/*
 * Use Java float[] where Bullet wants btScalar *.  This gets disabled
 * for some types (and re-enabled right after).
 */
%include "gdxEnableArrays.i"

/* Prefer libgdx's linear math types (Vector3, Matrix3, etc.). */
%include "gdxMathTypes.i"

/* Map "void *" to "jlong". */
/* %include "gdxVoidPointer.i"; */

/* Use "unsafe" enums (plain integer constants) instead of typesafe enum classes. */
%include "enumtypeunsafe.swg"
%javaconst(1);

/* Include Java imports for all the types we'll need in all extensions/custom types. */
%include "gdxJavaImports.i"

/*
 * btScalar.h defines macros the other types need, so process it first.  
 * It also defines some static functions that end up in gdxBulletJNI.java.
 */
%include "LinearMath/btScalar.h"

/*
 * Extend some classes with custom Java.
 */
%include "javacode/btTransform.i"
%include "javacode/btCollisionShape.i"
%include "javacode/btMotionState.i"

/*
 * Uncomment this include to generate some dummy types that test the 
 * gdxMathTypes.i work.
 */
/*
%include "test/voidPointerTest.i"
%include "test/mathTypesTest.i"
*/

/* Configure directors for types with virtual methods that need Java implementations */
%feature("director") btIDebugDraw;

/*
 * The rest of the types (some are disabled, commented out at the bottom).
 * 
 * The order below is important.  If an "%include"ed type depends on another
 * type, that other type's "#include" line needs to come before it.  Avoid
 * splitting the #include/%include pairs for a type, so move the whole type
 * block.
 */

%{
#include <LinearMath/btVector3.h>
%}
%include <LinearMath/btVector3.h>

%{
#include <LinearMath/btQuadWord.h>
%}
#include <LinearMath/btQuadWord.h>

%{
#include <LinearMath/btQuaternion.h>
%}
%include <LinearMath/btQuaternion.h>

%{
#include <LinearMath/btMatrix3x3.h>
%}
#include <LinearMath/btMatrix3x3.h>

%{
#include <LinearMath/btAabbUtil2.h>
%}
%include "LinearMath/btAabbUtil2.h"

%{
#include <LinearMath/btIDebugDraw.h>
%}
%include "LinearMath/btIDebugDraw.h"

%{
#include <LinearMath/btGeometryUtil.h>
%}
%include "LinearMath/btGeometryUtil.h"

%{
#include <LinearMath/btRandom.h>
%}
%include "LinearMath/btRandom.h"

%{
#include <LinearMath/btTransform.h>
%}
%include "LinearMath/btTransform.h"

%{
#include <LinearMath/btTransformUtil.h>
%}
%include "LinearMath/btTransformUtil.h"

%{
#include <LinearMath/btMotionState.h>
%}
%include "LinearMath/btMotionState.h"

/* Extension methods */
%include "custom/btDefaultMotionState.i"

%{
#include <LinearMath/btConvexHull.h>
%}
%include "LinearMath/btConvexHull.h"

%{
#include <LinearMath/btGrahamScan2dConvexHull.h>
%}
%include "LinearMath/btGrahamScan2dConvexHull.h"

%{
#include <LinearMath/btPoolAllocator.h>
%}
%include "LinearMath/btPoolAllocator.h"

%{
#include <LinearMath/btQuickprof.h>
%}
%include "LinearMath/btQuickprof.h"

%{
#include <LinearMath/btConvexHullComputer.h>
%}
%include "LinearMath/btConvexHullComputer.h"

%{
#include <LinearMath/btAlignedObjectArray.h>
%}
%include "LinearMath/btAlignedObjectArray.h"

%{
#include <LinearMath/btList.h>
%}
%include "LinearMath/btList.h"

%{
#include <LinearMath/btAlignedAllocator.h>
%}
%include "LinearMath/btAlignedAllocator.h"

%{
#include <LinearMath/btHashMap.h>
%}
%include "LinearMath/btHashMap.h"

%{
#include <LinearMath/btStackAlloc.h>
%}
%include "LinearMath/btStackAlloc.h"

%{
#include <LinearMath/btMinMax.h>
%}
%include "LinearMath/btMinMax.h"

/* Has nested classes or structs */
%include "custom/btDiscreteCollisionDetectorInterface.i"

%{
#include <BulletCollision/BroadphaseCollision/btBroadphaseProxy.h>
%}
%include "BulletCollision/BroadphaseCollision/btBroadphaseProxy.h"

%{
#include <BulletCollision/BroadphaseCollision/btBroadphaseInterface.h>
%}
%include "BulletCollision/BroadphaseCollision/btBroadphaseInterface.h"

%{
#include <BulletCollision/BroadphaseCollision/btDbvt.h>
%}
%include "BulletCollision/BroadphaseCollision/btDbvt.h"

%{
#include <BulletCollision/BroadphaseCollision/btQuantizedBvh.h>
%}
%include "BulletCollision/BroadphaseCollision/btQuantizedBvh.h"

%{
#include <BulletCollision/BroadphaseCollision/btDbvtBroadphase.h>
%}
%include "BulletCollision/BroadphaseCollision/btDbvtBroadphase.h"

%{
#include <BulletCollision/BroadphaseCollision/btSimpleBroadphase.h>
%}
%include "BulletCollision/BroadphaseCollision/btSimpleBroadphase.h"

%{
#include <BulletCollision/BroadphaseCollision/btMultiSapBroadphase.h>
%}
%include "BulletCollision/BroadphaseCollision/btMultiSapBroadphase.h"

%{
#include <BulletCollision/BroadphaseCollision/btCollisionAlgorithm.h>
%}
%include "BulletCollision/BroadphaseCollision/btCollisionAlgorithm.h"

%{
#include <BulletCollision/BroadphaseCollision/btOverlappingPairCallback.h>
%}
%include "BulletCollision/BroadphaseCollision/btOverlappingPairCallback.h"

%{
#include <BulletCollision/BroadphaseCollision/btAxisSweep3.h>
%}
%include "BulletCollision/BroadphaseCollision/btAxisSweep3.h"

%{
#include <BulletCollision/BroadphaseCollision/btDispatcher.h>
%}
%include "BulletCollision/BroadphaseCollision/btDispatcher.h"

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

%{
#include <BulletCollision/CollisionShapes/btTriangleInfoMap.h>
%}
%include "BulletCollision/CollisionShapes/btTriangleInfoMap.h"

%{
#include <BulletCollision/CollisionShapes/btStaticPlaneShape.h>
%}
%include "BulletCollision/CollisionShapes/btStaticPlaneShape.h"

%{
#include <BulletCollision/CollisionShapes/btHeightfieldTerrainShape.h>
%}
%include "BulletCollision/CollisionShapes/btHeightfieldTerrainShape.h"

%{
#include <BulletCollision/CollisionShapes/btTriangleMeshShape.h>
%}
%include "BulletCollision/CollisionShapes/btTriangleMeshShape.h"

%{
#include <BulletCollision/CollisionShapes/btBvhTriangleMeshShape.h>
%}
%include "BulletCollision/CollisionShapes/btBvhTriangleMeshShape.h"

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

%{
#include <BulletCollision/CollisionShapes/btTriangleShape.h>
%}
%include "BulletCollision/CollisionShapes/btTriangleShape.h"

%{
#include <BulletCollision/CollisionShapes/btShapeHull.h>
%}
%include "BulletCollision/CollisionShapes/btShapeHull.h"

%{
#include <BulletCollision/CollisionShapes/btSphereShape.h>
%}
%include "BulletCollision/CollisionShapes/btSphereShape.h"

%{
#include <BulletCollision/CollisionShapes/btMultiSphereShape.h>
%}
%include "BulletCollision/CollisionShapes/btMultiSphereShape.h"

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

%{
#include <BulletCollision/CollisionShapes/btTriangleIndexVertexArray.h>
%}
%include "BulletCollision/CollisionShapes/btTriangleIndexVertexArray.h"

%{
#include <BulletCollision/CollisionShapes/btMaterial.h>
%}
%include "BulletCollision/CollisionShapes/btMaterial.h"

%{
#include <BulletCollision/CollisionShapes/btScaledBvhTriangleMeshShape.h>
%}
%include "BulletCollision/CollisionShapes/btScaledBvhTriangleMeshShape.h"

%{
#include <BulletCollision/CollisionShapes/btConvexHullShape.h>
%}
%include "BulletCollision/CollisionShapes/btConvexHullShape.h"

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

%{
#include <BulletCollision/CollisionShapes/btUniformScalingShape.h>
%}
%include "BulletCollision/CollisionShapes/btUniformScalingShape.h"

%{
#include <BulletCollision/CollisionShapes/btCompoundShape.h>
%}
%include "BulletCollision/CollisionShapes/btCompoundShape.h"

%{
#include <BulletCollision/CollisionShapes/btConvexPointCloudShape.h>
%}
%include "BulletCollision/CollisionShapes/btConvexPointCloudShape.h"

%{
#include <BulletCollision/CollisionShapes/btConvex2dShape.h>
%}
%include "BulletCollision/CollisionShapes/btConvex2dShape.h"

/* Has extensions */
%include "custom/btCollisionObject.i"

/* Has nested classes or structs */
%include "custom/btRigidBody.i"

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
#include <BulletCollision/CollisionDispatch/btCollisionConfiguration.h>
%}
%include "BulletCollision/CollisionDispatch/btCollisionConfiguration.h"

%{
#include <BulletCollision/CollisionDispatch/btDefaultCollisionConfiguration.h>
%}
%include "BulletCollision/CollisionDispatch/btDefaultCollisionConfiguration.h"

%{
#include <BulletCollision/CollisionDispatch/btManifoldResult.h>
%}
%include "BulletCollision/CollisionDispatch/btManifoldResult.h"

%{
#include <BulletCollision/CollisionDispatch/btSphereSphereCollisionAlgorithm.h>
%}
%include "BulletCollision/CollisionDispatch/btSphereSphereCollisionAlgorithm.h"

%{
#include <BulletCollision/CollisionDispatch/btBoxBoxCollisionAlgorithm.h>
%}
%include "BulletCollision/CollisionDispatch/btBoxBoxCollisionAlgorithm.h"

%{
#include <BulletCollision/CollisionDispatch/btCollisionCreateFunc.h>
%}
%include "BulletCollision/CollisionDispatch/btCollisionCreateFunc.h"

%{
#include <BulletCollision/CollisionDispatch/btBox2dBox2dCollisionAlgorithm.h>
%}
%include "BulletCollision/CollisionDispatch/btBox2dBox2dCollisionAlgorithm.h"

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

%{
#include <BulletCollision/CollisionDispatch/btGhostObject.h>
%}
%include "BulletCollision/CollisionDispatch/btGhostObject.h"

%{
#include <BulletCollision/CollisionDispatch/btCollisionWorld.h>
%}
%include "BulletCollision/CollisionDispatch/btCollisionWorld.h"

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

%{
#include <BulletCollision/CollisionDispatch/btCollisionDispatcher.h>
%}
%include "BulletCollision/CollisionDispatch/btCollisionDispatcher.h"

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
#include <BulletCollision/CollisionDispatch/btCompoundCollisionAlgorithm.h>
%}
%include "BulletCollision/CollisionDispatch/btCompoundCollisionAlgorithm.h"

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

%{
#include <BulletCollision/NarrowPhaseCollision/btPersistentManifold.h>
%}
%include "BulletCollision/NarrowPhaseCollision/btPersistentManifold.h"

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

%{
#include <BulletCollision/NarrowPhaseCollision/btManifoldPoint.h>
%}
%include "BulletCollision/NarrowPhaseCollision/btManifoldPoint.h"

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
#include <BulletCollision/NarrowPhaseCollision/btVoronoiSimplexSolver.h>
%}
%include "BulletCollision/NarrowPhaseCollision/btVoronoiSimplexSolver.h"

%{
#include <BulletCollision/NarrowPhaseCollision/btSimplexSolverInterface.h>
%}
%include "BulletCollision/NarrowPhaseCollision/btSimplexSolverInterface.h"

/* Has nested classes or structs */
%include "custom/btTypedConstraint.i"

%{
#include <BulletDynamics/Dynamics/btDynamicsWorld.h>
%}
%include "BulletDynamics/Dynamics/btDynamicsWorld.h"

%{
#include <BulletDynamics/Dynamics/btSimpleDynamicsWorld.h>
%}
%include "BulletDynamics/Dynamics/btSimpleDynamicsWorld.h"

%{
#include <BulletDynamics/Dynamics/btActionInterface.h>
%}
%include "BulletDynamics/Dynamics/btActionInterface.h"

%{
#include <BulletDynamics/Dynamics/btDiscreteDynamicsWorld.h>
%}
%include "BulletDynamics/Dynamics/btDiscreteDynamicsWorld.h"

%{
#include <BulletDynamics/Character/btCharacterControllerInterface.h>
%}
%include "BulletDynamics/Character/btCharacterControllerInterface.h"

%{
#include <BulletDynamics/Character/btKinematicCharacterController.h>
%}
%include "BulletDynamics/Character/btKinematicCharacterController.h"

%{
#include <BulletDynamics/ConstraintSolver/btContactSolverInfo.h>
%}
%include "BulletDynamics/ConstraintSolver/btContactSolverInfo.h"

%{
#include <BulletDynamics/ConstraintSolver/btConstraintSolver.h>
%}
%include "BulletDynamics/ConstraintSolver/btConstraintSolver.h"

%{
#include <BulletDynamics/ConstraintSolver/btSequentialImpulseConstraintSolver.h>
%}
%include "BulletDynamics/ConstraintSolver/btSequentialImpulseConstraintSolver.h"

%{
#include <BulletDynamics/ConstraintSolver/btSolverBody.h>
%}
%include "BulletDynamics/ConstraintSolver/btSolverBody.h"

%{
#include <BulletDynamics/ConstraintSolver/btSliderConstraint.h>
%}
%include "BulletDynamics/ConstraintSolver/btSliderConstraint.h"

%{
#include <BulletDynamics/ConstraintSolver/btPoint2PointConstraint.h>
%}
%include "BulletDynamics/ConstraintSolver/btPoint2PointConstraint.h"

%{
#include <BulletDynamics/ConstraintSolver/btJacobianEntry.h>
%}
%include "BulletDynamics/ConstraintSolver/btJacobianEntry.h"

%{
#include <BulletDynamics/ConstraintSolver/btSolve2LinearConstraint.h>
%}
%include "BulletDynamics/ConstraintSolver/btSolve2LinearConstraint.h"

%{
#include <BulletDynamics/ConstraintSolver/btGeneric6DofConstraint.h>
%}
%include "BulletDynamics/ConstraintSolver/btGeneric6DofConstraint.h"

%{
#include <BulletDynamics/ConstraintSolver/btUniversalConstraint.h>
%}
%include "BulletDynamics/ConstraintSolver/btUniversalConstraint.h"

%{
#include <BulletDynamics/ConstraintSolver/btContactConstraint.h>
%}
%include "BulletDynamics/ConstraintSolver/btContactConstraint.h"

%{
#include <BulletDynamics/ConstraintSolver/btConeTwistConstraint.h>
%}
%include "BulletDynamics/ConstraintSolver/btConeTwistConstraint.h"

%{
#include <BulletDynamics/ConstraintSolver/btGeneric6DofSpringConstraint.h>
%}
%include "BulletDynamics/ConstraintSolver/btGeneric6DofSpringConstraint.h"

%{
#include <BulletDynamics/ConstraintSolver/btHingeConstraint.h>
%}
%include "BulletDynamics/ConstraintSolver/btHingeConstraint.h"

%{
#include <BulletDynamics/ConstraintSolver/btSolverConstraint.h>
%}
%include "BulletDynamics/ConstraintSolver/btSolverConstraint.h"

%{
#include <BulletDynamics/ConstraintSolver/btHinge2Constraint.h>
%}
%include "BulletDynamics/ConstraintSolver/btHinge2Constraint.h"

/* DISABLED STUFF BELOW HERE */

/*
 * btSerializer needs some typemap customization for sBulletDNAstr and friends.
 * SWIG doesn't know how to pass the unsized arrays back.
 */
/* 
%{
#include <LinearMath/btSerializer.h>
%}
%include "LinearMath/btSerializer.h"
*/

/* 
 * btWheelInfo doesn't compile because it doesnt have a 0-arg constructor for 
 * btAlignedObjectArray to call, so I disabled the vehicle stuff.
 */
/*
%{
#include <BulletDynamics/Vehicle/btVehicleRaycaster.h>
%}
%include "BulletDynamics/Vehicle/btVehicleRaycaster.h"

%{
#include <BulletDynamics/Vehicle/btRaycastVehicle.h>
%}
%include "BulletDynamics/Vehicle/btRaycastVehicle.h"

%{
#include <BulletDynamics/Vehicle/btWheelInfo.h>
%}
%include "BulletDynamics/Vehicle/btWheelInfo.h"
*/

/*
 * Because C++ templates are compile-time, we must pre-define all the
 * template classes to generate in Java.  This is at the bottom
 * so we can reference all the other types.
 */
 
%template(btCollisionObjectArray) btAlignedObjectArray<btCollisionObject *>;

/*
 * Include dummy methods for ones Bullet declares but doesn't
 * implement.  At the bottom so we can reference other types.
 */
%include "gdxMissingBulletMethods.i"
