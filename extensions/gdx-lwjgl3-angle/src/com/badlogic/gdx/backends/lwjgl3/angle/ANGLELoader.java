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

import java.io.*;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Random;
import java.util.UUID;
import java.util.zip.CRC32;

public class ANGLELoader {
	static public boolean isWindows = System.getProperty("os.name").contains("Windows");
	static public boolean isLinux = System.getProperty("os.name").contains("Linux")
		|| System.getProperty("os.name").contains("FreeBSD");
	static public boolean isMac = System.getProperty("os.name").contains("Mac");
	static public boolean isARM = System.getProperty("os.arch").startsWith("arm")
		|| System.getProperty("os.arch").startsWith("aarch64");
	static public boolean is64Bit = System.getProperty("os.arch").contains("64")
		|| System.getProperty("os.arch").startsWith("armv8");

	static private final Random random = new Random();
	static private File egl;
	static private File gles;
	static private File lastWorkingDir;

	public static void closeQuietly (Closeable c) {
		if (c != null) {
			try {
				c.close();
			} catch (Throwable ignored) {
			}
		}
	}

	/** Checks if the current jar/code location is inside an macOS app bundle
	 *
	 * @return True if app bundle */
	static boolean isAppBundlePath () {
		URL jarSource = ANGLELoader.class.getProtectionDomain().getCodeSource().getLocation();
		return jarSource.getPath().matches(".*/[^/]+\\.app/.*");
	}

	static String randomUUID () {
		return new UUID(random.nextLong(), random.nextLong()).toString();
	}

	public static String crc (InputStream input) {
		if (input == null) throw new IllegalArgumentException("input cannot be null.");
		CRC32 crc = new CRC32();
		byte[] buffer = new byte[4096];
		try {
			while (true) {
				int length = input.read(buffer);
				if (length == -1) break;
				crc.update(buffer, 0, length);
			}
		} catch (Exception ex) {
		} finally {
			closeQuietly(input);
		}
		return Long.toString(crc.getValue(), 16);
	}

	private static File extractFile (String sourcePath, File outFile) {
		try {
			if (!outFile.getParentFile().exists() && !outFile.getParentFile().mkdirs()) throw new GdxRuntimeException(
				"Couldn't create ANGLE native library output directory " + outFile.getParentFile().getAbsolutePath());
			OutputStream out = null;
			InputStream in = null;

			if (outFile.exists()) {
				return outFile;
			}

			try {
				out = new FileOutputStream(outFile);
				in = readFile(sourcePath);
				byte[] buffer = new byte[4096];
				while (true) {
					int length = in.read(buffer);
					if (length == -1) break;
					out.write(buffer, 0, length);
				}
				return outFile;
			} finally {
				closeQuietly(out);
				closeQuietly(in);
			}
		} catch (Throwable t) {
			throw new GdxRuntimeException("Couldn't load ANGLE shared library " + sourcePath, t);
		}
	}

	/** Returns a path to a file that can be written. Tries multiple locations and verifies writing succeeds.
	 *
	 * @return null if a writable path could not be found. */
	private static File getExtractedFile (String dirName, String fileName) {
		// Temp directory with username in path.
		File idealFile = new File(
			System.getProperty("java.io.tmpdir") + "/libgdx" + System.getProperty("user.name") + "/" + dirName, fileName);
		if (canWrite(idealFile)) return idealFile;

		// System provided temp directory.
		try {
			File file = File.createTempFile(dirName, null);
			if (file.delete()) {
				file = new File(file, fileName);
				if (canWrite(file)) return file;
			}
		} catch (IOException ignored) {
		}

		// User home.
		File file = new File(System.getProperty("user.home") + "/.libgdx/" + dirName, fileName);
		if (canWrite(file)) return file;

		// Relative directory.
		file = new File(".temp/" + dirName, fileName);
		if (canWrite(file)) return file;

		// We are running in the OS X sandbox.
		if (System.getenv("APP_SANDBOX_CONTAINER_ID") != null) return idealFile;

		return null;
	}

	/** Returns true if the parent directories of the file can be created and the file can be written. */
	private static boolean canWrite (File file) {
		File parent = file.getParentFile();
		File testFile;
		if (file.exists()) {
			if (!file.canWrite() || !canExecute(file)) return false;
			// Don't overwrite existing file just to check if we can write to directory.
			testFile = new File(parent, randomUUID().toString());
		} else {
			parent.mkdirs();
			if (!parent.isDirectory()) return false;
			testFile = file;
		}
		try {
			new FileOutputStream(testFile).close();
			if (!canExecute(testFile)) return false;
			return true;
		} catch (Throwable ex) {
			return false;
		} finally {
			testFile.delete();
		}
	}

	private static boolean canExecute (File file) {
		try {
			Method canExecute = File.class.getMethod("canExecute");
			if ((Boolean)canExecute.invoke(file)) return true;

			Method setExecutable = File.class.getMethod("setExecutable", boolean.class, boolean.class);
			setExecutable.invoke(file, true, false);

			return (Boolean)canExecute.invoke(file);
		} catch (Exception ignored) {
		}
		return false;
	}

	public static boolean isCompatible () {
		Libs sources = getSourcePaths();
		return ANGLELoader.class.getClassLoader().getResource(sources.egl) != null
			&& ANGLELoader.class.getClassLoader().getResource(sources.gles) != null;
	}

	private static InputStream readFile (String path) {
		InputStream input = ANGLELoader.class.getClassLoader().getResourceAsStream(path);
		if (input == null) throw new GdxRuntimeException("Unable to read file for extraction: " + path);
		return input;
	}

	public static void load () {
		Libs sources = getSourcePaths();
		String crc = crc(readFile(sources.egl)) + crc(readFile(sources.gles));
		egl = getExtractedFile(crc, new File(sources.egl).getName());
		gles = getExtractedFile(crc, new File(sources.gles).getName());

		if (!isMac) {
			extractFile(sources.egl, egl);
			System.load(egl.getAbsolutePath());
			extractFile(sources.gles, gles);
			System.load(gles.getAbsolutePath());
		} else {
			// On macOS, we can't preload the shared libraries. calling dlopen("path1/lib.dylib")
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
			if (isAppBundlePath()) {
				// Running inside an app bundle - do nothing
				return;
			}

			lastWorkingDir = new File(".");
			extractFile(sources.egl, new File(lastWorkingDir, egl.getName()));
			extractFile(sources.gles, new File(lastWorkingDir, gles.getName()));
		}
	}

	public static void postGlfwInit () {
		new File(lastWorkingDir, egl.getName()).delete();
		new File(lastWorkingDir, gles.getName()).delete();
	}

	private static Libs getSourcePaths () {
		String osDir = "";
		String arch = isARM ? (is64Bit ? "arm64" : "arm32") : (is64Bit ? "x64" : "x86");
		String ext = "";
		if (isWindows) {
			osDir = "windows";
			ext = ".dll";
		}
		if (isLinux) {
			osDir = "linux";
			ext = ".so";
		}
		if (isMac) {
			osDir = "macos";
			ext = ".dylib";
		}

		String dir = osDir + "/" + arch + "/angle";

		String eglSource = dir + "/libEGL" + ext;
		String glesSource = dir + "/libGLESv2" + ext;
		return new Libs(eglSource, glesSource);
	}

	private static class Libs {
		final String egl;
		final String gles;

		Libs (String egl, String gles) {
			this.egl = egl;
			this.gles = gles;
		}
	}
}
