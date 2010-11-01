package com.badlogic.gdx.backends.jogl;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.sun.opengl.impl.NativeLibLoader;

public class JoglNativesLoader {
	static boolean nativesLoaded = false;
	
	/**
	 * loads the necessary libraries depending on the operating system
	 */
	static void loadLibraries() {
		if (nativesLoaded)
			return;

		NativeLibLoader.disableLoading();
		com.sun.gluegen.runtime.NativeLibLoader.disableLoading();
		// By wkien: On some systems (read: mine) jogl_awt would not find its
		// dependency jawt if not loaded before
		System.loadLibrary("jawt");
		loadLibrary("gluegen-rt");
		loadLibrary("jogl_awt");
		loadLibrary("jogl");

		nativesLoaded = true;
	}

	/**
	 * helper method to load a specific library in an operation system dependant
	 * manner
	 * 
	 * @param resource
	 *            the name of the resource
	 */
	private static void loadLibrary(String resource) {
		String package_path = "/javax/media/";
		String library = "";

		String os = System.getProperty("os.name");
		String arch = System.getProperty("os.arch");

		if (os.contains("Windows")) {
			if (!arch.equals("amd64"))
				library = resource + "-win32.dll";
			else {
				library = resource + "-win64.dll";
			}
		}

		if (os.contains("Linux")) {
			if (!arch.equals("amd64"))
				library = "lib" + resource + "-linux32.so";
			else
				library = "lib" + resource + "-linux64.so";
		}

		if (os.contains("Mac")) {
			library = "lib" + resource + ".jnilib";
		}

		String so = System.getProperty("java.io.tmpdir") + "/"
				+ System.nanoTime() + library;
		InputStream in = JoglGraphics.class.getResourceAsStream(package_path
				+ library);
		if (in == null)
			throw new RuntimeException("couldn't find " + library
					+ " in jar file.");

		try {
			BufferedOutputStream out = new BufferedOutputStream(
					new FileOutputStream(so));
			byte[] bytes = new byte[1024 * 4];
			while (true) {
				int read_bytes = in.read(bytes);
				if (read_bytes == -1)
					break;

				out.write(bytes, 0, read_bytes);
			}
			out.close();
			in.close();
			System.load(so);
		} catch (FileNotFoundException e) {
			throw new RuntimeException("couldn't write " + library
					+ " to temporary file " + so);
		} catch (IOException e) {
			throw new RuntimeException("couldn't write " + library
					+ " to temporary file " + so);
		}
	}
}
