#ifndef GDX_MATHTYPES_H
#define GDX_MATHTYPES_H

#include <jni.h>
#include <gdx/common/jniHelpers.h>
#include <LinearMath/btVector3.h>
#include <LinearMath/btQuaternion.h>
#include <LinearMath/btMatrix3x3.h>
#include <LinearMath/btTransform.h>

void Vector3_to_btVector3(JNIEnv * const &jenv, btVector3 &target, jobject &source);
void btVector3_to_Vector3(JNIEnv * const &jenv, jobject &target, const btVector3 &source);

void Quaternion_to_btQuaternion(JNIEnv * const &jenv, btQuaternion &target, jobject &source);
void btQuaternion_to_Quaternion(JNIEnv * const &jenv, jobject &target, const btQuaternion & source);

void Matrix3_to_btMatrix3(JNIEnv * const &jenv, btMatrix3x3 &target, jobject &source);
void btMatrix3_to_Matrix3(JNIEnv * const &jenv, jobject &target, const btMatrix3x3 &source);

void Matrix4_to_btTransform(JNIEnv * const &jenv, btTransform &target, jobject &source);
void btTransform_to_Matrix4(JNIEnv * const &jenv, jobject &target, const btTransform &source);

#endif //GDX_MATHTYPES_H