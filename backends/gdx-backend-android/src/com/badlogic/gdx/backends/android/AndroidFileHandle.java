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
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.GdxRuntimeException;

/** @author mzechner
 * @author Nathan Sweet */
public class AndroidFileHandle extends FileHandle {
	// The asset manager, or null if this is not an internal file.
	final private AssetManager assets;

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
		return Gdx.files.getFileHandle(new File(file.getParent(), name).getPath(), type); //this way we can find the sibling even if it's inside the obb
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
		if (type == FileType.Internal) {
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

	public FileHandle[] list (FileFilter filter) {
		if (type == FileType.Internal) {
			try {
				String[] relativePaths = assets.list(file.getPath());
				FileHandle[] handles = new FileHandle[relativePaths.length];
				int count = 0;
				for (int i = 0, n = handles.length; i < n; i++) {
					String path = relativePaths[i];
					FileHandle child = new AndroidFileHandle(assets, new File(file, path), type);
					if (!filter.accept(child.file())) continue;
					handles[count] = child;
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
		return super.list(filter);
	}

	public FileHandle[] list (FilenameFilter filter) {
		if (type == FileType.Internal) {
			try {
				String[] relativePaths = assets.list(file.getPath());
				FileHandle[] handles = new FileHandle[relativePaths.length];
				int count = 0;
				for (int i = 0, n = handles.length; i < n; i++) {
					String path = relativePaths[i];
					if (!filter.accept(file, path)) continue;
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
		return super.list(filter);
	}

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

	public boolean exists () {
		if (type == FileType.Internal) {
			String fileName = file.getPath();
			try {
				assets.open(fileName).close(); // Check if file exists.
				return true;
			} catch (Exception ex) {
				// This is SUPER slow! but we need it for directories.
				try {
					return assets.list(fileName).length > 0;
				} catch (Exception ignored) {
				}
				return false;
			}
		}
		return super.exists();
	}

	public long length () {
		if (type == FileType.Internal) {
			AssetFileDescriptor fileDescriptor = null;
			try {
				fileDescriptor = assets.openFd(file.getPath());
				return fileDescriptor.getLength();
			} catch (IOException ignored) {
			} finally {
				if (fileDescriptor != null) {
					try {
						fileDescriptor.close();
					} catch (IOException e) {
					}
					;
				}
			}
		}
		return super.length();
	}

	public long lastModified () {
		return super.lastModified();
	}

	public File file () {
		if (type == FileType.Local) return new File(Gdx.files.getLocalStoragePath(), file.getPath());
		return super.file();
	}

	/**
	 * @return an AssetFileDescriptor for this file or null if the file is not of type Internal
	 * @throws IOException - thrown by AssetManager.openFd()
	 */
	public AssetFileDescriptor getAssetFileDescriptor() throws IOException {
		return assets != null ? assets.openFd(path()) : null;
	}
}
