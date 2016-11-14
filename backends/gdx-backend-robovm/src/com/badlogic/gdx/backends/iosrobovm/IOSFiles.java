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

package com.badlogic.gdx.backends.iosrobovm;

import org.robovm.apple.foundation.NSBundle;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.files.FileHandle;

public class IOSFiles implements Files {
	// TODO: Use NSSearchPathForDirectoriesInDomains instead?
	// $HOME should point to the app root dir.
	static final String appDir = System.getenv("HOME");
	static final String externalPath = appDir + "/Documents/";
	static final String localPath = appDir + "/Library/local/";
	static final String internalPath = NSBundle.getMainBundle().getBundlePath();

	public IOSFiles () {
		new FileHandle(externalPath).mkdirs();
		new FileHandle(localPath).mkdirs();
	}

	@Override
	public FileHandle getFileHandle (String fileName, FileType type) {
		return new IOSFileHandle(fileName, type);
	}

	@Override
	public FileHandle classpath (String path) {
		return new IOSFileHandle(path, FileType.Classpath);
	}

	@Override
	public FileHandle internal (String path) {
		return new IOSFileHandle(path, FileType.Internal);
	}

	@Override
	public FileHandle external (String path) {
		return new IOSFileHandle(path, FileType.External);
	}

	@Override
	public FileHandle absolute (String path) {
		return new IOSFileHandle(path, FileType.Absolute);
	}

	@Override
	public FileHandle local (String path) {
		return new IOSFileHandle(path, FileType.Local);
	}

	@Override
	public String getExternalStoragePath () {
		return externalPath;
	}

	@Override
	public boolean isExternalStorageAvailable () {
		return true;
	}

	@Override
	public String getLocalStoragePath () {
		return localPath;
	}

	@Override
	public boolean isLocalStorageAvailable () {
		return true;
	}
}
