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

package com.badlogic.gdx.backends.lwjgl3.angle;

import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.SharedLibraryLoader;

import java.io.File;
import java.io.IOException;

public class ANGLELoader {
	static public boolean isWindows = System.getProperty("os.name").contains("Windows");
	static public boolean isLinux = System.getProperty("os.name").contains("Linux")
		|| System.getProperty("os.name").contains("FreeBSD");
	static public boolean isMac = System.getProperty("os.name").contains("Mac");
	static public boolean isARM = System.getProperty("os.arch").startsWith("arm")
		|| System.getProperty("os.arch").startsWith("aarch64");
	static public boolean is64Bit = System.getProperty("os.arch").contains("64")
		|| System.getProperty("os.arch").startsWith("armv8");

	/** Holds the extracted library files to be deleted after the glfw initialization. This is currently only used on osx */
	static private File[] loadedLibraries = new File[0];

	/** Checks if the given directory is inside an osx app bundle
	 *
	 * @param directory Directory
	 * @return True if app bundle */
	static boolean isAppBundlePath (File directory) {
		// Finder uses "/" as working directory,
		// there might be a chroot afterwards to the apps Contents directory
		return directory.getAbsolutePath().equals("/.") || directory.getAbsolutePath().matches(".*/[^/]+\\.app/Contents/?.*");
	}

	public static void load () {
		if ((isARM && !isMac) || (!isWindows && !isLinux && !isMac)) {
			throw new GdxRuntimeException("ANGLE is only supported on x86/x86_64 Windows, x64 Linux, and x64/arm64 macOS.");
		}
		String osDir = null;
		String ext = null;
		if (isWindows) {
			osDir = is64Bit ? "windows64" : "windows32";
			ext = ".dll";
		}
		if (isLinux) {
			osDir = "linux64";
			ext = ".so";
		}
		if (isMac) {
			osDir = isARM ? "macosxarm64" : "macosx64";
			ext = ".dylib";
		}

		String eglFileName = "libEGL" + ext;
		String glesFileName = "libGLESv2" + ext;
		String eglSource = osDir + "/" + eglFileName;
		String glesSource = osDir + "/" + glesFileName;

		SharedLibraryLoader loader = new SharedLibraryLoader();
		try {
			if (!isMac) {
				String dirName = loader.crc(ANGLELoader.class.getResourceAsStream("/" + eglSource))
					+ loader.crc(ANGLELoader.class.getResourceAsStream("/" + glesSource));

				File egl = loader.extractFile(eglSource, dirName);
				System.load(egl.getAbsolutePath());

				File gles = loader.extractFile(glesSource, dirName);
				System.load(gles.getAbsolutePath());
			} else {
				// On macOS, we can't preload the shared libraries via the JVM. calling dlopen("path1/lib.dylib")
				// then calling dlopen("lib.dylib") will not return the dylib loaded in the first dlopen()
				// call, but instead perform the dlopen library search algorithm anew. Since the dylibs
				// we extract are not in any paths dlopen knows about, GLFW fails to load them.
				// Instead, we need to copy the shared libraries to the current working directory (which
				// we can't temporarily change in pure Java either...). The dylibs will get deleted
				// in postGlfwInit() once the first window has been created, and GLFW has loaded the dylibs.

				// Note: This only works if the app is NOT executed via an app bundle, since extracting the files
				// into the app bundle breaks the signature and osx won't run the app anymore at all.
				// Therefore, if you want to use angle with an app bundle, be sure to include the dylibs
				// manually since we won't extract them here.
				File lastWorkingDir = new File(".");
				if (isAppBundlePath(lastWorkingDir)) {
					// Running inside an app bundle - do nothing
					return;
				}

				loader.extractFileTo(eglSource, lastWorkingDir);
				loader.extractFileTo(glesSource, lastWorkingDir);
				loadedLibraries = new File[] {new File(lastWorkingDir, eglFileName), new File(lastWorkingDir, glesFileName),};
			}
		} catch (IOException e) {
			throw new GdxRuntimeException("Could not extract angle libraries: " + e.getMessage());
		}
	}

	public static void postGlfwInit () {
		for (File file : loadedLibraries) {
			file.delete();
		}
	}
}
