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

import java.io.File;
import java.io.IOException;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Build;
import android.os.Environment;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.files.FileHandle;

/** @author mzechner
 * @author Nathan Sweet */
public class AndroidFiles implements Files {
	protected String sdcard = null;
	protected final String localpath;
	
	protected boolean legacyWriting = true;
	protected final AssetManager assets;
	protected Context context;

	public AndroidFiles (AssetManager assets, Context context) {
		this.assets = assets;
		this.context = context;
		setupExternalStorage(this.context);
		localpath = sdcard;
	}

	public AndroidFiles (AssetManager assets, String localpath, Context context) {
		this.assets = assets;
		this.context = context;
		this.localpath = localpath.endsWith("/") ? localpath : localpath + "/";
		setupExternalStorage(this.context);
	}

	private void setupExternalStorage (Context context) {
		// Gets an application-specific directory that is writable on the external storage.
		File externalDir = context.getExternalFilesDir(null);
		
		if (externalDir == null) {
			this.sdcard = null;
		}
		
		if (!externalDir.exists()) {
			externalDir.mkdirs();
		}
		this.sdcard = externalDir.getAbsolutePath() + "/";
	}

	@Override
	public FileHandle getFileHandle (String path, FileType type) {
		return new AndroidFileHandle(type == FileType.Internal ? assets : null, path, type);
	}

	@Override
	public FileHandle classpath (String path) {
		return new AndroidFileHandle(null, path, FileType.Classpath);
	}

	@Override
	public FileHandle internal (String path) {
		return new AndroidFileHandle(assets, path, FileType.Internal);
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
		return this.sdcard;
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
	
	protected String getModernSdcardPath() {
		if (this.sdcard == null && this.isExternalStorageAvailable()) {
			this.setupExternalStorage(this.context);
		}
		return this.sdcard;
	}
}
