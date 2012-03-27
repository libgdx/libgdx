
package com.badlogic.gdx.graphics.g2d;

import com.badlogic.gdx.jnigen.AntScriptGenerator;
import com.badlogic.gdx.jnigen.BuildConfig;
import com.badlogic.gdx.jnigen.BuildExecutor;
import com.badlogic.gdx.jnigen.BuildTarget;
import com.badlogic.gdx.jnigen.BuildTarget.TargetOs;
import com.badlogic.gdx.jnigen.NativeCodeGenerator;

public class ImageBuild {
	public static void main (String[] args) throws Exception {
		new NativeCodeGenerator().generate();
		
		String[] excludes = { };
		String[] headers = { "libjpeg/", "giflib/", "../../../gdx/jni/gdx2d/" };
		
		BuildTarget win32home = BuildTarget.newDefaultTarget(TargetOs.Windows, false);
		win32home.compilerPrefix = "";
		win32home.buildFileName = "build-windows32home.xml";
		win32home.excludeFromMasterBuildFile = true;
		win32home.cExcludes = win32home.cppExcludes = excludes;
		win32home.cFlags += " -DHAVE_CONFIG_H";
		win32home.headerDirs = headers;
		
		BuildTarget win32 = BuildTarget.newDefaultTarget(TargetOs.Windows, false);
		win32.cExcludes = win32.cppExcludes = excludes;
		win32.headerDirs = headers;
		win32.cFlags += " -DHAVE_CONFIG_H";
		
		BuildTarget win64 = BuildTarget.newDefaultTarget(TargetOs.Windows, true);
		win64.cExcludes = win64.cppExcludes = excludes;
		win64.headerDirs = headers;
		win64.cFlags += " -DHAVE_CONFIG_H";
		
		BuildTarget lin32 = BuildTarget.newDefaultTarget(TargetOs.Linux, false);
		lin32.cExcludes = lin32.cppExcludes = excludes;
		lin32.headerDirs = headers;
		lin32.cFlags += " -DHAVE_CONFIG_H";
		
		BuildTarget lin64 = BuildTarget.newDefaultTarget(TargetOs.Linux, true);
		lin64.cExcludes = lin64.cppExcludes = excludes;
		lin64.headerDirs = headers;
		lin64.cFlags += " -DHAVE_CONFIG_H";
		
		BuildTarget mac = BuildTarget.newDefaultTarget(TargetOs.MacOsX, false);
		mac.cExcludes = mac.cppExcludes = excludes;
		mac.headerDirs = headers;
		mac.cFlags += " -DHAVE_CONFIG_H";
		
		BuildTarget android = BuildTarget.newDefaultTarget(TargetOs.Android, false);
		android.cExcludes = android.cppExcludes = excludes;
		android.headerDirs = headers;
		android.cFlags += " -DHAVE_CONFIG_H";
		
		new AntScriptGenerator().generate(new BuildConfig("gdx-image"), win32home, android, win32, win64, lin32, lin64, mac);

		// build natives
//		BuildExecutor.executeAnt("jni/build-windows32home.xml", "-v");
		BuildExecutor.executeAnt("jni/build.xml", "pack-natives -v");
	}
}
