/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.physics.bullet;

import java.io.File;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.jnigen.AntScriptGenerator;
import com.badlogic.gdx.jnigen.BuildConfig;
import com.badlogic.gdx.jnigen.BuildExecutor;
import com.badlogic.gdx.jnigen.BuildTarget;
import com.badlogic.gdx.jnigen.BuildTarget.TargetOs;
import com.badlogic.gdx.jnigen.NativeCodeGenerator;

public class BulletBuild {
	public static void main (String[] args) throws Exception {
		// generate C/C++ code
		new NativeCodeGenerator().generate("src", "bin", "jni");

		// Flags to accomodate SWIG generated code
		String cppFlags = "";

		// SWIG doesn't emit strict aliasing compliant code
		cppFlags += " -fno-strict-aliasing";
		// SWIG directors aren't clearly documented to require RTTI, but SWIG
		// normally generates a small number of dynamic_casts for director code.
		// gdx-bullet's swig build.xml replaces these with static C casts so we
		// can compile without RTTI and save some disk space. It seems to work
		// with these static casts.
		cppFlags += " -fno-rtti";
		// Disable profiling (it's on by default). If you change this, you
		// must regenerate the SWIG wrappers with the changed value.
		cppFlags += " -DBT_NO_PROFILE";
		//Bullet 2 compatibility with inverse dynamics
		cppFlags += " -DBT_USE_INVERSE_DYNAMICS_WITH_BULLET2";

		// generate build scripts
		String[] excludes = {"src/bullet/BulletMultiThreaded/GpuSoftBodySolvers/**"};
		String[] headers = {"src/bullet/", "src/custom/", "src/extras/Serialize/", "src/extras/"};

		BuildTarget win32home = BuildTarget.newDefaultTarget(TargetOs.Windows, false);
		win32home.compilerPrefix = "";
		win32home.buildFileName = "build-windows32home.xml";
		win32home.excludeFromMasterBuildFile = true;
		win32home.cExcludes = win32home.cppExcludes = excludes;
		win32home.headerDirs = headers;
		win32home.cppFlags += cppFlags;

		BuildTarget win32 = BuildTarget.newDefaultTarget(TargetOs.Windows, false);
		win32.cExcludes = win32.cppExcludes = excludes;
		win32.headerDirs = headers;
		win32.cppFlags += cppFlags;

		BuildTarget win64 = BuildTarget.newDefaultTarget(TargetOs.Windows, true);
		win64.cExcludes = win64.cppExcludes = excludes;
		win64.headerDirs = headers;
		win64.cppFlags += cppFlags;

		BuildTarget lin32 = BuildTarget.newDefaultTarget(TargetOs.Linux, false);
		lin32.cExcludes = lin32.cppExcludes = excludes;
		lin32.headerDirs = headers;
		lin32.cppFlags += cppFlags;

		BuildTarget lin64 = BuildTarget.newDefaultTarget(TargetOs.Linux, true);
		lin64.cExcludes = lin64.cppExcludes = excludes;
		lin64.headerDirs = headers;
		lin64.cppFlags += cppFlags;
		
		BuildTarget mac64 = BuildTarget.newDefaultTarget(TargetOs.MacOsX, true);
		mac64.cExcludes = mac64.cppExcludes = excludes;
		mac64.headerDirs = headers;
		mac64.cppFlags += cppFlags;

		BuildTarget android = BuildTarget.newDefaultTarget(TargetOs.Android, false);
		android.cExcludes = android.cppExcludes = excludes;
		android.headerDirs = headers;	
		android.cppFlags += cppFlags + " -fexceptions";
		
		BuildTarget ios = BuildTarget.newDefaultTarget(TargetOs.IOS, false);
		ios.cExcludes = ios.cppExcludes = excludes;
		ios.headerDirs = headers;
		ios.cppFlags += cppFlags;
		ios.cppFlags += " -stdlib=libc++";

		new AntScriptGenerator().generate(new BuildConfig("gdx-bullet"), win32home, win32, win64, lin32, lin64, mac64, android, ios);
		new FileHandle(new File("jni/Application.mk")).writeString("\nAPP_STL := stlport_static\n", true);

		// build natives
		// BuildExecutor.executeAnt("jni/build-windows32home.xml", "-v");
//		BuildExecutor.executeAnt("jni/build-linux64.xml", "");
//		BuildExecutor.executeAnt("jni/build-android32.xml", "");
//		BuildExecutor.executeAnt("jni/build.xml", "pack-natives");
	}
}
