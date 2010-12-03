/*
 * Copyright 2010 Mario Zechner (contact@badlogicgames.com), Nathan Sweet (admin@esotericsoftware.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package com.badlogic.gdx.files;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.GdxRuntimeException;

/**
 * Represents a file or directory on the filesystem, classpath, Android SD card, or Android assets directory. FileHandles are
 * created via a {@link Files} instance.
 * @author mzechner
 * @author Nathan Sweet <misc@n4te.com>
 */
public abstract class FileHandle {
	protected File file;
	protected FileType type;

	protected FileHandle () {
	}

	protected FileHandle (String fileName, FileType type) {
		this.type = type;

		switch (type) {
		case Internal:
			file = new File(fileName);
			if (file.exists()) break;
			// Fall through.
		case Classpath:
			if (FileHandle.class.getResourceAsStream("/" + fileName) == null)
				throw new GdxRuntimeException("File not found: " + fileName + " (" + type + ")");
			file = new File("/" + fileName);
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

	protected FileHandle (File file, FileType type) {
		this.file = file;
		this.type = type;

		switch (type) {
		case Internal:
			if (file.exists()) break;
			// Fall through.
		case Classpath:
			if (FileHandle.class.getResourceAsStream(file.getPath().replace('\\', '/')) == null)
				throw new GdxRuntimeException("File not found: " + file + " (" + type + ")");
			break;
		}
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
	 * @throw GdxRuntimeException if this file handle represents a directory, if it is a {@link FileType#Classpath} or
	 *        {@link FileType#Internal} and the file doesn't exist, or if it could not be read.
	 */
	public InputStream read () {
		if (type == FileType.Classpath || (type == FileType.Internal && !file.exists())) {
			InputStream input = FileHandle.class.getResourceAsStream(file.getPath().replace('\\', '/'));
			if (input == null) throw new GdxRuntimeException("File not found: " + file + " (" + type + ")");
			return input;
		}
		try {
			return new FileInputStream(file);
		} catch (FileNotFoundException ex) {
			if (file.isDirectory())
				throw new GdxRuntimeException("Cannot open a stream to a directory: " + file + " (" + type + ")", ex);
			throw new GdxRuntimeException("Error reading file: " + file + " (" + type + ")", ex);
		}
	}

	/**
	 * Returns a stream for writing to this file.
	 * @param append If false, this file will be overwritten if it exists, otherwise it will be appended.
	 * @throw GdxRuntimeException if this file handle represents a directory, if it is a {@link FileType#Classpath} or
	 *        {@link FileType#Internal} file, or if it could not be written.
	 */
	public OutputStream write (boolean append) {
		if (type == FileType.Classpath) throw new GdxRuntimeException("Cannot write to a classpath file: " + file);
		if (type == FileType.Internal) throw new GdxRuntimeException("Cannot write to an internal file: " + file);
		try {
			return new FileOutputStream(file, append);
		} catch (FileNotFoundException ex) {
			if (file.isDirectory())
				throw new GdxRuntimeException("Cannot open a stream to a directory: " + file + " (" + type + ")", ex);
			throw new GdxRuntimeException("Error writing file: " + file + " (" + type + ")", ex);
		}
	}

	/**
	 * Returns the paths to the children of this directory. Returns an empty list if this file handle represents a file and not a
	 * directory.
	 * @throw GdxRuntimeException if this file is an {@link FileType#Classpath} file.
	 */
	public FileHandle[] list () {
		if (type == FileType.Classpath) throw new GdxRuntimeException("Cannot list a classpath directory: " + file);
		String[] relativePaths = file.list();
		if (relativePaths == null) return new FileHandle[0];
		FileHandle[] handles = new FileHandle[relativePaths.length];
		for (int i = 0, n = relativePaths.length; i < n; i++)
			handles[i] = child(relativePaths[i]);
		return handles;
	}

	/**
	 * Returns true if this file is a directory. Always returns false for classpath files. On Android, an internal handle to an
	 * empty directory will return false.
	 */
	public boolean isDirectory () {
		if (type == FileType.Classpath) return false;
		return file.isDirectory();

	}

	/**
	 * Returns a handle to the child with the specified name.
	 * @throw GdxRuntimeException if this file handle is a {@link FileType#Classpath} or {@link FileType#Internal} and the child
	 *        doesn't exist.
	 */
	abstract public FileHandle child (String name);

	abstract public FileHandle parent ();

	/**
	 * @throw GdxRuntimeException if this file handle is a {@link FileType#Classpath} or {@link FileType#Internal} file.
	 */
	public void mkdirs () {
		if (type == FileType.Classpath) throw new GdxRuntimeException("Cannot mkdirs with a classpath file: " + file);
		if (type == FileType.Internal) throw new GdxRuntimeException("Cannot mkdirs with an internal file: " + file);
		file.mkdirs();
	}

	public boolean exists () {
		// Classpath and internal FileHandles can't be created unless they exist.
		if (type == FileType.Classpath || type == FileType.Internal) return true;
		return file.exists();
	}

	/**
	 * Deletes this file or empty directory and returns success. Will not delete a directory that has children.
	 * @throw GdxRuntimeException if this file handle is a {@link FileType#Classpath} or {@link FileType#Internal} file.
	 */
	public boolean delete () {
		if (type == FileType.Classpath) throw new GdxRuntimeException("Cannot delete a classpath file: " + file);
		if (type == FileType.Internal) throw new GdxRuntimeException("Cannot delete an internal file: " + file);
		return file.delete();
	}

	/**
	 * Deletes this file or directory and all children, recursively.
	 * @throw GdxRuntimeException if this file handle is a {@link FileType#Classpath} or {@link FileType#Internal} file.
	 */
	public boolean deleteDirectory () {
		if (type == FileType.Classpath) throw new GdxRuntimeException("Cannot delete a classpath file: " + file);
		if (type == FileType.Internal) throw new GdxRuntimeException("Cannot delete an internal file: " + file);
		return deleteDirectory(file);
	}

	/**
	 * Copies this file to the specified file, overwriting the file if it already exists.
	 * @throw GdxRuntimeException if the destination file handle is a {@link FileType#Classpath} or {@link FileType#Internal} file.
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
	 * @throw GdxRuntimeException if the source or destination file handle is a {@link FileType#Classpath} or
	 *        {@link FileType#Internal} file.
	 */
	public void moveTo (FileHandle dest) {
		if (type == FileType.Classpath) throw new GdxRuntimeException("Cannot move a classpath file: " + file);
		if (type == FileType.Internal) throw new GdxRuntimeException("Cannot move an internal file: " + file);
		copyTo(dest);
		delete();
	}

	/**
	 * Returns the length in bytes of this file, or 0 if this file is a directory or does not exist.
	 */
	public long length () {
		if (type == FileType.Classpath || (type == FileType.Internal && !file.exists())) {
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
			if (files != null) {
				for (int i = 0, n = files.length; i < n; i++) {
					if (files[i].isDirectory())
						deleteDirectory(files[i]);
					else
						files[i].delete();
				}
			}
		}
		return file.delete();
	}
}
