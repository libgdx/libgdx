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

package com.badlogic.gdx.backends.lwjgl3;

import java.io.File;
import java.lang.reflect.Method;

import com.badlogic.gdx.utils.GdxNativesLoader;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.SharedLibraryLoader;

public final class Lwjgl3NativesLoader {
	static public boolean load = true;

	static {
		System.setProperty("org.lwjgl.input.Mouse.allowNegativeMouseCoords", "true");

		// Don't extract natives if using JWS.
		try {
			Method method = Class.forName("javax.jnlp.ServiceManager").getDeclaredMethod("lookup", new Class[] {String.class});
			method.invoke(null, "javax.jnlp.PersistenceService");
			load = false;
		} catch (Throwable ex) {
			load = true;
		}
	}

	/** Extracts the LWJGL native libraries from the classpath and sets the "org.lwjgl.librarypath" system property. */
	static public void load () {
		GdxNativesLoader.load();
		if (GdxNativesLoader.disableNativesLoading) return;
		if (!load) return;

		SharedLibraryLoader loader = new SharedLibraryLoader();
		File nativesDir = null;
		try {
			if (SharedLibraryLoader.isWindows) {
				nativesDir = loader.extractFile(SharedLibraryLoader.is64Bit ? "lwjgl.dll" : "lwjgl32.dll", null).getParentFile();
				loader.extractFile(SharedLibraryLoader.is64Bit ? "glfw.dll" : "glfw32.dll", nativesDir.getName());
				loader.extractFile(SharedLibraryLoader.is64Bit ? "jemalloc.dll" : "jemalloc32.dll", nativesDir.getName());				
				loader.extractFile(SharedLibraryLoader.is64Bit ? "OpenAL.dll" : "OpenAL32.dll", nativesDir.getName());				
			} else if (SharedLibraryLoader.isMac) {
				nativesDir = loader.extractFile("liblwjgl.dylib", null).getParentFile();
				loader.extractFile("libglfw.dylib", nativesDir.getName());
				loader.extractFile("libjemalloc.dylib", nativesDir.getName());
				loader.extractFile("libopenal.dylib", nativesDir.getName());
			} else if (SharedLibraryLoader.isLinux) {
				nativesDir = loader.extractFile(SharedLibraryLoader.is64Bit ? "liblwjgl.so" : "liblwjgl32.so", null).getParentFile();				
				loader.extractFile(SharedLibraryLoader.is64Bit ? "libglfw.so" : "libglfw32.so", nativesDir.getName());
				loader.extractFile(SharedLibraryLoader.is64Bit ? "libjemalloc.so" : "libjemalloc32.so", nativesDir.getName());				
				loader.extractFile(SharedLibraryLoader.is64Bit ? "libopenal.so" : "libopenal32.so", nativesDir.getName());
			}
		} catch (Throwable ex) {
			throw new GdxRuntimeException("Unable to extract LWJGL natives.", ex);
		}
		System.setProperty("org.lwjgl.librarypath", nativesDir.getAbsolutePath());
		load = false;
	}
}
