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

	public File file () {
		if (type == FileType.External) return new File(IOSFiles.externalPath, file.getPath());
		if (type == FileType.Local) return new File(IOSFiles.localPath, file.getPath());
		return file;
	}
}
