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

package com.badlogic.gdx.backends.android;

import java.io.IOException;

import android.content.res.AssetManager;
import android.os.Environment;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.GdxRuntimeException;

/** @author mzechner
 * @author Nathan Sweet */
public class AndroidFiles implements Files {
	protected final String sdcard = Environment.getExternalStorageDirectory().getAbsolutePath() + "/";
	protected final String localpath;

	protected final AssetManager assets;
	private ZipResourceFile expansionFile = null;

	public AndroidFiles (AssetManager assets) {
		this.assets = assets;
		localpath = sdcard;
	}

	public AndroidFiles (AssetManager assets, String localpath) {
		this.assets = assets;
		this.localpath = localpath.endsWith("/") ? localpath : localpath + "/";
	}

	@Override
	public FileHandle getFileHandle (String path, FileType type) {
		FileHandle handle = new AndroidFileHandle(type == FileType.Internal ? assets : null, path, type);
		if (expansionFile != null && type == FileType.Internal) handle = getZipFileHandleIfExists(handle, path);
		return handle;
	}

	private FileHandle getZipFileHandleIfExists (FileHandle handle, String path) {
		try {
			assets.open(path).close(); // Check if file exists.
			return handle;
		} catch (Exception ex) {
			// try APK expansion instead
			FileHandle zipHandle = new AndroidZipFileHandle(path);
			if (!zipHandle.isDirectory())
				return zipHandle;
			else if (zipHandle.exists()) return zipHandle;
		}
		return handle;
	}

	@Override
	public FileHandle classpath (String path) {
		return new AndroidFileHandle(null, path, FileType.Classpath);
	}

	@Override
	public FileHandle internal (String path) {
		FileHandle handle = new AndroidFileHandle(assets, path, FileType.Internal);
		if (expansionFile != null) handle = getZipFileHandleIfExists(handle, path);
		return handle;
	}

	@Override
	public FileHandle external (String path) {
		return new AndroidFileHandle(null, path, FileType.External);
	}

	@Override
	public FileHandle absolute (String path) {
		return new AndroidFileHandle(null, path, FileType.Absolute);
	}

	@Override
	public FileHandle local (String path) {
		return new AndroidFileHandle(null, path, FileType.Local);
	}

	@Override
	public String getExternalStoragePath () {
		return sdcard;
	}

	@Override
	public boolean isExternalStorageAvailable () {
		return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
	}

	@Override
	public String getLocalStoragePath () {
		return localpath;
	}

	@Override
	public boolean isLocalStorageAvailable () {
		return true;
	}

	/**
	 * This method can be called to set the version code of the APK expansion
	 * file(s) used by the application
	 * 
	 * @param mainVersion
	 *            - version code of the main expansion file
	 * @param patchVersion
	 *            - version code of the patch expansion file
	 * 
	 * @return true if the APK expansion file could be opened, false otherwise
	 */
	public boolean setAPKExpansion(int mainVersion, int patchVersion) {
		try {
			expansionFile = APKExpansionSupport.getAPKExpansionZipFile(
					((AndroidApplication) Gdx.app).getBaseContext(),
					mainVersion, patchVersion);
		} catch (IOException ex) {
			throw new GdxRuntimeException("APK expansion main version " + mainVersion + " or patch version " + patchVersion + " couldn't be opened!");
		}
		return expansionFile != null;
	}

	/** @return The application's APK extension file */
	public ZipResourceFile getExpansionFile() {
		return expansionFile;
	}
}
