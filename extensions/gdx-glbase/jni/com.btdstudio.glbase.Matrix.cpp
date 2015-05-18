#include <com.btdstudio.glbase.Matrix.h>

//@line:5

	    #include "matrix.h"
	    
	    // Buffers for vector transforms (absolutely not thread safe)
	    float tmpMem[16];
    JNIEXPORT jlong JNICALL Java_com_btdstudio_glbase_Matrix_createMatrix(JNIEnv* env, jclass clazz) {


//@line:159

		Matrix* matrix = new Matrix();
		matrix->setIdentity();
		return (long long)matrix;
	

}

JNIEXPORT void JNICALL Java_com_btdstudio_glbase_Matrix_dispose(JNIEnv* env, jobject object, jlong handle) {


//@line:165

		delete (Matrix*)handle;
	

}

JNIEXPORT jfloat JNICALL Java_com_btdstudio_glbase_Matrix_getTmpMem(JNIEnv* env, jobject object, jint comp) {


//@line:169

		return tmpMem[comp];
	

}

JNIEXPORT void JNICALL Java_com_btdstudio_glbase_Matrix_setIdentity(JNIEnv* env, jobject object, jlong handle) {


//@line:173

		((Matrix*)handle)->setIdentity();
	

}

JNIEXPORT void JNICALL Java_com_btdstudio_glbase_Matrix_setZero(JNIEnv* env, jobject object, jlong handle) {


//@line:177

		((Matrix*)handle)->setZero();
	

}

JNIEXPORT void JNICALL Java_com_btdstudio_glbase_Matrix_setTranslation(JNIEnv* env, jobject object, jlong handle, jfloat tx, jfloat ty, jfloat tz) {


//@line:181

		((Matrix*)handle)->setTranslation(tx, ty, tz);
	

}

JNIEXPORT void JNICALL Java_com_btdstudio_glbase_Matrix_setScale(JNIEnv* env, jobject object, jlong handle, jfloat sx, jfloat sy, jfloat sz) {


//@line:185

		((Matrix*)handle)->setScale(sx, sy, sz);
	

}

JNIEXPORT void JNICALL Java_com_btdstudio_glbase_Matrix_setRotationX(JNIEnv* env, jobject object, jlong handle, jfloat angle) {


//@line:189

		((Matrix*)handle)->setRotationX(angle);
	

}

JNIEXPORT void JNICALL Java_com_btdstudio_glbase_Matrix_setRotationY(JNIEnv* env, jobject object, jlong handle, jfloat angle) {


//@line:193

		((Matrix*)handle)->setRotationY(angle);
	

}

JNIEXPORT void JNICALL Java_com_btdstudio_glbase_Matrix_setRotationZ(JNIEnv* env, jobject object, jlong handle, jfloat angle) {


//@line:197

		((Matrix*)handle)->setRotationZ(angle);
	

}

JNIEXPORT void JNICALL Java_com_btdstudio_glbase_Matrix_setFrustum(JNIEnv* env, jobject object, jlong handle, jfloat l, jfloat r, jfloat b, jfloat t, jfloat n, jfloat f) {


//@line:201

		((Matrix*)handle)->setFrustum(l, r, b, t, n, f);
	

}

JNIEXPORT void JNICALL Java_com_btdstudio_glbase_Matrix_setOrtho(JNIEnv* env, jobject object, jlong handle, jfloat l, jfloat r, jfloat b, jfloat t, jfloat n, jfloat f) {


//@line:205

		((Matrix*)handle)->setOrtho(l, r, b, t, n, f);
	

}

JNIEXPORT void JNICALL Java_com_btdstudio_glbase_Matrix_setPerspective(JNIEnv* env, jobject object, jlong handle, jfloat fovy, jfloat aspectRatio, jfloat near, jfloat far) {


//@line:209

		((Matrix*)handle)->setPerspective(fovy, aspectRatio, near, far);
	

}

JNIEXPORT void JNICALL Java_com_btdstudio_glbase_Matrix_translate(JNIEnv* env, jobject object, jlong handle, jfloat tx, jfloat ty, jfloat tz) {


//@line:213

		((Matrix*)handle)->translate(tx, ty, tz);
	

}

JNIEXPORT void JNICALL Java_com_btdstudio_glbase_Matrix_scale(JNIEnv* env, jobject object, jlong handle, jfloat sx, jfloat sy, jfloat sz) {


//@line:217

		((Matrix*)handle)->scale(sx, sy, sz);
	

}

JNIEXPORT void JNICALL Java_com_btdstudio_glbase_Matrix_rotateX(JNIEnv* env, jobject object, jlong handle, jfloat angle) {


//@line:221

		((Matrix*)handle)->rotateX(angle);
	

}

JNIEXPORT void JNICALL Java_com_btdstudio_glbase_Matrix_rotateY(JNIEnv* env, jobject object, jlong handle, jfloat angle) {


//@line:225

		((Matrix*)handle)->rotateY(angle);
	

}

JNIEXPORT void JNICALL Java_com_btdstudio_glbase_Matrix_rotateZ(JNIEnv* env, jobject object, jlong handle, jfloat angle) {


//@line:229

		((Matrix*)handle)->rotateZ(angle);
	

}

JNIEXPORT void JNICALL Java_com_btdstudio_glbase_Matrix_invert(JNIEnv* env, jobject object, jlong handle) {


//@line:233

		((Matrix*)handle)->invert();
	

}

JNIEXPORT void JNICALL Java_com_btdstudio_glbase_Matrix_transform3(JNIEnv* env, jobject object, jlong handle, jfloatArray obj_vector) {
	float* vector = (float*)env->GetPrimitiveArrayCritical(obj_vector, 0);


//@line:237

		tmpMem[0] = vector[0];
		tmpMem[1] = vector[1];
		tmpMem[2] = vector[2];
		((Matrix*)handle)->transform3(tmpMem);
	
	env->ReleasePrimitiveArrayCritical(obj_vector, vector, 0);

}

JNIEXPORT void JNICALL Java_com_btdstudio_glbase_Matrix_transform4(JNIEnv* env, jobject object, jlong handle, jfloatArray obj_vector) {
	float* vector = (float*)env->GetPrimitiveArrayCritical(obj_vector, 0);


//@line:244

		tmpMem[0] = vector[0];
		tmpMem[1] = vector[1];
		tmpMem[2] = vector[2];
		tmpMem[3] = vector[3];
		((Matrix*)handle)->transform4(tmpMem);
	
	env->ReleasePrimitiveArrayCritical(obj_vector, vector, 0);

}

JNIEXPORT void JNICALL Java_com_btdstudio_glbase_Matrix_lookAt(JNIEnv* env, jobject object, jlong handle, jfloatArray obj_eye, jfloatArray obj_center, jfloatArray obj_up) {
	float* eye = (float*)env->GetPrimitiveArrayCritical(obj_eye, 0);
	float* center = (float*)env->GetPrimitiveArrayCritical(obj_center, 0);
	float* up = (float*)env->GetPrimitiveArrayCritical(obj_up, 0);


//@line:252

		((Matrix*)handle)->lookAt(eye, center, up);
	
	env->ReleasePrimitiveArrayCritical(obj_eye, eye, 0);
	env->ReleasePrimitiveArrayCritical(obj_center, center, 0);
	env->ReleasePrimitiveArrayCritical(obj_up, up, 0);

}

JNIEXPORT void JNICALL Java_com_btdstudio_glbase_Matrix_multiply(JNIEnv* env, jobject object, jlong handle, jlong operandHandle) {


//@line:256

		((Matrix*)handle)->multiply((Matrix*)operandHandle);
	

}

JNIEXPORT void JNICALL Java_com_btdstudio_glbase_Matrix_premultiply(JNIEnv* env, jobject object, jlong handle, jlong operandHandle) {


//@line:260

		((Matrix*)handle)->premultiply((Matrix*)operandHandle);
	

}

JNIEXPORT void JNICALL Java_com_btdstudio_glbase_Matrix_copyFrom(JNIEnv* env, jobject object, jlong handle, jlong srcHandle) {


//@line:264

		((Matrix*)handle)->copyFrom((Matrix const*)srcHandle);
	

}

JNIEXPORT void JNICALL Java_com_btdstudio_glbase_Matrix_to3x4(JNIEnv* env, jobject object, jlong handle) {


//@line:268

		((Matrix*)handle)->to3x4(tmpMem);
	

}

JNIEXPORT void JNICALL Java_com_btdstudio_glbase_Matrix_to4x4(JNIEnv* env, jobject object, jlong handle) {


//@line:272

		for( int i=0; i<16; i++ ){
			tmpMem[i] = ((Matrix*)handle)->getMatrixPointer()[i];
		}
	

}

