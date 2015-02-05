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

package com.badlogic.gdx.backends.lwjgl3;

import java.io.File;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.files.FileHandle;

/** @author mzechner
 * @author Nathan Sweet */
public final class Lwjgl3Files implements Files {
	static public final String externalPath = System.getProperty("user.home") + File.separator;
	static public final String localPath = new File("").getAbsolutePath() + File.separator;
	
	public FileHandle getFileHandle (String fileName, FileType type) {
		return new Lwjgl3FileHandle(fileName, type);
	}

	public FileHandle classpath (String path) {
		return new Lwjgl3FileHandle(path, FileType.Classpath);
	}

	public FileHandle internal (String path) {
		return new Lwjgl3FileHandle(path, FileType.Internal);
	}

	public FileHandle external (String path) {
		return new Lwjgl3FileHandle(path, FileType.External);
	}

	public FileHandle absolute (String path) {
		return new Lwjgl3FileHandle(path, FileType.Absolute);
	}

	public FileHandle local (String path) {
		return new Lwjgl3FileHandle(path, FileType.Local);
	}

	public String getExternalStoragePath () {
		return externalPath;
	}

	public boolean isExternalStorageAvailable () {
		return true;
	}

	public String getLocalStoragePath () {
		return localPath;
	}

	public boolean isLocalStorageAvailable () {
		return true;
	}
}
