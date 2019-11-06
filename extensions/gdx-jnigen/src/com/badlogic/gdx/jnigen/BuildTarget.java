/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.jnigen;

/** Defines the configuration for building a native shared library for a specific platform. Used with {@link AntScriptGenerator}
 * to create Ant build files that invoke the compiler toolchain to create the shared libraries. */
public class BuildTarget {
	/** The target operating system of a build target. */
	public enum TargetOs {
		Windows, Linux, MacOsX, Android, IOS
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
	/** the libraries to be linked to the output, specify via e.g. -ldinput -ldxguid etc. **/
	public String libraries;
	/** The name used for folders for this specific target. Defaults to "${target}(64)" **/
	public String osFileName;
	/** The name used for the library file. This is a full file name, including file extension. Default is platform specific. E.g.
	 * "lib{sharedLibName}64.so" **/
	public String libName;

	/** Creates a new build target. See members of this class for a description of the parameters. */
	public BuildTarget (BuildTarget.TargetOs targetType, boolean is64Bit, String[] cIncludes, String[] cExcludes,
		String[] cppIncludes, String[] cppExcludes, String[] headerDirs, String compilerPrefix, String cFlags, String cppFlags,
		String linkerFlags) {
		if (targetType == null) throw new IllegalArgumentException("targetType must not be null");
		if (cIncludes == null) cIncludes = new String[0];
		if (cExcludes == null) cExcludes = new String[0];
		if (cppIncludes == null) cppIncludes = new String[0];
		if (cppExcludes == null) cppExcludes = new String[0];
		if (headerDirs == null) headerDirs = new String[0];
		if (compilerPrefix == null) compilerPrefix = "";
		if (cFlags == null) cFlags = "";
		if (cppFlags == null) cppFlags = "";
		if (linkerFlags == null) linkerFlags = "";

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
		this.libraries = "";
	}

	/** Creates a new default BuildTarget for the given OS, using common default values. */
	public static BuildTarget newDefaultTarget (BuildTarget.TargetOs type, boolean is64Bit) {
		if (type == TargetOs.Windows && !is64Bit) {
			// Windows 32-Bit
			return new BuildTarget(TargetOs.Windows, false, new String[] {"**/*.c"}, new String[0], new String[] {"**/*.cpp"},
				new String[0], new String[0], "i686-w64-mingw32-", "-c -Wall -O2 -mfpmath=sse -msse2 -fmessage-length=0 -m32",
				"-c -Wall -O2 -mfpmath=sse -msse2 -fmessage-length=0 -m32",
				"-Wl,--kill-at -shared -m32 -static -static-libgcc -static-libstdc++");
		}

		if (type == TargetOs.Windows && is64Bit) {
			// Windows 64-Bit
			return new BuildTarget(TargetOs.Windows, true, new String[] {"**/*.c"}, new String[0], new String[] {"**/*.cpp"},
				new String[0], new String[0], "x86_64-w64-mingw32-", "-c -Wall -O2 -mfpmath=sse -msse2 -fmessage-length=0 -m64",
				"-c -Wall -O2 -mfpmath=sse -msse2 -fmessage-length=0 -m64",
				"-Wl,--kill-at -shared -static -static-libgcc -static-libstdc++ -m64");
		}

		if (type == TargetOs.Linux && !is64Bit) {
			// Linux 32-Bit
			return new BuildTarget(TargetOs.Linux, false, new String[] {"**/*.c"}, new String[0], new String[] {"**/*.cpp"},
				new String[0], new String[0], "", "-c -Wall -O2 -mfpmath=sse -msse -fmessage-length=0 -m32 -fPIC",
				"-c -Wall -O2 -mfpmath=sse -msse -fmessage-length=0 -m32 -fPIC", "-shared -m32");
		}

		if (type == TargetOs.Linux && is64Bit) {
			// Linux 64-Bit
			return new BuildTarget(TargetOs.Linux, true, new String[] {"**/*.c"}, new String[0], new String[] {"**/*.cpp"},
				new String[0], new String[0], "", "-c -Wall -O2 -mfpmath=sse -msse -fmessage-length=0 -m64 -fPIC",
				"-c -Wall -O2 -mfpmath=sse -msse -fmessage-length=0 -m64 -fPIC", "-shared -m64 -Wl,-wrap,memcpy");
		}

		if (type == TargetOs.MacOsX && !is64Bit) {
			throw new RuntimeException("macOS 32-bit not supported");
		}

		if (type == TargetOs.MacOsX && is64Bit) {
			// Mac OS X x86 & x86_64
			BuildTarget mac = new BuildTarget(TargetOs.MacOsX, true, new String[] {"**/*.c"}, new String[0],
				new String[] {"**/*.cpp"}, new String[0], new String[0], "",
				"-c -Wall -O2 -arch x86_64 -DFIXED_POINT -fmessage-length=0 -fPIC -mmacosx-version-min=10.7 -stdlib=libc++",
				"-c -Wall -O2 -arch x86_64 -DFIXED_POINT -fmessage-length=0 -fPIC -mmacosx-version-min=10.7 -stdlib=libc++",
				"-shared -arch x86_64 -mmacosx-version-min=10.7 -stdlib=libc++");
			return mac;
		}

		if (type == TargetOs.Android) {
			BuildTarget android = new BuildTarget(TargetOs.Android, false, new String[] {"**/*.c"}, new String[0],
				new String[] {"**/*.cpp"}, new String[0], new String[0], "", "-O2 -Wall -D__ANDROID__", "-O2 -Wall -D__ANDROID__",
				"-lm");
			return android;
		}

		if (type == TargetOs.IOS) {
			// iOS, 386 simulator and armv7a, compiled to fat static lib
			BuildTarget ios = new BuildTarget(TargetOs.IOS, false, new String[] {"**/*.c"}, new String[0], new String[] {"**/*.cpp"},
				new String[0], new String[0], "", "-c -Wall -O2", "-c -Wall -O2", "rcs");
			return ios;
		}

		throw new RuntimeException("Unknown target type");
	}
}
