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
	protected String sdcard = Environment.getExternalStorageDirectory().getAbsolutePath() + "/";
	protected final String localpath;

	protected boolean legacyWriting = true;
	protected final AssetManager assets;

	public AndroidFiles (AssetManager assets, Context context) {
		this.assets = assets;
		setupExternalStorage(context);
		localpath = sdcard;
	}

	public AndroidFiles (AssetManager assets, String localpath, Context context) {
		this.assets = assets;
		this.localpath = localpath.endsWith("/") ? localpath : localpath + "/";
		setupExternalStorage(context);
	}

	private void setupExternalStorage (Context context) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			// Android 4.4 'KitKat' and above have new external storage guidelines 
			// Some devices don't respect this, so we have a nice little patch to detect that.
			
			File testFile = new File(this.sdcard, ".gdxexternaltest");
			
			if (testFile.exists()) {
				testFile.delete();
			}
			
			try {
				this.legacyWriting = testFile.createNewFile();
			} catch (IOException e) {
				// Legacy writing is not available.
				this.legacyWriting = false;
			}
			
			if (!this.legacyWriting) {
				// Gets an application-specific directory that is writable on the external storage.
				File externalDir = context.getExternalFilesDir(null);
				if (!externalDir.exists()) {
					externalDir.mkdirs();
				}
				
				this.sdcard = externalDir.getAbsolutePath() + "/";
				
				try {
					testFile.delete();
				} catch (Exception e) {
					// Ignored
				}
			}
		}
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
}
