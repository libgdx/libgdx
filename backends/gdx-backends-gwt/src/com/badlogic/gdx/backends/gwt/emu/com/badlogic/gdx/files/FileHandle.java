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

package com.badlogic.gdx.files;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class FileHandle {
	protected File file;
	protected FileType type;

	protected FileHandle () {
	}

	public FileHandle (String fileName) {
	}

	public FileHandle (File file) {
	}

	protected FileHandle (String fileName, FileType type) {
	}

	protected FileHandle (File file, FileType type) {
	}

	public String path () {
		throw new GdxRuntimeException("Stub");
	}

	public String name () {
		throw new GdxRuntimeException("Stub");
	}

	public String extension () {
		throw new GdxRuntimeException("Stub");
	}

	public String nameWithoutExtension () {
		throw new GdxRuntimeException("Stub");
	}

	/** @return the path and filename without the extension, e.g. dir/dir2/file.png -> dir/dir2/file */
	public String pathWithoutExtension () {
		throw new GdxRuntimeException("Stub");
	}

	public FileType type () {
		throw new GdxRuntimeException("Stub");
	}

	/** Returns a stream for reading this file as bytes.
	 * @throw GdxRuntimeException if the file handle represents a directory, doesn't exist, or could not be read. */
	public InputStream read () {
		throw new GdxRuntimeException("Stub");
	}

	/** Returns a buffered stream for reading this file as bytes.
	 * @throw GdxRuntimeException if the file handle represents a directory, doesn't exist, or could not be read. */
	public BufferedInputStream read (int bufferSize) {
		throw new GdxRuntimeException("Stub");
	}

	/** Returns a reader for reading this file as characters.
	 * @throw GdxRuntimeException if the file handle represents a directory, doesn't exist, or could not be read. */
	public Reader reader () {
		throw new GdxRuntimeException("Stub");
	}

	/** Returns a reader for reading this file as characters.
	 * @throw GdxRuntimeException if the file handle represents a directory, doesn't exist, or could not be read. */
	public Reader reader (String charset) {
		throw new GdxRuntimeException("Stub");
	}

	/** Returns a buffered reader for reading this file as characters.
	 * @throw GdxRuntimeException if the file handle represents a directory, doesn't exist, or could not be read. */
	public BufferedReader reader (int bufferSize) {
		throw new GdxRuntimeException("Stub");
	}

	/** Returns a buffered reader for reading this file as characters.
	 * @throw GdxRuntimeException if the file handle represents a directory, doesn't exist, or could not be read. */
	public BufferedReader reader (int bufferSize, String charset) {
		throw new GdxRuntimeException("Stub");
	}

	/** Reads the entire file into a string using the platform's default charset.
	 * @throw GdxRuntimeException if the file handle represents a directory, doesn't exist, or could not be read. */
	public String readString () {
		throw new GdxRuntimeException("Stub");
	}

	/** Reads the entire file into a string using the specified charset.
	 * @throw GdxRuntimeException if the file handle represents a directory, doesn't exist, or could not be read. */
	public String readString (String charset) {
		throw new GdxRuntimeException("Stub");
	}

	/** Reads the entire file into a byte array.
	 * @throw GdxRuntimeException if the file handle represents a directory, doesn't exist, or could not be read. */
	public byte[] readBytes () {
		throw new GdxRuntimeException("Stub");
	}

	/** Reads the entire file into the byte array. The byte array must be big enough to hold the file's data.
	 * @param bytes the array to load the file into
	 * @param offset the offset to start writing bytes
	 * @param size the number of bytes to read, see {@link #length()}
	 * @return the number of read bytes */
	public int readBytes (byte[] bytes, int offset, int size) {
		throw new GdxRuntimeException("Stub");
	}

	/** Returns a stream for writing to this file. Parent directories will be created if necessary.
	 * @param append If false, this file will be overwritten if it exists, otherwise it will be appended.
	 * @throw GdxRuntimeException if this file handle represents a directory, if it is a {@link FileType#Classpath} or
	 *        {@link FileType#Internal} file, or if it could not be written. */
	public OutputStream write (boolean append) {
		throw new GdxRuntimeException("Stub");
	}

	/** Returns a buffered stream for writing to this file. Parent directories will be created if necessary.
	 * @param append If false, this file will be overwritten if it exists, otherwise it will be appended.
	 * @param bufferSize The size of the buffer.
	 * @throws GdxRuntimeException if this file handle represents a directory, if it is a {@link FileType#Classpath} or
	 *            {@link FileType#Internal} file, or if it could not be written. */
	public OutputStream write (boolean append, int bufferSize) {
		throw new GdxRuntimeException("Stub");
	}

	/** Reads the remaining bytes from the specified stream and writes them to this file. The stream is closed. Parent directories
	 * will be created if necessary.
	 * @param append If false, this file will be overwritten if it exists, otherwise it will be appended.
	 * @throw GdxRuntimeException if this file handle represents a directory, if it is a {@link FileType#Classpath} or
	 *        {@link FileType#Internal} file, or if it could not be written. */
	public void write (InputStream input, boolean append) {
		throw new GdxRuntimeException("Stub");
	}

	/** Returns a writer for writing to this file using the default charset. Parent directories will be created if necessary.
	 * @param append If false, this file will be overwritten if it exists, otherwise it will be appended.
	 * @throw GdxRuntimeException if this file handle represents a directory, if it is a {@link FileType#Classpath} or
	 *        {@link FileType#Internal} file, or if it could not be written. */
	public Writer writer (boolean append) {
		throw new GdxRuntimeException("Stub");
	}

	/** Returns a writer for writing to this file. Parent directories will be created if necessary.
	 * @param append If false, this file will be overwritten if it exists, otherwise it will be appended.
	 * @param charset May be null to use the default charset.
	 * @throw GdxRuntimeException if this file handle represents a directory, if it is a {@link FileType#Classpath} or
	 *        {@link FileType#Internal} file, or if it could not be written. */
	public Writer writer (boolean append, String charset) {
		throw new GdxRuntimeException("Stub");
	}

	/** Writes the specified string to the file using the default charset. Parent directories will be created if necessary.
	 * @param append If false, this file will be overwritten if it exists, otherwise it will be appended.
	 * @throw GdxRuntimeException if this file handle represents a directory, if it is a {@link FileType#Classpath} or
	 *        {@link FileType#Internal} file, or if it could not be written. */
	public void writeString (String string, boolean append) {
		throw new GdxRuntimeException("Stub");
	}

	/** Writes the specified string to the file as UTF-8. Parent directories will be created if necessary.
	 * @param append If false, this file will be overwritten if it exists, otherwise it will be appended.
	 * @param charset May be null to use the default charset.
	 * @throw GdxRuntimeException if this file handle represents a directory, if it is a {@link FileType#Classpath} or
	 *        {@link FileType#Internal} file, or if it could not be written. */
	public void writeString (String string, boolean append, String charset) {
		throw new GdxRuntimeException("Stub");
	}

	/** Writes the specified bytes to the file. Parent directories will be created if necessary.
	 * @param append If false, this file will be overwritten if it exists, otherwise it will be appended.
	 * @throw GdxRuntimeException if this file handle represents a directory, if it is a {@link FileType#Classpath} or
	 *        {@link FileType#Internal} file, or if it could not be written. */
	public void writeBytes (byte[] bytes, boolean append) {
		throw new GdxRuntimeException("Stub");
	}

	/** Writes the specified bytes to the file. Parent directories will be created if necessary.
	 * @param append If false, this file will be overwritten if it exists, otherwise it will be appended.
	 * @throw GdxRuntimeException if this file handle represents a directory, if it is a {@link FileType#Classpath} or
	 *        {@link FileType#Internal} file, or if it could not be written. */
	public void writeBytes (byte[] bytes, int offset, int length, boolean append) {
		throw new GdxRuntimeException("Stub");
	}

	/** Returns the paths to the children of this directory that satisfy the specified filter. Returns an empty list if this file
	 * handle represents a file and not a directory. On the desktop, an {@link FileType#Internal} handle to a directory on the
	 * classpath will return a zero length array.
	 * @throw GdxRuntimeException if this file is an {@link FileType#Classpath} file. */
	public FileHandle[] list (FileFilter filter) {
		throw new GdxRuntimeException("Stub");
	}

	/** Returns the paths to the children of this directory that satisfy the specified filter. Returns an empty list if this file
	 * handle represents a file and not a directory. On the desktop, an {@link FileType#Internal} handle to a directory on the
	 * classpath will return a zero length array.
	 * @throw GdxRuntimeException if this file is an {@link FileType#Classpath} file. */
	public FileHandle[] list (FilenameFilter filter) {
		throw new GdxRuntimeException("Stub");
	}

	/** Returns the paths to the children of this directory. Returns an empty list if this file handle represents a file and not a
	 * directory. On the desktop, an {@link FileType#Internal} handle to a directory on the classpath will return a zero length
	 * array.
	 * @throw GdxRuntimeException if this file is an {@link FileType#Classpath} file. */
	public FileHandle[] list () {
		throw new GdxRuntimeException("Stub");
	}

	/** Returns the paths to the children of this directory with the specified suffix. Returns an empty list if this file handle
	 * represents a file and not a directory. On the desktop, an {@link FileType#Internal} handle to a directory on the classpath
	 * will return a zero length array.
	 * @throw GdxRuntimeException if this file is an {@link FileType#Classpath} file. */
	public FileHandle[] list (String suffix) {
		throw new GdxRuntimeException("Stub");
	}

	/** Returns true if this file is a directory. Always returns false for classpath files. On Android, an {@link FileType#Internal}
	 * handle to an empty directory will return false. On the desktop, an {@link FileType#Internal} handle to a directory on the
	 * classpath will return false. */
	public boolean isDirectory () {
		throw new GdxRuntimeException("Stub");
	}

	/** Returns a handle to the child with the specified name.
	 * @throw GdxRuntimeException if this file handle is a {@link FileType#Classpath} or {@link FileType#Internal} and the child
	 *        doesn't exist. */
	public FileHandle child (String name) {
		throw new GdxRuntimeException("Stub");
	}

	public FileHandle parent () {
		throw new GdxRuntimeException("Stub");
	}

	/** Returns a handle to the sibling with the specified name.
	 * @throw GdxRuntimeException if this file handle is a {@link FileType#Classpath} or {@link FileType#Internal} and the sibling
	 *        doesn't exist, or this file is the root. */
	public FileHandle sibling (String name) {
		throw new GdxRuntimeException("Stub");
	}

	/** @throw GdxRuntimeException if this file handle is a {@link FileType#Classpath} or {@link FileType#Internal} file. */
	public void mkdirs () {
		throw new GdxRuntimeException("Stub");
	}

	/** Returns true if the file exists. On Android, a {@link FileType#Classpath} or {@link FileType#Internal} handle to a directory
	 * will always return false. */
	public boolean exists () {
		throw new GdxRuntimeException("Stub");
	}

	/** Deletes this file or empty directory and returns success. Will not delete a directory that has children.
	 * @throw GdxRuntimeException if this file handle is a {@link FileType#Classpath} or {@link FileType#Internal} file. */
	public boolean delete () {
		throw new GdxRuntimeException("Stub");
	}

	/** Deletes this file or directory and all children, recursively.
	 * @throw GdxRuntimeException if this file handle is a {@link FileType#Classpath} or {@link FileType#Internal} file. */
	public boolean deleteDirectory () {
		throw new GdxRuntimeException("Stub");
	}

	/** Copies this file or directory to the specified file or directory. If this handle is a file, then 1) if the destination is a
	 * file, it is overwritten, or 2) if the destination is a directory, this file is copied into it, or 3) if the destination
	 * doesn't exist, {@link #mkdirs()} is called on the destination's parent and this file is copied into it with a new name. If
	 * this handle is a directory, then 1) if the destination is a file, GdxRuntimeException is thrown, or 2) if the destination is
	 * a directory, this directory is copied into it recursively, overwriting existing files, or 3) if the destination doesn't
	 * exist, {@link #mkdirs()} is called on the destination and this directory is copied into it recursively.
	 * @throw GdxRuntimeException if the destination file handle is a {@link FileType#Classpath} or {@link FileType#Internal} file,
	 *        or copying failed. */
	public void copyTo (FileHandle dest) {
		throw new GdxRuntimeException("Stub");
	}

	/** Moves this file to the specified file, overwriting the file if it already exists.
	 * @throw GdxRuntimeException if the source or destination file handle is a {@link FileType#Classpath} or
	 *        {@link FileType#Internal} file. */
	public void moveTo (FileHandle dest) {
		throw new GdxRuntimeException("Stub");
	}

	/** Returns the length in bytes of this file, or 0 if this file is a directory, does not exist, or the size cannot otherwise be
	 * determined. */
	public long length () {
		throw new GdxRuntimeException("Stub");
	}

	/** Returns the last modified time in milliseconds for this file. Zero is returned if the file doesn't exist. Zero is returned
	 * for {@link FileType#Classpath} files. On Android, zero is returned for {@link FileType#Internal} files. On the desktop, zero
	 * is returned for {@link FileType#Internal} files on the classpath. */
	public long lastModified () {
		throw new GdxRuntimeException("Stub");
	}

	public String toString () {
		throw new GdxRuntimeException("Stub");
	}
}
