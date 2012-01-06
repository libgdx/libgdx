/* Copyright (c) 2008-2011, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

#include "sys/stat.h"
#include "windows.h"
#include "sys/timeb.h"

#ifdef _MSC_VER
#  define S_ISREG(x) ((x) & _S_IFREG)
#  define S_ISDIR(x) ((x) & _S_IFDIR)
#  define FTIME _ftime_s
#else
#  define FTIME _ftime
#endif

#undef max
#undef min

#include "arch.h"
#include "system.h"

#define ACQUIRE(s, x) MutexResource MAKE_NAME(mutexResource_) (s, x)

using namespace vm;

namespace {

class MutexResource {
 public:
  MutexResource(System* s, HANDLE m): s(s), m(m) {
    int r UNUSED = WaitForSingleObject(m, INFINITE);
    assert(s, r == WAIT_OBJECT_0);
  }

  ~MutexResource() {
    bool success UNUSED = ReleaseMutex(m);
    assert(s, success);
  }

 private:
  System* s; 
  HANDLE m;
};

const unsigned SegFaultIndex = 0;
const unsigned DivideByZeroIndex = 1;

const unsigned HandlerCount = 2;

class MySystem;
MySystem* system;

LONG CALLBACK
handleException(LPEXCEPTION_POINTERS e);

DWORD WINAPI
run(void* r)
{
  static_cast<System::Runnable*>(r)->run();
  return 0;
}

const bool Verbose = false;

const unsigned Waiting = 1 << 0;
const unsigned Notified = 1 << 1;

class MySystem: public System {
 public:
  class Thread: public System::Thread {
   public:
    Thread(System* s, System::Runnable* r):
      s(s),
      r(r),
      next(0),
      flags(0)
    {
      mutex = CreateMutex(0, false, 0);
      assert(s, mutex);

      event = CreateEvent(0, true, false, 0);
      assert(s, event);
    }

    virtual void interrupt() {
      ACQUIRE(s, mutex);

      r->setInterrupted(true);

      if (flags & Waiting) {
        int r UNUSED = SetEvent(event);
        assert(s, r != 0);
      }
    }

    virtual bool getAndClearInterrupted() {
      ACQUIRE(s, mutex);

      bool interrupted = r->interrupted();

      r->setInterrupted(false);

      return interrupted;
    }

    virtual void join() {
      int r UNUSED = WaitForSingleObject(thread, INFINITE);
      assert(s, r == WAIT_OBJECT_0);
    }

    virtual void dispose() {
      CloseHandle(event);
      CloseHandle(mutex);
      CloseHandle(thread);
      s->free(this);
    }

    HANDLE thread;
    HANDLE mutex;
    HANDLE event;
    System* s;
    System::Runnable* r;
    Thread* next;
    unsigned flags;
  };

  class Mutex: public System::Mutex {
   public:
    Mutex(System* s): s(s) {
      mutex = CreateMutex(0, false, 0);
      assert(s, mutex);
    }

    virtual void acquire() {
      int r UNUSED = WaitForSingleObject(mutex, INFINITE);
      assert(s, r == WAIT_OBJECT_0);
    }

    virtual void release() {
      bool success UNUSED = ReleaseMutex(mutex);
      assert(s, success);
    }

    virtual void dispose() {
      CloseHandle(mutex);
      s->free(this);
    }

    System* s;
    HANDLE mutex;
  };

  class Monitor: public System::Monitor {
   public:
    Monitor(System* s): s(s), owner_(0), first(0), last(0), depth(0) {
      mutex = CreateMutex(0, false, 0);
      assert(s, mutex);
    }

    virtual bool tryAcquire(System::Thread* context) {
      Thread* t = static_cast<Thread*>(context);
      assert(s, t);

      if (owner_ == t) {
        ++ depth;
        return true;
      } else {
        switch (WaitForSingleObject(mutex, 0)) {
        case WAIT_TIMEOUT:
          return false;

        case WAIT_OBJECT_0:
          owner_ = t;
          ++ depth;
          return true;

        default:
          sysAbort(s);
        }
      }
    }

    virtual void acquire(System::Thread* context) {
      Thread* t = static_cast<Thread*>(context);
      assert(s, t);

      if (owner_ != t) {
        int r UNUSED = WaitForSingleObject(mutex, INFINITE);
        assert(s, r == WAIT_OBJECT_0);
        owner_ = t;
      }
      ++ depth;
    }

    virtual void release(System::Thread* context) {
      Thread* t = static_cast<Thread*>(context);
      assert(s, t);

      if (owner_ == t) {
        if (-- depth == 0) {
          owner_ = 0;
          bool success UNUSED = ReleaseMutex(mutex);
          assert(s, success);
        }
      } else {
        sysAbort(s);
      }
    }

    void append(Thread* t) {
      if (last) {
        last->next = t;
        last = t;
      } else {
        first = last = t;
      }
    }

    void remove(Thread* t) {
      Thread* previous = 0;
      for (Thread* current = first; current;) {
        if (t == current) {
          if (current == first) {
            first = t->next;
          } else {
            previous->next = t->next;
          }

          if (current == last) {
            last = previous;
          }

          t->next = 0;

          break;
        } else {
          previous = current;
          current = current->next;
        }
      }
    }

    virtual void wait(System::Thread* context, int64_t time) {
      wait(context, time, false);
    }

    virtual bool waitAndClearInterrupted(System::Thread* context, int64_t time)
    {
      return wait(context, time, true);
    }

    bool wait(System::Thread* context, int64_t time, bool clearInterrupted) {
      Thread* t = static_cast<Thread*>(context);
      assert(s, t);

      if (owner_ == t) {
        // Initialized here to make gcc 4.2 a happy compiler
        bool interrupted = false;
        bool notified = false;
        unsigned depth = 0;

        int r UNUSED;

        { ACQUIRE(s, t->mutex);

          interrupted = t->r->interrupted();
          if (interrupted and clearInterrupted) {
            t->r->setInterrupted(false);
          }

          t->flags |= Waiting;

          append(t);

          depth = this->depth;
          this->depth = 0;
          owner_ = 0;

          bool success UNUSED = ReleaseMutex(mutex);
          assert(s, success);

          if (not interrupted) {
            success = ResetEvent(t->event);
            assert(s, success);

            success = ReleaseMutex(t->mutex);
            assert(s, success);

            r = WaitForSingleObject(t->event, (time ? time : INFINITE));
            assert(s, r == WAIT_OBJECT_0 or r == WAIT_TIMEOUT);

            r = WaitForSingleObject(t->mutex, INFINITE);
            assert(s, r == WAIT_OBJECT_0);

            interrupted = t->r->interrupted();
            if (interrupted and clearInterrupted) {
              t->r->setInterrupted(false);
            }
          }

          notified = ((t->flags & Notified) != 0);
        
          t->flags = 0;
        }

        r = WaitForSingleObject(mutex, INFINITE);
        assert(s, r == WAIT_OBJECT_0);

        if (not notified) {
          remove(t);
        }

        t->next = 0;

        owner_ = t;
        this->depth = depth;

        return interrupted;
      } else {
        sysAbort(s);
      }
    }

    void doNotify(Thread* t) {
      ACQUIRE(s, t->mutex);

      t->flags |= Notified;

      bool success UNUSED = SetEvent(t->event);
      assert(s, success);
    }

    virtual void notify(System::Thread* context) {
      Thread* t = static_cast<Thread*>(context);
      assert(s, t);

      if (owner_ == t) {
        if (first) {
          Thread* t = first;
          first = first->next;
          if (t == last) {
            last = 0;
          }

          doNotify(t);
        }
      } else {
        sysAbort(s);
      }
    }

    virtual void notifyAll(System::Thread* context) {
      Thread* t = static_cast<Thread*>(context);
      assert(s, t);

      if (owner_ == t) {
        for (Thread* t = first; t; t = t->next) {
          doNotify(t);
        }
        first = last = 0;
      } else {
        sysAbort(s);
      }
    }
    
    virtual System::Thread* owner() {
      return owner_;
    }

    virtual void dispose() {
      assert(s, owner_ == 0);
      CloseHandle(mutex);
      s->free(this);
    }

    System* s;
    HANDLE mutex;
    Thread* owner_;
    Thread* first;
    Thread* last;
    unsigned depth;
  };

  class Local: public System::Local {
   public:
    Local(System* s): s(s) {
      key = TlsAlloc();
      assert(s, key != TLS_OUT_OF_INDEXES);
    }

    virtual void* get() {
      return TlsGetValue(key);
    }

    virtual void set(void* p) {
      bool r UNUSED = TlsSetValue(key, p);
      assert(s, r);
    }

    virtual void dispose() {
      bool r UNUSED = TlsFree(key);
      assert(s, r);

      s->free(this);
    }

    System* s;
    unsigned key;
  };

  class Region: public System::Region {
   public:
    Region(System* system, uint8_t* start, size_t length, HANDLE mapping,
           HANDLE file):
      system(system),
      start_(start),
      length_(length),
      mapping(mapping),
      file(file)
    { }

    virtual const uint8_t* start() {
      return start_;
    }

    virtual size_t length() {
      return length_;
    }

    virtual void dispose() {
      if (start_) {
        if (start_) UnmapViewOfFile(start_);
        if (mapping) CloseHandle(mapping);
        if (file) CloseHandle(file);
      }
      system->free(this);
    }

    System* system;
    uint8_t* start_;
    size_t length_;
    HANDLE mapping;
    HANDLE file;
  };

  class Directory: public System::Directory {
   public:
    Directory(System* s): s(s), handle(0), findNext(false) { }

    virtual const char* next() {
      if (handle and handle != INVALID_HANDLE_VALUE) {
        if (findNext) {
          if (FindNextFile(handle, &data)) {
            return data.cFileName;
          }
        } else {
          findNext = true;
          return data.cFileName;
        }
      }
      return 0;
    }

    virtual void dispose() {
      if (handle and handle != INVALID_HANDLE_VALUE) {
        FindClose(handle);
      }
      s->free(this);
    }

    System* s;
    HANDLE handle;
    WIN32_FIND_DATA data;
    bool findNext;
  };

  class Library: public System::Library {
   public:
    Library(System* s, HMODULE handle, const char* name):
      s(s),
      handle(handle),
      name_(name),
      next_(0)
    { }

    virtual void* resolve(const char* function) {
      void* address;
      FARPROC p = GetProcAddress(handle, function);
      memcpy(&address, &p, BytesPerWord);
      return address;
    }

    virtual const char* name() {
      return name_;
    }

    virtual System::Library* next() {
      return next_;
    }

    virtual void setNext(System::Library* lib) {
      next_ = lib;
    }

    virtual void disposeAll() {
      if (Verbose) {
        fprintf(stderr, "close %p\n", handle); fflush(stderr);
      }

      if (name_) {
        FreeLibrary(handle);
      }

      if (next_) {
        next_->disposeAll();
      }

      if (name_) {
        s->free(name_);
      }

      s->free(this);
    }

    System* s;
    HMODULE handle;
    const char* name_;
    System::Library* next_;
  };

  MySystem(const char* crashDumpDirectory):
    oldHandler(0),
    crashDumpDirectory(crashDumpDirectory)
  {
    expect(this, system == 0);
    system = this;

    memset(handlers, 0, sizeof(handlers));

    mutex = CreateMutex(0, false, 0);
    assert(this, mutex);
  }

  bool findHandler() {
    for (unsigned i = 0; i < HandlerCount; ++i) {
      if (handlers[i]) return true;
    }
    return false;
  }

  int registerHandler(System::SignalHandler* handler, int index) {
    if (handler) {
      handlers[index] = handler;
      
      if (oldHandler == 0) {
#ifdef ARCH_x86_32
        oldHandler = SetUnhandledExceptionFilter(handleException);
#elif defined ARCH_x86_64
        AddVectoredExceptionHandler(1, handleException);
        oldHandler = reinterpret_cast<LPTOP_LEVEL_EXCEPTION_FILTER>(1);
#endif
      }

      return 0;
    } else if (handlers[index]) {
      handlers[index] = 0;

      if (not findHandler()) {
#ifdef ARCH_x86_32
        SetUnhandledExceptionFilter(oldHandler);
        oldHandler = 0;
#elif defined ARCH_x86_64
        // do nothing, handlers are never "unregistered" anyway
#endif        
      }

      return 0;
    } else {
      return 1;
    }
  }

  virtual void* tryAllocate(unsigned sizeInBytes) {
    return malloc(sizeInBytes);
  }

  virtual void free(const void* p) {
    if (p) ::free(const_cast<void*>(p));
  }

  virtual void* tryAllocateExecutable(unsigned sizeInBytes) {
    return VirtualAlloc
      (0, sizeInBytes, MEM_COMMIT | MEM_RESERVE, PAGE_EXECUTE_READWRITE);
  }

  virtual void freeExecutable(const void* p, unsigned) {
    int r UNUSED = VirtualFree(const_cast<void*>(p), 0, MEM_RELEASE);
    assert(this, r);
  }

  virtual bool success(Status s) {
    return s == 0;
  }

  virtual Status attach(Runnable* r) {
    Thread* t = new (allocate(this, sizeof(Thread))) Thread(this, r);
    bool success UNUSED = DuplicateHandle
      (GetCurrentProcess(), GetCurrentThread(), GetCurrentProcess(),
       &(t->thread), 0, false, DUPLICATE_SAME_ACCESS);
    assert(this, success);
    r->attach(t);
    return 0;
  }

  virtual Status start(Runnable* r) {
    Thread* t = new (allocate(this, sizeof(Thread))) Thread(this, r);
    r->attach(t);
    DWORD id;
    t->thread = CreateThread(0, 0, run, r, 0, &id);
    assert(this, t->thread);
    return 0;
  }

  virtual Status make(System::Mutex** m) {
    *m = new (allocate(this, sizeof(Mutex))) Mutex(this);
    return 0;
  }

  virtual Status make(System::Monitor** m) {
    *m = new (allocate(this, sizeof(Monitor))) Monitor(this);
    return 0;
  }

  virtual Status make(System::Local** l) {
    *l = new (allocate(this, sizeof(Local))) Local(this);
    return 0;
  }

  virtual Status handleSegFault(SignalHandler* handler) {
    return registerHandler(handler, SegFaultIndex);
  }

  virtual Status handleDivideByZero(SignalHandler* handler) {
    return registerHandler(handler, DivideByZeroIndex);
  }

  virtual Status visit(System::Thread* st UNUSED, System::Thread* sTarget,
                       ThreadVisitor* visitor)
  {
    assert(this, st != sTarget);

    Thread* target = static_cast<Thread*>(sTarget);

    ACQUIRE(this, mutex);

    bool success = false;
    int rv = SuspendThread(target->thread);
    if (rv != -1) {
      CONTEXT context;
      memset(&context, 0, sizeof(CONTEXT));
      context.ContextFlags = CONTEXT_CONTROL;
      rv = GetThreadContext(target->thread, &context);

      if (rv) {
#ifdef ARCH_x86_32
        visitor->visit(reinterpret_cast<void*>(context.Eip),
                       reinterpret_cast<void*>(context.Ebp),
                       reinterpret_cast<void*>(context.Esp));
#elif defined ARCH_x86_64
        visitor->visit(reinterpret_cast<void*>(context.Rip),
                       reinterpret_cast<void*>(context.Rbp),
                       reinterpret_cast<void*>(context.Rsp));
#endif
        success = true;
      }

      rv = ResumeThread(target->thread);
      expect(this, rv != -1);
    }

    return (success ? 0 : 1);
  }

  virtual uint64_t call(void* function, uintptr_t* arguments, uint8_t* types,
                        unsigned count, unsigned size, unsigned returnType)
  {
    return dynamicCall(function, arguments, types, count, size, returnType);
  }

  virtual Status map(System::Region** region, const char* name) {
    Status status = 1;
    
    HANDLE file = CreateFile(name, FILE_READ_DATA, FILE_SHARE_READ, 0,
                             OPEN_EXISTING, 0, 0);
    if (file != INVALID_HANDLE_VALUE) {
      unsigned size = GetFileSize(file, 0);
      if (size != INVALID_FILE_SIZE) {
        HANDLE mapping = CreateFileMapping(file, 0, PAGE_READONLY, 0, size, 0);
        if (mapping) {
          void* data = MapViewOfFile(mapping, FILE_MAP_READ, 0, 0, 0);
          if (data) {
            *region = new (allocate(this, sizeof(Region)))
              Region(this, static_cast<uint8_t*>(data), size, file, mapping);
            status = 0;        
          }

          if (status) {
            CloseHandle(mapping);
          }
        }
      }

      if (status) {
        CloseHandle(file);
      }
    }
    
    return status;
  }

  virtual Status open(System::Directory** directory, const char* name) {
    Status status = 1;

    unsigned length = strlen(name);
    RUNTIME_ARRAY(char, buffer, length + 3);
    memcpy(RUNTIME_ARRAY_BODY(buffer), name, length);
    memcpy(RUNTIME_ARRAY_BODY(buffer) + length, "\\*", 3);

    Directory* d = new (allocate(this, sizeof(Directory))) Directory(this);
    d->handle = FindFirstFile(RUNTIME_ARRAY_BODY(buffer), &(d->data));
    if (d->handle == INVALID_HANDLE_VALUE) {
      d->dispose();
    } else {
      *directory = d;
      status = 0;
    }
    
    return status;
  }

  virtual FileType stat(const char* name, unsigned* length) {
    struct _stat s;
    int r = _stat(name, &s);
    if (r == 0) {
      if (S_ISREG(s.st_mode)) {
        *length = s.st_size;
        return TypeFile;
      } else if (S_ISDIR(s.st_mode)) {
        *length = 0;
        return TypeDirectory;
      } else {
        *length = 0;
        return TypeUnknown;
      }
    } else {
      *length = 0;
      return TypeDoesNotExist;
    }
  }

  virtual const char* libraryPrefix() {
    return SO_PREFIX;
  }

  virtual const char* librarySuffix() {
    return SO_SUFFIX;
  }

  virtual const char* toAbsolutePath(Allocator* allocator, const char* name) {
    if (strncmp(name, "//", 2) == 0
        or strncmp(name, "\\\\", 2) == 0
        or strncmp(name + 1, ":/", 2) == 0
        or strncmp(name + 1, ":\\", 2) == 0)
    {
      return copy(allocator, name);
    } else {
      TCHAR buffer[MAX_PATH];
      GetCurrentDirectory(MAX_PATH, buffer);
      return append(allocator, buffer, "\\", name);
    }
  }

  virtual Status load(System::Library** lib,
                      const char* name)
  {
    HMODULE handle;
    unsigned nameLength = (name ? strlen(name) : 0);
    if (name) {
      handle = LoadLibrary(name);
    } else {
      handle = GetModuleHandle(0);
    }
 
    if (handle) {
      if (Verbose) {
        fprintf(stderr, "open %s as %p\n", name, handle); fflush(stderr);
      }

      char* n;
      if (name) {
        n = static_cast<char*>(allocate(this, nameLength + 1));
        memcpy(n, name, nameLength + 1);
      } else {
        n = 0;
      }

      *lib = new (allocate(this, sizeof(Library))) Library(this, handle, n);

      return 0;
    } else {
      if (Verbose) {
        fprintf(stderr, "unable to open %s: %ld\n", name, GetLastError());
        fflush(stderr);
      }

      return 1;
    }
  }

  virtual char pathSeparator() {
    return ';';
  }

  virtual char fileSeparator() {
    return '\\';
  }

  virtual int64_t now() {
    // We used to use _ftime here, but that only gives us 1-second
    // resolution on Windows 7.  _ftime_s might work better, but MinGW
    // doesn't have it as of this writing.  So we use this mess instead:
    FILETIME time;
    GetSystemTimeAsFileTime(&time);
    return (((static_cast<int64_t>(time.dwHighDateTime) << 32)
             | time.dwLowDateTime) / 10000) - 11644473600000LL;
  }

  virtual void yield() {
    SwitchToThread();
  }

  virtual void exit(int code) {
    ::exit(code);
  }

  virtual void abort() {
    // trigger an EXCEPTION_ACCESS_VIOLATION, which we will catch and
    // generate a debug dump for
    *static_cast<int*>(0) = 0;
  }

  virtual void dispose() {
    system = 0;
    CloseHandle(mutex);
    ::free(this);
  }

  HANDLE mutex;
  SignalHandler* handlers[HandlerCount];
  LPTOP_LEVEL_EXCEPTION_FILTER oldHandler;
  const char* crashDumpDirectory;
};

#pragma pack(push,4)
struct MINIDUMP_EXCEPTION_INFORMATION {
  DWORD thread;
  LPEXCEPTION_POINTERS exception;
  BOOL exceptionInCurrentAddressSpace;
};
#pragma pack(pop)

struct MINIDUMP_USER_STREAM_INFORMATION;
struct MINIDUMP_CALLBACK_INFORMATION;

enum MINIDUMP_TYPE {
  MiniDumpNormal = 0,
  MiniDumpWithFullMemory = 2
};

typedef BOOL (*MiniDumpWriteDumpType)
(HANDLE processHandle,
 DWORD processId,
 HANDLE file,
 MINIDUMP_TYPE type,
 const MINIDUMP_EXCEPTION_INFORMATION* exception,
 const MINIDUMP_USER_STREAM_INFORMATION* userStream,
 const MINIDUMP_CALLBACK_INFORMATION* callback);

void
dump(LPEXCEPTION_POINTERS e, const char* directory)
{
  HINSTANCE dbghelp = LoadLibrary("dbghelp.dll");

  if (dbghelp) {
    MiniDumpWriteDumpType MiniDumpWriteDump = reinterpret_cast
      <MiniDumpWriteDumpType>(GetProcAddress(dbghelp, "MiniDumpWriteDump"));

    if (MiniDumpWriteDump) {
      char name[MAX_PATH];
      _timeb tb;
      FTIME(&tb);
      vm::snprintf(name, MAX_PATH, "%s\\crash-%"LLD".mdmp", directory,
                   (static_cast<int64_t>(tb.time) * 1000)
                   + static_cast<int64_t>(tb.millitm));

      HANDLE file = CreateFile
        (name, FILE_WRITE_DATA, 0, 0, CREATE_ALWAYS, 0, 0);

      if (file != INVALID_HANDLE_VALUE) {
        MINIDUMP_EXCEPTION_INFORMATION exception
           = { GetCurrentThreadId(), e, true };

         MiniDumpWriteDump
           (GetCurrentProcess(),
            GetCurrentProcessId(),
            file,
            MiniDumpWithFullMemory,
            &exception,
            0,
            0);

        CloseHandle(file);
      }
    }

    FreeLibrary(dbghelp);
  }
}

LONG CALLBACK
handleException(LPEXCEPTION_POINTERS e)
{
  System::SignalHandler* handler = 0;
  if (e->ExceptionRecord->ExceptionCode == EXCEPTION_ACCESS_VIOLATION) {
    handler = system->handlers[SegFaultIndex];
  } else if (e->ExceptionRecord->ExceptionCode == EXCEPTION_INT_DIVIDE_BY_ZERO)
  {
    handler = system->handlers[DivideByZeroIndex];
  }

  if (handler) {
#ifdef ARCH_x86_32
    void* ip = reinterpret_cast<void*>(e->ContextRecord->Eip);
    void* base = reinterpret_cast<void*>(e->ContextRecord->Ebp);
    void* stack = reinterpret_cast<void*>(e->ContextRecord->Esp);
    void* thread = reinterpret_cast<void*>(e->ContextRecord->Ebx);
#elif defined ARCH_x86_64
    void* ip = reinterpret_cast<void*>(e->ContextRecord->Rip);
    void* base = reinterpret_cast<void*>(e->ContextRecord->Rbp);
    void* stack = reinterpret_cast<void*>(e->ContextRecord->Rsp);
    void* thread = reinterpret_cast<void*>(e->ContextRecord->Rbx);
#endif

    bool jump = handler->handleSignal(&ip, &base, &stack, &thread);

#ifdef  ARCH_x86_32
    e->ContextRecord->Eip = reinterpret_cast<DWORD>(ip);
    e->ContextRecord->Ebp = reinterpret_cast<DWORD>(base);
    e->ContextRecord->Esp = reinterpret_cast<DWORD>(stack);
    e->ContextRecord->Ebx = reinterpret_cast<DWORD>(thread);
#elif defined ARCH_x86_64
    e->ContextRecord->Rip = reinterpret_cast<DWORD64>(ip);
    e->ContextRecord->Rbp = reinterpret_cast<DWORD64>(base);
    e->ContextRecord->Rsp = reinterpret_cast<DWORD64>(stack);
    e->ContextRecord->Rbx = reinterpret_cast<DWORD64>(thread);
#endif

    if (jump) {
      return EXCEPTION_CONTINUE_EXECUTION;
    } else if (system->crashDumpDirectory) {
      dump(e, system->crashDumpDirectory);
    }
  }

  return EXCEPTION_CONTINUE_SEARCH;
}

} // namespace

namespace vm {

JNIEXPORT System*
makeSystem(const char* crashDumpDirectory)
{
  return new (malloc(sizeof(MySystem))) MySystem(crashDumpDirectory);
}

} // namespace vm
