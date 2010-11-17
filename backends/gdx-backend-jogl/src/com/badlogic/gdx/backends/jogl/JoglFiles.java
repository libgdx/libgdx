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

package com.badlogic.gdx.backends.jogl;

import java.io.File;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.GdxRuntimeException;

/**
 * Implementation for a desktop application of {@link Files}. Internal resources are relative to the application root directory,
 * external files are relative to the user's home directory.
 *
 * @author mzechner
 *
 */
final class JoglFiles implements Files {
	private final String externalPath = System.getProperty("user.home") + "/";

	@Override public FileHandle getFileHandle (String fileName, FileType type) {
		File file;
		if (type == FileType.External)
			file = new File(this.externalPath + fileName);
		else if (type == FileType.Internal) {
			file = new File(fileName);
			if (FileHandle.class.getResourceAsStream("/" + fileName) == null && !file.exists())
				throw new GdxRuntimeException("File not found: " + fileName + " (" + type + ")");
		}
		return new JoglFileHandle(new File(fileName), type);
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
		return externalPath;
	}

	@Override public boolean isExternalStorageAvailable () {
		return true;
	}
}
