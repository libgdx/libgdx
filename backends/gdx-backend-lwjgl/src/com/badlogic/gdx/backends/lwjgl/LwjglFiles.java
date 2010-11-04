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

package com.badlogic.gdx.backends.lwjgl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.GdxRuntimeException;

/**
 * Implementation for a desktop application of {@link Files}. Internal resources are relative to the application root directory,
 * external files are relative to the user's home directory.
 * 
 * @author mzechner
 * 
 */
final class LwjglFiles implements Files {
	private final String externalPath = System.getProperty("user.home") + "/";

	public FileHandle getFileHandle (String fileName, FileType type) {
		if (type == FileType.External) fileName = this.externalPath + fileName;
		File file = new File(fileName);

		if (LwjglFileHandle.class.getResource("/" + fileName) == null && file.exists() == false)
			throw new GdxRuntimeException("File '" + fileName + "' doesn't exist");
		else
			return new LwjglFileHandle(file, type);
	}

	public String[] listDirectory (String directory, FileType type) {
		if (type == FileType.External) directory = this.externalPath + directory;
		File file = new File(directory);

		if (file.exists() == false) throw new GdxRuntimeException("Directory '" + directory + "' does not exist");

		return file.list();
	}

	public boolean makeDirectory (String directory, FileType type) {
		if (type == FileType.Internal) return false;

		File file = null;
		if (type == FileType.Absolute)
			file = new File(directory);
		else
			file = new File(this.externalPath + directory);
		return file.mkdirs();
	}

	public InputStream readFile (String fileName, FileType type) {
		if (type == FileType.External)
			fileName = this.externalPath + fileName;
		else if (type == FileType.Internal) {
			InputStream input = LwjglFileHandle.class.getResourceAsStream("/" + fileName);
			if (input != null) return input;
		}

		try {
			return new FileInputStream(fileName);
		} catch (FileNotFoundException ex) {
			throw new GdxRuntimeException("Error reading file: " + fileName);
		}
	}

	public OutputStream writeFile (String filename, FileType type) {
		if (type == FileType.Internal) return null;

		File file = null;
		if (type == FileType.Absolute)
			file = new File(filename);
		else
			file = new File(this.externalPath + filename);

		try {
			return new FileOutputStream(file);
		} catch (FileNotFoundException e) {
			throw new GdxRuntimeException("File '" + file + "' does not exist");
		}
	}
}
