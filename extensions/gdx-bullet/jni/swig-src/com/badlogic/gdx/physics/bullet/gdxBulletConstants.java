/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

 /* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 2.0.10
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package com.badlogic.gdx.physics.bullet;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Matrix4;

public interface gdxBulletConstants {
  public final static int BT_BULLET_VERSION = 281;
  public final static double BT_LARGE_FLOAT = 1e18;
  public final static String btVector3DataName = "btVector3FloatData";
  public final static int USE_BANCHLESS = 1;
  public final static int BT_USE_PLACEMENT_NEW = 1;
  public final static int DBVT_IMPL_GENERIC = 0;
  public final static int DBVT_IMPL_SSE = 1;
  public final static int DBVT_USE_TEMPLATE = 0;
  public final static int DBVT_USE_INTRINSIC_SSE = 1;
  public final static int DBVT_USE_MEMMOVE = 1;
  public final static int DBVT_ENABLE_BENCHMARK = 0;
  public final static int DBVT_SELECT_IMPL = 0;
  public final static int DBVT_MERGE_IMPL = 0;
  public final static int DBVT_INT0_IMPL = 0;
  public final static String btQuantizedBvhDataName = "btQuantizedBvhFloatData";
  public final static int MAX_SUBTREE_SIZE_IN_BYTES = 2048;
  public final static int MAX_NUM_PARTS_IN_BITS = 10;
  public final static int DBVT_BP_PROFILE = 0;
  public final static int DBVT_BP_PREVENTFALSEUPDATE = 0;
  public final static int DBVT_BP_ACCURATESLEEPING = 0;
  public final static int DBVT_BP_ENABLE_BENCHMARK = 0;
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
  public final static int USE_PATH_COMPRESSION = 1;
  public final static int STATIC_SIMULATION_ISLAND_OPTIMIZATION = 1;
  public final static int USE_DISPATCH_REGISTRY_ARRAY = 1;
  public final static int MANIFOLD_CACHE_SIZE = 4;
  public final static int VORONOI_SIMPLEX_MAX_VERTS = 5;
  public final static double VORONOI_DEFAULT_EQUAL_VERTEX_THRESHOLD = 0.0001;
  public final static int NO_VIRTUAL_INTERFACE = 1;
  public final static String btRigidBodyDataName = "btRigidBodyFloatData";
  public final static String btPoint2PointConstraintDataName = "btPoint2PointConstraintFloatData";
  public final static int BT_6DOF_FLAGS_AXIS_SHIFT = 3;
  public final static int _BT_USE_CENTER_LIMIT_ = 1;
  public final static String btHingeConstraintDataName = "btHingeConstraintFloatData";
  public final static String btSoftBodyDataName = "btSoftBodyFloatData";
}
