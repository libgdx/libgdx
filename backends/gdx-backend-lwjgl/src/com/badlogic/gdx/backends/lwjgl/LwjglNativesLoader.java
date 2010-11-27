package com.badlogic.gdx.backends.lwjgl;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import com.badlogic.gdx.Version;
import com.badlogic.gdx.utils.GdxRuntimeException;

final class LwjglNativesLoader {
	static void load() {
		System.setProperty("org.lwjgl.input.Mouse.allowNegativeMouseCoords", "true");
		Version.loadLibrary();

		String os = System.getProperty("os.name");
		String arch = System.getProperty("os.arch");
		boolean is64Bit = false;

		if (arch.equals("amd64")) is64Bit = true;

		if (os.contains("Windows")) loadLibrariesWindows(is64Bit);
		if (os.contains("Linux")) loadLibrariesLinux(is64Bit);
		if (os.contains("Mac")) loadLibrariesMac();

		
		//System.setProperty("org.lwjgl.librarypath", new File(System.getProperty("java.io.tmpdir")).getAbsolutePath());
	}

	private static void loadLibrariesWindows (boolean is64Bit) {
		String[] libNames = null;
		if (is64Bit)
			libNames = new String[] {"OpenAL64.dll", "lwjgl64.dll"};
		else
			libNames = new String[] {"OpenAL32.dll", "lwjgl.dll"};

		for (String libName : libNames)
			loadLibrary(libName, "/native/windows/", System.getProperty("java.io.tmpdir") + File.separator);
	}

	private static void loadLibrariesLinux (boolean is64Bit) {
		String[] libNames = null;
		if (is64Bit)
			libNames = new String[] {"libopenal64.so", "liblwjgl64.so"};
		else
			libNames = new String[] {"libopenal.so", "liblwjgl.so"};

		for (String libName : libNames)
			loadLibrary(libName, "/native/linux/", System.getProperty("java.io.tmpdir") + File.separator);
	}

	private static void loadLibrariesMac () {
		throw new GdxRuntimeException("loading native libs on Mac OS X not supported, mail contact@badlogicgames.com");
	}

	private static void loadLibrary (String libName, String classPath, String outputPath) {
		if (new File(outputPath + libName).exists()) return;
		
		InputStream in = null;
		BufferedOutputStream out = null;

		try {
			in = LwjglApplication.class.getResourceAsStream(classPath + libName);
			out = new BufferedOutputStream(new FileOutputStream(outputPath + libName));
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
		} catch (Throwable t) {
			//throw new GdxRuntimeException("Couldn't load lwjgl native, " + libName, t);
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
