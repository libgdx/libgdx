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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.CRC32;

import com.badlogic.gdx.Version;

public class GdxNativesLoader {
	static public boolean disableNativesLoading = false;
	static private boolean nativesLoaded = false;

	static public boolean isWindows = System.getProperty("os.name").contains("Windows");
	static public boolean isLinux = System.getProperty("os.name").contains("Linux");
	static public boolean isMac = System.getProperty("os.name").contains("Mac");
	static public boolean is64Bit = System.getProperty("os.arch").equals("amd64");
	static public File nativesDir = new File(System.getProperty("java.io.tmpdir") + "/libgdx/" + crc("gdx.dll"));

	static private String crc (String nativeFile) {
		InputStream input = GdxNativesLoader.class.getResourceAsStream("/" + nativeFile);
		if (input == null) return Version.VERSION; // fallback
		CRC32 crc = new CRC32();
		byte[] buffer = new byte[4096];
		try {
			while (true) {
				int length = input.read(buffer);
				if (length == -1) break;
				crc.update(buffer, 0, length);
			}
		} catch (Exception ex) {
			try {
				input.close();
			} catch (Exception ignored) {
			}
		}
		return Long.toString(crc.getValue());
	}

	static public boolean loadLibrary (String nativeFile32, String nativeFile64) {
		String path = extractLibrary(nativeFile32, nativeFile64);
		if (path != null) System.load(path);
		return path != null;
	}

	static public String extractLibrary (String native32, String native64) {
		String nativeFileName = is64Bit ? native64 : native32;
		File nativeFile = new File(nativesDir, nativeFileName);
		try {
			// Extract native from classpath to temp dir.
			InputStream input = GdxNativesLoader.class.getResourceAsStream("/" + nativeFileName);
			if (input == null) return null;
			nativesDir.mkdirs();
			FileOutputStream output = new FileOutputStream(nativeFile);
			byte[] buffer = new byte[4096];
			while (true) {
				int length = input.read(buffer);
				if (length == -1) break;
				output.write(buffer, 0, length);
			}
			input.close();
			output.close();
		} catch (IOException ex) {
		}
		return nativeFile.exists() ? nativeFile.getAbsolutePath() : null;
	}

	/**
	 * Loads the libgdx native libraries.
	 */
	static public void load () {
		if(disableNativesLoading) {
			System.out.println("So you don't like our native lib loading? Good, you are on your own now. We don't give support from here on out");
			return; 
		}
		if (nativesLoaded) return;

		String vm = System.getProperty("java.vm.name");
		if (vm == null || !vm.contains("Dalvik")) {
			if (isWindows) {
				nativesLoaded = loadLibrary("gdx.dll", "gdx-64.dll");
			} else if (isMac) {
				nativesLoaded = loadLibrary("libgdx.dylib", "libgdx.dylib");
			} else if (isLinux) {
				nativesLoaded = loadLibrary("libgdx.so", "libgdx-64.so");
			}
			if (nativesLoaded) return;
		}

		if (!is64Bit || isMac) {
			System.loadLibrary("gdx");
		} else {
			System.loadLibrary("gdx-64");
		}
		nativesLoaded = true;
	}
}
