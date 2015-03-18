#include <com.badlogic.gdx.backends.iosrobovm.IOSGLES10.h>

//@line:12

	#ifdef __APPLE__
	#include <TargetConditionals.h>
	
	#if TARGET_OS_IPHONE	
	#include <OpenGLES/ES1/gl.h>
	#include <OpenGLES/ES1/glext.h>
	#include <stdio.h>
	JNIEXPORT void JNICALL Java_com_badlogic_gdx_backends_iosrobovm_IOSGLES10_glActiveTexture(JNIEnv* env, jobject object, jint texture) {


//@line:23

		glActiveTexture(texture);
	

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_backends_iosrobovm_IOSGLES10_glBindTexture(JNIEnv* env, jobject object, jint target, jint texture) {


//@line:28

		glBindTexture(target, texture);
	 

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_backends_iosrobovm_IOSGLES10_glBlendFunc(JNIEnv* env, jobject object, jint sfactor, jint dfactor) {


//@line:33

		glBlendFunc(sfactor, dfactor);
	 

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_backends_iosrobovm_IOSGLES10_glClear(JNIEnv* env, jobject object, jint mask) {


//@line:38

		glClear(mask);
	 

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_backends_iosrobovm_IOSGLES10_glClearColor(JNIEnv* env, jobject object, jfloat red, jfloat green, jfloat blue, jfloat alpha) {


//@line:43

		glClearColor(red, green, blue, alpha);
	 

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_backends_iosrobovm_IOSGLES10_glClearDepthf(JNIEnv* env, jobject object, jfloat depth) {


//@line:48

		glClearDepthf(depth);
	 

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_backends_iosrobovm_IOSGLES10_glClearStencil(JNIEnv* env, jobject object, jint s) {


//@line:53

		glClearStencil(s);
	 

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_backends_iosrobovm_IOSGLES10_glColorMask(JNIEnv* env, jobject object, jboolean red, jboolean green, jboolean blue, jboolean alpha) {


//@line:58

		glColorMask(red, green, blue, alpha);
	 

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_backends_iosrobovm_IOSGLES10_glCompressedTexImage2D(JNIEnv* env, jobject object, jint target, jint level, jint internalformat, jint width, jint height, jint border, jint imageSize, jobject obj_data) {
	unsigned char* data = (unsigned char*)(obj_data?env->GetDirectBufferAddress(obj_data):0);


//@line:64

		// FIXME
	 

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_backends_iosrobovm_IOSGLES10_glCompressedTexSubImage2D(JNIEnv* env, jobject object, jint target, jint level, jint xoffset, jint yoffset, jint width, jint height, jint format, jint imageSize, jobject obj_data) {
	unsigned char* data = (unsigned char*)(obj_data?env->GetDirectBufferAddress(obj_data):0);


//@line:70

		// FIXME
	 

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_backends_iosrobovm_IOSGLES10_glCopyTexImage2D(JNIEnv* env, jobject object, jint target, jint level, jint internalformat, jint x, jint y, jint width, jint height, jint border) {


//@line:75

		// FIXME
	 

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_backends_iosrobovm_IOSGLES10_glCopyTexSubImage2D(JNIEnv* env, jobject object, jint target, jint level, jint xoffset, jint yoffset, jint x, jint y, jint width, jint height) {


//@line:80

		glCopyTexSubImage2D(target, level, xoffset, yoffset, x, y, width, height);
	 

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_backends_iosrobovm_IOSGLES10_glCullFace(JNIEnv* env, jobject object, jint mode) {


//@line:85

		glCullFace(mode);
	

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_backends_iosrobovm_IOSGLES10_glDeleteTextures__ILjava_nio_IntBuffer_2(JNIEnv* env, jobject object, jint n, jobject obj_textures) {
	int* textures = (int*)(obj_textures?env->GetDirectBufferAddress(obj_textures):0);


//@line:90

		// FIXME
	 

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_backends_iosrobovm_IOSGLES10_glDepthFunc(JNIEnv* env, jobject object, jint func) {


//@line:95

		glDepthFunc(func);
	 

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_backends_iosrobovm_IOSGLES10_glDepthMask(JNIEnv* env, jobject object, jboolean flag) {


//@line:100

		glDepthMask(flag);
	 

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_backends_iosrobovm_IOSGLES10_glDepthRangef(JNIEnv* env, jobject object, jfloat zNear, jfloat zFar) {


//@line:105

		glDepthRangef(zNear, zFar);
	 

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_backends_iosrobovm_IOSGLES10_glDisable(JNIEnv* env, jobject object, jint cap) {


//@line:110

		glDisable(cap);
	 

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_backends_iosrobovm_IOSGLES10_glDrawArrays(JNIEnv* env, jobject object, jint mode, jint first, jint count) {


//@line:115

		glDrawArrays(mode, first, count);
	 

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_backends_iosrobovm_IOSGLES10_glDrawElements__IIILjava_nio_Buffer_2(JNIEnv* env, jobject object, jint mode, jint count, jint type, jobject obj_indices) {
	unsigned char* indices = (unsigned char*)(obj_indices?env->GetDirectBufferAddress(obj_indices):0);


//@line:120

		// FIXME
	 

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_backends_iosrobovm_IOSGLES10_glEnable(JNIEnv* env, jobject object, jint cap) {


//@line:125

		glEnable(cap);
	 

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_backends_iosrobovm_IOSGLES10_glFinish(JNIEnv* env, jobject object) {


//@line:130

		glFinish();
	 

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_backends_iosrobovm_IOSGLES10_glFlush(JNIEnv* env, jobject object) {


//@line:135

		glFlush();
	 

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_backends_iosrobovm_IOSGLES10_glFrontFace(JNIEnv* env, jobject object, jint mode) {


//@line:140

		glFrontFace(mode);
	 

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_backends_iosrobovm_IOSGLES10_glGenTextures__ILjava_nio_IntBuffer_2(JNIEnv* env, jobject object, jint n, jobject obj_textures) {
	int* textures = (int*)(obj_textures?env->GetDirectBufferAddress(obj_textures):0);


//@line:145

		// FIXME
	 

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_backends_iosrobovm_IOSGLES10_glGetError(JNIEnv* env, jobject object) {


//@line:150

		return glGetError();
	 

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_backends_iosrobovm_IOSGLES10_glGetIntegerv__ILjava_nio_IntBuffer_2(JNIEnv* env, jobject object, jint pname, jobject obj_params) {
	int* params = (int*)(obj_params?env->GetDirectBufferAddress(obj_params):0);


//@line:155

		// FIXME
	 

}

JNIEXPORT jstring JNICALL Java_com_badlogic_gdx_backends_iosrobovm_IOSGLES10_glGetString(JNIEnv* env, jobject object, jint name) {


//@line:160

		// FIXME
	 

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_backends_iosrobovm_IOSGLES10_glHint(JNIEnv* env, jobject object, jint target, jint mode) {


//@line:165

		glHint(target, mode);
	 

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_backends_iosrobovm_IOSGLES10_glLineWidth(JNIEnv* env, jobject object, jfloat width) {


//@line:170

		glLineWidth(width);
	 

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_backends_iosrobovm_IOSGLES10_glPixelStorei(JNIEnv* env, jobject object, jint pname, jint param) {


//@line:175

		glPixelStorei(pname, param);
 

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_backends_iosrobovm_IOSGLES10_glPolygonOffset(JNIEnv* env, jobject object, jfloat factor, jfloat units) {


//@line:180

		glPolygonOffset(factor, units);
 

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_backends_iosrobovm_IOSGLES10_glReadPixels(JNIEnv* env, jobject object, jint x, jint y, jint width, jint height, jint format, jint type, jobject obj_pixels) {
	unsigned char* pixels = (unsigned char*)(obj_pixels?env->GetDirectBufferAddress(obj_pixels):0);


//@line:185

	// FIXME
 

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_backends_iosrobovm_IOSGLES10_glScissor(JNIEnv* env, jobject object, jint x, jint y, jint width, jint height) {


//@line:190

		glScissor(x, y, width, height);
 

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_backends_iosrobovm_IOSGLES10_glStencilFunc(JNIEnv* env, jobject object, jint func, jint ref, jint mask) {


//@line:195

		glStencilFunc(func, ref, mask);
 

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_backends_iosrobovm_IOSGLES10_glStencilMask(JNIEnv* env, jobject object, jint mask) {


//@line:200

		glStencilMask(mask);
 

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_backends_iosrobovm_IOSGLES10_glStencilOp(JNIEnv* env, jobject object, jint fail, jint zfail, jint zpass) {


//@line:205

		glStencilOp(fail, zfail, zpass);
 

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_backends_iosrobovm_IOSGLES10_glTexImage2D(JNIEnv* env, jobject object, jint target, jint level, jint internalformat, jint width, jint height, jint border, jint format, jint type, jobject obj_pixels) {
	unsigned char* pixels = (unsigned char*)(obj_pixels?env->GetDirectBufferAddress(obj_pixels):0);


//@line:211

	// FIXME
 

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_backends_iosrobovm_IOSGLES10_glTexParameterf(JNIEnv* env, jobject object, jint target, jint pname, jfloat param) {


//@line:216

		glTexParameterf(target, pname, param);
 

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_backends_iosrobovm_IOSGLES10_glTexSubImage2D(JNIEnv* env, jobject object, jint target, jint level, jint xoffset, jint yoffset, jint width, jint height, jint format, jint type, jobject obj_pixels) {
	unsigned char* pixels = (unsigned char*)(obj_pixels?env->GetDirectBufferAddress(obj_pixels):0);


//@line:222

	// FIXME
 

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_backends_iosrobovm_IOSGLES10_glViewport(JNIEnv* env, jobject object, jint x, jint y, jint width, jint height) {


//@line:227

		glViewport(x, y, width, height);
 

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_backends_iosrobovm_IOSGLES10_glClipPlanef__I_3FI(JNIEnv* env, jobject object, jint plane, jfloatArray obj_equation, jint offset) {
	float* equation = (float*)env->GetPrimitiveArrayCritical(obj_equation, 0);


//@line:232

	// FIXME
 
	env->ReleasePrimitiveArrayCritical(obj_equation, equation, 0);

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_backends_iosrobovm_IOSGLES10_glClipPlanef__ILjava_nio_FloatBuffer_2(JNIEnv* env, jobject object, jint plane, jobject obj_equation) {
	float* equation = (float*)(obj_equation?env->GetDirectBufferAddress(obj_equation):0);


//@line:237

	// FIXME
 

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_backends_iosrobovm_IOSGLES10_glGetClipPlanef__I_3FI(JNIEnv* env, jobject object, jint pname, jfloatArray obj_eqn, jint offset) {
	float* eqn = (float*)env->GetPrimitiveArrayCritical(obj_eqn, 0);


//@line:242

	// FIXME
 
	env->ReleasePrimitiveArrayCritical(obj_eqn, eqn, 0);

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_backends_iosrobovm_IOSGLES10_glGetClipPlanef__ILjava_nio_FloatBuffer_2(JNIEnv* env, jobject object, jint pname, jobject obj_eqn) {
	float* eqn = (float*)(obj_eqn?env->GetDirectBufferAddress(obj_eqn):0);


//@line:247

	// FIXME
 

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_backends_iosrobovm_IOSGLES10_glGetFloatv__I_3FI(JNIEnv* env, jobject object, jint pname, jfloatArray obj_params, jint offset) {
	float* params = (float*)env->GetPrimitiveArrayCritical(obj_params, 0);


//@line:252

	// FIXME
 
	env->ReleasePrimitiveArrayCritical(obj_params, params, 0);

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_backends_iosrobovm_IOSGLES10_glGetFloatv__ILjava_nio_FloatBuffer_2(JNIEnv* env, jobject object, jint pname, jobject obj_params) {
	float* params = (float*)(obj_params?env->GetDirectBufferAddress(obj_params):0);


//@line:257

	// FIXME
 

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_backends_iosrobovm_IOSGLES10_glGetLightfv__II_3FI(JNIEnv* env, jobject object, jint light, jint pname, jfloatArray obj_params, jint offset) {
	float* params = (float*)env->GetPrimitiveArrayCritical(obj_params, 0);


//@line:262

	// FIXME
 
	env->ReleasePrimitiveArrayCritical(obj_params, params, 0);

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_backends_iosrobovm_IOSGLES10_glGetLightfv__IILjava_nio_FloatBuffer_2(JNIEnv* env, jobject object, jint light, jint pname, jobject obj_params) {
	float* params = (float*)(obj_params?env->GetDirectBufferAddress(obj_params):0);


//@line:267

	// FIXME
 

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_backends_iosrobovm_IOSGLES10_glGetMaterialfv__II_3FI(JNIEnv* env, jobject object, jint face, jint pname, jfloatArray obj_params, jint offset) {
	float* params = (float*)env->GetPrimitiveArrayCritical(obj_params, 0);


//@line:272

	// FIXME
 
	env->ReleasePrimitiveArrayCritical(obj_params, params, 0);

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_backends_iosrobovm_IOSGLES10_glGetMaterialfv__IILjava_nio_FloatBuffer_2(JNIEnv* env, jobject object, jint face, jint pname, jobject obj_params) {
	float* params = (float*)(obj_params?env->GetDirectBufferAddress(obj_params):0);


//@line:277

	// FIXME
 

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_backends_iosrobovm_IOSGLES10_glGetTexParameterfv__II_3FI(JNIEnv* env, jobject object, jint target, jint pname, jfloatArray obj_params, jint offset) {
	float* params = (float*)env->GetPrimitiveArrayCritical(obj_params, 0);


//@line:282

	// FIXME
 
	env->ReleasePrimitiveArrayCritical(obj_params, params, 0);

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_backends_iosrobovm_IOSGLES10_glGetTexParameterfv__IILjava_nio_FloatBuffer_2(JNIEnv* env, jobject object, jint target, jint pname, jobject obj_params) {
	float* params = (float*)(obj_params?env->GetDirectBufferAddress(obj_params):0);


//@line:287

	// FIXME
 

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_backends_iosrobovm_IOSGLES10_glPointParameterf(JNIEnv* env, jobject object, jint pname, jfloat param) {


//@line:292

	// FIXME
 

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_backends_iosrobovm_IOSGLES10_glPointParameterfv__I_3FI(JNIEnv* env, jobject object, jint pname, jfloatArray obj_params, jint offset) {
	float* params = (float*)env->GetPrimitiveArrayCritical(obj_params, 0);


//@line:297

	// FIXME
 
	env->ReleasePrimitiveArrayCritical(obj_params, params, 0);

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_backends_iosrobovm_IOSGLES10_glPointParameterfv__ILjava_nio_FloatBuffer_2(JNIEnv* env, jobject object, jint pname, jobject obj_params) {
	float* params = (float*)(obj_params?env->GetDirectBufferAddress(obj_params):0);


//@line:302

	// FIXME
 

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_backends_iosrobovm_IOSGLES10_glTexParameterfv__II_3FI(JNIEnv* env, jobject object, jint target, jint pname, jfloatArray obj_params, jint offset) {
	float* params = (float*)env->GetPrimitiveArrayCritical(obj_params, 0);


//@line:307

	// FIXME
 
	env->ReleasePrimitiveArrayCritical(obj_params, params, 0);

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_backends_iosrobovm_IOSGLES10_glTexParameterfv__IILjava_nio_FloatBuffer_2(JNIEnv* env, jobject object, jint target, jint pname, jobject obj_params) {
	float* params = (float*)(obj_params?env->GetDirectBufferAddress(obj_params):0);


//@line:312

	// FIXME
 

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_backends_iosrobovm_IOSGLES10_glBindBuffer(JNIEnv* env, jobject object, jint target, jint buffer) {


//@line:317

	// FIXME
 

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_backends_iosrobovm_IOSGLES10_glBufferData(JNIEnv* env, jobject object, jint target, jint size, jobject obj_data, jint usage) {
	unsigned char* data = (unsigned char*)(obj_data?env->GetDirectBufferAddress(obj_data):0);


//@line:322

	// FIXME
 

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_backends_iosrobovm_IOSGLES10_glBufferSubData(JNIEnv* env, jobject object, jint target, jint offset, jint size, jobject obj_data) {
	unsigned char* data = (unsigned char*)(obj_data?env->GetDirectBufferAddress(obj_data):0);


//@line:327

	// FIXME
 

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_backends_iosrobovm_IOSGLES10_glColor4ub(JNIEnv* env, jobject object, jbyte red, jbyte green, jbyte blue, jbyte alpha) {


//@line:332

	// FIXME
 

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_backends_iosrobovm_IOSGLES10_glDeleteBuffers__I_3II(JNIEnv* env, jobject object, jint n, jintArray obj_buffers, jint offset) {
	int* buffers = (int*)env->GetPrimitiveArrayCritical(obj_buffers, 0);


//@line:337

	// FIXME
 
	env->ReleasePrimitiveArrayCritical(obj_buffers, buffers, 0);

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_backends_iosrobovm_IOSGLES10_glDeleteBuffers__ILjava_nio_IntBuffer_2(JNIEnv* env, jobject object, jint n, jobject obj_buffers) {
	int* buffers = (int*)(obj_buffers?env->GetDirectBufferAddress(obj_buffers):0);


//@line:342

	// FIXME
 

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_backends_iosrobovm_IOSGLES10_glGetBooleanv__I_3ZI(JNIEnv* env, jobject object, jint pname, jbooleanArray obj_params, jint offset) {
	bool* params = (bool*)env->GetPrimitiveArrayCritical(obj_params, 0);


//@line:347

	// FIXME
 
	env->ReleasePrimitiveArrayCritical(obj_params, params, 0);

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_backends_iosrobovm_IOSGLES10_glGetBooleanv__ILjava_nio_IntBuffer_2(JNIEnv* env, jobject object, jint pname, jobject obj_params) {
	int* params = (int*)(obj_params?env->GetDirectBufferAddress(obj_params):0);


//@line:352

	// FIXME
 

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_backends_iosrobovm_IOSGLES10_glGetBufferParameteriv__II_3II(JNIEnv* env, jobject object, jint target, jint pname, jintArray obj_params, jint offset) {
	int* params = (int*)env->GetPrimitiveArrayCritical(obj_params, 0);


//@line:357

	// FIXME
 
	env->ReleasePrimitiveArrayCritical(obj_params, params, 0);

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_backends_iosrobovm_IOSGLES10_glGetBufferParameteriv__IILjava_nio_IntBuffer_2(JNIEnv* env, jobject object, jint target, jint pname, jobject obj_params) {
	int* params = (int*)(obj_params?env->GetDirectBufferAddress(obj_params):0);


//@line:362

	// FIXME
 

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_backends_iosrobovm_IOSGLES10_glGenBuffers__I_3II(JNIEnv* env, jobject object, jint n, jintArray obj_buffers, jint offset) {
	int* buffers = (int*)env->GetPrimitiveArrayCritical(obj_buffers, 0);


//@line:367

	// FIXME
 
	env->ReleasePrimitiveArrayCritical(obj_buffers, buffers, 0);

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_backends_iosrobovm_IOSGLES10_glGenBuffers__ILjava_nio_IntBuffer_2(JNIEnv* env, jobject object, jint n, jobject obj_buffers) {
	int* buffers = (int*)(obj_buffers?env->GetDirectBufferAddress(obj_buffers):0);


//@line:372

	// FIXME
 

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_backends_iosrobovm_IOSGLES10_glGetTexEnviv__II_3II(JNIEnv* env, jobject object, jint envi, jint pname, jintArray obj_params, jint offset) {
	int* params = (int*)env->GetPrimitiveArrayCritical(obj_params, 0);


//@line:382

	// FIXME
 
	env->ReleasePrimitiveArrayCritical(obj_params, params, 0);

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_backends_iosrobovm_IOSGLES10_glGetTexEnviv__IILjava_nio_IntBuffer_2(JNIEnv* env, jobject object, jint envi, jint pname, jobject obj_params) {
	int* params = (int*)(obj_params?env->GetDirectBufferAddress(obj_params):0);


//@line:387

	// FIXME
 

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_backends_iosrobovm_IOSGLES10_glGetTexParameteriv__II_3II(JNIEnv* env, jobject object, jint target, jint pname, jintArray obj_params, jint offset) {
	int* params = (int*)env->GetPrimitiveArrayCritical(obj_params, 0);


//@line:392

	// FIXME
 
	env->ReleasePrimitiveArrayCritical(obj_params, params, 0);

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_backends_iosrobovm_IOSGLES10_glGetTexParameteriv__IILjava_nio_IntBuffer_2(JNIEnv* env, jobject object, jint target, jint pname, jobject obj_params) {
	int* params = (int*)(obj_params?env->GetDirectBufferAddress(obj_params):0);


//@line:397

	// FIXME
 

}

JNIEXPORT jboolean JNICALL Java_com_badlogic_gdx_backends_iosrobovm_IOSGLES10_glIsBuffer(JNIEnv* env, jobject object, jint buffer) {


//@line:402

		return glIsBuffer(buffer);
 

}

JNIEXPORT jboolean JNICALL Java_com_badlogic_gdx_backends_iosrobovm_IOSGLES10_glIsEnabled(JNIEnv* env, jobject object, jint cap) {


//@line:407

		return glIsEnabled(cap);
 

}

JNIEXPORT jboolean JNICALL Java_com_badlogic_gdx_backends_iosrobovm_IOSGLES10_glIsTexture(JNIEnv* env, jobject object, jint texture) {


//@line:412

		glIsTexture(texture);
 

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_backends_iosrobovm_IOSGLES10_glTexEnvi(JNIEnv* env, jobject object, jint target, jint pname, jint param) {


//@line:417

		glTexEnvi(target, pname, param);
 

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_backends_iosrobovm_IOSGLES10_glTexEnviv__II_3II(JNIEnv* env, jobject object, jint target, jint pname, jintArray obj_params, jint offset) {
	int* params = (int*)env->GetPrimitiveArrayCritical(obj_params, 0);


//@line:422

	// FIXME
 
	env->ReleasePrimitiveArrayCritical(obj_params, params, 0);

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_backends_iosrobovm_IOSGLES10_glTexEnviv__IILjava_nio_IntBuffer_2(JNIEnv* env, jobject object, jint target, jint pname, jobject obj_params) {
	int* params = (int*)(obj_params?env->GetDirectBufferAddress(obj_params):0);


//@line:427

	// FIXME
 

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_backends_iosrobovm_IOSGLES10_glTexParameteri(JNIEnv* env, jobject object, jint target, jint pname, jint param) {


//@line:432

		glTexParameteri(target, pname, param);
 

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_backends_iosrobovm_IOSGLES10_glTexParameteriv__II_3II(JNIEnv* env, jobject object, jint target, jint pname, jintArray obj_params, jint offset) {
	int* params = (int*)env->GetPrimitiveArrayCritical(obj_params, 0);


//@line:437

	// FIXME
 
	env->ReleasePrimitiveArrayCritical(obj_params, params, 0);

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_backends_iosrobovm_IOSGLES10_glTexParameteriv__IILjava_nio_IntBuffer_2(JNIEnv* env, jobject object, jint target, jint pname, jobject obj_params) {
	int* params = (int*)(obj_params?env->GetDirectBufferAddress(obj_params):0);


//@line:442

	// FIXME
 

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_backends_iosrobovm_IOSGLES10_glPointSizePointerOES(JNIEnv* env, jobject object, jint type, jint stride, jobject obj_pointer) {
	unsigned char* pointer = (unsigned char*)(obj_pointer?env->GetDirectBufferAddress(obj_pointer):0);


//@line:447

	// FIXME
 

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_backends_iosrobovm_IOSGLES10_glVertexPointer__IIII(JNIEnv* env, jobject object, jint size, jint type, jint stride, jint pointer) {


//@line:452

		glVertexPointer(size, type, stride, (void*)pointer);
 

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_backends_iosrobovm_IOSGLES10_glColorPointer__IIII(JNIEnv* env, jobject object, jint size, jint type, jint stride, jint pointer) {


//@line:457

		glColorPointer(size, type, stride, (void*)pointer);
 

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_backends_iosrobovm_IOSGLES10_glNormalPointer__III(JNIEnv* env, jobject object, jint type, jint stride, jint pointer) {


//@line:462

		glNormalPointer(type, stride, (void*)pointer);
 

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_backends_iosrobovm_IOSGLES10_glTexCoordPointer__IIII(JNIEnv* env, jobject object, jint size, jint type, jint stride, jint pointer) {


//@line:467

		glTexCoordPointer(size, type, stride, (void*)pointer);
 

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_backends_iosrobovm_IOSGLES10_glDrawElements__IIII(JNIEnv* env, jobject object, jint mode, jint count, jint type, jint indices) {


//@line:472

		glDrawElements(mode, count, type, (void*)indices);
 

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_backends_iosrobovm_IOSGLES10_glAlphaFunc(JNIEnv* env, jobject object, jint func, jfloat ref) {


//@line:477

		glAlphaFunc(func, ref);
 

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_backends_iosrobovm_IOSGLES10_glClientActiveTexture(JNIEnv* env, jobject object, jint texture) {


//@line:482

		glClientActiveTexture(texture);
 

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_backends_iosrobovm_IOSGLES10_glColor4f(JNIEnv* env, jobject object, jfloat red, jfloat green, jfloat blue, jfloat alpha) {


//@line:487

		glColor4f(red, green, blue, alpha);
 

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_backends_iosrobovm_IOSGLES10_glColorPointer__IIILjava_nio_Buffer_2(JNIEnv* env, jobject object, jint size, jint type, jint stride, jobject obj_pointer) {
	unsigned char* pointer = (unsigned char*)(obj_pointer?env->GetDirectBufferAddress(obj_pointer):0);


//@line:492

		glColorPointer(size, type, stride, pointer);
 

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_backends_iosrobovm_IOSGLES10_glDeleteTextures__I_3II(JNIEnv* env, jobject object, jint n, jintArray obj_textures, jint offset) {
	int* textures = (int*)env->GetPrimitiveArrayCritical(obj_textures, 0);


//@line:497

	// FIXME
 
	env->ReleasePrimitiveArrayCritical(obj_textures, textures, 0);

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_backends_iosrobovm_IOSGLES10_glDisableClientState(JNIEnv* env, jobject object, jint array) {


//@line:502

		glDisableClientState(array);
 

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_backends_iosrobovm_IOSGLES10_glEnableClientState(JNIEnv* env, jobject object, jint array) {


//@line:507

		glEnableClientState(array);
 

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_backends_iosrobovm_IOSGLES10_glFogf(JNIEnv* env, jobject object, jint pname, jfloat param) {


//@line:512

		glFogf(pname, param);
 

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_backends_iosrobovm_IOSGLES10_glFogfv__I_3FI(JNIEnv* env, jobject object, jint pname, jfloatArray obj_params, jint offset) {
	float* params = (float*)env->GetPrimitiveArrayCritical(obj_params, 0);


//@line:517

	// FIXME
 
	env->ReleasePrimitiveArrayCritical(obj_params, params, 0);

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_backends_iosrobovm_IOSGLES10_glFogfv__ILjava_nio_FloatBuffer_2(JNIEnv* env, jobject object, jint pname, jobject obj_params) {
	float* params = (float*)(obj_params?env->GetDirectBufferAddress(obj_params):0);


//@line:522

	// FIXME
 

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_backends_iosrobovm_IOSGLES10_glFrustumf(JNIEnv* env, jobject object, jfloat left, jfloat right, jfloat bottom, jfloat top, jfloat zNear, jfloat zFar) {


//@line:527

		glFrustumf(left, right, bottom, top, zNear, zFar);
 

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_backends_iosrobovm_IOSGLES10_glGenTextures__I_3II(JNIEnv* env, jobject object, jint n, jintArray obj_textures, jint offset) {
	int* textures = (int*)env->GetPrimitiveArrayCritical(obj_textures, 0);


//@line:532

	// FIXME
 
	env->ReleasePrimitiveArrayCritical(obj_textures, textures, 0);

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_backends_iosrobovm_IOSGLES10_glGetIntegerv__I_3II(JNIEnv* env, jobject object, jint pname, jintArray obj_params, jint offset) {
	int* params = (int*)env->GetPrimitiveArrayCritical(obj_params, 0);


//@line:537

	// FIXME
 
	env->ReleasePrimitiveArrayCritical(obj_params, params, 0);

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_backends_iosrobovm_IOSGLES10_glLightModelf(JNIEnv* env, jobject object, jint pname, jfloat param) {


//@line:542

		glLightModelf(pname, param);
 

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_backends_iosrobovm_IOSGLES10_glLightModelfv__I_3FI(JNIEnv* env, jobject object, jint pname, jfloatArray obj_params, jint offset) {
	float* params = (float*)env->GetPrimitiveArrayCritical(obj_params, 0);


//@line:547

	// FIXME
 
	env->ReleasePrimitiveArrayCritical(obj_params, params, 0);

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_backends_iosrobovm_IOSGLES10_glLightModelfv__ILjava_nio_FloatBuffer_2(JNIEnv* env, jobject object, jint pname, jobject obj_params) {
	float* params = (float*)(obj_params?env->GetDirectBufferAddress(obj_params):0);


//@line:552

	// FIXME
 

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_backends_iosrobovm_IOSGLES10_glLightf(JNIEnv* env, jobject object, jint light, jint pname, jfloat param) {


//@line:557

		glLightf(light, pname, param);
 

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_backends_iosrobovm_IOSGLES10_glLightfv__II_3FI(JNIEnv* env, jobject object, jint light, jint pname, jfloatArray obj_params, jint offset) {
	float* params = (float*)env->GetPrimitiveArrayCritical(obj_params, 0);


//@line:562

	// FIXME
 
	env->ReleasePrimitiveArrayCritical(obj_params, params, 0);

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_backends_iosrobovm_IOSGLES10_glLightfv__IILjava_nio_FloatBuffer_2(JNIEnv* env, jobject object, jint light, jint pname, jobject obj_params) {
	float* params = (float*)(obj_params?env->GetDirectBufferAddress(obj_params):0);


//@line:567

	// FIXME
 

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_backends_iosrobovm_IOSGLES10_glLoadIdentity(JNIEnv* env, jobject object) {


//@line:572

		glLoadIdentity();
 

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_backends_iosrobovm_IOSGLES10_glLoadMatrixf___3FI(JNIEnv* env, jobject object, jfloatArray obj_m, jint offset) {
	float* m = (float*)env->GetPrimitiveArrayCritical(obj_m, 0);


//@line:577

	// FIXME
 
	env->ReleasePrimitiveArrayCritical(obj_m, m, 0);

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_backends_iosrobovm_IOSGLES10_glLoadMatrixf__Ljava_nio_FloatBuffer_2(JNIEnv* env, jobject object, jobject obj_m) {
	float* m = (float*)(obj_m?env->GetDirectBufferAddress(obj_m):0);


//@line:582

	// FIXME
 

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_backends_iosrobovm_IOSGLES10_glLogicOp(JNIEnv* env, jobject object, jint opcode) {


//@line:587

		glLogicOp(opcode);
 

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_backends_iosrobovm_IOSGLES10_glMaterialf(JNIEnv* env, jobject object, jint face, jint pname, jfloat param) {


//@line:592

		glMaterialf(face, pname, param);
 

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_backends_iosrobovm_IOSGLES10_glMaterialfv__II_3FI(JNIEnv* env, jobject object, jint face, jint pname, jfloatArray obj_params, jint offset) {
	float* params = (float*)env->GetPrimitiveArrayCritical(obj_params, 0);


//@line:597

	// FIXME
 
	env->ReleasePrimitiveArrayCritical(obj_params, params, 0);

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_backends_iosrobovm_IOSGLES10_glMaterialfv__IILjava_nio_FloatBuffer_2(JNIEnv* env, jobject object, jint face, jint pname, jobject obj_params) {
	float* params = (float*)(obj_params?env->GetDirectBufferAddress(obj_params):0);


//@line:602

	// FIXME
 

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_backends_iosrobovm_IOSGLES10_glMatrixMode(JNIEnv* env, jobject object, jint mode) {


//@line:607

		glMatrixMode(mode);
 

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_backends_iosrobovm_IOSGLES10_glMultMatrixf___3FI(JNIEnv* env, jobject object, jfloatArray obj_m, jint offset) {
	float* m = (float*)env->GetPrimitiveArrayCritical(obj_m, 0);


//@line:612

	// FIXME
 
	env->ReleasePrimitiveArrayCritical(obj_m, m, 0);

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_backends_iosrobovm_IOSGLES10_glMultMatrixf__Ljava_nio_FloatBuffer_2(JNIEnv* env, jobject object, jobject obj_m) {
	float* m = (float*)(obj_m?env->GetDirectBufferAddress(obj_m):0);


//@line:617

	// FIXME
 

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_backends_iosrobovm_IOSGLES10_glMultiTexCoord4f(JNIEnv* env, jobject object, jint target, jfloat s, jfloat t, jfloat r, jfloat q) {


//@line:622

		glMultiTexCoord4f(target, s, t, r, q);
 

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_backends_iosrobovm_IOSGLES10_glNormal3f(JNIEnv* env, jobject object, jfloat nx, jfloat ny, jfloat nz) {


//@line:627

		glNormal3f(nx, ny, nz);
 

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_backends_iosrobovm_IOSGLES10_glNormalPointer__IILjava_nio_Buffer_2(JNIEnv* env, jobject object, jint type, jint stride, jobject obj_pointer) {
	unsigned char* pointer = (unsigned char*)(obj_pointer?env->GetDirectBufferAddress(obj_pointer):0);


//@line:632

	// FIXME
 

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_backends_iosrobovm_IOSGLES10_glOrthof(JNIEnv* env, jobject object, jfloat left, jfloat right, jfloat bottom, jfloat top, jfloat zNear, jfloat zFar) {


//@line:637

		glOrthof(left, right, bottom, top, zNear, zFar);
 

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_backends_iosrobovm_IOSGLES10_glPointSize(JNIEnv* env, jobject object, jfloat size) {


//@line:642

		glPointSize(size);
 

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_backends_iosrobovm_IOSGLES10_glPopMatrix(JNIEnv* env, jobject object) {


//@line:647

		glPopMatrix();
 

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_backends_iosrobovm_IOSGLES10_glPushMatrix(JNIEnv* env, jobject object) {


//@line:652

		glPushMatrix();
 

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_backends_iosrobovm_IOSGLES10_glRotatef(JNIEnv* env, jobject object, jfloat angle, jfloat x, jfloat y, jfloat z) {


//@line:657

		glRotatef(angle, x, y, z);
 

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_backends_iosrobovm_IOSGLES10_glSampleCoverage(JNIEnv* env, jobject object, jfloat value, jboolean invert) {


//@line:662

	// FIXME
 

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_backends_iosrobovm_IOSGLES10_glScalef(JNIEnv* env, jobject object, jfloat x, jfloat y, jfloat z) {


//@line:667

		glScalef(x, y, z);
 

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_backends_iosrobovm_IOSGLES10_glShadeModel(JNIEnv* env, jobject object, jint mode) {


//@line:672

		glShadeModel(mode);
 

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_backends_iosrobovm_IOSGLES10_glTexCoordPointer__IIILjava_nio_Buffer_2(JNIEnv* env, jobject object, jint size, jint type, jint stride, jobject obj_pointer) {
	unsigned char* pointer = (unsigned char*)(obj_pointer?env->GetDirectBufferAddress(obj_pointer):0);


//@line:677

	// FIXME
 

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_backends_iosrobovm_IOSGLES10_glTexEnvf(JNIEnv* env, jobject object, jint target, jint pname, jfloat param) {


//@line:682

		glTexEnvf(target, pname, param);
 

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_backends_iosrobovm_IOSGLES10_glTexEnvfv__II_3FI(JNIEnv* env, jobject object, jint target, jint pname, jfloatArray obj_params, jint offset) {
	float* params = (float*)env->GetPrimitiveArrayCritical(obj_params, 0);


//@line:687

	// FIXME
 
	env->ReleasePrimitiveArrayCritical(obj_params, params, 0);

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_backends_iosrobovm_IOSGLES10_glTexEnvfv__IILjava_nio_FloatBuffer_2(JNIEnv* env, jobject object, jint target, jint pname, jobject obj_params) {
	float* params = (float*)(obj_params?env->GetDirectBufferAddress(obj_params):0);


//@line:692

	// FIXME
 

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_backends_iosrobovm_IOSGLES10_glTranslatef(JNIEnv* env, jobject object, jfloat x, jfloat y, jfloat z) {


//@line:697

		glTranslatef(x, y, z);
 

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_backends_iosrobovm_IOSGLES10_glVertexPointer__IIILjava_nio_Buffer_2(JNIEnv* env, jobject object, jint size, jint type, jint stride, jobject obj_pointer) {
	unsigned char* pointer = (unsigned char*)(obj_pointer?env->GetDirectBufferAddress(obj_pointer):0);


//@line:702

	// FIXME
 

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_backends_iosrobovm_IOSGLES10_glPolygonMode(JNIEnv* env, jobject object, jint face, jint mode) {


//@line:707
		
 

}


//@line:710

	#endif
	#endif
	