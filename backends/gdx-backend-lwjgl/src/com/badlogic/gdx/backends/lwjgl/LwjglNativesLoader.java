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
package com.badlogic.gdx.backends.lwjgl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;

import com.badlogic.gdx.Version;
import com.badlogic.gdx.utils.GdxNativesLoader;

import static com.badlogic.gdx.utils.GdxNativesLoader.*;

final class LwjglNativesLoader {
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

	static void load () {
		GdxNativesLoader.load();
		if(GdxNativesLoader.disableNativesLoading) return;
		if (!load) return;
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
		load = false;
	}
}
