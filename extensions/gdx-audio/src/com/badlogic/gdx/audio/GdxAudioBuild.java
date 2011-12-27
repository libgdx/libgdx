package com.badlogic.gdx.audio;

import com.badlogic.gdx.jnigen.AntScriptGenerator;
import com.badlogic.gdx.jnigen.BuildConfig;
import com.badlogic.gdx.jnigen.BuildExecutor;
import com.badlogic.gdx.jnigen.BuildTarget;
import com.badlogic.gdx.jnigen.BuildTarget.TargetOs;
import com.badlogic.gdx.jnigen.NativeCodeGenerator;

public class GdxAudioBuild {
	public static void main(String[] args) throws Exception {
		new NativeCodeGenerator().generate("src", "bin", "jni");
		
		String[] headerDirs = new String[] { "kissfft", "vorbis" };
		String[] cIncludes = new String[] { "kissfft/*.c", "vorbis/*.c", "mpg123/*.c"};
		String[] cppIncludes = new String[] { "**/*AudioTools.cpp", "**/*KissFFT.cpp", "**/*VorbisDecoder.cpp"};
		
		BuildConfig buildConfig = new BuildConfig("gdxaudio");
		BuildTarget win32home = BuildTarget.newDefaultTarget(TargetOs.Windows, false);
		win32home.compilerPrefix = "";
		win32home.buildFileName = "build-windows32home.xml";
		win32home.headerDirs = headerDirs;
		win32home.cIncludes = cIncludes;
		win32home.cppIncludes = cppIncludes;
		
		BuildTarget win32 = BuildTarget.newDefaultTarget(TargetOs.Windows, false);
		win32.cFlags += "-DFIXED_POINT";
		win32.cppFlags += "-DFIXED_POINT";
		win32.headerDirs = headerDirs;
		win32.cIncludes = cIncludes;
		win32.cppIncludes = cppIncludes;
		
		BuildTarget win64 = BuildTarget.newDefaultTarget(TargetOs.Windows, true);
		win64.cFlags += "-DFIXED_POINT";
		win64.cppFlags += "-DFIXED_POINT";
		win64.headerDirs = headerDirs;
		win64.cIncludes = cIncludes;
		win64.cppIncludes = cppIncludes;
		
		BuildTarget lin32 = BuildTarget.newDefaultTarget(TargetOs.Linux, false);
		lin32.cFlags += "-DFIXED_POINT";
		lin32.cppFlags += "-DFIXED_POINT";
		lin32.headerDirs = headerDirs;
		lin32.cIncludes = cIncludes;
		lin32.cppIncludes = cppIncludes;
		
		BuildTarget lin64 = BuildTarget.newDefaultTarget(TargetOs.Linux, true);
		lin64.cFlags += "-DFIXED_POINT";
		lin64.cppFlags += "-DFIXED_POINT";
		lin64.headerDirs = headerDirs;
		lin64.cIncludes = cIncludes;
		lin64.cppIncludes = cppIncludes;
		
		BuildTarget android = BuildTarget.newDefaultTarget(TargetOs.Android, false);
		android.cFlags += "-DFIXED_POINT -D_ARM_ASSEM_ -D__ANDROID__";
		android.cppFlags += "-DFIXED_POINT -D_ARM_ASSEM_ -D__ANDROID__";
		android.headerDirs = headerDirs;
		android.cIncludes = cIncludes;
		android.cppIncludes = cppIncludes;
		
		new AntScriptGenerator().generate(buildConfig, win32home, win32, win64, lin32, lin64, android);
		
//		BuildExecutor.executeAnt("jni/build-windows32home.xml", "clean");
		BuildExecutor.executeAnt("jni/build-windows32home.xml", "-v");
	}
}
