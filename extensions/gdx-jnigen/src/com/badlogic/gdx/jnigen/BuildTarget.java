package com.badlogic.gdx.jnigen;

/**
 * Defines the configuration for building a native shared library for a specific platform. Used
 * with {@link AntScriptGenerator} to create Ant build files that invoke the compiler toolchain to
 * create the shared libraries.
 */
public class BuildTarget {
	/**
	 * The target operating system of a build target.
	 */
	public enum TargetOs {
		Windows,
		Linux,
		MacOsX,
		Android
	}
	
	/** the target operating system **/
	public BuildTarget.TargetOs os;
	/** whether this is a 64-bit build, not used for Android **/
	public boolean is64Bit;
	/** the C files and directories to be included in the build, accepts Ant path format, must not be null **/
	public String[] cIncludes;
	/** the C files and directories to be excluded from the build, accepts Ant path format, must not be null **/
	public String[] cExcludes;
	/** the C++ files and directories to be included in the build, accepts Ant path format, must not be null **/
	public String[] cppIncludes;
	/** the C++ files and directories to be excluded from the build, accepts Ant path format, must not be null **/
	public String[] cppExcludes;
	/** the directories containing headers for the build, must not be null **/
	public String[] headerDirs;
	/** prefix for the compiler (g++, gcc), useful for cross compilation, must not be null **/
	public String compilerPrefix;
	/** the flags passed to the C compiler, must not be null **/
	public String cFlags;
	/** the flags passed to the C++ compiler, must not be null **/
	public String cppFlags;
	/** the flags passed to the linker, must not be null **/
	public String linkerFlags;
	/** the name of the generated build file for this target, defaults to "build-${target}(64)?.xml", must not be null **/
	public String buildFileName;
	/** whether to exclude this build target from the master build file, useful for debugging **/
	public boolean excludeFromMasterBuildFile = false;
	/** Ant XML executed in a target before compilation **/
	public String preCompileTask;
	/** Ant Xml executed in a target after compilation **/
	public String postCompileTask;
	
	/**
	 * Creates a new build target. See members of this class for a description of the parameters.
	 */
	public BuildTarget(BuildTarget.TargetOs targetType, boolean is64Bit, String[] cIncludes, String[] cExcludes, String[] cppIncludes, String[] cppExcludes, String[] headerDirs, String compilerPrefix, String cFlags, String cppFlags, String linkerFlags) {
		if(targetType == null) throw new IllegalArgumentException("targetType must not be null");
		if(cIncludes == null) cIncludes = new String[0];
		if(cExcludes == null) cExcludes = new String[0];
		if(cppIncludes == null) cppIncludes = new String[0];
		if(cppExcludes == null) cppExcludes = new String[0];
		if(headerDirs == null) headerDirs = new String[0];
		if(compilerPrefix == null) compilerPrefix = "";
		if(cFlags == null) cFlags = "";
		if(cppFlags == null) cppFlags = "";
		if(linkerFlags == null) linkerFlags = "";
		
		this.os = targetType;
		this.is64Bit = is64Bit;
		this.cIncludes = cIncludes;
		this.cExcludes = cExcludes;
		this.cppIncludes = cppIncludes;
		this.cppExcludes = cppExcludes;
		this.headerDirs = headerDirs;
		this.compilerPrefix = compilerPrefix;
		this.cFlags = cFlags;
		this.cppFlags = cppFlags;
		this.linkerFlags = linkerFlags;
	}
	
	/**
	 * Creates a new default BuildTarget for the given OS, using common default values. 
	 */
	public static BuildTarget newDefaultTarget(BuildTarget.TargetOs type, boolean is64Bit) {
		if(type == TargetOs.Windows && !is64Bit) {
			// Windows 32-Bit
			return new BuildTarget(TargetOs.Windows, false, 
							new String[] { "**/*.c" }, new String[0], new String[] { "**/*.cpp" }, new String[0], new String[0], 
							"i586-mingw32msvc-", 
							"-c -Wall -O2 -mfpmath=sse -msse2 -fmessage-length=0 -m32", 
							"-c -Wall -O2 -mfpmath=sse -msse2 -fmessage-length=0 -m32",
							"-Wl,--kill-at -shared -m32");
		}
		
		if(type == TargetOs.Windows && is64Bit) {
			// Windows 64-Bit
			return new BuildTarget(TargetOs.Windows, true, 
							new String[] { "**/*.c" }, new String[0], new String[] { "**/*.cpp" }, new String[0], new String[0], 
							"x86_64-w64-mingw32-", 
							"-c -Wall -O2 -mfpmath=sse -msse2 -fmessage-length=0 -m64", 
							"-c -Wall -O2 -mfpmath=sse -msse2 -fmessage-length=0 -m64",
							"-Wl,--kill-at -shared -static-libgcc -static-libstdc++ -m64");
		}
		
		if(type == TargetOs.Linux && !is64Bit) {
			// Linux 32-Bit
			return new BuildTarget(TargetOs.Linux, false, 
							new String[] { "**/*.c" }, new String[0], new String[] { "**/*.cpp" }, new String[0], new String[0], 
							"", 
							"-c -Wall -O2 -mfpmath=sse -msse -fmessage-length=0 -m32 -fPIC", 
							"-c -Wall -O2 -mfpmath=sse -msse -fmessage-length=0 -m32 -fPIC",
							"-shared -m32");
		}
		
		if(type == TargetOs.Linux && is64Bit) {
			// Linux 64-Bit
			return new BuildTarget(TargetOs.Linux, true, 
							new String[] { "**/*.c" }, new String[0], new String[] { "**/*.cpp" }, new String[0], new String[0], 
							"", 
							"-c -Wall -O2 -mfpmath=sse -msse -fmessage-length=0 -m64 -fPIC", 
							"-c -Wall -O2 -mfpmath=sse -msse -fmessage-length=0 -m64 -fPIC",
							"-shared -m64");
		}
		
		if(type == TargetOs.MacOsX) {
			// Mac OS X x86 & x86_64
			BuildTarget mac = new BuildTarget(TargetOs.MacOsX, false, 
							new String[] { "**/*.c" }, new String[0], new String[] { "**/*.cpp" }, new String[0], new String[0],
							"",
							"-c -Wall -O2 -arch i386 -arch x86_64 -DFIXED_POINT -fmessage-length=0 -fPIC -mmacosx-version-min=10.5",
							"-c -Wall -O2 -arch i386 -arch x86_64 -DFIXED_POINT -fmessage-length=0 -fPIC -mmacosx-version-min=10.5",
							"-shared -arch i386 -arch x86_64 -mmacosx-version-min=10.5");
			mac.excludeFromMasterBuildFile = true;
			return mac;
		}
		
		if(type == TargetOs.Android) {
			BuildTarget android = new BuildTarget(TargetOs.Android, false,
												  new String[] { "**/*.c" }, new String[0], new String[] { "**/*.cpp" }, new String[0], new String[0],
												  "",
												  "-O2 -Wall -D__ANDROID__",
												  "-O2 -Wall -D__ANDROID__",
												  "-lm");
			return android;
		}
		
		throw new RuntimeException("Unknown target type");
	}
}