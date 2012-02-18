
package com.badlogic.gdx.graphics.g2d;

import com.badlogic.gdx.jnigen.AntScriptGenerator;
import com.badlogic.gdx.jnigen.BuildConfig;
import com.badlogic.gdx.jnigen.BuildExecutor;
import com.badlogic.gdx.jnigen.BuildTarget;
import com.badlogic.gdx.jnigen.BuildTarget.TargetOs;

public class ImageBuild {
	public static void main (String[] args) {
		String[] excludes = { };
		String[] headers = { "libjpeg/" };
		
		BuildTarget win32home = BuildTarget.newDefaultTarget(TargetOs.Windows, false);
		win32home.compilerPrefix = "";
		win32home.buildFileName = "build-windows32home.xml";
		win32home.excludeFromMasterBuildFile = true;
		win32home.cExcludes = win32home.cppExcludes = excludes;
		win32home.headerDirs = headers;
		
		BuildTarget win32 = BuildTarget.newDefaultTarget(TargetOs.Windows, false);
		win32.cExcludes = win32.cppExcludes = excludes;
		win32.headerDirs = headers;
		
		BuildTarget win64 = BuildTarget.newDefaultTarget(TargetOs.Windows, true);
		win64.cExcludes = win64.cppExcludes = excludes;
		win64.headerDirs = headers;
		
		BuildTarget lin32 = BuildTarget.newDefaultTarget(TargetOs.Linux, false);
		lin32.cExcludes = lin32.cppExcludes = excludes;
		lin32.headerDirs = headers;
		
		BuildTarget lin64 = BuildTarget.newDefaultTarget(TargetOs.Linux, true);
		lin64.cExcludes = lin64.cppExcludes = excludes;
		lin64.headerDirs = headers;
		
		BuildTarget mac = BuildTarget.newDefaultTarget(TargetOs.MacOsX, false);
		mac.cExcludes = mac.cppExcludes = excludes;
		mac.headerDirs = headers;
		
		BuildTarget android = BuildTarget.newDefaultTarget(TargetOs.Android, false);
		android.cExcludes = android.cppExcludes = excludes;
		android.headerDirs = headers;
		
		new AntScriptGenerator().generate(new BuildConfig("gdx-bullet"), win32home, android, win32, win64, lin32, lin64, mac);

		// build natives
//		BuildExecutor.executeAnt("jni/build-windows32home.xml", "-v");
	}
}
