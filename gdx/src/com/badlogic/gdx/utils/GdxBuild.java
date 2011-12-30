package com.badlogic.gdx.utils;

import com.badlogic.gdx.jnigen.AntScriptGenerator;
import com.badlogic.gdx.jnigen.BuildConfig;
import com.badlogic.gdx.jnigen.BuildExecutor;
import com.badlogic.gdx.jnigen.BuildTarget;
import com.badlogic.gdx.jnigen.NativeCodeGenerator;
import com.badlogic.gdx.jnigen.SharedLibraryLoader;
import com.badlogic.gdx.jnigen.BuildTarget.TargetOs;

/**
 * Builds the JNI wrappers via gdx-jnigen.
 * @author mzechner
 *
 */
public class GdxBuild {
	public static void main(String[] args) throws Exception {
		String JNI_DIR = "jni-new";
		String LIBS_DIR = "libs-new";
		
		// MD5Jni
		String[] includes = { "**/MD5Jni.java" };
		new NativeCodeGenerator().generate("src", "bin", JNI_DIR + "/", includes, null);
		
		// Matrix4
		includes = new String[] { "**/Matrix4.java" };
		new NativeCodeGenerator().generate("src", "bin", JNI_DIR + "/", includes, null);
		
		// ETC1
		includes = new String[] { "**/ETC1.java" };
		new NativeCodeGenerator().generate("src", "bin", JNI_DIR + "/etc1/", includes, null);
		
		// GDX2D
		includes = new String[] { "**/Gdx2DPixmap.java" };
		new NativeCodeGenerator().generate("src", "bin", JNI_DIR + "/gdx2d/", includes, null);

		// build
		String[] headerDirs = { "./", "etc1/", "gdx2d/" };
		BuildConfig config = new BuildConfig("gdx", "native", LIBS_DIR, JNI_DIR);
		BuildTarget target = BuildTarget.newDefaultTarget(TargetOs.Windows, false);
		target.compilerPrefix = "";
		target.excludeFromMasterBuildFile = true;
		target.headerDirs = headerDirs;
		
		new AntScriptGenerator().generate(config, target);
		BuildExecutor.executeAnt(JNI_DIR + "/build-windows32.xml", "clean link -v");
	}
}
