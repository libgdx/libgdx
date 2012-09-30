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
package com.badlogic.gdx.backends.ios;

import java.io.File;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.files.FileHandle;

public class IOSFileHandle extends FileHandle {
	public IOSFileHandle (String fileName, FileType type) {
		super(fileName, type);
	}

	public IOSFileHandle (File file, FileType type) {
		super(file, type);
	}

	public FileHandle child (String name) {
		if (file.getPath().length() == 0) return new IOSFileHandle(new File(name), type);
		return new IOSFileHandle(new File(file, name), type);
	}

	public FileHandle parent () {
		File parent = file.getParentFile();
		if (parent == null) {
			if (type == FileType.Absolute)
				parent = new File("/");
			else
				parent = new File("");
		}
		return new IOSFileHandle(parent, type);
	}

	/**
	 * This overrides the original method in FileHandle to prevent crashes on iOS. The original method
	 * has a fallback to FileType.Classpath when FileType.Internal is used. FileType.Classpath is not
	 * supported on iOS.
	 * 
	 * @return  True if the file exists.
	 */
	@Override
	public boolean exists () {
		return file.exists();
	}

	public File file () {
		if (type == FileType.External) return new File(IOSFiles.externalPath, file.getPath());
		if (type == FileType.Local) return new File(IOSFiles.localPath, file.getPath());
		return file;
	}
}