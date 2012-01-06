/* Copyright (c) 2008-2011, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

#include "machine.h"
#include "constants.h"
#include "processor.h"
#include "util.h"

using namespace vm;

namespace {

int64_t
search(Thread* t, object loader, object name,
       object (*op)(Thread*, object, object), bool replaceDots)
{
  if (LIKELY(name)) {
    PROTECT(t, loader);
    PROTECT(t, name);

    object n = makeByteArray(t, stringLength(t, name) + 1);
    char* s = reinterpret_cast<char*>(&byteArrayBody(t, n, 0));
    stringChars(t, name, s);
    
    if (replaceDots) {
      replace('.', '/', s);
    }

    return reinterpret_cast<int64_t>(op(t, loader, n));
  } else {
    throwNew(t, Machine::NullPointerExceptionType);
  }
}

object
resolveSystemClassThrow(Thread* t, object loader, object spec)
{
  return resolveSystemClass
    (t, loader, spec, true, Machine::ClassNotFoundExceptionType);
}

} // namespace

extern "C" JNIEXPORT void JNICALL
Avian_avian_Classes_acquireClassLock
(Thread* t, object, uintptr_t*)
{
  acquire(t, t->m->classLock);
}

extern "C" JNIEXPORT void JNICALL
Avian_avian_Classes_releaseClassLock
(Thread* t, object, uintptr_t*)
{
  release(t, t->m->classLock);
}

extern "C" JNIEXPORT int64_t JNICALL
Avian_avian_Classes_resolveVMClass
(Thread* t, object, uintptr_t* arguments)
{
  object loader = reinterpret_cast<object>(arguments[0]);
  object spec = reinterpret_cast<object>(arguments[1]);

  return reinterpret_cast<int64_t>
    (resolveClass(t, loader, spec, true, Machine::ClassNotFoundExceptionType));
}

extern "C" JNIEXPORT int64_t JNICALL
Avian_avian_SystemClassLoader_findLoadedVMClass
(Thread* t, object, uintptr_t* arguments)
{
  object loader = reinterpret_cast<object>(arguments[0]);
  object name = reinterpret_cast<object>(arguments[1]);

  return search(t, loader, name, findLoadedClass, true);
}

extern "C" JNIEXPORT int64_t JNICALL
Avian_avian_SystemClassLoader_findVMClass
(Thread* t, object, uintptr_t* arguments)
{
  object loader = reinterpret_cast<object>(arguments[0]);
  object name = reinterpret_cast<object>(arguments[1]);

  return search(t, loader, name, resolveSystemClassThrow, true);
}

extern "C" JNIEXPORT int64_t JNICALL
Avian_avian_SystemClassLoader_resourceURLPrefix
(Thread* t, object, uintptr_t* arguments)
{
  object loader = reinterpret_cast<object>(arguments[0]);
  object name = reinterpret_cast<object>(arguments[1]);

  if (LIKELY(name)) {
    THREAD_RUNTIME_ARRAY(t, char, n, stringLength(t, name) + 1);
    stringChars(t, name, RUNTIME_ARRAY_BODY(n));

    const char* name = static_cast<Finder*>
      (systemClassLoaderFinder(t, loader))->urlPrefix(RUNTIME_ARRAY_BODY(n));

    return name ? reinterpret_cast<uintptr_t>(makeString(t, "%s", name)) : 0;
  } else {
    throwNew(t, Machine::NullPointerExceptionType);
  }
}

extern "C" JNIEXPORT int64_t JNICALL
Avian_avian_SystemClassLoader_getClass
(Thread* t, object, uintptr_t* arguments)
{
  return reinterpret_cast<int64_t>
    (getJClass(t, reinterpret_cast<object>(arguments[0])));
}

#ifdef AVIAN_HEAPDUMP

extern "C" JNIEXPORT void JNICALL
Avian_avian_Machine_dumpHeap
(Thread* t, object, uintptr_t* arguments)
{
  object outputFile = reinterpret_cast<object>(*arguments);

  unsigned length = stringLength(t, outputFile);
  THREAD_RUNTIME_ARRAY(t, char, n, length + 1);
  stringChars(t, outputFile, RUNTIME_ARRAY_BODY(n));
  FILE* out = vm::fopen(RUNTIME_ARRAY_BODY(n), "wb");
  if (out) {
    { ENTER(t, Thread::ExclusiveState);
      dumpHeap(t, out);
    }
    fclose(out);
  } else {
    throwNew(t, Machine::RuntimeExceptionType, "file not found: %s", n);
  }
}

#endif//AVIAN_HEAPDUMP

extern "C" JNIEXPORT void JNICALL
Avian_java_lang_Runtime_exit
(Thread* t, object, uintptr_t* arguments)
{
  shutDown(t);

  t->m->system->exit(arguments[1]);
}

extern "C" JNIEXPORT int64_t JNICALL
Avian_avian_resource_Handler_00024ResourceInputStream_getContentLength
(Thread* t, object, uintptr_t* arguments)
{
  object path = reinterpret_cast<object>(*arguments);

  if (LIKELY(path)) {
    THREAD_RUNTIME_ARRAY(t, char, p, stringLength(t, path) + 1);
    stringChars(t, path, RUNTIME_ARRAY_BODY(p));

    System::Region* r = t->m->bootFinder->find(RUNTIME_ARRAY_BODY(p));
    if (r == 0) {
      r = t->m->appFinder->find(RUNTIME_ARRAY_BODY(p));
    }

    if (r) {
      jint rSize = r->length();
      r->dispose();
      return rSize;
    }
  }
  return -1;
}

extern "C" JNIEXPORT int64_t JNICALL
Avian_avian_resource_Handler_00024ResourceInputStream_open
(Thread* t, object, uintptr_t* arguments)
{
  object path = reinterpret_cast<object>(*arguments);

  if (LIKELY(path)) {
    THREAD_RUNTIME_ARRAY(t, char, p, stringLength(t, path) + 1);
    stringChars(t, path, RUNTIME_ARRAY_BODY(p));

    System::Region* r = t->m->bootFinder->find(RUNTIME_ARRAY_BODY(p));
    if (r == 0) {
      r = t->m->appFinder->find(RUNTIME_ARRAY_BODY(p));
    }

    return reinterpret_cast<int64_t>(r);
  } else {
    throwNew(t, Machine::NullPointerExceptionType);
  }
}

extern "C" JNIEXPORT int64_t JNICALL
Avian_avian_resource_Handler_00024ResourceInputStream_available
(Thread*, object, uintptr_t* arguments)
{
  int64_t peer; memcpy(&peer, arguments, 8);
  int32_t position = arguments[2];

  System::Region* region = reinterpret_cast<System::Region*>(peer);
  return static_cast<jint>(region->length()) - position;
}

extern "C" JNIEXPORT int64_t JNICALL
Avian_avian_resource_Handler_00024ResourceInputStream_read__JI
(Thread*, object, uintptr_t* arguments)
{
  int64_t peer; memcpy(&peer, arguments, 8);
  int32_t position = arguments[2];

  System::Region* region = reinterpret_cast<System::Region*>(peer);
  if (position >= static_cast<jint>(region->length())) {
    return -1;
  } else {
    return region->start()[position];
  }
}

extern "C" JNIEXPORT int64_t JNICALL
Avian_avian_resource_Handler_00024ResourceInputStream_read__JI_3BII
(Thread* t, object, uintptr_t* arguments)
{
  int64_t peer; memcpy(&peer, arguments, 8);
  int32_t position = arguments[2];
  object buffer = reinterpret_cast<object>(arguments[3]);
  int32_t offset = arguments[4];
  int32_t length = arguments[5];

  if (length == 0) return 0;
  
  System::Region* region = reinterpret_cast<System::Region*>(peer);
  if (length > static_cast<jint>(region->length()) - position) {
    length = static_cast<jint>(region->length()) - position;
  }
  if (length <= 0) {
    return -1;
  } else {
    memcpy(&byteArrayBody(t, buffer, offset), region->start() + position,
           length);
    return length;
  }
}

extern "C" JNIEXPORT void JNICALL
Avian_avian_resource_Handler_00024ResourceInputStream_close
(Thread*, object, uintptr_t* arguments)
{
  int64_t peer; memcpy(&peer, arguments, 8);
  reinterpret_cast<System::Region*>(peer)->dispose();
}

extern "C" JNIEXPORT void JNICALL
Avian_avian_Continuations_callWithCurrentContinuation
(Thread* t, object, uintptr_t* arguments)
{
  t->m->processor->callWithCurrentContinuation
    (t, reinterpret_cast<object>(*arguments));

  abort(t);
}

extern "C" JNIEXPORT void JNICALL
Avian_avian_Continuations_dynamicWind2
(Thread* t, object, uintptr_t* arguments)
{
  t->m->processor->dynamicWind
    (t, reinterpret_cast<object>(arguments[0]),
     reinterpret_cast<object>(arguments[1]),
     reinterpret_cast<object>(arguments[2]));

  abort(t);
}

extern "C" JNIEXPORT void JNICALL
Avian_avian_Continuations_00024Continuation_handleResult
(Thread* t, object, uintptr_t* arguments)
{
  t->m->processor->feedResultToContinuation
    (t, reinterpret_cast<object>(arguments[0]),
     reinterpret_cast<object>(arguments[1]));

  abort(t);
}

extern "C" JNIEXPORT void JNICALL
Avian_avian_Continuations_00024Continuation_handleException
(Thread* t, object, uintptr_t* arguments)
{
  t->m->processor->feedExceptionToContinuation
    (t, reinterpret_cast<object>(arguments[0]),
     reinterpret_cast<object>(arguments[1]));

  abort(t);
}

extern "C" JNIEXPORT int64_t JNICALL
Avian_avian_Singleton_getObject
(Thread* t, object, uintptr_t* arguments)
{
  return reinterpret_cast<int64_t>
    (singletonObject(t, reinterpret_cast<object>(arguments[0]), arguments[1]));
}

extern "C" JNIEXPORT int64_t JNICALL
Avian_avian_Singleton_getInt
(Thread* t, object, uintptr_t* arguments)
{
  return singletonValue
    (t, reinterpret_cast<object>(arguments[0]), arguments[1]);
}

extern "C" JNIEXPORT int64_t JNICALL
Avian_avian_Singleton_getLong
(Thread* t, object, uintptr_t* arguments)
{
  int64_t v;
  memcpy(&v, &singletonValue
         (t, reinterpret_cast<object>(arguments[0]), arguments[1]), 8);
  return v;
}
