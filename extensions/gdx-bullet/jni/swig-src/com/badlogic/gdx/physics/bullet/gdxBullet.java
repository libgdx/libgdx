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

public class gdxBullet implements gdxBulletConstants {
  public static int btGetVersion() {
    return gdxBulletJNI.btGetVersion();
  }

  public static float btSqrt(float y) {
    return gdxBulletJNI.btSqrt(y);
  }

  public static float btFabs(float x) {
    return gdxBulletJNI.btFabs(x);
  }

  public static float btCos(float x) {
    return gdxBulletJNI.btCos(x);
  }

  public static float btSin(float x) {
    return gdxBulletJNI.btSin(x);
  }

  public static float btTan(float x) {
    return gdxBulletJNI.btTan(x);
  }

  public static float btAcos(float x) {
    return gdxBulletJNI.btAcos(x);
  }

  public static float btAsin(float x) {
    return gdxBulletJNI.btAsin(x);
  }

  public static float btAtan(float x) {
    return gdxBulletJNI.btAtan(x);
  }

  public static float btAtan2(float x, float y) {
    return gdxBulletJNI.btAtan2(x, y);
  }

  public static float btExp(float x) {
    return gdxBulletJNI.btExp(x);
  }

  public static float btLog(float x) {
    return gdxBulletJNI.btLog(x);
  }

  public static float btPow(float x, float y) {
    return gdxBulletJNI.btPow(x, y);
  }

  public static float btFmod(float x, float y) {
    return gdxBulletJNI.btFmod(x, y);
  }

  public static float btAtan2Fast(float y, float x) {
    return gdxBulletJNI.btAtan2Fast(y, x);
  }

  public static boolean btFuzzyZero(float x) {
    return gdxBulletJNI.btFuzzyZero(x);
  }

  public static boolean btEqual(float a, float eps) {
    return gdxBulletJNI.btEqual(a, eps);
  }

  public static boolean btGreaterEqual(float a, float eps) {
    return gdxBulletJNI.btGreaterEqual(a, eps);
  }

  public static int btIsNegative(float x) {
    return gdxBulletJNI.btIsNegative(x);
  }

  public static float btRadians(float x) {
    return gdxBulletJNI.btRadians(x);
  }

  public static float btDegrees(float x) {
    return gdxBulletJNI.btDegrees(x);
  }

  public static float btFsel(float a, float b, float c) {
    return gdxBulletJNI.btFsel(a, b, c);
  }

  public static boolean btMachineIsLittleEndian() {
    return gdxBulletJNI.btMachineIsLittleEndian();
  }

  public static long btSelect(long condition, long valueIfConditionNonZero, long valueIfConditionZero) {
    return gdxBulletJNI.btSelect__SWIG_0(condition, valueIfConditionNonZero, valueIfConditionZero);
  }

  public static int btSelect(long condition, int valueIfConditionNonZero, int valueIfConditionZero) {
    return gdxBulletJNI.btSelect__SWIG_1(condition, valueIfConditionNonZero, valueIfConditionZero);
  }

  public static float btSelect(long condition, float valueIfConditionNonZero, float valueIfConditionZero) {
    return gdxBulletJNI.btSelect__SWIG_2(condition, valueIfConditionNonZero, valueIfConditionZero);
  }

  public static long btSwapEndian(long val) {
    return gdxBulletJNI.btSwapEndian__SWIG_0(val);
  }

  public static int btSwapEndian(int val) {
    return gdxBulletJNI.btSwapEndian__SWIG_1(val);
  }

  public static int btSwapEndian(short val) {
    return gdxBulletJNI.btSwapEndian__SWIG_3(val);
  }

  public static long btSwapEndianFloat(float d) {
    return gdxBulletJNI.btSwapEndianFloat(d);
  }

  public static float btUnswapEndianFloat(long a) {
    return gdxBulletJNI.btUnswapEndianFloat(a);
  }

  public static void btSwapEndianDouble(double d, java.nio.ByteBuffer dst) {
    assert dst.isDirect() : "Buffer must be allocated direct.";
    {
      gdxBulletJNI.btSwapEndianDouble(d, dst);
    }
  }

  public static double btUnswapEndianDouble(java.nio.ByteBuffer src) {
    assert src.isDirect() : "Buffer must be allocated direct.";
    {
      return gdxBulletJNI.btUnswapEndianDouble(src);
    }
  }

  public static float btNormalizeAngle(float angleInRadians) {
    return gdxBulletJNI.btNormalizeAngle(angleInRadians);
  }

  public static float btDot(Vector3 v1, Vector3 v2) {
    return gdxBulletJNI.btDot(v1, v2);
  }

  public static float btDistance2(Vector3 v1, Vector3 v2) {
    return gdxBulletJNI.btDistance2(v1, v2);
  }

  public static float btDistance(Vector3 v1, Vector3 v2) {
    return gdxBulletJNI.btDistance(v1, v2);
  }

  public static float btAngle(Vector3 v1, Vector3 v2) {
    return gdxBulletJNI.btAngle(v1, v2);
  }

  public static Vector3 btCross(Vector3 v1, Vector3 v2) {
	return gdxBulletJNI.btCross(v1, v2);
}

  public static float btTriple(Vector3 v1, Vector3 v2, Vector3 v3) {
    return gdxBulletJNI.btTriple(v1, v2, v3);
  }

  public static Vector3 lerp(Vector3 v1, Vector3 v2, float t) {
	return gdxBulletJNI.lerp(v1, v2, t);
}

  public static void btSwapScalarEndian(float sourceVal, SWIGTYPE_p_float destVal) {
    gdxBulletJNI.btSwapScalarEndian(sourceVal, SWIGTYPE_p_float.getCPtr(destVal));
  }

  public static void btSwapVector3Endian(Vector3 sourceVec, Vector3 destVec) {
    gdxBulletJNI.btSwapVector3Endian(sourceVec, destVec);
  }

  public static void btUnSwapVector3Endian(Vector3 vector) {
    gdxBulletJNI.btUnSwapVector3Endian(vector);
  }

  public static float dot(Quaternion q1, Quaternion q2) {
    return gdxBulletJNI.dot(q1, q2);
  }

  public static float length(Quaternion q) {
    return gdxBulletJNI.length(q);
  }

  public static float angle(Quaternion q1, Quaternion q2) {
    return gdxBulletJNI.angle(q1, q2);
  }

  public static Quaternion inverse(Quaternion q) {
	return gdxBulletJNI.inverse(q);
}

  public static Quaternion slerp(Quaternion q1, Quaternion q2, float t) {
	return gdxBulletJNI.slerp(q1, q2, t);
}

  public static Vector3 quatRotate(Quaternion rotation, Vector3 v) {
	return gdxBulletJNI.quatRotate(rotation, v);
}

  public static Quaternion shortestArcQuat(Vector3 v0, Vector3 v1) {
	return gdxBulletJNI.shortestArcQuat(v0, v1);
}

  public static Quaternion shortestArcQuatNormalize2(Vector3 v0, Vector3 v1) {
	return gdxBulletJNI.shortestArcQuatNormalize2(v0, v1);
}

  public static void AabbExpand(Vector3 aabbMin, Vector3 aabbMax, Vector3 expansionMin, Vector3 expansionMax) {
    gdxBulletJNI.AabbExpand(aabbMin, aabbMax, expansionMin, expansionMax);
  }

  public static boolean TestPointAgainstAabb2(Vector3 aabbMin1, Vector3 aabbMax1, Vector3 point) {
    return gdxBulletJNI.TestPointAgainstAabb2(aabbMin1, aabbMax1, point);
  }

  public static boolean TestAabbAgainstAabb2(Vector3 aabbMin1, Vector3 aabbMax1, Vector3 aabbMin2, Vector3 aabbMax2) {
    return gdxBulletJNI.TestAabbAgainstAabb2(aabbMin1, aabbMax1, aabbMin2, aabbMax2);
  }

  public static boolean TestTriangleAgainstAabb2(btVector3 vertices, Vector3 aabbMin, Vector3 aabbMax) {
    return gdxBulletJNI.TestTriangleAgainstAabb2(btVector3.getCPtr(vertices), vertices, aabbMin, aabbMax);
  }

  public static int btOutcode(Vector3 p, Vector3 halfExtent) {
    return gdxBulletJNI.btOutcode(p, halfExtent);
  }

  public static boolean btRayAabb2(Vector3 rayFrom, Vector3 rayInvDirection, long[] raySign, btVector3 bounds, SWIGTYPE_p_float tmin, float lambda_min, float lambda_max) {
    return gdxBulletJNI.btRayAabb2(rayFrom, rayInvDirection, raySign, btVector3.getCPtr(bounds), bounds, SWIGTYPE_p_float.getCPtr(tmin), lambda_min, lambda_max);
  }

  public static boolean btRayAabb(Vector3 rayFrom, Vector3 rayTo, Vector3 aabbMin, Vector3 aabbMax, SWIGTYPE_p_float param, Vector3 normal) {
    return gdxBulletJNI.btRayAabb(rayFrom, rayTo, aabbMin, aabbMax, SWIGTYPE_p_float.getCPtr(param), normal);
  }

  public static void btTransformAabb(Vector3 halfExtents, float margin, Matrix4 t, Vector3 aabbMinOut, Vector3 aabbMaxOut) {
    gdxBulletJNI.btTransformAabb__SWIG_0(halfExtents, margin, t, aabbMinOut, aabbMaxOut);
  }

  public static void btTransformAabb(Vector3 localAabbMin, Vector3 localAabbMax, float margin, Matrix4 trans, Vector3 aabbMinOut, Vector3 aabbMaxOut) {
    gdxBulletJNI.btTransformAabb__SWIG_1(localAabbMin, localAabbMax, margin, trans, aabbMinOut, aabbMaxOut);
  }

  public static long testQuantizedAabbAgainstQuantizedAabb(java.nio.IntBuffer aabbMin1, java.nio.IntBuffer aabbMax1, java.nio.IntBuffer aabbMin2, java.nio.IntBuffer aabbMax2) {
    assert aabbMin1.isDirect() : "Buffer must be allocated direct.";
    assert aabbMax1.isDirect() : "Buffer must be allocated direct.";
    assert aabbMin2.isDirect() : "Buffer must be allocated direct.";
    assert aabbMax2.isDirect() : "Buffer must be allocated direct.";
    {
      return gdxBulletJNI.testQuantizedAabbAgainstQuantizedAabb(aabbMin1, aabbMax1, aabbMin2, aabbMax2);
    }
  }

  public static void GEN_srand(long seed) {
    gdxBulletJNI.GEN_srand(seed);
  }

  public static long GEN_rand() {
    return gdxBulletJNI.GEN_rand();
  }

  public static Vector3 btAabbSupport(Vector3 halfExtents, Vector3 supportDir) {
	return gdxBulletJNI.btAabbSupport(halfExtents, supportDir);
}

  public static void GrahamScanConvexHull2D(SWIGTYPE_p_btAlignedObjectArrayT_GrahamVector2_t originalPoints, SWIGTYPE_p_btAlignedObjectArrayT_GrahamVector2_t hull) {
    gdxBulletJNI.GrahamScanConvexHull2D(SWIGTYPE_p_btAlignedObjectArrayT_GrahamVector2_t.getCPtr(originalPoints), SWIGTYPE_p_btAlignedObjectArrayT_GrahamVector2_t.getCPtr(hull));
  }

  public static SWIGTYPE_p_void btAlignedAllocInternal(long size, int alignment) {
    long cPtr = gdxBulletJNI.btAlignedAllocInternal(size, alignment);
    return (cPtr == 0) ? null : new SWIGTYPE_p_void(cPtr, false);
  }

  public static void btAlignedFreeInternal(SWIGTYPE_p_void ptr) {
    gdxBulletJNI.btAlignedFreeInternal(SWIGTYPE_p_void.getCPtr(ptr));
  }

  public static void btAlignedAllocSetCustom(SWIGTYPE_p_f_size_t__p_void allocFunc, SWIGTYPE_p_f_p_void__void freeFunc) {
    gdxBulletJNI.btAlignedAllocSetCustom(SWIGTYPE_p_f_size_t__p_void.getCPtr(allocFunc), SWIGTYPE_p_f_p_void__void.getCPtr(freeFunc));
  }

  public static void btAlignedAllocSetCustomAligned(SWIGTYPE_p_f_size_t_int__p_void allocFunc, SWIGTYPE_p_f_p_void__void freeFunc) {
    gdxBulletJNI.btAlignedAllocSetCustomAligned(SWIGTYPE_p_f_size_t_int__p_void.getCPtr(allocFunc), SWIGTYPE_p_f_p_void__void.getCPtr(freeFunc));
  }

  public static int getBT_HASH_NULL() {
    return gdxBulletJNI.BT_HASH_NULL_get();
  }

  public static boolean Intersect(btDbvtAabbMm a, btDbvtAabbMm b) {
    return gdxBulletJNI.Intersect__SWIG_0(btDbvtAabbMm.getCPtr(a), a, btDbvtAabbMm.getCPtr(b), b);
  }

  public static boolean Intersect(btDbvtAabbMm a, Vector3 b) {
    return gdxBulletJNI.Intersect__SWIG_1(btDbvtAabbMm.getCPtr(a), a, b);
  }

  public static float Proximity(btDbvtAabbMm a, btDbvtAabbMm b) {
    return gdxBulletJNI.Proximity(btDbvtAabbMm.getCPtr(a), a, btDbvtAabbMm.getCPtr(b), b);
  }

  public static int Select(btDbvtAabbMm o, btDbvtAabbMm a, btDbvtAabbMm b) {
    return gdxBulletJNI.Select(btDbvtAabbMm.getCPtr(o), o, btDbvtAabbMm.getCPtr(a), a, btDbvtAabbMm.getCPtr(b), b);
  }

  public static void Merge(btDbvtAabbMm a, btDbvtAabbMm b, btDbvtAabbMm r) {
    gdxBulletJNI.Merge(btDbvtAabbMm.getCPtr(a), a, btDbvtAabbMm.getCPtr(b), b, btDbvtAabbMm.getCPtr(r), r);
  }

  public static boolean NotEqual(btDbvtAabbMm a, btDbvtAabbMm b) {
    return gdxBulletJNI.NotEqual(btDbvtAabbMm.getCPtr(a), a, btDbvtAabbMm.getCPtr(b), b);
  }

  public static void setGOverlappingPairs(int value) {
    gdxBulletJNI.gOverlappingPairs_set(value);
  }

  public static int getGOverlappingPairs() {
    return gdxBulletJNI.gOverlappingPairs_get();
  }

  public static void setGRemovePairs(int value) {
    gdxBulletJNI.gRemovePairs_set(value);
  }

  public static int getGRemovePairs() {
    return gdxBulletJNI.gRemovePairs_get();
  }

  public static void setGAddedPairs(int value) {
    gdxBulletJNI.gAddedPairs_set(value);
  }

  public static int getGAddedPairs() {
    return gdxBulletJNI.gAddedPairs_get();
  }

  public static void setGFindPairs(int value) {
    gdxBulletJNI.gFindPairs_set(value);
  }

  public static int getGFindPairs() {
    return gdxBulletJNI.gFindPairs_get();
  }

  public static int getBT_NULL_PAIR() {
    return gdxBulletJNI.BT_NULL_PAIR_get();
  }

  public static boolean gdxCheckFilter(int filter, int flag) {
    return gdxBulletJNI.gdxCheckFilter__SWIG_0(filter, flag);
  }

  public static boolean gdxCheckFilter(btCollisionObject colObj0, btCollisionObject colObj1) {
    return gdxBulletJNI.gdxCheckFilter__SWIG_1(btCollisionObject.getCPtr(colObj0), colObj0, btCollisionObject.getCPtr(colObj1), colObj1);
  }

  public static void setGDeactivationTime(float value) {
    gdxBulletJNI.gDeactivationTime_set(value);
  }

  public static float getGDeactivationTime() {
    return gdxBulletJNI.gDeactivationTime_get();
  }

  public static void setGDisableDeactivation(boolean value) {
    gdxBulletJNI.gDisableDeactivation_set(value);
  }

  public static boolean getGDisableDeactivation() {
    return gdxBulletJNI.gDisableDeactivation_get();
  }

  public static void setGContactAddedCallback(SWIGTYPE_p_f_r_btManifoldPoint_p_q_const__btCollisionObjectWrapper_int_int_p_q_const__btCollisionObjectWrapper_int_int__bool value) {
    gdxBulletJNI.gContactAddedCallback_set(SWIGTYPE_p_f_r_btManifoldPoint_p_q_const__btCollisionObjectWrapper_int_int_p_q_const__btCollisionObjectWrapper_int_int__bool.getCPtr(value));
  }

  public static SWIGTYPE_p_f_r_btManifoldPoint_p_q_const__btCollisionObjectWrapper_int_int_p_q_const__btCollisionObjectWrapper_int_int__bool getGContactAddedCallback() {
    long cPtr = gdxBulletJNI.gContactAddedCallback_get();
    return (cPtr == 0) ? null : new SWIGTYPE_p_f_r_btManifoldPoint_p_q_const__btCollisionObjectWrapper_int_int_p_q_const__btCollisionObjectWrapper_int_int__bool(cPtr, false);
  }

  public static void btGenerateInternalEdgeInfo(btBvhTriangleMeshShape trimeshShape, btTriangleInfoMap triangleInfoMap) {
    gdxBulletJNI.btGenerateInternalEdgeInfo(btBvhTriangleMeshShape.getCPtr(trimeshShape), trimeshShape, btTriangleInfoMap.getCPtr(triangleInfoMap), triangleInfoMap);
  }

  public static void btAdjustInternalEdgeContacts(btManifoldPoint cp, btCollisionObjectWrapper trimeshColObj0Wrap, btCollisionObjectWrapper otherColObj1Wrap, int partId0, int index0, int normalAdjustFlags) {
    gdxBulletJNI.btAdjustInternalEdgeContacts__SWIG_0(btManifoldPoint.getCPtr(cp), cp, btCollisionObjectWrapper.getCPtr(trimeshColObj0Wrap), trimeshColObj0Wrap, btCollisionObjectWrapper.getCPtr(otherColObj1Wrap), otherColObj1Wrap, partId0, index0, normalAdjustFlags);
  }

  public static void btAdjustInternalEdgeContacts(btManifoldPoint cp, btCollisionObjectWrapper trimeshColObj0Wrap, btCollisionObjectWrapper otherColObj1Wrap, int partId0, int index0) {
    gdxBulletJNI.btAdjustInternalEdgeContacts__SWIG_1(btManifoldPoint.getCPtr(cp), cp, btCollisionObjectWrapper.getCPtr(trimeshColObj0Wrap), trimeshColObj0Wrap, btCollisionObjectWrapper.getCPtr(otherColObj1Wrap), otherColObj1Wrap, partId0, index0);
  }

  public static void setGContactBreakingThreshold(float value) {
    gdxBulletJNI.gContactBreakingThreshold_set(value);
  }

  public static float getGContactBreakingThreshold() {
    return gdxBulletJNI.gContactBreakingThreshold_get();
  }

  public static void setGContactDestroyedCallback(SWIGTYPE_p_f_p_void__bool value) {
    gdxBulletJNI.gContactDestroyedCallback_set(SWIGTYPE_p_f_p_void__bool.getCPtr(value));
  }

  public static SWIGTYPE_p_f_p_void__bool getGContactDestroyedCallback() {
    long cPtr = gdxBulletJNI.gContactDestroyedCallback_get();
    return (cPtr == 0) ? null : new SWIGTYPE_p_f_p_void__bool(cPtr, false);
  }

  public static void setGContactProcessedCallback(SWIGTYPE_p_f_r_btManifoldPoint_p_void_p_void__bool value) {
    gdxBulletJNI.gContactProcessedCallback_set(SWIGTYPE_p_f_r_btManifoldPoint_p_void_p_void__bool.getCPtr(value));
  }

  public static SWIGTYPE_p_f_r_btManifoldPoint_p_void_p_void__bool getGContactProcessedCallback() {
    long cPtr = gdxBulletJNI.gContactProcessedCallback_get();
    return (cPtr == 0) ? null : new SWIGTYPE_p_f_r_btManifoldPoint_p_void_p_void__bool(cPtr, false);
  }

  public static float btAdjustAngleToLimits(float angleInRadians, float angleLowerLimitInRadians, float angleUpperLimitInRadians) {
    return gdxBulletJNI.btAdjustAngleToLimits(angleInRadians, angleLowerLimitInRadians, angleUpperLimitInRadians);
  }

  public static void InternalTickCallback_CB(btDynamicsWorld world, float timeStep) {
    gdxBulletJNI.InternalTickCallback_CB(btDynamicsWorld.getCPtr(world), world, timeStep);
  }

  public static boolean ContactAddedListener_CB(btManifoldPoint cp, btCollisionObjectWrapper colObj0Wrap, int partId0, int index0, btCollisionObjectWrapper colObj1Wrap, int partId1, int index1) {
    return gdxBulletJNI.ContactAddedListener_CB(cp, btCollisionObjectWrapper.getCPtr(colObj0Wrap), colObj0Wrap, partId0, index0, btCollisionObjectWrapper.getCPtr(colObj1Wrap), colObj1Wrap, partId1, index1);
  }

  public static void setCurrentContactAddedListener(SWIGTYPE_p_BaseContactAddedListener value) {
    gdxBulletJNI.currentContactAddedListener_set(SWIGTYPE_p_BaseContactAddedListener.getCPtr(value));
  }

  public static SWIGTYPE_p_BaseContactAddedListener getCurrentContactAddedListener() {
    long cPtr = gdxBulletJNI.currentContactAddedListener_get();
    return (cPtr == 0) ? null : new SWIGTYPE_p_BaseContactAddedListener(cPtr, false);
  }

  public static boolean ContactProcessedListener_CB(btManifoldPoint cp, SWIGTYPE_p_void body0, SWIGTYPE_p_void body1) {
    return gdxBulletJNI.ContactProcessedListener_CB(cp, SWIGTYPE_p_void.getCPtr(body0), SWIGTYPE_p_void.getCPtr(body1));
  }

  public static void setCurrentContactProcessedListener(SWIGTYPE_p_BaseContactProcessedListener value) {
    gdxBulletJNI.currentContactProcessedListener_set(SWIGTYPE_p_BaseContactProcessedListener.getCPtr(value));
  }

  public static SWIGTYPE_p_BaseContactProcessedListener getCurrentContactProcessedListener() {
    long cPtr = gdxBulletJNI.currentContactProcessedListener_get();
    return (cPtr == 0) ? null : new SWIGTYPE_p_BaseContactProcessedListener(cPtr, false);
  }

  public static boolean ContactDestroyedListener_CB(SWIGTYPE_p_void userPersistentData) {
    return gdxBulletJNI.ContactDestroyedListener_CB(SWIGTYPE_p_void.getCPtr(userPersistentData));
  }

  public static void setCurrentContactDestroyedListener(ContactDestroyedListener value) {
    gdxBulletJNI.currentContactDestroyedListener_set(ContactDestroyedListener.getCPtr(value), value);
  }

  public static ContactDestroyedListener getCurrentContactDestroyedListener() {
    long cPtr = gdxBulletJNI.currentContactDestroyedListener_get();
    return (cPtr == 0) ? null : new ContactDestroyedListener(cPtr, false);
  }

  public static float resolveSingleCollision(btRigidBody body1, btCollisionObject colObj2, Vector3 contactPositionWorld, Vector3 contactNormalOnB, btContactSolverInfo solverInfo, float distance) {
    return gdxBulletJNI.resolveSingleCollision(btRigidBody.getCPtr(body1), body1, btCollisionObject.getCPtr(colObj2), colObj2, contactPositionWorld, contactNormalOnB, btContactSolverInfo.getCPtr(solverInfo), solverInfo, distance);
  }

  public static void resolveSingleBilateral(btRigidBody body1, Vector3 pos1, btRigidBody body2, Vector3 pos2, float distance, Vector3 normal, SWIGTYPE_p_float impulse, float timeStep) {
    gdxBulletJNI.resolveSingleBilateral(btRigidBody.getCPtr(body1), body1, pos1, btRigidBody.getCPtr(body2), body2, pos2, distance, normal, SWIGTYPE_p_float.getCPtr(impulse), timeStep);
  }

  public static Matrix3 Lerp(Matrix3 a, Matrix3 b, float t) {
	return gdxBulletJNI.Lerp(a, b, t);
}

  public static Vector3 Clamp(Vector3 v, float maxlength) {
	return gdxBulletJNI.Clamp(v, maxlength);
}

  public static float ClusterMetric(Vector3 x, Vector3 y) {
    return gdxBulletJNI.ClusterMetric(x, y);
  }

  public static Matrix3 ScaleAlongAxis(Vector3 a, float s) {
	return gdxBulletJNI.ScaleAlongAxis(a, s);
}

  public static Matrix3 Cross(Vector3 v) {
	return gdxBulletJNI.Cross(v);
}

  public static Matrix3 Diagonal(float x) {
	return gdxBulletJNI.Diagonal(x);
}

  public static Matrix3 Add(Matrix3 a, Matrix3 b) {
	return gdxBulletJNI.Add(a, b);
}

  public static Matrix3 Sub(Matrix3 a, Matrix3 b) {
	return gdxBulletJNI.Sub(a, b);
}

  public static Matrix3 Mul(Matrix3 a, float b) {
	return gdxBulletJNI.Mul(a, b);
}

  public static void Orthogonalize(Matrix3 m) {
    gdxBulletJNI.Orthogonalize(m);
  }

  public static Matrix3 MassMatrix(float im, Matrix3 iwi, Vector3 r) {
	return gdxBulletJNI.MassMatrix(im, iwi, r);
}

  public static Matrix3 ImpulseMatrix(float dt, float ima, float imb, Matrix3 iwi, Vector3 r) {
	return gdxBulletJNI.ImpulseMatrix__SWIG_0(dt, ima, imb, iwi, r);
}

  public static Matrix3 ImpulseMatrix(float ima, Matrix3 iia, Vector3 ra, float imb, Matrix3 iib, Vector3 rb) {
	return gdxBulletJNI.ImpulseMatrix__SWIG_1(ima, iia, ra, imb, iib, rb);
}

  public static Matrix3 AngularImpulseMatrix(Matrix3 iia, Matrix3 iib) {
	return gdxBulletJNI.AngularImpulseMatrix(iia, iib);
}

  public static Vector3 ProjectOnAxis(Vector3 v, Vector3 a) {
	return gdxBulletJNI.ProjectOnAxis(v, a);
}

  public static Vector3 ProjectOnPlane(Vector3 v, Vector3 a) {
	return gdxBulletJNI.ProjectOnPlane(v, a);
}

  public static void ProjectOrigin(Vector3 a, Vector3 b, Vector3 prj, SWIGTYPE_p_float sqd) {
    gdxBulletJNI.ProjectOrigin__SWIG_0(a, b, prj, SWIGTYPE_p_float.getCPtr(sqd));
  }

  public static void ProjectOrigin(Vector3 a, Vector3 b, Vector3 c, Vector3 prj, SWIGTYPE_p_float sqd) {
    gdxBulletJNI.ProjectOrigin__SWIG_1(a, b, c, prj, SWIGTYPE_p_float.getCPtr(sqd));
  }

  public static Vector3 BaryCoord(Vector3 a, Vector3 b, Vector3 c, Vector3 p) {
	return gdxBulletJNI.BaryCoord(a, b, c, p);
}

  public static float ImplicitSolve(SWIGTYPE_p_btSoftBody__ImplicitFn fn, Vector3 a, Vector3 b, float accuracy, int maxiterations) {
    return gdxBulletJNI.ImplicitSolve__SWIG_0(SWIGTYPE_p_btSoftBody__ImplicitFn.getCPtr(fn), a, b, accuracy, maxiterations);
  }

  public static float ImplicitSolve(SWIGTYPE_p_btSoftBody__ImplicitFn fn, Vector3 a, Vector3 b, float accuracy) {
    return gdxBulletJNI.ImplicitSolve__SWIG_1(SWIGTYPE_p_btSoftBody__ImplicitFn.getCPtr(fn), a, b, accuracy);
  }

  public static Vector3 NormalizeAny(Vector3 v) {
	return gdxBulletJNI.NormalizeAny(v);
}

  public static btDbvtAabbMm VolumeOf(Face f, float margin) {
    return new btDbvtAabbMm(gdxBulletJNI.VolumeOf__SWIG_0(Face.getCPtr(f), f, margin), true);
  }

  public static Vector3 CenterOf(Face f) {
	return gdxBulletJNI.CenterOf(Face.getCPtr(f), f);
}

  public static float AreaOf(Vector3 x0, Vector3 x1, Vector3 x2) {
    return gdxBulletJNI.AreaOf(x0, x1, x2);
  }

  public static float VolumeOf(Vector3 x0, Vector3 x1, Vector3 x2, Vector3 x3) {
    return gdxBulletJNI.VolumeOf__SWIG_1(x0, x1, x2, x3);
  }

  public static void EvaluateMedium(btSoftBodyWorldInfo wfi, Vector3 x, sMedium medium) {
    gdxBulletJNI.EvaluateMedium(btSoftBodyWorldInfo.getCPtr(wfi), wfi, x, sMedium.getCPtr(medium), medium);
  }

  public static void ApplyClampedForce(Node n, Vector3 f, float dt) {
    gdxBulletJNI.ApplyClampedForce(Node.getCPtr(n), n, f, dt);
  }

  public static int MatchEdge(Node a, Node b, Node ma, Node mb) {
    return gdxBulletJNI.MatchEdge(Node.getCPtr(a), a, Node.getCPtr(b), b, Node.getCPtr(ma), ma, Node.getCPtr(mb), mb);
  }

  public static int PolarDecompose(Matrix3 m, Matrix3 q, Matrix3 s) {
    return gdxBulletJNI.PolarDecompose(m, q, s);
  }

}
