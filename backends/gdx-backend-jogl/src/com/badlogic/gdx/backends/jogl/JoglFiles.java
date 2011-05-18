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
package com.badlogic.gdx.backends.jogl;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.files.FileHandle;

/**
 * @author mzechner
 * @author Nathan Sweet
 */
final class JoglFiles implements Files {
	private final String externalPath = System.getProperty("user.home") + "/";

	@Override public FileHandle getFileHandle (String fileName, FileType type) {
		return new JoglFileHandle(fileName, type);
	}

	@Override public FileHandle classpath (String path) {
		return new JoglFileHandle(path, FileType.Classpath);
	}

	@Override public FileHandle internal (String path) {
		return new JoglFileHandle(path, FileType.Internal);
	}

	@Override public FileHandle external (String path) {
		return new JoglFileHandle(path, FileType.External);
	}

	@Override public FileHandle absolute (String path) {
		return new JoglFileHandle(path, FileType.Absolute);
	}

	@Override public String getExternalStoragePath () {
		return externalPath;
	}

	@Override public boolean isExternalStorageAvailable () {
		return true;
	}
}
