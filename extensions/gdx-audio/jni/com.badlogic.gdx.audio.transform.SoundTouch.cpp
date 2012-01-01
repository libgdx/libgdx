#include <com.badlogic.gdx.audio.transform.SoundTouch.h>

//@line:69

	#include "SoundTouch.h"
	using namespace soundtouch;
	 JNIEXPORT jlong JNICALL Java_com_badlogic_gdx_audio_transform_SoundTouch_newSoundTouchJni(JNIEnv* env, jobject object) {


//@line:81

		return (jlong)(new SoundTouch());
	

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_audio_transform_SoundTouch_disposeJni(JNIEnv* env, jobject object, jlong addr) {


//@line:90

		delete (SoundTouch*)addr;
	

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_audio_transform_SoundTouch_setRateJni(JNIEnv* env, jobject object, jlong addr, jfloat newRate) {


//@line:101

    	((SoundTouch*)addr)->setRate(newRate);
    

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_audio_transform_SoundTouch_setTempoJni(JNIEnv* env, jobject object, jlong addr, jfloat newTempo) {


//@line:112

    	((SoundTouch*)addr)->setTempo(newTempo);
    

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_audio_transform_SoundTouch_setRateChangeJni(JNIEnv* env, jobject object, jlong addr, jfloat newRate) {


//@line:123

    	((SoundTouch*)addr)->setRateChange(newRate);
    

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_audio_transform_SoundTouch_setTempoChange(JNIEnv* env, jobject object, jlong addr, jfloat newTempo) {


//@line:134

    	((SoundTouch*)addr)->setTempoChange(newTempo);
    

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_audio_transform_SoundTouch_setPitchJni(JNIEnv* env, jobject object, jlong addr, jfloat newPitch) {


//@line:145

		((SoundTouch*)addr)->setPitch(newPitch);
    

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_audio_transform_SoundTouch_setPitchOctavesJni(JNIEnv* env, jobject object, jlong addr, jfloat newPitch) {


//@line:155

    	((SoundTouch*)addr)->setPitchOctaves(newPitch);
    

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_audio_transform_SoundTouch_setPitchSemiTonesJni__JI(JNIEnv* env, jobject object, jlong addr, jint newPitch) {


//@line:166

    	((SoundTouch*)addr)->setPitchSemiTones((int)newPitch);
    

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_audio_transform_SoundTouch_setPitchSemiTonesJni__JF(JNIEnv* env, jobject object, jlong addr, jfloat newPitch) {


//@line:177

    	((SoundTouch*)addr)->setPitchSemiTones((float)newPitch);
    

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_audio_transform_SoundTouch_setChannelsJni(JNIEnv* env, jobject object, jlong addr, jint numChannels) {


//@line:186

    	((SoundTouch*)addr)->setChannels(numChannels);
    

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_audio_transform_SoundTouch_setSampleRateJni(JNIEnv* env, jobject object, jlong addr, jint srate) {


//@line:195

    	((SoundTouch*)addr)->setSampleRate(srate);
    

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_audio_transform_SoundTouch_flushJni(JNIEnv* env, jobject object, jlong addr) {


//@line:211

    	((SoundTouch*)addr)->flush();
    

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_audio_transform_SoundTouch_putSamplesJni(JNIEnv* env, jobject object, jlong addr, jshortArray obj_samples, jint offset, jint numSamples) {
	short* samples = (short*)env->GetPrimitiveArrayCritical(obj_samples, 0);


//@line:225

    	((SoundTouch*)addr)->putSamples((const SAMPLETYPE *)samples + offset, numSamples);
	
	env->ReleasePrimitiveArrayCritical(obj_samples, samples, 0);

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_audio_transform_SoundTouch_clearJni(JNIEnv* env, jobject object, jlong addr) {


//@line:236

    	((SoundTouch*)addr)->clear();
    

}

JNIEXPORT jboolean JNICALL Java_com_badlogic_gdx_audio_transform_SoundTouch_setSettingJni(JNIEnv* env, jobject object, jlong addr, jint settingId, jint value) {


//@line:249

    	return (jboolean)((SoundTouch*)addr)->setSetting(settingId, value);
    

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_audio_transform_SoundTouch_getSettingJni(JNIEnv* env, jobject object, jlong addr, jint settingId) {


//@line:262

    	return ((SoundTouch*)addr)->getSetting(settingId);
    

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_audio_transform_SoundTouch_numUnprocessedSamplesJni(JNIEnv* env, jobject object, jlong addr) {


//@line:271

		return ((SoundTouch*)addr)->numUnprocessedSamples();
	

}

