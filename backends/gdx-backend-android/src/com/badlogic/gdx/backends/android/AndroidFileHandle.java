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
import java.io.InputStream;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.GdxRuntimeException;

/**
 * Performs libGDX file operations against an instance of
 * `android.content.res.AssetManager` when possible; acts like "default libGDX"
 * otherwise 
 * 
 * @author mzechner
 * @author Nathan Sweet
 *
 */
public class AndroidFileHandle extends FileHandle {
	// The asset manager, or null if this is not an internal file.
	final AssetManager assets;

	AndroidFileHandle (AssetManager assets, String fileName, FileType type) {
		super(fileName.replace('\\', '/'), type);
		this.assets = assets;
	}

	AndroidFileHandle (AssetManager assets, File file, FileType type) {
		super(file, type);
		this.assets = assets;
	}

	public FileHandle child (String name) {
		name = name.replace('\\', '/');
		if (file.getPath().length() == 0) return new AndroidFileHandle(assets, new File(name), type);
		return new AndroidFileHandle(assets, new File(file, name), type);
	}

	public FileHandle sibling (String name) {
		name = name.replace('\\', '/');
		if (file.getPath().length() == 0) throw new GdxRuntimeException("Cannot get the sibling of the root.");
		return new AndroidFileHandle(assets, new File(file.getParent(), name), type);
	}

	public FileHandle parent () {
		File parent = file.getParentFile();
		if (parent == null) {
			if (type == FileType.Absolute)
				parent = new File("/");
			else
				parent = new File("");
		}
		return new AndroidFileHandle(assets, parent, type);
	}

	public InputStream read () {
		if (type == FileType.Internal && existsAndroidAsset()) {
			try {
				return assets.open(file.getPath());
			} catch (IOException ex) {
				throw new GdxRuntimeException("Error reading file: " + file + " (" + type + ")", ex);
			}
		}
		return super.read();
	}

	public FileHandle[] list () {
		if (type == FileType.Internal) {
			try {
				String[] relativePaths = assets.list(file.getPath());
				FileHandle[] handles = new FileHandle[relativePaths.length];
				for (int i = 0, n = handles.length; i < n; i++)
					handles[i] = new AndroidFileHandle(assets, new File(file, relativePaths[i]), type);
				return handles;
			} catch (Exception ex) {
				throw new GdxRuntimeException("Error listing children: " + file + " (" + type + ")", ex);
			}
		}
		return super.list();
	}

	/**
	 * If the file isn't an asset - we're going to pretend that it's classpath.
	 * There's no need to do anything special to handle "fallback to classpath" here
	 */
	public FileHandle[] list (String suffix) {
		if (type == FileType.Internal) {
			try {
				String[] relativePaths = assets.list(file.getPath());
				FileHandle[] handles = new FileHandle[relativePaths.length];
				int count = 0;
				for (int i = 0, n = handles.length; i < n; i++) {
					String path = relativePaths[i];
					if (!path.endsWith(suffix)) continue;
					handles[count] = new AndroidFileHandle(assets, new File(file, path), type);
					count++;
				}
				if (count < relativePaths.length) {
					FileHandle[] newHandles = new FileHandle[count];
					System.arraycopy(handles, 0, newHandles, 0, count);
					handles = newHandles;
				}
				return handles;
			} catch (Exception ex) {
				throw new GdxRuntimeException("Error listing children: " + file + " (" + type + ")", ex);
			}
		}
		return super.list(suffix);
	}

	/**
	 * If the file isn't an asset - we're going to pretend that it's classpath.
	 * There's no need to do anything special to handle "fallback to classpath" here
	 */
	public boolean isDirectory () {
		if (type == FileType.Internal) {
			try {
				return assets.list(file.getPath()).length > 0;
			} catch (IOException ex) {
				return false;
			}
		}
		return super.isDirectory();
	}
	
	/**
	 * Does the file exist as an asset?
	 */
	private boolean existsAndroidAsset() {
		// slow ... but most everything will be slow with files right?
		assert(type == FileType.Internal);
		assert(assets != null);
		
		String fileName = file.getPath();
		try {
			assets.open(fileName).close(); // Check if file exists.
			return true;
		} catch (Exception ex) {
		}
		return false;
	}

	/**
	 * Does the file exist at all?
	 */
	public boolean exists () {
		
		// if the file is internal, and it's either an android asset or an asset folder - it exists
		if ( type == FileType.Internal && (existsAndroidAsset() || isDirectory()) ){
			return true;
		}
		
		// if the file doesn't exist in the Android asset stuff; fallback to "normal" GDX
		return super.exists();
	}

	public long length () {
		if (type == FileType.Internal && existsAndroidAsset()) {
			AssetFileDescriptor fileDescriptor = null;
			try {
				fileDescriptor = assets.openFd(file.getPath());
				return fileDescriptor.getLength();
			} catch (IOException ignored) {
			} finally {
				if (fileDescriptor != null) {
					try { fileDescriptor.close(); } catch(IOException e) { };
				}
			}
		}
		return super.length();
	}
}
