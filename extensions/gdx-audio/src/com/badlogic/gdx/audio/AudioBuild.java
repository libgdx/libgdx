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

package com.badlogic.gdx.audio;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.jnigen.AntScriptGenerator;
import com.badlogic.gdx.jnigen.BuildConfig;
import com.badlogic.gdx.jnigen.BuildTarget;
import com.badlogic.gdx.jnigen.BuildTarget.TargetOs;
import com.badlogic.gdx.jnigen.NativeCodeGenerator;

public class AudioBuild {
	public static void main (String[] args) throws Exception {
		new NativeCodeGenerator().generate("src", "bin", "jni", new String[] {"**/AudioTools.java", "**/KissFFT.java",
			"**/VorbisDecoder.java", "**/Mpg123Decoder.java", "**/SoundTouch.java"}, null);

		String[] headerDirs = new String[] {"kissfft", "vorbis", "soundtouch/include", "soundtouch/source/SoundTouch/"};
		String[] cIncludes = new String[] {"kissfft/*.c", "vorbis/*.c", "libmpg123/equalizer.c", "libmpg123/index.c",
			"libmpg123/layer2.c", "libmpg123/synth.c", "libmpg123/dct64.c", "libmpg123/format.c", "libmpg123/layer3.c",
			"libmpg123/ntom.c", "libmpg123/parse.c", "libmpg123/readers.c", "libmpg123/frame.c", "libmpg123/layer1.c",
			"libmpg123/libmpg123.c", "libmpg123/optimize.c", "libmpg123/synth_arm.S", "libmpg123/tabinit.c", "libmpg123/id3.c",
			"libmpg123/stringbuf.c", "libmpg123/icy.c", "libmpg123/icy2utf8.c", "libmpg123/compat.c", "libmpg123/synth_8bit.c",
			"libmpg123/synth_real.c", "libmpg123/synth_s32.c",};
		String[] cppIncludes = new String[] {"**/*AudioTools.cpp", "**/*KissFFT.cpp", "**/*VorbisDecoder.cpp",
			"**/*SoundTouch.cpp", "**/*Mpg123Decoder.cpp", "soundtouch/source/SoundTouch/*.cpp"};
		String[] cppExcludes = new String[] {"**/cpu_detect_x86_win.cpp"};
		String precompileTask = "<copy failonerror=\"true\" tofile=\"soundtouch/include/STTypes.h\" verbose=\"true\" overwrite=\"true\" file=\"STTypes.h.patched\"/>";
		String cFlags = " -DFIXED_POINT -DMPG123_NO_CONFIGURE -DOPT_GENERIC -DHAVE_STRERROR -DMPG123_NO_LARGENAME";
		String cppFlags = " -DFIXED_POINT -DMPG123_NO_CONFIGURE -DOPT_GENERIC -DHAVE_STRERROR -DMPG123_NO_LARGENAME";
		BuildConfig buildConfig = new BuildConfig("gdx-audio");
		BuildTarget win32home = BuildTarget.newDefaultTarget(TargetOs.Windows, false);
		win32home.cFlags += cFlags;
		win32home.cppFlags += cppFlags;
		win32home.compilerPrefix = "";
		win32home.buildFileName = "build-windows32home.xml";
		win32home.headerDirs = headerDirs;
		win32home.cIncludes = cIncludes;
		win32home.cppIncludes = cppIncludes;
		win32home.cppExcludes = cppExcludes;
		win32home.excludeFromMasterBuildFile = true;
		win32home.preCompileTask = precompileTask;

		BuildTarget win32 = BuildTarget.newDefaultTarget(TargetOs.Windows, false);
		win32.cFlags += cFlags;
		win32.cppFlags += cppFlags;
		win32.headerDirs = headerDirs;
		win32.cIncludes = cIncludes;
		win32.cppIncludes = cppIncludes;
		win32.cppExcludes = cppExcludes;
		win32.preCompileTask = precompileTask;

		BuildTarget win64 = BuildTarget.newDefaultTarget(TargetOs.Windows, true);
		win64.cFlags += cFlags;
		win64.cppFlags += cppFlags;
		win64.headerDirs = headerDirs;
		win64.cIncludes = cIncludes;
		win64.cppIncludes = cppIncludes;
		win64.cppExcludes = cppExcludes;
		win64.preCompileTask = precompileTask;

		BuildTarget lin32 = BuildTarget.newDefaultTarget(TargetOs.Linux, false);
		lin32.cFlags += cFlags;
		lin32.cppFlags += cppFlags;
		lin32.headerDirs = headerDirs;
		lin32.cIncludes = cIncludes;
		lin32.cppIncludes = cppIncludes;
		lin32.cppExcludes = cppExcludes;
		lin32.preCompileTask = precompileTask;

		BuildTarget lin64 = BuildTarget.newDefaultTarget(TargetOs.Linux, true);
		lin64.cFlags += cFlags;
		lin64.cppFlags += cppFlags;
		lin64.headerDirs = headerDirs;
		lin64.cIncludes = cIncludes;
		lin64.cppIncludes = cppIncludes;
		lin64.cppExcludes = cppExcludes;
		lin64.preCompileTask = precompileTask;

		BuildTarget mac = BuildTarget.newDefaultTarget(TargetOs.MacOsX, false);
		mac.cFlags += cFlags;
		mac.cppFlags += cppFlags;
		mac.headerDirs = headerDirs;
		mac.cIncludes = cIncludes;
		mac.cppIncludes = cppIncludes;
		mac.cppExcludes = cppExcludes;
		mac.preCompileTask = precompileTask;
		
		BuildTarget mac64 = BuildTarget.newDefaultTarget(TargetOs.MacOsX, true);
		mac64.cFlags += cFlags;
		mac64.cppFlags += cppFlags;
		mac64.headerDirs = headerDirs;
		mac64.cIncludes = cIncludes;
		mac64.cppIncludes = cppIncludes;
		mac64.cppExcludes = cppExcludes;
		mac64.preCompileTask = precompileTask;

		BuildTarget android = BuildTarget.newDefaultTarget(TargetOs.Android, false);
		android.cFlags += " -DFIXED_POINT -D_ARM_ASSEM_ -D__ANDROID__ -DMPG123_NO_CONFIGURE -DOPT_GENERIC -DHAVE_STRERROR -DMPG123_NO_LARGENAME -DASMALIGN_BYTE";
		android.cppFlags += " -DFIXED_POINT -D_ARM_ASSEM_ -D__ANDROID__ -DMPG123_NO_CONFIGURE -DOPT_GENERIC -DHAVE_STRERROR -DMPG123_NO_LARGENAME -DASMALIGN_BYTE";
		android.headerDirs = headerDirs;
		android.cIncludes = cIncludes;
		android.cppIncludes = cppIncludes;
		android.cppExcludes = cppExcludes;
		android.preCompileTask = precompileTask;

		new AntScriptGenerator().generate(buildConfig, win32home, win32, win64, lin32, lin64, mac, mac64, android);
		// we don't support x86 for the audio extension on Android. Dirty hack that disables x86 build...
		String androidMakeFile = new FileHandle("jni/Application.mk").readString();
		new FileHandle("jni/Application.mk").writeString(androidMakeFile.replaceAll("x86", ""), false);

// BuildExecutor.executeAnt("jni/build-linux64.xml", "clean postcompile -v");
// BuildExecutor.executeAnt("jni/build.xml", "pack-natives -v");
	}
}
