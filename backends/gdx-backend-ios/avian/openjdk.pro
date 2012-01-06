# proguard include file (http://proguard.sourceforge.net)

# This file is for use in combination with vm.pro when ProGuarding
# OpenJDK-based builds

# the following methods and fields are refered to by name in the VM:

-keepclassmembers class java.lang.Thread {
   public void run();
 }

-keep class java.lang.System {
   private static void initializeSystemClass();
 }

-keep class java.lang.ClassLoader {
   private static java.lang.ClassLoader scl;
   private static boolean sclSet;

   protected ClassLoader(java.lang.ClassLoader);
 }

-keep class avian.SystemClassLoader {
   protected java.net.URL findResource(java.lang.String);
 }

-keepnames class java.lang.ClassLoader {
   public java.lang.Class loadClass(java.lang.String);
   static void loadLibrary(java.lang.Class, java.lang.String, boolean);
   private static java.net.URL getBootstrapResource(java.lang.String);
   private static java.util.Enumeration getBootstrapResources(java.lang.String);
 }

-keep class java.util.Properties {
   public java.lang.Object setProperty(java.lang.String, java.lang.String);
 }

-keep class avian.OpenJDK {
   public static java.security.ProtectionDomain getProtectionDomain();
 }

-keepclassmembers public class java.security.PrivilegedAction {
   public java.lang.Object run();
 }

-keepclassmembers public class * implements java.security.PrivilegedAction {
   public java.lang.Object run();
 }

-keepclassmembers public class java.security.PrivilegedExceptionAction {
   public java.lang.Object run();
 }

-keepclassmembers public class * implements java.security.PrivilegedExceptionAction {
   public java.lang.Object run();
 }

-keep public class java.security.PrivilegedActionException {
   public PrivilegedActionException(java.lang.Exception);
 }

# these class names are used to disambiguate JNI method lookups:

-keepnames public class java.net.URL
-keepnames public class java.util.Enumeration
-keepnames public class java.security.ProtectionDomain
-keepnames public class java.security.PrivilegedAction
-keepnames public class java.security.PrivilegedExceptionAction
-keepnames public class java.security.AccessControlContext

# the following methods and fields are refered to by name in the OpenJDK
# native code:

-keep class java.util.Properties {
   public java.lang.Object put(java.lang.Object, java.lang.Object);
 }

-keepclassmembers class * {
   public boolean equals(java.lang.Object);
   public void wait();
   public void notify();
   public void notifyAll();
   public java.lang.String toString();
 }

-keepclassmembers class java.lang.String {
   public String(byte[]);
   public String(byte[], java.lang.String);
   public byte[] getBytes();
   public byte[] getBytes(java.lang.String);
 }

-keepclassmembers class java.lang.Boolean {
   public boolean getBoolean(java.lang.String);
 }

-keepclassmembers class java.util.zip.Inflater {
   long strm;
   boolean needDict;
   boolean finished;
   byte[] buf;
   int off;
   int len;
 }

-keepclassmembers class java.io.FileDescriptor {
   private int fd;
   private long handle;
 }

-keep class java.net.InetAddress {
   <fields>;
 }
-keep class java.net.Inet4Address {
   <fields>;
 }
-keep class java.net.Inet4AddressImpl
-keep class java.net.Inet6Address {
   <fields>;
 }
-keep class java.net.Inet6AddressImpl
-keep class java.net.InetSocketAddress {
   public InetSocketAddress(java.net.InetAddress, int);   
 }
-keep class java.net.ServerSocket

-keepclassmembers class java.net.PlainSocketImpl {
   <fields>;
 }

-keepclassmembers class java.io.FileInputStream {
   private java.io.FileDescriptor fd;   
 }

-keepclassmembers class java.io.FileOutputStream {
   private java.io.FileDescriptor fd;
   private boolean append;
 }

# changed in native code via sun.misc.Unsafe (todo: handle other
# Atomic* classes)
-keepclassmembers class java.util.concurrent.atomic.AtomicInteger {
   private int value;   
 }

# avoid inlining due to access check using a fixed offset into call stack:
-keep,allowshrinking,allowobfuscation class java.util.concurrent.atomic.AtomicReferenceFieldUpdater {
   *** newUpdater(...);
 }

# accessed reflectively via an AtomicReferenceFieldUpdater:
-keepclassmembers class java.io.BufferedInputStream {
   protected byte[] buf;
 }

-keep class java.lang.System {
   public static java.io.InputStream in;
   public static java.io.PrintStream out;
   public static java.io.PrintStream err;
   # avoid inlining due to access check using fixed offset into call stack:
   static java.lang.Class getCallerClass();
   # called from jni_util.c:
   static java.lang.String getProperty(java.lang.String);
 }

# refered to by name from native code:
-keepnames public class java.io.InputStream
-keepnames public class java.io.PrintStream

# avoid inlining due to access check using fixed offset into call stack:
-keep,allowshrinking,allowobfuscation class java.lang.System {
   static java.lang.Class getCallerClass();
 }

-keep class java.io.UnixFileSystem {
   public UnixFileSystem();
 }

-keep class java.io.WinNTFileSystem {
   public WinNTFileSystem();
 }

-keep class java.io.File {
   private java.lang.String path;
 }

-keepclassmembers class java.lang.ClassLoader$NativeLibrary {
   long handle;
   private int jniVersion;
 }

-keep class java.nio.charset.Charset {
   # called from jni_util.c:
   boolean isSupported(java.lang.String);
 }

# Charsets are loaded via reflection.  If you need others besides
# UTF-8, you'll need to add them (e.g. sun.nio.cs.ISO_8859_1).
-keep class sun.nio.cs.UTF_8

# loaded reflectively to handle embedded resources:
-keep class avian.resource.Handler

# refered to symbolically in MethodAccessorGenerator:
-keep class sun.reflect.MethodAccessorImpl {
   <methods>;
 }
-keep class sun.reflect.ConstructorAccessorImpl {
   <methods>;
 }
-keep class sun.reflect.SerializationConstructorAccessorImpl {
   <methods>;
 }

# referred to by name in LocaleData to load resources:
-keep class sun.util.resources.CalendarData
-keep class sun.util.resources.TimeZoneNames
-keep class sun.text.resources.FormatData

