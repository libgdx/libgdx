
package com.badlogic.gdx.backends.android;

import com.badlogic.gdx.Files;

public interface AndroidFiles extends Files {

	/** This method can be called to set the version code of the APK expansion file(s) used by the application
	 *
	 * @param mainVersion - version code of the main expansion file
	 * @param patchVersion - version code of the patch expansion file
	 *
	 * @return true if the APK expansion file could be opened, false otherwise */
	boolean setAPKExpansion (int mainVersion, int patchVersion);

	/** @return The application's APK extension file */
	ZipResourceFile getExpansionFile ();
}
