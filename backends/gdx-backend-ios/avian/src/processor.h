/* Copyright (c) 2008-2010, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

#ifndef PROCESSOR_H
#define PROCESSOR_H

#include "common.h"
#include "system.h"
#include "heap.h"
#include "bootimage.h"
#include "heapwalk.h"
#include "zone.h"
#include "assembler.h"

namespace vm {

class Processor {
 public:
  class StackWalker;

  class StackVisitor {
   public:
    virtual bool visit(StackWalker* walker) = 0;
  };

  class StackWalker {
   public:
    virtual void walk(StackVisitor* v) = 0;

    virtual object method() = 0;

    virtual int ip() = 0;

    virtual unsigned count() = 0;
  };

  virtual Thread*
  makeThread(Machine* m, object javaThread, Thread* parent) = 0;

  virtual object
  makeMethod(Thread* t,
             uint8_t vmFlags,
             uint8_t returnCode,
             uint8_t parameterCount,
             uint8_t parameterFootprint,
             uint16_t flags,
             uint16_t offset,
             object name,
             object spec,
             object addendum,
             object class_,
             object code) = 0;

  virtual object
  makeClass(Thread* t,
            uint16_t flags,
            uint16_t vmFlags,
            uint16_t fixedSize,
            uint8_t arrayElementSize,
            uint8_t arrayDimensions,
            object objectMask,
            object name,
            object sourceFile,
            object super,
            object interfaceTable,
            object virtualTable,
            object fieldTable,
            object methodTable,
            object addendum,
            object staticTable,
            object loader,
            unsigned vtableLength) = 0;

  virtual void
  initVtable(Thread* t, object c) = 0;

  virtual void
  visitObjects(Thread* t, Heap::Visitor* v) = 0;

  virtual void
  walkStack(Thread* t, StackVisitor* v) = 0;

  virtual int
  lineNumber(Thread* t, object method, int ip) = 0;

  virtual object*
  makeLocalReference(Thread* t, object o) = 0;

  virtual void
  disposeLocalReference(Thread* t, object* r) = 0;

  virtual object
  invokeArray(Thread* t, object method, object this_, object arguments) = 0;

  virtual object
  invokeList(Thread* t, object method, object this_, bool indirectObjects,
             va_list arguments) = 0;

  virtual object
  invokeList(Thread* t, object loader, const char* className,
             const char* methodName, const char* methodSpec,
             object this_, va_list arguments) = 0;

  virtual void
  dispose(Thread* t) = 0;

  virtual void
  dispose() = 0;

  virtual object
  getStackTrace(Thread* t, Thread* target) = 0;

  virtual void
  initialize(BootImage* image, uint8_t* code, unsigned capacity) = 0;

  virtual void
  compileMethod(Thread* t, Zone* zone, object* constants, object* calls,
                DelayedPromise** addresses, object method,
                OffsetResolver* resolver) = 0;

  virtual void
  visitRoots(Thread* t, HeapWalker* w) = 0;

  virtual void
  normalizeVirtualThunks(Thread* t) = 0;

  virtual unsigned*
  makeCallTable(Thread* t, HeapWalker* w) = 0;

  virtual void
  boot(Thread* t, BootImage* image, uint8_t* code) = 0;

  virtual void
  callWithCurrentContinuation(Thread* t, object receiver) = 0;

  virtual void
  dynamicWind(Thread* t, object before, object thunk, object after) = 0;

  virtual void
  feedResultToContinuation(Thread* t, object continuation, object result) = 0;

  virtual void
  feedExceptionToContinuation(Thread* t, object continuation,
                              object exception) = 0;

  virtual void
  walkContinuationBody(Thread* t, Heap::Walker* w, object o, unsigned start)
  = 0;

  object
  invoke(Thread* t, object method, object this_, ...)
  {
    va_list a;
    va_start(a, this_);

    object r = invokeList(t, method, this_, false, a);

    va_end(a);

    return r;
  }

  object
  invoke(Thread* t, object loader, const char* className,
         const char* methodName, const char* methodSpec, object this_, ...)
  {
    va_list a;
    va_start(a, this_);

    object r = invokeList
      (t, loader, className, methodName, methodSpec, this_, a);

    va_end(a);

    return r;
  }
};

Processor*
makeProcessor(System* system, Allocator* allocator, bool useNativeFeatures);

} // namespace vm

#endif//PROCESSOR_H
