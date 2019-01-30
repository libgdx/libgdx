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

import java.util.ArrayList;

import com.badlogic.gdx.jnigen.BuildTarget.TargetOs;
import com.badlogic.gdx.jnigen.FileDescriptor.FileType;

/** Generates Ant scripts for multiple native build targets based on the given {@link BuildConfig}.</p>
 * 
 * For each build target, an Ant build script is created that will compile C/C++ files to a shared library for a specific
 * platform. A master build script is generated that will execute the build scripts for each platform and bundles their shared
 * libraries into a Jar file containing all shared libraries for all desktop platform targets, and armeabi/ and armeabi-v7a/
 * folders containing the shard libraries for Android. The scripts can be executed from the command line or via the
 * {@link BuildExecutor}. The resulting shared libraries can be loaded with the {@link JniGenSharedLibraryLoader} which will load
 * the correct shared library from the natives jar/arm folders based on the platform the application is running on</p>
 * 
 * A common use case looks like this:
 * 
 * <pre>
 * BuildTarget win32 = BuildTarget.newDefaultBuildTarget(TargetOs.Windows, false);
 * BuildTarget win64 = BuildTarget.newDefaultBuildTarget(TargetOs.Windows, true);
 * BuildTarget linux32 = BuildTarget.newDefaultBuildTarget(TargetOs.Linux, false);
 * BuildTarget linux64 = BuildTarget.newDefaultBuildTarget(TargetOs.Linux, true);
 * BuildTarget mac = BuildTarget.newDefaultBuildTarget(TargetOs.MacOsX, false);
 * BuildTarget android = BuildTarget.newDefaultBuildTarget(TargetOs.Android, false);
 * BuildConfig config = new BuildConfig("mysharedlibrary");
 * 
 * new AntScriptGenerator().generate(config, win32, win64, linux32, linux64, mac, android);
 * BuildExecutor.executeAnt("jni/build.xml", "clean all -v");
 * 
 * // assuming the natives jar is on the classpath of the application 
 * new SharedLibraryLoader().load("mysharedlibrary)
 * </pre>
 * 
 * This will create the build scripts and execute the build of the shared libraries for each platform, provided that the compilers
 * are available on the system. Mac OS X might have to be treated separately as there are no cross-compilers for it.</p>
 * 
 * The generator will also copy the necessary JNI headers to the jni/jni-headers folder for Windows, Linux and Mac OS X.</p>
 * 
 * @author mzechner */
public class AntScriptGenerator {
	/** Creates a master build script and one build script for each target to generated native shared libraries.
	 * @param config the {@link BuildConfig}
	 * @param targets list of {@link BuildTarget} instances */
	public void generate (BuildConfig config, BuildTarget... targets) {
		// create all the directories for outputing object files, shared libs and natives jar as well as build scripts.
		if (!config.libsDir.exists()) {
			if (!config.libsDir.mkdirs())
				throw new RuntimeException("Couldn't create directory for shared library files in '" + config.libsDir + "'");
		}
		if (!config.jniDir.exists()) {
			if (!config.jniDir.mkdirs())
				throw new RuntimeException("Couldn't create native code directory '" + config.jniDir + "'");
		}

		// copy jni headers
		copyJniHeaders(config.jniDir.path());

		// copy memcpy_wrap.c, needed if your build platform uses the latest glibc, e.g. Ubuntu 12.10
		if (config.jniDir.child("memcpy_wrap.c").exists() == false) {
			new FileDescriptor("com/badlogic/gdx/jnigen/resources/scripts/memcpy_wrap.c", FileType.Classpath).copyTo(config.jniDir
				.child("memcpy_wrap.c"));
		}

		ArrayList<String> buildFiles = new ArrayList<String>();
		ArrayList<String> libsDirs = new ArrayList<String>();
		ArrayList<String> sharedLibFiles = new ArrayList<String>();

		// generate an Ant build script for each target
		for (BuildTarget target : targets) {
			String buildFile = generateBuildTargetTemplate(config, target);
			FileDescriptor libsDir = new FileDescriptor(getLibsDirectory(config, target));

			if (!libsDir.exists()) {
				if (!libsDir.mkdirs()) throw new RuntimeException("Couldn't create libs directory '" + libsDir + "'");
			}

			String buildFileName = "build-" + target.os.toString().toLowerCase() + (target.is64Bit ? "64" : "32") + ".xml";
			if (target.buildFileName != null) buildFileName = target.buildFileName;
			config.jniDir.child(buildFileName).writeString(buildFile, false);
			System.out.println("Wrote target '" + target.os + (target.is64Bit ? "64" : "") + "' build script '"
				+ config.jniDir.child(buildFileName) + "'");

			if (!target.excludeFromMasterBuildFile) {
				if (target.os != TargetOs.MacOsX && target.os != TargetOs.IOS) {
					buildFiles.add(buildFileName);
				}

				String sharedLibFilename = target.libName;
				if (sharedLibFilename == null)
					sharedLibFilename = getSharedLibFilename(target.os, target.is64Bit, config.sharedLibName);
				
				sharedLibFiles.add(sharedLibFilename);
				if (target.os != TargetOs.Android && target.os != TargetOs.IOS) {
					libsDirs.add("../" + libsDir.path().replace('\\', '/'));
				}
			}
		}

		// generate the master build script
		String template = new FileDescriptor("com/badlogic/gdx/jnigen/resources/scripts/build.xml.template", FileType.Classpath)
			.readString();
		StringBuilder clean = new StringBuilder();
		StringBuilder compile = new StringBuilder();
		StringBuilder pack = new StringBuilder();

		for (int i = 0; i < buildFiles.size(); i++) {
			clean.append("\t\t<ant antfile=\"" + buildFiles.get(i) + "\" target=\"clean\"/>\n");
			compile.append("\t\t<ant antfile=\"" + buildFiles.get(i) + "\"/>\n");
		}
		for (int i = 0; i < libsDirs.size(); i++) {
			pack.append("\t\t\t<fileset dir=\"" + libsDirs.get(i) + "\" includes=\"" + sharedLibFiles.get(i) + "\"/>\n");
		}

		if (config.sharedLibs != null) {
			for (String sharedLib : config.sharedLibs) {
				pack.append("\t\t\t<fileset dir=\"" + sharedLib + "\"/>\n");
			}
		}

		template = template.replace("%projectName%", config.sharedLibName + "-natives");
		template = template.replace("<clean/>", clean.toString());
		template = template.replace("<compile/>", compile.toString());
		template = template.replace("%packFile%", "../" + config.libsDir.path().replace('\\', '/') + "/" + config.sharedLibName
			+ "-natives.jar");
		template = template.replace("<pack/>", pack);

		config.jniDir.child("build.xml").writeString(template, false);
		System.out.println("Wrote master build script '" + config.jniDir.child("build.xml") + "'");
	}

	private void copyJniHeaders (String jniDir) {
		final String pack = "com/badlogic/gdx/jnigen/resources/headers";
		String files[] = {"classfile_constants.h", "jawt.h", "jdwpTransport.h", "jni.h", "linux/jawt_md.h", "linux/jni_md.h",
			"mac/jni_md.h", "win32/jawt_md.h", "win32/jni_md.h"};

		for (String file : files) {
			new FileDescriptor(pack, FileType.Classpath).child(file).copyTo(
				new FileDescriptor(jniDir).child("jni-headers").child(file));
		}
	}

	private String getSharedLibFilename (TargetOs os, boolean is64Bit, String sharedLibName) {
		// generate shared lib prefix and suffix, determine jni platform headers directory
		String libPrefix = "";
		String libSuffix = "";
		if (os == TargetOs.Windows) {
			libSuffix = (is64Bit ? "64" : "") + ".dll";
		}
		if (os == TargetOs.Linux || os == TargetOs.Android) {
			libPrefix = "lib";
			libSuffix = (is64Bit ? "64" : "") + ".so";
		}
		if (os == TargetOs.MacOsX) {
			libPrefix = "lib";
			libSuffix = (is64Bit ? "64" : "") + ".dylib";
		}
		if (os == TargetOs.IOS) {
			libPrefix = "lib";
			libSuffix = ".a";
		}
		return libPrefix + sharedLibName + libSuffix;
	}

	private String getJniPlatform (TargetOs os) {
		if (os == TargetOs.Windows) return "win32";
		if (os == TargetOs.Linux) return "linux";
		if (os == TargetOs.MacOsX) return "mac";
		return "";
	}

	private String getLibsDirectory (BuildConfig config, BuildTarget target) {
		String targetName = target.osFileName;
		if (targetName == null) targetName = target.os.toString().toLowerCase() + (target.is64Bit ? "64" : "32");
		return config.libsDir.child(targetName).path().replace('\\', '/');
	}

	private String generateBuildTargetTemplate (BuildConfig config, BuildTarget target) {
		// special case for android
		if (target.os == TargetOs.Android) {
			new AndroidNdkScriptGenerator().generate(config, target);
			String template = new FileDescriptor("com/badlogic/gdx/jnigen/resources/scripts/build-android.xml.template",
				FileType.Classpath).readString();
			template = template.replace("%precompile%", target.preCompileTask == null ? "" : target.preCompileTask);
			template = template.replace("%postcompile%", target.postCompileTask == null ? "" : target.postCompileTask);
			return template;
		}

		// read template file from resources
		String template = null;
		if (target.os == TargetOs.IOS) {
			template = new FileDescriptor("com/badlogic/gdx/jnigen/resources/scripts/build-ios.xml.template", FileType.Classpath)
				.readString();
		} else {
			template = new FileDescriptor("com/badlogic/gdx/jnigen/resources/scripts/build-target.xml.template", FileType.Classpath)
				.readString();
		}

		// generate shared lib filename and jni platform headers directory name
		String libName = target.libName;
		if (libName == null) libName = getSharedLibFilename(target.os, target.is64Bit, config.sharedLibName);
		String jniPlatform = getJniPlatform(target.os);

		// generate include and exclude fileset Ant description for C/C++
		// append memcpy_wrap.c to list of files to be build
		StringBuilder cIncludes = new StringBuilder();
		cIncludes.append("\t\t<include name=\"memcpy_wrap.c\"/>\n");
		for (String cInclude : target.cIncludes) {
			cIncludes.append("\t\t<include name=\"" + cInclude + "\"/>\n");
		}
		StringBuilder cppIncludes = new StringBuilder();
		for (String cppInclude : target.cppIncludes) {
			cppIncludes.append("\t\t<include name=\"" + cppInclude + "\"/>\n");
		}
		StringBuilder cExcludes = new StringBuilder();
		for (String cExclude : target.cExcludes) {
			cExcludes.append("\t\t<exclude name=\"" + cExclude + "\"/>\n");
		}
		StringBuilder cppExcludes = new StringBuilder();
		for (String cppExclude : target.cppExcludes) {
			cppExcludes.append("\t\t<exclude name=\"" + cppExclude + "\"/>\n");
		}

		// generate C/C++ header directories
		StringBuilder headerDirs = new StringBuilder();
		for (String headerDir : target.headerDirs) {
			headerDirs.append("\t\t\t<arg value=\"-I" + headerDir + "\"/>\n");
		}

		String targetFolder = target.osFileName;
		if (targetFolder == null) targetFolder = target.os.toString().toLowerCase() + (target.is64Bit ? "64" : "32");

		// replace template vars with proper values
		template = template.replace("%projectName%", config.sharedLibName + "-" + target.os + "-" + (target.is64Bit ? "64" : "32"));
		template = template.replace("%buildDir%", config.buildDir.child(targetFolder).path().replace('\\', '/'));
		template = template.replace("%libsDir%", "../" + getLibsDirectory(config, target));
		template = template.replace("%libName%", libName);
		template = template.replace("%jniPlatform%", jniPlatform);
		template = template.replace("%compilerPrefix%", target.compilerPrefix);
		template = template.replace("%cFlags%", target.cFlags);
		template = template.replace("%cppFlags%", target.cppFlags);
		template = template.replace("%linkerFlags%", target.linkerFlags);
		template = template.replace("%libraries%", target.libraries);
		template = template.replace("%cIncludes%", cIncludes);
		template = template.replace("%cExcludes%", cExcludes);
		template = template.replace("%cppIncludes%", cppIncludes);
		template = template.replace("%cppExcludes%", cppExcludes);
		template = template.replace("%headerDirs%", headerDirs);
		template = template.replace("%precompile%", target.preCompileTask == null ? "" : target.preCompileTask);
		template = template.replace("%postcompile%", target.postCompileTask == null ? "" : target.postCompileTask);

		return template;
	}
}
