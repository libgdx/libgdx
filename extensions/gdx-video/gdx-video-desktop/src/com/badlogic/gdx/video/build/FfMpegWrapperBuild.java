/*******************************************************************************
 * Copyright 2014 See AUTHORS file.
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


package com.badlogic.gdx.video.build;

import com.badlogic.gdx.jnigen.AntScriptGenerator;
import com.badlogic.gdx.jnigen.BuildConfig;
import com.badlogic.gdx.jnigen.BuildTarget;
import com.badlogic.gdx.jnigen.BuildTarget.TargetOs;
import com.badlogic.gdx.jnigen.NativeCodeGenerator;
import com.badlogic.gdx.video.FfMpeg;

/**
 * This class is used to create the buildscripts, and generate the jni files.
 *
 * @author Rob Bogie <rob.bogie@codepoke.net>
 */
public class FfMpegWrapperBuild {

	public static void main(String[] args) throws Exception {

		// Generate the C++ files
		new NativeCodeGenerator().generate(	"src/",
											"target/java/",
											"jni",
											new String[] { "**/*.java" },
											new String[] { "**/FfMpegWrapperBuild.java" });

		// Setup the build target
		BuildTarget targetLinux32 = BuildTarget.newDefaultTarget(TargetOs.Linux, false);
		targetLinux32.libraries = "-lavcodec -lavformat -lavutil -lswscale -lswresample -lpthread";
		targetLinux32.cppFlags = "-Iffmpeg/libs/linux32/include/";

		BuildTarget targetLinux64 = BuildTarget.newDefaultTarget(TargetOs.Linux, true);
		targetLinux64.libraries = "-lavcodec -lavformat -lavutil -lswscale -lswresample -lpthread";
		targetLinux32.cppFlags = "-Iffmpeg/libs/linux64/include/";

		BuildTarget targetWindows32 = BuildTarget.newDefaultTarget(TargetOs.Windows, false);
		targetWindows32.libraries = "-lavcodec -lavformat -lavutil -lswscale -lswresample -lpthread";
		targetWindows32.cppFlags += " -DWIN32 -Iffmpeg/libs/windows32/include/";
		targetWindows32.compilerPrefix = "i686-w64-mingw32-";

		BuildTarget targetWindows64 = BuildTarget.newDefaultTarget(TargetOs.Windows, true);
		targetWindows64.libraries = "-lavcodec -lavformat -lavutil -lswscale -lswresample -lpthread";
		targetWindows32.cppFlags += " -DWIN32 -Iffmpeg/libs/windows64/include/";
		targetWindows64.compilerPrefix = "x86_64-w64-mingw32-";

		BuildTarget mac = BuildTarget.newDefaultTarget(TargetOs.MacOsX, false);
		mac.libraries = "-lavcodec -lavformat -lavutil -lswscale -lswresample -lpthread";
		mac.excludeFromMasterBuildFile = true;

		BuildTarget mac64 = BuildTarget.newDefaultTarget(TargetOs.MacOsX, true);
		mac64.libraries = "-lavcodec -lavformat -lavutil -lswscale -lswresample -lpthread";
		mac64.excludeFromMasterBuildFile = true;

		BuildConfig buildConfig = new BuildConfig(FfMpeg.NATIVE_LIBRARY_NAME);

		// Generate Ant buildfiles
		AntScriptGenerator antScriptGenerator = new AntScriptGenerator();
		antScriptGenerator.generate(buildConfig, targetLinux32, targetLinux64, targetWindows32, targetWindows64, mac, mac64);
	}
}
