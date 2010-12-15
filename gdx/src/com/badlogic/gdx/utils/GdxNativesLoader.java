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

package com.badlogic.gdx.utils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class GdxNativesLoader {
	static boolean nativesLoaded = false;

	/**
	 * loads the necessary libraries depending on the operating system
	 */
	public static boolean loadLibraries () {
		if (nativesLoaded) return true;
		String os = System.getProperty("os.name");
		String arch = System.getProperty("os.arch");
		boolean is64Bit = false;

		if (arch.equals("amd64")) is64Bit = true;

		String prefix = getLibraryPrefix();
		String suffix = getLibrarySuffix();
		String libName = prefix + "gdx" + (is64Bit ? "-64" : "") + suffix;
		if (!loadLibrary(libName, "/", System.getProperty("java.io.tmpdir") + File.separator)) {
			return false;
		} else {
			nativesLoaded = true;
			return true;
		}
	}

	public static String getLibraryPrefix () {
		String os = System.getProperty("os.name");

		if (os.contains("Windows"))
			return "";
		else
			return "lib";
	}

	public static String getLibrarySuffix () {
		String os = System.getProperty("os.name");
		if (os.contains("Windows")) return ".dll";
		if (os.contains("Linux")) return ".so";
		if (os.contains("Mac")) return ".dylib";
		return "";
	}

	public static boolean is64Bit () {
		String arch = System.getProperty("os.arch");
		return arch.toLowerCase().contains("amd64");
	}

	public static boolean loadLibrary (String libName, String classPath, String outputPath) {
// if (new File(outputPath + libName).exists())
// return true;

		InputStream in = null;
		BufferedOutputStream out = null;

		try {
			String tmpName = System.nanoTime() + libName;
			in = GdxNativesLoader.class.getResourceAsStream(classPath + libName);
			out = new BufferedOutputStream(new FileOutputStream(outputPath + tmpName));
			byte[] bytes = new byte[1024 * 4];
			while (true) {
				int read_bytes = in.read(bytes);
				if (read_bytes == -1) break;

				out.write(bytes, 0, read_bytes);
			}
			out.close();
			out = null;
			in.close();
			in = null;
			System.load(outputPath + tmpName);
			return true;
		} catch (Throwable t) {
			System.err.println("GdxNativesLoader: Couldn't unpack and load native '" + libName + "'");
			return false;
		} finally {
			if (out != null) try {
				out.close();
			} catch (Exception ex) {
			}
			;
			if (in != null) try {
				in.close();
			} catch (Exception ex) {
			}
		}
	}
}
