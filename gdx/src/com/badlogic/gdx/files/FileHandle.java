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

package com.badlogic.gdx.files;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.utils.GdxRuntimeException;

/**
 * Represents a file or directory on the filesystem, classpath, Android SD card, or Android assets directory. FileHandles are
 * created via a {@link Files} instance.
 * @author mzechner
 * @author Nathan Sweet <misc@n4te.com>
 */
public abstract class FileHandle {
	protected final File file;
	protected final FileType type;

	protected FileHandle (File file, FileType type) {
		this.file = file;
		this.type = type;
	}

	public String path () {
		return file.getPath();
	}

	public String name () {
		return file.getName();
	}

	public String extension () {
		String name = file.getName();
		int dotIndex = name.lastIndexOf('.');
		if (dotIndex == -1) return "";
		return name.substring(dotIndex + 1);
	}

	public String nameWithoutExtension () {
		String name = file.getName();
		int dotIndex = name.lastIndexOf('.');
		if (dotIndex == -1) return name;
		return name.substring(0, dotIndex);
	}

	public FileType type () {
		return type;
	}

	/**
	 * Returns a stream for reading this file.
	 * @throw GdxRuntimeException if this file handle represents a directory or if it could not be read.
	 */
	public InputStream read () {
		if (type == FileType.Internal) {
			InputStream input = FileHandle.class.getResourceAsStream("/" + file.getPath());
			if (input != null) return input;
		}
		try {
			return new FileInputStream(file);
		} catch (FileNotFoundException ex) {
			throw new GdxRuntimeException("Error reading file: " + file + " (" + type + ")", ex);
		}
	}

	/**
	 * Returns a stream for writing to this file.
	 * @param append If false, this file will be overwritten if it exists, otherwise it is appended.
	 * @throw GdxRuntimeException if this file handle represents a directory, if it is an {@link FileType#Internal} file, or if it
	 *        could not be written.
	 */
	public OutputStream write (boolean append) {
		if (type == FileType.Internal) throw new GdxRuntimeException("Cannot write to an internal file: " + file);
		try {
			return new FileOutputStream(file, append);
		} catch (FileNotFoundException ex) {
			throw new GdxRuntimeException("Error writing file: " + file + " (" + type + ")", ex);
		}
	}

	/**
	 * Returns the paths to the children of this directory. Returns an empty list if this file handle represents a file and not a
	 * directory.
	 * @throw GdxRuntimeException if this file is an {@link FileType#Internal} file.
	 */
	public FileHandle[] list () {
		String[] relativePaths = file.list();
		if (relativePaths == null) return new FileHandle[0];
		FileHandle[] handles = new FileHandle[relativePaths.length];
		for (int i = 0, n = relativePaths.length; i < n; i++)
			handles[i] = child(relativePaths[i]);
		return handles;
	}

	public boolean isDirectory () {
		if (type == FileType.Internal) return false; // BOZO - This works on Android, where internal is assets dir.
		return file.isDirectory();

	}

	abstract public FileHandle child (String name);

	abstract public FileHandle parent ();

	/**
	 * @throw GdxRuntimeException if this file handle is an {@link FileType#Internal} file.
	 */
	public void mkdirs () {
		if (type == FileType.Internal) throw new GdxRuntimeException("Cannot mkdirs with an internal file: " + file);
		file.mkdirs();
	}

	public boolean exists () {
		return file.exists();
	}

	/**
	 * Deletes this file or empty directory and returns success. Will not delete a directory that has children.
	 * @throw GdxRuntimeException if this file handle is an {@link FileType#Internal} file.
	 */
	public boolean delete () {
		if (type == FileType.Internal) throw new GdxRuntimeException("Cannot delete an internal file: " + file);
		return file.delete();
	}

	/**
	 * Deletes this file or directory and all children, recursively.
	 * @throw GdxRuntimeException if this file handle is an {@link FileType#Internal} file.
	 */
	public boolean deleteDirectory () {
		if (type == FileType.Internal) throw new GdxRuntimeException("Cannot delete an internal file: " + file);
		return deleteDirectory(file);
	}

	/**
	 * Copies this file to the specified file, overwriting the file if it already exists.
	 * @throw GdxRuntimeException if the destination file handle is an {@link FileType#Internal} file.
	 */
	public void copyTo (FileHandle dest) {
		InputStream input = null;
		OutputStream output = null;
		try {
			input = read();
			output = dest.write(false);
			byte[] buffer = new byte[4096];
			while (true) {
				int length = input.read(buffer);
				if (length == -1) break;
				output.write(buffer, 0, length);
			}
		} catch (Exception ex) {
			throw new GdxRuntimeException("Error copying source file: " + file + " (" + type + ")\n" //
				+ "To destination: " + dest.file + " (" + dest.type + ")", ex);
		} finally {
			try {
				if (input != null) input.close();
			} catch (Exception ignored) {
			}
			try {
				if (output != null) output.close();
			} catch (Exception ignored) {
			}
		}
	}

	/**
	 * Moves this file to the specified file, overwriting the file if it already exists.
	 * @throw GdxRuntimeException if the destination file handle is an {@link FileType#Internal} file.
	 */
	public void moveTo (FileHandle dest) {
		copyTo(dest);
		delete();
	}

	/**
	 * Returns the length in bytes of this file, or 0 if this file is a directory or does not exist.
	 */
	public long length () {
		if (type == FileType.Internal) {
			try {
				InputStream input = read();
				long length = input.available();
				try {
					input.close();
				} catch (Exception ignored) {
				}
				return length;
			} catch (Exception ignored) {
			}
			return 0;
		}
		return file.length();
	}

	public String toString () {
		return file.getPath();
	}

	static private boolean deleteDirectory (File file) {
		if (file.exists()) {
			File[] files = file.listFiles();
			for (int i = 0, n = files.length; i < n; i++) {
				if (files[i].isDirectory())
					deleteDirectory(files[i]);
				else
					files[i].delete();
			}
		}
		return file.delete();
	}
}
