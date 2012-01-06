openjdk-sources = \
	$(openjdk-src)/share/native/common/check_code.c \
	$(openjdk-src)/share/native/common/check_format.c \
	$(openjdk-src)/share/native/common/check_version.c \
	$(openjdk-src)/share/native/common/jdk_util.c \
	$(openjdk-src)/share/native/common/jio.c \
	$(openjdk-src)/share/native/common/jni_util.c \
	$(openjdk-src)/share/native/common/verify_stub.c \
	$(openjdk-src)/share/native/java/io/FileInputStream.c \
	$(openjdk-src)/share/native/java/io/io_util.c \
	$(openjdk-src)/share/native/java/io/ObjectInputStream.c \
	$(openjdk-src)/share/native/java/io/ObjectOutputStream.c \
	$(openjdk-src)/share/native/java/io/ObjectStreamClass.c \
	$(openjdk-src)/share/native/java/io/RandomAccessFile.c \
	$(openjdk-src)/share/native/java/lang/Class.c \
	$(openjdk-src)/share/native/java/lang/ClassLoader.c \
	$(openjdk-src)/share/native/java/lang/Compiler.c \
	$(openjdk-src)/share/native/java/lang/Double.c \
	$(openjdk-src)/share/native/java/lang/Float.c \
	$(openjdk-src)/share/native/java/lang/Object.c \
	$(openjdk-src)/share/native/java/lang/Package.c \
	$(openjdk-src)/share/native/java/lang/ref/Finalizer.c \
	$(openjdk-src)/share/native/java/lang/reflect/Array.c \
	$(openjdk-src)/share/native/java/lang/reflect/Proxy.c \
	$(openjdk-src)/share/native/java/lang/ResourceBundle.c \
	$(openjdk-src)/share/native/java/lang/Runtime.c \
	$(openjdk-src)/share/native/java/lang/SecurityManager.c \
	$(openjdk-src)/share/native/java/lang/Shutdown.c \
	$(openjdk-src)/share/native/java/lang/StrictMath.c \
	$(openjdk-src)/share/native/java/lang/String.c \
	$(openjdk-src)/share/native/java/lang/System.c \
	$(openjdk-src)/share/native/java/lang/Thread.c \
	$(openjdk-src)/share/native/java/lang/Throwable.c \
	$(wildcard $(openjdk-src)/share/native/java/lang/fdlibm/src/*.c) \
	$(openjdk-src)/share/native/java/net/DatagramPacket.c \
	$(openjdk-src)/share/native/java/net/InetAddress.c \
	$(openjdk-src)/share/native/java/net/Inet4Address.c \
	$(openjdk-src)/share/native/java/net/Inet6Address.c \
	$(openjdk-src)/share/native/java/nio/Bits.c \
	$(openjdk-src)/share/native/java/security/AccessController.c \
	$(openjdk-src)/share/native/java/sql/DriverManager.c \
	$(openjdk-src)/share/native/java/util/concurrent/atomic/AtomicLong.c \
	$(openjdk-src)/share/native/java/util/TimeZone.c \
	$(openjdk-src)/share/native/java/util/zip/Adler32.c \
	$(openjdk-src)/share/native/java/util/zip/CRC32.c \
	$(openjdk-src)/share/native/java/util/zip/Deflater.c \
	$(openjdk-src)/share/native/java/util/zip/Inflater.c \
	$(openjdk-src)/share/native/java/util/zip/ZipEntry.c \
	$(openjdk-src)/share/native/java/util/zip/ZipFile.c \
	$(openjdk-src)/share/native/java/util/zip/zip_util.c \
	$(openjdk-src)/share/native/sun/management/VMManagementImpl.c \
	$(openjdk-src)/share/native/sun/misc/GC.c \
	$(openjdk-src)/share/native/sun/misc/MessageUtils.c \
	$(openjdk-src)/share/native/sun/misc/NativeSignalHandler.c \
	$(openjdk-src)/share/native/sun/misc/Signal.c \
	$(openjdk-src)/share/native/sun/misc/Version.c \
	$(openjdk-src)/share/native/sun/misc/VM.c \
	$(openjdk-src)/share/native/sun/misc/VMSupport.c \
	$(openjdk-src)/share/native/sun/reflect/ConstantPool.c \
	$(openjdk-src)/share/native/sun/reflect/NativeAccessors.c \
	$(openjdk-src)/share/native/sun/reflect/Reflection.c

openjdk-headers-classes = \
	java.io.Console \
	java.io.FileDescriptor \
	java.io.FileInputStream \
	java.io.FileOutputStream \
	java.io.FileSystem \
	java.io.ObjectInputStream \
	java.io.ObjectOutputStream \
	java.io.ObjectStreamClass \
	java.io.RandomAccessFile \
	java.lang.Class \
	java.lang.ClassLoader \
	java.lang.Compiler \
	java.lang.Double \
	java.lang.Float \
	java.lang.Integer \
	java.lang.Object \
	java.lang.Package \
	java.lang.Runtime \
	java.lang.SecurityManager \
	java.lang.Shutdown \
	java.lang.StrictMath \
	java.lang.String \
	java.lang.System \
	java.lang.Thread \
	java.lang.Throwable \
	java.lang.ref.Finalizer \
	java.lang.reflect.Array \
	java.lang.reflect.Proxy \
	java.net.InetAddress \
	java.net.Inet4Address \
	java.net.Inet6Address \
	java.net.DatagramPacket \
	java.net.SocketOptions \
	java.net.InetAddressImplFactory \
	java.net.Inet4AddressImpl \
	java.net.Inet6AddressImpl \
	java.net.NetworkInterface \
	java.net.PlainSocketImpl \
	java.net.SocketInputStream \
	java.net.SocketOutputStream \
	java.nio.MappedByteBuffer \
	java.security.AccessController \
	java.util.ResourceBundle \
	java.util.TimeZone \
	java.util.concurrent.atomic.AtomicLong \
	java.util.jar.JarFile \
	java.util.zip.Adler32 \
	java.util.zip.CRC32 \
	java.util.zip.Deflater \
	java.util.zip.Inflater \
	java.util.zip.ZipEntry \
	java.util.zip.ZipFile \
	sun.management.VMManagementImpl \
	sun.misc.GC \
	sun.misc.MessageUtils \
	sun.misc.NativeSignalHandler \
	sun.misc.Signal \
	sun.misc.VM \
	sun.misc.VMSupport \
	sun.misc.Version \
	sun.net.spi.DefaultProxySelector \
	sun.nio.ch.FileKey \
	sun.nio.ch.FileChannelImpl \
	sun.nio.ch.FileDispatcher \
	sun.nio.ch.DatagramChannelImpl \
	sun.nio.ch.DatagramDispatcher \
	sun.nio.ch.IOStatus \
	sun.nio.ch.IOUtil \
	sun.nio.ch.Net \
	sun.nio.ch.ServerSocketChannelImpl \
	sun.nio.ch.SocketChannelImpl \
	sun.nio.ch.SocketDispatcher \
	sun.nio.ch.PollArrayWrapper \
	sun.nio.ch.NativeThread \
	sun.reflect.ConstantPool \
	sun.reflect.NativeConstructorAccessorImpl \
	sun.reflect.NativeMethodAccessorImpl \
	sun.reflect.Reflection \
	sun.security.provider.NativeSeedGenerator

# todo: set properties according to architecture targeted and OpenJDK
# version used:
openjdk-cflags = \
	"-I$(src)/openjdk" \
	"-I$(build)/openjdk" \
	"-I$(openjdk-src)/share/javavm/export" \
	"-I$(openjdk-src)/share/native/common" \
	"-I$(openjdk-src)/share/native/java/io" \
	"-I$(openjdk-src)/share/native/java/lang" \
	"-I$(openjdk-src)/share/native/java/lang/fdlibm/include" \
	"-I$(openjdk-src)/share/native/java/net" \
	"-I$(openjdk-src)/share/native/java/util/zip" \
	"-I$(openjdk-src)/share/native/sun/management" \
	"-I$(openjdk-src)/share/native/sun/nio/ch" \
	"-I$(openjdk-src)/share/javavm/include" \
	-D_LITTLE_ENDIAN \
	-DARCHPROPNAME=\"x86\" \
	-DRELEASE=\"1.6.0\" \
	-DJDK_MAJOR_VERSION=\"1\" \
	-DJDK_MINOR_VERSION=\"6\" \
	-DJDK_MICRO_VERSION=\"0\" \
	-DJDK_BUILD_NUMBER=\"0\" \
	-D_GNU_SOURCE

ifeq ($(platform),darwin)
	openjdk-cflags += \
		-D_LFS_LARGEFILE=1 \
		-D_ALLBSD_SOURCE
endif

ifeq ($(platform),windows)
	openjdk-sources += \
		$(openjdk-src)/windows/native/java/io/canonicalize_md.c \
		$(openjdk-src)/windows/native/java/io/Console_md.c \
		$(openjdk-src)/windows/native/java/io/FileDescriptor_md.c \
		$(openjdk-src)/windows/native/java/io/FileInputStream_md.c \
		$(openjdk-src)/windows/native/java/io/FileOutputStream_md.c \
		$(openjdk-src)/windows/native/java/io/FileSystem_md.c \
		$(openjdk-src)/windows/native/java/io/io_util_md.c \
		$(openjdk-src)/windows/native/java/io/RandomAccessFile_md.c \
		$(openjdk-src)/windows/native/java/io/Win32FileSystem_md.c \
		$(openjdk-src)/windows/native/java/io/WinNTFileSystem_md.c \
		$(openjdk-src)/windows/native/java/lang/java_props_md.c \
		$(openjdk-src)/windows/native/java/lang/ProcessEnvironment_md.c \
		$(openjdk-src)/windows/native/java/lang/ProcessImpl_md.c \
		$(openjdk-src)/windows/native/java/net/net_util_md.c \
		$(openjdk-src)/windows/native/java/net/DualStackPlainSocketImpl.c \
		$(openjdk-src)/windows/native/java/net/InetAddressImplFactory.c \
		$(openjdk-src)/windows/native/java/net/Inet4AddressImpl.c \
		$(openjdk-src)/windows/native/java/net/Inet6AddressImpl.c \
		$(openjdk-src)/windows/native/java/net/NetworkInterface.c \
		$(openjdk-src)/windows/native/java/net/NetworkInterface_winXP.c \
		$(openjdk-src)/windows/native/java/net/NetworkInterface_win9x.c \
		$(openjdk-src)/windows/native/java/net/SocketInputStream.c \
		$(openjdk-src)/windows/native/java/net/SocketOutputStream.c \
		$(openjdk-src)/windows/native/java/util/WindowsPreferences.c \
		$(openjdk-src)/windows/native/java/util/logging.c \
		$(openjdk-src)/windows/native/java/util/TimeZone_md.c \
		$(openjdk-src)/windows/native/sun/io/Win32ErrorMode.c \
		$(openjdk-src)/windows/native/sun/nio/ch/DatagramChannelImpl.c \
		$(openjdk-src)/windows/native/sun/nio/ch/DatagramDispatcher.c \
		$(openjdk-src)/windows/native/sun/nio/ch/FileChannelImpl.c \
		$(openjdk-src)/windows/native/sun/nio/ch/FileDispatcher.c \
		$(openjdk-src)/windows/native/sun/nio/ch/FileKey.c \
		$(openjdk-src)/windows/native/sun/nio/ch/IOUtil.c \
		$(openjdk-src)/windows/native/sun/nio/ch/Net.c \
		$(openjdk-src)/windows/native/sun/nio/ch/ServerSocketChannelImpl.c \
		$(openjdk-src)/windows/native/sun/nio/ch/SocketChannelImpl.c \
		$(openjdk-src)/windows/native/sun/nio/ch/SocketDispatcher.c \
		$(openjdk-src)/windows/native/sun/nio/ch/WindowsSelectorImpl.c \
		$(openjdk-src)/windows/native/sun/security/provider/WinCAPISeedGenerator.c

	openjdk-headers-classes += \
		java.net.DualStackPlainSocketImpl \
		java.lang.ProcessImpl \
		sun.io.Win32ErrorMode \
		sun.nio.ch.WindowsSelectorImpl \

	openjdk-cflags += \
		"-I$(openjdk-src)/windows/javavm/export" \
		"-I$(openjdk-src)/windows/native/common" \
		"-I$(openjdk-src)/windows/native/java/io" \
		"-I$(openjdk-src)/windows/native/java/net" \
		"-I$(openjdk-src)/windows/native/java/util" \
		"-I$(openjdk-src)/windows/native/sun/nio/ch" \
		"-I$(openjdk-src)/windows/javavm/include" \
		"-I$(root)/win32/include" \
		-D_JNI_IMPLEMENTATION_ \
		-D_JAVASOFT_WIN32_TYPEDEF_MD_H_ \
		-Ds6_words=_s6_words \
		-Ds6_bytes=_s6_bytes
else
	openjdk-sources += \
		$(openjdk-src)/solaris/native/common/jdk_util_md.c \
		$(openjdk-src)/solaris/native/java/io/canonicalize_md.c \
		$(openjdk-src)/solaris/native/java/io/Console_md.c \
		$(openjdk-src)/solaris/native/java/io/FileDescriptor_md.c \
		$(openjdk-src)/solaris/native/java/io/FileInputStream_md.c \
		$(openjdk-src)/solaris/native/java/io/FileOutputStream_md.c \
		$(openjdk-src)/solaris/native/java/io/FileSystem_md.c \
		$(openjdk-src)/solaris/native/java/io/io_util_md.c \
		$(openjdk-src)/solaris/native/java/io/RandomAccessFile_md.c \
		$(openjdk-src)/solaris/native/java/io/UnixFileSystem_md.c \
		$(openjdk-src)/solaris/native/java/lang/java_props_md.c \
		$(openjdk-src)/solaris/native/java/lang/ProcessEnvironment_md.c \
		$(openjdk-src)/solaris/native/java/lang/UNIXProcess_md.c \
		$(openjdk-src)/solaris/native/java/net/net_util_md.c \
		$(openjdk-src)/solaris/native/java/net/InetAddressImplFactory.c \
		$(openjdk-src)/solaris/native/java/net/Inet4AddressImpl.c \
		$(openjdk-src)/solaris/native/java/net/Inet6AddressImpl.c \
		$(openjdk-src)/solaris/native/java/net/NetworkInterface.c \
		$(openjdk-src)/solaris/native/java/net/PlainSocketImpl.c \
		$(openjdk-src)/solaris/native/java/net/PlainDatagramSocketImpl.c \
		$(openjdk-src)/solaris/native/java/net/SocketInputStream.c \
		$(openjdk-src)/solaris/native/java/net/SocketOutputStream.c \
		$(openjdk-src)/solaris/native/java/nio/MappedByteBuffer.c \
		$(openjdk-src)/solaris/native/java/util/FileSystemPreferences.c \
		$(openjdk-src)/solaris/native/java/util/logging.c \
		$(openjdk-src)/solaris/native/java/util/TimeZone_md.c \
		$(openjdk-src)/solaris/native/sun/net/dns/ResolverConfigurationImpl.c \
		$(openjdk-src)/solaris/native/sun/net/spi/DefaultProxySelector.c \
		$(openjdk-src)/solaris/native/sun/nio/ch/DatagramChannelImpl.c \
		$(openjdk-src)/solaris/native/sun/nio/ch/DatagramDispatcher.c \
		$(openjdk-src)/solaris/native/sun/nio/ch/FileChannelImpl.c \
		$(openjdk-src)/solaris/native/sun/nio/ch/FileDispatcher.c \
		$(openjdk-src)/solaris/native/sun/nio/ch/FileKey.c \
		$(openjdk-src)/solaris/native/sun/nio/ch/IOUtil.c \
		$(openjdk-src)/solaris/native/sun/nio/ch/Net.c \
		$(openjdk-src)/solaris/native/sun/nio/ch/ServerSocketChannelImpl.c \
		$(openjdk-src)/solaris/native/sun/nio/ch/SocketChannelImpl.c \
		$(openjdk-src)/solaris/native/sun/nio/ch/SocketDispatcher.c \
		$(openjdk-src)/solaris/native/sun/nio/ch/EPollArrayWrapper.c \
		$(openjdk-src)/solaris/native/sun/nio/ch/PollArrayWrapper.c \
		$(openjdk-src)/solaris/native/sun/nio/ch/InheritedChannel.c \
		$(openjdk-src)/solaris/native/sun/nio/ch/NativeThread.c \

	ifeq ($(platform),linux)
		openjdk-sources += \
			$(openjdk-src)/solaris/native/java/net/linux_close.c
	endif

	openjdk-headers-classes += \
		java.net.PlainDatagramSocketImpl \
		java.io.UnixFileSystem \
		sun.nio.ch.InheritedChannel \
		sun.nio.ch.EPollArrayWrapper \

	openjdk-cflags += "-I$(openjdk-src)/solaris/javavm/export" \
		"-I$(openjdk-src)/solaris/native/common" \
		"-I$(openjdk-src)/solaris/native/java/io" \
		"-I$(openjdk-src)/solaris/native/java/lang" \
		"-I$(openjdk-src)/solaris/native/java/net" \
		"-I$(openjdk-src)/solaris/native/java/util" \
		"-I$(openjdk-src)/solaris/native/sun/management" \
		"-I$(openjdk-src)/solaris/native/sun/nio/ch" \
		"-I$(openjdk-src)/solaris/javavm/include" \
		"-I$(openjdk-src)/solaris/hpi/include"
endif

openjdk-local-sources = \
	$(src)/openjdk/my_net_util.c \
	$(src)/openjdk/my_management.c

c-objects = $(foreach x,$(1),$(patsubst $(2)/%.c,$(3)/%-openjdk.o,$(x)))

openjdk-objects = \
	$(call c-objects,$(openjdk-sources),$(openjdk-src),$(build)/openjdk)

openjdk-local-objects = \
	$(call c-objects,$(openjdk-local-sources),$(src)/openjdk,$(build)/openjdk)

openjdk-headers-dep = $(build)/openjdk/headers.dep
