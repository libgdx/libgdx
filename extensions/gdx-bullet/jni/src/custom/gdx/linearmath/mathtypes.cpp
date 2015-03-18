#include "mathtypes.h"
#include <stdio.h>
#include <string.h>

////////////////////////////////
//////// btVector3      ////////
////////////////////////////////
static jfieldID vector3_x = NULL, vector3_y = NULL, vector3_z = NULL;

void vector3_getFields(JNIEnv * const &jenv, jobject &v3) {
	jclass cls = jenv->GetObjectClass(v3);
	vector3_x = jenv->GetFieldID(cls, "x", "F");
	vector3_y = jenv->GetFieldID(cls, "y", "F");
	vector3_z = jenv->GetFieldID(cls, "z", "F");
	jenv->DeleteLocalRef(cls);
}

inline void vector3_ensurefields(JNIEnv * const &jenv, jobject &v3) {
	if (!vector3_x) vector3_getFields(jenv, v3);
}

void Vector3_to_btVector3(JNIEnv * const &jenv, btVector3 &target, jobject &source)
{
	vector3_ensurefields(jenv, source);
	target.setValue(jenv->GetFloatField(source, vector3_x), jenv->GetFloatField(source, vector3_y), jenv->GetFloatField(source, vector3_z));
}
	
void btVector3_to_Vector3(JNIEnv * const &jenv, jobject &target, const btVector3 &source)
{
	vector3_ensurefields(jenv, target);
	jenv->SetFloatField(target, vector3_x, source.getX());
	jenv->SetFloatField(target, vector3_y, source.getY());
	jenv->SetFloatField(target, vector3_z, source.getZ());
}



////////////////////////////////
//////// btQuaternion   ////////
////////////////////////////////
static jfieldID quaternion_x = NULL, quaternion_y = NULL, quaternion_z = NULL, quaternion_w = NULL;

void quaternion_getFields(JNIEnv * const &jenv, jobject &q) {
	jclass cls = jenv->GetObjectClass(q);
	quaternion_x = jenv->GetFieldID(cls, "x", "F");
	quaternion_y = jenv->GetFieldID(cls, "y", "F");
	quaternion_z = jenv->GetFieldID(cls, "z", "F");
	quaternion_w = jenv->GetFieldID(cls, "w", "F");
	jenv->DeleteLocalRef(cls);
}

inline void quaternion_ensurefields(JNIEnv * const &jenv, jobject &q) {
	if (!quaternion_x) quaternion_getFields(jenv, q);
}

void Quaternion_to_btQuaternion(JNIEnv * const &jenv, btQuaternion &target, jobject &source)
{
	quaternion_ensurefields(jenv, source);
	target.setValue(
			jenv->GetFloatField(source, quaternion_x),
			jenv->GetFloatField(source, quaternion_y),
			jenv->GetFloatField(source, quaternion_z),
			jenv->GetFloatField(source, quaternion_w));
}

void btQuaternion_to_Quaternion(JNIEnv * const &jenv, jobject &target, const btQuaternion & source)
{
	quaternion_ensurefields(jenv, target);
	jenv->SetFloatField(target, quaternion_x, source.getX());
	jenv->SetFloatField(target, quaternion_y, source.getY());
	jenv->SetFloatField(target, quaternion_z, source.getZ());
	jenv->SetFloatField(target, quaternion_w, source.getW());
}

////////////////////////////////
//////// btMatrix3x3    ////////
////////////////////////////////
static jfieldID matrix3_val = NULL;

void matrix3_getFields(JNIEnv * const &jenv, jobject &m3) {
	jclass cls = jenv->GetObjectClass(m3);
	matrix3_val = jenv->GetFieldID(cls, "val", "[F");
	jenv->DeleteLocalRef(cls);
}

inline void matrix3_ensurefields(JNIEnv * const &jenv, jobject &m3) {
	if (!matrix3_val) matrix3_getFields(jenv, m3);
}

void Matrix3_to_btMatrix3(JNIEnv * const &jenv, btMatrix3x3 &target, jobject &source)
{	  
	matrix3_ensurefields(jenv, source);
	
	jfloatArray valArray = (jfloatArray) jenv->GetObjectField(source, matrix3_val);
	jfloat * elements = jenv->GetFloatArrayElements(valArray, NULL);
	
	// Convert to column-major
	target.setValue(
	elements[0], elements[3], elements[6],
	elements[1], elements[4], elements[7],
	elements[2], elements[5], elements[8]);
	
	jenv->ReleaseFloatArrayElements(valArray, elements, JNI_ABORT);
	jenv->DeleteLocalRef(valArray);
}

void btMatrix3_to_Matrix3(JNIEnv * const &jenv, jobject &target, const btMatrix3x3 &source)
{
	matrix3_ensurefields(jenv, target);
	
	jfloatArray valArray = (jfloatArray) jenv->GetObjectField(target, matrix3_val);
	jfloat * elements = jenv->GetFloatArrayElements(valArray, NULL);
	
	// Convert to column-major
	elements[0] = (jfloat) source.getColumn(0).getX();
	elements[1] = (jfloat) source.getColumn(0).getY();
	elements[2] = (jfloat) source.getColumn(0).getZ();
	elements[3] = (jfloat) source.getColumn(1).getX();
	elements[4] = (jfloat) source.getColumn(1).getY();
	elements[5] = (jfloat) source.getColumn(1).getZ();
	elements[6] = (jfloat) source.getColumn(2).getX();
	elements[7] = (jfloat) source.getColumn(2).getY();
	elements[8] = (jfloat) source.getColumn(2).getZ();
	
	jenv->ReleaseFloatArrayElements(valArray, elements, 0);  
	jenv->DeleteLocalRef(valArray);
}

////////////////////////////////
//////// btTransform    ////////
////////////////////////////////
static jfieldID matrix4_val = NULL;

void matrix4_getFields(JNIEnv * const &jenv, jobject &m4) {
	jclass cls = jenv->GetObjectClass(m4);
	matrix4_val = jenv->GetFieldID(cls, "val", "[F");
	jenv->DeleteLocalRef(cls);
}

inline void matrix4_ensurefields(JNIEnv * const &jenv, jobject &m4) {
	if (!matrix4_val) matrix4_getFields(jenv, m4);
}

void Matrix4_to_btTransform(JNIEnv * const &jenv, btTransform &target, jobject &source)
{
	matrix4_ensurefields(jenv, source);
	
	jfloatArray valArray = (jfloatArray) jenv->GetObjectField(source, matrix4_val);
	jfloat * elements = jenv->GetFloatArrayElements(valArray, NULL);
	
	target.setFromOpenGLMatrix(elements);
	
	jenv->ReleaseFloatArrayElements(valArray, elements, JNI_ABORT);
	jenv->DeleteLocalRef(valArray);
}
	
void btTransform_to_Matrix4(JNIEnv * const &jenv, jobject &target, const btTransform &source)
{
	matrix4_ensurefields(jenv, target);
	
	jfloatArray valArray = (jfloatArray) jenv->GetObjectField(target, matrix4_val);
	jfloat * elements = jenv->GetFloatArrayElements(valArray, NULL);

    ATTRIBUTE_ALIGNED16(btScalar dst[16]);
	source.getOpenGLMatrix(dst);
    
    memcpy(elements, dst, sizeof(btScalar)*16);
	
	jenv->ReleaseFloatArrayElements(valArray, elements, 0);
	jenv->DeleteLocalRef(valArray);
}