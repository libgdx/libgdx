
package com.badlogic.gdx.files;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.utils.GdxRuntimeException;

/**
 * A FileHandle intended to be subclassed for the purpose of implemented {@link #read()} and/or {@link #write(boolean)}. Methods
 * that would modify a file instead throw UnsupportedOperationException.
 */
public abstract class FileHandleStream extends FileHandle {
	public FileHandleStream (String path) {
		super(new File(path), FileType.Absolute);
	}

	public FileHandle child (String name) {
		throw new UnsupportedOperationException();
	}

	public FileHandle parent () {
		throw new UnsupportedOperationException();
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

	public boolean isDirectory () {
		return false;
	}

	public long length () {
		return 0;
	}

	public boolean exists () {
		return true;
	}

	public InputStream read () {
		throw new UnsupportedOperationException();
	}

	public OutputStream write (boolean overwrite) {
		throw new UnsupportedOperationException();
	}

	public FileHandle[] list () {
		throw new UnsupportedOperationException();
	}

	public void mkdirs () {
		throw new UnsupportedOperationException();
	}

	public boolean delete () {
		throw new UnsupportedOperationException();
	}

	public boolean deleteDirectory () {
		throw new UnsupportedOperationException();
	}

	public void copyTo (FileHandle dest) {
		throw new UnsupportedOperationException();
	}

	public void moveTo (FileHandle dest) {
		throw new UnsupportedOperationException();
	}
}
