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

package com.badlogic.gdx.jnigen;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

/** Represents a file or directory on the filesystem or classpath. Taken from libgdx's FileHandle.
 * @author mzechner
 * @author Nathan Sweet */
public class FileDescriptor {
	/** Indicates how to resolve a path to a file.
	 * @author mzechner
	 * @author Nathan Sweet */
	public enum FileType {
		/** Path relative to the root of the classpath. Classpath files are always readonly. Note that classpath files are not
		 * compatible with some functionality on Android, such as Audio#newSound(FileHandle) and Audio#newMusic(FileHandle). */
		Classpath,

		/** Path that is a fully qualified, absolute filesystem path. To ensure portability across platforms use absolute files only
		 * when absolutely (heh) necessary. */
		Absolute;
	}

	protected File file;
	protected FileType type;

	protected FileDescriptor () {
	}

	/** Creates a new absolute FileHandle for the file name. Use this for tools on the desktop that don't need any of the backends.
	 * Do not use this constructor in case you write something cross-platform. Use the Files interface instead.
	 * @param fileName the filename. */
	public FileDescriptor (String fileName) {
		this.file = new File(fileName);
		this.type = FileType.Absolute;
	}

	/** Creates a new absolute FileHandle for the {@link File}. Use this for tools on the desktop that don't need any of the
	 * backends. Do not use this constructor in case you write something cross-platform. Use the Files interface instead.
	 * @param file the file. */
	public FileDescriptor (File file) {
		this.file = file;
		this.type = FileType.Absolute;
	}

	protected FileDescriptor (String fileName, FileType type) {
		this.type = type;
		file = new File(fileName);
	}

	protected FileDescriptor (File file, FileType type) {
		this.file = file;
		this.type = type;
	}

	public String path () {
		return file.getPath().replace('\\', '/');
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

	/** Returns a java.io.File that represents this file handle. Note the returned file will only be usable for
	 * {@link FileType#Absolute} and FileType#External file handles. */
	public File file () {
		return file;
	}

	/** Returns a stream for reading this file as bytes.
	 * @throw RuntimeException if the file handle represents a directory, doesn't exist, or could not be read. */
	public InputStream read () {
		if (type == FileType.Classpath && !file.exists()) {
			InputStream input = FileDescriptor.class.getResourceAsStream("/" + file.getPath().replace('\\', '/'));
			if (input == null) throw new RuntimeException("File not found: " + file + " (" + type + ")");
			return input;
		}
		try {
			return new FileInputStream(file());
		} catch (FileNotFoundException ex) {
			if (file().isDirectory())
				throw new RuntimeException("Cannot open a stream to a directory: " + file + " (" + type + ")", ex);
			throw new RuntimeException("Error reading file: " + file + " (" + type + ")", ex);
		}
	}

	/** Returns a reader for reading this file as characters.
	 * @throw RuntimeException if the file handle represents a directory, doesn't exist, or could not be read. */
	public Reader reader () {
		return new InputStreamReader(read());
	}

	/** Returns a reader for reading this file as characters.
	 * @throw RuntimeException if the file handle represents a directory, doesn't exist, or could not be read. */
	public Reader reader (String charset) {
		try {
			return new InputStreamReader(read(), charset);
		} catch (UnsupportedEncodingException ex) {
			throw new RuntimeException("Error reading file: " + this, ex);
		}
	}

	/** Returns a buffered reader for reading this file as characters.
	 * @throw RuntimeException if the file handle represents a directory, doesn't exist, or could not be read. */
	public BufferedReader reader (int bufferSize) {
		return new BufferedReader(new InputStreamReader(read()), bufferSize);
	}

	/** Returns a buffered reader for reading this file as characters.
	 * @throw RuntimeException if the file handle represents a directory, doesn't exist, or could not be read. */
	public BufferedReader reader (int bufferSize, String charset) {
		try {
			return new BufferedReader(new InputStreamReader(read(), charset), bufferSize);
		} catch (UnsupportedEncodingException ex) {
			throw new RuntimeException("Error reading file: " + this, ex);
		}
	}

	/** Reads the entire file into a string using the platform's default charset.
	 * @throw RuntimeException if the file handle represents a directory, doesn't exist, or could not be read. */
	public String readString () {
		return readString(null);
	}

	/** Reads the entire file into a string using the specified charset.
	 * @throw RuntimeException if the file handle represents a directory, doesn't exist, or could not be read. */
	public String readString (String charset) {
		StringBuilder output = new StringBuilder(512);
		InputStreamReader reader = null;
		try {
			if (charset == null)
				reader = new InputStreamReader(read());
			else
				reader = new InputStreamReader(read(), charset);
			char[] buffer = new char[256];
			while (true) {
				int length = reader.read(buffer);
				if (length == -1) break;
				output.append(buffer, 0, length);
			}
		} catch (IOException ex) {
			throw new RuntimeException("Error reading layout file: " + this, ex);
		} finally {
			try {
				if (reader != null) reader.close();
			} catch (IOException ignored) {
			}
		}
		return output.toString();
	}

	/** Reads the entire file into a byte array.
	 * @throw RuntimeException if the file handle represents a directory, doesn't exist, or could not be read. */
	public byte[] readBytes () {
		int length = (int)length();
		if (length == 0) length = 512;
		byte[] buffer = new byte[length];
		int position = 0;
		InputStream input = read();
		try {
			while (true) {
				int count = input.read(buffer, position, buffer.length - position);
				if (count == -1) break;
				position += count;
				if (position == buffer.length) {
					// Grow buffer.
					byte[] newBuffer = new byte[buffer.length * 2];
					System.arraycopy(buffer, 0, newBuffer, 0, position);
					buffer = newBuffer;
				}
			}
		} catch (IOException ex) {
			throw new RuntimeException("Error reading file: " + this, ex);
		} finally {
			try {
				if (input != null) input.close();
			} catch (IOException ignored) {
			}
		}
		if (position < buffer.length) {
			// Shrink buffer.
			byte[] newBuffer = new byte[position];
			System.arraycopy(buffer, 0, newBuffer, 0, position);
			buffer = newBuffer;
		}
		return buffer;
	}

	/** Reads the entire file into the byte array. The byte array must be big enough to hold the file's data.
	 * @param bytes the array to load the file into
	 * @param offset the offset to start writing bytes
	 * @param size the number of bytes to read, see {@link #length()}
	 * @return the number of read bytes */
	public int readBytes (byte[] bytes, int offset, int size) {
		InputStream input = read();
		int position = 0;
		try {
			while (true) {
				int count = input.read(bytes, offset + position, size - position);
				if (count <= 0) break;
				position += count;
			}
		} catch (IOException ex) {
			throw new RuntimeException("Error reading file: " + this, ex);
		} finally {
			try {
				if (input != null) input.close();
			} catch (IOException ignored) {
			}
		}
		return position - offset;
	}

	/** Returns a stream for writing to this file. Parent directories will be created if necessary.
	 * @param append If false, this file will be overwritten if it exists, otherwise it will be appended.
	 * @throw RuntimeException if this file handle represents a directory, if it is a {@link FileType#Classpath} or
	 *        FileType#Internal file, or if it could not be written. */
	public OutputStream write (boolean append) {
		if (type == FileType.Classpath) throw new RuntimeException("Cannot write to a classpath file: " + file);
		parent().mkdirs();
		try {
			return new FileOutputStream(file(), append);
		} catch (FileNotFoundException ex) {
			if (file().isDirectory())
				throw new RuntimeException("Cannot open a stream to a directory: " + file + " (" + type + ")", ex);
			throw new RuntimeException("Error writing file: " + file + " (" + type + ")", ex);
		}
	}

	/** Reads the remaining bytes from the specified stream and writes them to this file. The stream is closed. Parent directories
	 * will be created if necessary.
	 * @param append If false, this file will be overwritten if it exists, otherwise it will be appended.
	 * @throw RuntimeException if this file handle represents a directory, if it is a {@link FileType#Classpath} or
	 *        FileType#Internal file, or if it could not be written. */
	public void write (InputStream input, boolean append) {
		OutputStream output = null;
		try {
			output = write(append);
			byte[] buffer = new byte[4096];
			while (true) {
				int length = input.read(buffer);
				if (length == -1) break;
				output.write(buffer, 0, length);
			}
		} catch (Exception ex) {
			throw new RuntimeException("Error stream writing to file: " + file + " (" + type + ")", ex);
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

	/** Returns a writer for writing to this file using the default charset. Parent directories will be created if necessary.
	 * @param append If false, this file will be overwritten if it exists, otherwise it will be appended.
	 * @throw RuntimeException if this file handle represents a directory, if it is a {@link FileType#Classpath} or
	 *        FileType#Internal file, or if it could not be written. */
	public Writer writer (boolean append) {
		return writer(append, null);
	}

	/** Returns a writer for writing to this file. Parent directories will be created if necessary.
	 * @param append If false, this file will be overwritten if it exists, otherwise it will be appended.
	 * @param charset May be null to use the default charset.
	 * @throw RuntimeException if this file handle represents a directory, if it is a {@link FileType#Classpath} or
	 *        FileType#Internal file, or if it could not be written. */
	public Writer writer (boolean append, String charset) {
		if (type == FileType.Classpath) throw new RuntimeException("Cannot write to a classpath file: " + file);
		parent().mkdirs();
		try {
			FileOutputStream output = new FileOutputStream(file(), append);
			if (charset == null)
				return new OutputStreamWriter(output);
			else
				return new OutputStreamWriter(output, charset);
		} catch (IOException ex) {
			if (file().isDirectory())
				throw new RuntimeException("Cannot open a stream to a directory: " + file + " (" + type + ")", ex);
			throw new RuntimeException("Error writing file: " + file + " (" + type + ")", ex);
		}
	}

	/** Writes the specified string to the file using the default charset. Parent directories will be created if necessary.
	 * @param append If false, this file will be overwritten if it exists, otherwise it will be appended.
	 * @throw RuntimeException if this file handle represents a directory, if it is a {@link FileType#Classpath} or
	 *        FileType#Internal file, or if it could not be written. */
	public void writeString (String string, boolean append) {
		writeString(string, append, null);
	}

	/** Writes the specified string to the file as UTF-8. Parent directories will be created if necessary.
	 * @param append If false, this file will be overwritten if it exists, otherwise it will be appended.
	 * @param charset May be null to use the default charset.
	 * @throw RuntimeException if this file handle represents a directory, if it is a {@link FileType#Classpath} or
	 *        FileType#Internal file, or if it could not be written. */
	public void writeString (String string, boolean append, String charset) {
		Writer writer = null;
		try {
			writer = writer(append, charset);
			writer.write(string);
		} catch (Exception ex) {
			throw new RuntimeException("Error writing file: " + file + " (" + type + ")", ex);
		} finally {
			try {
				if (writer != null) writer.close();
			} catch (Exception ignored) {
			}
		}
	}

	/** Writes the specified bytes to the file. Parent directories will be created if necessary.
	 * @param append If false, this file will be overwritten if it exists, otherwise it will be appended.
	 * @throw RuntimeException if this file handle represents a directory, if it is a {@link FileType#Classpath} or
	 *        FileType#Internal file, or if it could not be written. */
	public void writeBytes (byte[] bytes, boolean append) {
		OutputStream output = write(append);
		try {
			output.write(bytes);
		} catch (IOException ex) {
			throw new RuntimeException("Error writing file: " + file + " (" + type + ")", ex);
		} finally {
			try {
				output.close();
			} catch (IOException ignored) {
			}
		}
	}

	/** Returns the paths to the children of this directory. Returns an empty list if this file handle represents a file and not a
	 * directory. On the desktop, an FileType#Internal handle to a directory on the classpath will return a zero length array.
	 * @throw RuntimeException if this file is an {@link FileType#Classpath} file. */
	public FileDescriptor[] list () {
		if (type == FileType.Classpath) throw new RuntimeException("Cannot list a classpath directory: " + file);
		String[] relativePaths = file().list();
		if (relativePaths == null) return new FileDescriptor[0];
		FileDescriptor[] handles = new FileDescriptor[relativePaths.length];
		for (int i = 0, n = relativePaths.length; i < n; i++)
			handles[i] = child(relativePaths[i]);
		return handles;
	}

	/** Returns the paths to the children of this directory with the specified suffix. Returns an empty list if this file handle
	 * represents a file and not a directory. On the desktop, an FileType#Internal handle to a directory on the classpath will
	 * return a zero length array.
	 * @throw RuntimeException if this file is an {@link FileType#Classpath} file. */
	public FileDescriptor[] list (String suffix) {
		if (type == FileType.Classpath) throw new RuntimeException("Cannot list a classpath directory: " + file);
		String[] relativePaths = file().list();
		if (relativePaths == null) return new FileDescriptor[0];
		FileDescriptor[] handles = new FileDescriptor[relativePaths.length];
		int count = 0;
		for (int i = 0, n = relativePaths.length; i < n; i++) {
			String path = relativePaths[i];
			if (!path.endsWith(suffix)) continue;
			handles[count] = child(path);
			count++;
		}
		if (count < relativePaths.length) {
			FileDescriptor[] newHandles = new FileDescriptor[count];
			System.arraycopy(handles, 0, newHandles, 0, count);
			handles = newHandles;
		}
		return handles;
	}

	/** Returns true if this file is a directory. Always returns false for classpath files. On Android, an FileType#Internal handle
	 * to an empty directory will return false. On the desktop, an FileType#Internal handle to a directory on the classpath will
	 * return false. */
	public boolean isDirectory () {
		if (type == FileType.Classpath) return false;
		return file().isDirectory();
	}

	/** Returns a handle to the child with the specified name.
	 * @throw RuntimeException if this file handle is a {@link FileType#Classpath} or FileType#Internal and the child doesn't
	 *        exist. */
	public FileDescriptor child (String name) {
		if (file.getPath().length() == 0) return new FileDescriptor(new File(name), type);
		return new FileDescriptor(new File(file, name), type);
	}

	public FileDescriptor parent () {
		File parent = file.getParentFile();
		if (parent == null) {
			if (type == FileType.Absolute)
				parent = new File("/");
			else
				parent = new File("");
		}
		return new FileDescriptor(parent, type);
	}

	/** @throw RuntimeException if this file handle is a {@link FileType#Classpath} or FileType#Internal file. */
	public boolean mkdirs () {
		if (type == FileType.Classpath) throw new RuntimeException("Cannot mkdirs with a classpath file: " + file);
		return file().mkdirs();
	}

	/** Returns true if the file exists. On Android, a {@link FileType#Classpath} or FileType#Internal handle to a directory will
	 * always return false. */
	public boolean exists () {
		if (type == FileType.Classpath) return FileDescriptor.class.getResource("/" + file.getPath().replace('\\', '/')) != null;
		return file().exists();
	}

	/** Deletes this file or empty directory and returns success. Will not delete a directory that has children.
	 * @throw RuntimeException if this file handle is a {@link FileType#Classpath} or FileType#Internal file. */
	public boolean delete () {
		if (type == FileType.Classpath) throw new RuntimeException("Cannot delete a classpath file: " + file);
		return file().delete();
	}

	/** Deletes this file or directory and all children, recursively.
	 * @throw RuntimeException if this file handle is a {@link FileType#Classpath} or FileType#Internal file. */
	public boolean deleteDirectory () {
		if (type == FileType.Classpath) throw new RuntimeException("Cannot delete a classpath file: " + file);
		return deleteDirectory(file());
	}

	/** Copies this file or directory to the specified file or directory. If this handle is a file, then 1) if the destination is a
	 * file, it is overwritten, or 2) if the destination is a directory, this file is copied into it, or 3) if the destination
	 * doesn't exist, {@link #mkdirs()} is called on the destination's parent and this file is copied into it with a new name. If
	 * this handle is a directory, then 1) if the destination is a file, RuntimeException is thrown, or 2) if the destination is a
	 * directory, this directory is copied recursively into it as a subdirectory, overwriting existing files, or 3) if the
	 * destination doesn't exist, {@link #mkdirs()} is called on the destination and this directory is copied recursively into it
	 * as a subdirectory.
	 * @throw RuntimeException if the destination file handle is a {@link FileType#Classpath} or FileType#Internal file, or copying
	 *        failed. */
	public void copyTo (FileDescriptor dest) {
		if (!isDirectory()) {
			if (dest.isDirectory()) dest = dest.child(name());
			copyFile(this, dest);
			return;
		}
		if (dest.exists()) {
			if (!dest.isDirectory()) throw new RuntimeException("Destination exists but is not a directory: " + dest);
		} else {
			dest.mkdirs();
			if (!dest.isDirectory()) throw new RuntimeException("Destination directory cannot be created: " + dest);
		}
		dest = dest.child(name());
		copyDirectory(this, dest);
	}

	/** Moves this file to the specified file, overwriting the file if it already exists.
	 * @throw RuntimeException if the source or destination file handle is a {@link FileType#Classpath} or FileType#Internal file. */
	public void moveTo (FileDescriptor dest) {
		if (type == FileType.Classpath) throw new RuntimeException("Cannot move a classpath file: " + file);
		copyTo(dest);
		delete();
	}

	/** Returns the length in bytes of this file, or 0 if this file is a directory, does not exist, or the size cannot otherwise be
	 * determined. */
	public long length () {
		if (type == FileType.Classpath || !file.exists()) {
			InputStream input = read();
			try {
				return input.available();
			} catch (Exception ignored) {
			} finally {
				try {
					input.close();
				} catch (IOException ignored) {
				}
			}
			return 0;
		}
		return file().length();
	}

	/** Returns the last modified time in milliseconds for this file. Zero is returned if the file doesn't exist. Zero is returned
	 * for {@link FileType#Classpath} files. On Android, zero is returned for FileType#Internal files. On the desktop, zero is
	 * returned for FileType#Internal files on the classpath. */
	public long lastModified () {
		return file().lastModified();
	}

	public String toString () {
		return file.getPath();
	}

	static public FileDescriptor tempFile (String prefix) {
		try {
			return new FileDescriptor(File.createTempFile(prefix, null));
		} catch (IOException ex) {
			throw new RuntimeException("Unable to create temp file.", ex);
		}
	}

	static public FileDescriptor tempDirectory (String prefix) {
		try {
			File file = File.createTempFile(prefix, null);
			if (!file.delete()) throw new IOException("Unable to delete temp file: " + file);
			if (!file.mkdir()) throw new IOException("Unable to create temp directory: " + file);
			return new FileDescriptor(file);
		} catch (IOException ex) {
			throw new RuntimeException("Unable to create temp file.", ex);
		}
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

	static private void copyFile (FileDescriptor source, FileDescriptor dest) {
		try {
			dest.write(source.read(), false);
		} catch (Exception ex) {
			throw new RuntimeException("Error copying source file: " + source.file + " (" + source.type + ")\n" //
				+ "To destination: " + dest.file + " (" + dest.type + ")", ex);
		}
	}

	static private void copyDirectory (FileDescriptor sourceDir, FileDescriptor destDir) {
		destDir.mkdirs();
		FileDescriptor[] files = sourceDir.list();
		for (int i = 0, n = files.length; i < n; i++) {
			FileDescriptor srcFile = files[i];
			FileDescriptor destFile = destDir.child(srcFile.name());
			if (srcFile.isDirectory())
				copyDirectory(srcFile, destFile);
			else
				copyFile(srcFile, destFile);
		}
	}
}
