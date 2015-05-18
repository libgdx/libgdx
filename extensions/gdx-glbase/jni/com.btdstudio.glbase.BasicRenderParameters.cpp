#include <com.btdstudio.glbase.BasicRenderParameters.h>

//@line:5

	    #include "Imrf.h"
	    #include "IanimationPlayer.h"
    JNIEXPORT jlong JNICALL Java_com_btdstudio_glbase_BasicRenderParameters_createBasicRenderParameters(JNIEnv* env, jclass clazz) {


//@line:65

		BasicRenderParameters* params = new BasicRenderParameters();
		return (long long)params;
	

}

JNIEXPORT jlong JNICALL Java_com_btdstudio_glbase_BasicRenderParameters_getMatrixRef(JNIEnv* env, jobject object, jlong handle) {


//@line:70

		BasicRenderParameters* params = static_cast<BasicRenderParameters*>((void*)handle);
		return (long long)&params->modelTransform;
	

}

JNIEXPORT void JNICALL Java_com_btdstudio_glbase_BasicRenderParameters_disposeBasicRenderParameters(JNIEnv* env, jobject object, jlong handle) {


//@line:75

		BasicRenderParameters* params = static_cast<BasicRenderParameters*>((void*)handle);
		delete params;
	

}

JNIEXPORT void JNICALL Java_com_btdstudio_glbase_BasicRenderParameters_setTexture(JNIEnv* env, jobject object, jlong handle, jint texture) {


//@line:80

		BasicRenderParameters* params = static_cast<BasicRenderParameters*>((void*)handle);
		params->texture = texture;
	

}

JNIEXPORT jint JNICALL Java_com_btdstudio_glbase_BasicRenderParameters_getTexture(JNIEnv* env, jobject object, jlong handle) {


//@line:85

		BasicRenderParameters* params = static_cast<BasicRenderParameters*>((void*)handle);
		return params->texture;
	

}

JNIEXPORT void JNICALL Java_com_btdstudio_glbase_BasicRenderParameters_setFramebuffer(JNIEnv* env, jobject object, jlong handle, jint framebuffer) {


//@line:90

		BasicRenderParameters* params = static_cast<BasicRenderParameters*>((void*)handle);
		params->framebuffer = framebuffer;
	

}

JNIEXPORT jint JNICALL Java_com_btdstudio_glbase_BasicRenderParameters_getFramebuffer(JNIEnv* env, jobject object, jlong handle) {


//@line:95

		BasicRenderParameters* params = static_cast<BasicRenderParameters*>((void*)handle);
		return params->framebuffer;
	

}

JNIEXPORT void JNICALL Java_com_btdstudio_glbase_BasicRenderParameters_setAnimationPlayer(JNIEnv* env, jobject object, jlong handle, jlong playerHandle) {


//@line:100

		BasicRenderParameters* params = static_cast<BasicRenderParameters*>((void*)handle);
		IAnimationPlayer* player = (IAnimationPlayer*)playerHandle;
		
		params->animationPlayer = player;
	

}

JNIEXPORT jlong JNICALL Java_com_btdstudio_glbase_BasicRenderParameters_getAnimationPlayer(JNIEnv* env, jobject object, jlong handle) {


//@line:107

		BasicRenderParameters* params = static_cast<BasicRenderParameters*>((void*)handle);
		return (long long)&params->animationPlayer;
	

}

