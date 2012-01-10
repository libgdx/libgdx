package com.badlogic.gdx.physics.tokamak;

import com.badlogic.gdx.jnigen.AntScriptGenerator;
import com.badlogic.gdx.jnigen.BuildConfig;
import com.badlogic.gdx.jnigen.BuildExecutor;
import com.badlogic.gdx.jnigen.BuildTarget;
import com.badlogic.gdx.jnigen.BuildTarget.TargetOs;
import com.badlogic.gdx.jnigen.NativeCodeGenerator;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.SharedLibraryLoader;

public class TokamakBuild {
	public static void main(String[] args) throws Exception {
		new NativeCodeGenerator().generate();
		
		String[] headers = { ".", "tokamak/include" };
		String[] cppIncludes = { "**/*.cpp" };
		
		
		BuildConfig config = new BuildConfig("gdx-tokamak", "../target", "libs", "jni");
		BuildTarget win32home = BuildTarget.newDefaultTarget(TargetOs.Windows, false);
		win32home.compilerPrefix = "";
		win32home.buildFileName = "build-windows32home.xml";
		win32home.headerDirs = headers;
		win32home.cppIncludes = cppIncludes;
		win32home.cppExcludes = new String[] { "**/perflinux.cpp" };
		win32home.excludeFromMasterBuildFile = true;
		
		BuildTarget win32 = BuildTarget.newDefaultTarget(TargetOs.Windows, false);
		win32.headerDirs = headers;
		win32.cppIncludes = cppIncludes;
		win32.cppExcludes = new String[] { "**/perflinux.cpp" };
		win32.linkerFlags += " -lm";
		
		BuildTarget win64 = BuildTarget.newDefaultTarget(TargetOs.Windows, true);
		win64.headerDirs = headers;
		win64.cppIncludes = cppIncludes;
		win64.cppExcludes = new String[] { "**/perflinux.cpp" };
		win64.linkerFlags += " -lm";
		
		BuildTarget lin32 = BuildTarget.newDefaultTarget(TargetOs.Linux, false);
		lin32.headerDirs = headers;
		lin32.cppIncludes = cppIncludes;
		lin32.cppExcludes = new String[] { "**/perfwin32.cpp" };
		lin32.linkerFlags += " -lm";
		
		BuildTarget lin64 = BuildTarget.newDefaultTarget(TargetOs.Linux, true);
		lin64.headerDirs = headers;
		lin64.cppIncludes = cppIncludes;
		lin64.cppExcludes = new String[] { "**/perfwin32.cpp" };
		lin64.linkerFlags += " -lm";
		
		BuildTarget mac = BuildTarget.newDefaultTarget(TargetOs.MacOsX, false);
		mac.headerDirs = headers;
		mac.cppIncludes = cppIncludes;
		mac.cppExcludes = new String[] { "**/perfwin32.cpp" };
		mac.linkerFlags += " -lm";
		
		BuildTarget android = BuildTarget.newDefaultTarget(TargetOs.Android, false);
		android.headerDirs = headers;
		android.cppIncludes = cppIncludes;
		android.cppExcludes = new String[] { "**/perfwin32.cpp" };
		android.linkerFlags += " -lm";
		
		new AntScriptGenerator().generate(config, win32home, win32, win64, lin32, lin64, mac, android);
		BuildExecutor.executeAnt("jni/build-windows32home.xml", "-v");
		BuildExecutor.executeAnt("jni/build.xml", "pack-natives -v");
		
		new SharedLibraryLoader("libs/gdx-tokamak-natives.jar").load("gdx-tokamak");
		Simulator sim = new Simulator(new SimulatorSizeInfo(), new Vector3(0, -10, 0));
		sim.setGravity(0, -1, 0);
		System.out.println(sim.getGravity());
		RigidBody body = sim.createRigidBody();
		sim.freeRigidBody(body);
		sim.dispose();
	}
}
