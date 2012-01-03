package com.badlogic.gdx.utils;

import com.badlogic.gdx.jnigen.AntScriptGenerator;
import com.badlogic.gdx.jnigen.BuildConfig;
import com.badlogic.gdx.jnigen.BuildExecutor;
import com.badlogic.gdx.jnigen.BuildTarget;
import com.badlogic.gdx.jnigen.NativeCodeGenerator;
import com.badlogic.gdx.jnigen.JniGenSharedLibraryLoader;
import com.badlogic.gdx.jnigen.BuildTarget.TargetOs;

/**
 * Builds the JNI wrappers via gdx-jnigen.
 * @author mzechner
 *
 */
public class GdxBuild {
	public static void main(String[] args) throws Exception {
		String JNI_DIR = "jni";
		String LIBS_DIR = "libs";
		
//		// MD5Jni
//		String[] includes = { "**/MD5Jni.java" };
//		new NativeCodeGenerator().generate("src", "bin", JNI_DIR + "/", includes, null);
//		
//		// Matrix4
//		includes = new String[] { "**/Matrix4.java" };
//		new NativeCodeGenerator().generate("src", "bin", JNI_DIR + "/", includes, null);
//		
//		// ETC1
//		includes = new String[] { "**/ETC1.java" };
//		new NativeCodeGenerator().generate("src", "bin", JNI_DIR + "/etc1/", includes, null);
//		
//		// GDX2D
//		includes = new String[] { "**/Gdx2DPixmap.java" };
//		new NativeCodeGenerator().generate("src", "bin", JNI_DIR + "/gdx2d/", includes, null);
//		
//		// Box2D
//		includes = new String[] { "**/box2d/**"};
//		new NativeCodeGenerator().generate("src", "bin", JNI_DIR + "/Box2D/", includes, null);
//		
//		new NativeCodeGenerator().generate("src", "bin", JNI_DIR, new String[] { "**/*" }, null);
//
//		// build
//		String[] headerDirs = { "./", "etc1/", "gdx2d/", "Box2D/" };
//		BuildConfig config = new BuildConfig("gdx", "../target/native", LIBS_DIR, JNI_DIR);
//		BuildTarget target = BuildTarget.newDefaultTarget(TargetOs.Windows, false);
//		target.compilerPrefix = "";
//		target.excludeFromMasterBuildFile = true;
//		target.headerDirs = headerDirs;
//		
//		new AntScriptGenerator().generate(config, target);
//		BuildExecutor.executeAnt(JNI_DIR + "/build-windows32.xml", "");
		
		// generate C/C++ code
		new NativeCodeGenerator().generate("src", "bin", JNI_DIR, new String[] { "**/*" }, null);
		
		// generate build scripts, for win32 only
		// custom target for testing purposes
		BuildTarget win32home = BuildTarget.newDefaultTarget(TargetOs.Windows, false);
		win32home.compilerPrefix = "";
		win32home.buildFileName = "build-windows32home.xml";
		win32home.excludeFromMasterBuildFile = true;
		BuildTarget win32 = BuildTarget.newDefaultTarget(TargetOs.Windows, false);
		BuildTarget win64 = BuildTarget.newDefaultTarget(TargetOs.Windows, true);
		BuildTarget lin32 = BuildTarget.newDefaultTarget(TargetOs.Linux, false);
		BuildTarget lin64 = BuildTarget.newDefaultTarget(TargetOs.Linux, true);
		BuildTarget android = BuildTarget.newDefaultTarget(TargetOs.Android, false);
		BuildTarget mac = BuildTarget.newDefaultTarget(TargetOs.MacOsX, false);
		new AntScriptGenerator().generate(new BuildConfig("gdx", "../target/native", LIBS_DIR, JNI_DIR), mac, win32home, win32, win64, lin32, lin64, android);
		
		// build natives
		BuildExecutor.executeAnt("jni/build-windows32home.xml", "-v");
		BuildExecutor.executeAnt("jni/build.xml", "pack-natives -v");
	}
}
