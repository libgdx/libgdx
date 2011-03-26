/*
 * Copyright 2010 Mario Zechner (contact@badlogicgames.com), Nathan Sweet (admin@esotericsoftware.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package com.badlogic.gdx.backends.jogl;

import com.badlogic.gdx.utils.GdxNativesLoader;
import com.sun.opengl.impl.NativeLibLoader;

import static com.badlogic.gdx.utils.GdxNativesLoader.*;

public class JoglNativesLoader {
	static private boolean nativesLoaded = false;

	/**
	 * loads the necessary libraries depending on the operating system
	 */
	static void load () {
		GdxNativesLoader.load();
		if(GdxNativesLoader.disableNativesLoading) return;

		if (nativesLoaded) return;

		NativeLibLoader.disableLoading();
		com.sun.gluegen.runtime.NativeLibLoader.disableLoading();
		// By wkien: On some systems (read: mine) jogl_awt would not find its
		// dependency jawt if not loaded before
		if (System.getProperty("os.name", "").contains("Windows")
			&& !System.getProperty("libgdx.nojawtpreloading", "false").contains("true")) {
			try {
				System.loadLibrary("jawt");
			} catch (Exception ex) {
				System.err.println("WARNING: Unable to load native jawt library: '" + ex.getMessage() + "'");
			}
		}

		if (isWindows) {
			loadLibrary("gluegen-rt-win32.dll", "gluegen-rt-win64.dll");
			loadLibrary("jogl_awt-win32.dll", "jogl_awt-win64.dll");
			loadLibrary("jogl-win32.dll", "jogl-win64.dll");
		} else if (isMac) {
			loadLibrary("libgluegen-rt.jnilib", "libgluegen-rt.jnilib");
			loadLibrary("libjogl_awt.jnilib", "libjogl_awt.jnilib");
			loadLibrary("libjogl.jnilib", "libjogl.jnilib");
		} else if (isLinux) {
			loadLibrary("libgluegen-rt-linux32.so", "libgluegen-rt-linux64.so");
			loadLibrary("libjogl_awt-linux32.so", "libjogl_awt-linux64.so");
			loadLibrary("libjogl-linux32.so", "libjogl-linux64.so");
		}

		if (isWindows) {
			extractLibrary("OpenAL32.dll", "OpenAL64.dll");
			extractLibrary("lwjgl.dll", "lwjgl64.dll");
		} else if (isMac) {
			extractLibrary("openal.dylib", "openal.dylib");
			extractLibrary("liblwjgl.jnilib", "liblwjgl.jnilib");
		} else if (isLinux) {
			extractLibrary("libopenal.so", "libopenal64.so");
			extractLibrary("liblwjgl.so", "liblwjgl64.so");
		}
		System.setProperty("org.lwjgl.librarypath", nativesDir.getAbsolutePath());

		nativesLoaded = true;
	}
}
