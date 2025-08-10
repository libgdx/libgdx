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

import static com.badlogic.gdx.utils.SharedLibraryLoader.*;

import com.badlogic.gdx.utils.*;

import java.io.File;
import java.lang.reflect.Method;

public final class LwjglNativesLoader {
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
			String lwjglLib = null;
			String openalLib = null;
			switch (os) {
			case Windows:
				switch (bitness) {
				case _32:
					lwjglLib = "lwjgl.dll";
					openalLib = "OpenAL32.dll";
					break;
				case _64:
					lwjglLib = "lwjgl64.dll";
					openalLib = "OpenAL64.dll";
					break;
				}
				break;
			case MacOsX:
				lwjglLib = "liblwjgl.dylib";
				openalLib = "openal.dylib";
				break;
			case Linux:
				switch (bitness) {
				case _32:
					lwjglLib = "liblwjgl.so";
					openalLib = "libopenal.so";
					break;
				case _64:
					lwjglLib = "liblwjgl64.so";
					openalLib = "libopenal64.so";
					break;
				}
				break;
			}
			if (lwjglLib == null) throw new GdxRuntimeException("Unknown LWJGL platform: " + os + ", " + bitness);
			nativesDir = loader.extractFile(lwjglLib, null).getParentFile();
			if (!LwjglApplicationConfiguration.disableAudio) loader.extractFileTo(openalLib, nativesDir);
		} catch (Throwable ex) {
			throw new GdxRuntimeException("Unable to extract LWJGL natives.", ex);
		}
		System.setProperty("org.lwjgl.librarypath", nativesDir.getAbsolutePath());
		load = false;
	}
}
