package com.badlogic.gdx.jnigen;

public class BuildTarget {
	public enum TargetOs {
		Windows,
		Linux,
		MacOsX,
		Android
	}
	
	public BuildTarget.TargetOs os;
	public boolean is64Bit;
	public String[] cIncludes;
	public String[] cExcludes;
	public String[] cppIncludes;
	public String[] cppExcludes;
	public String[] headerDirs;
	public String compilerPrefix;
	public String cFlags;
	public String cppFlags;
	public String linkerFlags;
	public String buildFileName;
	public boolean excludeFromMasterBuildFile = false;
	
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