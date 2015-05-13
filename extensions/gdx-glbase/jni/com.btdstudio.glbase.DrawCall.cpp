#include <com.btdstudio.glbase.DrawCall.h>

//@line:10

	 	#include "drawCall.h"
	    #include "IpolygonMap.h"
	    #include "IanimationPlayer.h"
	    #include "matrix.h"
	    #include "types.h"
	    #include "renderEnums.h"
    JNIEXPORT jlong JNICALL Java_com_btdstudio_glbase_DrawCall_getTransformRef(JNIEnv* env, jclass clazz, jlong handle) {


//@line:208

		DrawCall* dc = (DrawCall*)handle;
		return (long long)&dc->modelTransform;
	

}

JNIEXPORT void JNICALL Java_com_btdstudio_glbase_DrawCall_set__JJ(JNIEnv* env, jclass clazz, jlong handle, jlong pmHandle) {


//@line:213

		((DrawCall*)handle)->set(((IPolygonMap*)pmHandle));
	

}

JNIEXPORT void JNICALL Java_com_btdstudio_glbase_DrawCall_set__JI_3F(JNIEnv* env, jclass clazz, jlong handle, jint mode, jfloatArray obj_color) {
	float* color = (float*)env->GetPrimitiveArrayCritical(obj_color, 0);


//@line:217

		((DrawCall*)handle)->set(((RenderEnums::ClearMode)mode), color);
	
	env->ReleasePrimitiveArrayCritical(obj_color, color, 0);

}

JNIEXPORT void JNICALL Java_com_btdstudio_glbase_DrawCall_set__J_3F(JNIEnv* env, jclass clazz, jlong handle, jfloatArray obj_bbox) {
	float* bbox = (float*)env->GetPrimitiveArrayCritical(obj_bbox, 0);


//@line:221

		((DrawCall*)handle)->set(bbox);
	
	env->ReleasePrimitiveArrayCritical(obj_bbox, bbox, 0);

}

JNIEXPORT void JNICALL Java_com_btdstudio_glbase_DrawCall_set__JI(JNIEnv* env, jclass clazz, jlong handle, jint numParticles) {


//@line:225

		((DrawCall*)handle)->set(numParticles);
	

}

JNIEXPORT void JNICALL Java_com_btdstudio_glbase_DrawCall_setFullScreen(JNIEnv* env, jclass clazz, jlong handle) {


//@line:229

		((DrawCall*)handle)->setFullScreen();
	

}

JNIEXPORT void JNICALL Java_com_btdstudio_glbase_DrawCall_setCopy(JNIEnv* env, jclass clazz, jlong handle, jlong copy) {


//@line:233

		((DrawCall*)handle)->set(((DrawCall*)copy));
	

}

JNIEXPORT jlong JNICALL Java_com_btdstudio_glbase_DrawCall_getRenderTargetPolygonMap(JNIEnv* env, jclass clazz, jlong handle) {


//@line:237

		DrawCall* dc = (DrawCall*)handle;
		return (long long)dc->renderTarget.polygonMap;
	

}

JNIEXPORT jfloat JNICALL Java_com_btdstudio_glbase_DrawCall_getRenderTargetBox(JNIEnv* env, jclass clazz, jlong handle, jint pos) {


//@line:242

		DrawCall* dc = (DrawCall*)handle;
		return dc->renderTarget.box[pos];
	

}

JNIEXPORT jint JNICALL Java_com_btdstudio_glbase_DrawCall_getRenderTargetNumParticles(JNIEnv* env, jclass clazz, jlong handle) {


//@line:247

		DrawCall* dc = (DrawCall*)handle;
		return dc->renderTarget.numParticles;
	

}

JNIEXPORT jint JNICALL Java_com_btdstudio_glbase_DrawCall_getRenderTargetClearMode(JNIEnv* env, jclass clazz, jlong handle) {


//@line:252

		DrawCall* dc = (DrawCall*)handle;
		return (int)dc->renderTarget.clear.mode;
	

}

JNIEXPORT jfloat JNICALL Java_com_btdstudio_glbase_DrawCall_getRenderTargetClearColor(JNIEnv* env, jclass clazz, jlong handle, jint pos) {


//@line:257

		DrawCall* dc = (DrawCall*)handle;
		return dc->renderTarget.clear.color[pos];
	

}

JNIEXPORT jint JNICALL Java_com_btdstudio_glbase_DrawCall_getShader(JNIEnv* env, jclass clazz, jlong handle) {


//@line:262

		return ((DrawCall*)handle)->shader;
	

}

JNIEXPORT void JNICALL Java_com_btdstudio_glbase_DrawCall_setShader(JNIEnv* env, jclass clazz, jlong handle, jint shader) {


//@line:266

		((DrawCall*)handle)->shader = shader;
	

}

JNIEXPORT jint JNICALL Java_com_btdstudio_glbase_DrawCall_getTexture(JNIEnv* env, jclass clazz, jlong handle) {


//@line:270

		return ((DrawCall*)handle)->texture;
	

}

JNIEXPORT void JNICALL Java_com_btdstudio_glbase_DrawCall_setTexture(JNIEnv* env, jclass clazz, jlong handle, jint texture) {


//@line:274

		((DrawCall*)handle)->texture = texture;
	

}

JNIEXPORT jint JNICALL Java_com_btdstudio_glbase_DrawCall_getFramebuffer(JNIEnv* env, jclass clazz, jlong handle) {


//@line:278

		return ((DrawCall*)handle)->framebuffer;
	

}

JNIEXPORT void JNICALL Java_com_btdstudio_glbase_DrawCall_setFramebuffer(JNIEnv* env, jclass clazz, jlong handle, jint framebuffer) {


//@line:282

		((DrawCall*)handle)->framebuffer = framebuffer;
	

}

JNIEXPORT jlong JNICALL Java_com_btdstudio_glbase_DrawCall_getAnimationPlayer(JNIEnv* env, jclass clazz, jlong handle) {


//@line:286

		return (long long)((DrawCall*)handle)->animationPlayer;
	

}

JNIEXPORT void JNICALL Java_com_btdstudio_glbase_DrawCall_setAnimationPlayer(JNIEnv* env, jclass clazz, jlong handle, jlong playerHandle) {


//@line:290

		((DrawCall*)handle)->animationPlayer = (IAnimationPlayer*)playerHandle;
	

}

JNIEXPORT jint JNICALL Java_com_btdstudio_glbase_DrawCall_getMyUniformsSize(JNIEnv* env, jclass clazz, jlong handle) {


//@line:294

		return ((DrawCall*)handle)->myUniforms.size();
	

}

JNIEXPORT jlong JNICALL Java_com_btdstudio_glbase_DrawCall_getMyUniform(JNIEnv* env, jclass clazz, jlong handle, jint id) {


//@line:298

		if( ((DrawCall*)handle)->myUniforms.count(id) == 0 ) return 0L;
	 	return (long long)((DrawCall*)handle)->myUniforms[id];
	

}

JNIEXPORT void JNICALL Java_com_btdstudio_glbase_DrawCall_addMyUniform(JNIEnv* env, jclass clazz, jlong handle, jint id, jlong uniformHandle) {


//@line:303

		((DrawCall*)handle)->myUniforms[id] = (MyUniformValue*)uniformHandle;
	

}

JNIEXPORT void JNICALL Java_com_btdstudio_glbase_DrawCall_removeMyUniform(JNIEnv* env, jclass clazz, jlong handle, jint id) {


//@line:307

		((DrawCall*)handle)->myUniforms.erase(id);
	

}

JNIEXPORT jboolean JNICALL Java_com_btdstudio_glbase_DrawCall_getBlendSrcAlpha(JNIEnv* env, jclass clazz, jlong handle) {


//@line:311

		return ((DrawCall*)handle)->blendSrcAlpha;
	

}

JNIEXPORT void JNICALL Java_com_btdstudio_glbase_DrawCall_setBlendSrcAlpha(JNIEnv* env, jclass clazz, jlong handle, jboolean value) {


//@line:315

		((DrawCall*)handle)->blendSrcAlpha = value;;
	

}

JNIEXPORT jint JNICALL Java_com_btdstudio_glbase_DrawCall_getBlendMode(JNIEnv* env, jclass clazz, jlong handle) {


//@line:319

		return (int)((DrawCall*)handle)->blendMode;
	

}

JNIEXPORT void JNICALL Java_com_btdstudio_glbase_DrawCall_setBlendMode(JNIEnv* env, jclass clazz, jlong handle, jint mode) {


//@line:323

		((DrawCall*)handle)->blendMode = (RenderEnums::BlendMode)mode;
	

}

JNIEXPORT jint JNICALL Java_com_btdstudio_glbase_DrawCall_getCullingMode(JNIEnv* env, jclass clazz, jlong handle) {


//@line:327

		return (int)((DrawCall*)handle)->cullingMode;
	

}

JNIEXPORT void JNICALL Java_com_btdstudio_glbase_DrawCall_setCullingMode(JNIEnv* env, jclass clazz, jlong handle, jint mode) {


//@line:331

		((DrawCall*)handle)->cullingMode = (RenderEnums::CullingMode)mode;
	

}

JNIEXPORT jboolean JNICALL Java_com_btdstudio_glbase_DrawCall_getUseDepthTest(JNIEnv* env, jclass clazz, jlong handle) {


//@line:335

		return ((DrawCall*)handle)->useDepthTest;
	

}

JNIEXPORT void JNICALL Java_com_btdstudio_glbase_DrawCall_setUseDepthTest(JNIEnv* env, jclass clazz, jlong handle, jboolean value) {


//@line:339

		((DrawCall*)handle)->useDepthTest = value;
	

}

JNIEXPORT jint JNICALL Java_com_btdstudio_glbase_DrawCall_getDepthFunc(JNIEnv* env, jclass clazz, jlong handle) {


//@line:343

		return (int)((DrawCall*)handle)->depthFunc;
	

}

JNIEXPORT void JNICALL Java_com_btdstudio_glbase_DrawCall_setDepthFunc(JNIEnv* env, jclass clazz, jlong handle, jint mode) {


//@line:347

		((DrawCall*)handle)->depthFunc = (RenderEnums::DepthFunc)mode;
	

}

JNIEXPORT jboolean JNICALL Java_com_btdstudio_glbase_DrawCall_getDepthMask(JNIEnv* env, jclass clazz, jlong handle) {


//@line:351

		return ((DrawCall*)handle)->depthMask;
	

}

JNIEXPORT void JNICALL Java_com_btdstudio_glbase_DrawCall_setDepthMask(JNIEnv* env, jclass clazz, jlong handle, jboolean value) {


//@line:355

		((DrawCall*)handle)->depthMask = value;
	

}

JNIEXPORT jboolean JNICALL Java_com_btdstudio_glbase_DrawCall_getColorMask(JNIEnv* env, jclass clazz, jlong handle, jint pos) {


//@line:359

		return ((DrawCall*)handle)->colorMask[pos];
	

}

JNIEXPORT void JNICALL Java_com_btdstudio_glbase_DrawCall_setColorMask(JNIEnv* env, jclass clazz, jlong handle, jint pos, jboolean value) {


//@line:363

		((DrawCall*)handle)->colorMask[pos] = value;
	

}

