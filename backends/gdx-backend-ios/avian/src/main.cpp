/* Copyright (c) 2008-2011, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

#include "stdlib.h"
#include "stdio.h"
#include "string.h"
#include "jni.h"

#include "system.h"
#include "finder.h"

#if (defined __MINGW32__) || (defined _MSC_VER)
#  define PATH_SEPARATOR ';'
#else
#  define PATH_SEPARATOR ':'
#endif

#ifdef _MSC_VER

#  define not !
#  define or ||
#  define and &&
#  define xor ^

template <class T>
class RuntimeArray {
 public:
  RuntimeArray(unsigned size):
    body(static_cast<T*>(malloc(size * sizeof(T))))
  { }

  ~RuntimeArray() {
    free(body);
  }

  T* body;
};

#  define RUNTIME_ARRAY(type, name, size) RuntimeArray<type> name(size);
#  define RUNTIME_ARRAY_BODY(name) name.body

#else // not _MSC_VER

#  define RUNTIME_ARRAY(type, name, size) type name[size];
#  define RUNTIME_ARRAY_BODY(name) name

#endif // not _MSC_VER

#ifdef BOOT_LIBRARY

// since we aren't linking against libstdc++, we must implement this
// ourselves:
extern "C" void __cxa_pure_virtual(void) { abort(); }

// we link against a System implmentation, which requires this at link
// time, but it should not be used at runtime:
extern "C" uint64_t
vmNativeCall(void*, void*, unsigned, unsigned)
{
  abort();
  // abort is not declared __declspec(noreturn) on MSVC, so we have to
  // pretend it might return to make the compiler happy:
  return 0;
}

#endif // BOOT_LIBRARY

namespace {

const char*
mainClass(const char* jar)
{
  using namespace vm;

  System* system = makeSystem(0);

  class MyAllocator: public Allocator {
   public:
    MyAllocator(System* s): s(s) { }

    virtual void* tryAllocate(unsigned size) {
      return s->tryAllocate(size);
    }

    virtual void* allocate(unsigned size) {
      void* p = tryAllocate(size);
      if (p == 0) {
        abort(s);
      }
      return p;
    }

    virtual void free(const void* p, unsigned) {
      s->free(p);
    }

    System* s;
  } allocator(system);

  Finder* finder = makeFinder(system, &allocator, jar, 0);

  char* result = 0;

  System::Region* region = finder->find("META-INF/MANIFEST.MF");
  if (region) {
    unsigned start = 0;
    unsigned length;
    while (readLine(region->start(), region->length(), &start, &length)) {
      const unsigned PrefixLength = 12;
      if (strncmp("Main-Class: ", reinterpret_cast<const char*>
                  (region->start() + start), PrefixLength) == 0)
      {
        result = static_cast<char*>(malloc(length + 1 - PrefixLength));
        memcpy(result, region->start() + start + PrefixLength,
               length - PrefixLength);
        result[length - PrefixLength] = 0;
        break;
      }
      start += length;
    }

    region->dispose();
  }

  finder->dispose();

  system->dispose();

  return result;
}

void
usageAndExit(const char* name)
{
  fprintf
    (stderr, "usage: %s\n"
     "\t[{-cp|-classpath} <classpath>]\n"
     "\t[-Xmx<maximum heap size>]\n"
     "\t[-Xbootclasspath/p:<classpath to prepend to bootstrap classpath>]\n"
     "\t[-Xbootclasspath:<bootstrap classpath>]\n"
     "\t[-Xbootclasspath/a:<classpath to append to bootstrap classpath>]\n"
     "\t[-D<property name>=<property value> ...]\n"
     "\t{<class name>|-jar <app jar>} [<argument> ...]\n", name);
  exit(-1);
}

} // namespace

int
main(int ac, const char** av)
{
  JavaVMInitArgs vmArgs;
  vmArgs.version = JNI_VERSION_1_2;
  vmArgs.nOptions = 1;
  vmArgs.ignoreUnrecognized = JNI_TRUE;

  const char* class_ = 0;
  const char* jar = 0;
  int argc = 0;
  const char** argv = 0;
  const char* classpath = ".";

  for (int i = 1; i < ac; ++i) {
    if (strcmp(av[i], "-cp") == 0
        or strcmp(av[i], "-classpath") == 0)
    {
      classpath = av[++i];
    } else if (strcmp(av[i], "-jar") == 0)
    {
      jar = av[++i];
    } else if (strncmp(av[i], "-X", 2) == 0
               or strncmp(av[i], "-D", 2) == 0)
    {
      ++ vmArgs.nOptions;
    } else if (strcmp(av[i], "-client") == 0
               or strcmp(av[i], "-server") == 0)
    {
      // ignore
    } else {
      if (jar == 0) {
        class_ = av[i++];
      }
      if (i < ac) {
        argc = ac - i;
        argv = av + i;
        i = ac;
      }
    }
  }

  if (jar) {
    classpath = jar;
    
    class_ = mainClass(jar);

    if (class_ == 0) {
      fprintf(stderr, "Main-Class manifest header not found in %s\n", jar);
      exit(-1);
    }
  }

#ifdef BOOT_LIBRARY
  ++ vmArgs.nOptions;
#endif

#ifdef BOOT_IMAGE
  vmArgs.nOptions += 2;
#endif

#ifdef BOOT_BUILTINS
  ++ vmArgs.nOptions;
#endif

  RUNTIME_ARRAY(JavaVMOption, options, vmArgs.nOptions);
  vmArgs.options = RUNTIME_ARRAY_BODY(options);

  unsigned optionIndex = 0;

#ifdef BOOT_IMAGE
  vmArgs.options[optionIndex++].optionString
    = const_cast<char*>("-Davian.bootimage=bootimageBin");

  vmArgs.options[optionIndex++].optionString
    = const_cast<char*>("-Davian.codeimage=codeimageBin");
#endif

#ifdef BOOT_LIBRARY
  vmArgs.options[optionIndex++].optionString
    = const_cast<char*>("-Davian.bootstrap=" BOOT_LIBRARY);
#endif

#ifdef BOOT_BUILTINS
  vmArgs.options[optionIndex++].optionString
    = const_cast<char*>("-Davian.builtins=" BOOT_BUILTINS);
#endif

#define CLASSPATH_PROPERTY "-Djava.class.path="

  unsigned classpathSize = strlen(classpath);
  unsigned classpathPropertyBufferSize
    = sizeof(CLASSPATH_PROPERTY) + classpathSize;

  RUNTIME_ARRAY(char, classpathPropertyBuffer, classpathPropertyBufferSize);
  memcpy(RUNTIME_ARRAY_BODY(classpathPropertyBuffer),
         CLASSPATH_PROPERTY,
         sizeof(CLASSPATH_PROPERTY) - 1);
  memcpy(RUNTIME_ARRAY_BODY(classpathPropertyBuffer)
         + sizeof(CLASSPATH_PROPERTY) - 1,
         classpath,
         classpathSize + 1);

  vmArgs.options[optionIndex++].optionString
    = RUNTIME_ARRAY_BODY(classpathPropertyBuffer);

  for (int i = 1; i < ac; ++i) {
    if (strncmp(av[i], "-X", 2) == 0
        or strncmp(av[i], "-D", 2) == 0)
    {
      vmArgs.options[optionIndex++].optionString = const_cast<char*>(av[i]);
    }
  }

  if (class_ == 0) {
    usageAndExit(av[0]);
  }

  JavaVM* vm;
  void* env;
  JNI_CreateJavaVM(&vm, &env, &vmArgs);
  JNIEnv* e = static_cast<JNIEnv*>(env);

  jclass c = e->FindClass(class_);

  if (jar) {
    free(const_cast<char*>(class_));
  }

  if (not e->ExceptionCheck()) {
    jmethodID m = e->GetStaticMethodID(c, "main", "([Ljava/lang/String;)V");
    if (not e->ExceptionCheck()) {
      jclass stringClass = e->FindClass("java/lang/String");
      if (not e->ExceptionCheck()) {
        jobjectArray a = e->NewObjectArray(argc, stringClass, 0);
        if (not e->ExceptionCheck()) {
          for (int i = 0; i < argc; ++i) {
            e->SetObjectArrayElement(a, i, e->NewStringUTF(argv[i]));
          }
          
          e->CallStaticVoidMethod(c, m, a);
        }
      }
    }
  }

  int exitCode = 0;
  if (e->ExceptionCheck()) {
    exitCode = -1;
    e->ExceptionDescribe();
  }

  vm->DestroyJavaVM();

  return exitCode;
}
