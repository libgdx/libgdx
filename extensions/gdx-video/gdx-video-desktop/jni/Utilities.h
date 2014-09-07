#pragma once

#include <jni.h>
#include <sys/time.h>

/**
 * @brief Some small utility functions used mostly for debugging.
 * 
 * @author Rob Bogie <rob.bogie@codepoke.net>
 */

#ifdef WIN32
typedef unsigned char u_int8_t;
#endif

extern bool debugLoggingActive;

void logDebug(const char* format, ...);

void logError(const char* format, ...);

void debug(bool debug);

/**
 * @brief 	A small function to get the field named nativePointer from any given jni object.
 * 			This is used heavily when using java classes that manage instances of c++ classes.
 */
template<class T>
T* getClassPointer(JNIEnv* env, jobject object) {
	// Get class type
	jclass cls = env->GetObjectClass(object);
	//Get pointer field
	jfieldID fieldId = env->GetFieldID(cls, "nativePointer", "J");

	if (fieldId == 0) {
		logError("Could not get nativePointer");
		return 0;
	}

	return reinterpret_cast<T*>(env->GetLongField(object, fieldId));
}

typedef long long msec_t;
msec_t currentTimeMillis();
