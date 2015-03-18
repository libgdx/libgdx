/*
 *	Extra Java methods.
 */
 
%module btCollisionShape

/* newDerivedObject() required for down cast support (gdxDownCast.i). */

%typemap(javacode) btCollisionShape %{

  public static btCollisionShape newDerivedObject(long swigCPtr, boolean owner) {
    if (swigCPtr == 0) {
      return null;
    }
    
    final int shapeType = $moduleJNI.btCollisionShape_getShapeType(swigCPtr, null);
    
    switch (shapeType) {
    case BroadphaseNativeTypes.BOX_SHAPE_PROXYTYPE:
      return new btBoxShape(swigCPtr, owner);
    case BroadphaseNativeTypes.TRIANGLE_SHAPE_PROXYTYPE:
      return new btTriangleShape(swigCPtr, owner);
    case BroadphaseNativeTypes.TETRAHEDRAL_SHAPE_PROXYTYPE:
      return new btBU_Simplex1to4(swigCPtr, owner);
    case BroadphaseNativeTypes.CONVEX_TRIANGLEMESH_SHAPE_PROXYTYPE:
      return new btConvexTriangleMeshShape(swigCPtr, owner);
    case BroadphaseNativeTypes.CONVEX_HULL_SHAPE_PROXYTYPE:
      return new btConvexHullShape(swigCPtr, owner);
    case BroadphaseNativeTypes.CONVEX_POINT_CLOUD_SHAPE_PROXYTYPE:
      return new btConvexPointCloudShape(swigCPtr, owner);
    case BroadphaseNativeTypes.CUSTOM_POLYHEDRAL_SHAPE_TYPE:
      // TODO ?
      break;
    case BroadphaseNativeTypes.SPHERE_SHAPE_PROXYTYPE:
      return new btSphereShape(swigCPtr, owner);
    case BroadphaseNativeTypes.MULTI_SPHERE_SHAPE_PROXYTYPE:
      return new btMultiSphereShape(swigCPtr, owner);
    case BroadphaseNativeTypes.CAPSULE_SHAPE_PROXYTYPE:
      return new btCapsuleShape(swigCPtr, owner);
    case BroadphaseNativeTypes.CONE_SHAPE_PROXYTYPE:
      return new btConeShape(swigCPtr, owner);
    case BroadphaseNativeTypes.CONVEX_SHAPE_PROXYTYPE:
      return new btConvexShape(swigCPtr, owner);
    case BroadphaseNativeTypes.CYLINDER_SHAPE_PROXYTYPE:
      return new btCylinderShape(swigCPtr, owner);
    case BroadphaseNativeTypes.UNIFORM_SCALING_SHAPE_PROXYTYPE:
      return new btUniformScalingShape(swigCPtr, owner);
    case BroadphaseNativeTypes.MINKOWSKI_SUM_SHAPE_PROXYTYPE:
      // btMinkowskiSumShape is actually a MINKOWSKI_DIFFERENCE_SHAPE_PROXYTYPE and nothing
      // is one of these
      break;
    case BroadphaseNativeTypes.MINKOWSKI_DIFFERENCE_SHAPE_PROXYTYPE:
      return new btMinkowskiSumShape(swigCPtr, owner);
    case BroadphaseNativeTypes.BOX_2D_SHAPE_PROXYTYPE:
      return new btBox2dShape(swigCPtr, owner);
    case BroadphaseNativeTypes.CONVEX_2D_SHAPE_PROXYTYPE:
      return new btConvex2dShape(swigCPtr, owner);
    case BroadphaseNativeTypes.CUSTOM_CONVEX_SHAPE_TYPE:
      // TODO ?
      break;
    case BroadphaseNativeTypes.TRIANGLE_MESH_SHAPE_PROXYTYPE:
      return new btBvhTriangleMeshShape(swigCPtr, owner);
    case BroadphaseNativeTypes.SCALED_TRIANGLE_MESH_SHAPE_PROXYTYPE:
      return new btScaledBvhTriangleMeshShape(swigCPtr, owner);
    case BroadphaseNativeTypes.FAST_CONCAVE_MESH_PROXYTYPE:
      // TODO I couldn't find one
      break;
    case BroadphaseNativeTypes.TERRAIN_SHAPE_PROXYTYPE:
      return new btHeightfieldTerrainShape(swigCPtr, owner);
/*
    case BroadphaseNativeTypes.GIMPACT_SHAPE_PROXYTYPE:
      return new btGimpactShape(swigCPtr, owner);
*/
    case BroadphaseNativeTypes.MULTIMATERIAL_TRIANGLE_MESH_PROXYTYPE:
      return new btMultimaterialTriangleMeshShape(swigCPtr, owner);
    case BroadphaseNativeTypes.EMPTY_SHAPE_PROXYTYPE:
      return new btEmptyShape(swigCPtr, owner);
    case BroadphaseNativeTypes.STATIC_PLANE_PROXYTYPE:
      return new btStaticPlaneShape(swigCPtr, owner);
    case BroadphaseNativeTypes.CUSTOM_CONCAVE_SHAPE_TYPE:
      // TODO ?
      break;
    case BroadphaseNativeTypes.COMPOUND_SHAPE_PROXYTYPE:
      return new btCompoundShape(swigCPtr, owner);
/*
    case BroadphaseNativeTypes.SOFTBODY_SHAPE_PROXYTYPE:
      return new btSoftBodyShape(swigCPtr, owner);
    case BroadphaseNativeTypes.HFFLUID_SHAPE_PROXYTYPE:
      return new (swigCPtr, owner);
    case BroadphaseNativeTypes.HFFLUID_BUOYANT_CONVEX_SHAPE_PROXYTYPE:
      return new (swigCPtr, owner);
    case BroadphaseNativeTypes.INVALID_SHAPE_PROXYTYPE:
      return new (swigCPtr, owner);
*/
    }

    throw new RuntimeException("Unknown shape type " + Integer.toString(shapeType));
  }
%}