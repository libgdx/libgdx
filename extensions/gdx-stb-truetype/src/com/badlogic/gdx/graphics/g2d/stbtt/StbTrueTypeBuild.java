package com.badlogic.gdx.graphics.g2d.stbtt;

import com.badlogic.gdx.jnigen.BuildExecutor;
import com.badlogic.gdx.jnigen.AntScriptGenerator;
import com.badlogic.gdx.jnigen.BuildConfig;
import com.badlogic.gdx.jnigen.BuildTarget;
import com.badlogic.gdx.jnigen.BuildTarget.TargetOs;
import com.badlogic.gdx.jnigen.NativeCodeGenerator;

public class StbTrueTypeBuild {
	public static void main(String[] args) throws Exception {
		// generate C/C++ code
		new NativeCodeGenerator().generate();
		
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
		BuildTarget mac = BuildTarget.newDefaultTarget(TargetOs.MacOsX, false);
		BuildTarget android = BuildTarget.newDefaultTarget(TargetOs.Android, false);
		new AntScriptGenerator().generate(new BuildConfig("stbtruetype"), win32home, win32, win64, lin32, lin64, mac, android);
		
		// build natives
		BuildExecutor.executeAnt("jni/build-windows32home.xml", "-v");
	}
}
