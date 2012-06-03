/*
  Copyright (C) 2004, 2005 Jeroen Frijters

  This software is provided 'as-is', without any express or implied
  warranty.  In no event will the authors be held liable for any damages
  arising from the use of this software.

  Permission is granted to anyone to use this software for any purpose,
  including commercial applications, and to alter it and redistribute it
  freely, subject to the following restrictions:

  1. The origin of this software must not be misrepresented; you must not
     claim that you wrote the original software. If you use this software
     in a product, an acknowledgment in the product documentation would be
     appreciated but is not required.
  2. Altered source versions must be plainly marked as such, and must not be
     misrepresented as being the original software.
  3. This notice may not be removed or altered from any source distribution.

  Jeroen Frijters
  jeroen@frijters.net
  
*/
#ifndef __JNI_H__
#define __JNI_H__

#ifdef _WIN32
	#define JNICALL __stdcall
	#ifdef __cplusplus
		#define JNIEXPORT extern "C" __declspec(dllexport)
	#else
		#define JNIEXPORT __declspec(dllexport)
	#endif
#else
	#define JNICALL
	#ifdef __cplusplus
		#define JNIEXPORT extern "C"
	#else
		#define JNIEXPORT
	#endif
	#include <stdarg.h>
#endif

typedef void* jobject;
typedef void* jstring;
typedef void* jweak;
typedef void* jthrowable;
typedef void* jclass;
typedef void* jmethodID;
typedef void* jfieldID;
typedef void* jarray;
typedef void* jobjectArray;
typedef void* jbooleanArray;
typedef void* jbyteArray;
typedef void* jcharArray;
typedef void* jshortArray;
typedef void* jintArray;
typedef void* jlongArray;
typedef void* jfloatArray;
typedef void* jdoubleArray;

typedef unsigned char jboolean;
typedef signed char jbyte;
typedef unsigned short jchar;
typedef short jshort;
typedef int jint;
#ifdef _MSC_VER
	typedef __int64 jlong;
#else
	typedef long long int jlong;
#endif
typedef float jfloat;
typedef double jdouble;
typedef jint jsize;

typedef struct
{
	char *name;
	char *signature;
	void *fnPtr;
} JNINativeMethod;

typedef union jvalue
{
    jboolean z;
    jbyte    b;
    jchar    c;
    jshort   s;
    jint     i;
    jlong    j;
    jfloat   f;
    jdouble  d;
    jobject  l;
} jvalue;

typedef void* JavaVM;
typedef struct JNIEnv_methods *JNIEnv;

struct JNIEnv_methods
{
	jint (JNICALL *GetMethodArgs)(JNIEnv* pEnv, jmethodID method, jbyte* sig);
	void (JNICALL *reserved1)(JNIEnv* pEnv);
	void (JNICALL *reserved2)(JNIEnv* pEnv);
	void (JNICALL *reserved3)(JNIEnv* pEnv);

	jint (JNICALL *GetVersion)(JNIEnv* pEnv);

	jclass (JNICALL *DefineClass)(JNIEnv* pEnv, const char *name, jobject loader, const jbyte *buf, jsize len);
	jclass (JNICALL *FindClass)(JNIEnv* pEnv, const char *name);

	jmethodID (JNICALL *FromReflectedMethod)(JNIEnv* pEnv, jobject method);
	jfieldID (JNICALL *FromReflectedField)(JNIEnv* pEnv, jobject field);
	jobject (JNICALL *ToReflectedMethod)(JNIEnv* pEnv, jclass clazz, jmethodID methodID, jboolean isStatic);

	jclass (JNICALL *GetSuperclass)(JNIEnv* pEnv, jclass sub);
	jboolean (JNICALL *IsAssignableFrom)(JNIEnv* pEnv, jclass sub, jclass sup);

	jobject (JNICALL *ToReflectedField)(JNIEnv* pEnv, jclass clazz, jfieldID fieldID, jboolean isStatic);

	jint (JNICALL *Throw)(JNIEnv* pEnv, jthrowable obj);
	jint (JNICALL *ThrowNew)(JNIEnv* pEnv, jclass clazz, const char *msg);
	jthrowable (JNICALL *ExceptionOccurred)(JNIEnv* pEnv);
	void (JNICALL *ExceptionDescribe)(JNIEnv* pEnv);
	void (JNICALL *ExceptionClear)(JNIEnv* pEnv);
	void (JNICALL *FatalError)(JNIEnv* pEnv, const char *msg);

	jint (JNICALL *PushLocalFrame)(JNIEnv* pEnv, jint capacity); 
	jobject (JNICALL *PopLocalFrame)(JNIEnv* pEnv, jobject result);

	jobject (JNICALL *NewGlobalRef)(JNIEnv* pEnv, jobject lobj);
	void (JNICALL *DeleteGlobalRef)(JNIEnv* pEnv, jobject gref);
	void (JNICALL *DeleteLocalRef)(JNIEnv* pEnv, jobject obj);
	jboolean (JNICALL *IsSameObject)(JNIEnv* pEnv, jobject obj1, jobject obj2);

	jobject (JNICALL *NewLocalRef)(JNIEnv* pEnv, jobject ref);
	jint (JNICALL *EnsureLocalCapacity)(JNIEnv* pEnv, jint capacity); 

	jobject (JNICALL *AllocObject)(JNIEnv* pEnv, jclass clazz);
	jobject (JNICALL *NewObject)(JNIEnv* pEnv, jclass clazz, jmethodID methodID, ...);
	jobject (JNICALL *NewObjectV)(JNIEnv* pEnv, jclass clazz, jmethodID methodID, va_list args);
	jobject (JNICALL *NewObjectA)(JNIEnv* pEnv, jclass clazz, jmethodID methodID, jvalue *args);

	jclass (JNICALL *GetObjectClass)(JNIEnv* pEnv, jobject obj);
	jboolean (JNICALL *IsInstanceOf)(JNIEnv* pEnv, jobject obj, jclass clazz);

	jmethodID (JNICALL *GetMethodID)(JNIEnv* pEnv, jclass clazz, const char *name, const char *sig);

	jobject (JNICALL *CallObjectMethod)(JNIEnv* pEnv, jobject obj, jmethodID methodID, ...);
	jobject (JNICALL *CallObjectMethodV)(JNIEnv* pEnv, jobject obj, jmethodID methodID, va_list args);
	jobject (JNICALL *CallObjectMethodA)(JNIEnv* pEnv, jobject obj, jmethodID methodID, jvalue * args);

	jboolean (JNICALL *CallBooleanMethod)(JNIEnv* pEnv, jobject obj, jmethodID methodID, ...);
	jboolean (JNICALL *CallBooleanMethodV)(JNIEnv* pEnv, jobject obj, jmethodID methodID, va_list args);
	jboolean (JNICALL *CallBooleanMethodA)(JNIEnv* pEnv, jobject obj, jmethodID methodID, jvalue * args);

	jbyte (JNICALL *CallByteMethod)(JNIEnv* pEnv, jobject obj, jmethodID methodID, ...);
	jbyte (JNICALL *CallByteMethodV)(JNIEnv* pEnv, jobject obj, jmethodID methodID, va_list args);
	jbyte (JNICALL *CallByteMethodA)(JNIEnv* pEnv, jobject obj, jmethodID methodID, jvalue *args);

	jchar (JNICALL *CallCharMethod)(JNIEnv* pEnv, jobject obj, jmethodID methodID, ...);
	jchar (JNICALL *CallCharMethodV)(JNIEnv* pEnv, jobject obj, jmethodID methodID, va_list args);
	jchar (JNICALL *CallCharMethodA)(JNIEnv* pEnv, jobject obj, jmethodID methodID, jvalue *args);

	jshort (JNICALL *CallShortMethod)(JNIEnv* pEnv, jobject obj, jmethodID methodID, ...);
	jshort (JNICALL *CallShortMethodV)(JNIEnv* pEnv, jobject obj, jmethodID methodID, va_list args);
	jshort (JNICALL *CallShortMethodA)(JNIEnv* pEnv, jobject obj, jmethodID methodID, jvalue *args);

	jint (JNICALL *CallIntMethod)(JNIEnv* pEnv, jobject obj, jmethodID methodID, ...);
	jint (JNICALL *CallIntMethodV)(JNIEnv* pEnv, jobject obj, jmethodID methodID, va_list args);
	jint (JNICALL *CallIntMethodA)(JNIEnv* pEnv, jobject obj, jmethodID methodID, jvalue *args);

	jlong (JNICALL *CallLongMethod)(JNIEnv* pEnv, jobject obj, jmethodID methodID, ...);
	jlong (JNICALL *CallLongMethodV)(JNIEnv* pEnv, jobject obj, jmethodID methodID, va_list args);
	jlong (JNICALL *CallLongMethodA)(JNIEnv* pEnv, jobject obj, jmethodID methodID, jvalue *args);

	jfloat (JNICALL *CallFloatMethod)(JNIEnv* pEnv, jobject obj, jmethodID methodID, ...);
	jfloat (JNICALL *CallFloatMethodV)(JNIEnv* pEnv, jobject obj, jmethodID methodID, va_list args);
	jfloat (JNICALL *CallFloatMethodA)(JNIEnv* pEnv, jobject obj, jmethodID methodID, jvalue *args);

	jdouble (JNICALL *CallDoubleMethod)(JNIEnv* pEnv, jobject obj, jmethodID methodID, ...);
	jdouble (JNICALL *CallDoubleMethodV)(JNIEnv* pEnv, jobject obj, jmethodID methodID, va_list args);
	jdouble (JNICALL *CallDoubleMethodA)(JNIEnv* pEnv, jobject obj, jmethodID methodID, jvalue *args);

	void (JNICALL *CallVoidMethod)(JNIEnv* pEnv, jobject obj, jmethodID methodID, ...);
	void (JNICALL *CallVoidMethodV)(JNIEnv* pEnv, jobject obj, jmethodID methodID, va_list args);
	void (JNICALL *CallVoidMethodA)(JNIEnv* pEnv, jobject obj, jmethodID methodID, jvalue * args);

	jobject (JNICALL *CallNonvirtualObjectMethod)(JNIEnv* pEnv, jobject obj, jclass clazz, jmethodID methodID, ...);
	jobject (JNICALL *CallNonvirtualObjectMethodV)(JNIEnv* pEnv, jobject obj, jclass clazz, jmethodID methodID, va_list args);
	jobject (JNICALL *CallNonvirtualObjectMethodA)(JNIEnv* pEnv, jobject obj, jclass clazz, jmethodID methodID, jvalue * args);

	jboolean (JNICALL *CallNonvirtualBooleanMethod)(JNIEnv* pEnv, jobject obj, jclass clazz, jmethodID methodID, ...);
	jboolean (JNICALL *CallNonvirtualBooleanMethodV)(JNIEnv* pEnv, jobject obj, jclass clazz, jmethodID methodID, va_list args);
	jboolean (JNICALL *CallNonvirtualBooleanMethodA)(JNIEnv* pEnv, jobject obj, jclass clazz, jmethodID methodID, jvalue * args);

	jbyte (JNICALL *CallNonvirtualByteMethod)(JNIEnv* pEnv, jobject obj, jclass clazz, jmethodID methodID, ...);
	jbyte (JNICALL *CallNonvirtualByteMethodV)(JNIEnv* pEnv, jobject obj, jclass clazz, jmethodID methodID, va_list args);
	jbyte (JNICALL *CallNonvirtualByteMethodA)(JNIEnv* pEnv, jobject obj, jclass clazz, jmethodID methodID, jvalue *args);

	jchar (JNICALL *CallNonvirtualCharMethod)(JNIEnv* pEnv, jobject obj, jclass clazz, jmethodID methodID, ...);
	jchar (JNICALL *CallNonvirtualCharMethodV)(JNIEnv* pEnv, jobject obj, jclass clazz, jmethodID methodID, va_list args);
	jchar (JNICALL *CallNonvirtualCharMethodA)(JNIEnv* pEnv, jobject obj, jclass clazz, jmethodID methodID, jvalue *args);

	jshort (JNICALL *CallNonvirtualShortMethod)(JNIEnv* pEnv, jobject obj, jclass clazz, jmethodID methodID, ...);
	jshort (JNICALL *CallNonvirtualShortMethodV)(JNIEnv* pEnv, jobject obj, jclass clazz, jmethodID methodID, va_list args);
	jshort (JNICALL *CallNonvirtualShortMethodA)(JNIEnv* pEnv, jobject obj, jclass clazz, jmethodID methodID, jvalue *args);

	jint (JNICALL *CallNonvirtualIntMethod)(JNIEnv* pEnv, jobject obj, jclass clazz, jmethodID methodID, ...);
	jint (JNICALL *CallNonvirtualIntMethodV)(JNIEnv* pEnv, jobject obj, jclass clazz, jmethodID methodID, va_list args);
	jint (JNICALL *CallNonvirtualIntMethodA)(JNIEnv* pEnv, jobject obj, jclass clazz, jmethodID methodID, jvalue *args);

	jlong (JNICALL *CallNonvirtualLongMethod)(JNIEnv* pEnv, jobject obj, jclass clazz, jmethodID methodID, ...);
	jlong (JNICALL *CallNonvirtualLongMethodV)(JNIEnv* pEnv, jobject obj, jclass clazz, jmethodID methodID, va_list args);
	jlong (JNICALL *CallNonvirtualLongMethodA)(JNIEnv* pEnv, jobject obj, jclass clazz, jmethodID methodID, jvalue *args);

	jfloat (JNICALL *CallNonvirtualFloatMethod)(JNIEnv* pEnv, jobject obj, jclass clazz, jmethodID methodID, ...);
	jfloat (JNICALL *CallNonvirtualFloatMethodV)(JNIEnv* pEnv, jobject obj, jclass clazz, jmethodID methodID, va_list args);
	jfloat (JNICALL *CallNonvirtualFloatMethodA)(JNIEnv* pEnv, jobject obj, jclass clazz, jmethodID methodID, jvalue *args);

	jdouble (JNICALL *CallNonvirtualDoubleMethod)(JNIEnv* pEnv, jobject obj, jclass clazz, jmethodID methodID, ...);
	jdouble (JNICALL *CallNonvirtualDoubleMethodV)(JNIEnv* pEnv, jobject obj, jclass clazz, jmethodID methodID, va_list args);
	jdouble (JNICALL *CallNonvirtualDoubleMethodA)(JNIEnv* pEnv, jobject obj, jclass clazz, jmethodID methodID, jvalue *args);

	void (JNICALL *CallNonvirtualVoidMethod)(JNIEnv* pEnv, jobject obj, jclass clazz, jmethodID methodID, ...);
	void (JNICALL *CallNonvirtualVoidMethodV)(JNIEnv* pEnv, jobject obj, jclass clazz, jmethodID methodID, va_list args);
	void (JNICALL *CallNonvirtualVoidMethodA)(JNIEnv* pEnv, jobject obj, jclass clazz, jmethodID methodID, jvalue * args);

	jfieldID (JNICALL *GetFieldID)(JNIEnv* pEnv, jclass clazz, const char *name, const char *sig);

	jobject (JNICALL *GetObjectField)(JNIEnv* pEnv, jobject obj, jfieldID fieldID);
	jboolean (JNICALL *GetBooleanField)(JNIEnv* pEnv, jobject obj, jfieldID fieldID);
	jbyte (JNICALL *GetByteField)(JNIEnv* pEnv, jobject obj, jfieldID fieldID);
	jchar (JNICALL *GetCharField)(JNIEnv* pEnv, jobject obj, jfieldID fieldID);
	jshort (JNICALL *GetShortField)(JNIEnv* pEnv, jobject obj, jfieldID fieldID);
	jint (JNICALL *GetIntField)(JNIEnv* pEnv, jobject obj, jfieldID fieldID);
	jlong (JNICALL *GetLongField)(JNIEnv* pEnv, jobject obj, jfieldID fieldID);
	jfloat (JNICALL *GetFloatField)(JNIEnv* pEnv, jobject obj, jfieldID fieldID);
	jdouble (JNICALL *GetDoubleField)(JNIEnv* pEnv, jobject obj, jfieldID fieldID);

	void (JNICALL *SetObjectField)(JNIEnv* pEnv, jobject obj, jfieldID fieldID, jobject val);
	void (JNICALL *SetBooleanField)(JNIEnv* pEnv, jobject obj, jfieldID fieldID, jboolean val);
	void (JNICALL *SetByteField)(JNIEnv* pEnv, jobject obj, jfieldID fieldID, jbyte val);
	void (JNICALL *SetCharField)(JNIEnv* pEnv, jobject obj, jfieldID fieldID, jchar val);
	void (JNICALL *SetShortField)(JNIEnv* pEnv, jobject obj, jfieldID fieldID, jshort val);
	void (JNICALL *SetIntField)(JNIEnv* pEnv, jobject obj, jfieldID fieldID, jint val);
	void (JNICALL *SetLongField)(JNIEnv* pEnv, jobject obj, jfieldID fieldID, jlong val);
	void (JNICALL *SetFloatField)(JNIEnv* pEnv, jobject obj, jfieldID fieldID, jfloat val);
	void (JNICALL *SetDoubleField)(JNIEnv* pEnv, jobject obj, jfieldID fieldID, jdouble val);

	jmethodID (JNICALL *GetStaticMethodID)(JNIEnv* pEnv, jclass clazz, const char *name, const char *sig);

	jobject (JNICALL *CallStaticObjectMethod)(JNIEnv* pEnv, jclass clazz, jmethodID methodID, ...);
	jobject (JNICALL *CallStaticObjectMethodV)(JNIEnv* pEnv, jclass clazz, jmethodID methodID, va_list args);
	jobject (JNICALL *CallStaticObjectMethodA)(JNIEnv* pEnv, jclass clazz, jmethodID methodID, jvalue *args);

	jboolean (JNICALL *CallStaticBooleanMethod)(JNIEnv* pEnv, jclass clazz, jmethodID methodID, ...);
	jboolean (JNICALL *CallStaticBooleanMethodV)(JNIEnv* pEnv, jclass clazz, jmethodID methodID, va_list args);
	jboolean (JNICALL *CallStaticBooleanMethodA)(JNIEnv* pEnv, jclass clazz, jmethodID methodID, jvalue *args);

	jbyte (JNICALL *CallStaticByteMethod)(JNIEnv* pEnv, jclass clazz, jmethodID methodID, ...);
	jbyte (JNICALL *CallStaticByteMethodV)(JNIEnv* pEnv, jclass clazz, jmethodID methodID, va_list args);
	jbyte (JNICALL *CallStaticByteMethodA)(JNIEnv* pEnv, jclass clazz, jmethodID methodID, jvalue *args);

	jchar (JNICALL *CallStaticCharMethod)(JNIEnv* pEnv, jclass clazz, jmethodID methodID, ...);
	jchar (JNICALL *CallStaticCharMethodV)(JNIEnv* pEnv, jclass clazz, jmethodID methodID, va_list args);
	jchar (JNICALL *CallStaticCharMethodA)(JNIEnv* pEnv, jclass clazz, jmethodID methodID, jvalue *args);

	jshort (JNICALL *CallStaticShortMethod)(JNIEnv* pEnv, jclass clazz, jmethodID methodID, ...);
	jshort (JNICALL *CallStaticShortMethodV)(JNIEnv* pEnv, jclass clazz, jmethodID methodID, va_list args);
	jshort (JNICALL *CallStaticShortMethodA)(JNIEnv* pEnv, jclass clazz, jmethodID methodID, jvalue *args);

	jint (JNICALL *CallStaticIntMethod)(JNIEnv* pEnv, jclass clazz, jmethodID methodID, ...);
	jint (JNICALL *CallStaticIntMethodV)(JNIEnv* pEnv, jclass clazz, jmethodID methodID, va_list args);
	jint (JNICALL *CallStaticIntMethodA)(JNIEnv* pEnv, jclass clazz, jmethodID methodID, jvalue *args);

	jlong (JNICALL *CallStaticLongMethod)(JNIEnv* pEnv, jclass clazz, jmethodID methodID, ...);
	jlong (JNICALL *CallStaticLongMethodV)(JNIEnv* pEnv, jclass clazz, jmethodID methodID, va_list args);
	jlong (JNICALL *CallStaticLongMethodA)(JNIEnv* pEnv, jclass clazz, jmethodID methodID, jvalue *args);

	jfloat (JNICALL *CallStaticFloatMethod)(JNIEnv* pEnv, jclass clazz, jmethodID methodID, ...);
	jfloat (JNICALL *CallStaticFloatMethodV)(JNIEnv* pEnv, jclass clazz, jmethodID methodID, va_list args);
	jfloat (JNICALL *CallStaticFloatMethodA)(JNIEnv* pEnv, jclass clazz, jmethodID methodID, jvalue *args);

	jdouble (JNICALL *CallStaticDoubleMethod)(JNIEnv* pEnv, jclass clazz, jmethodID methodID, ...);
	jdouble (JNICALL *CallStaticDoubleMethodV)(JNIEnv* pEnv, jclass clazz, jmethodID methodID, va_list args);
	jdouble (JNICALL *CallStaticDoubleMethodA)(JNIEnv* pEnv, jclass clazz, jmethodID methodID, jvalue *args);

	void (JNICALL *CallStaticVoidMethod)(JNIEnv* pEnv, jclass cls, jmethodID methodID, ...);
	void (JNICALL *CallStaticVoidMethodV)(JNIEnv* pEnv, jclass cls, jmethodID methodID, va_list args);
	void (JNICALL *CallStaticVoidMethodA)(JNIEnv* pEnv, jclass cls, jmethodID methodID, jvalue * args);

	jfieldID (JNICALL *GetStaticFieldID)(JNIEnv* pEnv, jclass clazz, const char *name, const char *sig);
	jobject (JNICALL *GetStaticObjectField)(JNIEnv* pEnv, jclass clazz, jfieldID fieldID);
	jboolean (JNICALL *GetStaticBooleanField)(JNIEnv* pEnv, jclass clazz, jfieldID fieldID);
	jbyte (JNICALL *GetStaticByteField)(JNIEnv* pEnv, jclass clazz, jfieldID fieldID);
	jchar (JNICALL *GetStaticCharField)(JNIEnv* pEnv, jclass clazz, jfieldID fieldID);
	jshort (JNICALL *GetStaticShortField)(JNIEnv* pEnv, jclass clazz, jfieldID fieldID);
	jint (JNICALL *GetStaticIntField)(JNIEnv* pEnv, jclass clazz, jfieldID fieldID);
	jlong (JNICALL *GetStaticLongField)(JNIEnv* pEnv, jclass clazz, jfieldID fieldID);
	jfloat (JNICALL *GetStaticFloatField)(JNIEnv* pEnv, jclass clazz, jfieldID fieldID);
	jdouble (JNICALL *GetStaticDoubleField)(JNIEnv* pEnv, jclass clazz, jfieldID fieldID);

	void (JNICALL *SetStaticObjectField)(JNIEnv* pEnv, jclass clazz, jfieldID fieldID, jobject value);
	void (JNICALL *SetStaticBooleanField)(JNIEnv* pEnv, jclass clazz, jfieldID fieldID, jboolean value);
	void (JNICALL *SetStaticByteField)(JNIEnv* pEnv, jclass clazz, jfieldID fieldID, jbyte value);
	void (JNICALL *SetStaticCharField)(JNIEnv* pEnv, jclass clazz, jfieldID fieldID, jchar value);
	void (JNICALL *SetStaticShortField)(JNIEnv* pEnv, jclass clazz, jfieldID fieldID, jshort value);
	void (JNICALL *SetStaticIntField)(JNIEnv* pEnv, jclass clazz, jfieldID fieldID, jint value);
	void (JNICALL *SetStaticLongField)(JNIEnv* pEnv, jclass clazz, jfieldID fieldID, jlong value);
	void (JNICALL *SetStaticFloatField)(JNIEnv* pEnv, jclass clazz, jfieldID fieldID, jfloat value);
	void (JNICALL *SetStaticDoubleField)(JNIEnv* pEnv, jclass clazz, jfieldID fieldID, jdouble value);

	jstring (JNICALL *NewString)(JNIEnv* pEnv, const jchar *unicode, jsize len);
	jsize (JNICALL *GetStringLength)(JNIEnv* pEnv, jstring str);
	const jchar *(JNICALL *GetStringChars)(JNIEnv* pEnv, jstring str, jboolean *isCopy);
	void (JNICALL *ReleaseStringChars)(JNIEnv* pEnv, jstring str, const jchar *chars);

	jstring (JNICALL *NewStringUTF)(JNIEnv* pEnv, const char *utf);
	jsize (JNICALL *GetStringUTFLength)(JNIEnv* pEnv, jstring str);
	const char* (JNICALL *GetStringUTFChars)(JNIEnv* pEnv, jstring str, jboolean *isCopy);
	void (JNICALL *ReleaseStringUTFChars)(JNIEnv* pEnv, jstring str, const char* chars);

	jsize (JNICALL *GetArrayLength)(JNIEnv* pEnv, jarray array);

	jobjectArray (JNICALL *NewObjectArray)(JNIEnv* pEnv, jsize len, jclass clazz, jobject init);
	jobject (JNICALL *GetObjectArrayElement)(JNIEnv* pEnv, jobjectArray array, jsize index);
	void (JNICALL *SetObjectArrayElement)(JNIEnv* pEnv, jobjectArray array, jsize index, jobject val);

	jbooleanArray (JNICALL *NewBooleanArray)(JNIEnv* pEnv, jsize len);
	jbyteArray (JNICALL *NewByteArray)(JNIEnv* pEnv, jsize len);
	jcharArray (JNICALL *NewCharArray)(JNIEnv* pEnv, jsize len);
	jshortArray (JNICALL *NewShortArray)(JNIEnv* pEnv, jsize len);
	jintArray (JNICALL *NewIntArray)(JNIEnv* pEnv, jsize len);
	jlongArray (JNICALL *NewLongArray)(JNIEnv* pEnv, jsize len);
	jfloatArray (JNICALL *NewFloatArray)(JNIEnv* pEnv, jsize len);
	jdoubleArray (JNICALL *NewDoubleArray)(JNIEnv* pEnv, jsize len);

	jboolean * (JNICALL *GetBooleanArrayElements)(JNIEnv* pEnv, jbooleanArray array, jboolean *isCopy);
	jbyte * (JNICALL *GetByteArrayElements)(JNIEnv* pEnv, jbyteArray array, jboolean *isCopy);
	jchar * (JNICALL *GetCharArrayElements)(JNIEnv* pEnv, jcharArray array, jboolean *isCopy);
	jshort * (JNICALL *GetShortArrayElements)(JNIEnv* pEnv, jshortArray array, jboolean *isCopy);
	jint * (JNICALL *GetIntArrayElements)(JNIEnv* pEnv, jintArray array, jboolean *isCopy);
	jlong * (JNICALL *GetLongArrayElements)(JNIEnv* pEnv, jlongArray array, jboolean *isCopy);
	jfloat * (JNICALL *GetFloatArrayElements)(JNIEnv* pEnv, jfloatArray array, jboolean *isCopy);
	jdouble * (JNICALL *GetDoubleArrayElements)(JNIEnv* pEnv, jdoubleArray array, jboolean *isCopy);

	void (JNICALL *ReleaseBooleanArrayElements)(JNIEnv* pEnv, jbooleanArray array, jboolean *elems, jint mode);
	void (JNICALL *ReleaseByteArrayElements)(JNIEnv* pEnv, jbyteArray array, jbyte *elems, jint mode);
	void (JNICALL *ReleaseCharArrayElements)(JNIEnv* pEnv, jcharArray array, jchar *elems, jint mode);
	void (JNICALL *ReleaseShortArrayElements)(JNIEnv* pEnv, jshortArray array, jshort *elems, jint mode);
	void (JNICALL *ReleaseIntArrayElements)(JNIEnv* pEnv, jintArray array, jint *elems, jint mode);
	void (JNICALL *ReleaseLongArrayElements)(JNIEnv* pEnv, jlongArray array, jlong *elems, jint mode);
	void (JNICALL *ReleaseFloatArrayElements)(JNIEnv* pEnv, jfloatArray array, jfloat *elems, jint mode);
	void (JNICALL *ReleaseDoubleArrayElements)(JNIEnv* pEnv, jdoubleArray array, jdouble *elems, jint mode);

	void (JNICALL *GetBooleanArrayRegion)(JNIEnv* pEnv, jbooleanArray array, jsize start, jsize l, jboolean *buf);
	void (JNICALL *GetByteArrayRegion)(JNIEnv* pEnv, jbyteArray array, jsize start, jsize len, jbyte *buf);
	void (JNICALL *GetCharArrayRegion)(JNIEnv* pEnv, jcharArray array, jsize start, jsize len, jchar *buf);
	void (JNICALL *GetShortArrayRegion)(JNIEnv* pEnv, jshortArray array, jsize start, jsize len, jshort *buf);
	void (JNICALL *GetIntArrayRegion)(JNIEnv* pEnv, jintArray array, jsize start, jsize len, jint *buf);
	void (JNICALL *GetLongArrayRegion)(JNIEnv* pEnv, jlongArray array, jsize start, jsize len, jlong *buf);
	void (JNICALL *GetFloatArrayRegion)(JNIEnv* pEnv, jfloatArray array, jsize start, jsize len, jfloat *buf);
	void (JNICALL *GetDoubleArrayRegion)(JNIEnv* pEnv, jdoubleArray array, jsize start, jsize len, jdouble *buf);

	void (JNICALL *SetBooleanArrayRegion)(JNIEnv* pEnv, jbooleanArray array, jsize start, jsize l, jboolean *buf);
	void (JNICALL *SetByteArrayRegion)(JNIEnv* pEnv, jbyteArray array, jsize start, jsize len, jbyte *buf);
	void (JNICALL *SetCharArrayRegion)(JNIEnv* pEnv, jcharArray array, jsize start, jsize len, jchar *buf);
	void (JNICALL *SetShortArrayRegion)(JNIEnv* pEnv, jshortArray array, jsize start, jsize len, jshort *buf);
	void (JNICALL *SetIntArrayRegion)(JNIEnv* pEnv, jintArray array, jsize start, jsize len, jint *buf);
	void (JNICALL *SetLongArrayRegion)(JNIEnv* pEnv, jlongArray array, jsize start, jsize len, jlong *buf);
	void (JNICALL *SetFloatArrayRegion)(JNIEnv* pEnv, jfloatArray array, jsize start, jsize len, jfloat *buf);
	void (JNICALL *SetDoubleArrayRegion)(JNIEnv* pEnv, jdoubleArray array, jsize start, jsize len, jdouble *buf);

	jint (JNICALL *RegisterNatives)(JNIEnv* pEnv, jclass clazz, const JNINativeMethod *methods, jint nMethods);
	jint (JNICALL *UnregisterNatives)(JNIEnv* pEnv, jclass clazz);

	jint (JNICALL *MonitorEnter)(JNIEnv* pEnv, jobject obj);
	jint (JNICALL *MonitorExit)(JNIEnv* pEnv, jobject obj);

	jint (JNICALL *GetJavaVM)(JNIEnv* pEnv, JavaVM **vm);

	void (JNICALL *GetStringRegion)(JNIEnv* pEnv, jstring str, jsize start, jsize len, jchar *buf);
	void (JNICALL *GetStringUTFRegion)(JNIEnv* pEnv, jstring str, jsize start, jsize len, char *buf);

	void* (JNICALL *GetPrimitiveArrayCritical)(JNIEnv* pEnv, jarray array, jboolean *isCopy);
	void (JNICALL *ReleasePrimitiveArrayCritical)(JNIEnv* pEnv, jarray array, void *carray, jint mode);

	const jchar* (JNICALL *GetStringCritical)(JNIEnv* pEnv, jstring string, jboolean *isCopy);
	void (JNICALL *ReleaseStringCritical)(JNIEnv* pEnv, jstring string, const jchar *cstring);

	jweak (JNICALL *NewWeakGlobalRef)(JNIEnv* pEnv, jobject obj);
	void (JNICALL *DeleteWeakGlobalRef)(JNIEnv* pEnv, jweak ref);

	jboolean (JNICALL *ExceptionCheck)(JNIEnv* pEnv);

	jobject (JNICALL *NewDirectByteBuffer)(JNIEnv* pEnv, void* address, jlong capacity);
	void* (JNICALL *GetDirectBufferAddress)(JNIEnv* pEnv, jobject buf);
	jlong (JNICALL *GetDirectBufferCapacity)(JNIEnv* pEnv, jobject buf);
};

#endif //__JNI_H__
