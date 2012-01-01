#include <com.badlogic.gdx.audio.transform.SoundTouch.h>

//@line:70

	#include "SoundTouch.h"
	using namespace soundtouch;
	 JNIEXPORT jlong JNICALL Java_com_badlogic_gdx_audio_transform_SoundTouch_newSoundTouchJni(JNIEnv* env, jobject object) {


//@line:83

		return (jlong)(new SoundTouch());
	

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_audio_transform_SoundTouch_disposeJni(JNIEnv* env, jobject object, jlong addr) {


//@line:92

		delete (SoundTouch*)addr;
	

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_audio_transform_SoundTouch_setRateJni(JNIEnv* env, jobject object, jlong addr, jfloat newRate) {


//@line:103

    	((SoundTouch*)addr)->setRate(newRate);
    

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_audio_transform_SoundTouch_setTempoJni(JNIEnv* env, jobject object, jlong addr, jfloat newTempo) {


//@line:114

    	((SoundTouch*)addr)->setTempo(newTempo);
    

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_audio_transform_SoundTouch_setRateChangeJni(JNIEnv* env, jobject object, jlong addr, jfloat newRate) {


//@line:125

    	((SoundTouch*)addr)->setRateChange(newRate);
    

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_audio_transform_SoundTouch_setTempoChange(JNIEnv* env, jobject object, jlong addr, jfloat newTempo) {


//@line:136

    	((SoundTouch*)addr)->setTempoChange(newTempo);
    

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_audio_transform_SoundTouch_setPitchJni(JNIEnv* env, jobject object, jlong addr, jfloat newPitch) {


//@line:147

		((SoundTouch*)addr)->setPitch(newPitch);
    

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_audio_transform_SoundTouch_setPitchOctavesJni(JNIEnv* env, jobject object, jlong addr, jfloat newPitch) {


//@line:157

    	((SoundTouch*)addr)->setPitchOctaves(newPitch);
    

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_audio_transform_SoundTouch_setPitchSemiTonesJni__JI(JNIEnv* env, jobject object, jlong addr, jint newPitch) {


//@line:168

    	((SoundTouch*)addr)->setPitchSemiTones((int)newPitch);
    

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_audio_transform_SoundTouch_setPitchSemiTonesJni__JF(JNIEnv* env, jobject object, jlong addr, jfloat newPitch) {


//@line:179

    	((SoundTouch*)addr)->setPitchSemiTones((float)newPitch);
    

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_audio_transform_SoundTouch_setChannelsJni(JNIEnv* env, jobject object, jlong addr, jint numChannels) {


//@line:188

    	((SoundTouch*)addr)->setChannels(numChannels);
    

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_audio_transform_SoundTouch_setSampleRateJni(JNIEnv* env, jobject object, jlong addr, jint srate) {


//@line:197

    	((SoundTouch*)addr)->setSampleRate(srate);
    

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_audio_transform_SoundTouch_flushJni(JNIEnv* env, jobject object, jlong addr) {


//@line:213

    	((SoundTouch*)addr)->flush();
    

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_audio_transform_SoundTouch_putSamplesJni(JNIEnv* env, jobject object, jlong addr, jshortArray obj_samples, jint offset, jint numSamples) {
	short* samples = (short*)env->GetPrimitiveArrayCritical(obj_samples, 0);


//@line:227

    	((SoundTouch*)addr)->putSamples((const SAMPLETYPE *)samples + offset, numSamples);
	
	env->ReleasePrimitiveArrayCritical(obj_samples, samples, 0);

}

JNIEXPORT void JNICALL Java_com_badlogic_gdx_audio_transform_SoundTouch_clearJni(JNIEnv* env, jobject object, jlong addr) {


//@line:238

    	((SoundTouch*)addr)->clear();
    

}

JNIEXPORT jboolean JNICALL Java_com_badlogic_gdx_audio_transform_SoundTouch_setSettingJni(JNIEnv* env, jobject object, jlong addr, jint settingId, jint value) {


//@line:251

    	return (jboolean)((SoundTouch*)addr)->setSetting(settingId, value);
    

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_audio_transform_SoundTouch_getSettingJni(JNIEnv* env, jobject object, jlong addr, jint settingId) {


//@line:264

    	return ((SoundTouch*)addr)->getSetting(settingId);
    

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_audio_transform_SoundTouch_numUnprocessedSamplesJni(JNIEnv* env, jobject object, jlong addr) {


//@line:273

		return ((SoundTouch*)addr)->numUnprocessedSamples();
	

}

JNIEXPORT jint JNICALL Java_com_badlogic_gdx_audio_transform_SoundTouch_numSamplesJni(JNIEnv* env, jobject object, jlong addr) {


//@line:282

		return ((SoundTouch*)addr)->numSamples();
	

}

