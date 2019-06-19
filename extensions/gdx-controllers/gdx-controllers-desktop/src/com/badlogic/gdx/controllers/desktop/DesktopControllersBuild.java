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

package com.badlogic.gdx.controllers.desktop;

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

		String[] windowsSrc = {"*.cpp", "ois-v1-4svn/src/*.cpp", "ois-v1-4svn/src/win32/*.cpp"};

		String[] linuxSrc = {"*.cpp", "ois-v1-4svn/src/*.cpp", "ois-v1-4svn/src/linux/*.cpp"};

		String[] mac64Src = {"*.cpp", "ois-v1-4svn/src/*.cpp", "ois-v1-4svn/src/mac/*.mm", "ois-v1-4svn/src/mac/MacHIDManager.cpp",
			"ois-v1-4svn/src/mac/MacJoyStick.cpp",};

		String[] includes = new String[] {"ois-v1-4svn/includes", "dinput/"};

		BuildTarget win32home = BuildTarget.newDefaultTarget(TargetOs.Windows, false);
		win32home.buildFileName = "build-windows32home.xml";
		win32home.excludeFromMasterBuildFile = true;
		win32home.is64Bit = false;
		win32home.compilerPrefix = "";
		win32home.cppIncludes = windowsSrc;
		win32home.headerDirs = includes;
		win32home.cIncludes = new String[0];
		win32home.libraries = "-ldinput8 -ldxguid";

		BuildTarget win32 = BuildTarget.newDefaultTarget(TargetOs.Windows, false);
		win32.cppIncludes = windowsSrc;
		win32.headerDirs = includes;
		win32.libraries = "-ldinput8 -ldxguid";

		BuildTarget win64 = BuildTarget.newDefaultTarget(TargetOs.Windows, true);
		win64.cppIncludes = windowsSrc;
		win64.headerDirs = includes;
		win64.libraries = "-ldinput8 -ldxguid";

		BuildTarget lin32 = BuildTarget.newDefaultTarget(TargetOs.Linux, false);
		lin32.cppIncludes = linuxSrc;
		lin32.headerDirs = includes;
		lin32.libraries = "-lX11";

		BuildTarget lin64 = BuildTarget.newDefaultTarget(TargetOs.Linux, true);
		lin64.cppIncludes = linuxSrc;
		lin64.headerDirs = includes;
		lin64.libraries = "-lX11";
		
		BuildTarget mac64 = BuildTarget.newDefaultTarget(TargetOs.MacOsX, true);
		mac64.cppIncludes = mac64Src;
		mac64.headerDirs = includes;
		mac64.cppFlags += " -x objective-c++";
		mac64.libraries = "-framework CoreServices -framework Carbon -framework IOKit -framework Cocoa";

		new AntScriptGenerator().generate(buildConfig, win32home, win32, win64, lin32, lin64, mac64);
//		if (!BuildExecutor.executeAnt("jni/build-macosx32.xml", "-Dhas-compiler=true -v postcompile")) {
//			throw new Exception("build failed");
//		}
//		BuildExecutor.executeAnt("jni/build.xml", "pack-natives");
	}
}
