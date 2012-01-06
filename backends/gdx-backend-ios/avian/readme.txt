Quick Start
-----------

on Linux:
 $ export JAVA_HOME=/usr/local/java # or wherever you have the JDK installed
 $ make
 $ build/linux-i386/avian -cp build/linux-i386/test Hello

on Mac OS X:
 $ export JAVA_HOME=/Library/Java/Home
 $ make
 $ build/darwin-i386/avian -cp build/darwin-i386/test Hello
 
on Windows (MSYS):
 $ git clone git://oss.readytalk.com/win32.git ../win32
 $ export JAVA_HOME="C:/Program Files/Java/jdk1.6.0_07"
 $ make
 $ build/windows-i386/avian -cp build/windows-i386/test Hello

on Windows (Cygwin):
 $ git clone git://oss.readytalk.com/win32.git ../win32
 $ export JAVA_HOME="/cygdrive/c/Program Files/Java/jdk1.6.0_07"
 $ make
 $ build/windows-i386/avian -cp build/windows-i386/test Hello

Adjust JAVA_HOME according to your system, but be sure to use forward
slashes in the path.


Introduction
------------

Avian is a lightweight virtual machine and class library designed to
provide a useful subset of Java's features, suitable for building
self-contained applications.  More information is available at the
project web site:

  http://oss.readytalk.com/avian

If you have any trouble building, running, or embedding Avian, please
post a message to our discussion group:

  http://groups.google.com/group/avian

That's also the place for any other questions, comments, or
suggestions you might have.


Supported Platforms
-------------------

Avian can currently target the following platforms:

  Linux (i386, x86_64, ARM, and 32-bit PowerPC)
  Windows (i386 and x86_64)
  Mac OS X (i386, x86_64 and 32-bit PowerPC)


Building
--------

Build requirements include:

  * GNU make 3.80 or later
  * GCC 3.4 or later (4.5.1 or later for Windows/x86_64)
  * JDK 1.5 or later
  * MinGW 3.4 or later (only if compiling for Windows)
  * zlib 1.2.3 or later

Earlier versions of some of these packages may also work but have not
been tested.

The build is directed by a single makefile and may be influenced via
certain flags described below, all of which are optional.

 $ make \
     platform={linux,windows,darwin} \
     arch={i386,x86_64,powerpc,arm} \
     process={compile,interpret} \
     mode={debug,debug-fast,fast,small} \
     bootimage={true,false} \
     heapdump={true,false} \
     tails={true,false} \
     continuations={true,false} \
     openjdk=<openjdk installation directory> \
     openjdk-src=<openjdk source directory>

  * platform - the target platform
      default: output of $(uname -s | tr [:upper:] [:lower:]),
      normalized in some cases (e.g. CYGWIN_NT-5.1 -> windows)

  * arch - the target architecture
      default: output of $(uname -m), normalized in some cases
      (e.g. i686 -> i386)

  * mode - which set of compilation flags to use to determine
    optimization level, debug symbols, and whether to enable
    assertions
      default: fast

  * process - choice between pure interpreter or JIT compiler
      default: compile

  * bootimage - if true, create a boot image containing the pre-parsed
    class library and ahead-of-time compiled methods.  This option is
    only valid for process=compile builds.  Note that you may need to
    specify both build-arch=x86_64 and arch=x86_64 on 64-bit systems
    where "uname -m" prints "i386".
      default: false

  * heapdump - if true, implement avian.Machine.dumpHeap(String),
    which, when called, will generate a snapshot of the heap in a
    simple, ad-hoc format for memory profiling purposes.  See
    heapdump.cpp for details.
      default: false

  * tails - if true, optimize each tail call by replacing the caller's
    stack frame with the callee's.  This convention ensures proper
    tail recursion, suitable for languages such as Scheme.  This
    option is only valid for process=compile builds.
      default: false

  * continuations - if true, support continuations via the
    avian.Continuations methods callWithCurrentContinuation and
    dynamicWind.  See Continuations.java for details.  This option is
    only valid for process=compile builds.
      default: false

  * openjdk - if set, use OpenJDK class library instead of the default
    Avian class library.  See "Building with the OpenJDK Class
    Library" below for details.
      default: not set

  * openjdk-src - if this and the openjdk option above are both set,
    build an embeddable VM using the OpenJDK class library.  The JNI
    components of the OpenJDK class library will be built from the
    sources found under the specified directory.  See "Building with
    the OpenJDK Class Library" below for details.
      default: not set

These flags determine the name of the directory used for the build.
The name always starts with ${platform}-${arch}, and each non-default
build option is appended to the name.  For example, a debug build with
bootimage enabled on Linux/i386 would be built in
build/linux-i386-debug-bootimage.  This allows you to build with
several different sets of options independently and even
simultaneously without doing a clean build each time.

If you are compiling for Windows, you may either cross-compile using
MinGW or build natively on Windows under MSYS or Cygwin.

Installing MSYS:

  1. Download and install the current MinGW and MSYS packages from
  mingw.org, selecting the C and C++ compilers when prompted.  Use the
  post-install script to create the filesystem link to the compiler.

  2. Download GNU Make 3.81 from the MSYS download page
  (make-3.81-MSYS-1.0.11-2.tar.bz2) and extract the tar file into
  e.g. c:/msys/1.0.

Installing Cygwin:

  1. Download and run setup.exe from cygwin.com, installing the base
  system and these packages: make, gcc-mingw-g++,
  mingw64-i686-gcc-g++, mingw64-x86_64-gcc-g++, and (optionally) git.

You may also find our win32 repository useful: (run this from the
directory containing the avian directory)

  $ git clone git://oss.readytalk.com/win32.git

This gives you the Windows JNI headers, zlib headers and library, and
a few other useful libraries like OpenSSL, libjpeg, and libpng.
There's also a win64 repository for 64-bit builds:

  $ git clone git://oss.readytalk.com/win64.git


Building with the Microsoft Visual C++ Compiler
-----------------------------------------------

You can also build using the MSVC compiler, which makes debugging with
tools like WinDbg and Visual Studio much easier.  Note that you will
still need to have GCC installed - MSVC is only used to compile the
C++ portions of the VM, while the assembly code and helper tools are
built using GCC.

The MSVC build has been tested with Visual Studio Express Edition
versions 8, 9, and 10.  Other versions may also work.

To build with MSVC, install Cygwin as described above and set the
following environment variables:

 $ export PATH="/usr/local/bin:/usr/bin:/bin:/usr/X11R6/bin:/cygdrive/c/Program Files/Microsoft Visual Studio 9.0/Common7/IDE:/cygdrive/c/Program Files/Microsoft Visual Studio 9.0/VC/BIN:/cygdrive/c/Program Files/Microsoft Visual Studio 9.0/Common7/Tools:/cygdrive/c/WINDOWS/Microsoft.NET/Framework/v3.5:/cygdrive/c/WINDOWS/Microsoft.NET/Framework/v2.0.50727:/cygdrive/c/Program Files/Microsoft Visual Studio 9.0/VC/VCPackages:/cygdrive/c/Program Files/Microsoft SDKs/Windows/v6.0A/bin:/cygdrive/c/WINDOWS/system32:/cygdrive/c/WINDOWS:/cygdrive/c/WINDOWS/System32/Wbem"

 $ export LIBPATH="C:\WINDOWS\Microsoft.NET\Framework\v3.5;C:\WINDOWS\Microsoft.NET\Framework\v2.0.50727;C:\Program Files\Microsoft Visual Studio 9.0\VC\LIB;"

 $ export VCINSTALLDIR="C:\Program Files\Microsoft Visual Studio 9.0\VC"

 $ export LIB="C:\Program Files\Microsoft Visual Studio 9.0\VC\LIB;C:\Program Files\Microsoft SDKs\Windows\v6.0A\lib;"

 $ export INCLUDE="C:\Program Files\Microsoft Visual Studio 9.0\VC\INCLUDE;C:\Program Files\Microsoft SDKs\Windows\v6.0A\include;"

Adjust these definitions as necessary according to your MSVC
installation.

Finally, build with the msvc flag set to the MSVC tool directory:

 $ make msvc="/cygdrive/c/Program Files/Microsoft Visual Studio 9.0/VC"


Building with the OpenJDK Class Library
---------------------------------------

By default, Avian uses its own lightweight class library.  However,
that library only contains a relatively small subset of the classes
and methods included in the JRE.  If your application requires
features beyond that subset, you may want to tell Avian to use
OpenJDK's class library instead.  To do so, specify the directory
where OpenJDK is installed, e.g.:

 $ make openjdk=/usr/lib/jvm/java-6-openjdk

This will build Avian as a conventional JVM (e.g. libjvm.so) which
loads its boot class library and native libraries (e.g. libjava.so)
from /usr/lib/jvm/java-6-openjdk/jre at runtime.  To run an
application in this configuration, you'll need to make sure the VM is
in your library search path.  For example:

 $ LD_LIBRARY_PATH=build/linux-x86_64-openjdk \
     build/linux-x86_64-openjdk/avian-dynamic -cp /path/to/my/application \
     com.example.MyApplication

Alternatively, you can enable a stand-alone build using OpenJDK by
specifying the location of the OpenJDK source code, e.g.:

 $ make openjdk=$(pwd)/../jdk6/build/linux-amd64/j2sdk-image \
     openjdk-src=$(pwd)/../jdk6/jdk/src

You must ensure that the path specified for openjdk-src does not have
any spaces in it; make gets confused when dependency paths include
spaces, and we haven't found away around that except to avoid paths
with spaces entirely.

The result of such a build is a self-contained binary which does not
depend on external libraries, jars, or other files.  In this case, the
specified paths are used only at build time; anything needed at
runtime is embedded in the binary.  Thus, the process of running an
application is simplified:

 $ build/linux-x86_64-openjdk-src/avian -cp /path/to/my/application \
     com.example.MyApplication

Note that the resulting binary will be very large due to the size of
OpenJDK's class library.  This can be mitigated using UPX, preferably
an LZMA-enabled version:

 $ upx --lzma --best build/linux-x86_64-openjdk-src/avian

You can reduce the size futher for embedded builds by using ProGuard
and the supplied openjdk.pro configuration file (see "Embedding with
ProGuard and a Boot Image" below).  Also see app.mk in
git://oss.readytalk.com/avian-swt-examples.git for an example of using
Avian, OpenJDK, ProGuard, and UPX in concert.

Here are some examples of how to install OpenJDK and build Avian with
it on various OSes:

  Debian-based Linux:
    # conventional build:
    apt-get install openjdk-6-jdk
    make openjdk=/usr/lib/jvm/java-6-openjdk test

    # stand-alone build:
    apt-get install openjdk-6-jdk
    apt-get source openjdk-6-jdk
    apt-get build-dep openjdk-6-jdk
    (cd openjdk-6-6b18-1.8.3 && ./debian/rules patch)
    make openjdk=/usr/lib/jvm/java-6-openjdk \
      openjdk-src=$(pwd)/openjdk-6-6b18-1.8.3/build/openjdk/jdk/src \
      test

  Mac OS X:
    # Prerequisite: install MacPorts (http://www.macports.org/)
    sudo port selfupdate

    # conventional build:
    sudo port install openjdk6
    make openjdk=/opt/local/share/java/openjdk6 test

    # stand-alone build:
    sudo port fetch openjdk6
    sudo port patch openjdk6
    make openjdk=/opt/local/share/java/openjdk6 \
      openjdk-src=/opt/local/var/macports/build/_opt_local_var_macports_sources_rsync.macports.org_release_ports_java_openjdk6/work/jdk/src \
      test

  Windows (Cygwin):
    # conventional build:
    # Prerequisite: download and install the latest Windows OpenJDK
    # build from http://www.openscg.com/se/openjdk/
    make openjdk=/cygdrive/c/OpenSCG/openjdk-6.21 test

    # stand-alone build:
    # Prerequisite: install OpenSCG build as above, plus the
    # corresponding source bundle from
    # http://download.java.net/openjdk/jdk6/promoted/, e.g.:
    wget http://download.java.net/openjdk/jdk6/promoted/b21/openjdk-6-src-b21-20_jan_2011.tar.gz
    mkdir openjdk
    (cd openjdk && tar xzf ../openjdk-6-src-b21-20_jan_2011.tar.gz)
    make openjdk=/cygdrive/c/OpenSCG/openjdk-6.21 \
      openjdk-src=$(pwd)/openjdk/jdk/src \
      test


Installing
----------

Installing Avian is as simple as copying the executable to the desired
directory:

 $ cp build/${platform}-${arch}/avian ~/bin/


Embedding
---------

The following series of commands illustrates how to produce a
stand-alone executable out of a Java application using Avian.

Note: if you are building on Cygwin, prepend "x86_64-w64-mingw32-" or
"i686-w64-mingw32-" to the ar, g++, gcc, strip, and dlltool commands
below (e.g. x86_64-w64-mingw32-gcc).

Step 1: Build Avian, create a new directory, and populate it with the
VM object files and bootstrap classpath jar.

 $ make
 $ mkdir hello
 $ cd hello
 $ ar x ../build/${platform}-${arch}/libavian.a
 $ cp ../build/${platform}-${arch}/classpath.jar boot.jar

Step 2: Build the Java code and add it to the jar.

 $ cat >Hello.java <<EOF
public class Hello {
  public static void main(String[] args) {
    System.out.println("hello, world!");
  }
}
EOF
 $ javac -bootclasspath boot.jar Hello.java
 $ jar u0f boot.jar Hello.class

Step 3: Make an object file out of the jar.

 $ ../build/${platform}-${arch}/binaryToObject boot.jar boot-jar.o \
     _binary_boot_jar_start _binary_boot_jar_end ${platform} ${arch}

Step 4: Write a driver which starts the VM and runs the desired main
method.  Note the bootJar function, which will be called by the VM to
get a handle to the embedded jar.  We tell the VM about this jar by
setting the boot classpath to "[bootJar]".

 $ cat >main.cpp <<EOF
#include "stdint.h"
#include "jni.h"

#if (defined __MINGW32__) || (defined _MSC_VER)
#  define EXPORT __declspec(dllexport)
#else
#  define EXPORT __attribute__ ((visibility("default"))) \
  __attribute__ ((used))
#endif

#if (! defined __x86_64__) && ((defined __MINGW32__) || (defined _MSC_VER))
#  define SYMBOL(x) binary_boot_jar_##x
#else
#  define SYMBOL(x) _binary_boot_jar_##x
#endif

extern "C" {

  extern const uint8_t SYMBOL(start)[];
  extern const uint8_t SYMBOL(end)[];

  EXPORT const uint8_t*
  bootJar(unsigned* size)
  {
    *size = SYMBOL(end) - SYMBOL(start);
    return SYMBOL(start);
  }

} // extern "C"

int
main(int ac, const char** av)
{
  JavaVMInitArgs vmArgs;
  vmArgs.version = JNI_VERSION_1_2;
  vmArgs.nOptions = 1;
  vmArgs.ignoreUnrecognized = JNI_TRUE;

  JavaVMOption options[vmArgs.nOptions];
  vmArgs.options = options;

  options[0].optionString = const_cast<char*>("-Xbootclasspath:[bootJar]");

  JavaVM* vm;
  void* env;
  JNI_CreateJavaVM(&vm, &env, &vmArgs);
  JNIEnv* e = static_cast<JNIEnv*>(env);

  jclass c = e->FindClass("com/badllogic/avian/Hello");
  if (not e->ExceptionCheck()) {
    jmethodID m = e->GetStaticMethodID(c, "main", "([Ljava/lang/String;)V");
    if (not e->ExceptionCheck()) {
      jclass stringClass = e->FindClass("java/lang/String");
      if (not e->ExceptionCheck()) {
        jobjectArray a = e->NewObjectArray(ac-1, stringClass, 0);
        if (not e->ExceptionCheck()) {
          for (int i = 1; i < ac; ++i) {
            e->SetObjectArrayElement(a, i-1, e->NewStringUTF(av[i]));
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
EOF

on Linux:
 $ g++ -I$JAVA_HOME/include -I$JAVA_HOME/include/linux \
     -D_JNI_IMPLEMENTATION_ -c main.cpp -o main.o

on Mac OS X:
 $ g++ -I$JAVA_HOME/include -D_JNI_IMPLEMENTATION_ -c main.cpp -o main.o

on Windows:
 $ g++ -I$JAVA_HOME/include -I$JAVA_HOME/include/win32 \
     -D_JNI_IMPLEMENTATION_ -c main.cpp -o main.o

Step 5: Link the objects produced above to produce the final
executable, and optionally strip its symbols.

on Linux:
 $ g++ -rdynamic *.o -ldl -lpthread -lz -o hello
 $ strip --strip-all hello

on Mac OS X:
 $ g++ -rdynamic *.o -ldl -lpthread -lz -o hello -framework CoreFoundation
 $ strip -S -x hello

on Windows:
 $ dlltool -z hello.def *.o
 $ dlltool -d hello.def -e hello.exp
 $ g++ hello.exp *.o -L../../win32/lib -lmingwthrd -lm -lz -lws2_32 \
     -mwindows -mconsole -o hello.exe
 $ strip --strip-all hello.exe


Embedding with ProGuard and a Boot Image
----------------------------------------

The following illustrates how to embed an application as above, except
this time we preprocess the code using ProGuard and build a boot image
from it for quicker startup.  The pros and cons of using ProGuard are
as follow:

 * Pros: ProGuard will eliminate unused code, optimize the rest, and
   obfuscate it as well for maximum space savings

 * Cons: increased build time, especially for large applications, and
   extra effort needed to configure it for applications which rely
   heavily on reflection and/or calls to Java from native code

For boot image builds:

 * Pros: the boot image build pre-parses all the classes and compiles
   all the methods, obviating the need for JIT compilation at runtime.
   This also makes garbage collection faster, since the pre-parsed
   classes are never visited.

 * Cons: the pre-parsed classes and AOT-compiled methods take up more
   space in the executable than the equivalent class files.  In
   practice, this can make the executable 30-50% larger.  Also, AOT
   compilation does not yet yield significantly faster or smaller code
   than JIT compilation.  Finally, floating point code may be slower
   on 32-bit x86 since the compiler cannot assume SSE2 support will be
   available at runtime, and the x87 FPU is not supported except via
   out-of-line helper functions.

Note you can use ProGuard without using a boot image and vice-versa,
as desired.

The following instructions assume we are building for Linux/i386.
Please refer to the previous example for guidance on other platforms.

Step 1: Build Avian, create a new directory, and populate it with the
VM object files.

 $ make bootimage=true
 $ mkdir hello
 $ cd hello
 $ ar x ../build/linux-i386-bootimage/libavian.a

Step 2: Create a stage1 directory and extract the contents of the
class library jar into it.

 $ mkdir stage1
 $ (cd stage1 && jar xf ../../build/linux-i386-bootimage/classpath.jar)

Step 3: Build the Java code and add it to stage1.

 $ cat >Hello.java <<EOF
public class Hello {
  public static void main(String[] args) {
    System.out.println("hello, world!");
  }
}
EOF
 $ javac -bootclasspath stage1 -d stage1 Hello.java

Step 4: Create a ProGuard configuration file specifying Hello.main as
the entry point.

 $ cat >hello.pro <<EOF
-keep class Hello {
   public static void main(java.lang.String[]);
 }
EOF

Step 5: Run ProGuard with stage1 as input and stage2 as output.

 $ java -jar ../../proguard4.6/lib/proguard.jar \
     -injars stage1 -outjars stage2 @../vm.pro @hello.pro

(note: pass -dontusemixedcaseclassnames to ProGuard when building on
systems with case-insensitive filesystems such as Windows and OS X)

Step 6: Build the boot and code images.

 $ ../build/linux-i386-bootimage/bootimage-generator stage2 \
     bootimage.bin codeimage.bin

Step 7: Make an object file out of the boot and code images.

 $ ../build/linux-i386-bootimage/binaryToObject \
     bootimage.bin bootimage-bin.o \
     _binary_bootimage_bin_start _binary_bootimage_bin_end \
     linux i386 8 writable

 $ ../build/linux-i386-bootimage/binaryToObject \
     codeimage.bin codeimage-bin.o \
     _binary_codeimage_bin_start _binary_codeimage_bin_end \
     linux i386 8 executable

Step 8: Write a driver which starts the VM and runs the desired main
method.  Note the bootimageBin function, which will be called by the
VM to get a handle to the embedded boot image.  We tell the VM about
this function via the "avian.bootimage" property.

Note also that this example includes no resources besides class files.
If our application loaded resources such as images and properties
files via the classloader, we would also need to embed the jar file
containing them.  See the previous example for instructions.

 $ cat >main.cpp <<EOF
#include "stdint.h"
#include "jni.h"

#if (defined __MINGW32__) || (defined _MSC_VER)
#  define EXPORT __declspec(dllexport)
#else
#  define EXPORT __attribute__ ((visibility("default")))
#endif

#if (! defined __x86_64__) && ((defined __MINGW32__) || (defined _MSC_VER))
#  define BOOTIMAGE_BIN(x) binary_bootimage_bin_##x
#  define CODEIMAGE_BIN(x) binary_codeimage_bin_##x
#else
#  define BOOTIMAGE_BIN(x) _binary_bootimage_bin_##x
#  define CODEIMAGE_BIN(x) _binary_codeimage_bin_##x
#endif

extern "C" {

  extern const uint8_t BOOTIMAGE_BIN(start)[];
  extern const uint8_t BOOTIMAGE_BIN(end)[];

  EXPORT const uint8_t*
  bootimageBin(unsigned* size)
  {
    *size = BOOTIMAGE_BIN(end) - BOOTIMAGE_BIN(start);
    return BOOTIMAGE_BIN(start);
  }

  extern const uint8_t CODEIMAGE_BIN(start)[];
  extern const uint8_t CODEIMAGE_BIN(end)[];

  EXPORT const uint8_t*
  codeimageBin(unsigned* size)
  {
    *size = CODEIMAGE_BIN(end) - CODEIMAGE_BIN(start);
    return CODEIMAGE_BIN(start);
  }

} // extern "C"

int
main(int ac, const char** av)
{
  JavaVMInitArgs vmArgs;
  vmArgs.version = JNI_VERSION_1_2;
  vmArgs.nOptions = 2;
  vmArgs.ignoreUnrecognized = JNI_TRUE;

  JavaVMOption options[vmArgs.nOptions];
  vmArgs.options = options;

  options[0].optionString
    = const_cast<char*>("-Davian.bootimage=bootimageBin");

  options[1].optionString
    = const_cast<char*>("-Davian.codeimage=codeimageBin");

  JavaVM* vm;
  void* env;
  JNI_CreateJavaVM(&vm, &env, &vmArgs);
  JNIEnv* e = static_cast<JNIEnv*>(env);

  jclass c = e->FindClass("Hello");
  if (not e->ExceptionCheck()) {
    jmethodID m = e->GetStaticMethodID(c, "main", "([Ljava/lang/String;)V");
    if (not e->ExceptionCheck()) {
      jclass stringClass = e->FindClass("java/lang/String");
      if (not e->ExceptionCheck()) {
        jobjectArray a = e->NewObjectArray(ac-1, stringClass, 0);
        if (not e->ExceptionCheck()) {
          for (int i = 1; i < ac; ++i) {
            e->SetObjectArrayElement(a, i-1, e->NewStringUTF(av[i]));
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
EOF

 $ g++ -I$JAVA_HOME/include -I$JAVA_HOME/include/linux \
     -D_JNI_IMPLEMENTATION_ -c main.cpp -o main.o

Step 9: Link the objects produced above to produce the final
executable, and optionally strip its symbols.

 $ g++ -rdynamic *.o -ldl -lpthread -lz -o hello
 $ strip --strip-all hello


Trademarks
----------

Oracle and Java are registered trademarks of Oracle and/or its
affiliates.  Other names may be trademarks of their respective owners.

The Avian project is not affiliated with Oracle.
