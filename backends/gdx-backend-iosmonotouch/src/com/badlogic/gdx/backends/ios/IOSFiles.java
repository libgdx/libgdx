package com.badlogic.gdx.backends.ios;

import cli.System.Environment;
import cli.System.IO.Directory;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.files.FileHandle;

public class IOSFiles implements Files {
	static final String externalPath = Environment.GetFolderPath(Environment.SpecialFolder.wrap(Environment.SpecialFolder.MyDocuments));
	static final String localPath = Directory.GetParent(Environment.GetFolderPath(Environment.SpecialFolder.wrap(Environment.SpecialFolder.MyDocuments))).ToString() + "/Library/local";
	
	public IOSFiles() {
		new FileHandle(externalPath).mkdirs();
		new FileHandle(localPath).mkdirs();
	}
	
	@Override
	public FileHandle getFileHandle (String fileName, FileType type) {
		return new IOSFileHandle(fileName, type);
	}

	@Override
	public FileHandle classpath (String path) {
		return new IOSFileHandle(path, FileType.Classpath);
	}

	@Override
	public FileHandle internal (String path) {
		return new IOSFileHandle(path, FileType.Internal);
	}

	@Override
	public FileHandle external (String path) {
		return new IOSFileHandle(path, FileType.External);
	}

	@Override
	public FileHandle absolute (String path) {
		return new IOSFileHandle(path, FileType.Absolute);
	}

	@Override
	public FileHandle local (String path) {
		return new IOSFileHandle(path, FileType.Local);
	}

	@Override
	public String getExternalStoragePath() {
		return externalPath;
	}

	@Override
	public boolean isExternalStorageAvailable() {
		return true;
	}

	@Override
	public String getLocalStoragePath() {
		return localPath;
	}

	@Override
	public boolean isLocalStorageAvailable() {
		return true;
	}
}
