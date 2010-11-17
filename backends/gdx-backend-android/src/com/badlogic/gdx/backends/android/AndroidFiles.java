/*******************************************************************************
 * Copyright 2010 Mario Zechner (contact@badlogicgames.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.backends.android;

import java.io.File;
import java.io.InputStream;

import android.content.res.AssetManager;
import android.os.Environment;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.GdxRuntimeException;

/**
 * An implementation of the {@link Files} interface for Android. External files are stored and accessed relative to
 * Environment.getExternalStorageDirectory().getAbsolutePath(). Internal files are accessed relative to the assets directory.
 * 
 * @author mzechner
 * 
 */
public class AndroidFiles implements Files {
	protected final String sdcard = Environment.getExternalStorageDirectory().getAbsolutePath() + "/";
	protected final AssetManager assets;

	public AndroidFiles (AssetManager assets) {
		this.assets = assets;
	}

	@Override public FileHandle getFileHandle (String fileName, FileType type) {
		File file;
		if (type == FileType.Internal) {
			file = new File(fileName);
			if (FileHandle.class.getResourceAsStream("/" + fileName) == null) {
				try {
					InputStream in = assets.open(fileName);
					in.close();
				} catch (Exception ignored) {
				}
			}
		} else {
			if (type == FileType.External)
				file = new File(sdcard + fileName);
			else
				file = new File(fileName);
		}
		return new AndroidFileHandle(assets, file, type);
	}

	@Override public FileHandle internal (String path) {
		return getFileHandle(path, FileType.Internal);
	}

	@Override public FileHandle external (String path) {
		return getFileHandle(path, FileType.External);
	}

	@Override public FileHandle absolute (String path) {
		return getFileHandle(path, FileType.Absolute);
	}

	@Override public String getExternalStoragePath () {
		return sdcard;
	}

	@Override public boolean isExternalStorageAvailable () {
		return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
	}
}
