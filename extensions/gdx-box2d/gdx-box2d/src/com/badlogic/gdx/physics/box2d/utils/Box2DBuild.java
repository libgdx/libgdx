package com.badlogic.gdx.physics.box2d.utils;

import com.badlogic.gdx.jnigen.AntScriptGenerator;
import com.badlogic.gdx.jnigen.BuildConfig;
import com.badlogic.gdx.jnigen.BuildTarget;
import com.badlogic.gdx.jnigen.NativeCodeGenerator;
import com.badlogic.gdx.jnigen.BuildTarget.TargetOs;

public class Box2DBuild {
	public static void main(String[] args) throws Exception {
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
		BuildTarget ios = BuildTarget.newDefaultTarget(TargetOs.IOS, false);
		new NativeCodeGenerator().generate();
		new AntScriptGenerator().generate(new BuildConfig("gdx-box2d"), win32home, win32, win64, lin32, lin64, mac, android, ios);		
	}
}