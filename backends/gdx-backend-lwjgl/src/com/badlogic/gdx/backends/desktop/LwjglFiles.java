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

package com.badlogic.gdx.backends.desktop;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

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
final class LwjglFiles implements Files {
	private final String externalPath = System.getProperty("user.home") + "/";

	public FileHandle getFileHandle (String filename, FileType type) {
		File file = null;
		if (type == FileType.Absolute || type == FileType.Internal)
			file = new File(filename);
		else
			file = new File(this.externalPath + filename);

		// BOZO - Hack.
		if (!file.exists()) file = new File("resources/" + filename); 
		
		if (file.exists() == false)
			throw new GdxRuntimeException("File '" + filename + "' doesn't exist");
		else
			return new LwjglFileHandle(file);
	}

	public String[] listDirectory (String directory, FileType type) {
		File file = null;
		if (type == FileType.Absolute || type == FileType.Internal)
			file = new File(directory);
		else
			file = new File(this.externalPath + directory);

		if (file.exists() == false) throw new GdxRuntimeException("Directory '" + directory + "' does not exist");

		return file.list();
	}

	public boolean makeDirectory (String directory, FileType type) {
		File file = null;

		if (type == FileType.Internal) return false;

		if (type == FileType.Absolute)
			file = new File(directory);
		else
			file = new File(this.externalPath + directory);
		return file.mkdirs();
	}

	public InputStream readFile (String fileName, FileType type) {
		File file = null;
		InputStream in = null;
		if (type == FileType.Absolute || type == FileType.Internal)
			file = new File(fileName);
		else
			file = new File(this.externalPath + fileName);

		try {
			in = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			throw new GdxRuntimeException("File '" + file + "' does not exist");
		}

		return in;
	}

	public OutputStream writeFile (String filename, FileType type) {
		File file = null;
		FileOutputStream out = null;

		if (type == FileType.Internal) return null;

		if (type == FileType.Absolute)
			file = new File(filename);
		else
			file = new File(this.externalPath + filename);

		try {
			out = new FileOutputStream(file);
		} catch (FileNotFoundException e) {
			throw new GdxRuntimeException("File '" + file + "' does not exist");
		}

		return out;
	}
}
