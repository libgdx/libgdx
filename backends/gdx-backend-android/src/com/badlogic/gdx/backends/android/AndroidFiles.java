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

import android.content.res.AssetManager;
import android.os.Environment;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.GdxRuntimeException;

import java.io.*;

/**
 * An implementation of the {@link Files} interface for Android. External files are stored and accessed relative to
 * Environment.getExternalStorageDirectory().getAbsolutePath(). Internal files are accessed relative to the assets directory.
 * 
 * @author mzechner
 * 
 */
public class AndroidFiles implements Files {
	/** external storage path **/
	protected final String sdcard = Environment.getExternalStorageDirectory().getAbsolutePath() + "/";

	/** asset manager **/
	protected final AssetManager assets;

	public AndroidFiles (AssetManager assets) {
		this.assets = assets;
	}

	/**
	 * @return the asset manager.
	 */
	protected AssetManager getAssetManager () {
		return assets;
	}

	private InputStream readExternalFile (String fileName) {
		FileInputStream in = null;

		try {
			in = new FileInputStream(sdcard + fileName);
		} catch (FileNotFoundException ex) {
			throw new GdxRuntimeException("File not found: " + fileName + " (" + FileType.External + ")", ex);
		}

		return in;
	}

	private InputStream readInternalFile (String fileName) {
		InputStream in = null;
		try {
			in = assets.open(fileName);
		} catch (Exception ex) {
			throw new GdxRuntimeException("Unable to read file: " + fileName + " (" + FileType.Internal + ")", ex);
		}

		return in;
	}

	private OutputStream writeExternalFile (String filename) {
		FileOutputStream out = null;

		try {
			out = new FileOutputStream(sdcard + filename);
		} catch (FileNotFoundException ex) {
			throw new GdxRuntimeException("File not found: " + filename + " (" + FileType.External + ")", ex);
		}

		return out;
	}

	private InputStream readAbsoluteFile (String filename) {
		FileInputStream in = null;

		try {
			in = new FileInputStream(filename);
		} catch (FileNotFoundException ex) {
			throw new GdxRuntimeException("File not found: " + filename + " (" + FileType.Absolute + ")", ex);
		}

		return in;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override public FileHandle getFileHandle (String filename, FileType type) {
		if (type == FileType.Internal) {
			boolean exists = true;

			try {
				InputStream in = assets.open(filename);
				in.close();
			} catch (Exception ex) {
				exists = false;
			}

			if (!exists)
				throw new GdxRuntimeException("File not found: " + filename + " (" + type + ")");
			else
				return new AndroidFileHandle(assets, filename);
		}

		if (type == FileType.External) {
			if (new File(sdcard + filename).exists() == false)
				throw new GdxRuntimeException("File not found: " + filename + " (" + type + ")");
			else
				return new AndroidFileHandle(null, sdcard + filename);
		} else {
			if (new File(filename).exists() == false)
				throw new GdxRuntimeException("File not found: " + filename + " (" + type + ")");
			else
				return new AndroidFileHandle(null, filename);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override public String[] listDirectory (String directory, FileType type) {
		if (type == FileType.Internal) {
			try {
				return assets.list(directory);
			} catch (Exception ex) {
				throw new GdxRuntimeException("Unable to open directory: " + directory);
			}
		}

		if (type == FileType.External) {
			if (new File(sdcard + directory).exists() == false)
				throw new GdxRuntimeException("Unable to open directory: " + directory);
			else
				return new File(sdcard + directory).list();
		} else {
			if (new File(directory).exists() == false)
				throw new GdxRuntimeException("Unable to open directory: " + directory);
			else
				return new File(directory).list();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override public boolean makeDirectory (String directory, FileType type) {
		if (type == FileType.Internal) return false;

		if (type == FileType.External)
			return new File(sdcard + directory).mkdirs();
		else
			return new File(directory).mkdirs();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override public InputStream readFile (String fileName, FileType type) {
		if (type == FileType.Internal) return readInternalFile(fileName);
		if (type == FileType.External)
			return readExternalFile(fileName);
		else
			return readAbsoluteFile(fileName);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override public OutputStream writeFile (String filename, FileType type) {
		if (type == FileType.Internal) return null;
		if (type == FileType.External)
			return writeExternalFile(filename);
		else {
			FileOutputStream out = null;

			try {
				out = new FileOutputStream(filename);
			} catch (FileNotFoundException ex) {
				throw new GdxRuntimeException("File not found: " + filename + " (" + type + ")", ex);
			}

			return out;
		}
	}

	@Override
	public String getExternalStoragePath() {
		return sdcard;
	}

	@Override
	public boolean isExternalStorageAvailable() {
		return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
	}
}
