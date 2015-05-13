#include <com.btdstudio.glbase.IAnimationPlayer.h>

//@line:11

	    #include "IanimationPlayer.h"
	    #include <time.h>
    JNIEXPORT void JNICALL Java_com_btdstudio_glbase_IAnimationPlayer_play(JNIEnv* env, jclass clazz, jlong handle, jint playMode, jint addStartTimeMS) {


//@line:73

		((IAnimationPlayer*)handle)->play((IAnimationPlayer::PlayMode)playMode, addStartTimeMS);
	

}

JNIEXPORT void JNICALL Java_com_btdstudio_glbase_IAnimationPlayer_playSync(JNIEnv* env, jclass clazz, jlong handle, jint playMode, jlong parentHandle) {


//@line:77

		IAnimationPlayer* parent = (IAnimationPlayer*)parentHandle;
		((IAnimationPlayer*)handle)->playSync((IAnimationPlayer::PlayMode)playMode, parent);
	

}

JNIEXPORT void JNICALL Java_com_btdstudio_glbase_IAnimationPlayer_stopSync(JNIEnv* env, jclass clazz, jlong handle, jlong parentHandle) {


//@line:82

		IAnimationPlayer* parent = (IAnimationPlayer*)parentHandle;
		((IAnimationPlayer*)handle)->stopSync(parent);
	

}

JNIEXPORT void JNICALL Java_com_btdstudio_glbase_IAnimationPlayer_replaySync(JNIEnv* env, jclass clazz, jlong handle, jlong parentHandle) {


//@line:87

		IAnimationPlayer* parent = (IAnimationPlayer*)parentHandle;
		((IAnimationPlayer*)handle)->replaySync(parent);
	

}

JNIEXPORT void JNICALL Java_com_btdstudio_glbase_IAnimationPlayer_replay(JNIEnv* env, jclass clazz, jlong handle) {


//@line:92

		((IAnimationPlayer*)handle)->replay();
	

}

JNIEXPORT void JNICALL Java_com_btdstudio_glbase_IAnimationPlayer_stop(JNIEnv* env, jclass clazz, jlong handle) {


//@line:96

		((IAnimationPlayer*)handle)->stop();
	

}

JNIEXPORT void JNICALL Java_com_btdstudio_glbase_IAnimationPlayer_rewind(JNIEnv* env, jclass clazz, jlong handle) {


//@line:100

		((IAnimationPlayer*)handle)->rewind();
	

}

JNIEXPORT void JNICALL Java_com_btdstudio_glbase_IAnimationPlayer_unsetAnimation(JNIEnv* env, jclass clazz, jlong handle) {


//@line:104

		((IAnimationPlayer*)handle)->unsetAnimation();
	

}

JNIEXPORT jboolean JNICALL Java_com_btdstudio_glbase_IAnimationPlayer_isPlaying(JNIEnv* env, jclass clazz, jlong handle) {


//@line:108

		return ((IAnimationPlayer*)handle)->isPlaying();
	

}

JNIEXPORT void JNICALL Java_com_btdstudio_glbase_IAnimationPlayer_update(JNIEnv* env, jclass clazz, jlong handle) {


//@line:112

		struct timeval timev;
		gettimeofday( &timev, NULL );
		((IAnimationPlayer*)handle)->update(timev);
	

}

JNIEXPORT void JNICALL Java_com_btdstudio_glbase_IAnimationPlayer_dispose(JNIEnv* env, jclass clazz, jlong handle) {


//@line:118

		delete (IAnimationPlayer*)handle;
	

}

