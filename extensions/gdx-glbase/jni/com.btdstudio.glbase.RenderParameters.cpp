#include <com.btdstudio.glbase.RenderParameters.h>

//@line:8

	    #include "Iobject.h"
	JNIEXPORT jlong JNICALL Java_com_btdstudio_glbase_RenderParameters_createRenderParameters(JNIEnv* env, jclass clazz) {


//@line:87

		RenderParameters* params = new RenderParameters();
		return (long long)params;
	

}

JNIEXPORT void JNICALL Java_com_btdstudio_glbase_RenderParameters_disposeRenderParameters(JNIEnv* env, jobject object, jlong handle) {


//@line:92

		RenderParameters* params = static_cast<RenderParameters*>((void*)handle);
		delete params;
	

}

JNIEXPORT void JNICALL Java_com_btdstudio_glbase_RenderParameters_setShader(JNIEnv* env, jobject object, jlong handle, jint shader) {


//@line:97

		RenderParameters* params = static_cast<RenderParameters*>((void*)handle);
		params->shader = shader;
	

}

JNIEXPORT jint JNICALL Java_com_btdstudio_glbase_RenderParameters_getShader(JNIEnv* env, jobject object, jlong handle) {


//@line:102

		RenderParameters* params = static_cast<RenderParameters*>((void*)handle);
		return params->shader;
	

}

JNIEXPORT void JNICALL Java_com_btdstudio_glbase_RenderParameters_setBlendSrcAlpha(JNIEnv* env, jobject object, jlong handle, jboolean value) {


//@line:107

		RenderParameters* params = static_cast<RenderParameters*>((void*)handle);
		params->blendSrcAlpha = value;
	

}

JNIEXPORT jboolean JNICALL Java_com_btdstudio_glbase_RenderParameters_getBlendSrcAlpha(JNIEnv* env, jobject object, jlong handle) {


//@line:112

		RenderParameters* params = static_cast<RenderParameters*>((void*)handle);
		return params->blendSrcAlpha;
	

}

JNIEXPORT void JNICALL Java_com_btdstudio_glbase_RenderParameters_setBlendMode(JNIEnv* env, jobject object, jlong handle, jint blendMode) {


//@line:117

		RenderParameters* params = static_cast<RenderParameters*>((void*)handle);
		params->blendMode = (RenderEnums::BlendMode)blendMode;
	

}

JNIEXPORT jint JNICALL Java_com_btdstudio_glbase_RenderParameters_getBlendMode(JNIEnv* env, jobject object, jlong handle) {


//@line:122

		RenderParameters* params = static_cast<RenderParameters*>((void*)handle);
		return (int)params->blendMode;
	

}

JNIEXPORT void JNICALL Java_com_btdstudio_glbase_RenderParameters_setCullingMode(JNIEnv* env, jobject object, jlong handle, jint cullingMode) {


//@line:127

		RenderParameters* params = static_cast<RenderParameters*>((void*)handle);
		params->cullingMode = (RenderEnums::CullingMode)cullingMode;
	

}

JNIEXPORT jint JNICALL Java_com_btdstudio_glbase_RenderParameters_getCullingMode(JNIEnv* env, jobject object, jlong handle) {


//@line:132

		RenderParameters* params = static_cast<RenderParameters*>((void*)handle);
		return (int)params->cullingMode;
	

}

JNIEXPORT void JNICALL Java_com_btdstudio_glbase_RenderParameters_setUseDepthTest(JNIEnv* env, jobject object, jlong handle, jboolean value) {


//@line:137

		RenderParameters* params = static_cast<RenderParameters*>((void*)handle);
		params->useDepthTest = value;
	

}

JNIEXPORT jboolean JNICALL Java_com_btdstudio_glbase_RenderParameters_getUseDepthTest(JNIEnv* env, jobject object, jlong handle) {


//@line:142

		RenderParameters* params = static_cast<RenderParameters*>((void*)handle);
		return params->useDepthTest;
	

}

JNIEXPORT void JNICALL Java_com_btdstudio_glbase_RenderParameters_setDepthFunc(JNIEnv* env, jobject object, jlong handle, jint depthFunc) {


//@line:147

		RenderParameters* params = static_cast<RenderParameters*>((void*)handle);
		params->depthFunc = (RenderEnums::DepthFunc)depthFunc;
	

}

JNIEXPORT jint JNICALL Java_com_btdstudio_glbase_RenderParameters_getDepthFunc(JNIEnv* env, jobject object, jlong handle) {


//@line:152

		RenderParameters* params = static_cast<RenderParameters*>((void*)handle);
		return (int)params->depthFunc;
	

}

JNIEXPORT void JNICALL Java_com_btdstudio_glbase_RenderParameters_setDepthMask(JNIEnv* env, jobject object, jlong handle, jboolean value) {


//@line:157

		RenderParameters* params = static_cast<RenderParameters*>((void*)handle);
		params->depthMask = value;
	

}

JNIEXPORT jboolean JNICALL Java_com_btdstudio_glbase_RenderParameters_getDepthMask(JNIEnv* env, jobject object, jlong handle) {


//@line:162

		RenderParameters* params = static_cast<RenderParameters*>((void*)handle);
		return params->depthMask;
	

}

JNIEXPORT void JNICALL Java_com_btdstudio_glbase_RenderParameters_setColorMask(JNIEnv* env, jobject object, jlong handle, jint pos, jboolean value) {


//@line:167

		RenderParameters* params = static_cast<RenderParameters*>((void*)handle);
		params->colorMask[pos] = value;
	

}

JNIEXPORT jboolean JNICALL Java_com_btdstudio_glbase_RenderParameters_getColorMask(JNIEnv* env, jobject object, jlong handle, jint pos) {


//@line:172

		RenderParameters* params = static_cast<RenderParameters*>((void*)handle);
		return params->colorMask[pos];
	

}

