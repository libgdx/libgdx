package com.badlogic.gdx.jnigen.test;

import com.badlogic.gdx.jnigen.AntScriptExecutor;
import com.badlogic.gdx.jnigen.AntScriptGenerator;
import com.badlogic.gdx.jnigen.AntScriptGenerator.BuildConfig;
import com.badlogic.gdx.jnigen.AntScriptGenerator.BuildTarget;
import com.badlogic.gdx.jnigen.AntScriptGenerator.BuildTarget.TargetOs;
import com.badlogic.gdx.jnigen.NativeCodeGenerator;

public class Build {
	public static void main(String[] args) throws Exception {
		// generate C/C++ code
		new NativeCodeGenerator().generate("src", "bin", "jni");
		
		// generate build scripts, for win32 only
		BuildTarget win32 = BuildTarget.newDefaultTarget(TargetOs.Windows, false);
		BuildTarget win64 = BuildTarget.newDefaultTarget(TargetOs.Windows, true);
		BuildTarget lin32 = BuildTarget.newDefaultTarget(TargetOs.Linux, false);
		BuildTarget lin64 = BuildTarget.newDefaultTarget(TargetOs.Linux, true);
		new AntScriptGenerator().generate(new BuildConfig("test"), win32, win64, lin32, lin64);
		
		// build natives
		AntScriptExecutor.execute("jni/build.xml", "-v");
	}
}
