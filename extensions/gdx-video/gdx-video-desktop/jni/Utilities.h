/*******************************************************************************
 * Copyright 2014 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/


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
