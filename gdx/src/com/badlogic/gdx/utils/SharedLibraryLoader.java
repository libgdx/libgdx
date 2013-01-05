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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/** Loads shared libraries from a natives jar file (desktop) or arm folders (Android). For desktop projects, have the natives jar
 * in the classpath, for Android projects put the shared libraries in the libs/armeabi and libs/armeabi-v7a folders.
 * 
 * @author mzechner */
public class SharedLibraryLoader {
	static public boolean isWindows = System.getProperty("os.name").contains("Windows");
	static public boolean isLinux = System.getProperty("os.name").contains("Linux");
	static public boolean isMac = System.getProperty("os.name").contains("Mac");
	static public boolean isAndroid = false;
	static public boolean is64Bit = System.getProperty("os.arch").equals("amd64");
	static {
		String vm = System.getProperty("java.vm.name");
		if (vm != null && vm.contains("Dalvik")) {
			isAndroid = true;
			isWindows = false;
			isLinux = false;
			isMac = false;
			is64Bit = false;
		}
	}

	static private HashSet<String> loadedLibraries = new HashSet();

	private String nativesJar;

	public SharedLibraryLoader () {
	}

	/** Fetches the natives from the given natives jar file. Used for testing a shared lib on the fly.
	 * @param nativesJar */
	public SharedLibraryLoader (String nativesJar) {
		this.nativesJar = nativesJar;
	}

	/** Returns a CRC of the remaining bytes in the stream. */
	public String crc (InputStream input) {
		if (input == null) return "" + System.nanoTime(); // fallback
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

	/** Maps a platform independent library name to a platform dependent name. */
	public String mapLibraryName (String libraryName) {
		if (isWindows) return libraryName + (is64Bit ? "64.dll" : ".dll");
		if (isLinux) return "lib" + libraryName + (is64Bit ? "64.so" : ".so");
		if (isMac) return "lib" + libraryName + ".dylib";
		return libraryName;
	}

	/** Loads a shared library for the platform the application is running on.
	 * @param libraryName The platform independent library name. If not contain a prefix (eg lib) or suffix (eg .dll). */
	public synchronized void load (String libraryName) {
		libraryName = mapLibraryName(libraryName);
		if (loadedLibraries.contains(libraryName)) return;

		try {
			if (isAndroid)
				System.loadLibrary(libraryName);
			else
				System.load(extractFile(libraryName, null).getAbsolutePath());
		} catch (Throwable ex) {
			throw new GdxRuntimeException("Couldn't load shared library '" + libraryName + "' for target: "
				+ System.getProperty("os.name") + (is64Bit ? ", 64-bit" : ", 32-bit"), ex);
		}
		loadedLibraries.add(libraryName);
	}

	private InputStream readFile (String path) {
		if (nativesJar == null) return SharedLibraryLoader.class.getResourceAsStream("/" + path);

		// Read from JAR.
		try {
			ZipFile file = new ZipFile(nativesJar);
			ZipEntry entry = file.getEntry(path);
			if (entry == null) throw new GdxRuntimeException("Couldn't find '" + path + "' in JAR: " + nativesJar);
			return file.getInputStream(entry);
		} catch (IOException ex) {
			throw new GdxRuntimeException("Error reading '" + path + "' in JAR: " + nativesJar, ex);
		}
	}

	/** Extracts the specified file into the temp directory if it does not already exist or the CRC does not match.
	 * @param sourcePath The file to extract from the classpath or JAR.
	 * @param dirName The name of the subdirectory where the file will be extracted. If null, the file's CRC will be used.
	 * @return The extracted file. */
	public File extractFile (String sourcePath, String dirName) throws IOException {
		String sourceCrc = crc(readFile(sourcePath));
		if (dirName == null) dirName = sourceCrc;

		File extractedDir = new File(System.getProperty("java.io.tmpdir") + "/libgdx" + System.getProperty("user.name") + "/"
			+ dirName);
		File extractedFile = new File(extractedDir, new File(sourcePath).getName());

		String extractedCrc = null;
		if (extractedFile.exists()) {
			try {
				extractedCrc = crc(new FileInputStream(extractedFile));
			} catch (FileNotFoundException ignored) {
			}
		}

		// If file doesn't exist or the CRC doesn't match, extract it to the temp dir.
		if (extractedCrc == null || !extractedCrc.equals(sourceCrc)) {
			try {
				InputStream input = readFile(sourcePath);
				if (input == null) return null;
				extractedDir.mkdirs();
				FileOutputStream output = new FileOutputStream(extractedFile);
				byte[] buffer = new byte[4096];
				while (true) {
					int length = input.read(buffer);
					if (length == -1) break;
					output.write(buffer, 0, length);
				}
				input.close();
				output.close();
			} catch (IOException ex) {
				throw new GdxRuntimeException("Error extracting file: " + sourcePath, ex);
			}
		}
		return extractedFile.exists() ? extractedFile : null;
	}
}
