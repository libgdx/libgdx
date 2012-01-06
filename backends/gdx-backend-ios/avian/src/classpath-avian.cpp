/* Copyright (c) 2010-2011, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

#include "machine.h"
#include "classpath-common.h"
#include "process.h"

using namespace vm;

namespace {

namespace local {

class MyClasspath : public Classpath {
 public:
  MyClasspath(Allocator* allocator):
    allocator(allocator)
  { }

  virtual object
  makeJclass(Thread* t, object class_)
  {
    return vm::makeJclass(t, class_);
  }

  virtual object
  makeString(Thread* t, object array, int32_t offset, int32_t length)
  {
    return vm::makeString(t, array, offset, length, 0);
  }

  virtual object
  makeThread(Thread* t, Thread* parent)
  {
    object group;
    if (parent) {
      group = threadGroup(t, parent->javaThread);
    } else {
      group = makeThreadGroup(t, 0, 0, 0);
    }

    const unsigned NewState = 0;
    const unsigned NormalPriority = 5;

    return vm::makeThread
      (t, 0, 0, 0, 0, NewState, NormalPriority, 0, 0, 0,
       root(t, Machine::BootLoader), 0, 0, group, 0);
  }

  virtual void
  runThread(Thread* t)
  {
    object method = resolveMethod
      (t, root(t, Machine::BootLoader), "java/lang/Thread", "run",
       "(Ljava/lang/Thread;)V");

    t->m->processor->invoke(t, method, 0, t->javaThread);
  }

  virtual void
  resolveNative(Thread* t, object method)
  {
    vm::resolveNative(t, method);
  }

  virtual void
  boot(Thread*)
  {
    // ignore
  }

  virtual const char*
  bootClasspath()
  {
    return AVIAN_CLASSPATH;
  }

  virtual void
  dispose()
  {
    allocator->free(this, sizeof(*this));
  }

  Allocator* allocator;
};

void
enumerateThreads(Thread* t, Thread* x, object array, unsigned* index,
                 unsigned limit)
{
  if (*index < limit) {
    set(t, array, ArrayBody + (*index * BytesPerWord), x->javaThread);
    ++ (*index);

    if (x->peer) enumerateThreads(t, x->peer, array, index, limit);
    
    if (x->child) enumerateThreads(t, x->child, array, index, limit);
  }
}

} // namespace local

} // namespace

namespace vm {

Classpath*
makeClasspath(System*, Allocator* allocator, const char*, const char*)
{
  return new (allocator->allocate(sizeof(local::MyClasspath)))
    local::MyClasspath(allocator);
}

} // namespace vm

extern "C" JNIEXPORT int64_t JNICALL
Avian_java_lang_Object_toString
(Thread* t, object, uintptr_t* arguments)
{
  object this_ = reinterpret_cast<object>(arguments[0]);

  unsigned hash = objectHash(t, this_);
  object s = makeString
    (t, "%s@0x%x",
     &byteArrayBody(t, className(t, objectClass(t, this_)), 0),
     hash);

  return reinterpret_cast<int64_t>(s);
}

extern "C" JNIEXPORT int64_t JNICALL
Avian_java_lang_Object_getVMClass
(Thread* t, object, uintptr_t* arguments)
{
  return reinterpret_cast<int64_t>
    (objectClass(t, reinterpret_cast<object>(arguments[0])));
}

extern "C" JNIEXPORT void JNICALL
Avian_java_lang_Object_wait
(Thread* t, object, uintptr_t* arguments)
{
  object this_ = reinterpret_cast<object>(arguments[0]);
  int64_t milliseconds; memcpy(&milliseconds, arguments + 1, 8);

  vm::wait(t, this_, milliseconds);
}

extern "C" JNIEXPORT void JNICALL
Avian_java_lang_Object_notify
(Thread* t, object, uintptr_t* arguments)
{
  object this_ = reinterpret_cast<object>(arguments[0]);

  notify(t, this_);
}

extern "C" JNIEXPORT void JNICALL
Avian_java_lang_Object_notifyAll
(Thread* t, object, uintptr_t* arguments)
{
  object this_ = reinterpret_cast<object>(arguments[0]);

  notifyAll(t, this_);
}

extern "C" JNIEXPORT int64_t JNICALL
Avian_java_lang_Object_hashCode
(Thread* t, object, uintptr_t* arguments)
{
  object this_ = reinterpret_cast<object>(arguments[0]);

  return objectHash(t, this_);
}

extern "C" JNIEXPORT int64_t JNICALL
Avian_java_lang_Object_clone
(Thread* t, object, uintptr_t* arguments)
{
  return reinterpret_cast<int64_t>
    (clone(t, reinterpret_cast<object>(arguments[0])));
}

extern "C" JNIEXPORT int64_t JNICALL
Avian_java_io_ObjectInputStream_makeInstance
(Thread* t, object, uintptr_t* arguments)
{
  object c = reinterpret_cast<object>(arguments[0]);

  return reinterpret_cast<int64_t>(make(t, c));
}

extern "C" JNIEXPORT int64_t JNICALL
Avian_java_lang_reflect_Field_getPrimitive
(Thread* t, object, uintptr_t* arguments)
{
  object instance = reinterpret_cast<object>(arguments[0]);
  int code = arguments[1];
  int offset = arguments[2];

  switch (code) {
  case ByteField: 
    return cast<int8_t>(instance, offset);
  case BooleanField: 
    return cast<uint8_t>(instance, offset);
  case CharField: 
    return cast<uint16_t>(instance, offset);
  case ShortField: 
    return cast<int16_t>(instance, offset);
  case IntField: 
    return cast<int32_t>(instance, offset);
  case LongField: 
    return cast<int64_t>(instance, offset);
  case FloatField: 
    return cast<uint32_t>(instance, offset);
  case DoubleField: 
    return cast<uint64_t>(instance, offset);
  default:
    abort(t);
  }
}

extern "C" JNIEXPORT int64_t JNICALL
Avian_java_lang_reflect_Field_getObject
(Thread*, object, uintptr_t* arguments)
{
  object instance = reinterpret_cast<object>(arguments[0]);
  int offset = arguments[1];

  return reinterpret_cast<int64_t>(cast<object>(instance, offset));
}

extern "C" JNIEXPORT void JNICALL
Avian_java_lang_reflect_Field_setPrimitive
(Thread* t, object, uintptr_t* arguments)
{
  object instance = reinterpret_cast<object>(arguments[0]);
  int code = arguments[1];
  int offset = arguments[2];
  int64_t value; memcpy(&value, arguments + 3, 8);

  switch (code) {
  case ByteField:
    cast<int8_t>(instance, offset) = static_cast<int8_t>(value);
    break;
  case BooleanField:
    cast<uint8_t>(instance, offset) = static_cast<uint8_t>(value);
    break;
  case CharField:
    cast<uint16_t>(instance, offset) = static_cast<uint16_t>(value);
    break;
  case ShortField:
    cast<int16_t>(instance, offset) = static_cast<int16_t>(value);
    break;
  case IntField: 
    cast<int32_t>(instance, offset) = static_cast<int32_t>(value);
    break;
  case LongField: 
    cast<int64_t>(instance, offset) = static_cast<int64_t>(value);
    break;
  case FloatField: 
    cast<uint32_t>(instance, offset) = static_cast<uint32_t>(value);
    break;
  case DoubleField: 
    cast<uint64_t>(instance, offset) = static_cast<uint64_t>(value);
    break;
  default:
    abort(t);
  }
}

extern "C" JNIEXPORT void JNICALL
Avian_java_lang_reflect_Field_setObject
(Thread* t, object, uintptr_t* arguments)
{
  object instance = reinterpret_cast<object>(arguments[0]);
  int offset = arguments[1];
  object value = reinterpret_cast<object>(arguments[2]);

  set(t, instance, offset, value);
}

extern "C" JNIEXPORT int64_t JNICALL
Avian_java_lang_reflect_Constructor_make
(Thread* t, object, uintptr_t* arguments)
{
  object c = reinterpret_cast<object>(arguments[0]);

  return reinterpret_cast<int64_t>(make(t, c));
}

extern "C" JNIEXPORT int64_t JNICALL
Avian_java_lang_reflect_Method_getCaller
(Thread* t, object, uintptr_t*)
{
  return reinterpret_cast<int64_t>(getCaller(t, 2));
}

extern "C" JNIEXPORT int64_t JNICALL
Avian_java_lang_reflect_Method_invoke
(Thread* t, object, uintptr_t* arguments)
{
  object method = reinterpret_cast<object>(arguments[0]);
  object instance = reinterpret_cast<object>(arguments[1]);
  object args = reinterpret_cast<object>(arguments[2]);

  THREAD_RESOURCE0(t, {
      if (t->exception) {
        object exception = t->exception;
        t->exception = makeThrowable
          (t, Machine::InvocationTargetExceptionType, 0, 0, exception);
      }
    });

  unsigned returnCode = methodReturnCode(t, method);

  return reinterpret_cast<int64_t>
    (translateInvokeResult
     (t, returnCode, t->m->processor->invokeArray(t, method, instance, args)));
}

extern "C" JNIEXPORT int64_t JNICALL
Avian_java_lang_reflect_Array_getLength
(Thread* t, object, uintptr_t* arguments)
{
  object array = reinterpret_cast<object>(arguments[0]);

  if (LIKELY(array)) {
    unsigned elementSize = classArrayElementSize(t, objectClass(t, array));

    if (LIKELY(elementSize)) {
      return cast<uintptr_t>(array, BytesPerWord);
    } else {
      throwNew(t, Machine::IllegalArgumentExceptionType);
    }
  } else {
    throwNew(t, Machine::NullPointerExceptionType);
  }
}

extern "C" JNIEXPORT int64_t JNICALL
Avian_java_lang_reflect_Array_makeObjectArray
(Thread* t, object, uintptr_t* arguments)
{
  object elementType = reinterpret_cast<object>(arguments[0]);
  int length = arguments[1];

  return reinterpret_cast<int64_t>
    (makeObjectArray(t, jclassVmClass(t, elementType), length));
}

extern "C" JNIEXPORT int64_t JNICALL
Avian_java_lang_Float_floatToRawIntBits
(Thread*, object, uintptr_t* arguments)
{
  return static_cast<int32_t>(*arguments);
}

extern "C" JNIEXPORT int64_t JNICALL
Avian_java_lang_Float_intBitsToFloat
(Thread*, object, uintptr_t* arguments)
{
  return static_cast<int32_t>(*arguments);
}

extern "C" JNIEXPORT int64_t JNICALL
Avian_java_lang_Double_doubleToRawLongBits
(Thread*, object, uintptr_t* arguments)
{
  int64_t v; memcpy(&v, arguments, 8);
  return v;
}

extern "C" JNIEXPORT int64_t JNICALL
Avian_java_lang_Double_longBitsToDouble
(Thread*, object, uintptr_t* arguments)
{
  int64_t v; memcpy(&v, arguments, 8);
  return v;
}

extern "C" JNIEXPORT int64_t JNICALL
Avian_java_lang_String_intern
(Thread* t, object, uintptr_t* arguments)
{
  object this_ = reinterpret_cast<object>(arguments[0]);

  return reinterpret_cast<int64_t>(intern(t, this_));
}

extern "C" JNIEXPORT int64_t JNICALL
Avian_java_lang_System_getVMProperty
(Thread* t, object, uintptr_t* arguments)
{
  object name = reinterpret_cast<object>(arguments[0]);
  object found = reinterpret_cast<object>(arguments[1]);
  PROTECT(t, found);

  unsigned length = stringLength(t, name);
  THREAD_RUNTIME_ARRAY(t, char, n, length + 1);
  stringChars(t, name, RUNTIME_ARRAY_BODY(n));

  int64_t r = 0;
  if (::strcmp(RUNTIME_ARRAY_BODY(n), "java.lang.classpath") == 0) {
    r = reinterpret_cast<int64_t>
      (makeString(t, "%s", t->m->appFinder->path()));
  } else if (::strcmp(RUNTIME_ARRAY_BODY(n), "avian.version") == 0) {
    r = reinterpret_cast<int64_t>(makeString(t, AVIAN_VERSION));
  } else if (::strcmp(RUNTIME_ARRAY_BODY(n), "file.encoding") == 0) {
    r = reinterpret_cast<int64_t>(makeString(t, "ASCII"));
  } else {
    const char* v = findProperty(t, RUNTIME_ARRAY_BODY(n));
    if (v) {
      r = reinterpret_cast<int64_t>(makeString(t, v));
    }
  }
  
  if (r) {
    booleanArrayBody(t, found, 0) = true;
  }

  return r;
}

extern "C" JNIEXPORT void JNICALL
Avian_java_lang_System_arraycopy
(Thread* t, object, uintptr_t* arguments)
{
  arrayCopy(t, reinterpret_cast<object>(arguments[0]),
            arguments[1],
            reinterpret_cast<object>(arguments[2]),
            arguments[3],
            arguments[4]);
}

extern "C" JNIEXPORT int64_t JNICALL
Avian_java_lang_System_identityHashCode
(Thread* t, object, uintptr_t* arguments)
{
  object o = reinterpret_cast<object>(arguments[0]);

  if (LIKELY(o)) {
    return objectHash(t, o);
  } else {
    throwNew(t, Machine::NullPointerExceptionType);
  }
}

extern "C" JNIEXPORT void JNICALL
Avian_java_lang_Runtime_load
(Thread* t, object, uintptr_t* arguments)
{
  object name = reinterpret_cast<object>(arguments[0]);
  bool mapName = arguments[1];

  unsigned length = stringLength(t, name);
  THREAD_RUNTIME_ARRAY(t, char, n, length + 1);
  stringChars(t, name, RUNTIME_ARRAY_BODY(n));

  loadLibrary(t, "", RUNTIME_ARRAY_BODY(n), mapName, true);
}

extern "C" JNIEXPORT void JNICALL
Avian_java_lang_Runtime_gc
(Thread* t, object, uintptr_t*)
{
  collect(t, Heap::MajorCollection);
}

extern "C" JNIEXPORT int64_t JNICALL
Avian_java_lang_Runtime_freeMemory
(Thread*, object, uintptr_t*)
{
  // todo
  return 0;
}

extern "C" JNIEXPORT int64_t JNICALL
Avian_java_lang_Runtime_totalMemory
(Thread*, object, uintptr_t*)
{
  // todo
  return 0;
}

extern "C" JNIEXPORT void JNICALL
Avian_java_lang_Runtime_addShutdownHook
(Thread* t, object, uintptr_t* arguments)
{
  object hook = reinterpret_cast<object>(arguments[1]);
  PROTECT(t, hook);

  ACQUIRE(t, t->m->shutdownLock);

  setRoot(t, Machine::ShutdownHooks,
          makePair(t, hook, root(t, Machine::ShutdownHooks)));
}

extern "C" JNIEXPORT int64_t JNICALL
Avian_java_lang_Throwable_trace
(Thread* t, object, uintptr_t* arguments)
{
  return reinterpret_cast<int64_t>(getTrace(t, arguments[0]));
}

extern "C" JNIEXPORT int64_t JNICALL
Avian_java_lang_Throwable_resolveTrace
(Thread* t, object, uintptr_t* arguments)
{
  object trace = reinterpret_cast<object>(*arguments);
  PROTECT(t, trace);

  unsigned length = objectArrayLength(t, trace);
  object elementType = type(t, Machine::StackTraceElementType);
  object array = makeObjectArray(t, elementType, length);
  PROTECT(t, array);

  for (unsigned i = 0; i < length; ++i) {
    object ste = makeStackTraceElement(t, objectArrayBody(t, trace, i));
    set(t, array, ArrayBody + (i * BytesPerWord), ste);
  }

  return reinterpret_cast<int64_t>(array);
}

extern "C" JNIEXPORT int64_t JNICALL
Avian_java_lang_Thread_currentThread
(Thread* t, object, uintptr_t*)
{
  return reinterpret_cast<int64_t>(t->javaThread);
}

extern "C" JNIEXPORT int64_t JNICALL
Avian_java_lang_Thread_doStart
(Thread* t, object, uintptr_t* arguments)
{
  return reinterpret_cast<int64_t>
    (startThread(t, reinterpret_cast<object>(*arguments)));
}

extern "C" JNIEXPORT void JNICALL
Avian_java_lang_Thread_interrupt
(Thread* t, object, uintptr_t* arguments)
{
  int64_t peer; memcpy(&peer, arguments, 8);

  interrupt(t, reinterpret_cast<Thread*>(peer));
}

extern "C" JNIEXPORT int64_t JNICALL
Avian_java_lang_Thread_interrupted
(Thread* t, object, uintptr_t* arguments)
{
  int64_t peer; memcpy(&peer, arguments, 8);

  return getAndClearInterrupted(t, reinterpret_cast<Thread*>(peer));
}

extern "C" JNIEXPORT int64_t JNICALL
Avian_java_lang_Thread_getStackTrace
(Thread* t, object, uintptr_t* arguments)
{
  int64_t peer; memcpy(&peer, arguments, 8);

  if (reinterpret_cast<Thread*>(peer) == t) {
    return reinterpret_cast<int64_t>(makeTrace(t));
  } else {
    return reinterpret_cast<int64_t>
      (t->m->processor->getStackTrace(t, reinterpret_cast<Thread*>(peer)));
  }
}

extern "C" JNIEXPORT int64_t JNICALL
Avian_java_lang_Thread_activeCount
(Thread* t, object, uintptr_t*)
{
  return t->m->liveCount;
}

extern "C" JNIEXPORT int64_t JNICALL
Avian_java_lang_Thread_enumerate
(Thread* t, object, uintptr_t* arguments)
{
  object array = reinterpret_cast<object>(*arguments);

  ACQUIRE_RAW(t, t->m->stateLock);

  unsigned count = min(t->m->liveCount, objectArrayLength(t, array));
  unsigned index = 0;
  local::enumerateThreads(t, t->m->rootThread, array, &index, count);
  return count;
}

extern "C" JNIEXPORT void JNICALL
Avian_java_lang_Thread_yield
(Thread* t, object, uintptr_t*)
{
  t->m->system->yield();
}

extern "C" JNIEXPORT int64_t JNICALL
Avian_avian_Atomic_getOffset
(Thread* t, object, uintptr_t* arguments)
{
  return fieldOffset
    (t, jfieldVmField(t, reinterpret_cast<object>(arguments[0])));
}

extern "C" JNIEXPORT int64_t JNICALL
Avian_avian_Atomic_compareAndSwapObject
(Thread* t, object, uintptr_t* arguments)
{
  object target = reinterpret_cast<object>(arguments[0]);
  int64_t offset; memcpy(&offset, arguments + 1, 8);
  uintptr_t expect = arguments[3];
  uintptr_t update = arguments[4];

  bool success = atomicCompareAndSwap
    (&cast<uintptr_t>(target, offset), expect, update);

  if (success) {
    mark(t, target, offset);
  }

  return success;
}

extern "C" JNIEXPORT int64_t JNICALL
Avian_avian_Classes_primitiveClass
(Thread* t, object, uintptr_t* arguments)
{
  return reinterpret_cast<int64_t>(primitiveClass(t, arguments[0]));
}

extern "C" JNIEXPORT int64_t JNICALL
Avian_avian_Classes_defineVMClass
(Thread* t, object, uintptr_t* arguments)
{
  object loader = reinterpret_cast<object>(arguments[0]);
  object b = reinterpret_cast<object>(arguments[1]);
  int offset = arguments[2];
  int length = arguments[3];

  uint8_t* buffer = static_cast<uint8_t*>
    (t->m->heap->allocate(length));
  
  THREAD_RESOURCE2(t, uint8_t*, buffer, int, length,
                   t->m->heap->free(buffer, length));

  memcpy(buffer, &byteArrayBody(t, b, offset), length);

  return reinterpret_cast<int64_t>(defineClass(t, loader, buffer, length));
}

extern "C" JNIEXPORT void JNICALL
Avian_avian_Classes_initialize
(Thread* t, object, uintptr_t* arguments)
{
  object this_ = reinterpret_cast<object>(arguments[0]);

  initClass(t, this_);
}

extern "C" JNIEXPORT int64_t JNICALL
Avian_avian_Classes_isAssignableFrom
(Thread* t, object, uintptr_t* arguments)
{
  object this_ = reinterpret_cast<object>(arguments[0]);
  object that = reinterpret_cast<object>(arguments[1]);

  if (LIKELY(that)) {
    return vm::isAssignableFrom(t, this_, that);
  } else {
    throwNew(t, Machine::NullPointerExceptionType);
  }
}

extern "C" JNIEXPORT int64_t JNICALL
Avian_avian_Classes_getVMClass
(Thread* t, object, uintptr_t* arguments)
{
  return reinterpret_cast<int64_t>
    (objectClass(t, reinterpret_cast<object>(arguments[0])));
}
