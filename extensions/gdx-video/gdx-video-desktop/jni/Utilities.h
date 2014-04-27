#pragma once

#include <jni.h>
#include <sys/time.h>

#ifdef WIN32
typedef unsigned char u_int8_t;
#endif

extern bool debugLoggingActive;

void logDebug(const char* format, ...);

void logError(const char* format, ...);

void debug(bool debug);

template<class T>
T* getClassPointer(JNIEnv* env, jobject object) {
	// Get class type
	jclass cls = env->GetObjectClass(object);
	//Get pointer field (defined in SteamObject)
	jfieldID fieldId = env->GetFieldID(cls, "nativePointer", "J");

	if (fieldId == 0) {
		logError("Could not get nativePointer");
		return 0;
	}

	return reinterpret_cast<T*>(env->GetLongField(object, fieldId));
}

typedef long long msec_t;
msec_t currentTimeMillis();
