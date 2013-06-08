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

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.GdxRuntimeException;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

/** Represents a file or directory on the filesystem, classpath, Android SD card, or Android assets directory. FileHandles are
 * created via a {@link Files} instance.
 * 
 * Because some of the file types are backed by composite files and may be compressed (for example, if they are in an Android .apk or are
 * found via the classpath), the methods for extracting a {@link #path()} or {@link #file()} may not be appropriate for all types.
 * Use the Reader or Stream methods here to hide these dependencies from your platform independent code.
 * 
 * @author mzechner
 * @author Nathan Sweet */
public class FileHandle {
	protected File file;
	protected FileType type;

	protected FileHandle () {
	}

	/** Creates a new absolute FileHandle for the file name. Use this for tools on the desktop that don't need any of the backends.
	 * Do not use this constructor in case you write something cross-platform. Use the {@link Files} interface instead.
	 * @param fileName the filename. */
	public FileHandle (String fileName) {
		this.file = new File(fileName);
		this.type = FileType.Absolute;
	}

	/** Creates a new absolute FileHandle for the {@link File}. Use this for tools on the desktop that don't need any of the
	 * backends. Do not use this constructor in case you write something cross-platform. Use the {@link Files} interface instead.
	 * @param file the file. */
	public FileHandle (File file) {
		this.file = file;
		this.type = FileType.Absolute;
	}

	protected FileHandle (String fileName, FileType type) {
		this.type = type;
		file = new File(fileName);
	}

	protected FileHandle (File file, FileType type) {
		this.file = file;
		this.type = type;
	}

	/** @return the path of the file as specified on construction, e.g. Gdx.files.internal("dir/file.png") -> dir/file.png. backward
	 *         slashes will be replaced by forward slashes. */
	public String path () {
		return file.getPath().replace('\\', '/');
	}

	/** @return the name of the file, without any parent paths. */
	public String name () {
		return file.getName();
	}

	public String extension () {
		String name = file.getName();
		int dotIndex = name.lastIndexOf('.');
		if (dotIndex == -1) return "";
		return name.substring(dotIndex + 1);
	}

	/** @return the name of the file, without parent paths or the extension. */
	public String nameWithoutExtension () {
		String name = file.getName();
		int dotIndex = name.lastIndexOf('.');
		if (dotIndex == -1) return name;
		return name.substring(0, dotIndex);
	}

	/** @return the path and filename without the extension, e.g. dir/dir2/file.png -> dir/dir2/file. backward slashes will be
	 *         returned as forward slashes. */
	public String pathWithoutExtension () {
		String path = file.getPath().replace('\\', '/');
		int dotIndex = path.lastIndexOf('.');
		if (dotIndex == -1) return path;
		return path.substring(0, dotIndex);
	}

	public FileType type () {
		return type;
	}

	/** Returns a java.io.File that represents this file handle. Note the returned file will only be usable for
	 * {@link FileType#Absolute} and {@link FileType#External} file handles. */
	public File file () {
		if (type == FileType.External) return new File(Gdx.files.getExternalStoragePath(), file.getPath());
		return file;
	}

	/** Returns a stream for reading this file as bytes.
	 * @throws GdxRuntimeException if the file handle represents a directory, doesn't exist, or could not be read. */
	public InputStream read () {
		if (type == FileType.Classpath || (type == FileType.Internal && !file.exists())
			|| (type == FileType.Local && !file.exists())) {
			InputStream input = FileHandle.class.getResourceAsStream("/" + file.getPath().replace('\\', '/'));
			if (input == null) throw new GdxRuntimeException("File not found: " + file + " (" + type + ")");
			return input;
		}
		try {
			return new FileInputStream(file());
		} catch (Exception ex) {
			if (file().isDirectory())
				throw new GdxRuntimeException("Cannot open a stream to a directory: " + file + " (" + type + ")", ex);
			throw new GdxRuntimeException("Error reading file: " + file + " (" + type + ")", ex);
		}
	}

	/** Returns a buffered stream for reading this file as bytes.
	 * @throws GdxRuntimeException if the file handle represents a directory, doesn't exist, or could not be read. */
	public BufferedInputStream read (int bufferSize) {
		return new BufferedInputStream(read(), bufferSize);
	}

	/** Returns a reader for reading this file as characters.
	 * @throws GdxRuntimeException if the file handle represents a directory, doesn't exist, or could not be read. */
	public Reader reader () {
		return new InputStreamReader(read());
	}

	/** Returns a reader for reading this file as characters.
	 * @throws GdxRuntimeException if the file handle represents a directory, doesn't exist, or could not be read. */
	public Reader reader (String charset) {
		try {
			return new InputStreamReader(read(), charset);
		} catch (UnsupportedEncodingException ex) {
			throw new GdxRuntimeException("Error reading file: " + this, ex);
		}
	}

	/** Returns a buffered reader for reading this file as characters.
	 * @throws GdxRuntimeException if the file handle represents a directory, doesn't exist, or could not be read. */
	public BufferedReader reader (int bufferSize) {
		return new BufferedReader(new InputStreamReader(read()), bufferSize);
	}

	/** Returns a buffered reader for reading this file as characters.
	 * @throws GdxRuntimeException if the file handle represents a directory, doesn't exist, or could not be read. */
	public BufferedReader reader (int bufferSize, String charset) {
		try {
			return new BufferedReader(new InputStreamReader(read(), charset), bufferSize);
		} catch (UnsupportedEncodingException ex) {
			throw new GdxRuntimeException("Error reading file: " + this, ex);
		}
	}

	/** Reads the entire file into a string using the platform's default charset.
	 * @throws GdxRuntimeException if the file handle represents a directory, doesn't exist, or could not be read. */
	public String readString () {
		return readString(null);
	}

	/** Reads the entire file into a string using the specified charset.
	 * @throws GdxRuntimeException if the file handle represents a directory, doesn't exist, or could not be read. */
	public String readString (String charset) {
		int fileLength = (int)length();
		if (fileLength == 0) fileLength = 512;
		StringBuilder output = new StringBuilder(fileLength);
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
			throw new GdxRuntimeException("Error reading layout file: " + this, ex);
		} finally {
			try {
				if (reader != null) reader.close();
			} catch (IOException ignored) {
			}
		}
		return output.toString();
	}

	/** Reads the entire file into a byte array.
	 * @throws GdxRuntimeException if the file handle represents a directory, doesn't exist, or could not be read. */
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
					int b = input.read();
					if (b == -1) break;
					// Grow buffer.
					byte[] newBuffer = new byte[buffer.length * 2];
					System.arraycopy(buffer, 0, newBuffer, 0, position);
					buffer = newBuffer;
					buffer[position++] = (byte)b;
				}
			}
		} catch (IOException ex) {
			throw new GdxRuntimeException("Error reading file: " + this, ex);
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
			throw new GdxRuntimeException("Error reading file: " + this, ex);
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
	 * @throws GdxRuntimeException if this file handle represents a directory, if it is a {@link FileType#Classpath} or
	 *        {@link FileType#Internal} file, or if it could not be written. */
	public OutputStream write (boolean append) {
		if (type == FileType.Classpath) throw new GdxRuntimeException("Cannot write to a classpath file: " + file);
		if (type == FileType.Internal) throw new GdxRuntimeException("Cannot write to an internal file: " + file);
		parent().mkdirs();
		try {
			return new FileOutputStream(file(), append);
		} catch (Exception ex) {
			if (file().isDirectory())
				throw new GdxRuntimeException("Cannot open a stream to a directory: " + file + " (" + type + ")", ex);
			throw new GdxRuntimeException("Error writing file: " + file + " (" + type + ")", ex);
		}
	}

	/** Reads the remaining bytes from the specified stream and writes them to this file. The stream is closed. Parent directories
	 * will be created if necessary.
	 * @param append If false, this file will be overwritten if it exists, otherwise it will be appended.
	 * @throws GdxRuntimeException if this file handle represents a directory, if it is a {@link FileType#Classpath} or
	 *        {@link FileType#Internal} file, or if it could not be written. */
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
			throw new GdxRuntimeException("Error stream writing to file: " + file + " (" + type + ")", ex);
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
	 * @throws GdxRuntimeException if this file handle represents a directory, if it is a {@link FileType#Classpath} or
	 *        {@link FileType#Internal} file, or if it could not be written. */
	public Writer writer (boolean append) {
		return writer(append, null);
	}

	/** Returns a writer for writing to this file. Parent directories will be created if necessary.
	 * @param append If false, this file will be overwritten if it exists, otherwise it will be appended.
	 * @param charset May be null to use the default charset.
	 * @throws GdxRuntimeException if this file handle represents a directory, if it is a {@link FileType#Classpath} or
	 *        {@link FileType#Internal} file, or if it could not be written. */
	public Writer writer (boolean append, String charset) {
		if (type == FileType.Classpath) throw new GdxRuntimeException("Cannot write to a classpath file: " + file);
		if (type == FileType.Internal) throw new GdxRuntimeException("Cannot write to an internal file: " + file);
		parent().mkdirs();
		try {
			FileOutputStream output = new FileOutputStream(file(), append);
			if (charset == null)
				return new OutputStreamWriter(output);
			else
				return new OutputStreamWriter(output, charset);
		} catch (IOException ex) {
			if (file().isDirectory())
				throw new GdxRuntimeException("Cannot open a stream to a directory: " + file + " (" + type + ")", ex);
			throw new GdxRuntimeException("Error writing file: " + file + " (" + type + ")", ex);
		}
	}

	/** Writes the specified string to the file using the default charset. Parent directories will be created if necessary.
	 * @param append If false, this file will be overwritten if it exists, otherwise it will be appended.
	 * @throws GdxRuntimeException if this file handle represents a directory, if it is a {@link FileType#Classpath} or
	 *        {@link FileType#Internal} file, or if it could not be written. */
	public void writeString (String string, boolean append) {
		writeString(string, append, null);
	}

	/** Writes the specified string to the file as UTF-8. Parent directories will be created if necessary.
	 * @param append If false, this file will be overwritten if it exists, otherwise it will be appended.
	 * @param charset May be null to use the default charset.
	 * @throws GdxRuntimeException if this file handle represents a directory, if it is a {@link FileType#Classpath} or
	 *        {@link FileType#Internal} file, or if it could not be written. */
	public void writeString (String string, boolean append, String charset) {
		Writer writer = null;
		try {
			writer = writer(append, charset);
			writer.write(string);
		} catch (Exception ex) {
			throw new GdxRuntimeException("Error writing file: " + file + " (" + type + ")", ex);
		} finally {
			try {
				if (writer != null) writer.close();
			} catch (Exception ignored) {
			}
		}
	}

	/** Writes the specified bytes to the file. Parent directories will be created if necessary.
	 * @param append If false, this file will be overwritten if it exists, otherwise it will be appended.
	 * @throws GdxRuntimeException if this file handle represents a directory, if it is a {@link FileType#Classpath} or
	 *        {@link FileType#Internal} file, or if it could not be written. */
	public void writeBytes (byte[] bytes, boolean append) {
		OutputStream output = write(append);
		try {
			output.write(bytes);
		} catch (IOException ex) {
			throw new GdxRuntimeException("Error writing file: " + file + " (" + type + ")", ex);
		} finally {
			try {
				output.close();
			} catch (IOException ignored) {
			}
		}
	}

	/** Writes the specified bytes to the file. Parent directories will be created if necessary.
	 * @param append If false, this file will be overwritten if it exists, otherwise it will be appended.
	 * @throws GdxRuntimeException if this file handle represents a directory, if it is a {@link FileType#Classpath} or
	 *        {@link FileType#Internal} file, or if it could not be written. */
	public void writeBytes (byte[] bytes, int offset, int length, boolean append) {
		OutputStream output = write(append);
		try {
			output.write(bytes, offset, length);
		} catch (IOException ex) {
			throw new GdxRuntimeException("Error writing file: " + file + " (" + type + ")", ex);
		} finally {
			try {
				output.close();
			} catch (IOException ignored) {
			}
		}
	}

	/** Returns the paths to the children of this directory. Returns an empty list if this file handle represents a file and not a
	 * directory. On the desktop, an {@link FileType#Internal} handle to a directory on the classpath will return a zero length
	 * array.
	 * @throws GdxRuntimeException if this file is an {@link FileType#Classpath} file. */
	public FileHandle[] list () {
		if (type == FileType.Classpath) throw new GdxRuntimeException("Cannot list a classpath directory: " + file);
		String[] relativePaths = file().list();
		if (relativePaths == null) return new FileHandle[0];
		FileHandle[] handles = new FileHandle[relativePaths.length];
		for (int i = 0, n = relativePaths.length; i < n; i++)
			handles[i] = child(relativePaths[i]);
		return handles;
	}

	/** Returns the paths to the children of this directory with the specified suffix. Returns an empty list if this file handle
	 * represents a file and not a directory. On the desktop, an {@link FileType#Internal} handle to a directory on the classpath
	 * will return a zero length array.
	 * @throws GdxRuntimeException if this file is an {@link FileType#Classpath} file. */
	public FileHandle[] list (String suffix) {
		if (type == FileType.Classpath) throw new GdxRuntimeException("Cannot list a classpath directory: " + file);
		String[] relativePaths = file().list();
		if (relativePaths == null) return new FileHandle[0];
		FileHandle[] handles = new FileHandle[relativePaths.length];
		int count = 0;
		for (int i = 0, n = relativePaths.length; i < n; i++) {
			String path = relativePaths[i];
			if (!path.endsWith(suffix)) continue;
			handles[count] = child(path);
			count++;
		}
		if (count < relativePaths.length) {
			FileHandle[] newHandles = new FileHandle[count];
			System.arraycopy(handles, 0, newHandles, 0, count);
			handles = newHandles;
		}
		return handles;
	}

	/** Returns true if this file is a directory. Always returns false for classpath files. On Android, an {@link FileType#Internal}
	 * handle to an empty directory will return false. On the desktop, an {@link FileType#Internal} handle to a directory on the
	 * classpath will return false. */
	public boolean isDirectory () {
		if (type == FileType.Classpath) return false;
		return file().isDirectory();
	}

	/** Returns a handle to the child with the specified name.
	 * @throws GdxRuntimeException if this file handle is a {@link FileType#Classpath} or {@link FileType#Internal} and the child
	 *        doesn't exist. */
	public FileHandle child (String name) {
		if (file.getPath().length() == 0) return new FileHandle(new File(name), type);
		return new FileHandle(new File(file, name), type);
	}

	/** Returns a handle to the sibling with the specified name.
	 * @throws GdxRuntimeException if this file handle is a {@link FileType#Classpath} or {@link FileType#Internal} and the sibling
	 *        doesn't exist, or this file is the root. */
	public FileHandle sibling (String name) {
		if (file.getPath().length() == 0) throw new GdxRuntimeException("Cannot get the sibling of the root.");
		return new FileHandle(new File(file.getParent(), name), type);
	}

	public FileHandle parent () {
		File parent = file.getParentFile();
		if (parent == null) {
			if (type == FileType.Absolute)
				parent = new File("/");
			else
				parent = new File("");
		}
		return new FileHandle(parent, type);
	}

	/** @throws GdxRuntimeException if this file handle is a {@link FileType#Classpath} or {@link FileType#Internal} file. */
	public void mkdirs () {
		if (type == FileType.Classpath) throw new GdxRuntimeException("Cannot mkdirs with a classpath file: " + file);
		if (type == FileType.Internal) throw new GdxRuntimeException("Cannot mkdirs with an internal file: " + file);
		file().mkdirs();
	}

	/** Returns true if the file exists. On Android, a {@link FileType#Classpath} or {@link FileType#Internal} handle to a directory
	 * will always return false. */
	public boolean exists () {
		switch (type) {
		case Internal:
			if (file.exists()) return true;
			// Fall through.
		case Classpath:
			return FileHandle.class.getResource("/" + file.getPath().replace('\\', '/')) != null;
		}
		return file().exists();
	}

	/** Deletes this file or empty directory and returns success. Will not delete a directory that has children.
	 * @throws GdxRuntimeException if this file handle is a {@link FileType#Classpath} or {@link FileType#Internal} file. */
	public boolean delete () {
		if (type == FileType.Classpath) throw new GdxRuntimeException("Cannot delete a classpath file: " + file);
		if (type == FileType.Internal) throw new GdxRuntimeException("Cannot delete an internal file: " + file);
		return file().delete();
	}

	/** Deletes this file or directory and all children, recursively.
	 * @throws GdxRuntimeException if this file handle is a {@link FileType#Classpath} or {@link FileType#Internal} file. */
	public boolean deleteDirectory () {
		if (type == FileType.Classpath) throw new GdxRuntimeException("Cannot delete a classpath file: " + file);
		if (type == FileType.Internal) throw new GdxRuntimeException("Cannot delete an internal file: " + file);
		return deleteDirectory(file());
	}
	
	/** Deletes all children of this directory, recursively.
	 * @throws GdxRuntimeException if this file handle is a {@link FileType#Classpath} or {@link FileType#Internal} file. */
	public void emptyDirectory () {
		emptyDirectory(false);
	}
	
	/** Deletes all children of this directory, recursively. Optionally preserving the folder structure.
	 * @throws GdxRuntimeException if this file handle is a {@link FileType#Classpath} or {@link FileType#Internal} file. */
	public void emptyDirectory (boolean preserveTree) {
		if (type == FileType.Classpath) throw new GdxRuntimeException("Cannot delete a classpath file: " + file);
		if (type == FileType.Internal) throw new GdxRuntimeException("Cannot delete an internal file: " + file);
		emptyDirectory(file(), preserveTree);
	}

	/** Copies this file or directory to the specified file or directory. If this handle is a file, then 1) if the destination is a
	 * file, it is overwritten, or 2) if the destination is a directory, this file is copied into it, or 3) if the destination
	 * doesn't exist, {@link #mkdirs()} is called on the destination's parent and this file is copied into it with a new name. If
	 * this handle is a directory, then 1) if the destination is a file, GdxRuntimeException is thrown, or 2) if the destination is
	 * a directory, this directory is copied into it recursively, overwriting existing files, or 3) if the destination doesn't
	 * exist, {@link #mkdirs()} is called on the destination and this directory is copied into it recursively.
	 * @throws GdxRuntimeException if the destination file handle is a {@link FileType#Classpath} or {@link FileType#Internal} file,
	 *        or copying failed. */
	public void copyTo (FileHandle dest) {
		boolean sourceDir = isDirectory();
		if (!sourceDir) {
			if (dest.isDirectory()) dest = dest.child(name());
			copyFile(this, dest);
			return;
		}
		if (dest.exists()) {
			if (!dest.isDirectory()) throw new GdxRuntimeException("Destination exists but is not a directory: " + dest);
		} else {
			dest.mkdirs();
			if (!dest.isDirectory()) throw new GdxRuntimeException("Destination directory cannot be created: " + dest);
		}
		if (!sourceDir) dest = dest.child(name());
		copyDirectory(this, dest);
	}

	/** Moves this file to the specified file, overwriting the file if it already exists.
	 * @throws GdxRuntimeException if the source or destination file handle is a {@link FileType#Classpath} or
	 *        {@link FileType#Internal} file. */
	public void moveTo (FileHandle dest) {
		if (type == FileType.Classpath) throw new GdxRuntimeException("Cannot move a classpath file: " + file);
		if (type == FileType.Internal) throw new GdxRuntimeException("Cannot move an internal file: " + file);
		copyTo(dest);
		delete();
		if (exists () && isDirectory()) deleteDirectory();
	}

	/** Returns the length in bytes of this file, or 0 if this file is a directory, does not exist, or the size cannot otherwise be
	 * determined. */
	public long length () {
		if (type == FileType.Classpath || (type == FileType.Internal && !file.exists())) {
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
	 * for {@link FileType#Classpath} files. On Android, zero is returned for {@link FileType#Internal} files. On the desktop, zero
	 * is returned for {@link FileType#Internal} files on the classpath. */
	public long lastModified () {
		return file().lastModified();
	}

	public String toString () {
		return file.getPath().replace('\\', '/');
	}

	static public FileHandle tempFile (String prefix) {
		try {
			return new FileHandle(File.createTempFile(prefix, null));
		} catch (IOException ex) {
			throw new GdxRuntimeException("Unable to create temp file.", ex);
		}
	}

	static public FileHandle tempDirectory (String prefix) {
		try {
			File file = File.createTempFile(prefix, null);
			if (!file.delete()) throw new IOException("Unable to delete temp file: " + file);
			if (!file.mkdir()) throw new IOException("Unable to create temp directory: " + file);
			return new FileHandle(file);
		} catch (IOException ex) {
			throw new GdxRuntimeException("Unable to create temp file.", ex);
		}
	}

	static private void emptyDirectory (File file, boolean preserveTree) {
		if (file.exists()) {
			File[] files = file.listFiles();
			if (files != null) {
				for (int i = 0, n = files.length; i < n; i++) {
					if (!files[i].isDirectory())
						files[i].delete();
					else if (preserveTree)
						emptyDirectory(files[i], true);
					else
						deleteDirectory(files[i]);
				}
			}
		}
	}

	static private boolean deleteDirectory (File file) {
		emptyDirectory(file, false);
		return file.delete();
	}

	static private void copyFile (FileHandle source, FileHandle dest) {
		try {
			dest.write(source.read(), false);
		} catch (Exception ex) {
			throw new GdxRuntimeException("Error copying source file: " + source.file + " (" + source.type + ")\n" //
				+ "To destination: " + dest.file + " (" + dest.type + ")", ex);
		}
	}

	static private void copyDirectory (FileHandle sourceDir, FileHandle destDir) {
		destDir.mkdirs();
		FileHandle[] files = sourceDir.list();
		for (int i = 0, n = files.length; i < n; i++) {
			FileHandle srcFile = files[i];
			FileHandle destFile = destDir.child(srcFile.name());
			if (srcFile.isDirectory())
				copyDirectory(srcFile, destFile);
			else
				copyFile(srcFile, destFile);
		}
	}
}
