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

package com.badlogic.gdx.utils;

import com.badlogic.gdx.jnigen.AntScriptGenerator;
import com.badlogic.gdx.jnigen.BuildConfig;
import com.badlogic.gdx.jnigen.BuildExecutor;
import com.badlogic.gdx.jnigen.BuildTarget;
import com.badlogic.gdx.jnigen.BuildTarget.TargetOs;
import com.badlogic.gdx.jnigen.NativeCodeGenerator;

/** Builds the JNI wrappers via gdx-jnigen.
 * @author mzechner */
public class GdxBuild {
	public static void main (String[] args) throws Exception {
		String JNI_DIR = "jni";
		String LIBS_DIR = "libs";

		// generate C/C++ code
		new NativeCodeGenerator().generate("src", "bin", JNI_DIR, new String[] {"**/*"}, null);

		String[] excludeCpp = {"android/**", "iosgl/**"};

		// generate build scripts, for win32 only
		// custom target for testing purposes
		BuildTarget win32home = BuildTarget.newDefaultTarget(TargetOs.Windows, false);
		win32home.compilerPrefix = "";
		win32home.buildFileName = "build-windows32home.xml";
		win32home.excludeFromMasterBuildFile = true;
		win32home.cppExcludes = excludeCpp;
		BuildTarget win32 = BuildTarget.newDefaultTarget(TargetOs.Windows, false);
		win32.cppExcludes = excludeCpp;
		BuildTarget win64 = BuildTarget.newDefaultTarget(TargetOs.Windows, true);
		win64.cppExcludes = excludeCpp;
		BuildTarget lin32 = BuildTarget.newDefaultTarget(TargetOs.Linux, false);
		lin32.cppExcludes = excludeCpp;
		BuildTarget lin64 = BuildTarget.newDefaultTarget(TargetOs.Linux, true);
		lin64.cppExcludes = excludeCpp;
		BuildTarget android = BuildTarget.newDefaultTarget(TargetOs.Android, false);
		android.linkerFlags += " -lGLESv2 -llog";
		android.cppExcludes = new String[] {"iosgl/**"};
		BuildTarget mac = BuildTarget.newDefaultTarget(TargetOs.MacOsX, false);
		mac.cppExcludes = excludeCpp;
		BuildTarget mac64 = BuildTarget.newDefaultTarget(TargetOs.MacOsX, true);
		mac64.cppExcludes = excludeCpp;
		BuildTarget ios = BuildTarget.newDefaultTarget(TargetOs.IOS, false);
		ios.cppExcludes = new String[] {"android/**"};
		ios.headerDirs = new String[] {"iosgl"};
		new AntScriptGenerator().generate(new BuildConfig("gdx", "../target/native", LIBS_DIR, JNI_DIR), mac, mac64, win32home, win32,
			win64, lin32, lin64, android, ios);

		// build natives
		// BuildExecutor.executeAnt("jni/build-windows32home.xml", "-v");
		// BuildExecutor.executeAnt("jni/build.xml", "pack-natives -v");
	}
}
