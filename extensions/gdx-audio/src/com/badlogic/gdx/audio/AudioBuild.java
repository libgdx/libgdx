package com.badlogic.gdx.audio;

import com.badlogic.gdx.jnigen.AntScriptGenerator;
import com.badlogic.gdx.jnigen.BuildConfig;
import com.badlogic.gdx.jnigen.BuildExecutor;
import com.badlogic.gdx.jnigen.BuildTarget;
import com.badlogic.gdx.jnigen.BuildTarget.TargetOs;
import com.badlogic.gdx.jnigen.NativeCodeGenerator;

public class AudioBuild {
	public static void main(String[] args) throws Exception {
		new NativeCodeGenerator().generate("src", "bin", "jni", 
										   new String[] { "**/AudioTools.java", "**/KissFFT.java", "**/VorbisDecoder.java", "**/SoundTouch.java" }, 
										   new String[] { "**/Mpg123Decoder.java" });
		
		String[] headerDirs = new String[] { "kissfft", "vorbis", "soundtouch/include", "soundtouch/source/SoundTouch/" };
		String[] cIncludes = new String[] { 
											"kissfft/*.c", 
											"vorbis/*.c", 
		};
		String[] cppIncludes = new String[] { 
											  "**/*AudioTools.cpp", 
											  "**/*KissFFT.cpp", 
											  "**/*VorbisDecoder.cpp",
											  "**/*SoundTouch.cpp",
											  "soundtouch/source/SoundTouch/*.cpp"
		};
		String[] cppExcludes = new String[] { "**/cpu_detect_x86_win.cpp" };
		String precompileTask = "<copy failonerror=\"true\" tofile=\"soundtouch/include/STTypes.h\" verbose=\"true\" overwrite=\"true\" file=\"STTypes.h.patched\"/>";
		BuildConfig buildConfig = new BuildConfig("gdx-audio");
		BuildTarget win32home = BuildTarget.newDefaultTarget(TargetOs.Windows, false);
		win32home.compilerPrefix = "";
		win32home.buildFileName = "build-windows32home.xml";
		win32home.headerDirs = headerDirs;
		win32home.cIncludes = cIncludes;
		win32home.cppIncludes = cppIncludes;
		win32home.cppExcludes = cppExcludes;
		win32home.excludeFromMasterBuildFile = true;
		win32home.preCompileTask = precompileTask;
		
		BuildTarget win32 = BuildTarget.newDefaultTarget(TargetOs.Windows, false);
		win32.cFlags += " -DFIXED_POINT";
		win32.cppFlags += " -DFIXED_POINT";
		win32.headerDirs = headerDirs;
		win32.cIncludes = cIncludes;
		win32.cppIncludes = cppIncludes;
		win32.cppExcludes = cppExcludes;
		win32.preCompileTask = precompileTask;
		
		BuildTarget win64 = BuildTarget.newDefaultTarget(TargetOs.Windows, true);
		win64.cFlags += " -DFIXED_POINT";
		win64.cppFlags += " -DFIXED_POINT";
		win64.headerDirs = headerDirs;
		win64.cIncludes = cIncludes;
		win64.cppIncludes = cppIncludes;
		win64.cppExcludes = cppExcludes;
		win64.preCompileTask = precompileTask;
		
		BuildTarget lin32 = BuildTarget.newDefaultTarget(TargetOs.Linux, false);
		lin32.cFlags += " -DFIXED_POINT";
		lin32.cppFlags += " -DFIXED_POINT";
		lin32.headerDirs = headerDirs;
		lin32.cIncludes = cIncludes;
		lin32.cppIncludes = cppIncludes;
		lin32.cppExcludes = cppExcludes;
		lin32.preCompileTask = precompileTask;
		
		BuildTarget lin64 = BuildTarget.newDefaultTarget(TargetOs.Linux, true);
		lin64.cFlags += " -DFIXED_POINT";
		lin64.cppFlags += " -DFIXED_POINT";
		lin64.headerDirs = headerDirs;
		lin64.cIncludes = cIncludes;
		lin64.cppIncludes = cppIncludes;
		lin64.cppExcludes = cppExcludes;
		lin64.preCompileTask = precompileTask;
		
		BuildTarget android = BuildTarget.newDefaultTarget(TargetOs.Android, false);
		android.cFlags += " -DFIXED_POINT -D_ARM_ASSEM_ -D__ANDROID__";
		android.cppFlags += " -DFIXED_POINT -D_ARM_ASSEM_ -D__ANDROID__";
		android.headerDirs = headerDirs;
		android.cIncludes = cIncludes;
		android.cppIncludes = cppIncludes;
		android.cppExcludes = cppExcludes;
		android.preCompileTask = precompileTask;
		
		new AntScriptGenerator().generate(buildConfig, win32home, win32, win64, lin32, lin64, android);
		
		BuildExecutor.executeAnt("jni/build-windows32home.xml", " -v");
	}
}
