/* Copyright (c) 2008-2011, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

#ifndef MACHINE_H
#define MACHINE_H

#include "common.h"
#include "system.h"
#include "heap.h"
#include "finder.h"
#include "processor.h"
#include "constants.h"
#include "arch.h"

#ifdef PLATFORM_WINDOWS
#  define JNICALL __stdcall
#else
#  define JNICALL
#endif

#define PROTECT(thread, name)                                   \
  Thread::SingleProtector MAKE_NAME(protector_) (thread, &name);

#define ACQUIRE(t, x) MonitorResource MAKE_NAME(monitorResource_) (t, x)

#define ACQUIRE_OBJECT(t, x) \
  ObjectMonitorResource MAKE_NAME(monitorResource_) (t, x)

#define ACQUIRE_FIELD_FOR_READ(t, field) \
  FieldReadResource MAKE_NAME(monitorResource_) (t, field)

#define ACQUIRE_FIELD_FOR_WRITE(t, field) \
  FieldWriteResource MAKE_NAME(monitorResource_) (t, field)

#define ACQUIRE_RAW(t, x) RawMonitorResource MAKE_NAME(monitorResource_) (t, x)

#define ENTER(t, state) StateResource MAKE_NAME(stateResource_) (t, state)

#define THREAD_RESOURCE0(t, releaseBody)                                \
  class MAKE_NAME(Resource_): public Thread::Resource {                 \
  public:                                                               \
    MAKE_NAME(Resource_)(Thread* t): Resource(t) { }                    \
    ~MAKE_NAME(Resource_)() { releaseBody; }                            \
    virtual void release()                                              \
    { this->MAKE_NAME(Resource_)::~MAKE_NAME(Resource_)(); }            \
  } MAKE_NAME(resource_)(t);

#define OBJECT_RESOURCE(t, name, releaseBody)                           \
  class MAKE_NAME(Resource_): public Thread::Resource {                 \
  public:                                                               \
    MAKE_NAME(Resource_)(Thread* t, object name):                       \
      Resource(t), name(name), protector(t, &(this->name)) { }          \
    ~MAKE_NAME(Resource_)() { releaseBody; }                            \
    virtual void release()                                              \
    { this->MAKE_NAME(Resource_)::~MAKE_NAME(Resource_)(); }            \
                                                                        \
  private:                                                              \
    object name;                                                        \
    Thread::SingleProtector protector;                                  \
  } MAKE_NAME(resource_)(t, name);

#define THREAD_RESOURCE(t, type, name, releaseBody)                     \
  class MAKE_NAME(Resource_): public Thread::Resource {                 \
  public:                                                               \
    MAKE_NAME(Resource_)(Thread* t, type name):                         \
      Resource(t), name(name) { }                                       \
    ~MAKE_NAME(Resource_)() { releaseBody; }                            \
    virtual void release()                                              \
    { this->MAKE_NAME(Resource_)::~MAKE_NAME(Resource_)(); }            \
                                                                        \
  private:                                                              \
    type name;                                                          \
  } MAKE_NAME(resource_)(t, name);

#define THREAD_RESOURCE2(t, type1, name1, type2, name2, releaseBody)    \
  class MAKE_NAME(Resource_): public Thread::Resource {                 \
  public:                                                               \
    MAKE_NAME(Resource_)(Thread* t, type1 name1, type2 name2):          \
      Resource(t), name1(name1), name2(name2) { }                       \
    ~MAKE_NAME(Resource_)() { releaseBody; }                            \
    virtual void release()                                              \
    { this->MAKE_NAME(Resource_)::~MAKE_NAME(Resource_)(); }            \
                                                                        \
  private:                                                              \
    type1 name1;                                                        \
    type2 name2;                                                        \
  } MAKE_NAME(resource_)(t, name1, name2);

namespace vm {

const bool Verbose = false;
const bool DebugRun = false;
const bool DebugStack = false;
const bool DebugMonitors = false;
const bool DebugReferences = false;

const uintptr_t HashTakenMark = 1;
const uintptr_t ExtendedMark = 2;
const uintptr_t FixedMark = 3;

const unsigned ThreadHeapSizeInBytes = 64 * 1024;
const unsigned ThreadHeapSizeInWords = ThreadHeapSizeInBytes / BytesPerWord;

const unsigned ThreadBackupHeapSizeInBytes = 2 * 1024;
const unsigned ThreadBackupHeapSizeInWords
= ThreadBackupHeapSizeInBytes / BytesPerWord;

const unsigned StackSizeInBytes = 128 * 1024;
const unsigned StackSizeInWords = StackSizeInBytes / BytesPerWord;

const unsigned ThreadHeapPoolSize = 64;

const unsigned FixedFootprintThresholdInBytes
= ThreadHeapPoolSize * ThreadHeapSizeInBytes;

enum FieldCode {
  VoidField,
  ByteField,
  CharField,
  DoubleField,
  FloatField,
  IntField,
  LongField,
  ShortField,
  BooleanField,
  ObjectField
};

enum StackTag {
  IntTag, // must be zero
  ObjectTag
};

const int NativeLine = -2;
const int UnknownLine = -1;

// class vmFlags:
const unsigned ReferenceFlag = 1 << 0;
const unsigned WeakReferenceFlag = 1 << 1;
const unsigned NeedInitFlag = 1 << 2;
const unsigned InitFlag = 1 << 3;
const unsigned InitErrorFlag = 1 << 4;
const unsigned PrimitiveFlag = 1 << 5;
const unsigned BootstrapFlag = 1 << 6;
const unsigned HasFinalizerFlag = 1 << 7;
const unsigned LinkFlag = 1 << 8;
const unsigned HasFinalMemberFlag = 1 << 9;
const unsigned SingletonFlag = 1 << 10;
const unsigned ContinuationFlag = 1 << 11;

// method vmFlags:
const unsigned ClassInitFlag = 1 << 0;
const unsigned ConstructorFlag = 1 << 1;

#ifndef JNI_VERSION_1_6
#define JNI_VERSION_1_6 0x00010006
#endif

typedef Machine JavaVM;
typedef Thread JNIEnv;

typedef uint8_t jboolean;
typedef int8_t jbyte;
typedef uint16_t jchar;
typedef int16_t jshort;
typedef int32_t jint;
typedef int64_t jlong;
typedef float jfloat;
typedef double jdouble;

typedef jint jsize;

typedef object* jobject;

typedef jobject jclass;
typedef jobject jthrowable;
typedef jobject jstring;
typedef jobject jweak;

typedef jobject jarray;
typedef jarray jbooleanArray;
typedef jarray jbyteArray;
typedef jarray jcharArray;
typedef jarray jshortArray;
typedef jarray jintArray;
typedef jarray jlongArray;
typedef jarray jfloatArray;
typedef jarray jdoubleArray;
typedef jarray jobjectArray;

typedef uintptr_t jfieldID;
typedef uintptr_t jmethodID;

union jvalue {
  jboolean z;
  jbyte    b;
  jchar    c;
  jshort   s;
  jint     i;
  jlong    j;
  jfloat   f;
  jdouble  d;
  jobject  l;
};

struct JNINativeMethod {
  char* name;
  char* signature;
  void* function;
};

struct JavaVMVTable {
  void* reserved0;
  void* reserved1;
  void* reserved2;

#if (! TARGET_RT_MAC_CFM) && defined(__ppc__)
  void* cfm_vectors[4];
#endif

  jint
  (JNICALL *DestroyJavaVM)
  (JavaVM*);

  jint
  (JNICALL *AttachCurrentThread)
  (JavaVM*, JNIEnv**, void*);

  jint
  (JNICALL *DetachCurrentThread)
  (JavaVM*);

  jint
  (JNICALL *GetEnv)
  (JavaVM*, JNIEnv**, jint);

  jint
  (JNICALL *AttachCurrentThreadAsDaemon)
  (JavaVM*, JNIEnv**, void*);

#if TARGET_RT_MAC_CFM && defined(__ppc__)
    void* real_functions[5];
#endif
};

struct JNIEnvVTable {
  void* reserved0;
  void* reserved1;
  void* reserved2;
  void* reserved3;

#if (! TARGET_RT_MAC_CFM) && defined(__ppc__)
  void* cfm_vectors[225];
#endif

  jint
  (JNICALL *GetVersion)
    (JNIEnv*);

  jclass
  (JNICALL *DefineClass)
    (JNIEnv*, const char*, jobject, const jbyte*, jsize);

  jclass
  (JNICALL *FindClass)
    (JNIEnv*, const char*);

  jmethodID
  (JNICALL *FromReflectedMethod)
    (JNIEnv*, jobject);

  jfieldID
  (JNICALL *FromReflectedField)
    (JNIEnv*, jobject);

  jobject
  (JNICALL *ToReflectedMethod)
    (JNIEnv*, jclass, jmethodID, jboolean);

  jclass
  (JNICALL *GetSuperclass)
    (JNIEnv*, jclass);

  jboolean
  (JNICALL *IsAssignableFrom)
    (JNIEnv*, jclass, jclass);

  jobject
  (JNICALL *ToReflectedField)
    (JNIEnv*, jclass, jfieldID, jboolean);

  jint
  (JNICALL *Throw)
    (JNIEnv*, jthrowable);

  jint
  (JNICALL *ThrowNew)
    (JNIEnv*, jclass, const char*);

  jthrowable
  (JNICALL *ExceptionOccurred)
    (JNIEnv*);

  void
  (JNICALL *ExceptionDescribe)
  (JNIEnv*);

  void
  (JNICALL *ExceptionClear)
  (JNIEnv*);

  void
  (JNICALL *FatalError)
  (JNIEnv*, const char*);

  jint
  (JNICALL *PushLocalFrame)
    (JNIEnv*, jint);

  jobject
  (JNICALL *PopLocalFrame)
    (JNIEnv*, jobject);

  jobject
  (JNICALL *NewGlobalRef)
    (JNIEnv*, jobject);

  void
  (JNICALL *DeleteGlobalRef)
  (JNIEnv*, jobject);

  void
  (JNICALL *DeleteLocalRef)
  (JNIEnv*, jobject);

  jboolean
  (JNICALL *IsSameObject)
    (JNIEnv*, jobject, jobject);

  jobject
  (JNICALL *NewLocalRef)
    (JNIEnv*, jobject);

  jint
  (JNICALL *EnsureLocalCapacity)
    (JNIEnv*, jint);

  jobject
  (JNICALL *AllocObject)
    (JNIEnv*, jclass);

  jobject
  (JNICALL *NewObject)
    (JNIEnv*, jclass, jmethodID, ...);

  jobject
  (JNICALL *NewObjectV)
    (JNIEnv*, jclass, jmethodID, va_list);

  jobject
  (JNICALL *NewObjectA)
    (JNIEnv*, jclass, jmethodID, const jvalue*);

  jclass
  (JNICALL *GetObjectClass)
    (JNIEnv*, jobject);

  jboolean
  (JNICALL *IsInstanceOf)
    (JNIEnv*, jobject, jclass);

  jmethodID
  (JNICALL *GetMethodID)
    (JNIEnv*, jclass, const char*, const char*);

  jobject
  (JNICALL *CallObjectMethod)
    (JNIEnv*, jobject, jmethodID, ...);

  jobject
  (JNICALL *CallObjectMethodV)
    (JNIEnv*, jobject, jmethodID, va_list);

  jobject
  (JNICALL *CallObjectMethodA)
    (JNIEnv*, jobject, jmethodID, const jvalue*);

  jboolean
  (JNICALL *CallBooleanMethod)
    (JNIEnv*, jobject, jmethodID, ...);

  jboolean
  (JNICALL *CallBooleanMethodV)
    (JNIEnv*, jobject, jmethodID, va_list);

  jboolean
  (JNICALL *CallBooleanMethodA)
    (JNIEnv*, jobject, jmethodID, const jvalue*);

  jbyte
  (JNICALL *CallByteMethod)
    (JNIEnv*, jobject, jmethodID, ...);

  jbyte
  (JNICALL *CallByteMethodV)
    (JNIEnv*, jobject, jmethodID, va_list);

  jbyte
  (JNICALL *CallByteMethodA)
    (JNIEnv*, jobject, jmethodID, const jvalue*);

  jchar
  (JNICALL *CallCharMethod)
    (JNIEnv*, jobject, jmethodID, ...);

  jchar
  (JNICALL *CallCharMethodV)
    (JNIEnv*, jobject, jmethodID, va_list);

  jchar
  (JNICALL *CallCharMethodA)
    (JNIEnv*, jobject, jmethodID, const jvalue*);

  jshort
  (JNICALL *CallShortMethod)
    (JNIEnv*, jobject, jmethodID, ...);

  jshort
  (JNICALL *CallShortMethodV)
    (JNIEnv*, jobject, jmethodID, va_list);

  jshort
  (JNICALL *CallShortMethodA)
    (JNIEnv*, jobject, jmethodID, const jvalue*);

  jint
  (JNICALL *CallIntMethod)
    (JNIEnv*, jobject, jmethodID, ...);

  jint
  (JNICALL *CallIntMethodV)
    (JNIEnv*, jobject, jmethodID, va_list);

  jint
  (JNICALL *CallIntMethodA)
    (JNIEnv*, jobject, jmethodID, const jvalue*);

  jlong
  (JNICALL *CallLongMethod)
    (JNIEnv*, jobject, jmethodID, ...);

  jlong
  (JNICALL *CallLongMethodV)
    (JNIEnv*, jobject, jmethodID, va_list);

  jlong
  (JNICALL *CallLongMethodA)
    (JNIEnv*, jobject, jmethodID, const jvalue*);

  jfloat
  (JNICALL *CallFloatMethod)
  (JNIEnv*, jobject, jmethodID, ...);

  jfloat
  (JNICALL *CallFloatMethodV)
  (JNIEnv*, jobject, jmethodID, va_list);

  jfloat
  (JNICALL *CallFloatMethodA)
  (JNIEnv*, jobject, jmethodID, const jvalue*);

  jdouble
  (JNICALL *CallDoubleMethod)
  (JNIEnv*, jobject, jmethodID, ...);

  jdouble
  (JNICALL *CallDoubleMethodV)
  (JNIEnv*, jobject, jmethodID, va_list);

  jdouble
  (JNICALL *CallDoubleMethodA)
  (JNIEnv*, jobject, jmethodID, const jvalue*);

  void
  (JNICALL *CallVoidMethod)
  (JNIEnv*, jobject, jmethodID, ...);

  void
  (JNICALL *CallVoidMethodV)
  (JNIEnv*, jobject, jmethodID, va_list);

  void
  (JNICALL *CallVoidMethodA)
  (JNIEnv*, jobject, jmethodID, const jvalue*);

  jobject
  (JNICALL *CallNonvirtualObjectMethod)
    (JNIEnv*, jobject, jclass, jmethodID, ...);

  jobject
  (JNICALL *CallNonvirtualObjectMethodV)
    (JNIEnv*, jobject, jclass, jmethodID, va_list);

  jobject
  (JNICALL *CallNonvirtualObjectMethodA)
    (JNIEnv*, jobject, jclass, jmethodID, const jvalue*);

  jboolean
  (JNICALL *CallNonvirtualBooleanMethod)
    (JNIEnv*, jobject, jclass, jmethodID, ...);

  jboolean
  (JNICALL *CallNonvirtualBooleanMethodV)
    (JNIEnv*, jobject, jclass, jmethodID, va_list);

  jboolean
  (JNICALL *CallNonvirtualBooleanMethodA)
    (JNIEnv*, jobject, jclass, jmethodID, const jvalue*);

  jbyte
  (JNICALL *CallNonvirtualByteMethod)
    (JNIEnv*, jobject, jclass, jmethodID, ...);

  jbyte
  (JNICALL *CallNonvirtualByteMethodV)
    (JNIEnv*, jobject, jclass, jmethodID, va_list);

  jbyte
  (JNICALL *CallNonvirtualByteMethodA)
    (JNIEnv*, jobject, jclass, jmethodID, const jvalue*);

  jchar
  (JNICALL *CallNonvirtualCharMethod)
    (JNIEnv*, jobject, jclass, jmethodID, ...);

  jchar
  (JNICALL *CallNonvirtualCharMethodV)
    (JNIEnv*, jobject, jclass, jmethodID, va_list);

  jchar
  (JNICALL *CallNonvirtualCharMethodA)
    (JNIEnv*, jobject, jclass, jmethodID, const jvalue*);

  jshort
  (JNICALL *CallNonvirtualShortMethod)
    (JNIEnv*, jobject, jclass, jmethodID, ...);

  jshort
  (JNICALL *CallNonvirtualShortMethodV)
    (JNIEnv*, jobject, jclass, jmethodID,
     va_list);

  jshort
  (JNICALL *CallNonvirtualShortMethodA)
    (JNIEnv*, jobject, jclass, jmethodID,
     const jvalue*);

  jint
  (JNICALL *CallNonvirtualIntMethod)
    (JNIEnv*, jobject, jclass, jmethodID, ...);

  jint
  (JNICALL *CallNonvirtualIntMethodV)
    (JNIEnv*, jobject, jclass, jmethodID,
     va_list);

  jint
  (JNICALL *CallNonvirtualIntMethodA)
    (JNIEnv*, jobject, jclass, jmethodID,
     const jvalue*);

  jlong
  (JNICALL *CallNonvirtualLongMethod)
    (JNIEnv*, jobject, jclass, jmethodID, ...);

  jlong
  (JNICALL *CallNonvirtualLongMethodV)
    (JNIEnv*, jobject, jclass, jmethodID,
     va_list);
  jlong
  (JNICALL *CallNonvirtualLongMethodA)
    (JNIEnv*, jobject, jclass, jmethodID, const jvalue*);

  jfloat
  (JNICALL *CallNonvirtualFloatMethod)
  (JNIEnv*, jobject, jclass, jmethodID, ...);

  jfloat
  (JNICALL *CallNonvirtualFloatMethodV)
  (JNIEnv*, jobject, jclass, jmethodID, va_list);

  jfloat
  (JNICALL *CallNonvirtualFloatMethodA)
  (JNIEnv*, jobject, jclass, jmethodID, const jvalue*);

  jdouble
  (JNICALL *CallNonvirtualDoubleMethod)
  (JNIEnv*, jobject, jclass, jmethodID, ...);

  jdouble
  (JNICALL *CallNonvirtualDoubleMethodV)
  (JNIEnv*, jobject, jclass, jmethodID, va_list);

  jdouble
  (JNICALL *CallNonvirtualDoubleMethodA)
  (JNIEnv*, jobject, jclass, jmethodID, const jvalue*);

  void
  (JNICALL *CallNonvirtualVoidMethod)
  (JNIEnv*, jobject, jclass, jmethodID, ...);

  void
  (JNICALL *CallNonvirtualVoidMethodV)
  (JNIEnv*, jobject, jclass, jmethodID, va_list);

  void
  (JNICALL *CallNonvirtualVoidMethodA)
  (JNIEnv*, jobject, jclass, jmethodID, const jvalue*);

  jfieldID
  (JNICALL *GetFieldID)
    (JNIEnv*, jclass, const char*, const char*);

  jobject
  (JNICALL *GetObjectField)
    (JNIEnv*, jobject, jfieldID);

  jboolean
  (JNICALL *GetBooleanField)
    (JNIEnv*, jobject, jfieldID);

  jbyte
  (JNICALL *GetByteField)
    (JNIEnv*, jobject, jfieldID);

  jchar
  (JNICALL *GetCharField)
    (JNIEnv*, jobject, jfieldID);

  jshort
  (JNICALL *GetShortField)
    (JNIEnv*, jobject, jfieldID);

  jint
  (JNICALL *GetIntField)
    (JNIEnv*, jobject, jfieldID);

  jlong
  (JNICALL *GetLongField)
    (JNIEnv*, jobject, jfieldID);

  jfloat
  (JNICALL *GetFloatField)
  (JNIEnv*, jobject, jfieldID);

  jdouble
  (JNICALL *GetDoubleField)
  (JNIEnv*, jobject, jfieldID);

  void
  (JNICALL *SetObjectField)
  (JNIEnv*, jobject, jfieldID, jobject);

  void
  (JNICALL *SetBooleanField)
  (JNIEnv*, jobject, jfieldID, jboolean);

  void
  (JNICALL *SetByteField)
  (JNIEnv*, jobject, jfieldID, jbyte);

  void
  (JNICALL *SetCharField)
  (JNIEnv*, jobject, jfieldID, jchar);

  void
  (JNICALL *SetShortField)
  (JNIEnv*, jobject, jfieldID, jshort);

  void
  (JNICALL *SetIntField)
  (JNIEnv*, jobject, jfieldID, jint);

  void
  (JNICALL *SetLongField)
  (JNIEnv*, jobject, jfieldID, jlong);

  void
  (JNICALL *SetFloatField)
  (JNIEnv*, jobject, jfieldID, jfloat);

  void
  (JNICALL *SetDoubleField)
  (JNIEnv*, jobject, jfieldID, jdouble);

  jmethodID
  (JNICALL *GetStaticMethodID)
    (JNIEnv*, jclass, const char*, const char*);

  jobject
  (JNICALL *CallStaticObjectMethod)
    (JNIEnv*, jclass, jmethodID, ...);

  jobject
  (JNICALL *CallStaticObjectMethodV)
    (JNIEnv*, jclass, jmethodID, va_list);

  jobject
  (JNICALL *CallStaticObjectMethodA)
    (JNIEnv*, jclass, jmethodID, const jvalue*);

  jboolean
  (JNICALL *CallStaticBooleanMethod)
    (JNIEnv*, jclass, jmethodID, ...);

  jboolean
  (JNICALL *CallStaticBooleanMethodV)
    (JNIEnv*, jclass, jmethodID, va_list);

  jboolean
  (JNICALL *CallStaticBooleanMethodA)
    (JNIEnv*, jclass, jmethodID, const jvalue*);

  jbyte
  (JNICALL *CallStaticByteMethod)
    (JNIEnv*, jclass, jmethodID, ...);

  jbyte
  (JNICALL *CallStaticByteMethodV)
    (JNIEnv*, jclass, jmethodID, va_list);

  jbyte
  (JNICALL *CallStaticByteMethodA)
    (JNIEnv*, jclass, jmethodID, const jvalue*);

  jchar
  (JNICALL *CallStaticCharMethod)
    (JNIEnv*, jclass, jmethodID, ...);

  jchar
  (JNICALL *CallStaticCharMethodV)
    (JNIEnv*, jclass, jmethodID, va_list);

  jchar
  (JNICALL *CallStaticCharMethodA)
    (JNIEnv*, jclass, jmethodID, const jvalue*);

  jshort
  (JNICALL *CallStaticShortMethod)
    (JNIEnv*, jclass, jmethodID, ...);

  jshort
  (JNICALL *CallStaticShortMethodV)
    (JNIEnv*, jclass, jmethodID, va_list);

  jshort
  (JNICALL *CallStaticShortMethodA)
    (JNIEnv*, jclass, jmethodID, const jvalue*);

  jint
  (JNICALL *CallStaticIntMethod)
    (JNIEnv*, jclass, jmethodID, ...);

  jint
  (JNICALL *CallStaticIntMethodV)
    (JNIEnv*, jclass, jmethodID, va_list);

  jint
  (JNICALL *CallStaticIntMethodA)
    (JNIEnv*, jclass, jmethodID, const jvalue*);

  jlong
  (JNICALL *CallStaticLongMethod)
    (JNIEnv*, jclass, jmethodID, ...);

  jlong
  (JNICALL *CallStaticLongMethodV)
    (JNIEnv*, jclass, jmethodID, va_list);

  jlong
  (JNICALL *CallStaticLongMethodA)
    (JNIEnv*, jclass, jmethodID, const jvalue*);

  jfloat
  (JNICALL *CallStaticFloatMethod)
  (JNIEnv*, jclass, jmethodID, ...);

  jfloat
  (JNICALL *CallStaticFloatMethodV)
  (JNIEnv*, jclass, jmethodID, va_list);

  jfloat
  (JNICALL *CallStaticFloatMethodA)
  (JNIEnv*, jclass, jmethodID, const jvalue*);

  jdouble
  (JNICALL *CallStaticDoubleMethod)
  (JNIEnv*, jclass, jmethodID, ...);

  jdouble
  (JNICALL *CallStaticDoubleMethodV)
  (JNIEnv*, jclass, jmethodID, va_list);

  jdouble
  (JNICALL *CallStaticDoubleMethodA)
  (JNIEnv*, jclass, jmethodID, const jvalue*);

  void
  (JNICALL *CallStaticVoidMethod)
  (JNIEnv*, jclass, jmethodID, ...);

  void
  (JNICALL *CallStaticVoidMethodV)
  (JNIEnv*, jclass, jmethodID, va_list);

  void
  (JNICALL *CallStaticVoidMethodA)
  (JNIEnv*, jclass, jmethodID, const jvalue*);

  jfieldID
  (JNICALL *GetStaticFieldID)
    (JNIEnv*, jclass, const char*, const char*);

  jobject
  (JNICALL *GetStaticObjectField)
    (JNIEnv*, jclass, jfieldID);

  jboolean
  (JNICALL *GetStaticBooleanField)
    (JNIEnv*, jclass, jfieldID);

  jbyte
  (JNICALL *GetStaticByteField)
    (JNIEnv*, jclass, jfieldID);

  jchar
  (JNICALL *GetStaticCharField)
    (JNIEnv*, jclass, jfieldID);

  jshort
  (JNICALL *GetStaticShortField)
    (JNIEnv*, jclass, jfieldID);

  jint
  (JNICALL *GetStaticIntField)
    (JNIEnv*, jclass, jfieldID);

  jlong
  (JNICALL *GetStaticLongField)
    (JNIEnv*, jclass, jfieldID);

  jfloat
  (JNICALL *GetStaticFloatField)
  (JNIEnv*, jclass, jfieldID);

  jdouble
  (JNICALL *GetStaticDoubleField)
  (JNIEnv*, jclass, jfieldID);

  void
  (JNICALL *SetStaticObjectField)
  (JNIEnv*, jclass, jfieldID, jobject);

  void
  (JNICALL *SetStaticBooleanField)
  (JNIEnv*, jclass, jfieldID, jboolean);

  void
  (JNICALL *SetStaticByteField)
  (JNIEnv*, jclass, jfieldID, jbyte);

  void
  (JNICALL *SetStaticCharField)
  (JNIEnv*, jclass, jfieldID, jchar);

  void
  (JNICALL *SetStaticShortField)
  (JNIEnv*, jclass, jfieldID, jshort);

  void
  (JNICALL *SetStaticIntField)
  (JNIEnv*, jclass, jfieldID, jint);

  void
  (JNICALL *SetStaticLongField)
  (JNIEnv*, jclass, jfieldID, jlong);

  void
  (JNICALL *SetStaticFloatField)
  (JNIEnv*, jclass, jfieldID, jfloat);

  void
  (JNICALL *SetStaticDoubleField)
  (JNIEnv*, jclass, jfieldID, jdouble);

  jstring
  (JNICALL *NewString)
    (JNIEnv*, const jchar*, jsize);

  jsize
  (JNICALL *GetStringLength)
    (JNIEnv*, jstring);

  const jchar*
  (JNICALL *GetStringChars)
  (JNIEnv*, jstring, jboolean*);

  void
  (JNICALL *ReleaseStringChars)
  (JNIEnv*, jstring, const jchar*);

  jstring
  (JNICALL *NewStringUTF)
    (JNIEnv*, const char*);

  jsize
  (JNICALL *GetStringUTFLength)
    (JNIEnv*, jstring);

  const char*
  (JNICALL *GetStringUTFChars)
  (JNIEnv*, jstring, jboolean*);

  void
  (JNICALL *ReleaseStringUTFChars)
  (JNIEnv*, jstring, const char*);

  jsize
  (JNICALL *GetArrayLength)
    (JNIEnv*, jarray);

  jobjectArray
  (JNICALL *NewObjectArray)
    (JNIEnv*, jsize, jclass, jobject);

  jobject
  (JNICALL *GetObjectArrayElement)
    (JNIEnv*, jobjectArray, jsize);

  void
  (JNICALL *SetObjectArrayElement)
  (JNIEnv*, jobjectArray, jsize, jobject);

  jbooleanArray
  (JNICALL *NewBooleanArray)
    (JNIEnv*, jsize);

  jbyteArray
  (JNICALL *NewByteArray)
    (JNIEnv*, jsize);

  jcharArray
  (JNICALL *NewCharArray)
    (JNIEnv*, jsize);

  jshortArray
  (JNICALL *NewShortArray)
    (JNIEnv*, jsize);

  jintArray
  (JNICALL *NewIntArray)
    (JNIEnv*, jsize);

  jlongArray
  (JNICALL *NewLongArray)
    (JNIEnv*, jsize);

  jfloatArray
  (JNICALL *NewFloatArray)
    (JNIEnv*, jsize);

  jdoubleArray
  (JNICALL *NewDoubleArray)
    (JNIEnv*, jsize);

  jboolean*
  (JNICALL *GetBooleanArrayElements)
  (JNIEnv*, jbooleanArray, jboolean*);

  jbyte*
  (JNICALL *GetByteArrayElements)
  (JNIEnv*, jbyteArray, jboolean*);

  jchar*
  (JNICALL *GetCharArrayElements)
  (JNIEnv*, jcharArray, jboolean*);

  jshort*
  (JNICALL *GetShortArrayElements)
  (JNIEnv*, jshortArray, jboolean*);

  jint*
  (JNICALL *GetIntArrayElements)
  (JNIEnv*, jintArray, jboolean*);

  jlong*
  (JNICALL *GetLongArrayElements)
  (JNIEnv*, jlongArray, jboolean*);

  jfloat*
  (JNICALL *GetFloatArrayElements)
  (JNIEnv*, jfloatArray, jboolean*);

  jdouble*
  (JNICALL *GetDoubleArrayElements)
  (JNIEnv*, jdoubleArray, jboolean*);

  void
  (JNICALL *ReleaseBooleanArrayElements)
  (JNIEnv*, jbooleanArray, jboolean*, jint);

  void
  (JNICALL *ReleaseByteArrayElements)
  (JNIEnv*, jbyteArray, jbyte*, jint);

  void
  (JNICALL *ReleaseCharArrayElements)
  (JNIEnv*, jcharArray, jchar*, jint);

  void
  (JNICALL *ReleaseShortArrayElements)
  (JNIEnv*, jshortArray, jshort*, jint);

  void
  (JNICALL *ReleaseIntArrayElements)
  (JNIEnv*, jintArray, jint*, jint);

  void
  (JNICALL *ReleaseLongArrayElements)
  (JNIEnv*, jlongArray, jlong*, jint);

  void
  (JNICALL *ReleaseFloatArrayElements)
  (JNIEnv*, jfloatArray, jfloat*, jint);

  void
  (JNICALL *ReleaseDoubleArrayElements)
  (JNIEnv*, jdoubleArray, jdouble*, jint);

  void
  (JNICALL *GetBooleanArrayRegion)
  (JNIEnv*, jbooleanArray, jsize, jsize, jboolean*);

  void
  (JNICALL *GetByteArrayRegion)
  (JNIEnv*, jbyteArray, jsize, jsize, jbyte*);

  void
  (JNICALL *GetCharArrayRegion)
  (JNIEnv*, jcharArray, jsize, jsize, jchar*);

  void
  (JNICALL *GetShortArrayRegion)
  (JNIEnv*, jshortArray, jsize, jsize, jshort*);

  void
  (JNICALL *GetIntArrayRegion)
  (JNIEnv*, jintArray, jsize, jsize, jint*);

  void
  (JNICALL *GetLongArrayRegion)
  (JNIEnv*, jlongArray, jsize, jsize, jlong*);

  void
  (JNICALL *GetFloatArrayRegion)
  (JNIEnv*, jfloatArray, jsize, jsize, jfloat*);

  void
  (JNICALL *GetDoubleArrayRegion)
  (JNIEnv*, jdoubleArray, jsize, jsize, jdouble*);

  void
  (JNICALL *SetBooleanArrayRegion)
  (JNIEnv*, jbooleanArray, jsize, jsize, const jboolean*);

  void
  (JNICALL *SetByteArrayRegion)
  (JNIEnv*, jbyteArray, jsize, jsize, const jbyte*);

  void
  (JNICALL *SetCharArrayRegion)
  (JNIEnv*, jcharArray, jsize, jsize, const jchar*);

  void
  (JNICALL *SetShortArrayRegion)
  (JNIEnv*, jshortArray, jsize, jsize, const jshort*);

  void
  (JNICALL *SetIntArrayRegion)
  (JNIEnv*, jintArray, jsize, jsize, const jint*);

  void
  (JNICALL *SetLongArrayRegion)
  (JNIEnv*, jlongArray, jsize, jsize, const jlong*);

  void
  (JNICALL *SetFloatArrayRegion)
  (JNIEnv*, jfloatArray, jsize, jsize, const jfloat*);

  void
  (JNICALL *SetDoubleArrayRegion)
  (JNIEnv*, jdoubleArray, jsize, jsize, const jdouble*);

  jint
  (JNICALL *RegisterNatives)
    (JNIEnv*, jclass, const JNINativeMethod*, jint);

  jint
  (JNICALL *UnregisterNatives)
    (JNIEnv*, jclass);

  jint
  (JNICALL *MonitorEnter)
    (JNIEnv*, jobject);

  jint
  (JNICALL *MonitorExit)
    (JNIEnv*, jobject);

  jint
  (JNICALL *GetJavaVM)
    (JNIEnv*, JavaVM**);

  void
  (JNICALL *GetStringRegion)
  (JNIEnv*, jstring, jsize, jsize, jchar*);

  void
  (JNICALL *GetStringUTFRegion)
  (JNIEnv*, jstring, jsize, jsize, char*);

  void*
  (JNICALL *GetPrimitiveArrayCritical)
  (JNIEnv*, jarray, jboolean*);

  void
  (JNICALL *ReleasePrimitiveArrayCritical)
  (JNIEnv*, jarray, void*, jint);

  const jchar*
  (JNICALL *GetStringCritical)
  (JNIEnv*, jstring, jboolean*);

  void
  (JNICALL *ReleaseStringCritical)
  (JNIEnv*, jstring, const jchar*);

  jweak
  (JNICALL *NewWeakGlobalRef)
  (JNIEnv*, jobject);

  void
  (JNICALL *DeleteWeakGlobalRef)
  (JNIEnv*, jweak);

  jboolean
  (JNICALL *ExceptionCheck)
    (JNIEnv*);

  jobject
  (JNICALL *NewDirectByteBuffer)
    (JNIEnv*, void*, jlong);

  void*
  (JNICALL *GetDirectBufferAddress)
  (JNIEnv* env, jobject);

  jlong
  (JNICALL *GetDirectBufferCapacity)
    (JNIEnv*, jobject);

#if TARGET_RT_MAC_CFM && defined(__ppc__)
  void* real_functions[228];
#endif
};

inline void
atomicOr(uint32_t* p, int v)
{
  for (uint32_t old = *p;
       not atomicCompareAndSwap32(p, old, old | v);
       old = *p)
  { }
}

inline void
atomicAnd(uint32_t* p, int v)
{
  for (uint32_t old = *p;
       not atomicCompareAndSwap32(p, old, old & v);
       old = *p)
  { }
}

inline int
strcmp(const int8_t* a, const int8_t* b)
{
  return ::strcmp(reinterpret_cast<const char*>(a),
                  reinterpret_cast<const char*>(b));
}

void
noop();

class Reference {
 public:
  Reference(object target, Reference** handle):
    target(target),
    next(*handle),
    handle(handle),
    count(0)
  {
    if (next) {
      next->handle = &next;
    }
    *handle = this;
  }

  object target;
  Reference* next;
  Reference** handle;
  unsigned count;
};

class Classpath;

class Machine {
 public:
  enum Type {
#include "type-enums.cpp"
  };

  enum AllocationType {
    MovableAllocation,
    FixedAllocation,
    ImmortalAllocation
  };

  enum Root {
    BootLoader,
    AppLoader,
    BootstrapClassMap,
    FindLoadedClassMethod,
    LoadClassMethod,
    MonitorMap,
    StringMap,
    ByteArrayMap,
    PoolMap,
    ClassRuntimeDataTable,
    MethodRuntimeDataTable,
    JNIMethodTable,
    JNIFieldTable,
    ShutdownHooks,
    FinalizerThread,
    ObjectsToFinalize,
    ObjectsToClean,
    NullPointerException,
    ArithmeticException,
    ArrayIndexOutOfBoundsException,
    OutOfMemoryError,
    Shutdown,
    VirtualFileFinders,
    VirtualFiles
  };

  static const unsigned RootCount = VirtualFiles + 1;

  Machine(System* system, Heap* heap, Finder* bootFinder, Finder* appFinder,
          Processor* processor, Classpath* classpath, const char** properties,
          unsigned propertyCount, const char** arguments,
          unsigned argumentCount);

  ~Machine() { 
    dispose();
  }

  void dispose();

  JavaVMVTable* vtable;
  System* system;
  Heap::Client* heapClient;
  Heap* heap;
  Finder* bootFinder;
  Finder* appFinder;
  Processor* processor;
  Classpath* classpath;
  Thread* rootThread;
  Thread* exclusive;
  Thread* finalizeThread;
  Reference* jniReferences;
  const char** properties;
  unsigned propertyCount;
  const char** arguments;
  unsigned argumentCount;
  unsigned activeCount;
  unsigned liveCount;
  unsigned daemonCount;
  unsigned fixedFootprint;
  System::Local* localThread;
  System::Monitor* stateLock;
  System::Monitor* heapLock;
  System::Monitor* classLock;
  System::Monitor* referenceLock;
  System::Monitor* shutdownLock;
  System::Library* libraries;
  FILE* errorLog;
  object types;
  object roots;
  object finalizers;
  object tenuredFinalizers;
  object finalizeQueue;
  object weakReferences;
  object tenuredWeakReferences;
  bool unsafe;
  bool collecting;
  bool triedBuiltinOnLoad;
  bool dumpedHeapOnOOM;
  bool alive;
  JavaVMVTable javaVMVTable;
  JNIEnvVTable jniEnvVTable;
  uintptr_t* heapPool[ThreadHeapPoolSize];
  unsigned heapPoolIndex;
};

void
printTrace(Thread* t, object exception);

uint8_t&
threadInterrupted(Thread* t, object thread);

void
enterActiveState(Thread* t);

#ifdef VM_STRESS

inline void stress(Thread* t);

#else // not VM_STRESS

#define stress(t)

#endif // not VM_STRESS

uint64_t
runThread(Thread*, uintptr_t*);

uint64_t
run(Thread* t, uint64_t (*function)(Thread*, uintptr_t*),
    uintptr_t* arguments);

void
checkDaemon(Thread* t);

object&
root(Thread* t, Machine::Root root);

extern "C" uint64_t
vmRun(uint64_t (*function)(Thread*, uintptr_t*), uintptr_t* arguments,
      void* checkpoint);

extern "C" void
vmRun_returnAddress();

class Thread {
 public:
  enum State {
    NoState,
    ActiveState,
    IdleState,
    ZombieState,
    JoinedState,
    ExclusiveState,
    ExitState
  };

  static const unsigned UseBackupHeapFlag = 1 << 0;
  static const unsigned WaitingFlag = 1 << 1;
  static const unsigned TracingFlag = 1 << 2;
  static const unsigned DaemonFlag = 1 << 3;
  static const unsigned StressFlag = 1 << 4;
  static const unsigned ActiveFlag = 1 << 5;
  static const unsigned SystemFlag = 1 << 6;
  static const unsigned DisposeFlag = 1 << 7;

  class Protector {
   public:
    Protector(Thread* t): t(t), next(t->protector) {
      t->protector = this;
    }

    ~Protector() {
      t->protector = next;
    }

    virtual void visit(Heap::Visitor* v) = 0;

    Thread* t;
    Protector* next;
  };

  class SingleProtector: public Protector {
   public:
    SingleProtector(Thread* t, object* p): Protector(t), p(p) { }

    virtual void visit(Heap::Visitor* v) {
      v->visit(p);
    }

    object* p;
  };

  class Resource {
   public:
    Resource(Thread* t): t(t), next(t->resource) {
      t->resource = this;
    }

    ~Resource() {
      t->resource = next;
    }

    virtual void release() = 0;

    Thread* t;
    Resource* next;
  };

  class ClassInitStack: public Resource {
   public:
    ClassInitStack(Thread* t, object class_):
      Resource(t),
      next(t->classInitStack),
      class_(class_),
      protector(t, &(this->class_))
    {
      t->classInitStack = this;
    }

    ~ClassInitStack() {
      t->classInitStack = next;
    }

    virtual void release() {
      this->ClassInitStack::~ClassInitStack();
    }

    ClassInitStack* next;
    object class_;
    SingleProtector protector;
  };

  class Checkpoint {
   public:
    Checkpoint(Thread* t):
      t(t),
      next(t->checkpoint),
      resource(t->resource),
      protector(t->protector),
      noThrow(false)
    {
      t->checkpoint = this;
    }

    ~Checkpoint() {
      t->checkpoint = next;
    }

    virtual void NO_RETURN unwind() = 0;

    Thread* t;
    Checkpoint* next;
    Resource* resource;
    Protector* protector;
    bool noThrow;
  };

  class RunCheckpoint: public Checkpoint {
   public:
    RunCheckpoint(Thread* t):
      Checkpoint(t),
      stack(0)
    { }

    virtual void unwind() {
      void* stack = this->stack;
      this->stack = 0;
      expect(t->m->system, stack);
      vmJump(voidPointer(vmRun_returnAddress), 0, stack, t, 0, 0);
    }

    void* stack;
  };

  class Runnable: public System::Runnable {
   public:
    Runnable(Thread* t): t(t) { }

    virtual void attach(System::Thread* st) {
      t->systemThread = st;
    }

    virtual void run() {
      enterActiveState(t);

      vm::run(t, runThread, 0);

      if (t->exception and t->exception != root(t, Machine::Shutdown)) {
        printTrace(t, t->exception);
      }

      t->exit();
    }

    virtual bool interrupted() {
      return t->javaThread and threadInterrupted(t, t->javaThread);
    }

    virtual void setInterrupted(bool v) {
      threadInterrupted(t, t->javaThread) = v;
    }

    Thread* t;
  };

  Thread(Machine* m, object javaThread, Thread* parent);

  void init();
  void exit();
  void dispose();

  JNIEnvVTable* vtable;
  Machine* m;
  Thread* parent;
  Thread* peer;
  Thread* child;
  Thread* waitNext;
  State state;
  unsigned criticalLevel;
  System::Thread* systemThread;
  System::Monitor* lock;
  object javaThread;
  object exception;
  unsigned heapIndex;
  unsigned heapOffset;
  Protector* protector;
  ClassInitStack* classInitStack;
  Resource* resource;
  Checkpoint* checkpoint;
  Runnable runnable;
  uintptr_t* defaultHeap;
  uintptr_t* heap;
  uintptr_t backupHeap[ThreadBackupHeapSizeInWords];
  unsigned backupHeapIndex;
  unsigned flags;
};

class Classpath {
 public:
  virtual object
  makeJclass(Thread* t, object class_) = 0;

  virtual object
  makeString(Thread* t, object array, int32_t offset, int32_t length) = 0;

  virtual object
  makeThread(Thread* t, Thread* parent) = 0;

  virtual void
  runThread(Thread* t) = 0;

  virtual void
  resolveNative(Thread* t, object method) = 0;

  virtual void
  boot(Thread* t) = 0;

  virtual const char*
  bootClasspath() = 0;

  virtual void
  dispose() = 0;
};

#ifdef _MSC_VER

template <class T>
class ThreadRuntimeArray: public Thread::Resource {
 public:
  ThreadRuntimeArray(Thread* t, unsigned size):
    Resource(t),
    body(static_cast<T*>(t->m->heap->allocate(size * sizeof(T)))),
    size(size)
  { }

  ~ThreadRuntimeArray() {
    t->m->heap->free(body, size * sizeof(T));
  }

  virtual void release() {
    ThreadRuntimeArray::~ThreadRuntimeArray();
  }

  T* body;
  unsigned size;
};

#  define THREAD_RUNTIME_ARRAY(thread, type, name, size)        \
  ThreadRuntimeArray<type> name(thread, size);

#else // not _MSC_VER

#  define THREAD_RUNTIME_ARRAY(thread, type, name, size) type name[size];

#endif // not _MSC_VER

Classpath*
makeClasspath(System* system, Allocator* allocator, const char* javaHome,
              const char* embedPrefix);

typedef uint64_t (JNICALL *FastNativeFunction)(Thread*, object, uintptr_t*);

inline object
objectClass(Thread*, object o)
{
  return mask(cast<object>(o, 0));
}

void
enter(Thread* t, Thread::State state);

inline void
enterActiveState(Thread* t)
{
  enter(t, Thread::ActiveState);
}

class StateResource: public Thread::Resource {
 public:
  StateResource(Thread* t, Thread::State state):
    Resource(t), oldState(t->state)
  {
    enter(t, state);
  }

  ~StateResource() { enter(t, oldState); }

  virtual void release() {
    this->StateResource::~StateResource();
  }

 private:
  Thread::State oldState;
};

inline void
dispose(Thread* t, Reference* r)
{
  *(r->handle) = r->next;
  if (r->next) {
    r->next->handle = r->handle;
  }
  t->m->heap->free(r, sizeof(*r));
}

inline void
acquire(Thread*, Reference* r)
{
  ++ r->count;
}

inline void
release(Thread* t, Reference* r)
{
  if ((-- r->count) == 0) {
    dispose(t, r);
  }
}

void
collect(Thread* t, Heap::CollectionType type);

void
shutDown(Thread* t);

#ifdef VM_STRESS

inline void
stress(Thread* t)
{
  if ((not t->m->unsafe)
      and (t->flags & (Thread::StressFlag | Thread::TracingFlag)) == 0
      and t->state != Thread::NoState
      and t->state != Thread::IdleState)
  {
    atomicOr(&(t->flags), Thread::StressFlag);

#  ifdef VM_STRESS_MAJOR
    collect(t, Heap::MajorCollection);
#  else // not VM_STRESS_MAJOR
    collect(t, Heap::MinorCollection);
#  endif // not VM_STRESS_MAJOR

    atomicAnd(&(t->flags), ~Thread::StressFlag);
  }
}

#endif // not VM_STRESS

inline void
acquire(Thread* t, System::Monitor* m)
{
  if (not m->tryAcquire(t->systemThread)) {
    ENTER(t, Thread::IdleState);
    m->acquire(t->systemThread);
  }

  stress(t);
}

inline void
release(Thread* t, System::Monitor* m)
{
  m->release(t->systemThread);
}

class MonitorResource: public Thread::Resource {
 public:
  MonitorResource(Thread* t, System::Monitor* m):
    Resource(t), m(m)
  {
    acquire(t, m);
  }

  ~MonitorResource() {
    vm::release(t, m);
  }

  virtual void release() {
    this->MonitorResource::~MonitorResource();
  }

 private:
  System::Monitor* m;
};

class RawMonitorResource: public Thread::Resource {
 public:
  RawMonitorResource(Thread* t, System::Monitor* m):
    Resource(t), m(m)
  {
    m->acquire(t->systemThread);
  }

  ~RawMonitorResource() {
    vm::release(t, m);
  }

  virtual void release() {
    this->RawMonitorResource::~RawMonitorResource();
  }

 private:
  System::Monitor* m;
};

inline void NO_RETURN
abort(Thread* t)
{
  abort(t->m->system);
}

#ifndef NDEBUG
inline void
assert(Thread* t, bool v)
{
  assert(t->m->system, v);
}
#endif // not NDEBUG

inline void
expect(Thread* t, bool v)
{
  expect(t->m->system, v);
}

class FixedAllocator: public Allocator {
 public:
  FixedAllocator(System* s, uint8_t* base, unsigned capacity):
    s(s), base(base), offset(0), capacity(capacity)
  { }

  virtual void* tryAllocate(unsigned) {
    abort(s);
  }

  void* allocate(unsigned size, unsigned padAlignment) {
    unsigned paddedSize = pad(size, padAlignment);
    expect(s, offset + paddedSize < capacity);

    void* p = base + offset;
    offset += paddedSize;
    return p;
  }

  virtual void* allocate(unsigned size) {
    return allocate(size, BytesPerWord);
  }

  virtual void free(const void* p, unsigned size) {
    if (p >= base and static_cast<const uint8_t*>(p) + size == base + offset) {
      offset -= size;
    } else {
      abort(s);
    }
  }

  System* s;
  uint8_t* base;
  unsigned offset;
  unsigned capacity;
};

inline bool
ensure(Thread* t, unsigned sizeInBytes)
{
  if (t->heapIndex + ceiling(sizeInBytes, BytesPerWord)
      > ThreadHeapSizeInWords)
  {
    if (sizeInBytes <= ThreadBackupHeapSizeInBytes) {
      expect(t, (t->flags & Thread::UseBackupHeapFlag) == 0);

      atomicOr(&(t->flags), Thread::UseBackupHeapFlag);

      return true;
    } else {
      return false;
    }
  } else {
    return true;
  }
}

object
allocate2(Thread* t, unsigned sizeInBytes, bool objectMask);

object
allocate3(Thread* t, Allocator* allocator, Machine::AllocationType type,
          unsigned sizeInBytes, bool objectMask);

inline object
allocateSmall(Thread* t, unsigned sizeInBytes)
{
  assert(t, t->heapIndex + ceiling(sizeInBytes, BytesPerWord)
         <= ThreadHeapSizeInWords);

  object o = reinterpret_cast<object>(t->heap + t->heapIndex);
  t->heapIndex += ceiling(sizeInBytes, BytesPerWord);
  return o;
}

inline object
allocate(Thread* t, unsigned sizeInBytes, bool objectMask)
{
  stress(t);

  if (UNLIKELY(t->heapIndex + ceiling(sizeInBytes, BytesPerWord)
               > ThreadHeapSizeInWords
               or t->m->exclusive))
  {
    return allocate2(t, sizeInBytes, objectMask);
  } else {
    return allocateSmall(t, sizeInBytes);
  }
}

inline void
mark(Thread* t, object o, unsigned offset, unsigned count)
{
  t->m->heap->mark(o, offset / BytesPerWord, count);
}

inline void
mark(Thread* t, object o, unsigned offset)
{
  t->m->heap->mark(o, offset / BytesPerWord, 1);
}

inline void
set(Thread* t, object target, unsigned offset, object value)
{
  cast<object>(target, offset) = value;
  mark(t, target, offset);
}

inline void
setObjectClass(Thread*, object o, object value)
{
  cast<object>(o, 0)
    = reinterpret_cast<object>
    (reinterpret_cast<intptr_alias_t>(value)
     | (reinterpret_cast<intptr_alias_t>
        (cast<object>(o, 0)) & (~PointerMask)));
}

inline const char*
findProperty(Machine* m, const char* name)
{
  for (unsigned i = 0; i < m->propertyCount; ++i) {
    const char* p = m->properties[i];
    const char* n = name;
    while (*p and *p != '=' and *n and *p == *n) {
      ++ p;
      ++ n;
    }
    if (*p == '=' and *n == 0) {
      return p + 1;
    }
  }
  return 0;
}

inline const char*
findProperty(Thread* t, const char* name)
{
  return findProperty(t->m, name);
}

object&
arrayBodyUnsafe(Thread*, object, unsigned);

bool
instanceOf(Thread* t, object class_, object o);

#include "type-declarations.cpp"

inline uint64_t
runRaw(Thread* t,
       uint64_t (*function)(Thread*, uintptr_t*), uintptr_t* arguments)
{
  Thread::RunCheckpoint checkpoint(t);
  return vmRun(function, arguments, &checkpoint);
}

inline uint64_t
run(Thread* t, uint64_t (*function)(Thread*, uintptr_t*), uintptr_t* arguments)
{
  ENTER(t, Thread::ActiveState);
  return runRaw(t, function, arguments);
}

inline void
runJavaThread(Thread* t)
{
  t->m->classpath->runThread(t);
}

void
runFinalizeThread(Thread* t);

inline uint64_t
runThread(Thread* t, uintptr_t*)
{
  t->m->localThread->set(t);

  checkDaemon(t);

  if (t == t->m->finalizeThread) {
    runFinalizeThread(t);
  } else if (t->javaThread) {
    runJavaThread(t);
  }

  return 1;
}

inline bool
startThread(Thread* t, Thread* p)
{
  return t->m->system->success(t->m->system->start(&(p->runnable)));
}

inline void
addThread(Thread* t, Thread* p)
{
  ACQUIRE_RAW(t, t->m->stateLock);

  assert(t, p->state == Thread::NoState);

  p->state = Thread::IdleState;
  ++ t->m->liveCount;

  p->peer = p->parent->child;
  p->parent->child = p;

  if (p->javaThread) {
    threadPeer(t, p->javaThread) = reinterpret_cast<jlong>(p);
  }
}

inline void
removeThread(Thread* t, Thread* p)
{
  ACQUIRE_RAW(t, t->m->stateLock);

  assert(t, p->state == Thread::IdleState);

  -- t->m->liveCount;

  t->m->stateLock->notifyAll(t->systemThread);

  p->parent->child = p->peer;

  if (p->javaThread) {
    threadPeer(t, p->javaThread) = 0;
  }
}

inline Thread*
startThread(Thread* t, object javaThread)
{
  Thread* p = t->m->processor->makeThread(t->m, javaThread, t);

  addThread(t, p);

  if (startThread(t, p)) {
    return p;
  } else {
    removeThread(t, p);
    return 0;
  }
}

inline void
registerDaemon(Thread* t)
{
  ACQUIRE_RAW(t, t->m->stateLock);

  atomicOr(&(t->flags), Thread::DaemonFlag);

  ++ t->m->daemonCount;
        
  t->m->stateLock->notifyAll(t->systemThread);
}

inline void
checkDaemon(Thread* t)
{
  if (threadDaemon(t, t->javaThread)) {
    registerDaemon(t);
  }
}

inline uint64_t
initAttachedThread(Thread* t, uintptr_t* arguments)
{
  bool daemon = arguments[0];

  t->javaThread = t->m->classpath->makeThread(t, t->m->rootThread);

  threadPeer(t, t->javaThread) = reinterpret_cast<jlong>(t);

  if (daemon) {
    threadDaemon(t, t->javaThread) = true;

    registerDaemon(t);
  }

  t->m->localThread->set(t);

  return 1;
}

inline Thread*
attachThread(Machine* m, bool daemon)
{
  Thread* t = m->processor->makeThread(m, 0, m->rootThread);
  m->system->attach(&(t->runnable));

  addThread(t, t);

  enter(t, Thread::ActiveState);

  uintptr_t arguments[] = { daemon };

  if (run(t, initAttachedThread, arguments)) {
    enter(t, Thread::IdleState);
    return t;
  } else {
    t->exit();
    return 0;
  }
}

inline object&
root(Thread* t, Machine::Root root)
{
  return arrayBody(t, t->m->roots, root);
}

inline void
setRoot(Thread* t, Machine::Root root, object value)
{
  set(t, t->m->roots, ArrayBody + (root * BytesPerWord), value);
}

inline object
type(Thread* t, Machine::Type type)
{
  return arrayBody(t, t->m->types, type);
}

inline void
setType(Thread* t, Machine::Type type, object value)
{
  set(t, t->m->types, ArrayBody + (type * BytesPerWord), value);
}

inline bool
objectFixed(Thread*, object o)
{
  return (alias(o, 0) & (~PointerMask)) == FixedMark;
}

inline bool
objectExtended(Thread*, object o)
{
  return (alias(o, 0) & (~PointerMask)) == ExtendedMark;
}

inline bool
hashTaken(Thread*, object o)
{
  return (alias(o, 0) & (~PointerMask)) == HashTakenMark;
}

inline unsigned
baseSize(Thread* t, object o, object class_)
{
  return ceiling(classFixedSize(t, class_), BytesPerWord)
    + ceiling(classArrayElementSize(t, class_)
              * cast<uintptr_t>(o, classFixedSize(t, class_) - BytesPerWord),
              BytesPerWord);
}

object
makeTrace(Thread* t, Processor::StackWalker* walker);

object
makeTrace(Thread* t, Thread* target);

inline object
makeTrace(Thread* t)
{
  return makeTrace(t, t);
}

inline object
makeNew(Thread* t, object class_)
{
  assert(t, t->state == Thread::NoState or t->state == Thread::ActiveState);

  PROTECT(t, class_);
  unsigned sizeInBytes = pad(classFixedSize(t, class_));
  assert(t, sizeInBytes);
  object instance = allocate(t, sizeInBytes, classObjectMask(t, class_));
  setObjectClass(t, instance, class_);

  return instance;
}

object
makeNewGeneral(Thread* t, object class_);

inline object
make(Thread* t, object class_)
{
  if (UNLIKELY(classVmFlags(t, class_)
               & (WeakReferenceFlag | HasFinalizerFlag)))
  {
    return makeNewGeneral(t, class_);
  } else {
    return makeNew(t, class_);
  }
}

object
makeByteArray(Thread* t, const char* format, va_list a);

object
makeByteArray(Thread* t, const char* format, ...);

object
makeString(Thread* t, const char* format, ...);

int
stringUTFLength(Thread* t, object string, unsigned start, unsigned length);

inline int
stringUTFLength(Thread* t, object string)
{
  return stringUTFLength(t, string, 0, stringLength(t, string));
}

void
stringChars(Thread* t, object string, unsigned start, unsigned length,
            char* chars);

inline void
stringChars(Thread* t, object string, char* chars)
{
  stringChars(t, string, 0, stringLength(t, string), chars);
}

void
stringChars(Thread* t, object string, unsigned start, unsigned length,
            uint16_t* chars);

inline void
stringChars(Thread* t, object string, uint16_t* chars)
{
  stringChars(t, string, 0, stringLength(t, string), chars);
}

void
stringUTFChars(Thread* t, object string, unsigned start, unsigned length,
               char* chars, unsigned charsLength);

inline void
stringUTFChars(Thread* t, object string, char* chars, unsigned charsLength)
{
  stringUTFChars(t, string, 0, stringLength(t, string), chars, charsLength);  
}

bool
isAssignableFrom(Thread* t, object a, object b);

object
classInitializer(Thread* t, object class_);

object
frameMethod(Thread* t, int frame);

inline uintptr_t&
extendedWord(Thread* t UNUSED, object o, unsigned baseSize)
{
  assert(t, objectExtended(t, o));
  return cast<uintptr_t>(o, baseSize * BytesPerWord);
}

inline unsigned
extendedSize(Thread* t, object o, unsigned baseSize)
{
  return baseSize + objectExtended(t, o);
}

inline void
markHashTaken(Thread* t, object o)
{
  assert(t, not objectExtended(t, o));
  assert(t, not objectFixed(t, o));

  ACQUIRE_RAW(t, t->m->heapLock);

  alias(o, 0) |= HashTakenMark;
  t->m->heap->pad(o);
}

inline uint32_t
takeHash(Thread*, object o)
{
  // some broken code implicitly relies on System.identityHashCode
  // always returning a non-negative number (e.g. old versions of
  // com/sun/xml/bind/v2/util/CollisionCheckStack.hash), hence the "&
  // 0x7FFFFFFF":
  return (reinterpret_cast<uintptr_t>(o) / BytesPerWord) & 0x7FFFFFFF;
}

inline uint32_t
objectHash(Thread* t, object o)
{
  if (objectExtended(t, o)) {
    return extendedWord(t, o, baseSize(t, o, objectClass(t, o)));
  } else {
    if (not objectFixed(t, o)) {
      markHashTaken(t, o);
    }
    return takeHash(t, o);
  }
}

inline bool
objectEqual(Thread*, object a, object b)
{
  return a == b;
}

inline uint32_t
byteArrayHash(Thread* t, object array)
{
  return hash(&byteArrayBody(t, array, 0), byteArrayLength(t, array));
}

inline uint32_t
charArrayHash(Thread* t, object array)
{
  return hash(&charArrayBody(t, array, 0), charArrayLength(t, array));
}

inline bool
byteArrayEqual(Thread* t, object a, object b)
{
  return a == b or
    ((byteArrayLength(t, a) == byteArrayLength(t, b)) and
     memcmp(&byteArrayBody(t, a, 0), &byteArrayBody(t, b, 0),
            byteArrayLength(t, a)) == 0);
}

inline uint32_t
stringHash(Thread* t, object s)
{
  if (stringHashCode(t, s) == 0 and stringLength(t, s)) {
    object data = stringData(t, s);
    if (objectClass(t, data) == type(t, Machine::ByteArrayType)) {
      stringHashCode(t, s) = hash
        (&byteArrayBody(t, data, stringOffset(t, s)), stringLength(t, s));
    } else {
      stringHashCode(t, s) = hash
        (&charArrayBody(t, data, stringOffset(t, s)), stringLength(t, s));
    }
  }
  return stringHashCode(t, s);
}

inline uint16_t
stringCharAt(Thread* t, object s, int i)
{
  object data = stringData(t, s);
  if (objectClass(t, data) == type(t, Machine::ByteArrayType)) {
    return byteArrayBody(t, data, stringOffset(t, s) + i);
  } else {
    return charArrayBody(t, data, stringOffset(t, s) + i);
  }
}

inline bool
stringEqual(Thread* t, object a, object b)
{
  if (a == b) {
    return true;
  } else if (stringLength(t, a) == stringLength(t, b)) {
    for (unsigned i = 0; i < stringLength(t, a); ++i) {
      if (stringCharAt(t, a, i) != stringCharAt(t, b, i)) {
        return false;
      }
    }
    return true;
  } else {
    return false;
  }
}

inline uint32_t
methodHash(Thread* t, object method)
{
  return byteArrayHash(t, methodName(t, method))
    ^ byteArrayHash(t, methodSpec(t, method));
}

inline bool
methodEqual(Thread* t, object a, object b)
{
  return a == b or
    (byteArrayEqual(t, methodName(t, a), methodName(t, b)) and
     byteArrayEqual(t, methodSpec(t, a), methodSpec(t, b)));
}

class MethodSpecIterator {
 public:
  MethodSpecIterator(Thread* t, const char* s):
    t(t), s(s + 1)
  { }

  const char* next() {
    assert(t, *s != ')');

    const char* p = s;

    switch (*s) {
    case 'L':
      while (*s and *s != ';') ++ s;
      ++ s;
      break;

    case '[':
      while (*s == '[') ++ s;
      switch (*s) {
      case 'L':
        while (*s and *s != ';') ++ s;
        ++ s;
        break;

      default:
        ++ s;
        break;
      }
      break;
      
    default:
      ++ s;
      break;
    }
    
    return p;
  }

  bool hasNext() {
    return *s != ')';
  }

  const char* returnSpec() {
    assert(t, *s == ')');
    return s + 1;
  }

  Thread* t;
  const char* s;
};

unsigned
fieldCode(Thread* t, unsigned javaCode);

unsigned
fieldType(Thread* t, unsigned code);

unsigned
primitiveSize(Thread* t, unsigned code);

inline unsigned
fieldSize(Thread* t, unsigned code)
{
  if (code == ObjectField) {
    return BytesPerWord;
  } else {
    return primitiveSize(t, code);
  }
}

inline unsigned
fieldSize(Thread* t, object field)
{
  return fieldSize(t, fieldCode(t, field));
}

inline void
scanMethodSpec(Thread* t, const char* s, unsigned* parameterCount,
               unsigned* returnCode)
{
  unsigned count = 0;
  MethodSpecIterator it(t, s);
  for (; it.hasNext(); it.next()) {
    ++ count;
  }

  *parameterCount = count;
  *returnCode = fieldCode(t, *it.returnSpec());
}

object
findLoadedClass(Thread* t, object loader, object spec);

inline bool
emptyMethod(Thread* t, object method)
{
  return ((methodFlags(t, method) & ACC_NATIVE) == 0)
    and (codeLength(t, methodCode(t, method)) == 1)
    and (codeBody(t, methodCode(t, method), 0) == return_);
}

object
parseUtf8(Thread* t, const char* data, unsigned length);

object
parseClass(Thread* t, object loader, const uint8_t* data, unsigned length,
           Machine::Type throwType = Machine::NoClassDefFoundErrorType);

object
resolveClass(Thread* t, object loader, object name, bool throw_ = true,
             Machine::Type throwType = Machine::NoClassDefFoundErrorType);

inline object
resolveClass(Thread* t, object loader, const char* name, bool throw_ = true,
             Machine::Type throwType = Machine::NoClassDefFoundErrorType)
{
  PROTECT(t, loader);
  object n = makeByteArray(t, "%s", name);
  return resolveClass(t, loader, n, throw_, throwType);
}

object
resolveSystemClass
(Thread* t, object loader, object name, bool throw_ = true,
 Machine::Type throwType = Machine::NoClassDefFoundErrorType);

inline object
resolveSystemClass(Thread* t, object loader, const char* name)
{
  return resolveSystemClass(t, loader, makeByteArray(t, "%s", name));
}

void
linkClass(Thread* t, object loader, object class_);

object
resolveMethod(Thread* t, object class_, const char* methodName,
              const char* methodSpec);

inline object
resolveMethod(Thread* t, object loader, const char* className,
              const char* methodName, const char* methodSpec)
{
  return resolveMethod
    (t, resolveClass(t, loader, className), methodName, methodSpec);
}

object
resolveField(Thread* t, object class_, const char* fieldName,
             const char* fieldSpec);

inline object
resolveField(Thread* t, object loader, const char* className,
             const char* fieldName, const char* fieldSpec)
{
  return resolveField
    (t, resolveClass(t, loader, className), fieldName, fieldSpec);
}

bool
classNeedsInit(Thread* t, object c);

bool
preInitClass(Thread* t, object c);

void
postInitClass(Thread* t, object c);

void
initClass(Thread* t, object c);

object
resolveObjectArrayClass(Thread* t, object loader, object elementClass);

object
makeObjectArray(Thread* t, object elementClass, unsigned count);

inline object
makeObjectArray(Thread* t, unsigned count)
{
  return makeObjectArray(t, type(t, Machine::JobjectType), count);
}

object
findInTable(Thread* t, object table, object name, object spec,
            object& (*getName)(Thread*, object),
            object& (*getSpec)(Thread*, object));

inline object
findFieldInClass(Thread* t, object class_, object name, object spec)
{
  return findInTable
    (t, classFieldTable(t, class_), name, spec, fieldName, fieldSpec);
}

inline object
findFieldInClass2(Thread* t, object class_, const char* name, const char* spec)
{
  PROTECT(t, class_);
  object n = makeByteArray(t, "%s", name);
  PROTECT(t, n);
  object s = makeByteArray(t, "%s", spec);
  return findFieldInClass(t, class_, n, s);
}

inline object
findMethodInClass(Thread* t, object class_, object name, object spec)
{
  return findInTable
    (t, classMethodTable(t, class_), name, spec, methodName, methodSpec);
}

inline object
makeThrowable
(Thread* t, Machine::Type type, object message = 0, object trace = 0,
 object cause = 0)
{
  PROTECT(t, message);
  PROTECT(t, trace);
  PROTECT(t, cause);
    
  if (trace == 0) {
    trace = makeTrace(t);
  }

  object result = make(t, vm::type(t, type));
    
  set(t, result, ThrowableMessage, message);
  set(t, result, ThrowableTrace, trace);
  set(t, result, ThrowableCause, cause);

  return result;
}

inline object
makeThrowable(Thread* t, Machine::Type type, const char* format, va_list a)
{
  object s = makeByteArray(t, format, a);

  object message = t->m->classpath->makeString
    (t, s, 0, byteArrayLength(t, s) - 1);

  return makeThrowable(t, type, message);
}

inline object
makeThrowable(Thread* t, Machine::Type type, const char* format, ...)
{
  va_list a;
  va_start(a, format);
  object r = makeThrowable(t, type, format, a);
  va_end(a);

  return r;
}

void
popResources(Thread* t);

inline void NO_RETURN
throw_(Thread* t, object e)
{
  assert(t, t->exception == 0);
  assert(t, e);

  expect(t, not t->checkpoint->noThrow);

  t->exception = e;

  // printTrace(t, e);

  popResources(t);

  t->checkpoint->unwind();

  abort(t);
}

inline void NO_RETURN
throwNew
(Thread* t, Machine::Type type, object message = 0, object trace = 0,
 object cause = 0)
{
  throw_(t, makeThrowable(t, type, message, trace, cause));
}

inline void NO_RETURN
throwNew(Thread* t, Machine::Type type, const char* format, ...)
{
  va_list a;
  va_start(a, format);
  object r = makeThrowable(t, type, format, a);
  va_end(a);

  throw_(t, r);
}

object
findInHierarchyOrNull(Thread* t, object class_, object name, object spec,
                      object (*find)(Thread*, object, object, object));

inline object
findInHierarchy(Thread* t, object class_, object name, object spec,
                object (*find)(Thread*, object, object, object),
                Machine::Type errorType, bool throw_ = true)
{
  object o = findInHierarchyOrNull(t, class_, name, spec, find);

  if (throw_ and o == 0) {
    throwNew(t, errorType, "%s %s not found in %s",
             &byteArrayBody(t, name, 0),
             &byteArrayBody(t, spec, 0),
             &byteArrayBody(t, className(t, class_), 0));
  }

  return o;
}

inline object
findMethod(Thread* t, object class_, object name, object spec)
{
  return findInHierarchy
    (t, class_, name, spec, findMethodInClass, Machine::NoSuchMethodErrorType);
}

inline object
findMethodOrNull(Thread* t, object class_, const char* name, const char* spec)
{
  PROTECT(t, class_);
  object n = makeByteArray(t, "%s", name);
  PROTECT(t, n);
  object s = makeByteArray(t, "%s", spec);
  return findInHierarchyOrNull(t, class_, n, s, findMethodInClass);
}

inline object
findVirtualMethod(Thread* t, object method, object class_)
{
  return arrayBody(t, classVirtualTable(t, class_), methodOffset(t, method));
}

inline object
findInterfaceMethod(Thread* t, object method, object class_)
{
  assert(t, (classVmFlags(t, class_) & BootstrapFlag) == 0);

  object interface = methodClass(t, method);
  object itable = classInterfaceTable(t, class_);
  for (unsigned i = 0; i < arrayLength(t, itable); i += 2) {
    if (arrayBody(t, itable, i) == interface) {
      return arrayBody
        (t, arrayBody(t, itable, i + 1), methodOffset(t, method));
    }
  }
  abort(t);
}

inline unsigned
objectArrayLength(Thread* t UNUSED, object array)
{
  assert(t, classFixedSize(t, objectClass(t, array)) == BytesPerWord * 2);
  assert(t, classArrayElementSize(t, objectClass(t, array)) == BytesPerWord);
  return cast<uintptr_t>(array, BytesPerWord);
}

inline object&
objectArrayBody(Thread* t UNUSED, object array, unsigned index)
{
  assert(t, classFixedSize(t, objectClass(t, array)) == BytesPerWord * 2);
  assert(t, classArrayElementSize(t, objectClass(t, array)) == BytesPerWord);
  assert(t, classObjectMask(t, objectClass(t, array))
         == classObjectMask(t, arrayBody
                            (t, t->m->types, Machine::ArrayType)));
  return cast<object>(array, ArrayBody + (index * BytesPerWord));
}

unsigned
parameterFootprint(Thread* t, const char* s, bool static_);

void
addFinalizer(Thread* t, object target, void (*finalize)(Thread*, object));

inline bool
zombified(Thread* t)
{
  return t->state == Thread::ZombieState
    or t->state == Thread::JoinedState;
}

inline bool
acquireSystem(Thread* t, Thread* target)
{
  ACQUIRE_RAW(t, t->m->stateLock);

  if (not zombified(target)) {
    atomicOr(&(target->flags), Thread::SystemFlag);
    return true;
  } else {
    return false;
  }
}

inline void
releaseSystem(Thread* t, Thread* target)
{
  ACQUIRE_RAW(t, t->m->stateLock);

  assert(t, not zombified(target));

  atomicAnd(&(target->flags), ~Thread::SystemFlag);
}

inline bool
atomicCompareAndSwapObject(Thread* t, object target, unsigned offset,
                           object old, object new_)
{
  if (atomicCompareAndSwap(&cast<uintptr_t>(target, offset),
                           reinterpret_cast<uintptr_t>(old),
                           reinterpret_cast<uintptr_t>(new_)))
  {
    mark(t, target, offset);
    return true;
  } else {
    return false;
  }
}

// The following two methods (monitorAtomicAppendAcquire and
// monitorAtomicPollAcquire) use the Michael and Scott Non-Blocking
// Queue Algorithm: http://www.cs.rochester.edu/u/michael/PODC96.html

inline void
monitorAtomicAppendAcquire(Thread* t, object monitor, object node)
{
  if (node == 0) {
    PROTECT(t, monitor);

    node = makeMonitorNode(t, t, 0);
  }

  while (true) {
    object tail = monitorAcquireTail(t, monitor);
    
    loadMemoryBarrier();

    object next = monitorNodeNext(t, tail);

    loadMemoryBarrier();

    if (tail == monitorAcquireTail(t, monitor)) {
      if (next) {
        atomicCompareAndSwapObject
          (t, monitor, MonitorAcquireTail, tail, next);
      } else if (atomicCompareAndSwapObject
                 (t, tail, MonitorNodeNext, 0, node))
      {
        atomicCompareAndSwapObject
          (t, monitor, MonitorAcquireTail, tail, node);
        return;
      }
    }
  }
}

inline Thread*
monitorAtomicPollAcquire(Thread* t, object monitor, bool remove)
{
  while (true) {
    object head = monitorAcquireHead(t, monitor);

    loadMemoryBarrier();

    object tail = monitorAcquireTail(t, monitor);

    loadMemoryBarrier();

    object next = monitorNodeNext(t, head);

    loadMemoryBarrier();

    if (head == monitorAcquireHead(t, monitor)) {
      if (head == tail) {
        if (next) {
          atomicCompareAndSwapObject
            (t, monitor, MonitorAcquireTail, tail, next);
        } else {
          return 0;
        }
      } else {
        Thread* value = static_cast<Thread*>(monitorNodeValue(t, next));
        if ((not remove)
            or atomicCompareAndSwapObject
            (t, monitor, MonitorAcquireHead, head, next))
        {
          return value;
        }
      }
    }
  }
}

inline bool
monitorTryAcquire(Thread* t, object monitor)
{
  if (monitorOwner(t, monitor) == t
      or (monitorAtomicPollAcquire(t, monitor, false) == 0
          and atomicCompareAndSwap
          (reinterpret_cast<uintptr_t*>(&monitorOwner(t, monitor)), 0,
           reinterpret_cast<uintptr_t>(t))))
  {
    ++ monitorDepth(t, monitor);
    return true;
  } else {
    return false;
  }
}

inline void
monitorAcquire(Thread* t, object monitor, object node = 0)
{
  if (not monitorTryAcquire(t, monitor)) {
    PROTECT(t, monitor);
    PROTECT(t, node);

    ACQUIRE(t, t->lock);

    monitorAtomicAppendAcquire(t, monitor, node);
    
    // note that we don't try to acquire the lock until we're first in
    // line, both because it's fair and because we don't support
    // removing elements from arbitrary positions in the queue

    while (not (t == monitorAtomicPollAcquire(t, monitor, false)
                and atomicCompareAndSwap
                (reinterpret_cast<uintptr_t*>(&monitorOwner(t, monitor)), 0,
                 reinterpret_cast<uintptr_t>(t))))
    {
      ENTER(t, Thread::IdleState);
      
      t->lock->wait(t->systemThread, 0);
    }

    expect(t, t == monitorAtomicPollAcquire(t, monitor, true));
        
    ++ monitorDepth(t, monitor);
  }

  assert(t, monitorOwner(t, monitor) == t);
}

inline void
monitorRelease(Thread* t, object monitor)
{
  expect(t, monitorOwner(t, monitor) == t);

  if (-- monitorDepth(t, monitor) == 0) {
    monitorOwner(t, monitor) = 0;

    storeLoadMemoryBarrier();
    
    Thread* next = monitorAtomicPollAcquire(t, monitor, false);

    if (next and acquireSystem(t, next)) {
      ACQUIRE(t, next->lock);
       
      next->lock->notify(t->systemThread);

      releaseSystem(t, next);
    }
  }
}

inline void
monitorAppendWait(Thread* t, object monitor)
{
  assert(t, monitorOwner(t, monitor) == t);

  expect(t, (t->flags & Thread::WaitingFlag) == 0);
  expect(t, t->waitNext == 0);

  atomicOr(&(t->flags), Thread::WaitingFlag);

  if (monitorWaitTail(t, monitor)) {
    static_cast<Thread*>(monitorWaitTail(t, monitor))->waitNext = t;
  } else {
    monitorWaitHead(t, monitor) = t;
  }

  monitorWaitTail(t, monitor) = t;
}

inline void
monitorRemoveWait(Thread* t, object monitor)
{
  assert(t, monitorOwner(t, monitor) == t);

  Thread* previous = 0;
  for (Thread* current = static_cast<Thread*>(monitorWaitHead(t, monitor));
       current; current = current->waitNext)
  {
    if (t == current) {
      if (t == monitorWaitHead(t, monitor)) {
        monitorWaitHead(t, monitor) = t->waitNext;
      } else {
        previous->waitNext = t->waitNext;
      }

      if (t == monitorWaitTail(t, monitor)) {
        assert(t, t->waitNext == 0);
        monitorWaitTail(t, monitor) = previous;
      }

      t->waitNext = 0;
      atomicAnd(&(t->flags), ~Thread::WaitingFlag);

      return;
    } else {
      previous = current;
    }
  }

  abort(t);
}

inline bool
monitorFindWait(Thread* t, object monitor)
{
  assert(t, monitorOwner(t, monitor) == t);

  for (Thread* current = static_cast<Thread*>(monitorWaitHead(t, monitor));
       current; current = current->waitNext)
  {
    if (t == current) {
      return true;
    }
  }

  return false;
}

inline bool
monitorWait(Thread* t, object monitor, int64_t time)
{
  expect(t, monitorOwner(t, monitor) == t);

  bool interrupted;
  unsigned depth;

  PROTECT(t, monitor);

  // pre-allocate monitor node so we don't get an OutOfMemoryError
  // when we try to re-acquire the monitor below
  object monitorNode = makeMonitorNode(t, t, 0);
  PROTECT(t, monitorNode);

  { ACQUIRE(t, t->lock);

    monitorAppendWait(t, monitor);

    depth = monitorDepth(t, monitor);
    monitorDepth(t, monitor) = 1;

    monitorRelease(t, monitor);

    ENTER(t, Thread::IdleState);

    interrupted = t->lock->waitAndClearInterrupted(t->systemThread, time);
  }

  monitorAcquire(t, monitor, monitorNode);

  monitorDepth(t, monitor) = depth;

  if (t->flags & Thread::WaitingFlag) {
    monitorRemoveWait(t, monitor);
  } else {
    expect(t, not monitorFindWait(t, monitor));
  }

  assert(t, monitorOwner(t, monitor) == t);

  return interrupted;
}

inline Thread*
monitorPollWait(Thread* t, object monitor)
{
  assert(t, monitorOwner(t, monitor) == t);

  Thread* next = static_cast<Thread*>(monitorWaitHead(t, monitor));

  if (next) {
    monitorWaitHead(t, monitor) = next->waitNext;
    atomicAnd(&(next->flags), ~Thread::WaitingFlag);
    next->waitNext = 0;
    if (next == monitorWaitTail(t, monitor)) {
      monitorWaitTail(t, monitor) = 0;
    }
  } else {
    assert(t, monitorWaitTail(t, monitor) == 0);
  }

  return next;
}

inline bool
monitorNotify(Thread* t, object monitor)
{
  expect(t, monitorOwner(t, monitor) == t);
  
  Thread* next = monitorPollWait(t, monitor);

  if (next) {
    ACQUIRE(t, next->lock);

    next->lock->notify(t->systemThread);

    return true;
  } else {
    return false;
  }
}

inline void
monitorNotifyAll(Thread* t, object monitor)
{
  PROTECT(t, monitor);

  while (monitorNotify(t, monitor)) { }
}

class ObjectMonitorResource {
 public:
  ObjectMonitorResource(Thread* t, object o): o(o), protector(t, &(this->o)) {
    monitorAcquire(protector.t, o);
  }

  ~ObjectMonitorResource() {
    monitorRelease(protector.t, o);
  }

 private:
  object o;
  Thread::SingleProtector protector;
};

object
objectMonitor(Thread* t, object o, bool createNew);

inline void
acquire(Thread* t, object o)
{
  unsigned hash;
  if (DebugMonitors) {
    hash = objectHash(t, o);
  }

  object m = objectMonitor(t, o, true);

  if (DebugMonitors) {
    fprintf(stderr, "thread %p acquires %p for %x\n", t, m, hash);
  }

  monitorAcquire(t, m);
}

inline void
release(Thread* t, object o)
{
  unsigned hash;
  if (DebugMonitors) {
    hash = objectHash(t, o);
  }

  object m = objectMonitor(t, o, false);

  if (DebugMonitors) {
    fprintf(stderr, "thread %p releases %p for %x\n", t, m, hash);
  }

  monitorRelease(t, m);
}

inline void
wait(Thread* t, object o, int64_t milliseconds)
{
  unsigned hash;
  if (DebugMonitors) {
    hash = objectHash(t, o);
  }

  object m = objectMonitor(t, o, false);

  if (DebugMonitors) {
    fprintf(stderr, "thread %p waits %d millis on %p for %x\n",
            t, static_cast<int>(milliseconds), m, hash);
  }

  if (m and monitorOwner(t, m) == t) {
    PROTECT(t, m);

    bool interrupted = monitorWait(t, m, milliseconds);

    if (interrupted) {
      if (t->m->alive or (t->flags & Thread::DaemonFlag) == 0) {
        throwNew(t, Machine::InterruptedExceptionType);
      } else {
        throw_(t, root(t, Machine::Shutdown));
      }
    }
  } else {
    throwNew(t, Machine::IllegalMonitorStateExceptionType);
  }

  if (DebugMonitors) {
    fprintf(stderr, "thread %p wakes up on %p for %x\n",
            t, m, hash);
  }

  stress(t);
}

inline void
notify(Thread* t, object o)
{
  unsigned hash;
  if (DebugMonitors) {
    hash = objectHash(t, o);
  }

  object m = objectMonitor(t, o, false);

  if (DebugMonitors) {
    fprintf(stderr, "thread %p notifies on %p for %x\n",
            t, m, hash);
  }

  if (m and monitorOwner(t, m) == t) {
    monitorNotify(t, m);
  } else {
    throwNew(t, Machine::IllegalMonitorStateExceptionType);
  }
}

inline void
notifyAll(Thread* t, object o)
{
  object m = objectMonitor(t, o, false);

  if (DebugMonitors) {
    fprintf(stderr, "thread %p notifies all on %p for %x\n",
            t, m, objectHash(t, o));
  }

  if (m and monitorOwner(t, m) == t) {
    monitorNotifyAll(t, m);
  } else {
    throwNew(t, Machine::IllegalMonitorStateExceptionType);
  }
}

inline void
interrupt(Thread* t, Thread* target)
{
  if (acquireSystem(t, target)) {
    target->systemThread->interrupt();
    releaseSystem(t, target);
  }
}

inline bool
getAndClearInterrupted(Thread* t, Thread* target)
{
  if (acquireSystem(t, target)) {
    bool result = target->systemThread->getAndClearInterrupted();
    releaseSystem(t, target);
    return result;
  } else {
    return false;
  }
}

inline bool
exceptionMatch(Thread* t, object type, object exception)
{
  return type == 0
    or (exception != root(t, Machine::Shutdown)
        and instanceOf(t, type, t->exception));
}

object
intern(Thread* t, object s);

void
walk(Thread* t, Heap::Walker* w, object o, unsigned start);

int
walkNext(Thread* t, object o, int previous);

void
visitRoots(Machine* m, Heap::Visitor* v);

inline jobject
makeLocalReference(Thread* t, object o)
{
  return t->m->processor->makeLocalReference(t, o);
}

inline void
disposeLocalReference(Thread* t, jobject r)
{
  t->m->processor->disposeLocalReference(t, r);
}

inline bool
methodVirtual(Thread* t, object method)
{
  return (methodFlags(t, method) & (ACC_STATIC | ACC_PRIVATE)) == 0
    and byteArrayBody(t, methodName(t, method), 0) != '<';
}

inline unsigned
singletonMaskSize(unsigned count, unsigned bitsPerWord)
{
  if (count) {
    return ceiling(count + 2, bitsPerWord);
  }
  return 0;
}

inline unsigned
singletonMaskSize(unsigned count)
{
  return singletonMaskSize(count, BitsPerWord);
}

inline unsigned
singletonMaskSize(Thread* t, object singleton)
{
  unsigned length = singletonLength(t, singleton);
  if (length) {
    return ceiling(length + 2, BitsPerWord + 1);
  }
  return 0;
}

inline unsigned
singletonCount(Thread* t, object singleton)
{
  return singletonLength(t, singleton) - singletonMaskSize(t, singleton);
}

inline uint32_t*
singletonMask(Thread* t, object singleton)
{
  assert(t, singletonLength(t, singleton));
  return reinterpret_cast<uint32_t*>
    (&singletonBody(t, singleton, singletonCount(t, singleton)));
}

inline void
singletonMarkObject(uint32_t* mask, unsigned index)
{
  mask[(index + 2) / 32]
    |= (static_cast<uint32_t>(1) << ((index + 2) % 32));
}

inline void
singletonMarkObject(Thread* t, object singleton, unsigned index)
{
  singletonMarkObject(singletonMask(t, singleton), index);
}

inline bool
singletonIsObject(Thread* t, object singleton, unsigned index)
{
  assert(t, index < singletonCount(t, singleton));

  return (singletonMask(t, singleton)[(index + 2) / 32]
          & (static_cast<uint32_t>(1) << ((index + 2) % 32))) != 0;
}

inline object&
singletonObject(Thread* t, object singleton, unsigned index)
{
  assert(t, singletonIsObject(t, singleton, index));
  return reinterpret_cast<object&>(singletonBody(t, singleton, index));
}

inline uintptr_t&
singletonValue(Thread* t, object singleton, unsigned index)
{
  assert(t, not singletonIsObject(t, singleton, index));
  return singletonBody(t, singleton, index);
}

inline object
makeSingletonOfSize(Thread* t, unsigned count)
{
  object o = makeSingleton(t, count + singletonMaskSize(count));
  assert(t, singletonLength(t, o) == count + singletonMaskSize(t, o));
  if (count) {
    singletonMask(t, o)[0] = 1;
  }
  return o;
}

inline void
singletonSetBit(Thread* t, object singleton, unsigned start, unsigned index)
{
  singletonValue(t, singleton, start + (index / BitsPerWord))
    |= static_cast<uintptr_t>(1) << (index % BitsPerWord);
}

inline bool
singletonBit(Thread* t, object singleton, unsigned start, unsigned index)
{
  return (singletonValue(t, singleton, start + (index / BitsPerWord))
          & (static_cast<uintptr_t>(1) << (index % BitsPerWord))) != 0;
}

inline unsigned
poolMaskSize(unsigned count, unsigned bitsPerWord)
{
  return ceiling(count, bitsPerWord);
}

inline unsigned
poolMaskSize(unsigned count)
{
  return poolMaskSize(count, BitsPerWord);
}

inline unsigned
poolMaskSize(Thread* t, object pool)
{
  return ceiling(singletonCount(t, pool), BitsPerWord + 1);
}

inline unsigned
poolSize(Thread* t, object pool)
{
  return singletonCount(t, pool) - poolMaskSize(t, pool);
}

inline object
resolveClassInObject(Thread* t, object loader, object container,
                     unsigned classOffset, bool throw_ = true)
{
  object o = cast<object>(container, classOffset);

  loadMemoryBarrier();  

  if (objectClass(t, o) == type(t, Machine::ByteArrayType)) {
    PROTECT(t, container);

    o = resolveClass(t, loader, o, throw_);
    
    if (o) {
      storeStoreMemoryBarrier();

      set(t, container, classOffset, o);
    }
  }
  return o; 
}

inline object
resolveClassInPool(Thread* t, object loader, object method, unsigned index,
                   bool throw_ = true)
{
  object o = singletonObject(t, codePool(t, methodCode(t, method)), index);

  loadMemoryBarrier();

  if (objectClass(t, o) == type(t, Machine::ReferenceType)) {
    PROTECT(t, method);

    o = resolveClass(t, loader, referenceName(t, o), throw_);
    
    if (o) {
      storeStoreMemoryBarrier();

      set(t, codePool(t, methodCode(t, method)),
          SingletonBody + (index * BytesPerWord), o);
    }
  }
  return o; 
}

inline object
resolveClassInPool(Thread* t, object method, unsigned index,
                   bool throw_ = true)
{
  return resolveClassInPool(t, classLoader(t, methodClass(t, method)),
                            method, index, throw_);
}

inline object
resolve(Thread* t, object loader, object method, unsigned index,
        object (*find)(vm::Thread*, object, object, object),
        Machine::Type errorType, bool throw_ = true)
{
  object o = singletonObject(t, codePool(t, methodCode(t, method)), index);

  loadMemoryBarrier();  

  if (objectClass(t, o) == type(t, Machine::ReferenceType)) {
    PROTECT(t, method);

    object reference = o;
    PROTECT(t, reference);

    object class_ = resolveClassInObject(t, loader, o, ReferenceClass, throw_);
    
    if (class_) {
      o = findInHierarchy
        (t, class_, referenceName(t, reference), referenceSpec(t, reference),
         find, errorType, throw_);
    
      if (o) {
        storeStoreMemoryBarrier();

        set(t, codePool(t, methodCode(t, method)),
            SingletonBody + (index * BytesPerWord), o);
      }
    } else {
      o = 0;
    }
  }

  return o;
}

inline object
resolveField(Thread* t, object loader, object method, unsigned index,
             bool throw_ = true)
{
  return resolve(t, loader, method, index, findFieldInClass,
                 Machine::NoSuchFieldErrorType, throw_);
}

inline object
resolveField(Thread* t, object method, unsigned index, bool throw_ = true)
{
  return resolveField
    (t, classLoader(t, methodClass(t, method)), method, index, throw_);
}

inline void
acquireFieldForRead(Thread* t, object field)
{
  if (UNLIKELY((fieldFlags(t, field) & ACC_VOLATILE)
               and BytesPerWord == 4
               and (fieldCode(t, field) == DoubleField
                    or fieldCode(t, field) == LongField)))
  {
    acquire(t, field);        
  }
}

inline void
releaseFieldForRead(Thread* t, object field)
{
  if (UNLIKELY(fieldFlags(t, field) & ACC_VOLATILE)) {
    if (BytesPerWord == 4
        and (fieldCode(t, field) == DoubleField
             or fieldCode(t, field) == LongField))
    {
      release(t, field);        
    } else {
      loadMemoryBarrier();
    }
  }
}

class FieldReadResource {
 public:
  FieldReadResource(Thread* t, object o): o(o), protector(t, &(this->o)) {
    acquireFieldForRead(protector.t, o);
  }

  ~FieldReadResource() {
    releaseFieldForRead(protector.t, o);
  }

 private:
  object o;
  Thread::SingleProtector protector;
};

inline void
acquireFieldForWrite(Thread* t, object field)
{
  if (UNLIKELY(fieldFlags(t, field) & ACC_VOLATILE)) {
    if (BytesPerWord == 4
        and (fieldCode(t, field) == DoubleField
             or fieldCode(t, field) == LongField))
    {
      acquire(t, field);        
    } else {
      storeStoreMemoryBarrier();
    }
  }
}

inline void
releaseFieldForWrite(Thread* t, object field)
{
  if (UNLIKELY(fieldFlags(t, field) & ACC_VOLATILE)) {
    if (BytesPerWord == 4
        and (fieldCode(t, field) == DoubleField
             or fieldCode(t, field) == LongField))
    {
      release(t, field);        
    } else {
      storeLoadMemoryBarrier();
    }
  }
}

class FieldWriteResource {
 public:
  FieldWriteResource(Thread* t, object o): o(o), protector(t, &(this->o)) {
    acquireFieldForWrite(protector.t, o);
  }

  ~FieldWriteResource() {
    releaseFieldForWrite(protector.t, o);
  }

 private:
  object o;
  Thread::SingleProtector protector;
};

inline object
resolveMethod(Thread* t, object loader, object method, unsigned index,
                   bool throw_ = true)
{
  return resolve(t, loader, method, index, findMethodInClass,
                 Machine::NoSuchMethodErrorType, throw_);
}

inline object
resolveMethod(Thread* t, object method, unsigned index, bool throw_ = true)
{
  return resolveMethod
    (t, classLoader(t, methodClass(t, method)), method, index, throw_);
}

object
vectorAppend(Thread*, object, object);

inline object
getClassRuntimeDataIfExists(Thread* t, object c)
{
  if (classRuntimeDataIndex(t, c)) {
    return vectorBody(t, root(t, Machine::ClassRuntimeDataTable),
                      classRuntimeDataIndex(t, c) - 1);
  } else {
    return 0;
  }
}

inline object
getClassRuntimeData(Thread* t, object c)
{
  if (classRuntimeDataIndex(t, c) == 0) {
    PROTECT(t, c);

    ACQUIRE(t, t->m->classLock);

    if (classRuntimeDataIndex(t, c) == 0) {
      object runtimeData = makeClassRuntimeData(t, 0, 0, 0, 0);

      setRoot(t, Machine::ClassRuntimeDataTable, vectorAppend
              (t, root(t, Machine::ClassRuntimeDataTable), runtimeData));

      classRuntimeDataIndex(t, c) = vectorSize
        (t, root(t, Machine::ClassRuntimeDataTable));
    }
  }

  return vectorBody(t, root(t, Machine::ClassRuntimeDataTable),
                    classRuntimeDataIndex(t, c) - 1);
}

inline object
getMethodRuntimeData(Thread* t, object method)
{
  int index = methodRuntimeDataIndex(t, method);

  loadMemoryBarrier();

  if (index == 0) {
    PROTECT(t, method);

    ACQUIRE(t, t->m->classLock);

    if (methodRuntimeDataIndex(t, method) == 0) {
      object runtimeData = makeMethodRuntimeData(t, 0);

      setRoot(t, Machine::MethodRuntimeDataTable, vectorAppend
              (t, root(t, Machine::MethodRuntimeDataTable), runtimeData));

      storeStoreMemoryBarrier();

      methodRuntimeDataIndex(t, method) = vectorSize
        (t, root(t, Machine::MethodRuntimeDataTable));
    }
  }

  return vectorBody(t, root(t, Machine::MethodRuntimeDataTable),
                    methodRuntimeDataIndex(t, method) - 1);
}

inline object
getJClass(Thread* t, object c)
{
  PROTECT(t, c);

  object jclass = classRuntimeDataJclass(t, getClassRuntimeData(t, c));

  loadMemoryBarrier();

  if (jclass == 0) {
    ACQUIRE(t, t->m->classLock);

    jclass = classRuntimeDataJclass(t, getClassRuntimeData(t, c));
    if (jclass == 0) {
      jclass = t->m->classpath->makeJclass(t, c);

      storeStoreMemoryBarrier();
      
      set(t, getClassRuntimeData(t, c), ClassRuntimeDataJclass, jclass);
    }
  }

  return jclass;
}

inline object
primitiveClass(Thread* t, char name)
{
  switch (name) {
  case 'B': return type(t, Machine::JbyteType);
  case 'C': return type(t, Machine::JcharType);
  case 'D': return type(t, Machine::JdoubleType);
  case 'F': return type(t, Machine::JfloatType);
  case 'I': return type(t, Machine::JintType);
  case 'J': return type(t, Machine::JlongType);
  case 'S': return type(t, Machine::JshortType);
  case 'V': return type(t, Machine::JvoidType);
  case 'Z': return type(t, Machine::JbooleanType);
  default: throwNew(t, Machine::IllegalArgumentExceptionType);
  }
}
  
inline void
registerNative(Thread* t, object method, void* function)
{
  PROTECT(t, method);

  expect(t, methodFlags(t, method) & ACC_NATIVE);

  object native = makeNative(t, function, false);
  PROTECT(t, native);

  object runtimeData = getMethodRuntimeData(t, method);

  // ensure other threads only see the methodRuntimeDataNative field
  // populated once the object it points to has been populated:
  storeStoreMemoryBarrier();

  set(t, runtimeData, MethodRuntimeDataNative, native);
}

inline void
unregisterNatives(Thread* t, object c)
{
  if (classMethodTable(t, c)) {
    for (unsigned i = 0; i < arrayLength(t, classMethodTable(t, c)); ++i) {
      object method = arrayBody(t, classMethodTable(t, c), i);
      if (methodFlags(t, method) & ACC_NATIVE) {
        set(t, getMethodRuntimeData(t, method), MethodRuntimeDataNative, 0);
      }
    }
  }
}

void
populateMultiArray(Thread* t, object array, int32_t* counts,
                   unsigned index, unsigned dimensions);

object
getCaller(Thread* t, unsigned target);

object
defineClass(Thread* t, object loader, const uint8_t* buffer, unsigned length);

void
dumpHeap(Thread* t, FILE* out);

inline object
methodClone(Thread* t, object method)
{
  return makeMethod
    (t, methodVmFlags(t, method),
     methodReturnCode(t, method),
     methodParameterCount(t, method),
     methodParameterFootprint(t, method),
     methodFlags(t, method),
     methodOffset(t, method),
     methodNativeID(t, method),
     methodRuntimeDataIndex(t, method),
     methodName(t, method),
     methodSpec(t, method),
     methodAddendum(t, method),
     methodClass(t, method),
     methodCode(t, method));
}

inline uint64_t
exceptionHandler(uint64_t start, uint64_t end, uint64_t ip, uint64_t catchType)
{
  return (start << 48) | (end << 32) | (ip << 16) | catchType;
}

inline unsigned
exceptionHandlerStart(uint64_t eh)
{
  return eh >> 48;
}

inline unsigned
exceptionHandlerEnd(uint64_t eh)
{
  return (eh >> 32) & 0xFFFF;
}

inline unsigned
exceptionHandlerIp(uint64_t eh)
{
  return (eh >> 16) & 0xFFFF;
}

inline unsigned
exceptionHandlerCatchType(uint64_t eh)
{
  return eh & 0xFFFF;
}

inline uint64_t
lineNumber(uint64_t ip, uint64_t line)
{
  return (ip << 32) | line;
}

inline unsigned
lineNumberIp(uint64_t ln)
{
  return ln >> 32;
}

inline unsigned
lineNumberLine(uint64_t ln)
{
  return ln & 0xFFFFFFFF;
}

inline FILE*
errorLog(Thread* t)
{
  if (t->m->errorLog == 0) {
    const char* path = findProperty(t, "avian.error.log");
    if (path) {
      t->m->errorLog = vm::fopen(path, "wb");
    } else {
      t->m->errorLog = stderr;
    }
  }

  return t->m->errorLog;
}

} // namespace vm

void
vmPrintTrace(vm::Thread* t);

void*
vmAddressFromLine(vm::Thread* t, vm::object m, unsigned line);

#endif//MACHINE_H
