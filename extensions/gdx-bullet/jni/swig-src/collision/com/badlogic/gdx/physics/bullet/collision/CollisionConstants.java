/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.12
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package com.badlogic.gdx.physics.bullet.collision;

import com.badlogic.gdx.physics.bullet.BulletBase;
import com.badlogic.gdx.physics.bullet.linearmath.*;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Matrix4;

public interface CollisionConstants {
  public final static String btQuantizedBvhDataName = "btQuantizedBvhFloatData";
  public final static int MAX_SUBTREE_SIZE_IN_BYTES = 2048;
  public final static int MAX_NUM_PARTS_IN_BITS = 10;
  public final static int USE_OVERLAP_TEST_ON_REMOVES = 1;
  public final static int MAX_PREFERRED_PENETRATION_DIRECTIONS = 10;
  public final static int TRI_INFO_V0V1_CONVEX = 1;
  public final static int TRI_INFO_V1V2_CONVEX = 2;
  public final static int TRI_INFO_V2V0_CONVEX = 4;
  public final static int TRI_INFO_V0V1_SWAP_NORMALB = 8;
  public final static int TRI_INFO_V1V2_SWAP_NORMALB = 16;
  public final static int TRI_INFO_V2V0_SWAP_NORMALB = 32;
  public final static int TEST_INTERNAL_OBJECTS = 1;
  public final static int ACTIVE_TAG = 1;
  public final static int ISLAND_SLEEPING = 2;
  public final static int WANTS_DEACTIVATION = 3;
  public final static int DISABLE_DEACTIVATION = 4;
  public final static int DISABLE_SIMULATION = 5;
  public final static String btCollisionObjectDataName = "btCollisionObjectFloatData";
  public final static int DBVT_IMPL_GENERIC = 0;
  public final static int DBVT_IMPL_SSE = 1;
  public final static int DBVT_USE_TEMPLATE = 0;
  public final static int DBVT_USE_INTRINSIC_SSE = 1;
  public final static int DBVT_USE_MEMMOVE = 1;
  public final static int DBVT_ENABLE_BENCHMARK = 0;
  public final static int DBVT_SELECT_IMPL = 0;
  public final static int DBVT_MERGE_IMPL = 0;
  public final static int DBVT_INT0_IMPL = 0;
  public final static int DBVT_BP_PROFILE = 0;
  public final static int DBVT_BP_PREVENTFALSEUPDATE = 0;
  public final static int DBVT_BP_ACCURATESLEEPING = 0;
  public final static int DBVT_BP_ENABLE_BENCHMARK = 0;
  public final static int USE_PATH_COMPRESSION = 1;
  public final static int STATIC_SIMULATION_ISLAND_OPTIMIZATION = 1;
  public final static int USE_DISPATCH_REGISTRY_ARRAY = 1;
  public final static int MANIFOLD_CACHE_SIZE = 4;
  public final static int NO_VIRTUAL_INTERFACE = 1;
  public final static int VORONOI_SIMPLEX_MAX_VERTS = 5;
  public final static double VORONOI_DEFAULT_EQUAL_VERTEX_THRESHOLD = 0.0001;
  public final static double BOX_PLANE_EPSILON = 0.000001;
  public final static double PLANEDIREPSILON = 0.0000001;
  public final static double PARALELENORMALS = 0.000001;
  public final static int MAX_TRI_CLIPPING = 16;
  public final static int NORMAL_CONTACT_AVERAGE = 1;
  public final static double CONTACT_DIFF_EPSILON = 0.00001;
  public final static int BT_DEFAULT_MAX_POOLS = 16;
  public final static int GIMPACT_VS_PLANE_COLLISION = 1;
  public final static int GIM_ARRAY_GROW_INCREMENT = 2;
  public final static int GIM_ARRAY_GROW_FACTOR = 2;
  public final static int GUINT_BIT_COUNT = 32;
  public final static int GUINT_EXPONENT = 5;
  public final static int GIM_INVALID_HASH = 0xffffffff;
  public final static int GIM_DEFAULT_HASH_TABLE_SIZE = 380;
  public final static int GIM_DEFAULT_HASH_TABLE_NODE_SIZE = 4;
  public final static int GIM_HASH_TABLE_GROW_FACTOR = 2;
  public final static int GIM_MIN_RADIX_SORT_SIZE = 860;
  public final static int GIM_NUM_PRIME = 28;
  public final static double G_PI = 3.14159265358979;
  public final static double G_HALF_PI = 1.5707963;
  public final static double G_TWO_PI = 6.28318530;
  public final static double G_ROOT3 = 1.73205;
  public final static double G_ROOT2 = 1.41421;
  public final static int G_UINT_INFINITY = 0xffffffff;
  public final static int G_SIGN_BITMASK = 0x80000000;
  public final static int kHist = 2048;
  public final static int GJK_MAX_ITERATIONS = 128;
  public final static int EPA_MAX_VERTICES = 64;
  public final static int EPA_MAX_FACES = (64*2);
  public final static int EPA_MAX_ITERATIONS = 255;
  public final static double BT_MPR_TOLERANCE = 1E-6;
  public final static int BT_MPR_MAX_ITERATIONS = 1000;
}
