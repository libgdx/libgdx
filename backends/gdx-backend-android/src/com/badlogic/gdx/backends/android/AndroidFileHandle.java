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
import java.io.IOException;
import java.io.InputStream;

import android.content.res.AssetManager;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.GdxRuntimeException;

/**
 * @author mzechner
 * @author Nathan Sweet <misc@n4te.com>
 */
public class AndroidFileHandle extends FileHandle {
	// The asset manager, or null if this is not an internal file.
	final AssetManager assets;

	AndroidFileHandle (AssetManager assets, String fileName, FileType type) {
		this.assets = assets;
		this.type = type;

		switch (type) {
		case Classpath:
			if (FileHandle.class.getResourceAsStream("/" + fileName) == null)
				throw new GdxRuntimeException("File not found: " + fileName + " (" + type + ")");
			file = new File("/" + fileName);
			break;
		case Internal:
			try {
				assets.open(fileName).close();
			} catch (Exception ex) {
				throw new GdxRuntimeException("File not found: " + file + " (" + type + ")", ex);
			}
			file = new File(fileName);
			break;
		case External:
			file = new File(Gdx.files.getExternalStoragePath() + fileName);
			break;
		case Absolute:
			file = new File(fileName);
			break;
		default:
			throw new IllegalArgumentException("Unknown type: " + type);
		}
	}

	AndroidFileHandle (AssetManager manager, File file, FileType type) {
		this.assets = manager;
		this.file = file;
		this.type = type;

		switch (type) {
		case Classpath:
			if (FileHandle.class.getResourceAsStream(file.getPath().replace('\\', '/')) == null)
				throw new GdxRuntimeException("File not found: " + file + " (" + type + ")");
			break;
		case Internal:
			try {
				assets.open(file.getPath()).close();
			} catch (Exception ex) {
				throw new GdxRuntimeException("File not found: " + file + " (" + type + ")", ex);
			}
			break;
		}
	}

	public FileHandle child (String name) {
		return new AndroidFileHandle(assets, new File(file, name), type);
	}

	public FileHandle parent () {
		File parent = file.getParentFile();
		if (parent == null) {
			switch (type) {
			case Classpath:
			case Absolute:
				parent = new File("/");
				break;
			case Internal:
				parent = new File("");
				break;
			case External:
				parent = new File(Gdx.files.getExternalStoragePath());
				break;
			}
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

	public boolean isDirectory () {
		if (type == FileType.Internal) {
			try {
				assets.list(file.getPath());
				return true;
			} catch (Exception ex) {
				return false;
			}
		}
		return super.isDirectory();
	}
}
