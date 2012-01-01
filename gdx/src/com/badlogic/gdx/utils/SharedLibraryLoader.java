package com.badlogic.gdx.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.badlogic.gdx.jnigen.test.MyJniClass;

/**
 * Loads shared libraries from a natives jar file (desktop) or arm folders (Android). For desktop projects,
 * have the natives jar in the classpath, for Android projects put the shared libraries in the libs/armeabi
 * and libs/armeabi-v7a folders.
 *
 * @author mzechner
 *
 */
public class SharedLibraryLoader {
	private static Set<String> loadedLibraries = new HashSet<String>();
	private String nativesJar;
	
	public SharedLibraryLoader() {
	}
	
	/**
	 * Fetches the natives from the given natives jar file. Used
	 * for testing a shared lib on the fly, see {@link MyJniClass}.
	 * @param nativesJar
	 */
	public SharedLibraryLoader(String nativesJar) {
		this.nativesJar = nativesJar;
	}
	
	private String crc (String nativeFile) {
		InputStream input = SharedLibraryLoader.class.getResourceAsStream("/" + nativeFile);
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
	
	private boolean loadLibrary (String sharedLibName) {
		String path = extractLibrary(sharedLibName);
		if (path != null) System.load(path);
		return path != null;
	}

	private String extractLibrary (String sharedLibName) {
		File nativesDir = new File(System.getProperty("java.io.tmpdir") + "/jnigen/" + crc(sharedLibName));
		File nativeFile = new File(nativesDir, sharedLibName);
		try {
			// Extract native from classpath to temp dir.
			InputStream input = null;
			if(nativesJar == null) input = SharedLibraryLoader.class.getResourceAsStream("/" + sharedLibName);
			else input = getFromJar(nativesJar, sharedLibName);
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
			ex.printStackTrace();
			throw new RuntimeException(ex);
		}
		return nativeFile.exists() ? nativeFile.getAbsolutePath() : null;
	}

	private InputStream getFromJar(String jarFile, String sharedLibrary) throws IOException {
		ZipFile file = new ZipFile(nativesJar);
		ZipEntry entry = file.getEntry(sharedLibrary);
		return file.getInputStream(entry);
	}

	/**
	 * Loads a shared library with the given name for the platform the application
	 * is running on. The name should not contain a prefix (e.g. 'lib') or suffix (e.g. '.dll).
	 * @param sharedLibName
	 */
	public synchronized void load (String sharedLibName) {
		if (loadedLibraries.contains(sharedLibName)) return;
		
		boolean isWindows = System.getProperty("os.name").contains("Windows");
		boolean isLinux = System.getProperty("os.name").contains("Linux");
		boolean isMac = System.getProperty("os.name").contains("Mac");
		boolean isAndroid = false;
		boolean is64Bit = System.getProperty("os.arch").equals("amd64");
		String vm = System.getProperty("java.vm.name");
		if(vm != null && vm.contains("Dalvik")) {
			isAndroid = true;
			isWindows = false;
			isLinux = false;
			isMac = false;
			is64Bit = false;
		}
		
		boolean loaded = false;
		if(isWindows) {
			if(!is64Bit) loaded = loadLibrary(sharedLibName + ".dll");
			else loaded = loadLibrary(sharedLibName + "64.dll");
		}
		if(isLinux) {
			if(!is64Bit) loaded = loadLibrary("lib" + sharedLibName + ".so");
			else loaded = loadLibrary("lib" + sharedLibName + "64.so");
		}
		if(isMac) {
			loaded = loadLibrary("lib" + sharedLibName + ".dylib");
		}
		if(isAndroid) {
			System.loadLibrary(sharedLibName);
			loaded = true;
		}
		if(loaded) {
			loadedLibraries.add(sharedLibName);
		} else {
			throw new RuntimeException("Couldn't load shared library: '" + sharedLibName + "' for target " + System.getProperty("os.name") + ", " + (is64Bit?"64-bit":"32-bit"));
		}
	}
}
