/*
 * Use some libgdx types instead of Bullet types.
 */

// Vector3:
%{
#include <LinearMath/btVector3.h>
#define Vector3_to_btVector3(e, t, s) \
	static jfieldID xField = NULL, yField = NULL, zField = NULL; \
	if (xField == NULL) { \
		jclass sc = e->GetObjectClass(s); \
		xField = e->GetFieldID(sc, "x", "F"); \
		yField = e->GetFieldID(sc, "y", "F"); \
		zField = e->GetFieldID(sc, "z", "F"); \
		e->DeleteLocalRef(sc); \
	} \
	t.setValue(e->GetFloatField(s, xField), e->GetFloatField(s, yField), e->GetFloatField(s, zField));
	
#define btVector3_to_Vector3(e, t, s) \
	static jfieldID xField = NULL, yField = NULL, zField = NULL; \
	if (xField == NULL) { \
		jclass tc = e->GetObjectClass(t); \
		xField = e->GetFieldID(tc, "x", "F"); \
		yField = e->GetFieldID(tc, "y", "F"); \
		zField = e->GetFieldID(tc, "z", "F"); \
		e->DeleteLocalRef(tc); \
	  } \
	  e->SetFloatField(t, xField, s.getX()); \
	  e->SetFloatField(t, yField, s.getY()); \
	  e->SetFloatField(t, zField, s.getZ());
	 
%}
CREATE_POOLED_TYPEMAP(btVector3, Vector3, "Lcom/badlogic/gdx/math/Vector3;", Vector3_to_btVector3, btVector3_to_Vector3);
ENABLE_POOLED_TYPEMAP(btVector3, Vector3, "Lcom/badlogic/gdx/math/Vector3;");

// Quaternion:
%{
#include <LinearMath/btQuaternion.h>
SWIGINTERN inline void Quaternion_to_btQuaternion(JNIEnv * jenv, btQuaternion & target, jobject source) {
	static jfieldID xField = NULL, yField = NULL, zField = NULL, wField = NULL;
	if (xField == NULL) {
		jclass sourceClass = jenv->GetObjectClass(source);
		xField = jenv->GetFieldID(sourceClass, "x", "F");
		yField = jenv->GetFieldID(sourceClass, "y", "F");
		zField = jenv->GetFieldID(sourceClass, "z", "F");
		wField = jenv->GetFieldID(sourceClass, "w", "F");
		jenv->DeleteLocalRef(sourceClass);
	}
	
	target.setValue(
			jenv->GetFloatField(source, xField),
			jenv->GetFloatField(source, yField),
			jenv->GetFloatField(source, zField),
			jenv->GetFloatField(source, wField));
}

SWIGINTERN inline void btQuaternion_to_Quaternion(JNIEnv * jenv, jobject target, const btQuaternion & source) {
	static jfieldID xField = NULL, yField = NULL, zField = NULL, wField = NULL;
	if (xField == NULL) {
		jclass targetClass = jenv->GetObjectClass(target);
		xField = jenv->GetFieldID(targetClass, "x", "F");
		yField = jenv->GetFieldID(targetClass, "y", "F");
		zField = jenv->GetFieldID(targetClass, "z", "F");
		wField = jenv->GetFieldID(targetClass, "w", "F");
		jenv->DeleteLocalRef(targetClass);
	}
	
	jenv->SetFloatField(target, xField, source.getX());
	jenv->SetFloatField(target, yField, source.getY());
	jenv->SetFloatField(target, zField, source.getZ());
	jenv->SetFloatField(target, wField, source.getW());
}
%}
CREATE_POOLED_TYPEMAP(btQuaternion, Quaternion, "Lcom/badlogic/gdx/math/Quaternion;", Quaternion_to_btQuaternion, btQuaternion_to_Quaternion);
ENABLE_POOLED_TYPEMAP(btQuaternion, Quaternion, "Lcom/badlogic/gdx/math/Quaternion;");

// Matrix3:
%{
#include <LinearMath/btMatrix3x3.h>
SWIGINTERN inline void Matrix3_to_btMatrix3(JNIEnv * jenv, btMatrix3x3 & target, jobject source) {	  
	static jfieldID valField = NULL;
	if (valField == NULL) {
		jclass sourceClass = jenv->GetObjectClass(source);
		valField = jenv->GetFieldID(sourceClass, "val", "[F");
		jenv->DeleteLocalRef(sourceClass);
	}
	
	jfloatArray valArray = (jfloatArray) jenv->GetObjectField(source, valField);
	jfloat * elements = jenv->GetFloatArrayElements(valArray, NULL);
	
	// Convert to column-major
	target.setValue(
	elements[0], elements[3], elements[6],
	elements[1], elements[4], elements[7],
	elements[2], elements[5], elements[8]);
	
	jenv->ReleaseFloatArrayElements(valArray, elements, JNI_ABORT);
	jenv->DeleteLocalRef(valArray);
}
SWIGINTERN inline void btMatrix3_to_Matrix3(JNIEnv * jenv, jobject target, const btMatrix3x3 & source) {
	static jfieldID valField = NULL;
	if (valField == NULL) {
		jclass targetClass = jenv->GetObjectClass(target);
		valField = jenv->GetFieldID(targetClass, "val", "[F");
		jenv->DeleteLocalRef(targetClass);
	}
	
	jfloatArray valArray = (jfloatArray) jenv->GetObjectField(target, valField);
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
%}
CREATE_POOLED_TYPEMAP(btMatrix3x3, Matrix3, "Lcom/badlogic/gdx/math/Matrix3;", Matrix3_to_btMatrix3, btMatrix3_to_Matrix3);
ENABLE_POOLED_TYPEMAP(btMatrix3x3, Matrix3, "Lcom/badlogic/gdx/math/Matrix3;");

// Matrix4:
%{
#include <LinearMath/btTransform.h>
SWIGINTERN inline void Matrix4_to_btTransform(JNIEnv * jenv, btTransform & target, jobject source) {
	static jfieldID valField = NULL;
	if (valField == NULL) {
		jclass sourceClass = jenv->GetObjectClass(source);
		valField = jenv->GetFieldID(sourceClass, "val", "[F");
		jenv->DeleteLocalRef(sourceClass);
	}
	
	jfloatArray valArray = (jfloatArray) jenv->GetObjectField(source, valField);
	jfloat * elements = jenv->GetFloatArrayElements(valArray, NULL);
	
	target.setFromOpenGLMatrix(elements);
	
	jenv->ReleaseFloatArrayElements(valArray, elements, JNI_ABORT);
	jenv->DeleteLocalRef(valArray);
}
	
SWIGINTERN inline void btTransform_to_Matrix4(JNIEnv * jenv, jobject target, const btTransform & source) {
	static jfieldID valField = NULL;
	if (valField == NULL) {
		jclass targetClass = jenv->GetObjectClass(target);
		valField = jenv->GetFieldID(targetClass, "val", "[F");
		jenv->DeleteLocalRef(targetClass);
	}
	
	jfloatArray valArray = (jfloatArray) jenv->GetObjectField(target, valField);
	jfloat * elements = jenv->GetFloatArrayElements(valArray, NULL);

	source.getOpenGLMatrix(elements);
	
	jenv->ReleaseFloatArrayElements(valArray, elements, 0);
	jenv->DeleteLocalRef(valArray);
}
%}
CREATE_POOLED_TYPEMAP(btTransform, Matrix4, "Lcom/badlogic/gdx/math/Matrix4;", Matrix4_to_btTransform, btTransform_to_Matrix4);
ENABLE_POOLED_TYPEMAP(btTransform, Matrix4, "Lcom/badlogic/gdx/math/Matrix4;");