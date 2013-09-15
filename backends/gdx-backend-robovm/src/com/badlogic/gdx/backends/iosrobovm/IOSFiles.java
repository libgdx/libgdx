package com.badlogic.gdx.backends.iosrobovm;

import org.robovm.cocoatouch.foundation.NSBundle;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.files.FileHandle;

public class IOSFiles implements Files {
	// TODO: Use NSSearchPathForDirectoriesInDomains instead?
	// $HOME should point to the app root dir.
	static final String appDir = System.getenv("HOME");
	static final String externalPath = appDir + "/Documents";
	static final String localPath = appDir + "/Library/local";
	static final String internalPath = NSBundle.getMainBundle().getBundlePath();
	
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