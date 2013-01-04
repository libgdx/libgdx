
package com.badlogic.gdx.controllers;

import com.badlogic.gdx.jnigen.AntScriptGenerator;
import com.badlogic.gdx.jnigen.BuildConfig;
import com.badlogic.gdx.jnigen.BuildExecutor;
import com.badlogic.gdx.jnigen.BuildTarget;
import com.badlogic.gdx.jnigen.NativeCodeGenerator;
import com.badlogic.gdx.jnigen.BuildTarget.TargetOs;

public class DesktopControllersBuild {
	public static void main (String[] args) throws Exception {
		new NativeCodeGenerator().generate("src/", "bin/", "jni/");
		BuildConfig buildConfig = new BuildConfig("gdx-controllers-desktop");
		BuildTarget win32home = BuildTarget.newDefaultTarget(TargetOs.Windows, false);
		win32home.buildFileName = "build-windows32home.xml";
		win32home.is64Bit = false;
		win32home.compilerPrefix = "";
		win32home.cppIncludes = new String[] {
			"*.cpp",
			"ois-v1-4svn/src/*.cpp",
			"ois-v1-4svn/src/win32/*.cpp"
		};
		win32home.headerDirs = new String[] {
			"ois-v1-4svn/includes",
			"dinput/"
		};
		win32home.cIncludes = new String[0];
		win32home.libraries = "-ldinput8 -ldxguid";

		new AntScriptGenerator().generate(buildConfig, win32home);
		BuildExecutor.executeAnt("jni/build-windows32home.xml", "-Dhas-compiler=true clean postcompile -v");
		BuildExecutor.executeAnt("jni/build.xml", "pack-natives");
	}
}
