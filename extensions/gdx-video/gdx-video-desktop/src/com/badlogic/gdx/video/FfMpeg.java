package com.badlogic.gdx.video;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.badlogic.gdx.jnigen.JniGenSharedLibraryLoader;
import com.badlogic.gdx.jnigen.SharedLibraryFinder;

/**
 * This class manages the loading of the native libraries that wrap FFMpeg. It allows changing the path from which it
 * loads the libraries, and defaults to loading the file from jar containing the class.
 *
 * @author Rob Bogie <rob.bogie@codepoke.net>
 *
 */
public class FfMpeg {
	public static final String NATIVE_LIBRARY_NAME = "gdx-video-desktop";

	private static boolean loaded = false;
	private static String libraryPath = "";

	/**
	 * This will set the path in which it tries to find the native library.
	 *
	 * @param path
	 *            The path on which the library can be found. If it is null or an empty string, the default location
	 *            will be used. This is usually a SteamJavaNatives folder inside the jar.
	 */
	public static void setLibraryFilePath(String path) {
		libraryPath = path;
	}

	/**
	 * This method will load the libraries from the path given with setLibraryFilePath.
	 *
	 * @return whether loading was succesfull
	 */
	public static boolean loadLibraries() {
		if (loaded) {
			return true;
		}

		if (libraryPath == null || libraryPath.equals("")) {
			libraryPath = System.getProperty("java.io.tmpdir") + "/" + NATIVE_LIBRARY_NAME + "-java-natives/" + NATIVE_LIBRARY_NAME
							+ "-natives.jar";
			File outputFile = new File(libraryPath);
			outputFile.getParentFile()
						.mkdirs();

			InputStream input = FfMpeg.class.getResourceAsStream("/" + NATIVE_LIBRARY_NAME + "-natives.jar");
			if (input == null) {
				return false;
			}
			try {
				FileOutputStream output = new FileOutputStream(outputFile);
				int size = 0;
				while (size != -1) {
					byte[] buffer = new byte[4048];
					size = input.read(buffer);
					if (size > 0) {
						output.write(buffer, 0, size);
					}
				}
				input.close();
				output.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		JniGenSharedLibraryLoader libLoader = new JniGenSharedLibraryLoader(libraryPath, new SharedLibraryFinder() {
			@Override
			public String getSharedLibraryNameWindows(String sharedLibName, boolean is64Bit, ZipFile nativesJar) {
				Enumeration<? extends ZipEntry> entries = nativesJar.entries();
				while (entries.hasMoreElements()) {
					ZipEntry entry = entries.nextElement();
					String filename = entry.getName();

					if (filename.equals(sharedLibName + (is64Bit ? "64.dll" : ".dll"))
						|| filename.matches("windows" + (is64Bit ? "64" : "32") + "\\/" + sharedLibName + "[-]?\\d+\\.dll*")) {
						return filename;
					}
				}

				return null;
			}

			@Override
			public String getSharedLibraryNameLinux(String sharedLibName, boolean is64Bit, ZipFile nativesJar) {
				Enumeration<? extends ZipEntry> entries = nativesJar.entries();
				while (entries.hasMoreElements()) {
					ZipEntry entry = entries.nextElement();
					String filename = entry.getName();

					if (filename.equals("lib" + sharedLibName + (is64Bit ? "64.so" : ".so"))
						|| filename.matches("linux" + (is64Bit ? "64" : "32") + "\\/lib" + sharedLibName + "\\.so[.\\d]*")) {
						return filename;
					}
				}

				return null;
			}

			@Override
			public String getSharedLibraryNameMac(String sharedLibName, boolean is64Bit, ZipFile nativesJar) {
				return null;
			}

			@Override
			public String getSharedLibraryNameAndroid(String sharedLibName, ZipFile nativesJar) {
				return null;
			}
		});
		try {

			if (System.getProperty("os.name")
						.startsWith("Windows")) {
				libLoader.load("libwinpthread");
			}
			libLoader.load("avutil");
			libLoader.load("avcodec");
			libLoader.load("avformat");
			libLoader.load("swresample");
			libLoader.load("swscale");
			libLoader.load(NATIVE_LIBRARY_NAME);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			loaded = false;
			return false;
		}
		loaded = true;
		register();
		return true;
	}

	/**
	 * This tells whether the native libraries are already loaded.
	 *
	 * @return Whether the native libraries are already loaded.
	 */
	public static boolean isLoaded() {
		return loaded;
	}

	public static void setDebugLogging(boolean debugLogging) {
		if (!loaded) {
			if (!loadLibraries()) {
				return;
			}
		}
		setDebugLoggingNative(debugLogging);
	}

	/*
	 * Native functions
	 * @formatter:off
	 */

	/*JNI
	 	extern "C"
	 	{
	 	//This makes certain C libraries usable for ffmpeg
	 	#define __STDC_CONSTANT_MACROS
		#include <libavcodec/avcodec.h>
		#include <libavformat/avformat.h>
		#include <libswscale/swscale.h>
		}
		#include "Utilities.h"
	 */

	private native static void register();/*
		av_register_all();
		logDebug("av_register_all() called\n");
	 */

	/**
	 * This function can be used to turn on/off debug logging of the native code
	 * @param debugLogging whether logging should be turned on or off
	 */
	private native static void setDebugLoggingNative(boolean debugLogging);/*
		debug(debugLogging);
	 */
}
