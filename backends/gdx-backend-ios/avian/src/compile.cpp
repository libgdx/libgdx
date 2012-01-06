/* Copyright (c) 2008-2011, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

#include "machine.h"
#include "util.h"
#include "vector.h"
#include "process.h"
#include "assembler.h"
#include "target.h"
#include "compiler.h"
#include "arch.h"

using namespace vm;

extern "C" uint64_t
vmInvoke(void* thread, void* function, void* arguments,
         unsigned argumentFootprint, unsigned frameSize, unsigned returnType);

extern "C" void
vmInvoke_returnAddress();

extern "C" void
vmInvoke_safeStack();

extern "C" void
vmJumpAndInvoke(void* thread, void* function, void* stack,
                unsigned argumentFootprint, uintptr_t* arguments,
                unsigned frameSize);

namespace {

namespace local {

const bool DebugCompile = false;
const bool DebugNatives = false;
const bool DebugCallTable = false;
const bool DebugMethodTree = false;
const bool DebugFrameMaps = false;
const bool DebugIntrinsics = false;

const bool CheckArrayBounds = true;

#ifdef AVIAN_CONTINUATIONS
const bool Continuations = true;
#else
const bool Continuations = false;
#endif

const unsigned MaxNativeCallFootprint = TargetBytesPerWord == 8 ? 4 : 5;

const unsigned InitialZoneCapacityInBytes = 64 * 1024;

const unsigned ExecutableAreaSizeInBytes = 30 * 1024 * 1024;

enum Root {
  CallTable,
  MethodTree,
  MethodTreeSentinal,
  ObjectPools,
  StaticTableArray,
  VirtualThunks,
  ReceiveMethod,
  WindMethod,
  RewindMethod
};

enum ThunkIndex {
  compileMethodIndex,
  compileVirtualMethodIndex,
  invokeNativeIndex,
  throwArrayIndexOutOfBoundsIndex,
  throwStackOverflowIndex,

#define THUNK(s) s##Index,
#include "thunks.cpp"
#undef THUNK

  dummyIndex
};

const unsigned RootCount = RewindMethod + 1;

inline bool
isVmInvokeUnsafeStack(void* ip)
{
  return reinterpret_cast<uintptr_t>(ip)
    >= reinterpret_cast<uintptr_t>(voidPointer(vmInvoke_returnAddress))
    and reinterpret_cast<uintptr_t>(ip)
    < reinterpret_cast<uintptr_t> (voidPointer(vmInvoke_safeStack));
}

class MyThread;

void*
getIp(MyThread*);

class MyThread: public Thread {
 public:
  class CallTrace {
   public:
    CallTrace(MyThread* t, object method):
      t(t),
      ip(getIp(t)),
      stack(t->stack),
      scratch(t->scratch),
      continuation(t->continuation),
      nativeMethod((methodFlags(t, method) & ACC_NATIVE) ? method : 0),
      targetMethod(0),
      originalMethod(method),
      next(t->trace)
    {
      doTransition(t, 0, 0, 0, this);
    }

    ~CallTrace() {
      assert(t, t->stack == 0);

      t->scratch = scratch;

      doTransition(t, ip, stack, continuation, next);
    }

    MyThread* t;
    void* ip;
    void* stack;
    void* scratch;
    object continuation;
    object nativeMethod;
    object targetMethod;
    object originalMethod;
    CallTrace* next;
  };

  class Context {
   public:
    class MyProtector: public Thread::Protector {
     public:
      MyProtector(MyThread* t, Context* context):
        Protector(t), context(context)
      { }

      virtual void visit(Heap::Visitor* v) {
        v->visit(&(context->continuation));
      }

      Context* context;
    };

    Context(MyThread* t, void* ip, void* stack, object continuation,
            CallTrace* trace):
      ip(ip),
      stack(stack),
      continuation(continuation),
      trace(trace),
      protector(t, this)
    { }

    void* ip;
    void* stack;
    object continuation;
    CallTrace* trace;
    MyProtector protector;
  };

  class TraceContext: public Context {
   public:
    TraceContext(MyThread* t, void* ip, void* stack, object continuation,
                 CallTrace* trace):
      Context(t, ip, stack, continuation, trace),
      t(t),
      link(0),
      javaStackLimit(0),
      next(t->traceContext)
    {
      t->traceContext = this;
    }

    TraceContext(MyThread* t, void* link):
      Context(t, t->ip, t->stack, t->continuation, t->trace),
      t(t),
      link(link),
      javaStackLimit(0),
      next(t->traceContext)
    {
      t->traceContext = this;
    }

    ~TraceContext() {
      t->traceContext = next;
    }

    MyThread* t;
    void* link;
    void* javaStackLimit;
    TraceContext* next;
  };

  static void doTransition(MyThread* t, void* ip, void* stack,
                           object continuation, MyThread::CallTrace* trace)
  {
    // in this function, we "atomically" update the thread context
    // fields in such a way to ensure that another thread may
    // interrupt us at any time and still get a consistent, accurate
    // stack trace.  See MyProcessor::getStackTrace for details.

    assert(t, t->transition == 0);

    Context c(t, ip, stack, continuation, trace);

    compileTimeMemoryBarrier();

    t->transition = &c;

    compileTimeMemoryBarrier();

    t->ip = ip;
    t->stack = stack;
    t->continuation = continuation;
    t->trace = trace;

    compileTimeMemoryBarrier();

    t->transition = 0;
  }

  MyThread(Machine* m, object javaThread, MyThread* parent,
           bool useNativeFeatures):
    Thread(m, javaThread, parent),
    ip(0),
    stack(0),
    scratch(0),
    continuation(0),
    exceptionStackAdjustment(0),
    exceptionOffset(0),
    exceptionHandler(0),
    tailAddress(0),
    virtualCallTarget(0),
    virtualCallIndex(0),
    heapImage(0),
    codeImage(0),
    thunkTable(0),
    trace(0),
    reference(0),
    arch(parent
         ? parent->arch
         : makeArchitecture(m->system, useNativeFeatures)),
    transition(0),
    traceContext(0),
    stackLimit(0),
    methodLockIsClean(true)
  {
    arch->acquire();
  }

  void* ip;
  void* stack;
  void* scratch;
  object continuation;
  uintptr_t exceptionStackAdjustment;
  uintptr_t exceptionOffset;
  void* exceptionHandler;
  void* tailAddress;
  void* virtualCallTarget;
  uintptr_t virtualCallIndex;
  uintptr_t* heapImage;
  uint8_t* codeImage;
  void** thunkTable;
  CallTrace* trace;
  Reference* reference;
  Assembler::Architecture* arch;
  Context* transition;
  TraceContext* traceContext;
  uintptr_t stackLimit;
  bool methodLockIsClean;
};

void
transition(MyThread* t, void* ip, void* stack, object continuation,
           MyThread::CallTrace* trace)
{
  MyThread::doTransition(t, ip, stack, continuation, trace);
}

unsigned
parameterOffset(MyThread* t, object method)
{
  return methodParameterFootprint(t, method)
    + t->arch->frameFooterSize()
    + t->arch->frameReturnAddressSize() - 1;
}

object
resolveThisPointer(MyThread* t, void* stack)
{
  return reinterpret_cast<object*>(stack)
    [t->arch->frameFooterSize() + t->arch->frameReturnAddressSize()];
}

object
findMethod(Thread* t, object method, object instance)
{
  if ((methodFlags(t, method) & ACC_STATIC) == 0) {
    if (classFlags(t, methodClass(t, method)) & ACC_INTERFACE) {
      return findInterfaceMethod(t, method, objectClass(t, instance));
    } else if (methodVirtual(t, method)) {
      return findVirtualMethod(t, method, objectClass(t, instance));      
    }
  }
  return method;
}

object
resolveTarget(MyThread* t, void* stack, object method)
{
  object class_ = objectClass(t, resolveThisPointer(t, stack));

  if (classVmFlags(t, class_) & BootstrapFlag) {
    PROTECT(t, method);
    PROTECT(t, class_);

    resolveSystemClass(t, root(t, Machine::BootLoader), className(t, class_));
  }

  if (classFlags(t, methodClass(t, method)) & ACC_INTERFACE) {
    return findInterfaceMethod(t, method, class_);
  } else {
    return findVirtualMethod(t, method, class_);
  }
}

object
resolveTarget(MyThread* t, object class_, unsigned index)
{
  if (classVmFlags(t, class_) & BootstrapFlag) {
    PROTECT(t, class_);

    resolveSystemClass(t, root(t, Machine::BootLoader), className(t, class_));
  }

  return arrayBody(t, classVirtualTable(t, class_), index);
}

object&
root(Thread* t, Root root);

void
setRoot(Thread* t, Root root, object value);

unsigned
compiledSize(intptr_t address)
{
  return reinterpret_cast<target_uintptr_t*>(address)[-1];
}

intptr_t
methodCompiled(Thread* t, object method)
{
  return codeCompiled(t, methodCode(t, method));
}

intptr_t
compareIpToMethodBounds(Thread* t, intptr_t ip, object method)
{
  intptr_t start = methodCompiled(t, method);

  if (DebugMethodTree) {
    fprintf(stderr, "find %p in (%p,%p)\n",
            reinterpret_cast<void*>(ip),
            reinterpret_cast<void*>(start),
            reinterpret_cast<void*>(start + compiledSize(start)));
  }

  if (ip < start) {
    return -1;
  } else if (ip < start + static_cast<intptr_t>
             (compiledSize(start) + TargetBytesPerWord))
  {
    return 0;
  } else {
    return 1;
  }
}

object
methodForIp(MyThread* t, void* ip)
{
  if (DebugMethodTree) {
    fprintf(stderr, "query for method containing %p\n", ip);
  }

  // we must use a version of the method tree at least as recent as the
  // compiled form of the method containing the specified address (see
  // compile(MyThread*, FixedAllocator*, BootContext*, object)):
  loadMemoryBarrier();

  return treeQuery(t, root(t, MethodTree), reinterpret_cast<intptr_t>(ip),
                   root(t, MethodTreeSentinal), compareIpToMethodBounds);
}

unsigned
localSize(MyThread* t, object method)
{
  unsigned size = codeMaxLocals(t, methodCode(t, method));
  if ((methodFlags(t, method) & (ACC_SYNCHRONIZED | ACC_STATIC))
      == ACC_SYNCHRONIZED)
  {
    ++ size;
  }
  return size;
}

unsigned
alignedFrameSize(MyThread* t, object method)
{
  return t->arch->alignFrameSize
    (localSize(t, method)
     - methodParameterFootprint(t, method)
     + codeMaxStack(t, methodCode(t, method))
     + t->arch->frameFootprint(MaxNativeCallFootprint));
}

void
nextFrame(MyThread* t, void** ip, void** sp, object method, object target)
{
  object code = methodCode(t, method);
  intptr_t start = codeCompiled(t, code);
  void* link;
  void* javaStackLimit;

  if (t->traceContext) {
    link = t->traceContext->link;
    javaStackLimit = t->traceContext->javaStackLimit;
  } else {
    link = 0;
    javaStackLimit = 0;
  }

  // fprintf(stderr, "nextFrame %s.%s%s target %s.%s%s ip %p sp %p\n",
  //         &byteArrayBody(t, className(t, methodClass(t, method)), 0),
  //         &byteArrayBody(t, methodName(t, method), 0),
  //         &byteArrayBody(t, methodSpec(t, method), 0),
  //         target
  //         ? &byteArrayBody(t, className(t, methodClass(t, target)), 0)
  //         : 0,
  //         target
  //         ? &byteArrayBody(t, methodName(t, target), 0)
  //         : 0,
  //         target
  //         ? &byteArrayBody(t, methodSpec(t, target), 0)
  //         : 0,
  //         *ip, *sp);

  t->arch->nextFrame
    (reinterpret_cast<void*>(start), compiledSize(start),
     alignedFrameSize(t, method), link, javaStackLimit,
     target ? methodParameterFootprint(t, target) : -1, ip, sp);

  // fprintf(stderr, "next frame ip %p sp %p\n", *ip, *sp);
}

void*
getIp(MyThread* t, void* ip, void* stack)
{
  // Here we use the convention that, if the return address is neither
  // pushed on to the stack automatically as part of the call nor
  // stored in the caller's frame, it will be saved in MyThread::ip
  // instead of on the stack.  See the various implementations of
  // Assembler::saveFrame for details on how this is done.
  return t->arch->returnAddressOffset() < 0 ? ip : t->arch->frameIp(stack);
}

void*
getIp(MyThread* t)
{
  return getIp(t, t->ip, t->stack);
}

class MyStackWalker: public Processor::StackWalker {
 public:
  enum State {
    Start,
    Next,
    Trace,
    Continuation,
    Method,
    NativeMethod,
    Finish
  };

  class MyProtector: public Thread::Protector {
   public:
    MyProtector(MyStackWalker* walker):
      Protector(walker->t), walker(walker)
    { }

    virtual void visit(Heap::Visitor* v) {
      v->visit(&(walker->method_));
      v->visit(&(walker->target));
      v->visit(&(walker->continuation));
    }

    MyStackWalker* walker;
  };

  MyStackWalker(MyThread* t):
    t(t),
    state(Start),
    method_(0),
    target(0),
    protector(this)
  {
    if (t->traceContext) {
      ip_ = t->traceContext->ip;
      stack = t->traceContext->stack;
      trace = t->traceContext->trace;
      continuation = t->traceContext->continuation;
    } else {
      ip_ = getIp(t);
      stack = t->stack;
      trace = t->trace;
      continuation = t->continuation;      
    }
  }

  MyStackWalker(MyStackWalker* w):
    t(w->t),
    state(w->state),
    ip_(w->ip_),
    stack(w->stack),
    trace(w->trace),
    method_(w->method_),
    target(w->target),
    continuation(w->continuation),
    protector(this)
  { }

  virtual void walk(Processor::StackVisitor* v) {
    for (MyStackWalker it(this); it.valid();) {
      MyStackWalker walker(&it);
      if (not v->visit(&walker)) {
        break;
      }
      it.next();
    }
  }
    
  bool valid() {
    while (true) {
//       fprintf(stderr, "state: %d\n", state);
      switch (state) {
      case Start:
        if (trace and trace->nativeMethod) {
          method_ = trace->nativeMethod;
          state = NativeMethod;
        } else {
          state = Next;
        }
        break;

      case Next:
        if (stack) {
          target = method_;
          method_ = methodForIp(t, ip_);
          if (method_) {
            state = Method;
          } else if (continuation) {
            method_ = continuationMethod(t, continuation);
            state = Continuation;
          } else {
            state = Trace;
          }
        } else {
          state = Trace;
        }
        break;

      case Trace: {
        if (trace) {
          continuation = trace->continuation;
          stack = trace->stack;
          ip_ = trace->ip;
          trace = trace->next;

          state = Start;
        } else {
          state = Finish;
        }
      } break;

      case Continuation:
      case Method:
      case NativeMethod:
        return true;

      case Finish:
        return false;
   
      default:
        abort(t);
      }
    }
  }
    
  void next() {
    switch (state) {
    case Continuation:
      continuation = continuationNext(t, continuation);
      break;

    case Method:
      nextFrame(t, &ip_, &stack, method_, target);
      break;

    case NativeMethod:
      break;
   
    default:
      abort(t);
    }

    state = Next;
  }

  virtual object method() {
//     fprintf(stderr, "method %s.%s\n", &byteArrayBody
//             (t, className(t, methodClass(t, method_)), 0),
//             &byteArrayBody(t, methodName(t, method_), 0));
    return method_;
  }

  virtual int ip() {
    switch (state) {
    case Continuation:
      return reinterpret_cast<intptr_t>(continuationAddress(t, continuation))
        - methodCompiled(t, continuationMethod(t, continuation));

    case Method:
      return reinterpret_cast<intptr_t>(ip_) - methodCompiled(t, method_);
        
    case NativeMethod:
      return 0;

    default:
      abort(t);
    }
  }

  virtual unsigned count() {
    unsigned count = 0;

    for (MyStackWalker walker(this); walker.valid();) {
      walker.next();
      ++ count;
    }
    
    return count;
  }

  MyThread* t;
  State state;
  void* ip_;
  void* stack;
  MyThread::CallTrace* trace;
  object method_;
  object target;
  object continuation;
  MyProtector protector;
};

int
localOffset(MyThread* t, int v, object method)
{
  int parameterFootprint = methodParameterFootprint(t, method);
  int frameSize = alignedFrameSize(t, method);

  int offset = ((v < parameterFootprint) ?
                (frameSize
                 + parameterFootprint
                 + t->arch->frameFooterSize()
                 + t->arch->frameHeaderSize()
                 - v - 1) :
                (frameSize
                 + parameterFootprint
                 - v - 1));

  assert(t, offset >= 0);
  return offset;
}

int
localOffsetFromStack(MyThread* t, int index, object method)
{
  return localOffset(t, index, method)
    + t->arch->frameReturnAddressSize();
}

object*
localObject(MyThread* t, void* stack, object method, unsigned index)
{
  return static_cast<object*>(stack) + localOffsetFromStack(t, index, method);
}

int
stackOffsetFromFrame(MyThread* t, object method)
{
  return alignedFrameSize(t, method) + t->arch->frameHeaderSize();
}

void*
stackForFrame(MyThread* t, void* frame, object method)
{
  return static_cast<void**>(frame) - stackOffsetFromFrame(t, method);
}

class PoolElement: public Promise {
 public:
  PoolElement(Thread* t, object target, PoolElement* next):
    t(t), target(target), address(0), next(next)
  { }

  virtual int64_t value() {
    assert(t, resolved());
    return address;
  }

  virtual bool resolved() {
    return address != 0;
  }

  Thread* t;
  object target;
  intptr_t address;
  PoolElement* next;
};

class Context;
class SubroutineCall;

class Subroutine {
 public:
  Subroutine(unsigned ip, unsigned logIndex, Subroutine* listNext,
             Subroutine* stackNext):
    listNext(listNext),
    stackNext(stackNext),
    calls(0),
    handle(0),
    ip(ip),
    logIndex(logIndex),
    stackIndex(0),
    callCount(0),
    tableIndex(0),
    visited(false)
  { }

  Subroutine* listNext;
  Subroutine* stackNext;
  SubroutineCall* calls;
  Compiler::Subroutine* handle;
  unsigned ip;
  unsigned logIndex;
  unsigned stackIndex;
  unsigned callCount;
  unsigned tableIndex;
  bool visited;
};

class SubroutinePath;

class SubroutineCall {
 public:
  SubroutineCall(Subroutine* subroutine, Promise* returnAddress):
    subroutine(subroutine),
    returnAddress(returnAddress),
    paths(0),
    next(subroutine->calls)
  {
    subroutine->calls = this;
    ++ subroutine->callCount;
  }

  Subroutine* subroutine;
  Promise* returnAddress;
  SubroutinePath* paths;
  SubroutineCall* next;
};

class SubroutinePath {
 public:
  SubroutinePath(SubroutineCall* call, SubroutinePath* stackNext,
                 uintptr_t* rootTable):
    call(call),
    stackNext(stackNext),
    listNext(call->paths),
    rootTable(rootTable)
  {
    call->paths = this;
  }

  SubroutineCall* call;
  SubroutinePath* stackNext;
  SubroutinePath* listNext;
  uintptr_t* rootTable;
};

void
print(SubroutinePath* path)
{
  if (path) {
    fprintf(stderr, " (");
    while (true) {
      fprintf(stderr, "%p", path->call->returnAddress->resolved() ?
              reinterpret_cast<void*>(path->call->returnAddress->value()) : 0);
      path = path->stackNext;
      if (path) {
        fprintf(stderr, ", ");
      } else {
        break;
      }
    }
    fprintf(stderr, ")");
  }
}

class SubroutineTrace {
 public:
  SubroutineTrace(SubroutinePath* path, SubroutineTrace* next,
                  unsigned mapSize):
    path(path),
    next(next),
    watch(false)
  {
    memset(map, 0, mapSize * BytesPerWord);
  }

  SubroutinePath* path;
  SubroutineTrace* next;
  bool watch;
  uintptr_t map[0];
};

class TraceElement: public TraceHandler {
 public:
  static const unsigned VirtualCall = 1 << 0;
  static const unsigned TailCall    = 1 << 1;
  static const unsigned LongCall    = 1 << 2;

  TraceElement(Context* context, unsigned ip, object target, unsigned flags,
               TraceElement* next, unsigned mapSize):
    context(context),
    address(0),
    next(next),
    subroutineTrace(0),
    target(target),
    ip(ip),
    subroutineTraceCount(0),
    argumentIndex(0),
    flags(flags),
    watch(false)
  {
    memset(map, 0, mapSize * BytesPerWord);
  }

  virtual void handleTrace(Promise* address, unsigned argumentIndex) {
    if (this->address == 0) {
      this->address = address;
      this->argumentIndex = argumentIndex;
    }
  }

  Context* context;
  Promise* address;
  TraceElement* next;
  SubroutineTrace* subroutineTrace;
  object target;
  unsigned ip;
  unsigned subroutineTraceCount;
  unsigned argumentIndex;
  unsigned flags;
  bool watch;
  uintptr_t map[0];
};

class TraceElementPromise: public Promise {
 public:
  TraceElementPromise(System* s, TraceElement* trace): s(s), trace(trace) { }

  virtual int64_t value() {
    assert(s, resolved());
    return trace->address->value();
  }

  virtual bool resolved() {
    return trace->address != 0 and trace->address->resolved();
  }

  System* s;
  TraceElement* trace;
};

enum Event {
  PushContextEvent,
  PopContextEvent,
  IpEvent,
  MarkEvent,
  ClearEvent,
  PushExceptionHandlerEvent,
  TraceEvent,
  PushSubroutineEvent,
  PopSubroutineEvent
};

unsigned
frameMapSizeInBits(MyThread* t, object method)
{
  return localSize(t, method) + codeMaxStack(t, methodCode(t, method));
}

unsigned
frameMapSizeInWords(MyThread* t, object method)
{
  return ceiling(frameMapSizeInBits(t, method), BitsPerWord);
}

uint16_t*
makeVisitTable(MyThread* t, Zone* zone, object method)
{
  unsigned size = codeLength(t, methodCode(t, method)) * 2;
  uint16_t* table = static_cast<uint16_t*>(zone->allocate(size));
  memset(table, 0, size);
  return table;
}

uintptr_t*
makeRootTable(MyThread* t, Zone* zone, object method)
{
  unsigned size = frameMapSizeInWords(t, method)
    * codeLength(t, methodCode(t, method))
    * BytesPerWord;
  uintptr_t* table = static_cast<uintptr_t*>(zone->allocate(size));
  memset(table, 0xFF, size);
  return table;
}

enum Thunk {
#define THUNK(s) s##Thunk,

#include "thunks.cpp"

#undef THUNK
};

const unsigned ThunkCount = gcIfNecessaryThunk + 1;

intptr_t
getThunk(MyThread* t, Thunk thunk);

class BootContext {
 public:
  class MyProtector: public Thread::Protector {
   public:
    MyProtector(Thread* t, BootContext* c): Protector(t), c(c) { }

    virtual void visit(Heap::Visitor* v) {
      v->visit(&(c->constants));
      v->visit(&(c->calls));
    }

    BootContext* c;
  };

  BootContext(Thread* t, object constants, object calls,
              DelayedPromise* addresses, Zone* zone, OffsetResolver* resolver):
    protector(t, this), constants(constants), calls(calls),
    addresses(addresses), addressSentinal(addresses), zone(zone),
    resolver(resolver)
  { }

  MyProtector protector;
  object constants;
  object calls;
  DelayedPromise* addresses;
  DelayedPromise* addressSentinal;
  Zone* zone;
  OffsetResolver* resolver;
};

class Context {
 public:
  class MyResource: public Thread::Resource {
   public:
    MyResource(Context* c): Resource(c->thread), c(c) { }

    virtual void release() {
      c->dispose();
    }

    Context* c;
  };

  class MyProtector: public Thread::Protector {
   public:
    MyProtector(Context* c): Protector(c->thread), c(c) { }

    virtual void visit(Heap::Visitor* v) {
      v->visit(&(c->method));

      for (PoolElement* p = c->objectPool; p; p = p->next) {
        v->visit(&(p->target));
      }

      for (TraceElement* p = c->traceLog; p; p = p->next) {
        v->visit(&(p->target));
      }
    }

    Context* c;
  };

  class MyClient: public Compiler::Client {
   public:
    MyClient(MyThread* t): t(t) { }

    virtual intptr_t getThunk(UnaryOperation, unsigned) {
      abort(t);
    }
    
    virtual intptr_t getThunk(BinaryOperation op, unsigned size,
                              unsigned resultSize)
    {
      if (size == 8) {
        switch(op) {
        case Absolute:
          assert(t, resultSize == 8);
          return local::getThunk(t, absoluteLongThunk);

        case FloatNegate:
          assert(t, resultSize == 8);
          return local::getThunk(t, negateDoubleThunk);

        case FloatSquareRoot:
          assert(t, resultSize == 8);
          return local::getThunk(t, squareRootDoubleThunk);

        case Float2Float:
          assert(t, resultSize == 4);
          return local::getThunk(t, doubleToFloatThunk);

        case Float2Int:
          if (resultSize == 8) {
            return local::getThunk(t, doubleToLongThunk);
          } else {
            assert(t, resultSize == 4);
            return local::getThunk(t, doubleToIntThunk);
          }

        case Int2Float:
          if (resultSize == 8) {
            return local::getThunk(t, longToDoubleThunk);
          } else {
            assert(t, resultSize == 4);
            return local::getThunk(t, longToFloatThunk);
          }
          
        default: abort(t);
        }
      } else {
        assert(t, size == 4);

        switch(op) {
        case Absolute:
          assert(t, resultSize == 4);
          return local::getThunk(t, absoluteIntThunk);

        case FloatNegate:
          assert(t, resultSize == 4);
          return local::getThunk(t, negateFloatThunk);

        case FloatAbsolute:
          assert(t, resultSize == 4);
          return local::getThunk(t, absoluteFloatThunk);

        case Float2Float:
          assert(t, resultSize == 8);
          return local::getThunk(t, floatToDoubleThunk);

        case Float2Int:
          if (resultSize == 4) {
            return local::getThunk(t, floatToIntThunk);
          } else {
            assert(t, resultSize == 8);
            return local::getThunk(t, floatToLongThunk);
          }

        case Int2Float:
          if (resultSize == 4) {
            return local::getThunk(t, intToFloatThunk);
          } else {
            assert(t, resultSize == 8);
            return local::getThunk(t, intToDoubleThunk);
          }
          
        default: abort(t);
        }
      }
    }

    virtual intptr_t getThunk(TernaryOperation op, unsigned size, unsigned,
                              bool* threadParameter)
    {
      *threadParameter = false;

      if (size == 8) {
        switch (op) {
        case Divide:
          *threadParameter = true;
          return local::getThunk(t, divideLongThunk);

        case Remainder:
          *threadParameter = true;
          return local::getThunk(t, moduloLongThunk);

        case FloatAdd:
          return local::getThunk(t, addDoubleThunk);

        case FloatSubtract:
          return local::getThunk(t, subtractDoubleThunk);

        case FloatMultiply:
          return local::getThunk(t, multiplyDoubleThunk);

        case FloatDivide:
          return local::getThunk(t, divideDoubleThunk);

        case FloatRemainder:
          return local::getThunk(t, moduloDoubleThunk);

        case JumpIfFloatEqual:
        case JumpIfFloatNotEqual:
        case JumpIfFloatLess:
        case JumpIfFloatGreater:
        case JumpIfFloatLessOrEqual:
        case JumpIfFloatGreaterOrUnordered:
        case JumpIfFloatGreaterOrEqualOrUnordered:
          return local::getThunk(t, compareDoublesGThunk);

        case JumpIfFloatGreaterOrEqual:
        case JumpIfFloatLessOrUnordered:
        case JumpIfFloatLessOrEqualOrUnordered:
          return local::getThunk(t, compareDoublesLThunk);

        default: abort(t);
        }
      } else {
        assert(t, size == 4);
        switch (op) {
        case Divide:
          *threadParameter = true;
          return local::getThunk(t, divideIntThunk);

        case Remainder:
          *threadParameter = true;
          return local::getThunk(t, moduloIntThunk);

        case FloatAdd:
          return local::getThunk(t, addFloatThunk);

        case FloatSubtract:
          return local::getThunk(t, subtractFloatThunk);

        case FloatMultiply:
          return local::getThunk(t, multiplyFloatThunk);

        case FloatDivide:
          return local::getThunk(t, divideFloatThunk);

        case FloatRemainder:
          return local::getThunk(t, moduloFloatThunk);

        case JumpIfFloatEqual:
        case JumpIfFloatNotEqual:
        case JumpIfFloatLess:
        case JumpIfFloatGreater:
        case JumpIfFloatLessOrEqual:
        case JumpIfFloatGreaterOrUnordered:
        case JumpIfFloatGreaterOrEqualOrUnordered:
          return local::getThunk(t, compareFloatsGThunk);

        case JumpIfFloatGreaterOrEqual:
        case JumpIfFloatLessOrUnordered:
        case JumpIfFloatLessOrEqualOrUnordered:
          return local::getThunk(t, compareFloatsLThunk);

        default: abort(t);
        }
      }
    }

    MyThread* t;
  };

  Context(MyThread* t, BootContext* bootContext, object method):
    thread(t),
    zone(t->m->system, t->m->heap, InitialZoneCapacityInBytes),
    assembler(makeAssembler(t->m->system, t->m->heap, &zone, t->arch)),
    client(t),
    compiler(makeCompiler(t->m->system, assembler, &zone, &client)),
    method(method),
    bootContext(bootContext),
    objectPool(0),
    subroutines(0),
    traceLog(0),
    visitTable(makeVisitTable(t, &zone, method)),
    rootTable(makeRootTable(t, &zone, method)),
    subroutineTable(0),
    executableAllocator(0),
    executableStart(0),
    executableSize(0),
    objectPoolCount(0),
    traceLogCount(0),
    dirtyRoots(false),
    leaf(true),
    eventLog(t->m->system, t->m->heap, 1024),
    protector(this),
    resource(this)
  { }

  Context(MyThread* t):
    thread(t),
    zone(t->m->system, t->m->heap, InitialZoneCapacityInBytes),
    assembler(makeAssembler(t->m->system, t->m->heap, &zone, t->arch)),
    client(t),
    compiler(0),
    method(0),
    bootContext(0),
    objectPool(0),
    subroutines(0),
    traceLog(0),
    visitTable(0),
    rootTable(0),
    subroutineTable(0),
    executableAllocator(0),
    executableStart(0),
    executableSize(0),
    objectPoolCount(0),
    traceLogCount(0),
    dirtyRoots(false),
    leaf(true),
    eventLog(t->m->system, t->m->heap, 0),
    protector(this),
    resource(this)
  { }

  ~Context() {
    dispose();
  }

  void dispose() {
    if (compiler) {
      compiler->dispose();
    }

    assembler->dispose();

    if (executableAllocator) {
      executableAllocator->free(executableStart, executableSize);
    }

    eventLog.dispose();

    zone.dispose();
  }

  MyThread* thread;
  Zone zone;
  Assembler* assembler;
  MyClient client;
  Compiler* compiler;
  object method;
  BootContext* bootContext;
  PoolElement* objectPool;
  Subroutine* subroutines;
  TraceElement* traceLog;
  uint16_t* visitTable;
  uintptr_t* rootTable;
  Subroutine** subroutineTable;
  Allocator* executableAllocator;
  void* executableStart;
  unsigned executableSize;
  unsigned objectPoolCount;
  unsigned traceLogCount;
  bool dirtyRoots;
  bool leaf;
  Vector eventLog;
  MyProtector protector;
  MyResource resource;
};

unsigned
translateLocalIndex(Context* context, unsigned footprint, unsigned index)
{
  unsigned parameterFootprint = methodParameterFootprint
    (context->thread, context->method);

  if (index < parameterFootprint) {
    return parameterFootprint - index - footprint;
  } else {
    return index;
  }
}

Compiler::Operand*
loadLocal(Context* context, unsigned footprint, unsigned index)
{
  return context->compiler->loadLocal
    (footprint, translateLocalIndex(context, footprint, index));
}

void
storeLocal(Context* context, unsigned footprint, Compiler::Operand* value,
           unsigned index)
{
  context->compiler->storeLocal
    (footprint, value, translateLocalIndex(context, footprint, index));
}

FixedAllocator*
codeAllocator(MyThread* t);

class Frame {
 public:
  enum StackType {
    Integer,
    Long,
    Object
  };

  Frame(Context* context, uint8_t* stackMap):
    context(context),
    t(context->thread),
    c(context->compiler),
    subroutine(0),
    stackMap(stackMap),
    ip(0),
    sp(localSize()),
    level(0)
  {
    memset(stackMap, 0, codeMaxStack(t, methodCode(t, context->method)));
  }

  Frame(Frame* f, uint8_t* stackMap):
    context(f->context),
    t(context->thread),
    c(context->compiler),
    subroutine(f->subroutine),
    stackMap(stackMap),
    ip(f->ip),
    sp(f->sp),
    level(f->level + 1)
  {
    memcpy(stackMap, f->stackMap, codeMaxStack
           (t, methodCode(t, context->method)));

    if (level > 1) {
      context->eventLog.append(PushContextEvent);
    }
  }

  ~Frame() {
    if (level > 1) {
      context->eventLog.append(PopContextEvent);      
    }
  }

  Compiler::Operand* append(object o) {
    BootContext* bc = context->bootContext;
    if (bc) {
      Promise* p = new (bc->zone->allocate(sizeof(ListenPromise)))
        ListenPromise(t->m->system, bc->zone);

      PROTECT(t, o);
      object pointer = makePointer(t, p);
      bc->constants = makeTriple(t, o, pointer, bc->constants);

      return c->add
        (TargetBytesPerWord, c->memory
          (c->register_(t->arch->thread()), Compiler::AddressType,
           TargetThreadHeapImage), c->promiseConstant
         (p, Compiler::AddressType));
    } else {
      for (PoolElement* e = context->objectPool; e; e = e->next) {
        if (o == e->target) {
          return c->address(e);
        }
      }

      context->objectPool = new
        (context->zone.allocate(sizeof(PoolElement)))
        PoolElement(t, o, context->objectPool);

      ++ context->objectPoolCount;

      return c->address(context->objectPool);
    }
  }

  unsigned localSize() {
    return local::localSize(t, context->method);
  }

  unsigned stackSize() {
    return codeMaxStack(t, methodCode(t, context->method));
  }

  unsigned frameSize() {
    return localSize() + stackSize();
  }

  void set(unsigned index, uint8_t type) {
    assert(t, index < frameSize());

    if (type == Object) {
      context->eventLog.append(MarkEvent);
      context->eventLog.append2(index);
    } else {
      context->eventLog.append(ClearEvent);
      context->eventLog.append2(index);
    }

    int si = index - localSize();
    if (si >= 0) {
      stackMap[si] = type;
    }
  }

  uint8_t get(unsigned index) {
    assert(t, index < frameSize());
    int si = index - localSize();
    assert(t, si >= 0);
    return stackMap[si];
  }

  void pushedInt() {
    assert(t, sp + 1 <= frameSize());
    set(sp++, Integer);
  }

  void pushedLong() {
    assert(t, sp + 2 <= frameSize());
    set(sp++, Long);
    set(sp++, Long);
  }

  void pushedObject() {
    assert(t, sp + 1 <= frameSize());
    set(sp++, Object);
  }

  void popped(unsigned count) {
    assert(t, sp >= count);
    assert(t, sp - count >= localSize());
    while (count) {
      set(--sp, Integer);
      -- count;
    }
  }
  
  void poppedInt() {
    assert(t, sp >= 1);
    assert(t, sp - 1 >= localSize());
    assert(t, get(sp - 1) == Integer);
    -- sp;
  }
  
  void poppedLong() {
    assert(t, sp >= 1);
    assert(t, sp - 2 >= localSize());
    assert(t, get(sp - 1) == Long);
    assert(t, get(sp - 2) == Long);
    sp -= 2;
  }
  
  void poppedObject() {
    assert(t, sp >= 1);
    assert(t, sp - 1 >= localSize());
    assert(t, get(sp - 1) == Object);
    set(--sp, Integer);
  }

  void storedInt(unsigned index) {
    assert(t, index < localSize());
    set(index, Integer);
  }

  void storedLong(unsigned index) {
    assert(t, index + 1 < localSize());
    set(index, Long);
    set(index + 1, Long);
  }

  void storedObject(unsigned index) {
    assert(t, index < localSize());
    set(index, Object);
  }

  void dupped() {
    assert(t, sp + 1 <= frameSize());
    assert(t, sp - 1 >= localSize());
    set(sp, get(sp - 1));
    ++ sp;
  }

  void duppedX1() {
    assert(t, sp + 1 <= frameSize());
    assert(t, sp - 2 >= localSize());

    uint8_t b2 = get(sp - 2);
    uint8_t b1 = get(sp - 1);

    set(sp - 1, b2);
    set(sp - 2, b1);
    set(sp    , b1);

    ++ sp;
  }

  void duppedX2() {
    assert(t, sp + 1 <= frameSize());
    assert(t, sp - 3 >= localSize());

    uint8_t b3 = get(sp - 3);
    uint8_t b2 = get(sp - 2);
    uint8_t b1 = get(sp - 1);

    set(sp - 2, b3);
    set(sp - 1, b2);
    set(sp - 3, b1);
    set(sp    , b1);

    ++ sp;
  }

  void dupped2() {
    assert(t, sp + 2 <= frameSize());
    assert(t, sp - 2 >= localSize());

    uint8_t b2 = get(sp - 2);
    uint8_t b1 = get(sp - 1);

    set(sp, b2);
    set(sp + 1, b1);

    sp += 2;
  }

  void dupped2X1() {
    assert(t, sp + 2 <= frameSize());
    assert(t, sp - 3 >= localSize());

    uint8_t b3 = get(sp - 3);
    uint8_t b2 = get(sp - 2);
    uint8_t b1 = get(sp - 1);

    set(sp - 1, b3);
    set(sp - 3, b2);
    set(sp    , b2);
    set(sp - 2, b1);
    set(sp + 1, b1);

    sp += 2;
  }

  void dupped2X2() {
    assert(t, sp + 2 <= frameSize());
    assert(t, sp - 4 >= localSize());

    uint8_t b4 = get(sp - 4);
    uint8_t b3 = get(sp - 3);
    uint8_t b2 = get(sp - 2);
    uint8_t b1 = get(sp - 1);

    set(sp - 2, b4);
    set(sp - 1, b3);
    set(sp - 4, b2);
    set(sp    , b2);
    set(sp - 3, b1);
    set(sp + 1, b1);

    sp += 2;
  }

  void swapped() {
    assert(t, sp - 2 >= localSize());

    uint8_t saved = get(sp - 1);

    set(sp - 1, get(sp - 2));
    set(sp - 2, saved);
  }

  Promise* addressPromise(Promise* p) {
    BootContext* bc = context->bootContext;
    if (bc) {
      bc->addresses = new (bc->zone->allocate(sizeof(DelayedPromise)))
        DelayedPromise(t->m->system, bc->zone, p, bc->addresses);
      return bc->addresses;
    } else {
      return p;
    }
  }

  Compiler::Operand* addressOperand(Promise* p) {
    return c->promiseConstant(p, Compiler::AddressType);
  }

  Compiler::Operand* absoluteAddressOperand(Promise* p) {
    return context->bootContext
      ? c->add
        (TargetBytesPerWord, c->memory
         (c->register_(t->arch->thread()), Compiler::AddressType,
          TargetThreadCodeImage), c->promiseConstant
         (new (context->zone.allocate(sizeof(OffsetPromise)))
          OffsetPromise
          (p, - reinterpret_cast<intptr_t>(codeAllocator(t)->base)),
          Compiler::AddressType))
      : addressOperand(p);
  }

  Compiler::Operand* machineIp(unsigned logicalIp) {
    return c->promiseConstant(c->machineIp(logicalIp), Compiler::AddressType);
  }

  void visitLogicalIp(unsigned ip) {
    c->visitLogicalIp(ip);

    context->eventLog.append(IpEvent);
    context->eventLog.append2(ip);
  }

  void startLogicalIp(unsigned ip) {
    if (subroutine) {
      context->subroutineTable[ip] = subroutine;
    }

    c->startLogicalIp(ip);

    context->eventLog.append(IpEvent);
    context->eventLog.append2(ip);

    this->ip = ip;
  }

  void pushQuiet(unsigned footprint, Compiler::Operand* o) {
    c->push(footprint, o);
  }

  void pushLongQuiet(Compiler::Operand* o) {
    pushQuiet(2, o);
  }

  Compiler::Operand* popQuiet(unsigned footprint) {
    return c->pop(footprint);
  }

  Compiler::Operand* popLongQuiet() {
    Compiler::Operand* r = popQuiet(2);

    return r;
  }

  void pushInt(Compiler::Operand* o) {
    pushQuiet(1, o);
    pushedInt();
  }

  void pushAddress(Compiler::Operand* o) {
    pushQuiet(1, o);
    pushedInt();
  }

  void pushObject(Compiler::Operand* o) {
    pushQuiet(1, o);
    pushedObject();
  }

  void pushObject() {
    c->pushed();

    pushedObject();
  }

  void pushLong(Compiler::Operand* o) {
    pushLongQuiet(o);
    pushedLong();
  }

  void pop(unsigned count) {
    popped(count);
    c->popped(count);
  }

  Compiler::Operand* popInt() {
    poppedInt();
    return popQuiet(1);
  }

  Compiler::Operand* popLong() {
    poppedLong();
    return popLongQuiet();
  }
  
  Compiler::Operand* popObject() {
    poppedObject();
    return popQuiet(1);
  }

  void loadInt(unsigned index) {
    assert(t, index < localSize());
    pushInt(loadLocal(context, 1, index));
  }

  void loadLong(unsigned index) {
    assert(t, index < static_cast<unsigned>(localSize() - 1));
    pushLong(loadLocal(context, 2, index));
  }

  void loadObject(unsigned index) {
    assert(t, index < localSize());
    pushObject(loadLocal(context, 1, index));
  }

  void storeInt(unsigned index) {
    storeLocal(context, 1, popInt(), index);
    storedInt(translateLocalIndex(context, 1, index));
  }

  void storeLong(unsigned index) {
    storeLocal(context, 2, popLong(), index);
    storedLong(translateLocalIndex(context, 2, index));
  }

  void storeObject(unsigned index) {
    storeLocal(context, 1, popObject(), index);
    storedObject(translateLocalIndex(context, 1, index));
  }

  void storeObjectOrAddress(unsigned index) {
    storeLocal(context, 1, popQuiet(1), index);

    assert(t, sp >= 1);
    assert(t, sp - 1 >= localSize());
    if (get(sp - 1) == Object) {
      storedObject(translateLocalIndex(context, 1, index));
    } else {
      storedInt(translateLocalIndex(context, 1, index));
    }

    popped(1);
  }

  void dup() {
    pushQuiet(1, c->peek(1, 0));

    dupped();
  }

  void dupX1() {
    Compiler::Operand* s0 = popQuiet(1);
    Compiler::Operand* s1 = popQuiet(1);

    pushQuiet(1, s0);
    pushQuiet(1, s1);
    pushQuiet(1, s0);

    duppedX1();
  }

  void dupX2() {
    Compiler::Operand* s0 = popQuiet(1);

    if (get(sp - 2) == Long) {
      Compiler::Operand* s1 = popLongQuiet();

      pushQuiet(1, s0);
      pushLongQuiet(s1);
      pushQuiet(1, s0);
    } else {
      Compiler::Operand* s1 = popQuiet(1);
      Compiler::Operand* s2 = popQuiet(1);

      pushQuiet(1, s0);
      pushQuiet(1, s2);
      pushQuiet(1, s1);
      pushQuiet(1, s0);
    }

    duppedX2();
  }

  void dup2() {
    if (get(sp - 1) == Long) {
      pushLongQuiet(c->peek(2, 0));
    } else {
      Compiler::Operand* s0 = popQuiet(1);
      Compiler::Operand* s1 = popQuiet(1);

      pushQuiet(1, s1);
      pushQuiet(1, s0);
      pushQuiet(1, s1);
      pushQuiet(1, s0);
    }

    dupped2();
  }

  void dup2X1() {
    if (get(sp - 1) == Long) {
      Compiler::Operand* s0 = popLongQuiet();
      Compiler::Operand* s1 = popQuiet(1);

      pushLongQuiet(s0);
      pushQuiet(1, s1);
      pushLongQuiet(s0);
    } else {
      Compiler::Operand* s0 = popQuiet(1);
      Compiler::Operand* s1 = popQuiet(1);
      Compiler::Operand* s2 = popQuiet(1);

      pushQuiet(1, s1);
      pushQuiet(1, s0);
      pushQuiet(1, s2);
      pushQuiet(1, s1);
      pushQuiet(1, s0);
    }

    dupped2X1();
  }

  void dup2X2() {
    if (get(sp - 1) == Long) {
      Compiler::Operand* s0 = popLongQuiet();

      if (get(sp - 3) == Long) {
        Compiler::Operand* s1 = popLongQuiet();

        pushLongQuiet(s0);
        pushLongQuiet(s1);
        pushLongQuiet(s0);
      } else {
        Compiler::Operand* s1 = popQuiet(1);
        Compiler::Operand* s2 = popQuiet(1);

        pushLongQuiet(s0);
        pushQuiet(1, s2);
        pushQuiet(1, s1);
        pushLongQuiet(s0);
      }
    } else {
      Compiler::Operand* s0 = popQuiet(1);
      Compiler::Operand* s1 = popQuiet(1);
      Compiler::Operand* s2 = popQuiet(1);
      Compiler::Operand* s3 = popQuiet(1);

      pushQuiet(1, s1);
      pushQuiet(1, s0);
      pushQuiet(1, s3);
      pushQuiet(1, s2);
      pushQuiet(1, s1);
      pushQuiet(1, s0);
    }

    dupped2X2();
  }

  void swap() {
    Compiler::Operand* s0 = popQuiet(1);
    Compiler::Operand* s1 = popQuiet(1);

    pushQuiet(1, s0);
    pushQuiet(1, s1);

    swapped();
  }

  TraceElement* trace(object target, unsigned flags) {
    unsigned mapSize = frameMapSizeInWords(t, context->method);

    TraceElement* e = context->traceLog = new
      (context->zone.allocate(sizeof(TraceElement) + (mapSize * BytesPerWord)))
      TraceElement(context, ip, target, flags, context->traceLog, mapSize);

    ++ context->traceLogCount;

    context->eventLog.append(TraceEvent);
    context->eventLog.appendAddress(e);

    return e;
  }

  unsigned startSubroutine(unsigned ip, Promise* returnAddress) {
    pushAddress(absoluteAddressOperand(returnAddress));

    Subroutine* subroutine = 0;
    for (Subroutine* s = context->subroutines; s; s = s->listNext) {
      if (s->ip == ip) {
        subroutine = s;
        break;
      }
    }

    if (subroutine == 0) {
      context->subroutines = subroutine = new
        (context->zone.allocate(sizeof(Subroutine)))
        Subroutine(ip, context->eventLog.length() + 1 + BytesPerWord + 2,
                   context->subroutines, this->subroutine);

      if (context->subroutineTable == 0) {
        unsigned size = codeLength(t, methodCode(t, context->method))
          * sizeof(Subroutine*);

        context->subroutineTable = static_cast<Subroutine**>
          (context->zone.allocate(size));

        memset(context->subroutineTable, 0, size);
      }
    }

    subroutine->handle = c->startSubroutine();
    this->subroutine = subroutine;

    SubroutineCall* call = new
      (context->zone.allocate(sizeof(SubroutineCall)))
      SubroutineCall(subroutine, returnAddress);

    context->eventLog.append(PushSubroutineEvent);
    context->eventLog.appendAddress(call);

    unsigned nextIndexIndex = context->eventLog.length();
    context->eventLog.append2(0);

    c->saveLocals();

    return nextIndexIndex;
  }

  void returnFromSubroutine(unsigned returnAddressLocal) {
    c->returnFromSubroutine
      (subroutine->handle, loadLocal(context, 1, returnAddressLocal));

    subroutine->stackIndex = localOffsetFromStack
      (t, translateLocalIndex(context, 1, returnAddressLocal),
       context->method);
  }

  void endSubroutine(unsigned nextIndexIndex) {
    c->linkSubroutine(subroutine->handle);

    poppedInt();

    context->eventLog.append(PopSubroutineEvent);

    context->eventLog.set2(nextIndexIndex, context->eventLog.length());

    subroutine = subroutine->stackNext;
  }
  
  Context* context;
  MyThread* t;
  Compiler* c;
  Subroutine* subroutine;
  uint8_t* stackMap;
  unsigned ip;
  unsigned sp;
  unsigned level;
};

unsigned
savedTargetIndex(MyThread* t, object method)
{
  return codeMaxLocals(t, methodCode(t, method));
}

object
findCallNode(MyThread* t, void* address);

void
insertCallNode(MyThread* t, object node);

void*
findExceptionHandler(Thread* t, object method, void* ip)
{
  if (t->exception) {
    object table = codeExceptionHandlerTable(t, methodCode(t, method));
    if (table) {
      object index = arrayBody(t, table, 0);
      
      uint8_t* compiled = reinterpret_cast<uint8_t*>
        (methodCompiled(t, method));

      for (unsigned i = 0; i < arrayLength(t, table) - 1; ++i) {
        unsigned start = intArrayBody(t, index, i * 3);
        unsigned end = intArrayBody(t, index, (i * 3) + 1);
        unsigned key = difference(ip, compiled) - 1;

        if (key >= start and key < end) {
          object catchType = arrayBody(t, table, i + 1);

          if (exceptionMatch(t, catchType, t->exception)) {
            return compiled + intArrayBody(t, index, (i * 3) + 2);
          }
        }
      }
    }
  }

  return 0;
}

void
releaseLock(MyThread* t, object method, void* stack)
{
  if (methodFlags(t, method) & ACC_SYNCHRONIZED) {
    if (t->methodLockIsClean) {
      object lock;
      if (methodFlags(t, method) & ACC_STATIC) {
        lock = methodClass(t, method);
      } else {
        lock = *localObject
          (t, stackForFrame(t, stack, method), method,
           savedTargetIndex(t, method));
      }
    
      release(t, lock);
    } else {
      // got an exception while trying to acquire the lock for a
      // synchronized method -- don't try to release it, since we
      // never succeeded in acquiring it.
      t->methodLockIsClean = true;
    }
  }
}

void
findUnwindTarget(MyThread* t, void** targetIp, void** targetFrame,
                 void** targetStack, object* targetContinuation)
{
  void* ip;
  void* stack;
  object continuation;

  if (t->traceContext) {
    ip = t->traceContext->ip;
    stack = t->traceContext->stack;
    continuation = t->traceContext->continuation;
  } else {
    ip = getIp(t);
    stack = t->stack;
    continuation = t->continuation;      
  }

  object target = t->trace->targetMethod;

  *targetIp = 0;
  while (*targetIp == 0) {
    object method = methodForIp(t, ip);
    if (method) {
      void* handler = findExceptionHandler(t, method, ip);

      if (handler) {
        *targetIp = handler;

        nextFrame(t, &ip, &stack, method, target);

        void** sp = static_cast<void**>(stackForFrame(t, stack, method))
          + t->arch->frameReturnAddressSize();

        *targetFrame = static_cast<void**>
          (stack) + t->arch->framePointerOffset();
        *targetStack = sp;
        *targetContinuation = continuation;

        sp[localOffset(t, localSize(t, method), method)] = t->exception;

        t->exception = 0;
      } else {
        nextFrame(t, &ip, &stack, method, target);

        if (t->exception) {
          releaseLock(t, method, stack);
        }

        target = method;
      }
    } else {
      expect(t, ip);
      *targetIp = ip;
      *targetFrame = 0;
      *targetStack = static_cast<void**>(stack)
        + t->arch->frameReturnAddressSize();
      *targetContinuation = continuation;

      while (Continuations and *targetContinuation) {
        object c = *targetContinuation;

        object method = continuationMethod(t, c);

        void* handler = findExceptionHandler
          (t, method, continuationAddress(t, c));

        if (handler) {
          t->exceptionHandler = handler;

          t->exceptionStackAdjustment 
            = (stackOffsetFromFrame(t, method)
               - ((continuationFramePointerOffset(t, c) / BytesPerWord)
                  - t->arch->framePointerOffset()
                  + t->arch->frameReturnAddressSize())) * BytesPerWord;

          t->exceptionOffset
            = localOffset(t, localSize(t, method), method) * BytesPerWord;

          break;
        } else if (t->exception) {
          releaseLock(t, method,
                      reinterpret_cast<uint8_t*>(c)
                      + ContinuationBody
                      + continuationReturnAddressOffset(t, c)
                      - t->arch->returnAddressOffset());
        }

        *targetContinuation = continuationNext(t, c);
      }
    }
  }
}

object
makeCurrentContinuation(MyThread* t, void** targetIp, void** targetStack)
{
  void* ip = getIp(t);
  void* stack = t->stack;

  object context = t->continuation
    ? continuationContext(t, t->continuation)
    : makeContinuationContext(t, 0, 0, 0, 0, t->trace->originalMethod);
  PROTECT(t, context);

  object target = t->trace->targetMethod;
  PROTECT(t, target);

  object first = 0;
  PROTECT(t, first);

  object last = 0;
  PROTECT(t, last);

  *targetIp = 0;
  while (*targetIp == 0) {
    object method = methodForIp(t, ip);
    if (method) {
      PROTECT(t, method);

      void** top = static_cast<void**>(stack)
        + t->arch->frameReturnAddressSize()
        + t->arch->frameFooterSize();
      unsigned argumentFootprint
        = t->arch->argumentFootprint(methodParameterFootprint(t, target));
      unsigned alignment = t->arch->stackAlignmentInWords();
      if (TailCalls and argumentFootprint > alignment) {
        top += argumentFootprint - alignment;
      }

      void* nextIp = ip;
      nextFrame(t, &nextIp, &stack, method, target);

      void** bottom = static_cast<void**>(stack)
          + t->arch->frameReturnAddressSize();
      unsigned frameSize = bottom - top;
      unsigned totalSize = frameSize
        + t->arch->frameFooterSize()
        + t->arch->argumentFootprint(methodParameterFootprint(t, method));

      object c = makeContinuation
        (t, 0, context, method, ip,
         (frameSize
          + t->arch->frameFooterSize()
          + t->arch->returnAddressOffset()
          - t->arch->frameReturnAddressSize()) * BytesPerWord,
         (frameSize
          + t->arch->frameFooterSize()
          + t->arch->framePointerOffset()
          - t->arch->frameReturnAddressSize()) * BytesPerWord,
         totalSize);

      memcpy(&continuationBody(t, c, 0), top, totalSize * BytesPerWord);

      if (last) {
        set(t, last, ContinuationNext, c);
      } else {
        first = c;
      }
      last = c;

      ip = nextIp;

      target = method;
    } else {
      *targetIp = ip;
      *targetStack = static_cast<void**>(stack)
        + t->arch->frameReturnAddressSize();
    }
  }

  expect(t, last);
  set(t, last, ContinuationNext, t->continuation);

  return first;
}

void NO_RETURN
unwind(MyThread* t)
{
  void* ip;
  void* frame;
  void* stack;
  object continuation;
  findUnwindTarget(t, &ip, &frame, &stack, &continuation);

  t->trace->targetMethod = 0;
  t->trace->nativeMethod = 0;

  transition(t, ip, stack, continuation, t->trace);

  vmJump(ip, frame, stack, t, 0, 0);
}

class MyCheckpoint: public Thread::Checkpoint {
 public:
  MyCheckpoint(MyThread* t): Checkpoint(t) { }

  virtual void unwind() {
    local::unwind(static_cast<MyThread*>(t));
  }
};

uintptr_t
defaultThunk(MyThread* t);

uintptr_t
nativeThunk(MyThread* t);

uintptr_t
bootNativeThunk(MyThread* t);

uintptr_t
aioobThunk(MyThread* t);

uintptr_t
stackOverflowThunk(MyThread* t);

uintptr_t
virtualThunk(MyThread* t, unsigned index);

bool
unresolved(MyThread* t, uintptr_t methodAddress);

uintptr_t
methodAddress(Thread* t, object method)
{
  if (methodFlags(t, method) & ACC_NATIVE) {
    return bootNativeThunk(static_cast<MyThread*>(t));
  } else {
    return methodCompiled(t, method);
  }
}

void
tryInitClass(MyThread* t, object class_)
{
  initClass(t, class_);
}

void
compile(MyThread* t, FixedAllocator* allocator, BootContext* bootContext,
        object method);

object
resolveMethod(Thread* t, object pair)
{
  object reference = pairSecond(t, pair);
  PROTECT(t, reference);

  object class_ = resolveClassInObject
    (t, classLoader(t, methodClass(t, pairFirst(t, pair))), reference,
     ReferenceClass);

  return findInHierarchy
    (t, class_, referenceName(t, reference), referenceSpec(t, reference),
     findMethodInClass, Machine::NoSuchMethodErrorType);
}

bool
methodAbstract(Thread* t, object method)
{
  return methodCode(t, method) == 0
    and (methodFlags(t, method) & ACC_NATIVE) == 0;
}

int64_t
prepareMethodForCall(MyThread* t, object target)
{
  if (methodAbstract(t, target)) {
    throwNew(t, Machine::AbstractMethodErrorType, "%s.%s%s",
             &byteArrayBody(t, className(t, methodClass(t, target)), 0),
             &byteArrayBody(t, methodName(t, target), 0),
             &byteArrayBody(t, methodSpec(t, target), 0));
  } else { 
    if (unresolved(t, methodAddress(t, target))) {
      PROTECT(t, target);
      
      compile(t, codeAllocator(t), 0, target);
    }

    if (methodFlags(t, target) & ACC_NATIVE) {
      t->trace->nativeMethod = target;
    }

    return methodAddress(t, target);
  }
}

int64_t
findInterfaceMethodFromInstance(MyThread* t, object method, object instance)
{
  if (instance) {
    return prepareMethodForCall
      (t, findInterfaceMethod(t, method, objectClass(t, instance)));
  } else {
    throwNew(t, Machine::NullPointerExceptionType);
  }
}

int64_t
findInterfaceMethodFromInstanceAndReference
(MyThread* t, object pair, object instance)
{
  PROTECT(t, instance);

  object method = resolveMethod(t, pair);

  return findInterfaceMethodFromInstance(t, method, instance);
}

int64_t
findSpecialMethodFromReference(MyThread* t, object pair)
{
  PROTECT(t, pair);

  object target = resolveMethod(t, pair);

  object class_ = methodClass(t, pairFirst(t, pair));
  if (isSpecialMethod(t, target, class_)) {
    target = findVirtualMethod(t, target, classSuper(t, class_));
  }

  assert(t, (methodFlags(t, target) & ACC_STATIC) == 0);

  return prepareMethodForCall(t, target);
}

int64_t
findStaticMethodFromReference(MyThread* t, object pair)
{
  object target = resolveMethod(t, pair);

  assert(t, methodFlags(t, target) & ACC_STATIC);

  return prepareMethodForCall(t, target);
}

int64_t
findVirtualMethodFromReference(MyThread* t, object pair, object instance)
{
  PROTECT(t, instance);

  object target = resolveMethod(t, pair);

  target = findVirtualMethod(t, target, objectClass(t, instance));

  assert(t, (methodFlags(t, target) & ACC_STATIC) == 0);

  return prepareMethodForCall(t, target);
}

int64_t
getMethodAddress(MyThread* t, object target)
{
  return prepareMethodForCall(t, target);
}

int64_t
getJClassFromReference(MyThread* t, object pair)
{
  return reinterpret_cast<intptr_t>
    (getJClass
     (t, resolveClass
      (t, classLoader(t, methodClass(t, pairFirst(t, pair))),
       referenceName(t, pairSecond(t, pair)))));
}

int64_t
compareDoublesG(uint64_t bi, uint64_t ai)
{
  double a = bitsToDouble(ai);
  double b = bitsToDouble(bi);
  
  if (a < b) {
    return -1;
  } else if (a > b) {
    return 1;
  } else if (a == b) {
    return 0;
  } else {
    return 1;
  }
}

int64_t
compareDoublesL(uint64_t bi, uint64_t ai)
{
  double a = bitsToDouble(ai);
  double b = bitsToDouble(bi);
  
  if (a < b) {
    return -1;
  } else if (a > b) {
    return 1;
  } else if (a == b) {
    return 0;
  } else {
    return -1;
  }
}

int64_t
compareFloatsG(uint32_t bi, uint32_t ai)
{
  float a = bitsToFloat(ai);
  float b = bitsToFloat(bi);
  
  if (a < b) {
    return -1;
  } else if (a > b) {
    return 1;
  } else if (a == b) {
    return 0;
  } else {
    return 1;
  }
}

int64_t
compareFloatsL(uint32_t bi, uint32_t ai)
{
  float a = bitsToFloat(ai);
  float b = bitsToFloat(bi);
  
  if (a < b) {
    return -1;
  } else if (a > b) {
    return 1;
  } else if (a == b) {
    return 0;
  } else {
    return -1;
  }
}

int64_t
compareLongs(uint64_t b, uint64_t a)
{
  if (a < b) {
    return -1;
  } else if (a > b) {
    return 1;
  } else {
    return 0;
  }
}

uint64_t
addDouble(uint64_t b, uint64_t a)
{
  return doubleToBits(bitsToDouble(a) + bitsToDouble(b));
}

uint64_t
subtractDouble(uint64_t b, uint64_t a)
{
  return doubleToBits(bitsToDouble(a) - bitsToDouble(b));
}

uint64_t
multiplyDouble(uint64_t b, uint64_t a)
{
  return doubleToBits(bitsToDouble(a) * bitsToDouble(b));
}

uint64_t
divideDouble(uint64_t b, uint64_t a)
{
  return doubleToBits(bitsToDouble(a) / bitsToDouble(b));
}

uint64_t
moduloDouble(uint64_t b, uint64_t a)
{
  return doubleToBits(fmod(bitsToDouble(a), bitsToDouble(b)));
}

uint64_t
negateDouble(uint64_t a)
{
  return doubleToBits(- bitsToDouble(a));
}

uint64_t
squareRootDouble(uint64_t a)
{
  return doubleToBits(sqrt(bitsToDouble(a)));
}

uint64_t
doubleToFloat(int64_t a)
{
  return floatToBits(static_cast<float>(bitsToDouble(a)));
}

int64_t
doubleToInt(int64_t a)
{
  return static_cast<int32_t>(bitsToDouble(a));
}

int64_t
doubleToLong(int64_t a)
{
  return static_cast<int64_t>(bitsToDouble(a));
}

uint64_t
addFloat(uint32_t b, uint32_t a)
{
  return floatToBits(bitsToFloat(a) + bitsToFloat(b));
}

uint64_t
subtractFloat(uint32_t b, uint32_t a)
{
  return floatToBits(bitsToFloat(a) - bitsToFloat(b));
}

uint64_t
multiplyFloat(uint32_t b, uint32_t a)
{
  return floatToBits(bitsToFloat(a) * bitsToFloat(b));
}

uint64_t
divideFloat(uint32_t b, uint32_t a)
{
  return floatToBits(bitsToFloat(a) / bitsToFloat(b));
}

uint64_t
moduloFloat(uint32_t b, uint32_t a)
{
  return floatToBits(fmod(bitsToFloat(a), bitsToFloat(b)));
}

uint64_t
negateFloat(uint32_t a)
{
  return floatToBits(- bitsToFloat(a));
}

uint64_t
absoluteFloat(uint32_t a)
{
  return floatToBits(fabsf(bitsToFloat(a)));
}

int64_t
absoluteLong(int64_t a)
{
  return a > 0 ? a : -a;
}

int64_t
absoluteInt(int32_t a)
{
  return a > 0 ? a : -a;
}

unsigned
traceSize(Thread* t)
{
  class Counter: public Processor::StackVisitor {
   public:
    Counter(): count(0) { }

    virtual bool visit(Processor::StackWalker*) {
      ++ count;
      return true;
    }

    unsigned count;
  } counter;

  t->m->processor->walkStack(t, &counter);

  return FixedSizeOfArray + (counter.count * ArrayElementSizeOfArray)
    + (counter.count * FixedSizeOfTraceElement);
}

void NO_RETURN
throwArithmetic(MyThread* t)
{
  if (ensure(t, FixedSizeOfArithmeticException + traceSize(t))) {
    atomicOr(&(t->flags), Thread::TracingFlag);
    THREAD_RESOURCE0(t, atomicAnd(&(t->flags), ~Thread::TracingFlag));

    throwNew(t, Machine::ArithmeticExceptionType); 
  } else {
    // not enough memory available for a new exception and stack trace
    // -- use a preallocated instance instead
    throw_(t, root(t, Machine::ArithmeticException));
  }
}

int64_t
divideLong(MyThread* t, int64_t b, int64_t a)
{
  if (LIKELY(b)) {
    return a / b;
  } else {
    throwArithmetic(t);
  }
}

int64_t
divideInt(MyThread* t, int32_t b, int32_t a)
{
  if (LIKELY(b)) {
    return a / b;
  } else {
    throwArithmetic(t);
  }
}

int64_t
moduloLong(MyThread* t, int64_t b, int64_t a)
{
  if (LIKELY(b)) {
    return a % b;
  } else {
    throwArithmetic(t);
  }
}

int64_t
moduloInt(MyThread* t, int32_t b, int32_t a)
{
  if (LIKELY(b)) {
    return a % b;
  } else {
    throwArithmetic(t);
  }
}

uint64_t
floatToDouble(int32_t a)
{
  return doubleToBits(static_cast<double>(bitsToFloat(a)));
}

int64_t
floatToInt(int32_t a)
{
  return static_cast<int32_t>(bitsToFloat(a));
}

int64_t
floatToLong(int32_t a)
{
  return static_cast<int64_t>(bitsToFloat(a));
}

uint64_t
intToDouble(int32_t a)
{
  return doubleToBits(static_cast<double>(a));
}

uint64_t
intToFloat(int32_t a)
{
  return floatToBits(static_cast<float>(a));
}

uint64_t
longToDouble(int64_t a)
{
  return doubleToBits(static_cast<double>(a));
}

uint64_t
longToFloat(int64_t a)
{
  return floatToBits(static_cast<float>(a));
}

uint64_t
makeBlankObjectArray(MyThread* t, object class_, int32_t length)
{
  if (length >= 0) {
    return reinterpret_cast<uint64_t>(makeObjectArray(t, class_, length));
  } else {
    throwNew(t, Machine::NegativeArraySizeExceptionType, "%d", length);
  }
}

uint64_t
makeBlankObjectArrayFromReference(MyThread* t, object pair,
                                  int32_t length)
{
  return makeBlankObjectArray
    (t, resolveClass
     (t, classLoader(t, methodClass(t, pairFirst(t, pair))),
      referenceName(t, pairSecond(t, pair))), length);
}

uint64_t
makeBlankArray(MyThread* t, unsigned type, int32_t length)
{
  if (length >= 0) {
    object (*constructor)(Thread*, uintptr_t);
    switch (type) {
    case T_BOOLEAN:
      constructor = makeBooleanArray;
      break;

    case T_CHAR:
      constructor = makeCharArray;
      break;

    case T_FLOAT:
      constructor = makeFloatArray;
      break;

    case T_DOUBLE:
      constructor = makeDoubleArray;
      break;

    case T_BYTE:
      constructor = makeByteArray;
      break;

    case T_SHORT:
      constructor = makeShortArray;
      break;

    case T_INT:
      constructor = makeIntArray;
      break;

    case T_LONG:
      constructor = makeLongArray;
      break;

    default: abort(t);
    }

    return reinterpret_cast<uintptr_t>(constructor(t, length));
  } else {
    throwNew(t, Machine::NegativeArraySizeExceptionType, "%d", length);
  }
}

uint64_t
lookUpAddress(int32_t key, uintptr_t* start, int32_t count,
              uintptr_t default_)
{
  int32_t bottom = 0;
  int32_t top = count;
  for (int32_t span = top - bottom; span; span = top - bottom) {
    int32_t middle = bottom + (span / 2);
    uintptr_t* p = start + (middle * 2);
    int32_t k = *p;

    if (key < k) {
      top = middle;
    } else if (key > k) {
      bottom = middle + 1;
    } else {
      return p[1];
    }
  }

  return default_;
}

void
setMaybeNull(MyThread* t, object o, unsigned offset, object value)
{
  if (LIKELY(o)) {
    set(t, o, offset, value);
  } else {
    throwNew(t, Machine::NullPointerExceptionType);
  }
}

void
acquireMonitorForObject(MyThread* t, object o)
{
  if (LIKELY(o)) {
    acquire(t, o);
  } else {
    throwNew(t, Machine::NullPointerExceptionType);
  }
}

void
acquireMonitorForObjectOnEntrance(MyThread* t, object o)
{
  if (LIKELY(o)) {
    t->methodLockIsClean = false;
    acquire(t, o);
    t->methodLockIsClean = true;
  } else {
    throwNew(t, Machine::NullPointerExceptionType);
  }
}

void
releaseMonitorForObject(MyThread* t, object o)
{
  if (LIKELY(o)) {
    release(t, o);
  } else {
    throwNew(t, Machine::NullPointerExceptionType);
  }
}

object
makeMultidimensionalArray2(MyThread* t, object class_, uintptr_t* countStack,
                           int32_t dimensions)
{
  PROTECT(t, class_);

  THREAD_RUNTIME_ARRAY(t, int32_t, counts, dimensions);
  for (int i = dimensions - 1; i >= 0; --i) {
    RUNTIME_ARRAY_BODY(counts)[i] = countStack[dimensions - i - 1];
    if (UNLIKELY(RUNTIME_ARRAY_BODY(counts)[i] < 0)) {
      throwNew(t, Machine::NegativeArraySizeExceptionType, "%d",
               RUNTIME_ARRAY_BODY(counts)[i]);
      return 0;
    }
  }

  object array = makeArray(t, RUNTIME_ARRAY_BODY(counts)[0]);
  setObjectClass(t, array, class_);
  PROTECT(t, array);

  populateMultiArray(t, array, RUNTIME_ARRAY_BODY(counts), 0, dimensions);

  return array;
}

uint64_t
makeMultidimensionalArray(MyThread* t, object class_, int32_t dimensions,
                          int32_t offset)
{
  return reinterpret_cast<uintptr_t>
    (makeMultidimensionalArray2
     (t, class_, static_cast<uintptr_t*>(t->stack) + offset, dimensions));
}

uint64_t
makeMultidimensionalArrayFromReference(MyThread* t, object pair,
                                       int32_t dimensions,
                                       int32_t offset)
{
  return makeMultidimensionalArray
    (t, resolveClass
     (t, classLoader(t, methodClass(t, pairFirst(t, pair))),
      referenceName(t, pairSecond(t, pair))), dimensions, offset);
}

void NO_RETURN
throwArrayIndexOutOfBounds(MyThread* t)
{
  if (ensure(t, FixedSizeOfArrayIndexOutOfBoundsException + traceSize(t))) {
    atomicOr(&(t->flags), Thread::TracingFlag);
    THREAD_RESOURCE0(t, atomicAnd(&(t->flags), ~Thread::TracingFlag));

    throwNew(t, Machine::ArrayIndexOutOfBoundsExceptionType); 
  } else {
    // not enough memory available for a new exception and stack trace
    // -- use a preallocated instance instead
    throw_(t, root(t, Machine::ArrayIndexOutOfBoundsException));
  }
}

void NO_RETURN
throwStackOverflow(MyThread* t)
{
  throwNew(t, Machine::StackOverflowErrorType); 
}

void NO_RETURN
throw_(MyThread* t, object o)
{
  if (LIKELY(o)) {
    vm::throw_(t, o);
  } else {
    throwNew(t, Machine::NullPointerExceptionType);
  }
}

void
checkCast(MyThread* t, object class_, object o)
{
  if (UNLIKELY(o and not isAssignableFrom(t, class_, objectClass(t, o)))) {
    throwNew
      (t, Machine::ClassCastExceptionType, "%s as %s",
       &byteArrayBody(t, className(t, objectClass(t, o)), 0),
       &byteArrayBody(t, className(t, class_), 0));
  }
}

void
checkCastFromReference(MyThread* t, object pair, object o)
{
  PROTECT(t, o);

  object c = resolveClass
    (t, classLoader(t, methodClass(t, pairFirst(t, pair))),
     referenceName(t, pairSecond(t, pair)));

  checkCast(t, c, o);
}

object
resolveField(Thread* t, object pair)
{
  object reference = pairSecond(t, pair);
  PROTECT(t, reference);

  object class_ = resolveClassInObject
    (t, classLoader(t, methodClass(t, pairFirst(t, pair))), reference,
     ReferenceClass);

  return findInHierarchy
    (t, class_, referenceName(t, reference), referenceSpec(t, reference),
     findFieldInClass, Machine::NoSuchFieldErrorType);
}

uint64_t
getFieldValue(Thread* t, object target, object field)
{
  switch (fieldCode(t, field)) {
  case ByteField:
  case BooleanField:
    return cast<int8_t>(target, fieldOffset(t, field));

  case CharField:
  case ShortField:
    return cast<int16_t>(target, fieldOffset(t, field));

  case FloatField:
  case IntField:
    return cast<int32_t>(target, fieldOffset(t, field));

  case DoubleField:
  case LongField:
    return cast<int64_t>(target, fieldOffset(t, field));

  case ObjectField:
    return cast<intptr_t>(target, fieldOffset(t, field));

  default:
    abort(t);
  }
}

uint64_t
getStaticFieldValueFromReference(MyThread* t, object pair)
{
  object field = resolveField(t, pair);
  PROTECT(t, field);

  initClass(t, fieldClass(t, field));

  ACQUIRE_FIELD_FOR_READ(t, field);

  return getFieldValue(t, classStaticTable(t, fieldClass(t, field)), field);
}

uint64_t
getFieldValueFromReference(MyThread* t, object pair, object instance)
{
  PROTECT(t, instance);

  object field = resolveField(t, pair);
  PROTECT(t, field);

  ACQUIRE_FIELD_FOR_READ(t, field);

  return getFieldValue(t, instance, field);
}

void
setStaticLongFieldValueFromReference(MyThread* t, object pair, uint64_t value)
{
  object field = resolveField(t, pair);
  PROTECT(t, field);

  initClass(t, fieldClass(t, field));

  ACQUIRE_FIELD_FOR_WRITE(t, field);

  cast<int64_t>
    (classStaticTable(t, fieldClass(t, field)), fieldOffset(t, field)) = value;
}

void
setLongFieldValueFromReference(MyThread* t, object pair, object instance,
                               uint64_t value)
{
  PROTECT(t, instance);

  object field = resolveField(t, pair);
  PROTECT(t, field);

  ACQUIRE_FIELD_FOR_WRITE(t, field);

  cast<int64_t>(instance, fieldOffset(t, field)) = value;
}

void
setStaticObjectFieldValueFromReference(MyThread* t, object pair, object value)
{
  PROTECT(t, value);

  object field = resolveField(t, pair);
  PROTECT(t, field);

  initClass(t, fieldClass(t, field));

  ACQUIRE_FIELD_FOR_WRITE(t, field);

  set(t, classStaticTable(t, fieldClass(t, field)), fieldOffset(t, field),
      value);
}

void
setObjectFieldValueFromReference(MyThread* t, object pair, object instance,
                                 object value)
{
  PROTECT(t, instance);
  PROTECT(t, value);

  object field = resolveField(t, pair);
  PROTECT(t, field);

  ACQUIRE_FIELD_FOR_WRITE(t, field);

  set(t, instance, fieldOffset(t, field), value);
}

void
setFieldValue(MyThread* t, object target, object field, uint32_t value)
{
  switch (fieldCode(t, field)) {
  case ByteField:
  case BooleanField:
    cast<int8_t>(target, fieldOffset(t, field)) = value;
    break;

  case CharField:
  case ShortField:
    cast<int16_t>(target, fieldOffset(t, field)) = value;
    break;

  case FloatField:
  case IntField:
    cast<int32_t>(target, fieldOffset(t, field)) = value;
    break;

  default:
    abort(t);
  }
}

void
setStaticFieldValueFromReference(MyThread* t, object pair, uint32_t value)
{
  object field = resolveField(t, pair);
  PROTECT(t, field);

  initClass(t, fieldClass(t, field));

  ACQUIRE_FIELD_FOR_WRITE(t, field);

  setFieldValue(t, classStaticTable(t, fieldClass(t, field)), field, value);
}

void
setFieldValueFromReference(MyThread* t, object pair, object instance,
                           uint32_t value)
{
  PROTECT(t, instance);
  object field = resolveField(t, pair);
  PROTECT(t, field);

  ACQUIRE_FIELD_FOR_WRITE(t, field);

  setFieldValue(t, instance, field, value);
}

uint64_t
instanceOf64(Thread* t, object class_, object o)
{
  return instanceOf(t, class_, o);
}

uint64_t
instanceOfFromReference(Thread* t, object pair, object o)
{
  PROTECT(t, o);

  object c = resolveClass
    (t, classLoader(t, methodClass(t, pairFirst(t, pair))),
     referenceName(t, pairSecond(t, pair)));

  return instanceOf64(t, c, o);
}

uint64_t
makeNewGeneral64(Thread* t, object class_)
{
  return reinterpret_cast<uintptr_t>(makeNewGeneral(t, class_));
}

uint64_t
makeNew64(Thread* t, object class_)
{
  return reinterpret_cast<uintptr_t>(makeNew(t, class_));
}

uint64_t
makeNewFromReference(Thread* t, object pair)
{
  return makeNewGeneral64
    (t, resolveClass
     (t, classLoader(t, methodClass(t, pairFirst(t, pair))),
      referenceName(t, pairSecond(t, pair))));
}

uint64_t
getJClass64(Thread* t, object class_)
{
  return reinterpret_cast<uintptr_t>(getJClass(t, class_));
}

void
gcIfNecessary(MyThread* t)
{
  stress(t);

  if (UNLIKELY(t->flags & Thread::UseBackupHeapFlag)) {
    collect(t, Heap::MinorCollection);
  }
}

unsigned
resultSize(MyThread* t, unsigned code)
{
  switch (code) {
  case ByteField:
  case BooleanField:
  case CharField:
  case ShortField:
  case FloatField:
  case IntField:
    return 4;

  case ObjectField:
    return TargetBytesPerWord;

  case LongField:
  case DoubleField:
    return 8;

  case VoidField:
    return 0;

  default:
    abort(t);
  }
}

void
pushReturnValue(MyThread* t, Frame* frame, unsigned code,
                Compiler::Operand* result)
{
  switch (code) {
  case ByteField:
  case BooleanField:
  case CharField:
  case ShortField:
  case FloatField:
  case IntField:
    return frame->pushInt(result);

  case ObjectField:
    return frame->pushObject(result);

  case LongField:
  case DoubleField:
    return frame->pushLong(result);

  default:
    abort(t);
  }
}

Compiler::Operand*
popField(MyThread* t, Frame* frame, int code)
{
  switch (code) {
  case ByteField:
  case BooleanField:
  case CharField:
  case ShortField:
  case FloatField:
  case IntField:
    return frame->popInt();

  case DoubleField:
  case LongField:
    return frame->popLong();

  case ObjectField:
    return frame->popObject();

  default: abort(t);
  }
}

Compiler::OperandType
operandTypeForFieldCode(Thread* t, unsigned code)
{
  switch (code) {
  case ByteField:
  case BooleanField:
  case CharField:
  case ShortField:
  case IntField:
  case LongField:
    return Compiler::IntegerType;

  case ObjectField:
    return Compiler::ObjectType;

  case FloatField:
  case DoubleField:
    return Compiler::FloatType;

  case VoidField:
    return Compiler::VoidType;

  default:
    abort(t);
  }
}

bool
useLongJump(MyThread* t, uintptr_t target)
{
  uintptr_t reach = t->arch->maximumImmediateJump();
  FixedAllocator* a = codeAllocator(t);
  uintptr_t start = reinterpret_cast<uintptr_t>(a->base);
  uintptr_t end = reinterpret_cast<uintptr_t>(a->base) + a->capacity;
  assert(t, end - start < reach);

  return (target > end && (target - start) > reach)
    or (target < start && (end - target) > reach);
}

Compiler::Operand*
compileDirectInvoke(MyThread* t, Frame* frame, object target, bool tailCall,
                    bool useThunk, unsigned rSize, Promise* addressPromise)
{
  Compiler* c = frame->c;

  unsigned flags = (TailCalls and tailCall ? Compiler::TailJump : 0);
  unsigned traceFlags;

  if (addressPromise == 0 and useLongJump(t, methodAddress(t, target))) {
    flags |= Compiler::LongJumpOrCall;
    traceFlags = TraceElement::LongCall;
  } else {
    traceFlags = 0;
  }

  if (useThunk
      or (TailCalls and tailCall and (methodFlags(t, target) & ACC_NATIVE)))
  {
    if (frame->context->bootContext == 0) {
      flags |= Compiler::Aligned;
    }

    if (TailCalls and tailCall) {
      traceFlags |= TraceElement::TailCall;

      TraceElement* trace = frame->trace(target, traceFlags);

      Promise* returnAddressPromise = new
        (frame->context->zone.allocate(sizeof(TraceElementPromise)))
        TraceElementPromise(t->m->system, trace);

      Compiler::Operand* result = c->stackCall
        (c->promiseConstant(returnAddressPromise, Compiler::AddressType),
         flags,
         trace,
         rSize,
         operandTypeForFieldCode(t, methodReturnCode(t, target)),
         methodParameterFootprint(t, target));

      c->store
        (TargetBytesPerWord,
         frame->absoluteAddressOperand(returnAddressPromise),
         TargetBytesPerWord, c->memory
         (c->register_(t->arch->thread()), Compiler::AddressType,
          TargetThreadTailAddress));

      c->exit
        (c->constant
         ((methodFlags(t, target) & ACC_NATIVE)
          ? nativeThunk(t) : defaultThunk(t),
          Compiler::AddressType));

      return result;
    } else {
      return c->stackCall
        (c->constant(defaultThunk(t), Compiler::AddressType),
         flags,
         frame->trace(target, traceFlags),
         rSize,
         operandTypeForFieldCode(t, methodReturnCode(t, target)),
         methodParameterFootprint(t, target));
    }
  } else {
    Compiler::Operand* address =
      (addressPromise
       ? c->promiseConstant(addressPromise, Compiler::AddressType)
       : c->constant(methodAddress(t, target), Compiler::AddressType));

    return c->stackCall
      (address,
       flags,
       tailCall ? 0 : frame->trace
       ((methodFlags(t, target) & ACC_NATIVE) ? target : 0, 0),
       rSize,
       operandTypeForFieldCode(t, methodReturnCode(t, target)),
       methodParameterFootprint(t, target));
  }
}

bool
compileDirectInvoke(MyThread* t, Frame* frame, object target, bool tailCall)
{
  unsigned rSize = resultSize(t, methodReturnCode(t, target));

  Compiler::Operand* result = 0;

  if (emptyMethod(t, target)) {
    tailCall = false;
  } else {
    BootContext* bc = frame->context->bootContext;
    if (bc) {
      if ((methodClass(t, target) == methodClass(t, frame->context->method)
           or (not classNeedsInit(t, methodClass(t, target))))
          and (not (TailCalls and tailCall
                    and (methodFlags(t, target) & ACC_NATIVE))))
      {
        Promise* p = new (bc->zone->allocate(sizeof(ListenPromise)))
          ListenPromise(t->m->system, bc->zone);

        PROTECT(t, target);
        object pointer = makePointer(t, p);
        bc->calls = makeTriple(t, target, pointer, bc->calls);

        result = compileDirectInvoke
          (t, frame, target, tailCall, false, rSize, p);
      } else {
        result = compileDirectInvoke
          (t, frame, target, tailCall, true, rSize, 0);
      }
    } else if (unresolved(t, methodAddress(t, target))
               or classNeedsInit(t, methodClass(t, target)))
    {
      result = compileDirectInvoke
        (t, frame, target, tailCall, true, rSize, 0);
    } else {
      result = compileDirectInvoke
        (t, frame, target, tailCall, false, rSize, 0);
    }
  }

  frame->pop(methodParameterFootprint(t, target));

  if (rSize) {
    pushReturnValue(t, frame, methodReturnCode(t, target), result);
  }

  return tailCall;
}

unsigned
methodReferenceParameterFootprint(Thread* t, object reference, bool isStatic)
{
  return parameterFootprint
    (t, reinterpret_cast<const char*>
     (&byteArrayBody(t, referenceSpec(t, reference), 0)), isStatic);
}

int
methodReferenceReturnCode(Thread* t, object reference)
{
  unsigned parameterCount;
  unsigned returnCode;
  scanMethodSpec
    (t, reinterpret_cast<const char*>
     (&byteArrayBody(t, referenceSpec(t, reference), 0)), &parameterCount,
     &returnCode);

  return returnCode;
}

void
compileReferenceInvoke(MyThread* t, Frame* frame, Compiler::Operand* method,
                       object reference, bool isStatic, bool tailCall)
{
  unsigned parameterFootprint
    = methodReferenceParameterFootprint(t, reference, isStatic);

  int returnCode = methodReferenceReturnCode(t, reference);

  unsigned rSize = resultSize(t, returnCode);

  Compiler::Operand* result = frame->c->stackCall
    (method,
     tailCall ? Compiler::TailJump : 0,
     frame->trace(0, 0),
     rSize,
     operandTypeForFieldCode(t, returnCode),
     parameterFootprint);

  frame->pop(parameterFootprint);

  if (rSize) {
    pushReturnValue(t, frame, returnCode, result);
  }
}

void
compileDirectReferenceInvoke(MyThread* t, Frame* frame, Thunk thunk,
                             object reference, bool isStatic, bool tailCall)
{
  Compiler* c = frame->c;

  PROTECT(t, reference);

  object pair = makePair(t, frame->context->method, reference);

  compileReferenceInvoke
    (t, frame, c->call
     (c->constant(getThunk(t, thunk), Compiler::AddressType),
      0,
      frame->trace(0, 0),
      TargetBytesPerWord,
      Compiler::AddressType,
      2, c->register_(t->arch->thread()), frame->append(pair)),
     reference, isStatic, tailCall);
}

void
compileAbstractInvoke(MyThread* t, Frame* frame, Compiler::Operand* method,
                      object target, bool tailCall)
{
  unsigned parameterFootprint = methodParameterFootprint(t, target);
  
  int returnCode = methodReturnCode(t, target);

  unsigned rSize = resultSize(t, returnCode);

  Compiler::Operand* result = frame->c->stackCall
    (method,
     tailCall ? Compiler::TailJump : 0,
     frame->trace(0, 0),
     rSize,
     operandTypeForFieldCode(t, returnCode),
     parameterFootprint);

  frame->pop(parameterFootprint);

  if (rSize) {
    pushReturnValue(t, frame, returnCode, result);
  }    
}

void
compileDirectAbstractInvoke(MyThread* t, Frame* frame, Thunk thunk,
                            object target, bool tailCall)
{
  Compiler* c = frame->c;

  compileAbstractInvoke
    (t, frame, c->call
     (c->constant(getThunk(t, thunk), Compiler::AddressType),
      0,
      frame->trace(0, 0),
      TargetBytesPerWord,
      Compiler::AddressType,
      2, c->register_(t->arch->thread()), frame->append(target)),
     target, tailCall);
}

void
handleMonitorEvent(MyThread* t, Frame* frame, intptr_t function)
{
  Compiler* c = frame->c;
  object method = frame->context->method;

  if (methodFlags(t, method) & ACC_SYNCHRONIZED) {
    Compiler::Operand* lock;
    if (methodFlags(t, method) & ACC_STATIC) {
      PROTECT(t, method);

      lock = frame->append(methodClass(t, method));
    } else {
      lock = loadLocal(frame->context, 1, savedTargetIndex(t, method));
    }
    
    c->call(c->constant(function, Compiler::AddressType),
            0,
            frame->trace(0, 0),
            0,
            Compiler::VoidType,
            2, c->register_(t->arch->thread()), lock);
  }
}

void
handleEntrance(MyThread* t, Frame* frame)
{
  object method = frame->context->method;

  if ((methodFlags(t, method) & (ACC_SYNCHRONIZED | ACC_STATIC))
      == ACC_SYNCHRONIZED)
  {
    // save 'this' pointer in case it is overwritten.
    unsigned index = savedTargetIndex(t, method);
    storeLocal(frame->context, 1, loadLocal(frame->context, 1, 0), index);
    frame->set(index, Frame::Object);
  }

  handleMonitorEvent
    (t, frame, getThunk(t, acquireMonitorForObjectOnEntranceThunk));
}

void
handleExit(MyThread* t, Frame* frame)
{
  handleMonitorEvent
    (t, frame, getThunk(t, releaseMonitorForObjectThunk));
}

bool
inTryBlock(MyThread* t, object code, unsigned ip)
{
  object table = codeExceptionHandlerTable(t, code);
  if (table) {
    unsigned length = exceptionHandlerTableLength(t, table);
    for (unsigned i = 0; i < length; ++i) {
      uint64_t eh = exceptionHandlerTableBody(t, table, i);
      if (ip >= exceptionHandlerStart(eh)
          and ip < exceptionHandlerEnd(eh))
      {
        return true;
      }
    }
  }
  return false;
}

bool
needsReturnBarrier(MyThread* t, object method)
{
  return (methodFlags(t, method) & ConstructorFlag)
    and (classVmFlags(t, methodClass(t, method)) & HasFinalMemberFlag);
}

bool
returnsNext(MyThread* t, object code, unsigned ip)
{
  switch (codeBody(t, code, ip)) {
  case return_:
  case areturn:
  case ireturn:
  case freturn:
  case lreturn:
  case dreturn:
    return true;

  case goto_: {
    uint32_t offset = codeReadInt16(t, code, ++ip);
    uint32_t newIp = (ip - 3) + offset;
    assert(t, newIp < codeLength(t, code));

    return returnsNext(t, code, newIp);
  }

  case goto_w: {
    uint32_t offset = codeReadInt32(t, code, ++ip);
    uint32_t newIp = (ip - 5) + offset;
    assert(t, newIp < codeLength(t, code));
    
    return returnsNext(t, code, newIp);
  }

  default:
    return false;
  }
}

bool
isTailCall(MyThread* t, object code, unsigned ip, object caller,
           int calleeReturnCode)
{
  return TailCalls
    and ((methodFlags(t, caller) & ACC_SYNCHRONIZED) == 0)
    and (not inTryBlock(t, code, ip - 1))
    and (not needsReturnBarrier(t, caller))
    and (methodReturnCode(t, caller) == VoidField
         or methodReturnCode(t, caller) == calleeReturnCode)
    and returnsNext(t, code, ip);
}

bool
isTailCall(MyThread* t, object code, unsigned ip, object caller, object callee)
{
  return isTailCall(t, code, ip, caller, methodReturnCode(t, callee));
}

bool
isReferenceTailCall(MyThread* t, object code, unsigned ip, object caller,
                    object calleeReference)
{
  return isTailCall
    (t, code, ip, caller, methodReferenceReturnCode(t, calleeReference));
}

void
compile(MyThread* t, Frame* initialFrame, unsigned ip,
        int exceptionHandlerStart = -1);

void
saveStateAndCompile(MyThread* t, Frame* initialFrame, unsigned ip)
{
  Compiler::State* state = initialFrame->c->saveState();
  compile(t, initialFrame, ip);
  initialFrame->c->restoreState(state);
}

bool
integerBranch(MyThread* t, Frame* frame, object code, unsigned& ip,
              unsigned size, Compiler::Operand* a, Compiler::Operand* b)
{
  if (ip + 3 > codeLength(t, code)) {
    return false;
  }

  Compiler* c = frame->c;
  unsigned instruction = codeBody(t, code, ip++);
  uint32_t offset = codeReadInt16(t, code, ip);
  uint32_t newIp = (ip - 3) + offset;
  assert(t, newIp < codeLength(t, code));
  
  Compiler::Operand* target = frame->machineIp(newIp);

  switch (instruction) {
  case ifeq:
    c->jumpIfEqual(size, a, b, target);
    break;

  case ifne:
    c->jumpIfNotEqual(size, a, b, target);
    break;

  case ifgt:
    c->jumpIfGreater(size, a, b, target);
    break;

  case ifge:
    c->jumpIfGreaterOrEqual(size, a, b, target);
    break;

  case iflt:
    c->jumpIfLess(size, a, b, target);
    break;

  case ifle:
    c->jumpIfLessOrEqual(size, a, b, target);
    break;

  default:
    ip -= 3;
    return false;
  }

  saveStateAndCompile(t, frame, newIp);
  return true;
}

bool
floatBranch(MyThread* t, Frame* frame, object code, unsigned& ip,
            unsigned size, bool lessIfUnordered, Compiler::Operand* a,
            Compiler::Operand* b)
{
  if (ip + 3 > codeLength(t, code)) {
    return false;
  }

  Compiler* c = frame->c;
  unsigned instruction = codeBody(t, code, ip++);
  uint32_t offset = codeReadInt16(t, code, ip);
  uint32_t newIp = (ip - 3) + offset;
  assert(t, newIp < codeLength(t, code));
  
  Compiler::Operand* target = frame->machineIp(newIp);

  switch (instruction) {
  case ifeq:
    c->jumpIfFloatEqual(size, a, b, target);
    break;

  case ifne:
    c->jumpIfFloatNotEqual(size, a, b, target);
    break;

  case ifgt:
    if (lessIfUnordered) {
      c->jumpIfFloatGreater(size, a, b, target);
    } else {
      c->jumpIfFloatGreaterOrUnordered(size, a, b, target);
    }
    break;

  case ifge:
    if (lessIfUnordered) {
      c->jumpIfFloatGreaterOrEqual(size, a, b, target);
    } else {
      c->jumpIfFloatGreaterOrEqualOrUnordered(size, a, b, target);
    }
    break;

  case iflt:
    if (lessIfUnordered) {
      c->jumpIfFloatLessOrUnordered(size, a, b, target);
    } else {
      c->jumpIfFloatLess(size, a, b, target);
    }
    break;

  case ifle:
    if (lessIfUnordered) {
      c->jumpIfFloatLessOrEqualOrUnordered(size, a, b, target);
    } else {
      c->jumpIfFloatLessOrEqual(size, a, b, target);
    }
    break;

  default:
    ip -= 3;
    return false;
  }

  saveStateAndCompile(t, frame, newIp);
  return true;
}

bool
intrinsic(MyThread* t, Frame* frame, object target)
{
#define MATCH(name, constant)                                           \
  (byteArrayLength(t, name) == sizeof(constant)                         \
   and ::strcmp(reinterpret_cast<char*>(&byteArrayBody(t, name, 0)),    \
                constant) == 0)

  object className = vm::className(t, methodClass(t, target));
  if (UNLIKELY(MATCH(className, "java/lang/Math"))) {
    Compiler* c = frame->c;
    if (MATCH(methodName(t, target), "sqrt")
        and MATCH(methodSpec(t, target), "(D)D"))
    {
      frame->pushLong(c->fsqrt(8, frame->popLong()));
      return true;
    } else if (MATCH(methodName(t, target), "abs")) {
      if (MATCH(methodSpec(t, target), "(I)I")) {
        frame->pushInt(c->abs(4, frame->popInt()));
        return true;
      } else if (MATCH(methodSpec(t, target), "(J)J")) {
        frame->pushLong(c->abs(8, frame->popLong()));
        return true;
      } else if (MATCH(methodSpec(t, target), "(F)F")) {
        frame->pushInt(c->fabs(4, frame->popInt()));
        return true;
      }
    }
  }
  return false;
}

unsigned
targetFieldOffset(Context* context, object field)
{
  if (context->bootContext) {
    return context->bootContext->resolver->fieldOffset(context->thread, field);
  } else {
    return fieldOffset(context->thread, field);
  }
}

void
compile(MyThread* t, Frame* initialFrame, unsigned ip,
        int exceptionHandlerStart)
{
  THREAD_RUNTIME_ARRAY(t, uint8_t, stackMap,
                codeMaxStack(t, methodCode(t, initialFrame->context->method)));
  Frame myFrame(initialFrame, RUNTIME_ARRAY_BODY(stackMap));
  Frame* frame = &myFrame;
  Compiler* c = frame->c;
  Context* context = frame->context;

  object code = methodCode(t, context->method);
  PROTECT(t, code);
  
  while (ip < codeLength(t, code)) {
    if (context->visitTable[ip] ++) {
      // we've already visited this part of the code
      frame->visitLogicalIp(ip);
      return;
    }

    frame->startLogicalIp(ip);

    if (exceptionHandlerStart >= 0) {
      c->initLocalsFromLogicalIp(exceptionHandlerStart);

      exceptionHandlerStart = -1;

      frame->pushObject();
      
      c->call
        (c->constant(getThunk(t, gcIfNecessaryThunk), Compiler::AddressType),
         0,
         frame->trace(0, 0),
         0,
         Compiler::VoidType,
         1, c->register_(t->arch->thread()));
    }
    
//     fprintf(stderr, "ip: %d map: %ld\n", ip, *(frame->map));

    unsigned instruction = codeBody(t, code, ip++);

    switch (instruction) {
    case aaload:
    case baload:
    case caload:
    case daload:
    case faload:
    case iaload:
    case laload:
    case saload: {
      Compiler::Operand* index = frame->popInt();
      Compiler::Operand* array = frame->popObject();

      if (inTryBlock(t, code, ip - 1)) {
        c->saveLocals();
        frame->trace(0, 0);
      }

      if (CheckArrayBounds) {
        c->checkBounds(array, TargetArrayLength, index, aioobThunk(t));
      }

      switch (instruction) {
      case aaload:
        frame->pushObject
          (c->load
           (TargetBytesPerWord, TargetBytesPerWord, c->memory
            (array, Compiler::ObjectType, TargetArrayBody, index,
             TargetBytesPerWord),
            TargetBytesPerWord));
        break;

      case faload:
        frame->pushInt
          (c->load
           (4, 4, c->memory
            (array, Compiler::FloatType, TargetArrayBody, index, 4),
            TargetBytesPerWord));
        break;

      case iaload:
        frame->pushInt
          (c->load
           (4, 4, c->memory
            (array, Compiler::IntegerType, TargetArrayBody, index, 4),
            TargetBytesPerWord));
        break;

      case baload:
        frame->pushInt
          (c->load
           (1, 1, c->memory
            (array, Compiler::IntegerType, TargetArrayBody, index, 1),
            TargetBytesPerWord));
        break;

      case caload:
        frame->pushInt
          (c->loadz
           (2, 2, c->memory
            (array, Compiler::IntegerType, TargetArrayBody, index, 2),
            TargetBytesPerWord));
        break;

      case daload:
        frame->pushLong
          (c->load
           (8, 8, c->memory
            (array, Compiler::FloatType, TargetArrayBody, index, 8), 8));
        break;

      case laload:
        frame->pushLong
          (c->load
           (8, 8, c->memory
            (array, Compiler::IntegerType, TargetArrayBody, index, 8), 8));
        break;

      case saload:
        frame->pushInt
          (c->load
           (2, 2, c->memory
            (array, Compiler::IntegerType, TargetArrayBody, index, 2),
            TargetBytesPerWord));
        break;
      }
    } break;

    case aastore:
    case bastore:
    case castore:
    case dastore:
    case fastore:
    case iastore:
    case lastore:
    case sastore: {
      Compiler::Operand* value;
      if (instruction == dastore or instruction == lastore) {
        value = frame->popLong();
      } else if (instruction == aastore) {
        value = frame->popObject();
      } else {
        value = frame->popInt();
      }

      Compiler::Operand* index = frame->popInt();
      Compiler::Operand* array = frame->popObject();

      if (inTryBlock(t, code, ip - 1)) {
        c->saveLocals();
        frame->trace(0, 0);
      }

      if (CheckArrayBounds) {
        c->checkBounds(array, TargetArrayLength, index, aioobThunk(t));
      }

      switch (instruction) {
      case aastore: {
        c->call
          (c->constant(getThunk(t, setMaybeNullThunk), Compiler::AddressType),
           0,
           frame->trace(0, 0),
           0,
           Compiler::VoidType,
           4, c->register_(t->arch->thread()), array,
           c->add
           (4, c->constant(TargetArrayBody, Compiler::IntegerType),
            c->shl
            (4, c->constant(log(TargetBytesPerWord), Compiler::IntegerType),
             index)),
           value);
      } break;

      case fastore:
        c->store
          (TargetBytesPerWord, value, 4, c->memory
           (array, Compiler::FloatType, TargetArrayBody, index, 4));
        break;

      case iastore:
        c->store
          (TargetBytesPerWord, value, 4, c->memory
           (array, Compiler::IntegerType, TargetArrayBody, index, 4));
        break;

      case bastore:
        c->store
          (TargetBytesPerWord, value, 1, c->memory
           (array, Compiler::IntegerType, TargetArrayBody, index, 1));
        break;

      case castore:
      case sastore:
        c->store
          (TargetBytesPerWord, value, 2, c->memory
           (array, Compiler::IntegerType, TargetArrayBody, index, 2));
        break;

      case dastore:
        c->store
          (8, value, 8, c->memory
           (array, Compiler::FloatType, TargetArrayBody, index, 8));
        break;

      case lastore:
        c->store
          (8, value, 8, c->memory
           (array, Compiler::IntegerType, TargetArrayBody, index, 8));
        break;
      }
    } break;

    case aconst_null:
      frame->pushObject(c->constant(0, Compiler::ObjectType));
      break;

    case aload:
      frame->loadObject(codeBody(t, code, ip++));
      break;

    case aload_0:
      frame->loadObject(0);
      break;

    case aload_1:
      frame->loadObject(1);
      break;

    case aload_2:
      frame->loadObject(2);
      break;

    case aload_3:
      frame->loadObject(3);
      break;

    case anewarray: {
      uint16_t index = codeReadInt16(t, code, ip);
      
      object reference = singletonObject
        (t, codePool(t, methodCode(t, context->method)), index - 1);

      PROTECT(t, reference);

      object class_ = resolveClassInPool(t, context->method, index - 1, false);

      Compiler::Operand* length = frame->popInt();

      object argument;
      Thunk thunk;
      if (LIKELY(class_)) {
        argument = class_;
        thunk = makeBlankObjectArrayThunk;
      } else {
        argument = makePair(t, context->method, reference);
        thunk = makeBlankObjectArrayFromReferenceThunk;
      }

      frame->pushObject
        (c->call
         (c->constant(getThunk(t, thunk), Compiler::AddressType),
          0,
          frame->trace(0, 0),
          TargetBytesPerWord,
          Compiler::ObjectType,
          3, c->register_(t->arch->thread()), frame->append(argument),
          length));
    } break;

    case areturn: {
      handleExit(t, frame);
      c->return_(TargetBytesPerWord, frame->popObject());
    } return;

    case arraylength: {
      frame->pushInt
        (c->load
         (TargetBytesPerWord, TargetBytesPerWord,
          c->memory
          (frame->popObject(), Compiler::IntegerType,
           TargetArrayLength, 0, 1),
          TargetBytesPerWord));
    } break;

    case astore:
      frame->storeObjectOrAddress(codeBody(t, code, ip++));
      break;

    case astore_0:
      frame->storeObjectOrAddress(0);
      break;

    case astore_1:
      frame->storeObjectOrAddress(1);
      break;

    case astore_2:
      frame->storeObjectOrAddress(2);
      break;

    case astore_3:
      frame->storeObjectOrAddress(3);
      break;

    case athrow: {
      Compiler::Operand* target = frame->popObject();
      c->call
        (c->constant(getThunk(t, throw_Thunk), Compiler::AddressType),
         Compiler::NoReturn,
         frame->trace(0, 0),
         0,
         Compiler::VoidType,
         2, c->register_(t->arch->thread()), target);
    } return;

    case bipush:
      frame->pushInt
        (c->constant
         (static_cast<int8_t>(codeBody(t, code, ip++)),
          Compiler::IntegerType));
      break;

    case checkcast: {
      uint16_t index = codeReadInt16(t, code, ip);

      object reference = singletonObject
        (t, codePool(t, methodCode(t, context->method)), index - 1);

      PROTECT(t, reference);

      object class_ = resolveClassInPool(t, context->method, index - 1, false);

      object argument;
      Thunk thunk;
      if (LIKELY(class_)) {
        argument = class_;
        thunk = checkCastThunk;
      } else {
        argument = makePair(t, context->method, reference);
        thunk = checkCastFromReferenceThunk;
      }

      Compiler::Operand* instance = c->peek(1, 0);

      c->call
        (c->constant(getThunk(t, thunk), Compiler::AddressType),
         0,
         frame->trace(0, 0),
         0,
         Compiler::VoidType,
         3, c->register_(t->arch->thread()), frame->append(argument),
         instance);
    } break;

    case d2f: {
        frame->pushInt(c->f2f(8, 4, frame->popLong()));
    } break;

    case d2i: {
      frame->pushInt(c->f2i(8, 4, frame->popLong()));
    } break;

    case d2l: {
      frame->pushLong(c->f2i(8, 8, frame->popLong()));
    } break;

    case dadd: {
      Compiler::Operand* a = frame->popLong();
      Compiler::Operand* b = frame->popLong();

      frame->pushLong(c->fadd(8, a, b));
    } break;

    case dcmpg: {
      Compiler::Operand* a = frame->popLong();
      Compiler::Operand* b = frame->popLong();

      if (not floatBranch(t, frame, code, ip, 8, false, a, b)) {
        frame->pushInt
          (c->call
           (c->constant
            (getThunk(t, compareDoublesGThunk), Compiler::AddressType),
            0, 0, 4, Compiler::IntegerType, 4,
            static_cast<Compiler::Operand*>(0), a,
            static_cast<Compiler::Operand*>(0), b));
      }
    } break;

    case dcmpl: {
      Compiler::Operand* a = frame->popLong();
      Compiler::Operand* b = frame->popLong();

      if (not floatBranch(t, frame, code, ip, 8, true, a, b)) {
        frame->pushInt
          (c->call
           (c->constant
            (getThunk(t, compareDoublesLThunk), Compiler::AddressType),
            0, 0, 4, Compiler::IntegerType, 4,
            static_cast<Compiler::Operand*>(0), a,
            static_cast<Compiler::Operand*>(0), b));
      }
    } break;

    case dconst_0:
      frame->pushLong(c->constant(doubleToBits(0.0), Compiler::FloatType));
      break;
      
    case dconst_1:
      frame->pushLong(c->constant(doubleToBits(1.0), Compiler::FloatType));
      break;

    case ddiv: {
      Compiler::Operand* a = frame->popLong();
      Compiler::Operand* b = frame->popLong();

      frame->pushLong(c->fdiv(8, a, b));
    } break;

    case dmul: {
      Compiler::Operand* a = frame->popLong();
      Compiler::Operand* b = frame->popLong();

      frame->pushLong(c->fmul(8, a, b));
    } break;

    case dneg: {
      frame->pushLong(c->fneg(8, frame->popLong()));
    } break;

    case vm::drem: {
      Compiler::Operand* a = frame->popLong();
      Compiler::Operand* b = frame->popLong();

      frame->pushLong(c->frem(8, a, b));
    } break;

    case dsub: {
      Compiler::Operand* a = frame->popLong();
      Compiler::Operand* b = frame->popLong();

      frame->pushLong(c->fsub(8, a, b));
    } break;

    case dup:
      frame->dup();
      break;

    case dup_x1:
      frame->dupX1();
      break;

    case dup_x2:
      frame->dupX2();
      break;

    case dup2:
      frame->dup2();
      break;

    case dup2_x1:
      frame->dup2X1();
      break;

    case dup2_x2:
      frame->dup2X2();
      break;

    case f2d: {
      frame->pushLong(c->f2f(4, 8, frame->popInt()));
    } break;

    case f2i: {
      frame->pushInt(c->f2i(4, 4, frame->popInt()));
    } break;

    case f2l: {
      frame->pushLong(c->f2i(4, 8, frame->popInt()));
    } break;

    case fadd: {
      Compiler::Operand* a = frame->popInt();
      Compiler::Operand* b = frame->popInt();

      frame->pushInt(c->fadd(4, a, b));
    } break;

    case fcmpg: {
      Compiler::Operand* a = frame->popInt();
      Compiler::Operand* b = frame->popInt();

      if (not floatBranch(t, frame, code, ip, 4, false, a, b)) {
        frame->pushInt
          (c->call
           (c->constant
            (getThunk(t, compareFloatsGThunk), Compiler::AddressType),
            0, 0, 4, Compiler::IntegerType, 2, a, b));
      }
    } break;

    case fcmpl: {
      Compiler::Operand* a = frame->popInt();
      Compiler::Operand* b = frame->popInt();

      if (not floatBranch(t, frame, code, ip, 4, true, a, b)) {
        frame->pushInt
          (c->call
           (c->constant
            (getThunk(t, compareFloatsLThunk), Compiler::AddressType),
            0, 0, 4, Compiler::IntegerType, 2, a, b));
      }
    } break;

    case fconst_0:
      frame->pushInt(c->constant(floatToBits(0.0), Compiler::FloatType));
      break;
      
    case fconst_1:
      frame->pushInt(c->constant(floatToBits(1.0), Compiler::FloatType));
      break;
      
    case fconst_2:
      frame->pushInt(c->constant(floatToBits(2.0), Compiler::FloatType));
      break;

    case fdiv: {
      Compiler::Operand* a = frame->popInt();
      Compiler::Operand* b = frame->popInt();

      frame->pushInt(c->fdiv(4, a, b));
    } break;

    case fmul: {
      Compiler::Operand* a = frame->popInt();
      Compiler::Operand* b = frame->popInt();

      frame->pushInt(c->fmul(4, a, b));
    } break;

    case fneg: {
      frame->pushInt(c->fneg(4, frame->popInt()));
    } break;

    case vm::frem: {
      Compiler::Operand* a = frame->popInt();
      Compiler::Operand* b = frame->popInt();

      frame->pushInt(c->frem(4, a, b));   	
    } break;

    case fsub: {
      Compiler::Operand* a = frame->popInt();
      Compiler::Operand* b = frame->popInt();

      frame->pushInt(c->fsub(4, a, b));
    } break;

    case getfield:
    case getstatic: {
      uint16_t index = codeReadInt16(t, code, ip);
        
      object reference = singletonObject
        (t, codePool(t, methodCode(t, context->method)), index - 1);

      PROTECT(t, reference);

      object field = resolveField(t, context->method, index - 1, false);

      if (LIKELY(field)) {
        if ((fieldFlags(t, field) & ACC_VOLATILE)
            and TargetBytesPerWord == 4
            and (fieldCode(t, field) == DoubleField
                 or fieldCode(t, field) == LongField))
        {
          PROTECT(t, field);

          c->call
            (c->constant
             (getThunk(t, acquireMonitorForObjectThunk),
              Compiler::AddressType),
             0, frame->trace(0, 0), 0, Compiler::VoidType, 2,
             c->register_(t->arch->thread()),
             frame->append(field));
        }

        Compiler::Operand* table;

        if (instruction == getstatic) {
          assert(t, fieldFlags(t, field) & ACC_STATIC);

          PROTECT(t, field);

          if (fieldClass(t, field) != methodClass(t, context->method)
              and classNeedsInit(t, fieldClass(t, field)))
          {
            c->call
              (c->constant
               (getThunk(t, tryInitClassThunk), Compiler::AddressType),
               0,
               frame->trace(0, 0),
               0,
               Compiler::VoidType,
               2, c->register_(t->arch->thread()),
               frame->append(fieldClass(t, field)));
          }

          table = frame->append(classStaticTable(t, fieldClass(t, field)));
        } else {
          assert(t, (fieldFlags(t, field) & ACC_STATIC) == 0);

          table = frame->popObject();

          if (inTryBlock(t, code, ip - 3)) {
            c->saveLocals();
            frame->trace(0, 0);
          }
        }

        switch (fieldCode(t, field)) {
        case ByteField:
        case BooleanField:
          frame->pushInt
            (c->load
             (1, 1, c->memory
              (table, Compiler::IntegerType, targetFieldOffset
               (context, field), 0, 1), TargetBytesPerWord));
          break;

        case CharField:
          frame->pushInt
            (c->loadz
             (2, 2, c->memory
              (table, Compiler::IntegerType, targetFieldOffset
               (context, field), 0, 1), TargetBytesPerWord));
          break;

        case ShortField:
          frame->pushInt
            (c->load
             (2, 2, c->memory
              (table, Compiler::IntegerType, targetFieldOffset
               (context, field), 0, 1), TargetBytesPerWord));
          break;

        case FloatField:
          frame->pushInt
            (c->load
             (4, 4, c->memory
              (table, Compiler::FloatType, targetFieldOffset
               (context, field), 0, 1), TargetBytesPerWord));
          break;

        case IntField:
          frame->pushInt
            (c->load
             (4, 4, c->memory
              (table, Compiler::IntegerType, targetFieldOffset
               (context, field), 0, 1), TargetBytesPerWord));
          break;

        case DoubleField:
          frame->pushLong
            (c->load
             (8, 8, c->memory
              (table, Compiler::FloatType, targetFieldOffset
               (context, field), 0, 1), 8));
          break;

        case LongField:
          frame->pushLong
            (c->load
             (8, 8, c->memory
              (table, Compiler::IntegerType, targetFieldOffset
               (context, field), 0, 1), 8));
          break;

        case ObjectField:
          frame->pushObject
            (c->load
             (TargetBytesPerWord, TargetBytesPerWord,
              c->memory
              (table, Compiler::ObjectType, targetFieldOffset
               (context, field), 0, 1), TargetBytesPerWord));
          break;

        default:
          abort(t);
        }

        if (fieldFlags(t, field) & ACC_VOLATILE) {
          if (TargetBytesPerWord == 4
              and (fieldCode(t, field) == DoubleField
                   or fieldCode(t, field) == LongField))
          {
            c->call
              (c->constant
               (getThunk(t, releaseMonitorForObjectThunk),
                Compiler::AddressType),
               0, frame->trace(0, 0), 0, Compiler::VoidType, 2,
               c->register_(t->arch->thread()),
               frame->append(field));
          } else {
            c->loadBarrier();
          }
        }
      } else {
        int fieldCode = vm::fieldCode
          (t, byteArrayBody(t, referenceSpec(t, reference), 0));

        object pair = makePair(t, context->method, reference);

        unsigned rSize = resultSize(t, fieldCode);
        Compiler::OperandType rType = operandTypeForFieldCode(t, fieldCode);

        Compiler::Operand* result;
        if (instruction == getstatic) {
          result = c->call
            (c->constant
             (getThunk(t, getStaticFieldValueFromReferenceThunk),
              Compiler::AddressType),
             0, frame->trace(0, 0), rSize, rType, 2,
             c->register_(t->arch->thread()), frame->append(pair));
        } else {
          Compiler::Operand* instance = frame->popObject();

          result = c->call
            (c->constant
             (getThunk(t, getFieldValueFromReferenceThunk),
              Compiler::AddressType),
             0, frame->trace(0, 0), rSize, rType, 3,
             c->register_(t->arch->thread()), frame->append(pair),
             instance);
        }

        pushReturnValue(t, frame, fieldCode, result);
      }
    } break;

    case goto_: {
      uint32_t offset = codeReadInt16(t, code, ip);
      uint32_t newIp = (ip - 3) + offset;
      assert(t, newIp < codeLength(t, code));

      c->jmp(frame->machineIp(newIp));
      ip = newIp;
    } break;

    case goto_w: {
      uint32_t offset = codeReadInt32(t, code, ip);
      uint32_t newIp = (ip - 5) + offset;
      assert(t, newIp < codeLength(t, code));

      c->jmp(frame->machineIp(newIp));
      ip = newIp;
    } break;

    case i2b: {
      frame->pushInt
        (c->load(TargetBytesPerWord, 1, frame->popInt(), TargetBytesPerWord));
    } break;

    case i2c: {
      frame->pushInt
        (c->loadz(TargetBytesPerWord, 2, frame->popInt(), TargetBytesPerWord));
    } break;

    case i2d: {
      frame->pushLong(c->i2f(4, 8, frame->popInt()));
    } break;

    case i2f: {
      frame->pushInt(c->i2f(4, 4, frame->popInt()));
    } break;

    case i2l:
      frame->pushLong(c->load(TargetBytesPerWord, 4, frame->popInt(), 8));
      break;

    case i2s: {
      frame->pushInt
        (c->load(TargetBytesPerWord, 2, frame->popInt(), TargetBytesPerWord));
    } break;
      
    case iadd: {
      Compiler::Operand* a = frame->popInt();
      Compiler::Operand* b = frame->popInt();
      frame->pushInt(c->add(4, a, b));
    } break;
      
    case iand: {
      Compiler::Operand* a = frame->popInt();
      Compiler::Operand* b = frame->popInt();
      frame->pushInt(c->and_(4, a, b));
    } break;

    case iconst_m1:
      frame->pushInt(c->constant(-1, Compiler::IntegerType));
      break;

    case iconst_0:
      frame->pushInt(c->constant(0, Compiler::IntegerType));
      break;

    case iconst_1:
      frame->pushInt(c->constant(1, Compiler::IntegerType));
      break;

    case iconst_2:
      frame->pushInt(c->constant(2, Compiler::IntegerType));
      break;

    case iconst_3:
      frame->pushInt(c->constant(3, Compiler::IntegerType));
      break;

    case iconst_4:
      frame->pushInt(c->constant(4, Compiler::IntegerType));
      break;

    case iconst_5:
      frame->pushInt(c->constant(5, Compiler::IntegerType));
      break;

    case idiv: {
      Compiler::Operand* a = frame->popInt();
      Compiler::Operand* b = frame->popInt();

      if (inTryBlock(t, code, ip - 1)) {
        c->saveLocals();
        frame->trace(0, 0);
      }

      frame->pushInt(c->div(4, a, b));
    } break;

    case if_acmpeq:
    case if_acmpne: {
      uint32_t offset = codeReadInt16(t, code, ip);
      uint32_t newIp = (ip - 3) + offset;
      assert(t, newIp < codeLength(t, code));
        
      Compiler::Operand* a = frame->popObject();
      Compiler::Operand* b = frame->popObject();
      Compiler::Operand* target = frame->machineIp(newIp);

      if (instruction == if_acmpeq) {
        c->jumpIfEqual(TargetBytesPerWord, a, b, target);
      } else {
        c->jumpIfNotEqual(TargetBytesPerWord, a, b, target);
      }

      saveStateAndCompile(t, frame, newIp);
    } break;

    case if_icmpeq:
    case if_icmpne:
    case if_icmpgt:
    case if_icmpge:
    case if_icmplt:
    case if_icmple: {
      uint32_t offset = codeReadInt16(t, code, ip);
      uint32_t newIp = (ip - 3) + offset;
      assert(t, newIp < codeLength(t, code));
        
      Compiler::Operand* a = frame->popInt();
      Compiler::Operand* b = frame->popInt();
      Compiler::Operand* target = frame->machineIp(newIp);

      switch (instruction) {
      case if_icmpeq:
        c->jumpIfEqual(4, a, b, target);
        break;
      case if_icmpne:
        c->jumpIfNotEqual(4, a, b, target);
        break;
      case if_icmpgt:
        c->jumpIfGreater(4, a, b, target);
        break;
      case if_icmpge:
        c->jumpIfGreaterOrEqual(4, a, b, target);
        break;
      case if_icmplt:
        c->jumpIfLess(4, a, b, target);
        break;
      case if_icmple:
        c->jumpIfLessOrEqual(4, a, b, target);
        break;
      default:
        abort(t);
      }
      
      saveStateAndCompile(t, frame, newIp);
    } break;

    case ifeq:
    case ifne:
    case ifgt:
    case ifge:
    case iflt:
    case ifle: {
      uint32_t offset = codeReadInt16(t, code, ip);
      uint32_t newIp = (ip - 3) + offset;
      assert(t, newIp < codeLength(t, code));

      Compiler::Operand* target = frame->machineIp(newIp);

      Compiler::Operand* a = c->constant(0, Compiler::IntegerType);
      Compiler::Operand* b = frame->popInt();

      switch (instruction) {
      case ifeq:
        c->jumpIfEqual(4, a, b, target);
        break;
      case ifne:
        c->jumpIfNotEqual(4, a, b, target);
        break;
      case ifgt:
        c->jumpIfGreater(4, a, b, target);
        break;
      case ifge:
        c->jumpIfGreaterOrEqual(4, a, b, target);
        break;
      case iflt:
        c->jumpIfLess(4, a, b, target);
        break;
      case ifle:
        c->jumpIfLessOrEqual(4, a, b, target);
        break;
      default:
        abort(t);
      }

      saveStateAndCompile(t, frame, newIp);
    } break;

    case ifnull:
    case ifnonnull: {
      uint32_t offset = codeReadInt16(t, code, ip);
      uint32_t newIp = (ip - 3) + offset;
      assert(t, newIp < codeLength(t, code));

      Compiler::Operand* a = c->constant(0, Compiler::ObjectType);
      Compiler::Operand* b = frame->popObject();
      Compiler::Operand* target = frame->machineIp(newIp);

      if (instruction == ifnull) {
        c->jumpIfEqual(TargetBytesPerWord, a, b, target);
      } else {
        c->jumpIfNotEqual(TargetBytesPerWord, a, b, target);
      }

      saveStateAndCompile(t, frame, newIp);
    } break;

    case iinc: {
      uint8_t index = codeBody(t, code, ip++);
      int8_t count = codeBody(t, code, ip++);

      storeLocal
        (context, 1,
         c->add
         (4, c->constant(count, Compiler::IntegerType),
          loadLocal(context, 1, index)),
         index);
    } break;

    case iload:
    case fload:
      frame->loadInt(codeBody(t, code, ip++));
      break;

    case iload_0:
    case fload_0:
      frame->loadInt(0);
      break;

    case iload_1:
    case fload_1:
      frame->loadInt(1);
      break;

    case iload_2:
    case fload_2:
      frame->loadInt(2);
      break;

    case iload_3:
    case fload_3:
      frame->loadInt(3);
      break;

    case imul: {
      Compiler::Operand* a = frame->popInt();
      Compiler::Operand* b = frame->popInt();
      frame->pushInt(c->mul(4, a, b));
    } break;

    case ineg: {
      frame->pushInt(c->neg(4, frame->popInt()));
    } break;

    case instanceof: {
      uint16_t index = codeReadInt16(t, code, ip);

      object reference = singletonObject
        (t, codePool(t, methodCode(t, context->method)), index - 1);

      PROTECT(t, reference);

      object class_ = resolveClassInPool(t, context->method, index - 1, false);

      Compiler::Operand* instance = frame->popObject();

      object argument;
      Thunk thunk;
      TraceElement* trace;
      if (LIKELY(class_)) {
        argument = class_;
        thunk = instanceOf64Thunk;
        trace = 0;
      } else {
        argument = makePair(t, context->method, reference);
        thunk = instanceOfFromReferenceThunk;
        trace = frame->trace(0, 0);
      }

      frame->pushInt
        (c->call
         (c->constant(getThunk(t, thunk), Compiler::AddressType),
          0, trace, 4, Compiler::IntegerType,
          3, c->register_(t->arch->thread()), frame->append(argument),
          instance));
    } break;

    case invokeinterface: {
      context->leaf = false;

      uint16_t index = codeReadInt16(t, code, ip);
      ip += 2;

      object reference = singletonObject
        (t, codePool(t, methodCode(t, context->method)), index - 1);

      PROTECT(t, reference);

      object target = resolveMethod(t, context->method, index - 1, false);

      object argument;
      Thunk thunk;
      unsigned parameterFootprint;
      int returnCode;
      bool tailCall;
      if (LIKELY(target)) {
        assert(t, (methodFlags(t, target) & ACC_STATIC) == 0);

        argument = target;
        thunk = findInterfaceMethodFromInstanceThunk;
        parameterFootprint = methodParameterFootprint(t, target);
        returnCode = methodReturnCode(t, target);
        tailCall = isTailCall(t, code, ip, context->method, target);
      } else {
        argument = makePair(t, context->method, reference);
        thunk = findInterfaceMethodFromInstanceAndReferenceThunk;
        parameterFootprint = methodReferenceParameterFootprint
          (t, reference, false);
        returnCode = methodReferenceReturnCode(t, reference);
        tailCall = isReferenceTailCall
          (t, code, ip, context->method, reference);
      }

      unsigned rSize = resultSize(t, returnCode);

      Compiler::Operand* result = c->stackCall
        (c->call
         (c->constant(getThunk(t, thunk), Compiler::AddressType),
          0,
          frame->trace(0, 0),
          TargetBytesPerWord,
          Compiler::AddressType,
          3, c->register_(t->arch->thread()), frame->append(argument),
          c->peek(1, parameterFootprint - 1)),
         tailCall ? Compiler::TailJump : 0,
         frame->trace(0, 0),
         rSize,
         operandTypeForFieldCode(t, returnCode),
         parameterFootprint);

      frame->pop(parameterFootprint);

      if (rSize) {
        pushReturnValue(t, frame, returnCode, result);
      }
    } break;

    case invokespecial: {
      context->leaf = false;

      uint16_t index = codeReadInt16(t, code, ip);

      object reference = singletonObject
        (t, codePool(t, methodCode(t, context->method)), index - 1);

      PROTECT(t, reference);

      object target = resolveMethod(t, context->method, index - 1, false);

      if (LIKELY(target)) {
        object class_ = methodClass(t, context->method);
        if (isSpecialMethod(t, target, class_)) {
          target = findVirtualMethod(t, target, classSuper(t, class_));
        }

        assert(t, (methodFlags(t, target) & ACC_STATIC) == 0);

        bool tailCall = isTailCall(t, code, ip, context->method, target);

        if (UNLIKELY(methodAbstract(t, target))) {
          compileDirectAbstractInvoke
            (t, frame, getMethodAddressThunk, target, tailCall);
        } else {
          compileDirectInvoke(t, frame, target, tailCall);
        }
      } else {
        compileDirectReferenceInvoke
          (t, frame, findSpecialMethodFromReferenceThunk, reference, false,
           isReferenceTailCall(t, code, ip, context->method, reference));
      }
    } break;

    case invokestatic: {
      context->leaf = false;

      uint16_t index = codeReadInt16(t, code, ip);

      object reference = singletonObject
        (t, codePool(t, methodCode(t, context->method)), index - 1);

      PROTECT(t, reference);

      object target = resolveMethod(t, context->method, index - 1, false);

      if (LIKELY(target)) {
        assert(t, methodFlags(t, target) & ACC_STATIC);

        if (not intrinsic(t, frame, target)) {
          bool tailCall = isTailCall(t, code, ip, context->method, target);
          compileDirectInvoke(t, frame, target, tailCall);
        }
      } else {
        compileDirectReferenceInvoke
          (t, frame, findStaticMethodFromReferenceThunk, reference, true,
           isReferenceTailCall(t, code, ip, context->method, reference));
      }
    } break;

    case invokevirtual: {
      context->leaf = false;

      uint16_t index = codeReadInt16(t, code, ip);

      object reference = singletonObject
        (t, codePool(t, methodCode(t, context->method)), index - 1);

      PROTECT(t, reference);

      object target = resolveMethod(t, context->method, index - 1, false);

      if (LIKELY(target)) {
        assert(t, (methodFlags(t, target) & ACC_STATIC) == 0);

        bool tailCall = isTailCall(t, code, ip, context->method, target);

        if (LIKELY(methodVirtual(t, target))) {
          unsigned parameterFootprint = methodParameterFootprint(t, target);

          unsigned offset = TargetClassVtable
            + (methodOffset(t, target) * TargetBytesPerWord);

          Compiler::Operand* instance = c->peek(1, parameterFootprint - 1);

          unsigned rSize = resultSize(t, methodReturnCode(t, target));

          Compiler::Operand* result = c->stackCall
            (c->memory
             (c->and_
              (TargetBytesPerWord, c->constant
               (TargetPointerMask, Compiler::IntegerType),
               c->memory(instance, Compiler::ObjectType, 0, 0, 1)),
              Compiler::ObjectType, offset, 0, 1),
             tailCall ? Compiler::TailJump : 0,
             frame->trace(0, 0),
             rSize,
             operandTypeForFieldCode(t, methodReturnCode(t, target)),
             parameterFootprint);

          frame->pop(parameterFootprint);

          if (rSize) {
            pushReturnValue(t, frame, methodReturnCode(t, target), result);
          }
        } else {
          // OpenJDK generates invokevirtual calls to private methods
          // (e.g. readObject and writeObject for serialization), so
          // we must handle such cases here.

          compileDirectInvoke(t, frame, target, tailCall);          
        }
      } else {
        PROTECT(t, reference);

        object pair = makePair(t, context->method, reference);

        compileReferenceInvoke
          (t, frame, c->call
           (c->constant(getThunk(t, findVirtualMethodFromReferenceThunk),
                        Compiler::AddressType),
            0,
            frame->trace(0, 0),
            TargetBytesPerWord,
            Compiler::AddressType,
            3, c->register_(t->arch->thread()), frame->append(pair),
            c->peek(1, methodReferenceParameterFootprint
                    (t, reference, false) - 1)),
           reference, false, isReferenceTailCall
           (t, code, ip, context->method, reference));
      }
    } break;

    case ior: {
      Compiler::Operand* a = frame->popInt();
      Compiler::Operand* b = frame->popInt();
      frame->pushInt(c->or_(4, a, b));
    } break;

    case irem: {
      Compiler::Operand* a = frame->popInt();
      Compiler::Operand* b = frame->popInt();

      if (inTryBlock(t, code, ip - 1)) {
        c->saveLocals();
        frame->trace(0, 0);
      }

      frame->pushInt(c->rem(4, a, b));
    } break;

    case ireturn:
    case freturn: {
      handleExit(t, frame);
      c->return_(4, frame->popInt());
    } return;

    case ishl: {
      Compiler::Operand* a = frame->popInt();
      Compiler::Operand* b = frame->popInt();
      frame->pushInt(c->shl(4, a, b));
    } break;

    case ishr: {
      Compiler::Operand* a = frame->popInt();
      Compiler::Operand* b = frame->popInt();
      frame->pushInt(c->shr(4, a, b));
    } break;

    case istore:
    case fstore:
      frame->storeInt(codeBody(t, code, ip++));
      break;

    case istore_0:
    case fstore_0:
      frame->storeInt(0);
      break;

    case istore_1:
    case fstore_1:
      frame->storeInt(1);
      break;

    case istore_2:
    case fstore_2:
      frame->storeInt(2);
      break;

    case istore_3:
    case fstore_3:
      frame->storeInt(3);
      break;

    case isub: {
      Compiler::Operand* a = frame->popInt();
      Compiler::Operand* b = frame->popInt();
      frame->pushInt(c->sub(4, a, b));
    } break;

    case iushr: {
      Compiler::Operand* a = frame->popInt();
      Compiler::Operand* b = frame->popInt();
      frame->pushInt(c->ushr(4, a, b));
    } break;

    case ixor: {
      Compiler::Operand* a = frame->popInt();
      Compiler::Operand* b = frame->popInt();
      frame->pushInt(c->xor_(4, a, b));
    } break;

    case jsr:
    case jsr_w: {
      uint32_t thisIp;
      uint32_t newIp;

      if (instruction == jsr) {
        uint32_t offset = codeReadInt16(t, code, ip);
        thisIp = ip - 3;
        newIp = thisIp + offset;
      } else {
        uint32_t offset = codeReadInt32(t, code, ip);
        thisIp = ip - 5;
        newIp = thisIp + offset;
      }

      assert(t, newIp < codeLength(t, code));

      unsigned start = frame->startSubroutine(newIp, c->machineIp(ip));

      c->jmp(frame->machineIp(newIp));

      saveStateAndCompile(t, frame, newIp);

      frame->endSubroutine(start);
    } break;

    case l2d: {
      frame->pushLong(c->i2f(8, 8, frame->popLong()));
    } break;

    case l2f: {
      frame->pushInt(c->i2f(8, 4, frame->popLong()));
    } break;

    case l2i:
      frame->pushInt(c->load(8, 8, frame->popLong(), TargetBytesPerWord));
      break;

    case ladd: {
      Compiler::Operand* a = frame->popLong();
      Compiler::Operand* b = frame->popLong();
      frame->pushLong(c->add(8, a, b));
    } break;

    case land: {
      Compiler::Operand* a = frame->popLong();
      Compiler::Operand* b = frame->popLong();
      frame->pushLong(c->and_(8, a, b));
    } break;

    case lcmp: {
      Compiler::Operand* a = frame->popLong();
      Compiler::Operand* b = frame->popLong();

      if (not integerBranch(t, frame, code, ip, 8, a, b)) {
        frame->pushInt
          (c->call
           (c->constant
            (getThunk(t, compareLongsThunk), Compiler::AddressType),
            0, 0, 4, Compiler::IntegerType, 4,
            static_cast<Compiler::Operand*>(0), a,
            static_cast<Compiler::Operand*>(0), b));
      }
    } break;

    case lconst_0:
      frame->pushLong(c->constant(0, Compiler::IntegerType));
      break;

    case lconst_1:
      frame->pushLong(c->constant(1, Compiler::IntegerType));
      break;

    case ldc:
    case ldc_w: {
      uint16_t index;

      if (instruction == ldc) {
        index = codeBody(t, code, ip++);
      } else {
        index = codeReadInt16(t, code, ip);
      }

      object pool = codePool(t, code);

      if (singletonIsObject(t, pool, index - 1)) {
        object v = singletonObject(t, pool, index - 1);

        loadMemoryBarrier();  

        if (objectClass(t, v) == type(t, Machine::ReferenceType)) {
          object reference = v;
          PROTECT(t, reference);

          v = resolveClassInPool(t, context->method, index - 1, false);

          if (UNLIKELY(v == 0)) {
            frame->pushObject
              (c->call
               (c->constant
                (getThunk(t, getJClassFromReferenceThunk),
                 Compiler::AddressType),
                0,
                frame->trace(0, 0),
                TargetBytesPerWord,
                Compiler::ObjectType,
                2, c->register_(t->arch->thread()),
                frame->append(makePair(t, context->method, reference))));
          }
        }

        if (v) {
          if (objectClass(t, v) == type(t, Machine::ClassType)) {
            frame->pushObject
              (c->call
               (c->constant
                (getThunk(t, getJClass64Thunk), Compiler::AddressType),
                0,
                frame->trace(0, 0),
                TargetBytesPerWord,
                Compiler::ObjectType,
                2, c->register_(t->arch->thread()), frame->append(v)));
          } else {
            frame->pushObject(frame->append(v));
          }
        }
      } else {
        frame->pushInt
          (c->constant
           (singletonValue(t, pool, index - 1),
            singletonBit(t, pool, poolSize(t, pool), index - 1)
            ? Compiler::FloatType : Compiler::IntegerType));
      }
    } break;

    case ldc2_w: {
      uint16_t index = codeReadInt16(t, code, ip);

      object pool = codePool(t, code);

      uint64_t v;
      memcpy(&v, &singletonValue(t, pool, index - 1), 8);
      frame->pushLong
        (c->constant
         (v, singletonBit(t, pool, poolSize(t, pool), index - 1)
          ? Compiler::FloatType : Compiler::IntegerType));
    } break;

    case ldiv_: {
      Compiler::Operand* a = frame->popLong();
      Compiler::Operand* b = frame->popLong();

      if (inTryBlock(t, code, ip - 1)) {
        c->saveLocals();
        frame->trace(0, 0);
      }

      frame->pushLong(c->div(8, a, b));
    } break;

    case lload:
    case dload:
      frame->loadLong(codeBody(t, code, ip++));
      break;

    case lload_0:
    case dload_0:
      frame->loadLong(0);
      break;

    case lload_1:
    case dload_1:
      frame->loadLong(1);
      break;

    case lload_2:
    case dload_2:
      frame->loadLong(2);
      break;

    case lload_3:
    case dload_3:
      frame->loadLong(3);
      break;

    case lmul: {
      Compiler::Operand* a = frame->popLong();
      Compiler::Operand* b = frame->popLong();
      frame->pushLong(c->mul(8, a, b));
    } break;

    case lneg:
      frame->pushLong(c->neg(8, frame->popLong()));
      break;

    case lookupswitch: {
      int32_t base = ip - 1;

      ip = (ip + 3) & ~3; // pad to four byte boundary

      Compiler::Operand* key = frame->popInt();
    
      uint32_t defaultIp = base + codeReadInt32(t, code, ip);
      assert(t, defaultIp < codeLength(t, code));

      Compiler::Operand* default_ = frame->addressOperand
        (frame->addressPromise(c->machineIp(defaultIp)));

      int32_t pairCount = codeReadInt32(t, code, ip);

      if (pairCount) {
        Promise* start = 0;
        THREAD_RUNTIME_ARRAY(t, uint32_t, ipTable, pairCount);
        for (int32_t i = 0; i < pairCount; ++i) {
          unsigned index = ip + (i * 8);
          int32_t key = codeReadInt32(t, code, index);
          uint32_t newIp = base + codeReadInt32(t, code, index);
          assert(t, newIp < codeLength(t, code));

          RUNTIME_ARRAY_BODY(ipTable)[i] = newIp;

          Promise* p = c->poolAppend(key);
          if (i == 0) {
            start = p;
          }
          c->poolAppendPromise
            (frame->addressPromise(c->machineIp(newIp)));
        }
        assert(t, start);

        Compiler::Operand* address = c->call
          (c->constant(getThunk(t, lookUpAddressThunk), Compiler::AddressType),
           0, 0, TargetBytesPerWord, Compiler::AddressType,
           4, key, frame->absoluteAddressOperand(start),
           c->constant(pairCount, Compiler::IntegerType), default_);

        c->jmp
          (context->bootContext ? c->add
           (TargetBytesPerWord, c->memory
            (c->register_(t->arch->thread()), Compiler::AddressType,
             TargetThreadCodeImage), address)
           : address);

        Compiler::State* state = c->saveState();

        for (int32_t i = 0; i < pairCount; ++i) {
          compile(t, frame, RUNTIME_ARRAY_BODY(ipTable)[i]);

          c->restoreState(state);
        }
      } else {
        // a switch statement with no cases, apparently
        c->jmp(default_);
      }

      ip = defaultIp;
    } break;

    case lor: {
      Compiler::Operand* a = frame->popLong();
      Compiler::Operand* b = frame->popLong();
      frame->pushLong(c->or_(8, a, b));
    } break;

    case lrem: {
      Compiler::Operand* a = frame->popLong();
      Compiler::Operand* b = frame->popLong();

      if (inTryBlock(t, code, ip - 1)) {
        c->saveLocals();
        frame->trace(0, 0);
      }

      frame->pushLong(c->rem(8, a, b));
    } break;

    case lreturn:
    case dreturn: {
      handleExit(t, frame);
      c->return_(8, frame->popLong());
    } return;

    case lshl: {
      Compiler::Operand* a = frame->popInt();
      Compiler::Operand* b = frame->popLong();
      frame->pushLong(c->shl(8, a, b));
    } break;

    case lshr: {
      Compiler::Operand* a = frame->popInt();
      Compiler::Operand* b = frame->popLong();
      frame->pushLong(c->shr(8, a, b));
    } break;

    case lstore:
    case dstore:
      frame->storeLong(codeBody(t, code, ip++));
      break;

    case lstore_0:
    case dstore_0:
      frame->storeLong(0);
      break;

    case lstore_1:
    case dstore_1:
      frame->storeLong(1);
      break;

    case lstore_2:
    case dstore_2:
      frame->storeLong(2);
      break;

    case lstore_3:
    case dstore_3:
      frame->storeLong(3);
      break;

    case lsub: {
      Compiler::Operand* a = frame->popLong();
      Compiler::Operand* b = frame->popLong();
      frame->pushLong(c->sub(8, a, b));
    } break;

    case lushr: {
      Compiler::Operand* a = frame->popInt();
      Compiler::Operand* b = frame->popLong();
      frame->pushLong(c->ushr(8, a, b));
    } break;

    case lxor: {
      Compiler::Operand* a = frame->popLong();
      Compiler::Operand* b = frame->popLong();
      frame->pushLong(c->xor_(8, a, b));
    } break;

    case monitorenter: {
      Compiler::Operand* target = frame->popObject();
      c->call
        (c->constant
         (getThunk(t, acquireMonitorForObjectThunk), Compiler::AddressType),
         0, frame->trace(0, 0), 0, Compiler::VoidType, 2,
         c->register_(t->arch->thread()), target);
    } break;

    case monitorexit: {
      Compiler::Operand* target = frame->popObject();
      c->call
        (c->constant
         (getThunk(t, releaseMonitorForObjectThunk), Compiler::AddressType),
         0, frame->trace(0, 0), 0, Compiler::VoidType, 2,
         c->register_(t->arch->thread()), target);
    } break;

    case multianewarray: {
      uint16_t index = codeReadInt16(t, code, ip);
      uint8_t dimensions = codeBody(t, code, ip++);

      object reference = singletonObject
        (t, codePool(t, methodCode(t, context->method)), index - 1);

      PROTECT(t, reference);

      object class_ = resolveClassInPool(t, context->method, index - 1, false);

      object argument;
      Thunk thunk;
      if (LIKELY(class_)) {
        argument = class_;
        thunk = makeMultidimensionalArrayThunk;
      } else {
        argument = makePair(t, context->method, reference);
        thunk = makeMultidimensionalArrayFromReferenceThunk;
      }

      unsigned offset
        = localOffset
        (t, localSize(t, context->method) + c->topOfStack(), context->method)
        + t->arch->frameReturnAddressSize();

      Compiler::Operand* result = c->call
        (c->constant
         (getThunk(t, thunk), Compiler::AddressType),
         0,
         frame->trace(0, 0),
         TargetBytesPerWord,
         Compiler::ObjectType,
         4, c->register_(t->arch->thread()), frame->append(argument),
         c->constant(dimensions, Compiler::IntegerType),
         c->constant(offset, Compiler::IntegerType));

      frame->pop(dimensions);
      frame->pushObject(result);
    } break;

    case new_: {
      uint16_t index = codeReadInt16(t, code, ip);
        
      object reference = singletonObject
        (t, codePool(t, methodCode(t, context->method)), index - 1);

      PROTECT(t, reference);

      object class_ = resolveClassInPool(t, context->method, index - 1, false);

      object argument;
      Thunk thunk;
      if (LIKELY(class_)) {
        argument = class_;
        if (classVmFlags(t, class_) & (WeakReferenceFlag | HasFinalizerFlag)) {
          thunk = makeNewGeneral64Thunk;
        } else {
          thunk = makeNew64Thunk;
        }
      } else {
        argument = makePair(t, context->method, reference);
        thunk = makeNewFromReferenceThunk;
      }

      frame->pushObject
        (c->call
         (c->constant(getThunk(t, thunk), Compiler::AddressType),
          0,
          frame->trace(0, 0),
          TargetBytesPerWord,
          Compiler::ObjectType,
          2, c->register_(t->arch->thread()), frame->append(argument)));
    } break;

    case newarray: {
      uint8_t type = codeBody(t, code, ip++);

      Compiler::Operand* length = frame->popInt();

      frame->pushObject
        (c->call
         (c->constant(getThunk(t, makeBlankArrayThunk), Compiler::AddressType),
          0,
          frame->trace(0, 0),
          TargetBytesPerWord,
          Compiler::ObjectType,
          3, c->register_(t->arch->thread()),
          c->constant(type, Compiler::IntegerType), length));
    } break;

    case nop: break;

    case pop_:
      frame->pop(1);
      break;

    case pop2:
      frame->pop(2);
      break;

    case putfield:
    case putstatic: {
      uint16_t index = codeReadInt16(t, code, ip);
    
      object reference = singletonObject
        (t, codePool(t, methodCode(t, context->method)), index - 1);

      PROTECT(t, reference);

      object field = resolveField(t, context->method, index - 1, false);

      if (LIKELY(field)) {
        int fieldCode = vm::fieldCode(t, field);

        object staticTable = 0;

        if (instruction == putstatic) {
          assert(t, fieldFlags(t, field) & ACC_STATIC);

          if (fieldClass(t, field) != methodClass(t, context->method)
              and classNeedsInit(t, fieldClass(t, field)))
          {
            PROTECT(t, field);

            c->call
              (c->constant
               (getThunk(t, tryInitClassThunk), Compiler::AddressType),
               0,
               frame->trace(0, 0),
               0,
               Compiler::VoidType,
               2, c->register_(t->arch->thread()),
               frame->append(fieldClass(t, field)));
          }

          staticTable = classStaticTable(t, fieldClass(t, field));      
        } else {
          assert(t, (fieldFlags(t, field) & ACC_STATIC) == 0);

          if (inTryBlock(t, code, ip - 3)) {
            c->saveLocals();
            frame->trace(0, 0);
          }
        }

        if (fieldFlags(t, field) & ACC_VOLATILE) {
          if (TargetBytesPerWord == 4
              and (fieldCode == DoubleField or fieldCode == LongField))
          {
            PROTECT(t, field);

            c->call
              (c->constant
               (getThunk(t, acquireMonitorForObjectThunk),
                Compiler::AddressType),
               0, frame->trace(0, 0), 0, Compiler::VoidType, 2,
               c->register_(t->arch->thread()), frame->append(field));
          } else {
            c->storeStoreBarrier();
          }
        }

        Compiler::Operand* value = popField(t, frame, fieldCode);

        Compiler::Operand* table;

        if (instruction == putstatic) {
          PROTECT(t, field);

          table = frame->append(staticTable);
        } else {
          table = frame->popObject();
        }

        switch (fieldCode) {
        case ByteField:
        case BooleanField:
          c->store
            (TargetBytesPerWord, value, 1, c->memory
             (table, Compiler::IntegerType, targetFieldOffset
              (context, field), 0, 1));
          break;

        case CharField:
        case ShortField:
          c->store
            (TargetBytesPerWord, value, 2, c->memory
             (table, Compiler::IntegerType, targetFieldOffset
              (context, field), 0, 1));
          break;
            
        case FloatField:
          c->store
            (TargetBytesPerWord, value, 4, c->memory
             (table, Compiler::FloatType, targetFieldOffset
              (context, field), 0, 1));
          break;

        case IntField:
          c->store
            (TargetBytesPerWord, value, 4, c->memory
             (table, Compiler::IntegerType, targetFieldOffset
              (context, field), 0, 1));
          break;

        case DoubleField:
          c->store
            (8, value, 8, c->memory
             (table, Compiler::FloatType, targetFieldOffset
              (context, field), 0, 1));
          break;

        case LongField:
          c->store
            (8, value, 8, c->memory
             (table, Compiler::IntegerType, targetFieldOffset
              (context, field), 0, 1));
          break;

        case ObjectField:
          if (instruction == putfield) {
            c->call
              (c->constant
               (getThunk(t, setMaybeNullThunk), Compiler::AddressType),
               0,
               frame->trace(0, 0),
               0,
               Compiler::VoidType,
               4, c->register_(t->arch->thread()), table,
               c->constant(targetFieldOffset(context, field),
                           Compiler::IntegerType),
               value);
          } else {
            c->call
              (c->constant(getThunk(t, setThunk), Compiler::AddressType),
               0, 0, 0, Compiler::VoidType,
               4, c->register_(t->arch->thread()), table,
               c->constant(targetFieldOffset(context, field),
                           Compiler::IntegerType),
               value);
          }
          break;

        default: abort(t);
        }

        if (fieldFlags(t, field) & ACC_VOLATILE) {
          if (TargetBytesPerWord == 4
              and (fieldCode == DoubleField or fieldCode == LongField))
          {
            c->call
              (c->constant
               (getThunk(t, releaseMonitorForObjectThunk),
                Compiler::AddressType),
               0, frame->trace(0, 0), 0, Compiler::VoidType, 2,
               c->register_(t->arch->thread()), frame->append(field));
          } else {
            c->storeLoadBarrier();
          }
        }
      } else {
        int fieldCode = vm::fieldCode
          (t, byteArrayBody(t, referenceSpec(t, reference), 0));

        Compiler::Operand* value = popField(t, frame, fieldCode);
        unsigned rSize = resultSize(t, fieldCode);
        Compiler::OperandType rType = operandTypeForFieldCode(t, fieldCode);

        object pair = makePair(t, context->method, reference);

        switch (fieldCode) {
        case ByteField:
        case BooleanField:
        case CharField:
        case ShortField:
        case FloatField:
        case IntField: {
          if (instruction == putstatic) {
            c->call
              (c->constant
               (getThunk(t, setStaticFieldValueFromReferenceThunk),
                Compiler::AddressType),
               0, frame->trace(0, 0), rSize, rType, 3,
               c->register_(t->arch->thread()), frame->append(pair),
               value);
          } else {
            Compiler::Operand* instance = frame->popObject();

            c->call
              (c->constant
               (getThunk(t, setFieldValueFromReferenceThunk),
                Compiler::AddressType),
               0, frame->trace(0, 0), rSize, rType, 4,
               c->register_(t->arch->thread()), frame->append(pair),
               instance, value);
          }
        } break;

        case DoubleField:
        case LongField: {
          if (instruction == putstatic) {
            c->call
              (c->constant
               (getThunk(t, setStaticLongFieldValueFromReferenceThunk),
                Compiler::AddressType),
               0, frame->trace(0, 0), rSize, rType, 4,
               c->register_(t->arch->thread()), frame->append(pair),
               static_cast<Compiler::Operand*>(0), value);
          } else {
            Compiler::Operand* instance = frame->popObject();

            c->call
              (c->constant
               (getThunk(t, setLongFieldValueFromReferenceThunk),
                Compiler::AddressType),
               0, frame->trace(0, 0), rSize, rType, 5,
               c->register_(t->arch->thread()), frame->append(pair),
               instance, static_cast<Compiler::Operand*>(0), value);
          }
        } break;

        case ObjectField: {
          if (instruction == putstatic) {
            c->call
              (c->constant
               (getThunk(t, setStaticObjectFieldValueFromReferenceThunk),
                Compiler::AddressType),
               0, frame->trace(0, 0), rSize, rType, 3,
               c->register_(t->arch->thread()), frame->append(pair),
               value);
          } else {
            Compiler::Operand* instance = frame->popObject();

            c->call
              (c->constant
               (getThunk(t, setObjectFieldValueFromReferenceThunk),
                Compiler::AddressType),
               0, frame->trace(0, 0), rSize, rType, 4,
               c->register_(t->arch->thread()), frame->append(pair),
               instance, value);
          }
        } break;

        default: abort(t);
        }
      }
    } break;

    case ret: {
      unsigned index = codeBody(t, code, ip);
      frame->returnFromSubroutine(index);
    } return;

    case return_:
      if (needsReturnBarrier(t, context->method)) {
        c->storeStoreBarrier();
      }

      handleExit(t, frame);
      c->return_(0, 0);
      return;

    case sipush:
      frame->pushInt
        (c->constant
         (static_cast<int16_t>(codeReadInt16(t, code, ip)),
          Compiler::IntegerType));
      break;

    case swap:
      frame->swap();
      break;

    case tableswitch: {
      int32_t base = ip - 1;

      ip = (ip + 3) & ~3; // pad to four byte boundary

      uint32_t defaultIp = base + codeReadInt32(t, code, ip);
      assert(t, defaultIp < codeLength(t, code));
      
      int32_t bottom = codeReadInt32(t, code, ip);
      int32_t top = codeReadInt32(t, code, ip);
        
      Promise* start = 0;
      THREAD_RUNTIME_ARRAY(t, uint32_t, ipTable, top - bottom + 1);
      for (int32_t i = 0; i < top - bottom + 1; ++i) {
        unsigned index = ip + (i * 4);
        uint32_t newIp = base + codeReadInt32(t, code, index);
        assert(t, newIp < codeLength(t, code));

        RUNTIME_ARRAY_BODY(ipTable)[i] = newIp;

        Promise* p = c->poolAppendPromise
          (frame->addressPromise(c->machineIp(newIp)));
        if (i == 0) {
          start = p;
        }
      }
      assert(t, start);

      Compiler::Operand* key = frame->popInt();
      
      c->jumpIfLess(4, c->constant(bottom, Compiler::IntegerType), key,
                    frame->machineIp(defaultIp));

      c->save(1, key);

      saveStateAndCompile(t, frame, defaultIp);

      c->jumpIfGreater(4, c->constant(top, Compiler::IntegerType), key,
                       frame->machineIp(defaultIp));

      c->save(1, key);

      saveStateAndCompile(t, frame, defaultIp);

      Compiler::Operand* normalizedKey
        = (bottom
           ? c->sub(4, c->constant(bottom, Compiler::IntegerType), key) : key);

      Compiler::Operand* entry = c->memory
        (frame->absoluteAddressOperand(start), Compiler::AddressType, 0,
         normalizedKey, TargetBytesPerWord);

      c->jmp
        (c->load
         (TargetBytesPerWord, TargetBytesPerWord, context->bootContext
          ? c->add
          (TargetBytesPerWord, c->memory
            (c->register_(t->arch->thread()), Compiler::AddressType,
             TargetThreadCodeImage), entry)
          : entry,
          TargetBytesPerWord));

      Compiler::State* state = c->saveState();

      for (int32_t i = 0; i < top - bottom + 1; ++i) {
        compile(t, frame, RUNTIME_ARRAY_BODY(ipTable)[i]);

        c->restoreState(state);
      }

      ip = defaultIp;
    } break;

    case wide: {
      switch (codeBody(t, code, ip++)) {
      case aload: {
        frame->loadObject(codeReadInt16(t, code, ip));
      } break;

      case astore: {
        frame->storeObject(codeReadInt16(t, code, ip));
      } break;

      case iinc: {
        uint16_t index = codeReadInt16(t, code, ip);
        int16_t count = codeReadInt16(t, code, ip);

        storeLocal
          (context, 1,
           c->add
           (4, c->constant(count, Compiler::IntegerType), 
            loadLocal(context, 1, index)),
           index);
      } break;

      case iload: {
        frame->loadInt(codeReadInt16(t, code, ip));
      } break;

      case istore: {
        frame->storeInt(codeReadInt16(t, code, ip));
      } break;

      case lload: {
        frame->loadLong(codeReadInt16(t, code, ip));
      } break;

      case lstore: {
        frame->storeLong(codeReadInt16(t, code, ip));
      } break;

      case ret: {
        unsigned index = codeReadInt16(t, code, ip);
        c->jmp(loadLocal(context, 1, index));
        frame->returnFromSubroutine(index);
      } return;

      default: abort(t);
      }
    } break;

    default: abort(t);
    }
  }
}

FILE* compileLog = 0;

void
logCompile(MyThread* t, const void* code, unsigned size, const char* class_,
           const char* name, const char* spec)
{
  static bool open = false;
  if (not open) {
    open = true;
    const char* path = findProperty(t, "avian.jit.log");
    if (path) {
      compileLog = vm::fopen(path, "wb");
    } else if (DebugCompile) {
      compileLog = stderr;
    }
  }

  if (compileLog) {
    fprintf(compileLog, "%p %p %s.%s%s\n",
            code, static_cast<const uint8_t*>(code) + size,
            class_, name, spec);
  }
}

int
resolveIpForwards(Context* context, int start, int end)
{
  while (start < end and context->visitTable[start] == 0) {
    ++ start;
  }
  
  if (start >= end) {
    return -1;
  } else {
    return start;
  }
}

int
resolveIpBackwards(Context* context, int start, int end)
{
  while (start >= end and context->visitTable[start] == 0) {
    -- start;
  }
  
  if (start < end) {
    return -1;
  } else {
    return start;
  }
}

object
truncateIntArray(Thread* t, object array, unsigned length)
{
  expect(t, intArrayLength(t, array) > length);

  PROTECT(t, array);

  object newArray = makeIntArray(t, length);
  memcpy(&intArrayBody(t, newArray, 0), &intArrayBody(t, array, 0),
         length * 4);

  return newArray;
}

object
truncateArray(Thread* t, object array, unsigned length)
{
  expect(t, arrayLength(t, array) > length);

  PROTECT(t, array);

  object newArray = makeArray(t, length);
  memcpy(&arrayBody(t, newArray, 0), &arrayBody(t, array, 0),
         length * BytesPerWord);

  return newArray;
}

object
truncateLineNumberTable(Thread* t, object table, unsigned length)
{
  expect(t, lineNumberTableLength(t, table) > length);

  PROTECT(t, table);

  object newTable = makeLineNumberTable(t, length);
  memcpy(&lineNumberTableBody(t, newTable, 0),
         &lineNumberTableBody(t, table, 0),
         length * sizeof(uint64_t));

  return newTable;
}

object
translateExceptionHandlerTable(MyThread* t, Context* context, intptr_t start)
{
  Compiler* c = context->compiler;

  object oldTable = codeExceptionHandlerTable
    (t, methodCode(t, context->method));

  if (oldTable) {
    PROTECT(t, oldTable);

    unsigned length = exceptionHandlerTableLength(t, oldTable);

    object newIndex = makeIntArray(t, length * 3);
    PROTECT(t, newIndex);

    object newTable = makeArray(t, length + 1);
    PROTECT(t, newTable);

    unsigned ni = 0;
    for (unsigned oi = 0; oi < length; ++ oi) {
      uint64_t oldHandler = exceptionHandlerTableBody
        (t, oldTable, oi);

      int handlerStart = resolveIpForwards
        (context, exceptionHandlerStart(oldHandler),
         exceptionHandlerEnd(oldHandler));

      if (LIKELY(handlerStart >= 0)) {
        int handlerEnd = resolveIpBackwards
          (context, exceptionHandlerEnd(oldHandler),
           exceptionHandlerStart(oldHandler));

        assert(t, handlerEnd >= 0);

        intArrayBody(t, newIndex, ni * 3)
          = c->machineIp(handlerStart)->value() - start;

        intArrayBody(t, newIndex, (ni * 3) + 1)
          = c->machineIp(handlerEnd)->value() - start;

        intArrayBody(t, newIndex, (ni * 3) + 2)
          = c->machineIp(exceptionHandlerIp(oldHandler))->value() - start;

        object type;
        if (exceptionHandlerCatchType(oldHandler)) {
          type = resolveClassInPool
            (t, context->method, exceptionHandlerCatchType(oldHandler) - 1);
        } else {
          type = 0;
        }

        set(t, newTable, ArrayBody + ((ni + 1) * BytesPerWord), type);

        ++ ni;
      }
    }

    if (UNLIKELY(ni < length)) {
      newIndex = truncateIntArray(t, newIndex, ni * 3);
      newTable = truncateArray(t, newTable, ni + 1);
    }

    set(t, newTable, ArrayBody, newIndex);

    return newTable;
  } else {
    return 0;
  }
}

object
translateLineNumberTable(MyThread* t, Context* context, intptr_t start)
{
  object oldTable = codeLineNumberTable(t, methodCode(t, context->method));
  if (oldTable) {
    PROTECT(t, oldTable);

    unsigned length = lineNumberTableLength(t, oldTable);
    object newTable = makeLineNumberTable(t, length);
    unsigned ni = 0;
    for (unsigned oi = 0; oi < length; ++oi) {
      uint64_t oldLine = lineNumberTableBody(t, oldTable, oi);

      int ip = resolveIpForwards
        (context, lineNumberIp(oldLine), oi + 1 < length
         ? lineNumberIp(lineNumberTableBody(t, oldTable, oi + 1)) - 1
         : lineNumberIp(oldLine) + 1);

      if (LIKELY(ip >= 0)) {
        lineNumberTableBody(t, newTable, ni++) = lineNumber
          (context->compiler->machineIp(ip)->value() - start,
           lineNumberLine(oldLine));
      }
    }

    if (UNLIKELY(ni < length)) {
      newTable = truncateLineNumberTable(t, newTable, ni);      
    }

    return newTable;
  } else {
    return 0;
  }
}

void
printSet(uintptr_t m, unsigned limit)
{
  if (limit) {
    for (unsigned i = 0; i < 16; ++i) {
      if ((m >> i) & 1) {
        fprintf(stderr, "1");
      } else {
        fprintf(stderr, "_");
      }
    }
  }
}

void
calculateTryCatchRoots(Context* context, SubroutinePath* subroutinePath,
                       uintptr_t* roots, unsigned mapSize, unsigned start,
                       unsigned end)
{
  memset(roots, 0xFF, mapSize * BytesPerWord);

  if (DebugFrameMaps) {
    fprintf(stderr, "calculate try/catch roots from %d to %d", start, end);
    if (subroutinePath) {
      fprintf(stderr, " ");
      print(subroutinePath);
    }
    fprintf(stderr, "\n");
  }

  for (TraceElement* te = context->traceLog; te; te = te->next) {
    if (te->ip >= start and te->ip < end) {
      uintptr_t* traceRoots = 0;
      if (subroutinePath == 0) {
        traceRoots = te->map;
        te->watch = true;
      } else {
        for (SubroutineTrace* t = te->subroutineTrace; t; t = t->next) {
          if (t->path == subroutinePath) {
            traceRoots = t->map;
            t->watch = true;
            break;
          }
        }        
      }

      if (traceRoots) {
        if (DebugFrameMaps) {
          fprintf(stderr, "   use roots at ip %3d: ", te->ip);
          printSet(*traceRoots, mapSize);
          fprintf(stderr, "\n");
        }

        for (unsigned wi = 0; wi < mapSize; ++wi) {
          roots[wi] &= traceRoots[wi];
        }
      } else {
        if (DebugFrameMaps) {
          fprintf(stderr, "  skip roots at ip %3d\n", te->ip);
        }
      }
    }
  }

  if (DebugFrameMaps) {
    fprintf(stderr, "result roots          : ");
    printSet(*roots, mapSize);
    fprintf(stderr, "\n");
  }
}

unsigned
calculateFrameMaps(MyThread* t, Context* context, uintptr_t* originalRoots,
                   unsigned eventIndex, SubroutinePath* subroutinePath = 0,
                   uintptr_t* resultRoots = 0)
{
  // for each instruction with more than one predecessor, and for each
  // stack position, determine if there exists a path to that
  // instruction such that there is not an object pointer left at that
  // stack position (i.e. it is uninitialized or contains primitive
  // data).

  unsigned mapSize = frameMapSizeInWords(t, context->method);

  THREAD_RUNTIME_ARRAY(t, uintptr_t, roots, mapSize);
  if (originalRoots) {
    memcpy(RUNTIME_ARRAY_BODY(roots), originalRoots, mapSize * BytesPerWord);
  } else {
    memset(RUNTIME_ARRAY_BODY(roots), 0, mapSize * BytesPerWord);
  }

  int32_t ip = -1;

  // invariant: for each stack position, roots contains a zero at that
  // position if there exists some path to the current instruction
  // such that there is definitely not an object pointer at that
  // position.  Otherwise, roots contains a one at that position,
  // meaning either all known paths result in an object pointer at
  // that position, or the contents of that position are as yet
  // unknown.

  unsigned length = context->eventLog.length();
  while (eventIndex < length) {
    Event e = static_cast<Event>(context->eventLog.get(eventIndex++));
    switch (e) {
    case PushContextEvent: {
      eventIndex = calculateFrameMaps
        (t, context, RUNTIME_ARRAY_BODY(roots), eventIndex, subroutinePath,
         resultRoots);
    } break;

    case PopContextEvent:
      goto exit;

    case IpEvent: {
      ip = context->eventLog.get2(eventIndex);
      eventIndex += 2;

      if (DebugFrameMaps) {
        fprintf(stderr, "       roots at ip %3d: ", ip);
        printSet(*RUNTIME_ARRAY_BODY(roots), mapSize);
        fprintf(stderr, "\n");
      }

      uintptr_t* tableRoots
        = (subroutinePath ? subroutinePath->rootTable : context->rootTable)
        + (ip * mapSize);

      if (context->visitTable[ip] > 1) {
        for (unsigned wi = 0; wi < mapSize; ++wi) {
          uintptr_t newRoots = tableRoots[wi] & RUNTIME_ARRAY_BODY(roots)[wi];

          if ((eventIndex == length
               or context->eventLog.get(eventIndex) == PopContextEvent)
              and newRoots != tableRoots[wi])
          {
            if (DebugFrameMaps) {
              fprintf(stderr, "dirty roots!\n");
            }

            context->dirtyRoots = true;
          }

          tableRoots[wi] = newRoots;
          RUNTIME_ARRAY_BODY(roots)[wi] &= tableRoots[wi];
        }

        if (DebugFrameMaps) {
          fprintf(stderr, " table roots at ip %3d: ", ip);
          printSet(*tableRoots, mapSize);
          fprintf(stderr, "\n");
        }
      } else {
        memcpy(tableRoots, RUNTIME_ARRAY_BODY(roots), mapSize * BytesPerWord);
      }
    } break;

    case MarkEvent: {
      unsigned i = context->eventLog.get2(eventIndex);
      eventIndex += 2;

      markBit(RUNTIME_ARRAY_BODY(roots), i);
    } break;

    case ClearEvent: {
      unsigned i = context->eventLog.get2(eventIndex);
      eventIndex += 2;

      clearBit(RUNTIME_ARRAY_BODY(roots), i);
    } break;

    case PushExceptionHandlerEvent: {
      unsigned start = context->eventLog.get2(eventIndex);
      eventIndex += 2;
      unsigned end = context->eventLog.get2(eventIndex);
      eventIndex += 2;

      if (context->subroutineTable and context->subroutineTable[start]) {
        Subroutine* s = context->subroutineTable[start];
        unsigned originalEventIndex = eventIndex;

        for (SubroutineCall* c = s->calls; c; c = c->next) {
          for (SubroutinePath* p = c->paths; p; p = p->listNext) {
            calculateTryCatchRoots
              (context, p, RUNTIME_ARRAY_BODY(roots), mapSize, start, end);

            eventIndex = calculateFrameMaps
              (t, context, RUNTIME_ARRAY_BODY(roots), originalEventIndex, p);
          }
        }
      } else {
        calculateTryCatchRoots
          (context, 0, RUNTIME_ARRAY_BODY(roots), mapSize, start, end);

        eventIndex = calculateFrameMaps
          (t, context, RUNTIME_ARRAY_BODY(roots), eventIndex, 0);
      }
    } break;

    case TraceEvent: {
      TraceElement* te; context->eventLog.get(eventIndex, &te, BytesPerWord);
      if (DebugFrameMaps) {
        fprintf(stderr, " trace roots at ip %3d: ", ip);
        printSet(*RUNTIME_ARRAY_BODY(roots), mapSize);
        if (subroutinePath) {
          fprintf(stderr, " ");
          print(subroutinePath);
        }
        fprintf(stderr, "\n");
      }
        
      uintptr_t* map;
      bool watch;
      if (subroutinePath == 0) {
        map = te->map;
        watch = te->watch;
      } else {
        SubroutineTrace* trace = 0;
        for (SubroutineTrace* t = te->subroutineTrace; t; t = t->next) {
          if (t->path == subroutinePath) {
            trace = t;
            break;
          }
        }

        if (trace == 0) {
          te->subroutineTrace = trace = new
            (context->zone.allocate
             (sizeof(SubroutineTrace) + (mapSize * BytesPerWord)))
            SubroutineTrace(subroutinePath, te->subroutineTrace, mapSize);

          ++ te->subroutineTraceCount;
        }

        map = trace->map;
        watch = trace->watch;
      }

      for (unsigned wi = 0; wi < mapSize; ++wi) {
        uintptr_t v = RUNTIME_ARRAY_BODY(roots)[wi];

        if (watch and map[wi] != v) {
          if (DebugFrameMaps) {
            fprintf(stderr, "dirty roots due to trace watch!\n");
          }

          context->dirtyRoots = true;
        }

        map[wi] = v;
      }

      eventIndex += BytesPerWord;
    } break;

    case PushSubroutineEvent: {
      SubroutineCall* call;
      context->eventLog.get(eventIndex, &call, BytesPerWord);
      eventIndex += BytesPerWord;

      unsigned nextIndex = context->eventLog.get2(eventIndex);

      eventIndex = nextIndex;

      SubroutinePath* path = 0;
      for (SubroutinePath* p = call->paths; p; p = p->listNext) {
        if (p->stackNext == subroutinePath) {
          path = p;
          break;
        }
      }

      if (path == 0) {
        path = new (context->zone.allocate(sizeof(SubroutinePath)))
          SubroutinePath(call, subroutinePath,
                         makeRootTable(t, &(context->zone), context->method));
      }

      THREAD_RUNTIME_ARRAY(t, uintptr_t, subroutineRoots, mapSize);

      calculateFrameMaps
        (t, context, RUNTIME_ARRAY_BODY(roots), call->subroutine->logIndex,
         path, RUNTIME_ARRAY_BODY(subroutineRoots));

      for (unsigned wi = 0; wi < mapSize; ++wi) {
        RUNTIME_ARRAY_BODY(roots)[wi]
          &= RUNTIME_ARRAY_BODY(subroutineRoots)[wi];
      }      
    } break;

    case PopSubroutineEvent:
      eventIndex = static_cast<unsigned>(-1);
      goto exit;

    default: abort(t);
    }
  }

 exit:
  if (resultRoots and ip != -1) {
    if (DebugFrameMaps) {
      fprintf(stderr, "result roots at ip %3d: ", ip);
      printSet(*RUNTIME_ARRAY_BODY(roots), mapSize);
      if (subroutinePath) {
        fprintf(stderr, " ");
        print(subroutinePath);
      }
      fprintf(stderr, "\n");
    }

    memcpy(resultRoots, RUNTIME_ARRAY_BODY(roots), mapSize * BytesPerWord);
  }

  return eventIndex;
}

int
compareTraceElementPointers(const void* va, const void* vb)
{
  TraceElement* a = *static_cast<TraceElement* const*>(va);
  TraceElement* b = *static_cast<TraceElement* const*>(vb);
  if (a->address->value() > b->address->value()) {
    return 1;
  } else if (a->address->value() < b->address->value()) {
    return -1;
  } else {
    return 0;
  }
}

unsigned
simpleFrameMapTableSize(MyThread* t, object method, object map)
{
  int size = frameMapSizeInBits(t, method);
  return ceiling(intArrayLength(t, map) * size, 32 + size);
}

uint8_t*
finish(MyThread* t, FixedAllocator* allocator, Assembler* a, const char* name,
       unsigned length)
{
  uint8_t* start = static_cast<uint8_t*>
    (allocator->allocate(length, TargetBytesPerWord));

  a->setDestination(start);
  a->write();

  logCompile(t, start, length, 0, name, 0);

  return start;
}

void
setBit(int32_t* dst, unsigned index)
{
  dst[index / 32] |= static_cast<int32_t>(1) << (index % 32);
}

void
clearBit(int32_t* dst, unsigned index)
{
  dst[index / 32] &= ~(static_cast<int32_t>(1) << (index % 32));
}

void
copyFrameMap(int32_t* dst, uintptr_t* src, unsigned mapSizeInBits,
             unsigned offset, TraceElement* p,
             SubroutinePath* subroutinePath)
{
  if (DebugFrameMaps) {
    fprintf(stderr, "  orig roots at ip %3d: ", p->ip);
    printSet(src[0], ceiling(mapSizeInBits, BitsPerWord));
    print(subroutinePath);
    fprintf(stderr, "\n");

    fprintf(stderr, " final roots at ip %3d: ", p->ip);
  }

  for (unsigned j = 0; j < p->argumentIndex; ++j) {
    if (getBit(src, j)) {
      if (DebugFrameMaps) {
        fprintf(stderr, "1");
      }
      setBit(dst, offset + j);
    } else {
      if (DebugFrameMaps) {
        fprintf(stderr, "_");
      }
      clearBit(dst, offset + j);
    }
  }

  if (DebugFrameMaps) {
    print(subroutinePath);
    fprintf(stderr, "\n");
  }
}

class FrameMapTableHeader {
 public:
  FrameMapTableHeader(unsigned indexCount):
    indexCount(indexCount)
  { }

  unsigned indexCount;
};

class FrameMapTableIndexElement {
 public:
  FrameMapTableIndexElement(int offset, unsigned base, unsigned path):
    offset(offset),
    base(base),
    path(path)
  { }

  int offset;
  unsigned base;
  unsigned path;
};

class FrameMapTablePath {
 public:
  FrameMapTablePath(unsigned stackIndex, unsigned elementCount, unsigned next):
    stackIndex(stackIndex),
    elementCount(elementCount),
    next(next)
  { }

  unsigned stackIndex;
  unsigned elementCount;
  unsigned next;
  int32_t elements[0];
};

int
compareInt32s(const void* va, const void* vb)
{
  return *static_cast<int32_t const*>(va) - *static_cast<int32_t const*>(vb);
}

int
compare(SubroutinePath* a, SubroutinePath* b)
{
  if (a->stackNext) {
    int d = compare(a->stackNext, b->stackNext);
    if (d) return d;
  }
  int64_t av = a->call->returnAddress->value();
  int64_t bv = b->call->returnAddress->value();
  if (av > bv) {
    return 1;
  } else if (av < bv) {
    return -1;
  } else {
    return 0;
  }
}

int
compareSubroutineTracePointers(const void* va, const void* vb)
{
  return compare((*static_cast<SubroutineTrace* const*>(va))->path,
                 (*static_cast<SubroutineTrace* const*>(vb))->path);
}

object
makeGeneralFrameMapTable(MyThread* t, Context* context, uint8_t* start,
                         TraceElement** elements, unsigned elementCount,
                         unsigned pathFootprint, unsigned mapCount)
{
  unsigned mapSize = frameMapSizeInBits(t, context->method);
  unsigned indexOffset = sizeof(FrameMapTableHeader);
  unsigned mapsOffset = indexOffset
    + (elementCount * sizeof(FrameMapTableIndexElement));
  unsigned pathsOffset = mapsOffset + (ceiling(mapCount * mapSize, 32) * 4);

  object table = makeByteArray(t, pathsOffset + pathFootprint);
  
  int8_t* body = &byteArrayBody(t, table, 0);
  new (body) FrameMapTableHeader(elementCount);
 
  unsigned nextTableIndex = pathsOffset;
  unsigned nextMapIndex = 0;
  for (unsigned i = 0; i < elementCount; ++i) {
    TraceElement* p = elements[i];
    unsigned mapBase = nextMapIndex;

    unsigned pathIndex;
    if (p->subroutineTrace) {
      FrameMapTablePath* previous = 0;
      Subroutine* subroutine = p->subroutineTrace->path->call->subroutine;
      for (Subroutine* s = subroutine; s; s = s->stackNext) {
        if (s->tableIndex == 0) {
          unsigned pathObjectSize = sizeof(FrameMapTablePath)
            + (sizeof(int32_t) * s->callCount);

          assert(t, nextTableIndex + pathObjectSize
                 <= byteArrayLength(t, table));

          s->tableIndex = nextTableIndex;
          
          nextTableIndex += pathObjectSize;

          FrameMapTablePath* current = new (body + s->tableIndex)
            FrameMapTablePath
            (s->stackIndex, s->callCount,
             s->stackNext ? s->stackNext->tableIndex : 0);

          unsigned i = 0;
          for (SubroutineCall* c = subroutine->calls; c; c = c->next) {
            assert(t, i < s->callCount);

            current->elements[i++]
              = static_cast<intptr_t>(c->returnAddress->value())
              - reinterpret_cast<intptr_t>(start);
          }
          assert(t, i == s->callCount);

          qsort(current->elements, s->callCount, sizeof(int32_t),
                compareInt32s);

          if (previous) {
            previous->next = s->tableIndex;
          }

          previous = current;
        } else {
          break;
        }
      }

      pathIndex = subroutine->tableIndex;

      THREAD_RUNTIME_ARRAY
        (t, SubroutineTrace*, traces, p->subroutineTraceCount);

      unsigned i = 0;
      for (SubroutineTrace* trace = p->subroutineTrace;
           trace; trace = trace->next)
      {
        assert(t, i < p->subroutineTraceCount);
        RUNTIME_ARRAY_BODY(traces)[i++] = trace;
      }
      assert(t, i == p->subroutineTraceCount);

      qsort(RUNTIME_ARRAY_BODY(traces), p->subroutineTraceCount,
            sizeof(SubroutineTrace*), compareSubroutineTracePointers);

      for (unsigned i = 0; i < p->subroutineTraceCount; ++i) {
        assert(t, mapsOffset + ceiling(nextMapIndex + mapSize, 32) * 4
               <= pathsOffset);

        copyFrameMap(reinterpret_cast<int32_t*>(body + mapsOffset),
                     RUNTIME_ARRAY_BODY(traces)[i]->map, mapSize,
                     nextMapIndex, p, RUNTIME_ARRAY_BODY(traces)[i]->path);

        nextMapIndex += mapSize;
      }
    } else {
      pathIndex = 0;

      assert(t, mapsOffset + ceiling(nextMapIndex + mapSize, 32) * 4
             <= pathsOffset);

      copyFrameMap(reinterpret_cast<int32_t*>(body + mapsOffset), p->map,
                   mapSize, nextMapIndex, p, 0);
      
      nextMapIndex += mapSize;
    }

    unsigned elementIndex = indexOffset
      + (i * sizeof(FrameMapTableIndexElement));

    assert(t, elementIndex + sizeof(FrameMapTableIndexElement) <= mapsOffset);

    new (body + elementIndex) FrameMapTableIndexElement
      (static_cast<intptr_t>(p->address->value())
       - reinterpret_cast<intptr_t>(start), mapBase, pathIndex);
  }

  assert(t, nextMapIndex == mapCount * mapSize);

  return table;
}

object
makeSimpleFrameMapTable(MyThread* t, Context* context, uint8_t* start, 
                        TraceElement** elements, unsigned elementCount)
{
  unsigned mapSize = frameMapSizeInBits(t, context->method);
  object table = makeIntArray
    (t, elementCount + ceiling(elementCount * mapSize, 32));

  assert(t, intArrayLength(t, table) == elementCount
         + simpleFrameMapTableSize(t, context->method, table));

  for (unsigned i = 0; i < elementCount; ++i) {
    TraceElement* p = elements[i];

    intArrayBody(t, table, i) = static_cast<intptr_t>(p->address->value())
      - reinterpret_cast<intptr_t>(start);

    assert(t, elementCount + ceiling((i + 1) * mapSize, 32)
           <= intArrayLength(t, table));

    if (mapSize) {
      copyFrameMap(&intArrayBody(t, table, elementCount), p->map,
                   mapSize, i * mapSize, p, 0);
    }
  }

  return table;
}

void
finish(MyThread* t, FixedAllocator* allocator, Context* context)
{
  Compiler* c = context->compiler;

  if (false) {
    logCompile
      (t, 0, 0,
       reinterpret_cast<const char*>
       (&byteArrayBody(t, className(t, methodClass(t, context->method)), 0)),
       reinterpret_cast<const char*>
       (&byteArrayBody(t, methodName(t, context->method), 0)),
       reinterpret_cast<const char*>
       (&byteArrayBody(t, methodSpec(t, context->method), 0)));
  }

  // for debugging:
  if (false and
      ::strcmp
      (reinterpret_cast<const char*>
       (&byteArrayBody(t, className(t, methodClass(t, context->method)), 0)),
       "java/lang/System") == 0 and
      ::strcmp
      (reinterpret_cast<const char*>
       (&byteArrayBody(t, methodName(t, context->method), 0)),
       "<clinit>") == 0)
  {
    trap();
  }

  // todo: this is a CPU-intensive operation, so consider doing it
  // earlier before we've acquired the global class lock to improve
  // parallelism (the downside being that it may end up being a waste
  // of cycles if another thread compiles the same method in parallel,
  // which might be mitigated by fine-grained, per-method locking):
  c->compile(context->leaf ? 0 : stackOverflowThunk(t),
             TargetThreadStackLimit);

  // we must acquire the class lock here at the latest
 
  unsigned codeSize = c->resolve
    (allocator->base + allocator->offset + TargetBytesPerWord);

  unsigned total = pad(codeSize, TargetBytesPerWord)
    + pad(c->poolSize(), TargetBytesPerWord) + TargetBytesPerWord;

  target_uintptr_t* code = static_cast<target_uintptr_t*>
    (allocator->allocate(total, TargetBytesPerWord));
  code[0] = codeSize;
  uint8_t* start = reinterpret_cast<uint8_t*>(code + 1);

  context->executableAllocator = allocator;
  context->executableStart = code;
  context->executableSize = total;

  if (context->objectPool) {
    object pool = allocate3
      (t, allocator, Machine::ImmortalAllocation,
       FixedSizeOfArray + ((context->objectPoolCount + 1) * BytesPerWord),
       true);

    initArray(t, pool, context->objectPoolCount + 1);
    mark(t, pool, 0);

    set(t, pool, ArrayBody, root(t, ObjectPools));
    setRoot(t, ObjectPools, pool);

    unsigned i = 1;
    for (PoolElement* p = context->objectPool; p; p = p->next) {
      unsigned offset = ArrayBody + ((i++) * BytesPerWord);

      p->address = reinterpret_cast<uintptr_t>(pool) + offset;

      set(t, pool, offset, p->target);
    }
  }

  c->write();

  BootContext* bc = context->bootContext;
  if (bc) {
    for (DelayedPromise* p = bc->addresses;
         p != bc->addressSentinal;
         p = p->next)
    {
      p->basis = new (bc->zone->allocate(sizeof(ResolvedPromise)))
        ResolvedPromise(p->basis->value());
    }
  }

  { object newExceptionHandlerTable = translateExceptionHandlerTable
      (t, context, reinterpret_cast<intptr_t>(start));

    PROTECT(t, newExceptionHandlerTable);

    object newLineNumberTable = translateLineNumberTable
      (t, context, reinterpret_cast<intptr_t>(start));

    object code = methodCode(t, context->method);

    code = makeCode
      (t, 0, newExceptionHandlerTable, newLineNumberTable,
       reinterpret_cast<uintptr_t>(start), codeMaxStack(t, code),
       codeMaxLocals(t, code), 0);

    set(t, context->method, MethodCode, code);
  }

  if (context->traceLogCount) {
    THREAD_RUNTIME_ARRAY(t, TraceElement*, elements, context->traceLogCount);
    unsigned index = 0;
    unsigned pathFootprint = 0;
    unsigned mapCount = 0;
    for (TraceElement* p = context->traceLog; p; p = p->next) {
      assert(t, index < context->traceLogCount);

      if (p->address) {
        SubroutineTrace* trace = p->subroutineTrace;
        unsigned myMapCount = 1;
        if (trace) {
          for (Subroutine* s = trace->path->call->subroutine;
               s; s = s->stackNext)
          {
            unsigned callCount = s->callCount;
            myMapCount *= callCount;
            if (not s->visited) {
              s->visited = true;
              pathFootprint += sizeof(FrameMapTablePath)
                + (sizeof(int32_t) * callCount);
            }
          }
        }
      
        mapCount += myMapCount;

        RUNTIME_ARRAY_BODY(elements)[index++] = p;

        if (p->target) {
          insertCallNode
            (t, makeCallNode
             (t, p->address->value(), p->target, p->flags, 0));
        }
      }
    }

    qsort(RUNTIME_ARRAY_BODY(elements), index,
          sizeof(TraceElement*), compareTraceElementPointers);

    object map;
    if (pathFootprint) {
      map = makeGeneralFrameMapTable
        (t, context, start, RUNTIME_ARRAY_BODY(elements), index, pathFootprint,
         mapCount);
    } else {
      map = makeSimpleFrameMapTable
        (t, context, start, RUNTIME_ARRAY_BODY(elements), index);
    }

    set(t, methodCode(t, context->method), CodePool, map);
  }

  logCompile
    (t, start, codeSize,
     reinterpret_cast<const char*>
     (&byteArrayBody(t, className(t, methodClass(t, context->method)), 0)),
     reinterpret_cast<const char*>
     (&byteArrayBody(t, methodName(t, context->method), 0)),
     reinterpret_cast<const char*>
     (&byteArrayBody(t, methodSpec(t, context->method), 0)));

  // for debugging:
  if (false and
      ::strcmp
      (reinterpret_cast<const char*>
       (&byteArrayBody(t, className(t, methodClass(t, context->method)), 0)),
       "java/lang/System") == 0 and
      ::strcmp
      (reinterpret_cast<const char*>
       (&byteArrayBody(t, methodName(t, context->method), 0)),
       "<clinit>") == 0)
  {
    trap();
  }

  syncInstructionCache(start, codeSize);
}

void
compile(MyThread* t, Context* context)
{
  Compiler* c = context->compiler;

//   fprintf(stderr, "compiling %s.%s%s\n",
//           &byteArrayBody(t, className(t, methodClass(t, context->method)), 0),
//           &byteArrayBody(t, methodName(t, context->method), 0),
//           &byteArrayBody(t, methodSpec(t, context->method), 0));

  unsigned footprint = methodParameterFootprint(t, context->method);
  unsigned locals = localSize(t, context->method);
  c->init(codeLength(t, methodCode(t, context->method)), footprint, locals,
          alignedFrameSize(t, context->method));

  THREAD_RUNTIME_ARRAY(t, uint8_t, stackMap,
                codeMaxStack(t, methodCode(t, context->method)));
  Frame frame(context, RUNTIME_ARRAY_BODY(stackMap));

  unsigned index = methodParameterFootprint(t, context->method);
  if ((methodFlags(t, context->method) & ACC_STATIC) == 0) {
    frame.set(--index, Frame::Object);
    c->initLocal(1, index, Compiler::ObjectType);
  }

  for (MethodSpecIterator it
         (t, reinterpret_cast<const char*>
          (&byteArrayBody(t, methodSpec(t, context->method), 0)));
       it.hasNext();)
  {
    switch (*it.next()) {
    case 'L':
    case '[':
      frame.set(--index, Frame::Object);
      c->initLocal(1, index, Compiler::ObjectType);
      break;
      
    case 'J':
      frame.set(--index, Frame::Long);
      frame.set(--index, Frame::Long);
      c->initLocal(2, index, Compiler::IntegerType);
      break;

    case 'D':
      frame.set(--index, Frame::Long);
      frame.set(--index, Frame::Long);
      c->initLocal(2, index, Compiler::FloatType);
      break;
      
    case 'F':
      frame.set(--index, Frame::Integer);
      c->initLocal(1, index, Compiler::FloatType);
      break;
      
    default:
      frame.set(--index, Frame::Integer);
      c->initLocal(1, index, Compiler::IntegerType);
      break;
    }
  }

  handleEntrance(t, &frame);

  Compiler::State* state = c->saveState();

  compile(t, &frame, 0);

  context->dirtyRoots = false;
  unsigned eventIndex = calculateFrameMaps(t, context, 0, 0);

  object eht = codeExceptionHandlerTable(t, methodCode(t, context->method));
  if (eht) {
    PROTECT(t, eht);

    unsigned visitCount = exceptionHandlerTableLength(t, eht);

    THREAD_RUNTIME_ARRAY(t, bool, visited, visitCount);
    memset(RUNTIME_ARRAY_BODY(visited), 0, visitCount * sizeof(bool));

    bool progress = true;
    while (progress) {
      progress = false;

      for (unsigned i = 0; i < exceptionHandlerTableLength(t, eht); ++i) {
        uint64_t eh = exceptionHandlerTableBody(t, eht, i);
        int start = resolveIpForwards
          (context, exceptionHandlerStart(eh), exceptionHandlerEnd(eh));

        if ((not RUNTIME_ARRAY_BODY(visited)[i])
            and start >= 0
            and context->visitTable[start])
        {
          RUNTIME_ARRAY_BODY(visited)[i] = true;
          progress = true;

          c->restoreState(state);

          THREAD_RUNTIME_ARRAY
            (t, uint8_t, stackMap,
             codeMaxStack(t, methodCode(t, context->method)));
          Frame frame2(&frame, RUNTIME_ARRAY_BODY(stackMap));

          unsigned end = exceptionHandlerEnd(eh);
          if (exceptionHandlerIp(eh) >= static_cast<unsigned>(start)
              and exceptionHandlerIp(eh) < end)
          {
            end = exceptionHandlerIp(eh);
          }

          context->eventLog.append(PushExceptionHandlerEvent);
          context->eventLog.append2(start);
          context->eventLog.append2(end);

          for (unsigned i = 1;
               i < codeMaxStack(t, methodCode(t, context->method));
               ++i)
          {
            frame2.set(localSize(t, context->method) + i, Frame::Integer);
          }

          compile(t, &frame2, exceptionHandlerIp(eh), start);

          context->eventLog.append(PopContextEvent);

          eventIndex = calculateFrameMaps(t, context, 0, eventIndex);
        }
      }
    }
  }

  while (context->dirtyRoots) {
    context->dirtyRoots = false;
    calculateFrameMaps(t, context, 0, 0);
  }
}

void
updateCall(MyThread* t, UnaryOperation op, void* returnAddress, void* target)
{
  t->arch->updateCall(op, returnAddress, target);
}

void*
compileMethod2(MyThread* t, void* ip);

uint64_t
compileMethod(MyThread* t)
{
  void* ip;
  if (t->tailAddress) {
    ip = t->tailAddress;
    t->tailAddress = 0;
  } else {
    ip = getIp(t);
  }

  return reinterpret_cast<uintptr_t>(compileMethod2(t, ip));
}

void*
compileVirtualMethod2(MyThread* t, object class_, unsigned index)
{
  // If class_ has BootstrapFlag set, that means its vtable is not yet
  // available.  However, we must set t->trace->targetMethod to an
  // appropriate method to ensure we can accurately scan the stack for
  // GC roots.  We find such a method by looking for a superclass with
  // a vtable and using it instead:

  object c = class_;
  while (classVmFlags(t, c) & BootstrapFlag) {
    c = classSuper(t, c);
  }
  t->trace->targetMethod = arrayBody(t, classVirtualTable(t, c), index);

  THREAD_RESOURCE0(t, static_cast<MyThread*>(t)->trace->targetMethod = 0;);

  PROTECT(t, class_);

  object target = resolveTarget(t, class_, index);
  PROTECT(t, target);

  compile(t, codeAllocator(t), 0, target);

  void* address = reinterpret_cast<void*>(methodAddress(t, target));
  if (methodFlags(t, target) & ACC_NATIVE) {
    t->trace->nativeMethod = target;
  } else {
    classVtable(t, class_, methodOffset(t, target)) = address;
  }
  return address;
}

uint64_t
compileVirtualMethod(MyThread* t)
{
  object class_ = objectClass(t, static_cast<object>(t->virtualCallTarget));
  t->virtualCallTarget = 0;

  unsigned index = t->virtualCallIndex;
  t->virtualCallIndex = 0;

  return reinterpret_cast<uintptr_t>(compileVirtualMethod2(t, class_, index));
}

uint64_t
invokeNativeFast(MyThread* t, object method, void* function)
{
  FastNativeFunction f; memcpy(&f, &function, sizeof(void*));
  return f(t, method,
           static_cast<uintptr_t*>(t->stack)
           + t->arch->frameFooterSize()
           + t->arch->frameReturnAddressSize());
}

uint64_t
invokeNativeSlow(MyThread* t, object method, void* function)
{
  PROTECT(t, method);

  unsigned footprint = methodParameterFootprint(t, method) + 1;
  if (methodFlags(t, method) & ACC_STATIC) {
    ++ footprint;
  }
  unsigned count = methodParameterCount(t, method) + 2;

  THREAD_RUNTIME_ARRAY(t, uintptr_t, args, footprint);
  unsigned argOffset = 0;
  THREAD_RUNTIME_ARRAY(t, uint8_t, types, count);
  unsigned typeOffset = 0;

  RUNTIME_ARRAY_BODY(args)[argOffset++] = reinterpret_cast<uintptr_t>(t);
  RUNTIME_ARRAY_BODY(types)[typeOffset++] = POINTER_TYPE;

  uintptr_t* sp = static_cast<uintptr_t*>(t->stack)
    + t->arch->frameFooterSize()
    + t->arch->frameReturnAddressSize();

  object jclass = 0;
  PROTECT(t, jclass);

  if (methodFlags(t, method) & ACC_STATIC) {
    jclass = getJClass(t, methodClass(t, method));
    RUNTIME_ARRAY_BODY(args)[argOffset++]
      = reinterpret_cast<uintptr_t>(&jclass);
  } else {
    RUNTIME_ARRAY_BODY(args)[argOffset++]
      = reinterpret_cast<uintptr_t>(sp++);
  }
  RUNTIME_ARRAY_BODY(types)[typeOffset++] = POINTER_TYPE;

  MethodSpecIterator it
    (t, reinterpret_cast<const char*>
     (&byteArrayBody(t, methodSpec(t, method), 0)));
  
  while (it.hasNext()) {
    unsigned type = RUNTIME_ARRAY_BODY(types)[typeOffset++]
      = fieldType(t, fieldCode(t, *it.next()));

    switch (type) {
    case INT8_TYPE:
    case INT16_TYPE:
    case INT32_TYPE:
    case FLOAT_TYPE:
      RUNTIME_ARRAY_BODY(args)[argOffset++] = *(sp++);
      break;

    case INT64_TYPE:
    case DOUBLE_TYPE: {
      memcpy(RUNTIME_ARRAY_BODY(args) + argOffset, sp, 8);
      argOffset += (8 / BytesPerWord);
      sp += 2;
    } break;

    case POINTER_TYPE: {
      if (*sp) {
        RUNTIME_ARRAY_BODY(args)[argOffset++]
          = reinterpret_cast<uintptr_t>(sp);
      } else {
        RUNTIME_ARRAY_BODY(args)[argOffset++] = 0;
      }
      ++ sp;
    } break;

    default: abort(t);
    }
  }

  unsigned returnCode = methodReturnCode(t, method);
  unsigned returnType = fieldType(t, returnCode);
  uint64_t result;

  if (DebugNatives) {
    fprintf(stderr, "invoke native method %s.%s\n",
            &byteArrayBody(t, className(t, methodClass(t, method)), 0),
            &byteArrayBody(t, methodName(t, method), 0));
  }

  if (methodFlags(t, method) & ACC_SYNCHRONIZED) {
    if (methodFlags(t, method) & ACC_STATIC) {
      acquire(t, methodClass(t, method));
    } else {
      acquire(t, *reinterpret_cast<object*>(RUNTIME_ARRAY_BODY(args)[1]));
    }
  }

  Reference* reference = t->reference;

  { ENTER(t, Thread::IdleState);

    bool noThrow = t->checkpoint->noThrow;
    t->checkpoint->noThrow = true;
    THREAD_RESOURCE(t, bool, noThrow, t->checkpoint->noThrow = noThrow);

    result = t->m->system->call
      (function,
       RUNTIME_ARRAY_BODY(args),
       RUNTIME_ARRAY_BODY(types),
       count,
       footprint * BytesPerWord,
       returnType);
  }

  if (methodFlags(t, method) & ACC_SYNCHRONIZED) {
    if (methodFlags(t, method) & ACC_STATIC) {
      release(t, methodClass(t, method));
    } else {
      release(t, *reinterpret_cast<object*>(RUNTIME_ARRAY_BODY(args)[1]));
    }
  }

  if (DebugNatives) {
    fprintf(stderr, "return from native method %s.%s\n",
            &byteArrayBody(t, className(t, methodClass(t, method)), 0),
            &byteArrayBody(t, methodName(t, method), 0));
  }

  if (UNLIKELY(t->exception)) {
    object exception = t->exception;
    t->exception = 0;
    vm::throw_(t, exception);
  }

  switch (returnCode) {
  case ByteField:
  case BooleanField:
    result = static_cast<int8_t>(result);
    break;

  case CharField:
    result = static_cast<uint16_t>(result);
    break;

  case ShortField:
    result = static_cast<int16_t>(result);
    break;

  case FloatField:
  case IntField:
    result = static_cast<int32_t>(result);
    break;

  case LongField:
  case DoubleField:
    break;

  case ObjectField:
    result = static_cast<uintptr_t>(result) ? *reinterpret_cast<uintptr_t*>
      (static_cast<uintptr_t>(result)) : 0;
    break;

  case VoidField:
    result = 0;
    break;

  default: abort(t);
  }

  while (t->reference != reference) {
    dispose(t, t->reference);
  }

  return result;
}
  
uint64_t
invokeNative2(MyThread* t, object method)
{
  object native = methodRuntimeDataNative(t, getMethodRuntimeData(t, method));
  if (nativeFast(t, native)) {
    return invokeNativeFast(t, method, nativeFunction(t, native));
  } else {
    return invokeNativeSlow(t, method, nativeFunction(t, native));
  }
}

uint64_t
invokeNative(MyThread* t)
{
  if (t->trace->nativeMethod == 0) {
    void* ip;
    if (t->tailAddress) {
      ip = t->tailAddress;
      t->tailAddress = 0;
    } else {
      ip = getIp(t);
    }

    object node = findCallNode(t, ip);
    object target = callNodeTarget(t, node);
    if (callNodeFlags(t, node) & TraceElement::VirtualCall) {
      target = resolveTarget(t, t->stack, target);
    }
    t->trace->nativeMethod = target;
  }

  assert(t, t->tailAddress == 0);

  uint64_t result = 0;

  t->trace->targetMethod = t->trace->nativeMethod;

  t->m->classpath->resolveNative(t, t->trace->nativeMethod);

  result = invokeNative2(t, t->trace->nativeMethod);

  unsigned parameterFootprint = methodParameterFootprint
    (t, t->trace->targetMethod);

  uintptr_t* stack = static_cast<uintptr_t*>(t->stack);

  if (TailCalls
      and t->arch->argumentFootprint(parameterFootprint)
      > t->arch->stackAlignmentInWords())
  {
    stack += t->arch->argumentFootprint(parameterFootprint)
      - t->arch->stackAlignmentInWords();
  }

  stack += t->arch->frameReturnAddressSize();

  transition(t, getIp(t), stack, t->continuation, t->trace);

  t->trace->targetMethod = 0;
  t->trace->nativeMethod = 0;

  return result;
}

void
findFrameMapInSimpleTable(MyThread* t, object method, object table,
                          int32_t offset, int32_t** map, unsigned* start)
{
  unsigned tableSize = simpleFrameMapTableSize(t, method, table);
  unsigned indexSize = intArrayLength(t, table) - tableSize;

  *map = &intArrayBody(t, table, indexSize);
    
  unsigned bottom = 0;
  unsigned top = indexSize;
  for (unsigned span = top - bottom; span; span = top - bottom) {
    unsigned middle = bottom + (span / 2);
    int32_t v = intArrayBody(t, table, middle);
      
    if (offset == v) {
      *start = frameMapSizeInBits(t, method) * middle;
      return;
    } else if (offset < v) {
      top = middle;
    } else {
      bottom = middle + 1;
    }
  }

  abort(t);
}

unsigned
findFrameMap(MyThread* t, void* stack, object method, object table,
             unsigned pathIndex)
{
  if (pathIndex) {
    FrameMapTablePath* path = reinterpret_cast<FrameMapTablePath*>
      (&byteArrayBody(t, table, pathIndex));
    
    void* address = static_cast<void**>(stack)[path->stackIndex];
    uint8_t* base = reinterpret_cast<uint8_t*>(methodAddress(t, method));
    for (unsigned i = 0; i < path->elementCount; ++i) {
      if (address == base + path->elements[i]) {
        return i + (path->elementCount * findFrameMap
                    (t, stack, method, table, path->next));
      }
    }

    abort(t);
  } else {
    return 0;
  }
}

void
findFrameMapInGeneralTable(MyThread* t, void* stack, object method,
                           object table, int32_t offset, int32_t** map,
                           unsigned* start)
{
  FrameMapTableHeader* header = reinterpret_cast<FrameMapTableHeader*>
    (&byteArrayBody(t, table, 0));

  FrameMapTableIndexElement* index
    = reinterpret_cast<FrameMapTableIndexElement*>
    (&byteArrayBody(t, table, sizeof(FrameMapTableHeader)));

  *map = reinterpret_cast<int32_t*>(index + header->indexCount);

  unsigned bottom = 0;
  unsigned top = header->indexCount;
  for (unsigned span = top - bottom; span; span = top - bottom) {
    unsigned middle = bottom + (span / 2);
    FrameMapTableIndexElement* v = index + middle;
                                     
    if (offset == v->offset) {
      *start = v->base + (findFrameMap(t, stack, method, table, v->path)
                          * frameMapSizeInBits(t, method));
      return;
    } else if (offset < v->offset) {
      top = middle;
    } else {
      bottom = middle + 1;
    }
  }

  abort(t);
}

void
findFrameMap(MyThread* t, void* stack, object method, int32_t offset,
             int32_t** map, unsigned* start)
{
  object table = codePool(t, methodCode(t, method));
  if (objectClass(t, table) == type(t, Machine::IntArrayType)) {
    findFrameMapInSimpleTable(t, method, table, offset, map, start);
  } else {
    findFrameMapInGeneralTable(t, stack, method, table, offset, map, start);
  }
}

void
visitStackAndLocals(MyThread* t, Heap::Visitor* v, void* frame, object method,
                    void* ip)
{
  unsigned count = frameMapSizeInBits(t, method);

  if (count) {
    void* stack = stackForFrame(t, frame, method);

    int32_t* map;
    unsigned offset;
    findFrameMap
      (t, stack, method, difference
       (ip, reinterpret_cast<void*>(methodAddress(t, method))), &map, &offset);

    for (unsigned i = 0; i < count; ++i) {
      int j = offset + i;
      if (map[j / 32] & (static_cast<int32_t>(1) << (j % 32))) {
        v->visit(localObject(t, stack, method, i));        
      }
    }
  }
}

void
visitArgument(MyThread* t, Heap::Visitor* v, void* stack, unsigned index)
{
  v->visit(static_cast<object*>(stack)
           + index
           + t->arch->frameReturnAddressSize()
           + t->arch->frameFooterSize());
}

void
visitArguments(MyThread* t, Heap::Visitor* v, void* stack, object method)
{
  unsigned index = 0;

  if ((methodFlags(t, method) & ACC_STATIC) == 0) {
    visitArgument(t, v, stack, index++);
  }

  for (MethodSpecIterator it
         (t, reinterpret_cast<const char*>
          (&byteArrayBody(t, methodSpec(t, method), 0)));
       it.hasNext();)
  {
    switch (*it.next()) {
    case 'L':
    case '[':
      visitArgument(t, v, stack, index++);
      break;
      
    case 'J':
    case 'D':
      index += 2;
      break;

    default:
      ++ index;
      break;
    }
  }
}

void
visitStack(MyThread* t, Heap::Visitor* v)
{
  void* ip = getIp(t);
  void* stack = t->stack;

  MyThread::CallTrace* trace = t->trace;
  object targetMethod = (trace ? trace->targetMethod : 0);
  object target = targetMethod;

  while (stack) {
    if (targetMethod) {
      visitArguments(t, v, stack, targetMethod);
      targetMethod = 0;
    }

    object method = methodForIp(t, ip);
    if (method) {
      PROTECT(t, method);

      void* nextIp = ip;
      nextFrame(t, &nextIp, &stack, method, target);

      visitStackAndLocals(t, v, stack, method, ip);

      ip = nextIp;

      target = method;
    } else if (trace) {
      stack = trace->stack;
      ip = trace->ip;
      trace = trace->next;

      if (trace) {
        targetMethod = trace->targetMethod;
        target = targetMethod;
      } else {
        target = 0;
      }
    } else {
      break;
    }
  }
}

void
walkContinuationBody(MyThread* t, Heap::Walker* w, object c, int start)
{
  const int BodyOffset = ContinuationBody / BytesPerWord;

  object method = static_cast<object>
    (t->m->heap->follow(continuationMethod(t, c)));
  int count = frameMapSizeInBits(t, method);

  if (count) {
    int stack = BodyOffset
      + (continuationFramePointerOffset(t, c) / BytesPerWord)
      - t->arch->framePointerOffset()
      - stackOffsetFromFrame(t, method);

    int first = stack + localOffsetFromStack(t, count - 1, method);
    if (start > first) {
      count -= start - first;
    }

    int32_t* map;
    unsigned offset;
    findFrameMap
      (t, reinterpret_cast<uintptr_t*>(c) + stack, method, difference
       (continuationAddress(t, c),
        reinterpret_cast<void*>(methodAddress(t, method))), &map, &offset);

    for (int i = count - 1; i >= 0; --i) {
      int j = offset + i;
      if (map[j / 32] & (static_cast<int32_t>(1) << (j % 32))) {
        if (not w->visit(stack + localOffsetFromStack(t, i, method))) {
          return;
        }
      }
    }
  }
}

void
callContinuation(MyThread* t, object continuation, object result,
                 object exception, void* ip, void* stack)
{
  assert(t, t->exception == 0);

  if (exception) {
    t->exception = exception;

    MyThread::TraceContext c(t, ip, stack, continuation, t->trace);

    void* frame;
    findUnwindTarget(t, &ip, &frame, &stack, &continuation);
  }

  t->trace->nativeMethod = 0;
  t->trace->targetMethod = 0;

  popResources(t);

  transition(t, ip, stack, continuation, t->trace);

  vmJump(ip, 0, stack, t, reinterpret_cast<uintptr_t>(result), 0);
}

int8_t*
returnSpec(MyThread* t, object method)
{
  int8_t* s = &byteArrayBody(t, methodSpec(t, method), 0);
  while (*s and *s != ')') ++ s;
  expect(t, *s == ')');
  return s + 1;
}

object
returnClass(MyThread* t, object method)
{
  PROTECT(t, method);

  int8_t* spec = returnSpec(t, method);
  unsigned length = strlen(reinterpret_cast<char*>(spec));
  object name;
  if (*spec == '[') {
    name = makeByteArray(t, length + 1);
    memcpy(&byteArrayBody(t, name, 0), spec, length);
  } else {
    assert(t, *spec == 'L');
    assert(t, spec[length - 1] == ';');
    name = makeByteArray(t, length - 1);
    memcpy(&byteArrayBody(t, name, 0), spec + 1, length - 2);
  }

  return resolveClass(t, classLoader(t, methodClass(t, method)), name);
}

bool
compatibleReturnType(MyThread* t, object oldMethod, object newMethod)
{
  if (oldMethod == newMethod) {
    return true;
  } else if (methodReturnCode(t, oldMethod) == methodReturnCode(t, newMethod))
  {
    if (methodReturnCode(t, oldMethod) == ObjectField) {
      PROTECT(t, newMethod);

      object oldClass = returnClass(t, oldMethod);
      PROTECT(t, oldClass);

      object newClass = returnClass(t, newMethod);

      return isAssignableFrom(t, oldClass, newClass);
    } else {
      return true;
    }
  } else {
    return methodReturnCode(t, oldMethod) == VoidField;
  }
}

void
jumpAndInvoke(MyThread* t, object method, void* stack, ...)
{
  t->trace->targetMethod = 0;

  if (methodFlags(t, method) & ACC_NATIVE) {
    t->trace->nativeMethod = method;
  } else {
    t->trace->nativeMethod = 0;
  }

  unsigned argumentCount = methodParameterFootprint(t, method);
  THREAD_RUNTIME_ARRAY(t, uintptr_t, arguments, argumentCount);
  va_list a; va_start(a, stack);
  for (unsigned i = 0; i < argumentCount; ++i) {
    RUNTIME_ARRAY_BODY(arguments)[i] = va_arg(a, uintptr_t);
  }
  va_end(a);

  assert(t, t->exception == 0);

  popResources(t);
  
  vmJumpAndInvoke
    (t, reinterpret_cast<void*>(methodAddress(t, method)),
     stack,
     argumentCount * BytesPerWord,
     RUNTIME_ARRAY_BODY(arguments),
     (t->arch->alignFrameSize(t->arch->argumentFootprint(argumentCount))
      + t->arch->frameReturnAddressSize())
     * BytesPerWord);
}

void
callContinuation(MyThread* t, object continuation, object result,
                 object exception)
{
  enum {
    Call,
    Unwind,
    Rewind
  } action;

  object nextContinuation = 0;

  if (t->continuation == 0
      or continuationContext(t, t->continuation)
      != continuationContext(t, continuation))
  {
    PROTECT(t, continuation);
    PROTECT(t, result);
    PROTECT(t, exception);

    if (compatibleReturnType
        (t, t->trace->originalMethod, continuationContextMethod
         (t, continuationContext(t, continuation))))
    {
      object oldContext;
      object unwindContext;

      if (t->continuation) {
        oldContext = continuationContext(t, t->continuation);
        unwindContext = oldContext;
      } else {
        oldContext = 0;
        unwindContext = 0;
      }

      object rewindContext = 0;

      for (object newContext = continuationContext(t, continuation);
           newContext; newContext = continuationContextNext(t, newContext))
      {
        if (newContext == oldContext) {
          unwindContext = 0;
          break;
        } else {
          rewindContext = newContext;
        }
      }

      if (unwindContext
          and continuationContextContinuation(t, unwindContext))
      {
        nextContinuation = continuationContextContinuation(t, unwindContext);
        result = makeUnwindResult(t, continuation, result, exception);
        action = Unwind;
      } else if (rewindContext
                 and continuationContextContinuation(t, rewindContext))
      {
        nextContinuation = continuationContextContinuation(t, rewindContext);
        action = Rewind;

        if (root(t, RewindMethod) == 0) {
          PROTECT(t, nextContinuation);
            
          object method = resolveMethod
            (t, root(t, Machine::BootLoader), "avian/Continuations", "rewind",
             "(Ljava/lang/Runnable;Lavian/Callback;Ljava/lang/Object;"
             "Ljava/lang/Throwable;)V");

          PROTECT(t, method);
            
          compile(t, local::codeAllocator(t), 0, method);
            
          setRoot(t, RewindMethod, method);
        }
      } else {
        action = Call;
      }
    } else {
      throwNew(t, Machine::IncompatibleContinuationExceptionType);
    }
  } else {
    action = Call;
  }

  void* ip;
  void* frame;
  void* stack;
  object threadContinuation;
  findUnwindTarget(t, &ip, &frame, &stack, &threadContinuation);

  switch (action) {
  case Call: {
    callContinuation(t, continuation, result, exception, ip, stack);
  } break;

  case Unwind: {
    callContinuation(t, nextContinuation, result, 0, ip, stack);
  } break;

  case Rewind: {
    transition(t, 0, 0, nextContinuation, t->trace);

    jumpAndInvoke
      (t, root(t, RewindMethod), stack,
       continuationContextBefore(t, continuationContext(t, nextContinuation)),
       continuation, result, exception);
  } break;

  default:
    abort(t);
  }
}

void
callWithCurrentContinuation(MyThread* t, object receiver)
{
  object method = 0;
  void* ip = 0;
  void* stack = 0;

  { PROTECT(t, receiver);

    if (root(t, ReceiveMethod) == 0) {
      object m = resolveMethod
        (t, root(t, Machine::BootLoader), "avian/CallbackReceiver", "receive",
         "(Lavian/Callback;)Ljava/lang/Object;");

      if (m) {
        setRoot(t, ReceiveMethod, m);

        object continuationClass = type(t, Machine::ContinuationType);
        
        if (classVmFlags(t, continuationClass) & BootstrapFlag) {
          resolveSystemClass
            (t, root(t, Machine::BootLoader),
             vm::className(t, continuationClass));
        }
      }
    }

    method = findInterfaceMethod
      (t, root(t, ReceiveMethod), objectClass(t, receiver));
    PROTECT(t, method);
        
    compile(t, local::codeAllocator(t), 0, method);

    t->continuation = makeCurrentContinuation(t, &ip, &stack);
  }

  jumpAndInvoke(t, method, stack, receiver, t->continuation);
}

void
dynamicWind(MyThread* t, object before, object thunk, object after)
{
  void* ip = 0;
  void* stack = 0;

  { PROTECT(t, before);
    PROTECT(t, thunk);
    PROTECT(t, after);

    if (root(t, WindMethod) == 0) {
      object method = resolveMethod
        (t, root(t, Machine::BootLoader), "avian/Continuations", "wind",
         "(Ljava/lang/Runnable;Ljava/util/concurrent/Callable;"
         "Ljava/lang/Runnable;)Lavian/Continuations$UnwindResult;");

      if (method) {
        setRoot(t, WindMethod, method);
        compile(t, local::codeAllocator(t), 0, method);
      }
    }

    t->continuation = makeCurrentContinuation(t, &ip, &stack);

    object newContext = makeContinuationContext
      (t, continuationContext(t, t->continuation), before, after,
       t->continuation, t->trace->originalMethod);

    set(t, t->continuation, ContinuationContext, newContext);
  }

  jumpAndInvoke(t, root(t, WindMethod), stack, before, thunk, after);
}

class ArgumentList {
 public:
  ArgumentList(Thread* t, uintptr_t* array, unsigned size, bool* objectMask,
               object this_, const char* spec, bool indirectObjects,
               va_list arguments):
    t(static_cast<MyThread*>(t)),
    array(array),
    objectMask(objectMask),
    size(size),
    position(0),
    protector(this)
  {
    if (this_) {
      addObject(this_);
    }

    for (MethodSpecIterator it(t, spec); it.hasNext();) {
      switch (*it.next()) {
      case 'L':
      case '[':
        if (indirectObjects) {
          object* v = va_arg(arguments, object*);
          addObject(v ? *v : 0);
        } else {
          addObject(va_arg(arguments, object));
        }
        break;
      
      case 'J':
      case 'D':
        addLong(va_arg(arguments, uint64_t));
        break;

      default:
        addInt(va_arg(arguments, uint32_t));
        break;        
      }
    }
  }

  ArgumentList(Thread* t, uintptr_t* array, unsigned size, bool* objectMask,
               object this_, const char* spec, object arguments):
    t(static_cast<MyThread*>(t)),
    array(array),
    objectMask(objectMask),
    size(size),
    position(0),
    protector(this)
  {
    if (this_) {
      addObject(this_);
    }

    unsigned index = 0;
    for (MethodSpecIterator it(t, spec); it.hasNext();) {
      switch (*it.next()) {
      case 'L':
      case '[':
        addObject(objectArrayBody(t, arguments, index++));
        break;
      
      case 'J':
      case 'D':
        addLong(cast<int64_t>(objectArrayBody(t, arguments, index++), 8));
        break;

      default:
        addInt(cast<int32_t>(objectArrayBody(t, arguments, index++),
                             BytesPerWord));
        break;
      }
    }
  }

  void addObject(object v) {
    assert(t, position < size);

    array[position] = reinterpret_cast<uintptr_t>(v);
    objectMask[position] = true;
    ++ position;
  }

  void addInt(uintptr_t v) {
    assert(t, position < size);

    array[position] = v;
    objectMask[position] = false;
    ++ position;
  }

  void addLong(uint64_t v) {
    assert(t, position < size - 1);

    memcpy(array + position, &v, 8);

    objectMask[position] = false;
    objectMask[position + 1] = false;

    position += 2;
  }

  MyThread* t;
  uintptr_t* array;
  bool* objectMask;
  unsigned size;
  unsigned position;

  class MyProtector: public Thread::Protector {
   public:
    MyProtector(ArgumentList* list): Protector(list->t), list(list) { }

    virtual void visit(Heap::Visitor* v) {
      for (unsigned i = 0; i < list->position; ++i) {
        if (list->objectMask[i]) {
          v->visit(reinterpret_cast<object*>(list->array + i));
        }
      }
    }

    ArgumentList* list;
  } protector;
};

object
invoke(Thread* thread, object method, ArgumentList* arguments)
{
  MyThread* t = static_cast<MyThread*>(thread);

  if (false) {
    PROTECT(t, method);

    compile(t, local::codeAllocator(static_cast<MyThread*>(t)), 0,
            resolveMethod
            (t, root(t, Machine::AppLoader),
             "foo/ClassName",
             "methodName",
             "()V"));
  }

  uintptr_t stackLimit = t->stackLimit;
  uintptr_t stackPosition = reinterpret_cast<uintptr_t>(&t);
  if (stackLimit == 0) {
    t->stackLimit = stackPosition - StackSizeInBytes;
  } else if (stackPosition < stackLimit) {
    throwNew(t, Machine::StackOverflowErrorType);
  }

  THREAD_RESOURCE(t, uintptr_t, stackLimit,
                  static_cast<MyThread*>(t)->stackLimit = stackLimit);

  unsigned returnCode = methodReturnCode(t, method);
  unsigned returnType = fieldType(t, returnCode);

  uint64_t result;

  { MyThread::CallTrace trace(t, method);

    MyCheckpoint checkpoint(t);

    assert(t, arguments->position == arguments->size);

    result = vmInvoke
      (t, reinterpret_cast<void*>(methodAddress(t, method)),
       arguments->array,
       arguments->position * BytesPerWord,
       t->arch->alignFrameSize
       (t->arch->argumentFootprint(arguments->position))
       * BytesPerWord,
       returnType);
  }

  if (t->exception) { 
    if (UNLIKELY(t->flags & Thread::UseBackupHeapFlag)) {
      collect(t, Heap::MinorCollection);
    }
    
    object exception = t->exception;
    t->exception = 0;
    vm::throw_(t, exception);
  }

  object r;
  switch (returnCode) {
  case ByteField:
  case BooleanField:
  case CharField:
  case ShortField:
  case FloatField:
  case IntField:
    r = makeInt(t, result);
    break;

  case LongField:
  case DoubleField:
    r = makeLong(t, result);
    break;

  case ObjectField:
    r = reinterpret_cast<object>(result);
    break;

  case VoidField:
    r = 0;
    break;

  default:
    abort(t);
  }

  return r;
}

class SignalHandler: public System::SignalHandler {
 public:
  SignalHandler(Machine::Type type, Machine::Root root, unsigned fixedSize):
    m(0), type(type), root(root), fixedSize(fixedSize) { }

  virtual bool handleSignal(void** ip, void** frame, void** stack,
                            void** thread)
  {
    MyThread* t = static_cast<MyThread*>(m->localThread->get());
    if (t and t->state == Thread::ActiveState) {
      object node = methodForIp(t, *ip);
      if (node) {
        // add one to the IP since findLineNumber will subtract one
        // when we make the trace:
        MyThread::TraceContext context
          (t, static_cast<uint8_t*>(*ip) + 1,
           static_cast<void**>(*stack) - t->arch->frameReturnAddressSize(),
           t->continuation, t->trace);

        if (ensure(t, fixedSize + traceSize(t))) {
          atomicOr(&(t->flags), Thread::TracingFlag);
          t->exception = makeThrowable(t, type);
          atomicAnd(&(t->flags), ~Thread::TracingFlag);
        } else {
          // not enough memory available for a new exception and stack
          // trace -- use a preallocated instance instead
          t->exception = vm::root(t, root);
        }

        // printTrace(t, t->exception);

        object continuation;
        findUnwindTarget(t, ip, frame, stack, &continuation);

        transition(t, ip, stack, continuation, t->trace);

        *thread = t;

        return true;
      }
    }

    if (compileLog) {
      fflush(compileLog);
    }

    return false;
  }

  Machine* m;
  Machine::Type type;
  Machine::Root root;
  unsigned fixedSize;
};

bool
isThunk(MyThread* t, void* ip);

bool
isVirtualThunk(MyThread* t, void* ip);

bool
isThunkUnsafeStack(MyThread* t, void* ip);

void
boot(MyThread* t, BootImage* image, uint8_t* code);

class MyProcessor;

MyProcessor*
processor(MyThread* t);

void
compileThunks(MyThread* t, FixedAllocator* allocator);

class MyProcessor: public Processor {
 public:
  class Thunk {
   public:
    Thunk():
      start(0), frameSavedOffset(0), length(0)
    { }

    Thunk(uint8_t* start, unsigned frameSavedOffset, unsigned length):
      start(start), frameSavedOffset(frameSavedOffset), length(length)
    { }

    uint8_t* start;
    unsigned frameSavedOffset;
    unsigned length;
  };

  class ThunkCollection {
   public:
    Thunk default_;
    Thunk defaultVirtual;
    Thunk native;
    Thunk aioob;
    Thunk stackOverflow;
    Thunk table;
  };

  MyProcessor(System* s, Allocator* allocator, bool useNativeFeatures):
    s(s),
    allocator(allocator),
    roots(0),
    bootImage(0),
    heapImage(0),
    codeImage(0),
    codeImageSize(0),
    segFaultHandler(Machine::NullPointerExceptionType,
                    Machine::NullPointerException,
                    FixedSizeOfNullPointerException),
    divideByZeroHandler(Machine::ArithmeticExceptionType,
                        Machine::ArithmeticException,
                        FixedSizeOfArithmeticException),
    codeAllocator(s, 0, 0),
    callTableSize(0),
    useNativeFeatures(useNativeFeatures)
  {
    thunkTable[compileMethodIndex] = voidPointer(local::compileMethod);
    thunkTable[compileVirtualMethodIndex] = voidPointer(compileVirtualMethod);
    thunkTable[invokeNativeIndex] = voidPointer(invokeNative);
    thunkTable[throwArrayIndexOutOfBoundsIndex] = voidPointer
      (throwArrayIndexOutOfBounds);
    thunkTable[throwStackOverflowIndex] = voidPointer(throwStackOverflow);
#define THUNK(s) thunkTable[s##Index] = voidPointer(s);
#include "thunks.cpp"
#undef THUNK
    // Set the dummyIndex entry to a constant which should require the
    // maximum number of bytes to represent in assembly code
    // (i.e. can't be represented by a smaller number of bytes and
    // implicitly sign- or zero-extended).  We'll use this property
    // later to determine the maximum size of a thunk in the thunk
    // table.
    thunkTable[dummyIndex] = reinterpret_cast<void*>
      (static_cast<uintptr_t>(UINT64_C(0x5555555555555555)));
  }

  virtual Thread*
  makeThread(Machine* m, object javaThread, Thread* parent)
  {
    MyThread* t = new (m->heap->allocate(sizeof(MyThread)))
      MyThread(m, javaThread, static_cast<MyThread*>(parent),
               useNativeFeatures);

    t->heapImage = heapImage;
    t->codeImage = codeImage;
    t->thunkTable = thunkTable;

    if (false) {
      fprintf(stderr, "stack %d\n",
              difference(&(t->stack), t));
      fprintf(stderr, "scratch %d\n",
              difference(&(t->scratch), t));
      fprintf(stderr, "continuation %d\n",
              difference(&(t->continuation), t));
      fprintf(stderr, "exception %d\n",
              difference(&(t->exception), t));
      fprintf(stderr, "exceptionStackAdjustment %d\n",
              difference(&(t->exceptionStackAdjustment), t));
      fprintf(stderr, "exceptionOffset %d\n",
              difference(&(t->exceptionOffset), t));
      fprintf(stderr, "exceptionHandler %d\n",
              difference(&(t->exceptionHandler), t));
      fprintf(stderr, "tailAddress %d\n",
              difference(&(t->tailAddress), t));
      fprintf(stderr, "stackLimit %d\n",
              difference(&(t->stackLimit), t));
      fprintf(stderr, "ip %d\n",
              difference(&(t->ip), t));
      fprintf(stderr, "virtualCallTarget %d\n",
              difference(&(t->virtualCallTarget), t));
      fprintf(stderr, "virtualCallIndex %d\n",
              difference(&(t->virtualCallIndex), t));
      fprintf(stderr, "heapImage %d\n",
              difference(&(t->heapImage), t));
      fprintf(stderr, "codeImage %d\n",
              difference(&(t->codeImage), t));
      fprintf(stderr, "thunkTable %d\n",
              difference(&(t->thunkTable), t));
      exit(0);
    }

    t->init();

    return t;
  }

  virtual object
  makeMethod(vm::Thread* t,
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
             object code)
  {
    if (code) {
      codeCompiled(t, code) = local::defaultThunk(static_cast<MyThread*>(t));
    }

    return vm::makeMethod
      (t, vmFlags, returnCode, parameterCount, parameterFootprint, flags,
       offset, 0, 0, name, spec, addendum, class_, code);
  }

  virtual object
  makeClass(vm::Thread* t,
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
            object staticTable,
            object addendum, 
            object loader,
            unsigned vtableLength)
  {
    return vm::makeClass
      (t, flags, vmFlags, fixedSize, arrayElementSize, arrayDimensions,
       0, objectMask, name, sourceFile, super, interfaceTable, virtualTable,
       fieldTable, methodTable, staticTable, addendum, loader, 0,
       vtableLength);
  }

  virtual void
  initVtable(Thread* t, object c)
  {
    PROTECT(t, c);
    for (int i = classLength(t, c) - 1; i >= 0; --i) {
      void* thunk = reinterpret_cast<void*>
        (virtualThunk(static_cast<MyThread*>(t), i));
      classVtable(t, c, i) = thunk;
    }
  }

  virtual void
  visitObjects(Thread* vmt, Heap::Visitor* v)
  {
    MyThread* t = static_cast<MyThread*>(vmt);

    if (t == t->m->rootThread) {
      v->visit(&roots);
    }

    for (MyThread::CallTrace* trace = t->trace; trace; trace = trace->next) {
      v->visit(&(trace->continuation));
      v->visit(&(trace->nativeMethod));
      v->visit(&(trace->targetMethod));
      v->visit(&(trace->originalMethod));
    }

    v->visit(&(t->continuation));

    for (Reference* r = t->reference; r; r = r->next) {
      v->visit(&(r->target));
    }

    visitStack(t, v);
  }

  virtual void
  walkStack(Thread* vmt, StackVisitor* v)
  {
    MyThread* t = static_cast<MyThread*>(vmt);

    MyStackWalker walker(t);
    walker.walk(v);
  }

  virtual int
  lineNumber(Thread* vmt, object method, int ip)
  {
    return findLineNumber(static_cast<MyThread*>(vmt), method, ip);
  }

  virtual object*
  makeLocalReference(Thread* vmt, object o)
  {
    if (o) {
      MyThread* t = static_cast<MyThread*>(vmt);

      for (Reference* r = t->reference; r; r = r->next) {
        if (r->target == o) {
          acquire(t, r);

          return &(r->target);
        }
      }

      Reference* r = new (t->m->heap->allocate(sizeof(Reference)))
        Reference(o, &(t->reference));

      acquire(t, r);

      return &(r->target);
    } else {
      return 0;
    }
  }

  virtual void
  disposeLocalReference(Thread* t, object* r)
  {
    if (r) {
      release(t, reinterpret_cast<Reference*>(r));
    }
  }

  virtual object
  invokeArray(Thread* t, object method, object this_, object arguments)
  {
    assert(t, t->exception == 0);

    assert(t, t->state == Thread::ActiveState
           or t->state == Thread::ExclusiveState);

    assert(t, ((methodFlags(t, method) & ACC_STATIC) == 0) xor (this_ == 0));

    method = findMethod(t, method, this_);

    const char* spec = reinterpret_cast<char*>
      (&byteArrayBody(t, methodSpec(t, method), 0));

    unsigned size = methodParameterFootprint(t, method);
    THREAD_RUNTIME_ARRAY(t, uintptr_t, array, size);
    THREAD_RUNTIME_ARRAY(t, bool, objectMask, size);
    ArgumentList list
      (t, RUNTIME_ARRAY_BODY(array), size, RUNTIME_ARRAY_BODY(objectMask),
       this_, spec, arguments);
    
    PROTECT(t, method);

    compile(static_cast<MyThread*>(t),
            local::codeAllocator(static_cast<MyThread*>(t)), 0, method);

    return local::invoke(t, method, &list);
  }

  virtual object
  invokeList(Thread* t, object method, object this_, bool indirectObjects,
             va_list arguments)
  {
    assert(t, t->exception == 0);

    assert(t, t->state == Thread::ActiveState
           or t->state == Thread::ExclusiveState);

    assert(t, ((methodFlags(t, method) & ACC_STATIC) == 0) xor (this_ == 0));
    
    method = findMethod(t, method, this_);

    const char* spec = reinterpret_cast<char*>
      (&byteArrayBody(t, methodSpec(t, method), 0));

    unsigned size = methodParameterFootprint(t, method);
    THREAD_RUNTIME_ARRAY(t, uintptr_t, array, size);
    THREAD_RUNTIME_ARRAY(t, bool, objectMask, size);
    ArgumentList list
      (t, RUNTIME_ARRAY_BODY(array), size, RUNTIME_ARRAY_BODY(objectMask),
       this_, spec, indirectObjects, arguments);

    PROTECT(t, method);

    compile(static_cast<MyThread*>(t),
            local::codeAllocator(static_cast<MyThread*>(t)), 0, method);

    return local::invoke(t, method, &list);
  }

  virtual object
  invokeList(Thread* t, object loader, const char* className,
             const char* methodName, const char* methodSpec,
             object this_, va_list arguments)
  {
    assert(t, t->exception == 0);

    assert(t, t->state == Thread::ActiveState
           or t->state == Thread::ExclusiveState);

    unsigned size = parameterFootprint(t, methodSpec, this_ == 0);
    THREAD_RUNTIME_ARRAY(t, uintptr_t, array, size);
    THREAD_RUNTIME_ARRAY(t, bool, objectMask, size);
    ArgumentList list
      (t, RUNTIME_ARRAY_BODY(array), size, RUNTIME_ARRAY_BODY(objectMask),
       this_, methodSpec, false, arguments);

    object method = resolveMethod
      (t, loader, className, methodName, methodSpec);

    assert(t, ((methodFlags(t, method) & ACC_STATIC) == 0) xor (this_ == 0));

    PROTECT(t, method);
      
    compile(static_cast<MyThread*>(t), 
            local::codeAllocator(static_cast<MyThread*>(t)), 0, method);

    return local::invoke(t, method, &list);
  }

  virtual void dispose(Thread* vmt) {
    MyThread* t = static_cast<MyThread*>(vmt);

    while (t->reference) {
      vm::dispose(t, t->reference);
    }

    t->arch->release();

    t->m->heap->free(t, sizeof(*t));
  }

  virtual void dispose() {
    if (codeAllocator.base) {
      s->freeExecutable(codeAllocator.base, codeAllocator.capacity);
    }

    s->handleSegFault(0);

    allocator->free(this, sizeof(*this));
  }

  virtual object getStackTrace(Thread* vmt, Thread* vmTarget) {
    MyThread* t = static_cast<MyThread*>(vmt);
    MyThread* target = static_cast<MyThread*>(vmTarget);
    MyProcessor* p = this;

    class Visitor: public System::ThreadVisitor {
     public:
      Visitor(MyThread* t, MyProcessor* p, MyThread* target):
        t(t), p(p), target(target), trace(0)
      { }

      virtual void visit(void* ip, void* stack, void* link) {
        MyThread::TraceContext c(target, link);

        if (methodForIp(t, ip)) {
          // we caught the thread in Java code - use the register values
          c.ip = ip;
          c.stack = stack;
          c.javaStackLimit = stack;
        } else if (target->transition) {
          // we caught the thread in native code while in the middle
          // of updating the context fields (MyThread::stack, etc.)
          static_cast<MyThread::Context&>(c) = *(target->transition);
        } else if (isVmInvokeUnsafeStack(ip)) {
          // we caught the thread in native code just after returning
          // from java code, but before clearing MyThread::stack
          // (which now contains a garbage value), and the most recent
          // Java frame, if any, can be found in
          // MyThread::continuation or MyThread::trace
          c.ip = 0;
          c.stack = 0;
        } else if (target->stack
                   and (not isThunkUnsafeStack(t, ip))
                   and (not isVirtualThunk(t, ip)))
        {
          // we caught the thread in a thunk or native code, and the
          // saved stack pointer indicates the most recent Java frame
          // on the stack
          c.ip = getIp(target);
          c.stack = target->stack;
        } else if (isThunk(t, ip) or isVirtualThunk(t, ip)) {
          // we caught the thread in a thunk where the stack register
          // indicates the most recent Java frame on the stack
          
          // On e.g. x86, the return address will have already been
          // pushed onto the stack, in which case we use getIp to
          // retrieve it.  On e.g. PowerPC and ARM, it will be in the
          // link register.  Note that we can't just check if the link
          // argument is null here, since we use ecx/rcx as a
          // pseudo-link register on x86 for the purpose of tail
          // calls.
          c.ip = t->arch->hasLinkRegister() ? link : getIp(t, link, stack);
          c.stack = stack;
        } else {
          // we caught the thread in native code, and the most recent
          // Java frame, if any, can be found in
          // MyThread::continuation or MyThread::trace
          c.ip = 0;
          c.stack = 0;
        }

        if (ensure(t, traceSize(target))) {
          atomicOr(&(t->flags), Thread::TracingFlag);
          trace = makeTrace(t, target);
          atomicAnd(&(t->flags), ~Thread::TracingFlag);
        }
      }

      MyThread* t;
      MyProcessor* p;
      MyThread* target;
      object trace;
    } visitor(t, p, target);

    t->m->system->visit(t->systemThread, target->systemThread, &visitor);

    if (UNLIKELY(t->flags & Thread::UseBackupHeapFlag)) {
      PROTECT(t, visitor.trace);

      collect(t, Heap::MinorCollection);
    }

    return visitor.trace ? visitor.trace : makeObjectArray(t, 0);
  }

  virtual void initialize(BootImage* image, uint8_t* code, unsigned capacity) {
    bootImage = image;
    codeAllocator.base = code;
    codeAllocator.capacity = capacity;
  }

  virtual void compileMethod(Thread* vmt, Zone* zone, object* constants,
                             object* calls, DelayedPromise** addresses,
                             object method, OffsetResolver* resolver)
  {
    MyThread* t = static_cast<MyThread*>(vmt);
    BootContext bootContext(t, *constants, *calls, *addresses, zone, resolver);

    compile(t, &codeAllocator, &bootContext, method);

    *constants = bootContext.constants;
    *calls = bootContext.calls;
    *addresses = bootContext.addresses;
  }

  virtual void visitRoots(Thread* t, HeapWalker* w) {
    bootImage->methodTree = w->visitRoot(root(t, MethodTree));
    bootImage->methodTreeSentinal = w->visitRoot(root(t, MethodTreeSentinal));
    bootImage->virtualThunks = w->visitRoot(root(t, VirtualThunks));
  }

  virtual void normalizeVirtualThunks(Thread* t) {
    for (unsigned i = 0; i < wordArrayLength(t, root(t, VirtualThunks));
         i += 2)
    {
      if (wordArrayBody(t, root(t, VirtualThunks), i)) {
        wordArrayBody(t, root(t, VirtualThunks), i)
          -= reinterpret_cast<uintptr_t>(codeAllocator.base);
      }
    }
  }

  virtual unsigned* makeCallTable(Thread* t, HeapWalker* w) {
    bootImage->codeSize = codeAllocator.offset;
    bootImage->callCount = callTableSize;

    unsigned* table = static_cast<unsigned*>
      (t->m->heap->allocate(callTableSize * sizeof(unsigned) * 2));

    unsigned index = 0;
    for (unsigned i = 0; i < arrayLength(t, root(t, CallTable)); ++i) {
      for (object p = arrayBody(t, root(t, CallTable), i);
           p; p = callNodeNext(t, p))
      {
        table[index++] = targetVW
          (callNodeAddress(t, p)
           - reinterpret_cast<uintptr_t>(codeAllocator.base));
        table[index++] = targetVW
          (w->map()->find(callNodeTarget(t, p))
           | (static_cast<unsigned>(callNodeFlags(t, p)) << TargetBootShift));
      }
    }

    return table;
  }

  virtual void boot(Thread* t, BootImage* image, uint8_t* code) {
    if (codeAllocator.base == 0) {
      codeAllocator.base = static_cast<uint8_t*>
        (s->tryAllocateExecutable(ExecutableAreaSizeInBytes));
      codeAllocator.capacity = ExecutableAreaSizeInBytes;
    }

    if (image and code) {
      local::boot(static_cast<MyThread*>(t), image, code);
    } else {
      roots = makeArray(t, RootCount);

      setRoot(t, CallTable, makeArray(t, 128));
      
      setRoot(t, MethodTreeSentinal, makeTreeNode(t, 0, 0, 0));
      setRoot(t, MethodTree, root(t, MethodTreeSentinal));
      set(t, root(t, MethodTree), TreeNodeLeft,
          root(t, MethodTreeSentinal));
      set(t, root(t, MethodTree), TreeNodeRight,
          root(t, MethodTreeSentinal));
    }

    local::compileThunks(static_cast<MyThread*>(t), &codeAllocator);

    if (not (image and code)) {
      bootThunks = thunks;
    }

    segFaultHandler.m = t->m;
    expect(t, t->m->system->success
           (t->m->system->handleSegFault(&segFaultHandler)));

    divideByZeroHandler.m = t->m;
    expect(t, t->m->system->success
           (t->m->system->handleDivideByZero(&divideByZeroHandler)));
  }

  virtual void callWithCurrentContinuation(Thread* t, object receiver) {
    if (Continuations) {
      local::callWithCurrentContinuation(static_cast<MyThread*>(t), receiver);
    } else {
      abort(t);
    }
  }

  virtual void dynamicWind(Thread* t, object before, object thunk,
                           object after)
  {
    if (Continuations) {
      local::dynamicWind(static_cast<MyThread*>(t), before, thunk, after);
    } else {
      abort(t);
    }
  }

  virtual void feedResultToContinuation(Thread* t, object continuation,
                                        object result)
  {
    if (Continuations) {
      callContinuation(static_cast<MyThread*>(t), continuation, result, 0);
    } else {
      abort(t);
    }
  }

  virtual void feedExceptionToContinuation(Thread* t, object continuation,
                                           object exception)
  {
    if (Continuations) {
      callContinuation(static_cast<MyThread*>(t), continuation, 0, exception);
    } else {
      abort(t);
    }
  }

  virtual void walkContinuationBody(Thread* t, Heap::Walker* w, object o,
                                    unsigned start)
  {
    if (Continuations) {
      local::walkContinuationBody(static_cast<MyThread*>(t), w, o, start);
    } else {
      abort(t);
    }
  }
  
  System* s;
  Allocator* allocator;
  object roots;
  BootImage* bootImage;
  uintptr_t* heapImage;
  uint8_t* codeImage;
  unsigned codeImageSize;
  SignalHandler segFaultHandler;
  SignalHandler divideByZeroHandler;
  FixedAllocator codeAllocator;
  ThunkCollection thunks;
  ThunkCollection bootThunks;
  unsigned callTableSize;
  bool useNativeFeatures;
  void* thunkTable[dummyIndex + 1];
};

void*
compileMethod2(MyThread* t, void* ip)
{
  object node = findCallNode(t, ip);
  object target = callNodeTarget(t, node);

  PROTECT(t, node);
  PROTECT(t, target);

  t->trace->targetMethod = target;

  THREAD_RESOURCE0(t, static_cast<MyThread*>(t)->trace->targetMethod = 0);

  compile(t, codeAllocator(t), 0, target);

  uint8_t* updateIp = static_cast<uint8_t*>(ip);

  MyProcessor* p = processor(t);

  bool updateCaller = updateIp < p->codeImage
    or updateIp >= p->codeImage + p->codeImageSize;

  uintptr_t address;
  if (methodFlags(t, target) & ACC_NATIVE) {
    address = useLongJump(t, reinterpret_cast<uintptr_t>(ip))
      or (not updateCaller) ? bootNativeThunk(t) : nativeThunk(t);
  } else {
    address = methodAddress(t, target);
  }

  if (updateCaller) {
    UnaryOperation op;
    if (callNodeFlags(t, node) & TraceElement::LongCall) {
      if (callNodeFlags(t, node) & TraceElement::TailCall) {
        op = AlignedLongJump;
      } else {
        op = AlignedLongCall;
      }
    } else if (callNodeFlags(t, node) & TraceElement::TailCall) {
      op = AlignedJump;
    } else {
      op = AlignedCall;
    }

    updateCall(t, op, updateIp, reinterpret_cast<void*>(address));
  }

  return reinterpret_cast<void*>(address);
}

bool
isThunk(MyProcessor::ThunkCollection* thunks, void* ip)
{
  uint8_t* thunkStart = thunks->default_.start;
  uint8_t* thunkEnd = thunks->table.start
    + (thunks->table.length * ThunkCount);

  return (reinterpret_cast<uintptr_t>(ip)
          >= reinterpret_cast<uintptr_t>(thunkStart)
          and reinterpret_cast<uintptr_t>(ip)
          < reinterpret_cast<uintptr_t>(thunkEnd));
}

bool
isThunk(MyThread* t, void* ip)
{
  MyProcessor* p = processor(t);

  return isThunk(&(p->thunks), ip) or isThunk(&(p->bootThunks), ip);
}

bool
isThunkUnsafeStack(MyProcessor::Thunk* thunk, void* ip)
{
  return reinterpret_cast<uintptr_t>(ip)
    >= reinterpret_cast<uintptr_t>(thunk->start)
    and reinterpret_cast<uintptr_t>(ip)
    < reinterpret_cast<uintptr_t>(thunk->start + thunk->frameSavedOffset);
}

bool
isThunkUnsafeStack(MyProcessor::ThunkCollection* thunks, void* ip)
{
  const unsigned NamedThunkCount = 5;

  MyProcessor::Thunk table[NamedThunkCount + ThunkCount];

  table[0] = thunks->default_;
  table[1] = thunks->defaultVirtual;
  table[2] = thunks->native;
  table[3] = thunks->aioob;
  table[4] = thunks->stackOverflow;
    
  for (unsigned i = 0; i < ThunkCount; ++i) {
    new (table + NamedThunkCount + i) MyProcessor::Thunk
      (thunks->table.start + (i * thunks->table.length),
       thunks->table.frameSavedOffset,
       thunks->table.length);
  }

  for (unsigned i = 0; i < NamedThunkCount + ThunkCount; ++i) {
    if (isThunkUnsafeStack(table + i, ip)) {
      return true;
    }
  }

  return false;
}

bool
isVirtualThunk(MyThread* t, void* ip)
{
  for (unsigned i = 0; i < wordArrayLength(t, root(t, VirtualThunks)); i += 2)
  {
    uintptr_t start = wordArrayBody(t, root(t, VirtualThunks), i);
    uintptr_t end = start + wordArrayBody(t, root(t, VirtualThunks), i + 1);

    if (reinterpret_cast<uintptr_t>(ip) >= start
        and reinterpret_cast<uintptr_t>(ip) < end)
    {
      return true;
    }
  }

  return false;
}

bool
isThunkUnsafeStack(MyThread* t, void* ip)
{
  MyProcessor* p = processor(t);

  return isThunk(t, ip)
    and (isThunkUnsafeStack(&(p->thunks), ip)
         or isThunkUnsafeStack(&(p->bootThunks), ip));
}

object
findCallNode(MyThread* t, void* address)
{
  if (DebugCallTable) {
    fprintf(stderr, "find call node %p\n", address);
  }

  // we must use a version of the call table at least as recent as the
  // compiled form of the method containing the specified address (see
  // compile(MyThread*, Allocator*, BootContext*, object)):
  loadMemoryBarrier();

  object table = root(t, CallTable);

  intptr_t key = reinterpret_cast<intptr_t>(address);
  unsigned index = static_cast<uintptr_t>(key) & (arrayLength(t, table) - 1);

  for (object n = arrayBody(t, table, index);
       n; n = callNodeNext(t, n))
  {
    intptr_t k = callNodeAddress(t, n);

    if (k == key) {
      return n;
    }
  }

  return 0;
}

object
resizeTable(MyThread* t, object oldTable, unsigned newLength)
{
  PROTECT(t, oldTable);

  object oldNode = 0;
  PROTECT(t, oldNode);

  object newTable = makeArray(t, newLength);
  PROTECT(t, newTable);

  for (unsigned i = 0; i < arrayLength(t, oldTable); ++i) {
    for (oldNode = arrayBody(t, oldTable, i);
         oldNode;
         oldNode = callNodeNext(t, oldNode))
    {
      intptr_t k = callNodeAddress(t, oldNode);

      unsigned index = k & (newLength - 1);

      object newNode = makeCallNode
        (t, callNodeAddress(t, oldNode),
         callNodeTarget(t, oldNode),
         callNodeFlags(t, oldNode),
         arrayBody(t, newTable, index));

      set(t, newTable, ArrayBody + (index * BytesPerWord), newNode);
    }
  }

  return newTable;
}

object
insertCallNode(MyThread* t, object table, unsigned* size, object node)
{
  if (DebugCallTable) {
    fprintf(stderr, "insert call node %p\n",
            reinterpret_cast<void*>(callNodeAddress(t, node)));
  }

  PROTECT(t, table);
  PROTECT(t, node);

  ++ (*size);

  if (*size >= arrayLength(t, table) * 2) { 
    table = resizeTable(t, table, arrayLength(t, table) * 2);
  }

  intptr_t key = callNodeAddress(t, node);
  unsigned index = static_cast<uintptr_t>(key) & (arrayLength(t, table) - 1);

  set(t, node, CallNodeNext, arrayBody(t, table, index));
  set(t, table, ArrayBody + (index * BytesPerWord), node);

  return table;
}

void
insertCallNode(MyThread* t, object node)
{
  setRoot(t, CallTable, insertCallNode
          (t, root(t, CallTable), &(processor(t)->callTableSize), node));
}

object
makeClassMap(Thread* t, unsigned* table, unsigned count,
             uintptr_t* heap)
{
  object array = makeArray(t, nextPowerOfTwo(count));
  object map = makeHashMap(t, 0, array);
  PROTECT(t, map);
  
  for (unsigned i = 0; i < count; ++i) {
    object c = bootObject(heap, table[i]);
    hashMapInsert(t, map, className(t, c), c, byteArrayHash);
  }

  return map;
}

object
makeStaticTableArray(Thread* t, unsigned* bootTable, unsigned bootCount,
                     unsigned* appTable, unsigned appCount, uintptr_t* heap)
{
  object array = makeArray(t, bootCount + appCount);
  
  for (unsigned i = 0; i < bootCount; ++i) {
    set(t, array, ArrayBody + (i * BytesPerWord),
        classStaticTable(t, bootObject(heap, bootTable[i])));
  }

  for (unsigned i = 0; i < appCount; ++i) {
    set(t, array, ArrayBody + ((bootCount + i) * BytesPerWord),
        classStaticTable(t, bootObject(heap, appTable[i])));
  }

  return array;
}

object
makeStringMap(Thread* t, unsigned* table, unsigned count, uintptr_t* heap)
{
  object array = makeArray(t, nextPowerOfTwo(count));
  object map = makeWeakHashMap(t, 0, array);
  PROTECT(t, map);
  
  for (unsigned i = 0; i < count; ++i) {
    object s = bootObject(heap, table[i]);
    hashMapInsert(t, map, s, 0, stringHash);
  }

  return map;
}

object
makeCallTable(MyThread* t, uintptr_t* heap, unsigned* calls, unsigned count,
              uintptr_t base)
{
  object table = makeArray(t, nextPowerOfTwo(count));
  PROTECT(t, table);

  unsigned size = 0;
  for (unsigned i = 0; i < count; ++i) {
    unsigned address = calls[i * 2];
    unsigned target = calls[(i * 2) + 1];

    object node = makeCallNode
       (t, base + address, bootObject(heap, target & BootMask),
        target >> BootShift, 0);

    table = insertCallNode(t, table, &size, node);
  }

  return table;
}

void
fixupHeap(MyThread* t UNUSED, uintptr_t* map, unsigned size, uintptr_t* heap)
{
  for (unsigned word = 0; word < size; ++word) {
    uintptr_t w = map[word];
    if (w) {
      for (unsigned bit = 0; bit < BitsPerWord; ++bit) {
        if (w & (static_cast<uintptr_t>(1) << bit)) {
          unsigned index = indexOf(word, bit);

          uintptr_t* p = heap + index;
          assert(t, *p);
          
          uintptr_t number = *p & BootMask;
          uintptr_t mark = *p >> BootShift;

          if (number) {
            *p = reinterpret_cast<uintptr_t>(heap + (number - 1)) | mark;
            // fprintf(stderr, "fixup %d: %d 0x%x\n", index, static_cast<unsigned>(number), static_cast<unsigned>(*p));
          } else {
            *p = mark;
          }
        }
      }
    }
  }
}

void
resetClassRuntimeState(Thread* t, object c, uintptr_t* heap, unsigned heapSize)
{
  classRuntimeDataIndex(t, c) = 0;

  if (classArrayElementSize(t, c) == 0) {
    object staticTable = classStaticTable(t, c);
    if (staticTable) {
      for (unsigned i = 0; i < singletonCount(t, staticTable); ++i) {
        if (singletonIsObject(t, staticTable, i)
            and (reinterpret_cast<uintptr_t*>
                 (singletonObject(t, staticTable, i)) < heap or
                 reinterpret_cast<uintptr_t*>
                 (singletonObject(t, staticTable, i)) > heap + heapSize))
        {
          singletonObject(t, staticTable, i) = 0;
        }
      }
    }
  }

  if (classMethodTable(t, c)) {
    for (unsigned i = 0; i < arrayLength(t, classMethodTable(t, c)); ++i) {
      object m = arrayBody(t, classMethodTable(t, c), i);

      methodNativeID(t, m) = 0;
      methodRuntimeDataIndex(t, m) = 0;

      if (methodVmFlags(t, m) & ClassInitFlag) {
        classVmFlags(t, c) |= NeedInitFlag;
        classVmFlags(t, c) &= ~InitErrorFlag;
      }
    }
  }

  t->m->processor->initVtable(t, c);
}

void
resetRuntimeState(Thread* t, object map, uintptr_t* heap, unsigned heapSize)
{
  for (HashMapIterator it(t, map); it.hasMore();) {
    resetClassRuntimeState(t, tripleSecond(t, it.next()), heap, heapSize);
  }
}

void
fixupMethods(Thread* t, object map, BootImage* image UNUSED, uint8_t* code)
{
  for (HashMapIterator it(t, map); it.hasMore();) {
    object c = tripleSecond(t, it.next());

    if (classMethodTable(t, c)) {
      for (unsigned i = 0; i < arrayLength(t, classMethodTable(t, c)); ++i) {
        object method = arrayBody(t, classMethodTable(t, c), i);
        if (methodCode(t, method)) {
          assert(t, methodCompiled(t, method)
                 <= static_cast<int32_t>(image->codeSize));

          codeCompiled(t, methodCode(t, method))
            = methodCompiled(t, method) + reinterpret_cast<uintptr_t>(code);

          if (DebugCompile) {
            logCompile
              (static_cast<MyThread*>(t),
               reinterpret_cast<uint8_t*>(methodCompiled(t, method)),
               reinterpret_cast<uintptr_t*>
               (methodCompiled(t, method))[-1],
               reinterpret_cast<char*>
               (&byteArrayBody(t, className(t, methodClass(t, method)), 0)),
               reinterpret_cast<char*>
               (&byteArrayBody(t, methodName(t, method), 0)),
               reinterpret_cast<char*>
               (&byteArrayBody(t, methodSpec(t, method), 0)));
          }
        }
      }
    }

    t->m->processor->initVtable(t, c);
  }
}

MyProcessor::Thunk
thunkToThunk(const BootImage::Thunk& thunk, uint8_t* base)
{
  return MyProcessor::Thunk
    (base + thunk.start, thunk.frameSavedOffset, thunk.length);
}

void
findThunks(MyThread* t, BootImage* image, uint8_t* code)
{
  MyProcessor* p = processor(t);
  
  p->bootThunks.default_ = thunkToThunk(image->thunks.default_, code);
  p->bootThunks.defaultVirtual
    = thunkToThunk(image->thunks.defaultVirtual, code);
  p->bootThunks.native = thunkToThunk(image->thunks.native, code);
  p->bootThunks.aioob = thunkToThunk(image->thunks.aioob, code);
  p->bootThunks.stackOverflow
    = thunkToThunk(image->thunks.stackOverflow, code);
  p->bootThunks.table = thunkToThunk(image->thunks.table, code);
}

void
fixupVirtualThunks(MyThread* t, uint8_t* code)
{
  for (unsigned i = 0; i < wordArrayLength(t, root(t, VirtualThunks)); i += 2)
  {
    if (wordArrayBody(t, root(t, VirtualThunks), i)) {
      wordArrayBody(t, root(t, VirtualThunks), i)
        = wordArrayBody(t, root(t, VirtualThunks), i)
        + reinterpret_cast<uintptr_t>(code);
    }
  }
}

void
boot(MyThread* t, BootImage* image, uint8_t* code)
{
  assert(t, image->magic == BootImage::Magic);

  unsigned* bootClassTable = reinterpret_cast<unsigned*>(image + 1);
  unsigned* appClassTable = bootClassTable + image->bootClassCount;
  unsigned* stringTable = appClassTable + image->appClassCount;
  unsigned* callTable = stringTable + image->stringCount;

  uintptr_t* heapMap = reinterpret_cast<uintptr_t*>
    (padWord(reinterpret_cast<uintptr_t>(callTable + (image->callCount * 2))));

  unsigned heapMapSizeInWords = ceiling
    (heapMapSize(image->heapSize), BytesPerWord);
  uintptr_t* heap = heapMap + heapMapSizeInWords;

  MyProcessor* p = static_cast<MyProcessor*>(t->m->processor);

  t->heapImage = p->heapImage = heap;

  // fprintf(stderr, "heap from %p to %p\n",
  //         heap, heap + ceiling(image->heapSize, BytesPerWord));

  t->codeImage = p->codeImage = code;
  p->codeImageSize = image->codeSize;

  // fprintf(stderr, "code from %p to %p\n",
  //         code, code + image->codeSize);
 
  static bool fixed = false;

  if (not fixed) {
    fixupHeap(t, heapMap, heapMapSizeInWords, heap);
  }
  
  t->m->heap->setImmortalHeap(heap, image->heapSize / BytesPerWord);

  t->m->types = bootObject(heap, image->types);

  t->m->roots = makeArray(t, Machine::RootCount);

  setRoot(t, Machine::BootLoader, bootObject(heap, image->bootLoader));
  setRoot(t, Machine::AppLoader, bootObject(heap, image->appLoader));

  p->roots = makeArray(t, RootCount);
  
  setRoot(t, MethodTree, bootObject(heap, image->methodTree));
  setRoot(t, MethodTreeSentinal, bootObject(heap, image->methodTreeSentinal));

  setRoot(t, VirtualThunks, bootObject(heap, image->virtualThunks));

  { object map = makeClassMap(t, bootClassTable, image->bootClassCount, heap);
    set(t, root(t, Machine::BootLoader), ClassLoaderMap, map);
  }

  systemClassLoaderFinder(t, root(t, Machine::BootLoader)) = t->m->bootFinder;

  { object map = makeClassMap(t, appClassTable, image->appClassCount, heap);
    set(t, root(t, Machine::AppLoader), ClassLoaderMap, map);
  }

  systemClassLoaderFinder(t, root(t, Machine::AppLoader)) = t->m->appFinder;

  setRoot(t, Machine::StringMap, makeStringMap
          (t, stringTable, image->stringCount, heap));

  p->callTableSize = image->callCount;

  setRoot(t, CallTable, makeCallTable
          (t, heap, callTable, image->callCount,
           reinterpret_cast<uintptr_t>(code)));

  setRoot(t, StaticTableArray, makeStaticTableArray
          (t, bootClassTable, image->bootClassCount,
           appClassTable, image->appClassCount, heap));
    
  findThunks(t, image, code);

  if (fixed) {
    resetRuntimeState
      (t, classLoaderMap(t, root(t, Machine::BootLoader)), heap,
       image->heapSize);

    resetRuntimeState
      (t, classLoaderMap(t, root(t, Machine::AppLoader)), heap,
       image->heapSize);

    for (unsigned i = 0; i < arrayLength(t, t->m->types); ++i) {
      resetClassRuntimeState
        (t, type(t, static_cast<Machine::Type>(i)), heap, image->heapSize);
    }
  } else {
    fixupVirtualThunks(t, code);

    fixupMethods
      (t, classLoaderMap(t, root(t, Machine::BootLoader)), image, code);

    fixupMethods
      (t, classLoaderMap(t, root(t, Machine::AppLoader)), image, code);
  }

  fixed = true;

  setRoot(t, Machine::BootstrapClassMap, makeHashMap(t, 0, 0));
}

intptr_t
getThunk(MyThread* t, Thunk thunk)
{
  MyProcessor* p = processor(t);
  
  return reinterpret_cast<intptr_t>
    (p->thunks.table.start + (thunk * p->thunks.table.length));
}

BootImage::Thunk
thunkToThunk(const MyProcessor::Thunk& thunk, uint8_t* base)
{
  return BootImage::Thunk
    (thunk.start - base, thunk.frameSavedOffset, thunk.length);
}

void
compileCall(MyThread* t, Context* c, ThunkIndex index, bool call = true)
{
  Assembler* a = c->assembler;

  if (processor(t)->bootImage) {
    Assembler::Memory table(t->arch->thread(), TargetThreadThunkTable);
    Assembler::Register scratch(t->arch->scratch());
    a->apply(Move, TargetBytesPerWord, MemoryOperand, &table,
             TargetBytesPerWord, RegisterOperand, &scratch);
    Assembler::Memory proc(scratch.low, index * TargetBytesPerWord);
    a->apply(Move, TargetBytesPerWord, MemoryOperand, &proc,
             TargetBytesPerWord, RegisterOperand, &scratch);
    a->apply
      (call ? Call : Jump, TargetBytesPerWord, RegisterOperand, &scratch);
  } else {
    Assembler::Constant proc
      (new (c->zone.allocate(sizeof(ResolvedPromise)))
       ResolvedPromise(reinterpret_cast<intptr_t>(t->thunkTable[index])));

    a->apply
      (call ? LongCall : LongJump, TargetBytesPerWord, ConstantOperand, &proc);
  }
}

void
compileThunks(MyThread* t, FixedAllocator* allocator)
{
  MyProcessor* p = processor(t);

  { Context context(t);
    Assembler* a = context.assembler;
    
    a->saveFrame(TargetThreadStack, TargetThreadIp);

    p->thunks.default_.frameSavedOffset = a->length();

    Assembler::Register thread(t->arch->thread());
    a->pushFrame(1, TargetBytesPerWord, RegisterOperand, &thread);
  
    compileCall(t, &context, compileMethodIndex);

    a->popFrame(t->arch->alignFrameSize(1));

    Assembler::Register result(t->arch->returnLow());
    a->apply(Jump, TargetBytesPerWord, RegisterOperand, &result);

    p->thunks.default_.length = a->endBlock(false)->resolve(0, 0);

    p->thunks.default_.start = finish
      (t, allocator, a, "default", p->thunks.default_.length);
  }

  { Context context(t);
    Assembler* a = context.assembler;
    
    Assembler::Register class_(t->arch->virtualCallTarget());
    Assembler::Memory virtualCallTargetSrc
      (t->arch->stack(),
       (t->arch->frameFooterSize() + t->arch->frameReturnAddressSize())
       * TargetBytesPerWord);

    a->apply(Move, TargetBytesPerWord, MemoryOperand, &virtualCallTargetSrc,
             TargetBytesPerWord, RegisterOperand, &class_);

    Assembler::Memory virtualCallTargetDst
      (t->arch->thread(), TargetThreadVirtualCallTarget);

    a->apply(Move, TargetBytesPerWord, RegisterOperand, &class_,
             TargetBytesPerWord, MemoryOperand, &virtualCallTargetDst);

    Assembler::Register index(t->arch->virtualCallIndex());
    Assembler::Memory virtualCallIndex
      (t->arch->thread(), TargetThreadVirtualCallIndex);

    a->apply(Move, TargetBytesPerWord, RegisterOperand, &index,
             TargetBytesPerWord, MemoryOperand, &virtualCallIndex);
    
    a->saveFrame(TargetThreadStack, TargetThreadIp);

    p->thunks.defaultVirtual.frameSavedOffset = a->length();

    Assembler::Register thread(t->arch->thread());
    a->pushFrame(1, TargetBytesPerWord, RegisterOperand, &thread);

    compileCall(t, &context, compileVirtualMethodIndex);
  
    a->popFrame(t->arch->alignFrameSize(1));

    Assembler::Register result(t->arch->returnLow());
    a->apply(Jump, TargetBytesPerWord, RegisterOperand, &result);

    p->thunks.defaultVirtual.length = a->endBlock(false)->resolve(0, 0);

    p->thunks.defaultVirtual.start = finish
      (t, allocator, a, "defaultVirtual", p->thunks.defaultVirtual.length);
  }

  { Context context(t);
    Assembler* a = context.assembler;

    a->saveFrame(TargetThreadStack, TargetThreadIp);

    p->thunks.native.frameSavedOffset = a->length();

    Assembler::Register thread(t->arch->thread());
    a->pushFrame(1, TargetBytesPerWord, RegisterOperand, &thread);

    compileCall(t, &context, invokeNativeIndex);
  
    a->popFrameAndUpdateStackAndReturn
      (t->arch->alignFrameSize(1), TargetThreadStack);

    p->thunks.native.length = a->endBlock(false)->resolve(0, 0);

    p->thunks.native.start = finish
      (t, allocator, a, "native", p->thunks.native.length);
  }

  { Context context(t);
    Assembler* a = context.assembler;

    a->saveFrame(TargetThreadStack, TargetThreadIp);

    p->thunks.aioob.frameSavedOffset = a->length();

    Assembler::Register thread(t->arch->thread());
    a->pushFrame(1, TargetBytesPerWord, RegisterOperand, &thread);

    compileCall(t, &context, throwArrayIndexOutOfBoundsIndex);

    p->thunks.aioob.length = a->endBlock(false)->resolve(0, 0);

    p->thunks.aioob.start = finish
      (t, allocator, a, "aioob", p->thunks.aioob.length);
  }

  { Context context(t);
    Assembler* a = context.assembler;
      
    a->saveFrame(TargetThreadStack, TargetThreadIp);

    p->thunks.stackOverflow.frameSavedOffset = a->length();

    Assembler::Register thread(t->arch->thread());
    a->pushFrame(1, TargetBytesPerWord, RegisterOperand, &thread);

    compileCall(t, &context, throwStackOverflowIndex);

    p->thunks.stackOverflow.length = a->endBlock(false)->resolve(0, 0);

    p->thunks.stackOverflow.start = finish
      (t, allocator, a, "stackOverflow", p->thunks.stackOverflow.length);
  }

  { { Context context(t);
      Assembler* a = context.assembler;

      a->saveFrame(TargetThreadStack, TargetThreadIp);

      p->thunks.table.frameSavedOffset = a->length();

      compileCall(t, &context, dummyIndex, false);

      p->thunks.table.length = a->endBlock(false)->resolve(0, 0);

      p->thunks.table.start = static_cast<uint8_t*>
        (allocator->allocate
         (p->thunks.table.length * ThunkCount, TargetBytesPerWord));
    }

    uint8_t* start = p->thunks.table.start;

#define THUNK(s) {                                                      \
      Context context(t);                                               \
      Assembler* a = context.assembler;                                 \
                                                                        \
      a->saveFrame(TargetThreadStack, TargetThreadIp);                  \
                                                                        \
      p->thunks.table.frameSavedOffset = a->length();                   \
                                                                        \
      compileCall(t, &context, s##Index, false);                        \
                                                                        \
      expect(t, a->endBlock(false)->resolve(0, 0)                       \
             <= p->thunks.table.length);                                \
                                                                        \
      a->setDestination(start);                                         \
      a->write();                                                       \
                                                                        \
      logCompile(t, start, p->thunks.table.length, 0, #s, 0);           \
                                                                        \
      start += p->thunks.table.length;                                  \
    }
#include "thunks.cpp"
#undef THUNK
  }

  BootImage* image = p->bootImage;

  if (image) {
    uint8_t* imageBase = p->codeAllocator.base;

    image->thunks.default_ = thunkToThunk(p->thunks.default_, imageBase);
    image->thunks.defaultVirtual = thunkToThunk
      (p->thunks.defaultVirtual, imageBase);
    image->thunks.native = thunkToThunk(p->thunks.native, imageBase);
    image->thunks.aioob = thunkToThunk(p->thunks.aioob, imageBase);
    image->thunks.stackOverflow = thunkToThunk
      (p->thunks.stackOverflow, imageBase);
    image->thunks.table = thunkToThunk(p->thunks.table, imageBase);
  }
}

MyProcessor*
processor(MyThread* t)
{
  return static_cast<MyProcessor*>(t->m->processor);
}

uintptr_t
defaultThunk(MyThread* t)
{
  return reinterpret_cast<uintptr_t>(processor(t)->thunks.default_.start);
}

uintptr_t
bootDefaultThunk(MyThread* t)
{
  return reinterpret_cast<uintptr_t>(processor(t)->bootThunks.default_.start);
}

uintptr_t
defaultVirtualThunk(MyThread* t)
{
  return reinterpret_cast<uintptr_t>
    (processor(t)->thunks.defaultVirtual.start);
}

uintptr_t
nativeThunk(MyThread* t)
{
  return reinterpret_cast<uintptr_t>(processor(t)->thunks.native.start);
}

uintptr_t
bootNativeThunk(MyThread* t)
{
  return reinterpret_cast<uintptr_t>(processor(t)->bootThunks.native.start);
}

uintptr_t
aioobThunk(MyThread* t)
{
  return reinterpret_cast<uintptr_t>(processor(t)->thunks.aioob.start);
}

uintptr_t
stackOverflowThunk(MyThread* t)
{
  return reinterpret_cast<uintptr_t>(processor(t)->thunks.stackOverflow.start);
}

bool
unresolved(MyThread* t, uintptr_t methodAddress)
{
  return methodAddress == defaultThunk(t)
    or methodAddress == bootDefaultThunk(t);
}

uintptr_t
compileVirtualThunk(MyThread* t, unsigned index, unsigned* size)
{
  Context context(t);
  Assembler* a = context.assembler;

  ResolvedPromise indexPromise(index);
  Assembler::Constant indexConstant(&indexPromise);
  Assembler::Register indexRegister(t->arch->virtualCallIndex());
  a->apply(Move, TargetBytesPerWord, ConstantOperand, &indexConstant,
           TargetBytesPerWord, RegisterOperand, &indexRegister);
  
  ResolvedPromise defaultVirtualThunkPromise(defaultVirtualThunk(t));
  Assembler::Constant thunk(&defaultVirtualThunkPromise);
  a->apply(Jump, TargetBytesPerWord, ConstantOperand, &thunk);

  *size = a->endBlock(false)->resolve(0, 0);

  uint8_t* start = static_cast<uint8_t*>
    (codeAllocator(t)->allocate(*size, TargetBytesPerWord));

  a->setDestination(start);
  a->write();

  logCompile(t, start, *size, 0, "virtualThunk", 0);

  return reinterpret_cast<uintptr_t>(start);
}

uintptr_t
virtualThunk(MyThread* t, unsigned index)
{
  ACQUIRE(t, t->m->classLock);

  if (root(t, VirtualThunks) == 0
      or wordArrayLength(t, root(t, VirtualThunks)) <= index * 2)
  {
    object newArray = makeWordArray(t, nextPowerOfTwo((index + 1) * 2));
    if (root(t, VirtualThunks)) {
      memcpy(&wordArrayBody(t, newArray, 0),
             &wordArrayBody(t, root(t, VirtualThunks), 0),
             wordArrayLength(t, root(t, VirtualThunks)) * BytesPerWord);
    }
    setRoot(t, VirtualThunks, newArray);
  }

  if (wordArrayBody(t, root(t, VirtualThunks), index * 2) == 0) {
    unsigned size;
    uintptr_t thunk = compileVirtualThunk(t, index, &size);
    wordArrayBody(t, root(t, VirtualThunks), index * 2) = thunk;
    wordArrayBody(t, root(t, VirtualThunks), (index * 2) + 1) = size;
  }

  return wordArrayBody(t, root(t, VirtualThunks), index * 2);
}

void
compile(MyThread* t, FixedAllocator* allocator, BootContext* bootContext,
        object method)
{
  PROTECT(t, method);

  if (bootContext == 0) {
    initClass(t, methodClass(t, method));
  }

  if (methodAddress(t, method) != defaultThunk(t)) {
    return;
  }

  assert(t, (methodFlags(t, method) & ACC_NATIVE) == 0);

  // We must avoid acquiring any locks until after the first pass of
  // compilation, since this pass may trigger classloading operations
  // involving application classloaders and thus the potential for
  // deadlock.  To make this safe, we use a private clone of the
  // method so that we won't be confused if another thread updates the
  // original while we're working.

  object clone = methodClone(t, method);

  loadMemoryBarrier();

  if (methodAddress(t, method) != defaultThunk(t)) {
    return;
  }

  PROTECT(t, clone);

  Context context(t, bootContext, clone);
  compile(t, &context);

  { object ehTable = codeExceptionHandlerTable(t, methodCode(t, clone));

    if (ehTable) {
      PROTECT(t, ehTable);

      // resolve all exception handler catch types before we acquire
      // the class lock:
      for (unsigned i = 0; i < exceptionHandlerTableLength(t, ehTable); ++i) {
        uint64_t handler = exceptionHandlerTableBody(t, ehTable, i);
        if (exceptionHandlerCatchType(handler)) {
          resolveClassInPool
            (t, clone, exceptionHandlerCatchType(handler) - 1);
        }
      }
    }
  }

  ACQUIRE(t, t->m->classLock);

  if (methodAddress(t, method) != defaultThunk(t)) {
    return;
  }

  finish(t, allocator, &context);
 
  if (DebugMethodTree) {
    fprintf(stderr, "insert method at %p\n",
            reinterpret_cast<void*>(methodCompiled(t, clone)));
  }

  // We can't update the MethodCode field on the original method
  // before it is placed into the method tree, since another thread
  // might call the method, from which stack unwinding would fail
  // (since there is not yet an entry in the method tree).  However,
  // we can't insert the original method into the tree before updating
  // the MethodCode field on it since we rely on that field to
  // determine its position in the tree.  Therefore, we insert the
  // clone in its place.  Later, we'll replace the clone with the
  // original to save memory.

  setRoot
    (t, MethodTree, treeInsert
     (t, &(context.zone), root(t, MethodTree),
      methodCompiled(t, clone), clone, root(t, MethodTreeSentinal),
      compareIpToMethodBounds));

  storeStoreMemoryBarrier();

  set(t, method, MethodCode, methodCode(t, clone));

  if (methodVirtual(t, method)) {
    classVtable(t, methodClass(t, method), methodOffset(t, method))
      = reinterpret_cast<void*>(methodCompiled(t, clone));
  }

  // we've compiled the method and inserted it into the tree without
  // error, so we ensure that the executable area not be deallocated
  // when we dispose of the context:
  context.executableAllocator = 0;

  treeUpdate(t, root(t, MethodTree), methodCompiled(t, clone),
             method, root(t, MethodTreeSentinal), compareIpToMethodBounds);
}

object&
root(Thread* t, Root root)
{
  return arrayBody(t, processor(static_cast<MyThread*>(t))->roots, root);
}

void
setRoot(Thread* t, Root root, object value)
{
  set(t, processor(static_cast<MyThread*>(t))->roots,
      ArrayBody + (root * BytesPerWord), value);
}

FixedAllocator*
codeAllocator(MyThread* t)
{
  return &(processor(t)->codeAllocator);
}

} // namespace local

} // namespace

namespace vm {

Processor*
makeProcessor(System* system, Allocator* allocator, bool useNativeFeatures)
{
  return new (allocator->allocate(sizeof(local::MyProcessor)))
    local::MyProcessor(system, allocator, useNativeFeatures);
}

} // namespace vm
