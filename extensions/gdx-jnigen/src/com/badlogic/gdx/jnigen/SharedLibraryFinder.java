
package com.badlogic.gdx.jnigen;

import java.util.zip.ZipFile;

public interface SharedLibraryFinder {
	String getSharedLibraryNameWindows (String sharedLibName, boolean is64Bit, ZipFile nativesJar);

	String getSharedLibraryNameLinux (String sharedLibName, boolean is64Bit, ZipFile nativesJar);

	String getSharedLibraryNameMac (String sharedLibName, ZipFile nativesJar);

	String getSharedLibraryNameAndroid (String sharedLibName, ZipFile nativesJar);
}
